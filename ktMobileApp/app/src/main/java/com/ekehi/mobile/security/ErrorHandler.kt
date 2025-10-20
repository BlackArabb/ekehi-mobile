package com.ekehi.mobile.security

import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter

/**
 * ErrorHandler handles secure error handling and logging.
 * Implements OWASP secure coding practices for error handling and logging.
 */
object ErrorHandler {
    private const val TAG = "EkehiErrorHandler"
    
    /**
     * Handles an exception securely
     * @param exception The exception to handle
     * @param userMessage User-friendly message to display
     * @param logLevel The log level to use
     */
    fun handleException(
        exception: Exception,
        userMessage: String = "An error occurred",
        logLevel: LogLevel = LogLevel.ERROR
    ): ErrorResult {
        // Log the exception securely (without sensitive data)
        val stackTrace = getStackTrace(exception)
        val sanitizedMessage = sanitizeErrorMessage(exception.message ?: "Unknown error")
        
        // Log the error
        when (logLevel) {
            LogLevel.DEBUG -> Log.d(TAG, "$userMessage: $sanitizedMessage\n$stackTrace")
            LogLevel.INFO -> Log.i(TAG, "$userMessage: $sanitizedMessage")
            LogLevel.WARN -> Log.w(TAG, "$userMessage: $sanitizedMessage")
            LogLevel.ERROR -> Log.e(TAG, "$userMessage: $sanitizedMessage\n$stackTrace")
        }
        
        // Log to security logger as well
        SecurityLogger().logSecurityEvent(
            "Exception handled: $userMessage - $sanitizedMessage",
            logLevel
        )
        
        return ErrorResult(false, userMessage, sanitizedMessage)
    }
    
    /**
     * Handles a security exception
     * @param exception The security exception to handle
     * @param userMessage User-friendly message to display
     */
    fun handleSecurityException(
        exception: SecurityException,
        userMessage: String = "A security error occurred"
    ): ErrorResult {
        // Log the security exception
        val stackTrace = getStackTrace(exception)
        val sanitizedMessage = sanitizeErrorMessage(exception.message ?: "Unknown security error")
        
        Log.e(TAG, "$userMessage: $sanitizedMessage\n$stackTrace")
        
        // Log to security logger with high severity
        SecurityLogger().logSecurityThreat(
            "Security Exception",
            "$userMessage - $sanitizedMessage",
            ThreatSeverity.HIGH
        )
        
        return ErrorResult(false, userMessage, sanitizedMessage)
    }
    
    /**
     * Handles a network exception
     * @param exception The network exception to handle
     * @param userMessage User-friendly message to display
     */
    fun handleNetworkException(
        exception: Exception,
        userMessage: String = "A network error occurred"
    ): ErrorResult {
        // Log the network exception
        val sanitizedMessage = sanitizeErrorMessage(exception.message ?: "Unknown network error")
        
        Log.w(TAG, "$userMessage: $sanitizedMessage")
        
        // Log to security logger as medium threat (could indicate tampering)
        SecurityLogger().logSecurityThreat(
            "Network Exception",
            "$userMessage - $sanitizedMessage",
            ThreatSeverity.MEDIUM
        )
        
        return ErrorResult(false, userMessage, sanitizedMessage)
    }
    
    /**
     * Gets the stack trace as a string
     * @param throwable The throwable to get stack trace for
     * @return Stack trace as string
     */
    private fun getStackTrace(throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        return sw.toString()
    }
    
    /**
     * Sanitizes error messages to remove sensitive information
     * @param message The message to sanitize
     * @return Sanitized message
     */
    private fun sanitizeErrorMessage(message: String): String {
        // Remove potential sensitive data like passwords, tokens, etc.
        var sanitized = message
        
        // Remove passwords
        sanitized = sanitized.replace(Regex("password=\\S*"), "password=[REDACTED]")
        
        // Remove tokens
        sanitized = sanitized.replace(Regex("token=\\S*"), "token=[REDACTED]")
        
        // Remove email addresses
        sanitized = sanitized.replace(Regex("\\S+@\\S+\\.\\S+"), "[EMAIL REDACTED]")
        
        // Remove phone numbers
        sanitized = sanitized.replace(Regex("\\d{3}-\\d{3}-\\d{4}"), "[PHONE REDACTED]")
        
        // Limit length to prevent log overflow
        if (sanitized.length > 1000) {
            sanitized = sanitized.substring(0, 1000) + "... [TRUNCATED]"
        }
        
        return sanitized
    }
    
    /**
     * Logs a security event
     * @param event The security event to log
     * @param level The log level
     */
    fun logSecurityEvent(event: String, level: LogLevel = LogLevel.INFO) {
        SecurityLogger().logSecurityEvent(event, level)
    }
    
    /**
     * Logs an authentication event
     * @param userId The user ID
     * @param action The authentication action
     * @param success Whether the action was successful
     */
    fun logAuthEvent(userId: String, action: String, success: Boolean) {
        SecurityLogger().logAuthEvent(userId, action, success)
    }
    
    /**
     * Logs a data access event
     * @param userId The user ID
     * @param dataType The type of data accessed
     * @param action The action performed
     */
    fun logDataAccessEvent(userId: String, dataType: String, action: String) {
        SecurityLogger().logDataAccessEvent(userId, dataType, action)
    }
}

/**
 * Data class to hold error handling results
 * @param success Whether the operation was successful
 * @param userMessage User-friendly message
 * @param technicalMessage Technical message (sanitized)
 */
data class ErrorResult(
    val success: Boolean,
    val userMessage: String,
    val technicalMessage: String
)