package com.ekehi.mobile.domain.usecase

import com.ekehi.mobile.data.model.SocialTask
import com.ekehi.mobile.data.repository.SocialTaskRepository
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test

class SocialTaskUseCaseTest {
    private lateinit var socialTaskUseCase: SocialTaskUseCase
    private lateinit var socialTaskRepository: SocialTaskRepository

    @Before
    fun setUp() {
        socialTaskRepository = mockk()
        socialTaskUseCase = SocialTaskUseCase(socialTaskRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `getSocialTasks should emit success when repository returns success`() = runBlocking {
        // Given
        val socialTasks = listOf(
            SocialTask(
                id = "task-1",
                title = "Follow on Twitter",
                description = "Follow our official Twitter account",
                platform = "Twitter",
                taskType = "follow",
                rewardCoins = 5.0,
                actionUrl = "https://twitter.com/ekehi",
                verificationMethod = "manual",
                isActive = true,
                sortOrder = 1,
                isCompleted = false,
                completedAt = null,
                createdAt = "2023-01-01T00:00:00Z",
                updatedAt = "2023-01-01T00:00:00Z"
            )
        )
        coEvery { socialTaskRepository.getSocialTasks() } returns Result.success(socialTasks)

        // When
        val result = socialTaskUseCase.getSocialTasks()

        // Then
        val resource = result.first()
        assert(resource is com.ekehi.mobile.domain.model.Resource.Success)
    }

    @Test
    fun `getUserSocialTasks should emit success when repository returns success`() = runBlocking {
        // Given
        val userId = "test-user-id"
        val socialTasks = listOf(
            SocialTask(
                id = "task-1",
                title = "Follow on Twitter",
                description = "Follow our official Twitter account",
                platform = "Twitter",
                taskType = "follow",
                rewardCoins = 5.0,
                actionUrl = "https://twitter.com/ekehi",
                verificationMethod = "manual",
                isActive = true,
                sortOrder = 1,
                isCompleted = true,
                completedAt = "2023-01-01T00:00:00Z",
                createdAt = "2023-01-01T00:00:00Z",
                updatedAt = "2023-01-01T00:00:00Z"
            )
        )
        coEvery { socialTaskRepository.getUserSocialTasks(userId) } returns Result.success(socialTasks)

        // When
        val result = socialTaskUseCase.getUserSocialTasks(userId)

        // Then
        val resource = result.first()
        assert(resource is com.ekehi.mobile.domain.model.Resource.Success)
    }

    @Test
    fun `completeSocialTask should emit success when repository returns success`() = runBlocking {
        // Given
        val userId = "test-user-id"
        val taskId = "task-1"
        coEvery { socialTaskRepository.completeSocialTask(userId, taskId) } returns Result.success(Unit)

        // When
        val result = socialTaskUseCase.completeSocialTask(userId, taskId)

        // Then
        val resource = result.first()
        assert(resource is com.ekehi.mobile.domain.model.Resource.Success)
    }

    @Test
    fun `getSocialTasks should emit error when repository returns failure`() = runBlocking {
        // Given
        val exception = Exception("Failed to get social tasks")
        coEvery { socialTaskRepository.getSocialTasks() } returns Result.failure(exception)

        // When
        val result = socialTaskUseCase.getSocialTasks()

        // Then
        val resource = result.first()
        assert(resource is com.ekehi.mobile.domain.model.Resource.Error)
    }
}