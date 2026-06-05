package com.funtime.blog.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PassportStampDao {
    @Query("SELECT * FROM passport_stamps ORDER BY earnedAt ASC")
    fun getAll(): Flow<List<PassportStampEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(stamp: PassportStampEntity)

    @Query("DELETE FROM passport_stamps")
    suspend fun deleteAll()
}
