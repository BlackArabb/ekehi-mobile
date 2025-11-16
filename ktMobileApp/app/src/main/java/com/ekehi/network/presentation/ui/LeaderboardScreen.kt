package com.ekehi.network.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.presentation.viewmodel.LeaderboardViewModel
import com.ekehi.network.presentation.ui.components.LeaderboardScreenSkeleton

@Composable
fun LeaderboardScreen(
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    val leaderboardResource by viewModel.leaderboard.collectAsState()
    
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

            // Handle different states
            when (leaderboardResource) {
                is Resource.Loading -> {
                    LeaderboardScreenSkeleton()
                }
                is Resource.Success -> {
                    val leaderboardData = (leaderboardResource as Resource.Success<List<Map<String, Any>>>).data
                    val topUsers = leaderboardData.take(3).map { userMap ->
                        LeaderboardEntry(
                            rank = (userMap["rank"] as Number).toInt(),
                            username = userMap["username"] as String,
                            totalCoins = (userMap["totalCoins"] as Number).toDouble(),
                            miningPower = (userMap["miningPower"] as Number).toDouble(),
                            streak = (userMap["currentStreak"] as Number).toInt(),
                            referrals = (userMap["totalReferrals"] as Number).toInt()
                        )
                    }
                    
                    val remainingUsers = leaderboardData.drop(3).map { userMap ->
                        LeaderboardEntry(
                            rank = (userMap["rank"] as Number).toInt(),
                            username = userMap["username"] as String,
                            totalCoins = (userMap["totalCoins"] as Number).toDouble(),
                            miningPower = (userMap["miningPower"] as Number).toDouble(),
                            streak = (userMap["currentStreak"] as Number).toInt(),
                            referrals = (userMap["totalReferrals"] as Number).toInt()
                        )
                    }

                    // Podium for top 3 users
                    if (topUsers.isNotEmpty()) {
                        LeaderboardPodium(topUsers)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Leaderboard List
                    if (remainingUsers.isNotEmpty()) {
                        LeaderboardList(remainingUsers)
                    }
                }
                is Resource.Error -> {
                    val error = (leaderboardResource as Resource.Error).message
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
                            text = "No data available",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardPodium(topUsers: List<LeaderboardEntry>) {
    // Pad the list to ensure we have at least 3 users
    val paddedUsers = when (topUsers.size) {
        0 -> listOf(
            LeaderboardEntry(1, "No users", 0.0, 0.0, 0, 0),
            LeaderboardEntry(2, "No users", 0.0, 0.0, 0, 0),
            LeaderboardEntry(3, "No users", 0.0, 0.0, 0, 0)
        )
        1 -> listOf(
            topUsers[0],
            LeaderboardEntry(2, "No users", 0.0, 0.0, 0, 0),
            LeaderboardEntry(3, "No users", 0.0, 0.0, 0, 0)
        )
        2 -> listOf(
            topUsers[0],
            topUsers[1],
            LeaderboardEntry(3, "No users", 0.0, 0.0, 0, 0)
        )
        else -> topUsers.take(3)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        // 2nd place (left)
        PodiumItem(
            user = paddedUsers[1],
            modifier = Modifier.weight(1f)
        )

        // 1st place (center, tallest)
        PodiumItem(
            user = paddedUsers[0],
            modifier = Modifier
                .weight(1.2f)
                .padding(bottom = 16.dp)
        )

        // 3rd place (right)
        PodiumItem(
            user = paddedUsers[2],
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun PodiumItem(user: LeaderboardEntry, modifier: Modifier = Modifier) {
    val (backgroundColor, textColor) = when (user.rank) {
        1 -> Color(0xFFFFD700) to Color.Black // Gold
        2 -> Color(0xFFC0C0C0) to Color.Black // Silver
        3 -> Color(0xFFCD7F32) to Color.Black // Bronze
        else -> Color(0x1AFFFFFF) to Color.White
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // User info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Trophy icon
                Icon(
                    imageVector = when (user.rank) {
                        1 -> Icons.Default.Star
                        2 -> Icons.Default.StarHalf
                        3 -> Icons.Default.StarBorder
                        else -> Icons.Default.Person
                    },
                    contentDescription = "Rank ${user.rank}",
                    tint = textColor,
                    modifier = Modifier.size(24.dp)
                )

                // Username
                Text(
                    text = user.username,
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Score
                Text(
                    text = "${user.totalCoins.toInt()} EKH",
                    color = textColor,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Rank badge
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = when (user.rank) {
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
                text = user.rank.toString(),
                color = if (user.rank <= 3) Color.Black else Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
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
fun LeaderboardList(leaderboardEntries: List<LeaderboardEntry>) {
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