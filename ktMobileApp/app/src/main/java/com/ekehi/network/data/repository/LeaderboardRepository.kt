package com.ekehi.network.data.repository

import com.ekehi.network.service.AppwriteService
import io.appwrite.Query
import io.appwrite.exceptions.AppwriteException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LeaderboardRepository @Inject constructor(
        private val appwriteService: AppwriteService
) {
    suspend fun getLeaderboard(): Result<List<Map<String, Any>>> {
        return withContext(Dispatchers.IO) {
            try {
                // Fetch top 50 users ordered by totalCoins descending
                // Only include users with verified accounts (you may need to adjust this based on your verification logic)
                val response = appwriteService.databases.listDocuments(
                        databaseId = AppwriteService.DATABASE_ID,
                        collectionId = AppwriteService.USER_PROFILES_COLLECTION,
                        queries = listOf(
                                Query.orderDesc("totalCoins"),
                                Query.limit(50)
                        )
                )

                // Convert documents to leaderboard entries
                val leaderboardEntries = response.documents.mapIndexed { index, document ->
                    buildMap<String, Any> {
                        put("rank", index + 1)
                        put("username", document.data["username"] as? String ?: "user_${document.id.take(8)}")
                        put("totalCoins", (document.data["totalCoins"] as? Number)?.toDouble() ?: 0.0)
                        put("miningPower", (document.data["miningPower"] as? Number)?.toDouble() ?: 1.0)
                        put("currentStreak", (document.data["currentStreak"] as? Number)?.toInt() ?: 0)
                        put("totalReferrals", (document.data["totalReferrals"] as? Number)?.toInt() ?: 0)
                    }
                }

                Result.success(leaderboardEntries)
            } catch (e: AppwriteException) {
                Result.failure(e)
            }
        }
    }

    suspend fun getUserRank(userId: String): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                // First, get the user's totalCoins
                val userDoc = appwriteService.databases.getDocument(
                        databaseId = AppwriteService.DATABASE_ID,
                        collectionId = AppwriteService.USER_PROFILES_COLLECTION,
                        documentId = userId
                )

                val userTotalCoins = (userDoc.data["totalCoins"] as? Number)?.toDouble() ?: 0.0

                // Then count how many users have more coins than this user
                val response = appwriteService.databases.listDocuments(
                        databaseId = AppwriteService.DATABASE_ID,
                        collectionId = AppwriteService.USER_PROFILES_COLLECTION,
                        queries = listOf(
                                Query.greaterThan("totalCoins", userTotalCoins),
                                Query.limit(1000) // Limit to reasonable number
                        )
                )

                // User rank is the count of users with more coins + 1
                val rank = response.documents.size + 1
                Result.success(rank)
            } catch (e: AppwriteException) {
                Result.failure(e)
            }
        }
    }
    
    // Add a method to get verified users only
    suspend fun getVerifiedLeaderboard(): Result<List<Map<String, Any>>> {
        return withContext(Dispatchers.IO) {
            try {
                // Fetch top 50 verified users ordered by totalCoins descending
                // Assuming users with totalCoins > 0 are considered verified
                val response = appwriteService.databases.listDocuments(
                        databaseId = AppwriteService.DATABASE_ID,
                        collectionId = AppwriteService.USER_PROFILES_COLLECTION,
                        queries = listOf(
                                Query.greaterThan("totalCoins", 0.0),
                                Query.orderDesc("totalCoins"),
                                Query.limit(50)
                        )
                )

                // Convert documents to leaderboard entries
                val leaderboardEntries = response.documents.mapIndexed { index, document ->
                    buildMap<String, Any> {
                        put("rank", index + 1)
                        put("username", document.data["username"] as? String ?: "user_${document.id.take(8)}")
                        put("totalCoins", (document.data["totalCoins"] as? Number)?.toDouble() ?: 0.0)
                        put("miningPower", (document.data["miningPower"] as? Number)?.toDouble() ?: 1.0)
                        put("currentStreak", (document.data["currentStreak"] as? Number)?.toInt() ?: 0)
                        put("totalReferrals", (document.data["totalReferrals"] as? Number)?.toInt() ?: 0)
                    }
                }

                Result.success(leaderboardEntries)
            } catch (e: AppwriteException) {
                Result.failure(e)
            }
        }
    }
}