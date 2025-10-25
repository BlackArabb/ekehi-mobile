package com.ekehi.network.network.service

import android.content.Context
import io.appwrite.Client
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppwriteService @Inject constructor(
    private val client: Client,
    private val context: Context
) {
    val account: Account = Account(client)
    val databases: Databases = Databases(client)
    val storage: Storage = Storage(client)
    
    companion object {
        const val DATABASE_ID = "68c336e7000f87296feb"
        
        // Collection IDs - matching React Native app
        const val USERS_COLLECTION = "users"
        const val USER_PROFILES_COLLECTION = "user_profiles"
        const val MINING_SESSIONS_COLLECTION = "mining_sessions"
        const val SOCIAL_TASKS_COLLECTION = "social_tasks"
        const val USER_SOCIAL_TASKS_COLLECTION = "user_social_tasks"
        const val ACHIEVEMENTS_COLLECTION = "achievements"
        const val USER_ACHIEVEMENTS_COLLECTION = "user_achievements"
        const val PRESALE_PURCHASES_COLLECTION = "presale_purchases"
        const val AD_VIEWS_COLLECTION = "ad_views"
    }
}