package com.funtime.blog.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY searchedAt DESC LIMIT 10")
    fun getRecent(): Flow<List<SearchHistoryEntity>>

    @Query("SELECT * FROM search_history WHERE query = :query LIMIT 1")
    suspend fun findByQuery(query: String): SearchHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE id NOT IN (SELECT id FROM search_history ORDER BY searchedAt DESC LIMIT 10)")
    suspend fun trimToLimit()

    @Query("DELETE FROM search_history")
    suspend fun deleteAll()
}
