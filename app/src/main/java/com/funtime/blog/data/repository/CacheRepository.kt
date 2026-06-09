package com.funtime.blog.data.repository

import com.funtime.blog.data.api.dto.ArticleDetailDto
import com.funtime.blog.data.local.CachedArticleDao
import com.funtime.blog.data.local.CachedArticleEntity
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CacheRepository @Inject constructor(
    private val dao: CachedArticleDao,
    private val gson: Gson
) {
    suspend fun save(article: ArticleDetailDto) {
        val slug = article.slug ?: return
        dao.upsert(
            CachedArticleEntity(
                slug = slug,
                title = article.title ?: "",
                articleJson = gson.toJson(article),
                cachedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun get(slug: String): ArticleDetailDto? {
        val entity = dao.getBySlug(slug) ?: return null
        return try {
            gson.fromJson(entity.articleJson, ArticleDetailDto::class.java)
        } catch (_: Exception) {
            null
        }
    }
}
