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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.rememberCoroutineScope
import com.ekehi.network.presentation.viewmodel.MiningViewModel
import com.ekehi.network.service.MiningManager
import com.ekehi.network.ui.theme.EkehiMobileTheme
import com.ekehi.network.data.model.UserProfile
import com.ekehi.network.data.model.AdContent
import com.ekehi.network.data.model.AdType
import com.ekehi.network.domain.model.Resource
import kotlinx.coroutines.launch
import io.appwrite.Client
import com.ekehi.network.presentation.ui.components.MiningScreenSkeleton
import com.ekehi.network.presentation.ui.components.AdsCarousel
import com.ekehi.network.presentation.ui.components.ImageAdsCarousel
import com.ekehi.network.presentation.viewmodel.AdsViewModel
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.delay
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import android.app.Activity
import android.content.Intent
import android.util.Log
import com.ekehi.network.service.StartIoService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener
import com.startapp.sdk.adsbase.adlisteners.AdEventListener
import com.startapp.sdk.adsbase.Ad
import com.ekehi.network.di.StartIoServiceEntryPoint
import com.ekehi.network.util.EventBus
import com.ekehi.network.util.Event
import com.ekehi.network.R
import androidx.navigation.NavHostController

@Composable
fun MiningScreen(
    viewModel: MiningViewModel = hiltViewModel(),
    navController: NavHostController? = null
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
        // Show skeleton loader while loading
        if (userProfileResource is Resource.Loading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MiningScreenSkeleton()
            }
        } else {
            // Show actual content when data is loaded
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 100.dp), // Bottom padding for safe area
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // User Profile Card at the top with real user data and streaks
                UserProfileCard(
                    userProfile = userProfile,
                    onNavigateToProfile = { 
                        // Navigate to settings screen
                        try {
                            navController?.navigate("settings")
                        } catch (e: Exception) {
                            Log.e("MiningScreen", "Navigation error", e)
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                // Mining Stats - Pass the actual session earnings
                MiningScreenStats(userProfile = userProfile, sessionEarnings = sessionEarnings)

                Spacer(modifier = Modifier.height(16.dp))

                // Image Ads Carousel - Fetch real ads from Appwrite with debugging
                val adsViewModel: AdsViewModel = hiltViewModel()
                val imageAdsResource by adsViewModel.imageAds.collectAsState()
                
                // Load ads when screen appears
                LaunchedEffect(Unit) {
                    adsViewModel.loadImageAds()
                }
                
                ImageAdsCarousel(
                    imageAdsResource = imageAdsResource,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))

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

                // Referral Card (invite/friend buttons only)
                ReferralCard(navController = navController, userProfile = userProfile)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Direct Share Card (separated from ReferralCard)
                DirectShareCard()
                
                Spacer(modifier = Modifier.height(24.dp))
            }
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
    userProfile: UserProfile?,
    onNavigateToProfile: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: User Avatar, Username, and Total Balance
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Avatar (smaller size)
            Box(
                modifier = Modifier
                    .size(36.dp) // Further reduced size
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
                    modifier = Modifier.size(14.dp) // Reduced size
                )
                // Online indicator
                Box(
                    modifier = Modifier
                        .size(8.dp) // Reduced size
                        .align(Alignment.BottomEnd)
                        .offset(x = 1.dp, y = 1.dp) // Adjusted offset
                        .background(Color(0xFF10b981), CircleShape) // Green dot
                )
            }
            Spacer(modifier = Modifier.width(8.dp)) // Reduced spacing
            
            // User Info Section with reduced font sizes
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = userProfile?.username ?: "User Name",
                        color = Color.White,
                        fontSize = 14.sp, // Further reduced from 16.sp
                        fontWeight = FontWeight.Bold,
                        maxLines = 1 // Prevent text overflow
                    )
                    Spacer(modifier = Modifier.width(4.dp)) // Reduced spacing
                    
                    // Verification Badge (smaller)
                    Box(
                        modifier = Modifier
                            .size(14.dp) // Reduced size
                            .background(Color(0xFF3b82f6), CircleShape), // Blue circle
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp) // Reduced size
                                .background(Color.White, CircleShape) // White dot inside
                        )
                    }
                }
                Text(
                    text = "Total Balance",
                    color = Color(0xB3FFFFFF), // Light gray
                    fontSize = 10.sp, // Further reduced from 12.sp
                    maxLines = 1
                )
                Text(
                    text = "%.2f EKH".format(userProfile?.totalCoins ?: 0.0),
                    color = Color(0xFFffa000), // Orange
                    fontSize = 12.sp, // Further reduced from 14.sp
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
        
        // Right side: Streaks and Settings Icon
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stats Section with Streaks (reduced font sizes)
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
                            modifier = Modifier.size(12.dp) // Further reduced size
                        )
                        Spacer(modifier = Modifier.width(2.dp)) // Reduced spacing
                        Text(
                            text = (userProfile?.currentStreak ?: 0).toString(),
                            color = Color.White,
                            fontSize = 12.sp, // Further reduced from 14.sp
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Streak",
                        color = Color(0xB3FFFFFF), // Light gray
                        fontSize = 8.sp, // Further reduced from 10.sp
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(8.dp)) // Further reduced spacing
                
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
                            modifier = Modifier.size(12.dp) // Further reduced size
                        )
                        Spacer(modifier = Modifier.width(2.dp)) // Reduced spacing
                        Text(
                            text = (userProfile?.longestStreak ?: 0).toString(),
                            color = Color.White,
                            fontSize = 12.sp, // Further reduced from 14.sp
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Best",
                        color = Color(0xB3FFFFFF), // Light gray
                        fontSize = 8.sp, // Further reduced from 10.sp
                        maxLines = 1
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp)) // Reduced spacing
            
            // Settings Icon for quick access
            IconButton(
                onClick = onNavigateToProfile,
                modifier = Modifier.size(32.dp) // Reduced size for touch target
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color(0xFFffa000), // Orange color to match theme
                    modifier = Modifier.size(18.dp) // Reduced icon size
                )
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
fun MiningScreenStats(userProfile: UserProfile?, sessionEarnings: Double) {
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
                    value = "%.4f".format(userProfile?.totalCoins ?: 0.0),
                    label = "Total Mined",
                    iconPainter = painterResource(id = R.mipmap.ic_launcher)
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
fun StatCard(value: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector? = null, iconPainter: Painter? = null) {
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
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFFffa000),
                    modifier = Modifier.size(24.dp)
                )
            } else if (iconPainter != null) {
                Image(
                    painter = iconPainter,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
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
fun ReferralCard(navController: NavHostController? = null, userProfile: UserProfile? = null) {
    val context = LocalContext.current
    
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
            
            // Buttons Row (Invite/Friend buttons only)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // View Friends Button
                Button(
                    onClick = { 
                        try {
                            navController?.navigate("friends")
                        } catch (e: Exception) {
                            Log.e("MiningScreen", "Navigation error", e)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8b5cf6)
                    )
                ) {
                    Text(
                        text = "View Friends",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Share Referral Link Button
                Button(
                    onClick = { 
                        // Share referral link outside the app with user's actual referral code
                        val referralCode = userProfile?.referralCode ?: ""
                        val message = if (referralCode.isNotEmpty()) {
                            "Join Ekehi Network and earn EKH tokens! Download the app and use my referral code: $referralCode"
                        } else {
                            "Join Ekehi Network and earn EKH tokens! Download the app and start mining EKH!"
                        }
                        
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, message)
                            type = "text/plain"
                        }
                        
                        val shareIntent = Intent.createChooser(sendIntent, "Share referral link via")
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFffa000)
                    )
                ) {
                    Text(
                        text = "Share",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun DirectShareCard() {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x1AFFFFFF) // 10% opacity white
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = "Share Directly",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Social Media Sharing Icons in one line with actual logos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Facebook
                IconButton(
                    onClick = { 
                        shareToSocialMedia(context, "https://facebook.com/sharer/sharer.php?u=", "Join Ekehi Network and earn EKH tokens! Download the app and use my referral code: REF123456")
                    },
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF1877F2).copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                            .border(
                                width = 1.dp,
                                color = Color(0xFF1877F2),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.facebook),
                            contentDescription = "Facebook",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                
                // Telegram
                IconButton(
                    onClick = { 
                        shareToSocialMedia(context, "https://t.me/share/url?url=&text=", "Join Ekehi Network and earn EKH tokens! Download the app and use my referral code: REF123456")
                    },
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF0088CC).copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                            .border(
                                width = 1.dp,
                                color = Color(0xFF0088CC),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.telegram),
                            contentDescription = "Telegram",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                
                // WhatsApp
                IconButton(
                    onClick = { 
                        shareToSocialMedia(context, "https://api.whatsapp.com/send?text=", "Join Ekehi Network and earn EKH tokens! Download the app and use my referral code: REF123456")
                    },
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF25D366).copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                            .border(
                                width = 1.dp,
                                color = Color(0xFF25D366),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.whatsapp),
                            contentDescription = "WhatsApp",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                
                // Twitter
                IconButton(
                    onClick = { 
                        shareToSocialMedia(context, "https://twitter.com/intent/tweet?text=", "Join Ekehi Network and earn EKH tokens! Download the app and use my referral code: REF123456")
                    },
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF1DA1F2).copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                            .border(
                                width = 1.dp,
                                color = Color(0xFF1DA1F2),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.twitter),
                            contentDescription = "Twitter",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                
                // Messenger
                IconButton(
                    onClick = { 
                        shareToSocialMedia(context, "https://www.facebook.com/dialog/send?app_id=&display=popup&href=", "Join Ekehi Network and earn EKH tokens! Download the app and use my referral code: REF123456")
                    },
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF006AFF).copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                            .border(
                                width = 1.dp,
                                color = Color(0xFF006AFF),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.messenger),
                            contentDescription = "Messenger",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                
                // Discord
                IconButton(
                    onClick = { 
                        shareToSocialMedia(context, "https://discord.com/channels/@me", "Join Ekehi Network and earn EKH tokens! Download the app and use my referral code: REF123456")
                    },
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF5865F2).copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                            .border(
                                width = 1.dp,
                                color = Color(0xFF5865F2),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.discord),
                            contentDescription = "Discord",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                
                // Line
                IconButton(
                    onClick = { 
                        shareToSocialMedia(context, "https://social-plugins.line.me/lineit/share?text=", "Join Ekehi Network and earn EKH tokens! Download the app and use my referral code: REF123456")
                    },
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF00C300).copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                            .border(
                                width = 1.dp,
                                color = Color(0xFF00C300),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.line),
                            contentDescription = "LINE",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

// Helper function for sharing to social media
fun shareToSocialMedia(context: android.content.Context, baseUrl: String, message: String) {
    try {
        val url = baseUrl + android.net.Uri.encode(message)
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        android.util.Log.e("MiningScreen", "Error sharing to social media", e)
        // Fallback to generic share
        val sendIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            putExtra(android.content.Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        val shareIntent = android.content.Intent.createChooser(sendIntent, "Share via")
        context.startActivity(shareIntent)
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
                        // Completed: Show app logo from mipmap
                        Image(
                            painter = painterResource(id = R.mipmap.ic_launcher),
                            contentDescription = "Claim Reward",
                            modifier = Modifier.size(60.dp),
                        )
                    }
                    else -> {
                        // Ready to start: Show construction icon for mining
                        Icon(
                            imageVector = Icons.Default.Construction,
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
                        text = if (isCompleted) "Claim ${"%.2f".format(sessionReward)} EKH" else "Start Mining",
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
