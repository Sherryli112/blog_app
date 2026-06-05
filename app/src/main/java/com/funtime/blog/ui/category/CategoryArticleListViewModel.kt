package com.funtime.blog.ui.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funtime.blog.data.api.dto.ArticleItemDto
import com.funtime.blog.data.repository.ArticleRepository
import com.funtime.blog.ui.common.PaginatedState
import com.funtime.blog.ui.common.PaginationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class CategoryArticleListUiState(
    val theme: String = "",
    val category: String = "",
    val pagerState: PaginatedState<ArticleItemDto> = PaginatedState(isLoading = true)
)

@HiltViewModel
class CategoryArticleListViewModel @Inject constructor(
    private val repository: ArticleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val theme: String = checkNotNull(savedStateHandle["theme"])
    private val category: String = checkNotNull(savedStateHandle["category"])

    private val pager = PaginationHelper(viewModelScope) { page ->
        val resp = repository.getArticlesByCategory(theme = theme, category = category, page = page)
        Pair(resp.data, resp.meta.pagination.pageCount)
    }

    val uiState: StateFlow<CategoryArticleListUiState> = pager.state.map { pagerState ->
        CategoryArticleListUiState(theme = theme, category = category, pagerState = pagerState)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        CategoryArticleListUiState(theme = theme, category = category, pagerState = PaginatedState(isLoading = true))
    )

    init { pager.loadFirst() }

    fun loadMore() { pager.loadMore() }
    fun retry() { pager.loadFirst() }
}
