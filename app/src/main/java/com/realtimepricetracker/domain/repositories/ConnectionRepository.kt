package com.realtimepricetracker.domain.repositories

import kotlinx.coroutines.flow.Flow

/**
 * Domain repository interface for connection state management.
 */
interface ConnectionRepository {
    /**
     * Observe connection state changes
     */
    fun observeConnectionState(): Flow<Boolean>

    /**
     * Connect to the price feed
     */
    suspend fun connect(): Result<Unit>

    /**
     * Disconnect from the price feed
     */
    suspend fun disconnect(): Result<Unit>

    /**
     * Get current connection state
     */
    fun isConnected(): Boolean
}

