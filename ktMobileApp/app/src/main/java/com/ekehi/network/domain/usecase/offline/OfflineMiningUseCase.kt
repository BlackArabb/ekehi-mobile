package com.ekehi.network.domain.usecase.offline

import com.ekehi.network.data.model.MiningSession
import com.ekehi.network.data.repository.MiningRepository
import com.ekehi.network.data.repository.offline.OfflineMiningRepository
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.domain.usecase.MiningUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class OfflineMiningUseCase @Inject constructor(
    private val miningRepository: MiningRepository
) : MiningUseCase(miningRepository) {
    
    fun getOfflineMiningSessions(userId: String): Flow<Resource<List<MiningSession>>> = flow {
        emit(Resource.Loading)
        if (miningRepository is OfflineMiningRepository) {
            miningRepository.getOfflineMiningSessions(userId).collect { sessions ->
                emit(Resource.Success(sessions))
            }
        } else {
            emit(Resource.Error("Offline repository not available"))
        }
    }.catch { e ->
        emit(Resource.Error("Error getting offline mining sessions: ${e.message}"))
    }
    
    fun syncMiningSession(sessionId: String): Flow<Resource<MiningSession>> = flow {
        emit(Resource.Loading)
        if (miningRepository is OfflineMiningRepository) {
            val result = miningRepository.syncMiningSession(sessionId)
            if (result.isSuccess) {
                emit(Resource.Success(result.getOrNull()!!))
            } else {
                emit(Resource.Error("Failed to sync mining session: ${result.exceptionOrNull()?.message}"))
            }
        } else {
            val result = miningRepository.getMiningSession(sessionId)
            if (result.isSuccess) {
                emit(Resource.Success(result.getOrNull()!!))
            } else {
                emit(Resource.Error("Failed to get mining session: ${result.exceptionOrNull()?.message}"))
            }
        }
    }.catch { e ->
        emit(Resource.Error("Error syncing mining session: ${e.message}"))
    }
}