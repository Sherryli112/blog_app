package com.funtime.blog.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funtime.blog.data.local.ReadingHistoryEntity
import com.funtime.blog.data.local.UserSession
import com.funtime.blog.data.local.UserStatsEntity
import com.funtime.blog.data.repository.AuthRepository
import com.funtime.blog.data.repository.BookmarkRepository
import com.funtime.blog.data.repository.CheckinRepository
import com.funtime.blog.data.repository.CheckinResult
import com.funtime.blog.data.repository.PassportRepository
import com.funtime.blog.data.repository.ReadingHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val checkinRepository: CheckinRepository,
    private val readingHistoryRepository: ReadingHistoryRepository,
    private val passportRepository: PassportRepository,
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    val session: StateFlow<UserSession?> = authRepository.sessionFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val stats: StateFlow<UserStatsEntity?> = checkinRepository.statsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val recentHistory: StateFlow<List<ReadingHistoryEntity>> = readingHistoryRepository.historyFlow
        .map { it.take(5) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hasReadToday: StateFlow<Boolean> = checkinRepository.hasReadTodayFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _checkinResult = MutableStateFlow<CheckinResult?>(null)
    val checkinResult = _checkinResult.asStateFlow()

    private val _hasCheckedInToday = MutableStateFlow(false)
    val hasCheckedInToday = _hasCheckedInToday.asStateFlow()

    init {
        // session 每次變化（登入/登出/切換帳號）都重新從 DB 查今日簽到狀態
        viewModelScope.launch {
            session.collect { userSession ->
                _hasCheckedInToday.value = if (userSession != null) {
                    checkinRepository.hasCheckedInToday()
                } else {
                    false
                }
            }
        }
    }

    fun checkin() {
        viewModelScope.launch {
            val result = checkinRepository.checkin()
            _checkinResult.value = result
            if (result is CheckinResult.Success) _hasCheckedInToday.value = true
        }
    }

    fun dismissCheckinResult() {
        _checkinResult.value = null
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            checkinRepository.clearAll()
            readingHistoryRepository.clearAll()
            passportRepository.clearAll()
            bookmarkRepository.clearAll()
            _checkinResult.value = null
        }
    }
}
