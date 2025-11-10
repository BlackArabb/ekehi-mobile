package com.ekehi.network.service

import android.app.Activity
import android.content.Context
import android.util.Log
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.adsbase.StartAppSDK
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener
import com.startapp.sdk.adsbase.adlisteners.AdEventListener
import com.startapp.sdk.adsbase.adlisteners.VideoListener
import javax.inject.Inject
import javax.inject.Singleton

/**
 * StartIoService handles Start.io ad integration for the Ekehi Mobile app.
 * SDK is auto-initialized via AndroidManifest.xml with the APPLICATION_ID meta-data.
 */
@Singleton
class StartIoService @Inject constructor(
    private val context: Context
) {
    private val TAG = "StartIoService"
    private var startAppAd: StartAppAd? = null
    private var rewardedVideoAd: StartAppAd? = null
    private var rewardCallback: (() -> Unit)? = null

    init {
        // SDK is auto-initialized via AndroidManifest.xml
        // Just create ad instances
        try {
            startAppAd = StartAppAd(context)
            rewardedVideoAd = StartAppAd(context)
            Log.d(TAG, "✅ Start.io ad instances created successfully")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to create Start.io ad instances", e)
        }
    }

    /**
     * Initialize is now just a compatibility method.
     * SDK is already initialized via AndroidManifest.xml.
     */
    fun initialize(activity: Activity? = null) {
        Log.d(TAG, "SDK already initialized via AndroidManifest.xml")
    }

    /**
     * Load a rewarded video ad with completion callback
     * @param onVideoCompleted Callback invoked when user completes watching the video
     */
    fun loadRewardedVideoAd(onVideoCompleted: (() -> Unit)? = null) {
        try {
            // Store the reward callback
            rewardCallback = onVideoCompleted
            
            // Set video listener
            rewardedVideoAd?.setVideoListener(object : VideoListener {
                override fun onVideoCompleted() {
                    Log.d(TAG, "✅ Rewarded video completed - User earned reward!")
                    rewardCallback?.invoke()
                }
            })
            
            // Load the ad
            rewardedVideoAd?.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, object : AdEventListener {
                override fun onReceiveAd(ad: com.startapp.sdk.adsbase.Ad) {
                    Log.d(TAG, "✅ Rewarded video ad loaded successfully")
                }
                
                override fun onFailedToReceiveAd(ad: com.startapp.sdk.adsbase.Ad?) {
                    Log.e(TAG, "❌ Failed to load rewarded video ad")
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception loading rewarded video ad", e)
        }
    }

    /**
     * Show rewarded video ad
     * @param activity The activity context
     * @return true if ad was shown, false otherwise
     */
    fun showRewardedVideoAd(activity: Activity): Boolean {
        return try {
            if (rewardedVideoAd?.isReady == true) {
                rewardedVideoAd?.showAd(object : AdDisplayListener {
                    override fun adHidden(ad: com.startapp.sdk.adsbase.Ad?) {
                        Log.d(TAG, "Rewarded video ad closed")
                        // Reload ad for next time
                        loadRewardedVideoAd(rewardCallback)
                    }
                    
                    override fun adDisplayed(ad: com.startapp.sdk.adsbase.Ad?) {
                        Log.d(TAG, "✅ Rewarded video ad displayed successfully")
                    }
                    
                    override fun adClicked(ad: com.startapp.sdk.adsbase.Ad?) {
                        Log.d(TAG, "Rewarded video ad clicked")
                    }
                    
                    override fun adNotDisplayed(ad: com.startapp.sdk.adsbase.Ad?) {
                        Log.e(TAG, "❌ Rewarded video ad not displayed")
                        // Reload ad for next attempt
                        loadRewardedVideoAd(rewardCallback)
                    }
                })
                true
            } else {
                Log.w(TAG, "⚠️ Rewarded video ad not ready yet")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception showing rewarded video ad", e)
            false
        }
    }

    /**
     * Show exit ad with callback when ad closes
     * @param activity The activity context
     * @param onAdClosed Callback invoked when ad is closed or failed to show
     * @return true if ad loading started, false otherwise
     */
    fun showExitAd(activity: Activity, onAdClosed: () -> Unit): Boolean {
        return try {
            // Create a new ad instance for exit ad
            val exitAd = StartAppAd(context)
            
            // Load and show interstitial ad
            exitAd.loadAd(StartAppAd.AdMode.AUTOMATIC, object : AdEventListener {
                override fun onReceiveAd(ad: com.startapp.sdk.adsbase.Ad) {
                    Log.d(TAG, "Exit ad loaded, showing now...")
                    exitAd.showAd(object : AdDisplayListener {
                        override fun adHidden(ad: com.startapp.sdk.adsbase.Ad?) {
                            Log.d(TAG, "Exit ad closed")
                            onAdClosed()
                        }
                        
                        override fun adDisplayed(ad: com.startapp.sdk.adsbase.Ad?) {
                            Log.d(TAG, "✅ Exit ad displayed successfully")
                        }
                        
                        override fun adClicked(ad: com.startapp.sdk.adsbase.Ad?) {
                            Log.d(TAG, "Exit ad clicked")
                        }
                        
                        override fun adNotDisplayed(ad: com.startapp.sdk.adsbase.Ad?) {
                            Log.e(TAG, "❌ Exit ad not displayed")
                            onAdClosed()
                        }
                    })
                }
                
                override fun onFailedToReceiveAd(ad: com.startapp.sdk.adsbase.Ad?) {
                    Log.e(TAG, "❌ Failed to load exit ad")
                    onAdClosed()
                }
            })
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception with exit ad", e)
            onAdClosed()
            false
        }
    }

    /**
     * Check if Start.io SDK is initialized
     * @return true if initialized, false otherwise
     */
    fun isStartIoInitialized(): Boolean {
        return startAppAd != null && rewardedVideoAd != null
    }

    /**
     * Check if rewarded video ad is ready to be shown
     * @return true if ready, false otherwise
     */
    fun isRewardedAdReady(): Boolean {
        return rewardedVideoAd?.isReady == true
    }

    /**
     * Load a standard interstitial ad
     */
    fun loadInterstitialAd() {
        try {
            startAppAd?.loadAd(StartAppAd.AdMode.AUTOMATIC, object : AdEventListener {
                override fun onReceiveAd(ad: com.startapp.sdk.adsbase.Ad) {
                    Log.d(TAG, "✅ Interstitial ad loaded successfully")
                }
                
                override fun onFailedToReceiveAd(ad: com.startapp.sdk.adsbase.Ad?) {
                    Log.e(TAG, "❌ Failed to load interstitial ad")
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception loading interstitial ad", e)
        }
    }

    /**
     * Show a standard interstitial ad
     * @param activity The activity context
     * @return true if ad was shown, false otherwise
     */
    fun showInterstitialAd(activity: Activity): Boolean {
        return try {
            if (startAppAd?.isReady == true) {
                startAppAd?.showAd(object : AdDisplayListener {
                    override fun adHidden(ad: com.startapp.sdk.adsbase.Ad?) {
                        Log.d(TAG, "Interstitial ad closed")
                        // Reload ad for next time
                        loadInterstitialAd()
                    }
                    
                    override fun adDisplayed(ad: com.startapp.sdk.adsbase.Ad?) {
                        Log.d(TAG, "✅ Interstitial ad displayed successfully")
                    }
                    
                    override fun adClicked(ad: com.startapp.sdk.adsbase.Ad?) {
                        Log.d(TAG, "Interstitial ad clicked")
                    }
                    
                    override fun adNotDisplayed(ad: com.startapp.sdk.adsbase.Ad?) {
                        Log.e(TAG, "❌ Interstitial ad not displayed")
                        loadInterstitialAd()
                    }
                })
                true
            } else {
                Log.w(TAG, "⚠️ Interstitial ad not ready")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception showing interstitial ad", e)
            false
        }
    }
}