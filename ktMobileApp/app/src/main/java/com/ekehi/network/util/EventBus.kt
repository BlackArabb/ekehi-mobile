package com.ekehi.network.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object EventBus {
    private val _events = MutableSharedFlow<Event>(replay = 0)
    val events = _events.asSharedFlow()

    suspend fun sendEvent(event: Event) {
        _events.emit(event)
    }
}

sealed class Event {
    object RefreshUserProfile : Event()
    object RefreshLeaderboard : Event()
    data class RefreshSocialTasks(val userId: String) : Event()
}