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
import com.haven.app.ui.components.ProfileImage
import com.haven.app.data.model.FamilyMember
import com.haven.app.ui.components.HavenCard
import com.haven.app.ui.theme.LocalHavenColors
import com.haven.app.ui.theme.OutfitFamily
import com.haven.app.ui.theme.SpaceMonoFamily
import androidx.compose.ui.graphics.Brush
import com.haven.app.data.api.HavenApiManager
import com.haven.app.ui.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

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
    val myRole by viewModel.myRole.collectAsStateWithLifecycle()
    val isAdmin = myRole == "ADMIN"

    var editingName by remember { mutableStateOf(false) }
    var nameDraft by remember(familyName) { mutableStateOf(familyName) }
    var codeCopied by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Text(
            "Haven", fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold, color = t.text,
            fontFamily = OutfitFamily, letterSpacing = (-0.5).sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )

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
                                familyName.ifEmpty { "Tap to set Haven name" },
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
                        HavenMemberCard(
                            member = member, t = t, isAdmin = isAdmin,
                            onKick = { viewModel.kickMember(member.serverId) },
                            onPromote = { viewModel.promoteMember(member.serverId) },
                            onDemote = { viewModel.demoteMember(member.serverId) }
                        )
                    }
                }
            }
            // ── Add / Join Haven ──
            Text(
                "MORE HAVENS", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                color = t.textFade, fontFamily = SpaceMonoFamily, letterSpacing = 1.5.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HavenCard(
                    modifier = Modifier.weight(1f),
                    onClick = { showCreateDialog = true }
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Outlined.Add, "Create", Modifier.size(24.dp), tint = t.accent)
                        Spacer(Modifier.height(6.dp))
                        Text("Create Haven", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = t.text, fontFamily = OutfitFamily)
                        Text("Start a new group", fontSize = 9.sp, color = t.textFade, fontFamily = SpaceMonoFamily)
                    }
                }
                HavenCard(
                    modifier = Modifier.weight(1f),
                    onClick = { showJoinDialog = true }
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Outlined.GroupAdd, "Join", Modifier.size(24.dp), tint = t.ok)
                        Spacer(Modifier.height(6.dp))
                        Text("Join Haven", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = t.text, fontFamily = OutfitFamily)
                        Text("Enter invite code", fontSize = 9.sp, color = t.textFade, fontFamily = SpaceMonoFamily)
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }

    // Create Haven Dialog
    if (showCreateDialog) {
        var newName by remember { mutableStateOf("") }
        var newUserName by remember { mutableStateOf("") }
        var creating by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create New Haven", fontFamily = OutfitFamily, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newName, onValueChange = { newName = it },
                        label = { Text("Haven Name") }, placeholder = { Text("e.g. My Family") },
                        singleLine = true, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newUserName, onValueChange = { newUserName = it },
                        label = { Text("Your Name") }, placeholder = { Text("e.g. Joe") },
                        singleLine = true, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = newName.isNotBlank() && newUserName.isNotBlank() && !creating,
                    onClick = {
                        creating = true
                        scope.launch {
                            viewModel.createNewHaven(newName, newUserName)
                            showCreateDialog = false
                        }
                    }
                ) { Text(if (creating) "Creating..." else "Create", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Join Haven Dialog
    if (showJoinDialog) {
        var joinCode by remember { mutableStateOf("") }
        var joinUserName by remember { mutableStateOf("") }
        var joining by remember { mutableStateOf(false) }
        var joinError by remember { mutableStateOf<String?>(null) }
        val scope = rememberCoroutineScope()

        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            title = { Text("Join Haven", fontFamily = OutfitFamily, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = joinCode, onValueChange = { joinCode = it.uppercase() },
                        label = { Text("Invite Code") }, placeholder = { Text("e.g. ABC123") },
                        singleLine = true, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = joinUserName, onValueChange = { joinUserName = it },
                        label = { Text("Your Name") }, placeholder = { Text("e.g. Joe") },
                        singleLine = true, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
                    )
                    if (joinError != null) {
                        Text(joinError!!, color = LocalHavenColors.current.danger, fontSize = 12.sp, fontFamily = OutfitFamily)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = joinCode.length >= 4 && joinUserName.isNotBlank() && !joining,
                    onClick = {
                        joining = true
                        joinError = null
                        scope.launch {
                            val result = viewModel.joinNewHaven(joinCode, joinUserName)
                            if (result) {
                                showJoinDialog = false
                            } else {
                                joinError = "Invalid invite code"
                                joining = false
                            }
                        }
                    }
                ) { Text(if (joining) "Joining..." else "Join", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showJoinDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun HavenMemberCard(
    member: FamilyMember, t: com.haven.app.ui.theme.HavenColors,
    isAdmin: Boolean = false,
    onKick: () -> Unit = {}, onPromote: () -> Unit = {}, onDemote: () -> Unit = {}
) {
    val memberColor = Color(member.color)
    var showActions by remember { mutableStateOf(false) }

    HavenCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = if (isAdmin) {{ showActions = !showActions }} else null
    ) {
        Column {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(modifier = Modifier.size(44.dp)) {
                    if (member.photoUrl.isNotEmpty()) {
                        ProfileImage(
                            photoUrl = member.photoUrl, contentDescription = member.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).border(2.dp, memberColor, RoundedCornerShape(14.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(44.dp).background(memberColor.copy(alpha = 0.1f), RoundedCornerShape(14.dp)).border(2.dp, memberColor, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(member.initials, fontSize = 18.sp, fontWeight = FontWeight.Black, color = memberColor, fontFamily = OutfitFamily)
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(member.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = t.text, fontFamily = OutfitFamily)
                        if (member.role == "ADMIN") {
                            Box(
                                modifier = Modifier.background(t.accent.copy(alpha = 0.1f), RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text("ADMIN", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = t.accent, fontFamily = SpaceMonoFamily)
                            }
                        }
                    }
                    Text(member.status.displayName(), fontSize = 10.sp, color = t.textFade, fontFamily = SpaceMonoFamily)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(if (member.isOnline) t.ok else t.textFade, CircleShape))
                    Text(if (member.isOnline) "ONLINE" else "OFFLINE", fontSize = 9.sp, color = if (member.isOnline) t.ok else t.textFade, fontFamily = SpaceMonoFamily)
                }
            }

            // Admin actions
            if (isAdmin && showActions) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(if (t.isDark) t.surfaceAlt else t.bgSub).padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (member.role != "ADMIN") {
                        Box(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                                .background(t.accent.copy(alpha = 0.08f))
                                .clickable { onPromote(); showActions = false }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) { Text("Make Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = t.accent, fontFamily = OutfitFamily) }
                    } else {
                        Box(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                                .background(t.warn.copy(alpha = 0.08f))
                                .clickable { onDemote(); showActions = false }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) { Text("Remove Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = t.warn, fontFamily = OutfitFamily) }
                    }
                    Box(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                            .background(t.danger.copy(alpha = 0.08f))
                            .clickable { onKick(); showActions = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) { Text("Kick", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = t.danger, fontFamily = OutfitFamily) }
                }
            }
        }
    }
}
