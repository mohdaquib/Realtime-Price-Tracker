package com.realtimepricetracker.data.repositories

import com.realtimepricetracker.data.datasource.WebSocketDataSource
import com.realtimepricetracker.data.dto.PriceUpdateDto
import com.realtimepricetracker.data.dto.toDto
import com.realtimepricetracker.domain.config.DomainConstants
import com.realtimepricetracker.domain.entities.Stock
import com.realtimepricetracker.domain.repositories.PriceRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.random.Random

/**
 * Implementation of PriceRepository using WebSocket data source.
 */
class PriceRepositoryImpl(
    private val webSocketDataSource: WebSocketDataSource,
    private val gson: Gson
) : PriceRepository {

    override suspend fun getStocks(symbols: List<String>): Result<List<Stock>> {
        return try {
            val stocks = symbols.map { symbol ->
                Stock(
                    symbol = symbol,
                    price = 100.0 + Random.nextDouble(0.0, 200.0),
                    change = 0.0,
                    changePercentage = 0.0
                )
            }
            Result.success(stocks.sortedByDescending { it.price })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun subscribeToPriceUpdates(): Flow<Result<Stock>> {
        return webSocketDataSource.receivedMessages.map { message ->
            try {
                val dto = gson.fromJson(message, PriceUpdateDto::class.java)
                Result.success(dto.toDomain())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun sendPriceUpdate(stock: Stock): Result<Unit> {
        return try {
            val dto = stock.toDto()
            val json = gson.toJson(dto)
            webSocketDataSource.send(json)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


