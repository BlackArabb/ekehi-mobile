package com.ekehi.network.service

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MiningManager @Inject constructor(
    private val context: Context
) : LifecycleObserver {
    
    private var isServiceStarted = false
    
    init {
        // Register for lifecycle events
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }
    
    fun startMining() {
        if (isServiceStarted) return
        
        try {
            val intent = Intent(context, MiningService::class.java).apply {
                action = MiningService.ACTION_START
            }
            context.startService(intent)
            isServiceStarted = true
            Log.d("MiningManager", "Mining service started")
        } catch (e: Exception) {
            Log.e("MiningManager", "Failed to start mining service", e)
        }
    }
    
    fun stopMining() {
        if (!isServiceStarted) return
        
        try {
            val intent = Intent(context, MiningService::class.java).apply {
                action = MiningService.ACTION_STOP
            }
            context.startService(intent)
            isServiceStarted = false
            Log.d("MiningManager", "Mining service stopped")
        } catch (e: Exception) {
            Log.e("MiningManager", "Failed to stop mining service", e)
        }
    }
    
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        // App moved to foreground
        Log.d("MiningManager", "App moved to foreground")
        // You can resume mining here if needed
    }
    
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
        // App moved to background
        Log.d("MiningManager", "App moved to background")
        // You might want to continue mining in background or pause based on user settings
    }
}