package com.realtimepricetracker.domain.usecases

import com.realtimepricetracker.domain.entities.Stock
import com.realtimepricetracker.domain.repositories.PriceRepository

/**
 * Use case for sending a price update through the network.
 */
class SendPriceUpdateUseCase(private val priceRepository: PriceRepository) {
    suspend operator fun invoke(stock: Stock): Result<Unit> {
        return priceRepository.sendPriceUpdate(stock)
    }
}

