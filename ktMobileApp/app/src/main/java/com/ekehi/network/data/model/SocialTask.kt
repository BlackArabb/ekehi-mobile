package com.ekehi.network.data.model

data class SocialTask(
    val id: String,
    val title: String,
    val description: String,
    val platform: String,
    val taskType: String,
    val rewardCoins: Double,
    val actionUrl: String? = null,
    val verificationMethod: String,
    val isActive: Boolean,
    val sortOrder: Int,
    val isCompleted: Boolean = false,
    val createdAt: String,
    val updatedAt: String
)