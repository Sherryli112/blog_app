package com.funtime.blog.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.funtime.blog.data.api.dto.ArticleItemDto

private const val STRAPI_BASE_URL = "http://10.0.2.2:8787"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArticleCard(
    article: ArticleItemDto,
    onClick: (slug: String) -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: ((String) -> Unit)? = null
) {
    val coverUrl = article.cover?.url?.let {
        if (it.startsWith("http")) it else "$STRAPI_BASE_URL$it"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { article.slug?.let(onClick) },
                onLongClick = { article.slug?.let { s -> onLongClick?.invoke(s) } }
            ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            coverUrl?.let {
                AsyncImage(
                    model = it,
                    contentDescription = article.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = article.title ?: "",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = article.excerpt ?: "",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                article.author?.name?.let { authorName ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = authorName,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
