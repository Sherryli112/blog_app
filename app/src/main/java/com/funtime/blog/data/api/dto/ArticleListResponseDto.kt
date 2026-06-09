package com.funtime.blog.data.api.dto

import com.google.gson.annotations.SerializedName

data class ArticleListResponseDto(
    val data: List<ArticleItemDto>,
    val meta: MetaDto
)

data class ArticleItemDto(
    val id: Int,
    val title: String?,
    val excerpt: String?,
    val slug: String?,
    val content: String?,
    @SerializedName("publishedAt") val publishedAt: String?,
    val cover: CoverDto?,
    val author: AuthorDto?
)

data class CoverDto(
    val url: String?
)

data class AuthorDto(
    val name: String?,
    val slug: String?,
    val avatar: CoverDto?
)

data class MetaDto(
    val pagination: PaginationDto
)

data class PaginationDto(
    val page: Int,
    val pageSize: Int,
    val pageCount: Int,
    val total: Int
)
