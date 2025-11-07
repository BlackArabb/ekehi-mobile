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
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.presentation.navigation.AppNavigation
import com.ekehi.network.presentation.viewmodel.LoginViewModel
import com.ekehi.network.ui.theme.EkehiMobileTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : ComponentActivity() {
    
    private val loginViewModel: LoginViewModel by viewModels()
    private var isAuthCheckComplete = false
    private var isAuthenticated: Boolean? = null

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
                        navigateToMainActivity(isAuthenticated = true)
                    }
                    is Resource.Error -> {
                        Log.d("SplashActivity", "❌ User is NOT authenticated: ${state.message}")
                        isAuthenticated = false
                        isAuthCheckComplete = true
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
    
    private fun navigateToMainActivity(isAuthenticated: Boolean) {
        Log.d("SplashActivity", "Navigating to MainActivity with auth status: $isAuthenticated")
        
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("IS_AUTHENTICATED", isAuthenticated)
            // Clear the splash activity from back stack
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        startActivity(intent)
        finish()
    }
}