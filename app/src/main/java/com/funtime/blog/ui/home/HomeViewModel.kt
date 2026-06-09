package com.funtime.blog.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funtime.blog.data.api.dto.ArticleItemDto
import com.funtime.blog.data.repository.ArticleRepository
import com.funtime.blog.ui.common.PaginatedState
import com.funtime.blog.ui.common.PaginationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HomeUiState(
    val latestState: PaginatedState<ArticleItemDto> = PaginatedState(isLoading = true),
    val popularState: PaginatedState<ArticleItemDto> = PaginatedState()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ArticleRepository
) : ViewModel() {

    private val latestPager = PaginationHelper(viewModelScope) { page ->
        val resp = repository.getLatestArticles(page)
        Pair(resp.data, resp.meta.pagination.pageCount)
    }

    private val popularPager = PaginationHelper(viewModelScope) { page ->
        val resp = repository.getPopularArticles(page)
        Pair(resp.data, resp.meta.pagination.pageCount)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        latestPager.state, popularPager.state
    ) { latest, popular ->
        HomeUiState(latestState = latest, popularState = popular)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState(latestState = PaginatedState(isLoading = true)))

    init {
        latestPager.loadFirst()
    }

    fun loadPopular() { popularPager.loadFirst() }
    fun loadMoreLatest() { latestPager.loadMore() }
    fun loadMorePopular() { popularPager.loadMore() }
    fun retryLatest() { latestPager.loadFirst() }
    fun retryPopular() { popularPager.loadFirst() }
}
