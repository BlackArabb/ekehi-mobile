package com.ekehi.network.data.model

data class UserSocialTask(
    val userId: String,
    val taskId: String,
    val status: String, // pending, verified, completed
    val completedAt: String?,
    val verifiedAt: String?
)