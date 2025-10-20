package com.ekehi.mobile.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ekehi.mobile.domain.model.Resource
import com.ekehi.mobile.presentation.viewmodel.OAuthViewModel

@Composable
fun OAuthButtons(
    viewModel: OAuthViewModel = hiltViewModel(),
    onOAuthInitiated: (oauthUrl: String) -> Unit
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel.oauthState) {
        viewModel.oauthState.collect { resource ->
            when (resource) {
                is Resource.Loading -> {
                    errorMessage = null
                }
                is Resource.Success -> {
                    onOAuthInitiated(resource.data)
                }
                is Resource.Error -> {
                    errorMessage = resource.message
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Google Sign In Button
        Button(
            onClick = { viewModel.initiateGoogleOAuth() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4285F4) // Google blue color
            )
        ) {
            Text(
                text = "Continue with Google",
                color = Color.White
            )
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