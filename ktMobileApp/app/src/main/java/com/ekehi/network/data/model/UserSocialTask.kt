package com.ekehi.network.data.model

data class UserSocialTask(
    val id: String = "",
    val userId: String,
    val taskId: String,
    val status: String, // "pending", "verified", "rejected"
    val completedAt: String? = null,
    val verifiedAt: String? = null,
    val proofUrl: String? = null,
    val proofData: Map<String, Any>? = null,
    val verificationAttempts: Int = 0,
    val rejectionReason: String? = null,
    val username: String? = null
)