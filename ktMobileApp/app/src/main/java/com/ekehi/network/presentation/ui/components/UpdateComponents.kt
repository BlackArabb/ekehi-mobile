package com.ekehi.network.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.ekehi.network.data.model.UpdateStatus
import com.ekehi.network.presentation.viewmodel.UpdateViewModel
import com.ekehi.network.service.DownloadProgress

@Composable
fun UpdateCheckWrapper(
    viewModel: UpdateViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val updateStatus by viewModel.updateStatus.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val isDownloading by viewModel.isDownloading.collectAsState()

    content()

    when (val status = updateStatus) {
        is UpdateStatus.UpdateAvailable -> {
            UpdateDialog(
                config = status.config,
                isMandatory = status.isMandatory,
                isDownloading = isDownloading,
                downloadProgress = downloadProgress,
                onUpdate = { viewModel.startDownload(status.config.downloadUrl) },
                onDismiss = { if (!status.isMandatory) viewModel.dismissUpdate() }
            )
        }
        else -> {}
    }
}

@Composable
fun UpdateDialog(
    config: com.ekehi.network.data.model.AppVersionConfig,
    isMandatory: Boolean,
    isDownloading: Boolean,
    downloadProgress: DownloadProgress?,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { if (!isMandatory && !isDownloading) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !isMandatory && !isDownloading,
            dismissOnClickOutside = !isMandatory && !isDownloading
        )
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E293B), // Explicit dark background
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isMandatory) "Update Required" else "Update Available",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFA000) // Vibrant orange
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Version ${config.latestVersion} is now available.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White // Explicit white for contrast
                )
                
                if (config.releaseNotes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0x1AFFFFFF),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isExpanded = !isExpanded },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "What's New",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4ECDC4) // Teal color
                            )
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }

                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .heightIn(max = 200.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = config.releaseNotes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                if (isDownloading) {
                    val progress = when (downloadProgress) {
                        is DownloadProgress.Downloading -> downloadProgress.progress / 100f
                        else -> 0f
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp) // Taller progress bar
                                .clip(RoundedCornerShape(6.dp)),
                            color = Color(0xFF10B981), // Bright green
                            trackColor = Color(0x33FFFFFF)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Downloading... ${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (!isMandatory) {
                            TextButton(onClick = onDismiss) {
                                Text("Later", color = Color.White.copy(alpha = 0.7f))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        
                        Button(
                            onClick = onUpdate,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFA000),
                                contentColor = Color.White
                            )
                        ) {
                            Text("Update Now", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
