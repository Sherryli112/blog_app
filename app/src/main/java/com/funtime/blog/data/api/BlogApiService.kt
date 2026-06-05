package com.funtime.blog.data.api

import com.funtime.blog.data.api.dto.ArticleDetailDto
import com.funtime.blog.data.api.dto.ArticleListResponseDto
import com.funtime.blog.data.api.dto.RegionDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BlogApiService {

    @GET("articles")
    suspend fun getArticles(
        @Query("sort") sort: String,
        @Query("pagination[page]") page: Int,
        @Query("pagination[pageSize]") pageSize: Int = 10
    ): ArticleListResponseDto

    @GET("articles")
    suspend fun getArticlesByCategory(
        @Query("filters[ft_theme][display_name][\$eq]") theme: String,
        @Query("filters[ft_category][display_name][\$eq]") category: String,
        @Query("sort") sort: String = "custom_published_at:desc",
        @Query("pagination[page]") page: Int = 1,
        @Query("pagination[pageSize]") pageSize: Int = 10
    ): ArticleListResponseDto

    @GET("articles")
    suspend fun searchArticles(
        @Query("filters[tags][name][\$eq]") tag: String,
        @Query("sort") sort: String = "custom_published_at:desc",
        @Query("pagination[page]") page: Int = 1,
        @Query("pagination[pageSize]") pageSize: Int = 10
    ): ArticleListResponseDto

    @GET("articles")
    suspend fun getArticlesByAuthor(
        @Query("filters[author][slug][\$eq]") authorSlug: String,
        @Query("sort") sort: String = "custom_published_at:desc",
        @Query("pagination[page]") page: Int = 1,
        @Query("pagination[pageSize]") pageSize: Int = 10
    ): ArticleListResponseDto

    @GET("articles/slug/{slug}")
    suspend fun getArticleBySlug(
        @Path("slug") slug: String
    ): ArticleDetailDto

    @GET("ft-regions")
    suspend fun getRegions(): List<RegionDto>
}
