package com.funtime.blog.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

// ─── 色系定義 ────────────────────────────────────────────────────
private data class FrameTheme(
    val ring1: Color,   // 主環顏色 A
    val ring2: Color,   // 主環顏色 B（漸層）
    val accent: Color,  // 裝飾高光
    val glow: Color,    // 發光顏色
    val glowRadius: Float, // 1.0 = 標準發光強度
)

private fun frameTheme(level: Int) = when (level) {
    1 -> FrameTheme(Color(0xFF78909C), Color(0xFF37474F), Color(0xFFB0BEC5), Color(0xFF607D8B), 0.4f) // 鋼鐵灰
    2 -> FrameTheme(Color(0xFFCD853F), Color(0xFF8B4513), Color(0xFFFFD54F), Color(0xFFBF8040), 0.5f) // 青銅
    3 -> FrameTheme(Color(0xFFCFD8DC), Color(0xFF90A4AE), Color(0xFFFFFFFF), Color(0xFFB0BEC5), 0.55f) // 白銀
    4 -> FrameTheme(Color(0xFFFFD740), Color(0xFFFF8F00), Color(0xFFFFFFFF), Color(0xFFFFD740), 0.75f) // 黃金
    else -> FrameTheme(Color(0xFFFF6D00), Color(0xFFC62828), Color(0xFFFFD740), Color(0xFFFF6D00), 1.0f) // 傳奇
}

// ─── Canvas 輔助函式 ──────────────────────────────────────────────

private fun DrawScope.drawGlow(cx: Float, cy: Float, radius: Float, color: Color, blur: Float) {
    drawIntoCanvas { canvas ->
        canvas.nativeCanvas.drawCircle(
            cx, cy, radius,
            android.graphics.Paint().apply {
                isAntiAlias = true
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = blur * 0.6f
                this.color = color.toArgb()
                maskFilter = android.graphics.BlurMaskFilter(blur, android.graphics.BlurMaskFilter.Blur.NORMAL)
            }
        )
    }
}

private fun DrawScope.drawDiamond(cx: Float, cy: Float, size: Float, color: Color) {
    val path = Path().apply {
        moveTo(cx, cy - size)
        lineTo(cx + size * 0.55f, cy)
        lineTo(cx, cy + size)
        lineTo(cx - size * 0.55f, cy)
        close()
    }
    drawPath(path, color = color)
    drawPath(path, color = Color.White.copy(alpha = 0.3f), style = Stroke(width = 1.dp.toPx()))
}

// LoL 三峰皇冠
private fun DrawScope.drawLoLCrown(cx: Float, topY: Float, w: Float, h: Float, color: Color, accent: Color) {
    val path = Path().apply {
        moveTo(cx - w / 2f, topY)
        lineTo(cx - w / 2f, topY - h * 0.52f)
        lineTo(cx - w / 6f, topY - h * 0.28f)
        lineTo(cx, topY - h)
        lineTo(cx + w / 6f, topY - h * 0.28f)
        lineTo(cx + w / 2f, topY - h * 0.52f)
        lineTo(cx + w / 2f, topY)
        close()
    }
    drawPath(path, brush = Brush.verticalGradient(listOf(accent, color), startY = topY - h, endY = topY))
    drawPath(path, color = accent.copy(alpha = 0.6f), style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
}

// 王者榮耀風翅膀（從頂部向兩側展開的弧形）
private fun DrawScope.drawHoKWings(cx: Float, topY: Float, mainR: Float, theme: FrameTheme) {
    val wingSpan = mainR * 0.62f
    val wingH = mainR * 0.28f

    listOf(-1f, 1f).forEach { side ->
        val wingPath = Path().apply {
            moveTo(cx + side * mainR * 0.25f, topY)
            cubicTo(
                cx + side * mainR * 0.45f, topY - wingH * 0.5f,
                cx + side * wingSpan * 0.8f, topY - wingH * 0.8f,
                cx + side * wingSpan, topY - wingH * 0.2f
            )
        }
        drawPath(
            wingPath,
            brush = Brush.linearGradient(
                listOf(theme.ring1, theme.accent.copy(alpha = 0.7f)),
                start = Offset(cx, topY),
                end = Offset(cx + side * wingSpan, topY - wingH)
            ),
            style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round)
        )
    }

    // 翅膀末端小圓點
    listOf(-1f, 1f).forEach { side ->
        drawCircle(
            color = theme.accent,
            radius = 3.5.dp.toPx(),
            center = Offset(cx + side * wingSpan, topY - wingH * 0.2f)
        )
    }
}

