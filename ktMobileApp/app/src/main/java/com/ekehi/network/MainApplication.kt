package com.ekehi.network

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Application initialization code here
        
        // Initialize ProcessLifecycleOwner for lifecycle awareness
        ProcessLifecycleOwner.get()
    }
}