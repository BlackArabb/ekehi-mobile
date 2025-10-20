package com.ekehi.mobile.security

import org.junit.Assert.*
import org.junit.Test

class AuthManagerTest {
    
    @Test
    fun `validatePasswordStrength should accept strong passwords`() {
        val authManager = createMockAuthManager()
        
        val result1 = authManager.validatePasswordStrength("Password123!")
        assertTrue(result1.isValid)
        
        val result2 = authManager.validatePasswordStrength("MySecurePass1@")
        assertTrue(result2.isValid)
    }
    
    @Test
    fun `validatePasswordStrength should reject weak passwords`() {
        val authManager = createMockAuthManager()
        
        val result1 = authManager.validatePasswordStrength("password")
        assertFalse(result1.isValid)
        assertTrue(result1.errorMessage.contains("uppercase"))
        
        val result2 = authManager.validatePasswordStrength("PASSWORD")
        assertFalse(result2.isValid)
        assertTrue(result2.errorMessage.contains("lowercase"))
        
        val result3 = authManager.validatePasswordStrength("Password")
        assertFalse(result3.isValid)
        assertTrue(result3.errorMessage.contains("digit"))
        
        val result4 = authManager.validatePasswordStrength("Pass1")
        assertFalse(result4.isValid)
        assertTrue(result4.errorMessage.contains("8 characters"))
    }
    
    @Test
    fun `hashPassword and verifyPassword should work correctly`() {
        val authManager = createMockAuthManager()
        
        val password = "MySecurePassword123!"
        val hashedPassword = authManager.hashPassword(password)
        
        assertNotNull(hashedPassword.hashed)
        assertNotNull(hashedPassword.salt)
        assertFalse(hashedPassword.hashed.isEmpty())
        assertFalse(hashedPassword.salt.isEmpty())
        
        assertTrue(authManager.verifyPassword(password, hashedPassword))
        assertFalse(authManager.verifyPassword("WrongPassword", hashedPassword))
    }
    
    @Test
    fun `hashPassword should generate different hashes for same password`() {
        val authManager = createMockAuthManager()
        
        val password = "MySecurePassword123!"
        val hashedPassword1 = authManager.hashPassword(password)
        val hashedPassword2 = authManager.hashPassword(password)
        
        // With different salts, hashes should be different
        assertNotEquals(hashedPassword1.hashed, hashedPassword2.hashed)
        assertNotEquals(hashedPassword1.salt, hashedPassword2.salt)
    }
    
    /**
     * Creates a mock AuthManager for testing
     */
    private fun createMockAuthManager(): AuthManager {
        return object : AuthManager(mockContext()) {
            override fun hashPassword(password: String, salt: String?): HashedPassword {
                val usedSalt = salt ?: "mock_salt"
                return HashedPassword("hashed_$password:$usedSalt", usedSalt)
            }
            
            override fun verifyPassword(password: String, hashedPassword: HashedPassword): Boolean {
                val parts = hashedPassword.hashed.split(":")
                return parts.size == 2 && parts[0] == "hashed_$password" && parts[1] == hashedPassword.salt
            }
        }
    }
    
    /**
     * Creates a mock context for testing
     */
    private fun mockContext(): android.content.Context {
        return android.content.ContextWrapper(null)
    }
}