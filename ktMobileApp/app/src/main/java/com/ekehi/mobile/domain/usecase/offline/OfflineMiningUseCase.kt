package com.ekehi.mobile.domain.usecase.offline

import com.ekehi.mobile.data.repository.MiningRepository
import com.ekehi.mobile.data.repository.offline.OfflineMiningRepository
import com.ekehi.mobile.domain.model.Resource
import com.ekehi.mobile.domain.usecase.MiningUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class OfflineMiningUseCase @Inject constructor(
    private val miningRepository: MiningRepository
) : MiningUseCase(miningRepository) {
    
    fun getOfflineMiningSessions(userId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        if (miningRepository is OfflineMiningRepository) {
            miningRepository.getOfflineMiningSessions(userId).collect { sessions ->
                emit(Resource.Success(Unit))
            }
        } else {
            emit(Resource.Error("Offline repository not available"))
        }
    }.catch { e ->
        emit(Resource.Error("Error getting offline mining sessions: ${e.message}"))
    }
    
    fun syncMiningSession(sessionId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        if (miningRepository is OfflineMiningRepository) {
            val result = miningRepository.syncMiningSession(sessionId)
            if (result.isSuccess) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Failed to sync mining session: ${result.exceptionOrNull()?.message}"))
            }
        } else {
            val result = miningRepository.getMiningSession(sessionId)
            if (result.isSuccess) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Failed to get mining session: ${result.exceptionOrNull()?.message}"))
            }
        }
    }.catch { e ->
        emit(Resource.Error("Error syncing mining session: ${e.message}"))
    }
}