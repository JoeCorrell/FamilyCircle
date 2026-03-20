package com.haven.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.haven.app.data.model.FamilyMember
import com.haven.app.ui.components.HavenCard
import androidx.compose.material.icons.outlined.DirectionsCar
import com.haven.app.ui.theme.LocalHavenColors
import com.haven.app.ui.theme.OutfitFamily
import com.haven.app.ui.theme.SpaceMonoFamily
import com.haven.app.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onMemberClick: (FamilyMember) -> Unit,
    onSosClick: () -> Unit,
    onSafetyClick: () -> Unit,
    onMapClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onThemesClick: () -> Unit,
    onAddPlaceClick: () -> Unit,
    onPlacesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val t = LocalHavenColors.current
    val members by viewModel.members.collectAsStateWithLifecycle()
    val places by viewModel.places.collectAsStateWithLifecycle()
    val notifications by viewModel.recentNotifications.collectAsStateWithLifecycle()
    val drivesCount by viewModel.drivesCount.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadCount.collectAsStateWithLifecycle()
    val familyName by viewModel.familyName.collectAsStateWithLifecycle()
    val myHavens by viewModel.myHavens.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Haven",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = t.text,
                fontFamily = OutfitFamily,
                letterSpacing = (-0.5).sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(t.accentBg, RoundedCornerShape(10.dp))
                        .clickable { onThemesClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Palette, "Themes", Modifier.size(16.dp), tint = t.accent)
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(t.accentBg, RoundedCornerShape(10.dp))
                        .clickable { onNotificationsClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Notifications, "Notifications", Modifier.size(16.dp), tint = t.accent)
                    if (unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-4).dp, y = 4.dp)
                                .size(7.dp)
                                .background(t.danger, CircleShape)
                        )
                    }
                }
            }
        }

        // Haven switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val activeId = viewModel.activeHavenId
            if (myHavens.isEmpty()) {
                // Fallback single haven
                Box(
                    modifier = Modifier
                        .background(t.accent.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .border(1.5.dp, t.accent, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Home, "Haven", Modifier.size(14.dp), tint = t.accent)
                        Text(familyName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = t.accent, fontFamily = SpaceMonoFamily)
                    }
                }
            } else {
                myHavens.forEach { haven ->
                    val isActive = haven.havenId == activeId
                    Box(
                        modifier = Modifier
                            .background(
                                if (isActive) t.accent.copy(alpha = 0.1f) else t.card,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                if (isActive) 1.5.dp else 1.dp,
                                if (isActive) t.accent else t.border,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.switchHaven(haven.havenId) }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Home, "Haven", Modifier.size(14.dp), tint = if (isActive) t.accent else t.textFade)
                            Text(haven.havenName, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                color = if (isActive) t.accent else t.textMid, fontFamily = SpaceMonoFamily)
                        }
                    }
                }
            }
        }

        // Status line
        Text(
            if (members.isEmpty()) "Add family members to get started"
            else if (members.all { it.isOnline }) "Everyone's safe"
            else "${members.count { it.isOnline }} of ${members.size} online",
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
            fontSize = 13.sp,
            color = t.textFade,
            fontFamily = SpaceMonoFamily
        )

        // Bento Grid with staggered entrance
        var appeared by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { appeared = true }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Get started card when no members
            if (members.isEmpty()) {
                HavenCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onProfileClick
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Welcome to Haven",
                            fontSize = 18.sp, fontWeight = FontWeight.ExtraBold,
                            color = t.text, fontFamily = OutfitFamily
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Set up your profile and add family members to get started.",
                            fontSize = 13.sp, color = t.textMid,
                            fontFamily = OutfitFamily,
                            lineHeight = 20.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Brush.linearGradient(listOf(t.accent, t.accentMid)))
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text(
                                "Set Up Profile",
                                fontSize = 14.sp, fontWeight = FontWeight.Bold,
                                color = Color.White, fontFamily = OutfitFamily
                            )
                        }
                    }
                }
            }

            // Family card (full width)
            if (members.isNotEmpty()) {
                HavenCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("FAMILY", style = labelStyle(), color = t.textFade)
                            Text(
                                "${members.count { it.isOnline }} ONLINE",
                                fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                color = t.accent, fontFamily = SpaceMonoFamily
                            )
                        }
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(t.border)
                        )
                        Row(modifier = Modifier.fillMaxWidth()) {
                            members.forEachIndexed { index, member ->
                                val memberColor = Color(member.color)
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onMemberClick(member) }
                                        .then(
                                            if (index < members.size - 1)
                                                Modifier.drawBehind {
                                                    drawLine(
                                                        t.border,
                                                        Offset(size.width, 0f),
                                                        Offset(size.width, size.height),
                                                        1f
                                                    )
                                                }
                                            else Modifier
                                        )
                                        .padding(vertical = 12.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(memberColor.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                            .border(2.5.dp, memberColor, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            member.initials, fontSize = 15.sp,
                                            fontWeight = FontWeight.Black, color = memberColor,
                                            fontFamily = OutfitFamily
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        member.name, fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold, color = t.text,
                                        fontFamily = OutfitFamily
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    val lastSeen = member.lastSeenText()
                                    Text(
                                        if (lastSeen == "Now") "\u25CF LIVE" else lastSeen,
                                        fontSize = 8.sp, fontWeight = FontWeight.Bold,
                                        color = if (lastSeen == "Now") t.ok else t.textFade,
                                        fontFamily = SpaceMonoFamily
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SOS + Drive Score row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // SOS tile
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 110.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(listOf(Color(0xFFDC2626), Color(0xFF991B1B)))
                        )
                        .clickable { onSosClick() }
                        .padding(18.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("!", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White.copy(alpha = 0.8f))
                        Column {
                            Text("SOS", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White, fontFamily = OutfitFamily)
                            Text("EMERGENCY", fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f), fontFamily = SpaceMonoFamily)
                        }
                    }
                }

                // Driving tile
                HavenCard(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 110.dp),
                    onClick = onSafetyClick
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("DRIVING", style = labelStyle(), color = t.textFade)
                        Spacer(Modifier.height(10.dp))
                        Icon(Icons.Outlined.DirectionsCar, "Driving", Modifier.size(28.dp), tint = t.accent)
                        Spacer(Modifier.height(6.dp))
                        Text("View Trips", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = t.text, fontFamily = OutfitFamily)
                        Text("$drivesCount trips", fontSize = 9.sp, color = t.textFade, fontFamily = SpaceMonoFamily)
                    }
                }
            }

            // Places tile (full width)
            HavenCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onPlacesClick
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("PLACES", style = labelStyle(), color = t.textFade)
                        Text(
                            "+ ADD", fontSize = 9.sp, fontWeight = FontWeight.Bold,
                            color = t.accent, fontFamily = SpaceMonoFamily,
                            modifier = Modifier.clickable { onAddPlaceClick() }
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    if (places.isEmpty()) {
                        Text(
                            "No saved places yet. Tap + ADD to create one.",
                            fontSize = 12.sp, color = t.textFade, fontFamily = OutfitFamily
                        )
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            places.take(3).forEach { place ->
                                val placeColor = Color(place.color)
                                // Find members near this place (within ~500m rough check)
                                val nearbyMembers = members.filter { m ->
                                    if (m.latitude == 0.0 && m.longitude == 0.0) false
                                    else {
                                        val dLat = Math.abs(m.latitude - (place.membersPresent.toDouble())) // placeholder
                                        // Simple distance check using coordinate diff (~0.005 = ~500m)
                                        val latDiff = Math.abs(m.latitude)
                                        val lngDiff = Math.abs(m.longitude)
                                        place.membersPresent > 0 || m.currentAddress.contains(place.name, ignoreCase = true)
                                    }
                                }
                                val atPlace = members.filter { m ->
                                    m.currentAddress.isNotEmpty() && (
                                        m.currentAddress.contains(place.name, ignoreCase = true) ||
                                        place.name.contains(m.currentAddress.take(10), ignoreCase = true)
                                    )
                                }
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (t.isDark) t.surfaceAlt else t.bgSub,
                                            RoundedCornerShape(14.dp)
                                        )
                                        .padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(placeColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(Modifier.size(8.dp).background(placeColor, CircleShape))
                                    }
                                    Spacer(Modifier.height(5.dp))
                                    Text(place.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = t.text, fontFamily = OutfitFamily, maxLines = 1)
                                    if (atPlace.isNotEmpty()) {
                                        // Stacked member initials
                                        Row(horizontalArrangement = Arrangement.spacedBy((-4).dp)) {
                                            atPlace.take(3).forEach { m ->
                                                Box(
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .background(Color(m.color), CircleShape)
                                                        .border(1.dp, if (t.isDark) t.surfaceAlt else t.bgSub, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(m.initials, fontSize = 6.sp, fontWeight = FontWeight.Black, color = Color.White)
                                                }
                                            }
                                        }
                                    } else {
                                        Text("${place.membersPresent} here", fontSize = 9.sp, color = t.textFade, fontFamily = SpaceMonoFamily)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Recent activity (full width)
            if (notifications.isNotEmpty()) {
                HavenCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNotificationsClick
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("RECENT", style = labelStyle(), color = t.textFade)
                        Spacer(Modifier.height(8.dp))
                        notifications.take(3).forEach { notif ->
                            Row(
                                modifier = Modifier.padding(bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(Modifier.size(7.dp).background(Color(notif.color), CircleShape))
                                Text(notif.title, modifier = Modifier.weight(1f), fontSize = 12.sp, color = t.textMid, fontFamily = OutfitFamily)
                                Text(notif.timeAgo().split(" ")[0], fontSize = 9.sp, color = t.textFade, fontFamily = SpaceMonoFamily)
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

private fun FamilyMember.lastSeenText(): String {
    val diffMs = System.currentTimeMillis() - lastSeenTimestamp
    val minutes = diffMs / 60_000
    return when {
        minutes < 1 -> "Now"
        minutes < 60 -> "${minutes}m"
        minutes < 1440 -> "${minutes / 60}h"
        else -> "${minutes / 1440}d"
    }
}

@Composable
private fun labelStyle() = androidx.compose.ui.text.TextStyle(
    fontSize = 10.sp,
    fontWeight = FontWeight.Bold,
    fontFamily = SpaceMonoFamily,
    letterSpacing = 1.5.sp
)

