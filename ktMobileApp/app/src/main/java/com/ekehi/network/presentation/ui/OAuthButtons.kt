package com.ekehi.network.presentation.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.activity.ComponentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.ekehi.network.R
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.presentation.viewmodel.OAuthViewModel
import com.ekehi.network.ui.theme.EkehiMobileTheme

@Composable
fun OAuthButtons(
        viewModel: OAuthViewModel = hiltViewModel(),
        onOAuthSuccess: () -> Unit = {},
        onOAuthRegistrationSuccess: () -> Unit = {}, // New callback for registration success
        isRegistration: Boolean = false // Flag to determine if this is for registration
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    val oauthState by viewModel.oauthState.collectAsState()
    
    // Listen for OAuth success broadcast
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // When the screen resumes, check if OAuth was successful
                // This handles the case where the OAuth flow completed while the app was in background
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Register broadcast receiver for OAuth success
    val oauthSuccessReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "com.ekehi.network.OAUTH_SUCCESS") {
                    // OAuth was successful, notify the view model
                    viewModel.handleOAuthResult(true)
                }
            }
        }
    }
    
    DisposableEffect(context) {
        val filter = IntentFilter("com.ekehi.network.OAUTH_SUCCESS")
        context.registerReceiver(oauthSuccessReceiver, filter)
        
        onDispose {
            try {
                context.unregisterReceiver(oauthSuccessReceiver)
            } catch (e: Exception) {
                // Receiver might not be registered, ignore
            }
        }
    }
    
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
                    // OAuth was successful, notify the parent composable
                    if (isRegistration) {
                        onOAuthRegistrationSuccess() // Navigate to secondary info screen
                    } else {
                        onOAuthSuccess() // Navigate to main screen
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
    
    Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Google Sign In Button
        GoogleSignInButton(
            onClick = {
                activity?.let {
                    viewModel.initiateGoogleOAuth(it)
                } ?: run {
                    errorMessage = "Activity not available"
                }
            },
            isLoading = isLoading,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        errorMessage?.let {
            Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    text: String = "Continue with Google",
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF1f1f1f)
        ),
        border = BorderStroke(1.dp, Color(0xFF747775)),
        enabled = enabled,
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 1.dp,
            pressedElevation = 2.dp,
            focusedElevation = 1.dp,
            hoveredElevation = 1.dp
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color(0xFF4285F4),
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.google),
                    contentDescription = "Google Logo",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = text,
                    color = Color(0xFF1f1f1f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OAuthButtonsPreview() {
    EkehiMobileTheme {
        OAuthButtons(
            onOAuthSuccess = {},
            onOAuthRegistrationSuccess = {},
            isRegistration = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OAuthButtonsRegistrationPreview() {
    EkehiMobileTheme {
        OAuthButtons(
            onOAuthSuccess = {},
            onOAuthRegistrationSuccess = {},
            isRegistration = true
        )
    }
}