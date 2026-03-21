package com.haven.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.haven.app.ui.components.ProfileImage
import com.haven.app.ui.components.HavenCard
import com.haven.app.ui.theme.HavenColors
import com.haven.app.ui.theme.LocalHavenColors
import com.haven.app.ui.theme.OutfitFamily
import com.haven.app.ui.theme.SpaceMonoFamily
import com.haven.app.ui.viewmodel.SettingsViewModel

data class SettingsSection(
    val icon: ImageVector,
    val title: String,
    val desc: String,
    val items: List<String>
)

@Composable
fun SettingsScreen(
    onProfileClick: () -> Unit = {},
    onCirclesClick: () -> Unit = {},
    onPlacesClick: () -> Unit = {},
    onThemesClick: () -> Unit = {},
    onSectionClick: (SettingsSection) -> Unit = {},
    onAboutClick: () -> Unit = {},
    onJoinCircleClick: () -> Unit = {},
    onSignOut: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val t = LocalHavenColors.current
    val context = LocalContext.current
    val familyName by viewModel.familyName.collectAsStateWithLifecycle()
    val memberCount by viewModel.memberCount.collectAsStateWithLifecycle()
    val havenCount by viewModel.havenCount.collectAsStateWithLifecycle()
    val driveCount by viewModel.driveCount.collectAsStateWithLifecycle()
    val notificationCount by viewModel.notificationCount.collectAsStateWithLifecycle()
    val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
    val inviteCode by viewModel.inviteCode.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val userPhotoUrl by viewModel.userPhotoUrl.collectAsStateWithLifecycle()
    val userAvatarIcon by viewModel.userAvatarIcon.collectAsStateWithLifecycle()
    val userColor by viewModel.userColor.collectAsStateWithLifecycle()
    val userPhone by viewModel.userPhone.collectAsStateWithLifecycle()

    val avatarIconMap = mapOf(
        "Person" to Icons.Outlined.Person, "Face" to Icons.Outlined.Face,
        "Pets" to Icons.Outlined.Pets, "Star" to Icons.Outlined.Star,
        "Favorite" to Icons.Outlined.Favorite, "School" to Icons.Outlined.School,
        "Work" to Icons.Outlined.WorkOutline, "Sports" to Icons.Outlined.SportsEsports,
        "Music" to Icons.Outlined.MusicNote, "Flower" to Icons.Outlined.LocalFlorist,
        "Bolt" to Icons.Outlined.Bolt, "Diamond" to Icons.Outlined.Diamond,
    )

    val notificationsSection = SettingsSection(
        Icons.Outlined.Notifications, "Notifications", "Alerts & sounds",
        listOf("Push Alerts", "Location Alerts", "Battery Alerts", "Speed Alerts", "Quiet Hours")
    )
    val locationSection = SettingsSection(
        Icons.Outlined.LocationOn, "Location", "Sharing & precision",
        listOf("Share with Haven", "Precision", "Background Updates", "Wi-Fi Only", "History")
    )
    val privacySection = SettingsSection(
        Icons.Outlined.Lock, "Privacy", "Ghost mode & data",
        listOf("Ghost Mode", "Hide Address", "Block Requests", "Data Sharing", "Clear History")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // ── Profile Hero Card ──
            HavenCard(modifier = Modifier.fillMaxWidth(), onClick = onProfileClick) {
                // Banner gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(Brush.linearGradient(listOf(t.accent, t.accentMid)))
                ) {
                    // Decorative circles (anchored to end, screen-width-safe)
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.CenterEnd)
                            .offset(x = 20.dp, y = (-16).dp)
                            .background(Color.White.copy(alpha = 0.06f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.CenterEnd)
                            .offset(x = (-30).dp, y = 8.dp)
                            .background(Color.White.copy(alpha = 0.04f), CircleShape)
                    )
                    Text(
                        "EDIT PROFILE \u203A",
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 8.dp),
                        fontSize = 9.sp, fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.6f),
                        fontFamily = SpaceMonoFamily, letterSpacing = 1.sp
                    )
                }
                // Avatar + info overlapping banner
                Row(
                    modifier = Modifier
                        .padding(top = 30.dp, start = 20.dp, end = 20.dp, bottom = 18.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .border(3.5.dp, t.card, RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val resolvedIcon = avatarIconMap[userAvatarIcon]
                        val hueColor = Color(userColor)
                        if (userPhotoUrl.isNotEmpty() && userAvatarIcon.isEmpty()) {
                            ProfileImage(
                                photoUrl = userPhotoUrl,
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(14.dp))
                            )
                        } else if (resolvedIcon != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(hueColor.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(resolvedIcon, userAvatarIcon, Modifier.size(26.dp), tint = hueColor)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Brush.linearGradient(listOf(hueColor, t.accentMid))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    (userName.ifEmpty { "?" }).take(1).uppercase(),
                                    fontSize = 22.sp, fontWeight = FontWeight.Black,
                                    color = Color.White, fontFamily = OutfitFamily
                                )
                            }
                        }
                    }
                    Column(modifier = Modifier.padding(top = 28.dp)) {
                        Text(
                            userName.ifEmpty { "You" },
                            fontSize = 17.sp, fontWeight = FontWeight.ExtraBold,
                            color = t.text, fontFamily = OutfitFamily
                        )
                        Text(
                            userPhone.ifEmpty { "No phone" },
                            fontSize = 10.sp, color = t.textFade,
                            fontFamily = SpaceMonoFamily,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // ── Stat Bar ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatCell("$havenCount", "HAVENS", t.accent, t, Modifier.weight(1f))
                StatCell("$memberCount", "MEMBERS", t.ok, t, Modifier.weight(1f))
                StatCell("$driveCount", "DRIVES", t.warn, t, Modifier.weight(1f))
                StatCell("$notificationCount", "ALERTS", t.danger, t, Modifier.weight(1f))
            }

            // ── Quick Actions ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                HavenCard(
                    modifier = Modifier.weight(1f),
                    onClick = onJoinCircleClick
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(t.ok.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Add, "Join", Modifier.size(16.dp), tint = t.ok)
                        }
                        Column {
                            Text("Join Haven", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = t.text, fontFamily = OutfitFamily)
                            Text("INVITE CODE", fontSize = 9.sp, color = t.textFade, fontFamily = SpaceMonoFamily)
                        }
                    }
                }
                HavenCard(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (inviteCode.isNotEmpty()) {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Invite Code", inviteCode))
                            Toast.makeText(context, "Invite code copied!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(t.accentBg, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Share, "Invite", Modifier.size(16.dp), tint = t.accent)
                        }
                        Column {
                            Text("Invite", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = t.text, fontFamily = OutfitFamily)
                            Text("SHARE LINK", fontSize = 9.sp, color = t.textFade, fontFamily = SpaceMonoFamily)
                        }
                    }
                }
            }

            // ── MY HAVEN ──
            SectionLabel("MY HAVEN", t)
            SettingsRow(
                icon = Icons.Outlined.Person, title = "Haven",
                desc = "$memberCount members",
                isFirst = true, onClick = onCirclesClick, t = t
            )
            SettingsRow(
                icon = Icons.Outlined.LocationOn, title = "Saved Places",
                desc = "Your saved locations",
                onClick = onPlacesClick, t = t,
                rightContent = null
            )
            SettingsRow(
                icon = Icons.Outlined.Palette, title = "Appearance",
                desc = currentTheme.name,
                isLast = true, isGrad = true, onClick = onThemesClick, t = t,
                rightContent = {
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        listOf(currentTheme.accent, currentTheme.ok, currentTheme.warn).forEach { c ->
                            Box(modifier = Modifier.size(10.dp).background(c, RoundedCornerShape(4.dp)))
                        }
                    }
                }
            )

            // ── PREFERENCES ──
            SectionLabel("PREFERENCES", t)
            SettingsRow(
                icon = Icons.Outlined.Notifications, title = "Notifications",
                desc = "Alerts, sounds & quiet hours",
                isFirst = true, onClick = { onSectionClick(notificationsSection) }, t = t
            )
            SettingsRow(
                icon = Icons.Outlined.LocationOn, title = "Location",
                desc = "Sharing, precision & history",
                onClick = { onSectionClick(locationSection) }, t = t
            )
            SettingsRow(
                icon = Icons.Outlined.Lock, title = "Privacy & Safety",
                desc = "Ghost mode, data & blocking",
                isLast = true, onClick = { onSectionClick(privacySection) }, t = t
            )

            // ── SUPPORT ──
            SectionLabel("SUPPORT", t)
            SettingsRow(
                icon = Icons.Outlined.HelpOutline, title = "Help Center",
                desc = "FAQs & tutorials",
                isFirst = true,
                onClick = { Toast.makeText(context, "Opening Help Center...", Toast.LENGTH_SHORT).show() },
                t = t
            )
            SettingsRow(
                icon = Icons.Outlined.Email, title = "Contact Us",
                desc = "Get in touch with support",
                onClick = { Toast.makeText(context, "Opening support chat...", Toast.LENGTH_SHORT).show() },
                t = t
            )
            SettingsRow(
                icon = Icons.Outlined.Info, title = "About Haven",
                desc = "v1.0 \u2022 Terms \u2022 Privacy",
                isLast = true, onClick = onAboutClick, t = t
            )

            // ── Sign Out ──
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(t.danger.copy(alpha = 0.05f))
                    .border(1.dp, t.danger.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .clickable { onSignOut() }
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Sign Out", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    color = t.danger, fontFamily = OutfitFamily
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatCell(value: String, label: String, color: Color, t: HavenColors, modifier: Modifier = Modifier) {
    HavenCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = color, fontFamily = SpaceMonoFamily)
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = t.textFade, fontFamily = SpaceMonoFamily, letterSpacing = 1.2.sp)
        }
    }
}

