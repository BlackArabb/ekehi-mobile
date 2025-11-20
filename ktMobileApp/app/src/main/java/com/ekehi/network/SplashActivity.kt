package com.ekehi.network

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.ekehi.network.data.model.UserProfile
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.presentation.navigation.AppNavigation
import com.ekehi.network.presentation.viewmodel.LoginViewModel
import com.ekehi.network.presentation.viewmodel.ProfileViewModel
import com.ekehi.network.ui.theme.EkehiMobileTheme
import com.startapp.sdk.adsbase.StartAppAd
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : ComponentActivity() {
    
    private val loginViewModel: LoginViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private var isAuthCheckComplete = false
    private var isAuthenticated: Boolean? = null
    private var isProfileLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        Log.d("SplashActivity", "=== APP STARTED - SPLASH SCREEN ===")
        
        // Keep splash screen visible until authentication check is complete
        splashScreen.setKeepOnScreenCondition { 
            !isAuthCheckComplete || !isProfileLoaded
        }
        
        // Start authentication check
        checkAuthentication()
        
        // Observe authentication state
        observeAuthenticationState()
    }
    
    private fun checkAuthentication() {
        Log.d("SplashActivity", "Starting authentication check")
        loginViewModel.checkCurrentUser()
    }
    
    private fun observeAuthenticationState() {
        lifecycleScope.launch {
            loginViewModel.loginState.collect { state ->
                Log.d("SplashActivity", "Auth state: ${state::class.simpleName}")
                
                when (state) {
                    is Resource.Success -> {
                        Log.d("SplashActivity", "✅ User is authenticated")
                        isAuthenticated = true
                        isAuthCheckComplete = true
                        
                        // Load user profile data - we'll get the user ID after the splash screen
                        isProfileLoaded = true
                        navigateToMainActivity(isAuthenticated = true)
                    }
                    is Resource.Error -> {
                        Log.d("SplashActivity", "❌ User is NOT authenticated: ${state.message}")
                        isAuthenticated = false
                        isAuthCheckComplete = true
                        isProfileLoaded = true // No profile to load for unauthenticated users
                        navigateToMainActivity(isAuthenticated = false)
                    }
                    is Resource.Loading -> {
                        Log.d("SplashActivity", "⏳ Checking authentication...")
                        // Keep splash screen visible
                    }
                    is Resource.Idle -> {
                        // Initial state, do nothing
                    }
                }
            }
        }
    }
    
    private fun loadUserProfile(userId: String) {
        Log.d("SplashActivity", "Loading user profile for user: $userId")
        
        lifecycleScope.launch {
            try {
                profileViewModel.loadUserProfile(userId)
                profileViewModel.userProfile.collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            Log.d("SplashActivity", "✅ User profile loaded successfully")
                            isProfileLoaded = true
                            navigateToMainActivity(isAuthenticated = true)
                        }
                        is Resource.Error -> {
                            Log.e("SplashActivity", "❌ Failed to load user profile: ${resource.message}")
                            isProfileLoaded = true // Continue even if profile loading fails
                            navigateToMainActivity(isAuthenticated = true)
                        }
                        is Resource.Loading -> {
                            Log.d("SplashActivity", "⏳ Loading user profile...")
                            // Keep splash screen visible
                        }
                        else -> {
                            // For Idle state, continue
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SplashActivity", "Exception loading user profile", e)
                isProfileLoaded = true // Continue even if profile loading fails
                navigateToMainActivity(isAuthenticated = true)
            }
        }
    }
    
    private fun navigateToMainActivity(isAuthenticated: Boolean) {
        Log.d("SplashActivity", "Navigating to MainActivity with auth status: $isAuthenticated")
        
        // Reset the login state before navigating to ensure clean state for LoginScreen
        loginViewModel.resetState()
        
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("IS_AUTHENTICATED", isAuthenticated)
            // Clear the splash activity from back stack
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        startActivity(intent)
        finish()
    }
    
    override fun onBackPressed() {
        // Show exit ad when back button is pressed
        StartAppAd.onBackPressed(this)
        super.onBackPressed()
    }
}