package com.ekehi.network.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import java.io.File
import java.util.*

/**
 * MaliciousCodeProtection detects and prevents malicious code injection.
 * Implements OWASP secure coding practices for malicious code protection.
 */
class MaliciousCodeProtection(private val context: Context) {
    
    private val TAG = "MaliciousCodeProtection"
    
    /**
     * Checks for potentially malicious code or apps
     * @return MaliciousCodeCheckResult containing check results
     */
    fun checkForMaliciousCode(): MaliciousCodeCheckResult {
        val threats = mutableListOf<ThreatInfo>()
        
        // Check for known malicious packages
        val maliciousPackages = checkForMaliciousPackages()
        threats.addAll(maliciousPackages)
        
        // Check for suspicious permissions
        val suspiciousPermissions = checkForSuspiciousPermissions()
        threats.addAll(suspiciousPermissions)
        
        // Check for runtime modifications
        val runtimeModifications = checkForRuntimeModifications()
        threats.addAll(runtimeModifications)
        
        // Check for hooking frameworks
        val hookingFrameworks = checkForHookingFrameworks()
        threats.addAll(hookingFrameworks)
        
        return if (threats.isEmpty()) {
            MaliciousCodeCheckResult(true, "No malicious code detected", emptyList())
        } else {
            MaliciousCodeCheckResult(false, "Malicious code threats detected", threats)
        }
    }
    
