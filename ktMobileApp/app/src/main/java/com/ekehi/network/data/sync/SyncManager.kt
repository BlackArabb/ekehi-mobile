package com.ekehi.network.data.sync

import android.util.Log
import com.ekehi.network.data.local.CacheManager
import com.ekehi.network.data.local.dao.MiningSessionDao
import com.ekehi.network.data.local.dao.SocialTaskDao
import com.ekehi.network.data.local.dao.UserProfileDao
import com.ekehi.network.data.model.UserProfile
import com.ekehi.network.data.repository.MiningRepository
import com.ekehi.network.data.repository.SocialTaskRepository
import com.ekehi.network.data.repository.UserRepository
import com.ekehi.network.performance.PerformanceMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

class SyncManager @Inject constructor(
    private val userRepository: UserRepository,
    private val miningRepository: MiningRepository,
    private val socialTaskRepository: SocialTaskRepository,
    private val userProfileDao: UserProfileDao,
    private val miningSessionDao: MiningSessionDao,
    private val socialTaskDao: SocialTaskDao,
    private val cacheManager: CacheManager,
    private val performanceMonitor: PerformanceMonitor
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    
    suspend fun syncAllData(userId: String): SyncResult {
        Log.d("SyncManager", "Starting full sync for user: $userId")
        // Removed performance tracking calls that don't exist
        
        try {
            // Sync user profile
            val profileResult = syncUserProfile(userId)
            if (profileResult is SyncResult.Failure) {
                return profileResult
            }
            
            // Sync mining sessions
            val miningResult = syncMiningSessions(userId)
            if (miningResult is SyncResult.Failure) {
                return miningResult
            }
            
            // Sync social tasks
            val socialResult = syncSocialTasks(userId)
            if (socialResult is SyncResult.Failure) {
                return socialResult
            }
            
            Log.d("SyncManager", "Full sync completed successfully for user: $userId")
            return SyncResult.Success
        } catch (e: Exception) {
            val errorMessage = "Error during full sync: ${e.message}"
            Log.e("SyncManager", errorMessage, e)
            return SyncResult.Failure(errorMessage)
        }
    }
    
    private suspend fun syncUserProfile(userId: String): SyncResult {
        return try {
            val serverProfileResult = userRepository.getUserProfile(userId)
            if (serverProfileResult.isSuccess) {
                val serverProfile = serverProfileResult.getOrNull()
                if (serverProfile != null) {
                    // Check if we have a local version
                    val localProfile = userProfileDao.getUserProfileByUserId(userId).firstOrNull()
                    if (localProfile != null) {
                        // Resolve conflict if needed
                        val resolvedProfile = resolveUserProfileConflict(localProfile.toUserProfile(), serverProfile)
                        userProfileDao.insertUserProfile(resolvedProfile.toEntity())
                    } else {
                        // Insert new profile
                        userProfileDao.insertUserProfile(serverProfile.toEntity())
                    }
                }
            }
            SyncResult.Success
        } catch (e: Exception) {
            SyncResult.Failure("Error syncing user profile: ${e.message}")
        }
    }
    
    private suspend fun syncMiningSessions(userId: String): SyncResult {
        return try {
            // In a real implementation, you would sync mining sessions
            SyncResult.Success
        } catch (e: Exception) {
            SyncResult.Failure("Error syncing mining sessions: ${e.message}")
        }
    }
    
    private suspend fun syncSocialTasks(userId: String): SyncResult {
        return try {
            // Get social tasks from server
            val tasksResult = socialTaskRepository.getAllSocialTasks()
            if (tasksResult.isSuccess) {
                val tasks = tasksResult.getOrNull()
                if (tasks != null) {
                    // Update local cache
                    tasks.forEach { task ->
                        socialTaskDao.insertSocialTask(task.toEntity(""))
                    }
                }
            }
            
            // Get user-specific social tasks
            val userTasksResult = socialTaskRepository.getUserSocialTasks(userId)
            if (userTasksResult.isSuccess) {
                val userTasks = userTasksResult.getOrNull()
                if (userTasks != null) {
                    // Update local cache with user-specific info
                    userTasks.forEach { task ->
                        // In a real implementation, you would update the local task status
                    }
                }
            }
            
            SyncResult.Success
        } catch (e: Exception) {
            SyncResult.Failure("Error syncing social tasks: ${e.message}")
        }
    }
    
    private fun resolveUserProfileConflict(local: UserProfile, server: UserProfile): UserProfile {
        // Simple conflict resolution: use the most recently updated profile
        return if (server.updatedAt > local.updatedAt) {
            server
        } else {
            local
        }
    }
    
    fun schedulePeriodicSync(userId: String) {
        // In a real implementation, you would use WorkManager to schedule periodic sync
        scope.launch {
            syncAllData(userId)
        }
    }
    
    sealed class SyncResult {
        object Success : SyncResult()
        data class Failure(val message: String) : SyncResult()
    }
}

