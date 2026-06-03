package com.funtime.blog.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funtime.blog.data.api.dto.ArticleItemDto
import com.funtime.blog.ui.common.PaginatedState
import com.funtime.blog.ui.components.ArticleCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onArticleClick: (slug: String) -> Unit,
    onSearchClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("最新", "熱門")

    val latestListState = rememberLazyListState()
    val popularListState = rememberLazyListState()

    val shouldLoadMoreLatest by remember {
        derivedStateOf {
            val last = latestListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= latestListState.layoutInfo.totalItemsCount - 3 &&
                latestListState.layoutInfo.totalItemsCount > 0
        }
    }
    val shouldLoadMorePopular by remember {
        derivedStateOf {
            val last = popularListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= popularListState.layoutInfo.totalItemsCount - 3 &&
                popularListState.layoutInfo.totalItemsCount > 0
        }
    }

    LaunchedEffect(shouldLoadMoreLatest) {
        if (shouldLoadMoreLatest) viewModel.loadMoreLatest()
    }
    LaunchedEffect(shouldLoadMorePopular) {
        if (shouldLoadMorePopular) viewModel.loadMorePopular()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("FunTime 部落格") },
            actions = {
                IconButton(onClick = onSearchClick) {
                    Icon(Icons.Default.Search, contentDescription = "搜尋")
                }
            }
        )

        TabRow(
            selectedTabIndex = selectedTab,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab])
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = {
                        selectedTab = index
                        if (index == 1 && uiState.popularState.items.isEmpty() &&
                            !uiState.popularState.isLoading) {
                            viewModel.loadPopular()
                        }
                    },
                    text = { Text(title) }
                )
            }
        }

        val currentState = if (selectedTab == 0) uiState.latestState else uiState.popularState
        val currentListState = if (selectedTab == 0) latestListState else popularListState
        val onRetry = if (selectedTab == 0) viewModel::retryLatest else viewModel::retryPopular

        ArticleListContent(
            state = currentState,
            listState = currentListState,
            onRetry = onRetry,
            onArticleClick = onArticleClick
        )
    }
}

@Composable
private fun ArticleListContent(
    state: PaginatedState<ArticleItemDto>,
    listState: LazyListState,
    onRetry: () -> Unit,
    onArticleClick: (String) -> Unit
) {
    when {
        state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        state.error != null && state.items.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("載入失敗：${state.error}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onRetry) { Text("重試") }
                }
            }
        }
        else -> {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.items, key = { it.id }) { article ->
                    ArticleCard(article = article, onClick = onArticleClick)
                }
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            state.isLoadingMore -> CircularProgressIndicator()
                            state.error != null && state.items.isNotEmpty() -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Text(
                                        "載入失敗",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    TextButton(onClick = onRetry) { Text("重試") }
                                }
                            }
                            !state.hasMore && state.items.isNotEmpty() -> Text(
                                "已顯示全部文章",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
