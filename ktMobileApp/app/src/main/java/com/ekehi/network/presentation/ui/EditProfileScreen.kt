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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.hilt.navigation.compose.hiltViewModel
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.data.model.UserProfile
import com.ekehi.network.presentation.viewmodel.ProfileViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val userProfileResource by viewModel.userProfile.collectAsState()
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var username by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    
    // Initialize fields when profile is loaded
    LaunchedEffect(userProfileResource) {
        if (userProfileResource is Resource.Success) {
            val profile = (userProfileResource as Resource.Success<UserProfile>).data
            username = profile.username ?: ""
            name = profile.name ?: ""
        }
    }

    val isLoading = userProfileResource is Resource.Loading
    val isUpdating = false // Placeholder for update state if needed

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
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Edit Profile",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFffa000))
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                ) {
                    // Username Field
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username", color = Color(0xB3FFFFFF)) },
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

                    // Full Name Field
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
                    
                    // Info message about non-editable fields
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0x1AFFFFFF)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Note: Phone number and country cannot be changed after registration for security reasons.",
                            color = Color(0xB3FFFFFF),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Save Button
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        
                        val profile = (userProfileResource as? Resource.Success)?.data
                        if (profile != null) {
                            val updates = mutableMapOf<String, Any>()
                            if (username != profile.username) updates["username"] = username
                            if (name != (profile.name ?: "")) updates["name"] = name
                            
                            if (updates.isNotEmpty()) {
                                viewModel.updateUserProfile(profile.userId, updates)
                                onNavigateBack()
                            } else {
                                onNavigateBack()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFffa000)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Save Changes",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
