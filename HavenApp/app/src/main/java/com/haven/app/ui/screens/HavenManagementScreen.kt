package com.haven.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.haven.app.data.model.FamilyMember
import com.haven.app.ui.components.HavenCard
import com.haven.app.ui.theme.LocalHavenColors
import com.haven.app.ui.theme.OutfitFamily
import com.haven.app.ui.theme.SpaceMonoFamily
import com.haven.app.ui.viewmodel.ProfileViewModel

@Composable
fun HavenManagementScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val t = LocalHavenColors.current
    val focusManager = LocalFocusManager.current
    val clipboardManager = LocalClipboardManager.current
    val familyName by viewModel.familyName.collectAsStateWithLifecycle()
    val inviteCode by viewModel.inviteCode.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()

    var editingName by remember { mutableStateOf(false) }
    var nameDraft by remember(familyName) { mutableStateOf(familyName) }
    var codeCopied by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
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
                "Circle", fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold, color = t.text,
                fontFamily = OutfitFamily, letterSpacing = (-0.5).sp
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Circle name card
            HavenCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "CIRCLE NAME", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        color = t.textFade, fontFamily = SpaceMonoFamily, letterSpacing = 1.5.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    if (editingName) {
                        OutlinedTextField(
                            value = nameDraft,
                            onValueChange = { nameDraft = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = t.accent,
                                unfocusedBorderColor = t.border,
                                focusedContainerColor = if (t.isDark) t.surfaceAlt else t.bgSub,
                                unfocusedContainerColor = if (t.isDark) t.surfaceAlt else t.bgSub,
                                cursorColor = t.accent,
                                focusedTextColor = t.text,
                                unfocusedTextColor = t.text,
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                viewModel.updateFamilyName(nameDraft)
                                editingName = false
                                focusManager.clearFocus()
                            }),
                            trailingIcon = {
                                Icon(
                                    Icons.Outlined.Check, "Save",
                                    modifier = Modifier.clickable {
                                        viewModel.updateFamilyName(nameDraft)
                                        editingName = false
                                        focusManager.clearFocus()
                                    },
                                    tint = t.accent
                                )
                            }
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.clickable { editingName = true }
                        ) {
                            Text(
                                familyName.ifEmpty { "Tap to set circle name" },
                                fontSize = 20.sp, fontWeight = FontWeight.ExtraBold,
                                color = if (familyName.isEmpty()) t.textFade else t.text,
                                fontFamily = OutfitFamily, letterSpacing = (-0.3).sp
                            )
                            Icon(Icons.Outlined.Edit, "Edit", Modifier.size(16.dp), tint = t.textFade)
                        }
                    }
                }
            }

            // Invite code card
            if (inviteCode.isNotEmpty()) {
                HavenCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        clipboardManager.setText(AnnotatedString(inviteCode))
                        codeCopied = true
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "INVITE CODE", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            color = t.textFade, fontFamily = SpaceMonoFamily, letterSpacing = 1.5.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            inviteCode, fontSize = 28.sp, fontWeight = FontWeight.Black,
                            color = t.accent, fontFamily = SpaceMonoFamily, letterSpacing = 6.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (codeCopied) Icons.Outlined.Check else Icons.Outlined.ContentCopy,
                                "Copy",
                                modifier = Modifier.size(13.dp),
                                tint = if (codeCopied) t.ok else t.textMid
                            )
                            Text(
                                if (codeCopied) "Copied!" else "Tap to copy \u2022 Share with family to join",
                                fontSize = 11.sp,
                                color = if (codeCopied) t.ok else t.textMid,
                                fontFamily = OutfitFamily
                            )
                        }
                    }
                }
            }

            // Share invite action
            HavenCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    clipboardManager.setText(AnnotatedString(inviteCode))
                    codeCopied = true
                }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(t.accentBg, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.PersonAdd, "Add member", Modifier.size(20.dp), tint = t.accent)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Add Members", fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold, color = t.text, fontFamily = OutfitFamily
                        )
                        Text(
                            "Share your invite code so family can join",
                            fontSize = 10.sp, color = t.textFade, fontFamily = OutfitFamily
                        )
                    }
                    Icon(Icons.Outlined.Share, "Share", Modifier.size(16.dp), tint = t.accent)
                }
            }

            // Members section
            Text(
                "MEMBERS  ${members.size}", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                color = t.textFade, fontFamily = SpaceMonoFamily, letterSpacing = 1.5.sp
            )

            if (members.isEmpty()) {
                HavenCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "No members yet. Share your invite code to invite family.",
                        modifier = Modifier.padding(20.dp),
                        fontSize = 13.sp, color = t.textMid, fontFamily = OutfitFamily, lineHeight = 20.sp
                    )
                }
            } else {
                members.forEach { member ->
                    key(member.id) {
                        HavenMemberCard(member = member, t = t)
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun HavenMemberCard(member: FamilyMember, t: com.haven.app.ui.theme.HavenColors) {
    val memberColor = Color(member.color)
    HavenCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(modifier = Modifier.size(44.dp)) {
                if (member.photoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = member.photoUrl,
                        contentDescription = member.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(2.dp, memberColor, RoundedCornerShape(14.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(memberColor.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                            .border(2.dp, memberColor, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            member.initials, fontSize = 18.sp,
                            fontWeight = FontWeight.Black, color = memberColor,
                            fontFamily = OutfitFamily
                        )
                    }
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    member.name, fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold, color = t.text,
                    fontFamily = OutfitFamily
                )
                Text(
                    member.status.displayName(),
                    fontSize = 10.sp, color = t.textFade,
                    fontFamily = SpaceMonoFamily
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            if (member.isOnline) t.ok else t.textFade,
                            CircleShape
                        )
                )
                Text(
                    if (member.isOnline) "ONLINE" else "OFFLINE",
                    fontSize = 9.sp, color = if (member.isOnline) t.ok else t.textFade,
                    fontFamily = SpaceMonoFamily
                )
            }
        }
    }
}
