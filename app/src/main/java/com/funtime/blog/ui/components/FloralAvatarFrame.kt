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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

// ─── 配色（C 方案橙色系）────────────────────────────────────────────
private data class FloralTheme(
    val ringColor: Color,
    val petalColor: Color,
    val petalAccent: Color,
    val leafColor: Color,
    val centerColor: Color,
    val glowAlpha: Float,
)

private fun floralTheme(level: Int) = when (level) {
    1    -> FloralTheme(Color(0xFF8B6040), Color(0xFFD4A574), Color(0xFFEBBF8A), Color(0xFF7A8840), Color(0xFFFFF0D0), 0f)
    2    -> FloralTheme(Color(0xFF7A4010), Color(0xFFE0892A), Color(0xFFF5C070), Color(0xFF5A7030), Color(0xFFFFE0A0), 0.25f)
    3    -> FloralTheme(Color(0xFF6B3010), Color(0xFFF58900), Color(0xFFFFCC44), Color(0xFF4A6020), Color(0xFFFFD080), 0.45f)
    4    -> FloralTheme(Color(0xFF5A2800), Color(0xFFF58900), Color(0xFFFFD740), Color(0xFF3A5010), Color(0xFFFFE044), 0.65f)
    else -> FloralTheme(Color(0xFF4A2000), Color(0xFFFF5200), Color(0xFFF58900), Color(0xFF305010), Color(0xFFFFCC00), 0.85f)
}

// ─── Canvas 輔助函式 ──────────────────────────────────────────────────

private fun DrawScope.drawPetal(cx: Float, cy: Float, size: Float, angleDeg: Float, color: Color) {
    val rad = Math.toRadians(angleDeg.toDouble())
    val tipX = cx + cos(rad).toFloat() * size
    val tipY = cy + sin(rad).toFloat() * size
    val perpRad = rad + PI / 2.0
    val w = size * 0.38f
    val path = Path().apply {
        moveTo(cx, cy)
        cubicTo(
            cx + cos(perpRad).toFloat() * w, cy + sin(perpRad).toFloat() * w,
            tipX + cos(perpRad).toFloat() * w * 0.4f, tipY + sin(perpRad).toFloat() * w * 0.4f,
            tipX, tipY
        )
        cubicTo(
            tipX - cos(perpRad).toFloat() * w * 0.4f, tipY - sin(perpRad).toFloat() * w * 0.4f,
            cx - cos(perpRad).toFloat() * w, cy - sin(perpRad).toFloat() * w,
            cx, cy
        )
        close()
    }
    drawPath(path, color = color)
}

private fun DrawScope.drawFlower(
    cx: Float, cy: Float, size: Float,
    petalColor: Color, centerColor: Color, count: Int = 5
) {
    repeat(count) { i ->
        val angle = i * 360f / count - 90f
        drawPetal(cx, cy, size, angle, petalColor)
    }
    repeat(count) { i ->
        drawPetal(cx, cy, size * 0.5f, i * 360f / count - 90f + 360f / (count * 2), petalColor.copy(alpha = 0.45f))
    }
    drawCircle(centerColor, size * 0.22f, Offset(cx, cy))
}

private fun DrawScope.drawLeaf(cx: Float, cy: Float, size: Float, angleDeg: Float, color: Color) {
    val rad = Math.toRadians(angleDeg.toDouble())
    val tipX = cx + cos(rad).toFloat() * size
    val tipY = cy + sin(rad).toFloat() * size
    val perpRad = rad + PI / 2.0
    val w = size * 0.32f
    val path = Path().apply {
        moveTo(cx, cy)
        cubicTo(
            cx + cos(perpRad).toFloat() * w, cy + sin(perpRad).toFloat() * w,
            tipX + cos(perpRad).toFloat() * w * 0.35f, tipY + sin(perpRad).toFloat() * w * 0.35f,
            tipX, tipY
        )
        cubicTo(
            tipX - cos(perpRad).toFloat() * w * 0.35f, tipY - sin(perpRad).toFloat() * w * 0.35f,
            cx - cos(perpRad).toFloat() * w, cy - sin(perpRad).toFloat() * w,
            cx, cy
        )
        close()
    }
    drawPath(path, color = color)
    drawLine(color.copy(alpha = 0.55f), Offset(cx, cy), Offset(tipX, tipY), 0.9.dp.toPx())
}

