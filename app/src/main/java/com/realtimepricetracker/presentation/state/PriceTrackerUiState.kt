package com.realtimepricetracker.presentation.state

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Stable

/**
 * Represents a single stock item in the UI state.
 */
@Stable
data class StockUiModel(
    val symbol: String,
    val price: Double,
    val change: Double,
    val changePercentage: Double = 0.0,
    val flashColor: Color? = null
)

/**
 * Main UI state for the price tracker screen.
 * This represents everything needed to render the UI.
 */
@Stable
data class PriceTrackerUiState(
    val stocks: List<StockUiModel> = emptyList(),
    val isConnected: Boolean = false,
    val isRunning: Boolean = false,
    val isDarkMode: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null
)

