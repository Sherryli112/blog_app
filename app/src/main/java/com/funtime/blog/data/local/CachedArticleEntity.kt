package com.funtime.blog.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_articles")
data class CachedArticleEntity(
    @PrimaryKey val slug: String,
    val title: String,
    val articleJson: String,
    val cachedAt: Long
)
