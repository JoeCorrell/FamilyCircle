package com.haven.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haven.app.data.model.FamilyMember
import com.haven.app.ui.theme.LocalHavenColors
import com.haven.app.ui.theme.OutfitFamily
import com.haven.app.ui.theme.SpaceMonoFamily
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun OrbitMap(
    members: List<FamilyMember>,
    selectedMember: FamilyMember?,
    onMemberSelected: (FamilyMember) -> Unit,
    modifier: Modifier = Modifier
) {
    val t = LocalHavenColors.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(t.bgSub)
    ) {
        // Orbit rings and crosshair
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2
            val cy = size.height / 2
            val maxRadius = minOf(cx, cy) * 0.85f
            val rings = listOf(0.32f, 0.55f, 0.77f, 1.0f)

            // Draw orbit rings
            rings.forEachIndexed { index, fraction ->
                val radius = maxRadius * fraction
                drawCircle(
                    color = t.border,
                    radius = radius,
                    center = Offset(cx, cy),
                    style = Stroke(
                        width = if (index == 0) 1.5f else 0.8f,
                        pathEffect = if (index > 0) {
                            androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 12f))
                        } else null
                    )
                )
            }

            // Crosshair
            drawLine(t.accent.copy(alpha = 0.4f), Offset(cx, cy - 12), Offset(cx, cy + 12), 1.5f, StrokeCap.Round)
            drawLine(t.accent.copy(alpha = 0.4f), Offset(cx - 12, cy), Offset(cx + 12, cy), 1.5f, StrokeCap.Round)

            // Grid dots
            for (i in 0 until 8) {
                val angle = (i.toFloat() / 8f) * Math.PI.toFloat() * 2f
                drawCircle(
                    color = t.textFade.copy(alpha = 0.3f),
                    radius = 2f,
                    center = Offset(cx + cos(angle) * maxRadius, cy + sin(angle) * maxRadius)
                )
            }
        }

        // HOME label at center
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(44.dp)
                .background(t.accentBg, CircleShape)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "HOME",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = t.accent,
                fontFamily = SpaceMonoFamily,
                letterSpacing = 0.5.sp
            )
        }

        // Member nodes
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val cx = maxWidth / 2
            val cy = maxHeight / 2
            val maxRadius = minOf(cx, cy) * 0.85f

            members.forEach { member ->
                val rad = member.ringPosition * maxRadius.value * 0.9f + maxRadius.value * 0.15f
                val ang = (member.angle * Math.PI / 180f).toFloat()
                val x = cx + (cos(ang) * rad).dp
                val y = cy + (sin(ang) * rad).dp
                val isSelected = selectedMember?.id == member.id
                val memberColor = Color(member.color)

                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.25f else 1f,
                    animationSpec = spring(dampingRatio = 0.6f),
                    label = "scale"
                )

                Box(
                    modifier = Modifier
                        .offset(x = x - 22.dp, y = y - 22.dp)
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onMemberSelected(member) }
                ) {
                    // Member circle
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .then(
                                if (isSelected) Modifier.shadow(8.dp, CircleShape, ambientColor = memberColor)
                                else Modifier
                            )
                            .background(
                                if (isSelected) memberColor else t.surface,
                                CircleShape
                            )
                            .clip(CircleShape)
                            .padding(3.dp)
                            .background(
                                if (isSelected) memberColor else t.surface,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Outer ring using canvas
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = memberColor,
                                style = Stroke(width = 3f),
                                radius = size.minDimension / 2
                            )
                        }
                        Text(
                            text = member.initials,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isSelected) Color.White else memberColor,
                            fontFamily = OutfitFamily
                        )
                    }

                    // Speed indicator
                    if (member.speed > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 3.dp, y = (-3).dp)
                                .size(16.dp)
                                .background(t.warn, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${member.speed.toInt()}",
                                fontSize = 7.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    // Name chip below
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 26.dp)
                            .background(
                                if (isSelected) memberColor else t.surface,
                                RoundedCornerShape(8.dp)
                            )
                            .shadow(1.dp, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = member.name,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else t.text,
                            fontFamily = SpaceMonoFamily,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // Coordinate readout
        Text(
            text = "40.758\u00B0N 73.985\u00B0W",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .background(t.surface.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 8.sp,
            color = t.textFade,
            fontFamily = SpaceMonoFamily,
            letterSpacing = 0.8.sp
        )
    }
}