private fun DrawScope.drawBow(cx: Float, cy: Float, size: Float, color: Color, knotColor: Color) {
    listOf(-1f, 1f).forEach { side ->
        val loopPath = Path().apply {
            moveTo(cx, cy)
            cubicTo(
                cx + side * size * 0.4f, cy - size * 1.1f,
                cx + side * size * 1.6f, cy - size * 0.7f,
                cx + side * size * 1.2f, cy + size * 0.1f
            )
            cubicTo(
                cx + side * size * 0.8f, cy + size * 0.5f,
                cx + side * size * 0.15f, cy + size * 0.1f,
                cx, cy
            )
            close()
        }
        drawPath(loopPath, color = color)
        drawPath(loopPath, color = Color.White.copy(alpha = 0.22f), style = Stroke(0.8.dp.toPx()))
        // Ribbon tail
        val tailPath = Path().apply {
            moveTo(cx, cy)
            cubicTo(
                cx + side * size * 0.5f, cy + size * 0.5f,
                cx + side * size * 0.7f, cy + size * 0.9f,
                cx + side * size * 0.4f, cy + size * 1.2f
            )
        }
        drawPath(tailPath, color = color.copy(alpha = 0.75f), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
    }
    drawCircle(knotColor, size * 0.22f, Offset(cx, cy))
}

private fun DrawScope.drawHeart(cx: Float, cy: Float, size: Float, color: Color) {
    val path = Path().apply {
        moveTo(cx, cy + size)
        cubicTo(cx - size * 1.4f, cy + size * 0.3f, cx - size * 1.5f, cy - size * 0.7f, cx, cy - size * 0.2f)
        cubicTo(cx + size * 1.5f, cy - size * 0.7f, cx + size * 1.4f, cy + size * 0.3f, cx, cy + size)
        close()
    }
    drawPath(path, color = color)
}

private fun DrawScope.drawSparkle(cx: Float, cy: Float, size: Float, color: Color) {
    for (i in 0 until 4) {
        val rad = Math.toRadians(i * 90.0)
        drawLine(color, Offset(cx, cy), Offset(cx + cos(rad).toFloat() * size, cy + sin(rad).toFloat() * size), size * 0.35f, cap = StrokeCap.Round)
    }
    for (i in 0 until 4) {
        val rad = Math.toRadians(i * 90.0 + 45.0)
        drawLine(color.copy(alpha = 0.6f), Offset(cx, cy), Offset(cx + cos(rad).toFloat() * size * 0.65f, cy + sin(rad).toFloat() * size * 0.65f), size * 0.25f, cap = StrokeCap.Round)
    }
}

private fun DrawScope.drawButterfly(cx: Float, cy: Float, size: Float, wingColor: Color, bodyColor: Color) {
    listOf(-1f, 1f).forEach { side ->
        val upper = Path().apply {
            moveTo(cx, cy)
            cubicTo(cx + side * size * 0.3f, cy - size * 1.3f, cx + side * size * 1.5f, cy - size * 0.9f, cx + side * size * 1.1f, cy + size * 0.1f)
            cubicTo(cx + side * size * 0.7f, cy + size * 0.5f, cx + side * size * 0.2f, cy + size * 0.1f, cx, cy)
            close()
        }
        drawPath(upper, color = wingColor.copy(alpha = 0.88f))
        drawPath(upper, color = Color.White.copy(alpha = 0.18f), style = Stroke(0.7.dp.toPx()))
        val lower = Path().apply {
            moveTo(cx, cy)
            cubicTo(cx + side * size * 0.5f, cy + size * 0.4f, cx + side * size * 1.2f, cy + size * 0.6f, cx + side * size * 0.9f, cy + size * 1.0f)
            cubicTo(cx + side * size * 0.4f, cy + size * 1.2f, cx + side * size * 0.1f, cy + size * 0.6f, cx, cy)
            close()
        }
        drawPath(lower, color = wingColor.copy(alpha = 0.65f))
    }
    val bodyPath = Path().apply {
        moveTo(cx, cy - size * 0.35f)
        cubicTo(cx - size * 0.1f, cy, cx - size * 0.08f, cy + size * 0.5f, cx, cy + size * 0.65f)
        cubicTo(cx + size * 0.08f, cy + size * 0.5f, cx + size * 0.1f, cy, cx, cy - size * 0.35f)
    }
    drawPath(bodyPath, color = bodyColor)
}

private fun DrawScope.drawFloralGlow(cx: Float, cy: Float, r: Float, color: Color, alpha: Float, blur: Float) {
    if (alpha <= 0f) return
    drawIntoCanvas { canvas ->
        canvas.nativeCanvas.drawCircle(
            cx, cy, r,
            android.graphics.Paint().apply {
                isAntiAlias = true
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = blur * 0.5f
                this.color = color.copy(alpha = alpha).toArgb()
                maskFilter = android.graphics.BlurMaskFilter(blur, android.graphics.BlurMaskFilter.Blur.NORMAL)
            }
        )
    }
}

// ─── Style 1：花環框（參考圖1：深棕環 + 橙色花叢）───────────────────
@Composable
fun FloralAvatarFrame(username: String, level: Int, size: Dp = 80.dp) {
    val theme = floralTheme(level)
    val padding = size * 0.34f
    val total = size + padding * 2

    val infiniteTransition = rememberInfiniteTransition(label = "floral")
    val animAngle by infiniteTransition.animateFloat(
        0f, 360f, infiniteRepeatable(tween(12000, easing = LinearEasing)), label = "frot"
    )
    val anim = if (level >= 5) animAngle else 0f

    Box(modifier = Modifier.size(total), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier.size(size).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(username.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.35f).sp)
        }
        Canvas(modifier = Modifier.size(total)) {
            val cx = this.size.width / 2
            val cy = this.size.height / 2
            val avatarR = size.toPx() / 2
            val gap = padding.toPx()
            val ringR = avatarR + gap * 0.5f
            val fs = gap * 0.30f
            val ls = gap * 0.22f

            drawFloralGlow(cx, cy, ringR, theme.petalColor, theme.glowAlpha * 0.7f, gap * 0.32f)

            // 主環
            drawCircle(color = theme.ringColor, radius = ringR, center = Offset(cx, cy), style = Stroke(width = (3.5f + level * 0.4f).dp.toPx()))

            // 花叢分布（仿參考圖1，左下主叢 + 右上副叢）
            data class Cluster(val centerAngle: Double, val count: Int, val scale: Float)
            val clusters = when (level) {
                1    -> listOf(Cluster(225.0, 3, 1.0f))
                2    -> listOf(Cluster(225.0, 4, 1.0f), Cluster(30.0, 3, 0.82f))
                3    -> listOf(Cluster(225.0, 5, 1.0f), Cluster(30.0, 4, 0.88f), Cluster(135.0, 2, 0.68f))
                4    -> listOf(Cluster(225.0, 6, 1.0f), Cluster(30.0, 5, 0.92f), Cluster(135.0, 3, 0.75f), Cluster(315.0, 3, 0.75f))
                else -> listOf(Cluster(225.0, 6, 1.0f), Cluster(30.0, 6, 1.0f), Cluster(135.0, 4, 0.85f), Cluster(315.0, 4, 0.85f))
            }

            clusters.forEach { cluster ->
                val offsets = when (cluster.count) {
                    2 -> listOf(-14.0, 14.0)
                    3 -> listOf(-18.0, 0.0, 18.0)
                    4 -> listOf(-24.0, -8.0, 8.0, 24.0)
                    5 -> listOf(-27.0, -13.0, 0.0, 13.0, 27.0)
                    else -> listOf(-28.0, -15.0, -4.0, 4.0, 15.0, 28.0)
                }
                offsets.forEachIndexed { idx, delta ->
                    val a = Math.toRadians(cluster.centerAngle + delta)
                    val rVar = ringR + (if (idx % 2 == 0) gap * 0.04f else -gap * 0.03f)
                    val fx = cx + cos(a).toFloat() * rVar
                    val fy = cy + sin(a).toFloat() * rVar
                    val sc = if (idx == offsets.size / 2) cluster.scale else cluster.scale * 0.78f
                    drawFlower(fx, fy, fs * sc, theme.petalColor, theme.centerColor)
                }
                // 葉子
                if (cluster.count >= 3) {
                    listOf(-38.0, 38.0).forEach { d ->
                        val la = Math.toRadians(cluster.centerAngle + d)
                        val lx = cx + cos(la).toFloat() * (ringR + gap * 0.02f)
                        val ly = cy + sin(la).toFloat() * (ringR + gap * 0.02f)
                        val leafDir = (Math.toDegrees(la) + if (d < 0) -50.0 else 50.0).toFloat()
                        drawLeaf(lx, ly, ls, leafDir, theme.leafColor)
                    }
                }
            }

            // 散落花瓣（Lv3+）
            if (level >= 3) {
                listOf(170.0, 295.0, 358.0).take(level - 2).forEach { a ->
                    val rad = Math.toRadians(a + anim * 0.08)
                    val px = cx + cos(rad).toFloat() * (ringR + gap * 0.15f)
                    val py = cy + sin(rad).toFloat() * (ringR + gap * 0.15f)
                    drawPetal(px, py, fs * 0.52f, (a + anim * 0.25f).toFloat(), theme.petalAccent.copy(alpha = 0.75f))
                }
            }
            // Lv5 動態飄散花瓣
            if (level >= 5) {
                listOf(75.0, 165.0, 258.0, 342.0).forEach { a ->
                    val rad = Math.toRadians(a + anim * 0.14)
                    val px = cx + cos(rad).toFloat() * (ringR + gap * 0.28f)
                    val py = cy + sin(rad).toFloat() * (ringR + gap * 0.28f)
                    drawPetal(px, py, fs * 0.48f, (a + anim).toFloat(), theme.petalColor.copy(alpha = 0.68f))
                }
            }
        }
    }
}

