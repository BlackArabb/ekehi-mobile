package com.ekehi.mobile.data.local.dao

import androidx.room.*
import com.ekehi.mobile.data.local.entities.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles")
    fun getAllUserProfiles(): Flow<List<UserProfileEntity>>

    @Query("SELECT * FROM user_profiles WHERE userId = :userId LIMIT 1")
    fun getUserProfileByUserId(userId: String): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(userProfile: UserProfileEntity)

    @Update
    suspend fun updateUserProfile(userProfile: UserProfileEntity)

    @Delete
    suspend fun deleteUserProfile(userProfile: UserProfileEntity)

    @Query("DELETE FROM user_profiles")
    suspend fun deleteAllUserProfiles()
}