package com.funtime.blog.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [BookmarkedArticle::class], version = 1, exportSchema = false)
abstract class BookmarkDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
}
