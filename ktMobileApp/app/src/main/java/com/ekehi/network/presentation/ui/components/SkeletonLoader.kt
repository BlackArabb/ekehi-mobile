package com.ekehi.network.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
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
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
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
                .align(androidx.compose.ui.Alignment.CenterHorizontally)
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
        // Header Skeleton
        Box(
            modifier = Modifier
                .width(150.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0x33FFFFFF))
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
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
                    .height(80.dp)
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
            .padding(20.dp)
    ) {
        // Header Skeleton
        Box(
            modifier = Modifier
                .width(200.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0x33FFFFFF))
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Podium Skeleton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 2nd place
            CardSkeleton(
                modifier = Modifier
                    .width(100.dp)
                    .height(120.dp)
            )
            
            // 1st place
            CardSkeleton(
                modifier = Modifier
                    .width(120.dp)
                    .height(140.dp)
            )
            
            // 3rd place
            CardSkeleton(
                modifier = Modifier
                    .width(100.dp)
                    .height(100.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Leaderboard List Skeleton
        repeat(8) {
            CardSkeleton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}