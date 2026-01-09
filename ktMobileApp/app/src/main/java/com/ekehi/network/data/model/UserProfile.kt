package com.ekehi.network.data.model

data class UserProfile(
        val id: String,
        val userId: String, // This should remain as String since it's the first element of the array
        val username: String? = null,
        val email: String? = null,
        val phoneNumber: String = "",
        val country: String = "",
        val taskReward: Float = 0.0f,
        val miningReward: Float = 0.0f,
        val referralReward: Float = 0.0f,
        val autoMiningRate: Float = 0.0f,
        val miningPower: Float = 0.0f,
        val currentStreak: Int = 0,
        val longestStreak: Int = 0,
        val lastLoginDate: String? = null,
        val referralCode: String? = null, // This should remain as String since it's the first element of the array
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
) {
    val totalCoins: Float
        get() = taskReward + miningReward + referralReward
}