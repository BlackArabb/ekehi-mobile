package com.ekehi.network.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ekehi.network.R
import kotlinx.coroutines.*

class MiningService : Service() {
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isMining = false
    private var miningJob: Job? = null
    
    companion object {
        const val CHANNEL_ID = "MiningServiceChannel"
        const val NOTIFICATION_ID = 1
        const val ACTION_START = "START_MINING"
        const val ACTION_STOP = "STOP_MINING"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startMining()
            ACTION_STOP -> stopMining()
        }
        return START_STICKY // Restart service if killed
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun startMining() {
        if (isMining) return
        
        isMining = true
        startForeground(NOTIFICATION_ID, createNotification())
        
        // Start mining coroutine
        miningJob = coroutineScope.launch {
            while (isMining) {
                // Perform mining logic here
                // This is where you would update mining progress, earnings, etc.
                Log.d("MiningService", "Mining in progress...")
                
                // Update every second or adjust as needed
                delay(1000)
            }
        }
    }
    
    private fun stopMining() {
        isMining = false
        miningJob?.cancel()
        stopForeground(true)
        stopSelf()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Mining Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
    
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Ekehi Mining")
            .setContentText("Mining EKH tokens in background")
            .setSmallIcon(R.drawable.ic_notification)
            .build()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopMining()
        coroutineScope.cancel()
    }
}