package com.funtime.blog.ui.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * @param fetch Called with the requested page number (1-indexed).
 *   Returns Pair(items, totalPageCount). hasMore = currentPage < totalPageCount.
 */
class PaginationHelper<T>(
    private val scope: CoroutineScope,
    private val fetch: suspend (page: Int) -> Pair<List<T>, Int>
) {
    @Volatile private var currentPage = 0
    private var loadJob: Job? = null
    private var loadMoreJob: Job? = null

    private val _state = MutableStateFlow(PaginatedState<T>())
    val state: StateFlow<PaginatedState<T>> = _state.asStateFlow()

    fun loadFirst() {
        loadJob?.cancel()
        loadMoreJob?.cancel()
        currentPage = 0
        _state.value = PaginatedState(isLoading = true)
        loadJob = scope.launch {
            try {
                val (items, pageCount) = fetch(1)
                currentPage = 1
                _state.value = PaginatedState(
                    items = items,
                    isLoading = false,
                    hasMore = currentPage < pageCount
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = PaginatedState(isLoading = false, error = e.message)
            }
        }
    }

    fun loadMore() {
        val snapshot = _state.value
        if (!snapshot.hasMore || snapshot.isLoading || snapshot.isLoadingMore) return
        loadMoreJob?.cancel()
        _state.update { it.copy(isLoadingMore = true) }
        loadMoreJob = scope.launch {
            try {
                val nextPage = currentPage + 1
                val (items, pageCount) = fetch(nextPage)
                currentPage = nextPage
                _state.update { current ->
                    current.copy(
                        items = current.items + items,
                        isLoadingMore = false,
                        hasMore = currentPage < pageCount,
                        error = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoadingMore = false, error = e.message) }
            }
        }
    }
}
