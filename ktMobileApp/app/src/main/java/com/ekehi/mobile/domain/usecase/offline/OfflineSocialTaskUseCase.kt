package com.ekehi.mobile.domain.usecase.offline

import com.ekehi.mobile.data.repository.SocialTaskRepository
import com.ekehi.mobile.data.repository.offline.OfflineSocialTaskRepository
import com.ekehi.mobile.domain.model.Resource
import com.ekehi.mobile.domain.usecase.SocialTaskUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class OfflineSocialTaskUseCase @Inject constructor(
    private val socialTaskRepository: SocialTaskRepository
) : SocialTaskUseCase(socialTaskRepository) {
    
    fun getOfflineSocialTasks(userId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        if (socialTaskRepository is OfflineSocialTaskRepository) {
            socialTaskRepository.getOfflineSocialTasks(userId).collect { tasks ->
                emit(Resource.Success(Unit))
            }
        } else {
            emit(Resource.Error("Offline repository not available"))
        }
    }.catch { e ->
        emit(Resource.Error("Error getting offline social tasks: ${e.message}"))
    }
    
    fun syncSocialTasks(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        if (socialTaskRepository is OfflineSocialTaskRepository) {
            val result = socialTaskRepository.syncSocialTasks()
            if (result.isSuccess) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Failed to sync social tasks: ${result.exceptionOrNull()?.message}"))
            }
        } else {
            val result = socialTaskRepository.getSocialTasks()
            if (result.isSuccess) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Failed to get social tasks: ${result.exceptionOrNull()?.message}"))
            }
        }
    }.catch { e ->
        emit(Resource.Error("Error syncing social tasks: ${e.message}"))
    }
}