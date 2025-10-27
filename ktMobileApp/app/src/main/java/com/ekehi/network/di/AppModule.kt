package com.ekehi.network.di

import android.content.Context
import androidx.room.Room
import com.ekehi.network.data.local.EkehiDatabase
import com.ekehi.network.data.local.CacheManager
import com.ekehi.network.data.local.dao.MiningSessionDao
import com.ekehi.network.data.local.dao.SocialTaskDao
import com.ekehi.network.data.local.dao.UserProfileDao
import com.ekehi.network.data.repository.*
import com.ekehi.network.data.sync.SyncManager
import com.ekehi.network.data.sync.SyncService
import com.ekehi.network.domain.usecase.*
import com.ekehi.network.performance.PerformanceMonitor
import com.ekehi.network.security.SecurePreferences
import com.ekehi.network.service.AppwriteService
import com.ekehi.network.service.OAuthService
import com.ekehi.network.service.StartIoService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.appwrite.Client
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): EkehiDatabase {
        return Room.databaseBuilder(
            context,
            EkehiDatabase::class.java,
            "ekehi_database"
        ).build()
    }

    @Provides
    fun provideUserProfileDao(database: EkehiDatabase): UserProfileDao {
        return database.userProfileDao()
    }

    @Provides
    fun provideMiningSessionDao(database: EkehiDatabase): MiningSessionDao {
        return database.miningSessionDao()
    }

    @Provides
    fun provideSocialTaskDao(database: EkehiDatabase): SocialTaskDao {
        return database.socialTaskDao()
    }

    @Provides
    @Singleton
    fun provideAppwriteClient(@ApplicationContext context: Context): Client {
        return Client(context)
            .setEndpoint("https://cloud.appwrite.io/v1")
            .setProject("68c2dd6e002112935ed2") // Actual project ID
    }

    @Provides
    @Singleton
    fun providePerformanceMonitor(): PerformanceMonitor {
        return PerformanceMonitor()
    }

    @Provides
    @Singleton
    fun provideAppwriteService(client: Client, @ApplicationContext context: Context): AppwriteService {
        return AppwriteService(client, context)
    }

    @Provides
    @Singleton
    fun provideOAuthService(
        @ApplicationContext context: Context,
        client: Client
    ): OAuthService {
        return OAuthService(context, client)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(appwriteService: AppwriteService): AuthRepository {
        return AuthRepository(appwriteService)
    }

    @Provides
    @Singleton
    fun provideUserRepository(appwriteService: AppwriteService, performanceMonitor: PerformanceMonitor): UserRepository {
        return UserRepository(appwriteService, performanceMonitor)
    }

    @Provides
    @Singleton
    fun provideMiningRepository(appwriteService: AppwriteService, performanceMonitor: PerformanceMonitor): MiningRepository {
        return MiningRepository(appwriteService, performanceMonitor)
    }

    @Provides
    @Singleton
    fun provideSocialTaskRepository(appwriteService: AppwriteService, performanceMonitor: PerformanceMonitor): SocialTaskRepository {
        return SocialTaskRepository(appwriteService, performanceMonitor)
    }

    @Provides
    @Singleton
    fun provideCacheManager(@ApplicationContext context: Context): CacheManager {
        return CacheManager(context)
    }

    @Provides
    @Singleton
    fun provideAuthUseCase(authRepository: AuthRepository): AuthUseCase {
        return AuthUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideUserUseCase(userRepository: UserRepository): UserUseCase {
        return UserUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideMiningUseCase(miningRepository: MiningRepository): MiningUseCase {
        return MiningUseCase(miningRepository)
    }

    @Provides
    @Singleton
    fun provideSocialTaskUseCase(socialTaskRepository: SocialTaskRepository): SocialTaskUseCase {
        return SocialTaskUseCase(socialTaskRepository)
    }

    @Provides
    @Singleton
    fun provideSyncService(
        @ApplicationContext context: Context,
        userRepository: UserRepository,
        miningRepository: MiningRepository,
        socialTaskRepository: SocialTaskRepository,
        userProfileDao: UserProfileDao,
        miningSessionDao: MiningSessionDao,
        socialTaskDao: SocialTaskDao,
        cacheManager: CacheManager,
        performanceMonitor: PerformanceMonitor
    ): SyncService {
        return SyncService(
            context,
            userRepository,
            miningRepository,
            socialTaskRepository,
            userProfileDao,
            miningSessionDao,
            socialTaskDao,
            cacheManager,
            performanceMonitor
        )
    }

    @Provides
    @Singleton
    fun provideSyncManager(
        userRepository: UserRepository,
        miningRepository: MiningRepository,
        socialTaskRepository: SocialTaskRepository,
        userProfileDao: UserProfileDao,
        miningSessionDao: MiningSessionDao,
        socialTaskDao: SocialTaskDao,
        cacheManager: CacheManager,
        performanceMonitor: PerformanceMonitor
    ): SyncManager {
        return SyncManager(
            userRepository,
            miningRepository,
            socialTaskRepository,
            userProfileDao,
            miningSessionDao,
            socialTaskDao,
            cacheManager,
            performanceMonitor
        )
    }

    @Provides
    @Singleton
    fun provideStartIoService(@ApplicationContext context: Context): StartIoService {
        return StartIoService(context)
    }
    
    @Provides
    @Singleton
    fun provideSecurePreferences(@ApplicationContext context: Context): SecurePreferences {
        return SecurePreferences(context)
    }
}