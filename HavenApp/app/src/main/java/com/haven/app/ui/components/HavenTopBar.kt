package com.haven.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haven.app.ui.theme.LocalHavenColors
import com.haven.app.ui.theme.OutfitFamily

@Composable
fun HavenTopBar(
    showBack: Boolean,
    onBack: () -> Unit,
    onProfileClick: () -> Unit,
    userInitials: String,
    userColor: Long,
    userPhotoUrl: String = "",
) {
    val t = LocalHavenColors.current
    val memberColor = Color(userColor)

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
        AnimatedVisibility(
            visible = showBack,
            modifier = Modifier.align(Alignment.CenterStart),
            enter = fadeIn() + scaleIn(initialScale = 0.8f, animationSpec = spring()),
            exit = fadeOut() + scaleOut(targetScale = 0.8f, animationSpec = spring())
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(t.card, RoundedCornerShape(12.dp))
                    .border(1.dp, t.border, RoundedCornerShape(12.dp))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", Modifier.size(18.dp), tint = t.text)
            }
        }

        Text(
            "Haven",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = t.text,
            fontFamily = OutfitFamily,
            letterSpacing = (-0.5).sp
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(36.dp)
                .background(memberColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                .border(2.dp, memberColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .clickable { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            if (userPhotoUrl.isNotEmpty()) {
                ProfileImage(
                    photoUrl = userPhotoUrl,
                    contentDescription = "Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                )
            } else {
                Text(
                    userInitials.take(1).uppercase().ifEmpty { "?" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = memberColor,
                    fontFamily = OutfitFamily
                )
            }
        }
    }
}
