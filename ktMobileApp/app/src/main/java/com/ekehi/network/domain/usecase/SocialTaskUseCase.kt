package com.ekehi.network.domain.usecase

import com.ekehi.network.data.model.SocialTask
import com.ekehi.network.data.repository.SocialTaskRepository
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.domain.verification.VerificationResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

open class SocialTaskUseCase @Inject constructor(
    private val socialTaskRepository: SocialTaskRepository
) {
    fun getSocialTasks(): Flow<Resource<List<SocialTask>>> = flow {
        emit(Resource.Loading)
        val result = socialTaskRepository.getAllSocialTasks()
        if (result.isSuccess) {
            val data = result.getOrNull() ?: emptyList<SocialTask>()
            emit(Resource.Success(data))
        } else {
            emit(Resource.Error("Failed to get social tasks: ${result.exceptionOrNull()?.message}"))
        }
    }.catch { e ->
        emit(Resource.Error("Error getting social tasks: ${e.message}"))
    }

    fun getUserSocialTasks(userId: String): Flow<Resource<List<SocialTask>>> = flow {
        emit(Resource.Loading)
        val result = socialTaskRepository.getSocialTasksWithUserStatus(userId)
        if (result.isSuccess) {
            val data = result.getOrNull() ?: emptyList()
            emit(Resource.Success(data))
        } else {
            emit(Resource.Error("Failed to get user social tasks: ${result.exceptionOrNull()?.message}"))
        }
    }.catch { e ->
        emit(Resource.Error("Error getting user social tasks: ${e.message}"))
    }

    fun completeSocialTask(userId: String, taskId: String, proofData: Map<String, Any>? = null): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        
        val result = socialTaskRepository.completeSocialTask(userId, taskId, proofData)
        
        if (result.isSuccess) {
            val (userTask, verificationResult) = result.getOrThrow()
            
            // The UseCase just confirms the operation succeeded
            // The ViewModel will handle the actual VerificationResult
            // This way the ViewModel gets the result and can update UI accordingly
            when (verificationResult) {
                is VerificationResult.Success -> {
                    // Verification succeeded - coins were awarded in repository
                    emit(Resource.Success(Unit))
                }
                is VerificationResult.Pending -> {
                    // Pending review - no coins awarded yet
                    // Still return success since task submission succeeded
                    emit(Resource.Success(Unit))
                }
                is VerificationResult.Failure -> {
                    // Verification failed - no coins awarded
                    // Still return success since the API call succeeded
                    // The ViewModel reads verificationResult to show appropriate message
                    emit(Resource.Success(Unit))
                }
            }
        } else {
            // Repository operation itself failed (network error, etc)
            emit(Resource.Error("Failed to complete social task: ${result.exceptionOrNull()?.message}"))
        }
    }.catch { e ->
        emit(Resource.Error("Error completing social task: ${e.message}"))
    }

    fun getUserCompletedTasksCount(userId: String): Flow<Resource<Int>> = flow {
        emit(Resource.Loading)
        try {
            val result = socialTaskRepository.getUserCompletedTasksCount(userId)
            if (result.isSuccess) {
                val count = result.getOrNull() ?: 0
                emit(Resource.Success(count))
            } else {
                emit(Resource.Error("Failed to get completed tasks count: ${result.exceptionOrNull()?.message}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Error getting completed tasks count: ${e.message}"))
        }
    }.catch { e ->
        emit(Resource.Error("Error getting completed tasks count: ${e.message}"))
    }
}