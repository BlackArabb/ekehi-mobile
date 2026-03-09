package com.ekehi.network.domain.usecase

import android.util.Log
import com.ekehi.network.data.model.UserProfile
import com.ekehi.network.data.repository.UserRepository
import com.ekehi.network.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

open class UserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    companion object {
        private const val TAG = "UserUseCase"
    }
    
    fun getUserProfile(userId: String): Flow<Resource<UserProfile>> = flow {
        emit(Resource.Loading)
        val result = userRepository.getUserProfile(userId)
        if (result.isSuccess) {
            emit(Resource.Success(result.getOrNull()!!))
        } else {
            emit(Resource.Error("Failed to get user profile: ${result.exceptionOrNull()?.message}"))
        }
    }.catch { e ->
        emit(Resource.Error("Error getting user profile: ${e.message}"))
    }

    fun createUserProfile(userId: String, displayName: String, email: String? = null, phoneNumber: String = "", country: String = ""): Flow<Resource<UserProfile>> = flow {
        emit(Resource.Loading)
        val result = userRepository.createUserProfile(userId, displayName, email, phoneNumber, country)
        if (result.isSuccess) {
            emit(Resource.Success(result.getOrNull()!!))
        } else {
            emit(Resource.Error("Failed to create user profile: ${result.exceptionOrNull()?.message}"))
        }
    }.catch { e ->
        emit(Resource.Error("Error creating user profile: ${e.message}"))
    }

    /**
     * Updates user's total coin balance
     * @param userId The user ID
     * @param newBalance The new balance to set
     * @return Flow<Resource<UserProfile>> with updated profile
     */
    fun updateUserBalance(userId: String, newBalance: Float): Flow<Resource<UserProfile>> = flow {
        Log.d(TAG, "=== UPDATING USER BALANCE ===")
        Log.d(TAG, "User ID: $userId")
        Log.d(TAG, "New balance: $newBalance")
        
        emit(Resource.Loading)
        
        try {
            // Get current profile first
            val profileResult = userRepository.getUserProfile(userId)
            if (profileResult.isSuccess) {
                val currentProfile = profileResult.getOrNull()!!
                
                // Prepare updates
                val newTaskReward = newBalance
                val newTotalCoins = newTaskReward + currentProfile.miningReward + currentProfile.referralReward
                
                val updates = mapOf<String, Any>(
                    "taskReward" to newTaskReward,
                    "totalCoins" to newTotalCoins,
                    "updatedAt" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }.format(Date())
                )
                
                // Update the profile
                val result = userRepository.updateUserProfile(userId, updates)
                
                if (result.isSuccess) {
                    val updatedProfile = result.getOrNull()!!
                    Log.d(TAG, "✅ User balance updated successfully")
                    Log.d(TAG, "   Previous balance: ${currentProfile.totalCoins}")
                    Log.d(TAG, "   New balance: ${updatedProfile.totalCoins}")
                    emit(Resource.Success(updatedProfile))
                } else {
                    val error = result.exceptionOrNull()
                    val errorMessage = "Failed to update user balance: ${error?.message ?: "Unknown error"}"
                    Log.e(TAG, "❌ $errorMessage", error)
                    emit(Resource.Error(errorMessage))
                }
            } else {
                val error = profileResult.exceptionOrNull()
                val errorMessage = "Failed to get current profile: ${error?.message ?: "Unknown error"}"
                Log.e(TAG, "❌ $errorMessage", error)
                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
            val errorMessage = "Error updating user balance: ${e.message ?: "Unknown error"}"
            Log.e(TAG, "❌ BALANCE UPDATE ERROR", e)
            emit(Resource.Error(errorMessage))
        }
    }.catch { e ->
        val errorMessage = "Balance update error: ${e.message ?: "Unknown error"}"
        Log.e(TAG, "❌ BALANCE UPDATE EXCEPTION", e)
        emit(Resource.Error(errorMessage))
    }

    /**
     * Updates user profile with the provided fields
     * @param userId The user ID
     * @param updates Map of field names to values to update
     * @return Flow<Resource<UserProfile>> with updated profile
     */
    fun updateUserProfile(userId: String, updates: Map<String, Any>): Flow<Resource<UserProfile>> = flow {
        Log.d(TAG, "=== UPDATING USER PROFILE ===")
        Log.d(TAG, "User ID: $userId")
        Log.d(TAG, "Updates: $updates")
        
        emit(Resource.Loading)
        
        try {
            val result = userRepository.updateUserProfile(userId, updates)
            
            if (result.isSuccess) {
                val updatedProfile = result.getOrNull()!!
                Log.d(TAG, "✅ User profile updated successfully")
                emit(Resource.Success(updatedProfile))
            } else {
                val error = result.exceptionOrNull()
                val errorMessage = "Failed to update user profile: ${error?.message ?: "Unknown error"}"
                Log.e(TAG, "❌ $errorMessage", error)
                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
            val errorMessage = "Error updating user profile: ${e.message ?: "Unknown error"}"
            Log.e(TAG, "❌ PROFILE UPDATE ERROR", e)
            emit(Resource.Error(errorMessage))
        }
    }.catch { e ->
        val errorMessage = "Profile update error: ${e.message ?: "Unknown error"}"
        Log.e(TAG, "❌ PROFILE UPDATE EXCEPTION", e)
        emit(Resource.Error(errorMessage))
    }

    /**
     * Check if a phone number already exists in the system
     * @param phoneNumber The phone number to check
     * @param currentUserId The current user's ID (to exclude from check)
     * @return Flow<Resource<Boolean>> - true if phone exists, false otherwise
     */
    fun checkPhoneNumberExists(phoneNumber: String, currentUserId: String? = null): Flow<Resource<Boolean>> = flow {
        Log.d(TAG, "=== CHECKING PHONE NUMBER EXISTS ===")
        Log.d(TAG, "Phone: $phoneNumber, CurrentUserId: $currentUserId")
        
        emit(Resource.Loading)
        
        try {
            val result = userRepository.checkPhoneNumberExists(phoneNumber, currentUserId)
            
            if (result.isSuccess) {
                val exists = result.getOrNull() ?: false
                Log.d(TAG, "Phone number check result: exists=$exists")
                emit(Resource.Success(exists))
            } else {
                val error = result.exceptionOrNull()
                val errorMessage = "Failed to check phone number: ${error?.message ?: "Unknown error"}"
                Log.e(TAG, "❌ $errorMessage", error)
                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
            val errorMessage = "Error checking phone number: ${e.message ?: "Unknown error"}"
            Log.e(TAG, "❌ PHONE CHECK EXCEPTION", e)
            emit(Resource.Error(errorMessage))
        }
    }.catch { e ->
        val errorMessage = "Phone check error: ${e.message ?: "Unknown error"}"
        Log.e(TAG, "❌ PHONE CHECK EXCEPTION", e)
        emit(Resource.Error(errorMessage))
    }
}