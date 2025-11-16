package com.ekehi.network.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.presentation.viewmodel.SocialTasksViewModel
import com.ekehi.network.ui.theme.EkehiMobileTheme
import com.ekehi.network.presentation.ui.components.SocialTasksScreenSkeleton
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import java.net.URL
import androidx.compose.foundation.border

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialTasksScreen(
    viewModel: SocialTasksViewModel = hiltViewModel()
) {
    // In a real app, get the user ID from the authentication context
    // For now, we'll use a placeholder
    var userId by remember { mutableStateOf("user_id_placeholder") }
    val socialTasksResource by viewModel.socialTasks.collectAsState()
    var isGridView by remember { mutableStateOf(false) }

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
                .padding(horizontal = 20.dp)
        ) {
            // Header with toggle button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Social Tasks",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // Toggle view button
                IconButton(
                    onClick = { isGridView = !isGridView }
                ) {
                    Icon(
                        imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                        contentDescription = if (isGridView) "List View" else "Grid View",
                        tint = Color.White
                    )
                }
            }

            // Stats Section (matching React Native design)
            StatsSection(viewModel)

            Spacer(modifier = Modifier.height(16.dp))

            // Social Tasks List
            when (socialTasksResource) {
                is Resource.Loading -> {
                    SocialTasksScreenSkeleton()
                }
                is Resource.Success -> {
                    val tasks = (socialTasksResource as Resource.Success).data
                    if (isGridView) {
                        SocialTasksGrid(
                            tasks = tasks,
                            onTaskComplete = { taskId ->
                                viewModel.completeSocialTask(userId, taskId, "", 0.0)
                            },
                            onTaskVerify = { taskId ->
                                viewModel.verifySocialTask(userId, taskId)
                            }
                        )
                    } else {
                        SocialTasksList(
                            tasks = tasks,
                            onTaskComplete = { taskId ->
                                viewModel.completeSocialTask(userId, taskId, "", 0.0)
                            },
                            onTaskVerify = { taskId ->
                                viewModel.verifySocialTask(userId, taskId)
                            }
                        )
                    }
                }
                is Resource.Error -> {
                    val error = (socialTasksResource as Resource.Error).message
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error: $error",
                            color = Color.Red
                        )
                    }
                }
                else -> {
                    // Idle state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tasks available",
                            color = Color.White
                        )
                    }
                }
            }
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
fun SocialTasksList(
    tasks: List<com.ekehi.network.data.model.SocialTask>,
    onTaskComplete: (String) -> Unit,
    onTaskVerify: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(tasks) { task ->
            // Create a SocialTaskItem from the SocialTask model
            val taskItem = SocialTaskItem(
                id = task.id,
                title = task.title,
                description = task.description,
                platform = task.platform,
                link = task.actionUrl ?: "",
                reward = task.rewardCoins,
                isCompleted = task.isCompleted,
                isVerified = task.isVerified
            )
            
            SocialTaskCard(
                task = taskItem,
                onTaskComplete = onTaskComplete,
                onTaskVerify = onTaskVerify
            )
        }
    }
}

