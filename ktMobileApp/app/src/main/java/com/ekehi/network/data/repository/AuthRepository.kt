package com.ekehi.network.data.repository

import android.util.Log
import com.ekehi.network.data.model.User
import com.ekehi.network.service.AppwriteService
import io.appwrite.ID
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.Session
import io.appwrite.models.User as AppwriteUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepository @Inject constructor(
        private val appwriteService: AppwriteService
) {
    suspend fun login(email: String, password: String): Result<Session> {
        Log.d("AuthRepository", "Attempting login for email: $email")
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthRepository", "Calling Appwrite login API")
                val session = appwriteService.account.createEmailPasswordSession(
                        email = email,
                        password = password
                )
                Log.d("AuthRepository", "Login successful for email: $email")
                Result.success(session)
            } catch (e: AppwriteException) {
                val errorMessage = "Appwrite login failed: ${e.message}"
                Log.e("AuthRepository", errorMessage, e)
                Result.failure(e)
            }
        }
    }

    suspend fun register(email: String, password: String, name: String): Result<AppwriteUser<Map<String, Any>>> {
        Log.d("AuthRepository", "Attempting registration for email: $email")
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthRepository", "Calling Appwrite registration API")
                val user = appwriteService.account.create(
                        userId = ID.unique(),
                        email = email,
                        password = password,
                        name = name
                )
                Log.d("AuthRepository", "Registration successful for email: $email")
                Result.success(user)
            } catch (e: AppwriteException) {
                val errorMessage = "Appwrite registration failed: ${e.message}"
                Log.e("AuthRepository", errorMessage, e)
                Result.failure(e)
            }
        }
    }

    suspend fun logout(): Result<Unit> {
        Log.d("AuthRepository", "Attempting logout")
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthRepository", "Calling Appwrite logout API")
                appwriteService.account.deleteSession("current")
                Log.d("AuthRepository", "Logout successful")
                Result.success(Unit)
            } catch (e: AppwriteException) {
                val errorMessage = "Appwrite logout failed: ${e.message}"
                Log.e("AuthRepository", errorMessage, e)
                Result.failure(e)
            }
        }
    }

    suspend fun getCurrentUser(): Result<User> {
        Log.d("AuthRepository", "Fetching current user")
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthRepository", "Calling Appwrite get account API")
                val account = appwriteService.account.get()
                val user = User(
                        id = account.id,
                        name = account.name,
                        email = account.email,
                        createdAt = account.registration ?: "",
                        updatedAt = ""
                )
                Log.d("AuthRepository", "Current user fetched successfully: ${user.id}")
                Result.success(user)
            } catch (e: AppwriteException) {
                val errorMessage = "Failed to fetch current user: ${e.message}"
                Log.e("AuthRepository", errorMessage, e)
                Result.failure(e)
            }
        }
    }
}