package com.aquib.pricepulse.data.datasource

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.aquib.pricepulse.data.config.DataConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.Collections
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class WebSocketDataSource(
    private val scope: CoroutineScope,
    context: Context,
) {
    private val appContext = context.applicationContext

    // pingInterval tells OkHttp to send a WebSocket ping frame every 30 s.
    // If the server doesn't respond with a pong, OkHttp closes the socket
    // and fires onFailure — which triggers our reconnect path.
    private val client = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)   // 0 = no read timeout (streaming connection)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    // @Volatile makes the reference visible across threads without a full lock.
    // OkHttp callbacks fire on OkHttp's thread pool; disconnect() fires on the
    // main/scope thread. Visibility is sufficient here because atomicity of the
    // read-modify-write on the reference itself is guarded by isConnecting below.
    @Volatile private var webSocket: WebSocket? = null

    // Guards against creating two sockets simultaneously.
    // compareAndSet(false, true) is atomic — only one thread can "win" the connect race.
    private val isConnecting = AtomicBoolean(false)

    // True when the user wants the feed running (set by connect(), cleared by disconnect()).
    // Prevents reconnect loops after an intentional stop.
    private val shouldReconnect = AtomicBoolean(false)

    // Tracks how many consecutive failed attempts have happened so we can back off.
    private val reconnectAttempts = AtomicInteger(0)

    // The currently pending reconnect coroutine. Cancel it before scheduling a new one.
    @Volatile private var reconnectJob: Job? = null

    // Symbols currently subscribed. Needed to re-send subscribe messages on reconnect
    // because the new WebSocket connection has no memory of the previous session.
    // Access via synchronized(subscribedSymbols) before iteration.
    private val subscribedSymbols: MutableSet<String> =
        Collections.synchronizedSet(mutableSetOf())

    private val _connectionState = MutableStateFlow(false)
    val connectionState: StateFlow<Boolean> = _connectionState

    // replay = 0: messages are live events, not state. A collector that starts
    // after a trade message should NOT receive that old trade as if it just happened.
    // extraBufferCapacity = 64: lets the OkHttp thread emit without suspending even
    // if the downstream coroutine hasn't processed the previous message yet.
    private val _receivedMessages = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 64
    )
    val receivedMessages: SharedFlow<String> = _receivedMessages

    init {
        registerNetworkCallback()
    }

    // ── Public API ────────────────────────────────────────────────────────────

    fun connect() {
        shouldReconnect.set(true)
        // Both guards: already open OR already in the middle of a handshake.
        if (_connectionState.value || isConnecting.get()) return
        connectInternal()
    }

    fun subscribe(symbol: String) {
        subscribedSymbols.add(symbol)
        send(String.format(DataConstants.SUBSCRIBE_MESSAGE, symbol))
    }

    fun unsubscribe(symbol: String) {
        subscribedSymbols.remove(symbol)
        send(String.format(DataConstants.UNSUBSCRIBE_MESSAGE, symbol))
    }

    fun subscribeMultiple(symbols: List<String>) = symbols.forEach { subscribe(it) }
    fun unsubscribeMultiple(symbols: List<String>) = symbols.forEach { unsubscribe(it) }

    fun disconnect() {
        shouldReconnect.set(false)   // must happen before close() to block onClosed reconnect
        reconnectJob?.cancel()
        reconnectJob = null
        reconnectAttempts.set(0)
        subscribedSymbols.clear()
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        _connectionState.value = false
        isConnecting.set(false)
    }

    fun isConnected(): Boolean = _connectionState.value

    // ── Internal connection logic ─────────────────────────────────────────────

    private fun connectInternal() {
        // Atomic check-and-set: only one caller can proceed even under concurrent calls.
        if (!isConnecting.compareAndSet(false, true)) return

        val request = Request.Builder().url(DataConstants.WS_URL).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionState.value = true
                isConnecting.set(false)
                reconnectAttempts.set(0)

                // Re-subscribe immediately on the same callback thread so there is
                // no window where we are connected but receiving nothing.
                // The ViewModel will also call subscribe() asynchronously when it
                // observes the state change; duplicate subscribe messages are harmless.
                val snapshot = synchronized(subscribedSymbols) { subscribedSymbols.toList() }
                snapshot.forEach { symbol ->
                    webSocket.send(String.format(DataConstants.SUBSCRIBE_MESSAGE, symbol))
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                scope.launch { _receivedMessages.emit(text) }
            }

            // Server sent a Close frame. We must respond with our own Close frame
            // to complete the closing handshake. onClosed fires after that.
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
            }

            // Clean close — both sides have exchanged Close frames.
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                handleDisconnect()
            }

            // Unclean failure (network drop, server crash, ping timeout, etc.).
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                handleDisconnect()
            }
        })
    }

    private fun handleDisconnect() {
        _connectionState.value = false
        isConnecting.set(false)
        if (shouldReconnect.get()) scheduleReconnect()
    }

    private fun scheduleReconnect() {
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            val delayMs = reconnectDelay()
            delay(delayMs)
            if (shouldReconnect.get() && !_connectionState.value) {
                reconnectAttempts.incrementAndGet()
                connectInternal()
            }
        }
    }

    // Exponential backoff: 1 s → 2 s → 4 s → 8 s → 16 s → 32 s → 64 s (capped).
    // Jitter (0–1 s) spreads reconnect storms when many clients lose connectivity
    // at the same time (e.g., after a network outage).
    private fun reconnectDelay(): Long {
        val attempt = reconnectAttempts.get().coerceAtMost(6)
        val exponential = 1_000L shl attempt
        val jitter = (0..1_000).random().toLong()
        return exponential + jitter
    }

    private fun send(message: String) {
        // Returns false silently if the socket isn't open yet. Symbols added while
        // connecting are safe: they're in subscribedSymbols and will be resent in onOpen.
        webSocket?.send(message)
    }

    // ── Network monitoring ────────────────────────────────────────────────────

    private fun registerNetworkCallback() {
        try {
            val cm = appContext.getSystemService(ConnectivityManager::class.java)
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            cm.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    // Network came back. If we should be connected, reconnect now
                    // with a fresh backoff instead of waiting for the timer to fire.
                    if (shouldReconnect.get() && !_connectionState.value && !isConnecting.get()) {
                        reconnectJob?.cancel()
                        reconnectAttempts.set(0)
                        connectInternal()
                    }
                }

                override fun onLost(network: Network) {
                    // No point firing the pending reconnect timer — there's no network.
                    // It will be rescheduled when onAvailable fires.
                    reconnectJob?.cancel()
                }
            })
        } catch (_: Exception) {
            // Network monitoring is best-effort. If it fails (e.g., permissions
            // stripped by manufacturer), reconnect still works via onFailure.
        }
    }
}

