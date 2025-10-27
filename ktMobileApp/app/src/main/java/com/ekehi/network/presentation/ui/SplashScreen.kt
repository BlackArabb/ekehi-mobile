package com.ekehi.network.presentation.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ekehi.network.presentation.viewmodel.LoginViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ekehi Logo
            Icon(
                imageVector = Icons.Default.AccountBalance,
                contentDescription = "Ekehi Logo",
                tint = Color(0xFFffa000), // Orange color
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 32.dp)
            )

            // Loading Indicator
            CircularProgressIndicator(
                color = Color(0xFFffa000), // Orange color
                strokeWidth = 4.dp,
                modifier = Modifier.size(40.dp)
            )
        }
    }
    
    // Check if user is already logged in
    LaunchedEffect(Unit) {
        try {
            delay(2000) // 2 seconds delay for branding
            
            Log.d("SplashScreen", "Checking if user is already authenticated")
            // Check if user is already authenticated
            viewModel.checkCurrentUser()
        } catch (e: Exception) {
            Log.e("SplashScreen", "Error checking authentication status", e)
            // Navigate to landing screen as fallback
            try {
                navController.navigate("landing") {
                    popUpTo("splash") { inclusive = true }
                }
            } catch (navException: Exception) {
                Log.e("SplashScreen", "Error navigating to landing screen", navException)
            }
        }
    }
    
    // Observe the login state to navigate appropriately
    LaunchedEffect(Unit) {
        try {
            viewModel.loginState.collect { resource ->
                try {
                    when (resource) {
                        is com.ekehi.network.domain.model.Resource.Success -> {
                            Log.d("SplashScreen", "User is authenticated, navigating to main screen")
                            // User is authenticated, navigate to main screen
                            navController.navigate("main") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                        is com.ekehi.network.domain.model.Resource.Error -> {
                            Log.d("SplashScreen", "User is not authenticated, navigating to landing screen")
                            // User is not authenticated, navigate to landing screen
                            navController.navigate("landing") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                        else -> {
                            // For Loading or Idle states, do nothing
                            Log.d("SplashScreen", "Authentication check in progress")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SplashScreen", "Error handling login state", e)
                    // Navigate to landing screen as fallback
                    try {
                        navController.navigate("landing") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } catch (navException: Exception) {
                        Log.e("SplashScreen", "Error navigating to landing screen", navException)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SplashScreen", "Error observing login state", e)
            // Navigate to landing screen as fallback
            try {
                navController.navigate("landing") {
                    popUpTo("splash") { inclusive = true }
                }
            } catch (navException: Exception) {
                Log.e("SplashScreen", "Error navigating to landing screen", navException)
            }
        }
    }
}