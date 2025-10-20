package com.ekehi.mobile.domain.usecase

import com.ekehi.mobile.data.repository.AuthRepository
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test

class AuthUseCaseTest {
    private lateinit var authUseCase: AuthUseCase
    private lateinit var authRepository: AuthRepository

    @Before
    fun setUp() {
        authRepository = mockk()
        authUseCase = AuthUseCase(authRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `login should emit success when repository returns success`() = runBlocking {
        // Given
        val email = "test@example.com"
        val password = "password123"
        coEvery { authRepository.login(email, password) } returns Result.success(Unit)

        // When
        val result = authUseCase.login(email, password)

        // Then
        val resource = result.first()
        assert(resource is com.ekehi.mobile.domain.model.Resource.Success)
    }

    @Test
    fun `login should emit error when repository returns failure`() = runBlocking {
        // Given
        val email = "test@example.com"
        val password = "wrongpassword"
        val exception = Exception("Invalid credentials")
        coEvery { authRepository.login(email, password) } returns Result.failure(exception)

        // When
        val result = authUseCase.login(email, password)

        // Then
        val resource = result.first()
        assert(resource is com.ekehi.mobile.domain.model.Resource.Error)
    }

    @Test
    fun `register should emit success when repository returns success`() = runBlocking {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val name = "Test User"
        coEvery { authRepository.register(email, password, name) } returns Result.success(Unit)

        // When
        val result = authUseCase.register(email, password, name)

        // Then
        val resource = result.first()
        assert(resource is com.ekehi.mobile.domain.model.Resource.Success)
    }

    @Test
    fun `register should emit error when repository returns failure`() = runBlocking {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val name = "Test User"
        val exception = Exception("Registration failed")
        coEvery { authRepository.register(email, password, name) } returns Result.failure(exception)

        // When
        val result = authUseCase.register(email, password, name)

        // Then
        val resource = result.first()
        assert(resource is com.ekehi.mobile.domain.model.Resource.Error)
    }

    @Test
    fun `logout should emit success when repository returns success`() = runBlocking {
        // Given
        coEvery { authRepository.logout() } returns Result.success(Unit)

        // When
        val result = authUseCase.logout()

        // Then
        val resource = result.first()
        assert(resource is com.ekehi.mobile.domain.model.Resource.Success)
    }
}