// 王者榮耀風頂部中央寶石
private fun DrawScope.drawHoKTopGem(cx: Float, topY: Float, mainR: Float, theme: FrameTheme) {
    val gemH = mainR * 0.34f
    val gemW = mainR * 0.22f

    // 外框尖形
    val outerPath = Path().apply {
        moveTo(cx, topY - gemH)
        lineTo(cx + gemW, topY - gemH * 0.35f)
        lineTo(cx + gemW * 0.6f, topY)
        lineTo(cx - gemW * 0.6f, topY)
        lineTo(cx - gemW, topY - gemH * 0.35f)
        close()
    }
    drawPath(outerPath, brush = Brush.verticalGradient(
        listOf(theme.accent, theme.ring1),
        startY = topY - gemH, endY = topY
    ))

    // 內層高光三角
    val innerPath = Path().apply {
        moveTo(cx, topY - gemH * 0.8f)
        lineTo(cx + gemW * 0.4f, topY - gemH * 0.35f)
        lineTo(cx - gemW * 0.4f, topY - gemH * 0.35f)
        close()
    }
    drawPath(innerPath, color = Color.White.copy(alpha = 0.35f))

    // 輪廓線
    drawPath(outerPath, color = theme.accent, style = Stroke(width = 1.dp.toPx()))
}

// ─── Style C：LoL 幾何段位框 ─────────────────────────────────────
@Composable
fun LoLAvatarFrame(
    username: String,
    level: Int,
    size: Dp = 80.dp,
) {
    val theme = frameTheme(level)
    val padding = size * 0.28f
    val total = size + padding * 2

    Box(modifier = Modifier.size(total), contentAlignment = Alignment.Center) {
        // 頭像
        Box(
            modifier = Modifier.size(size).clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                username.take(1).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.35f).sp
            )
        }

        Canvas(modifier = Modifier.size(total)) {
            val cx = this.size.width / 2
            val cy = this.size.height / 2
            val avatarR = size.toPx() / 2
            val gap = padding.toPx()

            val innerR = avatarR + gap * 0.2f
            val mainR  = avatarR + gap * 0.52f
            val outerR = avatarR + gap * 0.78f
            val tickR  = avatarR + gap * 0.90f

            // 發光
            drawGlow(cx, cy, mainR, theme.glow, gap * theme.glowRadius * 0.7f)

            // 外細環
            drawCircle(color = theme.ring1.copy(alpha = 0.5f), radius = outerR,
                center = Offset(cx, cy), style = Stroke(width = 1.dp.toPx()))

            // 主環（sweep gradient）
            drawCircle(
                brush = Brush.sweepGradient(
                    listOf(theme.ring1, theme.ring2, theme.ring1, theme.ring2, theme.ring1),
                    center = Offset(cx, cy)
                ),
                radius = mainR, center = Offset(cx, cy),
                style = Stroke(width = 4.dp.toPx())
            )

            // 內細環
            drawCircle(color = theme.accent.copy(alpha = 0.6f), radius = innerR,
                center = Offset(cx, cy), style = Stroke(width = 1.dp.toPx()))

            // 8 個刻度線（4 長 + 4 短）
            for (i in 0 until 8) {
                val angleDeg = i * 45.0
                val rad = Math.toRadians(angleDeg)
                val isCardinal = i % 2 == 0
                val r1 = if (isCardinal) outerR - 1.dp.toPx() else outerR
                val r2 = if (isCardinal) tickR + 3.dp.toPx() else tickR
                drawLine(
                    color = theme.ring1,
                    start = Offset(cx + cos(rad).toFloat() * r1, cy + sin(rad).toFloat() * r1),
                    end   = Offset(cx + cos(rad).toFloat() * r2, cy + sin(rad).toFloat() * r2),
                    strokeWidth = if (isCardinal) 2.dp.toPx() else 1.dp.toPx()
                )
            }

            // 4 個對角菱形
            for (i in 0 until 4) {
                val rad = Math.toRadians(45.0 + i * 90.0)
                val dx = cos(rad).toFloat() * mainR
                val dy = sin(rad).toFloat() * mainR
                drawDiamond(cx + dx, cy + dy, 4.dp.toPx(), theme.accent)
            }

            // 頂部三峰皇冠（12 點鐘）
            val crownW = gap * 0.7f
            val crownH = gap * 0.5f
            drawLoLCrown(cx, cy - mainR - 1.dp.toPx(), crownW, crownH, theme.ring1, theme.accent)

            // 底部等級文字（簡潔）
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawText(
                    "LV $level",
                    cx,
                    cy + outerR + gap * 0.3f,
                    android.graphics.Paint().apply {
                        isAntiAlias = true
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = 9.dp.toPx()
                        color = theme.ring1.toArgb()
                        isFakeBoldText = true
                    }
                )
            }
        }
    }
}

