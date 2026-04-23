package com.realtimepricetracker.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realtimepricetracker.domain.config.DomainConstants
import com.realtimepricetracker.domain.entities.Stock
import com.realtimepricetracker.domain.usecases.GetInitialStocksUseCase
import com.realtimepricetracker.domain.usecases.ManageConnectionUseCase
import com.realtimepricetracker.domain.usecases.SendPriceUpdateUseCase
import com.realtimepricetracker.domain.usecases.SubscribeToPriceUpdatesUseCase
import com.realtimepricetracker.presentation.state.PriceTrackerUiState
import com.realtimepricetracker.presentation.state.StockUiModel
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

/**
 * ViewModel for the Price Tracker screen.
 * Implements MVVM pattern with StateFlow for reactive UI state management.
 */
class PriceTrackerViewModel(
    private val getInitialStocksUseCase: GetInitialStocksUseCase,
    private val subscribeToPriceUpdatesUseCase: SubscribeToPriceUpdatesUseCase,
    private val sendPriceUpdateUseCase: SendPriceUpdateUseCase,
    private val manageConnectionUseCase: ManageConnectionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PriceTrackerUiState())
    val uiState: StateFlow<PriceTrackerUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        // Initialize with stock data
        viewModelScope.launch {
            val result = getInitialStocksUseCase()
            result.onSuccess { stocks ->
                _uiState.update { state ->
                    state.copy(
                        stocks = stocks
                            .map { it.toUiModel() }
                            .sortedByDescending { it.price }
                    )
                }
            }
        }

        // Subscribe to price updates
        subscribeToPriceUpdatesUseCase()
            .onEach { result ->
                result.onSuccess { stock ->
                    updateStockData(stock)
                }
            }
            .launchIn(viewModelScope)

        // Observe connection state
        manageConnectionUseCase.observeConnectionState()
            .onEach { connected ->
                _uiState.update { it.copy(isConnected = connected) }
            }
            .launchIn(viewModelScope)
    }

    fun toggleFeed() {
        val current = _uiState.value
        if (current.isRunning) {
            stopFeed()
        } else {
            startFeed()
        }
    }

    private fun startFeed() {
        viewModelScope.launch {
            manageConnectionUseCase.connect()
        }
        timerJob = viewModelScope.launch {
            while (isActive) {
                DomainConstants.STOCK_SYMBOLS.forEach { symbol ->
                    val prevPrice = _uiState.value.stocks.find { it.symbol == symbol }?.price ?: 100.0
                    val change = Random.nextDouble(-5.0, 5.0)
                    val newPrice = prevPrice + change
                    val stock = Stock(
                        symbol = symbol,
                        price = newPrice,
                        change = change,
                        changePercentage = if (prevPrice != 0.0) (change / prevPrice) * 100 else 0.0
                    )
                    sendPriceUpdateUseCase(stock)
                }
                delay(2000)
            }
        }
        _uiState.update { it.copy(isRunning = true) }
    }

    private fun stopFeed() {
        timerJob?.cancel()
        viewModelScope.launch {
            manageConnectionUseCase.disconnect()
        }
        _uiState.update { it.copy(isRunning = false) }
    }

    private fun updateStockData(stock: Stock) {
        _uiState.update { state ->
            val updatedStocks = state.stocks
                .map {
                    if (it.symbol == stock.symbol) {
                        it.copy(
                            price = stock.price,
                            change = stock.change,
                            changePercentage = stock.changePercentage,
                            flashColor = if (stock.change > 0) Color.Green else Color.Red
                        )
                    } else {
                        it
                    }
                }
                .sortedByDescending { it.price }
            state.copy(stocks = updatedStocks)
        }

        // Reset flash color after delay
        viewModelScope.launch {
            delay(1000)
            _uiState.update { state ->
                val resetStocks = state.stocks.map {
                    if (it.symbol == stock.symbol) it.copy(flashColor = null) else it
                }
                state.copy(stocks = resetStocks)
            }
        }
    }

    fun toggleDarkMode() {
        _uiState.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    override fun onCleared() {
        stopFeed()
        super.onCleared()
    }

    private fun Stock.toUiModel(): StockUiModel = StockUiModel(
        symbol = symbol,
        price = price,
        change = change,
        changePercentage = changePercentage
    )
}