@Composable
private fun SectionLabel(text: String, t: HavenColors) {
    Text(
        text,
        modifier = Modifier.padding(top = 10.dp, bottom = 2.dp, start = 4.dp),
        fontSize = 9.sp, fontWeight = FontWeight.Bold,
        color = t.textFade, fontFamily = SpaceMonoFamily, letterSpacing = 1.5.sp
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    desc: String,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    isGrad: Boolean = false,
    onClick: () -> Unit = {},
    t: HavenColors,
    rightContent: @Composable (() -> Unit)? = null
) {
    val isOnly = isFirst && isLast
    val shape = when {
        isOnly -> RoundedCornerShape(16.dp)
        isFirst -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 6.dp, bottomEnd = 6.dp)
        isLast -> RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
        else -> RoundedCornerShape(6.dp)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, shape, ambientColor = t.border)
            .clip(shape)
            .background(t.card, shape)
            .border(1.dp, t.border, shape)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(
                        if (isGrad) Brush.linearGradient(listOf(t.accent, t.accentMid))
                        else Brush.linearGradient(listOf(t.accentBg, t.accentBg))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, title, modifier = Modifier.size(17.dp), tint = if (isGrad) Color.White else t.accent)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = t.text, fontFamily = OutfitFamily)
                Text(desc, fontSize = 10.sp, color = t.textFade, fontFamily = OutfitFamily)
            }
            rightContent?.invoke()
            Icon(Icons.Outlined.ChevronRight, "Go", modifier = Modifier.size(14.dp), tint = t.textFade)
        }
    }
}
