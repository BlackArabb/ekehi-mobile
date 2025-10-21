package com.ekehi.mobile.network.service

import android.content.Context
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.browser.customtabs.CustomTabsIntent
import io.appwrite.Client
import io.appwrite.services.Account
import io.appwrite.enums.OAuthProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OAuthService @Inject constructor(
        private val context: Context,  // Context first
        private val client: Client     // Client second
) {
    private val account: Account = Account(client)

    companion object {
        const val SUCCESS_URL = "ekehi://oauth/return"
        const val FAILURE_URL = "ekehi://auth"
    }

    suspend fun initiateGoogleOAuth(activity: ComponentActivity): Boolean {
        return try {
            account.createOAuth2Token(
                    provider = OAuthProvider.GOOGLE,
                    success = SUCCESS_URL,
                    failure = FAILURE_URL,
                    activity = activity
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun openOAuthInCustomTab(oauthUrl: String) {
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(context, Uri.parse(oauthUrl))
    }
}