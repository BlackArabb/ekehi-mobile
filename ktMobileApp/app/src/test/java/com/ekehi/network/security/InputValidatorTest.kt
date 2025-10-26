package com.ekehi.network.security

import org.junit.Assert.*
import org.junit.Test

class InputValidatorTest {
    
    @Test
    fun `isValidEmail should return true for valid emails`() {
        assertTrue(InputValidator.isValidEmail("test@example.com"))
        assertTrue(InputValidator.isValidEmail("user.name@domain.co.uk"))
        assertTrue(InputValidator.isValidEmail("test123@test-domain.org"))
    }
    
    @Test
    fun `isValidEmail should return false for invalid emails`() {
        assertFalse(InputValidator.isValidEmail("invalid.email"))
        assertFalse(InputValidator.isValidEmail("@example.com"))
        assertFalse(InputValidator.isValidEmail("test@"))
        assertFalse(InputValidator.isValidEmail(""))
    }
    
    @Test
    fun `isValidUsername should return true for valid usernames`() {
        assertTrue(InputValidator.isValidUsername("testuser"))
        assertTrue(InputValidator.isValidUsername("test_user"))
        assertTrue(InputValidator.isValidUsername("test-user"))
        assertTrue(InputValidator.isValidUsername("user123"))
    }
    
    @Test
    fun `isValidUsername should return false for invalid usernames`() {
        assertFalse(InputValidator.isValidUsername("ab")) // Too short
        assertFalse(InputValidator.isValidUsername("a".repeat(21))) // Too long
        assertFalse(InputValidator.isValidUsername("test user")) // Contains space
        assertFalse(InputValidator.isValidUsername("test@user")) // Contains special character
    }
    
    @Test
    fun `isValidPassword should return true for valid passwords`() {
        assertTrue(InputValidator.isValidPassword("Password123"))
        assertTrue(InputValidator.isValidPassword("MySecurePass1"))
        assertTrue(InputValidator.isValidPassword("TestPass123!"))
    }
    
    @Test
    fun `isValidPassword should return false for invalid passwords`() {
        assertFalse(InputValidator.isValidPassword("password")) // No uppercase
        assertFalse(InputValidator.isValidPassword("PASSWORD")) // No lowercase
        assertFalse(InputValidator.isValidPassword("Password")) // No digit
        assertFalse(InputValidator.isValidPassword("Pass1")) // Too short
    }
    
    @Test
    fun `isValidName should return true for valid names`() {
        assertTrue(InputValidator.isValidName("John Doe"))
        assertTrue(InputValidator.isValidName("Mary-Jane"))
        assertTrue(InputValidator.isValidName("O'Connor"))
    }
    
    @Test
    fun `isValidName should return false for invalid names`() {
        assertFalse(InputValidator.isValidName("John123")) // Contains digits
        assertFalse(InputValidator.isValidName("John@Doe")) // Contains special chars
        assertFalse(InputValidator.isValidName("")) // Empty
    }
    
    @Test
    fun `sanitizeInput should remove HTML tags`() {
        assertEquals("Hello World", InputValidator.sanitizeInput("<script>alert('test')</script>Hello World"))
        assertEquals("Hello World", InputValidator.sanitizeInput("<div>Hello World</div>"))
    }
    
    @Test
    fun `sanitizeInput should limit length`() {
        val longInput = "a".repeat(1500)
        val sanitized = InputValidator.sanitizeInput(longInput)
        assertTrue(sanitized.length <= 1000)
    }
    
    @Test
    fun `validateAndSanitizeText should validate length`() {
        val result1 = InputValidator.validateAndSanitizeText("test", 10)
        assertTrue(result1.isValid)
        assertEquals("test", result1.sanitizedInput)
        
        val longText = "a".repeat(300)
        val result2 = InputValidator.validateAndSanitizeText(longText, 255)
        assertFalse(result2.isValid)
    }
    
    @Test
    fun `validateNumericInput should validate numeric values`() {
        val result1 = InputValidator.validateNumericInput("123.45")
        assertTrue(result1.isValid)
        
        val result2 = InputValidator.validateNumericInput("abc")
        assertFalse(result2.isValid)
        
        val result3 = InputValidator.validateNumericInput("100", 0.0, 50.0)
        assertFalse(result3.isValid)
    }
}