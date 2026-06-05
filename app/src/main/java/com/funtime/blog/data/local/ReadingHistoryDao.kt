package com.funtime.blog.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingHistoryDao {
    @Query("SELECT * FROM reading_history ORDER BY readAt DESC")
    fun getAll(): Flow<List<ReadingHistoryEntity>>

    @Query("SELECT * FROM reading_history WHERE slug = :slug")
    suspend fun getBySlug(slug: String): ReadingHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ReadingHistoryEntity)

    @Query("DELETE FROM reading_history WHERE slug NOT IN (SELECT slug FROM reading_history ORDER BY readAt DESC LIMIT 100)")
    suspend fun trimToLatest100()

    @Query("DELETE FROM reading_history")
    suspend fun deleteAll()
}
