package com.funtime.blog.ui.article

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funtime.blog.data.api.dto.ArticleDetailDto
import com.funtime.blog.data.api.dto.ArticleItemDto
import org.json.JSONArray

private const val STRAPI_BASE_URL = "http://10.0.2.2:8787"
private const val WEBSITE_BASE_URL = "https://www.funtime.com.tw/blog"

data class TocItem(val id: String, val text: String, val level: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    onBack: () -> Unit,
    onAuthorClick: (slug: String) -> Unit = {},
    viewModel: ArticleDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isBookmarked by viewModel.isBookmarked.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val webViewRef = remember { mutableStateOf<WebView?>(null) }
    var showShareSheet by remember { mutableStateOf(false) }
    var showTocSheet by remember { mutableStateOf(false) }
    var tocItems by remember { mutableStateOf<List<TocItem>>(emptyList()) }

    // 相關文章有了之後，注入到 WebView
    val relatedArticles = uiState.relatedArticles
    LaunchedEffect(relatedArticles, webViewRef.value) {
        if (relatedArticles.isNotEmpty() && webViewRef.value != null) {
            val html = buildRelatedHtml(relatedArticles)
            val escaped = html.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "")
            webViewRef.value?.evaluateJavascript(
                "document.getElementById('related-placeholder').innerHTML='$escaped'",
                null
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.article?.title ?: "文章", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::toggleBookmark) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isBookmarked) "取消收藏" else "收藏",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else LocalContentColor.current
                        )
                    }
                    if (tocItems.isNotEmpty()) {
                        IconButton(onClick = { showTocSheet = true }) {
                            Icon(Icons.Default.FormatListBulleted, contentDescription = "目錄")
                        }
                    }
                    if (uiState.article != null) {
                        IconButton(onClick = { showShareSheet = true }) {
                            Icon(Icons.Default.Share, contentDescription = "分享")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("載入失敗：${uiState.error}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = viewModel::retry) { Text("重試") }
                    }
                }
            }
            uiState.article != null -> {
                val article = uiState.article!!
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    val url = request?.url?.toString() ?: return false
                                    return when {
                                        url.startsWith("funtime://author/") -> {
                                            onAuthorClick(url.removePrefix("funtime://author/"))
                                            true
                                        }
                                        url.startsWith("funtime://article/") -> {
                                            // handled by NavGraph via recompose - store and navigate
                                            true
                                        }
                                        else -> false
                                    }
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    webViewRef.value = view
                                    // 取目錄
                                    view?.evaluateJavascript(TOC_JS) { json ->
                                        if (json != null && json != "null") {
                                            try {
                                                val arr = JSONArray(json)
                                                tocItems = (0 until arr.length()).map { i ->
                                                    val obj = arr.getJSONObject(i)
                                                    TocItem(
                                                        id = obj.getString("id"),
                                                        text = obj.getString("text"),
                                                        level = obj.getString("level")
                                                    )
                                                }
                                            } catch (_: Exception) {}
                                        }
                                    }
                                    // 包表格
                                    view?.evaluateJavascript(TABLE_WRAP_JS, null)
                                }
                            }
                            settings.javaScriptEnabled = true
                            loadDataWithBaseURL(
                                STRAPI_BASE_URL,
                                buildHtml(article),
                                "text/html", "UTF-8", null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize().padding(innerPadding)
                )
            }
        }
    }

    // 分享 BottomSheet
    if (showShareSheet && uiState.article != null) {
        val article = uiState.article!!
        val articleUrl = "$WEBSITE_BASE_URL/${article.author?.slug}/${article.slug}"
        ModalBottomSheet(onDismissRequest = { showShareSheet = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("分享文章", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))

                ListItem(
                    headlineContent = { Text("分享到 LINE") },
                    modifier = Modifier.clickable {
                        shareToLine(context, articleUrl)
                        showShareSheet = false
                    }
                )
                ListItem(
                    headlineContent = { Text("複製連結") },
                    modifier = Modifier.clickable {
                        copyToClipboard(context, articleUrl)
                        showShareSheet = false
                    }
                )
                ListItem(
                    headlineContent = { Text("其他方式分享") },
                    modifier = Modifier.clickable {
                        shareGeneral(context, article.title ?: "", articleUrl)
                        showShareSheet = false
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // 目錄 BottomSheet
    if (showTocSheet) {
        ModalBottomSheet(onDismissRequest = { showTocSheet = false }) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("文章目錄", fontWeight = FontWeight.Bold, fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp))
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(tocItems) { item ->
                        val indent = if (item.level == "h3") 24.dp else 0.dp
                        Text(
                            text = item.text,
                            fontSize = if (item.level == "h2") 15.sp else 13.sp,
                            color = if (item.level == "h2")
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    webViewRef.value?.evaluateJavascript(
                                        "var el=document.getElementById('${item.id}');if(el)el.scrollIntoView({behavior:'smooth'})",
                                        null
                                    )
                                    showTocSheet = false
                                }
                                .padding(start = indent, top = 12.dp, bottom = 12.dp, end = 8.dp)
                        )
                        HorizontalDivider()
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

// ── HTML 建構 ────────────────────────────────────────────────────────────────

private fun buildHtml(article: ArticleDetailDto): String {
    val authorHtml = article.author?.let { author ->
        if (author.slug != null)
            """<a href="funtime://author/${author.slug}" class="author">${author.name ?: ""}</a>"""
        else
            """<span class="author">${author.name ?: ""}</span>"""
    } ?: ""

    val dateHtml = article.publishedAt?.let {
        """<span class="date">${it.take(10)}</span>"""
    } ?: ""

    val metaLine = listOf(authorHtml, dateHtml).filter { it.isNotEmpty() }.joinToString(" &nbsp;·&nbsp; ")

    return """
        <!DOCTYPE html>
        <html>
        <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <style>
          body { font-family: sans-serif; padding: 16px; margin: 0; line-height: 1.8; color: #333; font-size: 16px; overflow-x: hidden; word-wrap: break-word; }
          h1.article-title { font-size: 22px; font-weight: bold; line-height: 1.4; margin: 0 0 8px 0; }
          .meta { font-size: 13px; color: #888; margin-bottom: 16px; }
          .author { color: #f58900; text-decoration: none; }
          a.author:active { opacity: 0.7; }
          hr { border: none; border-top: 1px solid #eee; margin: 16px 0; }
          img { max-width: 100%; height: auto; border-radius: 4px; }
          .table-wrapper { width: 100%; overflow-x: auto; -webkit-overflow-scrolling: touch; margin: 12px 0; }
          table { border-collapse: collapse; min-width: 100%; }
          td, th { padding: 8px 12px; border: 1px solid #ddd; white-space: nowrap; font-size: 14px; }
          th { background-color: #f5f5f5; font-weight: bold; }
          h2 { font-size: 20px; margin-top: 24px; }
          h3 { font-size: 18px; margin-top: 16px; }
          p { margin: 12px 0; }
          a { color: #f58900; }
          .related-section { margin-top: 32px; padding-top: 16px; border-top: 2px solid #f58900; }
          .related-section h3 { margin-top: 0; color: #333; }
          .related-item { display: block; padding: 10px 0; border-bottom: 1px solid #eee; color: #333; text-decoration: none; font-size: 15px; }
          .related-item:active { opacity: 0.7; }
        </style>
        </head>
        <body>
          <h1 class="article-title">${article.title ?: ""}</h1>
          ${if (metaLine.isNotEmpty()) """<div class="meta">$metaLine</div>""" else ""}
          <hr>
          ${article.content ?: ""}
          <div id="related-placeholder"></div>
        </body>
        </html>
    """.trimIndent()
}

private fun buildRelatedHtml(articles: List<ArticleItemDto>): String {
    val items = articles.joinToString("") { article ->
        """<a href="funtime://article/${article.slug}" class="related-item">${article.title ?: ""}</a>"""
    }
    return """<div class="related-section"><h3>相關文章</h3>$items</div>"""
}

// ── JS 常數 ──────────────────────────────────────────────────────────────────

private const val TOC_JS = """
    (function() {
        var items = [];
        document.querySelectorAll('h2, h3').forEach(function(h, i) {
            if (!h.id) h.id = 'toc-' + i;
            items.push({id: h.id, text: h.textContent.trim(), level: h.tagName.toLowerCase()});
        });
        return JSON.stringify(items);
    })()
"""

private const val TABLE_WRAP_JS = """
    document.querySelectorAll('table').forEach(function(t) {
        if (t.parentElement.className !== 'table-wrapper') {
            var w = document.createElement('div');
            w.className = 'table-wrapper';
            t.parentNode.insertBefore(w, t);
            w.appendChild(t);
        }
    });
"""

// ── 分享工具函式 ──────────────────────────────────────────────────────────────

private fun shareToLine(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, url)
        setPackage("jp.naver.line.android")
    }
    runCatching {
        context.startActivity(intent)
    }.onFailure {
        shareGeneral(context, "", url)
    }
}

private fun copyToClipboard(context: Context, url: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("文章連結", url))
}

private fun shareGeneral(context: Context, title: String, url: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, url)
    }
    context.startActivity(Intent.createChooser(intent, "分享文章"))
}
