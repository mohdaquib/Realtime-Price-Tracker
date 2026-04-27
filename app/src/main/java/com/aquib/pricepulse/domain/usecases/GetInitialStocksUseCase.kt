package com.aquib.pricepulse.domain.usecases

import com.aquib.pricepulse.domain.config.DomainConstants
import com.aquib.pricepulse.domain.entities.Stock
import com.aquib.pricepulse.domain.repositories.PriceRepository
import javax.inject.Inject

class GetInitialStocksUseCase @Inject constructor(private val priceRepository: PriceRepository) {
    suspend operator fun invoke(): Result<List<Stock>> = priceRepository.getStocks(DomainConstants.STOCK_SYMBOLS)
}

