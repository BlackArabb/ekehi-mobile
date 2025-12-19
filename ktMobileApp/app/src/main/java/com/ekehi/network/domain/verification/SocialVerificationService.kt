package com.ekehi.network.domain.verification

import com.ekehi.network.data.model.SocialTask
import com.ekehi.network.data.model.UserSocialTask
import com.ekehi.network.service.TelegramBotService
import com.ekehi.network.service.YouTubeApiService
import com.ekehi.network.service.FacebookApiService
import javax.inject.Inject

class SocialVerificationService @Inject constructor(
    private val telegramService: TelegramBotService,
    private val youtubeService: YouTubeApiService,
    private val facebookService: FacebookApiService
) {
    
    suspend fun verifyTask(
        task: SocialTask,
        userTask: UserSocialTask,
        proofData: Map<String, Any>?
    ): VerificationResult {
        
        return when (task.platform.lowercase()) {
            "telegram" -> verifyTelegramTask(task, proofData)
            "youtube" -> verifyYouTubeTask(task, proofData)
            "facebook" -> verifyFacebookTask(task, proofData)
            "twitter", "x" -> verifyManualTask(task, proofData)
            else -> verifyManualTask(task, proofData)
        }
    }
    
    private suspend fun verifyTelegramTask(
        task: SocialTask,
        proofData: Map<String, Any>?
    ): VerificationResult {
        val telegramUserId = extractLong(proofData, "telegram_user_id")
            ?: return VerificationResult.Failure("Telegram User ID required")
        
        val channelUsername = task.verificationData?.get("channel_username")
            ?: return VerificationResult.Failure("Channel not configured")
        
        val result = telegramService.verifyChannelMembership(
            chatId = channelUsername,
            userId = telegramUserId
        )
        
        return if (result.isSuccess) {
            if (result.getOrNull() == true) {
                VerificationResult.Success("✅ Telegram membership verified!")
            } else {
                VerificationResult.Failure("❌ Not a member. Please join the channel first.")
            }
        } else {
            VerificationResult.Failure("Verification failed: ${result.exceptionOrNull()?.message}")
        }
    }
    
    private suspend fun verifyYouTubeTask(
        task: SocialTask,
        proofData: Map<String, Any>?
    ): VerificationResult {
        val userAccessToken = proofData?.get("youtube_access_token") as? String
            ?: return VerificationResult.Failure("Please connect your YouTube account")
        
        return when (task.taskType.lowercase()) {
            "subscribe", "channel_subscribe" -> {
                val channelId = task.verificationData?.get("channel_id")
                    ?: return VerificationResult.Failure("Channel not configured")
                
                val result = youtubeService.verifySubscription(channelId, userAccessToken)
                if (result.isSuccess) {
                    if (result.getOrNull() == true) {
                        VerificationResult.Success("✅ YouTube subscription verified!")
                    } else {
                        VerificationResult.Failure("❌ Not subscribed. Please subscribe first.")
                    }
                } else {
                    VerificationResult.Failure("Verification failed: ${result.exceptionOrNull()?.message}")
                }
            }
            
            "like", "video_like" -> {
                val videoId = task.verificationData?.get("video_id")
                    ?: return VerificationResult.Failure("Video not configured")
                
                val result = youtubeService.verifyVideoLike(videoId, userAccessToken)
                if (result.isSuccess) {
                    if (result.getOrNull() == true) {
                        VerificationResult.Success("✅ YouTube like verified!")
                    } else {
                        VerificationResult.Failure("❌ Video not liked. Please like first.")
                    }
                } else {
                    VerificationResult.Failure("Verification failed: ${result.exceptionOrNull()?.message}")
                }
            }
            
            else -> VerificationResult.Failure("Unknown YouTube task type: ${task.taskType}")
        }
    }
    
    private suspend fun verifyFacebookTask(
        task: SocialTask,
        proofData: Map<String, Any>?
    ): VerificationResult {
        val userAccessToken = proofData?.get("facebook_access_token") as? String
            ?: return VerificationResult.Failure("Please connect your Facebook account")
        
        return when (task.taskType.lowercase()) {
            "like_page", "page_like" -> {
                val pageId = task.verificationData?.get("page_id")
                    ?: return VerificationResult.Failure("Page not configured")
                
                val result = facebookService.verifyPageLike(pageId, userAccessToken)
                if (result.isSuccess) {
                    if (result.getOrNull() == true) {
                        VerificationResult.Success("✅ Facebook page like verified!")
                    } else {
                        VerificationResult.Failure("❌ Page not liked. Please like first.")
                    }
                } else {
                    VerificationResult.Failure("Verification failed: ${result.exceptionOrNull()?.message}")
                }
            }
            
            else -> VerificationResult.Failure("Unknown Facebook task type: ${task.taskType}")
        }
    }
    
    private fun verifyManualTask(
        task: SocialTask,
        proofData: Map<String, Any>?
    ): VerificationResult {
        val hasProof = proofData?.containsKey("screenshot_url") == true ||
                      proofData?.containsKey("proof_url") == true ||
                      proofData?.containsKey("username") == true
        
        return if (hasProof) {
            VerificationResult.Pending("⏳ Submitted for manual review. Usually takes 24-48 hours.")
        } else {
            VerificationResult.Failure("Please provide proof (screenshot or username) for verification.")
        }
    }
    
    private fun extractLong(map: Map<String, Any>?, key: String): Long? {
        return when (val value = map?.get(key)) {
            is Long -> value
            is Int -> value.toLong()
            is String -> value.toLongOrNull()
            is Number -> value.toLong()
            else -> null
        }
    }
}

sealed class VerificationResult {
    data class Success(val message: String) : VerificationResult()
    data class Failure(val reason: String) : VerificationResult()
    data class Pending(val message: String) : VerificationResult()
}