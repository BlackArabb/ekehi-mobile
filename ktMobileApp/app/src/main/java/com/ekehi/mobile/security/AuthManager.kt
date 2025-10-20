package com.ekehi.mobile.security

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor

/**
 * AuthManager handles secure authentication operations including biometric authentication.
 * Implements OWASP secure coding practices for authentication and password management.
 */
class AuthManager(private val context: Context) {
    
    private val biometricManager = BiometricManager.from(context)
    
    /**
     * Checks if biometric authentication is available
     * @return true if biometric authentication is available, false otherwise
     */
    fun isBiometricAuthAvailable(): Boolean {
        return when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }
    
    /**
     * Authenticates the user using biometrics
     * @param activity The activity to use for authentication
     * @param onSuccess Callback for successful authentication
     * @param onError Callback for authentication errors
     */
    fun authenticateWithBiometrics(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isBiometricAuthAvailable()) {
            onError("Biometric authentication is not available")
            return
        }
        
        val executor: Executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError("Authentication error: $errString")
                }
                
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }
                
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onError("Authentication failed")
                }
            })
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Use your biometric credential to login")
            .setNegativeButtonText("Cancel")
            .build()
        
        biometricPrompt.authenticate(promptInfo)
    }
    
    /**
     * Validates password strength according to security requirements
     * @param password The password to validate
     * @return ValidationResult containing validation result
     */
    fun validatePasswordStrength(password: String): ValidationResult {
        val errors = mutableListOf<String>()
        
        // Check minimum length
        if (password.length < 8) {
            errors.add("Password must be at least 8 characters long")
        }
        
        // Check for uppercase letter
        if (!password.any { it.isUpperCase() }) {
            errors.add("Password must contain at least one uppercase letter")
        }
        
        // Check for lowercase letter
        if (!password.any { it.isLowerCase() }) {
            errors.add("Password must contain at least one lowercase letter")
        }
        
        // Check for digit
        if (!password.any { it.isDigit() }) {
            errors.add("Password must contain at least one digit")
        }
        
        // Check for special character
        if (!password.any { it in "!@#$%^&*()_+-=[]{}|;:,.<>?" }) {
            errors.add("Password must contain at least one special character")
        }
        
        // Check for common passwords (simplified check)
        val commonPasswords = listOf("password", "12345678", "qwerty123")
        if (commonPasswords.any { password.lowercase().contains(it) }) {
            errors.add("Password is too common")
        }
        
        return if (errors.isEmpty()) {
            ValidationResult(true, password, "")
        } else {
            ValidationResult(false, "", errors.joinToString("; "))
        }
    }
    
    /**
     * Checks if the device is secured with a lock screen
     * @return true if device is secured, false otherwise
     */
    fun isDeviceSecured(): Boolean {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as android.app.KeyguardManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            keyguardManager.isDeviceSecure
        } else {
            @Suppress("DEPRECATION")
            keyguardManager.isKeyguardSecure
        }
    }
    
    /**
     * Hashes a password using a secure hashing algorithm
     * @param password The password to hash
     * @param salt The salt to use (if null, a random salt will be generated)
     * @return Hashed password with salt
     */
    fun hashPassword(password: String, salt: String? = null): HashedPassword {
        // In a real implementation, you would use a proper password hashing algorithm like bcrypt or scrypt
        // For this example, we'll use a simplified approach
        val usedSalt = salt ?: generateSalt()
        val hashed = CryptoManager().encrypt("$password:$usedSalt")
        return HashedPassword(hashed, usedSalt)
    }
    
    /**
     * Verifies a password against a hashed password
     * @param password The password to verify
     * @param hashedPassword The hashed password to compare against
     * @return true if password matches, false otherwise
     */
    fun verifyPassword(password: String, hashedPassword: HashedPassword): Boolean {
        return try {
            val decrypted = CryptoManager().decrypt(hashedPassword.hashed)
            val parts = decrypted.split(":")
            if (parts.size == 2) {
                val storedPassword = parts[0]
                val storedSalt = parts[1]
                storedPassword == password && storedSalt == hashedPassword.salt
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Generates a random salt
     * @return Random salt string
     */
    private fun generateSalt(length: Int = 32): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}

/**
 * Data class to hold hashed password and salt
 * @param hashed The hashed password
 * @param salt The salt used for hashing
 */
data class HashedPassword(
    val hashed: String,
    val salt: String
)