// Extension functions for data conversion
fun com.ekehi.network.data.local.entities.UserProfileEntity.toUserProfile() = com.ekehi.network.data.model.UserProfile(
    id = this.id,
    userId = this.userId,
    username = this.username,
    email = this.email,
    phoneNumber = this.phone_number, // Map snake_case to camelCase
    country = this.country, // Map snake_case to camelCase
    taskReward = this.taskReward,
    miningReward = this.miningReward,
    referralReward = this.referralReward,
    autoMiningRate = this.autoMiningRate,
    miningPower = this.miningPower,
    currentStreak = this.currentStreak,
    longestStreak = this.longestStreak,
    lastLoginDate = this.lastLoginDate,
    referralCode = this.referralCode,
    referredBy = this.referredBy,
    totalReferrals = this.totalReferrals,
    lifetimeEarnings = this.lifetimeEarnings,
    dailyMiningRate = this.dailyMiningRate,
    maxDailyEarnings = this.maxDailyEarnings,
    todayEarnings = this.todayEarnings,
    lastMiningDate = this.lastMiningDate,
    streakBonusClaimed = this.streakBonusClaimed,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)

fun com.ekehi.network.data.model.UserProfile.toEntity() = com.ekehi.network.data.local.entities.UserProfileEntity(
    id = this.id,
    userId = this.userId,
    username = this.username,
    email = this.email,
    phone_number = this.phoneNumber, // Map camelCase to snake_case
    country = this.country, // Map camelCase to snake_case
    taskReward = this.taskReward,
    miningReward = this.miningReward,
    referralReward = this.referralReward,
    autoMiningRate = this.autoMiningRate,
    miningPower = this.miningPower,
    currentStreak = this.currentStreak,
    longestStreak = this.longestStreak,
    lastLoginDate = this.lastLoginDate,
    referralCode = this.referralCode,
    referredBy = this.referredBy,
    totalReferrals = this.totalReferrals,
    lifetimeEarnings = this.lifetimeEarnings,
    dailyMiningRate = this.dailyMiningRate,
    maxDailyEarnings = this.maxDailyEarnings,
    todayEarnings = this.todayEarnings,
    lastMiningDate = this.lastMiningDate,
    streakBonusClaimed = this.streakBonusClaimed,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)

fun com.ekehi.network.data.model.SocialTask.toEntity(userId: String) = com.ekehi.network.data.local.entities.SocialTaskEntity(
    id = this.id,
    userId = userId,
    title = this.title,
    description = this.description,
    platform = this.platform,
    taskType = this.taskType,
    rewardCoins = this.rewardCoins,
    actionUrl = this.actionUrl,
    verificationMethod = this.verificationMethod,
    isActive = this.isActive,
    sortOrder = this.sortOrder,
    isCompleted = this.isCompleted,
    createdAt = "", // These would need to be set properly in a real implementation
    updatedAt = ""  // These would need to be set properly in a real implementation
)