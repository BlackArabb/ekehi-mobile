package com.ekehi.network.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.presentation.viewmodel.LeaderboardViewModel
import com.ekehi.network.presentation.ui.components.LeaderboardScreenSkeleton

// Data class for leaderboard entries
data class LeaderboardEntry(
    val rank: Int,
    val username: String,
    val totalCoins: Double,
    val miningPower: Double,
    val streak: Int,
    val referrals: Int
)

@Composable
fun LeaderboardScreen(
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    val leaderboardResource by viewModel.leaderboard.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadLeaderboard()
    }

    // Using the dark theme background to match React Native design
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E293B),
                        Color(0xFF334155)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Header with refresh button
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Leaderboard",
                                tint = Color(0xFFFFA000),
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "LEADERBOARD",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        }
                        
                        IconButton(
                            onClick = { viewModel.loadLeaderboard() },
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    color = Color(0x1AFFFFFF),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color(0x33FFFFFF),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    Text(
                        text = "Elite miners of the Ekehi Network",
                        color = Color(0xB3FFFFFF),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            
            // Champion's Throne - Top 3
            item {
                // Handle different states
                when (leaderboardResource) {
                    is Resource.Loading -> {
                        LeaderboardScreenSkeleton()
                    }
                    is Resource.Success -> {
                        val leaderboardData = (leaderboardResource as Resource.Success<List<Map<String, Any>>>).data
                        // Get the top 3 users
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
                        
                        if (topUsers.isNotEmpty()) {
                            ChampionThroneSection(topUsers)
                        }
                    }
                    is Resource.Error -> {
                        // Error handling
                    }
                    else -> {
                        // Idle state
                    }
                }
            }
            
            // Global Rankings
            item {
                Text(
                    text = "GLOBAL RANKINGS",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .background(
                            color = Color(0x0DFFFFFF),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0x1AFFFFFF),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(vertical = 12.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            
            // Ranked User List
            when (leaderboardResource) {
                is Resource.Loading -> {
                    // Loading state already handled above
                }
                is Resource.Success -> {
                    val leaderboardData = (leaderboardResource as Resource.Success<List<Map<String, Any>>>).data
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
                    
                    items(remainingUsers) { entry ->
                        LeaderboardEntryItem(entry)
                    }
                }
                is Resource.Error -> {
                    // Error handling
                }
                else -> {
                    // Idle state
                }
            }
        }
    }
}

@Composable
fun ChampionThroneSection(entries: List<LeaderboardEntry>) {
    // Ensure we have exactly 3 entries, padding with placeholders if needed
    val sortedEntries = entries.sortedBy { it.rank }
    val firstPlace = sortedEntries.find { it.rank == 1 }
    val secondPlace = sortedEntries.find { it.rank == 2 }
    val thirdPlace = sortedEntries.find { it.rank == 3 }
    
    // Create placeholders for missing ranks
    val first = firstPlace ?: LeaderboardEntry(1, "No user", 0.0, 0.0, 0, 0)
    val second = secondPlace ?: LeaderboardEntry(2, "No user", 0.0, 0.0, 0, 0)
    val third = thirdPlace ?: LeaderboardEntry(3, "No user", 0.0, 0.0, 0, 0)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x1AFFFF00),
                        Color(0x0DFFFF00)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0x4DFFFF00),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "HALL OF LEGENDS",
                color = Color(0xFFFFD700),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Podium arrangement
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // Second Place (left)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .scale(0.95f)
                ) {
                    // Rank badge
                    Box(
                        modifier = Modifier
                            .size(65.dp)
                            .background(
                                color = Color(0x1AC0C0C0),
                                shape = CircleShape
                            )
                            .border(
                                width = 2.dp,
                                color = Color(0x80C0C0C0),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "2",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Text(
                        text = second.username,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp),
                        maxLines = 1
                    )
                    
                    Text(
                        text = "${second.totalCoins.toInt()}",
                        color = Color(0xFFFFA000),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    Text(
                        text = "EKH",
                        color = Color(0xB3FFFFFF),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                
                // First Place (center)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .scale(1.1f)
                ) {
                    // Rank badge
                    Box(
                        modifier = Modifier
                            .size(75.dp)
                            .background(
                                color = Color(0x1AFFFF00),
                                shape = CircleShape
                            )
                            .border(
                                width = 2.dp,
                                color = Color(0x80FFFF00),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "1",
                            color = Color(0xFFFFD700),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Text(
                        text = "CHAMPION",
                        color = Color(0xFFFFD700),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    Text(
                        text = first.username,
                        color = Color(0xFFFFD700),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 1
                    )
                    
                    Text(
                        text = "${first.totalCoins.toInt()}",
                        color = Color(0xFFFFD700),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    Text(
                        text = "EKH",
                        color = Color(0xB3FFFFFF),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                
                // Third Place (right)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .scale(0.95f)
                ) {
                    // Rank badge
                    Box(
                        modifier = Modifier
                            .size(65.dp)
                            .background(
                                color = Color(0x1ACD7F32),
                                shape = CircleShape
                            )
                            .border(
                                width = 2.dp,
                                color = Color(0x80CD7F32),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "3",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Text(
                        text = third.username,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp),
                        maxLines = 1
                    )
                    
                    Text(
                        text = "${third.totalCoins.toInt()}",
                        color = Color(0xFFFFA000),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    Text(
                        text = "EKH",
                        color = Color(0xB3FFFFFF),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LeaderboardEntryItem(entry: LeaderboardEntry) {
    val badgeStyles = getCraftRankBadgeStyles(entry.rank)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(badgeStyles.colors),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = badgeStyles.borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left section - Rank & User
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Rank badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = Color(0x1A000000),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = badgeStyles.borderColor,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = entry.rank.toString(),
                        color = Color.White,
                        fontSize = if (entry.rank <= 10) 14.sp else 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Decorative gem
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-2).dp, y = 12.dp)
                            .background(
                                color = Color(0xFF4ECDC4),
                                shape = CircleShape
                            )
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // User details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = entry.username,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Power: ${entry.miningPower}",
                            color = Color(0xB3FFFFFF),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Streak: ${entry.streak}",
                            color = Color(0xB3FFFFFF),
                            fontSize = 12.sp
                        )
                    }
                }
            }
            
            // Right section - Earnings
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = entry.totalCoins.toInt().toString(),
                    color = Color(0xFFFFA000),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "EKH",
                    color = Color(0xB3FFFFFF),
                    fontSize = 12.sp
                )
            }
        }
    }
}

data class BadgeStyles(
    val colors: List<Color>,
    val borderColor: Color,
    val shadowColor: Color
)

fun getCraftRankBadgeStyles(rank: Int): BadgeStyles {
    return when (rank) {
        1 -> BadgeStyles(
            colors = listOf(Color(0x26FFFF00), Color(0x0DFFFF00)),
            borderColor = Color(0x4DFFFF00),
            shadowColor = Color(0xFFFFD700)
        )
        2 -> BadgeStyles(
            colors = listOf(Color(0x26C0C0C0), Color(0x0DC0C0C0)),
            borderColor = Color(0x4DC0C0C0),
            shadowColor = Color(0xFFC0C0C0)
        )
        3 -> BadgeStyles(
            colors = listOf(Color(0x26CD7F32), Color(0x0DCD7F32)),
            borderColor = Color(0x4DCD7F32),
            shadowColor = Color(0xFFCD7F32)
        )
        else -> BadgeStyles(
            colors = listOf(Color(0x14FFFFFF), Color(0x08FFFFFF)),
            borderColor = Color(0x33FFFFFF),
            shadowColor = Color(0xFF000000)
        )
    }
}