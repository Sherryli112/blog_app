package com.funtime.blog.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funtime.blog.data.api.dto.RegionDto
import com.funtime.blog.data.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryUiState(
    val isLoading: Boolean = false,
    val regions: List<RegionDto> = emptyList(),
    val error: String? = null,
    val expandedTheme: String? = null
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: ArticleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        loadRegions()
    }

    fun loadRegions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val regions = repository.getRegions()
                _uiState.update { it.copy(isLoading = false, regions = regions) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun toggleTheme(themeName: String) {
        _uiState.update { state ->
            state.copy(expandedTheme = if (state.expandedTheme == themeName) null else themeName)
        }
    }
}
