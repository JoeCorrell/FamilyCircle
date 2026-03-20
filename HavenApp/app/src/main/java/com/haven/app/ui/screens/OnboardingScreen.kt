package com.haven.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haven.app.ui.theme.LocalHavenColors
import com.haven.app.ui.theme.OutfitFamily
import com.haven.app.ui.theme.SpaceMonoFamily
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val description: String,
    val tipTitle: String,
    val tipText: String
)

private val pages = listOf(
    OnboardingPage(
        icon = Icons.Outlined.Home,
        title = "Home Dashboard",
        subtitle = "YOUR FAMILY AT A GLANCE",
        description = "See everyone's status, battery levels, and locations. Tap any member to view their details or send a check-in request.",
        tipTitle = "Quick Tip",
        tipText = "Tap the SOS tile in an emergency to alert all family members with your live location."
    ),
    OnboardingPage(
        icon = Icons.Outlined.Map,
        title = "Live Map",
        subtitle = "REAL-TIME TRACKING",
        description = "See all family members on the map in real time. Tap a member to see their speed, battery, and recent locations. Toggle satellite view for better detail.",
        tipTitle = "Try It",
        tipText = "Tap the recenter button to jump to your current location. Tap any member marker to see their info drawer."
    ),
    OnboardingPage(
        icon = Icons.Outlined.ChatBubbleOutline,
        title = "Family Chat",
        subtitle = "STAY CONNECTED",
        description = "Send messages to your family circle. Use the errand button to request tasks like grocery pickups with location and notes.",
        tipTitle = "Errands",
        tipText = "Tap the clipboard icon next to the message input to create an errand request for your family."
    ),
    OnboardingPage(
        icon = Icons.Outlined.LocationOn,
        title = "Saved Places",
        subtitle = "GEOFENCE ALERTS",
        description = "Save important locations like Home, School, or Work. Each place shows as a radius circle on the map. Get notified when family arrives or departs.",
        tipTitle = "Adding Places",
        tipText = "Go to Settings > Saved Places > tap + to add. You can tap the map to pick a location or type an address."
    ),
    OnboardingPage(
        icon = Icons.Outlined.Settings,
        title = "You're All Set!",
        subtitle = "CUSTOMIZE YOUR EXPERIENCE",
        description = "Edit your profile, change themes, manage your circle, and adjust notification preferences from Settings. Haven keeps your family connected and safe.",
        tipTitle = "Themes",
        tipText = "Choose from 10 themes including Sand, Ocean, Midnight, Rose, and more. Find them in Settings > Appearance."
    )
)

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val t = LocalHavenColors.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { pages.size })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(t.bg)
    ) {
        // Skip button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                "Skip",
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onComplete() }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                color = t.textFade, fontFamily = OutfitFamily
            )
        }

        // Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { pageIndex ->
            val page = pages[pageIndex]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Animated icon
                val iconScale by animateFloatAsState(
                    targetValue = if (pagerState.currentPage == pageIndex) 1f else 0.7f,
                    animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
                    label = "pageIcon"
                )

                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .scale(iconScale)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Brush.linearGradient(listOf(t.accent, t.accentMid))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(page.icon, page.title, Modifier.size(40.dp), tint = Color.White)
                }

                Spacer(Modifier.height(28.dp))

                // Subtitle label
                Text(
                    page.subtitle,
                    fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    color = t.accent, fontFamily = SpaceMonoFamily, letterSpacing = 2.sp
                )

                Spacer(Modifier.height(8.dp))

                // Title
                Text(
                    page.title,
                    fontSize = 26.sp, fontWeight = FontWeight.ExtraBold,
                    color = t.text, fontFamily = OutfitFamily, textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(14.dp))

                // Description
                Text(
                    page.description,
                    fontSize = 14.sp, color = t.textMid, fontFamily = OutfitFamily,
                    textAlign = TextAlign.Center, lineHeight = 22.sp
                )

                Spacer(Modifier.height(24.dp))

                // Tip card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(t.accentBg)
                        .border(1.dp, t.accent.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            page.tipTitle, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            color = t.accent, fontFamily = SpaceMonoFamily, letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            page.tipText, fontSize = 12.sp, color = t.textMid,
                            fontFamily = OutfitFamily, lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Bottom: dots + button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Page dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pages.forEachIndexed { index, _ ->
                    val dotWidth by animateFloatAsState(
                        targetValue = if (pagerState.currentPage == index) 24f else 8f,
                        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
                        label = "dot"
                    )
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(dotWidth.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (pagerState.currentPage == index) t.accent
                                else t.accent.copy(alpha = 0.2f)
                            )
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Next / Get Started button
            val isLast = pagerState.currentPage == pages.lastIndex
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(t.accent, t.accentMid)))
                    .clickable {
                        if (isLast) {
                            onComplete()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    }
                    .padding(vertical = 15.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (isLast) "Get Started" else "Next",
                        fontSize = 15.sp, fontWeight = FontWeight.Bold,
                        color = Color.White, fontFamily = OutfitFamily
                    )
                    if (!isLast) {
                        Icon(Icons.Outlined.ChevronRight, "Next", Modifier.size(18.dp), tint = Color.White)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
        }
    }
}
