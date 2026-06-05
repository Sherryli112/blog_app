package com.funtime.blog.ui.home

import app.cash.turbine.test
import com.funtime.blog.data.api.dto.*
import com.funtime.blog.data.repository.ArticleRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: ArticleRepository
    private lateinit var viewModel: HomeViewModel

    private fun makeResponse(vararg titles: String, pageCount: Int = 1) = ArticleListResponseDto(
        data = titles.mapIndexed { i, title ->
            ArticleItemDto(
                id = i + 1, title = title, excerpt = "excerpt", slug = "slug-$i",
                content = null, publishedAt = null, cover = null, author = null
            )
        },
        meta = MetaDto(PaginationDto(1, 10, pageCount, titles.size))
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads latest articles page 1`() = runTest {
        coEvery { repository.getLatestArticles(1, any()) } returns makeResponse("A", "B")
        viewModel = HomeViewModel(repository)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(listOf("A", "B"), state.latestState.items.map { it.title })
            assertFalse(state.latestState.isLoading)
            assertFalse(state.latestState.hasMore)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadMoreLatest appends page 2`() = runTest {
        coEvery { repository.getLatestArticles(1, any()) } returns makeResponse("A", pageCount = 2)
        coEvery { repository.getLatestArticles(2, any()) } returns makeResponse("B", pageCount = 2)
        viewModel = HomeViewModel(repository)
        viewModel.loadMoreLatest()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(listOf("A", "B"), state.latestState.items.map { it.title })
            assertFalse(state.latestState.hasMore)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadPopular loads popular articles`() = runTest {
        coEvery { repository.getLatestArticles(1, any()) } returns makeResponse("Latest")
        coEvery { repository.getPopularArticles(1, any()) } returns makeResponse("Popular")
        viewModel = HomeViewModel(repository)
        viewModel.loadPopular()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(listOf("Popular"), state.popularState.items.map { it.title })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `error state when API fails`() = runTest {
        coEvery { repository.getLatestArticles(1, any()) } throws RuntimeException("Network error")
        viewModel = HomeViewModel(repository)

        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull(state.latestState.error)
            assertFalse(state.latestState.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
