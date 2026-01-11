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
import kotlinx.coroutines.delay
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : ComponentActivity() {
    
    private val loginViewModel: LoginViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    
    @Inject
    lateinit var authUseCase: com.ekehi.network.domain.usecase.AuthUseCase
    @Inject
    lateinit var userUseCase: com.ekehi.network.domain.usecase.UserUseCase
    
    private var isAuthCheckComplete = false
    private var isAuthenticated: Boolean? = null
    private var isProfileLoaded = false
    private var authCheckTimeout = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        Log.d("SplashActivity", "=== APP STARTED - SPLASH SCREEN ===")
        
        // Keep splash screen visible until authentication check is complete
        splashScreen.setKeepOnScreenCondition { 
            !isAuthCheckComplete
        }
        
        // Start authentication check
        checkAuthentication()
        
        // Set a timeout to prevent infinite waiting (10 seconds)
        lifecycleScope.launch {
            delay(10000)
            if (!isAuthCheckComplete) {
                Log.w("SplashActivity", "⚠️ Authentication check timeout - proceeding as not authenticated")
                isAuthCheckComplete = true
                navigateToMainActivity(isAuthenticated = false)
            }
        }
        
        // Observe authentication state
        observeAuthenticationState()
    }
    
    private fun checkAuthentication() {
        Log.d("SplashActivity", "=== CHECKING AUTHENTICATION ===")
        lifecycleScope.launch {
            try {
                // Use the authentication check from LoginViewModel
                loginViewModel.checkCurrentUser()
            } catch (e: Exception) {
                Log.e("SplashActivity", "Error checking authentication: ${e.message}", e)
                isAuthenticated = false
                isAuthCheckComplete = true
                navigateToMainActivity(isAuthenticated = false)
            }
        }
    }
    
    private fun observeAuthenticationState() {
        lifecycleScope.launch {
            loginViewModel.loginState.collect { state ->
                Log.d("SplashActivity", "Auth state received: ${state::class.simpleName}")
                
                // Skip processing if we've already timed out
                if (authCheckTimeout) {
                    Log.d("SplashActivity", "Skipping auth state processing due to timeout")
                    return@collect
                }
                
                when (state) {
                    is Resource.Success -> {
                        Log.d("SplashActivity", "✅ User is authenticated, checking profile completeness")
                        isAuthenticated = true
                        
                        // Check profile completeness before navigating
                        lifecycleScope.launch {
                            try {
                                val currentUserResult = authUseCase.getCurrentUserIfLoggedIn().collect { authResource ->
                                    if (authResource is Resource.Success && authResource.data != null) {
                                        val user = authResource.data
                                        userUseCase.getUserProfile(user!!.id).collect { profileResource ->
                                            if (profileResource is Resource.Success) {
                                                val profile = profileResource.data
                                                val isIncomplete = profile.phoneNumber.isEmpty() || profile.country.isEmpty()
                                                Log.d("SplashActivity", "Profile completeness check: isIncomplete=$isIncomplete")
                                                
                                                isAuthCheckComplete = true
                                                delay(500)
                                                navigateToMainActivity(isAuthenticated = true, requiresAdditionalInfo = isIncomplete)
                                            } else if (profileResource is Resource.Error) {
                                                Log.e("SplashActivity", "Error loading profile: ${profileResource.message}")
                                                isAuthCheckComplete = true
                                                navigateToMainActivity(isAuthenticated = true, requiresAdditionalInfo = true) // Assume incomplete on error
                                            }
                                        }
                                    } else if (authResource is Resource.Error) {
                                        Log.e("SplashActivity", "Error getting user: ${authResource.message}")
                                        isAuthCheckComplete = true
                                        navigateToMainActivity(isAuthenticated = false)
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("SplashActivity", "Exception during profile check: ${e.message}")
                                isAuthCheckComplete = true
                                navigateToMainActivity(isAuthenticated = true, requiresAdditionalInfo = true)
                            }
                        }
                    }
                    is Resource.Error -> {
                        Log.d("SplashActivity", "❌ User is NOT authenticated: ${state.message}")
                        isAuthenticated = false
                        isAuthCheckComplete = true
                        
                        // Small delay to show splash screen properly
                        delay(500)
                        navigateToMainActivity(isAuthenticated = false)
                    }
                    is Resource.Loading -> {
                        Log.d("SplashActivity", "⏳ Checking authentication...")
                        // Keep splash screen visible
                    }
                    is Resource.Idle -> {
                        // Initial state, do nothing yet
                    }
                }
            }
        }
    }
    
    private fun navigateToMainActivity(isAuthenticated: Boolean, requiresAdditionalInfo: Boolean = false) {
        // Prevent multiple navigations
        if (isFinishing) {
            Log.d("SplashActivity", "Activity is finishing, skipping navigation")
            return
        }
        
        Log.d("SplashActivity", "=== NAVIGATING TO MAIN ACTIVITY ===")
        Log.d("SplashActivity", "Authenticated: $isAuthenticated, RequiresAdditionalInfo: $requiresAdditionalInfo")
        
        // Reset the login state before navigating
        loginViewModel.resetState()
        
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("IS_AUTHENTICATED", isAuthenticated)
            putExtra("REQUIRES_ADDITIONAL_INFO", requiresAdditionalInfo)
            putExtra("FROM_SPLASH", true)
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