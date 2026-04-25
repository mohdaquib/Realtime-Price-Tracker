package com.realtimepricetracker.domain.usecases

import com.realtimepricetracker.domain.entities.Stock
import com.realtimepricetracker.domain.repositories.PriceRepository

class GetCachedStocksUseCase(private val repository: PriceRepository) {
    suspend operator fun invoke(): Pair<List<Stock>, Long?> = repository.getCachedStocks()
}
