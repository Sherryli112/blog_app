package com.funtime.blog.ui.passport

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funtime.blog.data.local.PassportStampEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class StreakStampDef(val id: String, val emoji: String, val name: String, val desc: String)

private val STREAK_STAMPS = listOf(
    StreakStampDef("streak_7",   "🌙", "夜旅者",   "連續簽到 7 天"),
    StreakStampDef("streak_30",  "✈️", "空橋旅人", "連續簽到 30 天"),
    StreakStampDef("streak_100", "🌍", "環球旅人", "連續簽到 100 天"),
)

private fun regionEmoji(themeKey: String): String = when (themeKey.lowercase()) {
    "japan"                     -> "🇯🇵"
    "korea"                     -> "🇰🇷"
    "thailand"                  -> "🇹🇭"
    "taiwan"                    -> "🇹🇼"
    "singapore"                 -> "🇸🇬"
    "vietnam"                   -> "🇻🇳"
    "malaysia"                  -> "🇲🇾"
    "indonesia"                 -> "🇮🇩"
    "philippines"               -> "🇵🇭"
    "hong_kong", "hongkong"     -> "🇭🇰"
    "china"                     -> "🇨🇳"
    "france"                    -> "🇫🇷"
    "italy"                     -> "🇮🇹"
    "europe"                    -> "🇪🇺"
    "usa", "america"            -> "🇺🇸"
    "australia"                 -> "🇦🇺"
    else                        -> "🌏"
}

private fun formatDate(ms: Long): String =
    SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(ms))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassportScreen(
    onBack: () -> Unit,
    viewModel: PassportViewModel = hiltViewModel()
) {
    val stamps by viewModel.stamps.collectAsStateWithLifecycle()
    val earnedIds = remember(stamps) { stamps.map { it.id }.toSet() }
    val regionStamps = remember(stamps) { stamps.filter { it.type == "region" } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的護照") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            // 稀有章
            item {
                SectionTitle("稀有章（連續簽到里程碑）")
            }
            items(STREAK_STAMPS) { def ->
                val earned = earnedIds.contains(def.id)
                val stamp = stamps.find { it.id == def.id }
                StreakStampCard(def = def, earned = earned, earnedAt = stamp?.earnedAt)
            }

            item { Spacer(Modifier.height(8.dp)) }

            // 地區章
            item {
                SectionTitle("地區章（閱讀文章自動收集）")
            }
            if (regionStamps.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "閱讀文章後將自動收集地區章",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(regionStamps) { stamp ->
                    RegionStampCard(stamp = stamp)
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun StreakStampCard(def: StreakStampDef, earned: Boolean, earnedAt: Long?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (earned) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(def.emoji, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    def.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (earned) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    def.desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (earned) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (earned && earnedAt != null) {
                Text(
                    formatDate(earnedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else if (!earned) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RegionStampCard(stamp: PassportStampEntity) {
    val themeKey = stamp.id.removePrefix("region_")
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(regionEmoji(themeKey), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.width(12.dp))
            Text(
                stamp.displayName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
            )
            Text(
                formatDate(stamp.earnedAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
