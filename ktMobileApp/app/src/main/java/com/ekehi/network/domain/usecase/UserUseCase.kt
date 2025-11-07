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

    fun createUserProfile(userId: String, displayName: String): Flow<Resource<UserProfile>> = flow {
        emit(Resource.Loading)
        val result = userRepository.createUserProfile(userId, displayName)
        if (result.isSuccess) {
            emit(Resource.Success(result.getOrNull()!!))
        } else {
            emit(Resource.Error("Failed to create user profile: ${result.exceptionOrNull()?.message}"))
        }
    }.catch { e ->
        emit(Resource.Error("Error creating user profile: ${e.message}"))
    }
    
    /**
     * Updates the user's streak based on their last login date
     * @param userId The user's ID
     * @param userProfile The current user profile
     * @return Flow<Resource<UserProfile>> with updated profile
     */
    fun updateStreak(userId: String, userProfile: UserProfile): Flow<Resource<UserProfile>> = flow {
        Log.d(TAG, "Starting streak update for user: $userId")
        emit(Resource.Loading)
        
        try {
            // Get current date (normalized to start of day)
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            
            // Parse last login date if it exists
            val lastLoginDate = userProfile.lastLoginDate?.let { dateString ->
                try {
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(dateString)?.let {
                        Calendar.getInstance().apply {
                            time = it
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.time
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse last login date: $dateString", e)
                    null
                }
            }
            
            var updatedStreak = userProfile.currentStreak
            var updatedLongestStreak = userProfile.longestStreak
            var updatedTotalCoins = userProfile.totalCoins
            var updatedStreakBonusClaimed = userProfile.streakBonusClaimed
            
            // Check if this is a new day login
            if (lastLoginDate == null || lastLoginDate.before(today)) {
                // Calculate the difference in days
                val diffDays = if (lastLoginDate != null) {
                    val diffInMillis = today.time - lastLoginDate.time
                    (diffInMillis / (24 * 60 * 60 * 1000)).toInt()
                } else {
                    1 // First login
                }
                
                Log.d(TAG, "Days since last login: $diffDays")
                
                if (diffDays == 1) {
                    // Consecutive day - increment streak
                    updatedStreak = userProfile.currentStreak + 1
                    Log.d(TAG, "Consecutive login. New streak: $updatedStreak")
                    
                    // Check if user has reached 7 consecutive days
                    if (updatedStreak == 7 && userProfile.streakBonusClaimed < 1) {
                        // Award 5 EKH bonus
                        updatedTotalCoins += 5.0
                        updatedStreakBonusClaimed += 1
                        Log.d(TAG, "🎉 User achieved 7-day streak! Awarding 5 EKH bonus.")
                    }
                    
                    // Update longest streak if needed
                    if (updatedStreak > userProfile.longestStreak) {
                        updatedLongestStreak = updatedStreak
                        Log.d(TAG, "New longest streak: $updatedLongestStreak")
                    }
                } else if (diffDays > 1) {
                    // Missed days - reset streak
                    updatedStreak = 1
                    Log.d(TAG, "Missed days. Resetting streak to 1")
                }
                
                // Prepare updates
                val updates = mutableMapOf<String, Any>(
                    "currentStreak" to updatedStreak,
                    "longestStreak" to updatedLongestStreak,
                    "lastLoginDate" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date()),
                    "updatedAt" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date())
                )
                
                // Only update coins and streak bonus if they changed
                if (updatedTotalCoins != userProfile.totalCoins) {
                    updates["totalCoins"] = updatedTotalCoins
                }
                
                if (updatedStreakBonusClaimed != userProfile.streakBonusClaimed) {
                    updates["streakBonusClaimed"] = updatedStreakBonusClaimed
                }
                
                // Update the profile with new streak data
                val result = userRepository.updateUserProfile(userId, updates)
                
                if (result.isSuccess) {
                    val updatedProfile = result.getOrNull()!!
                    Log.d(TAG, "✅ User streak updated successfully: currentStreak=${updatedProfile.currentStreak}, longestStreak=${updatedProfile.longestStreak}")
                    emit(Resource.Success(updatedProfile))
                } else {
                    val error = result.exceptionOrNull()
                    val errorMessage = "Failed to update user streak: ${error?.message ?: "Unknown error"}"
                    Log.e(TAG, errorMessage, error)
                    emit(Resource.Error(errorMessage))
                }
            } else {
                Log.d(TAG, "User already logged in today, no streak update needed")
                emit(Resource.Success(userProfile))
            }
        } catch (e: Exception) {
            val errorMessage = "Error updating streak: ${e.message ?: "Unknown error"}"
            Log.e(TAG, errorMessage, e)
            emit(Resource.Error(errorMessage))
        }
    }.catch { e ->
        val errorMessage = "Streak update error: ${e.message ?: "Unknown error"}"
        Log.e(TAG, errorMessage, e)
        emit(Resource.Error(errorMessage))
    }
}