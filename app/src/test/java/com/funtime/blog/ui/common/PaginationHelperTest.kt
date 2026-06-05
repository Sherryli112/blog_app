package com.funtime.blog.ui.common

import app.cash.turbine.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PaginationHelperTest {

    @Test
    fun `loadFirst emits isLoading then items`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val pager = PaginationHelper<String>(
            scope = CoroutineScope(dispatcher),
            fetch = { Pair(listOf("A", "B"), 2) }
        )
        pager.state.test {
            assertEquals(PaginatedState<String>(hasMore = false), awaitItem()) // initial
            pager.loadFirst()
            assertEquals(true, awaitItem().isLoading)    // loading state
            val done = awaitItem()
            assertEquals(listOf("A", "B"), done.items)
            assertFalse(done.isLoading)
            assertTrue(done.hasMore)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadMore appends items and increments page`() = runTest(UnconfinedTestDispatcher()) {
        val pager = PaginationHelper<String>(
            scope = this,
            fetch = { page ->
                when (page) {
                    1 -> Pair(listOf("A"), 2)
                    2 -> Pair(listOf("B"), 2)
                    else -> Pair(emptyList(), 2)
                }
            }
        )
        pager.loadFirst()
        pager.loadMore()
        pager.state.test {
            val state = awaitItem()
            assertEquals(listOf("A", "B"), state.items)
            assertFalse(state.hasMore)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `hasMore is false when currentPage equals pageCount`() = runTest(UnconfinedTestDispatcher()) {
        val pager = PaginationHelper<String>(
            scope = this,
            fetch = { Pair(listOf("A"), 1) }
        )
        pager.loadFirst()
        pager.state.test {
            val state = awaitItem()
            assertFalse(state.hasMore)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadMore is no-op when hasMore is false`() = runTest(UnconfinedTestDispatcher()) {
        var callCount = 0
        val pager = PaginationHelper<String>(
            scope = this,
            fetch = { callCount++; Pair(listOf("A"), 1) }
        )
        pager.loadFirst()
        pager.loadMore() // hasMore=false, should be ignored
        assertEquals(1, callCount)
    }

    @Test
    fun `loadFirst on error sets error state`() = runTest(UnconfinedTestDispatcher()) {
        val pager = PaginationHelper<String>(
            scope = this,
            fetch = { throw RuntimeException("Network error") }
        )
        pager.loadFirst()
        pager.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals("Network error", state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
