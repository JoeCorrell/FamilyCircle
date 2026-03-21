package com.haven.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.haven.app.ui.components.ProfileImage
import com.haven.app.data.model.FamilyMember
import com.haven.app.ui.components.BatteryIndicator
import com.haven.app.ui.components.HavenCard
import com.haven.app.ui.theme.LocalHavenColors
import com.haven.app.ui.theme.OutfitFamily
import com.haven.app.ui.theme.SpaceMonoFamily
import com.haven.app.ui.viewmodel.MemberDetailViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun MemberDetailScreen(
    member: FamilyMember,
    onBack: () -> Unit,
    onChatClick: () -> Unit = {},
    detailViewModel: MemberDetailViewModel = hiltViewModel()
) {
    val t = LocalHavenColors.current
    val context = LocalContext.current
    val memberColor = Color(member.color)
    var checkInSent by remember { mutableStateOf(false) }
    val locationHistory by detailViewModel.locationHistory.collectAsStateWithLifecycle()
    val isLoadingHistory by detailViewModel.isLoadingHistory.collectAsStateWithLifecycle()

    // Load location history on first composition
    LaunchedEffect(member.id) {
        detailViewModel.loadLocationHistory(member.serverId)
    }

    fun call() {
        if (member.phoneNumber.isNotEmpty()) {
            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${member.phoneNumber}")))
        }
    }

    fun navigate() {
        if (member.latitude != 0.0 || member.longitude != 0.0) {
            val uri = Uri.parse("geo:${member.latitude},${member.longitude}?q=${member.latitude},${member.longitude}(${Uri.encode(member.name)})")
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Text(
            member.name, fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold, color = t.text,
            fontFamily = OutfitFamily, letterSpacing = (-0.5).sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )

        Column(
            modifier = Modifier.padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Hero card
            HavenCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (member.photoUrl.isNotEmpty()) {
                        ProfileImage(
                            photoUrl = member.photoUrl,
                            contentDescription = member.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .border(3.dp, memberColor, RoundedCornerShape(22.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(memberColor.copy(alpha = 0.1f), RoundedCornerShape(22.dp))
                                .border(3.dp, memberColor, RoundedCornerShape(22.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                member.initials, fontSize = 30.sp,
                                fontWeight = FontWeight.Black, color = memberColor,
                                fontFamily = OutfitFamily
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .background(memberColor.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                            .border(1.dp, memberColor.copy(alpha = 0.16f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                    ) {
                        Text(
                            member.status.displayName().uppercase(),
                            fontSize = 10.5.sp, fontWeight = FontWeight.Bold,
                            color = memberColor, fontFamily = SpaceMonoFamily,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                data class Action(val label: String, val icon: ImageVector, val onClick: () -> Unit, val enabled: Boolean = true)
                listOf(
                    Action("Call", Icons.Outlined.Phone, ::call, member.phoneNumber.isNotEmpty()),
                    Action("Navigate", Icons.Outlined.NearMe, ::navigate, member.latitude != 0.0 || member.longitude != 0.0),
                    Action("Message", Icons.Outlined.ChatBubbleOutline, onChatClick)
                ).forEach { action ->
                    HavenCard(
                        modifier = Modifier.weight(1f),
                        onClick = action.onClick
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                action.icon, action.label, Modifier.size(18.dp),
                                tint = if (action.enabled) t.accent else t.textFade
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                action.label.uppercase(),
                                fontSize = 9.5.sp, fontWeight = FontWeight.Bold,
                                color = if (action.enabled) t.textMid else t.textFade,
                                fontFamily = SpaceMonoFamily
                            )
                        }
                    }
                }
            }

            // Location (full width)
            HavenCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(20.dp)
                                .drawBehind { drawLocationPinIcon(t.accent) }
                        )
                        Text(
                            "LOCATION", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            color = t.textFade, fontFamily = SpaceMonoFamily, letterSpacing = 1.5.sp
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        member.currentAddress.ifEmpty { "Unknown" },
                        fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                        color = t.text, fontFamily = OutfitFamily
                    )
                    Spacer(Modifier.height(3.dp))
                    val diffMs = System.currentTimeMillis() - member.lastSeenTimestamp
                    val minutes = diffMs / 60_000
                    Text(
                        if (minutes < 1) "LIVE" else "${minutes}m AGO",
                        fontSize = 10.sp, color = if (minutes < 1) t.ok else t.textFade, fontFamily = SpaceMonoFamily
                    )
                }
            }

            // Battery + Speed row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HavenCard(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "BATTERY", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            color = t.textFade, fontFamily = SpaceMonoFamily, letterSpacing = 1.5.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        BatteryIndicator(value = member.batteryLevel, size = 40.dp)
                    }
                }
                HavenCard(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "SPEED", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            color = t.textFade, fontFamily = SpaceMonoFamily, letterSpacing = 1.5.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${member.speed.toInt()}",
                            fontSize = 26.sp, fontWeight = FontWeight.Black,
                            color = if (member.speed > 0) t.warn else t.ok,
                            fontFamily = SpaceMonoFamily
                        )
                        Text("MPH", fontSize = 9.sp, color = t.textFade, fontFamily = SpaceMonoFamily)
                    }
                }
            }

            // ── Location History Timeline ──
            HavenCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(20.dp)
                                .drawBehind { drawTimelineIcon(t.accent) }
                        )
                        Text(
                            "TIMELINE", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            color = t.textFade, fontFamily = SpaceMonoFamily, letterSpacing = 1.5.sp
                        )
                    }
                    Spacer(Modifier.height(12.dp))

                    if (isLoadingHistory) {
                        Text("Loading...", fontSize = 12.sp, color = t.textFade, fontFamily = OutfitFamily)
                    } else if (locationHistory.isEmpty()) {
                        // Show current status as fallback
                        val lastSeenText = if (member.isOnline) "Now"
                        else {
                            val diff = System.currentTimeMillis() - member.lastSeenTimestamp
                            val mins = (diff / 60000).toInt()
                            when {
                                mins < 1 -> "Just now"
                                mins < 60 -> "${mins}m ago"
                                mins < 1440 -> "${mins / 60}h ago"
                                else -> "${mins / 1440}d ago"
                            }
                        }
                        TimelineNode(
                            label = "${member.status.displayName()} \u2014 $lastSeenText",
                            detail = member.currentAddress.ifEmpty { "Unknown location" },
                            isLast = true,
                            dotColor = t.accent,
                            t = t
                        )
                    } else {
                        locationHistory.take(10).forEachIndexed { index, entry ->
                            val isLast = index == minOf(locationHistory.size - 1, 9)
                            val timeAgo = run {
                                val mins = ((System.currentTimeMillis() - entry.timestamp) / 60000).toInt()
                                when {
                                    mins < 1 -> "now"
                                    mins < 60 -> "${mins}m ago"
                                    mins < 1440 -> "${mins / 60}h ago"
                                    else -> "${mins / 1440}d ago"
                                }
                            }
                            val statusName = try {
                                entry.status.lowercase().replaceFirstChar { it.uppercase() }
                            } catch (_: Exception) { "Unknown" }

                            TimelineNode(
                                label = entry.address.ifEmpty { "Unknown location" },
                                detail = buildString {
                                    append(statusName)
                                    if (entry.speed > 2) append(" \u2022 ${entry.speed.toInt()} mph")
                                    append(" \u2022 $timeAgo")
                                },
                                isLast = isLast,
                                dotColor = if (index == 0) t.accent else t.accent.copy(alpha = 0.3f),
                                t = t,
                                speedBadge = if (entry.speed > 15) "${entry.speed.toInt()} mph" else null
                            )
                        }
                    }
                }
            }

            // Check-in button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .then(
                        if (checkInSent)
                            Modifier
                                .background(t.ok.copy(alpha = 0.07f))
                                .border(1.5.dp, t.ok.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        else
                            Modifier.background(Brush.linearGradient(listOf(t.accent, t.accentMid)))
                    )
                    .clickable {
                        if (!checkInSent) {
                            detailViewModel.sendCheckIn(member.name)
                            checkInSent = true
                        }
                    }
                    .padding(15.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (checkInSent) "\u2713 Check-In Sent" else "Request Check-In",
                    fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    color = if (checkInSent) t.ok else Color.White,
                    fontFamily = OutfitFamily
                )
            }
        }
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun TimelineNode(
    label: String,
    detail: String,
    isLast: Boolean,
    dotColor: Color,
    t: com.haven.app.ui.theme.HavenColors,
    speedBadge: String? = null
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(dotColor, CircleShape)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(32.dp)
                        .background(t.accent.copy(alpha = 0.13f), RoundedCornerShape(1.dp))
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    label, fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = t.text, fontFamily = OutfitFamily,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (speedBadge != null) {
                    Box(
                        modifier = Modifier
                            .background(t.warn.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(speedBadge, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = t.warn, fontFamily = SpaceMonoFamily)
                    }
                }
            }
            Text(
                detail, fontSize = 10.sp,
                color = t.textFade, fontFamily = SpaceMonoFamily
            )
            if (!isLast) Spacer(Modifier.height(4.dp))
        }
    }
}

