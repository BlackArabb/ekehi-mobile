package com.ekehi.network.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Remembers the first emission from a flow and caches it.
 * The data persists across recompositions but not across process death.
 * Use [refreshData] to trigger a refresh when needed.
 * 
 * @param key Unique key to differentiate multiple cached data sets
 * @param onRefresh Callback to trigger data refresh, returns a Flow
 * @param shouldRefresh Whether to trigger a refresh
 */
@Composable
fun <T> rememberCachedData(
    key: String,
    initialValue: T? = null,
    shouldRefresh: Boolean = false,
    onRefresh: () -> Flow<T>
): CachedDataState<T> {
    // Track if initial load has occurred
    val hasLoaded = rememberSaveable(key) { mutableStateOf(false) }
    
    // Cache the loaded data
    val cachedData = remember(key) { mutableStateOf<T?>(initialValue) }
    
    // Track refresh trigger
    val refreshTrigger = remember { mutableStateOf(0) }
    
    // Refresh when triggered
    LaunchedEffect(key, shouldRefresh, refreshTrigger.value) {
        if (hasLoaded.value && !shouldRefresh) {
            return@LaunchedEffect
        }
        
        onRefresh().collect { data ->
            cachedData.value = data
            hasLoaded.value = true
        }
    }
    
    return CachedDataState(
        data = cachedData.value,
        isLoading = !hasLoaded.value,
        hasLoaded = hasLoaded.value,
        refresh = { refreshTrigger.value++ }
    )
}

/**
 * State class for cached data
 */
data class CachedDataState<T>(
    val data: T?,
    val isLoading: Boolean,
    val hasLoaded: Boolean,
    val refresh: () -> Unit
)

/**
 * Simpler version that remembers if a screen has been loaded
 * and prevents re-loading on navigation back
 */
@Composable
fun rememberScreenLoaded(key: String): Boolean {
    return rememberSaveable(key) { mutableStateOf(false) }.value
}
