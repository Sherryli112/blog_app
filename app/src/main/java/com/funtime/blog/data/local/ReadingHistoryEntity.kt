package com.funtime.blog.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reading_history")
data class ReadingHistoryEntity(
    @PrimaryKey val slug: String,
    val title: String,
    val coverUrl: String?,
    val readAt: Long = System.currentTimeMillis()
)
