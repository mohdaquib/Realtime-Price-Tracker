package com.realtimepricetracker.domain.usecases

import com.realtimepricetracker.domain.entities.PriceAlert
import com.realtimepricetracker.domain.repositories.AlertRepository

class AddAlertUseCase(private val repository: AlertRepository) {
    suspend operator fun invoke(alert: PriceAlert) = repository.add(alert)
}
