package com.ekehi.mobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ekehi.mobile.data.local.dao.MiningSessionDao
import com.ekehi.mobile.data.local.dao.SocialTaskDao
import com.ekehi.mobile.data.local.dao.UserProfileDao
import com.ekehi.mobile.data.local.entities.MiningSessionEntity
import com.ekehi.mobile.data.local.entities.SocialTaskEntity
import com.ekehi.mobile.data.local.entities.UserProfileEntity

@Database(
    entities = [
        UserProfileEntity::class,
        MiningSessionEntity::class,
        SocialTaskEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class EkehiDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun miningSessionDao(): MiningSessionDao
    abstract fun socialTaskDao(): SocialTaskDao

    companion object {
        const val DATABASE_NAME = "ekehi_database"
    }
}