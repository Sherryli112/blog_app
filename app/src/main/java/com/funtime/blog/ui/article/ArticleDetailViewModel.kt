package com.funtime.blog.ui.article

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funtime.blog.data.api.dto.ArticleDetailDto
import com.funtime.blog.data.api.dto.ArticleItemDto
import com.funtime.blog.data.repository.ArticleRepository
import com.funtime.blog.data.repository.AuthRepository
import com.funtime.blog.data.repository.BookmarkRepository
import com.funtime.blog.data.repository.CheckinRepository
import com.funtime.blog.data.repository.PassportRepository
import com.funtime.blog.data.repository.ReadingHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val STRAPI_BASE_URL = "http://10.0.2.2:8787"

data class ArticleDetailUiState(
    val isLoading: Boolean = false,
    val article: ArticleDetailDto? = null,
    val relatedArticles: List<ArticleItemDto> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ArticleDetailViewModel @Inject constructor(
    private val repository: ArticleRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val readingHistoryRepository: ReadingHistoryRepository,
    private val checkinRepository: CheckinRepository,
    private val passportRepository: PassportRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val slug: String = checkNotNull(savedStateHandle["slug"])

    private val _uiState = MutableStateFlow(ArticleDetailUiState(isLoading = true))
    val uiState: StateFlow<ArticleDetailUiState> = _uiState.asStateFlow()

    val isBookmarked: StateFlow<Boolean> = bookmarkRepository.isBookmarked(slug)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleBookmark() {
        viewModelScope.launch { bookmarkRepository.toggle(slug) }
    }

    init {
        loadArticle()
    }

    private fun loadArticle() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val article = repository.getArticleBySlug(slug)
                _uiState.update { it.copy(isLoading = false, article = article) }
                loadRelatedArticles(article)
                trackReading(article)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun loadRelatedArticles(article: ArticleDetailDto) {
        val theme = article.theme?.displayName ?: return
        val category = article.category?.displayName ?: return
        viewModelScope.launch {
            try {
                val related = repository.getRelatedArticles(theme, category, article.slug ?: "")
                _uiState.update { it.copy(relatedArticles = related) }
            } catch (_: Exception) {}
        }
    }

    private fun trackReading(article: ArticleDetailDto) {
        val articleSlug = article.slug ?: return
        val title = article.title ?: return
        val rawCoverUrl = article.cover?.url
        val coverUrl = rawCoverUrl?.let {
            if (it.startsWith("http")) it else "$STRAPI_BASE_URL$it"
        }
        viewModelScope.launch {
            val session = authRepository.sessionFlow.first()
            if (session == null) return@launch
            try { readingHistoryRepository.record(slug = articleSlug, title = title, coverUrl = coverUrl) } catch (_: Exception) {}
            try { checkinRepository.addReadingXp() } catch (_: Exception) {}
            val theme = article.theme
            if (theme?.name != null) {
                try { passportRepository.unlockRegionStamp(theme.name, theme.displayName ?: theme.name) } catch (_: Exception) {}
            }
        }
    }

    fun retry() = loadArticle()
}
