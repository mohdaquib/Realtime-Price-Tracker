package com.realtimepricetracker.di

import com.google.gson.Gson
import com.realtimepricetracker.data.datasource.WebSocketDataSource
import com.realtimepricetracker.data.repositories.ConnectionRepositoryImpl
import com.realtimepricetracker.data.repositories.PriceRepositoryImpl
import com.realtimepricetracker.domain.repositories.ConnectionRepository
import com.realtimepricetracker.domain.repositories.PriceRepository
import com.realtimepricetracker.domain.usecases.GetInitialStocksUseCase
import com.realtimepricetracker.domain.usecases.ManageConnectionUseCase
import com.realtimepricetracker.domain.usecases.SendPriceUpdateUseCase
import com.realtimepricetracker.domain.usecases.SubscribeToPriceUpdatesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Service Locator for dependency injection.
 * A simple singleton-based DI container for managing application dependencies.
 */
object AppFactory {
    private val appScope = CoroutineScope(SupervisorJob())
    private val gson = Gson()

    // Data sources
    private val webSocketDataSource by lazy {
        WebSocketDataSource(appScope)
    }

    // Repositories
    val priceRepository: PriceRepository by lazy {
        PriceRepositoryImpl(webSocketDataSource, gson)
    }

    val connectionRepository: ConnectionRepository by lazy {
        ConnectionRepositoryImpl(webSocketDataSource)
    }

    // Use cases
    val getInitialStocksUseCase by lazy {
        GetInitialStocksUseCase(priceRepository)
    }

    val subscribeToPriceUpdatesUseCase by lazy {
        SubscribeToPriceUpdatesUseCase(priceRepository)
    }

    val sendPriceUpdateUseCase by lazy {
        SendPriceUpdateUseCase(priceRepository)
    }

    val manageConnectionUseCase by lazy {
        ManageConnectionUseCase(connectionRepository)
    }
}

