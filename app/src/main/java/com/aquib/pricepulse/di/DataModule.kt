package com.aquib.pricepulse.di

import android.content.Context
import com.google.gson.Gson
import com.aquib.pricepulse.data.datasource.FinnhubRestDataSource
import com.aquib.pricepulse.data.datasource.WebSocketDataSource
import com.aquib.pricepulse.data.local.AlertDataSource
import com.aquib.pricepulse.data.local.StockCacheDataSource
import com.aquib.pricepulse.data.local.WatchlistDataSource
import com.aquib.pricepulse.data.notification.NotificationHelper
import com.aquib.pricepulse.data.repositories.AlertRepositoryImpl
import com.aquib.pricepulse.data.repositories.ConnectionRepositoryImpl
import com.aquib.pricepulse.data.repositories.PriceRepositoryImpl
import com.aquib.pricepulse.data.repositories.WatchlistRepositoryImpl
import com.aquib.pricepulse.domain.repositories.AlertRepository
import com.aquib.pricepulse.domain.repositories.ConnectionRepository
import com.aquib.pricepulse.domain.repositories.PriceRepository
import com.aquib.pricepulse.domain.repositories.WatchlistRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideAppScope(): CoroutineScope = CoroutineScope(SupervisorJob())

    @Provides
    @Singleton
    fun provideWebSocketDataSource(
        @ApplicationContext context: Context,
        scope: CoroutineScope
    ): WebSocketDataSource = WebSocketDataSource(scope, context)

    @Provides
    @Singleton
    fun provideRestDataSource(client: OkHttpClient, gson: Gson): FinnhubRestDataSource = 
        FinnhubRestDataSource(client, gson)

    @Provides
    @Singleton
    fun provideStockCacheDataSource(
        @ApplicationContext context: Context,
        gson: Gson
    ): StockCacheDataSource = StockCacheDataSource(context, gson)

    @Provides
    @Singleton
    fun provideWatchlistDataSource(
        @ApplicationContext context: Context
    ): WatchlistDataSource = WatchlistDataSource(context)

    @Provides
    @Singleton
    fun provideAlertDataSource(
        @ApplicationContext context: Context,
        gson: Gson
    ): AlertDataSource = AlertDataSource(context, gson)

    @Provides
    @Singleton
    fun provideNotificationHelper(
        @ApplicationContext context: Context
    ): NotificationHelper = NotificationHelper(context)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPriceRepository(
        impl: PriceRepositoryImpl
    ): PriceRepository

    @Binds
    @Singleton
    abstract fun bindConnectionRepository(
        impl: ConnectionRepositoryImpl
    ): ConnectionRepository

    @Binds
    @Singleton
    abstract fun bindWatchlistRepository(
        impl: WatchlistRepositoryImpl
    ): WatchlistRepository

    @Binds
    @Singleton
    abstract fun bindAlertRepository(
        impl: AlertRepositoryImpl
    ): AlertRepository
}

