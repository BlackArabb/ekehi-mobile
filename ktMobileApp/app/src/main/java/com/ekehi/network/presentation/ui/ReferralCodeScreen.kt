package com.ekehi.network.presentation.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ekehi.network.data.model.UserProfile
import com.ekehi.network.domain.model.Resource
import com.ekehi.network.presentation.viewmodel.ProfileViewModel

@Composable
fun ReferralCodeScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val userProfileResource by viewModel.userProfile.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    // State for referral code input
    var referralCodeInput by remember { mutableStateOf("") }
    var isClaiming by remember { mutableStateOf(false) }
    var claimMessage by remember { mutableStateOf<String?>(null) }
    var claimError by remember { mutableStateOf<String?>(null) }
    
    // Extract the actual UserProfile from Resource
    val userProfile: UserProfile? = when (userProfileResource) {
        is Resource.Success -> (userProfileResource as Resource.Success<UserProfile>).data
        is Resource.Loading -> null
        else -> null
    }
    
    // Check if we're still loading
    val isLoading = userProfileResource is Resource.Loading
    
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
                        text = "Referral Code",
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
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Referral Icon
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Referral",
                    tint = Color(0xFFffa000),
                    modifier = Modifier
                        .size(80.dp)
                        .padding(bottom = 24.dp)
                )
                
                // Title
                Text(
                    text = "Invite Friends & Earn",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Description
                Text(
                    text = "Share your referral code with friends and earn rewards when they join!",
                    color = Color(0xB3FFFFFF),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(bottom = 32.dp)
                        .fillMaxWidth(0.8f)
                )
                
                // Referral Code Card
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
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Your Referral Code",
                            color = Color(0xB3FFFFFF),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        // Referral Code Display
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color(0xFFffa000),
                                modifier = Modifier
                                    .size(28.dp)
                                    .padding(bottom = 16.dp)
                            )
                        } else {
                            Text(
                                text = userProfile?.referralCode?.takeIf { it.isNotEmpty() } ?: "Not available",
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                        
                        // Copy Button
                        Button(
                            onClick = {
                                // Copy referral code to clipboard
                                val referralCode = userProfile?.referralCode?.takeIf { it.isNotEmpty() }
                                if (referralCode != null) {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Referral Code", referralCode)
                                    clipboard.setPrimaryClip(clip)
                                    
                                    // Show toast
                                    Toast.makeText(context, "Referral code copied to clipboard", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Referral code not available", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFffa000)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = userProfile?.referralCode?.isNotEmpty() ?: false
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 8.dp)
                            )
                            Text(
                                text = "Copy Code",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
                
                // Claim Referral Section
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
                            text = "Claim Referral",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Text(
                            text = "Enter a referral code you received from a friend to get 2 EKH!",
                            color = Color(0xB3FFFFFF),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // Referral Code Input
                        OutlinedTextField(
                            value = referralCodeInput,
                            onValueChange = { referralCodeInput = it },
                            label = { Text("Enter referral code", color = Color(0xB3FFFFFF)) },
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
                        
                        // Claim Button
                        Button(
                            onClick = {
                                if (referralCodeInput.isNotBlank() && userProfile?.userId?.isNotEmpty() == true) {
                                    isClaiming = true
                                    claimMessage = null
                                    claimError = null
                                    
                                    viewModel.claimReferral(userProfile.userId, referralCodeInput)
                                    
                                    // For now, just show a success message
                                    // In a real implementation, you would listen to the result from the ViewModel
                                    referralCodeInput = ""
                                    isClaiming = false
                                    claimMessage = "Referral claimed successfully! You received 2 EKH."
                                } else if (referralCodeInput.isBlank()) {
                                    claimError = "Please enter a referral code"
                                } else {
                                    claimError = "User profile not loaded"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            enabled = !isClaiming && referralCodeInput.isNotBlank() && userProfile?.userId?.isNotEmpty() == true
                        ) {
                            if (isClaiming) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Claim",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .padding(end = 8.dp)
                                )
                            }
                            Text(
                                text = if (isClaiming) "Claiming..." else "Claim 2 EKH",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                        
                        // Show success or error messages
                        claimMessage?.let { message ->
                            Text(
                                text = message,
                                color = Color(0xFF4CAF50),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        claimError?.let { error ->
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                }
                
                // Benefits Section
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
                            text = "Referral Benefits",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        BenefitItem(
                            icon = Icons.Default.CardGiftcard,
                            title = "For You",
                            description = "Earn 2 EKH for each successful referral"
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        BenefitItem(
                            icon = Icons.Default.PersonAdd,
                            title = "For Your Friends",
                            description = "Get a 50 EKH bonus when they sign up using your code"
                        )
                    }
                }
                
                // Share Button
                Button(
                    onClick = {
                        // Share referral code
                        val referralCode = userProfile?.referralCode?.takeIf { it.isNotEmpty() }
                        if (referralCode != null) {
                            val shareText = "Join Ekehi Network and start mining cryptocurrency! Use my referral code: $referralCode\n\nDownload the app now and earn rewards together!\n\n#EkehiNetwork #CryptoMining"
                            
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                putExtra(Intent.EXTRA_TITLE, "Join me on Ekehi Network!")
                                type = "text/plain"
                            }
                            
                            val shareIntent = Intent.createChooser(sendIntent, "Share referral code")
                            context.startActivity(shareIntent)
                        } else {
                            Toast.makeText(context, "Referral code not available", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3b82f6)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = userProfile?.referralCode?.isNotEmpty() ?: false
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 8.dp)
                    )
                    Text(
                        text = "Share with Friends",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun BenefitItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
        Column {
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
    }
}