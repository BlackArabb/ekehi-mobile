package com.ekehi.network.presentation.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.presentation.viewmodel.OAuthViewModel
import com.ekehi.network.ui.theme.EkehiMobileTheme
import com.ekehi.network.R

@Composable
fun LoginOptionsScreen(
    oAuthViewModel: OAuthViewModel = hiltViewModel(),
    onGoogleLogin: () -> Unit,
    onNavigateToGoogleSecondaryInfo: () -> Unit,  // Navigate to secondary info after Google OAuth
    onEmailLogin: () -> Unit,
    onNavigateToSignup: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val uriHandler = LocalUriHandler.current
    
    // Collect OAuth state
    val oauthState by oAuthViewModel.oauthState.collectAsState()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Handle OAuth state changes
    val requiresAdditionalInfo by oAuthViewModel.requiresAdditionalInfo.collectAsState()
    
    LaunchedEffect(oauthState) {
        when (oauthState) {
            is Resource.Loading -> {
                isLoading = true
                errorMessage = null
            }
            is Resource.Success -> {
                isLoading = false
                val success = (oauthState as Resource.Success<Boolean>).data
                if (success) {
                    if (requiresAdditionalInfo) {
                        // For login flow, navigate to secondary info screen if additional info is needed
                        onNavigateToGoogleSecondaryInfo()
                    } else {
                        onGoogleLogin()
                    }
                }
            }
            is Resource.Error -> {
                isLoading = false
                errorMessage = (oauthState as Resource.Error).message
            }
            is Resource.Idle -> {
                isLoading = false
                errorMessage = null
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1a1a2e),
                        Color(0xFF16213e),
                        Color(0xFF0f3460)
                    )
                )
            )
            .padding(16.dp)
    ) {
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 160.dp), // Space for bottom buttons and links
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween // This ensures content is spaced properly
        ) {
            // Top content
            Column(
                modifier = Modifier
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center // Center content vertically in the top portion
            ) {
                // Ekehi Logo
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher),
                    contentDescription = "Ekehi Logo",
                    modifier = Modifier
                        .size(80.dp)
                        .padding(bottom = 16.dp)
                )

                Text(
                    text = "Welcome back",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Sign in to continue your journey with us",
                    color = Color(0xB3FFFFFF),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }
            
            // Error Message (if needed, but in the main content area)
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                )
            }
        }

        // Bottom-anchored buttons for thumb-friendly access
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp), // Position buttons near bottom
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Google Sign In Button (Primary)
            Button(
                onClick = {
                    Log.d("LoginOptionsScreen", "Google login clicked")
                    // Get the current activity to initiate Google OAuth
                    val activity = context as? androidx.activity.ComponentActivity
                    if (activity != null) {
                        // Initiate Google OAuth for login
                        oAuthViewModel.initiateGoogleOAuthForLogin(activity)
                    } else {
                        Log.e("LoginOptionsScreen", "Could not get activity context for OAuth")
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4285F4), // Google blue color
                    disabledContainerColor = Color(0x33FFFFFF)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Continue with Google",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Divider with "OR"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(
                    modifier = Modifier.weight(1f),
                    color = Color(0x33FFFFFF)
                )
                Text(
                    text = "OR",
                    color = Color(0xB3FFFFFF),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Divider(
                    modifier = Modifier.weight(1f),
                    color = Color(0x33FFFFFF)
                )
            }

            // Sign in with Email Button (Secondary)
            Button(
                onClick = onEmailLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(
                        width = 1.dp,
                        color = Color(0xFFffa000),
                        shape = RoundedCornerShape(12.dp)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFFffa000) // Orange color
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Sign in with Email",
                    color = Color(0xFFffa000), // Orange color
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Sign Up Link (positioned at bottom)
        Text(
            text = "Don't have an account? Sign up",
            color = Color(0xFF3b82f6),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
                .clickable { onNavigateToSignup() }
        )
    }
}