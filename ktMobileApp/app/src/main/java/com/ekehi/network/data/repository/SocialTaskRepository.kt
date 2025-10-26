package com.ekehi.network.data.repository

import com.ekehi.network.service.AppwriteService
import com.ekehi.network.data.model.SocialTask
import com.ekehi.network.performance.PerformanceMonitor
import io.appwrite.models.Document
import io.appwrite.exceptions.AppwriteException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

open class SocialTaskRepository @Inject constructor(
    private val appwriteService: AppwriteService,
    private val performanceMonitor: PerformanceMonitor
) {

    suspend fun getSocialTasks(): Result<List<SocialTask>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = appwriteService.databases.listDocuments(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.SOCIAL_TASKS_COLLECTION
                )
                
                val tasks = response.documents.map { documentToSocialTask(it) }
                Result.success(tasks)
            } catch (e: AppwriteException) {
                Result.failure(e)
            }
        }
    }

    suspend fun getUserSocialTasks(userId: String): Result<List<SocialTask>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = appwriteService.databases.listDocuments(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.USER_SOCIAL_TASKS_COLLECTION,
                    queries = listOf("equal(\"userId\", \"$userId\")")
                )
                
                val userTaskIds = response.documents.map { 
                    @Suppress("UNCHECKED_CAST")
                    val data = it.data as Map<String, Any>
                    data["taskId"] as String 
                }
                
                // Get the full task details
                val tasks = userTaskIds.mapNotNull { taskId ->
                    try {
                        val taskDoc = appwriteService.databases.getDocument(
                            databaseId = AppwriteService.DATABASE_ID,
                            collectionId = AppwriteService.SOCIAL_TASKS_COLLECTION,
                            documentId = taskId
                        )
                        documentToSocialTask(taskDoc).copy(
                            isCompleted = true // Mark as completed since it's in user_social_tasks
                        )
                    } catch (e: AppwriteException) {
                        null
                    }
                }
                
                Result.success(tasks)
            } catch (e: AppwriteException) {
                Result.failure(e)
            }
        }
    }

    suspend fun completeSocialTask(userId: String, taskId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                appwriteService.databases.createDocument(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.USER_SOCIAL_TASKS_COLLECTION,
                    documentId = "unique()",
                    data = mapOf(
                        "userId" to userId,
                        "taskId" to taskId
                    )
                )
                
                Result.success(Unit)
            } catch (e: AppwriteException) {
                Result.failure(e)
            }
        }
    }

    private fun documentToSocialTask(document: Document<*>): SocialTask {
        @Suppress("UNCHECKED_CAST")
        val data = document.data as Map<String, Any>
        
        return SocialTask(
            id = document.id,
            title = data["title"] as String,
            description = data["description"] as String,
            platform = data["platform"] as String,
            taskType = data["taskType"] as String,
            rewardCoins = (data["rewardCoins"] as? Number)?.toDouble() ?: 0.0,
            actionUrl = data["actionUrl"] as? String,
            verificationMethod = data["verificationMethod"] as String,
            isActive = data["isActive"] as? Boolean ?: false,
            sortOrder = (data["sortOrder"] as? Number)?.toInt() ?: 0,
            createdAt = document.createdAt,
            updatedAt = document.updatedAt
        )
    }
}