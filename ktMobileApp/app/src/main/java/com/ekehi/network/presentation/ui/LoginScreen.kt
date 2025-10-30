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
import androidx.hilt.navigation.compose.hiltViewModel
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.presentation.viewmodel.LoginViewModel
import com.ekehi.network.presentation.viewmodel.OAuthViewModel
import com.ekehi.network.ui.theme.EkehiMobileTheme
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    oAuthViewModel: OAuthViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isRememberMeChecked by remember { mutableStateOf(false) }
    
    // Separate states for email login
    var isEmailLoginLoading by remember { mutableStateOf(false) }
    var emailLoginError by remember { mutableStateOf<String?>(null) }
    
    // Separate states for OAuth
    var isOAuthLoading by remember { mutableStateOf(false) }
    var oAuthError by remember { mutableStateOf<String?>(null) }
    
    val coroutineScope = rememberCoroutineScope()

    // Handle login state changes
    val loginState by viewModel.loginState.collectAsState()
    LaunchedEffect(loginState) {
        Log.d("LoginScreen", "Login state changed: ${loginState.javaClass.simpleName}")
        when (loginState) {
            is Resource.Loading -> {
                Log.d("LoginScreen", "Setting email login loading state to true")
                isEmailLoginLoading = true
                emailLoginError = null
            }
            is Resource.Success -> {
                Log.d("LoginScreen", "Email login successful, navigating to dashboard")
                isEmailLoginLoading = false
                // Use coroutineScope to safely call onLoginSuccess
                coroutineScope.launch {
                    onLoginSuccess()
                }
            }
            is Resource.Error -> {
                Log.e("LoginScreen", "Email login error: ${(loginState as Resource.Error).message}")
                isEmailLoginLoading = false
                emailLoginError = (loginState as Resource.Error).message
            }
            is Resource.Idle -> {
                Log.d("LoginScreen", "Email login state is idle")
                isEmailLoginLoading = false
                emailLoginError = null
            }
        }
    }

    // Handle OAuth state changes
    val oauthState by oAuthViewModel.oauthState.collectAsState()
    LaunchedEffect(oauthState) {
        when (oauthState) {
            is Resource.Loading -> {
                isOAuthLoading = true
                oAuthError = null
            }
            is Resource.Success -> {
                isOAuthLoading = false
                // For OAuth, we need to check if we have a valid session
                // The OAuthCallbackActivity should have set up the session
                // Let's verify the current user to confirm successful authentication
                viewModel.checkCurrentUser()
            }
            is Resource.Error -> {
                isOAuthLoading = false
                oAuthError = (oauthState as Resource.Error).message
            }
            is Resource.Idle -> {
                isOAuthLoading = false
                oAuthError = null
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
                    // Clear error when user starts typing
                    if (emailLoginError != null) {
                        emailLoginError = null
                    }
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
                    // Clear error when user starts typing
                    if (emailLoginError != null) {
                        emailLoginError = null
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
                enabled = !isEmailLoginLoading && email.isNotEmpty() && password.isNotEmpty(),
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
                modifier = Modifier.clickable { /* Handle sign up navigation */ }
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
            onLoginSuccess = {}
        )
    }
}