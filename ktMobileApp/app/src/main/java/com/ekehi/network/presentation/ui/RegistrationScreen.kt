package com.ekehi.network.presentation.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.presentation.viewmodel.RegistrationViewModel
import com.ekehi.network.presentation.viewmodel.OAuthViewModel

@Composable
fun RegistrationScreen(
    viewModel: RegistrationViewModel = hiltViewModel(),
    oAuthViewModel: OAuthViewModel = hiltViewModel(),
    onRegistrationSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPasswordMismatch by remember { mutableStateOf(false) }
    var isTermsAccepted by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.registrationState) {
        viewModel.registrationState.collect { resource ->
            when (resource) {
                is Resource.Loading -> {
                    isLoading = true
                    errorMessage = null
                    showPasswordMismatch = false
                }
                is Resource.Success -> {
                    isLoading = false
                    onRegistrationSuccess()
                }
                is Resource.Error -> {
                    isLoading = false
                    errorMessage = resource.message
                }
                is Resource.Idle -> {
                    isLoading = false
                    errorMessage = null
                    showPasswordMismatch = false
                }
            }
        }
    }

    // Reset the state when the screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.resetState()
        oAuthViewModel.resetState()
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
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ekehi Logo
            Icon(
                imageVector = Icons.Default.AccountBalance,
                contentDescription = "Ekehi Logo",
                tint = Color(0xFFffa000),
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "Ekehi Network",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Full Name Input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name", color = Color(0xB3FFFFFF)) },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFffa000),
                    unfocusedBorderColor = Color(0x33FFFFFF),
                    cursorColor = Color(0xFFffa000),
                    focusedLabelColor = Color(0xFFffa000),
                    unfocusedLabelColor = Color(0xB3FFFFFF)
                )
            )

            // Email Input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = Color(0xB3FFFFFF)) },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFffa000),
                    unfocusedBorderColor = Color(0x33FFFFFF),
                    cursorColor = Color(0xFFffa000),
                    focusedLabelColor = Color(0xFFffa000),
                    unfocusedLabelColor = Color(0xB3FFFFFF)
                )
            )

            // Password Input with Strength Indicator
            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    // Clear error when user starts typing
                    if (errorMessage != null) {
                        errorMessage = null
                    }
                },
                label = { Text("Password", color = Color(0xB3FFFFFF)) },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                visualTransformation = if (isPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(
                        onClick = { isPasswordVisible = !isPasswordVisible }
                    ) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                            tint = Color(0xB3FFFFFF)
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFffa000),
                    unfocusedBorderColor = Color(0x33FFFFFF),
                    cursorColor = Color(0xFFffa000),
                    focusedLabelColor = Color(0xFFffa000),
                    unfocusedLabelColor = Color(0xB3FFFFFF)
                )
            )

            // Password Strength Indicator
            PasswordStrengthIndicator(password = password)

            Spacer(modifier = Modifier.height(8.dp))

            // Confirm Password Input
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { 
                    confirmPassword = it
                    // Clear error when user starts typing
                    if (errorMessage != null) {
                        errorMessage = null
                    }
                    // Check if passwords match as user types
                    showPasswordMismatch = confirmPassword.isNotEmpty() && password != confirmPassword
                },
                label = { Text("Confirm Password", color = Color(0xB3FFFFFF)) },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                visualTransformation = if (isConfirmPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(
                        onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }
                    ) {
                        Icon(
                            imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (isConfirmPasswordVisible) "Hide password" else "Show password",
                            tint = Color(0xB3FFFFFF)
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFffa000),
                    unfocusedBorderColor = Color(0x33FFFFFF),
                    cursorColor = Color(0xFFffa000),
                    focusedLabelColor = Color(0xFFffa000),
                    unfocusedLabelColor = Color(0xB3FFFFFF)
                )
            )

            if (showPasswordMismatch) {
                Text(
                    text = "Passwords do not match",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }

            // Terms & Conditions Checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isTermsAccepted,
                    onCheckedChange = { isTermsAccepted = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFFffa000),
                        uncheckedColor = Color(0xB3FFFFFF)
                    )
                )
                Text(
                    text = "I agree to the Terms & Conditions",
                    color = Color(0xB3FFFFFF),
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clickable { /* Handle terms navigation */ }
                )
            }

            // Sign Up Button
            Button(
                onClick = {
                    if (password != confirmPassword) {
                        showPasswordMismatch = true
                    } else if (!isTermsAccepted) {
                        errorMessage = "Please accept the Terms & Conditions"
                    } else {
                        showPasswordMismatch = false
                        viewModel.register(name, email, password)
                    }
                },
                enabled = !isLoading && name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFffa000),
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
                        text = "Sign Up",
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
                    .padding(bottom = 24.dp),
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

            // Google OAuth Button
            OAuthButtons(
                viewModel = oAuthViewModel,
                onOAuthSuccess = {
                    // When OAuth is successful for registration, we should navigate to login
                    // or check if we need to create a user profile
                    onNavigateToLogin()
                },
                isRegistration = true
            )

            // Login Link
            Text(
                text = "Already have an account? Login",
                color = Color(0xFF3b82f6),
                fontSize = 14.sp,
                modifier = Modifier.clickable { onNavigateToLogin() }
            )

            // Error Message
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
fun PasswordStrengthIndicator(password: String) {
    val (strength, color) = when {
        password.isEmpty() -> 0 to Color.Transparent
        password.length < 6 -> 1 to Color.Red
        password.length < 10 -> 2 to Color.Yellow
        else -> 3 to Color.Green
    }

    Column {
        LinearProgressIndicator(
            progress = when (strength) {
                0 -> 0f
                1 -> 0.33f
                2 -> 0.66f
                else -> 1f
            },
            color = color,
            trackColor = Color(0x33FFFFFF),
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
        )
        
        Text(
            text = when (strength) {
                0 -> ""
                1 -> "Weak"
                2 -> "Medium"
                else -> "Strong"
            },
            color = color,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}