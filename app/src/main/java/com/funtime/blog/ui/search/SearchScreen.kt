package com.funtime.blog.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funtime.blog.ui.components.ArticleCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onArticleClick: (slug: String) -> Unit,
    onBack: () -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val pagerState = uiState.pagerState

    val shouldLoadMore by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= listState.layoutInfo.totalItemsCount - 3 &&
                listState.layoutInfo.totalItemsCount > 0
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadMore()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("搜尋") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
            }
        )

        OutlinedTextField(
            value = uiState.query,
            onValueChange = viewModel::updateQuery,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("輸入關鍵字（標籤）") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { viewModel.search() })
        )

        // 搜尋歷史（只在未搜尋且輸入框為空時顯示）
        if (!uiState.hasSearched && uiState.query.isEmpty() && uiState.searchHistory.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("最近搜尋", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = viewModel::clearHistory) { Text("清除") }
            }
            FlowRow(
                modifier = Modifier.padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.searchHistory.forEach { query ->
                    SuggestionChip(
                        onClick = { viewModel.searchFromHistory(query) },
                        label = { Text(query) }
                    )
                }
            }
        }

        when {
            pagerState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            pagerState.error != null && pagerState.items.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("搜尋失敗：${pagerState.error}")
                }
            }
            uiState.hasSearched && pagerState.items.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("沒有找到相關文章")
                }
            }
            !uiState.hasSearched -> {
                if (uiState.searchHistory.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("輸入標籤搜尋文章", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            else -> {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pagerState.items, key = { it.id }) { article ->
                        ArticleCard(article = article, onClick = onArticleClick)
                    }
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                pagerState.isLoadingMore -> CircularProgressIndicator()
                                pagerState.error != null && pagerState.items.isNotEmpty() -> {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        Text(
                                            "載入失敗",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        TextButton(onClick = viewModel::retryLoadMore) { Text("重試") }
                                    }
                                }
                                !pagerState.hasMore && pagerState.items.isNotEmpty() -> Text(
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
}
