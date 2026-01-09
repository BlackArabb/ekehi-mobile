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
        private const val STREAK_BONUS_AMOUNT = 5.0f
    }
    
    /**
     * Helper function to check if two Calendar instances represent the same day
     */
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        val sameYear = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
        val sameDayOfYear = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
        Log.d(TAG, "isSameDay check - Year: ${cal1.get(Calendar.YEAR)} == ${cal2.get(Calendar.YEAR)} = $sameYear")
        Log.d(TAG, "isSameDay check - Day of Year: ${cal1.get(Calendar.DAY_OF_YEAR)} == ${cal2.get(Calendar.DAY_OF_YEAR)} = $sameDayOfYear")
        return sameYear && sameDayOfYear
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
            // Get current date (normalized to start of day) - USE UTC to match lastLoginCalendar
            val todayCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            
            Log.d(TAG, "Today's date (UTC): ${todayCalendar.time}")
            
            // Parse last login date if it exists
            val lastLoginCalendar = userProfile.lastLoginDate?.let { dateString ->
                try {
                    Log.d(TAG, "Parsing last login date: $dateString")
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                    val parsedDate = dateFormat.parse(dateString)
                    Log.d(TAG, "Parsed date: $parsedDate")
                    
                    if (parsedDate != null) {
                        // Create calendar instance with UTC timezone to ensure consistency
                        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        calendar.time = parsedDate
                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        
                        Log.d(TAG, "Normalized last login date: ${calendar.time}")
                        calendar
                    } else {
                        Log.w(TAG, "Parsed date is null")
                        null
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
            val isNewDayLogin = when {
                lastLoginCalendar == null -> {
                    Log.d(TAG, "First login ever")
                    true
                }
                isSameDay(lastLoginCalendar, todayCalendar) -> {
                    Log.d(TAG, "Same day login detected")
                    false
                }
                else -> {
                    Log.d(TAG, "New day login detected")
                    true
                }
            }
            
            Log.d(TAG, "isNewDayLogin: $isNewDayLogin")
            Log.d(TAG, "lastLoginCalendar: ${lastLoginCalendar?.time}")
            Log.d(TAG, "todayCalendar: ${todayCalendar.time}")
            
            if (isNewDayLogin) {
                // Calculate the difference in days
                val diffDays = if (lastLoginCalendar != null) {
                    // Calculate days between dates using a more reliable method
                    // Convert both dates to milliseconds at start of day and calculate difference
                    val lastLoginMillis = lastLoginCalendar.timeInMillis
                    val todayMillis = todayCalendar.timeInMillis
                    val oneDayInMillis = 24 * 60 * 60 * 1000L
                    // Use Math.round to properly round the difference
                    val diffDaysCalculated = Math.round((todayMillis - lastLoginMillis).toDouble() / oneDayInMillis).toInt()
                    Log.d(TAG, "📅 Raw millisecond difference: ${todayMillis - lastLoginMillis} ms")
                    Log.d(TAG, "📅 Calculated difference: $diffDaysCalculated days")
                    diffDaysCalculated
                } else {
                    1 // First login ever - treat as 1 day difference to start streak
                }
                
                Log.d(TAG, "📅 Days since last login: $diffDays")
                
                // Initialize updatedStreak based on the logic
                updatedStreak = when {
                    lastLoginCalendar == null -> {
                        // First login ever
                        Log.d(TAG, "🎉 First login! Starting streak at 1")
                        1
                    }
                    diffDays == 1 -> {
                        // Consecutive day - increment streak
                        val newStreak = userProfile.currentStreak + 1
                        Log.d(TAG, "✅ Consecutive login! New streak: $newStreak")
                        
                        // Check if user has reached 7 consecutive days
                        // Award bonus when user completes 7 consecutive days and reset streak
                        if (newStreak == STREAK_BONUS_THRESHOLD) {
                            // Award 5 EKH bonus
                            updatedTotalCoins += STREAK_BONUS_AMOUNT
                            updatedStreakBonusClaimed += 1
                            bonusAwarded = true
                            
                            Log.d(TAG, "🎉🎉🎉 USER ACHIEVED 7-DAY STREAK!")
                            Log.d(TAG, "💰 Awarding $STREAK_BONUS_AMOUNT EKH bonus")
                            
                            // Reset streak to 1 after achieving 7-day streak
                            Log.d(TAG, "🔄 Resetting streak from $newStreak to 1 after 7-day achievement")
                            1
                        } else {
                            // Continue streak without resetting
                            newStreak
                        }
                    }
                    diffDays > 1 -> {
                        // Missed days - reset streak to 1
                        Log.d(TAG, "❌ Missed $diffDays days. Resetting streak to 1")
                        1
                    }
                    else -> {
                        // diffDays == 0 (same day login)
                        Log.d(TAG, "⚠️ Same day login. Keeping current streak: ${userProfile.currentStreak}")
                        emit(Resource.Success(userProfile))
                        return@flow
                    }
                }
                
                // Update longest streak if needed (before reset)
                if (updatedStreak > userProfile.longestStreak) {
                    updatedLongestStreak = updatedStreak
                    Log.d(TAG, "⭐ New longest streak: $updatedLongestStreak")
                }
                
                // Prepare updates with UTC timestamp
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                val currentDateTime = dateFormat.format(Date())
                
                Log.d(TAG, "Setting lastLoginDate to: $currentDateTime")
                
                val updates = mutableMapOf<String, Any>(
                    "currentStreak" to updatedStreak,
                    "longestStreak" to updatedLongestStreak,
                    "lastLoginDate" to currentDateTime,
                    "updatedAt" to currentDateTime
                )
                
                // Only update coins and streak bonus if they changed
                if (updatedTotalCoins != userProfile.totalCoins) {
                    // Since we removed totalCoins from the database, we need to update the individual reward fields
                    // For streak bonus, we'll add to taskReward
                    updates["taskReward"] = updatedTotalCoins
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
                val updates = mapOf<String, Any>(
                    "taskReward" to newBalance,
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
}