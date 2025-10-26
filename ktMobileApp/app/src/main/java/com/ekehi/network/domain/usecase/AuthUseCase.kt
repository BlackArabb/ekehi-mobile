package com.ekehi.network.domain.usecase

import android.util.Log
import com.ekehi.network.data.repository.AuthRepository
import com.ekehi.network.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AuthUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    fun login(email: String, password: String): Flow<Resource<Unit>> = flow {
        Log.d("AuthUseCase", "Starting login flow for email: $email")
        emit(Resource.Loading)
        Log.d("AuthUseCase", "Emitted Loading state")
        
        val result = authRepository.login(email, password)
        Log.d("AuthUseCase", "Login result received: ${result.isSuccess}")
        
        if (result.isSuccess) {
            Log.d("AuthUseCase", "Login successful for email: $email")
            emit(Resource.Success(Unit))
        } else {
            val errorMessage = "Login failed: ${result.exceptionOrNull()?.message}"
            Log.e("AuthUseCase", errorMessage)
            emit(Resource.Error(errorMessage))
        }
    }.catch { e ->
        val errorMessage = "Login error: ${e.message}"
        Log.e("AuthUseCase", errorMessage, e)
        emit(Resource.Error(errorMessage))
    }

    fun register(email: String, password: String, name: String): Flow<Resource<Unit>> = flow {
        Log.d("AuthUseCase", "Starting registration flow for email: $email")
        emit(Resource.Loading)
        val result = authRepository.register(email, password, name)
        if (result.isSuccess) {
            Log.d("AuthUseCase", "Registration successful for email: $email")
            emit(Resource.Success(Unit))
        } else {
            val errorMessage = "Registration failed: ${result.exceptionOrNull()?.message}"
            Log.e("AuthUseCase", errorMessage)
            emit(Resource.Error(errorMessage))
        }
    }.catch { e ->
        val errorMessage = "Registration error: ${e.message}"
        Log.e("AuthUseCase", errorMessage, e)
        emit(Resource.Error(errorMessage))
    }

    fun logout(): Flow<Resource<Unit>> = flow {
        Log.d("AuthUseCase", "Starting logout flow")
        emit(Resource.Loading)
        val result = authRepository.logout()
        if (result.isSuccess) {
            Log.d("AuthUseCase", "Logout successful")
            emit(Resource.Success(Unit))
        } else {
            val errorMessage = "Logout failed: ${result.exceptionOrNull()?.message}"
            Log.e("AuthUseCase", errorMessage)
            emit(Resource.Error(errorMessage))
        }
    }.catch { e ->
        val errorMessage = "Logout error: ${e.message}"
        Log.e("AuthUseCase", errorMessage, e)
        emit(Resource.Error(errorMessage))
    }
}