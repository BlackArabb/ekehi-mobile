package com.ekehi.network

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ekehi.network.presentation.navigation.AppNavigation
import com.ekehi.network.ui.theme.EkehiMobileTheme
import com.startapp.sdk.adsbase.StartAppAd
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get authentication status from intent
        val isAuthenticated = intent.getBooleanExtra("IS_AUTHENTICATED", false)
        
        Log.d("MainActivity", "=== MAIN ACTIVITY STARTED ===")
        Log.d("MainActivity", "Authentication status: $isAuthenticated")
        
        setContent {
            EkehiMobileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(isAuthenticated = isAuthenticated)
                }
            }
        }
    }
    
    override fun onBackPressed() {
        // Show exit ad when back button is pressed
        StartAppAd.onBackPressed(this)
        super.onBackPressed()
    }
}