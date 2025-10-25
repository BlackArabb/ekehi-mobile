package com.ekehi.network.data.repository

import com.ekehi.network.network.service.AppwriteService
import com.ekehi.network.data.model.UserProfile
import com.ekehi.network.performance.PerformanceMonitor
import io.appwrite.models.Document
import io.appwrite.exceptions.AppwriteException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

open class UserRepository @Inject constructor(
    private val appwriteService: AppwriteService,
    private val performanceMonitor: PerformanceMonitor
) {

    suspend fun getUserProfile(userId: String): Result<UserProfile> {
        return withContext(Dispatchers.IO) {
            try {
                val document = appwriteService.databases.getDocument(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.USER_PROFILES_COLLECTION,
                    documentId = userId
                )
                
                val profile = documentToUserProfile(document)
                Result.success(profile)
            } catch (e: AppwriteException) {
                Result.failure(e)
            }
        }
    }

    suspend fun createUserProfile(userId: String, username: String): Result<UserProfile> {
        return withContext(Dispatchers.IO) {
            try {
                val document = appwriteService.databases.createDocument(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.USER_PROFILES_COLLECTION,
                    documentId = userId,
                    data = mapOf(
                        "userId" to userId,
                        "username" to username,
                        "totalCoins" to 0.0,
                        "coinsPerSecond" to 0.0,
                        "autoMiningRate" to 0.0,
                        "miningPower" to 1.0,
                        "referralBonusRate" to 0.0,
                        "currentStreak" to 0,
                        "longestStreak" to 0,
                        "totalReferrals" to 0,
                        "lifetimeEarnings" to 0.0,
                        "dailyMiningRate" to 0.0,
                        "maxDailyEarnings" to 100.0,
                        "todayEarnings" to 0.0,
                        "streakBonusClaimed" to 0
                    )
                )
                
                val profile = documentToUserProfile(document)
                Result.success(profile)
            } catch (e: AppwriteException) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateUserProfile(userId: String, updates: Map<String, Any>): Result<UserProfile> {
        return withContext(Dispatchers.IO) {
            try {
                val document = appwriteService.databases.updateDocument(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.USER_PROFILES_COLLECTION,
                    documentId = userId,
                    data = updates
                )
                
                val profile = documentToUserProfile(document)
                Result.success(profile)
            } catch (e: AppwriteException) {
                Result.failure(e)
            }
        }
    }

    private fun documentToUserProfile(document: Document<*>): UserProfile {
        @Suppress("UNCHECKED_CAST")
        val data = document.data as Map<String, Any>
        
        return UserProfile(
            id = document.id,
            userId = data["userId"] as String,
            username = data["username"] as? String,
            email = data["email"] as? String,
            totalCoins = (data["totalCoins"] as? Number)?.toDouble() ?: 0.0,
            coinsPerSecond = (data["coinsPerSecond"] as? Number)?.toDouble() ?: 0.0,
            autoMiningRate = (data["autoMiningRate"] as? Number)?.toDouble() ?: 0.0,
            miningPower = (data["miningPower"] as? Number)?.toDouble() ?: 1.0,
            referralBonusRate = (data["referralBonusRate"] as? Number)?.toDouble() ?: 0.0,
            currentStreak = (data["currentStreak"] as? Number)?.toInt() ?: 0,
            longestStreak = (data["longestStreak"] as? Number)?.toInt() ?: 0,
            lastLoginDate = data["lastLoginDate"] as? String,
            referralCode = data["referralCode"] as? String,
            referredBy = data["referredBy"] as? String,
            totalReferrals = (data["totalReferrals"] as? Number)?.toInt() ?: 0,
            lifetimeEarnings = (data["lifetimeEarnings"] as? Number)?.toDouble() ?: 0.0,
            dailyMiningRate = (data["dailyMiningRate"] as? Number)?.toDouble() ?: 0.0,
            maxDailyEarnings = (data["maxDailyEarnings"] as? Number)?.toDouble() ?: 100.0,
            todayEarnings = (data["todayEarnings"] as? Number)?.toDouble() ?: 0.0,
            lastMiningDate = data["lastMiningDate"] as? String,
            streakBonusClaimed = (data["streakBonusClaimed"] as? Number)?.toInt() ?: 0,
            createdAt = document.createdAt,
            updatedAt = document.updatedAt
        )
    }
}