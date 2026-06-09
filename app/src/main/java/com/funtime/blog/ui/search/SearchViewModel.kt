package com.funtime.blog.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funtime.blog.data.api.dto.ArticleItemDto
import com.funtime.blog.data.repository.ArticleRepository
import com.funtime.blog.data.repository.SearchHistoryRepository
import com.funtime.blog.ui.common.PaginatedState
import com.funtime.blog.ui.common.PaginationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val hasSearched: Boolean = false,
    val pagerState: PaginatedState<ArticleItemDto> = PaginatedState(isLoading = false),
    val searchHistory: List<String> = emptyList()
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: ArticleRepository,
    private val historyRepository: SearchHistoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialKeyword: String = savedStateHandle["keyword"] ?: ""
    private val _query = MutableStateFlow(initialKeyword)
    private val _hasSearched = MutableStateFlow(false)

    private val pager = PaginationHelper(viewModelScope) { page ->
        val resp = repository.searchArticles(tag = _query.value.trim(), page = page)
        Pair(resp.data, resp.meta.pagination.pageCount)
    }

    val uiState: StateFlow<SearchUiState> = combine(
        _query, _hasSearched, pager.state, historyRepository.recentSearches
    ) { query, hasSearched, pagerState, history ->
        SearchUiState(query = query, hasSearched = hasSearched, pagerState = pagerState, searchHistory = history)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchUiState())

    init {
        if (initialKeyword.isNotBlank()) search()
    }

    fun updateQuery(query: String) { _query.value = query }

    fun search() {
        if (_query.value.trim().isEmpty()) return
        _hasSearched.value = true
        viewModelScope.launch { historyRepository.saveSearch(_query.value) }
        pager.loadFirst()
    }

    fun searchFromHistory(query: String) {
        _query.value = query
        search()
    }

    fun clearHistory() {
        viewModelScope.launch { historyRepository.clearAll() }
    }

    fun loadMore() { pager.loadMore() }

    fun retryLoadMore() { pager.loadMore() }
}
