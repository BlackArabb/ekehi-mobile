package com.ekehi.mobile.domain.usecase

import com.ekehi.mobile.data.repository.AuthRepository
import com.ekehi.mobile.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AuthUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    fun login(email: String, password: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        val result = authRepository.login(email, password)
        if (result.isSuccess) {
            emit(Resource.Success(Unit))
        } else {
            emit(Resource.Error("Login failed: ${result.exceptionOrNull()?.message}"))
        }
    }.catch { e ->
        emit(Resource.Error("Login error: ${e.message}"))
    }

    fun register(email: String, password: String, name: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        val result = authRepository.register(email, password, name)
        if (result.isSuccess) {
            emit(Resource.Success(Unit))
        } else {
            emit(Resource.Error("Registration failed: ${result.exceptionOrNull()?.message}"))
        }
    }.catch { e ->
        emit(Resource.Error("Registration error: ${e.message}"))
    }

    fun logout(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        val result = authRepository.logout()
        if (result.isSuccess) {
            emit(Resource.Success(Unit))
        } else {
            emit(Resource.Error("Logout failed: ${result.exceptionOrNull()?.message}"))
        }
    }.catch { e ->
        emit(Resource.Error("Logout error: ${e.message}"))
    }
}