package com.ekehi.mobile.data.model

data class UserProfile(
    val id: String,
    val userId: String,
    val username: String? = null,
    val totalCoins: Double = 0.0,
    val coinsPerSecond: Double = 0.0, // Deprecated - will be replaced with autoMiningRate
    val autoMiningRate: Double = 0.0, // New field for auto mining rate
    val miningPower: Double = 0.0,
    val referralBonusRate: Double = 0.0, // New field for referral bonus
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