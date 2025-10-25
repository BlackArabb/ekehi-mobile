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
import com.ekehi.network.network.service.AppwriteService
import dagger.hilt.android.AndroidEntryPoint
import io.appwrite.exceptions.AppwriteException
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OAuthCallbackActivity : ComponentActivity() {
    
    @Inject
    lateinit var appwriteService: AppwriteService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            OAuthCallbackScreen()
        }
        
        handleOAuthCallback()
    }
    
    private fun handleOAuthCallback() {
        intent?.data?.let { uri ->
            val userId = uri.getQueryParameter("userId")
            val secret = uri.getQueryParameter("secret")
            
            if (userId != null && secret != null) {
                // Instead of createSession, we'll just finish and let the main activity handle it
                val resultIntent = Intent().apply {
                    putExtra("oauth_success", true)
                    putExtra("userId", userId)
                    putExtra("secret", secret)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                // If no parameters, just finish and let the main activity handle it
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        } ?: run {
            // No data, finish
            setResult(Activity.RESULT_CANCELED)
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