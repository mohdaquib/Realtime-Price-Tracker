package com.aquib.pricepulse.domain.usecases

import com.aquib.pricepulse.domain.entities.Stock
import com.aquib.pricepulse.domain.repositories.PriceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for subscribing to real-time price updates.
 */
class SubscribeToPriceUpdatesUseCase @Inject constructor(private val priceRepository: PriceRepository) {
    operator fun invoke(): Flow<Result<Stock>> {
        return priceRepository.subscribeToPriceUpdates()
    }
}

