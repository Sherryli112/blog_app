package com.funtime.blog.ui.bookmark

import app.cash.turbine.test
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
class BookmarkViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var bookmarkRepository: BookmarkRepository
    private lateinit var articleRepository: ArticleRepository
    private lateinit var viewModel: BookmarkViewModel

    private val slugsFlow = MutableStateFlow<List<String>>(emptyList())

    private fun makeDetail(slug: String) = ArticleDetailDto(
        id = 1, title = "Title $slug", excerpt = "Excerpt", slug = slug,
        content = null, publishedAt = null, cover = null, author = null,
        tags = null, theme = null, category = null
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        bookmarkRepository = mockk()
        articleRepository = mockk()
        every { bookmarkRepository.getAllSlugs() } returns slugsFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `empty slugs shows empty list without loading`() = runTest {
        slugsFlow.value = emptyList()
        viewModel = BookmarkViewModel(bookmarkRepository, articleRepository)

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertTrue(state.articles.isEmpty())
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `slugs fetched successfully`() = runTest {
        slugsFlow.value = listOf("slug-1")
        coEvery { articleRepository.getArticleBySlug("slug-1") } returns makeDetail("slug-1")
        viewModel = BookmarkViewModel(bookmarkRepository, articleRepository)

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(1, state.articles.size)
            assertEquals("slug-1", state.articles[0].slug)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `failed article is silently skipped`() = runTest {
        slugsFlow.value = listOf("slug-ok", "slug-fail")
        coEvery { articleRepository.getArticleBySlug("slug-ok") } returns makeDetail("slug-ok")
        coEvery { articleRepository.getArticleBySlug("slug-fail") } throws RuntimeException("404")
        viewModel = BookmarkViewModel(bookmarkRepository, articleRepository)

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(1, state.articles.size)
            assertEquals("slug-ok", state.articles[0].slug)
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `all articles fail shows error`() = runTest {
        slugsFlow.value = listOf("slug-fail")
        coEvery { articleRepository.getArticleBySlug("slug-fail") } throws RuntimeException("Network error")
        viewModel = BookmarkViewModel(bookmarkRepository, articleRepository)

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertNotNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `removeBookmark calls repository toggle`() = runTest {
        slugsFlow.value = emptyList()
        coEvery { bookmarkRepository.toggle("slug-1") } just Runs
        viewModel = BookmarkViewModel(bookmarkRepository, articleRepository)

        viewModel.removeBookmark("slug-1")

        coVerify { bookmarkRepository.toggle("slug-1") }
    }

    @Test
    fun `retry re-fetches articles`() = runTest {
        slugsFlow.value = listOf("slug-1")
        coEvery { articleRepository.getArticleBySlug("slug-1") } returns makeDetail("slug-1")
        viewModel = BookmarkViewModel(bookmarkRepository, articleRepository)

        // Confirm initial load succeeded
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.articles.size)
            cancelAndIgnoreRemainingEvents()
        }

        // Simulate network recovery after failure
        coEvery { articleRepository.getArticleBySlug("slug-1") } returns makeDetail("slug-1")
        viewModel.retry()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(1, state.articles.size)
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
