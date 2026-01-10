package com.ekehi.network.presentation.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.presentation.viewmodel.LoginViewModel
import com.ekehi.network.presentation.viewmodel.OAuthViewModel
import com.ekehi.network.ui.theme.EkehiMobileTheme
import com.ekehi.network.R


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EmailLoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateToRegistration: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isRememberMeChecked by remember { mutableStateOf(false) }
    
    // Collect states properly
    val loginState by viewModel.loginState.collectAsState()
    
    // Derive UI states from the collected states
    val isEmailLoginLoading = loginState is Resource.Loading
    val emailLoginError = (loginState as? Resource.Error)?.message

    // Handle login success
    LaunchedEffect(loginState) {
        Log.d("EmailLoginScreen", "Login state changed: ${loginState.javaClass.simpleName}")
        when (loginState) {
            is Resource.Success -> {
                Log.d("EmailLoginScreen", "Email login successful, navigating to dashboard")
                onLoginSuccess()
            }
            is Resource.Error -> {
                // Use safe cast to avoid smart cast issues
                (loginState as? Resource.Error)?.let { error ->
                    Log.e("EmailLoginScreen", "Email login failed: ${error.message}")
                }
                // Error is automatically displayed via emailLoginError
            }
            is Resource.Loading -> {
                Log.d("EmailLoginScreen", "Email login in progress...")
            }
            is Resource.Idle -> {
                Log.d("EmailLoginScreen", "Email login state is idle")
            }
        }
    }


    // Reset the state when the screen is first displayed
    LaunchedEffect(Unit) {
        Log.d("EmailLoginScreen", "Screen mounted - resetting state")
        viewModel.resetState()
    }
    
    // Also reset state when the screen is disposed
    DisposableEffect(Unit) {
        onDispose {
            Log.d("EmailLoginScreen", "Screen disposed - resetting state")
            viewModel.resetState()
        }
    }

    // Get focus manager and keyboard controller
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    
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
        val scrollState = rememberScrollState()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ekehi Logo/Branding
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
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Password
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        Log.d("EmailLoginScreen", "=== LOGIN DONE (IME) PRESSED ===")
                        focusManager.clearFocus()
                        keyboardController?.hide()

                        val trimmedEmail = email.trim()
                        val trimmedPassword = password.trim()

                        Log.d("EmailLoginScreen", "Proceeding with login from IME...")
                        viewModel.login(trimmedEmail, trimmedPassword)
                    }
                ),
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
                    Log.d("EmailLoginScreen", "=== LOGIN BUTTON CLICKED ===")
                    Log.d("EmailLoginScreen", "Email: '$email'")
                    Log.d("EmailLoginScreen", "Password: '${password}'")
                    Log.d("EmailLoginScreen", "isEmailLoginLoading: $isEmailLoginLoading")

                    // Always clear focus and hide keyboard first
                    focusManager.clearFocus()
                    keyboardController?.hide()

                    val trimmedEmail = email.trim()
                    val trimmedPassword = password.trim()

                    Log.d("EmailLoginScreen", "Proceeding with login...")
                    viewModel.login(trimmedEmail, trimmedPassword)
                },
                // Only disable while a request is in progress; validation is handled in the ViewModel
                enabled = !isEmailLoginLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
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
                Log.e("EmailLoginScreen", "Displaying email login error message: $it")
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
fun EmailLoginScreenPreview() {
    EkehiMobileTheme {
        EmailLoginScreen(
            onLoginSuccess = {},
            onNavigateToRegistration = {}
        )
    }
}