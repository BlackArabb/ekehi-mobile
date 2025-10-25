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
import com.ekehi.network.presentation.viewmodel.LeaderboardViewModel

@Composable
fun LeaderboardScreen(
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadLeaderboard()
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
                text = "Leaderboard",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 20.dp, bottom = 24.dp)
            )

            // User Rank Card
            UserRankCard()

            Spacer(modifier = Modifier.height(24.dp))

            // Leaderboard List
            LeaderboardList()
        }
    }
}

@Composable
fun UserRankCard() {
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
            // Rank
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color(0xFFffa000),
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "25",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // User Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Your Username",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "1,250 EKH",
                    color = Color(0xFFffa000),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Stats
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "12",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Streak",
                    color = Color(0xB3FFFFFF), // 70% opacity white
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "5",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Referrals",
                    color = Color(0xB3FFFFFF), // 70% opacity white
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun LeaderboardList() {
    // In a real implementation, this would be populated with actual data
    val leaderboardEntries = listOf(
        LeaderboardEntry(
            rank = 1,
            username = "CryptoMinerPro",
            totalCoins = 15000.0,
            miningPower = 10.0,
            streak = 45,
            referrals = 25
        ),
        LeaderboardEntry(
            rank = 2,
            username = "EKHMaster",
            totalCoins = 12500.0,
            miningPower = 8.5,
            streak = 38,
            referrals = 22
        ),
        LeaderboardEntry(
            rank = 3,
            username = "DigitalGold",
            totalCoins = 11000.0,
            miningPower = 7.8,
            streak = 35,
            referrals = 18
        )
    )

    LazyColumn {
        items(leaderboardEntries) { entry ->
            LeaderboardEntryCard(entry = entry)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun LeaderboardEntryCard(entry: LeaderboardEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.rank <= 3) {
                // Gold, Silver, Bronze colors for top 3
                when (entry.rank) {
                    1 -> Color(0xFFFFD700) // Gold
                    2 -> Color(0xFFC0C0C0) // Silver
                    3 -> Color(0xFFCD7F32) // Bronze
                    else -> Color(0x1AFFFFFF) // Default
                }
            } else {
                Color(0x1AFFFFFF) // 10% opacity white
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = when (entry.rank) {
                            1 -> Color(0xFFFFD700) // Gold
                            2 -> Color(0xFFC0C0C0) // Silver
                            3 -> Color(0xFFCD7F32) // Bronze
                            else -> Color(0x33FFFFFF) // Light white
                        },
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = entry.rank.toString(),
                    color = if (entry.rank <= 3) Color.Black else Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Username
            Text(
                text = entry.username,
                color = if (entry.rank <= 3) Color.Black else Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Total Coins
            Text(
                text = "${entry.totalCoins.toInt()}",
                color = if (entry.rank <= 3) Color.Black else Color(0xFFffa000),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Mining Power
            Text(
                text = "${entry.miningPower}",
                color = if (entry.rank <= 3) Color.Black else Color.White,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Streak
            Text(
                text = entry.streak.toString(),
                color = if (entry.rank <= 3) Color.Black else Color.White,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Referrals
            Text(
                text = entry.referrals.toString(),
                color = if (entry.rank <= 3) Color.Black else Color.White,
                fontSize = 12.sp
            )
        }
    }
}

data class LeaderboardEntry(
    val rank: Int,
    val username: String,
    val totalCoins: Double,
    val miningPower: Double,
    val streak: Int,
    val referrals: Int
)