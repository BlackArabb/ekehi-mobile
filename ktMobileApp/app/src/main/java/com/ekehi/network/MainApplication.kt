package com.ekehi.network

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.HiltAndroidApp
import com.ekehi.network.service.StartIoService
import javax.inject.Inject
import android.util.Log
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger

@HiltAndroidApp
class MainApplication : Application() {
    @Inject
    lateinit var startIoService: StartIoService
    
    override fun onCreate() {
        super.onCreate()
        // Application initialization code here
        
        // Initialize Facebook SDK
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)
        Log.d("MainApplication", "Facebook SDK initialized")
        
        // Initialize ProcessLifecycleOwner for lifecycle awareness
        ProcessLifecycleOwner.get()
        
        // Initialize Start.io service
        try {
            startIoService.initialize()
            Log.d("MainApplication", "Start.io service initialized")
        } catch (e: Exception) {
            Log.e("MainApplication", "Failed to initialize Start.io service", e)
        }


    }
}