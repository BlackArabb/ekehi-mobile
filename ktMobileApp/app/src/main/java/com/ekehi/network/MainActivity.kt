package com.ekehi.network

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ekehi.network.service.StartIoService
import com.ekehi.network.presentation.navigation.AppNavigation
import com.ekehi.network.ui.theme.EkehiMobileTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var startIoService: StartIoService
    
    private lateinit var oauthResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // Initialize Start.io
            startIoService.initialize()
        } catch (e: Exception) {
            // Log the error but don't crash the app
            Log.e("MainActivity", "Error initializing Start.io", e)
        }
        
        // Register for OAuth activity result
        oauthResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d("MainActivity", "Received OAuth activity result: ${result.resultCode}")
            try {
                if (result.resultCode == RESULT_OK) {
                    val data = result.data
                    val success = data?.getBooleanExtra("oauth_success", false) ?: false
                    if (success) {
                        Log.d("MainActivity", "OAuth successful, user authenticated")
                        // The OAuthCallbackActivity will handle updating the app state
                    } else {
                        val errorMessage = data?.getStringExtra("error_message") ?: "OAuth failed"
                        Log.e("MainActivity", "OAuth failed: $errorMessage")
                    }
                } else {
                    Log.e("MainActivity", "OAuth activity cancelled or failed")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error handling OAuth result", e)
            }
        }
        
        setContent {
            EkehiMobileTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    // This function is no longer needed but kept for reference
}