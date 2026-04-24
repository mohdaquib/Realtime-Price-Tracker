package com.realtimepricetracker.domain.usecases

import com.realtimepricetracker.domain.repositories.WatchlistRepository

class AddToWatchlistUseCase(private val repository: WatchlistRepository) {
    suspend operator fun invoke(symbol: String) = repository.add(symbol)
}
