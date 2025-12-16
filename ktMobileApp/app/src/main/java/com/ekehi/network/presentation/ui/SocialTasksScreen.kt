package com.ekehi.network.presentation.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ekehi.network.auth.SocialAuthManager
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.presentation.viewmodel.SocialTasksViewModel
import com.ekehi.network.presentation.viewmodel.VerificationState
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn

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
    
    // OAuth Launchers
    val youtubeSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            task.addOnSuccessListener { account ->
                val accessToken = authManager.getYouTubeAccessToken(account)
                viewModel.setYouTubeAccessToken(accessToken ?: "")
            }
        }
    }
    
    val facebookLoginLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        authManager.handleFacebookResult(
            requestCode = result.resultCode,
            resultCode = Activity.RESULT_OK,
            data = result.data
        )
    }
    
    var selectedTask by remember { mutableStateOf<SocialTaskItem?>(null) }
    
    LaunchedEffect(Unit) {
        viewModel.loadSocialTasks()
        viewModel.loadUserSocialTasks(userId)
    }
    
    // Handle verification state changes
    LaunchedEffect(verificationState) {
        when (verificationState) {
            is VerificationState.Success -> {
                // Refresh tasks after success
                viewModel.loadUserSocialTasks(userId)
            }
            else -> {}
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a2e))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Header
            Text(
                text = "Social Tasks",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 20.dp, bottom = 24.dp)
            )
            
            // Stats Section
            StatsSection(viewModel)
            
            Spacer(modifier = Modifier.height(16.dp))
            
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
                                onClick = { selectedTask = taskItem },
                                verificationState = verificationState
                            )
                        }
                    }
                }
                is Resource.Loading -> {
                    CircularProgressIndicator()
                }
                is Resource.Error -> {
                    Text("Error loading tasks", color = Color.Red)
                }
                else -> {}
            }
        }
        
        // Task Completion Dialog
        selectedTask?.let { task ->
            TaskCompletionDialog(
                task = task,
                viewModel = viewModel,
                authManager = authManager,
                youtubeSignInLauncher = youtubeSignInLauncher,
                onDismiss = { selectedTask = null },
                onSubmit = { proofData ->
                    viewModel.completeSocialTask(userId, task.id, proofData)
                    selectedTask = null
                }
            )
        }
        
        // Verification State Snackbar
        when (verificationState) {
            is VerificationState.Success -> {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text((verificationState as VerificationState.Success).message)
                }
            }
            is VerificationState.Error -> {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Text((verificationState as VerificationState.Error).message)
                }
            }
            is VerificationState.Pending -> {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.tertiary
                ) {
                    Text((verificationState as VerificationState.Pending).message)
                }
            }
            else -> {}
        }
    }
}

