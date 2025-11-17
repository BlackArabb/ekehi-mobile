package com.ekehi.network.data.model

data class Referral(
    val id: String,
    val referrerId: String,
    val referredUserId: String,
    val referredUserName: String? = null,
    val referralCode: String,
    val rewardAmount: Double = 0.5,
    val rewardClaimed: Boolean = false,
    val createdAt: Long? = null,
    val claimedAt: Long? = null
)