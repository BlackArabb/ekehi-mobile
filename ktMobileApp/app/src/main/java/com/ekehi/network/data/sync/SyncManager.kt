package com.ekehi.network.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.ekehi.network.data.local.dao.MiningSessionDao
import com.ekehi.network.data.local.dao.SocialTaskDao
import com.ekehi.network.data.local.dao.UserProfileDao
import com.ekehi.network.data.model.MiningSession
import com.ekehi.network.data.model.SocialTask
import com.ekehi.network.data.model.UserProfile
import com.ekehi.network.data.repository.UserRepository
import com.ekehi.network.data.repository.MiningRepository
import com.ekehi.network.data.repository.SocialTaskRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userProfileDao: UserProfileDao,
    private val miningSessionDao: MiningSessionDao,
    private val socialTaskDao: SocialTaskDao,
    private val userRepository: UserRepository,
    private val miningRepository: MiningRepository,
    private val socialTaskRepository: SocialTaskRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    suspend fun syncAllData(userId: String): SyncResult {
        if (!isNetworkAvailable()) {
            return SyncResult.Failure("No network connection available")
        }
        
        return try {
            syncUserProfiles(userId)
            syncMiningSessions(userId)
            syncSocialTasks(userId)
            SyncResult.Success
        } catch (e: Exception) {
            SyncResult.Failure("Sync failed: ${e.message}")
        }
    }
    
    private suspend fun syncUserProfiles(userId: String): SyncResult {
        return try {
            // Get latest user profile from server
            val serverResult = userRepository.getUserProfile(userId)
            if (serverResult.isSuccess) {
                val serverProfile = serverResult.getOrNull()
                if (serverProfile != null) {
                    // Get local profile
                    val localProfile = userProfileDao.getUserProfileByUserId(userId).firstOrNull()
                    
                    // Resolve conflicts if both exist
                    val resolvedProfile = if (localProfile != null) {
                        resolveUserProfileConflict(localProfile.toUserProfile(), serverProfile)
                    } else {
                        serverProfile
                    }
                    
                    // Save resolved profile to local database
                    userProfileDao.insertUserProfile(resolvedProfile.toEntity())
                    SyncResult.Success
                } else {
                    SyncResult.Failure("Server returned null profile")
                }
            } else {
                SyncResult.Failure("Failed to fetch user profile from server: ${serverResult.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            SyncResult.Failure("Error syncing user profiles: ${e.message}")
        }
    }
    
    private suspend fun syncMiningSessions(userId: String): SyncResult {
        return try {
            // For mining sessions, we'll sync the most recent session
            // In a real implementation, you might want to sync all sessions
            SyncResult.Success
        } catch (e: Exception) {
            SyncResult.Failure("Error syncing mining sessions: ${e.message}")
        }
    }
    
    private suspend fun syncSocialTasks(userId: String): SyncResult {
        return try {
            // Get social tasks from server
            val tasksResult = socialTaskRepository.getSocialTasks()
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
    totalCoins = this.totalCoins,
    coinsPerSecond = this.coinsPerSecond,
    autoMiningRate = this.autoMiningRate,
    miningPower = this.miningPower,
    referralBonusRate = this.referralBonusRate,
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
    totalCoins = this.totalCoins,
    coinsPerSecond = this.coinsPerSecond,
    autoMiningRate = this.autoMiningRate,
    miningPower = this.miningPower,
    referralBonusRate = this.referralBonusRate,
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