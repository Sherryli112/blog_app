package com.funtime.blog.data.repository

import com.funtime.blog.data.local.SearchHistoryDao
import com.funtime.blog.data.local.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchHistoryRepository @Inject constructor(
    private val dao: SearchHistoryDao
) {
    val recentSearches: Flow<List<String>> = dao.getRecent().map { list ->
        list.map { it.query }
    }

    suspend fun saveSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        val existing = dao.findByQuery(trimmed)
        if (existing != null) {
            dao.insert(existing.copy(searchedAt = System.currentTimeMillis()))
        } else {
            dao.insert(SearchHistoryEntity(query = trimmed, searchedAt = System.currentTimeMillis()))
            dao.trimToLimit()
        }
    }

    suspend fun clearAll() = dao.deleteAll()
}
