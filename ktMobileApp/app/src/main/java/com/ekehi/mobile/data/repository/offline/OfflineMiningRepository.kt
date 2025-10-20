package com.ekehi.mobile.data.repository.offline

import com.ekehi.mobile.data.local.CacheManager
import com.ekehi.mobile.data.local.dao.MiningSessionDao
import com.ekehi.mobile.data.local.entities.MiningSessionEntity
import com.ekehi.mobile.data.model.MiningSession
import com.ekehi.mobile.data.repository.CachingRepository
import com.ekehi.mobile.data.repository.MiningRepository
import com.ekehi.mobile.network.service.AppwriteService
import com.ekehi.mobile.performance.PerformanceMonitor
import io.appwrite.exceptions.AppwriteException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineMiningRepository @Inject constructor(
    appwriteService: AppwriteService,
    performanceMonitor: PerformanceMonitor,
    private val miningSessionDao: MiningSessionDao,
    private val cacheManager: CacheManager
) : MiningRepository(appwriteService, performanceMonitor) {
    
    private val cachingRepository = object : CachingRepository(cacheManager) {}
    
    fun getOfflineMiningSessions(userId: String): Flow<List<MiningSession>> {
        return miningSessionDao.getMiningSessionsByUserId(userId).map { entities ->
            entities.map { it.toMiningSession() }
        }
    }
    
    suspend fun cacheMiningSession(miningSession: MiningSession) {
        miningSessionDao.insertMiningSession(miningSession.toEntity())
    }
    
    suspend fun syncMiningSession(sessionId: String): Result<MiningSession> {
        return try {
            val result = getMiningSession(sessionId)
            if (result.isSuccess) {
                cacheMiningSession(result.getOrNull()!!)
            }
            result
        } catch (e: AppwriteException) {
            // If online sync fails, we might want to handle this differently
            Result.failure(e)
        }
    }
}

// Extension functions to convert between entity and model
fun MiningSession.toEntity(): MiningSessionEntity {
    return MiningSessionEntity(
        id = this.id,
        userId = this.userId,
        coinsEarned = this.coinsEarned,
        clicksMade = this.clicksMade,
        sessionDuration = this.sessionDuration,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

fun MiningSessionEntity.toMiningSession(): MiningSession {
    return MiningSession(
        id = this.id,
        userId = this.userId,
        coinsEarned = this.coinsEarned,
        clicksMade = this.clicksMade,
        sessionDuration = this.sessionDuration,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}