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
                val userId = uri.getQueryParameter("userId")
                val secret = uri.getQueryParameter("secret")
                
                if (userId != null && secret != null) {
                    Log.d("OAuthCallbackActivity", "Processing OAuth callback with userId: $userId")
                    // Handle the OAuth callback in a coroutine
                    lifecycleScope.launch {
                        try {
                            val result = oAuthService.handleOAuthCallback(userId, secret)
                            if (result.isSuccess) {
                                Log.d("OAuthCallbackActivity", "OAuth callback processed successfully")
                                val resultIntent = Intent().apply {
                                    putExtra("oauth_success", true)
                                    putExtra("userId", userId)
                                }
                                setResult(Activity.RESULT_OK, resultIntent)
                                finish()
                            } else {
                                Log.e("OAuthCallbackActivity", "Failed to process OAuth callback: ${result.exceptionOrNull()?.message}")
                                val resultIntent = Intent().apply {
                                    putExtra("oauth_success", false)
                                    putExtra("error_message", result.exceptionOrNull()?.message)
                                }
                                setResult(Activity.RESULT_CANCELED, resultIntent)
                                finish()
                            }
                        } catch (e: Exception) {
                            Log.e("OAuthCallbackActivity", "Exception during OAuth callback processing: ${e.message}", e)
                            val resultIntent = Intent().apply {
                                putExtra("oauth_success", false)
                                putExtra("error_message", e.message)
                            }
                            setResult(Activity.RESULT_CANCELED, resultIntent)
                            finish()
                        }
                    }
                } else {
                    Log.w("OAuthCallbackActivity", "Missing userId or secret in OAuth callback")
                    // If no parameters, just finish and let the main activity handle it
                    val resultIntent = Intent().apply {
                        putExtra("oauth_success", false)
                        putExtra("error_message", "Missing OAuth parameters")
                    }
                    setResult(Activity.RESULT_CANCELED, resultIntent)
                    finish()
                }
            } ?: run {
                Log.w("OAuthCallbackActivity", "No data in OAuth callback")
                // No data, finish
                val resultIntent = Intent().apply {
                    putExtra("oauth_success", false)
                    putExtra("error_message", "No OAuth data received")
                }
                setResult(Activity.RESULT_CANCELED, resultIntent)
                finish()
            }
        } catch (e: Exception) {
            Log.e("OAuthCallbackActivity", "Error handling OAuth callback: ${e.message}", e)
            val resultIntent = Intent().apply {
                putExtra("oauth_success", false)
                putExtra("error_message", e.message)
            }
            setResult(Activity.RESULT_CANCELED, resultIntent)
            finish()
        }
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