// ─── Style 2：藤蔓框（參考圖2：纏繞雙色環 + 蝴蝶花卉）────────────────
@Composable
fun VineAvatarFrame(username: String, level: Int, size: Dp = 80.dp) {
    val theme = floralTheme(level)
    val padding = size * 0.32f
    val total = size + padding * 2

    val infiniteTransition = rememberInfiniteTransition(label = "vine")
    val rotAngle by infiniteTransition.animateFloat(
        0f, 360f, infiniteRepeatable(tween(14000, easing = LinearEasing)), label = "vrot"
    )
    val anim = if (level >= 5) rotAngle else 0f

    Box(modifier = Modifier.size(total), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier.size(size).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(username.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.35f).sp)
        }
        Canvas(modifier = Modifier.size(total)) {
            val cx = this.size.width / 2
            val cy = this.size.height / 2
            val avatarR = size.toPx() / 2
            val gap = padding.toPx()
            val ringR = avatarR + gap * 0.48f
            val fs = gap * 0.27f
            val ls = gap * 0.20f
            val strokeW = (3.5f + level * 0.35f).dp.toPx()
            val twists = 6 + level * 2

            drawFloralGlow(cx, cy, ringR, theme.petalColor, theme.glowAlpha * 0.65f, gap * 0.30f)

            // 纏繞環：交替深淺弧段
            rotate(anim, Offset(cx, cy)) {
                val segAngle = 360f / twists
                for (i in 0 until twists) {
                    val color = if (i % 2 == 0) theme.ringColor else theme.petalColor.copy(alpha = 0.82f)
                    drawArc(
                        color = color,
                        startAngle = i * segAngle - 90f,
                        sweepAngle = segAngle,
                        useCenter = false,
                        topLeft = Offset(cx - ringR, cy - ringR),
                        size = Size(ringR * 2, ringR * 2),
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                }
            }

            // 花朵 + 葉子
            val flowerAngles = when (level) {
                1    -> listOf(60.0)
                2    -> listOf(60.0, 240.0)
                3    -> listOf(60.0, 180.0, 300.0)
                4    -> listOf(30.0, 120.0, 210.0, 300.0)
                else -> listOf(0.0, 72.0, 144.0, 216.0, 288.0)
            }
            flowerAngles.forEach { a ->
                val rad = Math.toRadians(a)
                val fx = cx + cos(rad).toFloat() * ringR
                val fy = cy + sin(rad).toFloat() * ringR
                drawFlower(fx, fy, fs, theme.petalColor, theme.centerColor)
                listOf(-22.0, 22.0).forEach { d ->
                    val lr = Math.toRadians(a + d)
                    val lx = cx + cos(lr).toFloat() * (ringR - gap * 0.04f)
                    val ly = cy + sin(lr).toFloat() * (ringR - gap * 0.04f)
                    drawLeaf(lx, ly, ls, (Math.toDegrees(lr) + 90.0).toFloat(), theme.leafColor)
                }
            }

            // 蝴蝶在 12 點鐘（Lv1 以上）
            drawButterfly(cx, cy - ringR - gap * 0.04f, gap * 0.23f, theme.petalAccent, theme.centerColor)
        }
    }
}

