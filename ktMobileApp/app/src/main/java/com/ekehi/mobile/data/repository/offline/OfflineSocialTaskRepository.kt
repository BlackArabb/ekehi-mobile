package com.ekehi.mobile.data.repository.offline

import com.ekehi.mobile.data.local.CacheManager
import com.ekehi.mobile.data.local.dao.SocialTaskDao
import com.ekehi.mobile.data.local.entities.SocialTaskEntity
import com.ekehi.mobile.data.model.SocialTask
import com.ekehi.mobile.data.repository.CachingRepository
import com.ekehi.mobile.data.repository.SocialTaskRepository
import com.ekehi.mobile.network.service.AppwriteService
import com.ekehi.mobile.performance.PerformanceMonitor
import io.appwrite.exceptions.AppwriteException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineSocialTaskRepository @Inject constructor(
    appwriteService: AppwriteService,
    performanceMonitor: PerformanceMonitor,
    private val socialTaskDao: SocialTaskDao,
    private val cacheManager: CacheManager
) : SocialTaskRepository(appwriteService, performanceMonitor) {
    
    private val cachingRepository = object : CachingRepository(cacheManager) {}
    
    fun getOfflineSocialTasks(userId: String): Flow<List<SocialTask>> {
        return socialTaskDao.getSocialTasksByUserId(userId).map { entities ->
            entities.map { it.toSocialTask() }
        }
    }
    
    fun getOfflineIncompleteSocialTasks(userId: String): Flow<List<SocialTask>> {
        return socialTaskDao.getIncompleteSocialTasksByUserId(userId).map { entities ->
            entities.map { it.toSocialTask() }
        }
    }
    
    suspend fun cacheSocialTask(socialTask: SocialTask, userId: String) {
        socialTaskDao.insertSocialTask(socialTask.toEntity(userId))
    }
    
    suspend fun syncSocialTasks(): Result<List<SocialTask>> {
        return try {
            val result = getSocialTasks()
            if (result.isSuccess) {
                result.getOrNull()?.forEach { task ->
                    // We need to associate with a userId, but for global tasks this might be empty
                    cacheSocialTask(task, "")
                }
            }
            result
        } catch (e: AppwriteException) {
            // If online sync fails, return cached data
            Result.failure(e)
        }
    }
}

// Extension functions to convert between entity and model
fun SocialTask.toEntity(userId: String): SocialTaskEntity {
    return SocialTaskEntity(
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
        createdAt = this.createdAt, // Use actual createdAt from SocialTask
        updatedAt = this.updatedAt  // Use actual updatedAt from SocialTask
    )
}

fun SocialTaskEntity.toSocialTask(): SocialTask {
    return SocialTask(
        id = this.id,
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
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}