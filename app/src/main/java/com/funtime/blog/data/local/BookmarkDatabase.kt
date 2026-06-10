package com.funtime.blog.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

@Database(
    entities = [BookmarkedArticle::class, SearchHistoryEntity::class, CachedArticleEntity::class],
    version = 3,
    exportSchema = false
)
abstract class BookmarkDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun cachedArticleDao(): CachedArticleDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS search_history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "query TEXT NOT NULL, " +
                "searchedAt INTEGER NOT NULL)"
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS cached_articles (" +
                "slug TEXT PRIMARY KEY NOT NULL, " +
                "title TEXT NOT NULL, " +
                "articleJson TEXT NOT NULL, " +
                "cachedAt INTEGER NOT NULL)"
        )
    }
}
