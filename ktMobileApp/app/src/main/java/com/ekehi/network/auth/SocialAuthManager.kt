package com.ekehi.network.auth

import android.content.Context
import android.content.Intent
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
    
    // ===== YOUTUBE OAUTH =====
    fun getYouTubeSignInClient(): GoogleSignInClient {
        if (googleSignInClient == null) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
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
    
    fun getYouTubeAccessToken(account: GoogleSignInAccount): String? {
        // In production, exchange auth code for access token via backend
        // For now, we'll use the ID token (limited functionality)
        return account.idToken
    }
    
    // ===== FACEBOOK OAUTH =====
    fun loginWithFacebook(
        loginManager: LoginManager,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        loginManager.registerCallback(facebookCallbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                val accessToken = result.accessToken.token
                onSuccess(accessToken)
            }
            
            override fun onCancel() {
                onError("Login cancelled")
            }
            
            override fun onError(error: FacebookException) {
                onError(error.message ?: "Login failed")
            }
        })
    }
    
    fun handleFacebookResult(requestCode: Int, resultCode: Int, data: Intent?) {
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data)
    }
}