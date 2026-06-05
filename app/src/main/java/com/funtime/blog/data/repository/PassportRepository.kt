package com.funtime.blog.data.repository

import com.funtime.blog.data.local.PassportStampDao
import com.funtime.blog.data.local.PassportStampEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PassportRepository @Inject constructor(
    private val dao: PassportStampDao
) {
    companion object {
        val MILESTONE_DAYS = setOf(7, 30, 100)

        private val MILESTONE_META = mapOf(
            7   to ("streak_7"   to "夜旅者"),
            30  to ("streak_30"  to "空橋旅人"),
            100 to ("streak_100" to "環球旅人")
        )
    }

    val stampsFlow: Flow<List<PassportStampEntity>> = dao.getAll()

    suspend fun unlockRegionStamp(themeKey: String, displayName: String) {
        dao.insert(PassportStampEntity(id = "region_$themeKey", type = "region", displayName = displayName))
    }

    suspend fun unlockStreakStamp(streak: Int) {
        val (id, name) = MILESTONE_META[streak] ?: return
        dao.insert(PassportStampEntity(id = id, type = "streak", displayName = name))
    }

    suspend fun clearAll() = dao.deleteAll()
}