@Composable
fun StatsSection(viewModel: SocialTasksViewModel) {
    val socialTasksResource by viewModel.socialTasks.collectAsState()
    
    // Calculate stats from tasks
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
            .background(
                color = Color(0x0DFFFFFF), // 5% opacity white
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0x4DFFA000), // 30% opacity orange
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Completed Tasks Card
        Box(modifier = Modifier.weight(1f)) {
            StatCard(
                value = "$completedTasks/$totalTasks",
                label = "Completed"
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // EKH Earned Card
        Box(modifier = Modifier.weight(1f)) {
            StatCard(
                value = totalRewards.toString(),
                label = "EKH Earned"
            )
        }
    }
}

@Composable
fun StatCard(
    value: String,
    label: String
) {
    Column(
        modifier = Modifier
            .background(
                color = Color(0x1AFFFFFF), // 10% opacity white
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0x4DFFA000), // 30% opacity orange
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            )
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = Color(0x99FFFFFF), // 60% opacity white
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun TaskCompletionDialog(
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
        title = { 
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = getPlatformIcon(task.platform),
                    contentDescription = null,
                    tint = getPlatformColor(task.platform)
                )
                Text(getDialogTitle(task.platform, task.verificationMethod))
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when {
                    // TELEGRAM
                    task.platform.lowercase() == "telegram" -> {
                        TelegramVerificationUI(
                            telegramUserId = telegramUserId,
                            onTelegramUserIdChange = { telegramUserId = it }
                        )
                    }
                    
                    // YOUTUBE
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
                    
                    // FACEBOOK
                    task.platform.lowercase() == "facebook" -> {
                        FacebookVerificationUI(
                            actionUrl = task.link,
                            onConnectFacebook = {
                                authManager.loginWithFacebook(
                                    loginManager = LoginManager.getInstance(),
                                    onSuccess = { accessToken ->
                                        facebookAccessToken = accessToken
                                    },
                                    onError = { error ->
                                        // Handle error
                                    }
                                )
                                LoginManager.getInstance().logInWithReadPermissions(
                                    context as Activity,
                                    listOf("user_likes")
                                )
                            }
                        )
                    }
                    
                    // MANUAL (Twitter, etc.)
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
                
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
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
                    
                    // Add Facebook access token if this is a Facebook task
                    val enhancedProofData = if (task.platform.lowercase() == "facebook" && facebookAccessToken != null) {
                        proofData.toMutableMap().apply {
                            put("facebook_access_token", facebookAccessToken!!)
                        }
                    } else {
                        proofData
                    }
                    
                    // Validate Telegram user ID before submitting
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
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text(getSubmitButtonText(task.platform))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
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
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "To verify your Telegram membership:",
            style = MaterialTheme.typography.bodyMedium,
        )
        
        Text(
            text = "1. Open Telegram\n2. Search for @ekehi_task_bot\n3. Send /start to get your ID\n4. Copy your numeric ID from the bot's response\n5. Paste it below",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        OutlinedTextField(
            value = telegramUserId,
            onValueChange = { newValue ->
                // Only allow numeric input for Telegram user ID
                if (newValue.all { it.isDigit() } || newValue.isEmpty()) {
                    onTelegramUserIdChange(newValue)
                }
            },
            label = { Text("Telegram User ID (Numbers only)") },
            placeholder = { Text("e.g., 123456789") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Send, "Telegram")
            },
            isError = hasError,
            supportingText = {
                if (hasError) {
                    Text(errorMessage)
                } else {
                    Text("Enter your unique Telegram ID")
                }
            }
        )
        
        Button(
            onClick = {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://t.me/ekehi_task_bot"))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Fallback to opening Telegram app
                    try {
                        val intent = context.packageManager.getLaunchIntentForPackage("org.telegram.messenger")
                        context.startActivity(intent)
                    } catch (ex: Exception) {
                        // If Telegram app is not installed, open in browser
                        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://web.telegram.org/#/im?p=@ekehi_task_bot"))
                        context.startActivity(intent)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0088CC)
            )
        ) {
            Icon(Icons.Default.OpenInNew, "Open")
            Spacer(Modifier.width(8.dp))
            Text("Open @ekehi_task_bot")
        }
        
        // Add explanation about where to find the ID
        Text(
            text = "Note: After sending /start to the bot, it will reply with your unique Telegram ID. Copy only the numbers.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun YouTubeVerificationUI(
    taskType: String,
    actionUrl: String,
    onConnectYouTube: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = when (taskType.lowercase()) {
                "subscribe", "channel_subscribe" -> "Subscribe to our YouTube channel"
                "like", "video_like" -> "Like our YouTube video"
                else -> "Complete the YouTube task"
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "1. Complete the task on YouTube\n2. Connect your YouTube account\n3. We'll verify automatically",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        if (actionUrl.isNotEmpty()) {
            Button(
                onClick = { /* TODO: Open YouTube URL */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF0000)
                )
            ) {
                Icon(Icons.Default.PlayArrow, "YouTube")
                Spacer(Modifier.width(8.dp))
                Text("Open on YouTube")
            }
        }
        
        Button(
            onClick = onConnectYouTube,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.AccountCircle, "Connect")
            Spacer(Modifier.width(8.dp))
            Text("Connect YouTube Account")
        }
    }
}

@Composable
fun FacebookVerificationUI(
    actionUrl: String,
    onConnectFacebook: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Like our Facebook page",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "1. Like the page on Facebook\n2. Connect your Facebook account\n3. We'll verify automatically",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        if (actionUrl.isNotEmpty()) {
            Button(
                onClick = { /* TODO: Open Facebook URL */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4267B2)
                )
            ) {
                Icon(Icons.Default.ThumbUp, "Facebook")
                Spacer(Modifier.width(8.dp))
                Text("Open on Facebook")
            }
        }
        
        Button(
            onClick = onConnectFacebook,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.AccountCircle, "Connect")
            Spacer(Modifier.width(8.dp))
            Text("Connect Facebook Account")
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
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Submit proof of task completion",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Your submission will be reviewed within 24-48 hours.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text("Your $platform Username") },
            placeholder = { Text("e.g., @yourhandle") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Person, "Username")
            }
        )
        
        OutlinedTextField(
            value = proofUrl,
            onValueChange = onProofUrlChange,
            label = { Text("Screenshot URL or Post Link") },
            placeholder = { Text("https://...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Link, "URL")
            }
        )
    }
}

