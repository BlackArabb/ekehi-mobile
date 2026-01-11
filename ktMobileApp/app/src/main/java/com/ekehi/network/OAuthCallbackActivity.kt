package com.ekehi.network

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.ekehi.network.service.OAuthService
import dagger.hilt.android.AndroidEntryPoint
import io.appwrite.exceptions.AppwriteException
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OAuthCallbackActivity : ComponentActivity() {
    
    @Inject
    lateinit var oAuthService: OAuthService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            OAuthCallbackScreen()
        }
        
        handleOAuthCallback()
    }
    
    private fun handleOAuthCallback() {
        try {
            intent?.data?.let { uri ->
                Log.d("OAuthCallbackActivity", "Received OAuth callback: $uri")
                
                // Handle the new callback URL format
                when (uri.host) {
                    "oauth2" -> {
                        when (uri.lastPathSegment) {
                            "success" -> {
                                val userId = uri.getQueryParameter("userId")
                                val secret = uri.getQueryParameter("secret")
                                
                                if (userId != null && secret != null) {
                                    Log.d("OAuthCallbackActivity", "Processing OAuth callback with userId: $userId")
                                    // Handle the OAuth callback in a coroutine
                                    lifecycleScope.launch {
                                        try {
                                            val result = oAuthService.handleOAuthCallback(userId, secret)
                                            if (result.isSuccess) {
                                                Log.d("OAuthCallbackActivity", "✅ OAuth callback processed successfully")
                                                
                                                val resultData = result.getOrNull()
                                                val isProfileComplete = resultData?.get("isProfileComplete") as? Boolean ?: false
                                                
                                                Log.d("OAuthCallbackActivity", "Profile completeness: $isProfileComplete")
                                                
                                                if (isProfileComplete) {
                                                    // Profile is complete, go directly to main
                                                    navigateToMainActivity(isAuthenticated = true, userId = userId, showProfileCompletion = false)
                                                } else {
                                                    // Profile is incomplete, go to profile completion
                                                    navigateToMainActivity(isAuthenticated = true, userId = userId, showProfileCompletion = true)
                                                }
                                            } else {
                                                Log.e("OAuthCallbackActivity", "❌ Failed to process OAuth callback: ${result.exceptionOrNull()?.message}")
                                                showErrorAndFinish(result.exceptionOrNull()?.message ?: "Authentication failed")
                                            }
                                        } catch (e: Exception) {
                                            Log.e("OAuthCallbackActivity", "❌ Exception during OAuth callback: ${e.message}", e)
                                            showErrorAndFinish(e.message ?: "Authentication failed")
                                        }
                                    }
                                } else {
                                    Log.w("OAuthCallbackActivity", "Missing userId or secret in OAuth callback")
                                    showErrorAndFinish("Missing authentication parameters")
                                }
                            }
                            "failure" -> {
                                Log.e("OAuthCallbackActivity", "OAuth authentication failed")
                                showErrorAndFinish("Authentication failed")
                            }
                            else -> {
                                Log.w("OAuthCallbackActivity", "Unknown OAuth callback path: ${uri.lastPathSegment}")
                                showErrorAndFinish("Unknown authentication response")
                            }
                        }
                    }
                    else -> {
                        Log.w("OAuthCallbackActivity", "Unknown OAuth callback host: ${uri.host}")
                        showErrorAndFinish("Unknown authentication host")
                    }
                }
            } ?: run {
                Log.w("OAuthCallbackActivity", "No data in OAuth callback")
                showErrorAndFinish("No authentication data received")
            }
        } catch (e: Exception) {
            Log.e("OAuthCallbackActivity", "Error handling OAuth callback: ${e.message}", e)
            showErrorAndFinish(e.message ?: "Authentication error")
        }
    }
    
    private fun navigateToMainActivity(isAuthenticated: Boolean, userId: String? = null, showProfileCompletion: Boolean = false) {
        Log.d("OAuthCallbackActivity", "Navigating to MainActivity - authenticated: $isAuthenticated, showProfileCompletion: $showProfileCompletion")
        
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("IS_AUTHENTICATED", isAuthenticated)
            putExtra("USER_ID", userId)
            putExtra("FROM_OAUTH", true)
            putExtra("SHOW_PROFILE_COMPLETION", showProfileCompletion)
            // Clear the entire task stack and start fresh
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        startActivity(intent)
        finish()
    }
    


    private fun navigateToProfileCompletion(userId: String) {
        Log.d("OAuthCallbackActivity", "Navigating to profile completion screen for userId: $userId")
        
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("IS_AUTHENTICATED", true)
            putExtra("USER_ID", userId)
            putExtra("FROM_OAUTH", true)
            putExtra("SHOW_PROFILE_COMPLETION", true)  // Flag to show profile completion
            // Clear the entire task stack and start fresh
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        startActivity(intent)
        finish()
    }
    
     private fun showErrorAndFinish(errorMessage: String) {
        Log.e("OAuthCallbackActivity", "OAuth error: $errorMessage")
        
        // Navigate back to MainActivity with error
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("IS_AUTHENTICATED", false)
            putExtra("OAUTH_ERROR", errorMessage)
            putExtra("FROM_OAUTH", true)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        startActivity(intent)
        finish()
    }
}

@Composable
fun OAuthCallbackScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Text(
            text = "Completing Authentication",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = "Please wait while we complete the sign in process",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}