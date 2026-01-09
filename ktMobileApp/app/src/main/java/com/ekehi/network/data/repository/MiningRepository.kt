package com.ekehi.network.data.repository

import android.content.Context
import android.util.Log
import com.ekehi.network.service.AppwriteService
import com.ekehi.network.data.model.MiningSession
import com.ekehi.network.performance.PerformanceMonitor
import io.appwrite.models.Document
import io.appwrite.exceptions.AppwriteException
import io.appwrite.ID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

open class MiningRepository @Inject constructor(
        private val appwriteService: AppwriteService,
        private val performanceMonitor: PerformanceMonitor,
        private val context: Context
) {

    companion object {
        private const val TAG = "MiningRepository"
        private const val PREFS_NAME = "mining_prefs"
        private const val KEY_MINING_SESSION = "miningSession"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Starts a 24-hour mining session
     * Stores startTime locally (like AsyncStorage in React Native)
     */
    suspend fun startMining(userId: String): Result<MiningSessionData> {
        return withContext(Dispatchers.IO) {
            try {
                val startTime = System.currentTimeMillis()
                val duration = 24 * 60 * 60 * 1000L // 24 hours in milliseconds
                val reward = 2.0 // 2 EKH

                // Save to local storage (like AsyncStorage)
                val sessionData = MiningSessionData(
                        userId = userId,
                        startTime = startTime,
                        duration = duration,
                        reward = reward,
                        finalRewardClaimed = false
                )
                saveMiningSession(sessionData)

                Log.d(TAG, "Mining session started: $sessionData")
                Result.success(sessionData)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start mining", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Checks for ongoing mining session (like checkOngoingMiningSession in React Native)
     * Calculates elapsed time and remaining time
     */
    suspend fun checkOngoingMiningSession(): Result<MiningSessionStatus?> {
        return withContext(Dispatchers.IO) {
            try {
                val savedSession = getMiningSession()

                if (savedSession == null) {
                    return@withContext Result.success(null)
                }

                val now = System.currentTimeMillis()
                val elapsed = now - savedSession.startTime
                val elapsedSeconds = (elapsed / 1000).toInt()
                val remainingMs = savedSession.duration - elapsed
                val remainingSeconds = (remainingMs / 1000).toInt().coerceAtLeast(0)

                val progress = (elapsed.toDouble() / savedSession.duration.toDouble()).coerceIn(0.0, 1.0)
                val isComplete = elapsed >= savedSession.duration

                val status = MiningSessionStatus(
                        userId = savedSession.userId,
                        startTime = savedSession.startTime,
                        duration = savedSession.duration,
                        reward = savedSession.reward,
                        remainingSeconds = remainingSeconds,
                        progress = progress,
                        isComplete = isComplete,
                        finalRewardClaimed = savedSession.finalRewardClaimed
                )

                Log.d(TAG, "Mining session status: remaining=${remainingSeconds}s, progress=${progress}, isComplete=$isComplete")
                Result.success(status)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to check mining session", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Claims the final 2 EKH reward after 24 hours
     * Updates user profile and marks reward as claimed
     */
    suspend fun claimFinalReward(userId: String, reward: Double): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Claiming final reward: $reward EKH for user: $userId")

                // Get user profile
                val profiles = appwriteService.databases.listDocuments(
                        databaseId = AppwriteService.DATABASE_ID,
                        collectionId = AppwriteService.USER_PROFILES_COLLECTION,
                        queries = listOf(
                                io.appwrite.Query.equal("userId", userId),
                                io.appwrite.Query.limit(1)
                        )
                )

                if (profiles.documents.isEmpty()) {
                    return@withContext Result.failure(Exception("User profile not found"))
                }

                val profile = profiles.documents.first()
                @Suppress("UNCHECKED_CAST")
                val profileData = profile.data as Map<String, Any>

                val currentMiningReward = (profileData["miningReward"] as? Number)?.toDouble() ?: 0.0
                val todayEarnings = (profileData["todayEarnings"] as? Number)?.toDouble() ?: 0.0

                // Format date in ISO 8601 format for Appwrite
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                val currentDate = dateFormat.format(Date())

                // Update user profile with mining reward
                appwriteService.databases.updateDocument(
                        databaseId = AppwriteService.DATABASE_ID,
                        collectionId = AppwriteService.USER_PROFILES_COLLECTION,
                        documentId = profile.id,
                        data = mapOf(
                                "miningReward" to (currentMiningReward + reward),
                                "todayEarnings" to (todayEarnings + reward),
                                "updatedAt" to currentDate
                        )
                )

                // Mark reward as claimed in local storage
                val session = getMiningSession()
                if (session != null) {
                    val updatedSession = session.copy(finalRewardClaimed = true)
                    saveMiningSession(updatedSession)
                }

                // Record mining session in database (for history/analytics)
                recordMiningSession(userId, reward, 24 * 60 * 60)

                Log.d(TAG, "Final reward claimed successfully: $reward EKH")
                Result.success(true)
            } catch (e: AppwriteException) {
                Log.e(TAG, "Failed to claim reward", e)
                Result.failure(e)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to claim reward", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Records mining session in database for analytics
     * (Like recordMiningSession in React Native)
     */
    private suspend fun recordMiningSession(userId: String, coinsEarned: Double, sessionDuration: Int) {
        try {
            // Format date in ISO 8601 format for Appwrite
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val currentDate = dateFormat.format(Date())

            appwriteService.databases.createDocument(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.MINING_SESSIONS_COLLECTION,
                    documentId = ID.unique(),
                    data = mapOf(
                            "userId" to userId,
                            "coinsEarned" to coinsEarned,
                            "clicksMade" to 0,
                            "sessionDuration" to sessionDuration,
                            "createdAt" to currentDate
                    )
            )
            Log.d(TAG, "Mining session recorded in database")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to record mining session", e)
        }
    }

    /**
     * Clears the mining session from local storage
     * ENHANCED: Now clears all mining-related data
     */
    suspend fun clearMiningSession(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Clearing mining session...")
                
                // Clear the main mining session key
                prefs.edit().remove(KEY_MINING_SESSION).apply()
                
                // Also clear any other mining-related keys that might exist
                prefs.edit().apply {
                    remove("mining_start_time")
                    remove("mining_end_time")
                    remove("is_mining_active")
                    remove("mining_user_id")
                    apply()
                }
                
                Log.d(TAG, "✅ Mining session cleared successfully")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to clear mining session", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * NEW METHOD: Clears all mining data for a specific user
     */
    suspend fun clearMiningSessionForUser(userId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val savedSession = getMiningSession()
                if (savedSession != null && savedSession.userId == userId) {
                    clearMiningSession()
                    Log.d(TAG, "Cleared mining session for user: $userId")
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear mining session for user", e)
                Result.failure(e)
            }
        }
    }

    // ========= Local Storage Methods (Like AsyncStorage) =========

    /**
     * Saves mining session to SharedPreferences (like AsyncStorage)
     */
    private fun saveMiningSession(sessionData: MiningSessionData) {
        val json = JSONObject().apply {
            put("userId", sessionData.userId)
            put("startTime", sessionData.startTime)
            put("duration", sessionData.duration)
            put("reward", sessionData.reward)
            put("finalRewardClaimed", sessionData.finalRewardClaimed)
        }.toString()

        prefs.edit().putString(KEY_MINING_SESSION, json).apply()
        Log.d(TAG, "Mining session saved to local storage")
    }

    /**
     * Gets mining session from SharedPreferences (like AsyncStorage)
     */
    private fun getMiningSession(): MiningSessionData? {
        val json = prefs.getString(KEY_MINING_SESSION, null) ?: return null

        return try {
            val jsonObject = JSONObject(json)
            MiningSessionData(
                userId = jsonObject.getString("userId"),
                startTime = jsonObject.getLong("startTime"),
                duration = jsonObject.getLong("duration"),
                reward = jsonObject.getDouble("reward"),
                finalRewardClaimed = jsonObject.getBoolean("finalRewardClaimed")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse mining session", e)
            null
        }
    }

    // ========= Keep Existing Methods for Compatibility =========

    suspend fun getMiningSession(sessionId: String): Result<MiningSession> {
        return withContext(Dispatchers.IO) {
            try {
                val document = appwriteService.databases.getDocument(
                        databaseId = AppwriteService.DATABASE_ID,
                        collectionId = AppwriteService.MINING_SESSIONS_COLLECTION,
                        documentId = sessionId
                )

                val session = documentToMiningSession(document)
                Result.success(session)
            } catch (e: AppwriteException) {
                Result.failure(e)
            }
        }
    }

    suspend fun createMiningSession(userId: String): Result<MiningSession> {
        return withContext(Dispatchers.IO) {
            try {
                // Format date in ISO 8601 format for Appwrite
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                val currentDate = dateFormat.format(Date())

                val document = appwriteService.databases.createDocument(
                        databaseId = AppwriteService.DATABASE_ID,
                        collectionId = AppwriteService.MINING_SESSIONS_COLLECTION,
                        documentId = ID.unique(),
                        data = mapOf(
                                "userId" to userId,
                                "amount" to 2.0,
                                "timestamp" to currentDate,
                                "status" to "pending",
                                "duration" to 30
                        )
                )

                val session = documentToMiningSession(document)
                Result.success(session)
            } catch (e: AppwriteException) {
                Result.failure(e)
            }
        }
    }

    suspend fun completeMiningSession(sessionId: String, userId: String): Result<MiningSession> {
        return withContext(Dispatchers.IO) {
            try {
                // Format date in ISO 8601 format for Appwrite
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                val currentDate = dateFormat.format(Date())

                val updates = mapOf(
                        "status" to "completed",
                        "timestamp" to currentDate
                )

                val document = appwriteService.databases.updateDocument(
                        databaseId = AppwriteService.DATABASE_ID,
                        collectionId = AppwriteService.MINING_SESSIONS_COLLECTION,
                        documentId = sessionId,
                        data = updates
                )

                val session = documentToMiningSession(document)
                Result.success(session)
            } catch (e: AppwriteException) {
                Result.failure(e)
            }
        }
    }

    private fun documentToMiningSession(document: Document<*>): MiningSession {
        @Suppress("UNCHECKED_CAST")
        val data = document.data as Map<String, Any>

        return MiningSession(
                id = document.id ?: "",
                userId = data["userId"] as? String ?: "",
                coinsEarned = (data["amount"] as? Number)?.toDouble() ?: 2.0,
                clicksMade = 0,
                sessionDuration = (data["duration"] as? Number)?.toInt() ?: 30,
                createdAt = document.createdAt ?: "1970-01-01T00:00:00.000Z",
                updatedAt = document.updatedAt ?: "1970-01-01T00:00:00.000Z"
        )
    }
}

/**
 * Local mining session data (stored in SharedPreferences)
 */
data class MiningSessionData(
        val userId: String,
        val startTime: Long,
        val duration: Long,
        val reward: Double,
        val finalRewardClaimed: Boolean
)

/**
 * Current mining session status with calculated values
 */
data class MiningSessionStatus(
        val userId: String,
        val startTime: Long,
        val duration: Long,
        val reward: Double,
        val remainingSeconds: Int,
        val progress: Double,
        val isComplete: Boolean,
        val finalRewardClaimed: Boolean
)