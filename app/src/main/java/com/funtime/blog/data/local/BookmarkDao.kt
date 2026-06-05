package com.funtime.blog.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT slug FROM bookmarks ORDER BY bookmarkedAt DESC")
    fun getAllSlugs(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE slug = :slug)")
    fun isBookmarked(slug: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: BookmarkedArticle)

    @Query("DELETE FROM bookmarks WHERE slug = :slug")
    suspend fun delete(slug: String): Int

    @Query("DELETE FROM bookmarks")
    suspend fun deleteAll()
}
