package com.funtime.blog.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funtime.blog.data.api.dto.ArticleItemDto
import com.funtime.blog.data.repository.ArticleRepository
import com.funtime.blog.ui.common.PaginatedState
import com.funtime.blog.ui.common.PaginationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val hasSearched: Boolean = false,
    val pagerState: PaginatedState<ArticleItemDto> = PaginatedState(isLoading = false)
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: ArticleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialKeyword: String = savedStateHandle["keyword"] ?: ""
    private val _query = MutableStateFlow(initialKeyword)
    private val _hasSearched = MutableStateFlow(false)

    private val pager = PaginationHelper(viewModelScope) { page ->
        val resp = repository.searchArticles(query = _query.value.trim(), page = page)
        Pair(resp.data, resp.meta.pagination.pageCount)
    }

    val uiState: StateFlow<SearchUiState> = combine(
        _query, _hasSearched, pager.state
    ) { query, hasSearched, pagerState ->
        SearchUiState(query = query, hasSearched = hasSearched, pagerState = pagerState)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchUiState())

    init {
        if (initialKeyword.isNotBlank()) search()
    }

    fun updateQuery(query: String) { _query.value = query }

    fun search() {
        if (_query.value.trim().isEmpty()) return
        _hasSearched.value = true
        pager.loadFirst()
    }

    fun loadMore() { pager.loadMore() }

    fun retryLoadMore() { pager.loadMore() }
}
