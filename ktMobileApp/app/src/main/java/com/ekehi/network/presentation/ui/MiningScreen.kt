package com.ekehi.network.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.ekehi.network.presentation.viewmodel.MiningViewModel
import com.ekehi.network.service.MiningManager
import com.ekehi.network.ui.theme.EkehiMobileTheme
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay

@Composable
fun MiningScreen(
    viewModel: MiningViewModel = hiltViewModel()
) {
    // Get MiningManager through DI
    val miningManager = EntryPointAccessors.fromApplication(
        LocalContext.current.applicationContext,
        MiningManagerEntryPoint::class.java
    ).getMiningManager()
    
    val scrollState = rememberScrollState()
    val isMining by viewModel.is24HourMiningActive.collectAsState()
    val remainingTime by viewModel.remainingTime.collectAsState()
    val progressPercentage by viewModel.progressPercentage.collectAsState()
    val sessionReward by viewModel.sessionReward.collectAsState()
    val finalRewardClaimed by viewModel.finalRewardClaimed.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Refresh mining status when screen appears
    LaunchedEffect(Unit) {
        viewModel.refreshMiningStatus()
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Mining Dashboard",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 20.dp, bottom = 24.dp)
            )

            // Circular Progress Bar
            MiningProgressBar(
                progress = (progressPercentage / 100).toFloat(),
                remainingTime = viewModel.formatTime(remainingTime),
                isMining = isMining
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Mining Stats - Pass the actual session earnings
            MiningScreenStats(totalMined = 0.0, sessionEarnings = sessionReward)

            Spacer(modifier = Modifier.height(32.dp))

            // Mining Button
            MiningButton(
                onClick = { 
                    viewModel.handleMine()
                },
                isMining = isMining,
                isCompleted = remainingTime <= 0 && isMining
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Ad Bonus Button
            MiningAdBonusButton()

            Spacer(modifier = Modifier.height(24.dp))

            // Auto Mining Status
            MiningAutoMiningStatus()

            Spacer(modifier = Modifier.height(24.dp))

            // Referral Card
            ReferralCard()
        }
        
        // Show error message if there is one
        if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text(
                        text = errorMessage!!,
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MiningProgressBar(progress: Float, remainingTime: String, isMining: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(200.dp)
        ) {
            CircularProgressIndicator(
                progress = if (isMining) progress else 0f,
                modifier = Modifier.size(200.dp),
                color = Color(0xFFffa000),
                strokeWidth = 12.dp,
                strokeCap = StrokeCap.Round
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isMining) "Mining in Progress" else "Ready to Mine",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (isMining) remainingTime else "24:00:00",
                    color = Color(0xFFffa000),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = "2.0 EKH Reward",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun MiningScreenStats(totalMined: Double, sessionEarnings: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x1AFFFFFF) // 10% opacity white
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Mining Statistics",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatCard(
                    value = "0.0833",
                    label = "EKH/hour",
                    icon = Icons.Default.Speed
                )

                StatCard(
                    value = "%.4f".format(totalMined),
                    label = "Total Mined",
                    icon = Icons.Default.AccountBalance
                )

                StatCard(
                    value = "%.4f".format(sessionEarnings),
                    label = "This Session",
                    icon = Icons.Default.TrendingUp
                )
            }
        }
    }
}

@Composable
fun StatCard(value: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x1AFFFFFF) // 10% opacity white
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFffa000),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = Color.Gray,
                fontSize = 12.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun MiningButton(onClick: () -> Unit, isMining: Boolean, isCompleted: Boolean = false) {
    val buttonText = when {
        isCompleted -> "Claim Reward"
        isMining -> "Stop Mining"
        else -> "Start Mining"
    }
    
    val buttonColor = when {
        isCompleted -> Color(0xFF4ecdc4) // Teal for claim reward
        isMining -> Color(0xFFff6b6b) // Red for stop mining
        else -> Color(0xFFffa000) // Orange for start mining
    }
    
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor
        ),
        shape = CircleShape,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        Text(
            text = buttonText,
            color = Color.Black,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MiningAdBonusButton() {
    Button(
        onClick = { /* Handle ad bonus */ },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Watch Ad",
            tint = Color(0xFF4ecdc4),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Watch Ad for Bonus",
            color = Color(0xFF4ecdc4),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun MiningAutoMiningStatus() {
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
            Icon(
                imageVector = Icons.Default.Autorenew,
                contentDescription = "Auto Mining",
                tint = Color(0xFF4ecdc4),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Auto Mining",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Keep app in background to continue mining",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            Switch(
                checked = true,
                onCheckedChange = { /* Handle toggle */ },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF4ecdc4),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.Gray
                )
            )
        }
    }
}

@Composable
fun ReferralCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x1AFFFFFF) // 10% opacity white
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Referral",
                    tint = Color(0xFFffa000),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Invite Friends",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Earn 0.5 EKH for each friend who joins",
                color = Color.Gray,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { /* Handle share */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFffa000)
                )
            ) {
                Text(
                    text = "Share Referral Link",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MiningScreenPreview() {
    EkehiMobileTheme {
        MiningScreen()
    }
}

// Entry point for accessing MiningManager through DI
@EntryPoint
@InstallIn(SingletonComponent::class)
interface MiningManagerEntryPoint {
    fun getMiningManager(): MiningManager
}