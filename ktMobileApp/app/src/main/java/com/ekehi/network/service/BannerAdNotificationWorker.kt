package com.ekehi.network.service

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ekehi.network.data.model.AdContent
import com.ekehi.network.data.repository.AdsRepository
import com.ekehi.network.security.SecurePreferences
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Worker for checking and sending notifications about new banner ads
 */
@HiltWorker
class BannerAdNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val adsRepository: AdsRepository,
    private val pushNotificationService: PushNotificationService,
    private val securePreferences: SecurePreferences
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "BannerAdNotificationWorker"
        private const val WORK_NAME = "banner_ad_notification_check"
        private const val CHECK_INTERVAL_HOURS = 12L // Check every 12 hours

        /**
         * Schedule periodic banner ad notification checks
         */
        fun schedulePeriodicCheck(context: Context) {
            Log.d(TAG, "Scheduling periodic banner ad notification check")
            
            val workRequest = PeriodicWorkRequestBuilder<BannerAdNotificationWorker>(
                CHECK_INTERVAL_HOURS, TimeUnit.HOURS
            )
                .setBackoffCriteria(
                    androidx.work.BackoffPolicy.EXPONENTIAL,
                    15, TimeUnit.MINUTES
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
            
            Log.d(TAG, "Banner ad notification check scheduled - interval: ${CHECK_INTERVAL_HOURS}h")
        }

        /**
         * Cancel scheduled banner ad notification checks
         */
        fun cancelScheduledCheck(context: Context) {
            Log.d(TAG, "Canceling scheduled banner ad notification check")
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting banner ad notification check")

            // Check if push notifications are enabled (banner ads use push notifications)
            if (!securePreferences.getBoolean("push_notifications_enabled", true)) {
                Log.d(TAG, "Push notifications disabled - skipping")
                return@withContext Result.success()
            }

            // Get current user ID
            val userId = securePreferences.getString("user_id", null)
            if (userId.isNullOrEmpty()) {
                Log.d(TAG, "No user logged in - skipping notification check")
                return@withContext Result.success()
            }

            // Check for new/active banner ads
            try {
                val ads = adsRepository.getActiveAds()
                if (ads.isNotEmpty()) {
                    Log.d(TAG, "Found ${ads.size} active banner ads - sending notification")
                    
                    // Send notification for new banner ads
                    pushNotificationService.showNotification(
                        "New Advertising Opportunities! 📢",
                        "${ads.size} new banner ads available. Check them out in the app!",
                        "new_banner_ads".hashCode()
                    )
                } else {
                    Log.d(TAG, "No new banner ads found")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get banner ads: ${e.message}", e)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in banner ad notification check: ${e.message}", e)
            Result.retry()
        }
    }
}
