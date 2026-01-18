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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
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
                // Increase limit to 100 to handle repetitive tasks history
                val response = appwriteService.databases.listDocuments(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.USER_SOCIAL_TASKS_COLLECTION,
                    queries = listOf(
                        Query.equal("userId", userId),
                        Query.limit(100),
                        Query.orderDesc("completedAt")
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
                Log.i("EKEHI_DEBUG", "getSocialTasksWithUserStatus called for userId: $userId")
                val allTasksResult = getAllSocialTasks()
                if (allTasksResult.isFailure) {
                    Log.e("EKEHI_DEBUG", "Failed to get all tasks: ${allTasksResult.exceptionOrNull()?.message}")
                    return@withContext Result.failure(allTasksResult.exceptionOrNull() ?: Exception("Failed to get social tasks"))
                }
                
                val allTasks = allTasksResult.getOrNull() ?: emptyList()
                Log.i("EKEHI_DEBUG", "Loaded ${allTasks.size} total tasks")
                
                // Get user's completed tasks
                val userTasksResult = getUserSocialTasks(userId)
                if (userTasksResult.isFailure) {
                    Log.w("EKEHI_DEBUG", "Failed to get user tasks for $userId: ${userTasksResult.exceptionOrNull()?.message}")
                    // Return all tasks without user status if we can't get user tasks
                    return@withContext Result.success(allTasks)
                }
                
                val userTasks = userTasksResult.getOrNull() ?: emptyList()
                Log.i("EKEHI_DEBUG", "Loaded ${userTasks.size} user task records for $userId")
                
                // Group user tasks by task ID
                val userTasksByTaskId = userTasks.groupBy { it.taskId }
                
                val now = System.currentTimeMillis()
                val oneDayAgo = now - (24 * 60 * 60 * 1000L)
                
                // Combine all tasks with user status
                val tasksWithStatus = allTasks.map { task ->
                    val userTasksForThisTask = userTasksByTaskId[task.id] ?: emptyList()
                    
                    if (task.platform.lowercase() == "blog") {
                        // Detailed logging for blog tasks to find why count is 0
                        Log.i("EKEHI_DEBUG", "Filtering ${userTasksForThisTask.size} records for Blog Task: ${task.title}")
                        userTasksForThisTask.forEachIndexed { index, ut ->
                            val compTime = parseIsoDate(ut.completedAt)
                            val isRecent = compTime > oneDayAgo
                            Log.i("EKEHI_DEBUG", "  Record #$index: status=${ut.status}, completedAt=${ut.completedAt}, isRecent=$isRecent")
                        }

                        // Implement automatic 24h reset based on last completion time
                        val sortedUserTasks = userTasksForThisTask.sortedByDescending { parseIsoDate(it.completedAt) }
                        val oneDayMs = 24 * 60 * 60 * 1000L
                        
                        var completionCountToday = 0
                        var lastReferenceTime = now
                        var latestVerifiedTime = 0L
                        
                        // Count verified tasks in the current 24h active block
                        for (ut in sortedUserTasks) {
                            val compTime = parseIsoDate(ut.completedAt)
                            
                            // If gap since last task (or now) is > 24h, this starts a new reset cycle
                            if (lastReferenceTime - compTime > oneDayMs) {
                                break
                            }
                            
                            if (ut.status == "verified") {
                                completionCountToday++
                                if (latestVerifiedTime == 0L) latestVerifiedTime = compTime
                            }
                            lastReferenceTime = compTime
                        }
                        
                        val latestUserTask = sortedUserTasks.firstOrNull()
                        
                        val cooldownMs = task.cooldownMinutes * 60 * 1000L
                        val nextCooldownAvailableTime = latestVerifiedTime + cooldownMs
                        val nextLimitAvailableTime = latestVerifiedTime + oneDayMs
                        
                        val isCooldownActive = latestVerifiedTime > 0 && now < nextCooldownAvailableTime
                        val isLimitReached = completionCountToday >= task.maxCompletionsPerDay
                        
                        val nextAvailableAt = when {
                            isLimitReached -> java.time.Instant.ofEpochMilli(nextLimitAvailableTime).toString()
                            isCooldownActive -> java.time.Instant.ofEpochMilli(nextCooldownAvailableTime).toString()
                            else -> null
                        }
                        
                        Log.i("EKEHI_DEBUG", "  Final Result: count=$completionCountToday/${task.maxCompletionsPerDay}, isLimitReached=$isLimitReached, isCooldownActive=$isCooldownActive")
                        
                        task.copy(
                            isCompleted = isLimitReached || isCooldownActive,
                            isVerified = isLimitReached,
                            status = if (isLimitReached) "verified" else if (isCooldownActive) "pending" else null,
                            completionCountToday = completionCountToday,
                            nextAvailableAt = nextAvailableAt,
                            completedAt = latestUserTask?.completedAt,
                            verifiedAt = latestUserTask?.verifiedAt
                        )
                    } else {
                        val latestUserTask = userTasksForThisTask.maxByOrNull { 
                            parseIsoDate(it.completedAt)
                        }
                        // Standard task handling
                        task.copy(
                            isCompleted = latestUserTask != null && latestUserTask.status == "verified",
                            isVerified = latestUserTask?.status == "verified",
                            status = latestUserTask?.status,
                            completedAt = latestUserTask?.completedAt,
                            verifiedAt = latestUserTask?.verifiedAt
                        )
                    }
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
        Log.i("EKEHI_DEBUG", "completeSocialTask called: userId=$userId, taskId=$taskId")
        return withContext(Dispatchers.IO) {
            try {
                // 1. Get task details
                val taskDoc = appwriteService.databases.getDocument(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.SOCIAL_TASKS_COLLECTION,
                    documentId = taskId
                )
                val task = documentToSocialTask(taskDoc)
                
                Log.i("EKEHI_DEBUG", "Processing task: ${task.title} [${task.platform}]")
                
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
                        Log.i("EKEHI_DEBUG", "Found username: $username")
                    }
                } catch (e: Exception) {
                    Log.e("EKEHI_DEBUG", "Failed to get username: ${e.message}")
                }
                
                // 3. Check completion rules
                val userTasksResult = getUserSocialTasks(userId)
                val userTasks = userTasksResult.getOrNull() ?: emptyList()
                val userTasksForThisTask = userTasks.filter { it.taskId == taskId }
                val now = System.currentTimeMillis()
                
                if (task.platform.lowercase() == "blog") {
                    Log.i("EKEHI_DEBUG", "Executing blog-specific logic")
                    
                    // Implement automatic 24h reset based on last completion time
                    val sortedUserTasks = userTasksForThisTask.sortedByDescending { parseIsoDate(it.completedAt) }
                    val oneDayMs = 24 * 60 * 60 * 1000L
                    
                    var completionCountToday = 0
                    var lastReferenceTime = now
                    var latestVerifiedTime = 0L
                    
                    for (ut in sortedUserTasks) {
                        val compTime = parseIsoDate(ut.completedAt)
                        if (lastReferenceTime - compTime > oneDayMs) break
                        
                        if (ut.status == "verified") {
                            completionCountToday++
                            if (latestVerifiedTime == 0L) latestVerifiedTime = compTime
                        }
                        lastReferenceTime = compTime
                    }
                    
                    Log.i("EKEHI_DEBUG", "Blog completions in current block: $completionCountToday")
                    
                    if (completionCountToday >= task.maxCompletionsPerDay) {
                        val nextResetTime = latestVerifiedTime + oneDayMs
                        val remainingMs = nextResetTime - now
                        val remainingHours = remainingMs / (60 * 60 * 1000)
                        val remainingMinutes = (remainingMs / (60 * 1000)) % 60
                        
                        Log.w("EKEHI_DEBUG", "Blog limit reached. Next reset in $remainingHours h $remainingMinutes mins")
                        return@withContext Result.failure(Exception("Daily limit reached. Try again in $remainingHours hours and $remainingMinutes minutes."))
                    }
                    
                    val cooldownMs = task.cooldownMinutes * 60 * 1000L
                    if (latestVerifiedTime > 0 && now < latestVerifiedTime + cooldownMs) {
                        val remainingMs = (latestVerifiedTime + cooldownMs) - now
                        val remainingMinutes = (remainingMs / (60 * 1000)) + 1
                        Log.w("EKEHI_DEBUG", "Blog cooldown active: $remainingMinutes mins left")
                        return@withContext Result.failure(Exception("Cooldown active. Available in $remainingMinutes minutes."))
                    }
                    
                    // Blog tasks ALWAYS create a NEW document to track history
                    Log.i("EKEHI_DEBUG", "Creating new record for blog completion")
                    val userTask = createUserTask(userId, taskId, proofData, username).getOrThrow()
                    
                    // 5. Verify task
                    Log.i("EKEHI_DEBUG", "Starting auto-verification for blog")
                    val verificationResult = verificationService.verifyTask(
                        task = task,
                        userTask = userTask,
                        proofData = proofData
                    )
                    
                    // 6. Update based on verification result
                    val finalUserTask = if (verificationResult is VerificationResult.Success) {
                        Log.i("EKEHI_DEBUG", "Blog verification SUCCESS, awarding coins")
                        val verified = updateUserTaskStatus(
                            documentId = userTask.id,
                            status = "verified",
                            verifiedAt = java.time.Instant.now().toString(),
                            username = username
                        ).getOrThrow()
                        awardCoinsToUser(userId, task.rewardCoins)
                        verified
                    } else {
                        Log.w("EKEHI_DEBUG", "Blog verification FAILED")
                        userTask
                    }
                    
                    return@withContext Result.success(Pair(finalUserTask, verificationResult))
                    
                } else {
                    // Standard task: check if already completed
                    val latestUserTask = userTasksForThisTask.maxByOrNull {
                        parseIsoDate(it.completedAt)
                    }
                    
                    if (latestUserTask?.status == "verified") {
                        return@withContext Result.failure(Exception("Task already completed"))
                    }
                    
                    // 4. Create or update user task
                    val userTask = if (latestUserTask != null) {
                        updateUserTaskStatus(
                            documentId = latestUserTask.id,
                            status = "pending",
                            proofData = proofData,
                            username = username
                        ).getOrThrow()
                    } else {
                        createUserTask(userId, taskId, proofData, username).getOrThrow()
                    }
                    
                    // 5. Verify task
                    val verificationResult = verificationService.verifyTask(
                        task = task,
                        userTask = userTask,
                        proofData = proofData
                    )
                    
                    // 6. Update based on verification result
                    val finalUserTask = when (verificationResult) {
                        is VerificationResult.Success -> {
                            val verified = updateUserTaskStatus(
                                documentId = userTask.id,
                                status = "verified",
                                verifiedAt = java.time.Instant.now().toString(),
                                username = username
                            ).getOrThrow()
                            awardCoinsToUser(userId, task.rewardCoins)
                            verified
                        }
                        is VerificationResult.Pending -> {
                            updateUserTaskStatus(
                                documentId = userTask.id,
                                status = "pending",
                                username = username
                            ).getOrThrow()
                        }
                        is VerificationResult.Failure -> {
                            updateUserTaskStatus(
                                documentId = userTask.id,
                                status = "rejected",
                                rejectionReason = verificationResult.reason,
                                username = username
                            ).getOrThrow()
                        }
                    }
                    
                    return@withContext Result.success(Pair(finalUserTask, verificationResult))
                }
                
            } catch (e: Exception) {
                Log.e("EKEHI_DEBUG", "Error completing task: ${e.message}", e)
                
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
                            "verifiedAt" to java.time.Instant.now().toString(),
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
                "completedAt" to java.time.Instant.now().toString(),
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
            Log.i("EKEHI_DEBUG", "Awarding $amount coins to user $userId")
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
                val currentMiningReward = (profileData["miningReward"] as? Number)?.toDouble() ?: 0.0
                val currentReferralReward = (profileData["referralReward"] as? Number)?.toDouble() ?: 0.0
                
                val newTaskReward = currentTaskReward + amount
                val newTotalCoins = newTaskReward + currentMiningReward + currentReferralReward
                
                Log.i("EKEHI_DEBUG", "Updating user profile $documentId: old=$currentTaskReward, new=$newTaskReward")
                
                appwriteService.databases.updateDocument(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.USER_PROFILES_COLLECTION,
                    documentId = documentId,
                    data = mapOf(
                        "taskReward" to newTaskReward,
                        "totalCoins" to newTotalCoins,
                        "updatedAt" to java.time.Instant.now().toString()
                    )
                )
                Log.i("EKEHI_DEBUG", "Coins awarded successfully")
            } else {
                Log.e("EKEHI_DEBUG", "User profile not found for userId $userId")
            }
        } catch (e: Exception) {
            Log.e("EKEHI_DEBUG", "Failed to award coins: ${e.message}", e)
        }
    }

    private fun parseIsoDate(dateStr: String?): Long {
        if (dateStr.isNullOrEmpty()) return 0L
        // Normalize: Instant.parse only likes 'Z', but Appwrite sometimes returns +00:00
        val normalizedDate = dateStr.replace("+00:00", "Z")
        return try {
            java.time.Instant.parse(normalizedDate).toEpochMilli()
        } catch (e: Exception) {
            try {
                // Fallback to SimpleDateFormat for older/custom formats
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                format.parse(normalizedDate)?.time ?: 0L
            } catch (e2: Exception) {
                0L
            }
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
            // Frequency-based attributes (primarily used for 'blog' platform)
            maxCompletionsPerDay = (data["maxCompletionsPerDay"] as? Number)?.toInt() ?: 1,
            cooldownMinutes = (data["cooldownMinutes"] as? Number)?.toInt() ?: 0,
            createdAt = document.createdAt ?: "1970-01-01T00:00:00.000Z",
            updatedAt = document.updatedAt ?: "1970-01-01T00:00:00.000Z"
        )
        
        Log.i("EKEHI_DEBUG", "Created task object: id=${task.id}, title=${task.title}, platform=${task.platform}")
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