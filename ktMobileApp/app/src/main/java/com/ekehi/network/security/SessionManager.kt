package com.ekehi.network.security

import android.content.Context
import java.security.SecureRandom
import java.util.*

/**
 * SessionManager handles secure session management.
 * Implements OWASP secure coding practices for session management.
 */
class SessionManager(private val context: Context, private val securePreferences: SecurePreferences) {

    private val SESSION_ID_KEY = "session_id"
    private val SESSION_EXPIRY_KEY = "session_expiry"
    private val SESSION_TIMEOUT = 30 * 60 * 1000L // 30 minutes

    /**
     * Creates a new secure session
     * @return The session ID
     */
    fun createSession(): String {
        val sessionId = generateSecureSessionId()
        val expiryTime = System.currentTimeMillis() + SESSION_TIMEOUT

        securePreferences.putString(SESSION_ID_KEY, sessionId)
        securePreferences.putLong(SESSION_EXPIRY_KEY, expiryTime)

        SecurityLogger().logSecurityEvent("Session created: $sessionId", LogLevel.INFO, true)

        return sessionId
    }

    /**
     * Validates if the current session is valid
     * @return true if session is valid, false otherwise
     */
    fun isSessionValid(): Boolean {
        val sessionId = securePreferences.getString(SESSION_ID_KEY, null)
        val expiryTime = securePreferences.getLong(SESSION_EXPIRY_KEY, 0L)

        return sessionId != null && System.currentTimeMillis() < expiryTime
    }

    /**
     * Gets the current session ID
     * @return The session ID or null if no valid session
     */
    fun getSessionId(): String? {
        return if (isSessionValid()) {
            securePreferences.getString(SESSION_ID_KEY, null)
        } else {
            null
        }
    }

    /**
     * Extends the current session
     */
    fun extendSession() {
        if (isSessionValid()) {
            val expiryTime = System.currentTimeMillis() + SESSION_TIMEOUT
            securePreferences.putLong(SESSION_EXPIRY_KEY, expiryTime)
            SecurityLogger().logSecurityEvent("Session extended", LogLevel.INFO)
        }
    }

    /**
     * Invalidates the current session
     */
    fun invalidateSession() {
        val sessionId = getSessionId()
        securePreferences.remove(SESSION_ID_KEY)
        securePreferences.remove(SESSION_EXPIRY_KEY)

        if (sessionId != null) {
            SecurityLogger().logSecurityEvent("Session invalidated: $sessionId", LogLevel.INFO, true)
        }
    }

    /**
     * Checks if the session has expired
     * @return true if session has expired, false otherwise
     */
    fun isSessionExpired(): Boolean {
        val expiryTime = securePreferences.getLong(SESSION_EXPIRY_KEY, 0L)
        return expiryTime > 0 && System.currentTimeMillis() >= expiryTime
    }

    /**
     * Generates a cryptographically secure session ID
     * @return Secure session ID
     */
    private fun generateSecureSessionId(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)

        // Convert to hex string
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02x", b))
        }

        return sb.toString()
    }

    /**
     * Regenerates session ID to prevent session fixation attacks
     * @return New session ID
     */
    fun regenerateSessionId(): String {
        val oldSessionId = getSessionId()
        val newSessionId = generateSecureSessionId()
        val expiryTime = securePreferences.getLong(SESSION_EXPIRY_KEY, 0L)

        securePreferences.putString(SESSION_ID_KEY, newSessionId)

        if (oldSessionId != null) {
            SecurityLogger().logSecurityEvent("Session ID regenerated from: $oldSessionId to: $newSessionId", LogLevel.INFO, true)
        }

        return newSessionId
    }
}