package com.realtimepricetracker.domain.usecases

import com.realtimepricetracker.domain.repositories.AlertRepository

class RemoveAlertUseCase(private val repository: AlertRepository) {
    suspend operator fun invoke(id: String) = repository.remove(id)
}
