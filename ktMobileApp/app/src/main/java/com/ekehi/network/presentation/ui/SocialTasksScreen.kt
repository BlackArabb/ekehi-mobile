package com.ekehi.network.presentation.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ekehi.network.auth.SocialAuthManager
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.presentation.viewmodel.SocialTasksViewModel
import com.ekehi.network.presentation.viewmodel.VerificationState
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn

// Brand Colors
object BrandColors {
    val Primary = Color(0xFFffa000)
    val PrimaryDark = Color(0xFFcc8000)
    val PrimaryLight = Color(0xFFffb333)
    val Black = Color(0xFF000000)
    val DarkGray = Color(0xFF1a1a1a)
    val MediumGray = Color(0xFF2d2d2d)
    val LightGray = Color(0xFF404040)
    val White = Color(0xFFFFFFFF)
    val Success = Color(0xFF10b981)
    val Error = Color(0xFFef4444)
    val Warning = Color(0xFFf59e0b)
    val CardBackground = Color(0xFF0d0d0d)
    val CardBorder = Color(0x4DFFA000) // 30% opacity orange
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialTasksScreen(
    viewModel: SocialTasksViewModel = hiltViewModel(),
    authManager: SocialAuthManager
) {
    val context = LocalContext.current
    var userId by remember { mutableStateOf("user_id_placeholder") }
    val socialTasksResource by viewModel.socialTasks.collectAsState()
    val verificationState by viewModel.verificationState.collectAsState()
    
    val youtubeSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        android.util.Log.d("SocialTasksScreen", "=== YouTube OAuth Result ===")
        android.util.Log.d("SocialTasksScreen", "Result code: ${result.resultCode}")
        android.util.Log.d("SocialTasksScreen", "RESULT_OK: ${Activity.RESULT_OK}")
        android.util.Log.d("SocialTasksScreen", "RESULT_CANCELED: ${Activity.RESULT_CANCELED}")
        android.util.Log.d("SocialTasksScreen", "Has data: ${result.data != null}")
        
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                result.data?.let { data ->
                    try {
                        android.util.Log.d("SocialTasksScreen", "Processing sign-in result...")
                        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                        
                        task.addOnSuccessListener { account ->
                            android.util.Log.d("SocialTasksScreen", "Sign-in SUCCESS")
                            android.util.Log.d("SocialTasksScreen", "Account email: ${account.email}")
                            android.util.Log.d("SocialTasksScreen", "Has serverAuthCode: ${account.serverAuthCode != null}")
                            android.util.Log.d("SocialTasksScreen", "Has idToken: ${account.idToken != null}")
                            
                            // Get the access token
                            val accessToken = authManager.getYouTubeAccessToken(account)
                            
                            if (accessToken != null && accessToken.isNotEmpty()) {
                                viewModel.setYouTubeAccessToken(accessToken)
                                android.util.Log.d("SocialTasksScreen", "YouTube access token set successfully")
                                
                                // Show success toast
                                android.widget.Toast.makeText(
                                    context,
                                    "YouTube account connected!",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                android.util.Log.e("SocialTasksScreen", "Failed to get YouTube access token")
                                android.widget.Toast.makeText(
                                    context,
                                    "Failed to get access token. Please try again.",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        }.addOnFailureListener { e ->
                            android.util.Log.e("SocialTasksScreen", "Sign-in FAILED: ${e.message}", e)
                            android.widget.Toast.makeText(
                                context,
                                "Sign-in failed: ${e.message}",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("SocialTasksScreen", "Error processing sign-in: ${e.message}", e)
                        android.widget.Toast.makeText(
                            context,
                            "Error: ${e.message}",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                } ?: run {
                    android.util.Log.e("SocialTasksScreen", "Result data is NULL")
                    android.widget.Toast.makeText(
                        context,
                        "No data received from Google",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
            Activity.RESULT_CANCELED -> {
                android.util.Log.w("SocialTasksScreen", "User cancelled sign-in")
                android.widget.Toast.makeText(
                    context,
                    "Sign-in cancelled",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                android.util.Log.e("SocialTasksScreen", "Unknown result code: ${result.resultCode}")
                android.widget.Toast.makeText(
                    context,
                    "Unexpected result: ${result.resultCode}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
        android.util.Log.d("SocialTasksScreen", "=== End YouTube OAuth Result ===")
    }
    
    val facebookLoginLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            android.util.Log.d("SocialTasksScreen", "=== Facebook OAuth Result ===")
            android.util.Log.d("SocialTasksScreen", "Result code: ${result.resultCode}")
            android.util.Log.d("SocialTasksScreen", "Has data: ${result.data != null}")
            
            authManager.handleFacebookResult(
                requestCode = result.resultCode,
                resultCode = Activity.RESULT_OK,
                data = result.data
            )
            
            android.util.Log.d("SocialTasksScreen", "Facebook result handled")
            android.util.Log.d("SocialTasksScreen", "=== End Facebook OAuth Result ===")
        } catch (e: Exception) {
            android.util.Log.e("SocialTasksScreen", "Facebook login error in launcher: ${e.message}", e)
            android.widget.Toast.makeText(
                context,
                "Facebook error: ${e.message}",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
    
    var selectedTask by remember { mutableStateOf<SocialTaskItem?>(null) }
    var showVerificationDialog by remember { mutableStateOf(false) }
    var taskActionCompleted by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.loadSocialTasks()
        viewModel.loadUserSocialTasks(userId)
    }
    
    // Handle verification state changes and refresh
    LaunchedEffect(verificationState) {
        when (verificationState) {
            is VerificationState.Success -> {
                // Refresh tasks after success
                viewModel.loadUserSocialTasks(userId)
                viewModel.loadSocialTasks()
            }
            is VerificationState.Error -> {
                // Also refresh on error to show updated status
                viewModel.loadUserSocialTasks(userId)
                viewModel.loadSocialTasks()
            }
            else -> {}
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BrandColors.Black,
                        BrandColors.DarkGray
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Header with gradient accent and refresh button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Social Tasks",
                        color = BrandColors.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(4.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        BrandColors.Primary,
                                        BrandColors.PrimaryLight
                                    )
                                ),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
                
                // Refresh button
                IconButton(
                    onClick = {
                        viewModel.loadSocialTasks()
                        viewModel.loadUserSocialTasks(userId)
                    },
                    modifier = Modifier
                        .background(
                            BrandColors.Primary.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = BrandColors.Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Enhanced Stats Section
            EnhancedStatsSection(viewModel)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Tasks List
            when (socialTasksResource) {
                is Resource.Success -> {
                    val tasks = (socialTasksResource as Resource.Success).data
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(tasks) { task ->
                            val taskItem = SocialTaskItem(
                                id = task.id,
                                title = task.title,
                                description = task.description,
                                platform = task.platform,
                                taskType = task.taskType,
                                link = task.actionUrl ?: "",
                                reward = task.rewardCoins,
                                isCompleted = task.isCompleted,
                                isVerified = task.isVerified,
                                verificationMethod = task.verificationMethod
                            )
                            
                            EnhancedSocialTaskCard(
                                task = taskItem,
                                onClick = { 
                                    selectedTask = taskItem
                                    taskActionCompleted = false
                                },
                                verificationState = verificationState
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = BrandColors.Primary,
                            strokeWidth = 3.dp
                        )
                    }
                }
                is Resource.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = BrandColors.Error.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = BrandColors.Error
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Error loading tasks",
                                color = BrandColors.Error,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                else -> {}
            }
        }
        
        // Task Action Dialog
        if (selectedTask != null && !showVerificationDialog) {
            TaskActionDialog(
                task = selectedTask!!,
                onDismiss = { 
                    selectedTask = null
                    taskActionCompleted = false
                },
                onTaskCompleted = {
                    taskActionCompleted = true
                    showVerificationDialog = true
                }
            )
        }
        
        // Verification Dialog
        if (selectedTask != null && showVerificationDialog) {
            TaskVerificationDialog(
                task = selectedTask!!,
                viewModel = viewModel,
                authManager = authManager,
                youtubeSignInLauncher = youtubeSignInLauncher,
                onDismiss = { 
                    selectedTask = null
                    showVerificationDialog = false
                    taskActionCompleted = false
                },
                onSubmit = { proofData ->
                    viewModel.completeSocialTask(userId, selectedTask!!.id, proofData)
                    selectedTask = null
                    showVerificationDialog = false
                    taskActionCompleted = false
                }
            )
        }
        
        // Verification State Snackbar
        when (verificationState) {
            is VerificationState.Success -> {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = BrandColors.Success,
                    contentColor = BrandColors.White
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = BrandColors.White)
                        Spacer(Modifier.width(8.dp))
                        Text((verificationState as VerificationState.Success).message)
                    }
                }
            }
            is VerificationState.Error -> {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = BrandColors.Error,
                    contentColor = BrandColors.White
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, null, tint = BrandColors.White)
                        Spacer(Modifier.width(8.dp))
                        Text((verificationState as VerificationState.Error).message)
                    }
                }
            }
            is VerificationState.Pending -> {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = BrandColors.Warning,
                    contentColor = BrandColors.Black
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, null, tint = BrandColors.Black)
                        Spacer(Modifier.width(8.dp))
                        Text((verificationState as VerificationState.Pending).message)
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun EnhancedStatsSection(viewModel: SocialTasksViewModel) {
    val socialTasksResource by viewModel.socialTasks.collectAsState()
    
    var completedTasks = 0
    var totalTasks = 0
    var totalRewards = 0.0
    
    if (socialTasksResource is Resource.Success) {
        val tasks = (socialTasksResource as Resource.Success).data
        totalTasks = tasks.size
        completedTasks = tasks.count { it.isCompleted }
        totalRewards = tasks.filter { it.isCompleted }.sumOf { it.rewardCoins }
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        BrandColors.CardBackground,
                        BrandColors.MediumGray
                    )
                ),
                RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        BrandColors.Primary,
                        BrandColors.PrimaryLight
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        EnhancedStatCard(
            icon = Icons.Default.TaskAlt,
            value = "$completedTasks/$totalTasks",
            label = "Completed",
            modifier = Modifier.weight(1f)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        EnhancedStatCard(
            icon = Icons.Default.Toll,
            value = String.format("%.1f", totalRewards),
            label = "EKH Earned",
            modifier = Modifier.weight(1f),
            isHighlight = true
        )
    }
}

@Composable
fun EnhancedStatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    isHighlight: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isHighlight) BrandColors.Primary else BrandColors.PrimaryLight,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = value,
            color = BrandColors.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = BrandColors.White.copy(alpha = 0.7f),
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun EnhancedSocialTaskCard(
    task: SocialTaskItem,
    onClick: () -> Unit,
    verificationState: VerificationState
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .then(
                if (!task.isVerified) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = BrandColors.CardBackground
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box {
            // Gradient accent line at top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                getPlatformColor(task.platform),
                                getPlatformColor(task.platform).copy(alpha = 0.5f)
                            )
                        )
                    )
            )
            
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Platform icon with gradient background
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(4.dp, RoundedCornerShape(16.dp))
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        getPlatformColor(task.platform),
                                        getPlatformColor(task.platform).copy(alpha = 0.7f)
                                    )
                                ),
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getPlatformIcon(task.platform),
                            contentDescription = task.platform,
                            tint = BrandColors.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Spacer(Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            color = BrandColors.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = task.description,
                            color = BrandColors.White.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp),
                            lineHeight = 20.sp
                        )
                        
                        Spacer(Modifier.height(8.dp))
                        
                        // Verification badge
                        Row(
                            modifier = Modifier
                                .background(
                                    color = if (task.verificationMethod == "api") 
                                        BrandColors.Success.copy(alpha = 0.15f)
                                    else 
                                        BrandColors.Warning.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    if (task.verificationMethod == "api") 
                                        BrandColors.Success.copy(alpha = 0.3f)
                                    else 
                                        BrandColors.Warning.copy(alpha = 0.3f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (task.verificationMethod == "api") 
                                    Icons.Default.Verified 
                                else 
                                    Icons.Default.Schedule,
                                contentDescription = null,
                                tint = if (task.verificationMethod == "api") 
                                    BrandColors.Success 
                                else 
                                    BrandColors.Warning,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (task.verificationMethod == "api") 
                                    "Auto-verified" 
                                else 
                                    "Manual review",
                                color = if (task.verificationMethod == "api") 
                                    BrandColors.Success 
                                else 
                                    BrandColors.Warning,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                Divider(
                    color = BrandColors.LightGray.copy(alpha = 0.3f),
                    thickness = 1.dp
                )
                
                Spacer(Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reward badge
                    Row(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        BrandColors.Primary.copy(alpha = 0.2f),
                                        BrandColors.PrimaryLight.copy(alpha = 0.1f)
                                    )
                                ),
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.5.dp,
                                BrandColors.Primary.copy(alpha = 0.5f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Toll,
                            contentDescription = null,
                            tint = BrandColors.Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "+${task.reward}",
                            color = BrandColors.Primary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "EKH",
                            color = BrandColors.Primary.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    if (task.isVerified) {
                        Row(
                            modifier = Modifier
                                .background(
                                    BrandColors.Success.copy(alpha = 0.15f),
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    1.5.dp,
                                    BrandColors.Success.copy(alpha = 0.3f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                "Verified",
                                tint = BrandColors.Success,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Completed",
                                color = BrandColors.Success,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Button(
                            onClick = onClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandColors.Primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 2.dp
                            )
                        ) {
                            Text(
                                "Start Task",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskActionDialog(
    task: SocialTaskItem,
    onDismiss: () -> Unit,
    onTaskCompleted: () -> Unit
) {
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BrandColors.MediumGray,
        shape = RoundedCornerShape(24.dp),
        title = { 
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            getPlatformColor(task.platform).copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getPlatformIcon(task.platform),
                        contentDescription = null,
                        tint = getPlatformColor(task.platform),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    task.title,
                    color = BrandColors.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrandColors.White.copy(alpha = 0.8f),
                    lineHeight = 22.sp
                )
                
                Divider(color = BrandColors.LightGray.copy(alpha = 0.3f))
                
                // Task Instructions
                Column(
                    modifier = Modifier
                        .background(
                            BrandColors.CardBackground,
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            BrandColors.Primary.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = BrandColors.Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "How to complete:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = BrandColors.White
                        )
                    }
                    
                    Text(
                        text = getTaskInstructions(task.platform, task.taskType),
                        style = MaterialTheme.typography.bodySmall,
                        color = BrandColors.White.copy(alpha = 0.7f),
                        lineHeight = 20.sp
                    )
                }
                
                Divider(color = BrandColors.LightGray.copy(alpha = 0.3f))
                
                // Reward info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    BrandColors.Primary.copy(alpha = 0.15f),
                                    BrandColors.PrimaryLight.copy(alpha = 0.05f)
                                )
                            ),
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            BrandColors.Primary.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reward:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrandColors.White
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Toll,
                            contentDescription = null,
                            tint = BrandColors.Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "+${task.reward} EKH",
                            color = BrandColors.Primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
                
                // Open Platform Button
                if (task.link.isNotEmpty()) {
                    Button(
                        onClick = {
                            try {
                                val url = if (task.link.startsWith("http://") || task.link.startsWith("https://")) {
                                    task.link
                                } else {
                                    "https://${task.link}"
                                }
                                
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                when (task.platform.lowercase()) {
                                    "telegram" -> {
                                        try {
                                            val intent = context.packageManager.getLaunchIntentForPackage("org.telegram.messenger")
                                            if (intent != null) {
                                                context.startActivity(intent)
                                            } else {
                                                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://web.telegram.org"))
                                                context.startActivity(webIntent)
                                            }
                                        } catch (ex: Exception) {
                                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://telegram.org"))
                                            context.startActivity(webIntent)
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = getPlatformColor(task.platform)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(Icons.Default.OpenInNew, "Open")
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Open on ${task.platform.capitalize()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = BrandColors.Error.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = BrandColors.Error
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "⚠️ No action URL configured. Please contact support.",
                                style = MaterialTheme.typography.bodySmall,
                                color = BrandColors.Error
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onTaskCompleted,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandColors.Success
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Default.Check, "Done")
                Spacer(Modifier.width(8.dp))
                Text(
                    "I've Completed This",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = BrandColors.White.copy(alpha = 0.7f)
                )
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TaskVerificationDialog(
    task: SocialTaskItem,
    viewModel: SocialTasksViewModel,
    authManager: SocialAuthManager,
    youtubeSignInLauncher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>,
    onDismiss: () -> Unit,
    onSubmit: (Map<String, Any>) -> Unit
) {
    val context = LocalContext.current
    var telegramUserId by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var proofUrl by remember { mutableStateOf("") }
    var facebookAccessToken by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BrandColors.MediumGray,
        shape = RoundedCornerShape(24.dp),
        title = { 
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            BrandColors.Success.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = BrandColors.Success,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    "Verify Completion",
                    color = BrandColors.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            // Wrap content in a LazyColumn to make it scrollable
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp), // Set max height for scrolling
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Now let's verify that you've completed the task.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrandColors.White
                    )
                }
                
                item {
                    when {
                        task.platform.lowercase() == "telegram" -> {
                            TelegramVerificationUI(
                                telegramUserId = telegramUserId,
                                onTelegramUserIdChange = { telegramUserId = it }
                            )
                        }
                        
                        task.platform.lowercase() == "youtube" -> {
                            YouTubeVerificationUI(
                                taskType = task.taskType,
                                actionUrl = task.link,
                                onConnectYouTube = {
                                    val signInIntent = authManager.getYouTubeSignInClient().signInIntent
                                    youtubeSignInLauncher.launch(signInIntent)
                                }
                            )
                        }
                        
                        task.platform.lowercase() == "facebook" -> {
                            FacebookVerificationUI(
                                actionUrl = task.link,
                                onConnectFacebook = {
                                    authManager.loginWithFacebook(
                                        loginManager = LoginManager.getInstance(),
                                        onSuccess = { accessToken ->
                                            facebookAccessToken = accessToken
                                        },
                                        onError = { error -> }
                                    )
                                    LoginManager.getInstance().logInWithReadPermissions(
                                        context as Activity,
                                        listOf("user_likes")
                                    )
                                }
                            )
                        }
                        
                        else -> {
                            ManualVerificationUI(
                                platform = task.platform,
                                username = username,
                                proofUrl = proofUrl,
                                onUsernameChange = { username = it },
                                onProofUrlChange = { proofUrl = it }
                            )
                        }
                    }
                }
                
                if (errorMessage.isNotEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = BrandColors.Error.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = BrandColors.Error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = errorMessage,
                                    color = BrandColors.Error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isLoading = true
                    errorMessage = ""
                    
                    val proofData = buildProofData(
                        platform = task.platform,
                        telegramUserId = telegramUserId,
                        username = username,
                        proofUrl = proofUrl
                    )
                    
                    val enhancedProofData = if (task.platform.lowercase() == "facebook" && facebookAccessToken != null) {
                        proofData.toMutableMap().apply {
                            put("facebook_access_token", facebookAccessToken!!)
                        }
                    } else {
                        proofData
                    }
                    
                    if (task.platform.lowercase() == "telegram" && !isValidTelegramUserId(telegramUserId)) {
                        errorMessage = getTelegramUserIdErrorMessage(telegramUserId)
                        if (errorMessage.isEmpty()) {
                            errorMessage = "Please enter a valid Telegram User ID"
                        }
                        isLoading = false
                    } else if (enhancedProofData.isEmpty()) {
                        errorMessage = "Please provide required information"
                        isLoading = false
                    } else {
                        onSubmit(enhancedProofData)
                    }
                },
                enabled = !isLoading && isReadyToSubmit(
                    platform = task.platform,
                    telegramUserId = telegramUserId,
                    username = username,
                    proofUrl = proofUrl
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandColors.Primary,
                    disabledContainerColor = BrandColors.LightGray
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = BrandColors.White
                    )
                } else {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Submit Verification", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = BrandColors.White.copy(alpha = 0.7f)
                )
            ) {
                Text("Back")
            }
        }
    )
}

@Composable
fun TelegramVerificationUI(
    telegramUserId: String,
    onTelegramUserIdChange: (String) -> Unit
) {
    val context = LocalContext.current
    val errorMessage = getTelegramUserIdErrorMessage(telegramUserId)
    val hasError = errorMessage.isNotEmpty()
    
    Column(
        modifier = Modifier
            .background(BrandColors.CardBackground, RoundedCornerShape(12.dp))
            .border(1.dp, BrandColors.Primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color(0xFF0088CC).copy(alpha = 0.1f),
                    RoundedCornerShape(8.dp)
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFF0088CC),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Get your Telegram User ID to verify",
                style = MaterialTheme.typography.bodySmall,
                color = BrandColors.White,
                fontWeight = FontWeight.Bold
            )
        }
        
        Text(
            text = "Steps to get your User ID:",
            style = MaterialTheme.typography.bodyMedium,
            color = BrandColors.White,
            fontWeight = FontWeight.Bold
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    BrandColors.Black.copy(alpha = 0.3f),
                    RoundedCornerShape(8.dp)
                )
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "1️⃣ Click 'Open Verification Bot' button below",
                style = MaterialTheme.typography.bodySmall,
                color = BrandColors.White.copy(alpha = 0.9f),
                lineHeight = 18.sp
            )
            Text(
                text = "2️⃣ Send /start to the bot",
                style = MaterialTheme.typography.bodySmall,
                color = BrandColors.White.copy(alpha = 0.9f),
                lineHeight = 18.sp
            )
            Text(
                text = "3️⃣ Bot will reply with your User ID (numbers only)",
                style = MaterialTheme.typography.bodySmall,
                color = BrandColors.White.copy(alpha = 0.9f),
                lineHeight = 18.sp
            )
            Text(
                text = "4️⃣ Copy the ID and paste it below",
                style = MaterialTheme.typography.bodySmall,
                color = BrandColors.White.copy(alpha = 0.9f),
                lineHeight = 18.sp
            )
        }
        
        // Button to open verification bot
        Button(
            onClick = {
                try {
                    // Open the ekehi_task_bot specifically
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/ekehi_task_bot"))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Fallback to opening Telegram app
                    try {
                        val intent = context.packageManager.getLaunchIntentForPackage("org.telegram.messenger")
                        if (intent != null) {
                            context.startActivity(intent)
                        } else {
                            // Open web version
                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://web.telegram.org"))
                            context.startActivity(webIntent)
                        }
                    } catch (ex: Exception) {
                        // Last resort
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0088CC)
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Icon(Icons.Default.OpenInNew, "Open", tint = BrandColors.White)
            Spacer(Modifier.width(8.dp))
            Text(
                "Open Verification Bot",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
        
        Spacer(Modifier.height(4.dp))
        
        Text(
            text = "Enter your User ID:",
            style = MaterialTheme.typography.bodyMedium,
            color = BrandColors.White,
            fontWeight = FontWeight.Bold
        )
        
        OutlinedTextField(
            value = telegramUserId,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() } || newValue.isEmpty()) {
                    onTelegramUserIdChange(newValue)
                }
            },
            label = { Text("Telegram User ID", color = BrandColors.White.copy(alpha = 0.7f)) },
            placeholder = { Text("e.g., 123456789", color = BrandColors.White.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Tag, "ID", tint = Color(0xFF0088CC))
            },
            isError = hasError,
            supportingText = {
                if (hasError) {
                    Text(errorMessage, color = BrandColors.Error)
                } else {
                    Text("Only numbers, 8-12 digits", color = BrandColors.White.copy(alpha = 0.6f))
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandColors.Primary,
                unfocusedBorderColor = BrandColors.LightGray,
                focusedTextColor = BrandColors.White,
                unfocusedTextColor = BrandColors.White,
                cursorColor = BrandColors.Primary,
                errorBorderColor = BrandColors.Error
            )
        )
        
        // Help text
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    BrandColors.Warning.copy(alpha = 0.1f),
                    RoundedCornerShape(8.dp)
                )
                .border(
                    1.dp,
                    BrandColors.Warning.copy(alpha = 0.3f),
                    RoundedCornerShape(8.dp)
                )
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Lightbulb,
                contentDescription = null,
                tint = BrandColors.Warning,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Make sure you've already joined the channel before verifying!",
                style = MaterialTheme.typography.bodySmall,
                color = BrandColors.White.copy(alpha = 0.9f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun YouTubeVerificationUI(
    taskType: String,
    actionUrl: String,
    isAlreadySignedIn: Boolean = false,
    onConnectYouTube: () -> Unit
) {
    var isConnecting by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    // Reset connecting state after a timeout
    LaunchedEffect(isConnecting) {
        if (isConnecting) {
            kotlinx.coroutines.delay(10000) // 10 seconds timeout
            isConnecting = false
            android.util.Log.w("YouTubeVerificationUI", "Connection timeout - resetting state")
        }
    }
    
    Column(
        modifier = Modifier
            .background(BrandColors.CardBackground, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFFF0000).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isAlreadySignedIn) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        BrandColors.Success.copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    )
                    .border(
                        1.dp,
                        BrandColors.Success.copy(alpha = 0.3f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = BrandColors.Success,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Already connected with Google account",
                    style = MaterialTheme.typography.bodySmall,
                    color = BrandColors.Success,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Text(
            text = if (isAlreadySignedIn) 
                "We'll verify using your connected account" 
            else 
                "Connect your YouTube account to verify automatically",
            style = MaterialTheme.typography.bodyMedium,
            color = BrandColors.White,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "We'll check if you've completed the task on YouTube.",
            style = MaterialTheme.typography.bodySmall,
            color = BrandColors.White.copy(alpha = 0.7f)
        )
        
        if (!isAlreadySignedIn) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color(0xFFFF0000).copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFFFF0000),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Make sure you've completed the task before connecting",
                    style = MaterialTheme.typography.bodySmall,
                    color = BrandColors.White.copy(alpha = 0.9f),
                    fontSize = 12.sp
                )
            }
            
            Button(
                onClick = {
                    android.util.Log.d("YouTubeVerificationUI", "Connect button clicked")
                    isConnecting = true
                    try {
                        onConnectYouTube()
                    } catch (e: Exception) {
                        android.util.Log.e("YouTubeVerificationUI", "Error calling onConnectYouTube: ${e.message}", e)
                        isConnecting = false
                        android.widget.Toast.makeText(
                            context,
                            "Error: ${e.message}",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF0000)
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                enabled = !isConnecting
            ) {
                if (isConnecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = BrandColors.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Connecting...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.AccountCircle, "Connect")
                    Spacer(Modifier.width(8.dp))
                    Text("Connect YouTube Account", fontWeight = FontWeight.Bold)
                }
            }
            
            if (isConnecting) {
                Text(
                    text = "Check your browser/app for the sign-in page",
                    style = MaterialTheme.typography.bodySmall,
                    color = BrandColors.Warning,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun FacebookVerificationUI(
    actionUrl: String,
    onConnectFacebook: () -> Unit
) {
    val context = LocalContext.current
    var isConnecting by remember { mutableStateOf(false) }
    
    // Reset connecting state after timeout
    LaunchedEffect(isConnecting) {
        if (isConnecting) {
            kotlinx.coroutines.delay(10000) // 10 seconds timeout
            isConnecting = false
            android.util.Log.w("FacebookVerificationUI", "Connection timeout - resetting state")
        }
    }
    
    Column(
        modifier = Modifier
            .background(BrandColors.CardBackground, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF4267B2).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Connect your Facebook account to verify automatically",
            style = MaterialTheme.typography.bodyMedium,
            color = BrandColors.White,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "We'll check if you've liked the page on Facebook.",
            style = MaterialTheme.typography.bodySmall,
            color = BrandColors.White.copy(alpha = 0.7f)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color(0xFF4267B2).copy(alpha = 0.1f),
                    RoundedCornerShape(8.dp)
                )
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFF4267B2),
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Make sure you've liked the page before connecting",
                style = MaterialTheme.typography.bodySmall,
                color = BrandColors.White.copy(alpha = 0.9f),
                fontSize = 12.sp
            )
        }
        
        Button(
            onClick = {
                android.util.Log.d("FacebookVerificationUI", "Connect button clicked")
                isConnecting = true
                try {
                    onConnectFacebook()
                } catch (e: Exception) {
                    android.util.Log.e("FacebookVerificationUI", "Error calling onConnectFacebook: ${e.message}", e)
                    isConnecting = false
                    android.widget.Toast.makeText(
                        context,
                        "Facebook connection failed: ${e.message}",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4267B2)
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            enabled = !isConnecting
        ) {
            if (isConnecting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = BrandColors.White
                )
                Spacer(Modifier.width(8.dp))
                Text("Connecting...", fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.AccountCircle, "Connect")
                Spacer(Modifier.width(8.dp))
                Text("Connect Facebook Account", fontWeight = FontWeight.Bold)
            }
        }
        
        if (isConnecting) {
            Text(
                text = "Check your browser/app for the Facebook login page",
                style = MaterialTheme.typography.bodySmall,
                color = BrandColors.Warning,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun ManualVerificationUI(
    platform: String,
    username: String,
    proofUrl: String,
    onUsernameChange: (String) -> Unit,
    onProofUrlChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .background(BrandColors.CardBackground, RoundedCornerShape(12.dp))
            .border(1.dp, BrandColors.Primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Provide proof of completion",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = BrandColors.White
        )
        
        Text(
            text = "Your submission will be reviewed within 24-48 hours.",
            style = MaterialTheme.typography.bodySmall,
            color = BrandColors.Warning
        )
        
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text("Your $platform Username", color = BrandColors.White.copy(alpha = 0.7f)) },
            placeholder = { Text("e.g., @yourhandle", color = BrandColors.White.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Person, "Username", tint = BrandColors.Primary)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandColors.Primary,
                unfocusedBorderColor = BrandColors.LightGray,
                focusedTextColor = BrandColors.White,
                unfocusedTextColor = BrandColors.White,
                cursorColor = BrandColors.Primary
            )
        )
        
        OutlinedTextField(
            value = proofUrl,
            onValueChange = onProofUrlChange,
            label = { Text("Screenshot URL or Post Link", color = BrandColors.White.copy(alpha = 0.7f)) },
            placeholder = { Text("https://...", color = BrandColors.White.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Link, "URL", tint = BrandColors.Primary)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandColors.Primary,
                unfocusedBorderColor = BrandColors.LightGray,
                focusedTextColor = BrandColors.White,
                unfocusedTextColor = BrandColors.White,
                cursorColor = BrandColors.Primary
            )
        )
    }
}

fun getTaskInstructions(platform: String, taskType: String): String {
    return when (platform.lowercase()) {
        "telegram" -> "1. Click the button below to open the Telegram channel/group\n2. Join the channel/group\n3. Come back here and click 'I've Completed This'"
        "youtube" -> when (taskType.lowercase()) {
            "subscribe", "channel_subscribe" -> "1. Click the button below to open YouTube\n2. Subscribe to the channel\n3. Come back here and click 'I've Completed This'"
            "like", "video_like" -> "1. Click the button below to open YouTube\n2. Watch and like the video\n3. Come back here and click 'I've Completed This'"
            else -> "1. Click the button below to open YouTube\n2. Complete the required action\n3. Come back here and click 'I've Completed This'"
        }
        "facebook" -> "1. Click the button below to open Facebook\n2. Like the page\n3. Come back here and click 'I've Completed This'"
        "twitter", "x" -> "1. Click the button below to open Twitter/X\n2. Complete the action (follow, like, or retweet)\n3. Come back here and click 'I've Completed This'"
        "instagram" -> "1. Click the button below to open Instagram\n2. Follow the account or like the post\n3. Come back here and click 'I've Completed This'"
        else -> "1. Click the button below to open ${platform.capitalize()}\n2. Complete the required task\n3. Come back here and click 'I've Completed This'"
    }
}

fun isReadyToSubmit(platform: String, telegramUserId: String, username: String, proofUrl: String): Boolean {
    return when (platform.lowercase()) {
        "telegram" -> telegramUserId.isNotEmpty() && telegramUserId.all { it.isDigit() } && telegramUserId.length >= 8
        "youtube", "facebook" -> true
        else -> username.isNotEmpty() || proofUrl.isNotEmpty()
    }
}

fun isValidTelegramUserId(userId: String): Boolean {
    return userId.isNotEmpty() && 
           userId.all { it.isDigit() } && 
           userId.length >= 8 && 
           userId.length <= 12 &&
           userId.toLongOrNull() != null
}

fun getTelegramUserIdErrorMessage(userId: String): String {
    return when {
        userId.isEmpty() -> ""
        !userId.all { it.isDigit() } -> "User ID should contain only numbers"
        userId.length < 8 -> "User ID should be at least 8 digits"
        userId.length > 12 -> "User ID should not exceed 12 digits"
        else -> ""
    }
}

fun buildProofData(platform: String, telegramUserId: String, username: String, proofUrl: String): Map<String, Any> {
    return buildMap {
        when (platform.lowercase()) {
            "telegram" -> {
                telegramUserId.toLongOrNull()?.let { userId ->
                    put("telegram_user_id", userId)
                    put("user_id", userId) // Alternative key some backends expect
                }
            }
            "youtube" -> {
                put("requires_youtube_oauth", true)
            }
            "facebook" -> {
                put("requires_facebook_oauth", true)
            }
            else -> {
                if (username.isNotEmpty()) put("username", username)
                if (proofUrl.isNotEmpty()) put("proof_url", proofUrl)
            }
        }
    }
}

fun getPlatformColor(platform: String): Color {
    return when (platform.lowercase()) {
        "telegram" -> Color(0xFF0088CC)
        "youtube" -> Color(0xFFFF0000)
        "facebook" -> Color(0xFF4267B2)
        "twitter", "x" -> Color(0xFF1DA1F2)
        "instagram" -> Color(0xFFE4405F)
        else -> BrandColors.Primary
    }
}

fun getPlatformIcon(platform: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (platform.lowercase()) {
        "telegram" -> Icons.Default.Send
        "youtube" -> Icons.Default.PlayArrow
        "facebook" -> Icons.Default.ThumbUp
        "twitter", "x" -> Icons.Default.Message
        "instagram" -> Icons.Default.PhotoCamera
        else -> Icons.Default.Public
    }
}

data class SocialTaskItem(
    val id: String,
    val title: String,
    val description: String,
    val platform: String,
    val taskType: String,
    val link: String,
    val reward: Double,
    val isCompleted: Boolean,
    val isVerified: Boolean,
    val verificationMethod: String
)