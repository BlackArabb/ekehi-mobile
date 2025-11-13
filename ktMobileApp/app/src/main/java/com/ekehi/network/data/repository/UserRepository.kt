package com.ekehi.network.data.repository

import android.util.Log
import com.ekehi.network.service.AppwriteService
import com.ekehi.network.data.model.UserProfile
import com.ekehi.network.performance.PerformanceMonitor
import io.appwrite.models.Document
import io.appwrite.exceptions.AppwriteException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random

open class UserRepository @Inject constructor(
    private val appwriteService: AppwriteService,
    private val performanceMonitor: PerformanceMonitor
) {

    suspend fun getUserProfile(userId: String): Result<UserProfile> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("UserRepository", "Attempting to fetch profile for userId: $userId")
                
                // Fetch user profile from Appwrite database using userId field (like React Native app)
                val response = appwriteService.databases.listDocuments(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.USER_PROFILES_COLLECTION,
                    queries = listOf(
                        io.appwrite.Query.equal("userId", userId)
                    )
                )
                
                Log.d("UserRepository", "Found ${response.documents.size} documents for userId: $userId")
                
                if (response.documents.isNotEmpty()) {
                    val profile = documentToUserProfile(response.documents[0])
                    Log.d("UserRepository", "Successfully fetched profile for user: $userId")
                    Result.success(profile)
                } else {
                    val errorMessage = "User profile not found for userId: $userId"
                    Log.e("UserRepository", errorMessage)
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: AppwriteException) {
                val errorMessage = "Appwrite exception while fetching profile: ${e.message}"
                Log.e("UserRepository", errorMessage, e)
                Result.failure(e)
            } catch (e: Exception) {
                val errorMessage = "Unexpected error while fetching profile: ${e.message}"
                Log.e("UserRepository", errorMessage, e)
                Result.failure(e)
            }
        }
    }

    suspend fun createUserProfile(userId: String, username: String): Result<UserProfile> {
        return withContext(Dispatchers.IO) {
            try {
                // Generate a unique referral code
                val referralCode = "REF${Random.nextInt(100000, 999999)}"
                
                val document = appwriteService.databases.createDocument(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.USER_PROFILES_COLLECTION,
                    documentId = "unique()",
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
                        "streakBonusClaimed" to 0,
                        "referralCode" to referralCode
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
                Log.d("UserRepository", "Attempting to find profile for userId: $userId")
                
                // First, find the user profile document by userId field
                val response = appwriteService.databases.listDocuments(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.USER_PROFILES_COLLECTION,
                    queries = listOf(
                        io.appwrite.Query.equal("userId", userId)
                    )
                )
                
                Log.d("UserRepository", "Found ${response.documents.size} documents for userId: $userId")
                
                if (response.documents.isNotEmpty()) {
                    val documentId = response.documents[0].id
                    Log.d("UserRepository", "Found document with ID: $documentId")
                    
                    // Add updatedAt timestamp to the updates
                    val updatesWithTimestamp = updates.toMutableMap()
                    updatesWithTimestamp["updatedAt"] = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).format(java.util.Date())
                    
                    val document = appwriteService.databases.updateDocument(
                        databaseId = AppwriteService.DATABASE_ID,
                        collectionId = AppwriteService.USER_PROFILES_COLLECTION,
                        documentId = documentId,
                        data = updatesWithTimestamp
                    )
                    
                    val profile = documentToUserProfile(document)
                    Log.d("UserRepository", "Successfully updated profile for user: $userId")
                    Result.success(profile)
                } else {
                    val errorMessage = "User profile not found for userId: $userId"
                    Log.e("UserRepository", errorMessage)
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: AppwriteException) {
                val errorMessage = "Appwrite exception while updating profile: ${e.message}"
                Log.e("UserRepository", errorMessage, e)
                Result.failure(e)
            } catch (e: Exception) {
                val errorMessage = "Unexpected error while updating profile: ${e.message}"
                Log.e("UserRepository", errorMessage, e)
                Result.failure(e)
            }
        }
    }

    suspend fun updateUserProfileByDocumentId(documentId: String, updates: Map<String, Any>): Result<UserProfile> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("UserRepository", "Updating profile by document ID: $documentId")
                
                // Add updatedAt timestamp to the updates
                val updatesWithTimestamp = updates.toMutableMap()
                updatesWithTimestamp["updatedAt"] = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).format(java.util.Date())
                
                val document = appwriteService.databases.updateDocument(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.USER_PROFILES_COLLECTION,
                    documentId = documentId,
                    data = updatesWithTimestamp
                )
                
                val profile = documentToUserProfile(document)
                Log.d("UserRepository", "Successfully updated profile by document ID: $documentId")
                Result.success(profile)
            } catch (e: AppwriteException) {
                val errorMessage = "Appwrite exception while updating profile by document ID: ${e.message}"
                Log.e("UserRepository", errorMessage, e)
                Result.failure(e)
            } catch (e: Exception) {
                val errorMessage = "Unexpected error while updating profile by document ID: ${e.message}"
                Log.e("UserRepository", errorMessage, e)
                Result.failure(e)
            }
        }
    }

    private fun documentToUserProfile(document: Document<*>): UserProfile {
        @Suppress("UNCHECKED_CAST")
        val data = document.data as Map<String, Any>
        
        return UserProfile(
            id = document.id ?: "",
            userId = data["userId"] as? String ?: "",
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
            referralCode = when (val referralCodeValue = data["referralCode"]) {
                is List<*> -> referralCodeValue.firstOrNull() as? String
                is String -> referralCodeValue
                else -> null
            },
            referredBy = data["referredBy"] as? String,
            totalReferrals = (data["totalReferrals"] as? Number)?.toInt() ?: 0,
            lifetimeEarnings = (data["lifetimeEarnings"] as? Number)?.toDouble() ?: 0.0,
            dailyMiningRate = (data["dailyMiningRate"] as? Number)?.toDouble() ?: 0.0,
            maxDailyEarnings = (data["maxDailyEarnings"] as? Number)?.toDouble() ?: 100.0,
            todayEarnings = (data["todayEarnings"] as? Number)?.toDouble() ?: 0.0,
            lastMiningDate = data["lastMiningDate"] as? String,
            streakBonusClaimed = (data["streakBonusClaimed"] as? Number)?.toInt() ?: 0,
            createdAt = document.createdAt ?: "1970-01-01T00:00:00.000Z",
            updatedAt = document.updatedAt ?: "1970-01-01T00:00:00.000Z"
        )
    }
}