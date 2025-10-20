package com.ekehi.mobile.data.repository

import com.ekehi.mobile.network.service.AppwriteService
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.Document
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test

class MiningRepositoryTest {
    private lateinit var miningRepository: MiningRepository
    private lateinit var appwriteService: AppwriteService
    private lateinit var databases: io.appwrite.services.Databases

    @Before
    fun setUp() {
        // Mock the Appwrite service and databases
        appwriteService = mockk()
        databases = mockk()
        every { appwriteService.databases } returns databases
        
        miningRepository = MiningRepository(appwriteService)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `getMiningSession should return success when API call succeeds`() = runBlocking {
        // Given
        val sessionId = "test-session-id"
        val document = mockk<Document>()
        every { document.id } returns sessionId
        every { document.data } returns mapOf(
            "userId" to "test-user-id",
            "coinsEarned" to 50.0,
            "clicksMade" to 100,
            "sessionDuration" to 300
        )
        every { document.createdAt } returns "2023-01-01T00:00:00Z"
        every { document.updatedAt } returns "2023-01-01T00:05:00Z"
        
        coEvery { 
            databases.getDocument(
                databaseId = any(),
                collectionId = any(),
                documentId = sessionId
            ) 
        } returns document

        // When
        val result = miningRepository.getMiningSession(sessionId)

        // Then
        assert(result.isSuccess)
        val session = result.getOrNull()
        assert(session != null)
        assert(session!!.id == sessionId)
        assert(session.coinsEarned == 50.0)
        assert(session.clicksMade == 100)
        assert(session.sessionDuration == 300)
    }

    @Test
    fun `getMiningSession should return failure when API call fails`() = runBlocking {
        // Given
        val sessionId = "test-session-id"
        coEvery { 
            databases.getDocument(
                databaseId = any(),
                collectionId = any(),
                documentId = sessionId
            ) 
        } throws AppwriteException("Network error")

        // When
        val result = miningRepository.getMiningSession(sessionId)

        // Then
        assert(result.isFailure)
        assert(result.exceptionOrNull() is AppwriteException)
    }

    @Test
    fun `createMiningSession should return success when API call succeeds`() = runBlocking {
        // Given
        val userId = "test-user-id"
        val document = mockk<Document>()
        every { document.id } returns "new-session-id"
        every { document.data } returns mapOf(
            "userId" to userId,
            "coinsEarned" to 0.0,
            "clicksMade" to 0,
            "sessionDuration" to 0
        )
        every { document.createdAt } returns "2023-01-01T00:00:00Z"
        every { document.updatedAt } returns "2023-01-01T00:00:00Z"
        
        coEvery { 
            databases.createDocument(
                databaseId = any(),
                collectionId = any(),
                documentId = "unique()",
                data = any()
            ) 
        } returns document

        // When
        val result = miningRepository.createMiningSession(userId)

        // Then
        assert(result.isSuccess)
        val session = result.getOrNull()
        assert(session != null)
        assert(session!!.userId == userId)
        assert(session.coinsEarned == 0.0)
        assert(session.clicksMade == 0)
        assert(session.sessionDuration == 0)
    }

    @Test
    fun `updateMiningSession should return success when API call succeeds`() = runBlocking {
        // Given
        val sessionId = "test-session-id"
        val updates = mapOf("coinsEarned" to 75.0, "clicksMade" to 150)
        val document = mockk<Document>()
        every { document.id } returns sessionId
        every { document.data } returns mapOf(
            "userId" to "test-user-id",
            "coinsEarned" to 75.0,
            "clicksMade" to 150,
            "sessionDuration" to 300
        )
        every { document.createdAt } returns "2023-01-01T00:00:00Z"
        every { document.updatedAt } returns "2023-01-01T00:06:00Z"
        
        coEvery { 
            databases.updateDocument(
                databaseId = any(),
                collectionId = any(),
                documentId = sessionId,
                data = any()
            ) 
        } returns document

        // When
        val result = miningRepository.updateMiningSession(sessionId, updates)

        // Then
        assert(result.isSuccess)
        val session = result.getOrNull()
        assert(session != null)
        assert(session!!.coinsEarned == 75.0)
        assert(session.clicksMade == 150)
    }
}