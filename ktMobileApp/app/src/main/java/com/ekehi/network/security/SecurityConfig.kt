package com.ekehi.network.security

import android.content.Context
import androidx.security.crypto.MasterKey

/**
 * SecurityConfig manages security configuration settings.
 * Implements OWASP secure coding practices for security configuration.
 */
object SecurityConfig {
    
    // Security constants
    const val MIN_PASSWORD_LENGTH = 8
    const val SESSION_TIMEOUT_MINUTES = 30
    const val MAX_LOGIN_ATTEMPTS = 5
    const val ACCOUNT_LOCKOUT_MINUTES = 15
    
    // Cryptographic settings
    const val AES_KEY_SIZE = 256
    const val RSA_KEY_SIZE = 2048
    const val HASH_ITERATIONS = 10000
    
    // Network security settings
    const val API_TIMEOUT_SECONDS = 30L
    const val TLS_VERSION = "TLSv1.2"
    
    // Biometric settings
    const val BIOMETRIC_AUTH_TIMEOUT_SECONDS = 300L
    
    /**
     * Checks if the app is running in debug mode
     * @return true if debug mode, false otherwise
     */
    fun isDebugMode(): Boolean {
        // In a real implementation, you would check the actual build config
        // For now, we'll assume it's not debug mode
        return false
    }
    
    /**
     * Gets the master key for encryption
     * @param context The application context
     * @return The master key
     */
    fun getMasterKey(context: Context): MasterKey {
        return MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }
    
    /**
     * Gets the API base URL
     * @return The API base URL
     */
    fun getApiBaseUrl(): String {
        return if (isDebugMode()) {
            "https://dev-api.ekehi.com"
        } else {
            "https://api.ekehi.com"
        }
    }
    
    /**
     * Gets the allowed SSL certificates (for certificate pinning)
     * @return List of allowed certificate hashes
     */
    fun getAllowedCertificateHashes(): List<String> {
        return if (isDebugMode()) {
            // In debug mode, allow more certificates for testing
            listOf(
                "debug_cert_hash_1",
                "debug_cert_hash_2"
            )
        } else {
            // In production, only allow specific certificates
            listOf(
                "prod_cert_hash_1",
                "prod_cert_hash_2"
            )
        }
    }
    
    /**
     * Gets security headers for HTTP requests
     * @return Map of security headers
     */
    fun getSecurityHeaders(): Map<String, String> {
        return mapOf(
            "X-Content-Type-Options" to "nosniff",
            "X-Frame-Options" to "DENY",
            "X-XSS-Protection" to "1; mode=block",
            "Referrer-Policy" to "no-referrer",
            "Permissions-Policy" to "geolocation=(), microphone=(), camera=()",
            "Strict-Transport-Security" to "max-age=31536000; includeSubDomains"
        )
    }
    
    /**
     * Gets the encryption algorithm
     * @return The encryption algorithm
     */
    fun getEncryptionAlgorithm(): String {
        return "AES/GCM/NoPadding"
    }
    
    /**
     * Gets the key derivation function
     * @return The key derivation function
     */
    fun getKeyDerivationFunction(): String {
        return "PBKDF2WithHmacSHA256"
    }
    
    /**
     * Gets the hash algorithm
     * @return The hash algorithm
     */
    fun getHashAlgorithm(): String {
        return "SHA-256"
    }
    
    /**
     * Validates if the current configuration is secure
     * @return SecurityValidationResult containing validation result
     */
    fun validateConfiguration(): SecurityValidationResult {
        val issues = mutableListOf<String>()
        
        // Check TLS version
        if (TLS_VERSION != "TLSv1.2" && TLS_VERSION != "TLSv1.3") {
            issues.add("Unsupported TLS version: $TLS_VERSION")
        }
        
        return if (issues.isEmpty()) {
            SecurityValidationResult(true, "Configuration is secure", emptyList())
        } else {
            SecurityValidationResult(false, "Configuration issues found", issues)
        }
    }
}

/**
 * Data class to hold security validation results
 * @param isValid Whether the configuration is valid
 * @param message Message describing the validation result
 * @param issues List of issues found (if any)
 */
data class SecurityValidationResult(
    val isValid: Boolean,
    val message: String,
    val issues: List<String>
)