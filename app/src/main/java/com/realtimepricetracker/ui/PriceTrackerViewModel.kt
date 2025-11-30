package com.realtimepricetracker.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realtimepricetracker.Constants
import com.realtimepricetracker.data.PriceUpdate
import com.realtimepricetracker.network.WebsocketManager
import com.realtimepricetracker.utils.toJson
import com.realtimepricetracker.utils.toPriceUpdate
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class PriceTrackerViewModel(
    private val websocketManager: WebsocketManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            websocketManager.receivedMessages.collect { message ->
                val update = message.toPriceUpdate()
                updateStockData(update)
            }
        }
        websocketManager.connectionState.onEach { connected ->
            _uiState.update { it.copy(isConnected = connected) }
        }.launchIn(viewModelScope)
    }

    private fun startFeed() {
        websocketManager.connect()
        timerJob = viewModelScope.launch {
            while (isActive) {
                Constants.STOCK_SYMBOLS.forEach { symbol ->
                    val prevPrice = _uiState.value.symbols.find { it.symbol == symbol }?.price ?: 100.0
                    val change = Random.nextDouble(-5.0, 5.0)
                    val newPrice = prevPrice + change
                    val update = PriceUpdate(symbol, newPrice, change)
                    websocketManager.send(update.toJson())
                }
                delay(2000)
            }
        }
        _uiState.update { it.copy(isRunning = true) }
    }

    private fun stopFeed() {
        timerJob?.cancel()
        websocketManager.disconnect()
        _uiState.update { it.copy(isRunning = false) }
    }

    private fun updateStockData(update: PriceUpdate) {
        _uiState.update { state ->
            val updatedSymbols =
                state.symbols
                    .map {
                        if (it.symbol == update.symbol) {
                            it.copy(
                                price = update.price,
                                change = update.change,
                                flashColor =
                                    if (update.change > 0) {
                                        Color.Green
                                    } else {
                                        Color.Red
                                    },
                            )
                        } else {
                            it
                        }
                    }.sortedByDescending { it.price }
            state.copy(symbols = updatedSymbols)
        }
        viewModelScope.launch {
            delay(1000)
            _uiState.update { state ->
                val resetSymbols = state.symbols.map { if (it.symbol == update.symbol) it.copy(flashColor = null) else it }
                state.copy(symbols = resetSymbols)
            }
        }
    }

    override fun onCleared() {
        stopFeed()
        super.onCleared()
    }
}

data class UiState(
    val symbols: List<StockData> =
        Constants.STOCK_SYMBOLS
            .map {
                StockData(
                    symbol = it,
                    100.0 + Random.nextDouble(0.0, 200.0),
                    0.0,
                )
            }.sortedByDescending { it.price },
    val isConnected: Boolean = false,
    val isRunning: Boolean = false,
)

data class StockData(
    val symbol: String,
    val price: Double,
    val change: Double,
    val flashColor: Color? = null,
)
