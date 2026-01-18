package com.ekehi.network.service

import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import com.ekehi.network.auth.SocialAuthManager
import com.ekehi.network.data.repository.UserRepository
import com.ekehi.network.util.DebugLogger
import io.appwrite.Client
import io.appwrite.models.User as AppwriteUser
import io.appwrite.services.Account as AccountService
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
    private val userRepository: UserRepository,
    private val socialAuthManager: SocialAuthManager
) {
    private val account: AccountService = AccountService(client)
    private val scope = CoroutineScope(Dispatchers.Main)

    companion object {
        const val SUCCESS_URL = "appwrite-callback-68c2dd6e002112935ed2://oauth2/success"
        const val FAILURE_URL = "appwrite-callback-68c2dd6e002112935ed2://oauth2/failure"
        const val GOOGLE_SIGN_IN_REQUEST_CODE = 1001
    }

    fun initiateGoogleOAuth(activity: ComponentActivity, forceBrowser: Boolean = false) {
        DebugLogger.logStep("INITIATE_GOOGLE_OAUTH", "Starting OAuth flow (forceBrowser=$forceBrowser)")
        
        if (!forceBrowser) {
            // Try Native Google Sign-In first for a better "in-app popup" experience
            try {
                Log.d("OAuthService", "Attempting Native Google Sign-In")
                socialAuthManager.signInWithGoogle(activity, GOOGLE_SIGN_IN_REQUEST_CODE)
                DebugLogger.logStep("NATIVE_OAUTH_TRIGGERED", "✅ Success")
                return
            } catch (e: Exception) {
                Log.e("OAuthService", "Native Google Sign-In failed, falling back to browser", e)
            }
        }
        
        // Browser-based OAuth flow
        scope.launch {
            try {
                // Delete any existing session before starting OAuth
                try {
                    DebugLogger.logStep("CHECK_EXISTING_SESSION")
                    account.getSession("current")
                    DebugLogger.logStep("DELETE_EXISTING_SESSION", "Session found, deleting")
                    account.deleteSession("current")
                    DebugLogger.logStep("SESSION_DELETED", "✅ Success")
                } catch (e: Exception) {
                    DebugLogger.logStep("NO_EXISTING_SESSION", "No session to delete")
                }
                
                // Start the OAuth flow
                // Note: SUCCESS_URL can be modified to include prompt=consent if the Appwrite server supports it
                // and correctly passes it to Google.
                account.createOAuth2Token(
                    provider = OAuthProvider.GOOGLE,
                    success = SUCCESS_URL,
                    failure = FAILURE_URL,
                    activity = activity
                )
                DebugLogger.logStep("OAUTH_FLOW_INITIATED", "✅ Successfully triggered browser")
            } catch (e: Exception) {
                DebugLogger.logError("OAUTH_INITIATION", "Failed to initiate OAuth", e)
            }
        }
    }

    suspend fun handleNativeGoogleLogin(idToken: String): Result<Map<String, Any>> {
        return withContext(Dispatchers.IO) {
            try {
                DebugLogger.logStep("NATIVE_GOOGLE_LOGIN", "Processing idToken")
                
                // Create session using Appwrite's native OAuth session method
                // For native Google sign-in, use "google" as the userId and the idToken as the secret
                account.createSession(
                    userId = "google",
                    secret = idToken
                )
                DebugLogger.logStep("SESSION_CREATED", "✅ Success")
                
                // Fetch the user to finalize the login process
                val appwriteUser = account.get()
                val userId = appwriteUser.id
                
                return@withContext finalizeLogin(userId, appwriteUser)
            } catch (e: Exception) {
                DebugLogger.logError("NATIVE_GOOGLE_LOGIN", "Failed", e)
                return@withContext Result.failure(e)
            }
        }
    }
    
    private suspend fun finalizeLogin(userId: String, appwriteUser: io.appwrite.models.User<Map<String, Any>>): Result<Map<String, Any>> {
        try {
            val name = appwriteUser.name
            val email = appwriteUser.email
            
            val username = if (!name.isNullOrEmpty()) name else email.substringBefore("@")
            
            // Check/create user profile
            val profileResult = userRepository.getUserProfile(userId)
            var userProfile = if (profileResult.isFailure) {
                userRepository.createUserProfile(userId, username, email, "", "").getOrThrow()
            } else {
                profileResult.getOrThrow()
            }
            
            val isProfileComplete = userProfile.phoneNumber.isNotEmpty() && userProfile.country.isNotEmpty()
            
            return Result.success(mapOf(
                "success" to true,
                "userId" to userId,
                "isProfileComplete" to isProfileComplete
            ))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun handleOAuthCallback(userId: String, secret: String): Result<Map<String, Any>> {
        return withContext(Dispatchers.IO) {
            try {
                DebugLogger.logStep("OAUTH_CALLBACK_START", "UserId: $userId")
                
                // Step 1: Create session
                DebugLogger.logStep("CREATE_SESSION", "Creating OAuth session")
                account.createSession(
                    userId = userId,
                    secret = secret
                )
                DebugLogger.logStep("SESSION_CREATED", "✅ Success")
                
                // Step 2: Get user info
                DebugLogger.logStep("GET_USER_INFO", "Fetching user details")
                val appwriteUser = account.get()
                val name = appwriteUser.name
                val email = appwriteUser.email
                val username = if (!name.isNullOrEmpty()) name else email.substringBefore("@")
                
                DebugLogger.logState("USER_INFO", "Retrieved", mapOf(
                    "userId" to userId,
                    "username" to username,
                    "email" to email
                ))
                
                // Step 3: Check/create user profile
                DebugLogger.logStep("CHECK_PROFILE", "Checking if profile exists")
                val profileResult = userRepository.getUserProfile(userId)
                var userProfile = if (profileResult.isFailure) {
                    DebugLogger.logStep("CREATE_PROFILE", "Profile not found, creating new")
                    
                    val createProfileResult = userRepository.createUserProfile(
                        userId, username, email, "", ""
                    )
                    
                    if (createProfileResult.isSuccess) {
                        val profile = createProfileResult.getOrThrow()
                        DebugLogger.logState("PROFILE_CREATED", "Success", mapOf(
                            "email" to profile.email,
                            "phoneNumber" to profile.phoneNumber,
                            "country" to profile.country
                        ))
                        profile
                    } else {
                        val exception = createProfileResult.exceptionOrNull()
                        DebugLogger.logError("CREATE_PROFILE", "Failed", exception as? Exception ?: Exception(exception?.message ?: "Unknown error"))
                        return@withContext Result.failure(
                            createProfileResult.exceptionOrNull() ?: Exception("Failed to create profile")
                        )
                    }
                } else {
                    val profile = profileResult.getOrThrow()
                    DebugLogger.logState("PROFILE_EXISTS", "Found", mapOf(
                        "phoneNumber" to profile.phoneNumber,
                        "country" to profile.country
                    ))
                    profile
                }
                
                // Step 4: Check completeness
                val isProfileComplete = userProfile.phoneNumber.isNotEmpty() && 
                                       userProfile.country.isNotEmpty()
                
                DebugLogger.logState("PROFILE_COMPLETENESS", "Checked", mapOf(
                    "isComplete" to isProfileComplete,
                    "phoneNumber" to userProfile.phoneNumber,
                    "country" to userProfile.country
                ))
                
                DebugLogger.logStep("OAUTH_CALLBACK_COMPLETE", "✅ All steps successful")
                
                return@withContext Result.success(mapOf(
                    "success" to true,
                    "userId" to userId,
                    "isProfileComplete" to isProfileComplete
                ))
            } catch (e: Exception) {
                DebugLogger.logError("OAUTH_CALLBACK", "Failed at some step", e)
                return@withContext Result.failure(e)
            }
        }
    }
    
    suspend fun getAccount(): AppwriteUser<Map<String, Any>>? {
        return try {
            account.get()
        } catch (e: Exception) {
            DebugLogger.logError("GET_ACCOUNT", "Failed", e)
            null
        }
    }
}