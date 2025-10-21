package com.ekehi.mobile.di

import android.content.Context
import androidx.room.Room
import com.ekehi.mobile.MainApplication
import com.ekehi.mobile.data.local.CacheManager
import com.ekehi.mobile.data.local.EkehiDatabase
import com.ekehi.mobile.data.local.dao.MiningSessionDao
import com.ekehi.mobile.data.local.dao.SocialTaskDao
import com.ekehi.mobile.data.local.dao.UserProfileDao
import com.ekehi.mobile.data.repository.AuthRepository
import com.ekehi.mobile.data.repository.UserRepository
import com.ekehi.mobile.data.repository.MiningRepository
import com.ekehi.mobile.data.repository.SocialTaskRepository
import com.ekehi.mobile.data.repository.LeaderboardRepository
import com.ekehi.mobile.data.repository.offline.OfflineUserRepository
import com.ekehi.mobile.data.repository.offline.OfflineMiningRepository
import com.ekehi.mobile.data.repository.offline.OfflineSocialTaskRepository
import com.ekehi.mobile.data.sync.SyncManager
import com.ekehi.mobile.domain.usecase.AuthUseCase
import com.ekehi.mobile.domain.usecase.UserUseCase
import com.ekehi.mobile.domain.usecase.MiningUseCase
import com.ekehi.mobile.domain.usecase.SocialTaskUseCase
import com.ekehi.mobile.domain.usecase.LeaderboardUseCase
import com.ekehi.mobile.domain.usecase.offline.OfflineUserUseCase
import com.ekehi.mobile.domain.usecase.offline.OfflineMiningUseCase
import com.ekehi.mobile.domain.usecase.offline.OfflineSocialTaskUseCase
import com.ekehi.mobile.network.service.AppwriteService
import com.ekehi.mobile.network.service.NotificationService
import com.ekehi.mobile.network.service.PushNotificationService
import com.ekehi.mobile.analytics.AnalyticsService
import com.ekehi.mobile.analytics.AnalyticsManager
import com.ekehi.mobile.performance.PerformanceMonitor

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
    fun provideAppwriteClient(): Client {
        return Client()
                .setEndpoint("https://fra.cloud.appwrite.io/v1")
                .setProject("68c2dd6e002112935ed2")
    }

    @Provides
    @Singleton
    fun provideAppwriteService(client: Client): AppwriteService {
        return AppwriteService(client)
    }

    // REMOVED: provideOAuthService - OAuthService has @Inject constructor, so Hilt handles it automatically

    @Provides
    @Singleton
    fun provideNotificationService(@ApplicationContext context: Context): NotificationService {
        return NotificationService(context)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(appwriteService: AppwriteService): AuthRepository {
        return AuthRepository(appwriteService)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
            appwriteService: AppwriteService,
            performanceMonitor: PerformanceMonitor,
            userProfileDao: UserProfileDao,
            cacheManager: CacheManager
    ): UserRepository {
        return OfflineUserRepository(appwriteService, performanceMonitor, userProfileDao, cacheManager)
    }

    @Provides
    @Singleton
    fun provideMiningRepository(
            appwriteService: AppwriteService,
            performanceMonitor: PerformanceMonitor,
            miningSessionDao: MiningSessionDao,
            cacheManager: CacheManager
    ): MiningRepository {
        return OfflineMiningRepository(appwriteService, performanceMonitor, miningSessionDao, cacheManager)
    }

    @Provides
    @Singleton
    fun provideSocialTaskRepository(
            appwriteService: AppwriteService,
            performanceMonitor: PerformanceMonitor,
            socialTaskDao: SocialTaskDao,
            cacheManager: CacheManager
    ): SocialTaskRepository {
        return OfflineSocialTaskRepository(appwriteService, performanceMonitor, socialTaskDao, cacheManager)
    }

    @Provides
    @Singleton
    fun provideLeaderboardRepository(appwriteService: AppwriteService): LeaderboardRepository {
        return LeaderboardRepository(appwriteService)
    }

    @Provides
    @Singleton
    fun provideAuthUseCase(authRepository: AuthRepository): AuthUseCase {
        return AuthUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideUserUseCase(userRepository: UserRepository): UserUseCase {
        return OfflineUserUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideMiningUseCase(miningRepository: MiningRepository): MiningUseCase {
        return OfflineMiningUseCase(miningRepository)
    }

    @Provides
    @Singleton
    fun provideSocialTaskUseCase(socialTaskRepository: SocialTaskRepository): SocialTaskUseCase {
        return OfflineSocialTaskUseCase(socialTaskRepository)
    }

    @Provides
    @Singleton
    fun provideLeaderboardUseCase(leaderboardRepository: LeaderboardRepository): LeaderboardUseCase {
        return LeaderboardUseCase(leaderboardRepository)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): EkehiDatabase {
        return Room.databaseBuilder(
                context,
                EkehiDatabase::class.java,
                EkehiDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideUserProfileDao(database: EkehiDatabase): UserProfileDao {
        return database.userProfileDao()
    }

    @Provides
    @Singleton
    fun provideMiningSessionDao(database: EkehiDatabase): MiningSessionDao {
        return database.miningSessionDao()
    }

    @Provides
    @Singleton
    fun provideSocialTaskDao(database: EkehiDatabase): SocialTaskDao {
        return database.socialTaskDao()
    }

    @Provides
    @Singleton
    fun provideCacheManager(@ApplicationContext context: Context): CacheManager {
        return CacheManager(context)
    }

    @Provides
    @Singleton
    fun provideSyncManager(
            @ApplicationContext context: Context,
            userProfileDao: UserProfileDao,
            miningSessionDao: MiningSessionDao,
            socialTaskDao: SocialTaskDao,
            userRepository: UserRepository,
            miningRepository: MiningRepository,
            socialTaskRepository: SocialTaskRepository
    ): SyncManager {
        return SyncManager(
                context,
                userProfileDao,
                miningSessionDao,
                socialTaskDao,
                userRepository,
                miningRepository,
                socialTaskRepository
        )
    }

    @Provides
    @Singleton
    fun providePushNotificationService(@ApplicationContext context: Context): PushNotificationService {
        return PushNotificationService(context)
    }

    @Provides
    @Singleton
    fun provideAnalyticsService(@ApplicationContext context: Context): AnalyticsService {
        return AnalyticsService(context)
    }

    @Provides
    @Singleton
    fun provideAnalyticsManager(analyticsService: AnalyticsService): AnalyticsManager {
        return AnalyticsManager(analyticsService)
    }

    @Provides
    @Singleton
    fun providePerformanceMonitor(): PerformanceMonitor {
        return PerformanceMonitor()
    }
}