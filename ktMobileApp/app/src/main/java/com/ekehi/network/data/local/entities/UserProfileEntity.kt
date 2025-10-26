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
    val totalCoins: Double = 0.0,
    val coinsPerSecond: Double = 0.0,
    val autoMiningRate: Double = 0.0,
    val miningPower: Double = 0.0,
    val referralBonusRate: Double = 0.0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastLoginDate: String? = null,
    val referralCode: String? = null,
    val referredBy: String? = null,
    val totalReferrals: Int = 0,
    val lifetimeEarnings: Double = 0.0,
    val dailyMiningRate: Double = 0.0,
    val maxDailyEarnings: Double = 0.0,
    val todayEarnings: Double = 0.0,
    val lastMiningDate: String? = null,
    val streakBonusClaimed: Int = 0,
    val createdAt: String,
    val updatedAt: String
)