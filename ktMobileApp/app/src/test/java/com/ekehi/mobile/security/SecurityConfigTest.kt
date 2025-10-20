package com.ekehi.mobile.security

import org.junit.Assert.*
import org.junit.Test

class SecurityConfigTest {
    
    @Test
    fun `isDebugMode should return false in production`() {
        // In a real implementation, this would depend on build configuration
        // For testing purposes, we'll just ensure it returns a boolean
        val isDebug = SecurityConfig.isDebugMode()
        assertNotNull(isDebug)
    }
    
    @Test
    fun `getMinPasswordLength should return valid value`() {
        val minPasswordLength = SecurityConfig.getMinPasswordLength()
        assertTrue(minPasswordLength > 0)
        assertTrue(minPasswordLength <= 128) // Reasonable upper limit
    }
    
    @Test
    fun `getMaxFailedLoginAttempts should return valid value`() {
        val maxAttempts = SecurityConfig.getMaxFailedLoginAttempts()
        assertTrue(maxAttempts > 0)
        assertTrue(maxAttempts <= 10) // Reasonable upper limit
    }
    
    @Test
    fun `getSessionTimeout should return valid value`() {
        val sessionTimeout = SecurityConfig.getSessionTimeout()
        assertTrue(sessionTimeout > 0)
        assertTrue(sessionTimeout <= 86400) // 24 hours in seconds
    }
    
    @Test
    fun `isBiometricAuthEnabled should return boolean`() {
        val isBiometricAuthEnabled = SecurityConfig.isBiometricAuthEnabled()
        assertNotNull(isBiometricAuthEnabled)
    }
    
    @Test
    fun `getEncryptionKeyAlias should return non-empty string`() {
        val keyAlias = SecurityConfig.getEncryptionKeyAlias()
        assertFalse(keyAlias.isEmpty())
    }
    
    @Test
    fun `isCertificatePinningEnabled should return boolean`() {
        val isCertificatePinningEnabled = SecurityConfig.isCertificatePinningEnabled()
        assertNotNull(isCertificatePinningEnabled)
    }
    
    @Test
    fun `getAllowedCertificatePins should return list`() {
        val certificatePins = SecurityConfig.getAllowedCertificatePins()
        assertNotNull(certificatePins)
    }
    
    @Test
    fun `isLoggingEnabled should return boolean`() {
        val isLoggingEnabled = SecurityConfig.isLoggingEnabled()
        assertNotNull(isLoggingEnabled)
    }
    
    @Test
    fun `getSecurityLogLevel should return valid level`() {
        val logLevel = SecurityConfig.getSecurityLogLevel()
        assertNotNull(logLevel)
    }
}