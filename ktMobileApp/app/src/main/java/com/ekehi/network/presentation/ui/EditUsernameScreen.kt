package com.ekehi.network.presentation.ui

import android.util.Log
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ekehi.network.data.model.UserProfile
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.presentation.viewmodel.ProfileViewModel
import com.ekehi.network.ui.theme.EkehiMobileTheme
import kotlin.collections.mutableMapOf
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun EditUsernameScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val userProfileResource by viewModel.userProfile.collectAsState()
    val scrollState = rememberScrollState()
    
    // Extract the actual UserProfile from Resource
    val userProfile: UserProfile? = when (userProfileResource) {
        is Resource.Success -> (userProfileResource as Resource.Success<UserProfile>).data
        else -> null
    }
    
    // Form state
    var username by remember { mutableStateOf(userProfile?.username ?: "") }
    var isUpdating by remember { mutableStateOf(false) }
    var updateError by remember { mutableStateOf<String?>(null) }
    
    // Reset updating state when profile update completes
    LaunchedEffect(userProfileResource) {
        if (isUpdating) {
            when (userProfileResource) {
                is Resource.Success -> {
                    isUpdating = false
                    // Navigate back on successful update
                    onNavigateBack()
                }
                is Resource.Error -> {
                    isUpdating = false
                    updateError = (userProfileResource as Resource.Error).message
                }
                else -> {}
            }
        }
    }
    
    // Update form fields when user profile loads
    LaunchedEffect(userProfile) {
        userProfile?.let {
            username = it.username ?: ""
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
                        text = "Edit Username",
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
                // Profile Picture Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0x1AFFFFFF) // 10% opacity white
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // User Avatar
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF8b5cf6),
                                            Color(0xFFa855f7)
                                        )
                                    ),
                                    shape = RoundedCornerShape(40.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "User Avatar",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { /* Handle profile picture change */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFffa000)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Change Photo",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                
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
                        // Username Field
                        OutlinedTextField(
                            value = username,
                            onValueChange = { 
                                username = it
                                // Clear error when user starts typing
                                if (updateError != null) {
                                    updateError = null
                                }
                            },
                            label = { 
                                Text(
                                    text = "Username",
                                    color = Color(0xB3FFFFFF)
                                )
                            },
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
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Save Button
                Button(
                    onClick = {
                        // Prepare updates for username only
                        val updates = mutableMapOf<String, Any>()
                        
                        // Only add username if it has changed
                        userProfile?.username?.let { currentUsername ->
                            if (username != currentUsername && username.isNotEmpty()) {
                                updates["username"] = username
                            }
                        } ?: run {
                            // If no current username and new username is not empty, add the new one
                            if (username.isNotEmpty()) {
                                updates["username"] = username
                            }
                        }
                        
                        if (updates.isNotEmpty()) {
                            isUpdating = true
                            updateError = null
                            Log.d("EditUsernameScreen", "Updating profile with: $updates")
                            viewModel.updateUserProfile(userProfile?.userId ?: "", updates)
                        } else {
                            Log.d("EditUsernameScreen", "No changes to save")
                            // Show a message that no changes were made
                        }
                    },
                    enabled = !isUpdating && username.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFffa000),
                        disabledContainerColor = Color(0x33FFFFFF)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "Save Changes",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Error Message
                updateError?.let {
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

@Preview(showBackground = true)
@Composable
fun EditUsernameScreenPreview() {
    EkehiMobileTheme {
        EditUsernameScreen(
            onNavigateBack = {}
        )
    }
}