// ─── Style 3：可愛雙環框（參考圖3：雙環 + 蝴蝶結 + 小裝飾）───────────
@Composable
fun CuteAvatarFrame(username: String, level: Int, size: Dp = 80.dp) {
    val theme = floralTheme(level)
    val padding = size * 0.36f
    val total = size + padding * 2

    val infiniteTransition = rememberInfiniteTransition(label = "cute")
    val sparkAngle by infiniteTransition.animateFloat(
        0f, 360f, infiniteRepeatable(tween(7000, easing = LinearEasing)), label = "spark"
    )
    val anim = if (level >= 4) sparkAngle else 0f

    Box(modifier = Modifier.size(total), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier.size(size).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(username.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.35f).sp)
        }
        Canvas(modifier = Modifier.size(total)) {
            val cx = this.size.width / 2
            val cy = this.size.height / 2
            val avatarR = size.toPx() / 2
            val gap = padding.toPx()
            val outerR = avatarR + gap * 0.74f
            val innerR = avatarR + gap * 0.30f
            val fs = gap * 0.24f

            drawFloralGlow(cx, cy, outerR, theme.petalColor, theme.glowAlpha * 0.55f, gap * 0.28f)

            // 外環（實線）
            drawCircle(
                color = theme.ringColor,
                radius = outerR,
                center = Offset(cx, cy),
                style = Stroke(width = 3.dp.toPx())
            )

            // 內環（虛線）
            drawCircle(
                color = theme.petalColor.copy(alpha = 0.72f),
                radius = innerR,
                center = Offset(cx, cy),
                style = Stroke(
                    width = 1.6.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4.5f))
                )
            )

            // 蝴蝶結（12 點鐘）
            drawBow(cx, cy - outerR, gap * 0.27f, theme.petalColor, theme.centerColor)

            // 愛心（Lv2+，6 點鐘左右）
            if (level >= 2) {
                listOf(-28.0, 28.0).forEach { a ->
                    val rad = Math.toRadians(90.0 + a)
                    val hx = cx + cos(rad).toFloat() * outerR
                    val hy = cy + sin(rad).toFloat() * outerR
                    drawHeart(hx, hy, fs * 0.6f, theme.petalColor)
                }
            }

            // 小花（Lv3+，3/9 點鐘）
            if (level >= 3) {
                listOf(-60.0, 60.0).forEach { a ->
                    val rad = Math.toRadians(90.0 + a)
                    val fx = cx + cos(rad).toFloat() * outerR
                    val fy = cy + sin(rad).toFloat() * outerR
                    drawFlower(fx, fy, fs * 0.78f, theme.petalColor, theme.centerColor, 5)
                }
            }

            // 閃爍星（Lv4+，在雙環之間旋轉）
            if (level >= 4) {
                listOf(45.0, 135.0, 225.0, 315.0).forEach { a ->
                    val rad = Math.toRadians(a + anim * 0.12)
                    val sr = innerR + (outerR - innerR) * 0.5f
                    drawSparkle(cx + cos(rad).toFloat() * sr, cy + sin(rad).toFloat() * sr, fs * 0.38f, theme.petalAccent)
                }
            }

            // Lv5：外環外額外旋轉閃爍
            if (level >= 5) {
                listOf(0.0, 120.0, 240.0).forEach { a ->
                    val rad = Math.toRadians(a + anim)
                    val sr = outerR + gap * 0.14f
                    drawSparkle(cx + cos(rad).toFloat() * sr, cy + sin(rad).toFloat() * sr, fs * 0.32f, theme.centerColor)
                }
            }
        }
    }
}

