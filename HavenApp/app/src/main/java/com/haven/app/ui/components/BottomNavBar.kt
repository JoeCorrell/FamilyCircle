package com.haven.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haven.app.ui.theme.LocalHavenColors
import com.haven.app.ui.theme.SpaceMonoFamily

enum class NavTab(val label: String) {
    HOME("Home"),
    MAP("Map"),
    CHAT("Chat"),
    SETTINGS("More");
}

@Composable
fun BottomNavBar(
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val t = LocalHavenColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = t.border,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1f
                )
            }
            .background(t.bg.copy(alpha = 0.95f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .padding(bottom = 14.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavTab.entries.forEach { tab ->
            val isActive = selectedTab == tab

            val iconScale by animateFloatAsState(
                targetValue = if (isActive) 1.12f else 1f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
                label = "navScale"
            )
            val pillColor by animateColorAsState(
                targetValue = if (isActive) t.accentBg else Color.Transparent,
                animationSpec = tween(250),
                label = "navPill"
            )
            val iconColor by animateColorAsState(
                targetValue = if (isActive) t.accent else t.textFade,
                animationSpec = tween(200),
                label = "navIcon"
            )

            Column(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onTabSelected(tab) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 44.dp, height = 30.dp)
                        .background(pillColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            }
                    ) {
                        when (tab) {
                            NavTab.HOME -> drawHomeIcon(iconColor)
                            NavTab.MAP -> drawMapIcon(iconColor)
                            NavTab.CHAT -> drawChatIcon(iconColor)
                            NavTab.SETTINGS -> drawSettingsIcon(iconColor)
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = tab.label,
                    fontSize = 9.sp,
                    fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
                    color = iconColor,
                    fontFamily = SpaceMonoFamily,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// ── Embedded nav icons ──

private fun DrawScope.drawHomeIcon(color: Color) {
    val w = size.width
    val h = size.height
    val s = Stroke(width = 1.7f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    // Roof
    val roof = Path().apply {
        moveTo(w * 0.1f, h * 0.45f)
        lineTo(w * 0.5f, h * 0.1f)
        lineTo(w * 0.9f, h * 0.45f)
    }
    drawPath(roof, color, style = s)
    // House body
    drawRoundRect(color, Offset(w * 0.18f, h * 0.42f), Size(w * 0.64f, h * 0.48f),
        cornerRadius = CornerRadius(w * 0.04f), style = s)
    // Door
    drawRoundRect(color, Offset(w * 0.38f, h * 0.58f), Size(w * 0.24f, h * 0.32f),
        cornerRadius = CornerRadius(w * 0.04f), style = s)
}

private fun DrawScope.drawMapIcon(color: Color) {
    val w = size.width
    val h = size.height
    val s = Stroke(width = 1.7f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    // Folded map
    val map = Path().apply {
        moveTo(w * 0.1f, h * 0.15f)
        lineTo(w * 0.37f, h * 0.25f)
        lineTo(w * 0.63f, h * 0.15f)
        lineTo(w * 0.9f, h * 0.25f)
        lineTo(w * 0.9f, h * 0.85f)
        lineTo(w * 0.63f, h * 0.75f)
        lineTo(w * 0.37f, h * 0.85f)
        lineTo(w * 0.1f, h * 0.75f)
        close()
    }
    drawPath(map, color, style = s)
    // Fold lines
    drawLine(color, Offset(w * 0.37f, h * 0.25f), Offset(w * 0.37f, h * 0.85f), strokeWidth = 1.4f, cap = StrokeCap.Round)
    drawLine(color, Offset(w * 0.63f, h * 0.15f), Offset(w * 0.63f, h * 0.75f), strokeWidth = 1.4f, cap = StrokeCap.Round)
}

private fun DrawScope.drawChatIcon(color: Color) {
    val w = size.width
    val h = size.height
    val s = Stroke(width = 1.7f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    // Bubble
    val bubble = Path().apply {
        moveTo(w * 0.5f, h * 0.85f)
        lineTo(w * 0.3f, h * 0.72f)
        lineTo(w * 0.12f, h * 0.72f)
        cubicTo(w * 0.06f, h * 0.72f, w * 0.06f, h * 0.72f, w * 0.06f, h * 0.62f)
        lineTo(w * 0.06f, h * 0.18f)
        cubicTo(w * 0.06f, h * 0.08f, w * 0.06f, h * 0.08f, w * 0.16f, h * 0.08f)
        lineTo(w * 0.84f, h * 0.08f)
        cubicTo(w * 0.94f, h * 0.08f, w * 0.94f, h * 0.08f, w * 0.94f, h * 0.18f)
        lineTo(w * 0.94f, h * 0.62f)
        cubicTo(w * 0.94f, h * 0.72f, w * 0.94f, h * 0.72f, w * 0.84f, h * 0.72f)
        lineTo(w * 0.5f, h * 0.72f)
        close()
    }
    drawPath(bubble, color, style = s)
    // Dots
    val dotR = w * 0.04f
    val dotY = h * 0.4f
    drawCircle(color, dotR, Offset(w * 0.35f, dotY))
    drawCircle(color, dotR, Offset(w * 0.5f, dotY))
    drawCircle(color, dotR, Offset(w * 0.65f, dotY))
}

private fun DrawScope.drawSettingsIcon(color: Color) {
    val w = size.width
    val h = size.height
    val s = Stroke(width = 1.7f, cap = StrokeCap.Round)
    // Three horizontal lines with dots (sliders)
    val lines = listOf(0.25f, 0.5f, 0.75f)
    val knobs = listOf(0.65f, 0.35f, 0.55f)
    lines.forEachIndexed { i, y ->
        drawLine(color, Offset(w * 0.12f, h * y), Offset(w * 0.88f, h * y), strokeWidth = 1.5f, cap = StrokeCap.Round)
        drawCircle(color, w * 0.07f, Offset(w * knobs[i], h * y))
    }
}
