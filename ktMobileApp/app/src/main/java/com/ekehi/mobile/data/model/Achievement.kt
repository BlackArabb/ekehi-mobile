package com.ekehi.mobile.data.model

data class Achievement(
    val id: String,
    val achievementId: String,
    val title: String,
    val description: String,
    val type: String,
    val target: Int,
    val reward: Double,
    val rarity: String,
    val isActive: Boolean,
    val isUnlocked: Boolean = false,
    val isClaimed: Boolean = false,
    val progress: Int = 0,
    val createdAt: String,
    val updatedAt: String
)