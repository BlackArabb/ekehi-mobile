package com.ekehi.mobile.network.service

import io.appwrite.models.RealtimeResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealtimeNotificationHandler @Inject constructor(
    private val notificationService: NotificationService,
    private val notificationHandler: NotificationHandler
) {
    fun handleRealtimeEvent(event: RealtimeResponse) {
        try {
            // Delegate to the new notification handler for better organization
            notificationHandler.handleRealtimeEvent(event)
            
            // Also maintain the original notification service for backward compatibility
            val payload = event.payload ?: return
            val collectionId = payload["collectionId"]?.toString() ?: return
            
            when (collectionId) {
                "mining_sessions" -> {
                    handleMiningSessionEvent(payload)
                }
                "user_social_tasks" -> {
                    handleSocialTaskEvent(payload)
                }
                "user_profiles" -> {
                    handleUserProfileEvent(payload)
                }
                "presale_purchases" -> {
                    handlePresaleEvent(payload)
                }
            }
        } catch (e: Exception) {
            // Handle any errors in processing the event
            e.printStackTrace()
        }
    }
    
    private fun handleMiningSessionEvent(payload: Map<String, Any>) {
        try {
            val eventType = payload["event"]?.toString() ?: return
            
            when (eventType) {
                "database.documents.create", "database.documents.update" -> {
                    // Check if this is a mining session completion
                    val coinsEarned = (payload["coinsEarned"] as? Number)?.toDouble() ?: 0.0
                    
                    if (coinsEarned > 0) {
                        notificationService.showMiningCompletedNotification(coinsEarned)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun handleSocialTaskEvent(payload: Map<String, Any>) {
        try {
            val eventType = payload["event"]?.toString() ?: return
            
            when (eventType) {
                "database.documents.create" -> {
                    // A new social task completion
                    val reward = 0.5 // Default reward for social tasks
                    notificationService.showSocialTaskCompletedNotification(reward)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun handleUserProfileEvent(payload: Map<String, Any>) {
        try {
            val eventType = payload["event"]?.toString() ?: return
            
            when (eventType) {
                "database.documents.update" -> {
                    // Check for referral bonuses
                    val totalReferrals = (payload["totalReferrals"] as? Number)?.toInt() ?: 0
                    val previousTotalReferrals = 0 // In a real implementation, you would track this
                    
                    if (totalReferrals > previousTotalReferrals) {
                        val referralBonus = 0.5 // Default referral bonus
                        notificationService.showReferralBonusNotification(referralBonus)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun handlePresaleEvent(payload: Map<String, Any>) {
        try {
            val eventType = payload["event"]?.toString() ?: return
            
            when (eventType) {
                "database.documents.create" -> {
                    // A new presale purchase
                    // In a real implementation, you might show a notification for this
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}