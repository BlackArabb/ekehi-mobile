package com.ekehi.network.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.ekehi.network.data.local.dao.MiningSessionDao
import com.ekehi.network.data.local.dao.SocialTaskDao
import com.ekehi.network.data.local.dao.UserProfileDao
import com.ekehi.network.data.repository.UserRepository
import com.ekehi.network.data.repository.MiningRepository
import com.ekehi.network.data.repository.SocialTaskRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncService @Inject constructor(
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
    
    fun syncAllData(userId: String) {
        if (!isNetworkAvailable()) return
        
        scope.launch {
            syncUserProfiles(userId)
            syncMiningSessions(userId)
            syncSocialTasks(userId)
        }
    }
    
    private suspend fun syncUserProfiles(userId: String) {
        try {
            // Get user profile from server
            val result = userRepository.getUserProfile(userId)
            if (result.isSuccess) {
                val profile = result.getOrNull()
                if (profile != null) {
                    // Update local cache
                    userProfileDao.insertUserProfile(profile.toEntity())
                }
            }
        } catch (e: Exception) {
            // Handle sync error
            e.printStackTrace()
        }
    }
    
    private suspend fun syncMiningSessions(userId: String) {
        try {
            // For mining sessions, we might want to sync all sessions for the user
            // This is a simplified approach - in reality, you might want more sophisticated sync logic
        } catch (e: Exception) {
            // Handle sync error
            e.printStackTrace()
        }
    }
    
    private suspend fun syncSocialTasks(userId: String) {
        try {
            // Get social tasks from server
            val result = socialTaskRepository.getSocialTasks()
            if (result.isSuccess) {
                val tasks = result.getOrNull()
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
                        // Mark as completed in local database
                        // This is a simplified approach
                    }
                }
            }
        } catch (e: Exception) {
            // Handle sync error
            e.printStackTrace()
        }
    }
    
    fun schedulePeriodicSync(userId: String) {
        // In a real implementation, you would use WorkManager to schedule periodic sync
        // For now, we'll just sync immediately if network is available
        syncAllData(userId)
    }
}