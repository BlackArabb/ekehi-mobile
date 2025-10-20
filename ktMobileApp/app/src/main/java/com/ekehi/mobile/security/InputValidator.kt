package com.ekehi.mobile.security

import android.util.Patterns
import java.util.regex.Pattern

/**
 * Centralized input validation utility for the Ekehi Mobile app.
 * Implements OWASP secure coding practices for input validation.
 */
object InputValidator {
    
    // Email validation pattern
    private val EMAIL_PATTERN = Patterns.EMAIL_ADDRESS
    
    // Username validation pattern (alphanumeric, underscore, hyphen, 3-20 characters)
    private val USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,20}$")
    
    // Password validation pattern (at least 8 characters, one uppercase, one lowercase, one digit)
    private val PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,}$")
    
    // Name validation pattern (letters, spaces, apostrophes, hyphens, 1-50 characters)
    private val NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s'-]{1,50}$")
    
    /**
     * Validates an email address
     * @param email The email to validate
     * @return true if valid, false otherwise
     */
    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && EMAIL_PATTERN.matcher(email).matches()
    }
    
    /**
     * Validates a username
     * @param username The username to validate
     * @return true if valid, false otherwise
     */
    fun isValidUsername(username: String): Boolean {
        return username.isNotEmpty() && USERNAME_PATTERN.matcher(username).matches()
    }
    
    /**
     * Validates a password
     * @param password The password to validate
     * @return true if valid, false otherwise
     */
    fun isValidPassword(password: String): Boolean {
        return password.isNotEmpty() && PASSWORD_PATTERN.matcher(password).matches()
    }
    
    /**
     * Validates a name
     * @param name The name to validate
     * @return true if valid, false otherwise
     */
    fun isValidName(name: String): Boolean {
        return name.isNotEmpty() && NAME_PATTERN.matcher(name).matches()
    }
    
    /**
     * Validates a string length
     * @param input The string to validate
     * @param minLength Minimum length (inclusive)
     * @param maxLength Maximum length (inclusive)
     * @return true if valid, false otherwise
     */
    fun isValidLength(input: String, minLength: Int, maxLength: Int): Boolean {
        return input.length in minLength..maxLength
    }
    
    /**
     * Sanitizes input by removing potentially dangerous characters
     * @param input The input to sanitize
     * @return Sanitized string
     */
    fun sanitizeInput(input: String): String {
        // Remove HTML tags
        var sanitized = input.replace(Regex("<[^>]*>"), "")
        
        // Remove SQL injection patterns
        sanitized = sanitized.replace(Regex("[';\\-\\-\\\\]"), "")
        
        // Limit length to prevent buffer overflow
        if (sanitized.length > 1000) {
            sanitized = sanitized.substring(0, 1000)
        }
        
        return sanitized
    }
    
    /**
     * Validates and sanitizes input for general text fields
     * @param input The input to validate and sanitize
     * @param maxLength Maximum allowed length
     * @return ValidationResult containing validation result and sanitized input
     */
    fun validateAndSanitizeText(input: String, maxLength: Int = 255): ValidationResult {
        // Check for null or empty input
        if (input.isEmpty()) {
            return ValidationResult(false, "", "Input cannot be empty")
        }
        
        // Check length
        if (input.length > maxLength) {
            return ValidationResult(false, "", "Input exceeds maximum length of $maxLength characters")
        }
        
        // Sanitize input
        val sanitized = sanitizeInput(input)
        
        return ValidationResult(true, sanitized, "")
    }
    
    /**
     * Validates numeric input
     * @param input The input to validate
     * @param minValue Minimum allowed value (inclusive)
     * @param maxValue Maximum allowed value (inclusive)
     * @return ValidationResult containing validation result
     */
    fun validateNumericInput(input: String, minValue: Double = Double.NEGATIVE_INFINITY, 
                           maxValue: Double = Double.POSITIVE_INFINITY): ValidationResult {
        return try {
            val value = input.toDouble()
            if (value >= minValue && value <= maxValue) {
                ValidationResult(true, input, "")
            } else {
                ValidationResult(false, "", "Value must be between $minValue and $maxValue")
            }
        } catch (e: NumberFormatException) {
            ValidationResult(false, "", "Invalid numeric input")
        }
    }
}

/**
 * Data class to hold validation results
 * @param isValid Whether the validation passed
 * @param sanitizedInput The sanitized input (if applicable)
 * @param errorMessage Error message if validation failed
 */
data class ValidationResult(
    val isValid: Boolean,
    val sanitizedInput: String,
    val errorMessage: String
)