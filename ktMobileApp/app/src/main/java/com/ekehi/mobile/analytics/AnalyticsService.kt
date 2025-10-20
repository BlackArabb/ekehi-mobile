package com.ekehi.mobile.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

    fun logEvent(eventName: String, params: Bundle? = null) {
        firebaseAnalytics.logEvent(eventName, params)
    }

    fun logScreenView(screenName: String, screenClass: String? = null) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass ?: screenName)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    fun logLogin(method: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, method)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
    }

    fun logSignUp(method: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, method)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle)
    }

    fun logMiningSessionStart(userId: String, sessionId: String) {
        val bundle = Bundle().apply {
            putString("user_id", userId)
            putString("session_id", sessionId)
        }
        firebaseAnalytics.logEvent("mining_session_start", bundle)
    }

    fun logMiningSessionEnd(userId: String, sessionId: String, coinsEarned: Double, duration: Int) {
        val bundle = Bundle().apply {
            putString("user_id", userId)
            putString("session_id", sessionId)
            putDouble("coins_earned", coinsEarned)
            putInt("duration_seconds", duration)
        }
        firebaseAnalytics.logEvent("mining_session_end", bundle)
    }

    fun logSocialTaskCompleted(userId: String, taskId: String, taskTitle: String, reward: Double) {
        val bundle = Bundle().apply {
            putString("user_id", userId)
            putString("task_id", taskId)
            putString("task_title", taskTitle)
            putDouble("reward", reward)
        }
        firebaseAnalytics.logEvent("social_task_completed", bundle)
    }

    fun logReferralBonus(userId: String, referralUserId: String, bonusAmount: Double) {
        val bundle = Bundle().apply {
            putString("user_id", userId)
            putString("referral_user_id", referralUserId)
            putDouble("bonus_amount", bonusAmount)
        }
        firebaseAnalytics.logEvent("referral_bonus_earned", bundle)
    }

    fun logStreakBonus(userId: String, streakDays: Int, bonusAmount: Double) {
        val bundle = Bundle().apply {
            putString("user_id", userId)
            putInt("streak_days", streakDays)
            putDouble("bonus_amount", bonusAmount)
        }
        firebaseAnalytics.logEvent("streak_bonus_earned", bundle)
    }

    fun setUserProperty(name: String, value: String) {
        firebaseAnalytics.setUserProperty(name, value)
    }

    fun setUserId(userId: String) {
        firebaseAnalytics.setUserId(userId)
    }
}