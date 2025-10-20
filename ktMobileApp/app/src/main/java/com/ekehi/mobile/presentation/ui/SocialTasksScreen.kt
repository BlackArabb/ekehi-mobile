package com.ekehi.mobile.presentation.ui

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
import com.ekehi.mobile.presentation.viewmodel.SocialTasksViewModel

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

            // Social Tasks List
            SocialTasksList(
                onTaskComplete = { taskId ->
                    viewModel.completeSocialTask(userId, taskId, "", 0.0) // Add missing parameters
                }
            )
        }
    }
}

@Composable
fun SocialTasksList(onTaskComplete: (String) -> Unit) {
    // In a real implementation, this would be populated with actual data
    val socialTasks = listOf(
        SocialTaskItem(
            id = "1",
            title = "Follow us on Twitter",
            description = "Follow our official Twitter account for updates",
            platform = "Twitter",
            reward = 0.5,
            isCompleted = false
        ),
        SocialTaskItem(
            id = "2",
            title = "Join our Telegram",
            description = "Join our Telegram community for exclusive updates",
            platform = "Telegram",
            reward = 0.5,
            isCompleted = true
        ),
        SocialTaskItem(
            id = "3",
            title = "Share on Facebook",
            description = "Share our app on Facebook with your friends",
            platform = "Facebook",
            reward = 1.0,
            isCompleted = false
        )
    )

    LazyColumn {
        items(socialTasks) { task ->
            SocialTaskCard(
                task = task,
                onTaskComplete = onTaskComplete
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SocialTaskCard(
    task: SocialTaskItem,
    onTaskComplete: (String) -> Unit
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
                        color = when (task.platform) {
                            "Twitter" -> Color(0xFF1DA1F2)
                            "Telegram" -> Color(0xFF0088CC)
                            "Facebook" -> Color(0xFF4267B2)
                            else -> Color(0xFFffa000)
                        },
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (task.platform) {
                        "Twitter" -> Icons.Default.Message
                        "Telegram" -> Icons.Default.Send
                        "Facebook" -> Icons.Default.ThumbUp
                        else -> Icons.Default.Public
                    },
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
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Complete Button or Checkmark
            if (task.isCompleted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Completed",
                    tint = Color(0xFF10b981), // green
                    modifier = Modifier.size(24.dp)
                )
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

data class SocialTaskItem(
    val id: String,
    val title: String,
    val description: String,
    val platform: String,
    val reward: Double,
    val isCompleted: Boolean
)