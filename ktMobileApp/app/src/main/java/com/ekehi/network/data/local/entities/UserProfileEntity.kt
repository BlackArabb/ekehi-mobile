package com.ekehi.network.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val username: String? = null,
    val email: String? = null,
    val phone_number: String = "", // Add this line
    val country: String = "", // Add this line
    val totalCoins: Float = 0.0f,
    val autoMiningRate: Float = 0.0f,
    val miningPower: Float = 0.0f,
    val referralBonusRate: Float = 0.0f,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastLoginDate: String? = null,
    val referralCode: String? = null,
    val referredBy: String? = null,
    val totalReferrals: Int = 0,
    val lifetimeEarnings: Float = 0.0f,
    val dailyMiningRate: Float = 0.0f,
    val maxDailyEarnings: Float = 0.0f,
    val todayEarnings: Float = 0.0f,
    val lastMiningDate: String? = null,
    val streakBonusClaimed: Int = 0,
    val createdAt: String,
    val updatedAt: String
)