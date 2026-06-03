package com.funtime.blog.ui.bookmark

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funtime.blog.data.api.dto.ArticleDetailDto
import com.funtime.blog.data.api.dto.ArticleItemDto
import com.funtime.blog.ui.components.ArticleCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkScreen(
    onArticleClick: (slug: String) -> Unit,
    viewModel: BookmarkViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingDeleteSlug by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("書籤") }) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null && uiState.articles.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(uiState.error!!)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = viewModel::retry) { Text("重試") }
                    }
                }
                uiState.articles.isEmpty() -> {
                    Text(
                        text = "尚未收藏任何文章",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.articles, key = { it.slug ?: it.id.toString() }) { article ->
                            ArticleCard(
                                article = article.toItemDto(),
                                onClick = onArticleClick,
                                onLongClick = { pendingDeleteSlug = article.slug }
                            )
                        }
                    }
                }
            }
        }
    }

    pendingDeleteSlug?.let { slug ->
        AlertDialog(
            onDismissRequest = { pendingDeleteSlug = null },
            title = { Text("移除書籤") },
            text = { Text("確定要從書籤移除這篇文章嗎？") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeBookmark(slug)
                    pendingDeleteSlug = null
                }) { Text("移除") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteSlug = null }) { Text("取消") }
            }
        )
    }
}

private fun ArticleDetailDto.toItemDto() = ArticleItemDto(
    id = id,
    title = title,
    excerpt = excerpt,
    slug = slug,
    content = content,
    publishedAt = publishedAt,
    cover = cover,
    author = author
)
