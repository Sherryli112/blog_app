package com.funtime.blog.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checkins")
data class CheckinEntity(
    @PrimaryKey val dateKey: String,
    val streakDay: Int,
    val xpEarned: Int,
    val checkinAt: Long = System.currentTimeMillis()
)
