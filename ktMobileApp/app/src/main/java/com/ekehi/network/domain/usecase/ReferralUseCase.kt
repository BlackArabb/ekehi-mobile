package com.ekehi.network.domain.usecase

import com.ekehi.network.data.model.Referral
import com.ekehi.network.data.repository.UserRepository
import com.ekehi.network.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
}