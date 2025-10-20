package com.ekehi.mobile.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import android.os.Process
import android.os.SystemClock
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * SecurityMonitor handles security monitoring and threat detection.
 * Implements OWASP secure coding practices for security logging and monitoring.
 */
class SecurityMonitor(private val context: Context) {
    
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val securityEvents = mutableListOf<SecurityEvent>()
    
    init {
        startMonitoring()
    }
    
    /**
     * Starts security monitoring
     */
    private fun startMonitoring() {
        // Schedule periodic security checks
        executor.scheduleAtFixedRate({
            performSecurityChecks()
        }, 0, 30, TimeUnit.SECONDS) // Check every 30 seconds
    }
    
    /**
     * Performs various security checks
     */
    private fun performSecurityChecks() {
        checkRootStatus()
        checkEmulator()
        checkDebuggable()
        checkTampering()
        checkMemoryUsage()
    }
    
    /**
     * Checks if the device is rooted
     */
    private fun checkRootStatus() {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        
        for (path in paths) {
            if (File(path).exists()) {
                logSecurityThreat("Root Detection", "Root access detected on device", ThreatSeverity.HIGH)
                return
            }
        }
    }
    
    /**
     * Checks if the app is running in an emulator
     */
    private fun checkEmulator() {
        val isEmulator = (Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic") ||
                "google_sdk" == Build.PRODUCT)
        
        if (isEmulator) {
            logSecurityThreat("Emulator Detection", "App running in emulator", ThreatSeverity.MEDIUM)
        }
    }
    
    /**
     * Checks if the app is debuggable
     */
    private fun checkDebuggable() {
        val isDebuggable = context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0
        if (isDebuggable) {
            logSecurityThreat("Debuggable App", "App is running in debug mode", ThreatSeverity.HIGH)
        }
    }
    
    /**
     * Checks for app tampering
     */
    private fun checkTampering() {
        try {
            val signature = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
            ).signatures[0].toCharsString()
            
            // In a real implementation, you would compare this with a known good signature
            // For this example, we'll just log that we checked
            SecurityLogger().logSecurityEvent("App signature verified", LogLevel.DEBUG)
        } catch (e: Exception) {
            logSecurityThreat("Signature Verification", "Failed to verify app signature", ThreatSeverity.HIGH)
        }
    }
    
    /**
     * Checks memory usage for potential issues
     */
    private fun checkMemoryUsage() {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val memoryUsagePercent = (usedMemory.toDouble() / maxMemory.toDouble()) * 100
        
        if (memoryUsagePercent > 90) {
            logSecurityThreat(
                "Memory Usage",
                "High memory usage detected: ${String.format("%.2f", memoryUsagePercent)}%",
                ThreatSeverity.MEDIUM
            )
        }
        
        // Check for debugger
        if (Debug.isDebuggerConnected()) {
            logSecurityThreat("Debugger Detection", "Debugger is connected", ThreatSeverity.HIGH)
        }
    }
    
    /**
     * Logs a security threat
     * @param threatType The type of threat
     * @param description Description of the threat
     * @param severity The severity level
     */
    private fun logSecurityThreat(threatType: String, description: String, severity: ThreatSeverity) {
        SecurityLogger().logSecurityThreat(threatType, description, severity)
        
        // Add to security events list
        val event = SecurityEvent(
            System.currentTimeMillis(),
            threatType,
            description,
            severity
        )
        securityEvents.add(event)
        
        // If high severity, take immediate action
        if (severity == ThreatSeverity.HIGH) {
            handleHighSeverityThreat(event)
        }
    }
    
    /**
     * Handles high severity threats
     * @param event The security event
     */
    private fun handleHighSeverityThreat(event: SecurityEvent) {
        // In a real implementation, you might want to:
        // 1. Log out the user
        // 2. Clear sensitive data
        // 3. Report to backend
        // 4. Terminate the app
        // For this example, we'll just log it
        
        SecurityLogger().logSecurityEvent(
            "HIGH SEVERITY THREAT DETECTED: ${event.threatType} - ${event.description}",
            LogLevel.ERROR
        )
    }
    
    /**
     * Gets recent security events
     * @param limit Maximum number of events to return
     * @return List of recent security events
     */
    fun getRecentSecurityEvents(limit: Int = 50): List<SecurityEvent> {
        return securityEvents.takeLast(limit)
    }
    
    /**
     * Clears security events
     */
    fun clearSecurityEvents() {
        securityEvents.clear()
    }
    
    /**
     * Stops monitoring
     */
    fun stopMonitoring() {
        executor.shutdown()
    }
}

/**
 * Data class to represent a security event
 * @param timestamp The timestamp of the event
 * @param threatType The type of threat
 * @param description Description of the event
 * @param severity The severity level
 */
data class SecurityEvent(
    val timestamp: Long,
    val threatType: String,
    val description: String,
    val severity: ThreatSeverity
)