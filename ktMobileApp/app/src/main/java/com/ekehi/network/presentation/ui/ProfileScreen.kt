package com.ekehi.network.presentation.ui

import com.ekehi.network.domain.model.Resource
import com.ekehi.network.data.model.UserProfile
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.ekehi.network.presentation.viewmodel.ProfileViewModel
import com.ekehi.network.ui.theme.EkehiMobileTheme
import com.ekehi.network.util.EventBus
import com.ekehi.network.util.Event
import kotlinx.coroutines.flow.collect
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import com.ekehi.network.service.StartIoService
import com.ekehi.network.presentation.ui.components.ProfileHeaderSkeleton
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener
import com.startapp.sdk.adsbase.Ad
import com.ekehi.network.di.StartIoServiceEntryPoint

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToReferralCode: () -> Unit,
    onLogout: () -> Unit
) {
    val userProfileResource by viewModel.userProfile.collectAsState()
    val scrollState = rememberScrollState()
    
    // Get StartIoService through DI
    val context = LocalContext.current
    val startIoService = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            StartIoServiceEntryPoint::class.java
        ).startIoService()
    }
    
    // Initialize StartIoService
    LaunchedEffect(Unit) {
        startIoService.initialize()
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
                    .padding(16.dp)
            ) {
                ProfileHeaderSkeleton()
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Stats Section Skeleton
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0x1AFFFFFF)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x1AFFFFFF))
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Content Section Skeleton
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0x1AFFFFFF)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x1AFFFFFF))
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Actions Section Skeleton
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0x1AFFFFFF)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x1AFFFFFF))
                    )
                }
            }
        } else {
            // Show actual content when data is loaded
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                // Extract the actual UserProfile from Resource
                val userProfile: UserProfile? = when (userProfileResource) {
                    is Resource.Success -> (userProfileResource as Resource.Success<UserProfile>).data
                    else -> null
                }
                
                // Get current user ID
                val currentUserId = userProfile?.userId?.takeIf { it.isNotEmpty() } ?: "user_id_placeholder"
                
                // Listen for profile refresh events
                LaunchedEffect(Unit) {
                    EventBus.events.collect { event ->
                        when (event) {
                            is Event.RefreshUserProfile -> {
                                Log.d("ProfileScreen", "Received RefreshUserProfile event")
                                if (currentUserId.isNotEmpty() && currentUserId != "user_id_placeholder") {
                                    viewModel.refreshUserProfile()
                                }
                            }
                            else -> {}
                        }
                    }
                }
                
                ProfileHeader(
                    userProfile = userProfile,
                    onNavigateToEditProfile = onNavigateToEditProfile
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                ProfileStatsSection(userProfile = userProfile)
                Spacer(modifier = Modifier.height(24.dp))
                ProfileContentSection(userProfile = userProfile)
                Spacer(modifier = Modifier.height(24.dp))
                
                // Actions Section
                ProfileActionsSection(
                    onSettings = onNavigateToSettings,
                    onReferralCode = onNavigateToReferralCode,
                    onLogout = onLogout
                )
            }
        }
    }
}

