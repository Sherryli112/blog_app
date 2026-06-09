package com.funtime.blog.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funtime.blog.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val fontSize: Int = 100,
    val darkMode: String = "system"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        repository.fontSize,
        repository.darkMode
    ) { fontSize, darkMode ->
        SettingsUiState(fontSize = fontSize, darkMode = darkMode)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setFontSize(size: Int) {
        viewModelScope.launch { repository.setFontSize(size) }
    }

    fun setDarkMode(mode: String) {
        viewModelScope.launch { repository.setDarkMode(mode) }
    }
}
