package com.funtime.blog.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CheckinDao {
    @Query("SELECT * FROM checkins WHERE dateKey = :dateKey")
    suspend fun getByDate(dateKey: String): CheckinEntity?

    @Query("SELECT * FROM checkins ORDER BY dateKey DESC LIMIT 1")
    suspend fun getLatest(): CheckinEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(checkin: CheckinEntity)

    @Query("DELETE FROM checkins")
    suspend fun deleteAll()
}
