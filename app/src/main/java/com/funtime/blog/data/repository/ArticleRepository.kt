package com.funtime.blog.data.repository

import com.funtime.blog.data.api.BlogApiService
import com.funtime.blog.data.api.dto.ArticleDetailDto
import com.funtime.blog.data.api.dto.ArticleItemDto
import com.funtime.blog.data.api.dto.ArticleListResponseDto
import com.funtime.blog.data.api.dto.RegionDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticleRepository @Inject constructor(
    private val apiService: BlogApiService
) {
    suspend fun getLatestArticles(page: Int = 1, pageSize: Int = 10): ArticleListResponseDto =
        apiService.getArticles("custom_published_at:desc", page, pageSize)

    suspend fun getPopularArticles(page: Int = 1, pageSize: Int = 10): ArticleListResponseDto =
        apiService.getArticles("hot_rank:asc", page, pageSize)

    suspend fun getArticleBySlug(slug: String): ArticleDetailDto =
        apiService.getArticleBySlug(slug)

    suspend fun getRegions(): List<RegionDto> =
        apiService.getRegions()

    suspend fun getArticlesByCategory(theme: String, category: String, page: Int = 1): ArticleListResponseDto =
        apiService.getArticlesByCategory(theme = theme, category = category, page = page)

    suspend fun getRelatedArticles(theme: String, category: String, excludeSlug: String): List<ArticleItemDto> =
        apiService.getArticlesByCategory(theme = theme, category = category, pageSize = 6)
            .data.filter { it.slug != excludeSlug }.take(5)

    suspend fun searchArticles(tag: String, page: Int = 1): ArticleListResponseDto =
        apiService.searchArticles(tag = tag, page = page)

    suspend fun getArticlesByAuthor(authorSlug: String, page: Int = 1): ArticleListResponseDto =
        apiService.getArticlesByAuthor(authorSlug = authorSlug, page = page)
}
