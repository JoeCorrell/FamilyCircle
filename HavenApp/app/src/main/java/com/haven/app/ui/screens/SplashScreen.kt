package com.haven.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haven.app.R
import com.haven.app.ui.theme.LocalHavenColors
import com.haven.app.ui.theme.OutfitFamily
import com.haven.app.ui.theme.SpaceMonoFamily
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val t = LocalHavenColors.current

    var phase by remember { mutableIntStateOf(0) }

    // Phase timeline
    LaunchedEffect(Unit) {
        delay(100)
        phase = 1 // Shield appears
        delay(600)
        phase = 2 // Text appears
        delay(800)
        phase = 3 // Tagline
        delay(600)
        phase = 4 // Fade out
        delay(500)
        onFinished()
    }

    // Shield scale
    val shieldScale by animateFloatAsState(
        targetValue = when (phase) {
            0 -> 0.3f
            1, 2, 3 -> 1f
            else -> 1.2f
        },
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "shieldScale"
    )
    val shieldAlpha by animateFloatAsState(
        targetValue = if (phase >= 1 && phase < 4) 1f else 0f,
        animationSpec = tween(if (phase == 4) 400 else 500),
        label = "shieldAlpha"
    )

    // Title
    val titleAlpha by animateFloatAsState(
        targetValue = if (phase >= 2 && phase < 4) 1f else 0f,
        animationSpec = tween(400),
        label = "titleAlpha"
    )
    val titleOffset by animateFloatAsState(
        targetValue = if (phase >= 2) 0f else 20f,
        animationSpec = tween(400, easing = EaseOutCubic),
        label = "titleOffset"
    )

    // Tagline
    val tagAlpha by animateFloatAsState(
        targetValue = if (phase >= 3 && phase < 4) 1f else 0f,
        animationSpec = tween(400),
        label = "tagAlpha"
    )

    // Pulse rings
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val ring1 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseOut), RepeatMode.Restart),
        label = "ring1"
    )
    val ring1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseOut), RepeatMode.Restart),
        label = "ring1a"
    )
    val ring2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(tween(2000, 500, easing = EaseOut), RepeatMode.Restart),
        label = "ring2"
    )
    val ring2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(2000, 500, easing = EaseOut), RepeatMode.Restart),
        label = "ring2a"
    )

    // Overall fade
    val screenAlpha by animateFloatAsState(
        targetValue = if (phase == 4) 0f else 1f,
        animationSpec = tween(400),
        label = "screenAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(screenAlpha)
            .background(t.bg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Shield with pulse rings
            Box(contentAlignment = Alignment.Center) {
                // Pulse rings
                if (phase >= 1) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .scale(ring1)
                            .alpha(ring1Alpha)
                            .background(t.accent.copy(alpha = 0.1f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .scale(ring2)
                            .alpha(ring2Alpha)
                            .background(t.accent.copy(alpha = 0.08f), CircleShape)
                    )
                }

                // App icon
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = "Haven",
                    modifier = Modifier
                        .size(120.dp)
                        .scale(shieldScale)
                        .alpha(shieldAlpha)
                )
            }

            Spacer(Modifier.height(28.dp))

            // Title
            Text(
                "HAVEN",
                modifier = Modifier
                    .alpha(titleAlpha)
                    .graphicsLayer { translationY = titleOffset },
                fontSize = 28.sp, fontWeight = FontWeight.Black,
                color = t.text, fontFamily = SpaceMonoFamily, letterSpacing = 6.sp
            )

            Spacer(Modifier.height(8.dp))

            // Tagline
            Text(
                "Family Safety Reimagined",
                modifier = Modifier.alpha(tagAlpha),
                fontSize = 13.sp, fontWeight = FontWeight.Medium,
                color = t.textFade, fontFamily = OutfitFamily, letterSpacing = 1.sp
            )
        }
    }
}
