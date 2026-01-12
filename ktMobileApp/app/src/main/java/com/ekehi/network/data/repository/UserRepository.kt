package com.ekehi.network.data.repository

import android.util.Log
import com.ekehi.network.service.AppwriteService
import com.ekehi.network.data.model.UserProfile
import com.ekehi.network.data.model.Referral
import com.ekehi.network.performance.PerformanceMonitor
import com.ekehi.network.security.SecurePreferences
import io.appwrite.models.Document
import io.appwrite.exceptions.AppwriteException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random

open class UserRepository @Inject constructor(
    private val appwriteService: AppwriteService,
    private val performanceMonitor: PerformanceMonitor,
    private val securePreferences: SecurePreferences
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
                        io.appwrite.Query.equal("userId", listOf(userId))
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

    suspend fun createUserProfile(userId: String, username: String, email: String? = null, phoneNumber: String = "", country: String = ""): Result<UserProfile> {
        Log.d("UserRepository", "Creating user profile for userId: $userId, username: $username, email: $email, phone: $phoneNumber, country: $country")
        return withContext(Dispatchers.IO) {
            try {
                // Generate a unique referral code in EKH + 6-digit format
                val referralCode = "EKH${Random.nextInt(100000, 999999)}"
                    
                // Get current timestamp
                val currentTimestamp = java.time.Instant.now().toString()
                    
                Log.d("UserRepository", "About to create Appwrite document in database: ${AppwriteService.DATABASE_ID}, collection: ${AppwriteService.USER_PROFILES_COLLECTION}")
                    
                val document = appwriteService.databases.createDocument(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.USER_PROFILES_COLLECTION,
                    documentId = "unique()",
                    data = mapOf(
                        "userId" to listOf(userId),
                        "username" to username,
                        "currentStreak" to 0,
                        "longestStreak" to 0,
                        "lastLoginDate" to null,
                        "referralCode" to listOf(referralCode),
                        "referredBy" to null,
                        "totalReferrals" to 0,
                        "lastMiningDate" to null,
                        "streakBonusClaimed" to 0,
                        "dailyMiningRate" to 2.0,
                        "walletAddress" to null,
                        "country" to country,
                        "phoneNumber" to phoneNumber,
                        "taskReward" to 0.0,
                        "miningReward" to 0L,
                        "referralReward" to 0.0,
                        "autoMiningRate" to 0.0,
                        "todayEarnings" to 0.0,
                        "lifetimeEarnings" to 0.0,
                        "maxDailyEarnings" to 10000.0,
                        "email" to email,
                        "createdAt" to currentTimestamp,
                        "updatedAt" to currentTimestamp
                    )
                )
                    
                Log.d("UserRepository", "Appwrite document created successfully with ID: ${document.id}")
                
                val profile = documentToUserProfile(document)
                
                Log.d("UserRepository", "Profile converted successfully, username: ${profile.username}")
                
                // Check if there's a stored referral code to claim
                val storedReferralCode = securePreferences.getString("referral_code", null)
                if (!storedReferralCode.isNullOrEmpty()) {
                    Log.d("UserRepository", "Found stored referral code: $storedReferralCode")
                    // Claim the referral code for this new user
                    claimReferral(userId, storedReferralCode)
                    // Clear the stored referral code
                    securePreferences.remove("referral_code")
                }
                
                Log.d("UserRepository", "User profile creation completed successfully for userId: $userId")
                Result.success(profile)
            } catch (e: AppwriteException) {
                Log.e("UserRepository", "Appwrite exception while creating profile: ${e.message}", e)
                Log.e("UserRepository", "Appwrite error details - Code: ${e.code}, Message: ${e.message}, Type: ${e.type}")
                Result.failure(e)
            } catch (e: Exception) {
                Log.e("UserRepository", "Unexpected exception while creating profile: ${e.message}", e)
                Log.e("UserRepository", "Error type: ${e::class.simpleName}")
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
                        io.appwrite.Query.equal("userId", listOf(userId))
                    )
                )
                
                Log.d("UserRepository", "Found ${response.documents.size} documents for userId: $userId")
                
                if (response.documents.isNotEmpty()) {
                    val documentId = response.documents[0].id
                    Log.d("UserRepository", "Found document with ID: $documentId")
                    
                    // Add updatedAt timestamp to the updates
                    val updatesWithTimestamp = updates.toMutableMap()
                    updatesWithTimestamp["updatedAt"] = java.time.Instant.now().toString()
                    
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
                updatesWithTimestamp["updatedAt"] = java.time.Instant.now().toString()
                
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

    suspend fun getReferralsByReferrerId(referrerId: String): List<Referral> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("UserRepository", "Attempting to fetch referrals for referrerId: $referrerId")
                
                // Fetch user profile to get referral code
                val response = appwriteService.databases.listDocuments(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.USER_PROFILES_COLLECTION,
                    queries = listOf(
                        io.appwrite.Query.equal("referredBy", listOf(referrerId))
                    )
                )
                
                Log.d("UserRepository", "Found ${response.documents.size} referrals for referrerId: $referrerId")
                
                val referrals = response.documents.map { document ->
                    @Suppress("UNCHECKED_CAST")
                    val data = document.data as Map<String, Any>
                    
                    // Get user details
                    val referredUserId = when (val userIdValue = data["userId"]) {
                        is List<*> -> userIdValue.firstOrNull() as? String ?: ""
                        is String -> userIdValue
                        else -> ""
                    }
                    
                    val referredUserName = data["username"] as? String
                    
                    Referral(
                        id = document.id ?: "",
                        referrerId = referrerId,
                        referredUserId = referredUserId,
                        referredUserName = referredUserName,
                        referralCode = data["referralCode"] as? String ?: "",
                        rewardAmount = 2.0, // Fixed reward amount
                        rewardClaimed = true, // For now, marking as claimed
                        createdAt = parseTimestamp(data["createdAt"] as? String ?: ""),
                        claimedAt = parseTimestamp(data["updatedAt"] as? String ?: "")
                    )
                }
                
                referrals
            } catch (e: Exception) {
                Log.e("UserRepository", "Error fetching referrals", e)
                emptyList()
            }
        }
    }

    /**
     * Claims a referral code for the current user
     * @param userId The ID of the user claiming the referral
     * @param referralCode The referral code to claim
     * @return Result indicating success or failure with a message
     */
    suspend fun claimReferral(userId: String, referralCode: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("UserRepository", "Attempting to claim referral code: $referralCode for user: $userId")
                
                // Validate input
                if (referralCode.isBlank()) {
                    return@withContext Result.failure(Exception("Referral code cannot be empty"))
                }
                
                // First, check if the current user already has a referredBy field
                val currentUserResponse = appwriteService.databases.listDocuments(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.USER_PROFILES_COLLECTION,
                    queries = listOf(
                        io.appwrite.Query.equal("userId", listOf(userId))
                    )
                )
                
                if (currentUserResponse.documents.isEmpty()) {
                    return@withContext Result.failure(Exception("User profile not found"))
                }
                
                val currentUserDoc = currentUserResponse.documents[0]
                @Suppress("UNCHECKED_CAST")
                val currentUserData = currentUserDoc.data as Map<String, Any>
                
                // Check if user has already been referred
                val alreadyReferred = currentUserData["referredBy"] as? String
                if (!alreadyReferred.isNullOrEmpty()) {
                    return@withContext Result.failure(Exception("You have already been referred"))
                }
                
                // Find the user with this referral code
                val response = appwriteService.databases.listDocuments(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.USER_PROFILES_COLLECTION,
                    queries = listOf(
                        io.appwrite.Query.equal("referralCode", listOf(referralCode.trim()))
                    )
                )
                
                if (response.documents.isEmpty()) {
                    return@withContext Result.failure(Exception("Invalid referral code"))
                }
                
                val referrerDoc = response.documents[0]
                @Suppress("UNCHECKED_CAST")
                val referrerData = referrerDoc.data as Map<String, Any>
                
                // Make sure the user is not referring themselves
                val referrerUserId = when (val referrerUserIdValue = referrerData["userId"]) {
                    is List<*> -> referrerUserIdValue.firstOrNull() as? String ?: ""
                    is String -> referrerUserIdValue
                    else -> ""
                }
                if (referrerUserId == userId) {
                    return@withContext Result.failure(Exception("You cannot refer yourself"))
                }
                
                // Update referrer's profile with referral count and fixed reward
                val currentTotalReferrals = (referrerData["totalReferrals"] as? Number)?.toInt() ?: 0
                val currentTaskReward = (referrerData["taskReward"] as? Number)?.toDouble() ?: 0.0
                
                // Check if referrer has reached max referrals (50)
                if (currentTotalReferrals >= 50) {
                    return@withContext Result.failure(Exception("This referral code has reached the maximum number of referrals"))
                }
                
                // Update referrer's profile - give fixed 2 EKH reward to referralReward
                val currentReferrerReferralReward = (referrerData["referralReward"] as? Number)?.toDouble() ?: 0.0
                
                appwriteService.databases.updateDocument(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.USER_PROFILES_COLLECTION,
                    documentId = referrerDoc.id,
                    data = mapOf(
                        "referralReward" to (currentReferrerReferralReward + 2.0), // Fixed 2 EKH reward for referrer
                        "totalReferrals" to (currentTotalReferrals + 1),
                        "updatedAt" to java.time.Instant.now().toString()
                    )
                )
                                    
                // Update current user's profile with referredBy field and give them 2 EKH as referral reward
                val currentUserReferralReward = (currentUserData["referralReward"] as? Number)?.toDouble() ?: 0.0
                                    
                appwriteService.databases.updateDocument(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.USER_PROFILES_COLLECTION,
                    documentId = currentUserDoc.id,
                    data = mapOf(
                        "referredBy" to referrerUserId,
                        "referralReward" to (currentUserReferralReward + 2.0), // 2 EKH for referee as referral reward
                        "updatedAt" to java.time.Instant.now().toString()
                    )
                )
                
                Log.d("UserRepository", "Successfully claimed referral code: $referralCode")
                return@withContext Result.success("Referral claimed successfully! You received 2 EKH.")
            } catch (e: Exception) {
                Log.e("UserRepository", "Error claiming referral", e)
                return@withContext Result.failure(e)
            }
        }
    }

    // Helper method to convert Appwrite document to Referral model
    private fun documentToReferral(document: Document<*>): Referral {
        @Suppress("UNCHECKED_CAST")
        val data = document.data as Map<String, Any>
        
        return Referral(
            id = document.id ?: "",
            referrerId = data["referrerId"] as? String ?: "",
            referredUserId = data["referredUserId"] as? String ?: "",
            referredUserName = data["referredUserName"] as? String,
            referralCode = when (val referralCodeValue = data["referralCode"]) {
                is List<*> -> referralCodeValue.firstOrNull() as? String ?: ""
                is String -> referralCodeValue
                else -> ""
            },
            rewardAmount = (data["rewardAmount"] as? Number)?.toDouble() ?: 2.0,
            rewardClaimed = data["rewardClaimed"] as? Boolean ?: false,
            createdAt = (data["createdAt"] as? String)?.let { parseTimestamp(it) },
            claimedAt = (data["claimedAt"] as? String)?.let { parseTimestamp(it) }
        )
    }

    private fun parseTimestamp(timestamp: String): Long? {
        return try {
            java.time.LocalDateTime.parse(timestamp.replace("Z", ""), java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME).atZone(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
        } catch (e: Exception) {
            try {
                // Alternative parsing for different formats
                java.time.Instant.parse(timestamp).toEpochMilli()
            } catch (ex: Exception) {
                null
            }
        }
    }
    
    private fun documentToUserProfile(document: Document<*>): UserProfile {
        @Suppress("UNCHECKED_CAST")
        val data = document.data as Map<String, Any>
        
        return UserProfile(
            id = document.id ?: "",
            userId = when (val userIdValue = data["userId"]) {
                is List<*> -> userIdValue.firstOrNull() as? String ?: ""
                is String -> userIdValue
                else -> ""
            },
            username = data["username"] as? String,
            email = data["email"] as? String,
            phoneNumber = data["phoneNumber"] as? String ?: "",
            country = data["country"] as? String ?: "",
            taskReward = (data["taskReward"] as? Number)?.toFloat() ?: 0.0f,
            miningReward = (data["miningReward"] as? Number)?.toFloat() ?: 0.0f,
            referralReward = (data["referralReward"] as? Number)?.toFloat() ?: 0.0f,
            autoMiningRate = (data["autoMiningRate"] as? Number)?.toFloat() ?: 0.0f,
            miningPower = 0.0f,
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
            lifetimeEarnings = (data["lifetimeEarnings"] as? Number)?.toFloat() ?: 0.0f,
            dailyMiningRate = (data["dailyMiningRate"] as? Number)?.toFloat() ?: 2.0f,
            maxDailyEarnings = (data["maxDailyEarnings"] as? Number)?.toFloat() ?: 10000.0f,
            todayEarnings = (data["todayEarnings"] as? Number)?.toFloat() ?: 0.0f,
            lastMiningDate = data["lastMiningDate"] as? String,
            streakBonusClaimed = (data["streakBonusClaimed"] as? Number)?.toInt() ?: 0,
            createdAt = document.createdAt ?: "1970-01-01T00:00:00.000Z",
            updatedAt = document.updatedAt ?: "1970-01-01T00:00:00.000Z"
        )
    }
}
