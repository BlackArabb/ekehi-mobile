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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ekehi.network.presentation.viewmodel.LoginViewModel
import com.ekehi.network.ui.theme.EkehiMobileTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
        navController: NavController,
        viewModel: LoginViewModel = hiltViewModel()
) {
    var isChecking by remember { mutableStateOf(true) }
    var hasNavigated by remember { mutableStateOf(false) }

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
            if (isChecking) {
                CircularProgressIndicator(
                        color = Color(0xFFffa000), // Orange color
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                        text = "Checking session...",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }

    // Single unified LaunchedEffect to handle authentication check and navigation
    LaunchedEffect(Unit) {
        if (hasNavigated) return@LaunchedEffect

        try {
            delay(2000) // 2 seconds delay for branding

            Log.d("SplashScreen", "Checking if user is already authenticated")
            viewModel.checkCurrentUser()

            // Collect login state only once to determine navigation
            viewModel.loginState.collect { resource ->
                if (hasNavigated) return@collect

                try {
                    when (resource) {
                        is com.ekehi.network.domain.model.Resource.Success -> {
                            Log.d("SplashScreen", "User is authenticated, navigating to main screen")
                            hasNavigated = true
                            navController.navigate("main") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                        is com.ekehi.network.domain.model.Resource.Error -> {
                            Log.d("SplashScreen", "User is not authenticated, navigating to landing screen")
                            hasNavigated = true
                            navController.navigate("landing") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                        else -> {
                            Log.d("SplashScreen", "Authentication check in progress")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SplashScreen", "Error handling login state", e)
                    if (!hasNavigated) {
                        hasNavigated = true
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
        } catch (e: Exception) {
            Log.e("SplashScreen", "Error checking authentication status", e)
            if (!hasNavigated) {
                hasNavigated = true
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
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    EkehiMobileTheme {
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
    }
}