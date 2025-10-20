package com.ekehi.mobile.domain.usecase

import com.ekehi.mobile.data.model.UserProfile
import com.ekehi.mobile.data.repository.UserRepository
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test

class UserUseCaseTest {
    private lateinit var userUseCase: UserUseCase
    private lateinit var userRepository: UserRepository

    @Before
    fun setUp() {
        userRepository = mockk()
        userUseCase = UserUseCase(userRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `getUserProfile should emit success when repository returns success`() = runBlocking {
        // Given
        val userId = "test-user-id"
        val userProfile = UserProfile(
            id = "profile-id",
            userId = userId,
            username = "testuser",
            totalCoins = 100.0,
            coinsPerSecond = 0.5,
            autoMiningRate = 1.0,
            miningPower = 2.0,
            referralBonusRate = 0.1,
            currentStreak = 5,
            longestStreak = 10,
            lastLoginDate = "2023-01-01",
            referralCode = "REF123",
            referredBy = null,
            totalReferrals = 3,
            lifetimeEarnings = 200.0,
            dailyMiningRate = 50.0,
            maxDailyEarnings = 100.0,
            todayEarnings = 25.0,
            lastMiningDate = "2023-01-01",
            streakBonusClaimed = 1,
            createdAt = "2023-01-01T00:00:00Z",
            updatedAt = "2023-01-01T01:00:00Z"
        )
        coEvery { userRepository.getUserProfile(userId) } returns Result.success(userProfile)

        // When
        val result = userUseCase.getUserProfile(userId)

        // Then
        val resource = result.first()
        assert(resource is com.ekehi.mobile.domain.model.Resource.Success)
    }

    @Test
    fun `getUserProfile should emit error when repository returns failure`() = runBlocking {
        // Given
        val userId = "test-user-id"
        val exception = Exception("User not found")
        coEvery { userRepository.getUserProfile(userId) } returns Result.failure(exception)

        // When
        val result = userUseCase.getUserProfile(userId)

        // Then
        val resource = result.first()
        assert(resource is com.ekehi.mobile.domain.model.Resource.Error)
    }

    @Test
    fun `createUserProfile should emit success when repository returns success`() = runBlocking {
        // Given
        val userId = "test-user-id"
        val displayName = "Test User"
        coEvery { userRepository.createUserProfile(userId, displayName) } returns Result.success(mockk())

        // When
        val result = userUseCase.createUserProfile(userId, displayName)

        // Then
        val resource = result.first()
        assert(resource is com.ekehi.mobile.domain.model.Resource.Success)
    }

    @Test
    fun `createUserProfile should emit error when repository returns failure`() = runBlocking {
        // Given
        val userId = "test-user-id"
        val displayName = "Test User"
        val exception = Exception("Failed to create profile")
        coEvery { userRepository.createUserProfile(userId, displayName) } returns Result.failure(exception)

        // When
        val result = userUseCase.createUserProfile(userId, displayName)

        // Then
        val resource = result.first()
        assert(resource is com.ekehi.mobile.domain.model.Resource.Error)
    }
}