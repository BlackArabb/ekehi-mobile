package com.ekehi.network.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.presentation.viewmodel.LeaderboardViewModel
import com.ekehi.network.presentation.ui.components.LeaderboardScreenSkeleton

// Updated User data class to include profile image URL and rank change
// This would need to be updated in the actual data model
/*
data class User(
    val rank: Int,
    val name: String,
    val profileImageUrl: String,
    val rankChange: Int // positive for upward movement, negative for downward
)
*/

@Composable
fun LeaderboardScreen(
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    val leaderboardResource by viewModel.leaderboard.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadLeaderboard()
    }

    // Using the dark purple background as requested
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1B2E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
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
                    
                    // Get the current user's rank (assuming it's in the data)
                    // In a real implementation, this would come from the authenticated user's data
                    val currentUserRank = leaderboardData.find { userMap ->
                        // This is a placeholder - in a real app, you would match against the current user ID
                        // For now, we'll just use the first user's rank as an example
                        true
                    }?.get("rank") as? Number ?: 0
                    
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

                    // Top Three Users Section
                    if (topUsers.isNotEmpty()) {
                        TopThreeUsersSection(topUsers)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // User Rank Banner - now showing actual user rank
                    UserRankBanner(currentUserRank.toInt())

                    Spacer(modifier = Modifier.height(16.dp))

                    // Ranked User List
                    if (remainingUsers.isNotEmpty()) {
                        RankedUserList(remainingUsers)
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

/**
 * Top 3 Users Section
 */
@Composable
fun TopThreeUsersSection(entries: List<LeaderboardEntry>) {
    // Ensure we have exactly 3 entries, padding with placeholders if needed
    val sortedEntries = entries.sortedBy { it.rank }
    val firstPlace = sortedEntries.find { it.rank == 1 }
    val secondPlace = sortedEntries.find { it.rank == 2 }
    val thirdPlace = sortedEntries.find { it.rank == 3 }
    
    // Create placeholders for missing ranks
    val first = firstPlace ?: LeaderboardEntry(1, "No user", 0.0, 0.0, 0, 0)
    val second = secondPlace ?: LeaderboardEntry(2, "No user", 0.0, 0.0, 0, 0)
    val third = thirdPlace ?: LeaderboardEntry(3, "No user", 0.0, 0.0, 0, 0)
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Second place (left)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Placeholder for profile image since we don't have actual image URLs
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFC0C0C0)), // Silver for 2nd
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            Text(
                second.username,
                color = Color.White,
                fontSize = 14.sp
            )
            Text(
                "Rank ${second.rank}",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // First place (center)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Placeholder for profile image since we don't have actual image URLs
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFD700)), // Gold for 1st
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            // Crown for 1st place
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = "Crown",
                tint = Color.Yellow,
                modifier = Modifier.size(24.dp)
            )

            Text(
                first.username,
                color = Color.White,
                fontSize = 14.sp
            )
            Text(
                "Rank ${first.rank}",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Third place (right)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Placeholder for profile image since we don't have actual image URLs
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFCD7F32)), // Bronze for 3rd
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            Text(
                third.username,
                color = Color.White,
                fontSize = 14.sp
            )
            Text(
                "Rank ${third.rank}",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

/**
 * User Rank Banner
 */
@Composable
fun UserRankBanner(rank: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF6A4FCF), shape = RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "Your Current Rank: $rank",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

/**
 * Ranked User List
 */
@Composable
fun RankedUserList(entries: List<LeaderboardEntry>) {
    LazyColumn {
        items(entries) { entry ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Placeholder for profile image
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFffa000)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        entry.username,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        "Rank ${entry.rank}",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Placeholder for rank change indicator
                // In a real implementation, this would show actual rank change data
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Rank Change",
                    tint = Color.Red,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// Keeping the existing data class for compatibility with the rest of the app
// In a full implementation, we would update this to match the new requirements
// with profile image URL and rank change fields
data class LeaderboardEntry(
    val rank: Int,
    val username: String,
    val totalCoins: Double,
    val miningPower: Double,
    val streak: Int,
    val referrals: Int
)