package com.ekehi.network.presentation.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.presentation.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel.loginState) {
        Log.d("LoginScreen", "Observing login state changes")
        viewModel.loginState.collect { resource ->
            Log.d("LoginScreen", "Login state changed: ${resource.javaClass.simpleName}")
            when (resource) {
                is Resource.Loading -> {
                    Log.d("LoginScreen", "Setting loading state to true")
                    isLoading = true
                    errorMessage = null
                }
                is Resource.Success -> {
                    Log.d("LoginScreen", "Login successful, navigating to dashboard")
                    isLoading = false
                    onLoginSuccess()
                }
                is Resource.Error -> {
                    Log.e("LoginScreen", "Login error: ${resource.message}")
                    isLoading = false
                    errorMessage = resource.message
                }
                is Resource.Idle -> {
                    Log.d("LoginScreen", "Login state is idle")
                    isLoading = false
                    errorMessage = null
                }
            }
        }
    }

    // Reset the state when the screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Ekehi Mobile Login",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it
                // Clear error when user starts typing
                if (errorMessage != null) {
                    errorMessage = null
                }
            },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                // Clear error when user starts typing
                if (errorMessage != null) {
                    errorMessage = null
                }
            },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        Button(
            onClick = {
                Log.d("LoginScreen", "Login button clicked with email: $email")
                viewModel.login(email, password)
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Login")
            }
        }

        errorMessage?.let {
            Log.e("LoginScreen", "Displaying error message: $it")
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}