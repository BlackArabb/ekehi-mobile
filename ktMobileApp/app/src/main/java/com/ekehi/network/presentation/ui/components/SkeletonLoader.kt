package com.ekehi.network.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SkeletonLoader() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Profile Header Skeleton
        CardSkeleton(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Stats Section Skeleton
        CardSkeleton(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Content Section Skeleton
        CardSkeleton(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Actions Section Skeleton
        CardSkeleton(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    }
}

@Composable
fun CardSkeleton(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x1AFFFFFF))
    )
}

@Composable
fun ProfileHeaderSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar Skeleton
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Color(0x33FFFFFF))
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Username Skeleton
        Box(
            modifier = Modifier
                .width(150.dp)
                .height(26.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0x33FFFFFF))
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Join Date Skeleton
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0x33FFFFFF))
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Verification Badge Skeleton
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color(0x33FFFFFF))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(15.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0x33FFFFFF))
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Edit Profile Button Skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x33FFFFFF))
        )
    }
}

// Skeleton loader for Mining Screen
@Composable
fun MiningScreenSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        // User Profile Card Skeleton
        CardSkeleton(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Mining Stats Skeleton
        CardSkeleton(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Enhanced Mining Button Skeleton
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(Color(0x1AFFFFFF))
                .align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Ad Bonus Button Skeleton
        CardSkeleton(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Auto Mining Status Skeleton
        CardSkeleton(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Referral Card Skeleton
        CardSkeleton(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )
    }
}

// Skeleton loader for Social Tasks Screen
@Composable
fun SocialTasksScreenSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // Header with toggle button skeleton
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0x33FFFFFF))
            )
            
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x1AFFFFFF))
                    .border(
                        width = 1.dp,
                        color = Color(0x33FFFFFF),
                        shape = RoundedCornerShape(12.dp)
                    )
            )
        }
        
        // Stats Section Skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    color = Color(0x0DFFFFFF),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color(0x4DFFA000),
                    shape = RoundedCornerShape(16.dp)
                )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Filter Tabs Skeleton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(4) {
                CardSkeleton(
                    modifier = Modifier
                        .width(70.dp)
                        .height(40.dp)
                )
                if (it < 3) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Social Tasks List Skeleton
        repeat(5) {
            CardSkeleton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0x14FFFFFF),
                                Color(0x08FFFFFF)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0x4DFFA000),
                        shape = RoundedCornerShape(16.dp)
                    )
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Skeleton loader for Leaderboard Screen
@Composable
fun LeaderboardScreenSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        // Header with refresh button skeleton
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
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF))
                    )
                    Box(
                        modifier = Modifier
                            .width(150.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x33FFFFFF))
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x1AFFFFFF))
                        .border(
                            width = 1.dp,
                            color = Color(0x33FFFFFF),
                            shape = RoundedCornerShape(12.dp)
                        )
                )
            }
            
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0x33FFFFFF))
                    .padding(top = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Champion's Throne Section Skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(24.dp))
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
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Global Rankings Header Skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0x0DFFFFFF))
                .border(
                    width = 1.dp,
                    color = Color(0x1AFFFFFF),
                    shape = RoundedCornerShape(20.dp)
                )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Ranked User List Skeleton
        repeat(8) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0x14FFFFFF),
                                Color(0x08FFFFFF)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0x1AFFFFFF),
                        shape = RoundedCornerShape(16.dp)
                    )
            )
            
            if (it < 7) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}