package com.funtime.blog.data.repository

import com.funtime.blog.data.local.BookmarkDao
import com.funtime.blog.data.local.BookmarkedArticle
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepository @Inject constructor(
    private val dao: BookmarkDao
) {
    fun isBookmarked(slug: String): Flow<Boolean> = dao.isBookmarked(slug)

    fun getAllSlugs(): Flow<List<String>> = dao.getAllSlugs()

    suspend fun toggle(slug: String) {
        val deleted = dao.delete(slug)
        if (deleted == 0) {
            dao.insert(BookmarkedArticle(slug = slug, bookmarkedAt = System.currentTimeMillis()))
        }
    }
}
