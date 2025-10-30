package com.ekehi.network.domain.usecase

import com.ekehi.network.data.model.MiningSession
import com.ekehi.network.data.repository.MiningRepository
import com.ekehi.network.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

open class MiningUseCase @Inject constructor(
    private val miningRepository: MiningRepository
) {
    // This use case is kept for compatibility but not used in the new implementation
    // The new implementation uses direct repository calls in the ViewModel
}
