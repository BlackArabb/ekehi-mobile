package com.ekehi.network.domain.usecase

import com.ekehi.network.data.model.MiningSession
import com.ekehi.network.data.repository.MiningRepository
import com.ekehi.network.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

open class MiningUseCase @Inject constructor(
    private val miningRepository: MiningRepository
) {
    fun startMiningSession(userId: String): Flow<Resource<MiningSession>> = flow {
        emit(Resource.Loading)
        val result = miningRepository.createMiningSession(userId)
        if (result.isSuccess) {
            emit(Resource.Success(result.getOrNull()!!))
        } else {
            emit(Resource.Error("Failed to start mining session: ${result.exceptionOrNull()?.message}"))
        }
    }.catch { e ->
        emit(Resource.Error("Error starting mining session: ${e.message}"))
    }

    fun updateMiningSession(sessionId: String, updates: Map<String, Any>): Flow<Resource<MiningSession>> = flow {
        emit(Resource.Loading)
        val result = miningRepository.updateMiningSession(sessionId, updates)
        if (result.isSuccess) {
            emit(Resource.Success(result.getOrNull()!!))
        } else {
            emit(Resource.Error("Failed to update mining session: ${result.exceptionOrNull()?.message}"))
        }
    }.catch { e ->
        emit(Resource.Error("Error updating mining session: ${e.message}"))
    }

    fun getMiningSession(sessionId: String): Flow<Resource<MiningSession>> = flow {
        emit(Resource.Loading)
        val result = miningRepository.getMiningSession(sessionId)
        if (result.isSuccess) {
            emit(Resource.Success(result.getOrNull()!!))
        } else {
            emit(Resource.Error("Failed to get mining session: ${result.exceptionOrNull()?.message}"))
        }
    }.catch { e ->
        emit(Resource.Error("Error getting mining session: ${e.message}"))
    }
}