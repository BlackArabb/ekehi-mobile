package com.ekehi.network.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ekehi.network.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class DownloadService : Service() {
    @Inject
    lateinit var apkDownloadManager: ApkDownloadManager

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var downloadJob: Job? = null

    companion object {
        const val CHANNEL_ID = "DownloadServiceChannel"
        const val NOTIFICATION_ID = 2
        const val ACTION_START_DOWNLOAD = "START_DOWNLOAD"
        const val EXTRA_URL = "DOWNLOAD_URL"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_START_DOWNLOAD) {
            val url = intent.getStringExtra(EXTRA_URL) ?: return START_NOT_STICKY
            startDownload(url)
        }
        return START_NOT_STICKY
    }

    private fun startDownload(url: String) {
        startForeground(NOTIFICATION_ID, createNotification("Starting download...", 0))
        
        downloadJob?.cancel()
        downloadJob = serviceScope.launch {
            try {
                apkDownloadManager.downloadAndInstallApk(url).collect { progress ->
                    when (progress) {
                        is DownloadProgress.Downloading -> {
                            updateNotification("Downloading... ${progress.progress}%", progress.progress)
                        }
                        is DownloadProgress.Completed -> {
                            updateNotification("Download complete", 100)
                            stopSelf()
                        }
                        is DownloadProgress.Failed -> {
                            Log.e("DownloadService", "Download failed: ${progress.error}")
                            stopSelf()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DownloadService", "Error during download", e)
                stopSelf()
            }
        }
    }

    private fun updateNotification(content: String, progress: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(content, progress))
    }

    private fun createNotification(content: String, progress: Int): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("App Update")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setProgress(100, progress, progress == 0)

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Download Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
