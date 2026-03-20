package com.haven.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haven.app.ui.theme.LocalHavenColors
import com.haven.app.ui.theme.SpaceMonoFamily

@Composable
fun BatteryIndicator(
    value: Int,
    size: Dp = 28.dp,
    modifier: Modifier = Modifier
) {
    val t = LocalHavenColors.current
    val color = when {
        value > 50 -> t.ok
        value > 20 -> t.warn
        else -> t.danger
    }
    val strokeWidth = 2.5f

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasSize = this.size.minDimension
            val radius = (canvasSize - strokeWidth * 2) / 2
            val arcSize = Size(radius * 2, radius * 2)
            val topLeft = Offset(
                (canvasSize - radius * 2) / 2,
                (canvasSize - radius * 2) / 2
            )

            // Background arc
            drawArc(
                color = t.border,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Value arc
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * value / 100f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Text(
            text = "$value",
            color = color,
            fontSize = (size.value * 0.3f).sp,
            fontWeight = FontWeight.Bold,
            fontFamily = SpaceMonoFamily
        )
    }
}
