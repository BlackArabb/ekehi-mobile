package com.ekehi.network.service

import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import com.ekehi.network.data.repository.UserRepository
import io.appwrite.Client
import io.appwrite.services.Account
import io.appwrite.enums.OAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OAuthService @Inject constructor(
    private val context: Context,
    private val client: Client,
    private val userRepository: UserRepository
) {
    private val account: Account = Account(client)
    private val scope = CoroutineScope(Dispatchers.Main)

    companion object {
        const val SUCCESS_URL = "appwrite-callback-68c2dd6e002112935ed2://oauth2/success"
        const val FAILURE_URL = "appwrite-callback-68c2dd6e002112935ed2://oauth2/failure"
    }

    fun initiateGoogleOAuth(activity: ComponentActivity) {
        Log.d("OAuthService", "Initiating Google OAuth flow")
        scope.launch {
            try {
                // Delete any existing session before starting OAuth
                try {
                    Log.d("OAuthService", "Checking for existing session before OAuth")
                    account.getSession("current")
                    Log.d("OAuthService", "Existing session found, deleting it")
                    account.deleteSession("current")
                    Log.d("OAuthService", "✅ Existing session deleted")
                } catch (e: Exception) {
                    Log.d("OAuthService", "No existing session to delete")
                }
                
                // Start the OAuth flow
                account.createOAuth2Token(
                    provider = OAuthProvider.GOOGLE,
                    success = SUCCESS_URL,
                    failure = FAILURE_URL,
                    activity = activity
                )
                Log.d("OAuthService", "Google OAuth flow initiated successfully")
            } catch (e: Exception) {
                Log.e("OAuthService", "Failed to initiate Google OAuth: ${e.message}", e)
            }
        }
    }
    
    suspend fun handleOAuthCallback(userId: String, secret: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("OAuthService", "=== HANDLING OAUTH CALLBACK ===")
                Log.d("OAuthService", "UserId: $userId")
                
                // Step 1: Create session using the OAuth token
                Log.d("OAuthService", "Step 1: Creating OAuth session")
                account.createSession(
                    userId = userId,
                    secret = secret
                )
                Log.d("OAuthService", "✅ OAuth session created successfully")
                
                // Step 2: Get user info
                Log.d("OAuthService", "Step 2: Getting user info")
                val appwriteUser = account.get()
                val username = appwriteUser.name.ifEmpty { appwriteUser.email.substringBefore("@") }
                val email = appwriteUser.email
                Log.d("OAuthService", "✅ User info retrieved - Username: $username, Email: $email")
                
                // Step 3: Check if user profile exists, create if it doesn't
                Log.d("OAuthService", "Step 3: Checking/creating user profile")
                val profileResult = userRepository.getUserProfile(userId)
                if (profileResult.isFailure) {
                    Log.d("OAuthService", "User profile not found, creating new profile")
                    
                    val createProfileResult = userRepository.createUserProfile(userId, username, email, "", "")
                    if (createProfileResult.isSuccess) {
                        Log.d("OAuthService", "✅ User profile created successfully")
                        Log.d("OAuthService", "   Email: ${createProfileResult.getOrNull()?.email}")
                        Log.d("OAuthService", "   Phone: ${createProfileResult.getOrNull()?.phoneNumber}")
                        Log.d("OAuthService", "   Country: ${createProfileResult.getOrNull()?.country}")
                    } else {
                        Log.e("OAuthService", "❌ Failed to create user profile: ${createProfileResult.exceptionOrNull()?.message}")
                        return@withContext Result.failure(
                            createProfileResult.exceptionOrNull() ?: Exception("Failed to create user profile")
                        )
                    }
                } else {
                    Log.d("OAuthService", "✅ User profile already exists")
                }
                
                Log.d("OAuthService", "=== OAUTH CALLBACK COMPLETED SUCCESSFULLY ===")
                return@withContext Result.success(true)
            } catch (e: Exception) {
                Log.e("OAuthService", "❌ Failed to process OAuth callback: ${e.message}", e)
                return@withContext Result.failure(e)
            }
        }
    }
}