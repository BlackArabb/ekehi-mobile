package com.ekehi.network.data.repository

import android.content.Context
import android.util.Log
import com.ekehi.network.data.model.User
import com.ekehi.network.security.SecurePreferences
import com.ekehi.network.service.AppwriteService
import com.ekehi.network.service.MiningManager
import com.ekehi.network.util.EventBus
import com.ekehi.network.util.Event
import io.appwrite.ID
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.Session
import io.appwrite.models.User as AppwriteUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepository @Inject constructor(
        private val appwriteService: AppwriteService,
        private val securePreferences: SecurePreferences,
        private val userRepository: UserRepository,
        private val context: Context,
        private val miningManager: MiningManager
) {
    companion object {
        private const val TAG = "AuthRepository"
        private const val PREF_USER_ID = "user_id"
        private const val PREF_USER_EMAIL = "user_email"
        private const val PREF_USER_NAME = "user_name"
        private const val PREF_LOGIN_TIMESTAMP = "login_timestamp"
    }

    suspend fun login(email: String, password: String): Result<Session> {
        Log.d(TAG, "Attempting login for email: $email")
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Checking for existing session")
                // First check if there's already an active session
                val currentSession = getCurrentSession()
                if (currentSession.isSuccess) {
                    Log.d(TAG, "Existing session found, deleting it before creating new one")
                    // Delete existing session before creating a new one
                    try {
                        appwriteService.account.deleteSession("current")
                        Log.d(TAG, "Existing session deleted successfully")
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to delete existing session: ${e.message}", e)
                    }
                }

                Log.d(TAG, "Calling Appwrite login API")
                val session = appwriteService.account.createEmailPasswordSession(
                        email = email,
                        password = password
                )
                Log.d(TAG, "Login successful for email: $email")

                // Store user info securely after successful login
                storeUserInfo(email)

                return@withContext Result.success(session)
            } catch (e: AppwriteException) {
                val errorMessage = "Appwrite login failed: ${e.message}"
                Log.e(TAG, errorMessage, e)
                return@withContext Result.failure(e)
            } catch (e: Exception) {
                val errorMessage = "Login failed: ${e.message}"
                Log.e(TAG, errorMessage, e)
                return@withContext Result.failure(e)
            }
        }
    }

    suspend fun register(email: String, password: String, name: String, referralCode: String = "", phoneNumber: String = "", country: String = ""): Result<AppwriteUser<Map<String, Any>>> {
        android.util.Log.e("REGISTRATION_TEST", "===== REGISTRATION STARTED =====")
        Log.d(TAG, "=== REGISTRATION STARTED ===")
        Log.d(TAG, "Email: $email, Name: $name, Phone: $phoneNumber, Country: $country")
        return withContext(Dispatchers.IO) {
            try {
                // Step 0: CRITICAL - Delete any existing session first
                Log.d(TAG, "Step 0: Checking for existing session")
                try {
                    val currentSession = getCurrentSession()
                    if (currentSession.isSuccess) {
                        Log.d(TAG, "Existing session found, deleting it before registration")
                        appwriteService.account.deleteSession("current")
                        Log.d(TAG, "✅ Existing session deleted")
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "No existing session to delete")
                }
                
                // Step 1: Create the Appwrite account
                Log.d(TAG, "Step 1: Creating Appwrite account")
                val user = appwriteService.account.create(
                    userId = ID.unique(),
                    email = email,
                    password = password,
                    name = name
                )
                Log.d(TAG, "✅ Account created successfully - userId: ${user.id}")
                
                // Step 2: Auto-login the user immediately
                Log.d(TAG, "Step 2: Auto-logging in user")
                try {
                    val session = appwriteService.account.createEmailPasswordSession(
                        email = email,
                        password = password
                    )
                    Log.d(TAG, "✅ Auto-login successful - sessionId: ${session.id}")
                    
                    // Store user info after successful login
                    storeUserInfo(email)
                    Log.d(TAG, "✅ User info stored")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Auto-login failed: ${e.message}", e)
                    // This is critical - if auto-login fails, profile creation will also fail
                    // So we should return failure here
                    return@withContext Result.failure(Exception("Registration succeeded but auto-login failed: ${e.message}"))
                }
                
                // Step 3: Store referral code if provided
                if (referralCode.isNotEmpty()) {
                    try {
                        securePreferences.putString("referral_code", referralCode)
                        Log.d(TAG, "✅ Stored referral code: $referralCode")
                    } catch (e: Exception) {
                        Log.w(TAG, "⚠️ Failed to store referral code: ${e.message}", e)
                    }
                }
                
                // Step 4: Create user profile (now that user is authenticated)
                Log.d(TAG, "About to create user profile for regular registration")
                try {
                    Log.d(TAG, "Step 4: Creating user profile for userId: ${user.id}")
                    val profileResult = userRepository.createUserProfile(user.id, name, email, phoneNumber, country)
                    if (profileResult.isSuccess) {
                        val profile = profileResult.getOrNull()
                        if (profile != null) {
                            Log.d(TAG, "✅ User profile created successfully")
                            Log.d(TAG, "   Username: ${profile.username}")
                            Log.d(TAG, "   Email: ${profile.email}")
                            Log.d(TAG, "   Phone: ${profile.phoneNumber}")
                            Log.d(TAG, "   Country: ${profile.country}")
                            Log.d(TAG, "   Task Reward: ${profile.taskReward}")
                            Log.d(TAG, "   Referral Code: ${profile.referralCode}")
                        } else {
                            Log.e(TAG, "⚠️ Profile created but is null")
                            return@withContext Result.failure(Exception("Profile created but is null"))
                        }
                    } else {
                        val error = profileResult.exceptionOrNull()
                        Log.e(TAG, "❌ Failed to create user profile: ${error?.message}")
                        return@withContext Result.failure(Exception("Failed to create user profile: ${error?.message}"))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Exception while creating user profile: ${e.message}", e)
                    return@withContext Result.failure(e)
                }
                Log.d(TAG, "Completed user profile creation attempt for regular registration")
                
                Log.d(TAG, "=== REGISTRATION COMPLETED SUCCESSFULLY ===")
                return@withContext Result.success(user)
            } catch (e: AppwriteException) {
                val errorMessage = "Appwrite registration failed: ${e.message}"
                Log.e(TAG, "❌ $errorMessage", e)
                return@withContext Result.failure(e)
            } catch (e: Exception) {
                val errorMessage = "Registration failed: ${e.message}"
                Log.e(TAG, "❌ $errorMessage", e)
                return@withContext Result.failure(e)
            }
        }
    }

    suspend fun loginWithGoogle(idToken: String): Result<Session> {
        Log.d(TAG, "Attempting Google login")
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Checking for existing session")
                // First check if there's already an active session
                val currentSession = getCurrentSession()
                if (currentSession.isSuccess) {
                    Log.d(TAG, "Existing session found, deleting it before creating new one")
                    // Delete existing session before creating a new one
                    try {
                        appwriteService.account.deleteSession("current")
                        Log.d(TAG, "Existing session deleted successfully")
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to delete existing session: ${e.message}", e)
                    }
                }
                    
                Log.d(TAG, "Google OAuth flow should be handled through OAuthService")
                // This function should not be called directly for OAuth login
                // OAuth login is handled through the OAuthService and OAuthCallbackActivity
                // Return failure to indicate this function should not be used
                return@withContext Result.failure(Exception("Google OAuth flow should be handled through OAuthService"))
            } catch (e: AppwriteException) {
                val errorMessage = "Appwrite Google login failed: ${e.message}"
                Log.e(TAG, errorMessage, e)
                return@withContext Result.failure(e)
            } catch (e: Exception) {
                val errorMessage = "Google login failed: ${e.message}"
                Log.e(TAG, errorMessage, e)
                return@withContext Result.failure(e)
            }
        }
    }

    suspend fun registerWithGoogle(idToken: String, name: String, email: String, phoneNumber: String = "", country: String = ""): Result<AppwriteUser<Map<String, Any>>> {
        Log.d(TAG, "=== GOOGLE REGISTRATION STARTED ===")
        Log.d(TAG, "Email: $email, Name: $name, Phone: $phoneNumber, Country: $country")
        return withContext(Dispatchers.IO) {
            try {
                // Step 1: Create the Appwrite account
                Log.d(TAG, "Step 1: Creating Appwrite account via Google OAuth")
                val user = appwriteService.account.create(
                    userId = ID.unique(),
                    email = email,
                    password = "", // OAuth users don't have passwords
                    name = name
                )
                Log.d(TAG, "✅ Google account created successfully - userId: ${user.id}")
                
                // Note: For Google OAuth, the user should already be logged in via the OAuth flow
                // But we'll verify by checking if we can get the current user
                try {
                    val currentUser = appwriteService.account.get()
                    Log.d(TAG, "✅ User is already logged in via OAuth - userId: ${currentUser.id}")
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ User not logged in after OAuth registration: ${e.message}")
                }
                
                // Step 2: Create user profile
                Log.d(TAG, "About to create user profile for Google registration")
                try {
                    Log.d(TAG, "Step 2: Creating user profile for userId: ${user.id}")
                    val profileResult = userRepository.createUserProfile(user.id, name, email, phoneNumber, country)
                    if (profileResult.isSuccess) {
                        val profile = profileResult.getOrNull()
                        if (profile != null) {
                            Log.d(TAG, "✅ User profile created successfully")
                            Log.d(TAG, "   Username: ${profile.username}")
                            Log.d(TAG, "   Email: ${profile.email}")
                            Log.d(TAG, "   Phone: ${profile.phoneNumber}")
                            Log.d(TAG, "   Country: ${profile.country}")
                            Log.d(TAG, "   Task Reward: ${profile.taskReward}")
                            Log.d(TAG, "   Referral Code: ${profile.referralCode}")
                        } else {
                            Log.e(TAG, "⚠️ Profile created but is null")
                            return@withContext Result.failure(Exception("Profile created but is null"))
                        }
                    } else {
                        val error = profileResult.exceptionOrNull()
                        Log.e(TAG, "❌ Failed to create user profile: ${error?.message}")
                        return@withContext Result.failure(Exception("Failed to create user profile: ${error?.message}"))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Exception while creating user profile: ${e.message}", e)
                    return@withContext Result.failure(e)
                }
                Log.d(TAG, "Completed user profile creation attempt for Google registration")
                
                Log.d(TAG, "=== GOOGLE REGISTRATION COMPLETED SUCCESSFULLY ===")
                return@withContext Result.success(user)
            } catch (e: AppwriteException) {
                val errorMessage = "Appwrite Google registration failed: ${e.message}"
                Log.e(TAG, "❌ $errorMessage", e)
                return@withContext Result.failure(e)
            } catch (e: Exception) {
                val errorMessage = "Google registration failed: ${e.message}"
                Log.e(TAG, "❌ $errorMessage", e)
                return@withContext Result.failure(e)
            }
        }
    }

    suspend fun logout(): Result<Unit> {
        Log.d("AuthRepository", "=== LOGOUT STARTED ===")
        return withContext(Dispatchers.IO) {
            try {
                // Step 1: Notify ViewModels to stop mining BEFORE clearing data
                Log.d("AuthRepository", "Step 1: Notifying ViewModels to reset state")
                EventBus.sendEvent(Event.UserLoggedOut)
                
                // Step 2: Stop mining service
                Log.d("AuthRepository", "Step 2: Stopping mining service")
                stopMiningService()
                Log.d("AuthRepository", "✅ Mining service stopped")
                
                // Step 3: Clear mining session data
                Log.d("AuthRepository", "Step 3: Clearing mining session data")
                clearMiningSession()
                Log.d("AuthRepository", "✅ Mining session cleared")
                
                // Step 4: Clear stored user info
                Log.d("AuthRepository", "Step 4: Clearing user info")
                clearUserInfo()
                Log.d("AuthRepository", "✅ Local user info cleared")
                
                // Step 5: Delete Appwrite session
                Log.d("AuthRepository", "Step 5: Calling Appwrite logout API")
                appwriteService.account.deleteSession("current")
                Log.d("AuthRepository", "✅ Appwrite session deleted")

                Log.d("AuthRepository", "=== LOGOUT COMPLETED SUCCESSFULLY ===")
                return@withContext Result.success(Unit)
            } catch (e: AppwriteException) {
                val errorMessage = "Appwrite logout failed: ${e.message}"
                Log.e("AuthRepository", "❌ $errorMessage", e)
                // Even if Appwrite logout fails, clear local data
                EventBus.sendEvent(Event.UserLoggedOut)
                stopMiningService()
                clearMiningSession()
                clearUserInfo()
                return@withContext Result.failure(e)
            } catch (e: Exception) {
                val errorMessage = "Logout failed: ${e.message}"
                Log.e("AuthRepository", "❌ $errorMessage", e)
                // Even if logout fails, clear local data
                EventBus.sendEvent(Event.UserLoggedOut)
                stopMiningService()
                clearMiningSession()
                clearUserInfo()
                return@withContext Result.failure(e)
            }
        }
    }

    // Stop mining service on logout
    private fun stopMiningService() {
        try {
            Log.d(TAG, "Stopping MiningManager...")
            miningManager.stopMining()
            Log.d(TAG, "MiningManager stopped successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop mining service: ${e.message}", e)
        }
    }

    // Clear mining session data on logout - ENHANCED VERSION
    private fun clearMiningSession() {
        try {
            Log.d(TAG, "=== CLEARING MINING SESSION ===")
            
            // Clear from mining_prefs SharedPreferences
            val miningPrefs = context.getSharedPreferences("mining_prefs", Context.MODE_PRIVATE)
            val editor = miningPrefs.edit()
            
            // Clear all mining-related keys
            editor.remove("miningSession")
            editor.remove("mining_session")
            editor.remove("mining_start_time")
            editor.remove("mining_end_time")
            editor.remove("is_mining")
            editor.remove("mining_active")
            editor.remove("last_mining_update")
            editor.remove("mined_coins")
            editor.remove("mining_duration")
            editor.apply()
            
            Log.d(TAG, "✅ Mining session cleared from SharedPreferences")
            
            // Also clear from default SharedPreferences if used
            try {
                val defaultPrefs = context.getSharedPreferences(
                    "${context.packageName}_preferences", 
                    Context.MODE_PRIVATE
                )
                defaultPrefs.edit().apply {
                    remove("miningSession")
                    remove("mining_session")
                    apply()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not clear default preferences: ${e.message}")
            }
            
            Log.d(TAG, "✅ Mining session cleared successfully")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to clear mining session: ${e.message}", e)
        }
    }

    suspend fun getCurrentUser(): Result<User> {
        Log.d(TAG, "Fetching current user")
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Calling Appwrite get account API")
                val account = appwriteService.account.get()
                val user = User(
                        id = account.id,
                        name = account.name,
                        email = account.email,
                        createdAt = account.registration ?: "",
                        updatedAt = ""
                )
                Log.d(TAG, "Current user fetched successfully: ${user.id}")
                return@withContext Result.success(user)
            } catch (e: AppwriteException) {
                val errorMessage = "Failed to fetch current user: ${e.message}"
                Log.e(TAG, errorMessage, e)
                return@withContext Result.failure(e)
            } catch (e: Exception) {
                val errorMessage = "Failed to fetch current user: ${e.message}"
                Log.e(TAG, errorMessage, e)
                return@withContext Result.failure(e)
            }
        }
    }

    // Check if user has valid stored credentials without making network call
    suspend fun checkStoredCredentials(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Checking stored credentials")
                val hasValidCredentials = hasValidStoredCredentials()
                Log.d(TAG, "Stored credentials valid: $hasValidCredentials")
                return@withContext Result.success(hasValidCredentials)
            } catch (e: Exception) {
                Log.e(TAG, "Error checking stored credentials: ${e.message}", e)
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
                Log.d(TAG, "Checking for active session")
                // Try to get current session from Appwrite
                val session = appwriteService.account.getSession("current")
                Log.d(TAG, "Active session found: ${session.userId}")
                return@withContext Result.success(true)
            } catch (e: AppwriteException) {
                Log.d(TAG, "No active session: ${e.message}")
                return@withContext Result.success(false)
            } catch (e: Exception) {
                Log.d(TAG, "Session check error: ${e.message}")
                return@withContext Result.success(false)
            }
        }
    }
    
    /**
     * Checks if there's a valid Appwrite session and user profile exists
     */
    suspend fun isAuthenticatedWithProfile(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Checking for active session and user profile")
                // First check if there's an active session
                val sessionResult = hasActiveSession()
                if (sessionResult.getOrNull() == true) {
                    // Session exists, now check if user profile exists
                    val userResult = getCurrentUserIfLoggedIn()
                    if (userResult.isSuccess && userResult.getOrNull() != null) {
                        val user = userResult.getOrNull()!!
                        // Check if user profile exists
                        val profileResult = userRepository.getUserProfile(user.id)
                        if (profileResult.isSuccess) {
                            Log.d(TAG, "User is authenticated and profile exists")
                            return@withContext Result.success(true)
                        } else {
                            Log.d(TAG, "User is authenticated but profile does not exist")
                            return@withContext Result.success(false)
                        }
                    } else {
                        Log.d(TAG, "User is authenticated but failed to get user data")
                        return@withContext Result.success(false)
                    }
                } else {
                    Log.d(TAG, "No active session")
                    return@withContext Result.success(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking authentication with profile: ${e.message}", e)
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
                Log.e(TAG, "Failed to get current user: ${e.message}")
                return@withContext Result.success(null)
            }
        }
    }

    // New method to check if there's a current session
    private suspend fun getCurrentSession(): Result<Session> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Checking for current session")
                val session = appwriteService.account.getSession("current")
                Log.d(TAG, "Current session found")
                Result.success(session)
            } catch (e: AppwriteException) {
                Log.d(TAG, "No current session found: ${e.message}")
                Result.failure(e)
            } catch (e: Exception) {
                Log.d(TAG, "No current session found: ${e.message}")
                Result.failure(e)
            }
        }
    }

    // Store user info securely after successful login
    private fun storeUserInfo(email: String) {
        try {
            Log.d(TAG, "Storing user info for email: $email")
            securePreferences.putString(PREF_USER_EMAIL, email)
            securePreferences.putLong(PREF_LOGIN_TIMESTAMP, System.currentTimeMillis())
            Log.d(TAG, "User info stored successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to store user info: ${e.message}", e)
        }
    }

    // Clear stored user info on logout
    private fun clearUserInfo() {
        try {
            Log.d(TAG, "Clearing stored user info")
            securePreferences.remove(PREF_USER_EMAIL)
            securePreferences.remove(PREF_USER_ID)
            securePreferences.remove(PREF_USER_NAME)
            securePreferences.remove(PREF_LOGIN_TIMESTAMP)
            // Also clear the referral code if it exists
            securePreferences.remove("referral_code")
            Log.d(TAG, "Stored user info cleared successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear user info: ${e.message}", e)
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
            Log.e(TAG, "Error checking stored credentials: ${e.message}", e)
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
        Log.d(TAG, "Attempting to update user password")
        Log.d(TAG, "Current password length: ${currentPassword.length}")
        Log.d(TAG, "New password length: ${newPassword.length}")
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Calling Appwrite update password API")
                val result = appwriteService.account.updatePassword(
                    password = newPassword,
                    oldPassword = currentPassword
                )
                Log.d(TAG, "Password updated successfully")
                Log.d(TAG, "Update password result: $result")
                return@withContext Result.success(Unit)
            } catch (e: AppwriteException) {
                val errorMessage = "Appwrite password update failed: ${e.message}"
                Log.e(TAG, errorMessage, e)
                Log.e(TAG, "Appwrite error code: ${e.code}")
                Log.e(TAG, "Appwrite error type: ${e.type}")
                return@withContext Result.failure(e)
            } catch (e: Exception) {
                val errorMessage = "Password update failed: ${e.message}"
                Log.e(TAG, errorMessage, e)
                return@withContext Result.failure(e)
            }
        }
    }

    /**
     * Creates a user profile if one doesn't exist for the current user
     */
    suspend fun createUserProfileIfNotExists(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Checking if user profile exists")
                
                // Get current user
                val currentUserResult = getCurrentUserIfLoggedIn()
                if (currentUserResult.isSuccess) {
                    val currentUser = currentUserResult.getOrNull()
                    if (currentUser != null) {
                        // Check if profile exists
                        val profileResult = userRepository.getUserProfile(currentUser.id)
                        if (profileResult.isFailure) {
                            // Profile doesn't exist, create it
                            Log.d(TAG, "User profile not found, creating new profile for userId: ${currentUser.id}")
                            val createResult = userRepository.createUserProfile(currentUser.id, currentUser.name, currentUser.email, "", "")
                            if (createResult.isSuccess) {
                                Log.d(TAG, "User profile created successfully")
                                Log.d(TAG, "   Email: ${createResult.getOrNull()?.email}")
                                Log.d(TAG, "   Phone: ${createResult.getOrNull()?.phoneNumber}")
                                Log.d(TAG, "   Country: ${createResult.getOrNull()?.country}")
                                return@withContext Result.success(Unit)
                            } else {
                                val error = createResult.exceptionOrNull()
                                Log.e(TAG, "Failed to create user profile: ${error?.message}")
                                return@withContext Result.failure(error ?: Exception("Failed to create user profile"))
                            }
                        } else {
                            // Profile exists
                            Log.d(TAG, "User profile already exists for userId: ${currentUser.id}")
                            return@withContext Result.success(Unit)
                        }
                    } else {
                        Log.e(TAG, "No current user found")
                        return@withContext Result.failure(Exception("No current user found"))
                    }
                } else {
                    val error = currentUserResult.exceptionOrNull()
                    Log.e(TAG, "Failed to get current user: ${error?.message}")
                    return@withContext Result.failure(error ?: Exception("Failed to get current user"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in createUserProfileIfNotExists: ${e.message}", e)
                return@withContext Result.failure(e)
            }
        }
    }
}