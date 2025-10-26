package com.ekehi.network.performance

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerformanceMonitor @Inject constructor() {
    private val tag = "PerformanceMonitor"
    
    fun measureExecutionTime(block: () -> Unit, operationName: String): Long {
        val startTime = System.currentTimeMillis()
        block()
        val endTime = System.currentTimeMillis()
        val executionTime = endTime - startTime
        
        Log.d(tag, "Operation '$operationName' took ${executionTime}ms")
        
        return executionTime
    }
    
    fun <T> measureExecutionTimeWithResult(block: () -> T, operationName: String): Pair<T, Long> {
        val startTime = System.currentTimeMillis()
        val result = block()
        val endTime = System.currentTimeMillis()
        val executionTime = endTime - startTime
        
        Log.d(tag, "Operation '$operationName' took ${executionTime}ms")
        
        return Pair(result, executionTime)
    }
    
    fun logMemoryUsage() {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val memoryUsagePercent = (usedMemory.toDouble() / maxMemory.toDouble()) * 100
        
        Log.d(tag, "Memory usage: ${usedMemory / (1024 * 1024)}MB / ${maxMemory / (1024 * 1024)}MB (${String.format("%.2f", memoryUsagePercent)}%)")
    }
    
    fun logFrameRate(fps: Double) {
        Log.d(tag, "Current frame rate: ${String.format("%.2f", fps)} FPS")
    }
}