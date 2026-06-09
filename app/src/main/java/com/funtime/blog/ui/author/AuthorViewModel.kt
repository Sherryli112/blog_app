package com.funtime.blog.ui.author

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funtime.blog.data.api.dto.ArticleItemDto
import com.funtime.blog.data.api.dto.AuthorDto
import com.funtime.blog.data.repository.ArticleRepository
import com.funtime.blog.ui.common.PaginatedState
import com.funtime.blog.ui.common.PaginationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class AuthorUiState(
    val author: AuthorDto? = null,
    val pagerState: PaginatedState<ArticleItemDto> = PaginatedState(isLoading = true)
)

@HiltViewModel
class AuthorViewModel @Inject constructor(
    private val repository: ArticleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val authorSlug: String = checkNotNull(savedStateHandle["authorSlug"])

    private val pager = PaginationHelper(viewModelScope) { page ->
        val resp = repository.getArticlesByAuthor(authorSlug = authorSlug, page = page)
        Pair(resp.data, resp.meta.pagination.pageCount)
    }

    val uiState: StateFlow<AuthorUiState> = pager.state.map { pagerState ->
        AuthorUiState(
            author = pagerState.items.firstOrNull()?.author,
            pagerState = pagerState
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuthorUiState())

    init { pager.loadFirst() }

    fun loadMore() { pager.loadMore() }
    fun retry() { pager.loadFirst() }
}
