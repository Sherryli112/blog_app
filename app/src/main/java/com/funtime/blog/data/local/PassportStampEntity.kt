package com.funtime.blog.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "passport_stamps")
data class PassportStampEntity(
    @PrimaryKey val id: String,
    val type: String,        // "region" | "streak"
    val displayName: String,
    val earnedAt: Long = System.currentTimeMillis()
)
