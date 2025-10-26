package com.ekehi.network.domain.usecase

import com.ekehi.network.data.model.SocialTask
import com.ekehi.network.data.repository.SocialTaskRepository
import com.ekehi.network.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

open class SocialTaskUseCase @Inject constructor(
    private val socialTaskRepository: SocialTaskRepository
) {
    fun getSocialTasks(): Flow<Resource<List<SocialTask>>> = flow {
        emit(Resource.Loading)
        val result = socialTaskRepository.getSocialTasks()
        if (result.isSuccess) {
            val data = result.getOrNull() ?: emptyList()
            emit(Resource.Success(data))
        } else {
            emit(Resource.Error("Failed to get social tasks: ${result.exceptionOrNull()?.message}"))
        }
    }.catch { e ->
        emit(Resource.Error("Error getting social tasks: ${e.message}"))
    }

    fun getUserSocialTasks(userId: String): Flow<Resource<List<SocialTask>>> = flow {
        emit(Resource.Loading)
        val result = socialTaskRepository.getUserSocialTasks(userId)
        if (result.isSuccess) {
            val data = result.getOrNull() ?: emptyList()
            emit(Resource.Success(data))
        } else {
            emit(Resource.Error("Failed to get user social tasks: ${result.exceptionOrNull()?.message}"))
        }
    }.catch { e ->
        emit(Resource.Error("Error getting user social tasks: ${e.message}"))
    }

    fun completeSocialTask(userId: String, taskId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        val result = socialTaskRepository.completeSocialTask(userId, taskId)
        if (result.isSuccess) {
            emit(Resource.Success(Unit))
        } else {
            emit(Resource.Error("Failed to complete social task: ${result.exceptionOrNull()?.message}"))
        }
    }.catch { e ->
        emit(Resource.Error("Error completing social task: ${e.message}"))
    }
}