package com.ekehi.mobile.data.repository

import com.ekehi.mobile.data.local.CacheManager
import com.ekehi.mobile.data.local.CacheStrategy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

abstract class CachingRepository(
    private val cacheManager: CacheManager
) {
    protected fun <T> cacheFirst(
        cacheCall: suspend () -> T,
        networkCall: suspend () -> Result<T>,
        saveCall: suspend (T) -> Unit
    ): Flow<Result<T>> = flow {
        emit(Result.success(cacheCall()))
        
        // Try to refresh from network
        try {
            val networkResult = networkCall()
            if (networkResult.isSuccess) {
                val data = networkResult.getOrNull()
                if (data != null) {
                    saveCall(data)
                    emit(Result.success(data))
                }
            } else {
                // Network failed, but we already emitted cache data
                // So we don't emit an error here
            }
        } catch (e: Exception) {
            // Network failed, but we already emitted cache data
            // So we don't emit an error here
        }
    }
    
    protected fun <T> networkFirst(
        networkCall: suspend () -> Result<T>,
        cacheCall: suspend () -> T,
        saveCall: suspend (T) -> Unit
    ): Flow<Result<T>> = flow {
        try {
            val networkResult = networkCall()
            if (networkResult.isSuccess) {
                val data = networkResult.getOrNull()
                if (data != null) {
                    saveCall(data)
                    emit(Result.success(data))
                }
            } else {
                // Try cache as fallback
                try {
                    val cacheData = cacheCall()
                    emit(Result.success(cacheData))
                } catch (cacheException: Exception) {
                    emit(Result.failure(networkResult.exceptionOrNull() ?: cacheException))
                }
            }
        } catch (e: Exception) {
            // Try cache as fallback
            try {
                val cacheData = cacheCall()
                emit(Result.success(cacheData))
            } catch (cacheException: Exception) {
                emit(Result.failure(e))
            }
        }
    }
    
    fun <T> executeWithStrategy(
        strategy: CacheStrategy,
        cacheCall: suspend () -> T,
        networkCall: suspend () -> Result<T>,
        saveCall: suspend (T) -> Unit
    ): Flow<Result<T>> = flow {
        when (strategy) {
            CacheStrategy.CACHE_FIRST -> {
                cacheFirst(cacheCall, networkCall, saveCall).collect { emit(it) }
            }
            CacheStrategy.NETWORK_FIRST -> {
                networkFirst(networkCall, cacheCall, saveCall).collect { emit(it) }
            }
            CacheStrategy.CACHE_ONLY -> {
                try {
                    emit(Result.success(cacheCall()))
                } catch (e: Exception) {
                    emit(Result.failure(e))
                }
            }
            CacheStrategy.NETWORK_ONLY -> {
                try {
                    val result = networkCall()
                    if (result.isSuccess) {
                        val data = result.getOrNull()
                        if (data != null) {
                            saveCall(data)
                        }
                    }
                    emit(result)
                } catch (e: Exception) {
                    emit(Result.failure(e))
                }
            }
        }
    }
}