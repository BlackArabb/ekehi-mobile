package com.ekehi.network.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    Dialog(
        onDismissRequest = { if (!isMandatory && !isDownloading) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !isMandatory && !isDownloading,
            dismissOnClickOutside = !isMandatory && !isDownloading
        )
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
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
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Version ${config.latestVersion} is now available.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (config.releaseNotes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "What's New:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Text(
                        text = config.releaseNotes,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
                    )
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
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Downloading... ${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (!isMandatory) {
                            TextButton(onClick = onDismiss) {
                                Text("Later")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        
                        Button(
                            onClick = onUpdate,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Update Now")
                        }
                    }
                }
            }
        }
    }
}
