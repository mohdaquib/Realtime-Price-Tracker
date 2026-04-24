package com.realtimepricetracker.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realtimepricetracker.domain.config.DomainConstants
import com.realtimepricetracker.domain.entities.Stock
import com.realtimepricetracker.domain.usecases.AddToWatchlistUseCase
import com.realtimepricetracker.domain.usecases.GetInitialStocksUseCase
import com.realtimepricetracker.domain.usecases.ManageConnectionUseCase
import com.realtimepricetracker.domain.usecases.ObserveWatchlistUseCase
import com.realtimepricetracker.domain.usecases.RemoveFromWatchlistUseCase
import com.realtimepricetracker.domain.usecases.SubscribeToPriceUpdatesUseCase
import com.realtimepricetracker.domain.usecases.WatchSymbolsUseCase
import com.realtimepricetracker.presentation.state.AppTab
import com.realtimepricetracker.presentation.state.PriceTrackerUiState
import com.realtimepricetracker.presentation.state.StockUiModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PriceTrackerViewModel(
    private val getInitialStocksUseCase: GetInitialStocksUseCase,
    private val subscribeToPriceUpdatesUseCase: SubscribeToPriceUpdatesUseCase,
    private val watchSymbolsUseCase: WatchSymbolsUseCase,
    private val manageConnectionUseCase: ManageConnectionUseCase,
    private val observeWatchlistUseCase: ObserveWatchlistUseCase,
    private val addToWatchlistUseCase: AddToWatchlistUseCase,
    private val removeFromWatchlistUseCase: RemoveFromWatchlistUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PriceTrackerUiState())
    val uiState: StateFlow<PriceTrackerUiState> = _uiState.asStateFlow()

    private val priceHistories = mutableMapOf<String, ArrayDeque<Float>>()

    companion object {
        private const val MAX_HISTORY_SIZE = 50
    }

    init {
        loadInitialStocks()

        subscribeToPriceUpdatesUseCase()
            .onEach { result ->
                result.onSuccess { stock ->
                    updateStockData(stock)
                }.onFailure { error ->
                    _uiState.update { it.copy(error = "Update error: ${error.message}") }
                }
            }
            .launchIn(viewModelScope)

        manageConnectionUseCase.observeConnectionState()
            .onEach { connected ->
                _uiState.update { it.copy(isConnected = connected) }
                if (connected && _uiState.value.isRunning) {
                    watchSymbolsUseCase.subscribe(DomainConstants.STOCK_SYMBOLS)
                }
            }
            .launchIn(viewModelScope)

        observeWatchlistUseCase()
            .onEach { watchlist -> _uiState.update { it.copy(watchlist = watchlist) } }
            .launchIn(viewModelScope)
    }

    private fun loadInitialStocks() {
        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val result = getInitialStocksUseCase()
            result.onSuccess { stocks ->
                stocks.forEach { stock ->
                    priceHistories.getOrPut(stock.symbol) { ArrayDeque() }
                        .addLast(stock.price.toFloat())
                }
                _uiState.update { state ->
                    state.copy(
                        stocks = stocks
                            .map { it.toUiModel() }
                            .sortedByDescending { it.price },
                        loading = false
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(loading = false, error = "Failed to load initial stocks: ${error.message}")
                }
            }
        }
    }

    fun toggleFeed() {
        if (_uiState.value.isRunning) stopFeed() else startFeed()
    }

    private fun startFeed() {
        _uiState.update { it.copy(isRunning = true, error = null) }
        viewModelScope.launch {
            manageConnectionUseCase.connect()
            watchSymbolsUseCase.subscribe(DomainConstants.STOCK_SYMBOLS)
        }
    }

    private fun stopFeed() {
        _uiState.update { it.copy(isRunning = false) }
        viewModelScope.launch {
            watchSymbolsUseCase.unsubscribe(DomainConstants.STOCK_SYMBOLS)
            manageConnectionUseCase.disconnect()
        }
    }

    private fun updateStockData(stock: Stock) {
        val history = priceHistories.getOrPut(stock.symbol) { ArrayDeque() }
        history.addLast(stock.price.toFloat())
        if (history.size > MAX_HISTORY_SIZE) history.removeFirst()
        val snapshot = history.toList()

        _uiState.update { state ->
            val updatedStocks = state.stocks
                .map {
                    if (it.symbol == stock.symbol) {
                        it.copy(
                            price = stock.price,
                            change = stock.change,
                            changePercentage = stock.changePercentage,
                            flashColor = if (stock.change >= 0) Color.Green else Color.Red,
                            priceHistory = snapshot
                        )
                    } else {
                        it
                    }
                }
                .sortedByDescending { it.price }
            state.copy(stocks = updatedStocks)
        }

        viewModelScope.launch {
            delay(500)
            _uiState.update { state ->
                val resetStocks = state.stocks.map {
                    if (it.symbol == stock.symbol) it.copy(flashColor = null) else it
                }
                state.copy(stocks = resetStocks)
            }
        }
    }

    fun selectStock(symbol: String?) {
        _uiState.update { it.copy(selectedSymbol = symbol) }
    }

    fun toggleWatchlist(symbol: String) {
        viewModelScope.launch {
            if (_uiState.value.watchlist.contains(symbol)) {
                removeFromWatchlistUseCase(symbol)
            } else {
                addToWatchlistUseCase(symbol)
            }
        }
    }

    fun setActiveTab(tab: AppTab) {
        _uiState.update { it.copy(activeTab = tab, selectedSymbol = null) }
    }

    fun toggleDarkMode() {
        _uiState.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun refresh() {
        loadInitialStocks()
    }

    override fun onCleared() {
        stopFeed()
        super.onCleared()
    }

    private fun Stock.toUiModel(): StockUiModel = StockUiModel(
        symbol = symbol,
        price = price,
        change = change,
        changePercentage = changePercentage,
        priceHistory = priceHistories[symbol]?.toList() ?: emptyList()
    )
}
