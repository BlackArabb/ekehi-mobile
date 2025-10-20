package com.ekehi.mobile.domain.usecase.offline

import com.ekehi.mobile.data.repository.UserRepository
import com.ekehi.mobile.data.repository.offline.OfflineUserRepository
import com.ekehi.mobile.domain.model.Resource
import com.ekehi.mobile.domain.usecase.UserUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class OfflineUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) : UserUseCase(userRepository) {
    
    fun getOfflineUserProfile(userId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        if (userRepository is OfflineUserRepository) {
            userRepository.getOfflineUserProfile(userId).collect { profile ->
                if (profile != null) {
                    emit(Resource.Success(Unit))
                } else {
                    emit(Resource.Error("No cached profile found"))
                }
            }
        } else {
            emit(Resource.Error("Offline repository not available"))
        }
    }.catch { e ->
        emit(Resource.Error("Error getting offline user profile: ${e.message}"))
    }
    
    fun syncUserProfile(userId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        if (userRepository is OfflineUserRepository) {
            val result = userRepository.syncUserProfile(userId)
            if (result.isSuccess) {
                emit(Resource.Success(Unit))
            } else {
                // Try to get cached data
                userRepository.getOfflineUserProfile(userId).collect { profile ->
                    if (profile != null) {
                        emit(Resource.Success(Unit))
                    } else {
                        emit(Resource.Error("Failed to sync user profile: ${result.exceptionOrNull()?.message}"))
                    }
                }
            }
        } else {
            val result = userRepository.getUserProfile(userId)
            if (result.isSuccess) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Failed to get user profile: ${result.exceptionOrNull()?.message}"))
            }
        }
    }.catch { e ->
        emit(Resource.Error("Error syncing user profile: ${e.message}"))
    }
}