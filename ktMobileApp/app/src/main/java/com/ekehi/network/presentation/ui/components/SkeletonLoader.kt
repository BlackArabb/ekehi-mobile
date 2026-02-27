package com.ekehi.network.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
    ShimmerEffect(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
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
        // Glassmorphism card for TOTAL BALANCE - matching actual UI
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0x33FFFFFF))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // TOTAL BALANCE label
                ShimmerEffect(
                    modifier = Modifier
                        .width(100.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Balance value
                ShimmerEffect(
                    modifier = Modifier
                        .width(80.dp)
                        .height(28.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Username Skeleton
        ShimmerEffect(
            modifier = Modifier
                .width(150.dp)
                .height(22.dp)
                .clip(RoundedCornerShape(4.dp))
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Join Date Skeleton
        ShimmerEffect(
            modifier = Modifier
                .width(120.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Verification Badge Skeleton
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShimmerEffect(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            ShimmerEffect(
                modifier = Modifier
                    .width(100.dp)
                    .height(15.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Edit Profile Button Skeleton
        ShimmerEffect(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(12.dp))
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
        // Updated User Profile Card Skeleton to match the new UI
        UserProfileCardSkeleton()
        
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

@Composable
fun UserProfileCardSkeleton() {
    // Match the structure of the updated UserProfileCard
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: User Avatar and Info
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Avatar Skeleton (36.dp size)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0x33FFFFFF))
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // User Info Section
            Column {
                // Username and Verification Badge Row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x33FFFFFF))
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    // Verification Badge Skeleton (14.dp size)
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF))
                    )
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                // Total Balance Label
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0x33FFFFFF))
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                // Total Balance Value
                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0x33FFFFFF))
                )
            }
        }
        
        // Right side: Streaks and Settings Icon
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stats Section with Streaks
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Current Streak
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Streak Icon and Value Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color(0x33FFFFFF))
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Box(
                            modifier = Modifier
                                .width(10.dp)
                                .height(12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x33FFFFFF))
                        )
                    }
                    // Streak Label
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x33FFFFFF))
                            .padding(top = 2.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Longest Streak
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Best Icon and Value Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color(0x33FFFFFF))
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Box(
                            modifier = Modifier
                                .width(10.dp)
                                .height(12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x33FFFFFF))
                        )
                    }
                    // Best Label
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x33FFFFFF))
                            .padding(top = 2.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Settings Icon Skeleton (32.dp size)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0x33FFFFFF))
            )
        }
    }
}

// Skeleton loader for Social Tasks Screen
@Composable
fun SocialTasksScreenSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        // Header with title and refresh button skeleton - matching actual UI
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Title skeleton
                ShimmerEffect(
                    modifier = Modifier
                        .width(180.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Gradient underline skeleton
                ShimmerEffect(
                    modifier = Modifier
                        .width(80.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                )
            }
            
            // Refresh button skeleton
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x33FFA000))
            )
        }
        
        // EnhancedStatsSection Skeleton - matching actual EnhancedStatsSection
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0x1AFFA000))
                .border(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFffa000),
                            Color(0xFFffb333)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Completed stat skeleton
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ShimmerEffect(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ShimmerEffect(
                        modifier = Modifier
                            .width(50.dp)
                            .height(26.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ShimmerEffect(
                        modifier = Modifier
                            .width(60.dp)
                            .height(13.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Earned stat skeleton
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ShimmerEffect(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ShimmerEffect(
                        modifier = Modifier
                            .width(50.dp)
                            .height(26.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ShimmerEffect(
                        modifier = Modifier
                            .width(40.dp)
                            .height(13.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Social Tasks List Skeleton - matching actual task cards layout
        repeat(5) { index ->
            // Task card skeleton matching EnhancedSocialTaskCard layout
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .shadow(6.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0x14FFFFFF),
                                Color(0x08FFFFFF)
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0x4DFFA000),
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                // Inner shimmer for card content
                ShimmerEffect(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                )
            }
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

// Skeleton loader for Profile Stats Section - matching actual ProfileStatsSection layout
@Composable
fun ProfileStatsSectionSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x1AFFFFFF)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Statistics title skeleton
            ShimmerEffect(
                modifier = Modifier
                    .width(100.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // First row - 2 stat cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCardSkeleton(modifier = Modifier.weight(1f))
                StatCardSkeleton(modifier = Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Second row - 2 stat cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCardSkeleton(modifier = Modifier.weight(1f))
                StatCardSkeleton(modifier = Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Third row - 1 stat card (Referral Count)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCardSkeleton(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun StatCardSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x1AFFFFFF)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon placeholder
            ShimmerEffect(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Value placeholder
            ShimmerEffect(
                modifier = Modifier
                    .width(50.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Label placeholder
            ShimmerEffect(
                modifier = Modifier
                    .width(70.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    }
}

// Skeleton loader for Profile Content Section - matching actual ProfileContentSection layout
@Composable
fun ProfileContentSectionSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x1AFFFFFF)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Account Information title skeleton
            ShimmerEffect(
                modifier = Modifier
                    .width(160.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Detail item 1 - Phone
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ShimmerEffect(
                    modifier = Modifier
                        .width(60.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                ShimmerEffect(
                    modifier = Modifier
                        .width(120.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Detail item 2 - Country
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ShimmerEffect(
                    modifier = Modifier
                        .width(70.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                ShimmerEffect(
                    modifier = Modifier
                        .width(100.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Detail item 3 - Account Created
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ShimmerEffect(
                    modifier = Modifier
                        .width(120.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                ShimmerEffect(
                    modifier = Modifier
                        .width(80.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

// Skeleton loader for Profile Actions Section - matching actual ProfileActionsSection layout
@Composable
fun ProfileActionsSectionSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x1AFFFFFF)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Actions title skeleton
            ShimmerEffect(
                modifier = Modifier
                    .width(80.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action button 1 - Settings
            ActionButtonSkeleton()
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Action button 2 - Referral Code
            ActionButtonSkeleton()
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Action button 3 - Logout
            ActionButtonSkeleton()
        }
    }
}

@Composable
fun ActionButtonSkeleton() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x1AFFFFFF))
    ) {
        ShimmerEffect(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
        )
    }
}