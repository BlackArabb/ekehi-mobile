package com.ekehi.network.presentation.ui

import com.ekehi.network.domain.model.Resource
import com.ekehi.network.data.model.UserProfile
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.ekehi.network.presentation.viewmodel.ProfileViewModel

@Composable
fun ProfileScreenStats() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatCard(
            value = "12",
            label = "Streak",
            icon = Icons.Default.LocalFireDepartment,
            iconColor = Color(0xFFef4444)
        )

        StatCard(
            value = "5",
            label = "Referrals",
            icon = Icons.Default.People,
            iconColor = Color(0xFF3b82f6)
        )

        StatCard(
            value = "0.0833",
            label = "EKH/hour",
            icon = Icons.Default.TrendingUp,
            iconColor = Color(0xFF10b981)
        )
    }
}

@Composable
fun StatCard(value: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color) {
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
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = value,
                color = Color.White,
                fontSize = 18.sp,
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
                .padding(20.dp)
        ) {
            // Header
            ProfileHeader(
                userProfile = userProfile,
                onNavigateToSettings = onNavigateToSettings
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Mining Stats
            ProfileScreenStats()

            Spacer(modifier = Modifier.height(24.dp))

            // Auto Mining Status
            ProfileAutoMiningStatus()

            Spacer(modifier = Modifier.height(24.dp))

            // Referral Stats
            ReferralStats()

            Spacer(modifier = Modifier.height(24.dp))

            // Achievements
            AchievementsSection()

            Spacer(modifier = Modifier.height(24.dp))

            // Settings Section
            SettingsSection(onNavigateToSettings = onNavigateToSettings)
        }
    }
}

@Composable
fun ProfileHeader(
    userProfile: com.ekehi.network.data.model.UserProfile?,
    onNavigateToSettings: () -> Unit
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
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // User Info
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
                            .background(Color(0xFF3b82f6), shape = androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color.White, shape = androidx.compose.foundation.shape.CircleShape)
                        )
                    }
                }

                Text(
                    text = userProfile?.email ?: "user@example.com",
                    color = Color(0xB3FFFFFF), // 70% opacity white
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Text(
                    text = "Total Balance",
                    color = Color(0xB3FFFFFF), // 70% opacity white
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Text(
                    text = userProfile?.totalCoins?.let { "${String.format("%,.2f", it)} EKH" } ?: "1,250.50 EKH",
                    color = Color(0xFFffa000),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Edit Button
            IconButton(
                onClick = onNavigateToSettings
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile",
                    tint = Color(0xFFffa000)
                )
            }
        }
    }
}

@Composable
fun MiningStats() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatCard(
            value = "12",
            label = "Streak",
            icon = Icons.Default.LocalFireDepartment,
            iconColor = Color(0xFFef4444)
        )

        StatCard(
            value = "5",
            label = "Referrals",
            icon = Icons.Default.People,
            iconColor = Color(0xFF3b82f6)
        )

        StatCard(
            value = "0.0833",
            label = "EKH/hour",
            icon = Icons.Default.TrendingUp,
            iconColor = Color(0xFF10b981)
        )
    }
}

@Composable
fun ProfileAutoMiningStatus() {
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Auto Mining",
                    tint = Color(0xFF10b981),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Auto Mining Status",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Not Active",
                        color = Color(0xFFffa000),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Status",
                        color = Color(0xB3FFFFFF), // 70% opacity white
                        fontSize = 12.sp
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "0.0000 EKH/sec",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Current Rate",
                        color = Color(0xB3FFFFFF), // 70% opacity white
                        fontSize = 12.sp
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$0.00 / $50.00",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Spent / Required",
                        color = Color(0xB3FFFFFF), // 70% opacity white
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ReferralStats() {
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = "Referrals",
                    tint = Color(0xFF3b82f6),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Referral Statistics",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "5",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Total Referrals",
                        color = Color(0xB3FFFFFF), // 70% opacity white
                        fontSize = 12.sp
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "2.5 EKH",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Earned from Referrals",
                        color = Color(0xB3FFFFFF), // 70% opacity white
                        fontSize = 12.sp
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "1.0 EKH",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Per Referral",
                        color = Color(0xB3FFFFFF), // 70% opacity white
                        fontSize = 12.sp
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ekehi://referral/ABC123",
                    color = Color(0xB3FFFFFF), // 70% opacity white
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { /* Handle copy referral link */ }
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = Color(0xFFffa000)
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementsSection() {
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Achievements",
                    tint = Color(0xFFf6ad55),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Achievements",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AchievementBadge(awardCount = "5", category = "Mining Prolificity", label = "5 Mining Attempts Today!")

                AchievementBadge(awardCount = "10", category = "Mining Mastery", label = "10 Mining Sessions Over All Times")
                // Spacer here only appears visible while editing preview content as achievement is smaller than the container
                Spacer(modifier = Modifier.width(8.dp))
                AchievementBadge(awardCount = "3", category = "Mining Efficiency", label = "3 Mining Attempts Within 10 Minutes")
            }
        }
    }
}

@Composable
fun AchievementBadge(awardCount: String, category: String, label: String) {
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
            Text(
                text = awardCount,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = category,
                color = Color(0xB3FFFFFF), // 70% opacity white
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = label,
                color = Color(0xB3FFFFFF), // 70% opacity white
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun SettingsSection(onNavigateToSettings: () -> Unit) {
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
                text = "Account Settings",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingItem(
                text = "Edit Profile",
                icon = Icons.Default.Edit,
                onClick = onNavigateToSettings
            )

            SettingItem(
                text = "Security",
                icon = Icons.Default.Security,
                onClick = { /* Handle security */ }
            )

            SettingItem(
                text = "Notifications",
                icon = Icons.Default.Notifications,
                onClick = { /* Handle notifications */ }
            )

            SettingItem(
                text = "Privacy Policy",
                icon = Icons.Default.Policy,
                onClick = { /* Handle privacy policy */ }
            )

            SettingItem(
                text = "Terms of Service",
                icon = Icons.Default.Description,
                onClick = { /* Handle terms of service */ }
            )

            SettingItem(
                text = "Sign Out",
                icon = Icons.Default.ExitToApp,
                onClick = { /* Handle sign out */ },
                textColor = Color(0xFFef4444) // Red for sign out
            )
        }
    }
}

@Composable
fun SettingItem(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, textColor: Color = Color.White) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = textColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = text,
            color = textColor,
            fontSize = 16.sp,
            modifier = Modifier
                .padding(start = 16.dp)
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