// Helper Functions
fun getDialogTitle(platform: String, verificationMethod: String): String {
    return when (platform.lowercase()) {
        "telegram" -> "Connect Telegram"
        "youtube" -> "Connect YouTube"
        "facebook" -> "Connect Facebook"
        "twitter", "x" -> "Submit Proof"
        else -> "Complete Task"
    }
}

fun getSubmitButtonText(platform: String): String {
    return when (platform.lowercase()) {
        "telegram" -> "Verify Membership"
        "youtube", "facebook" -> "Verify Action"
        else -> "Submit for Review"
    }
}

fun isReadyToSubmit(
    platform: String,
    telegramUserId: String,
    username: String,
    proofUrl: String
): Boolean {
    return when (platform.lowercase()) {
        "telegram" -> telegramUserId.isNotEmpty() && telegramUserId.all { it.isDigit() } && telegramUserId.length >= 8
        "youtube", "facebook" -> true // OAuth handles validation
        else -> username.isNotEmpty() || proofUrl.isNotEmpty()
    }
}

/**
 * Validates Telegram user ID format
 */
fun isValidTelegramUserId(userId: String): Boolean {
    return userId.isNotEmpty() && 
           userId.all { it.isDigit() } && 
           userId.length >= 8 && 
           userId.length <= 12 &&
           userId.toLongOrNull() != null
}

/**
 * Provides helpful error messages for Telegram user ID validation
 */
fun getTelegramUserIdErrorMessage(userId: String): String {
    return when {
        userId.isEmpty() -> "Please enter your Telegram User ID"
        !userId.all { it.isDigit() } -> "User ID should contain only numbers"
        userId.length < 8 -> "User ID should be at least 8 digits"
        userId.length > 12 -> "User ID should not exceed 12 digits"
        else -> ""
    }
}

fun buildProofData(
    platform: String,
    telegramUserId: String,
    username: String,
    proofUrl: String
): Map<String, Any> {
    return buildMap {
        when (platform.lowercase()) {
            "telegram" -> {
                telegramUserId.toLongOrNull()?.let {
                    put("telegram_user_id", it)
                }
            }
            "youtube" -> {
                // Access token will be added by ViewModel
                put("requires_youtube_oauth", true)
            }
            "facebook" -> {
                // Access token will be added by ViewModel
                put("requires_facebook_oauth", true)
            }
            else -> {
                if (username.isNotEmpty()) put("username", username)
                if (proofUrl.isNotEmpty()) put("proof_url", proofUrl)
            }
        }
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
            .border(1.dp, Color(0x4DFFA000), androidx.compose.foundation.shape.RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0x1AFFFFFF)),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Task content (same as before)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            getPlatformColor(task.platform),
                            androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getPlatformIcon(task.platform),
                        contentDescription = task.platform,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = task.description,
                        color = Color(0xB3FFFFFF),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    // Show verification method badge
                    Text(
                        text = if (task.verificationMethod == "api") "✓ Auto-verified" else "⏳ Manual review",
                        color = if (task.verificationMethod == "api") Color(0xFF10b981) else Color(0xFFf59e0b),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "+${task.reward} EKH",
                    color = Color(0xFFffa000),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                if (task.isVerified) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, "Verified", tint = Color(0xFF10b981), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Completed", color = Color(0xFF10b981), fontSize = 14.sp)
                    }
                } else {
                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFffa000))
                    ) {
                        Text("Complete")
                    }
                }
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
        else -> Color(0xFFffa000)
    }
}

fun getPlatformIcon(platform: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (platform.lowercase()) {
        "telegram" -> Icons.Default.Send
        "youtube" -> Icons.Default.PlayArrow
        "facebook" -> Icons.Default.ThumbUp
        "twitter", "x" -> Icons.Default.Message
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