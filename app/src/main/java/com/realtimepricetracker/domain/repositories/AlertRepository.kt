package com.realtimepricetracker.domain.repositories

import com.realtimepricetracker.domain.entities.PriceAlert
import kotlinx.coroutines.flow.Flow

interface AlertRepository {
    fun observeAlerts(): Flow<List<PriceAlert>>
    suspend fun add(alert: PriceAlert)
    suspend fun remove(id: String)
}
