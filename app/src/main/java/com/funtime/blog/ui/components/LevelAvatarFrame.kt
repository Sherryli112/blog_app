package com.funtime.blog.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── 等級對應色系 ──────────────────────────────────────────────
private fun levelGradientColors(level: Int): List<Color> = when (level) {
    1 -> listOf(Color(0xFFBDBDBD), Color(0xFF9E9E9E), Color(0xFFBDBDBD)) // 灰（新手）
    2 -> listOf(Color(0xFFCD7F32), Color(0xFFE8A44A), Color(0xFFCD7F32)) // 銅
    3 -> listOf(Color(0xFFB0BEC5), Color(0xFFECEFF1), Color(0xFFB0BEC5)) // 銀
    4 -> listOf(Color(0xFFFFD700), Color(0xFFFFF176), Color(0xFFFFD700)) // 金
    else -> listOf(Color(0xFF7C4DFF), Color(0xFF40C4FF), Color(0xFF7C4DFF)) // 寶石（環球旅人）
}

private fun levelBorderColor(level: Int): Color = when (level) {
    1 -> Color(0xFFBDBDBD)
    2 -> Color(0xFFCD7F32)
    3 -> Color(0xFFB0BEC5)
    4 -> Color(0xFFFFD700)
    else -> Color(0xFF7C4DFF)
}

private fun levelStarCount(level: Int) = level.coerceIn(1, 5)

// ── Style A：圓形漸層邊框 ──────────────────────────────────────
/**
 * 風格 A：依等級顯示不同顏色的 sweep gradient 外圈。
 * 新手=灰、銅=鋸齒熱情、銀=冷靜、金=輝煌、環球=寶石藍紫。
 */
@Composable
fun GradientAvatarFrame(
    username: String,
    level: Int,
    size: Dp = 64.dp,
    borderWidth: Dp = 3.dp,
) {
    val colors = levelGradientColors(level)

    Box(
        modifier = Modifier
            .size(size)
            .drawWithContent {
                val borderPx = borderWidth.toPx()
                drawContent()
                drawCircle(
                    brush = Brush.sweepGradient(colors),
                    radius = this.size.minDimension / 2f - borderPx / 2f,
                    style = Stroke(width = borderPx * 3f)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size - borderWidth * 4)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = username.take(1).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.35f).sp
            )
        }
    }
}

// ── Style B：圓形邊框 + 角標（星數）─────────────────────────────
/**
 * 風格 B：單色實心外框 + 右下角星星角標，類似遊戲頭貼。
 * 等級數 = 星星數（1–5 顆），角標底色與邊框同色系。
 */
@Composable
fun BadgeAvatarFrame(
    username: String,
    level: Int,
    size: Dp = 64.dp,
    borderWidth: Dp = 3.dp,
) {
    val borderColor = levelBorderColor(level)
    val starCount = levelStarCount(level)
    val badgeSize = size * 0.38f
    val iconSize = badgeSize * 0.55f

    Box(
        modifier = Modifier.size(size + badgeSize / 2),
    ) {
        // 主頭像圓形 + 實心邊框
        Box(
            modifier = Modifier
                .size(size)
                .align(Alignment.TopStart)
                .drawWithContent {
                    drawContent()
                    drawCircle(
                        color = borderColor,
                        radius = this.size.minDimension / 2f - borderWidth.toPx() / 2f,
                        style = Stroke(width = borderWidth.toPx() * 3f)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(size - borderWidth * 4)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = username.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.35f).sp
                )
            }
        }

        // 右下角星星角標
        Box(
            modifier = Modifier
                .size(badgeSize)
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
                .background(borderColor),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(starCount.coerceAtMost(3)) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(iconSize / starCount.coerceAtMost(3).toFloat().coerceAtLeast(1f) * 1.2f)
                    )
                }
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────
@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
private fun PreviewGradientFrames() {
    MaterialTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            (1..5).forEach { lv ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    GradientAvatarFrame(username = "王", level = lv, size = 56.dp)
                    Spacer(Modifier.height(4.dp))
                    Text("Lv$lv", fontSize = 10.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
private fun PreviewBadgeFrames() {
    MaterialTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            (1..5).forEach { lv ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BadgeAvatarFrame(username = "王", level = lv, size = 56.dp)
                    Spacer(Modifier.height(4.dp))
                    Text("Lv$lv", fontSize = 10.sp)
                }
            }
        }
    }
}
