package com.funtime.blog.data.repository

import com.funtime.blog.data.local.ReadingHistoryDao
import com.funtime.blog.data.local.ReadingHistoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadingHistoryRepository @Inject constructor(
    private val dao: ReadingHistoryDao
) {
    val historyFlow: Flow<List<ReadingHistoryEntity>> = dao.getAll()

    suspend fun clearAll() = dao.deleteAll()

    suspend fun record(slug: String, title: String, coverUrl: String?) {
        dao.upsert(ReadingHistoryEntity(slug = slug, title = title, coverUrl = coverUrl))
        dao.trimToLatest100()
    }
}
