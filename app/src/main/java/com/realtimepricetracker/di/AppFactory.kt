package com.realtimepricetracker.di

import android.content.Context
import com.google.gson.Gson
import com.realtimepricetracker.data.datasource.FinnhubRestDataSource
import com.realtimepricetracker.data.datasource.WebSocketDataSource
import com.realtimepricetracker.data.local.AlertDataSource
import com.realtimepricetracker.data.local.WatchlistDataSource
import com.realtimepricetracker.data.notification.NotificationHelper
import com.realtimepricetracker.data.repositories.AlertRepositoryImpl
import com.realtimepricetracker.data.repositories.ConnectionRepositoryImpl
import com.realtimepricetracker.data.repositories.PriceRepositoryImpl
import com.realtimepricetracker.data.repositories.WatchlistRepositoryImpl
import com.realtimepricetracker.domain.repositories.AlertRepository
import com.realtimepricetracker.domain.repositories.ConnectionRepository
import com.realtimepricetracker.domain.repositories.PriceRepository
import com.realtimepricetracker.domain.repositories.WatchlistRepository
import com.realtimepricetracker.domain.usecases.AddAlertUseCase
import com.realtimepricetracker.domain.usecases.AddToWatchlistUseCase
import com.realtimepricetracker.domain.usecases.CheckAlertsUseCase
import com.realtimepricetracker.domain.usecases.GetInitialStocksUseCase
import com.realtimepricetracker.domain.usecases.ManageConnectionUseCase
import com.realtimepricetracker.domain.usecases.ObserveAlertsUseCase
import com.realtimepricetracker.domain.usecases.ObserveWatchlistUseCase
import com.realtimepricetracker.domain.usecases.RemoveAlertUseCase
import com.realtimepricetracker.domain.usecases.RemoveFromWatchlistUseCase
import com.realtimepricetracker.domain.usecases.SubscribeToPriceUpdatesUseCase
import com.realtimepricetracker.domain.usecases.WatchSymbolsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

object AppFactory {
    private lateinit var appContext: Context
    private val appScope = CoroutineScope(SupervisorJob())
    private val gson = Gson()

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val restDataSource by lazy { FinnhubRestDataSource(gson = gson) }
    private val webSocketDataSource by lazy { WebSocketDataSource(appScope, appContext) }

    val priceRepository: PriceRepository by lazy {
        PriceRepositoryImpl(webSocketDataSource, restDataSource, gson)
    }
    val connectionRepository: ConnectionRepository by lazy {
        ConnectionRepositoryImpl(webSocketDataSource)
    }

    private val watchlistDataSource by lazy { WatchlistDataSource(appContext) }
    val watchlistRepository: WatchlistRepository by lazy {
        WatchlistRepositoryImpl(watchlistDataSource)
    }

    private val alertDataSource by lazy { AlertDataSource(appContext, gson) }
    val alertRepository: AlertRepository by lazy { AlertRepositoryImpl(alertDataSource) }

    val notificationHelper by lazy { NotificationHelper(appContext) }

    // Price use cases
    val getInitialStocksUseCase by lazy { GetInitialStocksUseCase(priceRepository) }
    val subscribeToPriceUpdatesUseCase by lazy { SubscribeToPriceUpdatesUseCase(priceRepository) }
    val watchSymbolsUseCase by lazy { WatchSymbolsUseCase(priceRepository) }
    val manageConnectionUseCase by lazy { ManageConnectionUseCase(connectionRepository) }

    // Watchlist use cases
    val observeWatchlistUseCase by lazy { ObserveWatchlistUseCase(watchlistRepository) }
    val addToWatchlistUseCase by lazy { AddToWatchlistUseCase(watchlistRepository) }
    val removeFromWatchlistUseCase by lazy { RemoveFromWatchlistUseCase(watchlistRepository) }

    // Alert use cases
    val observeAlertsUseCase by lazy { ObserveAlertsUseCase(alertRepository) }
    val addAlertUseCase by lazy { AddAlertUseCase(alertRepository) }
    val removeAlertUseCase by lazy { RemoveAlertUseCase(alertRepository) }
    val checkAlertsUseCase by lazy { CheckAlertsUseCase() }
}
