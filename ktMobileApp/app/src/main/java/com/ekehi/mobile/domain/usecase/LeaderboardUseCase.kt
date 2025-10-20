package com.ekehi.mobile.domain.usecase

import com.ekehi.mobile.data.repository.LeaderboardRepository
import com.ekehi.mobile.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class LeaderboardUseCase @Inject constructor(
    private val leaderboardRepository: LeaderboardRepository
) {
    fun getLeaderboard(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        val result = leaderboardRepository.getLeaderboard()
        if (result.isSuccess) {
            emit(Resource.Success(Unit))
        } else {
            emit(Resource.Error("Failed to get leaderboard: ${result.exceptionOrNull()?.message}"))
        }
    }.catch { e ->
        emit(Resource.Error("Error getting leaderboard: ${e.message}"))
    }

    fun getUserRank(userId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        val result = leaderboardRepository.getUserRank(userId)
        if (result.isSuccess) {
            emit(Resource.Success(Unit))
        } else {
            emit(Resource.Error("Failed to get user rank: ${result.exceptionOrNull()?.message}"))
        }
    }.catch { e ->
        emit(Resource.Error("Error getting user rank: ${e.message}"))
    }
}