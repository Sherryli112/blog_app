package com.funtime.blog.ui.article

import app.cash.turbine.test
import androidx.lifecycle.SavedStateHandle
import com.funtime.blog.data.api.dto.*
import com.funtime.blog.data.repository.ArticleRepository
import com.funtime.blog.data.repository.BookmarkRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArticleDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var articleRepository: ArticleRepository
    private lateinit var bookmarkRepository: BookmarkRepository

    private fun makeDetail(slug: String) = ArticleDetailDto(
        id = 1, title = "Title", excerpt = "Excerpt", slug = slug,
        content = null, publishedAt = null, cover = null, author = null,
        tags = null, theme = null, category = null
    )

    private fun makeViewModel(slug: String): ArticleDetailViewModel {
        val savedState = SavedStateHandle(mapOf("slug" to slug))
        return ArticleDetailViewModel(articleRepository, bookmarkRepository, savedState)
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        articleRepository = mockk()
        bookmarkRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `isBookmarked reflects repository state`() = runTest {
        coEvery { articleRepository.getArticleBySlug("slug-1") } returns makeDetail("slug-1")
        val isBookmarkedFlow = MutableStateFlow(false)
        every { bookmarkRepository.isBookmarked("slug-1") } returns isBookmarkedFlow

        val viewModel = makeViewModel("slug-1")

        viewModel.isBookmarked.test {
            assertFalse(awaitItem())
            isBookmarkedFlow.value = true
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleBookmark calls repository toggle`() = runTest {
        coEvery { articleRepository.getArticleBySlug("slug-1") } returns makeDetail("slug-1")
        every { bookmarkRepository.isBookmarked("slug-1") } returns MutableStateFlow(false)
        coEvery { bookmarkRepository.toggle("slug-1") } just Runs

        val viewModel = makeViewModel("slug-1")
        viewModel.toggleBookmark()

        coVerify { bookmarkRepository.toggle("slug-1") }
    }
}
