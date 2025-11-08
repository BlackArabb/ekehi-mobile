package com.ekehi.network.presentation.ui

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ContactSupportScreen(
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    // Form state
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
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
                        text = "Contact Support",
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
                // Description
                Text(
                    text = "Need help? Our support team is here to assist you.",
                    color = Color(0xB3FFFFFF),
                    fontSize = 16.sp,
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                        .fillMaxWidth()
                )
                
                // Contact Methods Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0x1AFFFFFF) // 10% opacity white
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Contact Methods",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        ContactMethodItem(
                            icon = Icons.Default.Email,
                            title = "Email Support",
                            description = "support@ekehi.network",
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:support@ekehi.network")
                                        putExtra(Intent.EXTRA_SUBJECT, "Ekehi Network Support Request")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Log.e("ContactSupport", "Failed to open email app", e)
                                    Toast.makeText(context, "Unable to open email app", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        ContactMethodItem(
                            icon = Icons.Default.Language,
                            title = "Visit Website",
                            description = "www.ekehi.network",
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.ekehi.network"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Log.e("ContactSupport", "Failed to open website", e)
                                    Toast.makeText(context, "Unable to open website", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        ContactMethodItem(
                            icon = Icons.Default.QuestionAnswer,
                            title = "FAQ",
                            description = "View frequently asked questions",
                            onClick = {
                                // In a real app, you would navigate to an FAQ screen
                                Toast.makeText(context, "FAQ section would open here", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
                
                // Send Message Form
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0x1AFFFFFF) // 10% opacity white
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Send Us a Message",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // Subject Field
                        OutlinedTextField(
                            value = subject,
                            onValueChange = { 
                                subject = it
                                // Clear error when user starts typing
                                if (errorMessage != null) {
                                    errorMessage = null
                                }
                            },
                            label = { 
                                Text(
                                    text = "Subject",
                                    color = Color(0xB3FFFFFF)
                                )
                            },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFffa000),
                                unfocusedBorderColor = Color(0x33FFFFFF),
                                cursorColor = Color(0xFFffa000),
                                focusedLabelColor = Color(0xFFffa000),
                                unfocusedLabelColor = Color(0xB3FFFFFF)
                            )
                        )
                        
                        // Message Field
                        OutlinedTextField(
                            value = message,
                            onValueChange = { 
                                message = it
                                // Clear error when user starts typing
                                if (errorMessage != null) {
                                    errorMessage = null
                                }
                            },
                            label = { 
                                Text(
                                    text = "Message",
                                    color = Color(0xB3FFFFFF)
                                )
                            },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .padding(bottom = 16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFffa000),
                                unfocusedBorderColor = Color(0x33FFFFFF),
                                cursorColor = Color(0xFFffa000),
                                focusedLabelColor = Color(0xFFffa000),
                                unfocusedLabelColor = Color(0xB3FFFFFF)
                            )
                        )
                        
                        // Send Button
                        Button(
                            onClick = {
                                // Validate form
                                if (subject.isEmpty()) {
                                    errorMessage = "Please enter a subject"
                                    return@Button
                                }
                                
                                if (message.isEmpty()) {
                                    errorMessage = "Please enter a message"
                                    return@Button
                                }
                                
                                // In a real implementation, you would call the API to send the message
                                // For now, we'll just show a success message
                                isLoading = true
                                errorMessage = null
                                
                                // Simulate API call
                                // In a real app, you would make an actual API call here
                                // For demonstration, we'll just show a success message after a delay
                                android.os.Handler().postDelayed({
                                    isLoading = false
                                    Toast.makeText(context, "Message sent successfully", Toast.LENGTH_SHORT).show()
                                    // Clear form
                                    subject = ""
                                    message = ""
                                }, 1000)
                            },
                            enabled = !isLoading && subject.isNotEmpty() && message.isNotEmpty(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFffa000),
                                disabledContainerColor = Color(0x33FFFFFF)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    text = "Send Message",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                
                // Error Message
                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ContactMethodItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Color(0xFFffa000),
            modifier = Modifier
                .size(24.dp)
                .padding(end = 12.dp)
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                color = Color(0xB3FFFFFF),
                fontSize = 14.sp
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Navigate",
            tint = Color(0xB3FFFFFF),
            modifier = Modifier.size(24.dp)
        )
    }
}