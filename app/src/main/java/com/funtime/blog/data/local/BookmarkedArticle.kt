package com.funtime.blog.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkedArticle(
    @PrimaryKey val slug: String,
    val bookmarkedAt: Long
)
