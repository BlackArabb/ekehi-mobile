package com.ekehi.mobile.data.local

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CacheManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    fun shouldUseCache(): Boolean {
        return !isNetworkAvailable()
    }
    
    fun getCacheStrategy(): CacheStrategy {
        return if (isNetworkAvailable()) {
            CacheStrategy.NETWORK_FIRST
        } else {
            CacheStrategy.CACHE_FIRST
        }
    }
}

enum class CacheStrategy {
    NETWORK_FIRST,
    CACHE_FIRST,
    CACHE_ONLY,
    NETWORK_ONLY
}