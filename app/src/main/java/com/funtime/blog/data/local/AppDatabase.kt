package com.funtime.blog.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.execSQL

@Database(
    entities = [
        BookmarkedArticle::class,
        ReadingHistoryEntity::class,
        CheckinEntity::class,
        UserStatsEntity::class,
        PassportStampEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun readingHistoryDao(): ReadingHistoryDao
    abstract fun checkinDao(): CheckinDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun passportStampDao(): PassportStampDao

    companion object {

        // 一次修復所有因舊 migration bug 損壞的表格
        val MIGRATION_5_6 = object : Migration(5, 6) {
            private fun fix(exec: (String) -> Unit) {
                exec("DROP TABLE IF EXISTS `reading_history`")
                exec("CREATE TABLE IF NOT EXISTS `reading_history` (`slug` TEXT NOT NULL, `title` TEXT NOT NULL, `coverUrl` TEXT, `readAt` INTEGER NOT NULL, PRIMARY KEY(`slug`))")
                exec("DROP TABLE IF EXISTS `checkins`")
                exec("CREATE TABLE IF NOT EXISTS `checkins` (`dateKey` TEXT NOT NULL, `streakDay` INTEGER NOT NULL, `xpEarned` INTEGER NOT NULL, `checkinAt` INTEGER NOT NULL, PRIMARY KEY(`dateKey`))")
                exec("DROP TABLE IF EXISTS `user_stats`")
                exec("CREATE TABLE IF NOT EXISTS `user_stats` (`id` INTEGER NOT NULL, `totalXp` INTEGER NOT NULL, `level` INTEGER NOT NULL, `currentStreak` INTEGER NOT NULL, `longestStreak` INTEGER NOT NULL, `lastReadingXpDate` TEXT, PRIMARY KEY(`id`))")
                exec("DROP TABLE IF EXISTS `passport_stamps`")
                exec("CREATE TABLE IF NOT EXISTS `passport_stamps` (`id` TEXT NOT NULL, `type` TEXT NOT NULL, `displayName` TEXT NOT NULL, `earnedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            }
            override fun migrate(connection: SQLiteConnection) = fix { connection.execSQL(it) }
            @Suppress("DEPRECATION")
            override fun migrate(db: SupportSQLiteDatabase) = fix { db.execSQL(it) }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            private fun fix(exec: (String) -> Unit) {
                exec("DROP TABLE IF EXISTS `reading_history`")
                exec("CREATE TABLE IF NOT EXISTS `reading_history` (`slug` TEXT NOT NULL, `title` TEXT NOT NULL, `coverUrl` TEXT, `readAt` INTEGER NOT NULL, PRIMARY KEY(`slug`))")
            }
            override fun migrate(connection: SQLiteConnection) = fix { connection.execSQL(it) }
            @Suppress("DEPRECATION")
            override fun migrate(db: SupportSQLiteDatabase) = fix { db.execSQL(it) }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            private fun fix(exec: (String) -> Unit) {
                exec("CREATE TABLE IF NOT EXISTS `passport_stamps` (`id` TEXT NOT NULL, `type` TEXT NOT NULL, `displayName` TEXT NOT NULL, `earnedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            }
            override fun migrate(connection: SQLiteConnection) = fix { connection.execSQL(it) }
            @Suppress("DEPRECATION")
            override fun migrate(db: SupportSQLiteDatabase) = fix { db.execSQL(it) }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            private fun fix(exec: (String) -> Unit) {
                exec("ALTER TABLE `user_stats` ADD COLUMN `lastReadingXpDate` TEXT")
            }
            override fun migrate(connection: SQLiteConnection) = fix { connection.execSQL(it) }
            @Suppress("DEPRECATION")
            override fun migrate(db: SupportSQLiteDatabase) = fix { db.execSQL(it) }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            private fun fix(exec: (String) -> Unit) {
                exec("CREATE TABLE IF NOT EXISTS `reading_history` (`slug` TEXT NOT NULL, `title` TEXT NOT NULL, `coverUrl` TEXT, `readAt` INTEGER NOT NULL, PRIMARY KEY(`slug`))")
                exec("CREATE TABLE IF NOT EXISTS `checkins` (`dateKey` TEXT NOT NULL, `streakDay` INTEGER NOT NULL, `xpEarned` INTEGER NOT NULL, `checkinAt` INTEGER NOT NULL, PRIMARY KEY(`dateKey`))")
                exec("CREATE TABLE IF NOT EXISTS `user_stats` (`id` INTEGER NOT NULL, `totalXp` INTEGER NOT NULL, `level` INTEGER NOT NULL, `currentStreak` INTEGER NOT NULL, `longestStreak` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            }
            override fun migrate(connection: SQLiteConnection) = fix { connection.execSQL(it) }
            @Suppress("DEPRECATION")
            override fun migrate(db: SupportSQLiteDatabase) = fix { db.execSQL(it) }
        }
    }
}
