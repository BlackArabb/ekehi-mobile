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

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit
) {
    val userProfileResource by viewModel.userProfile.collectAsState()
    val scrollState = rememberScrollState()

    // Extract the actual UserProfile from Resource
    val userProfile: UserProfile? = when (userProfileResource) {
        is Resource.Success -> (userProfileResource as Resource.Success<UserProfile>).data
        else -> null
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
                .padding(16.dp)
        ) {
            // Header Section (30% of screen)
            ProfileHeader(
                userProfile = userProfile,
                onNavigateToSettings = onNavigateToSettings
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Stats Section (horizontal cards)
            ProfileStatsSection(userProfile = userProfile)

            Spacer(modifier = Modifier.height(24.dp))

            // Main Content Section (scrollable)
            ProfileContentSection(userProfile = userProfile)

            Spacer(modifier = Modifier.height(24.dp))

            // Actions Section
            ProfileActionsSection(
                onEditProfile = { /* Handle edit profile */ },
                onSettings = onNavigateToSettings,
                onReferralCode = { /* Handle referral code */ },
                onLogout = { /* Handle logout */ }
            )
        }
    }
}

@Composable
fun ProfileHeader(
    userProfile: UserProfile?,
    onNavigateToSettings: () -> Unit
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // User Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
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
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Info
            Text(
                text = userProfile?.username ?: "User Name",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = userProfile?.email ?: "user@example.com",
                color = Color(0xB3FFFFFF), // 70% opacity white
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Join Date
            Text(
                text = "Joined: ${userProfile?.createdAt?.substring(0, 10) ?: "N/A"}",
                color = Color(0xB3FFFFFF), // 70% opacity white
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Verification Badge
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Verified",
                    tint = Color(0xFF3b82f6),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Verified Account",
                    color = Color(0xFF3b82f6),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Edit Profile Button
            Button(
                onClick = onNavigateToSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatCard(
                    value = "%.2f EKH".format(userProfile?.totalCoins ?: 0.0),
                    label = "Total Mined",
                    icon = Icons.Default.AccountBalance,
                    iconColor = Color(0xFFffa000)
                )

                StatCard(
                    value = (userProfile?.totalReferrals ?: 0).toString(),
                    label = "Tasks Completed",
                    icon = Icons.Default.Task,
                    iconColor = Color(0xFF3b82f6)
                )

                StatCard(
                    value = "#${userProfile?.miningPower?.toInt() ?: 0}",
                    label = "Current Rank",
                    icon = Icons.Default.Leaderboard,
                    iconColor = Color(0xFF10b981)
                )

                StatCard(
                    value = (userProfile?.totalReferrals ?: 0).toString(),
                    label = "Referral Count",
                    icon = Icons.Default.People,
                    iconColor = Color(0xFF8b5cf6)
                )
            }
        }
    }
}

@Composable
fun StatCard(value: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color) {
    Card(
        modifier = Modifier
            .width(80.dp)
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x1AFFFFFF) // 10% opacity white
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = value,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = label,
                color = Color(0xB3FFFFFF), // 70% opacity white
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 2.dp)
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
                label = "Email",
                value = userProfile?.email ?: "N/A"
            )

            ProfileDetailItem(
                label = "Phone",
                value = "Not provided"
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
                label = "Best Mining Day",
                value = "%.2f EKH".format(userProfile?.maxDailyEarnings ?: 0.0)
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
            .padding(vertical = 6.dp),
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
    onEditProfile: () -> Unit,
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
                text = "Edit Profile",
                icon = Icons.Default.Edit,
                onClick = onEditProfile
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
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
                .padding(start = 12.dp)
                .weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Navigate",
            tint = Color(0xB3FFFFFF), // 70% opacity white
            modifier = Modifier.size(24.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    EkehiMobileTheme {
        ProfileScreen(
            onNavigateToSettings = {}
        )
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
                totalCoins = 125.50,
                miningPower = 2.5,
                autoMiningRate = 0.005,
                totalReferrals = 3,
                maxDailyEarnings = 12.75,
                createdAt = "2023-01-15T10:30:00Z",
                updatedAt = "2023-01-15T10:30:00Z"
            ),
            onNavigateToSettings = {}
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