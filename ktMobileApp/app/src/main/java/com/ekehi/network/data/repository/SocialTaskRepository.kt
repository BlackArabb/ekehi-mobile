package com.ekehi.network.data.repository

import com.ekehi.network.service.AppwriteService
import com.ekehi.network.data.model.SocialTask
import com.ekehi.network.data.model.UserSocialTask
import com.ekehi.network.domain.verification.SocialVerificationService
import com.ekehi.network.domain.verification.VerificationResult
import com.ekehi.network.performance.PerformanceMonitor
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.util.Log
import io.appwrite.models.Document
import io.appwrite.exceptions.AppwriteException
import io.appwrite.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject

open class SocialTaskRepository @Inject constructor(
    private val appwriteService: AppwriteService,
    private val performanceMonitor: PerformanceMonitor,
    private val verificationService: SocialVerificationService
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
                        Query.equal("userId", userId)
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
                        isCompleted = userTask != null && userTask.status == "verified", // Only completed when verified
                        isVerified = userTask?.status == "verified",
                        status = userTask?.status,
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

    suspend fun completeSocialTask(
        userId: String,
        taskId: String,
        proofData: Map<String, Any>?
    ): Result<Pair<UserSocialTask, VerificationResult>> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Get task details
                val taskDoc = appwriteService.databases.getDocument(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.SOCIAL_TASKS_COLLECTION,
                    documentId = taskId
                )
                val task = documentToSocialTask(taskDoc)
                
                // Log the task details for debugging
                Log.d("SocialTaskRepository", "Task platform: ${task.platform}")
                Log.d("SocialTaskRepository", "Verification data: ${task.verificationData}")
                
                // 2. Get user profile to get username
                var username: String? = null
                try {
                    val userResponse = appwriteService.databases.listDocuments(
                        databaseId = AppwriteService.DATABASE_ID,
                        collectionId = AppwriteService.USER_PROFILES_COLLECTION,
                        queries = listOf(
                            Query.equal("userId", listOf(userId))
                        )
                    )
                    
                    if (userResponse.documents.isNotEmpty()) {
                        val userData = userResponse.documents[0].data as Map<String, Any>
                        username = userData["username"] as? String
                    }
                } catch (e: Exception) {
                    Log.e("SocialTaskRepository", "Failed to get username: ${e.message}")
                }
                
                // 3. Check if already completed
                val existingTaskResult = getUserTaskByTaskId(userId, taskId)
                if (existingTaskResult.isSuccess) {
                    val existing = existingTaskResult.getOrNull()
                    if (existing?.status == "verified") {
                        return@withContext Result.failure(
                            Exception("Task already completed")
                        )
                    }
                }
                
                // 4. Create or update user task
                val userTask = if (existingTaskResult.isSuccess && existingTaskResult.getOrNull() != null) {
                    updateUserTaskStatus(
                        documentId = existingTaskResult.getOrNull()!!.id,
                        status = "pending",
                        proofData = proofData,
                        username = username
                    ).getOrThrow()
                } else {
                    createUserTask(userId, taskId, proofData, username).getOrThrow()
                }
                
                // 5. Verify task
                Log.d("SocialTaskRepository", "Starting verification with proofData: $proofData")
                val verificationResult = verificationService.verifyTask(
                    task = task,
                    userTask = userTask,
                    proofData = proofData
                )
                Log.d("SocialTaskRepository", "Verification result: $verificationResult")
                
                // 6. Update based on verification result - ONLY award coins on SUCCESS
                val finalUserTask = when (verificationResult) {
                    is VerificationResult.Success -> {
                        Log.d("SocialTaskRepository", "Verification SUCCESS - awarding coins")
                        val verified = updateUserTaskStatus(
                            documentId = userTask.id,
                            status = "verified",
                            verifiedAt = java.time.Instant.ofEpochMilli(System.currentTimeMillis()).toString(),
                            username = username
                        ).getOrThrow()
                        
                        // Award coins ONLY on success
                        awardCoinsToUser(userId, task.rewardCoins)
                        verified
                    }
                    is VerificationResult.Pending -> {
                        Log.d("SocialTaskRepository", "Verification PENDING - no coins awarded yet")
                        updateUserTaskStatus(
                            documentId = userTask.id,
                            status = "pending",
                            username = username
                        ).getOrThrow()
                    }
                    is VerificationResult.Failure -> {
                        Log.e("SocialTaskRepository", "Verification FAILED: ${verificationResult.reason}")
                        // Mark as rejected, DO NOT award coins
                        updateUserTaskStatus(
                            documentId = userTask.id,
                            status = "rejected",
                            rejectionReason = verificationResult.reason,
                            username = username
                        ).getOrThrow()
                    }
                }
                
                Result.success(Pair(finalUserTask, verificationResult))
                
            } catch (e: Exception) {
                Log.e("SocialTaskRepository", "Error completing task: ${e.message}", e)
                
                // Check if this is a unique constraint error related to telegram_user_id
                val errorMessage = e.message?.lowercase() ?: ""
                if (errorMessage.contains("unique") || errorMessage.contains("duplicate") || 
                    errorMessage.contains("telegram_user_id")) {
                    // This might be the issue where the same telegram_user_id is being used
                    // The error message could be more descriptive
                    Result.failure(Exception("Telegram ID already used for this task type. You can't complete the same task twice, but if you're trying different tasks, please contact support."))
                } else {
                    Result.failure(e)
                }
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
                        Query.equal("userId", userId),
                        Query.equal("taskId", taskId)
                    )
                )
                
                if (response.documents.isNotEmpty()) {
                    val documentId = response.documents[0].id ?: ""
                    
                    // Get the task details to get the reward amount
                    val taskResponse = appwriteService.databases.getDocument(
                        databaseId = AppwriteService.DATABASE_ID,
                        collectionId = AppwriteService.SOCIAL_TASKS_COLLECTION,
                        documentId = taskId
                    )
                    val task = documentToSocialTask(taskResponse)
                    
                    // Get user profile to get username
                    var username: String? = null
                    try {
                        val userResponse = appwriteService.databases.listDocuments(
                            databaseId = AppwriteService.DATABASE_ID,
                            collectionId = AppwriteService.USER_PROFILES_COLLECTION,
                            queries = listOf(
                                Query.equal("userId", listOf(userId))
                            )
                        )
                        
                        if (userResponse.documents.isNotEmpty()) {
                            val userData = userResponse.documents[0].data as Map<String, Any>
                            username = userData["username"] as? String
                        }
                    } catch (e: Exception) {
                        // If we can't get the username, we'll just proceed without it
                        Log.e("SocialTaskRepository", "Failed to get username: ${e.message}")
                    }
                    
                    // Update the document to mark as verified
                    val document = appwriteService.databases.updateDocument(
                        databaseId = AppwriteService.DATABASE_ID,
                        collectionId = AppwriteService.USER_SOCIAL_TASKS_COLLECTION,
                        documentId = documentId,
                        data = mapOf(
                            "status" to "verified",
                            "verifiedAt" to java.time.Instant.ofEpochMilli(System.currentTimeMillis()).toString(),
                            "username" to username
                        )
                    )
                    
                    val userSocialTask = documentToUserSocialTask(document)
                    
                    // Award coins for manually verified tasks
                    awardCoinsToUser(userId, task.rewardCoins)
                    
                    Result.success(userSocialTask)
                } else {
                    Result.failure(Exception("User social task not found"))
                }
            } catch (e: AppwriteException) {
                Result.failure(e)
            }
        }
    }
    
    private suspend fun getUserTaskByTaskId(
        userId: String,
        taskId: String
    ): Result<UserSocialTask?> {
        return try {
            val response = appwriteService.databases.listDocuments(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.USER_SOCIAL_TASKS_COLLECTION,
                queries = listOf(
                    Query.equal("userId", userId),
                    Query.equal("taskId", taskId)
                )
            )
            
            val userTask = response.documents.firstOrNull()?.let {
                documentToUserSocialTask(it)
            }
            
            Result.success(userTask)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun createUserTask(
        userId: String,
        taskId: String,
        proofData: Map<String, Any>?,
        username: String? = null
    ): Result<UserSocialTask> {
        return try {
            val gson = Gson()
            val data = mutableMapOf<String, Any>(
                "userId" to userId,
                "taskId" to taskId,
                "status" to "pending",
                "completedAt" to java.time.Instant.ofEpochMilli(System.currentTimeMillis()).toString(),
                "verificationAttempts" to 1
            )
            
            // Add username if provided
            username?.let { data["username"] = it }
            
            // Extract and add telegram_user_id if present in proofData for verification
            // NOTE: This field should NOT be part of a unique constraint alone
            // The unique constraint should be on userId + taskId combination
            proofData?.let { pd ->
                if (pd.containsKey("telegram_user_id")) {
                    pd["telegram_user_id"]?.let { telegramId ->
                        when (telegramId) {
                            is Long -> data["telegram_user_id"] = telegramId
                            is Int -> data["telegram_user_id"] = telegramId.toLong()
                            is String -> {
                                telegramId.toLongOrNull()?.let { data["telegram_user_id"] = it }
                            }
                            else -> {
                                // Handle other types or null values
                            }
                        }
                    }
                }
            }

            // Convert proofData to JSON string for Appwrite storage
            proofData?.let { 
                data["proofData"] = gson.toJson(it)
            }
            
            val document = appwriteService.databases.createDocument(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.USER_SOCIAL_TASKS_COLLECTION,
                documentId = "unique()",
                data = data
            )
            
            Result.success(documentToUserSocialTask(document))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun updateUserTaskStatus(
        documentId: String,
        status: String,
        verifiedAt: String? = null,
        rejectionReason: String? = null,
        proofData: Map<String, Any>? = null,
        username: String? = null
    ): Result<UserSocialTask> {
        return try {
            val gson = Gson()
            val data = mutableMapOf<String, Any?>("status" to status)
            verifiedAt?.let { data["verifiedAt"] = it }
            rejectionReason?.let { data["rejectionReason"] = it }
            
            // Add username if provided
            username?.let { data["username"] = it }
            
            // Extract and add telegram_user_id if present in proofData for verification
            // NOTE: This field should NOT be part of a unique constraint alone
            // The unique constraint should be on userId + taskId combination
            proofData?.let { pd ->
                if (pd.containsKey("telegram_user_id")) {
                    pd["telegram_user_id"]?.let { telegramId ->
                        when (telegramId) {
                            is Long -> data["telegram_user_id"] = telegramId
                            is Int -> data["telegram_user_id"] = telegramId.toLong()
                            is String -> {
                                telegramId.toLongOrNull()?.let { data["telegram_user_id"] = it }
                            }
                            else -> {
                                // Handle other types or null values
                            }
                        }
                    }
                }
            }
            
            // Convert proofData to JSON string for Appwrite storage
            proofData?.let { 
                data["proofData"] = gson.toJson(it)
            }
            
            val document = appwriteService.databases.updateDocument(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.USER_SOCIAL_TASKS_COLLECTION,
                documentId = documentId,
                data = data
            )
            
            Result.success(documentToUserSocialTask(document))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun awardCoinsToUser(userId: String, amount: Double) {
        try {
            // Get user profile document using userId field (which is stored as a list)
            val response = appwriteService.databases.listDocuments(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.USER_PROFILES_COLLECTION,
                queries = listOf(
                    io.appwrite.Query.equal("userId", listOf(userId))
                )
            )
            
            if (response.documents.isNotEmpty()) {
                val profileDoc = response.documents[0]
                val documentId = profileDoc.id
                
                @Suppress("UNCHECKED_CAST")
                val profileData = profileDoc.data as Map<String, Any>
                val currentTaskReward = (profileData["taskReward"] as? Number)?.toDouble() ?: 0.0
                val newTaskReward = currentTaskReward + amount
                
                appwriteService.databases.updateDocument(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.USER_PROFILES_COLLECTION,
                    documentId = documentId,
                    data = mapOf(
                        "taskReward" to newTaskReward,
                        "updatedAt" to java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).format(java.util.Date())
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun documentToSocialTask(document: Document<*>): SocialTask {
        @Suppress("UNCHECKED_CAST")
        val data = document.data as Map<String, Any>
        val gson = Gson()
        
        // Parse verificationData - it can be either a JSON string or a Map
        val verificationDataMap: Map<String, String>? = try {
            when (val verificationData = data["verificationData"]) {
                is String -> {
                    // If it's a JSON string, parse it
                    if (verificationData.isNotEmpty()) {
                        Log.d("SocialTaskRepository", "Parsing verificationData string: $verificationData")
                        val type = object : TypeToken<Map<String, String>>() {}.type
                        val parsed = gson.fromJson<Map<String, String>>(verificationData, type)
                        Log.d("SocialTaskRepository", "Parsed verificationData: $parsed")
                        parsed
                    } else {
                        Log.w("SocialTaskRepository", "verificationData is empty string")
                        null
                    }
                }
                is Map<*, *> -> {
                    // If it's already a map, cast it
                    @Suppress("UNCHECKED_CAST")
                    val mapped = verificationData as? Map<String, String>
                    Log.d("SocialTaskRepository", "verificationData is already a map: $mapped")
                    mapped
                }
                null -> {
                    Log.w("SocialTaskRepository", "verificationData is null")
                    null
                }
                else -> {
                    Log.w("SocialTaskRepository", "verificationData is unknown type: ${verificationData::class.java}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("SocialTaskRepository", "Failed to parse verificationData: ${e.message}", e)
            null
        }
        
        val task = SocialTask(
            id = document.id ?: "",
            title = data["title"] as? String ?: "",
            description = data["description"] as? String ?: "",
            platform = data["platform"] as? String ?: "",
            taskType = data["taskType"] as? String ?: "",
            rewardCoins = (data["rewardCoins"] as? Number)?.toDouble() ?: 0.0,
            actionUrl = data["actionUrl"] as? String,
            verificationMethod = data["verificationMethod"] as? String ?: "manual",
            verificationData = verificationDataMap,
            isActive = data["isActive"] as? Boolean ?: false,
            sortOrder = (data["sortOrder"] as? Number)?.toInt() ?: 0,
            createdAt = document.createdAt ?: "1970-01-01T00:00:00.000Z",
            updatedAt = document.updatedAt ?: "1970-01-01T00:00:00.000Z"
        )
        
        Log.d("SocialTaskRepository", "Created task: id=${task.id}, platform=${task.platform}, verificationData=${task.verificationData}")
        return task
    }

    private fun documentToUserSocialTask(document: Document<*>): UserSocialTask {
        @Suppress("UNCHECKED_CAST")
        val data = document.data as Map<String, Any>
        val gson = Gson()
        
        // Parse proofData from JSON string
        val proofDataMap: Map<String, Any>? = try {
            val proofDataStr = data["proofData"] as? String
            if (!proofDataStr.isNullOrEmpty()) {
                val type = object : TypeToken<Map<String, Any>>() {}.type
                gson.fromJson(proofDataStr, type)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
        
        return UserSocialTask(
            id = document.id ?: "",
            userId = data["userId"] as? String ?: "",
            taskId = data["taskId"] as? String ?: "",
            status = data["status"] as? String ?: "pending",
            completedAt = data["completedAt"] as? String,
            verifiedAt = data["verifiedAt"] as? String,
            proofUrl = data["proofUrl"] as? String,
            proofData = proofDataMap,
            verificationAttempts = (data["verificationAttempts"] as? Number)?.toInt() ?: 0,
            rejectionReason = data["rejectionReason"] as? String,
            username = data["username"] as? String
        )
    }

    suspend fun getUserCompletedTasksCount(userId: String): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                // Get all user social tasks that are verified/completed
                val response = appwriteService.databases.listDocuments(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.USER_SOCIAL_TASKS_COLLECTION,
                    queries = listOf(
                        Query.equal("userId", userId),
                        Query.equal("status", "verified")
                    )
                )
                
                // Return the count of verified tasks
                Result.success(response.total.toInt())
            } catch (e: AppwriteException) {
                Result.failure(e)
            }
        }
    }

    /**
     * Delete a pending task submission and revert its status to available
     */
    suspend fun deletePendingTask(userId: String, taskId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // First, find the user social task document
                val response = appwriteService.databases.listDocuments(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.USER_SOCIAL_TASKS_COLLECTION,
                    queries = listOf(
                        Query.equal("userId", userId),
                        Query.equal("taskId", taskId),
                        Query.equal("status", "pending")
                    )
                )
                
                if (response.documents.isNotEmpty()) {
                    val documentId = response.documents[0].id ?: ""
                    
                    // Delete the document
                    appwriteService.databases.deleteDocument(
                        databaseId = AppwriteService.DATABASE_ID,
                        collectionId = AppwriteService.USER_SOCIAL_TASKS_COLLECTION,
                        documentId = documentId
                    )
                    
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Pending task not found"))
                }
            } catch (e: AppwriteException) {
                Result.failure(e)
            }
        }
    }
}