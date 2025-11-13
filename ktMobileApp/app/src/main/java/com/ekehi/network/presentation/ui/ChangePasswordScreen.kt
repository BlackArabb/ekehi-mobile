package com.ekehi.network.presentation.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.presentation.viewmodel.SettingsViewModel

@Composable
fun ChangePasswordScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    // Form state
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var isCurrentPasswordVisible by remember { mutableStateOf(false) }
    var isNewPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPasswordMismatch by remember { mutableStateOf(false) }
    
    // Observe password change result
    val passwordChangeResult by viewModel.passwordChangeResult.collectAsState()
    
    // Handle password change result
    LaunchedEffect(passwordChangeResult) {
        Log.d("ChangePasswordScreen", "Password change result updated: ${passwordChangeResult?.javaClass?.simpleName}")
        when (passwordChangeResult) {
            is Resource.Success -> {
                Log.d("ChangePasswordScreen", "Password change successful, navigating back")
                isLoading = false
                Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show()
                onNavigateBack()
            }
            is Resource.Error -> {
                Log.d("ChangePasswordScreen", "Password change failed: ${(passwordChangeResult as Resource.Error).message}")
                isLoading = false
                errorMessage = (passwordChangeResult as Resource.Error).message
            }
            is Resource.Loading -> {
                Log.d("ChangePasswordScreen", "Password change in progress")
                isLoading = true
                errorMessage = null
            }
            else -> {
                Log.d("ChangePasswordScreen", "Password change result is null or idle")
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
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Top App Bar
            TopAppBar(
                title = {
                    Text(
                        text = "Change Password",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Description
                Text(
                    text = "Enter your current password and create a new password",
                    color = Color(0xB3FFFFFF),
                    fontSize = 16.sp,
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                        .fillMaxWidth()
                )
                
                // Form Fields
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0x1AFFFFFF) // 10% opacity white
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Current Password Field
                        OutlinedTextField(
                            value = currentPassword,
                            onValueChange = { 
                                currentPassword = it
                                // Clear error when user starts typing
                                if (errorMessage != null) {
                                    errorMessage = null
                                }
                            },
                            label = { 
                                Text(
                                    text = "Current Password",
                                    color = Color(0xB3FFFFFF)
                                )
                            },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            visualTransformation = if (isCurrentPasswordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { isCurrentPasswordVisible = !isCurrentPasswordVisible }
                                ) {
                                    Icon(
                                        imageVector = if (isCurrentPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (isCurrentPasswordVisible) "Hide password" else "Show password",
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
                        
                        // New Password Field
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { 
                                newPassword = it
                                // Clear error when user starts typing
                                if (errorMessage != null) {
                                    errorMessage = null
                                }
                                // Check if passwords match as user types
                                showPasswordMismatch = confirmNewPassword.isNotEmpty() && newPassword != confirmNewPassword
                            },
                            label = { 
                                Text(
                                    text = "New Password",
                                    color = Color(0xB3FFFFFF)
                                )
                            },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            visualTransformation = if (isNewPasswordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { isNewPasswordVisible = !isNewPasswordVisible }
                                ) {
                                    Icon(
                                        imageVector = if (isNewPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (isNewPasswordVisible) "Hide password" else "Show password",
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
                        ChangePasswordStrengthIndicator(password = newPassword)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Confirm New Password Field
                        OutlinedTextField(
                            value = confirmNewPassword,
                            onValueChange = { 
                                confirmNewPassword = it
                                // Clear error when user starts typing
                                if (errorMessage != null) {
                                    errorMessage = null
                                }
                                // Check if passwords match as user types
                                showPasswordMismatch = confirmNewPassword.isNotEmpty() && newPassword != confirmNewPassword
                            },
                            label = { 
                                Text(
                                    text = "Confirm New Password",
                                    color = Color(0xB3FFFFFF)
                                )
                            },
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
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Change Password Button
                Button(
                    onClick = {
                        // Validate form
                        if (newPassword != confirmNewPassword) {
                            showPasswordMismatch = true
                            return@Button
                        }
                        
                        if (newPassword.length < 6) {
                            errorMessage = "Password must be at least 6 characters long"
                            return@Button
                        }
                        
                        // Call the ViewModel to change the password
                        viewModel.changePassword(currentPassword, newPassword)
                    },
                    enabled = !isLoading && currentPassword.isNotEmpty() && newPassword.isNotEmpty() && confirmNewPassword.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
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
                            text = "Change Password",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
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
}

@Composable
fun ChangePasswordStrengthIndicator(password: String) {
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