@Composable
fun SocialTasksGrid(
    tasks: List<com.ekehi.network.data.model.SocialTask>,
    onTaskComplete: (String) -> Unit,
    onTaskVerify: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(0.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tasks) { task ->
            // Create a SocialTaskItem from the SocialTask model
            val taskItem = SocialTaskItem(
                id = task.id,
                title = task.title,
                description = task.description,
                platform = task.platform,
                link = task.actionUrl ?: "",
                reward = task.rewardCoins,
                isCompleted = task.isCompleted,
                isVerified = task.isVerified
            )
            
            SocialTaskGridItem(
                task = taskItem,
                onTaskComplete = onTaskComplete,
                onTaskVerify = onTaskVerify
            )
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
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color(0x4DFFA000), // 30% opacity orange
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x1AFFFFFF) // 10% opacity white
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Task Header (Icon, Title, Description)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Platform Icon with favicon support
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            color = getPlatformColor(task.platform),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (task.link.isNotEmpty()) {
                        // Try to load favicon, fallback to platform icon if failed
                        val faviconUrl = getFaviconUrl(task.link)
                        Image(
                            painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(LocalContext.current)
                                    .data(faviconUrl)
                                    .crossfade(true)
                                    .build()
                            ),
                            contentDescription = task.platform,
                            modifier = Modifier.size(32.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Icon(
                            imageVector = getPlatformIcon(task.platform),
                            contentDescription = task.platform,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Task Info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = task.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = task.description,
                        color = Color(0xB3FFFFFF), // 70% opacity white
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp),
                        lineHeight = 20.sp
                    )
                    if (task.link.isNotEmpty()) {
                        Text(
                            text = extractDomain(task.link),
                            color = Color(0x80FFFFFF), // 50% opacity white
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Task Footer (Reward and Action Button)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Reward Container
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "+${task.reward}",
                        color = Color(0xFFffa000),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "EKH",
                        color = Color(0x99FFFFFF), // 60% opacity white
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Action Button
                if (task.isVerified) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Verified",
                            tint = Color(0xFF10b981), // green
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Completed",
                            color = Color(0xFF10b981),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (task.isCompleted) {
                    Button(
                        onClick = { onTaskVerify(task.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFf59e0b)
                        ),
                        modifier = Modifier
                            .height(36.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Completed",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = { onTaskComplete(task.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFffa000)
                        ),
                        modifier = Modifier
                            .height(36.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew, // Changed from ExternalLink to OpenInNew
                                contentDescription = "Complete",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Complete",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SocialTaskGridItem(
    task: SocialTaskItem,
    onTaskComplete: (String) -> Unit,
    onTaskVerify: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color(0x4DFFA000), // 30% opacity orange
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x1AFFFFFF) // 10% opacity white
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Platform Icon with favicon support
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        color = getPlatformColor(task.platform),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (task.link.isNotEmpty()) {
                    // Try to load favicon, fallback to platform icon if failed
                    val faviconUrl = getFaviconUrl(task.link)
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalContext.current)
                                .data(faviconUrl)
                                .crossfade(true)
                                .build()
                        ),
                        contentDescription = task.platform,
                        modifier = Modifier.size(32.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(
                        imageVector = getPlatformIcon(task.platform),
                        contentDescription = task.platform,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Task Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = task.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                Text(
                    text = task.description,
                    color = Color(0xB3FFFFFF), // 70% opacity white
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 2,
                    lineHeight = 16.sp
                )
                if (task.link.isNotEmpty()) {
                    Text(
                        text = extractDomain(task.link),
                        color = Color(0x80FFFFFF), // 50% opacity white
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 4.dp),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
                Text(
                    text = "+${task.reward} EKH",
                    color = Color(0xFFffa000),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Button
            if (task.isVerified) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Verified",
                        tint = Color(0xFF10b981), // green
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Completed",
                        color = Color(0xFF10b981),
                        fontSize = 10.sp
                    )
                }
            } else if (task.isCompleted) {
                Button(
                    onClick = { onTaskVerify(task.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFf59e0b)
                    ),
                    modifier = Modifier
                        .height(28.dp)
                        .fillMaxWidth(0.8f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Completed",
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }
                }
            } else {
                Button(
                    onClick = { onTaskComplete(task.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFffa000)
                    ),
                    modifier = Modifier
                        .height(28.dp)
                        .fillMaxWidth(0.8f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew, // Changed from ExternalLink to OpenInNew
                            contentDescription = "Complete",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Complete",
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

// Function to generate favicon URL like React Native app
fun getFaviconUrl(url: String): String {
    return try {
        val domain = URL(url).host
        "https://www.google.com/s2/favicons?domain=$domain&sz=64"
    } catch (e: Exception) {
        "https://www.google.com/s2/favicons?domain=example.com&sz=64"
    }
}

// Function to extract domain from URL like React Native app
fun extractDomain(url: String): String {
    return try {
        val domain = URL(url).host
        if (domain.startsWith("www.")) {
            domain.substring(4)
        } else {
            domain
        }
    } catch (e: Exception) {
        "unknown"
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

@Preview(showBackground = true)
@Composable
fun SocialTasksScreenPreview() {
    EkehiMobileTheme {
        SocialTasksScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun SocialTaskCardPreview() {
    EkehiMobileTheme {
        SocialTaskCard(
            task = SocialTaskItem(
                id = "1",
                title = "Follow us on Twitter",
                description = "Follow our official Twitter account for updates",
                platform = "Twitter",
                link = "https://twitter.com/ekehi_network",
                reward = 0.5,
                isCompleted = false,
                isVerified = false
            ),
            onTaskComplete = {},
            onTaskVerify = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SocialTaskFilterTabsPreview() {
    EkehiMobileTheme {
        SocialTaskFilterTabs()
    }
}