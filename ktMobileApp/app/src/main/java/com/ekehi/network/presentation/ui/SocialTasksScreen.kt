package com.ekehi.network.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.ekehi.network.presentation.viewmodel.SocialTasksViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialTasksScreen(
    viewModel: SocialTasksViewModel = hiltViewModel()
) {
    var userId by remember { mutableStateOf("user_id_placeholder") } // In a real app, get from auth context

    LaunchedEffect(Unit) {
        viewModel.loadSocialTasks()
        viewModel.loadUserSocialTasks(userId)
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
                .padding(20.dp)
        ) {
            // Header
            Text(
                text = "Social Tasks",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 20.dp, bottom = 24.dp)
            )

            // Filter Tabs
            SocialTaskFilterTabs()

            Spacer(modifier = Modifier.height(16.dp))

            // Social Tasks List
            SocialTasksList(
                onTaskComplete = { taskId ->
                    viewModel.completeSocialTask(userId, taskId, "", 0.0) // Add missing parameters
                },
                onTaskVerify = { taskId ->
                    viewModel.verifySocialTask(userId, taskId)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialTaskFilterTabs() {
    val filterOptions = listOf("All", "Pending", "Completed", "Verified")
    var selectedFilter by remember { mutableStateOf("All") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        filterOptions.forEach { option ->
            FilterChip(
                selected = selectedFilter == option,
                onClick = { selectedFilter = option },
                label = { 
                    Text(
                        text = option, 
                        color = if (selectedFilter == option) Color.Black else Color.White
                    ) 
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFffa000)
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
fun SocialTasksList(onTaskComplete: (String) -> Unit, onTaskVerify: (String) -> Unit) {
    // In a real implementation, this would be populated with actual data from viewModel
    val socialTasks = listOf(
        SocialTaskItem(
            id = "1",
            title = "Follow us on Twitter",
            description = "Follow our official Twitter account for updates",
            platform = "Twitter",
            link = "https://twitter.com/ekehi_network",
            reward = 0.5,
            isCompleted = false,
            isVerified = false
        ),
        SocialTaskItem(
            id = "2",
            title = "Join our Telegram",
            description = "Join our Telegram community for exclusive updates",
            platform = "Telegram",
            link = "https://t.me/ekehi_network",
            reward = 0.5,
            isCompleted = true,
            isVerified = false
        ),
        SocialTaskItem(
            id = "3",
            title = "Share on Facebook",
            description = "Share our app on Facebook with your friends",
            platform = "Facebook",
            link = "https://facebook.com/ekehi.network",
            reward = 1.0,
            isCompleted = true,
            isVerified = true
        ),
        SocialTaskItem(
            id = "4",
            title = "Follow on Instagram",
            description = "Follow our Instagram account for visual updates",
            platform = "Instagram",
            link = "https://instagram.com/ekehi_network",
            reward = 0.5,
            isCompleted = false,
            isVerified = false
        ),
        SocialTaskItem(
            id = "5",
            title = "Subscribe on YouTube",
            description = "Subscribe to our YouTube channel for tutorials",
            platform = "YouTube",
            link = "https://youtube.com/ekehi_network",
            reward = 0.75,
            isCompleted = false,
            isVerified = false
        )
    )

    LazyColumn {
        items(socialTasks) { task ->
            SocialTaskCard(
                task = task,
                onTaskComplete = onTaskComplete,
                onTaskVerify = onTaskVerify
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SocialTaskCard(
    task: SocialTaskItem,
    onTaskComplete: (String) -> Unit,
    onTaskVerify: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x1AFFFFFF) // 10% opacity white
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Platform Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = getPlatformColor(task.platform),
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getPlatformIcon(task.platform),
                    contentDescription = task.platform,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Task Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = task.description,
                    color = Color(0xB3FFFFFF), // 70% opacity white
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "+${task.reward} EKH",
                    color = Color(0xFFffa000),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                // Status badge
                if (task.isVerified) {
                    Text(
                        text = "Verified",
                        color = Color(0xFF10b981),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else if (task.isCompleted) {
                    Text(
                        text = "Completed - Awaiting Verification",
                        color = Color(0xFFf59e0b),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Action Button
            if (task.isVerified) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Verified",
                    tint = Color(0xFF10b981), // green
                    modifier = Modifier.size(24.dp)
                )
            } else if (task.isCompleted) {
                Button(
                    onClick = { onTaskVerify(task.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFf59e0b)
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = "Verify",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            } else {
                Button(
                    onClick = { onTaskComplete(task.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFffa000)
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = "Complete",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

fun getPlatformColor(platform: String): Color {
    return when (platform.lowercase()) {
        "twitter", "x" -> Color(0xFF1DA1F2)
        "telegram" -> Color(0xFF0088CC)
        "facebook" -> Color(0xFF4267B2)
        "instagram" -> Color(0xFFE1306C)
        "youtube" -> Color(0xFFFF0000)
        "tiktok" -> Color(0xFF69C9D0)
        "linkedin" -> Color(0xFF0077B5)
        "ekehi" -> Color(0xFFffa000)
        else -> Color(0xFFffa000)
    }
}

fun getPlatformIcon(platform: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (platform.lowercase()) {
        "twitter", "x" -> Icons.Default.Message
        "telegram" -> Icons.Default.Send
        "facebook" -> Icons.Default.ThumbUp
        "instagram" -> Icons.Default.Camera
        "youtube" -> Icons.Default.PlayArrow
        "tiktok" -> Icons.Default.MusicNote
        "linkedin" -> Icons.Default.Work
        "ekehi" -> Icons.Default.Public
        else -> Icons.Default.Public
    }
}

data class SocialTaskItem(
    val id: String,
    val title: String,
    val description: String,
    val platform: String,
    val link: String,
    val reward: Double,
    val isCompleted: Boolean,
    val isVerified: Boolean
)