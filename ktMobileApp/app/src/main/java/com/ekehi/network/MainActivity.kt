package com.ekehi.network

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.ekehi.network.presentation.navigation.AppNavigation
import com.ekehi.network.presentation.viewmodel.LoginViewModel
import com.ekehi.network.presentation.viewmodel.OAuthViewModel
import com.ekehi.network.ui.theme.EkehiMobileTheme
import com.facebook.CallbackManager
import com.startapp.sdk.adsbase.StartAppAd
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val loginViewModel: LoginViewModel by viewModels()
    private val oAuthViewModel: OAuthViewModel by viewModels()
    private lateinit var callbackManager: CallbackManager
    
    private var isAuthenticated by mutableStateOf(false)
    private var isAuthChecked by mutableStateOf(false)
    private var requiresAdditionalInfo by mutableStateOf(false)
    private var oauthError by mutableStateOf<String?>(null)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Facebook callback manager
        callbackManager = CallbackManager.Factory.create()
        
        Log.d("MainActivity", "=== MAIN ACTIVITY CREATED ===")
        
        // Determine where we came from
        val fromSplash = intent.getBooleanExtra("FROM_SPLASH", false)
        val fromOAuth = intent.getBooleanExtra("FROM_OAUTH", false)
        
        Log.d("MainActivity", "Source - FromSplash: $fromSplash, FromOAuth: $fromOAuth")
        
        when {
            fromOAuth -> {
                // Coming from OAuth callback
                handleOAuthIntent(intent)
            }
            fromSplash -> {
                // Coming from SplashActivity - use the authentication state it determined
                isAuthenticated = intent.getBooleanExtra("IS_AUTHENTICATED", false)
                requiresAdditionalInfo = intent.getBooleanExtra("REQUIRES_ADDITIONAL_INFO", false)
                isAuthChecked = true
                Log.d("MainActivity", "From Splash - Authenticated: $isAuthenticated, RequiresAdditionalInfo: $requiresAdditionalInfo")
            }
            else -> {
                // Direct launch (shouldn't happen normally, but handle it)
                Log.w("MainActivity", "⚠️ Direct launch detected - checking authentication")
                checkAuthenticationStatus()
            }
        }
        
        setContent {
            EkehiMobileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isAuthChecked) {
                        AppNavigation(
                            isAuthenticated = isAuthenticated,
                            requiresAdditionalInfo = requiresAdditionalInfo
                        )
                    }
                }
            }
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        // Handle Facebook login result
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        
        val fromOAuth = intent.getBooleanExtra("FROM_OAUTH", false)
        if (fromOAuth) {
            handleOAuthIntent(intent)
        }
    }
    
    private fun handleOAuthIntent(intent: Intent?) {
        intent?.let {
            val fromOAuth = it.getBooleanExtra("FROM_OAUTH", false)
            
            if (fromOAuth) {
                Log.d("MainActivity", "=== HANDLING OAUTH CALLBACK ===")
                
                isAuthenticated = it.getBooleanExtra("IS_AUTHENTICATED", false)
                requiresAdditionalInfo = it.getBooleanExtra("SHOW_PROFILE_COMPLETION", false)
                oauthError = it.getStringExtra("OAUTH_ERROR")
                isAuthChecked = true
                
                val userId = it.getStringExtra("USER_ID")
                
                Log.d("MainActivity", "OAuth Result - Authenticated: $isAuthenticated, UserId: $userId")
                
                if (isAuthenticated && userId != null) {
                    Log.d("MainActivity", "✅ OAuth login successful")
                    
                    val showProfileCompletion = it.getBooleanExtra("SHOW_PROFILE_COMPLETION", false)
                    oAuthViewModel.handleOAuthResult(success = true, requiresAdditionalInfo = showProfileCompletion)
                    
                    // CRITICAL: Call onOAuthSuccess to set the authResolution which triggers navigation
                    oAuthViewModel.onOAuthSuccess()
                    
                    // Refresh login state
                    lifecycleScope.launch {
                        loginViewModel.checkCurrentUser()
                    }
                } else if (oauthError != null) {
                    Log.e("MainActivity", "❌ OAuth login failed: $oauthError")
                    oAuthViewModel.handleOAuthResult(success = false, errorMessage = oauthError)
                }
            }
        }
    }
    
    private fun checkAuthenticationStatus() {
        Log.d("MainActivity", "Checking authentication status directly")
        
        lifecycleScope.launch {
            loginViewModel.checkCurrentUser()
            
            loginViewModel.loginState.collect { state ->
                when (state) {
                    is com.ekehi.network.domain.model.Resource.Success -> {
                        Log.d("MainActivity", "✅ User is authenticated")
                        isAuthenticated = true
                        isAuthChecked = true
                    }
                    is com.ekehi.network.domain.model.Resource.Error -> {
                        Log.d("MainActivity", "❌ User is NOT authenticated")
                        isAuthenticated = false
                        isAuthChecked = true
                    }
                    is com.ekehi.network.domain.model.Resource.Loading -> {
                        Log.d("MainActivity", "⏳ Checking authentication...")
                    }
                    is com.ekehi.network.domain.model.Resource.Idle -> {
                        // Initial state
                    }
                }
            }
        }
    }
    
    override fun onBackPressed() {
        StartAppAd.onBackPressed(this)
        super.onBackPressed()
    }
}