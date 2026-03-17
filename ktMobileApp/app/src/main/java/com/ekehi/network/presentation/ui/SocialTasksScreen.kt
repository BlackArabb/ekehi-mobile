// COMPLETE SocialTasksScreen.kt - REPLACE ENTIRE FILE
// Fixed: blog task cooldown countdown, correct button state order, card clickability

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
import kotlinx.coroutines.delay
import java.net.URL

// ─────────────────────────────────────────────
// Brand Colors
// ─────────────────────────────────────────────
object BrandColors {
    val Primary        = Color(0xFFffa000)
    val PrimaryDark    = Color(0xFFcc8000)
    val PrimaryLight   = Color(0xFFffb333)
    val Black          = Color(0xFF000000)
    val DarkGray       = Color(0xFF1a1a1a)
    val MediumGray     = Color(0xFF2d2d2d)
    val LightGray      = Color(0xFF404040)
    val White          = Color(0xFFFFFFFF)
    val Success        = Color(0xFF10b981)
    val Error          = Color(0xFFef4444)
    val Warning        = Color(0xFFf59e0b)
    val CardBackground = Color(0xFF0d0d0d)
    val CardBorder     = Color(0x4DFFA000)
    val Gray           = Color(0xFF6b7280)
}

// ─────────────────────────────────────────────
// Constants
// ─────────────────────────────────────────────
private const val TELEGRAM_USER_ID_MIN_LENGTH = 8
private const val TELEGRAM_USER_ID_MAX_LENGTH = 12

