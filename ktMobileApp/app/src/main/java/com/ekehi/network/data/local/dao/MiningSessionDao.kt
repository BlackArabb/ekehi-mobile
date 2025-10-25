package com.ekehi.network.data.local.dao

import androidx.room.*
import com.ekehi.network.data.local.entities.MiningSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MiningSessionDao {
    @Query("SELECT * FROM mining_sessions")
    fun getAllMiningSessions(): Flow<List<MiningSessionEntity>>

    @Query("SELECT * FROM mining_sessions WHERE id = :sessionId LIMIT 1")
    fun getMiningSessionById(sessionId: String): Flow<MiningSessionEntity?>

    @Query("SELECT * FROM mining_sessions WHERE userId = :userId ORDER BY createdAt DESC")
    fun getMiningSessionsByUserId(userId: String): Flow<List<MiningSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMiningSession(miningSession: MiningSessionEntity)

    @Update
    suspend fun updateMiningSession(miningSession: MiningSessionEntity)

    @Delete
    suspend fun deleteMiningSession(miningSession: MiningSessionEntity)

    @Query("DELETE FROM mining_sessions")
    suspend fun deleteAllMiningSessions()
}