package com.funtime.blog.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

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
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """CREATE TABLE IF NOT EXISTS `passport_stamps` (
                        `id` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `displayName` TEXT NOT NULL,
                        `earnedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )"""
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE `user_stats` ADD COLUMN `lastReadingXpDate` TEXT")
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """CREATE TABLE IF NOT EXISTS `reading_history` (
                        `slug` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `coverUrl` TEXT,
                        `readAt` INTEGER NOT NULL,
                        PRIMARY KEY(`slug`)
                    )"""
                )
                connection.execSQL(
                    """CREATE TABLE IF NOT EXISTS `checkins` (
                        `dateKey` TEXT NOT NULL,
                        `streakDay` INTEGER NOT NULL,
                        `xpEarned` INTEGER NOT NULL,
                        `checkinAt` INTEGER NOT NULL,
                        PRIMARY KEY(`dateKey`)
                    )"""
                )
                connection.execSQL(
                    """CREATE TABLE IF NOT EXISTS `user_stats` (
                        `id` INTEGER NOT NULL,
                        `totalXp` INTEGER NOT NULL,
                        `level` INTEGER NOT NULL,
                        `currentStreak` INTEGER NOT NULL,
                        `longestStreak` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )"""
                )
            }
        }
    }
}
