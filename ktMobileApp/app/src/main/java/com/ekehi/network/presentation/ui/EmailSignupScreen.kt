package com.ekehi.network.presentation.ui

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
import com.ekehi.network.presentation.viewmodel.RegistrationViewModel
import com.ekehi.network.presentation.viewmodel.OAuthViewModel
import com.ekehi.network.ui.theme.EkehiMobileTheme
import com.ekehi.network.R

import androidx.compose.material3.ExperimentalMaterial3Api
import com.ekehi.network.presentation.ui.components.CountryDropdownField
import com.ekehi.network.domain.model.Country

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EmailSignupScreen(
    viewModel: RegistrationViewModel = hiltViewModel(),
    onRegistrationSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var referralCode by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var showPasswordMismatch by remember { mutableStateOf(false) }
    var isTermsAccepted by remember { mutableStateOf(false) }
    
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val countries by viewModel.countries.collectAsState()
    
    // Collect state properly
    val registrationState by viewModel.registrationState.collectAsState()
    
    // Derive UI states from the collected state
    val isLoading = registrationState is Resource.Loading
    val errorMessage = (registrationState as? Resource.Error)?.message

    // Calculate if button should be enabled
    val isSignUpButtonEnabled = remember(name, email, password, confirmPassword, isTermsAccepted, isLoading, phoneNumber, selectedCountry) {
        !isLoading && 
        name.trim().isNotEmpty() && 
        email.trim().isNotEmpty() && 
        password.trim().isNotEmpty() && 
        confirmPassword.trim().isNotEmpty() &&
        phoneNumber.trim().isNotEmpty() &&
        selectedCountry != null &&
        isTermsAccepted &&
        password.trim() == confirmPassword.trim()
    }

    // Handle registration success
    LaunchedEffect(registrationState) {
        Log.d("RegistrationScreen", "Registration state changed: ${registrationState.javaClass.simpleName}")
        when (registrationState) {
            is Resource.Success -> {
                Log.d("RegistrationScreen", "Registration successful, navigating to main screen")
                onRegistrationSuccess()
            }
            is Resource.Error -> {
                // Use safe cast to avoid smart cast issues
                (registrationState as? Resource.Error)?.let { error ->
                    Log.e("RegistrationScreen", "Registration failed: ${error.message}")
                }
                // Error is automatically displayed via errorMessage
            }
            is Resource.Loading -> {
                Log.d("RegistrationScreen", "Registration in progress...")
            }
            is Resource.Idle -> {
                Log.d("RegistrationScreen", "Registration state is idle")
            }
        }
    }

    // Reset the state when the screen is first displayed
    LaunchedEffect(Unit) {
        Log.d("EmailSignupScreen", "Screen mounted - resetting state")
        viewModel.resetState()
    }
    
    // Also reset state when the screen is disposed
    DisposableEffect(Unit) {
        onDispose {
            Log.d("EmailSignupScreen", "Screen disposed - resetting state")
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
            // Ekehi Logo
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher),
                contentDescription = "Ekehi Logo",
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "Create your account",
                color = Color.White,
                fontSize = 24.sp,
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

            // Warning about phone number
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0x33ffa000)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFffa000),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Important: Your phone number is the only way to recover your password. Please ensure it is active and reachable.",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Country Selection Dropdown
            CountryDropdownField(
                countries = countries,
                selectedCountry = selectedCountry,
                onCountrySelected = { viewModel.onCountrySelected(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Phone Number Input
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { viewModel.onPhoneNumberChanged(it) },
                label = { Text("Phone Number", color = Color(0xB3FFFFFF)) },
                placeholder = { Text("e.g. +234 800 000 0000", color = Color(0x66FFFFFF)) },
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
                ),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                )
            )

            // Password Input with Strength Indicator
            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    // Remove the error clearing logic as errorMessage is now a val
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
                    // Remove the error clearing logic as errorMessage is now a val
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

            // Referral Code Input
            OutlinedTextField(
                value = referralCode,
                onValueChange = { referralCode = it },
                label = { Text("Referral Code (Optional)", color = Color(0xB3FFFFFF)) },
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
                    Log.d("EmailSignupScreen", "=== SIGN UP BUTTON CLICKED ===")
                    Log.d("EmailSignupScreen", "Name: '$name'")
                    Log.d("EmailSignupScreen", "Email: '$email'")
                    Log.d("EmailSignupScreen", "Password: '${password}'")
                    Log.d("EmailSignupScreen", "Confirm Password: '${confirmPassword}'")
                    Log.d("EmailSignupScreen", "Referral Code: '$referralCode'")
                    Log.d("EmailSignupScreen", "Terms Accepted: $isTermsAccepted")
                    Log.d("EmailSignupScreen", "isSignUpButtonEnabled: $isSignUpButtonEnabled")
                    Log.d("EmailSignupScreen", "isLoading: $isLoading")
                    
                    // Clear focus and hide keyboard
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    
                    val trimmedName = name.trim()
                    val trimmedEmail = email.trim()
                    val trimmedPassword = password.trim()
                    val trimmedConfirmPassword = confirmPassword.trim()
                    val trimmedReferralCode = referralCode.trim()
                    
                    // Validate inputs
                    val isValid = trimmedName.isNotEmpty() && 
                                trimmedEmail.isNotEmpty() && 
                                trimmedPassword.isNotEmpty() && 
                                trimmedConfirmPassword.isNotEmpty() &&
                                isTermsAccepted &&
                                trimmedPassword == trimmedConfirmPassword &&
                                android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() &&
                                !isLoading
                    
                    if (isValid) {
                        Log.d("EmailSignupScreen", "All inputs valid, proceeding with registration...")
                        showPasswordMismatch = false
                        // Pass registration data including phone and country
                        viewModel.register(
                            trimmedName, 
                            trimmedEmail, 
                            trimmedPassword, 
                            trimmedReferralCode, 
                            phoneNumber.trim(), 
                            selectedCountry?.name ?: ""
                        )
                    } else {
                        Log.d("EmailSignupScreen", "Validation failed:")
                        if (trimmedName.isEmpty()) Log.d("EmailSignupScreen", "- Name is empty")
                        if (trimmedEmail.isEmpty()) Log.d("EmailSignupScreen", "- Email is empty")
                        if (trimmedPassword.isEmpty()) Log.d("EmailSignupScreen", "- Password is empty")
                        if (trimmedConfirmPassword.isEmpty()) Log.d("EmailSignupScreen", "- Confirm password is empty")
                        if (!isTermsAccepted) Log.d("EmailSignupScreen", "- Terms not accepted")
                        if (trimmedPassword != trimmedConfirmPassword) Log.d("EmailSignupScreen", "- Passwords don't match")
                        if (trimmedEmail.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                            Log.d("EmailSignupScreen", "- Invalid email format")
                        }
                        if (isLoading) Log.d("EmailSignupScreen", "- Already loading")
                        
                        // Show specific error messages
                        if (trimmedPassword != trimmedConfirmPassword) {
                            showPasswordMismatch = true
                        }
                    }
                },
                enabled = isSignUpButtonEnabled,
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
    
            Spacer(modifier = Modifier.height(16.dp))
    
            // Login Link
            Text(
                text = "Already have an account? Sign in",
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

@Preview(showBackground = true)
@Composable
fun EmailSignupScreenPreview() {
    EkehiMobileTheme {
        EmailSignupScreen(
            onRegistrationSuccess = {},
            onNavigateToLogin = {}
        )
    }
}