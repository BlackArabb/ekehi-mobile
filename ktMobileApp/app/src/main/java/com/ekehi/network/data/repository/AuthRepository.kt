package com.ekehi.network.data.repository

import android.util.Log
import com.ekehi.network.data.model.User
import com.ekehi.network.security.SecurePreferences
import com.ekehi.network.service.AppwriteService
import io.appwrite.ID
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.Session
import io.appwrite.models.User as AppwriteUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepository @Inject constructor(
        private val appwriteService: AppwriteService,
        private val securePreferences: SecurePreferences
) {
    companion object {
        private const val PREF_USER_ID = "user_id"
        private const val PREF_USER_EMAIL = "user_email"
        private const val PREF_USER_NAME = "user_name"
        private const val PREF_LOGIN_TIMESTAMP = "login_timestamp"
    }

    suspend fun login(email: String, password: String): Result<Session> {
        Log.d("AuthRepository", "Attempting login for email: $email")
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthRepository", "Checking for existing session")
                // First check if there's already an active session
                val currentSession = getCurrentSession()
                if (currentSession.isSuccess) {
                    Log.d("AuthRepository", "Existing session found, deleting it before creating new one")
                    // Delete existing session before creating a new one
                    try {
                        appwriteService.account.deleteSession("current")
                        Log.d("AuthRepository", "Existing session deleted successfully")
                    } catch (e: Exception) {
                        Log.w("AuthRepository", "Failed to delete existing session: ${e.message}", e)
                    }
                }

                Log.d("AuthRepository", "Calling Appwrite login API")
                val session = appwriteService.account.createEmailPasswordSession(
                        email = email,
                        password = password
                )
                Log.d("AuthRepository", "Login successful for email: $email")

                // Store user info securely after successful login
                storeUserInfo(email)

                return@withContext Result.success(session)
            } catch (e: AppwriteException) {
                val errorMessage = "Appwrite login failed: ${e.message}"
                Log.e("AuthRepository", errorMessage, e)
                return@withContext Result.failure(e)
            } catch (e: Exception) {
                val errorMessage = "Login failed: ${e.message}"
                Log.e("AuthRepository", errorMessage, e)
                return@withContext Result.failure(e)
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
                return@withContext Result.success(user)
            } catch (e: AppwriteException) {
                val errorMessage = "Appwrite registration failed: ${e.message}"
                Log.e("AuthRepository", errorMessage, e)
                return@withContext Result.failure(e)
            } catch (e: Exception) {
                val errorMessage = "Registration failed: ${e.message}"
                Log.e("AuthRepository", errorMessage, e)
                return@withContext Result.failure(e)
            }
        }
    }

    suspend fun loginWithGoogle(idToken: String): Result<Session> {
        Log.d("AuthRepository", "Attempting Google login")
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthRepository", "Checking for existing session")
                // First check if there's already an active session
                val currentSession = getCurrentSession()
                if (currentSession.isSuccess) {
                    Log.d("AuthRepository", "Existing session found, deleting it before creating new one")
                    // Delete existing session before creating a new one
                    try {
                        appwriteService.account.deleteSession("current")
                        Log.d("AuthRepository", "Existing session deleted successfully")
                    } catch (e: Exception) {
                        Log.w("AuthRepository", "Failed to delete existing session: ${e.message}", e)
                    }
                }

                Log.d("AuthRepository", "Calling Appwrite Google OAuth login API")
                // This is a placeholder - in a real implementation,
                // you would need to handle the OAuth callback properly
                // The actual OAuth flow is handled through the OAuthService
                // and OAuthCallbackActivity
                return@withContext Result.failure(Exception("Google OAuth flow should be handled through OAuthService"))
            } catch (e: AppwriteException) {
                val errorMessage = "Appwrite Google login failed: ${e.message}"
                Log.e("AuthRepository", errorMessage, e)
                return@withContext Result.failure(e)
            } catch (e: Exception) {
                val errorMessage = "Google login failed: ${e.message}"
                Log.e("AuthRepository", errorMessage, e)
                return@withContext Result.failure(e)
            }
        }
    }

    suspend fun registerWithGoogle(idToken: String, name: String, email: String): Result<AppwriteUser<Map<String, Any>>> {
        Log.d("AuthRepository", "Attempting Google registration for email: $email")
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthRepository", "Calling Appwrite Google OAuth registration API")
                // This is a placeholder - in a real implementation,
                // you would need to handle the OAuth callback properly
                // The actual OAuth flow is handled through the OAuthService
                // and OAuthCallbackActivity
                return@withContext Result.failure(Exception("Google OAuth flow should be handled through OAuthService"))
            } catch (e: AppwriteException) {
                val errorMessage = "Appwrite Google registration failed: ${e.message}"
                Log.e("AuthRepository", errorMessage, e)
                return@withContext Result.failure(e)
            } catch (e: Exception) {
                val errorMessage = "Google registration failed: ${e.message}"
                Log.e("AuthRepository", errorMessage, e)
                return@withContext Result.failure(e)
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

                // Clear stored user info
                clearUserInfo()

                return@withContext Result.success(Unit)
            } catch (e: AppwriteException) {
                val errorMessage = "Appwrite logout failed: ${e.message}"
                Log.e("AuthRepository", errorMessage, e)
                return@withContext Result.failure(e)
            } catch (e: Exception) {
                val errorMessage = "Logout failed: ${e.message}"
                Log.e("AuthRepository", errorMessage, e)
                return@withContext Result.failure(e)
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
                return@withContext Result.success(user)
            } catch (e: AppwriteException) {
                val errorMessage = "Failed to fetch current user: ${e.message}"
                Log.e("AuthRepository", errorMessage, e)
                return@withContext Result.failure(e)
            } catch (e: Exception) {
                val errorMessage = "Failed to fetch current user: ${e.message}"
                Log.e("AuthRepository", errorMessage, e)
                return@withContext Result.failure(e)
            }
        }
    }

    // Check if user has valid stored credentials without making network call
    suspend fun checkStoredCredentials(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthRepository", "Checking stored credentials")
                val hasValidCredentials = hasValidStoredCredentials()
                Log.d("AuthRepository", "Stored credentials valid: $hasValidCredentials")
                return@withContext Result.success(hasValidCredentials)
            } catch (e: Exception) {
                Log.e("AuthRepository", "Error checking stored credentials: ${e.message}", e)
                return@withContext Result.failure(e)
            }
        }
    }

    /**
     * Checks if there's a valid Appwrite session
     */
    suspend fun hasActiveSession(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthRepository", "Checking for active session")
                // Try to get current session from Appwrite
                val session = appwriteService.account.getSession("current")
                Log.d("AuthRepository", "Active session found: ${session.userId}")
                return@withContext Result.success(true)
            } catch (e: AppwriteException) {
                Log.d("AuthRepository", "No active session: ${e.message}")
                return@withContext Result.success(false)
            } catch (e: Exception) {
                Log.d("AuthRepository", "Session check error: ${e.message}")
                return@withContext Result.success(false)
            }
        }
    }

    /**
     * Gets current user if session exists
     */
    suspend fun getCurrentUserIfLoggedIn(): Result<User?> {
        return withContext(Dispatchers.IO) {
            try {
                // First check if session exists
                val sessionCheck = hasActiveSession()
                if (sessionCheck.getOrNull() == true) {
                    // Session exists, get user
                    val account = appwriteService.account.get()
                    val user = User(
                        id = account.id,
                        name = account.name,
                        email = account.email,
                        createdAt = account.registration ?: "",
                        updatedAt = ""
                    )
                    return@withContext Result.success(user)
                } else {
                    // No session
                    return@withContext Result.success(null)
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Failed to get current user: ${e.message}")
                return@withContext Result.success(null)
            }
        }
    }

    // New method to check if there's a current session
    private suspend fun getCurrentSession(): Result<Session> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthRepository", "Checking for current session")
                val session = appwriteService.account.getSession("current")
                Log.d("AuthRepository", "Current session found")
                Result.success(session)
            } catch (e: AppwriteException) {
                Log.d("AuthRepository", "No current session found: ${e.message}")
                Result.failure(e)
            } catch (e: Exception) {
                Log.d("AuthRepository", "No current session found: ${e.message}")
                Result.failure(e)
            }
        }
    }

    // Store user info securely after successful login
    private fun storeUserInfo(email: String) {
        try {
            Log.d("AuthRepository", "Storing user info for email: $email")
            securePreferences.putString(PREF_USER_EMAIL, email)
            securePreferences.putLong(PREF_LOGIN_TIMESTAMP, System.currentTimeMillis())
            Log.d("AuthRepository", "User info stored successfully")
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to store user info: ${e.message}", e)
        }
    }

    // Clear stored user info on logout
    private fun clearUserInfo() {
        try {
            Log.d("AuthRepository", "Clearing stored user info")
            securePreferences.remove(PREF_USER_EMAIL)
            securePreferences.remove(PREF_USER_ID)
            securePreferences.remove(PREF_USER_NAME)
            securePreferences.remove(PREF_LOGIN_TIMESTAMP)
            Log.d("AuthRepository", "Stored user info cleared successfully")
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to clear user info: ${e.message}", e)
        }
    }

    // Check if user has valid stored credentials
    fun hasValidStoredCredentials(): Boolean {
        return try {
            val email = securePreferences.getString(PREF_USER_EMAIL, null)
            val timestamp = securePreferences.getLong(PREF_LOGIN_TIMESTAMP, 0)

            // Check if we have stored credentials and they're not too old (e.g., 30 days)
            email != null && timestamp > 0 &&
                (System.currentTimeMillis() - timestamp) < (30 * 24 * 60 * 60 * 1000L)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error checking stored credentials: ${e.message}", e)
            false
        }
    }
    
    /**
     * Update user password
     * @param currentPassword The user's current password
     * @param newPassword The new password to set
     * @return Result indicating success or failure
     */
    suspend fun updatePassword(currentPassword: String, newPassword: String): Result<Unit> {
        Log.d("AuthRepository", "Attempting to update user password")
        Log.d("AuthRepository", "Current password length: ${currentPassword.length}")
        Log.d("AuthRepository", "New password length: ${newPassword.length}")
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthRepository", "Calling Appwrite update password API")
                val result = appwriteService.account.updatePassword(
                    password = newPassword,
                    oldPassword = currentPassword
                )
                Log.d("AuthRepository", "Password updated successfully")
                Log.d("AuthRepository", "Update password result: $result")
                return@withContext Result.success(Unit)
            } catch (e: AppwriteException) {
                val errorMessage = "Appwrite password update failed: ${e.message}"
                Log.e("AuthRepository", errorMessage, e)
                Log.e("AuthRepository", "Appwrite error code: ${e.code}")
                Log.e("AuthRepository", "Appwrite error type: ${e.type}")
                return@withContext Result.failure(e)
            } catch (e: Exception) {
                val errorMessage = "Password update failed: ${e.message}"
                Log.e("AuthRepository", errorMessage, e)
                return@withContext Result.failure(e)
            }
        }
    }
}