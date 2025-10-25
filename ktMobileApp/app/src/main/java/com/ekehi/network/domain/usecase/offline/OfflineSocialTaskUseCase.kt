package com.ekehi.network.domain.usecase.offline

import com.ekehi.network.data.model.SocialTask
import com.ekehi.network.data.repository.SocialTaskRepository
import com.ekehi.network.data.repository.offline.OfflineSocialTaskRepository
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.domain.usecase.SocialTaskUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class OfflineSocialTaskUseCase @Inject constructor(
    private val socialTaskRepository: SocialTaskRepository
) : SocialTaskUseCase(socialTaskRepository) {
    
    fun getOfflineSocialTasks(userId: String): Flow<Resource<List<SocialTask>>> = flow {
        emit(Resource.Loading)
        if (socialTaskRepository is OfflineSocialTaskRepository) {
            socialTaskRepository.getOfflineSocialTasks(userId).collect { tasks ->
                emit(Resource.Success<List<SocialTask>>(tasks))
            }
        } else {
            emit(Resource.Error("Offline repository not available"))
        }
    }.catch { e ->
        emit(Resource.Error("Error getting offline social tasks: ${e.message}"))
    }
    
    fun syncSocialTasks(): Flow<Resource<List<SocialTask>>> = flow {
        emit(Resource.Loading)
        if (socialTaskRepository is OfflineSocialTaskRepository) {
            val result = socialTaskRepository.syncSocialTasks()
            if (result.isSuccess) {
                // Return empty list as sync doesn't return tasks directly
                emit(Resource.Success<List<SocialTask>>(emptyList()))
            } else {
                emit(Resource.Error("Failed to sync social tasks: ${result.exceptionOrNull()?.message}"))
            }
        } else {
            val result = socialTaskRepository.getSocialTasks()
            if (result.isSuccess) {
                emit(Resource.Success<List<SocialTask>>(result.getOrNull() ?: emptyList()))
            } else {
                emit(Resource.Error("Failed to get social tasks: ${result.exceptionOrNull()?.message}"))
            }
        }
    }.catch { e ->
        emit(Resource.Error("Error syncing social tasks: ${e.message}"))
    }
}