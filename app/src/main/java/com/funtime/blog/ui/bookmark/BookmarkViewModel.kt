package com.funtime.blog.ui.bookmark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funtime.blog.data.api.dto.ArticleDetailDto
import com.funtime.blog.data.repository.ArticleRepository
import com.funtime.blog.data.repository.BookmarkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookmarkUiState(
    val isLoading: Boolean = false,
    val articles: List<ArticleDetailDto> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class BookmarkViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val articleRepository: ArticleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookmarkUiState())
    val uiState: StateFlow<BookmarkUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            bookmarkRepository.getAllSlugs().collect { slugs ->
                fetchArticles(slugs)
            }
        }
    }

    private suspend fun fetchArticles(slugs: List<String>) {
        if (slugs.isEmpty()) {
            _uiState.value = BookmarkUiState(isLoading = false)
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        val articles = slugs.mapNotNull { slug ->
            try { articleRepository.getArticleBySlug(slug) } catch (_: Exception) { null }
        }
        _uiState.update {
            it.copy(
                isLoading = false,
                articles = articles,
                error = if (articles.isEmpty()) "載入失敗，請檢查網路連線" else null
            )
        }
    }

    fun removeBookmark(slug: String) {
        viewModelScope.launch { bookmarkRepository.toggle(slug) }
    }

    fun retry() {
        viewModelScope.launch {
            val slugs = bookmarkRepository.getAllSlugs().first()
            fetchArticles(slugs)
        }
    }
}
