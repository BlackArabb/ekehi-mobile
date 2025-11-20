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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.runtime.DisposableEffect
import androidx.hilt.navigation.compose.hiltViewModel
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.presentation.viewmodel.LoginViewModel
import com.ekehi.network.presentation.viewmodel.OAuthViewModel
import com.ekehi.network.ui.theme.EkehiMobileTheme

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    oAuthViewModel: OAuthViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateToRegistration: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isRememberMeChecked by remember { mutableStateOf(false) }
    
    // Collect states properly
    val loginState by viewModel.loginState.collectAsState()
    val oauthState by oAuthViewModel.oauthState.collectAsState()
    
    // Derive UI states from the collected states
    val isEmailLoginLoading = loginState is Resource.Loading
    val emailLoginError = (loginState as? Resource.Error)?.message
    val oAuthError = (oauthState as? Resource.Error)?.message

    // Calculate if button should be enabled
    val isLoginButtonEnabled = remember(email, password, isEmailLoginLoading) {
        !isEmailLoginLoading && email.trim().isNotEmpty() && password.trim().isNotEmpty()
    }

    // Handle login success
    LaunchedEffect(loginState) {
        Log.d("LoginScreen", "Login state changed: ${loginState.javaClass.simpleName}")
        if (loginState is Resource.Success) {
            Log.d("LoginScreen", "Email login successful, navigating to dashboard")
            onLoginSuccess()
        }
    }

    // Handle OAuth success
    LaunchedEffect(oauthState) {
        if (oauthState is Resource.Success) {
            // For OAuth, verify the current user
            viewModel.checkCurrentUser()
        }
    }

    // Reset the state when the screen is first displayed
    LaunchedEffect(Unit) {
        Log.d("LoginScreen", "Screen mounted - resetting states")
        viewModel.resetState()
        oAuthViewModel.resetState()
    }
    
    // Also reset state when the screen is disposed
    DisposableEffect(Unit) {
        onDispose {
            Log.d("LoginScreen", "Screen disposed - resetting states")
            viewModel.resetState()
            oAuthViewModel.resetState()
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
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ekehi Logo/Branding
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

            // Email Input
            OutlinedTextField(
                value = email,
                onValueChange = { 
                    email = it
                    // Remove the error clearing logic as emailLoginError is now a val
                },
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

            // Password Input with Show/Hide Toggle
            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    // Remove the error clearing logic as emailLoginError is now a val
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
                    .padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFffa000),
                    unfocusedBorderColor = Color(0x33FFFFFF),
                    cursorColor = Color(0xFFffa000),
                    focusedLabelColor = Color(0xFFffa000),
                    unfocusedLabelColor = Color(0xB3FFFFFF)
                )
            )

            // Remember Me Checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isRememberMeChecked,
                    onCheckedChange = { isRememberMeChecked = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFFffa000),
                        uncheckedColor = Color(0xB3FFFFFF)
                    )
                )
                Text(
                    text = "Remember me",
                    color = Color(0xB3FFFFFF),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Login Button
            Button(
                onClick = {
                    Log.d("LoginScreen", "Login button clicked with email: $email")
                    viewModel.login(email, password)
                },
                enabled = isLoginButtonEnabled,
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
                if (isEmailLoginLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Login",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Forgot Password Link
            Text(
                text = "Forgot Password?",
                color = Color(0xFF3b82f6),
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .clickable { /* Handle forgot password */ }
            )

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
                    // When OAuth is successful, check the current user
                    viewModel.checkCurrentUser()
                },
                isRegistration = false
            )

            // Sign Up Link
            Text(
                text = "Don't have an account? Sign up",
                color = Color(0xFF3b82f6),
                fontSize = 14.sp,
                modifier = Modifier.clickable { 
                    onNavigateToRegistration()
                }
            )

            // Error Messages
            emailLoginError?.let {
                Log.e("LoginScreen", "Displaying email login error message: $it")
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            
            oAuthError?.let {
                Log.e("LoginScreen", "Displaying OAuth error message: $it")
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    EkehiMobileTheme {
        LoginScreen(
            onLoginSuccess = {},
            onNavigateToRegistration = {}
        )
    }
}