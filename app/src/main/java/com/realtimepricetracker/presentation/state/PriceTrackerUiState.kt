package com.realtimepricetracker.presentation.state

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.realtimepricetracker.domain.entities.PriceAlert

enum class AppTab { MARKETS, WATCHLIST }

@Stable
data class StockUiModel(
    val symbol: String,
    val price: Double,
    val change: Double,
    val changePercentage: Double = 0.0,
    val flashColor: Color? = null,
    val priceHistory: List<Float> = emptyList()
)

@Stable
data class PriceTrackerUiState(
    val stocks: List<StockUiModel> = emptyList(),
    val watchlist: Set<String> = emptySet(),
    val activeTab: AppTab = AppTab.MARKETS,
    val isConnected: Boolean = false,
    val isRunning: Boolean = false,
    val isDarkMode: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null,
    val selectedSymbol: String? = null,
    val alerts: List<PriceAlert> = emptyList(),
    val showAlertDialogForSymbol: String? = null
)