@Composable
fun ProfileHeader(
    userProfile: UserProfile?,
    onNavigateToEditProfile: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x1AFFFFFF) // 10% opacity white
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // User Avatar
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF8b5cf6),
                                Color(0xFFa855f7)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(45.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // User Info
            Text(
                text = userProfile?.username ?: "User Name",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Join Date
            Text(
                text = "Joined: ${userProfile?.createdAt?.substring(0, 10) ?: "N/A"}",
                color = Color(0xB3FFFFFF), // 70% opacity white
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Verification Badge
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Verified",
                    tint = Color(0xFF3b82f6),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Verified Account",
                    color = Color(0xFF3b82f6),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Edit Profile Button
            Button(
                onClick = onNavigateToEditProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFffa000)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Edit Profile",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ProfileStatsSection(userProfile: UserProfile?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x1AFFFFFF) // 10% opacity white
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Statistics",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 2 rows 2 columns layout
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // First row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        value = "%.2f EKH".format(userProfile?.totalCoins ?: 0.0),
                        label = "Total Mined",
                        icon = Icons.Default.AccountBalance,
                        iconColor = Color(0xFFffa000),
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        value = (userProfile?.totalReferrals ?: 0).toString(),
                        label = "Tasks Completed",
                        icon = Icons.Default.Task,
                        iconColor = Color(0xFF3b82f6),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Second row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        value = "#${userProfile?.miningPower?.toInt() ?: 0}",
                        label = "Current Rank",
                        icon = Icons.Default.Leaderboard,
                        iconColor = Color(0xFF10b981),
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        value = (userProfile?.totalReferrals ?: 0).toString(),
                        label = "Referral Count",
                        icon = Icons.Default.People,
                        iconColor = Color(0xFF8b5cf6),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(value: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f), // Make it square
        colors = CardDefaults.cardColors(
            containerColor = Color(0x1AFFFFFF) // 10% opacity white
        ),
        shape = RoundedCornerShape(12.dp)
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
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = value,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = label,
                color = Color(0xB3FFFFFF), // 70% opacity white
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun ProfileContentSection(userProfile: UserProfile?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x1AFFFFFF) // 10% opacity white
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Account Information
            Text(
                text = "Account Information",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            ProfileDetailItem(
                label = "Phone",
                value = userProfile?.phone_number?.toString() ?: ""
            )

            ProfileDetailItem(
                label = "Country",
                value = userProfile?.country ?: ""
            )

            ProfileDetailItem(
                label = "Account Created",
                value = userProfile?.createdAt?.substring(0, 10) ?: "N/A"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mining Statistics
            Text(
                text = "Mining Statistics",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            ProfileDetailItem(
                label = "Total Mined",
                value = "%.2f EKH".format(userProfile?.totalCoins ?: 0.0)
            )

            ProfileDetailItem(
                label = "Mining Rate",
                value = "%.4f EKH/hour".format(userProfile?.autoMiningRate ?: 0.0)
            )

            ProfileDetailItem(
                label = "Referral Bonus",
                value = "%.4f EKH/hour".format(userProfile?.referralBonusRate ?: 0.0)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Activity
            Text(
                text = "Recent Activity",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Placeholder for recent mining sessions
            Text(
                text = "No recent mining sessions",
                color = Color(0xB3FFFFFF),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Placeholder for recent tasks
            Text(
                text = "No recent tasks completed",
                color = Color(0xB3FFFFFF),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun ProfileDetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color(0xB3FFFFFF), // 70% opacity white
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ProfileActionsSection(
    onSettings: () -> Unit,
    onReferralCode: () -> Unit,
    onLogout: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x1AFFFFFF) // 10% opacity white
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Actions",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ActionButton(
                text = "Settings",
                icon = Icons.Default.Settings,
                onClick = onSettings
            )

            ActionButton(
                text = "Referral Code",
                icon = Icons.Default.Share,
                onClick = onReferralCode
            )

            Spacer(modifier = Modifier.height(8.dp))

            ActionButton(
                text = "Logout",
                icon = Icons.Default.Logout,
                onClick = onLogout,
                isDanger = true
            )
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    isDanger: Boolean = false
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0x1AFFFFFF), // 10% opacity white
        onClick = {
            Log.d("ActionButton", "Button clicked: $text")
            onClick()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = if (isDanger) Color(0xFFef4444) else Color(0xFFffa000),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = text,
                color = if (isDanger) Color(0xFFef4444) else Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = Color(0xB3FFFFFF),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileHeaderPreview() {
    EkehiMobileTheme {
        ProfileHeader(
            userProfile = UserProfile(
                id = "1",
                userId = "user1",
                username = "John Doe",
                email = "john@example.com",
                phone_number = "1234567890",
                country = "United States",
                totalCoins = 125.50,
                miningPower = 2.5,
                autoMiningRate = 0.005,
                totalReferrals = 3,
                maxDailyEarnings = 12.75,
                createdAt = "2023-01-15T10:30:00Z",
                updatedAt = "2023-01-15T10:30:00Z"
            ),
            onNavigateToEditProfile = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileStatsSectionPreview() {
    EkehiMobileTheme {
        ProfileStatsSection(
            userProfile = UserProfile(
                id = "1",
                userId = "user1",
                username = "John Doe",
                email = "john@example.com",
                totalCoins = 125.50,
                miningPower = 2.5,
                autoMiningRate = 0.005,
                totalReferrals = 3,
                maxDailyEarnings = 12.75,
                createdAt = "2023-01-15T10:30:00Z",
                updatedAt = "2023-01-15T10:30:00Z"
            )
        )
    }
}

// Remove the duplicate StartIoServiceEntryPoint interface as it's now in a separate file
// The interface is now located in com.ekehi.network.di.StartIoServiceEntryPoint
