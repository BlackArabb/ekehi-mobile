package com.ekehi.network.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
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
import com.ekehi.network.data.model.UserProfile
import com.ekehi.network.domain.model.Resource
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import android.app.Activity
import android.util.Log
import com.ekehi.network.service.StartIoService
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener
import com.startapp.sdk.adsbase.adlisteners.AdEventListener
import com.startapp.sdk.adsbase.Ad
import com.ekehi.network.di.StartIoServiceEntryPoint
import com.ekehi.network.util.EventBus
import com.ekehi.network.util.Event

@Composable
fun MiningScreen(
    viewModel: MiningViewModel = hiltViewModel()
) {
    // Get MiningManager through DI
    /*val miningManager = EntryPointAccessors.fromApplication(
        LocalContext.current.applicationContext,
        MiningManagerEntryPoint::class.java
    ).getMiningManager()*/
    
    // Get StartIoService through DI
    val startIoService = EntryPointAccessors.fromApplication(
        LocalContext.current.applicationContext,
        StartIoServiceEntryPoint::class.java
    ).startIoService() // Changed from getStartIoService() to startIoService()
    
    val context = LocalContext.current
    val activity = context as? Activity
    
    val scrollState = rememberScrollState()
    val isMining by viewModel.is24HourMiningActive.collectAsState()
    val remainingTime by viewModel.remainingTime.collectAsState()
    val progressPercentage by viewModel.progressPercentage.collectAsState()
    val sessionReward by viewModel.sessionReward.collectAsState()
    val sessionEarnings by viewModel.sessionEarnings.collectAsState() // Add real-time earnings
    /*val finalRewardClaimed by viewModel.finalRewardClaimed.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()*/
    val errorMessage by viewModel.errorMessage.collectAsState()
    val userProfileResource by viewModel.userProfile.collectAsState()
    
    // Extract the actual UserProfile from Resource
    val userProfile: UserProfile? = when (userProfileResource) {
        is Resource.Success -> (userProfileResource as Resource.Success<UserProfile>).data
        else -> null
    }

    // Refresh mining status when screen appears
    LaunchedEffect(Unit) {
        viewModel.refreshMiningStatus()
    }
    
    // Listen for profile refresh events
    LaunchedEffect(Unit) {
        EventBus.events.collect { event ->
            when (event) {
                is Event.RefreshUserProfile -> {
                    // Refresh user profile when we receive this event
                    Log.d("MiningScreen", "Received RefreshUserProfile event")
                    viewModel.refreshUserProfile()
                }
                else -> {
                    // Handle other events if needed
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
                .padding(horizontal = 20.dp)
                .padding(bottom = 100.dp), // Bottom padding for safe area
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // User Profile Card at the top with real user data and streaks
            UserProfileCard(userProfile = userProfile)
            
            Spacer(modifier = Modifier.height(24.dp))

            // Mining Stats - Pass the actual session earnings
            MiningScreenStats(totalMined = userProfile?.totalCoins ?: 0.0, sessionEarnings = sessionEarnings)

            Spacer(modifier = Modifier.height(32.dp))

            // Enhanced Mining Button with Circular Progress
            EnhancedMiningButton(
                isMining = isMining,
                remainingTime = remainingTime,
                progressPercentage = progressPercentage,
                sessionReward = sessionReward,
                onClick = { 
                    viewModel.handleMine()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Ad Bonus Button - Modified to use StartIoService
            MiningAdBonusButton(startIoService = startIoService, activity = activity)

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
fun UserProfileCard(
    userProfile: UserProfile?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color(0x1AFFFFFF), // rgba(255, 255, 255, 0.1)
                shape = RoundedCornerShape(20.dp)
            )
            .shadow(elevation = 5.dp, shape = RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x0CFFFFFF) // rgba(255, 255, 255, 0.05)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Avatar
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF8b5cf6),
                                Color(0xFFa855f7)
                            )
                        ),
                        shape = CircleShape
                    )
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                
                // Online indicator
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                        .background(Color(0xFF10b981), CircleShape) // Green dot
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // User Info Section
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = userProfile?.username ?: "User Name",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Verification Badge
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(Color(0xFF3b82f6), CircleShape), // Blue circle
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.White, CircleShape) // White dot inside
                        )
                    }
                }
                
                Text(
                    text = "Total Balance",
                    color = Color(0xB3FFFFFF), // Light gray
                    fontSize = 14.sp
                )
                
                Text(
                    text = "%.2f EKH".format(userProfile?.totalCoins ?: 0.0),
                    color = Color(0xFFffa000), // Orange
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Stats Section with Streaks
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Current Streak
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = Color(0xFFef4444), // Red
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = (userProfile?.currentStreak ?: 0).toString(),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Streak",
                        color = Color(0xB3FFFFFF), // Light gray
                        fontSize = 12.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Longest Streak
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Longest Streak",
                            tint = Color(0xFFfbbf24), // Yellow
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = (userProfile?.longestStreak ?: 0).toString(),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Best",
                        color = Color(0xB3FFFFFF), // Light gray
                        fontSize = 12.sp
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
                    label = "Current",
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
fun MiningAdBonusButton(
    startIoService: StartIoService,
    activity: Activity?
) {
    val context = LocalContext.current
    var isAdLoading by remember { mutableStateOf(false) }
    var adErrorMessage by remember { mutableStateOf<String?>(null) }
    
    // Load ad when component is first composed
    LaunchedEffect(Unit) {
        if (startIoService.isStartIoInitialized()) {
            startIoService.loadRewardedAd(object : AdEventListener {
                override fun onReceiveAd(ad: Ad) {
                    Log.d("MiningScreen", "Ad loaded successfully")
                }
                
                override fun onFailedToReceiveAd(ad: Ad?) {
                    Log.e("MiningScreen", "Failed to load ad")
                    adErrorMessage = "Failed to load ad. Please try again later."
                }
            })
        }
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { 
                // Reset error message
                adErrorMessage = null
                
                // Check if we have an activity and Start.io is initialized
                if (activity == null) {
                    adErrorMessage = "Unable to show ad: No activity context"
                    return@Button
                }
                
                if (!startIoService.isStartIoInitialized()) {
                    adErrorMessage = "Ads not initialized. Please try again later."
                    return@Button
                }
                
                // Check if ad is ready
                if (startIoService.isRewardedAdReady()) {
                    // Show the ad
                    startIoService.showRewardedAd(activity, object : AdDisplayListener {
                        override fun adHidden(ad: Ad?) {
                            Log.d("MiningScreen", "Ad closed by user")
                            // TODO: Add reward to user's account here
                        }
                        
                        override fun adDisplayed(ad: Ad?) {
                            Log.d("MiningScreen", "Ad displayed successfully")
                        }
                        
                        override fun adClicked(ad: Ad?) {
                            Log.d("MiningScreen", "Ad clicked by user")
                        }
                        
                        override fun adNotDisplayed(ad: Ad?) {
                            Log.e("MiningScreen", "Ad not displayed")
                            adErrorMessage = "Ad could not be displayed. Please try again."
                        }
                    })
                } else {
                    adErrorMessage = "Ad not ready yet. Please try again in a moment."
                    // Try to load a new ad
                    startIoService.loadRewardedAd()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF8b5cf6),
                                Color(0xFF7c3aed)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isAdLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Watch Ad",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = "Watch Ad for +0.5 EKH",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Watch a short ad to earn bonus EKH tokens",
                color = Color(0xB3FFFFFF), // Light gray
                fontSize = 14.sp
            )
        }
        
        // Show error message if there is one
        adErrorMessage?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                color = Color.Red,
                fontSize = 12.sp
            )
        }
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

