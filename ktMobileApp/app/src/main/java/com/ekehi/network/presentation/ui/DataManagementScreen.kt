package com.ekehi.network.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.presentation.viewmodel.SettingsViewModel
import com.ekehi.network.util.EventBus
import com.ekehi.network.util.Event
import kotlinx.coroutines.launch

@Composable
fun DataManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    
    // Collect account deletion result
    val accountDeletionResult by viewModel.accountDeletionResult.collectAsState()
    
    // Handle account deletion result
    LaunchedEffect(accountDeletionResult) {
        accountDeletionResult?.let { resource ->
            when (resource) {
                is Resource.Success -> {
                    Log.d("DataManagementScreen", "Account deletion successful")
                    Toast.makeText(
                        context,
                        "Your account has been deleted successfully",
                        Toast.LENGTH_LONG
                    ).show()
                    onNavigateBack()
                }
                is Resource.Error -> {
                    Log.e("DataManagementScreen", "Account deletion failed: ${resource.message}")
                    Toast.makeText(
                        context,
                        "Failed to delete account: ${resource.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    isDeleting = false
                }
                is Resource.Loading -> {
                    Log.d("DataManagementScreen", "Account deletion in progress")
                }
                is Resource.Idle -> {
                    // Do nothing
                }
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
                        text = "Data Management",
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
                Text(
                    text = "Manage your personal data and privacy settings",
                    color = Color(0xB3FFFFFF),
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                        .fillMaxWidth()
                )
                
                // Data Management Options
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0x1AFFFFFF) // 10% opacity white
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        DataSectionHeader("Your Data")
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        DataOptionItem(
                            icon = Icons.Default.Download,
                            title = "Export Data",
                            description = "Download a copy of your personal data",
                            onClick = { showExportDialog = true }
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        DataOptionItem(
                            icon = Icons.Default.Delete,
                            title = "Delete Account",
                            description = "Permanently delete your account and all associated data",
                            onClick = { showDeleteDialog = true }
                        )
                        
            
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Data Information
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0x1AFFFFFF) // 10% opacity white
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        DataSectionHeader("Data Storage")
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        DataSectionText(
                            "Your data is securely stored on our servers and encrypted both in transit and at rest. " +
                                    "We retain your data for as long as your account is active or as needed to provide our services."
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        DataSectionText(
                            "When you delete your account, we begin deleting your personal data within 30 days. " +
                                    "Some information may be retained longer for legal or security purposes."
                        )
                    }
                }
            }
        }
        
        // Loading indicator for deletion
        if (isDeleting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFFffa000)
                )
            }
        }
    }
    
    // Export Data Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { if (!isExporting) showExportDialog = false },
            title = {
                Text(
                    text = "Export Data",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Your data export will be prepared and sent to your email address. This may take a few minutes.",
                    color = Color(0xB3FFFFFF)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isExporting = true
                            try {
                                // Get current user email
                                val user = viewModel.getCurrentUser()
                                val email = user?.email ?: "user@example.com"
                                
                                Log.d("DataManagementScreen", "Initiating data export for email: $email")
                                
                                // Send event for data export
                                EventBus.sendEvent(Event.UserDataExported(email))
                                
                                // In a real implementation, this would call an API to prepare and send the data
                                // For now, we'll just show a success message
                                kotlinx.coroutines.delay(1000) // Simulate API call
                                
                                Toast.makeText(
                                    context,
                                    "Data export initiated. You will receive an email shortly.",
                                    Toast.LENGTH_LONG
                                ).show()
                                
                                showExportDialog = false
                            } catch (e: Exception) {
                                Log.e("DataManagementScreen", "Export failed: ${e.message}", e)
                                Toast.makeText(
                                    context,
                                    "Failed to export data: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            } finally {
                                isExporting = false
                            }
                        }
                    },
                    enabled = !isExporting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFffa000)
                    )
                ) {
                    if (isExporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Exporting...")
                    } else {
                        Text("Confirm")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { if (!isExporting) showExportDialog = false }
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
    
    // Delete Account Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteDialog = false },
            title = {
                Text(
                    text = "Delete Account",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete your account? This action cannot be undone. " +
                            "All your data will be permanently deleted.",
                    color = Color(0xB3FFFFFF)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isDeleting = true
                            try {
                                Log.d("DataManagementScreen", "Initiating account deletion")
                                viewModel.deleteAccount()
                            } catch (e: Exception) {
                                Log.e("DataManagementScreen", "Delete failed: ${e.message}", e)
                                Toast.makeText(
                                    context,
                                    "Failed to delete account: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                                isDeleting = false
                            }
                        }
                    },
                    enabled = !isDeleting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFef4444)
                    )
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Deleting...")
                    } else {
                        Text("Delete")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { if (!isDeleting) showDeleteDialog = false }
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun DataOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        color = Color.Transparent,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFFffa000),
                modifier = Modifier.size(24.dp)
            )
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    color = Color(0xB3FFFFFF),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = Color(0xB3FFFFFF),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun DataSectionHeader(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun DataSectionText(text: String) {
    Text(
        text = text,
        color = Color(0xB3FFFFFF),
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
}