package com.ekehi.network.network.service

import android.app.Activity
import android.content.Context
import android.util.Log
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.adsbase.StartAppSDK

/**
 * StartIoService handles Start.io ad integration for the Ekehi Mobile app.
 * This service provides functionality for showing rewarded ads and exit ads.
 */
class StartIoService(private val context: Context) {
    private val TAG = "StartIoService"
    private var isInitialized = false
    private var exitAdShown = false
    private val appId = "209257659" // Start.io App ID from React Native implementation

    /**
     * Initialize Start.io SDK
     */
    fun initialize() {
        if (isInitialized) {
            Log.d(TAG, "Already initialized")
            return
        }

        try {
            Log.d(TAG, "Initializing with App ID: $appId")
            StartAppSDK.init(context, appId)
            
            // Set user consent
            StartAppSDK.setUserConsent(true)
            
            // Set ad frequency
            StartAppAd.setSecondsBetweenAds(60)
            StartAppAd.setActivitiesBetweenAds(3)
            
            isInitialized = true
            Log.d(TAG, "✅ Start.io initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to initialize Start.io", e)
            isInitialized = false
        }
    }

    /**
     * Show a rewarded ad
     * @return true if ad was shown successfully, false otherwise
     */
    fun showRewardedAd(activity: Activity): Boolean {
        Log.d(TAG, "showRewardedAd called")

        if (!isInitialized) {
            Log.w(TAG, "Start.io not initialized")
            return false
        }

        try {
            val startAppAd = StartAppAd(context)
            startAppAd.loadAd()
            startAppAd.showAd()
            Log.d(TAG, "✅ Rewarded ad shown successfully")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to show rewarded ad", e)
            return false
        }
    }

    /**
     * Show an exit ad
     * @return true if ad was shown successfully, false otherwise
     */
    fun showExitAd(activity: Activity): Boolean {
        if (exitAdShown) {
            return false
        }

        try {
            val startAppAd = StartAppAd(context)
            startAppAd.loadAd()
            startAppAd.showAd()
            exitAdShown = true
            Log.d(TAG, "✅ Exit ad shown")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to show exit ad", e)
            return false
        }
    }

    /**
     * Check if Start.io is initialized
     * @return true if initialized, false otherwise
     */
    fun isStartIoInitialized(): Boolean {
        return isInitialized
    }

    /**
     * Reset exit ad flag
     */
    fun resetExitAd() {
        exitAdShown = false
    }
}