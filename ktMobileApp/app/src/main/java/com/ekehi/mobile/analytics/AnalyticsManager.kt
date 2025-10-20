package com.ekehi.mobile.analytics

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsManager @Inject constructor(
    private val analyticsService: AnalyticsService
) {
    fun trackLogin(method: String) {
        analyticsService.logLogin(method)
    }

    fun trackSignUp(method: String) {
        analyticsService.logSignUp(method)
    }

    fun trackScreenView(screenName: String) {
        analyticsService.logScreenView(screenName)
    }

    fun trackMiningSessionStart(userId: String, sessionId: String) {
        analyticsService.logMiningSessionStart(userId, sessionId)
    }

    fun trackMiningSessionEnd(userId: String, sessionId: String, coinsEarned: Double, duration: Int) {
        analyticsService.logMiningSessionEnd(userId, sessionId, coinsEarned, duration)
    }

    fun trackSocialTaskCompleted(userId: String, taskId: String, taskTitle: String, reward: Double) {
        analyticsService.logSocialTaskCompleted(userId, taskId, taskTitle, reward)
    }

    fun trackReferralBonus(userId: String, referralUserId: String, bonusAmount: Double) {
        analyticsService.logReferralBonus(userId, referralUserId, bonusAmount)
    }

    fun trackStreakBonus(userId: String, streakDays: Int, bonusAmount: Double) {
        analyticsService.logStreakBonus(userId, streakDays, bonusAmount)
    }

    fun setUserProperties(userId: String, properties: Map<String, String>) {
        analyticsService.setUserId(userId)
        properties.forEach { (key, value) ->
            analyticsService.setUserProperty(key, value)
        }
    }

    fun trackUserProperties(userId: String, userProfile: com.ekehi.mobile.data.model.UserProfile) {
        analyticsService.setUserId(userId)
        analyticsService.setUserProperty("total_coins", userProfile.totalCoins.toString())
        analyticsService.setUserProperty("mining_power", userProfile.miningPower.toString())
        analyticsService.setUserProperty("current_streak", userProfile.currentStreak.toString())
        analyticsService.setUserProperty("total_referrals", userProfile.totalReferrals.toString())
    }
}