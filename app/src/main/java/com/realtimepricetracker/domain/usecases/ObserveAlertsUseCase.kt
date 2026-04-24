package com.realtimepricetracker.domain.usecases

import com.realtimepricetracker.domain.entities.PriceAlert
import com.realtimepricetracker.domain.repositories.AlertRepository
import kotlinx.coroutines.flow.Flow

class ObserveAlertsUseCase(private val repository: AlertRepository) {
    operator fun invoke(): Flow<List<PriceAlert>> = repository.observeAlerts()
}
