// COMPLETE SocialTasksScreen.kt - REPLACE ENTIRE FILE
// This is the complete file with the fixed blog task card layout

package com.ekehi.network.presentation.ui

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import coil.compose.AsyncImage
import com.ekehi.network.auth.SocialAuthManager
import com.ekehi.network.data.model.SocialTask
import com.ekehi.network.data.repository.AuthRepository
import com.ekehi.network.presentation.viewmodel.SocialTasksViewModel
import com.ekehi.network.presentation.ui.EkhLogo
import com.ekehi.network.presentation.viewmodel.VerificationState
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.presentation.ui.components.SocialTasksScreenSkeleton
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.ekehi.network.util.EventBus
import com.ekehi.network.util.Event
import kotlinx.coroutines.delay
import java.net.URL

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
    val Gray = Color(0xFF6b7280)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialTasksScreen(
    viewModel: SocialTasksViewModel = hiltViewModel(),
    authManager: SocialAuthManager
) {
    val context = LocalContext.current
    var userId by remember { mutableStateOf("") }
    val socialTasksResource by viewModel.socialTasks.collectAsState()
    val verificationState by viewModel.verificationState.collectAsState()
    
    var selectedTask by remember { mutableStateOf<SocialTaskItem?>(null) }
    var showVerificationDialog by remember { mutableStateOf(false) }
    var taskActionCompleted by remember { mutableStateOf(false) }
    
    val authRepository = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            AuthRepositoryEntryPoint::class.java
        ).authRepository()
    }
    
    // Preserve scroll position and content state
    val scrollState = rememberScrollState()
    
    // Track if screen has been loaded - persists across navigation
    var hasLoaded by rememberSaveable { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    
    // Load data only once on initial interaction
    LaunchedEffect(Unit) {
        if (!hasLoaded) {
            Log.i("EKEHI_DEBUG", "SocialTasksScreen initial load...")
            try {
                val result = authRepository.getCurrentUserIfLoggedIn()
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    if (user != null) {
                        userId = user.id
                        Log.i("EKEHI_DEBUG", "Found userId: $userId, loading tasks...")
                        // Check if we have cached data first
                        val cachedData = viewModel.getCachedTasks()
                        if (cachedData is Resource.Success) {
                            viewModel.restoreFromCache()
                        }
                        viewModel.loadUserSocialTasks(userId)
                    } else {
                        Log.w("EKEHI_DEBUG", "User is null despite successful auth check")
                        val cachedData = viewModel.getCachedTasks()
                        if (cachedData is Resource.Success) {
                            viewModel.restoreFromCache()
                        }
                        viewModel.loadSocialTasks()
                    }
                } else {
                    Log.w("EKEHI_DEBUG", "Auth check failed, loading tasks without user status")
                    val cachedData = viewModel.getCachedTasks()
                    if (cachedData is Resource.Success) {
                        viewModel.restoreFromCache()
                    }
                    viewModel.loadSocialTasks()
                }
            } catch (e: Exception) {
                Log.e("EKEHI_DEBUG", "Failed to get current user ID", e)
                val cachedData = viewModel.getCachedTasks()
                if (cachedData is Resource.Success) {
                    viewModel.restoreFromCache()
                }
                viewModel.loadSocialTasks()
            }
            hasLoaded = true
        } else {
            Log.i("EKEHI_DEBUG", "Screen already loaded, using cached data")
        }
    }

    LaunchedEffect(verificationState) {
        if (verificationState is VerificationState.Success || verificationState is VerificationState.Error) {
            Log.i("EKEHI_DEBUG", "Verification state changed: $verificationState")
            // Don't refresh the entire list, just clear verification state
            // The task status will be updated locally
            viewModel.clearVerificationState()
        }
    }

    LaunchedEffect(socialTasksResource) {
        val tasks = (socialTasksResource as? Resource.Success)?.data ?: emptyList()
        val hasAnyCooldown = tasks.any { it.nextAvailableAt != null }
        
        if (hasAnyCooldown) {
            while(true) {
                delay(60000)
                Log.i("EKEHI_DEBUG", "Checking for task cooldown updates...")
                // Only refresh if we need to update cooldown status
                if (userId.isNotEmpty()) {
                    // Check if any task status might have changed due to cooldowns expiring
                    viewModel.loadUserSocialTasks(userId)
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
                
                IconButton(
                    onClick = {
                        // Only refresh tasks via ViewModel - no need to reload entire screen
                        Log.i("EKEHI_DEBUG", "Refreshing social tasks only...")
                        viewModel.loadSocialTasks()
                        if (userId.isNotEmpty()) {
                            viewModel.loadUserSocialTasks(userId)
                        }
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
            
            EnhancedStatsSection(viewModel)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            when (socialTasksResource) {
                is Resource.Success -> {
                    val tasks = (socialTasksResource as Resource.Success).data
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(tasks) { task ->
                            // Check for local state updates for this specific task
                            val localTaskState = viewModel.getLocalTaskState(task.id)
                            val currentTask = localTaskState ?: task
                            
                            val taskItem = SocialTaskItem(
                                id = currentTask.id,
                                title = currentTask.title,
                                description = currentTask.description,
                                platform = currentTask.platform,
                                taskType = currentTask.taskType,
                                link = currentTask.actionUrl ?: "",
                                reward = currentTask.rewardCoins,
                                isCompleted = currentTask.isCompleted,
                                isVerified = currentTask.isVerified,
                                verificationMethod = currentTask.verificationMethod,
                                status = currentTask.status ?: "available",
                                maxCompletionsPerDay = currentTask.maxCompletionsPerDay,
                                cooldownMinutes = currentTask.cooldownMinutes,
                                completionCountToday = currentTask.completionCountToday,
                                nextAvailableAt = currentTask.nextAvailableAt,
                                totalAccumulatedRewards = currentTask.totalAccumulatedRewards,
                                totalCompletions = currentTask.totalCompletions
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
                    SocialTasksScreenSkeleton()
                     LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
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
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(
                                                BrandColors.LightGray,
                                                RoundedCornerShape(8.dp)
                                            )
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(60.dp)
                                            .height(26.dp)
                                            .background(
                                                BrandColors.LightGray,
                                                RoundedCornerShape(8.dp)
                                            )
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(50.dp)
                                            .height(13.dp)
                                            .background(
                                                BrandColors.LightGray,
                                                RoundedCornerShape(4.dp)
                                            )
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(
                                                BrandColors.LightGray,
                                                RoundedCornerShape(8.dp)
                                            )
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(60.dp)
                                            .height(26.dp)
                                            .background(
                                                BrandColors.LightGray,
                                                RoundedCornerShape(8.dp)
                                            )
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(50.dp)
                                            .height(13.dp)
                                            .background(
                                                BrandColors.LightGray,
                                                RoundedCornerShape(4.dp)
                                            )
                                    )
                                }
                            }
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                        
                        items(5) { index ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(6.dp, RoundedCornerShape(20.dp)),
                                colors = CardDefaults.cardColors(
                                    containerColor = BrandColors.CardBackground
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Box {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(
                                                        BrandColors.Primary,
                                                        BrandColors.PrimaryLight
                                                    )
                                                )
                                            )
                                    )
                                    
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .shadow(4.dp, RoundedCornerShape(16.dp))
                                                    .background(
                                                        BrandColors.LightGray,
                                                        RoundedCornerShape(16.dp)
                                                    )
                                            )
                                            
                                            Spacer(Modifier.width(16.dp))
                                            
                                            Column(modifier = Modifier.weight(1f)) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(120.dp)
                                                        .height(20.dp)
                                                        .background(
                                                            BrandColors.LightGray,
                                                            RoundedCornerShape(4.dp)
                                                        )
                                                )
                                                
                                                Spacer(Modifier.height(8.dp))
                                                
                                                repeat(2) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(14.dp)
                                                            .background(
                                                                BrandColors.LightGray,
                                                                RoundedCornerShape(4.dp)
                                                            )
                                                            .padding(top = 2.dp)
                                                    )
                                                }
                                                
                                                Spacer(Modifier.height(8.dp))
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .width(80.dp)
                                                        .height(12.dp)
                                                        .background(
                                                            BrandColors.LightGray,
                                                            RoundedCornerShape(4.dp)
                                                        )
                                                )
                                                
                                                Spacer(Modifier.height(12.dp))
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .width(90.dp)
                                                        .height(24.dp)
                                                        .background(
                                                            BrandColors.LightGray,
                                                            RoundedCornerShape(8.dp)
                                                        )
                                                )
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
                                            Box(
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
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(20.dp)
                                                            .background(
                                                                BrandColors.LightGray,
                                                                RoundedCornerShape(4.dp)
                                                            )
                                                    )
                                                    Spacer(Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .width(40.dp)
                                                            .height(18.dp)
                                                            .background(
                                                                BrandColors.LightGray,
                                                                RoundedCornerShape(4.dp)
                                                            )
                                                    )
                                                }
                                            }
                                            
                                            Box(
                                                modifier = Modifier
                                                    .width(80.dp)
                                                    .height(40.dp)
                                                    .background(
                                                        BrandColors.LightGray,
                                                        RoundedCornerShape(12.dp)
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                        }
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
        
        if (selectedTask != null && !showVerificationDialog) {
            if (selectedTask!!.status == "pending") {
                PendingTaskDialog(
                    task = selectedTask!!,
                    onDeleteTask = { 
                        viewModel.deletePendingTask(userId, selectedTask!!.id)
                        selectedTask = null
                    },
                    onDismiss = { 
                        selectedTask = null
                    }
                )
            } else {
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
        }
        
        if (selectedTask != null && showVerificationDialog) {
            TaskVerificationDialog(
                task = selectedTask!!,
                viewModel = viewModel,
                authManager = authManager,
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
        
        when (verificationState) {
            is VerificationState.Success -> {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = BrandColors.Success,
                    contentColor = BrandColors.White,
                    action = {
                        TextButton(
                            onClick = { viewModel.clearVerificationState() },
                            colors = ButtonDefaults.textButtonColors(contentColor = BrandColors.White)
                        ) {
                            Text("Close")
                        }
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = BrandColors.White)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = (verificationState as VerificationState.Success).message,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            is VerificationState.Error -> {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = BrandColors.Error,
                    contentColor = BrandColors.White,
                    action = {
                        TextButton(
                            onClick = { viewModel.clearVerificationState() },
                            colors = ButtonDefaults.textButtonColors(contentColor = BrandColors.White)
                        ) {
                            Text("Close")
                        }
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Error, null, tint = BrandColors.White)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = (verificationState as VerificationState.Error).message,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            is VerificationState.Pending -> {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = BrandColors.Warning,
                    contentColor = BrandColors.Black,
                    action = {
                        TextButton(
                            onClick = { viewModel.clearVerificationState() },
                            colors = ButtonDefaults.textButtonColors(contentColor = BrandColors.Black)
                        ) {
                            Text("Close")
                        }
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Schedule, null, tint = BrandColors.Black)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = (verificationState as VerificationState.Pending).message,
                            modifier = Modifier.weight(1f)
                        )
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
    val localTaskStates by viewModel.localTaskStates.collectAsState()
    
    var completedTasks = 0
    var totalTasks = 0
    var totalRewards = 0.0
    var blogRewards = 0.0
    
    // Use combined tasks (original + local overrides) for accurate stats
    val tasks = if (localTaskStates.isNotEmpty()) {
        val baseTasks = when (socialTasksResource) {
            is Resource.Success -> (socialTasksResource as Resource.Success).data
            else -> emptyList()
        }
        baseTasks.map { task -> localTaskStates[task.id] ?: task }
    } else {
        when (socialTasksResource) {
            is Resource.Success -> (socialTasksResource as Resource.Success).data
            else -> emptyList()
        }
    }
    
    if (tasks.isNotEmpty()) {
        totalTasks = tasks.size
        completedTasks = tasks.count { it.isCompleted }
        totalRewards = tasks.filter { it.isCompleted }.sumOf { it.rewardCoins }
        
        blogRewards = tasks
            .filter { it.platform.lowercase() == "blog" }
            .sumOf { it.totalAccumulatedRewards }
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
            label = "Earned",
            modifier = Modifier.weight(1f),
            isHighlight = true,
            showLogoInLabel = true
        )
        
        if (blogRewards > 0) {
            Spacer(modifier = Modifier.width(16.dp))
            
            EnhancedStatCard(
                icon = Icons.Default.Repeat,
                value = String.format("%.1f", blogRewards),
                label = "Blog Total",
                modifier = Modifier.weight(1f),
                showLogoInLabel = true
            )
        }
    }
}

@Composable
fun EnhancedStatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    isHighlight: Boolean = false,
    showLogoInLabel: Boolean = false
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            if (showLogoInLabel) {
                EkhLogo(size = 14.dp)
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                color = BrandColors.White.copy(alpha = 0.7f),
                fontSize = 13.sp
            )
        }
    }
}

// FIXED VERSION OF EnhancedSocialTaskCard
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
                if (task.platform.lowercase() == "blog" && task.nextAvailableAt != null) {
                    Modifier
                } else if (task.platform.lowercase() == "blog" && task.completionCountToday >= task.maxCompletionsPerDay) {
                    Modifier
                } else if (!task.isVerified && task.status != "pending") {
                    Modifier.clickable(onClick = onClick)
                } else if (task.status == "pending") {
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
            
            // FIXED: Simplified badge showing only today's count
            if (task.platform.lowercase() == "blog") {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp, end = 16.dp),
                    color = BrandColors.Primary.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = "${task.completionCountToday}/${task.maxCompletionsPerDay}",
                        color = BrandColors.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
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
                        if (task.platform.lowercase() == "blog") {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = "Blog",
                                tint = BrandColors.White,
                                modifier = Modifier.size(28.dp)
                            )
                        } else if (task.link.isNotEmpty()) {
                            AsyncImage(
                                model = getFaviconUrl(task.link),
                                contentDescription = task.platform,
                                modifier = Modifier.size(32.dp),
                                placeholder = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_help),
                                error = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_help)
                            )
                        } else {
                            Icon(
                                imageVector = getPlatformIcon(task.platform),
                                contentDescription = task.platform,
                                tint = BrandColors.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
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
                        
                        if (task.link.isNotEmpty()) {
                            Text(
                                text = extractDomain(task.link),
                                color = BrandColors.White.copy(alpha = 0.5f),
                                fontSize = 12.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        
                        Spacer(Modifier.height(8.dp))
                        
                        // FIXED: Badge with total rewards integrated
                        Row(
                            modifier = Modifier
                                .background(
                                    color = if (task.platform.lowercase() == "blog")
                                        BrandColors.Primary.copy(alpha = 0.15f)
                                    else if (task.verificationMethod == "api") 
                                        BrandColors.Success.copy(alpha = 0.15f)
                                    else 
                                        BrandColors.Warning.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    if (task.platform.lowercase() == "blog")
                                        BrandColors.Primary.copy(alpha = 0.3f)
                                    else if (task.verificationMethod == "api") 
                                        BrandColors.Success.copy(alpha = 0.3f)
                                    else 
                                        BrandColors.Warning.copy(alpha = 0.3f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (task.platform.lowercase() == "blog")
                                    Icons.Default.Repeat
                                else if (task.verificationMethod == "api") 
                                    Icons.Default.Verified 
                                else 
                                    Icons.Default.Schedule,
                                contentDescription = null,
                                tint = if (task.platform.lowercase() == "blog")
                                    BrandColors.Primary
                                else if (task.verificationMethod == "api") 
                                    BrandColors.Success 
                                else 
                                    BrandColors.Warning,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            
                            // FIXED: Show daily limit AND total for blog
                            if (task.platform.lowercase() == "blog") {
                                Column {
                                    Text(
                                        text = "Daily Limit: ${task.maxCompletionsPerDay}",
                                        color = BrandColors.Primary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (task.totalCompletions > 0) {
                                        Text(
                                            text = "Earned ${task.totalCompletions}× (${String.format("%.1f", task.totalAccumulatedRewards)} EKH total)",
                                            color = BrandColors.Primary.copy(alpha = 0.8f),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Normal,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = if (task.verificationMethod == "api") 
                                        "Auto-verification" 
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
                        EkhLogo(size = 14.dp)
                    }
                    
                    if (task.platform.lowercase() == "blog" && task.nextAvailableAt != null) {
                        Button(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                disabledContainerColor = BrandColors.Gray.copy(alpha = 0.2f),
                                disabledContentColor = BrandColors.Gray
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp
                            )
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            val remainingTime = remember(task.nextAvailableAt) {
                                calculateRemainingCooldown(task.nextAvailableAt)
                            }
                            Text(
                                text = if (remainingTime.isNotEmpty()) remainingTime else "Cooldown",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    } else if (task.isVerified) {
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
                    } else if (task.status == "pending") {
                        Row(
                            modifier = Modifier
                                .background(
                                    BrandColors.Warning.copy(alpha = 0.15f),
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    1.5.dp,
                                    BrandColors.Warning.copy(alpha = 0.3f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                "Reviewing",
                                tint = BrandColors.Warning,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Reviewing",
                                color = BrandColors.Warning,
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



// PART 2 OF SocialTasksScreen.kt - APPEND THIS TO PART 1

@Composable
fun TaskActionDialog(
    task: SocialTaskItem,
    onDismiss: () -> Unit,
    onTaskCompleted: () -> Unit
) {
    val context = LocalContext.current
    var cooldownRemaining by remember { mutableStateOf(0L) }
    var hasOpenedBlog by remember { mutableStateOf(false) }
    val isBlogTask = task.platform.lowercase() == "blog"

    LaunchedEffect(cooldownRemaining) {
        if (cooldownRemaining > 0) {
            kotlinx.coroutines.delay(1000)
            cooldownRemaining = (cooldownRemaining - 1).coerceAtLeast(0)
        }
    }
    
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

                                if (isBlogTask) {
                                    hasOpenedBlog = true
                                    cooldownRemaining = 20L
                                }
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
            if (task.status == "pending") {
                Button(
                    onClick = {},
                    enabled = false,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandColors.Gray
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Schedule, "Pending")
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Under Review",
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (isBlogTask && !hasOpenedBlog) {
                Button(
                    onClick = {},
                    enabled = false,
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = BrandColors.Gray.copy(alpha = 0.3f),
                        disabledContentColor = BrandColors.Gray
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Lock, "Locked")
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Visit Blog First",
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (isBlogTask && cooldownRemaining > 0) {
                Button(
                    onClick = {},
                    enabled = false,
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = BrandColors.Primary.copy(alpha = 0.3f),
                        disabledContentColor = BrandColors.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Timer, "Cooldown")
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Wait ${cooldownRemaining}s",
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = onTaskCompleted,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandColors.Primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Task Completed",
                        fontWeight = FontWeight.Bold
                    )
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
    onDismiss: () -> Unit,
    onSubmit: (Map<String, Any>) -> Unit
) {
    val context = LocalContext.current
    var telegramUserId by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var proofUrl by remember { mutableStateOf("") }
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp),
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
                        task.platform.lowercase() == "blog" -> {
                            Text(
                                text = "Click submit below to claim your reward. You can do this up to ${task.maxCompletionsPerDay} times every 24 hours.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = BrandColors.White.copy(alpha = 0.8f)
                            )
                        }
                        
                        task.platform.lowercase() == "telegram" -> {
                            TelegramVerificationUI(
                                telegramUserId = telegramUserId,
                                onTelegramUserIdChange = { telegramUserId = it }
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
            if (isLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = BrandColors.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Submitting for Review...",
                        color = BrandColors.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
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
                        
                        if (task.platform.lowercase() == "telegram" && !isValidTelegramUserId(telegramUserId)) {
                            errorMessage = getTelegramUserIdErrorMessage(telegramUserId)
                            if (errorMessage.isEmpty()) {
                                errorMessage = "Please enter a valid Telegram User ID"
                            }
                            isLoading = false
                        } else if (proofData.isEmpty()) {
                            errorMessage = "Please provide required information"
                            isLoading = false
                        } else {
                            onSubmit(proofData)
                        }
                    },
                    enabled = !isLoading && isReadyToSubmit(
                        platform = task.platform,
                        telegramUserId = telegramUserId,
                        username = username,
                        proofUrl = proofUrl,
                        taskType = task.taskType
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandColors.Primary,
                        disabledContainerColor = BrandColors.LightGray
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
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
        
        Button(
            onClick = {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/ekehi_task_bot"))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    try {
                        val intent = context.packageManager.getLaunchIntentForPackage("org.telegram.messenger")
                        if (intent != null) {
                            context.startActivity(intent)
                        } else {
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

@Composable
fun PendingTaskDialog(
    task: SocialTaskItem,
    onDeleteTask: () -> Unit,
    onDismiss: () -> Unit
) {
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
                            BrandColors.Warning.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = BrandColors.Warning,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    "Task Under Review",
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
                    text = "Your submission for \"${task.title}\" is currently under review by our team. This usually takes 24-48 hours.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrandColors.White.copy(alpha = 0.8f),
                    lineHeight = 22.sp
                )
                
                Divider(color = BrandColors.LightGray.copy(alpha = 0.3f))
                
                Text(
                    text = "What would you like to do?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrandColors.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandColors.Primary
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text("Wait for Review", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDeleteTask()
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = BrandColors.Error
                )
            ) {
                Text("Delete Submission")
            }
        }
    )
}

// Helper functions
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

fun isReadyToSubmit(platform: String, telegramUserId: String, username: String, proofUrl: String, taskType: String = ""): Boolean {
    if (platform.lowercase() == "blog") return true
    return when (platform.lowercase()) {
        "telegram" -> telegramUserId.isNotEmpty() && telegramUserId.all { it.isDigit() } && telegramUserId.length >= 8
        else -> username.isNotEmpty() || proofUrl.isNotEmpty()
    }
}

fun calculateRemainingCooldown(nextAvailableAt: String?): String {
    if (nextAvailableAt == null) return ""
    return try {
        val nextTime = parseIsoDate(nextAvailableAt)
        val now = System.currentTimeMillis()
        val diffMs = nextTime - now
        if (diffMs <= 0) return ""
        
        val minutes = (diffMs / (60 * 1000)) % 60
        val hours = (diffMs / (60 * 60 * 1000))
        
        if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    } catch (e: Exception) {
        ""
    }
}

fun parseIsoDate(dateStr: String?): Long {
    if (dateStr.isNullOrEmpty()) return 0L
    val normalizedDate = dateStr.replace("+00:00", "Z")
    return try {
        java.time.Instant.parse(normalizedDate).toEpochMilli()
    } catch (e: Exception) {
        try {
            val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }
            format.parse(normalizedDate)?.time ?: 0L
        } catch (e2: Exception) {
            0L
        }
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
        put("platform", platform)
        put("submitted_at", System.currentTimeMillis())
        
        when (platform.lowercase()) {
            "telegram" -> {
                telegramUserId.toLongOrNull()?.let { userId ->
                    put("telegram_user_id", userId)
                    put("user_id", userId)
                }
            }
            else -> {
                if (username.isNotEmpty()) put("username", username)
                if (proofUrl.isNotEmpty()) put("proof_url", proofUrl)
            }
        }
        
        if (username.isNotEmpty()) put("submitted_username", username)
        if (proofUrl.isNotEmpty()) put("submitted_proof_url", proofUrl)
        if (telegramUserId.isNotEmpty()) put("submitted_telegram_id", telegramUserId)
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

fun extractDomain(url: String): String {
    return try {
        val domain = URL(url).host
        if (domain.startsWith("www.")) domain.substring(4) else domain
    } catch (e: Exception) {
        "unknown"
    }
}

fun getFaviconUrl(url: String): String {
    return try {
        val domain = URL(url).host
        "https://www.google.com/s2/favicons?domain=$domain&sz=64"
    } catch (e: Exception) {
        "https://www.google.com/s2/favicons?domain=example.com&sz=64"
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
    val verificationMethod: String,
    val status: String = "available",
    val maxCompletionsPerDay: Int = 1,
    val cooldownMinutes: Int = 0,
    val completionCountToday: Int = 0,
    val nextAvailableAt: String? = null,
    val totalAccumulatedRewards: Double = 0.0,
    val totalCompletions: Int = 0
)

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AuthRepositoryEntryPoint {
    fun authRepository(): AuthRepository
}