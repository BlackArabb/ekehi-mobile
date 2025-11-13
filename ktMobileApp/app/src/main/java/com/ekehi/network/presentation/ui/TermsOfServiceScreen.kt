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
fun TermsOfServiceScreen(
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
                        text = "Terms of Service",
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
                
                // Terms Content
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
                        SectionHeader("1. Acceptance of Terms")
                        SectionText(
                            "By accessing or using the Ekehi Network mobile application, you agree to be bound by these Terms of Service and all applicable laws and regulations. If you do not agree with any of these terms, you are prohibited from using or accessing this app."
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        SectionHeader("2. Use License")
                        SectionText(
                            "Permission is granted to temporarily download one copy of the materials (information or software) on Ekehi Network's mobile application for personal, non-commercial transitory viewing only. This is the grant of a license, not a transfer of title."
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        SectionHeader("3. Disclaimer")
                        SectionText(
                            "The materials on Ekehi Network's mobile application are provided on an 'as is' basis. Ekehi Network makes no warranties, expressed or implied, and hereby disclaims and negates all other warranties including, without limitation, implied warranties or conditions of merchantability, fitness for a particular purpose, or non-infringement of intellectual property or other violation of rights."
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        SectionHeader("4. Limitations")
                        SectionText(
                            "In no event shall Ekehi Network or its suppliers be liable for any damages (including, without limitation, damages for loss of data or profit, or due to business interruption) arising out of the use or inability to use the materials on Ekehi Network's mobile application."
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        SectionHeader("5. Revisions and Errata")
                        SectionText(
                            "The materials appearing on Ekehi Network's mobile application could include technical, typographical, or photographic errors. Ekehi Network does not warrant that any of the materials on its mobile application are accurate, complete or current."
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        SectionHeader("6. Governing Law")
                        SectionText(
                            "These terms and conditions are governed by and construed in accordance with the laws of the jurisdiction in which Ekehi Network operates and you irrevocably submit to the exclusive jurisdiction of the courts in that location."
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun SectionText(text: String) {
    Text(
        text = text,
        color = Color(0xB3FFFFFF),
        fontSize = 16.sp,
        lineHeight = 24.sp
    )
}