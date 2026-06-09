package com.funtime.blog.data.api.dto

data class ArticleDetailResponseDto(
    val data: ArticleDetailDto?
)

data class ArticleDetailDto(
    val id: Int,
    val title: String?,
    val excerpt: String?,
    val slug: String?,
    val content: String?,
    val publishedAt: String?,
    val cover: CoverDto?,
    val author: AuthorDto?,
    val tags: List<String>?,
    val theme: ArticleMetaDto?,
    val category: ArticleMetaDto?
)

data class ArticleMetaDto(
    val name: String?,
    val displayName: String?
)
