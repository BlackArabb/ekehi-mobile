package com.ekehi.network.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    
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
        ) {
            // Top App Bar
            TopAppBar(
                title = {
                    Text(
                        text = "Privacy Policy",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Last Updated: November 10, 2025",
                    color = Color(0xB3FFFFFF),
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                        .fillMaxWidth()
                )
                
                // Privacy Policy Content
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0x1AFFFFFF) // 10% opacity white
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        SectionHeader("1. Information We Collect")
                        SectionText(
                            "We collect information you provide directly to us, such as when you create an account, participate in mining activities, or contact support. This may include your email address, username, and device information."
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        SectionHeader("2. How We Use Your Information")
                        SectionText(
                            "We use the information we collect to provide, maintain, and improve our services. This includes enabling you to participate in mining activities, communicating with you about your account, and personalizing your experience."
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        SectionHeader("3. Information Sharing and Disclosure")
                        SectionText(
                            "We do not share your personal information with third parties except as necessary to provide our services, comply with legal obligations, or protect our rights and property. We may share anonymous, aggregated data for analytical purposes."
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        SectionHeader("4. Data Security")
                        SectionText(
                            "We implement industry-standard security measures to protect your information. However, no method of transmission over the internet or electronic storage is 100% secure, so we cannot guarantee absolute security."
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        SectionHeader("5. Your Rights")
                        SectionText(
                            "You have the right to access, update, or delete your personal information. You may also opt out of certain data collection activities through your device settings or by contacting our support team."
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        SectionHeader("6. Children's Privacy")
                        SectionText(
                            "Our services are not intended for children under 13. We do not knowingly collect personal information from children under 13. If we become aware that we have collected such information, we will take steps to delete it."
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        SectionHeader("7. Changes to This Policy")
                        SectionText(
                            "We may update this privacy policy from time to time. We will notify you of any changes by posting the new policy on this page and updating the 'Last Updated' date."
                        )
                    }
                }
            }
        }
    }
}