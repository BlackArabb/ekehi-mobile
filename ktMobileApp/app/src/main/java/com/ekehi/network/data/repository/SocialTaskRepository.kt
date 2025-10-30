package com.ekehi.network.data.repository

import com.ekehi.network.service.AppwriteService
import com.ekehi.network.data.model.SocialTask
import com.ekehi.network.data.model.UserSocialTask
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

    suspend fun getAllSocialTasks(): Result<List<SocialTask>> {
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

    suspend fun getUserSocialTasks(userId: String): Result<List<UserSocialTask>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = appwriteService.databases.listDocuments(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.USER_SOCIAL_TASKS_COLLECTION,
                    queries = listOf(
                        io.appwrite.Query.equal("userId", userId)
                    )
                )
                
                val userTasks = response.documents.map { documentToUserSocialTask(it) }
                Result.success(userTasks)
            } catch (e: AppwriteException) {
                Result.failure(e)
            }
        }
    }

    suspend fun getSocialTasksWithUserStatus(userId: String): Result<List<SocialTask>> {
        return withContext(Dispatchers.IO) {
            try {
                // Get all social tasks
                val allTasksResult = getAllSocialTasks()
                if (allTasksResult.isFailure) {
                    return@withContext Result.failure(allTasksResult.exceptionOrNull() ?: Exception("Failed to get social tasks"))
                }
                
                val allTasks = allTasksResult.getOrNull() ?: emptyList()
                
                // Get user's completed tasks
                val userTasksResult = getUserSocialTasks(userId)
                if (userTasksResult.isFailure) {
                    // Return all tasks without user status if we can't get user tasks
                    return@withContext Result.success(allTasks)
                }
                
                val userTasks = userTasksResult.getOrNull() ?: emptyList()
                
                // Create a map of user tasks by task ID for quick lookup
                val userTaskMap = userTasks.associateBy { it.taskId }
                
                // Combine all tasks with user status
                val tasksWithStatus = allTasks.map { task ->
                    val userTask = userTaskMap[task.id]
                    task.copy(
                        isCompleted = userTask != null,
                        isVerified = userTask?.status == "verified",
                        completedAt = userTask?.completedAt,
                        verifiedAt = userTask?.verifiedAt
                    )
                }
                
                Result.success(tasksWithStatus)
            } catch (e: AppwriteException) {
                Result.failure(e)
            }
        }
    }

    suspend fun completeSocialTask(userId: String, taskId: String): Result<UserSocialTask> {
        return withContext(Dispatchers.IO) {
            try {
                val document = appwriteService.databases.createDocument(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.USER_SOCIAL_TASKS_COLLECTION,
                    documentId = "unique()",
                    data = mapOf(
                        "userId" to userId,
                        "taskId" to taskId,
                        "status" to "completed",
                        "completedAt" to System.currentTimeMillis().toString(),
                        "verifiedAt" to null
                    )
                )
                
                val userSocialTask = documentToUserSocialTask(document)
                Result.success(userSocialTask)
            } catch (e: AppwriteException) {
                Result.failure(e)
            }
        }
    }

    suspend fun verifySocialTask(userId: String, taskId: String): Result<UserSocialTask> {
        return withContext(Dispatchers.IO) {
            try {
                // First, find the user social task document
                val response = appwriteService.databases.listDocuments(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.USER_SOCIAL_TASKS_COLLECTION,
                    queries = listOf(
                        io.appwrite.Query.equal("userId", userId),
                        io.appwrite.Query.equal("taskId", taskId)
                    )
                )
                
                if (response.documents.isNotEmpty()) {
                    val documentId = response.documents[0].id
                    
                    // Update the document to mark as verified
                    val document = appwriteService.databases.updateDocument(
                        databaseId = AppwriteService.DATABASE_ID,
                        collectionId = AppwriteService.USER_SOCIAL_TASKS_COLLECTION,
                        documentId = documentId,
                        data = mapOf(
                            "status" to "verified",
                            "verifiedAt" to System.currentTimeMillis().toString()
                        )
                    )
                    
                    val userSocialTask = documentToUserSocialTask(document)
                    Result.success(userSocialTask)
                } else {
                    Result.failure(Exception("User social task not found"))
                }
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

    private fun documentToUserSocialTask(document: Document<*>): UserSocialTask {
        @Suppress("UNCHECKED_CAST")
        val data = document.data as Map<String, Any>
        
        return UserSocialTask(
            userId = data["userId"] as String,
            taskId = data["taskId"] as String,
            status = data["status"] as? String ?: "pending",
            completedAt = data["completedAt"] as? String,
            verifiedAt = data["verifiedAt"] as? String
        )
    }
}