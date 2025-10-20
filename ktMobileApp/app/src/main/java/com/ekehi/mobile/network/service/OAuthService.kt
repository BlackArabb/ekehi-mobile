package com.ekehi.mobile.network.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import io.appwrite.Client
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
        const val GOOGLE_PROVIDER = "google"
        const val SUCCESS_URL = "ekehi://oauth/return"
        const val FAILURE_URL = "ekehi://auth"
    }
    
    fun initiateGoogleOAuth(): String {
        return account.createOAuth2Token(
            provider = GOOGLE_PROVIDER,
            success = SUCCESS_URL,
            failure = FAILURE_URL
        )
    }
    
    fun openOAuthInCustomTab(oauthUrl: String) {
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(context, Uri.parse(oauthUrl))
    }
}