// ─── Style D：王者榮耀 華麗寶石框 ───────────────────────────────
@Composable
fun HoKAvatarFrame(
    username: String,
    level: Int,
    size: Dp = 80.dp,
) {
    val theme = frameTheme(level)
    val padding = size * 0.36f
    val total = size + padding * 2

    // 傳奇等級旋轉動畫（level < 5 時值為 0，不影響繪製）
    val infiniteTransition = rememberInfiniteTransition(label = "hok_rotate")
    val animatedAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing)),
        label = "rotate"
    )
    val rotateAngle = if (level >= 5) animatedAngle else 0f

    Box(modifier = Modifier.size(total), contentAlignment = Alignment.Center) {
        // 頭像
        Box(
            modifier = Modifier.size(size).clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                username.take(1).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.35f).sp
            )
        }

        Canvas(modifier = Modifier.size(total)) {
            val cx = this.size.width / 2
            val cy = this.size.height / 2
            val avatarR = size.toPx() / 2
            val gap = padding.toPx()
            val mainR = avatarR + gap * 0.45f

            // 強發光
            drawGlow(cx, cy, mainR, theme.glow, gap * theme.glowRadius * 0.9f)
            if (level >= 4) {
                drawGlow(cx, cy, mainR, theme.accent.copy(alpha = 0.4f), gap * 0.25f)
            }

            // 傳奇等級：旋轉漸層環底層
            if (level >= 5) {
                rotate(rotateAngle, pivot = Offset(cx, cy)) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            listOf(Color.Transparent, theme.accent.copy(0.5f), Color.Transparent,
                                theme.ring2.copy(0.4f), Color.Transparent),
                            center = Offset(cx, cy)
                        ),
                        radius = mainR + gap * 0.12f,
                        center = Offset(cx, cy),
                        style = Stroke(width = 6.dp.toPx())
                    )
                }
            }

            // 主環（厚，雙色漸層）
            drawCircle(
                brush = Brush.sweepGradient(
                    listOf(theme.ring2, theme.ring1, theme.accent.copy(alpha = 0.9f), theme.ring1, theme.ring2),
                    center = Offset(cx, cy)
                ),
                radius = mainR,
                center = Offset(cx, cy),
                style = Stroke(width = 5.5.dp.toPx())
            )

            // 內細線環
            drawCircle(
                color = theme.accent.copy(alpha = 0.7f),
                radius = avatarR + gap * 0.1f,
                center = Offset(cx, cy),
                style = Stroke(width = 1.dp.toPx())
            )

            // 4 個斜角長菱形（突出主環外）
            for (i in 0 until 4) {
                val angleDeg = 45.0 + i * 90.0
                val rad = Math.toRadians(angleDeg)
                val spikeCx = cx + cos(rad).toFloat() * (mainR + gap * 0.18f)
                val spikeCy = cy + sin(rad).toFloat() * (mainR + gap * 0.18f)

                rotate(angleDeg.toFloat(), pivot = Offset(spikeCx, spikeCy)) {
                    drawDiamond(spikeCx, spikeCy, gap * 0.18f, theme.ring1)
                    drawDiamond(spikeCx, spikeCy, gap * 0.10f, theme.accent)
                }
            }

            // 左右 3/9 點鐘位置：圓形側寶石（Lv3 以上）
            if (level >= 3) {
                listOf(-1f, 1f).forEach { side ->
                    val gemCx = cx + side * (mainR + gap * 0.05f)
                    drawCircle(color = theme.ring1, radius = 4.5.dp.toPx(), center = Offset(gemCx, cy))
                    drawCircle(color = theme.accent, radius = 2.dp.toPx(), center = Offset(gemCx, cy))
                }
            }

            // 頂部翅膀（Lv2 以上）
            if (level >= 2) {
                drawHoKWings(cx, cy - mainR, mainR, theme)
            }

            // 頂部中央寶石
            drawHoKTopGem(cx, cy - mainR + 1.dp.toPx(), mainR, theme)

            // 底部小裝飾
            val botY = cy + mainR
            drawDiamond(cx, botY + gap * 0.16f, gap * 0.10f, theme.ring1)
            drawLine(
                color = theme.ring1.copy(alpha = 0.7f),
                start = Offset(cx - gap * 0.22f, botY + gap * 0.06f),
                end   = Offset(cx + gap * 0.22f, botY + gap * 0.06f),
                strokeWidth = 1.2.dp.toPx()
            )
        }
    }
}

// ─── Preview ──────────────────────────────────────────────────────
@Preview(showBackground = true, backgroundColor = 0xFF1A1A2E)
@Composable
private fun PreviewLoLFrames() {
    MaterialTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            (1..5).forEach { lv ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LoLAvatarFrame(username = "王", level = lv, size = 52.dp)
                    Spacer(Modifier.height(4.dp))
                    Text("Lv$lv", fontSize = 9.sp, color = Color.White)
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A2E)
@Composable
private fun PreviewHoKFrames() {
    MaterialTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            (1..5).forEach { lv ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    HoKAvatarFrame(username = "王", level = lv, size = 52.dp)
                    Spacer(Modifier.height(4.dp))
                    Text("Lv$lv", fontSize = 9.sp, color = Color.White)
                }
            }
        }
    }
}
