package com.ekehi.network.domain.usecase

import android.util.Log
import com.ekehi.network.data.repository.AuthRepository
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.data.model.User
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
            val errorMessage = "Login failed: ${result.exceptionOrNull()?.message ?: "Unknown error"}"
            Log.e("AuthUseCase", errorMessage)
            emit(Resource.Error(errorMessage))
        }
    }.catch { e ->
        val errorMessage = "Login error: ${e.message ?: "Unknown error"}"
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
            val errorMessage = "Registration failed: ${result.exceptionOrNull()?.message ?: "Unknown error"}"
            Log.e("AuthUseCase", errorMessage)
            emit(Resource.Error(errorMessage))
        }
    }.catch { e ->
        val errorMessage = "Registration error: ${e.message ?: "Unknown error"}"
        Log.e("AuthUseCase", errorMessage, e)
        emit(Resource.Error(errorMessage))
    }

    fun loginWithGoogle(idToken: String): Flow<Resource<Unit>> = flow {
        Log.d("AuthUseCase", "Starting Google login flow")
        emit(Resource.Loading)
        Log.d("AuthUseCase", "Emitted Loading state for Google login")
        
        val result = authRepository.loginWithGoogle(idToken)
        Log.d("AuthUseCase", "Google login result received: ${result.isSuccess}")
        
        if (result.isSuccess) {
            Log.d("AuthUseCase", "Google login successful")
            emit(Resource.Success(Unit))
        } else {
            val errorMessage = "Google login failed: ${result.exceptionOrNull()?.message ?: "Unknown error"}"
            Log.e("AuthUseCase", errorMessage)
            emit(Resource.Error(errorMessage))
        }
    }.catch { e ->
        val errorMessage = "Google login error: ${e.message ?: "Unknown error"}"
        Log.e("AuthUseCase", errorMessage, e)
        emit(Resource.Error(errorMessage))
    }

    fun registerWithGoogle(idToken: String, name: String, email: String): Flow<Resource<Unit>> = flow {
        Log.d("AuthUseCase", "Starting Google registration flow for email: $email")
        emit(Resource.Loading)
        val result = authRepository.registerWithGoogle(idToken, name, email)
        if (result.isSuccess) {
            Log.d("AuthUseCase", "Google registration successful for email: $email")
            emit(Resource.Success(Unit))
        } else {
            val errorMessage = "Google registration failed: ${result.exceptionOrNull()?.message ?: "Unknown error"}"
            Log.e("AuthUseCase", errorMessage)
            emit(Resource.Error(errorMessage))
        }
    }.catch { e ->
        val errorMessage = "Google registration error: ${e.message ?: "Unknown error"}"
        Log.e("AuthUseCase", errorMessage, e)
        emit(Resource.Error(errorMessage))
    }

    fun getCurrentUser(): Flow<Resource<Unit>> = flow {
        Log.d("AuthUseCase", "Starting get current user flow")
        emit(Resource.Loading)
        val result = authRepository.getCurrentUser()
        if (result.isSuccess) {
            Log.d("AuthUseCase", "Current user fetched successfully")
            emit(Resource.Success(Unit))
        } else {
            val errorMessage = "Failed to fetch current user: ${result.exceptionOrNull()?.message ?: "Unknown error"}"
            Log.e("AuthUseCase", errorMessage)
            emit(Resource.Error(errorMessage))
        }
    }.catch { e ->
        val errorMessage = "Error fetching current user: ${e.message ?: "Unknown error"}"
        Log.e("AuthUseCase", errorMessage, e)
        emit(Resource.Error(errorMessage))
    }
    
    fun checkStoredCredentials(): Flow<Resource<Boolean>> = flow {
        Log.d("AuthUseCase", "Starting check stored credentials flow")
        emit(Resource.Loading)
        val result = authRepository.checkStoredCredentials()
        if (result.isSuccess) {
            Log.d("AuthUseCase", "Stored credentials check completed: ${result.getOrNull()}")
            emit(Resource.Success(result.getOrNull() ?: false))
        } else {
            val errorMessage = "Failed to check stored credentials: ${result.exceptionOrNull()?.message ?: "Unknown error"}"
            Log.e("AuthUseCase", errorMessage)
            emit(Resource.Error(errorMessage))
        }
    }.catch { e ->
        val errorMessage = "Error checking stored credentials: ${e.message ?: "Unknown error"}"
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
            val errorMessage = "Logout failed: ${result.exceptionOrNull()?.message ?: "Unknown error"}"
            Log.e("AuthUseCase", errorMessage)
            emit(Resource.Error(errorMessage))
        }
    }.catch { e ->
        val errorMessage = "Logout error: ${e.message ?: "Unknown error"}"
        Log.e("AuthUseCase", errorMessage, e)
        emit(Resource.Error(errorMessage))
    }
}