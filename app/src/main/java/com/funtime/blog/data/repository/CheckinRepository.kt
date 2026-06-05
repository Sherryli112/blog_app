package com.funtime.blog.data.repository

import com.funtime.blog.data.local.AppDatabase
import com.funtime.blog.data.local.CheckinDao
import com.funtime.blog.data.local.CheckinEntity
import com.funtime.blog.data.local.UserStatsDao
import com.funtime.blog.data.local.UserStatsEntity
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

private const val XP_CHECKIN_BASE = 50
private const val XP_READING = 20

fun checkinXpForStreak(streak: Int): Int {
    val multiplier = when {
        streak >= 100 -> 5.0
        streak >= 30  -> 3.0
        streak >= 7   -> 2.0
        streak >= 3   -> 1.5
        else          -> 1.0
    }
    return (XP_CHECKIN_BASE * multiplier).toInt()
}

sealed class CheckinResult {
    data class Success(val streak: Int, val xpEarned: Int, val totalXp: Int) : CheckinResult()
    object AlreadyCheckedIn : CheckinResult()
}

@Singleton
class CheckinRepository @Inject constructor(
    private val db: AppDatabase,
    private val checkinDao: CheckinDao,
    private val userStatsDao: UserStatsDao,
    private val passportRepository: PassportRepository
) {
    val statsFlow: Flow<UserStatsEntity?> = userStatsDao.observe()

    // hasReadTodayFlow 與 addReadingXp 共用同一資料來源，確保語義一致
    val hasReadTodayFlow: Flow<Boolean> = statsFlow.map { stats ->
        stats?.lastReadingXpDate == dateKey()
    }

    private fun dateKey(offsetDays: Int = 0): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Taipei"))
        if (offsetDays != 0) cal.add(Calendar.DAY_OF_MONTH, offsetDays)
        return "%04d-%02d-%02d".format(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    suspend fun hasCheckedInToday(): Boolean = checkinDao.getByDate(dateKey()) != null

    suspend fun checkin(): CheckinResult {
        val today = dateKey()
        if (checkinDao.getByDate(today) != null) return CheckinResult.AlreadyCheckedIn

        val stats = userStatsDao.get() ?: UserStatsEntity()
        val latest = checkinDao.getLatest()
        val newStreak = if (latest?.dateKey == dateKey(-1)) stats.currentStreak + 1 else 1

        val xpEarned = checkinXpForStreak(newStreak)
        val newTotalXp = stats.totalXp + xpEarned
        val newLongest = maxOf(stats.longestStreak, newStreak)

        // 原子操作：兩張表一起寫，避免 crash 後 streak 不一致
        db.withTransaction {
            checkinDao.insert(CheckinEntity(dateKey = today, streakDay = newStreak, xpEarned = xpEarned))
            userStatsDao.upsert(
                stats.copy(
                    totalXp = newTotalXp,
                    level = calculateLevel(newTotalXp),
                    currentStreak = newStreak,
                    longestStreak = newLongest
                )
            )
            passportRepository.unlockStreakStamp(newStreak)
        }
        return CheckinResult.Success(streak = newStreak, xpEarned = xpEarned, totalXp = newTotalXp)
    }

    suspend fun addReadingXp() {
        val stats = userStatsDao.get() ?: UserStatsEntity()
        val today = dateKey()
        if (stats.lastReadingXpDate == today) return
        val newXp = stats.totalXp + XP_READING
        userStatsDao.upsert(
            stats.copy(
                totalXp = newXp,
                level = calculateLevel(newXp),
                lastReadingXpDate = today
            )
        )
    }

    suspend fun clearAll() {
        userStatsDao.deleteAll()
        checkinDao.deleteAll()
    }

    fun calculateLevel(xp: Int): Int = when {
        xp >= 40000 -> 5
        xp >= 15000 -> 4
        xp >= 5000  -> 3
        xp >= 1000  -> 2
        else        -> 1
    }
}
