package com.ekehi.network.util

import android.util.Log

object DebugLogger {
    private const val TAG = "OAUTH_FLOW_DEBUG"
    
    fun logStep(step: String, details: String = "") {
        val timestamp = System.currentTimeMillis()
        val message = if (details.isNotEmpty()) {
            "[$timestamp] STEP: $step | DETAILS: $details"
        } else {
            "[$timestamp] STEP: $step"
        }
        Log.d(TAG, "=".repeat(80))
        Log.d(TAG, message)
        Log.d(TAG, "=".repeat(80))
    }
    
    fun logState(component: String, state: String, data: Map<String, Any?> = emptyMap()) {
        Log.d(TAG, ">>> STATE CHANGE: $component -> $state")
        if (data.isNotEmpty()) {
            data.forEach { (key, value) ->
                Log.d(TAG, "    $key: $value")
            }
        }
    }
    
    fun logError(component: String, error: String, exception: Exception? = null) {
        Log.e(TAG, "❌ ERROR in $component: $error")
        exception?.let {
            Log.e(TAG, "    Exception: ${it.message}", it)
        }
    }
    
    fun logNavigation(from: String, to: String, reason: String = "") {
        Log.d(TAG, "🚀 NAVIGATION: $from -> $to ${if (reason.isNotEmpty()) "| Reason: $reason" else ""}")
    }
}