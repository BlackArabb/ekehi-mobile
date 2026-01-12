package com.ekehi.network.service

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApkDownloadManager @Inject constructor(
    private val context: Context
) {
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    
    fun downloadAndInstallApk(downloadUrl: String): Flow<DownloadProgress> = callbackFlow {
        val fileName = "ekehi_update_${System.currentTimeMillis()}.apk"
        
        val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
            setTitle("Ekehi App Update")
            setDescription("Downloading latest version...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        }
        
        val downloadId = downloadManager.enqueue(request)
        
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor = downloadManager.query(query)
                    
                    if (cursor.moveToFirst()) {
                        val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        val status = cursor.getInt(statusIndex)
                        
                        when (status) {
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                val downloadUri = downloadManager.getUriForDownloadedFile(downloadId)
                                if (downloadUri != null) {
                                    trySend(DownloadProgress.Completed(downloadUri.toString()))
                                    installApk(downloadUri)
                                } else {
                                    trySend(DownloadProgress.Failed("Could not get download URI"))
                                }
                                close()
                            }
                            DownloadManager.STATUS_FAILED -> {
                                trySend(DownloadProgress.Failed("Download failed"))
                                close()
                            }
                        }
                    }
                    cursor.close()
                }
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(
                receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
        }
        
        // Monitor progress
        var downloading = true
        while (downloading) {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)
            
            if (cursor.moveToFirst()) {
                val bytesDownloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                val bytesTotalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                
                if (bytesDownloadedIndex != -1 && bytesTotalIndex != -1) {
                    val bytesDownloaded = cursor.getLong(bytesDownloadedIndex)
                    val bytesTotal = cursor.getLong(bytesTotalIndex)
                    
                    if (bytesTotal > 0) {
                        val progress = (bytesDownloaded * 100 / bytesTotal).toInt()
                        trySend(DownloadProgress.Downloading(progress))
                    }
                }
                
                if (statusIndex != -1) {
                    val status = cursor.getInt(statusIndex)
                    downloading = status == DownloadManager.STATUS_RUNNING || status == DownloadManager.STATUS_PENDING
                }
            }
            cursor.close()
            
            kotlinx.coroutines.delay(500)
        }
        
        awaitClose {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {
                // Receiver might already be unregistered
            }
        }
    }
    
    private fun installApk(uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("ApkDownload", "Error installing APK", e)
        }
    }
}

sealed class DownloadProgress {
    data class Downloading(val progress: Int) : DownloadProgress()
    data class Completed(val uri: String) : DownloadProgress()
    data class Failed(val error: String) : DownloadProgress()
}
