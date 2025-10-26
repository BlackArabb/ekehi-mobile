package com.ekehi.network.security

import org.junit.Assert.*
import org.junit.Test

class AccessControlManagerTest {
    
    @Test
    fun `hasRole should work correctly`() {
        val accessControlManager = AccessControlManager()
        
        assertTrue(accessControlManager.hasRole(UserRole.ADMIN, UserRole.USER))
        assertTrue(accessControlManager.hasRole(UserRole.ADMIN, UserRole.ADMIN))
        assertFalse(accessControlManager.hasRole(UserRole.USER, UserRole.ADMIN))
        assertTrue(accessControlManager.hasRole(UserRole.PREMIUM_USER, UserRole.USER))
    }
    
    @Test
    fun `hasPermission should work correctly`() {
        val accessControlManager = AccessControlManager()
        
        // All users can read
        assertTrue(accessControlManager.hasPermission("user1", "resource1", Permission.READ))
        assertTrue(accessControlManager.hasPermission("admin1", "resource1", Permission.READ))
        
        // Users can write to their own resources
        assertTrue(accessControlManager.hasPermission("user1", "user1", Permission.WRITE))
        assertFalse(accessControlManager.hasPermission("user1", "user2", Permission.WRITE))
        
        // Admins can write to any resource
        assertTrue(accessControlManager.hasPermission("admin1", "user1", Permission.WRITE))
        assertTrue(accessControlManager.hasPermission("admin1", "user2", Permission.WRITE))
        
        // Only admins can delete
        assertFalse(accessControlManager.hasPermission("user1", "user1", Permission.DELETE))
        assertTrue(accessControlManager.hasPermission("admin1", "user1", Permission.DELETE))
        
        // Only admins have admin permissions
        assertFalse(accessControlManager.hasPermission("user1", "user1", Permission.ADMIN))
        assertTrue(accessControlManager.hasPermission("admin1", "user1", Permission.ADMIN))
    }
    
    @Test
    fun `isResourceOwner should work correctly`() {
        val accessControlManager = AccessControlManager()
        
        assertTrue(accessControlManager.isResourceOwner("user1", "user1"))
        assertFalse(accessControlManager.isResourceOwner("user1", "user2"))
        assertFalse(accessControlManager.isResourceOwner("admin1", "user1"))
    }
    
    @Test
    fun `validateProfileAccess should work correctly`() {
        val accessControlManager = AccessControlManager()
        
        // Users can access their own profile
        val result1 = accessControlManager.validateProfileAccess("user1", "user1")
        assertTrue(result1.granted)
        assertTrue(result1.message.contains("own profile"))
        
        // Admins can access any profile
        val result2 = accessControlManager.validateProfileAccess("admin1", "user1")
        assertTrue(result2.granted)
        assertTrue(result2.message.contains("admin access"))
        
        // Regular users cannot access other profiles
        val result3 = accessControlManager.validateProfileAccess("user1", "user2")
        assertFalse(result3.granted)
        assertTrue(result3.message.contains("insufficient privileges"))
    }
    
    @Test
    fun `validateMiningDataAccess should work correctly`() {
        val accessControlManager = AccessControlManager()
        
        // Users can access their own mining data
        val result1 = accessControlManager.validateMiningDataAccess("user1", "user1")
        assertTrue(result1.granted)
        assertTrue(result1.message.contains("own mining data"))
        
        // Admins can access any mining data
        val result2 = accessControlManager.validateMiningDataAccess("admin1", "user1")
        assertTrue(result2.granted)
        assertTrue(result2.message.contains("admin access"))
        
        // Regular users cannot access other users' mining data
        val result3 = accessControlManager.validateMiningDataAccess("user1", "user2")
        assertFalse(result3.granted)
        assertTrue(result3.message.contains("cannot view"))
    }
    
    @Test
    fun `validateSocialTaskAccess should work correctly`() {
        val accessControlManager = AccessControlManager()
        
        // Users can access their own social tasks
        val result1 = accessControlManager.validateSocialTaskAccess("user1", "user1")
        assertTrue(result1.granted)
        assertTrue(result1.message.contains("own social tasks"))
        
        // Admins can access any social tasks
        val result2 = accessControlManager.validateSocialTaskAccess("admin1", "user1")
        assertTrue(result2.granted)
        assertTrue(result2.message.contains("admin access"))
        
        // Regular users cannot access other users' social tasks
        val result3 = accessControlManager.validateSocialTaskAccess("user1", "user2")
        assertFalse(result3.granted)
        assertTrue(result3.message.contains("cannot view"))
    }
}