@Composable
fun EnhancedMiningButton(
    isMining: Boolean,
    remainingTime: Int,
    progressPercentage: Double,
    sessionReward: Double,
    onClick: () -> Unit
) {
    val isCompleted = isMining && remainingTime <= 0
    // Dynamic button size based on screen width (min 150dp, max 40% of screen)
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val buttonSize = screenWidth * 0.4f
    val clampedButtonSize = buttonSize.coerceIn(150.dp, 200.dp)
    val progressSize = clampedButtonSize + 40.dp
    
    // Pulsating animation for active mining
    val pulseAnimation = rememberInfiniteTransition()
    val pulseScale by pulseAnimation.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(progressSize)
        ) {
            // Circular Progress Bar (Visible when mining is active and time remaining > 0)
            if (isMining && remainingTime > 0) {
                CircularProgressBar(
                    progress = (progressPercentage / 100).toFloat()
                    //isPulsating = false // Removed pulse animation as requested
                )
            }
            
            // Main Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(clampedButtonSize)
                    .clip(CircleShape)
                    .background(
                        brush = when {
                            isCompleted -> Brush.verticalGradient(
                                colors = listOf(Color(0xFFffa000), Color(0xFFff8f00))
                            )
                            isMining -> Brush.verticalGradient(
                                colors = listOf(Color(0xFF10b981), Color(0xFF059669))
                            )
                            else -> Brush.verticalGradient(
                                colors = listOf(Color(0xFFffa000), Color(0xFFff8f00), Color(0xFFff6f00))
                            )
                        }
                    )
                    .clickable(
                        enabled = !isMining || isCompleted,
                        onClick = onClick
                    )
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        spotColor = Color.Black.copy(alpha = 0.4f),
                        ambientColor = Color.Black.copy(alpha = 0.4f)
                    )
            ) {
                // Button Content based on state
                when {
                    isMining && remainingTime > 0 -> {
                        // Active mining: Show time countdown
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = formatTime(remainingTime),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Remaining",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }
                    isCompleted -> {
                        // Completed: Show coins icon
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = "Claim Reward",
                            tint = Color.White,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                    else -> {
                        // Ready to start: Show pickaxe icon
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Start Mining",
                            tint = Color.White,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }
            }
        }
        
        // Status Indicator with margin as requested
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            when {
                isMining && remainingTime > 0 -> {
                    // Mining status with pulsating dot
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                color = Color(0x1AFFFFFF),
                                shape = CircleShape
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Canvas(
                            modifier = Modifier
                                .size(12.dp)
                                .graphicsLayer {
                                    scaleX = pulseScale
                                    scaleY = pulseScale
                                }
                        ) {
                            drawCircle(color = Color(0xFF10b981))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mining",
                            color = Color(0xFF10b981),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                else -> {
                    // Action text
                    Text(
                        text = if (isCompleted) "Claim ${"%.2f".format(sessionReward)} EKH Reward" else "Start Mining",
                        color = Color(0xFFffa000),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

fun formatTime(seconds: Int): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format("%02d:%02d:%02d", hrs, mins, secs)
}

@Composable
fun CircularProgressBar(
    progress: Float
    /*isPulsating: Boolean*/
) {
    val strokeWidth = 10.dp
    val backgroundColor = Color(0x1AFFFFFF) // 10% opacity white
    val progressColor = Color(0xFF10b981) // Green
    
    Canvas(
        modifier = Modifier
            .size(190.dp)
    ) {
        /*val canvasSize = size.minDimension*/
        val stroke = Stroke(width = strokeWidth.toPx())
        
        // Background circle
        drawCircle(
            color = backgroundColor,
            style = stroke
        )
        
        // Progress arc
        if (progress > 0) {
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360 * progress,
                useCenter = false,
                style = stroke
            )
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
