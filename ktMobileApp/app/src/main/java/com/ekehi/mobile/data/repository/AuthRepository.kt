package com.ekehi.mobile.data.repository

import com.ekehi.mobile.data.model.User
import com.ekehi.mobile.network.service.AppwriteService
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
        return withContext(Dispatchers.IO) {
            try {
                val session = appwriteService.account.createEmailPasswordSession(
                        email = email,
                        password = password
                )
                Result.success(session)
            } catch (e: AppwriteException) {
                Result.failure(e)
            }
        }
    }

    suspend fun register(email: String, password: String, name: String): Result<AppwriteUser<Map<String, Any>>> {
        return withContext(Dispatchers.IO) {
            try {
                val user = appwriteService.account.create(
                        userId = ID.unique(),
                        email = email,
                        password = password,
                        name = name
                )
                Result.success(user)
            } catch (e: AppwriteException) {
                Result.failure(e)
            }
        }
    }

    suspend fun logout(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                appwriteService.account.deleteSession("current")
                Result.success(Unit)
            } catch (e: AppwriteException) {
                Result.failure(e)
            }
        }
    }

    suspend fun getCurrentUser(): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val account = appwriteService.account.get()
                val user = User(
                        id = account.id,
                        name = account.name,
                        email = account.email,
                        createdAt = account.registration ?: "",
                        updatedAt = ""
                )
                Result.success(user)
            } catch (e: AppwriteException) {
                Result.failure(e)
            }
        }
    }
}