package com.ekehi.network.data.model

data class MiningSession(
    val id: String,
    val userId: String,
    val coinsEarned: Double,
    val clicksMade: Int,
    val sessionDuration: Int,
    val createdAt: String,
    val updatedAt: String
) {
    constructor(
        id: String,
        userId: String,
        amount: Double,
        timestamp: String,
        status: String,
        duration: Int,
        createdAt: String,
        updatedAt: String
    ) : this(
        id = id,
        userId = userId,
        coinsEarned = amount,
        clicksMade = 0, // Default value
        sessionDuration = duration,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}