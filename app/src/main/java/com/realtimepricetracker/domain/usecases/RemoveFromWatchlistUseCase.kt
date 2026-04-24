package com.realtimepricetracker.domain.usecases

import com.realtimepricetracker.domain.repositories.WatchlistRepository

class RemoveFromWatchlistUseCase(private val repository: WatchlistRepository) {
    suspend operator fun invoke(symbol: String) = repository.remove(symbol)
}
