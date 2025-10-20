package com.ekehi.mobile.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64

/**
 * CryptoManager handles encryption and decryption operations using Android Keystore system.
 * Implements OWASP secure coding practices for cryptographic controls.
 */
class CryptoManager {
    private val KEY_ALIAS = "ekehi_crypto_key"
    private val ANDROID_KEYSTORE = "AndroidKeyStore"
    private val TRANSFORMATION = "AES/GCM/NoPadding"
    private val GCM_IV_LENGTH = 12
    private val GCM_TAG_LENGTH = 128
    
    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }
    
    init {
        createKeysIfNecessary()
    }
    
    /**
     * Encrypts the given plaintext
     * @param plainText The text to encrypt
     * @return Base64 encoded encrypted string
     */
    fun encrypt(plainText: String): String {
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            
            val iv = cipher.iv
            val encryption = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            
            // Combine IV and encrypted data
            val encryptedData = ByteArray(iv.size + encryption.size)
            System.arraycopy(iv, 0, encryptedData, 0, iv.size)
            System.arraycopy(encryption, 0, encryptedData, iv.size, encryption.size)
            
            return Base64.encodeToString(encryptedData, Base64.DEFAULT)
        } catch (e: Exception) {
            throw SecurityException("Encryption failed", e)
        }
    }
    
    /**
     * Decrypts the given encrypted text
     * @param encryptedText Base64 encoded encrypted string
     * @return Decrypted plaintext
     */
    fun decrypt(encryptedText: String): String {
        try {
            val encryptedData = Base64.decode(encryptedText, Base64.DEFAULT)
            
            // Extract IV and encrypted data
            val iv = ByteArray(GCM_IV_LENGTH)
            val encryption = ByteArray(encryptedData.size - GCM_IV_LENGTH)
            System.arraycopy(encryptedData, 0, iv, 0, iv.size)
            System.arraycopy(encryptedData, iv.size, encryption, 0, encryption.size)
            
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
            
            val decrypted = cipher.doFinal(encryption)
            return String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            throw SecurityException("Decryption failed", e)
        }
    }
    
    /**
     * Creates encryption keys if they don't exist
     */
    private fun createKeysIfNecessary() {
        try {
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, 
                    ANDROID_KEYSTORE
                )
                
                val keyGenSpec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                 .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                 .setKeySize(256)
                 .build()
                 
                keyGenerator.init(keyGenSpec)
                keyGenerator.generateKey()
            }
        } catch (e: Exception) {
            throw SecurityException("Failed to create encryption keys", e)
        }
    }
    
    /**
     * Gets the secret key from the keystore
     * @return The secret key
     */
    private fun getSecretKey(): SecretKey {
        return keyStore.getKey(KEY_ALIAS, null) as SecretKey
    }
}