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
            setAllowedOverRoaming(true)
            setAllowedOverMetered(true)
            setMimeType("application/vnd.android.package-archive")
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
                                Log.d("ApkDownload", "Download successful, starting installation")
                                val downloadUri = downloadManager.getUriForDownloadedFile(downloadId)
                                if (downloadUri != null) {
                                    trySend(DownloadProgress.Completed(downloadUri.toString()))
                                    installApk(downloadId) // Pass downloadId instead of URI
                                } else {
                                    Log.e("ApkDownload", "Could not get download URI for ID: $downloadId")
                                    trySend(DownloadProgress.Failed("Could not get download URI"))
                                }
                                close()
                            }
                            DownloadManager.STATUS_FAILED -> {
                                val reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                                val reason = if (reasonIndex != -1) cursor.getInt(reasonIndex) else -1
                                Log.e("ApkDownload", "Download failed with status: $status, reason: $reason")
                                trySend(DownloadProgress.Failed("Download failed (Error code: $reason)"))
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
    
    private fun installApk(downloadId: Long) {
        try {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)
            
            if (cursor.moveToFirst()) {
                val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val status = if (statusIndex != -1) cursor.getInt(statusIndex) else -1
                
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    val localUriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                    val localUriString = if (localUriIndex != -1) cursor.getString(localUriIndex) else null
                    
                    if (localUriString != null) {
                        val file = File(Uri.parse(localUriString).path!!)
                        
                        if (file.exists()) {
                            Log.d("ApkDownload", "File found: ${file.absolutePath}, size: ${file.length()} bytes")
                            
                            // Check if file is likely an APK (not an HTML error page)
                            if (file.length() < 1024 * 100) { // Less than 100KB is suspicious for an app
                                Log.e("ApkDownload", "File size too small (${file.length()}), possibly an error page instead of APK")
                                return
                            }
                            
                            val contentUri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                            
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(contentUri, "application/vnd.android.package-archive")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            
                            Log.d("ApkDownload", "Starting Package Installer intent with URI: $contentUri")
                            context.startActivity(intent)
                        } else {
                            Log.e("ApkDownload", "File does not exist at path: ${file.absolutePath}")
                            // Fallback to basic URI method if file path mapping failed
                            val downloadUri = downloadManager.getUriForDownloadedFile(downloadId)
                            if (downloadUri != null) fallbackInstall(downloadUri)
                        }
                    } else {
                        Log.e("ApkDownload", "Local URI is null for successful download")
                        val downloadUri = downloadManager.getUriForDownloadedFile(downloadId)
                        if (downloadUri != null) fallbackInstall(downloadUri)
                    }
                }
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e("ApkDownload", "Error during APK installation: ${e.message}", e)
        }
    }

    private fun fallbackInstall(uri: Uri) {
        try {
            Log.d("ApkDownload", "Attempting fallback installation with URI: $uri")
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("ApkDownload", "Fallback installation failed", e)
        }
    }
}

sealed class DownloadProgress {
    data class Downloading(val progress: Int) : DownloadProgress()
    data class Completed(val uri: String) : DownloadProgress()
    data class Failed(val error: String) : DownloadProgress()
}
