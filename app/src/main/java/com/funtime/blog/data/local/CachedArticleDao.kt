package com.funtime.blog.data.local

import androidx.room.*

@Dao
interface CachedArticleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CachedArticleEntity)

    @Query("SELECT * FROM cached_articles WHERE slug = :slug LIMIT 1")
    suspend fun getBySlug(slug: String): CachedArticleEntity?

    @Query("DELETE FROM cached_articles WHERE cachedAt < :threshold")
    suspend fun deleteOlderThan(threshold: Long)
}