// ─── Previews ────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF1C1008, name = "花環框（參考圖1風格）")
@Composable
private fun PreviewFloral() {
    MaterialTheme {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("花環框 FloralAvatarFrame", color = Color(0xFFFFCC88), fontSize = 11.sp, modifier = Modifier.padding(bottom = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                (1..5).forEach { lv ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        FloralAvatarFrame(username = "趣", level = lv, size = 52.dp)
                        Spacer(Modifier.height(4.dp))
                        Text("Lv$lv", fontSize = 9.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1008, name = "藤蔓框（參考圖2風格）")
@Composable
private fun PreviewVine() {
    MaterialTheme {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("藤蔓框 VineAvatarFrame", color = Color(0xFFFFCC88), fontSize = 11.sp, modifier = Modifier.padding(bottom = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                (1..5).forEach { lv ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        VineAvatarFrame(username = "趣", level = lv, size = 52.dp)
                        Spacer(Modifier.height(4.dp))
                        Text("Lv$lv", fontSize = 9.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1008, name = "可愛雙環框（參考圖3風格）")
@Composable
private fun PreviewCute() {
    MaterialTheme {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("可愛雙環框 CuteAvatarFrame", color = Color(0xFFFFCC88), fontSize = 11.sp, modifier = Modifier.padding(bottom = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                (1..5).forEach { lv ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CuteAvatarFrame(username = "趣", level = lv, size = 52.dp)
                        Spacer(Modifier.height(4.dp))
                        Text("Lv$lv", fontSize = 9.sp, color = Color.White)
                    }
                }
            }
        }
    }
}
