package com.ekehi.network.service

import android.app.Activity
import android.content.Context
import android.util.Log
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.adsbase.StartAppSDK
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener
import com.startapp.sdk.adsbase.adlisteners.AdEventListener
import com.startapp.sdk.adsbase.adlisteners.VideoListener

/**
 * StartIoService handles Start.io ad integration for the Ekehi Mobile app.
 * This service provides functionality for showing rewarded ads and exit ads.
 * 
 * IMPORTANT: This service assumes Start.io SDK is initialized via AndroidManifest.xml
 * with the meta-data tag: <meta-data android:name="com.startapp.sdk.APPLICATION_ID" android:value="210617452" />
 */
class StartIoService(private val context: Context) {
    private val TAG = "StartIoService"
    private var isInitialized = false
    private var exitAdShown = false
    private var startAppAd: StartAppAd? = null

    /**
     * Initialize Start.io SDK - but now we check if it's already initialized via AndroidManifest.xml
     * According to Start.io docs, when using AndroidManifest.xml initialization, SDK is auto-initialized
     */
    fun initialize(activity: Activity? = null) {
        if (isInitialized) {
            Log.d(TAG, "Already initialized")
            return
        }

        try {
            Log.d(TAG, "Checking Start.io SDK initialization status...")
            
            // When using AndroidManifest.xml initialization, the SDK should be auto-initialized
            // We just need to create the StartAppAd instance
            startAppAd = StartAppAd(context)
            
            // Test if SDK is working by checking if we can access it
            if (startAppAd != null) {
                isInitialized = true
                Log.d(TAG, "✅ Start.io SDK appears to be initialized (via AndroidManifest.xml)")
            } else {
                Log.e(TAG, "❌ Failed to create StartAppAd instance")
                isInitialized = false
            }
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
            if (listener != null) {
                startAppAd?.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, listener)
            } else {
                startAppAd?.loadAd(StartAppAd.AdMode.REWARDED_VIDEO)
            }
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
            // Show the ad with listener if provided
            if (listener != null) {
                startAppAd?.showAd(listener)
            } else {
                startAppAd?.showAd()
            }
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
            if (listener != null) {
                startAppAd?.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, listener)
            } else {
                startAppAd?.loadAd(StartAppAd.AdMode.REWARDED_VIDEO)
            }
            Log.d(TAG, "Loading exit ad...")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to load exit ad", e)
        }
    }

    /**
     * Show an exit ad
     * @return true if ad was shown successfully, false otherwise
     */
    fun showExitAd(activity: Activity, onAdClosed: (() -> Unit)? = null): Boolean {
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
            // Show the ad with listener
            startAppAd?.showAd(object : AdDisplayListener {
                override fun adHidden(ad: com.startapp.sdk.adsbase.Ad?) {
                    Log.d(TAG, "Exit ad closed by user")
                    exitAdShown = true
                    onAdClosed?.invoke()
                }
                
                override fun adDisplayed(ad: com.startapp.sdk.adsbase.Ad?) {
                    Log.d(TAG, "Exit ad displayed successfully")
                }
                
                override fun adClicked(ad: com.startapp.sdk.adsbase.Ad?) {
                    Log.d(TAG, "Exit ad clicked by user")
                }
                
                override fun adNotDisplayed(ad: com.startapp.sdk.adsbase.Ad?) {
                    Log.e(TAG, "Exit ad not displayed")
                    // Still consider it shown to prevent multiple attempts
                    exitAdShown = true
                    onAdClosed?.invoke()
                }
            })
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

    /**
     * Load a rewarded video ad with reward callback
     * @param onVideoCompleted callback when user completes watching the video
     */
    fun loadRewardedVideoAd(onVideoCompleted: (() -> Unit)? = null) {
        if (!isInitialized) {
            Log.w(TAG, "Start.io not initialized")
            return
        }

        try {
            startAppAd?.setVideoListener(object : VideoListener {
                override fun onVideoCompleted() {
                    Log.d(TAG, "Rewarded video completed")
                    onVideoCompleted?.invoke()
                }
            })
            
            startAppAd?.loadAd(StartAppAd.AdMode.REWARDED_VIDEO)
            Log.d(TAG, "Loading rewarded video ad...")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to load rewarded video ad", e)
        }
    }

    /**
     * Show a rewarded video ad
     * @return true if ad was shown successfully, false otherwise
     */
    fun showRewardedVideoAd(activity: Activity): Boolean {
        Log.d(TAG, "showRewardedVideoAd called")

        if (!isInitialized) {
            Log.w(TAG, "Start.io not initialized")
            return false
        }

        if (startAppAd == null) {
            Log.w(TAG, "StartAppAd not initialized")
            return false
        }

        try {
            // Show the ad
            startAppAd?.showAd()
            Log.d(TAG, "✅ Rewarded video ad shown successfully")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to show rewarded video ad", e)
            return false
        }
    }
}