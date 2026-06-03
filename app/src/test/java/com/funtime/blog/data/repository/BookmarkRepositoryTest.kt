package com.funtime.blog.data.repository

import app.cash.turbine.test
import com.funtime.blog.data.local.BookmarkDao
import com.funtime.blog.data.local.BookmarkedArticle
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class BookmarkRepositoryTest {

    private lateinit var dao: BookmarkDao
    private lateinit var repository: BookmarkRepository

    @Before
    fun setUp() {
        dao = mockk()
        repository = BookmarkRepository(dao)
    }

    @Test
    fun `toggle inserts when not bookmarked`() = runTest {
        coEvery { dao.delete("slug-1") } returns 0
        coEvery { dao.insert(any()) } just Runs

        repository.toggle("slug-1")

        coVerify { dao.delete("slug-1") }
        coVerify { dao.insert(match { it.slug == "slug-1" }) }
    }

    @Test
    fun `toggle deletes when already bookmarked`() = runTest {
        coEvery { dao.delete("slug-1") } returns 1

        repository.toggle("slug-1")

        coVerify { dao.delete("slug-1") }
        coVerify(exactly = 0) { dao.insert(any()) }
    }

    @Test
    fun `isBookmarked delegates to dao`() = runTest {
        val flow = MutableStateFlow(true)
        every { dao.isBookmarked("slug-1") } returns flow

        repository.isBookmarked("slug-1").test {
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAllSlugs delegates to dao`() = runTest {
        val flow = MutableStateFlow(listOf("slug-1", "slug-2"))
        every { dao.getAllSlugs() } returns flow

        repository.getAllSlugs().test {
            assertEquals(listOf("slug-1", "slug-2"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
