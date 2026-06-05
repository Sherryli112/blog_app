package com.funtime.blog.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        BookmarkedArticle::class,
        ReadingHistoryEntity::class,
        CheckinEntity::class,
        UserStatsEntity::class,
        PassportStampEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun readingHistoryDao(): ReadingHistoryDao
    abstract fun checkinDao(): CheckinDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun passportStampDao(): PassportStampDao

    companion object {
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS `passport_stamps` (
                        `id` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `displayName` TEXT NOT NULL,
                        `earnedAt` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`id`)
                    )"""
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `user_stats` ADD COLUMN `lastReadingXpDate` TEXT")
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS `reading_history` (
                        `slug` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `coverUrl` TEXT,
                        `readAt` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`slug`)
                    )"""
                )
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS `checkins` (
                        `dateKey` TEXT NOT NULL,
                        `streakDay` INTEGER NOT NULL,
                        `xpEarned` INTEGER NOT NULL,
                        `checkinAt` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`dateKey`)
                    )"""
                )
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS `user_stats` (
                        `id` INTEGER NOT NULL,
                        `totalXp` INTEGER NOT NULL DEFAULT 0,
                        `level` INTEGER NOT NULL DEFAULT 1,
                        `currentStreak` INTEGER NOT NULL DEFAULT 0,
                        `longestStreak` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`id`)
                    )"""
                )
            }
        }
    }
}