    /**
     * Checks for known malicious packages
     * @return List of threat information
     */
    private fun checkForMaliciousPackages(): List<ThreatInfo> {
        val threats = mutableListOf<ThreatInfo>()
        val maliciousPackageNames = getKnownMaliciousPackages()
        
        try {
            val installedPackages = context.packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            
            for (packageInfo in installedPackages) {
                if (maliciousPackageNames.contains(packageInfo.packageName)) {
                    threats.add(ThreatInfo(
                        "Malicious Package",
                        "Detected malicious package: ${packageInfo.packageName}",
                        ThreatSeverity.HIGH
                    ))
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to check for malicious packages", e)
        }
        
        return threats
    }
    
    /**
     * Gets list of known malicious packages
     * @return List of malicious package names
     */
    private fun getKnownMaliciousPackages(): Set<String> {
        // This is a simplified list - in a real implementation, this would be more comprehensive
        return setOf(
            "com.example.maliciousapp",
            "org.hacking.tool",
            "net.spyware.agent"
        )
    }
    
    /**
     * Checks for suspicious permissions
     * @return List of threat information
     */
    private fun checkForSuspiciousPermissions(): List<ThreatInfo> {
        val threats = mutableListOf<ThreatInfo>()
        
        try {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_PERMISSIONS
            )
            
            val requestedPermissions = packageInfo.requestedPermissions ?: emptyArray()
            
            // Check for overly broad permissions
            for (permission in requestedPermissions) {
                if (isSuspiciousPermission(permission)) {
                    threats.add(ThreatInfo(
                        "Suspicious Permission",
                        "App requests suspicious permission: $permission",
                        ThreatSeverity.MEDIUM
                    ))
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to check for suspicious permissions", e)
        }
        
        return threats
    }
    
    /**
     * Checks if a permission is suspicious
     * @param permission The permission to check
     * @return true if suspicious, false otherwise
     */
    private fun isSuspiciousPermission(permission: String): Boolean {
        // List of permissions that might be suspicious if not needed by the app
        val suspiciousPermissions = setOf(
            "android.permission.SYSTEM_ALERT_WINDOW",
            "android.permission.WRITE_SETTINGS",
            "android.permission.PACKAGE_USAGE_STATS"
        )
        
        return suspiciousPermissions.contains(permission)
    }
    
    /**
     * Checks for runtime modifications
     * @return List of threat information
     */
    private fun checkForRuntimeModifications(): List<ThreatInfo> {
        val threats = mutableListOf<ThreatInfo>()
        
        // Check if running in emulator (can be used for malicious purposes)
        if (isEmulator()) {
            threats.add(ThreatInfo(
                "Emulator Detection",
                "App running in emulator (potential for malicious testing)",
                ThreatSeverity.MEDIUM
            ))
        }
        
        // Check if debuggable
        if (isDebuggable()) {
            threats.add(ThreatInfo(
                "Debuggable App",
                "App is debuggable (potential for runtime modification)",
                ThreatSeverity.HIGH
            ))
        }
        
        // Check for root access
        if (isRooted()) {
            threats.add(ThreatInfo(
                "Root Access",
                "Device is rooted (potential for malicious code injection)",
                ThreatSeverity.HIGH
            ))
        }
        
        return threats
    }
    
    /**
     * Checks for hooking frameworks
     * @return List of threat information
     */
    private fun checkForHookingFrameworks(): List<ThreatInfo> {
        val threats = mutableListOf<ThreatInfo>()
        
        // Check for common hooking framework files
        val hookingFiles = arrayOf(
            "/system/lib/libxposed.so",
            "/system/lib/libsubstrate.so",
            "/system/lib/libfrida.so",
            "/data/local/tmp/frida-agent.so"
        )
        
        for (filePath in hookingFiles) {
            if (File(filePath).exists()) {
                threats.add(ThreatInfo(
                    "Hooking Framework",
                    "Detected hooking framework file: $filePath",
                    ThreatSeverity.HIGH
                ))
            }
        }
        
        return threats
    }
    
    /**
     * Checks if device is rooted
     * @return true if rooted, false otherwise
     */
    private fun isRooted(): Boolean {
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
                return true
            }
        }
        
        return false
    }
    
    /**
     * Checks if running in emulator
     * @return true if emulator, false otherwise
     */
    private fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic") ||
                "google_sdk" == Build.PRODUCT)
    }
    
    /**
     * Checks if app is debuggable
     * @return true if debuggable, false otherwise
     */
    private fun isDebuggable(): Boolean {
        return context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    }
    
    /**
     * Prevents code injection by validating input
     * @param input The input to validate
     * @return true if safe, false otherwise
     */
    fun preventCodeInjection(input: String): Boolean {
        // Check for common code injection patterns
        val injectionPatterns = arrayOf(
            "javascript:",
            "data:",
            "vbscript:",
            "onload",
            "onerror",
            "onclick",
            "eval\\(",
            "exec\\(",
            "system\\("
        )
        
        for (pattern in injectionPatterns) {
            if (input.contains(Regex(pattern, RegexOption.IGNORE_CASE))) {
                SecurityLogger().logSecurityThreat(
                    "Code Injection Attempt",
                    "Potential code injection detected in input: $input",
                    ThreatSeverity.HIGH
                )
                return false
            }
        }
        
        return true
    }
    
    /**
     * Sanitizes input to prevent code injection
     * @param input The input to sanitize
     * @return Sanitized input
     */
    fun sanitizeInput(input: String): String {
        // Remove potentially dangerous characters and patterns
        var sanitized = input
        
        // Remove script tags
        sanitized = sanitized.replace(Regex("<script[^>]*>.*?</script>", RegexOption.IGNORE_CASE), "")
        
        // Remove event handlers
        sanitized = sanitized.replace(Regex("on\\w+\\s*=", RegexOption.IGNORE_CASE), "")
        
        // Remove javascript URLs
        sanitized = sanitized.replace(Regex("javascript:", RegexOption.IGNORE_CASE), "")
        
        // Remove data URLs
        sanitized = sanitized.replace(Regex("data:", RegexOption.IGNORE_CASE), "")
        
        return sanitized
    }
}

/**
 * Data class to hold malicious code check results
 * @param isSafe Whether the app is safe from malicious code
 * @param message Message describing the check result
 * @param threats List of threats found (if any)
 */
data class MaliciousCodeCheckResult(
    val isSafe: Boolean,
    val message: String,
    val threats: List<ThreatInfo>
)

/**
 * Data class to hold threat information
 * @param threatType The type of threat
 * @param description Description of the threat
 * @param severity The severity level
 */
data class ThreatInfo(
    val threatType: String,
    val description: String,
    val severity: ThreatSeverity
)