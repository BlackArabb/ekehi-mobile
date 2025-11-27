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
                // Start the OAuth flow - this will redirect to the OAuth provider
                // and then back to our app via the callback URLs
                account.createOAuth2Token(
                    provider = OAuthProvider.GOOGLE,
                    success = SUCCESS_URL,
                    failure = FAILURE_URL,
                    activity = activity
                )
                Log.d("OAuthService", "Google OAuth flow initiated successfully")
            } catch (e: Exception) {
                Log.e("OAuthService", "Failed to initiate Google OAuth: ${e.message}", e)
                e.printStackTrace()
            }
        }
    }
    
    // This method would be called when we receive the OAuth callback
    suspend fun handleOAuthCallback(userId: String, secret: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("OAuthService", "Handling OAuth callback for userId: $userId")
                // Check if there's already an active session
                try {
                    account.getSession("current")
                    Log.d("OAuthService", "Existing session found, deleting it before creating new one")
                    // Delete existing session before creating a new one
                    account.deleteSession("current")
                    Log.d("OAuthService", "Existing session deleted successfully")
                } catch (e: Exception) {
                    Log.d("OAuthService", "No existing session found or failed to delete: ${e.message}")
                }
                
                // Create a session using the OAuth token
                account.createSession(
                    userId = userId,
                    secret = secret
                )
                Log.d("OAuthService", "OAuth session created successfully")
                
                // Check if user profile exists, create one if it doesn't
                try {
                    val profileResult = userRepository.getUserProfile(userId)
                    if (profileResult.isFailure) {
                        Log.d("OAuthService", "User profile not found, creating new profile")
                        // Get user info from Appwrite account
                        val appwriteUser = account.get()
                        val username = appwriteUser.name.ifEmpty { appwriteUser.email.substringBefore("@") }
                        
                        // Create user profile and wait for completion
                        val createProfileResult = userRepository.createUserProfile(userId, username)
                        if (createProfileResult.isSuccess) {
                            Log.d("OAuthService", "User profile created successfully")
                        } else {
                            Log.e("OAuthService", "Failed to create user profile: ${createProfileResult.exceptionOrNull()?.message}")
                            // Return failure if we couldn't create the profile
                            return@withContext Result.failure(createProfileResult.exceptionOrNull() ?: Exception("Failed to create user profile"))
                        }
                    } else {
                        Log.d("OAuthService", "User profile already exists")
                    }
                } catch (e: Exception) {
                    Log.e("OAuthService", "Error checking/creating user profile: ${e.message}", e)
                    // Return failure if there was an error checking/creating the profile
                    return@withContext Result.failure(e)
                }
                
                return@withContext Result.success(true)
            } catch (e: Exception) {
                Log.e("OAuthService", "Failed to create OAuth session: ${e.message}", e)
                return@withContext Result.failure(e)
            }
        }
    }
}
