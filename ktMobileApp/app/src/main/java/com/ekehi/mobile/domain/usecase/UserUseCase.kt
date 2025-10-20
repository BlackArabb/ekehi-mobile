package com.ekehi.mobile.domain.usecase

import com.ekehi.mobile.data.repository.UserRepository
import com.ekehi.mobile.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

open class UserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    fun getUserProfile(userId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        val result = userRepository.getUserProfile(userId)
        if (result.isSuccess) {
            emit(Resource.Success(Unit))
        } else {
            emit(Resource.Error("Failed to get user profile: ${result.exceptionOrNull()?.message}"))
        }
    }.catch { e ->
        emit(Resource.Error("Error getting user profile: ${e.message}"))
    }

    fun createUserProfile(userId: String, displayName: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        val result = userRepository.createUserProfile(userId, displayName)
        if (result.isSuccess) {
            emit(Resource.Success(Unit))
        } else {
            emit(Resource.Error("Failed to create user profile: ${result.exceptionOrNull()?.message}"))
        }
    }.catch { e ->
        emit(Resource.Error("Error creating user profile: ${e.message}"))
    }
}