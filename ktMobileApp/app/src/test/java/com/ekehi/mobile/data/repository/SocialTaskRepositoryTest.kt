package com.ekehi.mobile.data.repository

import com.ekehi.mobile.network.service.AppwriteService
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.Document
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test

class SocialTaskRepositoryTest {
    private lateinit var socialTaskRepository: SocialTaskRepository
    private lateinit var appwriteService: AppwriteService
    private lateinit var databases: io.appwrite.services.Databases

    @Before
    fun setUp() {
        // Mock the Appwrite service and databases
        appwriteService = mockk()
        databases = mockk()
        every { appwriteService.databases } returns databases
        
        socialTaskRepository = SocialTaskRepository(appwriteService)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `getSocialTasks should return success when API call succeeds`() = runBlocking {
        // Given
        val document1 = mockk<Document>()
        every { document1.id } returns "task-1"
        every { document1.data } returns mapOf(
            "title" to "Follow on Twitter",
            "description" to "Follow our official Twitter account",
            "platform" to "Twitter",
            "taskType" to "follow",
            "rewardCoins" to 5.0,
            "actionUrl" to "https://twitter.com/ekehi",
            "verificationMethod" to "manual",
            "isActive" to true,
            "sortOrder" to 1
        )
        every { document1.createdAt } returns "2023-01-01T00:00:00Z"
        every { document1.updatedAt } returns "2023-01-01T00:00:00Z"
        
        val document2 = mockk<Document>()
        every { document2.id } returns "task-2"
        every { document2.data } returns mapOf(
            "title" to "Join Telegram Group",
            "description" to "Join our official Telegram group",
            "platform" to "Telegram",
            "taskType" to "join",
            "rewardCoins" to 10.0,
            "actionUrl" to "https://t.me/ekehi",
            "verificationMethod" to "manual",
            "isActive" to true,
            "sortOrder" to 2
        )
        every { document2.createdAt } returns "2023-01-01T00:00:00Z"
        every { document2.updatedAt } returns "2023-01-01T00:00:00Z"
        
        val documentList = mockk<io.appwrite.models.DocumentList<Document>>()
        every { documentList.documents } returns listOf(document1, document2)
        
        coEvery { 
            databases.listDocuments(
                databaseId = any(),
                collectionId = any()
            ) 
        } returns documentList

        // When
        val result = socialTaskRepository.getSocialTasks()

        // Then
        assert(result.isSuccess)
        val tasks = result.getOrNull()
        assert(tasks != null)
        assert(tasks!!.size == 2)
        assert(tasks[0].title == "Follow on Twitter")
        assert(tasks[1].title == "Join Telegram Group")
    }

    @Test
    fun `getSocialTasks should return failure when API call fails`() = runBlocking {
        // Given
        coEvery { 
            databases.listDocuments(
                databaseId = any(),
                collectionId = any()
            ) 
        } throws AppwriteException("Network error")

        // When
        val result = socialTaskRepository.getSocialTasks()

        // Then
        assert(result.isFailure)
        assert(result.exceptionOrNull() is AppwriteException)
    }

    @Test
    fun `getUserSocialTasks should return success when API calls succeed`() = runBlocking {
        // Given
        val userId = "test-user-id"
        
        // Mock user social tasks
        val userTaskDoc = mockk<Document>()
        every { userTaskDoc.id } returns "user-task-1"
        every { userTaskDoc.data } returns mapOf(
            "userId" to userId,
            "taskId" to "task-1"
        )
        every { userTaskDoc.createdAt } returns "2023-01-01T00:00:00Z"
        every { userTaskDoc.updatedAt } returns "2023-01-01T00:00:00Z"
        
        val userTaskList = mockk<io.appwrite.models.DocumentList<Document>>()
        every { userTaskList.documents } returns listOf(userTaskDoc)
        
        coEvery { 
            databases.listDocuments(
                databaseId = any(),
                collectionId = any(),
                queries = any()
            ) 
        } returns userTaskList
        
        // Mock task details
        val taskDoc = mockk<Document>()
        every { taskDoc.id } returns "task-1"
        every { taskDoc.data } returns mapOf(
            "title" to "Follow on Twitter",
            "description" to "Follow our official Twitter account",
            "platform" to "Twitter",
            "taskType" to "follow",
            "rewardCoins" to 5.0,
            "actionUrl" to "https://twitter.com/ekehi",
            "verificationMethod" to "manual",
            "isActive" to true,
            "sortOrder" to 1
        )
        every { taskDoc.createdAt } returns "2023-01-01T00:00:00Z"
        every { taskDoc.updatedAt } returns "2023-01-01T00:00:00Z"
        
        coEvery { 
            databases.getDocument(
                databaseId = any(),
                collectionId = any(),
                documentId = "task-1"
            ) 
        } returns taskDoc

        // When
        val result = socialTaskRepository.getUserSocialTasks(userId)

        // Then
        assert(result.isSuccess)
        val tasks = result.getOrNull()
        assert(tasks != null)
        assert(tasks!!.size == 1)
        assert(tasks[0].title == "Follow on Twitter")
        assert(tasks[0].isCompleted)
    }

    @Test
    fun `completeSocialTask should return success when API call succeeds`() = runBlocking {
        // Given
        val userId = "test-user-id"
        val taskId = "task-1"
        
        coEvery { 
            databases.createDocument(
                databaseId = any(),
                collectionId = any(),
                documentId = "unique()",
                data = any()
            ) 
        } returns mockk()

        // When
        val result = socialTaskRepository.completeSocialTask(userId, taskId)

        // Then
        assert(result.isSuccess)
    }
}