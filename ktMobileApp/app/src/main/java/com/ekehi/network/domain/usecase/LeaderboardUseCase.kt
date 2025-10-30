package com.ekehi.network.domain.usecase

import com.ekehi.network.data.repository.LeaderboardRepository
import com.ekehi.network.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class LeaderboardUseCase @Inject constructor(
    private val leaderboardRepository: LeaderboardRepository
) {
    fun getLeaderboard(): Flow<Resource<List<Map<String, Any>>>> = flow {
        emit(Resource.Loading)
        val result = leaderboardRepository.getVerifiedLeaderboard()
        if (result.isSuccess) {
            val data = result.getOrNull() ?: emptyList()
            emit(Resource.Success(data))
        } else {
            emit(Resource.Error("Failed to get leaderboard: ${result.exceptionOrNull()?.message}"))
        }
    }.catch { e ->
        emit(Resource.Error("Error getting leaderboard: ${e.message}"))
    }

    fun getUserRank(userId: String): Flow<Resource<Int>> = flow {
        emit(Resource.Loading)
        val result = leaderboardRepository.getUserRank(userId)
        if (result.isSuccess) {
            val data = result.getOrNull() ?: 0
            emit(Resource.Success(data))
        } else {
            emit(Resource.Error("Failed to get user rank: ${result.exceptionOrNull()?.message}"))
        }
    }.catch { e ->
        emit(Resource.Error("Error getting user rank: ${e.message}"))
    }
}