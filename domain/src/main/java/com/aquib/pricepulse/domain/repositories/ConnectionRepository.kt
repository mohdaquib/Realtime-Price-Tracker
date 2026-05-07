package com.aquib.pricepulse.domain.repositories

import kotlinx.coroutines.flow.Flow

interface ConnectionRepository {
    fun observeConnectionState(): Flow<Boolean>
    suspend fun connect(): Result<Unit>
    suspend fun disconnect(): Result<Unit>
    fun isConnected(): Boolean
}
