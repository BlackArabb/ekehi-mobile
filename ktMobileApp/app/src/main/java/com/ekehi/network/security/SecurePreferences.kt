package com.ekehi.network.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SecurePreferences handles secure storage of sensitive data using EncryptedSharedPreferences.
 * Implements OWASP secure coding practices for data storage.
 */
@Singleton
class SecurePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey: MasterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedSharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "ekehi_secure_preferences",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    private val editor: SharedPreferences.Editor = encryptedSharedPreferences.edit()
    
    /**
     * Stores a string value securely
     * @param key The key to store the value under
     * @param value The value to store
     */
    fun putString(key: String, value: String?) {
        editor.putString(key, value).apply()
    }
    
    /**
     * Retrieves a string value securely
     * @param key The key to retrieve the value for
     * @param defaultValue The default value if key doesn't exist
     * @return The stored value or default value
     */
    fun getString(key: String, defaultValue: String?): String? {
        return encryptedSharedPreferences.getString(key, defaultValue)
    }
    
    /**
     * Stores an integer value securely
     * @param key The key to store the value under
     * @param value The value to store
     */
    fun putInt(key: String, value: Int) {
        editor.putInt(key, value).apply()
    }
    
    /**
     * Retrieves an integer value securely
     * @param key The key to retrieve the value for
     * @param defaultValue The default value if key doesn't exist
     * @return The stored value or default value
     */
    fun getInt(key: String, defaultValue: Int): Int {
        return encryptedSharedPreferences.getInt(key, defaultValue)
    }
    
    /**
     * Stores a boolean value securely
     * @param key The key to store the value under
     * @param value The value to store
     */
    fun putBoolean(key: String, value: Boolean) {
        editor.putBoolean(key, value).apply()
    }
    
    /**
     * Retrieves a boolean value securely
     * @param key The key to retrieve the value for
     * @param defaultValue The default value if key doesn't exist
     * @return The stored value or default value
     */
    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return encryptedSharedPreferences.getBoolean(key, defaultValue)
    }
    
    /**
     * Stores a long value securely
     * @param key The key to store the value under
     * @param value The value to store
     */
    fun putLong(key: String, value: Long) {
        editor.putLong(key, value).apply()
    }
    
    /**
     * Retrieves a long value securely
     * @param key The key to retrieve the value for
     * @param defaultValue The default value if key doesn't exist
     * @return The stored value or default value
     */
    fun getLong(key: String, defaultValue: Long): Long {
        return encryptedSharedPreferences.getLong(key, defaultValue)
    }
    
    /**
     * Removes a value securely
     * @param key The key to remove
     */
    fun remove(key: String) {
        editor.remove(key).apply()
    }
    
    /**
     * Clears all stored values
     */
    fun clear() {
        editor.clear().apply()
    }
    
    /**
     * Checks if a key exists
     * @param key The key to check
     * @return true if key exists, false otherwise
     */
    fun contains(key: String): Boolean {
        return encryptedSharedPreferences.contains(key)
    }
}