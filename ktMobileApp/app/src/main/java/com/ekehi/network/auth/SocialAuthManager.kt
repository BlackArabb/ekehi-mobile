package com.ekehi.network.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
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
    private val facebookCallbackManager = CallbackManager.Factory.create()
    private var facebookCallback: FacebookCallback<LoginResult>? = null
    
    // ===== YOUTUBE OAUTH =====
    fun getYouTubeSignInClient(): GoogleSignInClient {
        if (googleSignInClient == null) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(com.ekehi.network.BuildConfig.YOUTUBE_CLIENT_ID) // Add this for better token handling
                .requestScopes(
                    Scope("https://www.googleapis.com/auth/youtube.readonly"),
                    Scope("https://www.googleapis.com/auth/youtube.force-ssl")
                )
                .requestServerAuthCode(com.ekehi.network.BuildConfig.YOUTUBE_CLIENT_ID)
                .build()
            
            googleSignInClient = GoogleSignIn.getClient(context, gso)
        }
        return googleSignInClient!!
    }
    
    /**
     * Get YouTube access token from Google account
     * IMPORTANT: For production, you should exchange serverAuthCode for access token via your backend
     * For now, we try to get it from the account or use serverAuthCode
     */
    fun getYouTubeAccessToken(account: GoogleSignInAccount): String? {
        return try {
            // Try to get the server auth code (this is what you exchange for access token)
            val serverAuthCode = account.serverAuthCode
            
            if (serverAuthCode != null) {
                Log.d("SocialAuthManager", "Got serverAuthCode: ${serverAuthCode.take(20)}...")
                // In production: send this to your backend to exchange for access token
                // For now, return the serverAuthCode (your backend should handle the exchange)
                return serverAuthCode
            }
            
            // Fallback: try to get ID token (limited functionality)
            val idToken = account.idToken
            if (idToken != null) {
                Log.d("SocialAuthManager", "Using idToken as fallback: ${idToken.take(20)}...")
                return idToken
            }
            
            Log.e("SocialAuthManager", "No token available from Google account")
            null
        } catch (e: Exception) {
            Log.e("SocialAuthManager", "Error getting YouTube access token: ${e.message}", e)
            null
        }
    }
    
    /**
     * Check if user is already signed in with Google
     * This is useful for users who signed up with Google OAuth
     */
    fun getLastSignedInGoogleAccount(): GoogleSignInAccount? {
        return try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account != null) {
                Log.d("SocialAuthManager", "Found existing Google account: ${account.email}")
            }
            account
        } catch (e: Exception) {
            Log.e("SocialAuthManager", "Error getting last signed in account: ${e.message}", e)
            null
        }
    }
    
    /**
     * Sign out from Google
     */
    fun signOutGoogle(onComplete: () -> Unit = {}) {
        try {
            getYouTubeSignInClient().signOut().addOnCompleteListener {
                Log.d("SocialAuthManager", "Google sign out complete")
                onComplete()
            }
        } catch (e: Exception) {
            Log.e("SocialAuthManager", "Error signing out from Google: ${e.message}", e)
            onComplete()
        }
    }
    
    // ===== FACEBOOK OAUTH =====
    /**
     * Initialize Facebook login with callbacks
     * This must be called before attempting to login
     */
    fun loginWithFacebook(
        loginManager: LoginManager,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            // Unregister previous callback if exists
            facebookCallback?.let {
                try {
                    loginManager.unregisterCallback(facebookCallbackManager)
                } catch (e: Exception) {
                    Log.w("SocialAuthManager", "Could not unregister previous callback: ${e.message}")
                }
            }
            
            // Create and register new callback
            facebookCallback = object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    Log.d("SocialAuthManager", "Facebook login success")
                    val accessToken = result.accessToken.token
                    Log.d("SocialAuthManager", "Facebook access token: ${accessToken.take(20)}...")
                    onSuccess(accessToken)
                }
                
                override fun onCancel() {
                    Log.d("SocialAuthManager", "Facebook login cancelled")
                    onError("Login cancelled")
                }
                
                override fun onError(error: FacebookException) {
                    Log.e("SocialAuthManager", "Facebook login error: ${error.message}", error)
                    onError(error.message ?: "Login failed")
                }
            }
            
            loginManager.registerCallback(facebookCallbackManager, facebookCallback)
            Log.d("SocialAuthManager", "Facebook callback registered successfully")
        } catch (e: Exception) {
            Log.e("SocialAuthManager", "Error setting up Facebook login: ${e.message}", e)
            onError("Failed to setup Facebook login: ${e.message}")
        }
    }
    
    /**
     * Handle Facebook activity result
     * Call this from your activity result launcher
     */
    fun handleFacebookResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            Log.d("SocialAuthManager", "Handling Facebook result: requestCode=$requestCode, resultCode=$resultCode")
            facebookCallbackManager.onActivityResult(requestCode, resultCode, data)
        } catch (e: Exception) {
            Log.e("SocialAuthManager", "Error handling Facebook result: ${e.message}", e)
        }
    }
    
    /**
     * Get Facebook callback manager for manual result handling
     */
    fun getFacebookCallbackManager(): CallbackManager = facebookCallbackManager
    
    /**
     * Sign out from Facebook
     */
    fun signOutFacebook() {
        try {
            LoginManager.getInstance().logOut()
            Log.d("SocialAuthManager", "Facebook sign out complete")
        } catch (e: Exception) {
            Log.e("SocialAuthManager", "Error signing out from Facebook: ${e.message}", e)
        }
    }
}