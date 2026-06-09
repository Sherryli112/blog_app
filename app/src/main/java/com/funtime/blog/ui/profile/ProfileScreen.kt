package com.funtime.blog.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.funtime.blog.data.local.ReadingHistoryEntity
import com.funtime.blog.data.local.UserStatsEntity
import com.funtime.blog.data.repository.CheckinResult
import com.funtime.blog.data.repository.PassportRepository
import com.funtime.blog.data.repository.checkinXpForStreak
import com.funtime.blog.ui.components.HoKAvatarFrame

private fun levelName(level: Int) = when (level) {
    1 -> "旅行新手"; 2 -> "旅遊愛好者"; 3 -> "城市探索者"; 4 -> "亞洲達人"; 5 -> "環球旅人"; else -> "旅行新手"
}

private fun xpForLevel(level: Int) = when (level) {
    1 -> 0; 2 -> 1000; 3 -> 5000; 4 -> 15000; 5 -> 40000; else -> 0
}

private fun xpForNextLevel(level: Int): Int? = when (level) {
    1 -> 1000; 2 -> 5000; 3 -> 15000; 4 -> 40000; else -> null
}


@Composable
fun ProfileScreen(
    onLoginClick: () -> Unit,
    onArticleClick: (slug: String) -> Unit,
    onPassportClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val recentHistory by viewModel.recentHistory.collectAsStateWithLifecycle()
    val hasCheckedInToday by viewModel.hasCheckedInToday.collectAsStateWithLifecycle()
    val hasReadToday by viewModel.hasReadToday.collectAsStateWithLifecycle()
    val checkinResult by viewModel.checkinResult.collectAsStateWithLifecycle()

    val isLoggedIn = session != null
    val currentStats = stats  // 保持 nullable，null 期間不渲染卡片，避免 0 值閃爍

    if (isLoggedIn) checkinResult?.let { result ->
        if (result is CheckinResult.Success) {
            CheckinSuccessDialog(
                streak = result.streak,
                xpEarned = result.xpEarned,
                onDismiss = viewModel::dismissCheckinResult
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 頁面標題
        item {
            Text(
                "我的",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        // 登入狀態 header
        item {
            if (isLoggedIn) {
                LoggedInHeader(
                    username = session!!.username,
                    level = currentStats?.level ?: 1,
                    onLogout = viewModel::logout
                )
            } else {
                GuestBanner(onLoginClick = onLoginClick)
            }
        }

        // XP 卡、任務、簽到：登入就顯示，stats 為 null（從未簽到）時用預設值，避免死結
        if (isLoggedIn) {
            val displayStats = currentStats ?: UserStatsEntity()
            item {
                XpCard(stats = displayStats, modifier = Modifier.padding(horizontal = 16.dp))
            }
            item {
                XpTasksCard(
                    hasCheckedInToday = hasCheckedInToday,
                    hasReadToday = hasReadToday,
                    currentStreak = displayStats.currentStreak,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            item {
                StreakCheckinRow(
                    stats = displayStats,
                    hasCheckedInToday = hasCheckedInToday,
                    onCheckin = viewModel::checkin,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // 護照入口（登入後才顯示）
        if (isLoggedIn) {
            item {
                PassportEntryCard(onClick = onPassportClick, modifier = Modifier.padding(horizontal = 16.dp))
            }
        }

        // 最近閱讀（僅登入後顯示，避免前一用戶的閱讀記錄外洩）
        if (isLoggedIn) {
            item {
                Text(
                    "最近閱讀",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
                )
            }
            if (recentHistory.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "還沒有閱讀記錄，快去看幾篇文章吧！",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(recentHistory) { article ->
                    HistoryItem(article = article, onClick = { onArticleClick(article.slug) })
                }
            }
        }
    }
}

@Composable
private fun GuestBanner(onLoginClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "登入以解鎖簽到功能",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "登入後可每日簽到累積 XP",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            Button(onClick = onLoginClick) {
                Text("登入")
            }
        }
    }
}

@Composable
private fun XpTasksCard(
    hasCheckedInToday: Boolean,
    hasReadToday: Boolean,
    currentStreak: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "今日任務",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            TaskRow(
                icon = "🔥",
                label = "每日簽到",
                xp = "+${checkinXpForStreak(currentStreak + 1)} XP",
                done = hasCheckedInToday
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            )
            TaskRow(
                icon = "📖",
                label = "閱讀一篇文章",
                xp = "+20 XP",
                done = hasReadToday
            )
        }
    }
}

@Composable
private fun TaskRow(icon: String, label: String, xp: String, done: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.width(10.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        if (done) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                "已完成",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        } else {
            Text(
                xp,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LoggedInHeader(username: String, level: Int, onLogout: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HoKAvatarFrame(username = username, level = level, size = 56.dp)
        Spacer(Modifier.width(12.dp))
        Text(
            username,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onLogout) {
            Text("登出", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun XpCard(stats: UserStatsEntity, modifier: Modifier = Modifier) {
    val level = stats.level
    val xp = stats.totalXp
    val nextXp = xpForNextLevel(level)
    val progress = if (nextXp != null) {
        (xp - xpForLevel(level)).toFloat() / (nextXp - xpForLevel(level)).toFloat()
    } else 1f

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Lv.$level · ${levelName(level)}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    if (nextXp != null) "$xp / $nextXp XP" else "MAX LEVEL",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
private fun StreakCheckinRow(
    stats: UserStatsEntity,
    hasCheckedInToday: Boolean,
    onCheckin: () -> Unit,
    modifier: Modifier = Modifier
) {
    // IntrinsicSize.Max 讓左右兩塊高度自動對齊（以較高的一塊為準）
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 連續天數卡片
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = Color(0xFFFF5722),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            "${stats.currentStreak} 天",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "連續簽到",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 右側：已簽到 / 簽到按鈕
            when {
                hasCheckedInToday -> Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "今日已簽到",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                else -> Button(
                    onClick = onCheckin,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("每日簽到")
                        Text(
                            "+${checkinXpForStreak(stats.currentStreak + 1)} XP",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }

    }
}

@Composable
private fun PassportEntryCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🗺️", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "我的護照",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "查看已收集的地區章與稀有章",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun HistoryItem(article: ReadingHistoryEntity, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (!article.coverUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = article.coverUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            article.title,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CheckinSuccessDialog(streak: Int, xpEarned: Int, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        },
        title = { Text("簽到成功！", textAlign = TextAlign.Center) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "+$xpEarned XP",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "連續簽到 $streak 天",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (streak in PassportRepository.MILESTONE_DAYS) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "🎉 解鎖稀有護照章！",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("繼續") }
        }
    )
}
