package com.funtime.blog.data.repository

import com.funtime.blog.data.api.BlogApiService
import com.funtime.blog.data.api.dto.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ArticleRepositoryTest {

    private lateinit var apiService: BlogApiService
    private lateinit var repository: ArticleRepository

    @Before
    fun setUp() {
        apiService = mockk()
        repository = ArticleRepository(apiService)
    }

    @Test
    fun `getLatestArticles calls API with correct sort param`() = runTest {
        val mockResponse = ArticleListResponseDto(
            data = emptyList(),
            meta = MetaDto(PaginationDto(1, 10, 0, 0))
        )
        coEvery { apiService.getArticles("custom_published_at:desc", 1, 10) } returns mockResponse

        repository.getLatestArticles(page = 1)

        coVerify { apiService.getArticles("custom_published_at:desc", 1, 10) }
    }

    @Test
    fun `getPopularArticles calls API with correct sort param`() = runTest {
        val mockResponse = ArticleListResponseDto(
            data = emptyList(),
            meta = MetaDto(PaginationDto(1, 10, 0, 0))
        )
        coEvery { apiService.getArticles("hot_rank:asc", 1, 10) } returns mockResponse

        repository.getPopularArticles(page = 1)

        coVerify { apiService.getArticles("hot_rank:asc", 1, 10) }
    }

    @Test
    fun `getArticleBySlug delegates to API`() = runTest {
        val mockResponse = ArticleDetailDto(
            id = 1, title = null, excerpt = null, slug = "my-slug",
            content = null, publishedAt = null, cover = null, author = null,
            tags = null, theme = null, category = null
        )
        coEvery { apiService.getArticleBySlug("my-slug") } returns mockResponse

        repository.getArticleBySlug("my-slug")

        coVerify { apiService.getArticleBySlug("my-slug") }
    }

    @Test
    fun `getLatestArticles returns data from API`() = runTest {
        val article = ArticleItemDto(
            id = 1, title = "Test", excerpt = "Excerpt",
            slug = "test", content = null, publishedAt = null, cover = null, author = null
        )
        val mockResponse = ArticleListResponseDto(
            data = listOf(article),
            meta = MetaDto(PaginationDto(1, 10, 1, 1))
        )
        coEvery { apiService.getArticles(any(), any(), any()) } returns mockResponse

        val result = repository.getLatestArticles(page = 1)

        assertEquals(1, result.data.size)
        assertEquals("Test", result.data[0].title)
    }
}
