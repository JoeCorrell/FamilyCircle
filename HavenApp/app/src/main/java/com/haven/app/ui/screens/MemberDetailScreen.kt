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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
        // Header with back button
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(t.accentBg, RoundedCornerShape(10.dp))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.ChevronLeft, "Back", Modifier.size(16.dp), tint = t.accent)
            }
            Text(
                member.name, fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold, color = t.text,
                fontFamily = OutfitFamily, letterSpacing = (-0.5).sp
            )
        }

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
                    Text(
                        "LOCATION", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        color = t.textFade, fontFamily = SpaceMonoFamily, letterSpacing = 1.5.sp
                    )
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
                        fontSize = 10.sp, color = t.textFade, fontFamily = SpaceMonoFamily
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

            // Timeline
            HavenCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Text(
                        "TIMELINE", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        color = t.textFade, fontFamily = SpaceMonoFamily, letterSpacing = 1.5.sp
                    )
                    Spacer(Modifier.height(10.dp))
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
                    val timelineItems = listOf(
                        "${member.status.name.lowercase().replaceFirstChar { it.uppercase() }} \u2014 $lastSeenText",
                        "${member.currentAddress.ifEmpty { "Unknown location" }}"
                    )
                    timelineItems.forEachIndexed { index, item ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(
                                            if (index == timelineItems.lastIndex) t.accent
                                            else t.accent.copy(alpha = 0.2f),
                                            CircleShape
                                        )
                                )
                                if (index < timelineItems.lastIndex) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(18.dp)
                                            .background(t.accent.copy(alpha = 0.13f), RoundedCornerShape(1.dp))
                                    )
                                }
                            }
                            Text(
                                item, fontSize = 12.sp,
                                color = if (index == timelineItems.lastIndex) t.text else t.textMid,
                                fontWeight = if (index == timelineItems.lastIndex) FontWeight.SemiBold else FontWeight.Normal,
                                fontFamily = OutfitFamily
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
