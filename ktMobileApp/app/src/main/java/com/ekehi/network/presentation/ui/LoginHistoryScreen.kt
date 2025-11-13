package com.ekehi.network.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LoginHistoryScreen(
    onNavigateBack: () -> Unit
) {
    // Sample login history data
    val loginHistory = listOf(
        LoginRecord(
            timestamp = System.currentTimeMillis() - 86400000, // 1 day ago
            ipAddress = "192.168.1.100",
            location = "New York, NY",
            status = LoginStatus.SUCCESS
        ),
        LoginRecord(
            timestamp = System.currentTimeMillis() - 172800000, // 2 days ago
            ipAddress = "10.0.0.5",
            location = "Los Angeles, CA",
            status = LoginStatus.SUCCESS
        ),
        LoginRecord(
            timestamp = System.currentTimeMillis() - 259200000, // 3 days ago
            ipAddress = "172.16.0.10",
            location = "Chicago, IL",
            status = LoginStatus.FAILED
        ),
        LoginRecord(
            timestamp = System.currentTimeMillis() - 345600000, // 4 days ago
            ipAddress = "192.168.0.25",
            location = "Houston, TX",
            status = LoginStatus.SUCCESS
        ),
        LoginRecord(
            timestamp = System.currentTimeMillis() - 432000000, // 5 days ago
            ipAddress = "10.10.10.10",
            location = "Phoenix, AZ",
            status = LoginStatus.FAILED
        )
    )
    
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
        ) {
            // Top App Bar
            TopAppBar(
                title = {
                    Text(
                        text = "Login History",
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                item {
                    Text(
                        text = "Review your recent login activity",
                        color = Color(0xB3FFFFFF),
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(bottom = 24.dp)
                            .fillMaxWidth()
                    )
                }
                
                items(loginHistory) { record ->
                    LoginHistoryItem(record)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun LoginHistoryItem(record: LoginRecord) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
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
            // Status Icon
            Icon(
                imageVector = if (record.status == LoginStatus.SUCCESS) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = if (record.status == LoginStatus.SUCCESS) "Success" else "Failed",
                tint = if (record.status == LoginStatus.SUCCESS) Color.Green else Color.Red,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 12.dp)
            )
            
            // Record Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = formatTimestamp(record.timestamp),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = record.ipAddress,
                    color = Color(0xB3FFFFFF),
                    fontSize = 14.sp
                )
                
                Text(
                    text = record.location,
                    color = Color(0xB3FFFFFF),
                    fontSize = 14.sp
                )
            }
            
            // Status Text
            Text(
                text = if (record.status == LoginStatus.SUCCESS) "Success" else "Failed",
                color = if (record.status == LoginStatus.SUCCESS) Color.Green else Color.Red,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

data class LoginRecord(
    val timestamp: Long,
    val ipAddress: String,
    val location: String,
    val status: LoginStatus
)

enum class LoginStatus {
    SUCCESS,
    FAILED
}