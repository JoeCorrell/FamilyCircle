package com.haven.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import com.haven.app.data.api.HavenInfo
import com.haven.app.ui.theme.LocalHavenColors
import com.haven.app.ui.theme.OutfitFamily
import com.haven.app.ui.theme.SpaceMonoFamily

@Composable
fun HavenTopBar(
    showBack: Boolean,
    onBack: () -> Unit,
    havenName: String = "Haven",
    havens: List<HavenInfo> = emptyList(),
    activeHavenId: String? = null,
    onSwitchHaven: (String) -> Unit = {},
) {
    val t = LocalHavenColors.current
    var showDropdown by remember { mutableStateOf(false) }
    val hasMultipleHavens = havens.size > 1

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(t.bg)
                .drawBehind {
                    drawLine(t.border, Offset(0f, size.height), Offset(size.width, size.height), 1f)
                }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            // Back button — left side
            if (showBack) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(36.dp)
                        .background(t.card, RoundedCornerShape(12.dp))
                        .border(1.dp, t.border, RoundedCornerShape(12.dp))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", Modifier.size(18.dp), tint = t.text)
                }
            }

            // Haven name — centered, tappable if multiple havens
            Row(
                modifier = Modifier
                    .then(
                        if (hasMultipleHavens) Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showDropdown = !showDropdown }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                        else Modifier
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    havenName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = t.text,
                    fontFamily = OutfitFamily,
                    letterSpacing = (-0.5).sp
                )
                if (hasMultipleHavens) {
                    Canvas(modifier = Modifier.size(12.dp)) {
                        val w = size.width
                        val h = size.height
                        val path = Path().apply {
                            moveTo(w * 0.2f, h * 0.35f)
                            lineTo(w * 0.5f, h * 0.7f)
                            lineTo(w * 0.8f, h * 0.35f)
                        }
                        drawPath(path, t.textFade, style = Stroke(width = w * 0.15f, cap = StrokeCap.Round))
                    }
                }
            }
        }

        // ── Dropdown menu ──
        AnimatedVisibility(
            visible = showDropdown,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(t.card)
                    .drawBehind {
                        drawLine(t.border, Offset(0f, size.height), Offset(size.width, size.height), 1f)
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                havens.forEach { haven ->
                    val isActive = haven.havenId == activeHavenId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isActive) t.accent.copy(alpha = 0.08f) else Color.Transparent)
                            .clickable {
                                onSwitchHaven(haven.havenId)
                                showDropdown = false
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Haven icon
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    if (isActive) t.accent.copy(alpha = 0.12f) else t.bgSub,
                                    RoundedCornerShape(10.dp)
                                )
                                .border(
                                    if (isActive) 1.5.dp else 1.dp,
                                    if (isActive) t.accent else t.border,
                                    RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(16.dp)) {
                                drawShieldIcon(if (isActive) t.accent else t.textFade)
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                haven.havenName,
                                fontSize = 14.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                color = if (isActive) t.accent else t.text,
                                fontFamily = OutfitFamily
                            )
                            Text(
                                haven.memberName,
                                fontSize = 10.sp,
                                color = t.textFade,
                                fontFamily = SpaceMonoFamily
                            )
                        }
                        if (isActive) {
                            Canvas(modifier = Modifier.size(16.dp)) {
                                val w = size.width; val h = size.height
                                val path = Path().apply {
                                    moveTo(w * 0.15f, h * 0.5f)
                                    lineTo(w * 0.4f, h * 0.75f)
                                    lineTo(w * 0.85f, h * 0.25f)
                                }
                                drawPath(path, t.accent, style = Stroke(width = w * 0.12f, cap = StrokeCap.Round))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawShieldIcon(color: Color) {
    val w = size.width; val h = size.height; val cx = w / 2f
    val s = Stroke(width = w * 0.1f, cap = StrokeCap.Round)
    val shield = Path().apply {
        moveTo(cx, h * 0.05f)
        cubicTo(w * 0.15f, h * 0.05f, w * 0.08f, h * 0.15f, w * 0.08f, h * 0.35f)
        cubicTo(w * 0.08f, h * 0.65f, w * 0.25f, h * 0.82f, cx, h * 0.95f)
        cubicTo(w * 0.75f, h * 0.82f, w * 0.92f, h * 0.65f, w * 0.92f, h * 0.35f)
        cubicTo(w * 0.92f, h * 0.15f, w * 0.85f, h * 0.05f, cx, h * 0.05f)
        close()
    }
    drawPath(shield, color, style = s)
}
