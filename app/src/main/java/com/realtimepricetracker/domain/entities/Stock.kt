package com.realtimepricetracker.domain.entities

/**
 * Domain model representing a stock with its current price information.
 * This is the core business entity independent of UI or data layer concerns.
 */
data class Stock(
    val symbol: String,
    val price: Double,
    val change: Double,
    val changePercentage: Double = 0.0
)

