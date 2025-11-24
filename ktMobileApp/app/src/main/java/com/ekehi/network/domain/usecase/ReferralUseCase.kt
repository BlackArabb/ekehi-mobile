package com.ekehi.network.domain.usecase

import com.ekehi.network.data.model.Referral
import com.ekehi.network.data.repository.UserRepository
import com.ekehi.network.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch
import javax.inject.Inject

class ReferralUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    
    suspend fun getReferrals(): List<Referral> {
        return try {
            userRepository.getReferrals()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Claims a referral code for the current user
     * @param userId The ID of the user claiming the referral
     * @param referralCode The referral code to claim
     * @return Flow<Resource<String>> with success message or error
     */
    fun claimReferral(userId: String, referralCode: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading)
        val result = userRepository.claimReferral(userId, referralCode)
        if (result.isSuccess) {
            emit(Resource.Success(result.getOrNull()!!))
        } else {
            emit(Resource.Error("Failed to claim referral: ${result.exceptionOrNull()?.message}"))
        }
    }.catch { e ->
        emit(Resource.Error("Error claiming referral: ${e.message}"))
    }
}