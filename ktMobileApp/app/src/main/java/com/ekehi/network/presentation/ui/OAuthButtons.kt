package com.ekehi.network.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.activity.ComponentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.presentation.viewmodel.OAuthViewModel
import com.ekehi.network.ui.theme.EkehiMobileTheme

@Composable
fun OAuthButtons(
        viewModel: OAuthViewModel = hiltViewModel(),
        onOAuthSuccess: () -> Unit = {},
        isRegistration: Boolean = false // Flag to determine if this is for registration
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val oauthState by viewModel.oauthState.collectAsState()

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
                    onOAuthSuccess()
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
        Button(
                onClick = {
                    activity?.let {
                        viewModel.initiateGoogleOAuth(it)
                    } ?: run {
                        errorMessage = "Activity not available"
                    }
                },
                modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4285F4) // Google blue color
                ),
                enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Google",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRegistration) "Sign up with Google" else "Sign in with Google",
                        color = Color.White
                    )
                }
            }
        }

        errorMessage?.let {
            Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OAuthButtonsPreview() {
    EkehiMobileTheme {
        OAuthButtons(
            onOAuthSuccess = {},
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
            isRegistration = true
        )
    }
}