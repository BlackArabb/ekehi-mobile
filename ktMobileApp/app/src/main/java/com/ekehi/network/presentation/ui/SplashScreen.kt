package com.ekehi.network.presentation.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    Log.d("SplashScreen", "Splash screen displayed")
    
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        Log.d("SplashScreen", "Starting splash screen delay")
        try {
            // Show splash screen for 2 seconds
            delay(2000)
            isLoading = false
            Log.d("SplashScreen", "Navigating to landing screen")
            navController.navigate("landing")
        } catch (e: Exception) {
            Log.e("SplashScreen", "Error in splash screen: ${e.message}", e)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Simple logo/text for splash screen
                Text(
                    text = "Ekehi",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.Blue,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                CircularProgressIndicator()
                Text(
                    text = "Loading...",
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}