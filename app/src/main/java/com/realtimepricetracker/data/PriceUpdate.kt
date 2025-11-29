package com.realtimepricetracker.data

data class PriceUpdate(val symbol: String, val price: Double, val change: Double)