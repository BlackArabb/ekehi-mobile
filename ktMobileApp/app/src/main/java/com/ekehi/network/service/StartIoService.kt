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
    private var isVideoAdLoaded = false

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
     * We'll preload ads here.
     */
    fun initialize() {
        Log.d(TAG, "SDK already initialized via AndroidManifest.xml")
        
        // Preload ads
        loadRewardedVideoAd()
        loadInterstitialAd()
    }

    /**
     * Load a rewarded video ad with completion callback
     * @param onVideoCompleted Callback invoked when user completes watching the video
     */
    fun loadRewardedVideoAd(onVideoCompleted: (() -> Unit)? = null) {
        try {
            Log.d(TAG, "📥 Loading rewarded video ad...")
            
            // Store the reward callback
            rewardCallback = onVideoCompleted
            isVideoAdLoaded = false
            
            // Create a fresh ad instance for rewarded video
            rewardedVideoAd = StartAppAd(context)
            
            // Set video listener BEFORE loading
            rewardedVideoAd?.setVideoListener(object : VideoListener {
                override fun onVideoCompleted() {
                    Log.d(TAG, "✅✅✅ Rewarded video COMPLETED - User earned reward!")
                    rewardCallback?.invoke()
                }
            })
            
            // Load the ad with REWARDED_VIDEO mode
            rewardedVideoAd?.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, object : AdEventListener {
                override fun onReceiveAd(ad: com.startapp.sdk.adsbase.Ad) {
                    Log.d(TAG, "✅ Rewarded video ad loaded successfully")
                    isVideoAdLoaded = true
                }
                
                override fun onFailedToReceiveAd(ad: com.startapp.sdk.adsbase.Ad?) {
                    Log.e(TAG, "❌ Failed to load rewarded video ad")
                    Log.e(TAG, "   Reason: ${ad?.errorMessage ?: "Unknown error"}")
                    isVideoAdLoaded = false
                    
                    // Try to load a standard interstitial as fallback
                    loadInterstitialAd()
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception loading rewarded video ad", e)
            isVideoAdLoaded = false
        }
    }

    /**
     * Show rewarded video ad
     * @param activity The activity context
     * @return true if ad was shown, false otherwise
     */
    fun showRewardedVideoAd(activity: Activity): Boolean {
        Log.d(TAG, "📺 Attempting to show rewarded video ad")
        Log.d(TAG, "   Ad ready status: $isVideoAdLoaded")
        Log.d(TAG, "   Is loaded flag: $isVideoAdLoaded")
        
        return try {
            if (rewardedVideoAd == null) {
                Log.e(TAG, "❌ Rewarded video ad instance is null")
                return false
            }
            
            if (!isVideoAdLoaded) {
                Log.w(TAG, "⚠️ Rewarded video ad not ready yet")
                Log.w(TAG, "   Try loading the ad first")
                
                // Try to show interstitial ad as fallback
                Log.d(TAG, "Checking interstitial ad readiness: ${startAppAd != null}")
                if (startAppAd != null) {
                    Log.d(TAG, "📺 Showing interstitial ad as fallback")
                    return showInterstitialAd()
                } else {
                    Log.w(TAG, "⚠️ Interstitial ad not ready either")
                }
                
                return false
            }
            
            // Show the ad with display listener
            rewardedVideoAd?.showAd(object : AdDisplayListener {
                override fun adHidden(ad: com.startapp.sdk.adsbase.Ad?) {
                    Log.d(TAG, "📱 Rewarded video ad closed/hidden")
                    // Reload ad for next time
                    isVideoAdLoaded = false
                    loadRewardedVideoAd(rewardCallback)
                }
                
                override fun adDisplayed(ad: com.startapp.sdk.adsbase.Ad?) {
                    Log.d(TAG, "✅✅✅ Rewarded video ad DISPLAYED successfully!")
                }
                
                override fun adClicked(ad: com.startapp.sdk.adsbase.Ad?) {
                    Log.d(TAG, "👆 Rewarded video ad clicked")
                }
                
                override fun adNotDisplayed(ad: com.startapp.sdk.adsbase.Ad?) {
                    Log.e(TAG, "❌ Rewarded video ad NOT displayed")
                    Log.e(TAG, "   Reason: ${ad?.errorMessage ?: "Unknown error"}")
                    // Reload ad for next attempt
                    isVideoAdLoaded = false
                    loadRewardedVideoAd(rewardCallback)
                }
            })
            
            Log.d(TAG, "✅ Show rewarded video ad called successfully")
            true
            
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
    fun showExitAd(onAdClosed: () -> Unit): Boolean {
        return try {
            Log.d(TAG, "🚪 Loading exit ad...")
            
            // Create a new ad instance for exit ad
            val exitAd = StartAppAd(context)
            
            // Load and show interstitial ad
            exitAd.loadAd(StartAppAd.AdMode.AUTOMATIC, object : AdEventListener {
                override fun onReceiveAd(ad: com.startapp.sdk.adsbase.Ad) {
                    Log.d(TAG, "✅ Exit ad loaded, showing now...")
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
        val isReady = isVideoAdLoaded && rewardedVideoAd != null
        Log.d(TAG, "🔍 Checking if rewarded ad is ready: $isReady")
        Log.d(TAG, "   isVideoAdLoaded: $isVideoAdLoaded")
        Log.d(TAG, "   rewardedVideoAd != null: ${rewardedVideoAd != null}")
        return isReady
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
    fun showInterstitialAd(): Boolean {
        Log.d(TAG, "Attempting to show interstitial ad")
        Log.d(TAG, "Interstitial ad ready status: ${startAppAd != null}")
        return try {
            if (startAppAd != null) {
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
                Log.d(TAG, "Interstitial ad show call completed")
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