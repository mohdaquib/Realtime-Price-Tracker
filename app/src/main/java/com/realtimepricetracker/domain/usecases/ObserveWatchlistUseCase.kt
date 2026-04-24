package com.realtimepricetracker.domain.usecases

import com.realtimepricetracker.domain.repositories.WatchlistRepository
import kotlinx.coroutines.flow.Flow

class ObserveWatchlistUseCase(private val repository: WatchlistRepository) {
    operator fun invoke(): Flow<Set<String>> = repository.observeWatchlist()
}
