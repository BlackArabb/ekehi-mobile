package com.ekehi.network.data.model

data class UserProfile(
        val id: String,
        val userId: String,
        val username: String? = null,
        val name: String? = null,
        val email: String? = null,
        val phoneNumber: String = "",
        val country: String = "",
        val taskReward: Float = 0.0f,
        val miningReward: Float = 0.0f,
        val referralReward: Float = 0.0f,
        val totalCoins: Float = 0.0f,
        val autoMiningRate: Float = 0.0f,
        val miningPower: Float = 0.0f,
        val referralCode: String? = null,
        val referredBy: String? = null,
        val totalReferrals: Int = 0,
        val lifetimeEarnings: Float = 0.0f,
        val dailyMiningRate: Float = 0.0f,
        val maxDailyEarnings: Float = 0.0f,
        val todayEarnings: Float = 0.0f,
        val lastMiningDate: String? = null,
        val createdAt: String,
        val updatedAt: String
)