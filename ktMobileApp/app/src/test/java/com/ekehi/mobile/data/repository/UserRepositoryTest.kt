package com.ekehi.mobile.data.repository

import com.ekehi.mobile.data.model.UserProfile
import com.ekehi.mobile.network.service.AppwriteService
import io.appwrite.Client
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Databases
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test

class UserRepositoryTest {
    private lateinit var userRepository: UserRepository
    private lateinit var appwriteService: AppwriteService
    private lateinit var databases: Databases

    @Before
    fun setUp() {
        // Mock the Appwrite service and databases
        appwriteService = mockk()
        databases = mockk()
        every { appwriteService.databases } returns databases
        
        userRepository = UserRepository(appwriteService)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `getUserProfile should return success when API call succeeds`() = runBlocking {
        // Given
        val userId = "test-user-id"
        val document = mockk<io.appwrite.models.Document>()
        every { document.id } returns "profile-id"
        every { document.data } returns mapOf(
            "userId" to userId,
            "username" to "testuser",
            "totalCoins" to 100.0,
            "coinsPerSecond" to 0.5,
            "autoMiningRate" to 1.0,
            "miningPower" to 2.0,
            "referralBonusRate" to 0.1,
            "currentStreak" to 5,
            "longestStreak" to 10,
            "totalReferrals" to 3,
            "lifetimeEarnings" to 200.0,
            "dailyMiningRate" to 50.0,
            "maxDailyEarnings" to 100.0,
            "todayEarnings" to 25.0,
            "streakBonusClaimed" to 1
        )
        every { document.createdAt } returns "2023-01-01T00:00:00Z"
        every { document.updatedAt } returns "2023-01-01T01:00:00Z"
        
        coEvery { 
            databases.getDocument(
                databaseId = any(),
                collectionId = any(),
                documentId = userId
            ) 
        } returns document

        // When
        val result = userRepository.getUserProfile(userId)

        // Then
        assert(result.isSuccess)
        val profile = result.getOrNull()
        assert(profile != null)
        assert(profile!!.userId == userId)
        assert(profile.username == "testuser")
        assert(profile.totalCoins == 100.0)
    }

    @Test
    fun `getUserProfile should return failure when API call fails`() = runBlocking {
        // Given
        val userId = "test-user-id"
        coEvery { 
            databases.getDocument(
                databaseId = any(),
                collectionId = any(),
                documentId = userId
            ) 
        } throws AppwriteException("Network error")

        // When
        val result = userRepository.getUserProfile(userId)

        // Then
        assert(result.isFailure)
        assert(result.exceptionOrNull() is AppwriteException)
    }

    @Test
    fun `createUserProfile should return success when API call succeeds`() = runBlocking {
        // Given
        val userId = "test-user-id"
        val username = "testuser"
        val document = mockk<io.appwrite.models.Document>()
        every { document.id } returns userId
        every { document.data } returns mapOf(
            "userId" to userId,
            "username" to username,
            "totalCoins" to 0.0,
            "coinsPerSecond" to 0.0,
            "autoMiningRate" to 0.0,
            "miningPower" to 1.0,
            "referralBonusRate" to 0.0,
            "currentStreak" to 0,
            "longestStreak" to 0,
            "totalReferrals" to 0,
            "lifetimeEarnings" to 0.0,
            "dailyMiningRate" to 0.0,
            "maxDailyEarnings" to 100.0,
            "todayEarnings" to 0.0,
            "streakBonusClaimed" to 0
        )
        every { document.createdAt } returns "2023-01-01T00:00:00Z"
        every { document.updatedAt } returns "2023-01-01T00:00:00Z"
        
        coEvery { 
            databases.createDocument(
                databaseId = any(),
                collectionId = any(),
                documentId = userId,
                data = any()
            ) 
        } returns document

        // When
        val result = userRepository.createUserProfile(userId, username)

        // Then
        assert(result.isSuccess)
        val profile = result.getOrNull()
        assert(profile != null)
        assert(profile!!.userId == userId)
        assert(profile.username == username)
    }

    @Test
    fun `updateUserProfile should return success when API call succeeds`() = runBlocking {
        // Given
        val userId = "test-user-id"
        val updates = mapOf("totalCoins" to 150.0)
        val document = mockk<io.appwrite.models.Document>()
        every { document.id } returns userId
        every { document.data } returns mapOf(
            "userId" to userId,
            "username" to "testuser",
            "totalCoins" to 150.0,
            "coinsPerSecond" to 0.5,
            "autoMiningRate" to 1.0,
            "miningPower" to 2.0,
            "referralBonusRate" to 0.1,
            "currentStreak" to 5,
            "longestStreak" to 10,
            "totalReferrals" to 3,
            "lifetimeEarnings" to 200.0,
            "dailyMiningRate" to 50.0,
            "maxDailyEarnings" to 100.0,
            "todayEarnings" to 25.0,
            "streakBonusClaimed" to 1
        )
        every { document.createdAt } returns "2023-01-01T00:00:00Z"
        every { document.updatedAt } returns "2023-01-01T02:00:00Z"
        
        coEvery { 
            databases.updateDocument(
                databaseId = any(),
                collectionId = any(),
                documentId = userId,
                data = any()
            ) 
        } returns document

        // When
        val result = userRepository.updateUserProfile(userId, updates)

        // Then
        assert(result.isSuccess)
        val profile = result.getOrNull()
        assert(profile != null)
        assert(profile!!.totalCoins == 150.0)
    }
}