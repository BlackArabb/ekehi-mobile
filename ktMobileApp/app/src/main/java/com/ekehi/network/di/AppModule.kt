package com.ekehi.network.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ekehi.network.analytics.AnalyticsManager
import com.ekehi.network.data.local.EkehiDatabase
import com.ekehi.network.data.local.CacheManager
import com.ekehi.network.data.local.dao.MiningSessionDao
import com.ekehi.network.data.local.dao.SocialTaskDao
import com.ekehi.network.data.local.dao.UserProfileDao
import com.ekehi.network.data.repository.AuthRepository
import com.ekehi.network.data.repository.MiningRepository
import com.ekehi.network.data.repository.SocialTaskRepository
import com.ekehi.network.data.repository.UserRepository
import com.ekehi.network.data.repository.offline.OfflineSocialTaskRepository
import com.ekehi.network.domain.usecase.AuthUseCase
import com.ekehi.network.domain.usecase.MiningUseCase
import com.ekehi.network.domain.usecase.SocialTaskUseCase
import com.ekehi.network.domain.usecase.UserUseCase
import com.ekehi.network.domain.verification.SocialVerificationService
import com.ekehi.network.performance.PerformanceMonitor
import com.ekehi.network.presentation.viewmodel.LoginViewModel
import com.ekehi.network.presentation.viewmodel.SettingsViewModel
import com.ekehi.network.presentation.viewmodel.StreakViewModel
import com.ekehi.network.security.SecurePreferences
import com.ekehi.network.service.*
import com.ekehi.network.data.sync.SyncManager
import com.ekehi.network.data.sync.SyncService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.appwrite.Client
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
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
        )
        .addMigrations(
            object : androidx.room.migration.Migration(1, 2) {
                override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                    // Add taskReward and miningReward columns to user_profiles table
                    database.execSQL("ALTER TABLE user_profiles ADD COLUMN taskReward REAL NOT NULL DEFAULT 0.0")
                    database.execSQL("ALTER TABLE user_profiles ADD COLUMN miningReward REAL NOT NULL DEFAULT 0.0")
                }
            },
            object : androidx.room.migration.Migration(2, 3) {
                override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                    // Create a new table without referralBonusRate column
                    database.execSQL("""
                        CREATE TABLE user_profiles_new (
                            id TEXT PRIMARY KEY NOT NULL,
                            userId TEXT NOT NULL,
                            username TEXT,
                            email TEXT,
                            phone_number TEXT NOT NULL DEFAULT '',
                            country TEXT NOT NULL DEFAULT '',
                            taskReward REAL NOT NULL DEFAULT 0.0,
                            miningReward REAL NOT NULL DEFAULT 0.0,
                            autoMiningRate REAL NOT NULL DEFAULT 0.0,
                            miningPower REAL NOT NULL DEFAULT 0.0,
                            currentStreak INTEGER NOT NULL DEFAULT 0,
                            longestStreak INTEGER NOT NULL DEFAULT 0,
                            lastLoginDate TEXT,
                            referralCode TEXT,
                            referredBy TEXT,
                            totalReferrals INTEGER NOT NULL DEFAULT 0,
                            lifetimeEarnings REAL NOT NULL DEFAULT 0.0,
                            dailyMiningRate REAL NOT NULL DEFAULT 0.0,
                            maxDailyEarnings REAL NOT NULL DEFAULT 0.0,
                            todayEarnings REAL NOT NULL DEFAULT 0.0,
                            lastMiningDate TEXT,
                            streakBonusClaimed INTEGER NOT NULL DEFAULT 0,
                            createdAt TEXT NOT NULL,
                            updatedAt TEXT NOT NULL
                        )
                    """)
                    
                    // Copy data from old table to new table
                    database.execSQL("""
                        INSERT INTO user_profiles_new (
                            id, userId, username, email, phone_number, country,
                            taskReward, miningReward, autoMiningRate, miningPower,
                            currentStreak, longestStreak, lastLoginDate, referralCode,
                            referredBy, totalReferrals, lifetimeEarnings, dailyMiningRate,
                            maxDailyEarnings, todayEarnings, lastMiningDate, streakBonusClaimed,
                            createdAt, updatedAt
                        )
                        SELECT
                            id, userId, username, email, phone_number, country,
                            taskReward, miningReward, autoMiningRate, miningPower,
                            currentStreak, longestStreak, lastLoginDate, referralCode,
                            referredBy, totalReferrals, lifetimeEarnings, dailyMiningRate,
                            maxDailyEarnings, todayEarnings, lastMiningDate, streakBonusClaimed,
                            createdAt, updatedAt
                        FROM user_profiles
                    """)
                    
                    // Drop the old table
                    database.execSQL("DROP TABLE user_profiles")
                    
                    // Rename the new table
                    database.execSQL("ALTER TABLE user_profiles_new RENAME TO user_profiles")
                }
            },
            object : androidx.room.migration.Migration(3, 4) {
                override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                    // Add referralReward column to user_profiles table
                    database.execSQL("ALTER TABLE user_profiles ADD COLUMN referralReward REAL NOT NULL DEFAULT 0.0")
                }
            }
        )
        .build()
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
                .setEndpoint("https://fra.cloud.appwrite.io/v1") // Frankfurt region endpoint to match React Native app
                .setProject("68c2dd6e002112935ed2") // Actual project ID
    }

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }

    @Provides
    @Singleton
    fun provideTelegramBotService(httpClient: HttpClient): TelegramBotService {
        return TelegramBotService(httpClient)
    }

    @Provides
    @Singleton
    fun provideYouTubeApiService(httpClient: HttpClient): YouTubeApiService {
        return YouTubeApiService(httpClient)
    }

    @Provides
    @Singleton
    fun provideFacebookApiService(httpClient: HttpClient): FacebookApiService {
        return FacebookApiService(httpClient)
    }

    @Provides
    @Singleton
    fun provideSocialVerificationService(
        telegramBotService: TelegramBotService,
        youTubeApiService: YouTubeApiService,
        facebookApiService: FacebookApiService
    ): SocialVerificationService {
        return SocialVerificationService(telegramBotService, youTubeApiService, facebookApiService)
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
            client: Client,
            userRepository: UserRepository
    ): OAuthService {
        return OAuthService(context, client, userRepository)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        appwriteService: AppwriteService, 
        securePreferences: SecurePreferences, 
        userRepository: UserRepository,
        @ApplicationContext context: Context,
        miningManager: MiningManager
    ): AuthRepository {
        return AuthRepository(appwriteService, securePreferences, userRepository, context, miningManager)
    }

    @Provides
    @Singleton
    fun provideUserRepository(appwriteService: AppwriteService, performanceMonitor: PerformanceMonitor, securePreferences: SecurePreferences): UserRepository {
        return UserRepository(appwriteService, performanceMonitor, securePreferences)
    }

    @Provides
    @Singleton
    fun provideMiningRepository(
            appwriteService: AppwriteService,
            performanceMonitor: PerformanceMonitor,
            @ApplicationContext context: Context
    ): MiningRepository {
        return MiningRepository(appwriteService, performanceMonitor, context)
    }

    @Provides
    @Singleton
    fun provideSocialTaskRepository(
        appwriteService: AppwriteService, 
        performanceMonitor: PerformanceMonitor,
        socialVerificationService: SocialVerificationService
    ): SocialTaskRepository {
        return SocialTaskRepository(appwriteService, performanceMonitor, socialVerificationService)
    }

    @Provides
    @Singleton
    fun provideOfflineSocialTaskRepository(
        appwriteService: AppwriteService,
        performanceMonitor: PerformanceMonitor,
        socialVerificationService: SocialVerificationService,
        socialTaskDao: SocialTaskDao,
        cacheManager: CacheManager
    ): OfflineSocialTaskRepository {
        return OfflineSocialTaskRepository(appwriteService, performanceMonitor, socialVerificationService, socialTaskDao, cacheManager)
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

    // SecurePreferences is provided in SecurityModule
    // @Provides
    // @Singleton
    // fun provideSecurePreferences(@ApplicationContext context: Context): SecurePreferences {
    //     return SecurePreferences(context)
    // }

    @Provides
    @Singleton
    fun provideMiningManager(@ApplicationContext context: Context): MiningManager {
        return MiningManager(context)
    }

    @Provides
    @Singleton
    fun provideNotificationService(@ApplicationContext context: Context, securePreferences: SecurePreferences): NotificationService {
        return NotificationService(context, securePreferences)
    }
    
    @Provides
    @Singleton
    fun providePushNotificationService(@ApplicationContext context: Context, securePreferences: SecurePreferences): PushNotificationService {
        return PushNotificationService(context, securePreferences)
    }

    @Provides
    @Singleton
    fun provideStreakViewModel(userUseCase: UserUseCase): StreakViewModel {
        return StreakViewModel(userUseCase)
    }

    @Provides
    @Singleton
    fun provideSettingsViewModel(authUseCase: AuthUseCase, securePreferences: SecurePreferences): SettingsViewModel {
        return SettingsViewModel(authUseCase, securePreferences)
    }
    
    @Provides
    @Singleton
    fun provideVersionCheckService(
        @ApplicationContext context: Context,
        client: Client
    ): VersionCheckService {
        return VersionCheckService(context, client)
    }

    @Provides
    @Singleton
    fun provideApkDownloadManager(
        @ApplicationContext context: Context
    ): ApkDownloadManager {
        return ApkDownloadManager(context)
    }

    @Provides
    @Singleton
    fun provideLoginViewModel(
        authUseCase: AuthUseCase,
        userUseCase: UserUseCase,
        analyticsManager: AnalyticsManager,
        performanceMonitor: PerformanceMonitor
    ): LoginViewModel {
        return LoginViewModel(authUseCase, userUseCase, analyticsManager, performanceMonitor)
    }

}