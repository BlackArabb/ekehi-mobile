package com.ekehi.network.data.model

data class MiningSession(
    val id: String,
    val userId: String,
    val coinsEarned: Double,
    val clicksMade: Int,
    val sessionDuration: Int,
    val createdAt: String,
    val updatedAt: String
)