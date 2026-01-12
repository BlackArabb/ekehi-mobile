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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
                        val message = (leaderboardResource as Resource.Error).message
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color.Red,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = message,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 16.dp)
                                )
                                Button(
                                    onClick = { viewModel.loadLeaderboard() },
                                    modifier = Modifier.padding(top = 16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000))
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
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
    val sortedEntries = entries.sortedBy { it.rank }
    val first = sortedEntries.find { it.rank == 1 } ?: LeaderboardEntry(1, "No user", 0.0, 0.0, 0, 0)
    val second = sortedEntries.find { it.rank == 2 } ?: LeaderboardEntry(2, "No user", 0.0, 0.0, 0, 0)
    val third = sortedEntries.find { it.rank == 3 } ?: LeaderboardEntry(3, "No user", 0.0, 0.0, 0, 0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Section Header with glow effect
        Text(
            text = "HALL OF LEGENDS",
            color = Color(0xFFFFD700),
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 4.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Second Place
            PodiumMember(
                entry = second,
                height = 160.dp,
                color = Color(0xFFC0C0C0),
                modifier = Modifier.weight(1f),
                rank = "2"
            )

            // First Place
            PodiumMember(
                entry = first,
                height = 220.dp,
                color = Color(0xFFFFD700),
                isFirst = true,
                modifier = Modifier.weight(1.2f),
                rank = "1"
            )

            // Third Place
            PodiumMember(
                entry = third,
                height = 140.dp,
                color = Color(0xFFCD7F32),
                modifier = Modifier.weight(1f),
                rank = "3"
            )
        }
    }
}

@Composable
fun PodiumMember(
    entry: LeaderboardEntry,
    height: androidx.compose.ui.unit.Dp,
    color: Color,
    modifier: Modifier = Modifier,
    isFirst: Boolean = false,
    rank: String
) {
    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Avatar / Badge Area
        Box(contentAlignment = Alignment.TopCenter) {
            if (isFirst) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier
                        .size(32.dp)
                        .offset(y = (-28).dp)
                )
            }

            // Glass Circle for User
            Box(
                modifier = Modifier
                    .size(if (isFirst) 80.dp else 64.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(color.copy(alpha = 0.2f), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
                    .border(
                        width = if (isFirst) 3.dp else 2.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(color, color.copy(alpha = 0.1f))
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = entry.username.take(1).uppercase(),
                    color = color,
                    fontSize = if (isFirst) 32.sp else 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Small Rank Badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 10.dp)
                    .size(24.dp)
                    .background(color, CircleShape)
                    .border(1.dp, Color(0xFF0F172A), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = rank,
                    color = if (isFirst) Color.Black else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Username
        Text(
            text = entry.username,
            color = Color.White,
            fontSize = if (isFirst) 15.sp else 13.sp,
            fontWeight = if (isFirst) FontWeight.Bold else FontWeight.SemiBold,
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        // Balance
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = entry.totalCoins.toInt().toString(),
                color = if (isFirst) Color(0xFFFFD700) else Color(0xFFFFA000),
                fontSize = if (isFirst) 18.sp else 16.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "EKH",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Modern Podium Pillar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.25f),
                            color.copy(alpha = 0.05f)
                        )
                    ),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(color.copy(alpha = 0.5f), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
        )
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