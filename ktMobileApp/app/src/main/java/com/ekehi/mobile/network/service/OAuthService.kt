package com.ekehi.mobile.network.service

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import io.appwrite.Client
import io.appwrite.models.OAuthProvider
import io.appwrite.services.Account
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OAuthService @Inject constructor(
        private val client: Client,
        private val context: Context
) {
    private val account: Account = Account(client)

    companion object {
        const val SUCCESS_URL = "ekehi://oauth/return"
        const val FAILURE_URL = "ekehi://auth"
    }

    // Make this suspend and return properly
    suspend fun initiateGoogleOAuth(activity: Activity): Boolean {
        return try {
            account.createOAuth2Token(
                    provider = OAuthProvider.GOOGLE, // Use enum
                    success = SUCCESS_URL,
                    failure = FAILURE_URL,
                    activity = activity // Add activity parameter
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    fun openOAuthInCustomTab(oauthUrl: String) {
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(context, Uri.parse(oauthUrl))
    }
}