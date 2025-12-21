package com.ekehi.network.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.Scope
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocialAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private var googleSignInClient: GoogleSignInClient? = null
    private var youtubeSignInClient: GoogleSignInClient? = null
    private val facebookCallbackManager = CallbackManager.Factory.create()
    
    private val TAG = "SocialAuthManager"
    
    // ===== YOUTUBE OAUTH (FOR TASK VERIFICATION) =====
    
    /**
     * Get YouTube-specific sign-in client with YouTube API scopes
     * This is SEPARATE from your regular Google sign-in for authentication
     */
    fun getYouTubeSignInClient(): GoogleSignInClient {
        return try {
            if (youtubeSignInClient == null) {
                Log.d(TAG, "=== Creating YouTube OAuth Client (for task verification) ===")
                
                // Check if already signed in with regular Google (from sign-up/sign-in)
                val existingAccount = GoogleSignIn.getLastSignedInAccount(context)
                if (existingAccount != null) {
                    Log.d(TAG, "✓ User already signed in with Google: ${existingAccount.email}")
                    
                    // Check if existing account has YouTube scopes
                    val hasYouTubeScope = existingAccount.grantedScopes?.any { 
                        it.scopeUri.contains("youtube") 
                    } ?: false
                    
                    if (hasYouTubeScope) {
                        Log.d(TAG, "✓ Existing account already has YouTube scope - reusing")
                        // Create a client that will reuse the existing sign-in
                        youtubeSignInClient = createYouTubeClient()
                        return youtubeSignInClient!!
                    } else {
                        Log.d(TAG, "⚠ Existing account lacks YouTube scope - will request additional permissions")
                    }
                }
                
                youtubeSignInClient = createYouTubeClient()
                Log.d(TAG, "✓ YouTube OAuth Client created")
            }
            youtubeSignInClient!!
        } catch (e: Exception) {
            Log.e(TAG, "CRITICAL: Failed to create YouTube sign-in client", e)
            throw e
        }
    }
    
    /**
     * Create YouTube-specific OAuth client
     */
    private fun createYouTubeClient(): GoogleSignInClient {
        // For YouTube API access, we need YouTube-specific scopes
        // If you don't have a separate YouTube client ID, you can use the same one as sign-in
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(com.ekehi.network.BuildConfig.YOUTUBE_CLIENT_ID)
            .requestServerAuthCode(com.ekehi.network.BuildConfig.YOUTUBE_CLIENT_ID)
            .requestScopes(
                Scope("https://www.googleapis.com/auth/youtube.readonly"),
                Scope("https://www.googleapis.com/auth/youtube.force-ssl")
            )
            .build()
        
        return GoogleSignIn.getClient(context, gso)
    }
    
    /**
     * Get YouTube access token from Google account
     * This checks both new sign-in and existing account
     */
    fun getYouTubeAccessToken(account: GoogleSignInAccount): String? {
        return try {
            Log.d(TAG, "=== Getting YouTube Access Token ===")
            Log.d(TAG, "Account email: ${account.email}")
            
            // Check granted scopes
            val grantedScopes = account.grantedScopes
            Log.d(TAG, "Granted scopes:")
            grantedScopes?.forEach { scope ->
                Log.d(TAG, "  - ${scope.scopeUri}")
            }
            
            val hasYouTubeScope = grantedScopes?.any { 
                it.scopeUri.contains("youtube") 
            } ?: false
            
            if (!hasYouTubeScope) {
                Log.e(TAG, "✗ Account doesn't have YouTube scope!")
                Log.e(TAG, "User needs to grant YouTube permissions")
                return null
            }
            
            // Try server auth code first (best for backend token exchange)
            val serverAuthCode = account.serverAuthCode
            if (serverAuthCode != null) {
                Log.d(TAG, "✓ Server Auth Code available: ${serverAuthCode.take(15)}...")
                return serverAuthCode
            }
            
            // Fallback to ID token
            val idToken = account.idToken
            if (idToken != null) {
                Log.d(TAG, "✓ Using ID Token: ${idToken.take(15)}...")
                return idToken
            }
            
            Log.e(TAG, "✗ No authentication token available")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Exception while getting YouTube access token", e)
            null
        }
    }
    
    /**
     * Check if the currently signed-in user has YouTube permissions
     */
    fun checkYouTubePermissions(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account == null) {
            Log.d(TAG, "No Google account signed in")
            return false
        }
        
        val hasYouTubeScope = account.grantedScopes?.any { 
            it.scopeUri.contains("youtube") 
        } ?: false
        
        Log.d(TAG, "YouTube permissions check: $hasYouTubeScope")
        return hasYouTubeScope
    }
    
    /**
     * Get the existing Google account (from sign-up/sign-in)
     */
    fun getLastSignedInGoogleAccount(): GoogleSignInAccount? {
        return try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account != null) {
                Log.d(TAG, "✓ Found existing Google account: ${account.email}")
                
                // Check YouTube scope
                val hasYouTubeScope = account.grantedScopes?.any { 
                    it.scopeUri.contains("youtube") 
                } ?: false
                Log.d(TAG, "  Has YouTube scope: $hasYouTubeScope")
            }
            account
        } catch (e: Exception) {
            Log.e(TAG, "Error getting last signed in account", e)
            null
        }
    }

    /**
     * Sign out from Google
     */
    fun signOutGoogle(onComplete: () -> Unit = {}) {
        try {
            Log.d(TAG, "Signing out from Google...")
            getYouTubeSignInClient().signOut().addOnCompleteListener {
                Log.d(TAG, "✓ Google sign out complete")
                onComplete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error signing out from Google", e)
            onComplete()
        }
    }
    
    // ===== FACEBOOK OAUTH =====
    
    /**
     * Initialize Facebook login with callbacks
     */
    fun loginWithFacebook(
        loginManager: LoginManager,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            Log.d(TAG, "=== Setting up Facebook Login ===")
            
            loginManager.registerCallback(facebookCallbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    Log.d(TAG, "✓ Facebook login SUCCESS")
                    val accessToken = result.accessToken.token
                    val userId = result.accessToken.userId
                    Log.d(TAG, "  User ID: $userId")
                    Log.d(TAG, "  Access Token: ${accessToken.take(20)}...")
                    
                    // Log granted permissions
                    val permissions = result.accessToken.permissions
                    Log.d(TAG, "  Granted permissions: ${permissions.joinToString(", ")}")
                    
                    // Check if we have the required permissions
                    if (permissions.contains("user_likes") || permissions.contains("pages_read_engagement")) {
                        Log.d(TAG, "✓ Has required Facebook permissions")
                    } else {
                        Log.w(TAG, "⚠ Missing some Facebook permissions - verification may fail")
                    }
                    
                    onSuccess(accessToken)
                }
                
                override fun onCancel() {
                    Log.w(TAG, "✗ Facebook login CANCELLED by user")
                    onError("Login cancelled")
                }
                
                override fun onError(error: FacebookException) {
                    Log.e(TAG, "✗ Facebook login ERROR", error)
                    Log.e(TAG, "  Error type: ${error.javaClass.simpleName}")
                    Log.e(TAG, "  Error message: ${error.message}")
                    onError(error.message ?: "Login failed")
                }
            })
            
            Log.d(TAG, "✓ Facebook callback registered")
        } catch (e: Exception) {
            Log.e(TAG, "CRITICAL: Failed to setup Facebook login", e)
            onError("Failed to setup Facebook login: ${e.message}")
        }
    }
    
    /**
     * Handle Facebook activity results
     */
    fun handleFacebookResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "=== Handling Facebook Result ===")
        Log.d(TAG, "Request code: $requestCode")
        Log.d(TAG, "Result code: $resultCode")
        
        try {
            facebookCallbackManager.onActivityResult(requestCode, resultCode, data)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling Facebook result", e)
        }
    }

    /**
     * Sign out from Facebook
     */
    fun signOutFacebook() {
        try {
            Log.d(TAG, "Signing out from Facebook...")
            LoginManager.getInstance().logOut()
            Log.d(TAG, "✓ Facebook sign out complete")
        } catch (e: Exception) {
            Log.e(TAG, "Error signing out from Facebook", e)
        }
    }
    
    /**
     * Get current user ID from the signed-in account
     */
    fun getCurrentUserId(): String? {
        return try {
            // Try to get Google account first
            val googleAccount = GoogleSignIn.getLastSignedInAccount(context)
            if (googleAccount != null) {
                return googleAccount.id
            }
            
            // If no Google account, return null
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user ID", e)
            null
        }
    }
}

/**
 * Check if Google Play Services is available
 */
fun isGooglePlayServicesAvailable(context: Context): Boolean {
    val googleApiAvailability = GoogleApiAvailability.getInstance()
    val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
    return resultCode == ConnectionResult.SUCCESS
}

/**
 * Get error message for Google Play Services issues
 */
fun getGooglePlayServicesErrorMessage(context: Context): String {
    val googleApiAvailability = GoogleApiAvailability.getInstance()
    val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
    return when (resultCode) {
        ConnectionResult.SUCCESS -> "Available"
        ConnectionResult.SERVICE_MISSING -> "Google Play Services is missing"
        ConnectionResult.SERVICE_UPDATING -> "Google Play Services is updating"
        ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> "Update required"
        ConnectionResult.SERVICE_DISABLED -> "Google Play Services is disabled"
        ConnectionResult.SERVICE_INVALID -> "Invalid installation"
        else -> "Error code: $resultCode"
    }
}