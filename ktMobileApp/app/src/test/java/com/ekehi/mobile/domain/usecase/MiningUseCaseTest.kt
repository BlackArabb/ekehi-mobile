package com.ekehi.mobile.domain.usecase

import com.ekehi.mobile.data.model.MiningSession
import com.ekehi.mobile.data.repository.MiningRepository
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test

class MiningUseCaseTest {
    private lateinit var miningUseCase: MiningUseCase
    private lateinit var miningRepository: MiningRepository

    @Before
    fun setUp() {
        miningRepository = mockk()
        miningUseCase = MiningUseCase(miningRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `startMiningSession should emit success when repository returns success`() = runBlocking {
        // Given
        val userId = "test-user-id"
        val miningSession = MiningSession(
            id = "session-id",
            userId = userId,
            coinsEarned = 0.0,
            clicksMade = 0,
            sessionDuration = 0,
            createdAt = "2023-01-01T00:00:00Z",
            updatedAt = "2023-01-01T00:00:00Z"
        )
        coEvery { miningRepository.createMiningSession(userId) } returns Result.success(miningSession)

        // When
        val result = miningUseCase.startMiningSession(userId)

        // Then
        val resource = result.first()
        assert(resource is com.ekehi.mobile.domain.model.Resource.Success)
    }

    @Test
    fun `startMiningSession should emit error when repository returns failure`() = runBlocking {
        // Given
        val userId = "test-user-id"
        val exception = Exception("Failed to start mining session")
        coEvery { miningRepository.createMiningSession(userId) } returns Result.failure(exception)

        // When
        val result = miningUseCase.startMiningSession(userId)

        // Then
        val resource = result.first()
        assert(resource is com.ekehi.mobile.domain.model.Resource.Error)
    }

    @Test
    fun `updateMiningSession should emit success when repository returns success`() = runBlocking {
        // Given
        val sessionId = "session-id"
        val updates = mapOf("coinsEarned" to 50.0, "clicksMade" to 100)
        val miningSession = MiningSession(
            id = sessionId,
            userId = "test-user-id",
            coinsEarned = 50.0,
            clicksMade = 100,
            sessionDuration = 300,
            createdAt = "2023-01-01T00:00:00Z",
            updatedAt = "2023-01-01T00:05:00Z"
        )
        coEvery { miningRepository.updateMiningSession(sessionId, updates) } returns Result.success(miningSession)

        // When
        val result = miningUseCase.updateMiningSession(sessionId, updates)

        // Then
        val resource = result.first()
        assert(resource is com.ekehi.mobile.domain.model.Resource.Success)
    }

    @Test
    fun `getMiningSession should emit success when repository returns success`() = runBlocking {
        // Given
        val sessionId = "session-id"
        coEvery { miningRepository.getMiningSession(sessionId) } returns Result.success(mockk())

        // When
        val result = miningUseCase.getMiningSession(sessionId)

        // Then
        val resource = result.first()
        assert(resource is com.ekehi.mobile.domain.model.Resource.Success)
    }
}