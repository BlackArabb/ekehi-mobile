package com.ekehi.network.service

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
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
    private val client: Client
) {
    private val account: Account = Account(client)
    private val scope = CoroutineScope(Dispatchers.Main)

    companion object {
        const val SUCCESS_URL = "ekehi://oauth/return"
        const val FAILURE_URL = "ekehi://auth"
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
                    val currentSession = account.getSession("current")
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
                Result.success(true)
            } catch (e: Exception) {
                Log.e("OAuthService", "Failed to create OAuth session: ${e.message}", e)
                Result.failure(e)
            }
        }
    }
}