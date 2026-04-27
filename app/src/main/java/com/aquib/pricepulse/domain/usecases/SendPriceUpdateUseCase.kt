package com.aquib.pricepulse.domain.usecases

import com.aquib.pricepulse.domain.entities.Stock
import com.aquib.pricepulse.domain.repositories.PriceRepository

/**
 * Use case for sending a price update through the network.
 */
class SendPriceUpdateUseCase(private val priceRepository: PriceRepository) {
    suspend operator fun invoke(stock: Stock): Result<Unit> {
        return priceRepository.sendPriceUpdate(stock)
    }
}


