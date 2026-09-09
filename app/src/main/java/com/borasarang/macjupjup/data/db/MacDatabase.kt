package com.borasarang.macjupjup.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.borasarang.macjupjup.data.db.dao.AppDao
import com.borasarang.macjupjup.data.db.dao.AppSourceMappingDao
import com.borasarang.macjupjup.data.db.dao.CrawlLogDao
import com.borasarang.macjupjup.data.db.dao.CrawlSourceDao
import com.borasarang.macjupjup.data.db.dao.NotificationLogDao
import com.borasarang.macjupjup.data.db.dao.VersionHistoryDao
import com.borasarang.macjupjup.data.db.entity.App
import com.borasarang.macjupjup.data.db.entity.AppSourceMapping
import com.borasarang.macjupjup.data.db.entity.CrawlLog
import com.borasarang.macjupjup.data.db.entity.CrawlSource
import com.borasarang.macjupjup.data.db.entity.NotificationLog
import com.borasarang.macjupjup.data.db.entity.VersionHistory

@Database(
    entities = [App::class, AppSourceMapping::class, CrawlSource::class, VersionHistory::class, CrawlLog::class, NotificationLog::class],
    version = 2,
    exportSchema = false,
)
abstract class MacDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    abstract fun appSourceMappingDao(): AppSourceMappingDao
    abstract fun crawlSourceDao(): CrawlSourceDao
    abstract fun versionHistoryDao(): VersionHistoryDao
    abstract fun crawlLogDao(): CrawlLogDao
    abstract fun notificationLogDao(): NotificationLogDao

    companion object {
        @Volatile
        private var instance: MacDatabase? = null

        fun getInstance(context: Context): MacDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MacDatabase::class.java,
                    "macjupjup.db",
                ).addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration(false).build().also { instance = it }
            }
        }

        /** 마이그레이션 실패 시 최후 수단 (데이터 손실 감수, 앱 벽돌 방지) */
        fun getInstanceFallback(context: Context): MacDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MacDatabase::class.java,
                    "macjupjup.db",
                ).fallbackToDestructiveMigration(true).build().also { instance = it }
            }
        }
    }
}
