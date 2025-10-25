package com.ekehi.network.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mining_sessions")
data class MiningSessionEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val coinsEarned: Double,
    val clicksMade: Int,
    val sessionDuration: Int,
    val createdAt: String,
    val updatedAt: String
)