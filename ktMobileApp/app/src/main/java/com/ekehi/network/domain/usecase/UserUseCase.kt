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
        private const val STREAK_BONUS_THRESHOLD = 7
        private const val STREAK_BONUS_AMOUNT = 5.0
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
     * 
     * Streak Rules:
     * 1. Streak increases by 1 every consecutive day
     * 2. If user misses a day, streak resets to 1
     * 3. If user completes 7 consecutive days, they get 5 EKH bonus and streak resets to 1
     * 
     * @param userId The user's ID
     * @param userProfile The current user profile
     * @return Flow<Resource<UserProfile>> with updated profile
     */
    fun updateStreak(userId: String, userProfile: UserProfile): Flow<Resource<UserProfile>> = flow {
        Log.d(TAG, "==========================================")
        Log.d(TAG, "Starting streak update for user: $userId")
        Log.d(TAG, "Current streak: ${userProfile.currentStreak}")
        Log.d(TAG, "Longest streak: ${userProfile.longestStreak}")
        Log.d(TAG, "Last login: ${userProfile.lastLoginDate}")
        Log.d(TAG, "==========================================")
        
        emit(Resource.Loading)
        
        try {
            // Get current date (normalized to start of day)
            val todayCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            
            // Parse last login date if it exists
            val lastLoginCalendar = userProfile.lastLoginDate?.let { dateString ->
                try {
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(dateString)?.let {
                        Calendar.getInstance().apply {
                            time = it
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
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
            var bonusAwarded = false
            
            // Check if this is a new day login
            if (lastLoginCalendar == null || lastLoginCalendar.before(todayCalendar)) {
                // Calculate the difference in days
                val diffDays = if (lastLoginCalendar != null) {
                    val diffInMillis = todayCalendar.timeInMillis - lastLoginCalendar.timeInMillis
                    (diffInMillis / (24 * 60 * 60 * 1000)).toInt()
                } else {
                    0 // First login ever - start with streak 1
                }
                
                Log.d(TAG, "📅 Days since last login: $diffDays")
                
                when {
                    lastLoginCalendar == null -> {
                        // First login ever
                        updatedStreak = 1
                        Log.d(TAG, "🎉 First login! Starting streak at 1")
                    }
                    diffDays == 1 -> {
                        // Consecutive day - increment streak
                        updatedStreak = userProfile.currentStreak + 1
                        Log.d(TAG, "✅ Consecutive login! New streak: $updatedStreak")
                        
                        // Check if user has reached 7 consecutive days
                        if (updatedStreak == STREAK_BONUS_THRESHOLD) {
                            // Award 5 EKH bonus
                            updatedTotalCoins += STREAK_BONUS_AMOUNT
                            updatedStreakBonusClaimed += 1
                            bonusAwarded = true
                            
                            Log.d(TAG, "🎉🎉🎉 USER ACHIEVED 7-DAY STREAK!")
                            Log.d(TAG, "💰 Awarding $STREAK_BONUS_AMOUNT EKH bonus")
                            Log.d(TAG, "🔄 Resetting streak to 1 for new cycle")
                            
                            // Reset streak to 1 after 7 days (start new cycle)
                            updatedStreak = 1
                        }
                        
                        // Update longest streak if needed (before reset)
                        if (updatedStreak > userProfile.longestStreak) {
                            updatedLongestStreak = updatedStreak
                            Log.d(TAG, "⭐ New longest streak: $updatedLongestStreak")
                        }
                    }
                    diffDays > 1 -> {
                        // Missed days - reset streak to 1
                        updatedStreak = 1
                        Log.d(TAG, "❌ Missed $diffDays days. Resetting streak to 1")
                    }
                    else -> {
                        // diffDays == 0 or negative (shouldn't happen but handle it)
                        Log.d(TAG, "⚠️ Same day login or invalid date. Keeping current streak: ${userProfile.currentStreak}")
                        emit(Resource.Success(userProfile))
                        return@flow
                    }
                }
                
                // Prepare updates
                val currentDateTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date())
                val updates = mutableMapOf<String, Any>(
                    "currentStreak" to updatedStreak,
                    "longestStreak" to updatedLongestStreak,
                    "lastLoginDate" to currentDateTime,
                    "updatedAt" to currentDateTime
                )
                
                // Only update coins and streak bonus if they changed
                if (updatedTotalCoins != userProfile.totalCoins) {
                    updates["totalCoins"] = updatedTotalCoins
                    Log.d(TAG, "💰 Total coins updated: ${userProfile.totalCoins} -> $updatedTotalCoins")
                }
                
                if (updatedStreakBonusClaimed != userProfile.streakBonusClaimed) {
                    updates["streakBonusClaimed"] = updatedStreakBonusClaimed
                    Log.d(TAG, "🎁 Streak bonuses claimed: ${userProfile.streakBonusClaimed} -> $updatedStreakBonusClaimed")
                }
                
                Log.d(TAG, "📝 Updating user profile with:")
                Log.d(TAG, "   Current Streak: ${userProfile.currentStreak} -> $updatedStreak")
                Log.d(TAG, "   Longest Streak: ${userProfile.longestStreak} -> $updatedLongestStreak")
                if (bonusAwarded) {
                    Log.d(TAG, "   🎁 Bonus Awarded: +$STREAK_BONUS_AMOUNT EKH")
                }
                
                // Update the profile with new streak data
                val result = userRepository.updateUserProfile(userId, updates)
                
                if (result.isSuccess) {
                    val updatedProfile = result.getOrNull()!!
                    Log.d(TAG, "==========================================")
                    Log.d(TAG, "✅ USER STREAK UPDATED SUCCESSFULLY")
                    Log.d(TAG, "   Current Streak: ${updatedProfile.currentStreak}")
                    Log.d(TAG, "   Longest Streak: ${updatedProfile.longestStreak}")
                    Log.d(TAG, "   Total Coins: ${updatedProfile.totalCoins}")
                    if (bonusAwarded) {
                        Log.d(TAG, "   🎉 7-DAY STREAK BONUS AWARDED!")
                    }
                    Log.d(TAG, "==========================================")
                    emit(Resource.Success(updatedProfile))
                } else {
                    val error = result.exceptionOrNull()
                    val errorMessage = "Failed to update user streak: ${error?.message ?: "Unknown error"}"
                    Log.e(TAG, "❌ $errorMessage", error)
                    emit(Resource.Error(errorMessage))
                }
            } else {
                Log.d(TAG, "ℹ️ User already logged in today, no streak update needed")
                Log.d(TAG, "==========================================")
                emit(Resource.Success(userProfile))
            }
        } catch (e: Exception) {
            val errorMessage = "Error updating streak: ${e.message ?: "Unknown error"}"
            Log.e(TAG, "==========================================")
            Log.e(TAG, "❌ STREAK UPDATE ERROR")
            Log.e(TAG, errorMessage, e)
            Log.e(TAG, "==========================================")
            emit(Resource.Error(errorMessage))
        }
    }.catch { e ->
        val errorMessage = "Streak update error: ${e.message ?: "Unknown error"}"
        Log.e(TAG, "==========================================")
        Log.e(TAG, "❌ STREAK UPDATE EXCEPTION")
        Log.e(TAG, errorMessage, e)
        Log.e(TAG, "==========================================")
        emit(Resource.Error(errorMessage))
    }
}