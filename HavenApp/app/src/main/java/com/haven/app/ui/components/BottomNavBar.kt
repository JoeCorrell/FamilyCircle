package com.haven.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haven.app.ui.theme.LocalHavenColors
import com.haven.app.ui.theme.SpaceMonoFamily

enum class NavTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Outlined.Home),
    MAP("Map", Icons.Outlined.Map),
    CHAT("Chat", Icons.Outlined.ChatBubbleOutline),
    SETTINGS("More", Icons.Outlined.Settings);
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
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            },
                        tint = iconColor
                    )
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
