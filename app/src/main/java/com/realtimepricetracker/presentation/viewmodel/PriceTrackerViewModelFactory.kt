package com.realtimepricetracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.realtimepricetracker.di.AppFactory

class PriceTrackerViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PriceTrackerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PriceTrackerViewModel(
                getInitialStocksUseCase = AppFactory.getInitialStocksUseCase,
                subscribeToPriceUpdatesUseCase = AppFactory.subscribeToPriceUpdatesUseCase,
                watchSymbolsUseCase = AppFactory.watchSymbolsUseCase,
                manageConnectionUseCase = AppFactory.manageConnectionUseCase,
                observeWatchlistUseCase = AppFactory.observeWatchlistUseCase,
                addToWatchlistUseCase = AppFactory.addToWatchlistUseCase,
                removeFromWatchlistUseCase = AppFactory.removeFromWatchlistUseCase,
                observeAlertsUseCase = AppFactory.observeAlertsUseCase,
                addAlertUseCase = AppFactory.addAlertUseCase,
                removeAlertUseCase = AppFactory.removeAlertUseCase,
                checkAlertsUseCase = AppFactory.checkAlertsUseCase,
                notificationHelper = AppFactory.notificationHelper,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
