package com.ekehi.network.security

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * SecurityLogger handles security-related logging with appropriate sanitization.
 * Implements OWASP secure coding practices for logging.
 */
class SecurityLogger {
    private val TAG = "EkehiSecurity"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    /**
     * Logs a security event
     * @param event The security event description
     * @param level The log level (DEBUG, INFO, WARN, ERROR)
     * @param sensitiveData Whether the event contains sensitive data (will be redacted)
     */
    fun logSecurityEvent(event: String, level: LogLevel = LogLevel.INFO, sensitiveData: Boolean = false) {
        val timestamp = dateFormat.format(Date())
        val message = if (sensitiveData) {
            "[$timestamp] SECURITY EVENT: [SENSITIVE DATA REDACTED]"
        } else {
            "[$timestamp] SECURITY EVENT: $event"
        }
        
        when (level) {
            LogLevel.DEBUG -> Log.d(TAG, message)
            LogLevel.INFO -> Log.i(TAG, message)
            LogLevel.WARN -> Log.w(TAG, message)
            LogLevel.ERROR -> Log.e(TAG, message)
        }
    }
    
    /**
     * Logs an authentication event
     * @param userId The user ID (will be partially redacted)
     * @param action The authentication action (login, logout, failed attempt, etc.)
     * @param success Whether the action was successful
     */
    fun logAuthEvent(userId: String, action: String, success: Boolean) {
        // Redact user ID for privacy
        val redactedUserId = if (userId.length > 4) {
            "*".repeat(userId.length - 4) + userId.takeLast(4)
        } else {
            "*".repeat(userId.length)
        }
        
        val status = if (success) "SUCCESS" else "FAILED"
        val message = "AUTH $status - User: $redactedUserId, Action: $action"
        logSecurityEvent(message, if (success) LogLevel.INFO else LogLevel.WARN)
    }
    
    /**
     * Logs a data access event
     * @param userId The user ID (will be partially redacted)
     * @param dataType The type of data accessed
     * @param action The action performed (read, write, delete, etc.)
     */
    fun logDataAccessEvent(userId: String, dataType: String, action: String) {
        // Redact user ID for privacy
        val redactedUserId = if (userId.length > 4) {
            "*".repeat(userId.length - 4) + userId.takeLast(4)
        } else {
            "*".repeat(userId.length)
        }
        
        val message = "DATA ACCESS - User: $redactedUserId, Type: $dataType, Action: $action"
        logSecurityEvent(message, LogLevel.INFO)
    }
    
    /**
     * Logs a potential security threat
     * @param threatType The type of threat detected
     * @param description Description of the threat
     * @param severity The severity level of the threat
     */
    fun logSecurityThreat(threatType: String, description: String, severity: ThreatSeverity) {
        val message = "SECURITY THREAT - Type: $threatType, Severity: $severity, Description: $description"
        val level = when (severity) {
            ThreatSeverity.LOW -> LogLevel.INFO
            ThreatSeverity.MEDIUM -> LogLevel.WARN
            ThreatSeverity.HIGH -> LogLevel.ERROR
        }
        logSecurityEvent(message, level)
    }
}

enum class LogLevel {
    DEBUG, INFO, WARN, ERROR
}

enum class ThreatSeverity {
    LOW, MEDIUM, HIGH
}