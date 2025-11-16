package com.ekehi.network.presentation.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
            .padding(16.dp)
            .background(Color(0xFF1E1B2E)) // Dark purple background
    ) {
        // Header Skeleton
        Box(
            modifier = Modifier
                .width(200.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0x33FFFFFF))
                .padding(top = 20.dp, bottom = 24.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Top Three Users Section Skeleton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 2nd place skeleton
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile image skeleton
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Username skeleton
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0x33FFFFFF))
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Rank skeleton
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0x33FFFFFF))
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 1st place skeleton
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile image skeleton (larger for 1st place)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Crown skeleton
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Username skeleton
                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0x33FFFFFF))
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Rank skeleton
                Box(
                    modifier = Modifier
                        .width(50.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0x33FFFFFF))
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 3rd place skeleton
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile image skeleton
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Username skeleton
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0x33FFFFFF))
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Rank skeleton
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0x33FFFFFF))
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // User Rank Banner Skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF6A4FCF)) // Purple banner color
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Ranked User List Skeleton
        repeat(8) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile image skeleton
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFffa000)) // Orange color for profile
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Username and rank skeleton
                Column {
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x33FFFFFF))
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x33FFFFFF))
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Rank change indicator skeleton
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                )
            }
            
            if (it < 7) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}