package com.ekehi.network.service

import android.app.Activity
import android.content.Context
import android.util.Log
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.adsbase.StartAppSDK
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener
import com.startapp.sdk.adsbase.adlisteners.AdEventListener

/**
 * StartIoService handles Start.io ad integration for the Ekehi Mobile app.
 * This service provides functionality for showing rewarded ads and exit ads.
 */
class StartIoService(private val context: Context) {
    private val TAG = "StartIoService"
    private var isInitialized = false
    private var exitAdShown = false
    private val appId = "209257659" // Start.io App ID from React Native implementation
    private var startAppAd: StartAppAd? = null

    /**
     * Initialize Start.io SDK with disabled automatic features
     */
    fun initialize() {
        if (isInitialized) {
            Log.d(TAG, "Already initialized")
            return
        }

        try {
            Log.d(TAG, "Initializing with App ID: $appId")
            // Initialize with disabled automatic features
            StartAppSDK.init(context, appId, false) // Third parameter is for test mode
            
            // Disable automatic ad loading features that are available in current SDK
            // Note: Some methods may have been deprecated or removed in newer versions
            // We'll try to call them and catch any exceptions if they don't exist
            
            try {
                StartAppAd.disableSplash() // Disable splash ads
            } catch (e: Exception) {
                Log.w(TAG, "disableSplash method not available in current SDK version")
            }
            
            try {
                StartAppAd.disableAutoInterstitial() // Disable automatic interstitial ads
            } catch (e: Exception) {
                Log.w(TAG, "disableAutoInterstitial method not available in current SDK version")
            }
            
            isInitialized = true
            
            // Initialize the ad object
            startAppAd = StartAppAd(context)
            
            Log.d(TAG, "✅ Start.io initialized successfully with automatic features disabled")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to initialize Start.io", e)
            isInitialized = false
        }
    }

    /**
     * Load a rewarded ad asynchronously
     * This should be called before showing the ad to ensure it's ready
     */
    fun loadRewardedAd(listener: AdEventListener? = null) {
        if (!isInitialized) {
            Log.w(TAG, "Start.io not initialized")
            return
        }

        try {
            startAppAd?.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, listener)
            Log.d(TAG, "Loading rewarded ad...")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to load rewarded ad", e)
        }
    }

    /**
     * Show a rewarded ad
     * @return true if ad was shown successfully, false otherwise
     */
    fun showRewardedAd(activity: Activity, listener: AdDisplayListener? = null): Boolean {
        Log.d(TAG, "showRewardedAd called")

        if (!isInitialized) {
            Log.w(TAG, "Start.io not initialized")
            return false
        }

        if (startAppAd == null) {
            Log.w(TAG, "StartAppAd not initialized")
            return false
        }

        try {
            // Show the ad without specifying activity parameter
            startAppAd?.showAd()
            Log.d(TAG, "✅ Rewarded ad shown successfully")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to show rewarded ad", e)
            return false
        }
    }

    /**
     * Load an exit ad asynchronously
     */
    fun loadExitAd(listener: AdEventListener? = null) {
        if (!isInitialized) {
            Log.w(TAG, "Start.io not initialized")
            return
        }

        try {
            // Use rewarded video mode for exit ads as EXIT mode was removed
            startAppAd?.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, listener)
            Log.d(TAG, "Loading exit ad...")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to load exit ad", e)
        }
    }

    /**
     * Show an exit ad
     * @return true if ad was shown successfully, false otherwise
     */
    fun showExitAd(activity: Activity, listener: AdDisplayListener? = null): Boolean {
        if (exitAdShown) {
            return false
        }

        if (!isInitialized) {
            Log.w(TAG, "Start.io not initialized")
            return false
        }

        if (startAppAd == null) {
            Log.w(TAG, "StartAppAd not initialized")
            return false
        }

        try {
            // Show the ad without specifying activity parameter
            startAppAd?.showAd()
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
     * Check if a rewarded ad is ready to be shown
     * @return true if ready, false otherwise
     */
    fun isRewardedAdReady(): Boolean {
        return startAppAd?.isReady ?: false
    }

    /**
     * Check if an exit ad is ready to be shown
     * @return true if ready, false otherwise
     */
    fun isExitAdReady(): Boolean {
        return startAppAd?.isReady ?: false
    }

    /**
     * Reset exit ad flag
     */
    fun resetExitAd() {
        exitAdShown = false
    }
}