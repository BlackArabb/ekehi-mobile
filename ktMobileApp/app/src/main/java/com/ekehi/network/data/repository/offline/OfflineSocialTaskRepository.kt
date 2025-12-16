package com.ekehi.network.data.repository.offline

import com.ekehi.network.data.local.CacheManager
import com.ekehi.network.data.local.dao.SocialTaskDao
import com.ekehi.network.data.local.entities.SocialTaskEntity
import com.ekehi.network.data.model.SocialTask
import com.ekehi.network.data.model.UserSocialTask
import com.ekehi.network.data.repository.CachingRepository
import com.ekehi.network.data.repository.SocialTaskRepository
import com.ekehi.network.domain.verification.SocialVerificationService
import com.ekehi.network.service.AppwriteService
import com.ekehi.network.performance.PerformanceMonitor
import io.appwrite.exceptions.AppwriteException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineSocialTaskRepository @Inject constructor(
    appwriteService: AppwriteService,
    performanceMonitor: PerformanceMonitor,
    socialVerificationService: SocialVerificationService,
    private val socialTaskDao: SocialTaskDao,
    private val cacheManager: CacheManager
) : SocialTaskRepository(appwriteService, performanceMonitor, socialVerificationService) {
    
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
    
    // Remove the override methods since they're final in the parent class
    // We'll use the parent's implementation directly
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