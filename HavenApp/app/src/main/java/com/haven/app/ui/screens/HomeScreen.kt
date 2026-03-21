package com.haven.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.haven.app.data.model.FamilyMember
import com.haven.app.ui.components.HavenCard
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
    val recentErrands by viewModel.recentErrands.collectAsStateWithLifecycle()
    val myRole by viewModel.myRole.collectAsStateWithLifecycle()
    val currentlyDriving by viewModel.currentlyDriving.collectAsStateWithLifecycle()
    val recentCheckins by viewModel.recentCheckins.collectAsStateWithLifecycle()
    val memberColors by viewModel.memberColors.collectAsStateWithLifecycle()
    val isAdmin = myRole == "ADMIN"
    val label = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = SpaceMonoFamily, letterSpacing = 1.5.sp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ── Status ──
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(6.dp).background(t.ok, CircleShape))
            Text(
                if (members.isEmpty()) "Getting started"
                else if (members.all { it.isOnline }) "Everyone's safe"
                else "${members.count { it.isOnline }} of ${members.size} online",
                fontSize = 11.sp, color = t.textMid, fontFamily = OutfitFamily
            )
        }

        // ── Haven Switcher ──
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val activeId = viewModel.activeHavenId
            if (myHavens.isNotEmpty()) {
                myHavens.forEach { haven ->
                    val isActive = haven.havenId == activeId
                    Box(
                        modifier = Modifier
                            .background(if (isActive) t.accent.copy(alpha = 0.1f) else t.card, RoundedCornerShape(12.dp))
                            .border(if (isActive) 1.5.dp else 1.dp, if (isActive) t.accent else t.border, RoundedCornerShape(12.dp))
                            .clickable { viewModel.switchHaven(haven.havenId) }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(haven.havenName, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            color = if (isActive) t.accent else t.textMid, fontFamily = OutfitFamily)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .background(t.accent.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .border(1.5.dp, t.accent, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(familyName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = t.accent, fontFamily = OutfitFamily)
                }
            }
        }
        Spacer(Modifier.height(4.dp))

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── Welcome Card (no members) ──
            if (members.isEmpty()) {
                HavenCard(modifier = Modifier.fillMaxWidth(), onClick = onProfileClick) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.GroupAdd, "Welcome", Modifier.size(36.dp), tint = t.accent)
                        Spacer(Modifier.height(12.dp))
                        Text("Welcome to Haven", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = t.text, fontFamily = OutfitFamily)
                        Spacer(Modifier.height(6.dp))
                        Text("Set up your profile and share your invite code to add family.", fontSize = 13.sp, color = t.textMid, fontFamily = OutfitFamily, lineHeight = 20.sp, textAlign = TextAlign.Center)
                    }
                }
            }

            // ── Family Members ──
            if (members.isNotEmpty()) {
                HavenCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("FAMILY", style = label, color = t.textFade)
                            Text("${members.count { it.isOnline }} ONLINE", style = label, color = t.ok)
                        }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(t.border))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            val displayMembers = members.take(5)
                            val overflow = members.size - displayMembers.size
                            displayMembers.forEachIndexed { index, member ->
                                val mc = Color(member.color)
                                val isLast = index == displayMembers.size - 1 && overflow == 0
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onMemberClick(member) }
                                        .then(
                                            if (!isLast) Modifier.drawBehind {
                                                drawLine(t.border, Offset(size.width, 0f), Offset(size.width, size.height), 1f)
                                            } else Modifier
                                        )
                                        .padding(vertical = 14.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(modifier = Modifier.size(40.dp)) {
                                        Box(
                                            modifier = Modifier.size(40.dp).background(mc.copy(alpha = 0.08f), RoundedCornerShape(13.dp)).border(2.5.dp, mc, RoundedCornerShape(13.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(member.initials, fontSize = 16.sp, fontWeight = FontWeight.Black, color = mc, fontFamily = OutfitFamily)
                                        }
                                        if (member.isOnline) {
                                            Box(modifier = Modifier.align(Alignment.BottomEnd).size(10.dp).background(t.ok, CircleShape).border(2.dp, t.card, CircleShape))
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(member.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = t.text, fontFamily = OutfitFamily, maxLines = 1)
                                    val seen = member.lastSeenText()
                                    Text(
                                        if (seen == "Now") "LIVE" else seen,
                                        fontSize = 8.sp, fontWeight = FontWeight.Bold,
                                        color = if (seen == "Now") t.ok else t.textFade, fontFamily = SpaceMonoFamily
                                    )
                                }
                            }
                            if (overflow > 0) {
                                Column(
                                    modifier = Modifier.weight(1f).padding(vertical = 14.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier.size(40.dp).background(t.bgSub, RoundedCornerShape(13.dp)).border(2.5.dp, t.border, RoundedCornerShape(13.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("+$overflow", fontSize = 13.sp, fontWeight = FontWeight.Black, color = t.textMid, fontFamily = OutfitFamily)
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text("more", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = t.textFade, fontFamily = OutfitFamily)
                                }
                            }
                        }
                    }
                }
            }

            // ── Active Drives Banner ──
            if (currentlyDriving.isNotEmpty()) {
                HavenCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(28.dp)
                                    .drawBehind { drawCarIcon(t.warn) }
                            )
                            Text("ACTIVE DRIVES", style = label, color = t.warn)
                        }
                        Spacer(Modifier.height(10.dp))
                        currentlyDriving.forEach { driver ->
                            val dc = Color(driver.color)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(32.dp)
                                        .background(dc.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                        .border(2.dp, dc, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(driver.initials, fontSize = 12.sp, fontWeight = FontWeight.Black, color = dc, fontFamily = OutfitFamily)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(driver.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = t.text, fontFamily = OutfitFamily)
                                    Text(
                                        driver.currentAddress.ifEmpty { "On the road" },
                                        fontSize = 10.sp, color = t.textMid, fontFamily = OutfitFamily,
                                        maxLines = 1, overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(t.warn.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        "${driver.speed.toInt()} mph",
                                        fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                        color = t.warn, fontFamily = SpaceMonoFamily
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Quick Actions Row ──
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // SOS
                Box(
                    modifier = Modifier.weight(1f).heightIn(min = 100.dp).clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFFDC2626), Color(0xFF991B1B))))
                        .clickable { onSosClick() }.padding(16.dp),
                ) {
                    Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier.size(24.dp)
                                .drawBehind { drawSosIcon(Color.White.copy(alpha = 0.8f)) }
                        )
                        Column {
                            Text("SOS", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White, fontFamily = OutfitFamily)
                            Text("EMERGENCY", fontSize = 8.sp, color = Color.White.copy(alpha = 0.5f), fontFamily = SpaceMonoFamily)
                        }
                    }
                }
                // Map
                HavenCard(modifier = Modifier.weight(1f).heightIn(min = 100.dp), onClick = onMapClick) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Box(
                            modifier = Modifier.size(24.dp)
                                .drawBehind { drawMapPinIcon(t.accent) }
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Map", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = t.text, fontFamily = OutfitFamily)
                        Text("VIEW FAMILY", fontSize = 8.sp, color = t.textFade, fontFamily = SpaceMonoFamily)
                    }
                }
                // Driving
                HavenCard(modifier = Modifier.weight(1f).heightIn(min = 100.dp), onClick = onSafetyClick) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Box(
                            modifier = Modifier.size(24.dp)
                                .drawBehind { drawCarIcon(t.accent) }
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Trips", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = t.text, fontFamily = OutfitFamily)
                        Text("$drivesCount DRIVES", fontSize = 8.sp, color = t.textFade, fontFamily = SpaceMonoFamily)
                    }
                }
            }

            // ── Recent Check-Ins ──
            if (recentCheckins.isNotEmpty()) {
                HavenCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(20.dp)
                                    .drawBehind { drawCheckinIcon(t.accent) }
                            )
                            Text("RECENT CHECK-INS", style = label, color = t.textFade)
                        }
                        Spacer(Modifier.height(10.dp))
                        recentCheckins.forEach { checkin ->
                            val sc = Color(memberColors[checkin.senderName] ?: 0xFF999999)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(28.dp)
                                        .background(sc.copy(alpha = 0.1f), RoundedCornerShape(9.dp))
                                        .border(1.5.dp, sc, RoundedCornerShape(9.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(checkin.emoji, fontSize = 14.sp)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        checkin.senderName,
                                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                                        color = t.text, fontFamily = OutfitFamily
                                    )
                                    if (checkin.message.isNotEmpty()) {
                                        Text(
                                            checkin.message,
                                            fontSize = 10.sp, color = t.textMid, fontFamily = OutfitFamily,
                                            maxLines = 1, overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                val timeAgo = checkinTimeAgo(checkin.timestamp.toLong())
                                Text(timeAgo, fontSize = 9.sp, color = t.textFade, fontFamily = SpaceMonoFamily)
                            }
                        }
                    }
                }
            }

            // ── Places ──
            HavenCard(modifier = Modifier.fillMaxWidth(), onClick = onPlacesClick) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("PLACES", style = label, color = t.textFade)
                        if (isAdmin) {
                            Text("+ ADD", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = t.accent, fontFamily = SpaceMonoFamily,
                                modifier = Modifier.clickable { onAddPlaceClick() })
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    if (places.isEmpty()) {
                        Text("No saved places. Tap + ADD to create one.", fontSize = 12.sp, color = t.textFade, fontFamily = OutfitFamily)
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            places.take(4).forEach { place ->
                                val pc = Color(place.color)
                                Column(
                                    modifier = Modifier.weight(1f).background(if (t.isDark) t.surfaceAlt else t.bgSub, RoundedCornerShape(14.dp)).padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(modifier = Modifier.size(28.dp).background(pc.copy(alpha = 0.12f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                        Box(Modifier.size(8.dp).background(pc, CircleShape))
                                    }
                                    Spacer(Modifier.height(5.dp))
                                    Text(place.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = t.text, fontFamily = OutfitFamily, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${place.membersPresent} here", fontSize = 8.sp, color = t.textFade, fontFamily = SpaceMonoFamily)
                                }
                            }
                        }
                    }
                }
            }

            // ── Recent Activity ──
            if (notifications.isNotEmpty()) {
                HavenCard(modifier = Modifier.fillMaxWidth(), onClick = onNotificationsClick) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("RECENT ACTIVITY", style = label, color = t.textFade)
                        Spacer(Modifier.height(10.dp))
                        notifications.take(4).forEach { notif ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(Modifier.size(8.dp).background(Color(notif.color), CircleShape))
                                Text(notif.title, modifier = Modifier.weight(1f), fontSize = 12.sp, color = t.text, fontFamily = OutfitFamily, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(notif.timeAgo(), fontSize = 9.sp, color = t.textFade, fontFamily = SpaceMonoFamily)
                            }
                        }
                    }
                }
            }

            // ── Recent Errands ──
            if (recentErrands.isNotEmpty()) {
                HavenCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("ERRANDS", style = label, color = t.textFade)
                        Spacer(Modifier.height(10.dp))
                        recentErrands.forEach { errand ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(28.dp).background(
                                        if (errand.status == "ACCEPTED") t.ok.copy(alpha = 0.1f) else t.warn.copy(alpha = 0.1f),
                                        RoundedCornerShape(8.dp)
                                    ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        if (errand.status == "ACCEPTED") Icons.Outlined.Check else Icons.Outlined.Assignment,
                                        null, Modifier.size(14.dp),
                                        tint = if (errand.status == "ACCEPTED") t.ok else t.warn
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(errand.item, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = t.text, fontFamily = OutfitFamily, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(
                                        if (errand.status == "ACCEPTED") "${errand.acceptedName} is on it"
                                        else "${errand.senderName} needs this",
                                        fontSize = 9.sp, color = if (errand.status == "ACCEPTED") t.ok else t.textFade, fontFamily = SpaceMonoFamily
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
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

private fun checkinTimeAgo(timestamp: Long): String {
    val diffMs = System.currentTimeMillis() - timestamp
    val minutes = diffMs / 60_000
    return when {
        minutes < 1 -> "now"
        minutes < 60 -> "${minutes}m"
        minutes < 1440 -> "${minutes / 60}h"
        else -> "${minutes / 1440}d"
    }
}

// ── Custom drawn icons ──

private fun DrawScope.drawSosIcon(color: Color) {
    val w = size.width; val h = size.height
    val cx = w / 2f; val cy = h / 2f
    val s = Stroke(width = w * 0.1f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    // Shield outline
    val shield = Path().apply {
        moveTo(cx, h * 0.05f)
        cubicTo(w * 0.15f, h * 0.05f, w * 0.08f, h * 0.15f, w * 0.08f, h * 0.35f)
        cubicTo(w * 0.08f, h * 0.65f, w * 0.25f, h * 0.82f, cx, h * 0.95f)
        cubicTo(w * 0.75f, h * 0.82f, w * 0.92f, h * 0.65f, w * 0.92f, h * 0.35f)
        cubicTo(w * 0.92f, h * 0.15f, w * 0.85f, h * 0.05f, cx, h * 0.05f)
        close()
    }
    drawPath(shield, color, style = s)
    // Exclamation
    drawLine(color, Offset(cx, h * 0.28f), Offset(cx, h * 0.58f), strokeWidth = w * 0.11f, cap = StrokeCap.Round)
    drawCircle(color, radius = w * 0.06f, center = Offset(cx, h * 0.73f))
}

private fun DrawScope.drawMapPinIcon(color: Color) {
    val w = size.width; val h = size.height
    val cx = w / 2f
    val s = Stroke(width = w * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    // Pin body — proper teardrop
    val pin = Path().apply {
        moveTo(cx, h * 0.92f)
        cubicTo(cx - w * 0.08f, h * 0.68f, w * 0.1f, h * 0.5f, w * 0.1f, h * 0.36f)
        cubicTo(w * 0.1f, h * 0.12f, w * 0.28f, h * 0.04f, cx, h * 0.04f)
        cubicTo(w * 0.72f, h * 0.04f, w * 0.9f, h * 0.12f, w * 0.9f, h * 0.36f)
        cubicTo(w * 0.9f, h * 0.5f, cx + w * 0.08f, h * 0.68f, cx, h * 0.92f)
        close()
    }
    drawPath(pin, color, style = s)
    // Inner dot
    drawCircle(color, radius = w * 0.14f, center = Offset(cx, h * 0.35f))
}

private fun DrawScope.drawCarIcon(color: Color) {
    val w = size.width; val h = size.height
    val s = Stroke(width = w * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    // Cabin (windshield shape)
    val cabin = Path().apply {
        moveTo(w * 0.22f, h * 0.45f)
        lineTo(w * 0.32f, h * 0.2f)
        lineTo(w * 0.68f, h * 0.2f)
        lineTo(w * 0.78f, h * 0.45f)
    }
    drawPath(cabin, color, style = s)
    // Body
    drawRoundRect(color, Offset(w * 0.06f, h * 0.42f), androidx.compose.ui.geometry.Size(w * 0.88f, h * 0.28f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.08f), style = s)
    // Wheels
    drawCircle(color, radius = w * 0.1f, center = Offset(w * 0.28f, h * 0.76f), style = Stroke(width = w * 0.08f))
    drawCircle(color, radius = w * 0.1f, center = Offset(w * 0.72f, h * 0.76f), style = Stroke(width = w * 0.08f))
    // Headlights
    drawCircle(color, radius = w * 0.04f, center = Offset(w * 0.14f, h * 0.52f))
    drawCircle(color, radius = w * 0.04f, center = Offset(w * 0.86f, h * 0.52f))
}

private fun DrawScope.drawCheckinIcon(color: Color) {
    val w = size.width; val h = size.height
    val s = Stroke(width = w * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    // Speech bubble with heart
    val bubble = Path().apply {
        moveTo(w * 0.5f, h * 0.9f)
        lineTo(w * 0.28f, h * 0.72f)
        lineTo(w * 0.14f, h * 0.72f)
        cubicTo(w * 0.06f, h * 0.72f, w * 0.06f, h * 0.64f, w * 0.06f, h * 0.58f)
        lineTo(w * 0.06f, h * 0.16f)
        cubicTo(w * 0.06f, h * 0.06f, w * 0.14f, h * 0.06f, w * 0.18f, h * 0.06f)
        lineTo(w * 0.82f, h * 0.06f)
        cubicTo(w * 0.86f, h * 0.06f, w * 0.94f, h * 0.06f, w * 0.94f, h * 0.16f)
        lineTo(w * 0.94f, h * 0.58f)
        cubicTo(w * 0.94f, h * 0.64f, w * 0.94f, h * 0.72f, w * 0.86f, h * 0.72f)
        lineTo(w * 0.5f, h * 0.72f)
        close()
    }
    drawPath(bubble, color, style = s)
    // Waving hand lines (3 arcs)
    drawLine(color, Offset(w * 0.38f, h * 0.28f), Offset(w * 0.38f, h * 0.52f), strokeWidth = w * 0.07f, cap = StrokeCap.Round)
    drawLine(color, Offset(w * 0.52f, h * 0.22f), Offset(w * 0.52f, h * 0.52f), strokeWidth = w * 0.07f, cap = StrokeCap.Round)
    drawLine(color, Offset(w * 0.66f, h * 0.28f), Offset(w * 0.66f, h * 0.52f), strokeWidth = w * 0.07f, cap = StrokeCap.Round)
}
