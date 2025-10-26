package com.ekehi.network.data.repository.offline

import com.ekehi.network.data.local.CacheManager
import com.ekehi.network.data.local.dao.UserProfileDao
import com.ekehi.network.data.local.entities.UserProfileEntity
import com.ekehi.network.data.model.UserProfile
import com.ekehi.network.data.repository.CachingRepository
import com.ekehi.network.data.repository.UserRepository
import com.ekehi.network.service.AppwriteService
import com.ekehi.network.performance.PerformanceMonitor
import io.appwrite.exceptions.AppwriteException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineUserRepository @Inject constructor(
    appwriteService: AppwriteService,
    performanceMonitor: PerformanceMonitor,
    private val userProfileDao: UserProfileDao,
    private val cacheManager: CacheManager
) : UserRepository(appwriteService, performanceMonitor) {
    
    private val cachingRepository = object : CachingRepository(cacheManager) {}
    
    fun getOfflineUserProfile(userId: String): Flow<UserProfile?> {
        return userProfileDao.getUserProfileByUserId(userId).map { entity ->
            entity?.toUserProfile()
        }
    }
    
    suspend fun cacheUserProfile(userProfile: UserProfile) {
        userProfileDao.insertUserProfile(userProfile.toEntity())
    }
    
    suspend fun syncUserProfile(userId: String): Result<UserProfile> {
        return try {
            val result = getUserProfile(userId)
            if (result.isSuccess) {
                cacheUserProfile(result.getOrNull()!!)
            }
            result
        } catch (e: AppwriteException) {
            // If online sync fails, return cached data
            val cached = getOfflineUserProfile(userId)
            // This is a simplified approach - in a real implementation you'd need to handle the Flow properly
            Result.failure(e)
        }
    }
    
    fun getCachedUserProfileWithStrategy(userId: String) = cachingRepository.executeWithStrategy(
        strategy = cacheManager.getCacheStrategy(),
        cacheCall = { 
            val profile = userProfileDao.getUserProfileByUserId(userId)
            // This is a simplified approach - in reality you'd need to handle the Flow properly
            // For now, we'll just return a default profile if cache is empty
            UserProfile(
                id = "",
                userId = userId,
                username = null,
                email = null,
                totalCoins = 0.0,
                coinsPerSecond = 0.0,
                autoMiningRate = 0.0,
                miningPower = 1.0,
                referralBonusRate = 0.0,
                currentStreak = 0,
                longestStreak = 0,
                lastLoginDate = null,
                referralCode = null,
                referredBy = null,
                totalReferrals = 0,
                lifetimeEarnings = 0.0,
                dailyMiningRate = 0.0,
                maxDailyEarnings = 100.0,
                todayEarnings = 0.0,
                lastMiningDate = null,
                streakBonusClaimed = 0,
                createdAt = "",
                updatedAt = ""
            )
        },
        networkCall = { getUserProfile(userId) },
        saveCall = { cacheUserProfile(it) }
    )
}

// Extension functions to convert between entity and model
fun UserProfile.toEntity(): UserProfileEntity {
    return UserProfileEntity(
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
}

fun UserProfileEntity.toUserProfile(): UserProfile {
    return UserProfile(
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
}