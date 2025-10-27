package com.ekehi.network.data.repository

import com.ekehi.network.service.AppwriteService
import com.ekehi.network.data.model.MiningSession
import com.ekehi.network.performance.PerformanceMonitor
import io.appwrite.models.Document
import io.appwrite.exceptions.AppwriteException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

open class MiningRepository @Inject constructor(
    private val appwriteService: AppwriteService,
    private val performanceMonitor: PerformanceMonitor
) {

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
                val document = appwriteService.databases.createDocument(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.MINING_SESSIONS_COLLECTION,
                    documentId = "unique()",
                    data = mapOf(
                        "userId" to userId,
                        "amount" to 2.0, // 2 EKH reward
                        "timestamp" to System.currentTimeMillis().toString(),
                        "status" to "pending",
                        "duration" to 30 // 30 seconds
                    )
                )
                
                val session = documentToMiningSession(document)
                Result.success(session)
            } catch (e: AppwriteException) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateMiningSession(sessionId: String, updates: Map<String, Any>): Result<MiningSession> {
        return withContext(Dispatchers.IO) {
            try {
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

    suspend fun completeMiningSession(sessionId: String, userId: String): Result<MiningSession> {
        return withContext(Dispatchers.IO) {
            try {
                val updates = mapOf(
                    "status" to "completed",
                    "timestamp" to System.currentTimeMillis().toString()
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
            id = document.id,
            userId = data["userId"] as String,
            amount = (data["amount"] as? Number)?.toDouble() ?: 2.0,
            timestamp = data["timestamp"] as? String ?: System.currentTimeMillis().toString(),
            status = data["status"] as? String ?: "pending",
            duration = (data["duration"] as? Number)?.toInt() ?: 30,
            createdAt = document.createdAt,
            updatedAt = document.updatedAt
        )
    }
}