// ── Custom drawn icons ──

private fun DrawScope.drawLocationPinIcon(color: Color) {
    val w = size.width; val h = size.height; val cx = w / 2f
    val s = Stroke(width = w * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    // Pin body — teardrop
    val pin = Path().apply {
        moveTo(cx, h * 0.92f)
        cubicTo(cx - w * 0.08f, h * 0.68f, w * 0.1f, h * 0.5f, w * 0.1f, h * 0.36f)
        cubicTo(w * 0.1f, h * 0.12f, w * 0.28f, h * 0.04f, cx, h * 0.04f)
        cubicTo(w * 0.72f, h * 0.04f, w * 0.9f, h * 0.12f, w * 0.9f, h * 0.36f)
        cubicTo(w * 0.9f, h * 0.5f, cx + w * 0.08f, h * 0.68f, cx, h * 0.92f)
        close()
    }
    drawPath(pin, color, style = s)
    // Inner ring
    drawCircle(color, radius = w * 0.16f, center = Offset(cx, h * 0.35f), style = Stroke(width = w * 0.07f))
    drawCircle(color, radius = w * 0.05f, center = Offset(cx, h * 0.35f))
}

private fun DrawScope.drawTimelineIcon(color: Color) {
    val w = size.width; val h = size.height
    val lineX = w * 0.25f
    val sw = w * 0.07f
    // Vertical connector
    drawLine(color.copy(alpha = 0.3f), Offset(lineX, h * 0.12f), Offset(lineX, h * 0.88f), strokeWidth = sw, cap = StrokeCap.Round)
    // Nodes (3 dots, decreasing opacity)
    val nodes = listOf(0.18f to 1f, 0.5f to 0.6f, 0.82f to 0.35f)
    nodes.forEach { (y, alpha) ->
        drawCircle(color.copy(alpha = alpha), radius = w * 0.1f, center = Offset(lineX, h * y))
    }
    // Lines from nodes
    val lines = listOf(0.18f to 0.88f, 0.5f to 0.72f, 0.82f to 0.58f)
    lines.forEachIndexed { i, (y, endX) ->
        drawLine(color.copy(alpha = nodes[i].second), Offset(w * 0.42f, h * y), Offset(w * endX, h * y),
            strokeWidth = sw, cap = StrokeCap.Round)
    }
}
