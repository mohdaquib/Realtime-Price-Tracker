package com.realtimepricetracker.domain.usecases

import com.realtimepricetracker.domain.entities.Stock
import com.realtimepricetracker.domain.repositories.PriceRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for subscribing to real-time price updates.
 */
class SubscribeToPriceUpdatesUseCase(private val priceRepository: PriceRepository) {
    operator fun invoke(): Flow<Result<Stock>> {
        return priceRepository.subscribeToPriceUpdates()
    }
}

