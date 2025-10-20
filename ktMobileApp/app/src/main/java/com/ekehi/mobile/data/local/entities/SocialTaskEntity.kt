package com.ekehi.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "social_tasks")
data class SocialTaskEntity(
    @PrimaryKey
    val id: String,
    val userId: String, // Add userId field
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