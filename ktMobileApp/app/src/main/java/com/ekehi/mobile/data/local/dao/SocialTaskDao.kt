package com.ekehi.mobile.data.local.dao

import androidx.room.*
import com.ekehi.mobile.data.local.entities.SocialTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SocialTaskDao {
    @Query("SELECT * FROM social_tasks")
    fun getAllSocialTasks(): Flow<List<SocialTaskEntity>>

    @Query("SELECT * FROM social_tasks WHERE id = :taskId LIMIT 1")
    fun getSocialTaskById(taskId: String): Flow<SocialTaskEntity?>

    @Query("SELECT * FROM social_tasks WHERE userId = :userId")
    fun getSocialTasksByUserId(userId: String): Flow<List<SocialTaskEntity>>

    @Query("SELECT * FROM social_tasks WHERE userId = :userId AND isCompleted = 0")
    fun getIncompleteSocialTasksByUserId(userId: String): Flow<List<SocialTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSocialTask(socialTask: SocialTaskEntity)

    @Update
    suspend fun updateSocialTask(socialTask: SocialTaskEntity)

    @Delete
    suspend fun deleteSocialTask(socialTask: SocialTaskEntity)

    @Query("DELETE FROM social_tasks")
    suspend fun deleteAllSocialTasks()
}