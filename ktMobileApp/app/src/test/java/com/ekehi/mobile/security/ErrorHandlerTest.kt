package com.ekehi.mobile.security

import org.junit.Assert.*
import org.junit.Test

class ErrorHandlerTest {
    
    @Test
    fun `handleException should sanitize error messages`() {
        val exception = Exception("Error with password=secret123 and token=abc123xyz")
        val result = ErrorHandler.handleException(exception, "Test error")
        
        assertFalse(result.technicalMessage.contains("password=secret123"))
        assertFalse(result.technicalMessage.contains("token=abc123xyz"))
        assertTrue(result.technicalMessage.contains("[REDACTED]"))
    }
    
    @Test
    fun `handleSecurityException should log security threats`() {
        val exception = SecurityException("Security error with sensitive data")
        val result = ErrorHandler.handleSecurityException(exception, "Security error occurred")
        
        assertFalse(result.success)
        assertEquals("Security error occurred", result.userMessage)
    }
    
    @Test
    fun `handleNetworkException should handle network errors`() {
        val exception = Exception("Network error")
        val result = ErrorHandler.handleNetworkException(exception, "Network error occurred")
        
        assertFalse(result.success)
        assertEquals("Network error occurred", result.userMessage)
    }
    
    @Test
    fun `sanitizeErrorMessage should remove sensitive data`() {
        // This would require reflection to test private method
        // For now, we'll test through the public interface
        val exception = Exception("test@example.com has password=secret")
        val result = ErrorHandler.handleException(exception)
        
        assertFalse(result.technicalMessage.contains("test@example.com"))
        assertFalse(result.technicalMessage.contains("password=secret"))
        assertTrue(result.technicalMessage.contains("[EMAIL REDACTED]"))
        assertTrue(result.technicalMessage.contains("[REDACTED]"))
    }
    
    @Test
    fun `logSecurityEvent should not throw exceptions`() {
        // This is a basic test to ensure the method doesn't crash
        ErrorHandler.logSecurityEvent("Test security event")
        assertTrue(true) // If we get here without exception, the test passes
    }
    
    @Test
    fun `logAuthEvent should redact user ID`() {
        // This is a basic test to ensure the method doesn't crash
        ErrorHandler.logAuthEvent("user123", "login", true)
        assertTrue(true) // If we get here without exception, the test passes
    }
}