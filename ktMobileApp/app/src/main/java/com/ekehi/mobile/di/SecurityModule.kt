package com.ekehi.mobile.di

import android.content.Context
import com.ekehi.mobile.security.CryptoManager
import com.ekehi.mobile.security.SecurePreferences
import com.ekehi.mobile.security.SecurityLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {
    
    @Provides
    @Singleton
    fun provideCryptoManager(): CryptoManager {
        return CryptoManager()
    }
    
    @Provides
    @Singleton
    fun provideSecurePreferences(@ApplicationContext context: Context): SecurePreferences {
        return SecurePreferences(context)
    }
    
    @Provides
    @Singleton
    fun provideSecurityLogger(): SecurityLogger {
        return SecurityLogger()
    }
}