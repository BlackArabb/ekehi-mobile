package com.ekehi.mobile.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.ekehi.mobile.data.model.UserProfile
import com.ekehi.mobile.domain.model.Resource
import com.ekehi.mobile.presentation.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToMining: () -> Unit,
    onNavigateToSocial: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToPresale: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val scrollState = rememberScrollState()
    val userProfile by viewModel.userProfile.collectAsState()

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Welcome back!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                    Text(
                        text = userProfile?.username ?: "User",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                
                IconButton(
                    onClick = onNavigateToProfile
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Mining Stats
            ProfileScreenStats() // Using the same stats component

            Spacer(modifier = Modifier.height(24.dp))

            // Ad Bonus Button
            DashboardAdBonusButton()

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Access Section
            QuickAccessSection(
                onNavigateToSocial = onNavigateToSocial,
                onNavigateToLeaderboard = onNavigateToLeaderboard,
                onNavigateToPresale = onNavigateToPresale,
                onNavigateToWallet = onNavigateToWallet,
                onNavigateToProfile = onNavigateToProfile
            )
        }
    }
}

@Composable
fun DashboardAdBonusButton() {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = { /* Handle ad watch */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF8b5cf6)
            ),
            enabled = true // In a real implementation, this would be based on cooldown
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Watch Ad",
                    tint = Color.White
                )
                Text(
                    text = "Watch Ad for +0.5 EKH",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Text(
            text = "Watch a short ad to earn bonus EKH tokens",
            color = Color(0xB3FFFFFF), // 70% opacity white
            fontSize = 12.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun QuickAccessSection(
    onNavigateToSocial: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToPresale: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Column {
        Text(
            text = "Quick Access",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickAccessCard(
                icon = Icons.Default.Share,
                label = "Social",
                iconColor = Color(0xFF3b82f6),
                onClick = onNavigateToSocial
            )

            QuickAccessCard(
                icon = Icons.Default.EmojiEmotions,
                label = "Leaders",
                iconColor = Color(0xFFf59e0b),
                onClick = onNavigateToLeaderboard
            )

            QuickAccessCard(
                icon = Icons.Default.ShoppingCart,
                label = "Presale",
                iconColor = Color(0xFF10b981),
                onClick = onNavigateToPresale
            )

            QuickAccessCard(
                icon = Icons.Default.AccountBalance,
                label = "Wallet",
                iconColor = Color(0xFF8b5cf6),
                onClick = onNavigateToWallet
            )

            QuickAccessCard(
                icon = Icons.Default.Person,
                label = "Profile",
                iconColor = Color(0xFFec4899),
                onClick = onNavigateToProfile
            )
        }
    }
}

@Composable
fun QuickAccessCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(60.dp)
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x1AFFFFFF) // 10% opacity white
        ),
        onClick = onClick
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
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                color = Color.White,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 4.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
