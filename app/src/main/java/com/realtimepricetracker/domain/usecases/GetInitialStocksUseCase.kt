package com.realtimepricetracker.domain.usecases

import com.realtimepricetracker.domain.config.DomainConstants
import com.realtimepricetracker.domain.entities.Stock
import com.realtimepricetracker.domain.repositories.PriceRepository

/**
 * Use case for retrieving initial stock data for all configured symbols.
 */
class GetInitialStocksUseCase(private val priceRepository: PriceRepository) {
    suspend operator fun invoke(): Result<List<Stock>> {
        return priceRepository.getStocks(DomainConstants.STOCK_SYMBOLS)
    }
}


