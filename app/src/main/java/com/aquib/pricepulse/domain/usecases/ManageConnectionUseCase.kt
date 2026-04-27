package com.aquib.pricepulse.domain.usecases

import com.aquib.pricepulse.domain.repositories.ConnectionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for managing connection lifecycle.
 */
class ManageConnectionUseCase @Inject constructor(private val connectionRepository: ConnectionRepository) {
    suspend fun connect(): Result<Unit> = connectionRepository.connect()

    suspend fun disconnect(): Result<Unit> = connectionRepository.disconnect()

    fun observeConnectionState(): Flow<Boolean> = connectionRepository.observeConnectionState()

    fun isConnected(): Boolean = connectionRepository.isConnected()
}