// ─────────────────────────────────────────────
// SocialTasksScreen
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialTasksScreen(
    viewModel: SocialTasksViewModel = hiltViewModel(),
    authManager: SocialAuthManager
) {
    val context = LocalContext.current
    var userId by remember { mutableStateOf("") }
    val socialTasksResource by viewModel.socialTasks.collectAsState()
    val verificationState  by viewModel.verificationState.collectAsState()

    var selectedTask           by remember { mutableStateOf<SocialTaskItem?>(null) }
    var showVerificationDialog by remember { mutableStateOf(false) }
    var taskActionCompleted    by remember { mutableStateOf(false) }

    val authRepository = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            AuthRepositoryEntryPoint::class.java
        ).authRepository()
    }

    var hasLoaded by rememberSaveable { mutableStateOf(false) }

    // ── Initial data load (once per session) ─────────────────────────────
    // Shows cached data immediately so the screen is never blank, then
    // fetches fresh data in the background. Runs only once — never on
    // recomposition.
    LaunchedEffect(Unit) {
        if (!hasLoaded) {
            // Show cache immediately — user sees content right away
            if (viewModel.getCachedTasks() is Resource.Success) {
                viewModel.restoreFromCache()
            }
            try {
                val user = authRepository.getCurrentUserIfLoggedIn().getOrNull()
                if (user != null) {
                    userId = user.id
                    viewModel.loadUserSocialTasks(userId)
                } else {
                    viewModel.loadSocialTasks()
                }
            } catch (e: Exception) {
                Log.e("EKEHI_DEBUG", "Failed to get current user ID", e)
                // Only fetch unauthenticated if nothing is cached
                if (viewModel.getCachedTasks() !is Resource.Success) {
                    viewModel.loadSocialTasks()
                }
            }
            hasLoaded = true
        }
    }

    // ── Cooldown refresh — stable 5-min timer, NOT keyed on resource ──────
    // The old LaunchedEffect(socialTasksResource) caused an infinite reload
    // loop: loadUserSocialTasks -> new resource -> effect restarts -> repeat.
    // This timer is stable (keyed on Unit), runs in the background, and only
    // calls the network when a cooldown is actually still active.
    LaunchedEffect(Unit) {
        while (true) {
            delay(300_000) // 5 minutes
            val tasks = (socialTasksResource as? Resource.Success)?.data ?: emptyList()
            val hasActiveCooldown = tasks.any {
                it.nextAvailableAt != null &&
                calculateRemainingCooldown(it.nextAvailableAt).isNotEmpty()
            }
            if (hasActiveCooldown && userId.isNotEmpty()) {
                viewModel.loadUserSocialTasks(userId)
            }
        }
    }

    // ── Auto-dismiss snackbars after 4 s ─────────────────────────────────
    LaunchedEffect(verificationState) {
        if (verificationState is VerificationState.Success ||
            verificationState is VerificationState.Error   ||
            verificationState is VerificationState.Pending) {
            delay(4_000)
            viewModel.clearVerificationState()
        }
    }

    // ─────────────────────────────────────────
    // Root layout
    // ─────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(BrandColors.Black, BrandColors.DarkGray))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Header row
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
                                    listOf(BrandColors.Primary, BrandColors.PrimaryLight)
                                ),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
                IconButton(
                    onClick = {
                        // Use a single load call — calling both simultaneously
                        // causes a race and can flash the error state.
                        if (userId.isNotEmpty()) {
                            viewModel.loadUserSocialTasks(userId)
                        } else {
                            viewModel.loadSocialTasks()
                        }
                    },
                    modifier = Modifier.background(
                        BrandColors.Primary.copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    )
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = BrandColors.Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            EnhancedStatsSection(viewModel)
            Spacer(modifier = Modifier.height(24.dp))

            // Task list
            when (socialTasksResource) {
                is Resource.Success -> {
                    val tasks = (socialTasksResource as Resource.Success).data
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(tasks) { task ->
                            val currentTask = viewModel.getLocalTaskState(task.id) ?: task
                            val taskItem = SocialTaskItem(
                                id                     = currentTask.id,
                                title                  = currentTask.title,
                                description            = currentTask.description,
                                platform               = currentTask.platform,
                                taskType               = currentTask.taskType,
                                link                   = currentTask.actionUrl ?: "",
                                reward                 = currentTask.rewardCoins,
                                isCompleted            = currentTask.isCompleted,
                                isVerified             = currentTask.isVerified,
                                verificationMethod     = currentTask.verificationMethod,
                                status                 = currentTask.status ?: "available",
                                maxCompletionsPerDay   = currentTask.maxCompletionsPerDay,
                                cooldownMinutes        = currentTask.cooldownMinutes,
                                completionCountToday   = currentTask.completionCountToday,
                                nextAvailableAt        = currentTask.nextAvailableAt,
                                nextResetTime          = currentTask.nextResetTime,
                                totalAccumulatedRewards = currentTask.totalAccumulatedRewards,
                                totalCompletions       = currentTask.totalCompletions
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
                        item { Spacer(modifier = Modifier.height(20.dp)) }
                    }
                }
                is Resource.Loading -> {
                    // Only show skeleton on first load when there is no cached
                    // content. On subsequent refreshes keep the existing list
                    // visible so the screen never goes blank.
                    val cached = viewModel.getCachedTasks()
                    if (cached is Resource.Success && cached.data.isNotEmpty()) {
                        val tasks = cached.data
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(tasks) { task ->
                                val currentTask = viewModel.getLocalTaskState(task.id) ?: task
                                EnhancedSocialTaskCard(
                                    task = SocialTaskItem(
                                        id = currentTask.id, title = currentTask.title,
                                        description = currentTask.description, platform = currentTask.platform,
                                        taskType = currentTask.taskType, link = currentTask.actionUrl ?: "",
                                        reward = currentTask.rewardCoins, isCompleted = currentTask.isCompleted,
                                        isVerified = currentTask.isVerified, verificationMethod = currentTask.verificationMethod,
                                        status = currentTask.status ?: "available",
                                        maxCompletionsPerDay = currentTask.maxCompletionsPerDay,
                                        cooldownMinutes = currentTask.cooldownMinutes,
                                        completionCountToday = currentTask.completionCountToday,
                                        nextAvailableAt = currentTask.nextAvailableAt,
                                        nextResetTime = currentTask.nextResetTime,
                                        totalAccumulatedRewards = currentTask.totalAccumulatedRewards,
                                        totalCompletions = currentTask.totalCompletions
                                    ),
                                    onClick = { selectedTask = SocialTaskItem(
                                        id = currentTask.id, title = currentTask.title,
                                        description = currentTask.description, platform = currentTask.platform,
                                        taskType = currentTask.taskType, link = currentTask.actionUrl ?: "",
                                        reward = currentTask.rewardCoins, isCompleted = currentTask.isCompleted,
                                        isVerified = currentTask.isVerified, verificationMethod = currentTask.verificationMethod,
                                        status = currentTask.status ?: "available",
                                        maxCompletionsPerDay = currentTask.maxCompletionsPerDay,
                                        cooldownMinutes = currentTask.cooldownMinutes,
                                        completionCountToday = currentTask.completionCountToday,
                                        nextAvailableAt = currentTask.nextAvailableAt,
                                        nextResetTime = currentTask.nextResetTime,
                                        totalAccumulatedRewards = currentTask.totalAccumulatedRewards,
                                        totalCompletions = currentTask.totalCompletions
                                    )},
                                    verificationState = verificationState
                                )
                            }
                            item { Spacer(modifier = Modifier.height(20.dp)) }
                        }
                    } else {
                        SocialTasksScreenSkeleton()
                    }
                }
                is Resource.Error -> {
                    // Never wipe the screen on a background error.
                    // Show cached content + a dismissible top banner instead.
                    val cached = viewModel.getCachedTasks()
                    if (cached is Resource.Success && cached.data.isNotEmpty()) {
                        Column {
                            // Non-intrusive error banner — user can still see tasks
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = BrandColors.Error.copy(alpha = 0.08f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.WifiOff, null,
                                        tint = BrandColors.Error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Could not refresh. Showing last saved data.",
                                        color = BrandColors.Error,
                                        fontSize = 13.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            val tasks = cached.data
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(tasks) { task ->
                                    val currentTask = viewModel.getLocalTaskState(task.id) ?: task
                                    EnhancedSocialTaskCard(
                                        task = SocialTaskItem(
                                            id = currentTask.id, title = currentTask.title,
                                            description = currentTask.description, platform = currentTask.platform,
                                            taskType = currentTask.taskType, link = currentTask.actionUrl ?: "",
                                            reward = currentTask.rewardCoins, isCompleted = currentTask.isCompleted,
                                            isVerified = currentTask.isVerified, verificationMethod = currentTask.verificationMethod,
                                            status = currentTask.status ?: "available",
                                            maxCompletionsPerDay = currentTask.maxCompletionsPerDay,
                                            cooldownMinutes = currentTask.cooldownMinutes,
                                            completionCountToday = currentTask.completionCountToday,
                                            nextAvailableAt = currentTask.nextAvailableAt,
                                            nextResetTime = currentTask.nextResetTime,
                                            totalAccumulatedRewards = currentTask.totalAccumulatedRewards,
                                            totalCompletions = currentTask.totalCompletions
                                        ),
                                        onClick = { selectedTask = SocialTaskItem(
                                            id = currentTask.id, title = currentTask.title,
                                            description = currentTask.description, platform = currentTask.platform,
                                            taskType = currentTask.taskType, link = currentTask.actionUrl ?: "",
                                            reward = currentTask.rewardCoins, isCompleted = currentTask.isCompleted,
                                            isVerified = currentTask.isVerified, verificationMethod = currentTask.verificationMethod,
                                            status = currentTask.status ?: "available",
                                            maxCompletionsPerDay = currentTask.maxCompletionsPerDay,
                                            cooldownMinutes = currentTask.cooldownMinutes,
                                            completionCountToday = currentTask.completionCountToday,
                                            nextAvailableAt = currentTask.nextAvailableAt,
                                            nextResetTime = currentTask.nextResetTime,
                                            totalAccumulatedRewards = currentTask.totalAccumulatedRewards,
                                            totalCompletions = currentTask.totalCompletions
                                        )},
                                        verificationState = verificationState
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(20.dp)) }
                            }
                        }
                    } else {
                        // Truly no data at all — show full error with retry
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Spacer(modifier = Modifier.height(40.dp))
                            Icon(
                                Icons.Default.WifiOff, null,
                                tint = BrandColors.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                "Could not load tasks",
                                color = BrandColors.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                "Check your connection and try again.",
                                color = BrandColors.Gray,
                                fontSize = 14.sp
                            )
                            Button(
                                onClick = {
                                    if (userId.isNotEmpty()) viewModel.loadUserSocialTasks(userId)
                                    else viewModel.loadSocialTasks()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandColors.Primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Try Again", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                else -> {}
            }
        }

        // ── Dialogs ──────────────────────────────────────────────────────
        if (selectedTask != null && !showVerificationDialog) {
            if (selectedTask!!.status == "pending") {
                PendingTaskDialog(
                    task = selectedTask!!,
                    onDeleteTask = {
                        viewModel.deletePendingTask(userId, selectedTask!!.id)
                        selectedTask = null
                    },
                    onDismiss = { selectedTask = null }
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
                    },
                    onBlogSubmit = {
                        // Blog tasks need no review — submit directly and close
                        viewModel.completeSocialTask(
                            userId,
                            selectedTask!!.id,
                            mapOf(
                                "platform"     to "blog",
                                "submitted_at" to System.currentTimeMillis()
                            )
                        )
                        selectedTask = null
                        taskActionCompleted = false
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

        // ── Snackbars ─────────────────────────────────────────────────────
        when (verificationState) {
            is VerificationState.Success -> {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    containerColor = BrandColors.Success,
                    contentColor = BrandColors.White,
                    action = {
                        TextButton(
                            onClick = { viewModel.clearVerificationState() },
                            colors = ButtonDefaults.textButtonColors(contentColor = BrandColors.White)
                        ) { Text("Close") }
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.CheckCircle, null, tint = BrandColors.White)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            (verificationState as VerificationState.Success).message,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            is VerificationState.Error -> {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    containerColor = BrandColors.Error,
                    contentColor = BrandColors.White,
                    action = {
                        TextButton(
                            onClick = { viewModel.clearVerificationState() },
                            colors = ButtonDefaults.textButtonColors(contentColor = BrandColors.White)
                        ) { Text("Close") }
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Error, null, tint = BrandColors.White)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            (verificationState as VerificationState.Error).message,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            is VerificationState.Pending -> {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    containerColor = BrandColors.Warning,
                    contentColor = BrandColors.Black,
                    action = {
                        TextButton(
                            onClick = { viewModel.clearVerificationState() },
                            colors = ButtonDefaults.textButtonColors(contentColor = BrandColors.Black)
                        ) { Text("Close") }
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Schedule, null, tint = BrandColors.Black)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            (verificationState as VerificationState.Pending).message,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            else -> {}
        }
    }
}

// ─────────────────────────────────────────────
// Stats Section
// ─────────────────────────────────────────────
@Composable
fun EnhancedStatsSection(viewModel: SocialTasksViewModel) {
    val socialTasksResource by viewModel.socialTasks.collectAsState()
    val localTaskStates     by viewModel.localTaskStates.collectAsState()

    val tasks = remember(socialTasksResource, localTaskStates) {
        val base = (socialTasksResource as? Resource.Success)?.data ?: emptyList()
        if (localTaskStates.isNotEmpty()) base.map { localTaskStates[it.id] ?: it } else base
    }

    val completedTasks = remember(tasks) { tasks.count { it.isCompleted } }
    val totalTasks     = remember(tasks) { tasks.size }
    val totalRewards   = remember(tasks) { tasks.filter { it.isCompleted }.sumOf { it.rewardCoins } }
    val blogRewards    = remember(tasks) {
        tasks.filter { it.platform.lowercase() == "blog" }.sumOf { it.totalAccumulatedRewards }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(listOf(BrandColors.CardBackground, BrandColors.MediumGray)),
                RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(listOf(BrandColors.Primary, BrandColors.PrimaryLight)),
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
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isHighlight) BrandColors.Primary else BrandColors.PrimaryLight,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(text = value, color = BrandColors.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
            if (showLogoInLabel) {
                EkhLogo(size = 14.dp)
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(text = label, color = BrandColors.White.copy(alpha = 0.7f), fontSize = 13.sp)
        }
    }
}

// ─────────────────────────────────────────────
// Task Card
// ─────────────────────────────────────────────
@Composable
fun EnhancedSocialTaskCard(
    task: SocialTaskItem,
    onClick: () -> Unit,
    verificationState: VerificationState
) {
    val isBlog = task.platform.lowercase() == "blog"

    // Treat nextAvailableAt as active only when there is actually time remaining.
    // If the timestamp is in the past the cooldown has expired even if the server
    // hasn't cleared the field yet.
    val hasCooldownActive = isBlog &&
        task.nextAvailableAt != null &&
        calculateRemainingCooldown(task.nextAvailableAt).isNotEmpty()

    // Determine card-level clickability
    val cardClickable: Modifier = when {
        hasCooldownActive                                                  -> Modifier  // cooldown active
        isBlog && task.completionCountToday >= task.maxCompletionsPerDay  -> Modifier  // daily limit
        isBlog                                                            -> Modifier.clickable(onClick = onClick)
        task.isVerified                                                   -> Modifier  // done
        task.status == "pending"                                          -> Modifier.clickable(onClick = onClick)
        else                                                              -> Modifier.clickable(onClick = onClick)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .then(cardClickable),
        colors = CardDefaults.cardColors(containerColor = BrandColors.CardBackground),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box {
            // Top accent bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(listOf(
                            getPlatformColor(task.platform),
                            getPlatformColor(task.platform).copy(alpha = 0.5f)
                        ))
                    )
            )

            // Blog completion badge (top-right)
            if (isBlog) {
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
                // Platform icon + title + description
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(4.dp, RoundedCornerShape(16.dp))
                            .background(
                                Brush.radialGradient(listOf(
                                    getPlatformColor(task.platform),
                                    getPlatformColor(task.platform).copy(alpha = 0.7f)
                                )),
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            isBlog -> Icon(
                                Icons.Default.Repeat, "Blog",
                                tint = BrandColors.White, modifier = Modifier.size(28.dp)
                            )
                            task.link.isNotEmpty() -> AsyncImage(
                                model = getFaviconUrl(task.link),
                                contentDescription = task.platform,
                                modifier = Modifier.size(32.dp),
                                placeholder = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_help),
                                error = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_help)
                            )
                            else -> Icon(
                                getPlatformIcon(task.platform), task.platform,
                                tint = BrandColors.White, modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(task.title, color = BrandColors.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(
                            task.description,
                            color = BrandColors.White.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp),
                            lineHeight = 20.sp
                        )
                        if (task.link.isNotEmpty()) {
                            Text(
                                extractDomain(task.link),
                                color = BrandColors.White.copy(alpha = 0.5f),
                                fontSize = 12.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        // Verification method / blog info badge
                        Row(
                            modifier = Modifier
                                .background(
                                    color = when {
                                        isBlog -> BrandColors.Primary.copy(alpha = 0.15f)
                                        task.verificationMethod == "api" -> BrandColors.Success.copy(alpha = 0.15f)
                                        else -> BrandColors.Warning.copy(alpha = 0.15f)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    when {
                                        isBlog -> BrandColors.Primary.copy(alpha = 0.3f)
                                        task.verificationMethod == "api" -> BrandColors.Success.copy(alpha = 0.3f)
                                        else -> BrandColors.Warning.copy(alpha = 0.3f)
                                    },
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when {
                                    isBlog -> Icons.Default.Repeat
                                    task.verificationMethod == "api" -> Icons.Default.Verified
                                    else -> Icons.Default.Schedule
                                },
                                contentDescription = null,
                                tint = when {
                                    isBlog -> BrandColors.Primary
                                    task.verificationMethod == "api" -> BrandColors.Success
                                    else -> BrandColors.Warning
                                },
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            if (isBlog) {
                                Column {
                                    Text(
                                        "Daily Limit: ${task.maxCompletionsPerDay}",
                                        color = BrandColors.Primary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (task.totalCompletions > 0) {
                                        Text(
                                            "Earned ${task.totalCompletions}× " +
                                            "(${String.format("%.1f", task.totalAccumulatedRewards)} EKH total)",
                                            color = BrandColors.Primary.copy(alpha = 0.8f),
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    if (task.verificationMethod == "api") "Auto-verification" else "Manual review",
                                    color = if (task.verificationMethod == "api") BrandColors.Success else BrandColors.Warning,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Divider(color = BrandColors.LightGray.copy(alpha = 0.3f), thickness = 1.dp)
                Spacer(Modifier.height(16.dp))

                // Bottom row: reward badge + action button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reward badge
                    Row(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(listOf(
                                    BrandColors.Primary.copy(alpha = 0.2f),
                                    BrandColors.PrimaryLight.copy(alpha = 0.1f)
                                )),
                                RoundedCornerShape(12.dp)
                            )
                            .border(1.5.dp, BrandColors.Primary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Toll, null, tint = BrandColors.Primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("+${task.reward}", color = BrandColors.Primary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(4.dp))
                        EkhLogo(size = 14.dp)
                    }

                    // ── Action button / status badge ──────────────────────
                    // IMPORTANT: Blog states checked FIRST before isVerified,
                    // so a blog task in cooldown never shows "Completed".
                    // hasCooldownActive is true only when nextAvailableAt is in
                    // the future — an expired timestamp is treated as no cooldown.
                    when {

                        // 1. Blog – active cooldown between completions (time still remaining)
                        hasCooldownActive -> {
                            // Track live countdown; flip cooldownExpired when it hits zero
                            // so the card immediately switches to Start Task without a server
                            // refresh.
                            var cooldownExpired by remember(task.nextAvailableAt) {
                                mutableStateOf(false)
                            }
                            val remainingTime by produceState(
                                initialValue = calculateRemainingCooldown(task.nextAvailableAt),
                                key1 = task.nextAvailableAt
                            ) {
                                while (true) {
                                    delay(1_000)
                                    val updated = calculateRemainingCooldown(task.nextAvailableAt)
                                    value = updated
                                    if (updated.isEmpty()) {
                                        cooldownExpired = true
                                        break
                                    }
                                }
                            }

                            if (!cooldownExpired) {
                                // Still counting down
                                Button(
                                    onClick = {},
                                    enabled = false,
                                    colors = ButtonDefaults.buttonColors(
                                        disabledContainerColor = BrandColors.Gray.copy(alpha = 0.2f),
                                        disabledContentColor   = BrandColors.Gray
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp)
                                ) {
                                    Icon(Icons.Default.Timer, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = remainingTime,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            } else {
                                // Cooldown just expired — show Start Task immediately
                                Button(
                                    onClick = onClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandColors.Primary),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                                    elevation = ButtonDefaults.buttonElevation(4.dp, 2.dp)
                                ) {
                                    Text(
                                        text = if (task.completionCountToday > 0)
                                            "Continue (${task.completionCountToday}/${task.maxCompletionsPerDay})"
                                        else "Start Task",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Icon(Icons.Default.OpenInNew, null, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        // 2. Blog – daily limit fully reached
                        isBlog && task.completionCountToday >= task.maxCompletionsPerDay -> {
                            Row(
                                modifier = Modifier
                                    .background(BrandColors.Primary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .border(1.5.dp, BrandColors.Primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Repeat, null, tint = BrandColors.Primary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Column {
                                    Text(
                                        "${task.completionCountToday}/${task.maxCompletionsPerDay} Today",
                                        color = BrandColors.Primary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (task.nextResetTime != null) {
                                        val timeUntilReset by produceState(
                                            initialValue = calculateTimeUntilReset(task.nextResetTime),
                                            key1 = task.nextResetTime
                                        ) {
                                            while (true) {
                                                delay(60_000)
                                                value = calculateTimeUntilReset(task.nextResetTime)
                                            }
                                        }
                                        Text(
                                            "Resets in $timeUntilReset",
                                            color = BrandColors.Primary.copy(alpha = 0.8f),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        // 3. Blog – available (0 completions or partial, no cooldown)
                        isBlog -> {
                            Button(
                                onClick = onClick,
                                colors = ButtonDefaults.buttonColors(containerColor = BrandColors.Primary),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                                elevation = ButtonDefaults.buttonElevation(4.dp, 2.dp)
                            ) {
                                Text(
                                    text = if (task.completionCountToday > 0)
                                        "Continue (${task.completionCountToday}/${task.maxCompletionsPerDay})"
                                    else
                                        "Start Task",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(Modifier.width(6.dp))
                                Icon(Icons.Default.OpenInNew, null, modifier = Modifier.size(18.dp))
                            }
                        }

                        // 4. Non-blog – verified/completed
                        task.isVerified -> {
                            Row(
                                modifier = Modifier
                                    .background(BrandColors.Success.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .border(1.5.dp, BrandColors.Success.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, null, tint = BrandColors.Success, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Completed", color = BrandColors.Success, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // 5. Pending review
                        task.status == "pending" -> {
                            Row(
                                modifier = Modifier
                                    .background(BrandColors.Warning.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .border(1.5.dp, BrandColors.Warning.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Schedule, null, tint = BrandColors.Warning, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Reviewing", color = BrandColors.Warning, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // 6. Default – not started
                        else -> {
                            Button(
                                onClick = onClick,
                                colors = ButtonDefaults.buttonColors(containerColor = BrandColors.Primary),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                                elevation = ButtonDefaults.buttonElevation(4.dp, 2.dp)
                            ) {
                                Text("Start Task", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(Modifier.width(6.dp))
                                Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// TaskActionDialog
// ─────────────────────────────────────────────
@Composable
fun TaskActionDialog(
    task: SocialTaskItem,
    onDismiss: () -> Unit,
    onTaskCompleted: () -> Unit,
    onBlogSubmit: () -> Unit           // Blog tasks: claim reward directly, no review
) {
    val context = LocalContext.current
    var cooldownRemaining by remember { mutableStateOf(0L) }
    var hasOpenedBlog     by remember { mutableStateOf(false) }
    val isBlogTask = task.platform.lowercase() == "blog"

    LaunchedEffect(cooldownRemaining) {
        if (cooldownRemaining > 0) {
            delay(1_000)
            cooldownRemaining = (cooldownRemaining - 1).coerceAtLeast(0)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BrandColors.MediumGray,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(getPlatformColor(task.platform).copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(getPlatformIcon(task.platform), null, tint = getPlatformColor(task.platform), modifier = Modifier.size(24.dp))
                }
                Text(task.title, color = BrandColors.White, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(task.description, style = MaterialTheme.typography.bodyMedium, color = BrandColors.White.copy(alpha = 0.8f), lineHeight = 22.sp)
                Divider(color = BrandColors.LightGray.copy(alpha = 0.3f))

                // How-to box
                Column(
                    modifier = Modifier
                        .background(BrandColors.CardBackground, RoundedCornerShape(12.dp))
                        .border(1.dp, BrandColors.Primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = BrandColors.Primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("How to complete:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = BrandColors.White)
                    }
                    Text(getTaskInstructions(task.platform, task.taskType), style = MaterialTheme.typography.bodySmall, color = BrandColors.White.copy(alpha = 0.7f), lineHeight = 20.sp)
                }

                Divider(color = BrandColors.LightGray.copy(alpha = 0.3f))

                // Reward row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(listOf(
                                BrandColors.Primary.copy(alpha = 0.15f),
                                BrandColors.PrimaryLight.copy(alpha = 0.05f)
                            )),
                            RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, BrandColors.Primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Reward:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = BrandColors.White)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Toll, null, tint = BrandColors.Primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("+${task.reward} EKH", color = BrandColors.Primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }

                // Open link button
                if (task.link.isNotEmpty()) {
                    Button(
                        onClick = {
                            try {
                                val url = if (task.link.startsWith("http")) task.link else "https://${task.link}"
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                if (isBlogTask) { hasOpenedBlog = true; cooldownRemaining = 20L }
                            } catch (e: Exception) {
                                if (task.platform.lowercase() == "telegram") {
                                    try {
                                        val intent = context.packageManager.getLaunchIntentForPackage("org.telegram.messenger")
                                        context.startActivity(intent ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://web.telegram.org")))
                                    } catch (ex: Exception) {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://telegram.org")))
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = getPlatformColor(task.platform)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(Icons.Default.OpenInNew, "Open")
                        Spacer(Modifier.width(8.dp))
                        Text("Open on ${task.platform.replaceFirstChar { it.uppercase() }}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BrandColors.Error.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = BrandColors.Error)
                            Spacer(Modifier.width(8.dp))
                            Text("⚠️ No action URL configured. Please contact support.", style = MaterialTheme.typography.bodySmall, color = BrandColors.Error)
                        }
                    }
                }
            }
        },
        confirmButton = {
            when {
                task.status == "pending" -> Button(
                    onClick = {}, enabled = false,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandColors.Gray),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Schedule, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Under Review", fontWeight = FontWeight.Bold)
                }

                isBlogTask && !hasOpenedBlog -> Button(
                    onClick = {}, enabled = false,
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = BrandColors.Gray.copy(alpha = 0.3f),
                        disabledContentColor   = BrandColors.Gray
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Lock, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Visit Blog First", fontWeight = FontWeight.Bold)
                }

                isBlogTask && cooldownRemaining > 0 -> Button(
                    onClick = {}, enabled = false,
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = BrandColors.Primary.copy(alpha = 0.3f),
                        disabledContentColor   = BrandColors.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Timer, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Wait ${cooldownRemaining}s", fontWeight = FontWeight.Bold)
                }

                else -> Button(
                    onClick = if (isBlogTask) onBlogSubmit else onTaskCompleted,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandColors.Primary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Icon(
                        if (isBlogTask) Icons.Default.CheckCircle else Icons.Default.Done,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isBlogTask) "Claim Reward" else "Task Completed",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = BrandColors.White.copy(alpha = 0.7f))) {
                Text("Cancel")
            }
        }
    )
}

// ─────────────────────────────────────────────
// TaskVerificationDialog
// ─────────────────────────────────────────────
@Composable
fun TaskVerificationDialog(
    task: SocialTaskItem,
    viewModel: SocialTasksViewModel,
    authManager: SocialAuthManager,
    onDismiss: () -> Unit,
    onSubmit: (Map<String, Any>) -> Unit
) {
    var telegramUserId by remember { mutableStateOf("") }
    var username       by remember { mutableStateOf("") }
    var proofUrl       by remember { mutableStateOf("") }
    var isLoading      by remember { mutableStateOf(false) }
    var errorMessage   by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BrandColors.MediumGray,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(BrandColors.Success.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Verified, null, tint = BrandColors.Success, modifier = Modifier.size(24.dp))
                }
                Text("Verify Completion", color = BrandColors.White, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Now let's verify that you've completed the task.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = BrandColors.White)
                }
                item {
                    when {
                        task.platform.lowercase() == "blog" -> Text(
                            "Click submit below to claim your reward. You can do this up to ${task.maxCompletionsPerDay} times every 24 hours.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BrandColors.White.copy(alpha = 0.8f)
                        )
                        task.platform.lowercase() == "telegram" -> TelegramVerificationUI(
                            telegramUserId = telegramUserId,
                            onTelegramUserIdChange = { telegramUserId = it }
                        )
                        else -> ManualVerificationUI(
                            platform = task.platform,
                            username = username,
                            proofUrl = proofUrl,
                            onUsernameChange = { username = it },
                            onProofUrlChange = { proofUrl = it }
                        )
                    }
                }
                if (errorMessage.isNotEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BrandColors.Error.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, null, tint = BrandColors.Error, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(errorMessage, color = BrandColors.Error, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = BrandColors.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Submitting for Review...", color = BrandColors.White, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = ""
                        val proofData = buildProofData(task.platform, telegramUserId, username, proofUrl)
                        when {
                            task.platform.lowercase() == "telegram" && !isValidTelegramUserId(telegramUserId) -> {
                                errorMessage = getTelegramUserIdErrorMessage(telegramUserId)
                                    .ifEmpty { "Please enter a valid Telegram User ID" }
                                isLoading = false
                            }
                            proofData.isEmpty() -> {
                                errorMessage = "Please provide required information"
                                isLoading = false
                            }
                            else -> onSubmit(proofData)
                        }
                    },
                    enabled = !isLoading && isReadyToSubmit(task.platform, telegramUserId, username, proofUrl, task.taskType),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandColors.Primary,
                        disabledContainerColor = BrandColors.LightGray
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Send, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Submit Verification", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = BrandColors.White.copy(alpha = 0.7f))) {
                Text("Back")
            }
        }
    )
}

// ─────────────────────────────────────────────
// TelegramVerificationUI
// ─────────────────────────────────────────────
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
            modifier = Modifier.fillMaxWidth().background(Color(0xFF0088CC).copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, null, tint = Color(0xFF0088CC), modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Get your Telegram User ID to verify", style = MaterialTheme.typography.bodySmall, color = BrandColors.White, fontWeight = FontWeight.Bold)
        }

        Text("Steps to get your User ID:", style = MaterialTheme.typography.bodyMedium, color = BrandColors.White, fontWeight = FontWeight.Bold)

        Column(
            modifier = Modifier.fillMaxWidth().background(BrandColors.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "1️⃣ Click 'Open Verification Bot' button below",
                "2️⃣ Send /start to the bot",
                "3️⃣ Bot will reply with your User ID (numbers only)",
                "4️⃣ Copy the ID and paste it below"
            ).forEach {
                Text(it, style = MaterialTheme.typography.bodySmall, color = BrandColors.White.copy(alpha = 0.9f), lineHeight = 18.sp)
            }
        }

        Button(
            onClick = {
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/ekehi_task_bot")))
                } catch (e: Exception) {
                    try {
                        val intent = context.packageManager.getLaunchIntentForPackage("org.telegram.messenger")
                        context.startActivity(intent ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://web.telegram.org")))
                    } catch (ex: Exception) { /* last resort */ }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0088CC)),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Icon(Icons.Default.OpenInNew, "Open", tint = BrandColors.White)
            Spacer(Modifier.width(8.dp))
            Text("Open Verification Bot", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(Modifier.height(4.dp))
        Text("Enter your User ID:", style = MaterialTheme.typography.bodyMedium, color = BrandColors.White, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = telegramUserId,
            onValueChange = { if (it.all(Char::isDigit) || it.isEmpty()) onTelegramUserIdChange(it) },
            label = { Text("Telegram User ID", color = BrandColors.White.copy(alpha = 0.7f)) },
            placeholder = { Text("e.g., 123456789", color = BrandColors.White.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Tag, "ID", tint = Color(0xFF0088CC)) },
            isError = hasError,
            supportingText = {
                if (hasError) Text(errorMessage, color = BrandColors.Error)
                else Text("Only numbers, $TELEGRAM_USER_ID_MIN_LENGTH-$TELEGRAM_USER_ID_MAX_LENGTH digits", color = BrandColors.White.copy(alpha = 0.6f))
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = BrandColors.Primary,
                unfocusedBorderColor = BrandColors.LightGray,
                focusedTextColor     = BrandColors.White,
                unfocusedTextColor   = BrandColors.White,
                cursorColor          = BrandColors.Primary,
                errorBorderColor     = BrandColors.Error
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrandColors.Warning.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                .border(1.dp, BrandColors.Warning.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Lightbulb, null, tint = BrandColors.Warning, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Make sure you've already joined the channel before verifying!", style = MaterialTheme.typography.bodySmall, color = BrandColors.White.copy(alpha = 0.9f), fontSize = 12.sp)
        }
    }
}

// ─────────────────────────────────────────────
// ManualVerificationUI
// ─────────────────────────────────────────────
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
        Text("Provide proof of completion", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = BrandColors.White)
        Text("Your submission will be reviewed within 24-48 hours.", style = MaterialTheme.typography.bodySmall, color = BrandColors.Warning)

        OutlinedTextField(
            value = username, onValueChange = onUsernameChange,
            label = { Text("Your $platform Username", color = BrandColors.White.copy(alpha = 0.7f)) },
            placeholder = { Text("e.g., @yourhandle", color = BrandColors.White.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            leadingIcon = { Icon(Icons.Default.Person, "Username", tint = BrandColors.Primary) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandColors.Primary, unfocusedBorderColor = BrandColors.LightGray,
                focusedTextColor = BrandColors.White, unfocusedTextColor = BrandColors.White, cursorColor = BrandColors.Primary
            )
        )
        OutlinedTextField(
            value = proofUrl, onValueChange = onProofUrlChange,
            label = { Text("Screenshot URL or Post Link", color = BrandColors.White.copy(alpha = 0.7f)) },
            placeholder = { Text("https://...", color = BrandColors.White.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            leadingIcon = { Icon(Icons.Default.Link, "URL", tint = BrandColors.Primary) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandColors.Primary, unfocusedBorderColor = BrandColors.LightGray,
                focusedTextColor = BrandColors.White, unfocusedTextColor = BrandColors.White, cursorColor = BrandColors.Primary
            )
        )
    }
}

// ─────────────────────────────────────────────
// PendingTaskDialog
// ─────────────────────────────────────────────
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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(48.dp).background(BrandColors.Warning.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Schedule, null, tint = BrandColors.Warning, modifier = Modifier.size(24.dp))
                }
                Text("Task Under Review", color = BrandColors.White, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Your submission for \"${task.title}\" is currently under review by our team. This usually takes 24-48 hours.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrandColors.White.copy(alpha = 0.8f),
                    lineHeight = 22.sp
                )
                Divider(color = BrandColors.LightGray.copy(alpha = 0.3f))
                Text("What would you like to do?", style = MaterialTheme.typography.bodyMedium, color = BrandColors.White, fontWeight = FontWeight.Bold)
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = BrandColors.Primary),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) { Text("Wait for Review", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDeleteTask, colors = ButtonDefaults.textButtonColors(contentColor = BrandColors.Error)) {
                Text("Delete Submission")
            }
        }
    )
}

// ─────────────────────────────────────────────
// Helper functions
// ─────────────────────────────────────────────
fun getTaskInstructions(platform: String, taskType: String): String = when (platform.lowercase()) {
    "telegram" -> "1. Click the button below to open the Telegram channel/group\n2. Join the channel/group\n3. Come back here and click 'Task Completed'"
    "youtube"  -> when (taskType.lowercase()) {
        "subscribe", "channel_subscribe" -> "1. Click the button below to open YouTube\n2. Subscribe to the channel\n3. Come back here and click 'Task Completed'"
        "like", "video_like"             -> "1. Click the button below to open YouTube\n2. Watch and like the video\n3. Come back here and click 'Task Completed'"
        else                             -> "1. Click the button below to open YouTube\n2. Complete the required action\n3. Come back here and click 'Task Completed'"
    }
    "facebook"        -> "1. Click the button below to open Facebook\n2. Like the page\n3. Come back here and click 'Task Completed'"
    "twitter", "x"   -> "1. Click the button below to open Twitter/X\n2. Complete the action (follow, like, or retweet)\n3. Come back here and click 'Task Completed'"
    "instagram"       -> "1. Click the button below to open Instagram\n2. Follow the account or like the post\n3. Come back here and click 'Task Completed'"
    else              -> "1. Click the button below to open ${platform.replaceFirstChar { it.uppercase() }}\n2. Complete the required task\n3. Come back here and click 'Task Completed'"
}

fun isReadyToSubmit(platform: String, telegramUserId: String, username: String, proofUrl: String, taskType: String = ""): Boolean {
    if (platform.lowercase() == "blog") return true
    return when (platform.lowercase()) {
        "telegram" -> telegramUserId.isNotEmpty() && telegramUserId.all(Char::isDigit) && telegramUserId.length >= TELEGRAM_USER_ID_MIN_LENGTH
        else       -> username.isNotEmpty() || proofUrl.isNotEmpty()
    }
}

/**
 * Returns a human-readable countdown string with seconds precision.
 * e.g. "3h 45m", "12m 30s", "45s"
 */
fun calculateRemainingCooldown(nextAvailableAt: String?): String {
    if (nextAvailableAt == null) return ""
    return try {
        val diffMs = parseIsoDate(nextAvailableAt) - System.currentTimeMillis()
        if (diffMs <= 0) return ""
        val totalSeconds = diffMs / 1_000
        val hours   = totalSeconds / 3_600
        val minutes = (totalSeconds % 3_600) / 60
        val seconds = totalSeconds % 60
        when {
            hours   > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m ${seconds}s"
            else        -> "${seconds}s"
        }
    } catch (e: Exception) { "" }
}

fun calculateTimeUntilReset(nextResetTime: String?): String {
    if (nextResetTime == null) return "24h"
    return try {
        val diffMs = parseIsoDate(nextResetTime) - System.currentTimeMillis()
        if (diffMs <= 0) return "Now"
        val hours   = diffMs / (3_600_000)
        val minutes = (diffMs % 3_600_000) / 60_000
        if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    } catch (e: Exception) { "24h" }
}

fun parseIsoDate(dateStr: String?): Long {
    if (dateStr.isNullOrEmpty()) return 0L
    val normalized = dateStr.replace("+00:00", "Z")
    return try {
        java.time.Instant.parse(normalized).toEpochMilli()
    } catch (e: Exception) {
        try {
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
                .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                .parse(normalized)?.time ?: 0L
        } catch (e2: Exception) { 0L }
    }
}

fun isValidTelegramUserId(userId: String): Boolean =
    userId.isNotEmpty() &&
    userId.all(Char::isDigit) &&
    userId.length in TELEGRAM_USER_ID_MIN_LENGTH..TELEGRAM_USER_ID_MAX_LENGTH &&
    userId.toLongOrNull() != null

fun getTelegramUserIdErrorMessage(userId: String): String = when {
    userId.isEmpty()                                     -> ""
    !userId.all(Char::isDigit)                           -> "User ID should contain only numbers"
    userId.length < TELEGRAM_USER_ID_MIN_LENGTH          -> "User ID should be at least $TELEGRAM_USER_ID_MIN_LENGTH digits"
    userId.length > TELEGRAM_USER_ID_MAX_LENGTH          -> "User ID should not exceed $TELEGRAM_USER_ID_MAX_LENGTH digits"
    else                                                 -> ""
}

fun buildProofData(platform: String, telegramUserId: String, username: String, proofUrl: String): Map<String, Any> =
    buildMap {
        put("platform", platform)
        put("submitted_at", System.currentTimeMillis())
        when (platform.lowercase()) {
            "telegram" -> telegramUserId.toLongOrNull()?.let { put("telegram_user_id", it) }
            else -> {
                if (username.isNotEmpty()) put("username", username)
                if (proofUrl.isNotEmpty()) put("proof_url", proofUrl)
            }
        }
        if (username.isNotEmpty()) put("submitted_username", username)
        if (proofUrl.isNotEmpty()) put("submitted_proof_url", proofUrl)
    }

fun getPlatformColor(platform: String): Color = when (platform.lowercase()) {
    "telegram"        -> Color(0xFF0088CC)
    "youtube"         -> Color(0xFFFF0000)
    "facebook"        -> Color(0xFF4267B2)
    "twitter", "x"   -> Color(0xFF1DA1F2)
    "instagram"       -> Color(0xFFE4405F)
    else              -> BrandColors.Primary
}

fun getPlatformIcon(platform: String): androidx.compose.ui.graphics.vector.ImageVector = when (platform.lowercase()) {
    "telegram"        -> Icons.Default.Send
    "youtube"         -> Icons.Default.PlayArrow
    "facebook"        -> Icons.Default.ThumbUp
    "twitter", "x"   -> Icons.Default.Message
    "instagram"       -> Icons.Default.PhotoCamera
    else              -> Icons.Default.Public
}

fun extractDomain(url: String): String = try {
    URL(url).host.removePrefix("www.")
} catch (e: Exception) { "unknown" }

fun getFaviconUrl(url: String): String = try {
    "https://www.google.com/s2/favicons?domain=${URL(url).host}&sz=64"
} catch (e: Exception) {
    "https://www.google.com/s2/favicons?domain=example.com&sz=64"
}

// ─────────────────────────────────────────────
// Data model
// ─────────────────────────────────────────────
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
    val nextResetTime: String? = null,
    val totalAccumulatedRewards: Double = 0.0,
    val totalCompletions: Int = 0
)

// ─────────────────────────────────────────────
// Hilt entry point
// ─────────────────────────────────────────────
@EntryPoint
@InstallIn(SingletonComponent::class)
interface AuthRepositoryEntryPoint {
    fun authRepository(): AuthRepository
}