package com.haven.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.haven.app.ui.components.ProfileImage
import com.haven.app.ui.components.HavenCard
import com.haven.app.ui.theme.LocalHavenColors
import com.haven.app.ui.theme.OutfitFamily
import com.haven.app.ui.theme.SpaceMonoFamily
import com.haven.app.ui.viewmodel.ProfileViewModel
import androidx.compose.ui.graphics.vector.ImageVector

private val AVATAR_ICONS = listOf(
    "Person" to Icons.Outlined.Person,
    "Face" to Icons.Outlined.Face,
    "Pets" to Icons.Outlined.Pets,
    "Star" to Icons.Outlined.Star,
    "Favorite" to Icons.Outlined.Favorite,
    "School" to Icons.Outlined.School,
    "Work" to Icons.Outlined.WorkOutline,
    "Sports" to Icons.Outlined.SportsEsports,
    "Music" to Icons.Outlined.MusicNote,
    "Flower" to Icons.Outlined.LocalFlorist,
    "Bolt" to Icons.Outlined.Bolt,
    "Diamond" to Icons.Outlined.Diamond,
)

private val HUE_OPTIONS = listOf(
    0xFFE879A0, 0xFF60A5FA, 0xFFA78BFA, 0xFF34D399, 0xFFFBBF24,
    0xFFF87171, 0xFFFB923C, 0xFF38BDF8, 0xFFC084FC, 0xFF4ADE80,
    0xFFF472B6, 0xFF818CF8
)

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val t = LocalHavenColors.current
    val focusManager = LocalFocusManager.current
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val photoUrl by viewModel.photoUrl.collectAsStateWithLifecycle()
    val isUploadingPhoto by viewModel.isUploadingPhoto.collectAsStateWithLifecycle()
    val emergencyContacts by viewModel.emergencyContacts.collectAsStateWithLifecycle()
    val avatarIcon by viewModel.avatarIcon.collectAsStateWithLifecycle()
    val userColor by viewModel.userColor.collectAsStateWithLifecycle()
    val phoneNumber by viewModel.phoneNumber.collectAsStateWithLifecycle()

    var nameDraft by remember(userName) { mutableStateOf(userName) }
    var phoneDraft by remember(phoneNumber) { mutableStateOf(phoneNumber) }
    var selectedHue by remember(userColor) { mutableStateOf(userColor) }
    var selectedIcon by remember(avatarIcon) { mutableStateOf(avatarIcon) }
    var showIconPicker by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }

    var showAddContact by remember { mutableStateOf(false) }
    var newContactName by remember { mutableStateOf("") }
    var newContactPhone by remember { mutableStateOf("") }
    var newContactRelation by remember { mutableStateOf("") }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.uploadAvatar(it) }
    }

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
                "Edit Profile", fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold, color = t.text,
                fontFamily = OutfitFamily, letterSpacing = (-0.5).sp
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Avatar Section ──
            HavenCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clickable { imagePicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        val hueColor = Color(selectedHue)
                        val iconEntry = AVATAR_ICONS.firstOrNull { it.first == selectedIcon }
                        if (photoUrl.isNotEmpty() && selectedIcon.isEmpty()) {
                            ProfileImage(
                                photoUrl = photoUrl,
                                contentDescription = "Profile photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(RoundedCornerShape(26.dp))
                                    .border(3.5.dp, hueColor, RoundedCornerShape(26.dp))
                            )
                        } else if (iconEntry != null) {
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(RoundedCornerShape(26.dp))
                                    .background(hueColor.copy(alpha = 0.12f))
                                    .border(3.5.dp, hueColor, RoundedCornerShape(26.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(iconEntry.second, iconEntry.first, Modifier.size(40.dp), tint = hueColor)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(RoundedCornerShape(26.dp))
                                    .background(Brush.linearGradient(listOf(t.accent, t.accentMid)))
                                    .border(3.5.dp, hueColor, RoundedCornerShape(26.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isUploadingPhoto) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(28.dp),
                                        color = Color.White, strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        (userName.ifEmpty { "?" }).take(1).uppercase(),
                                        fontSize = 34.sp, fontWeight = FontWeight.Black,
                                        color = Color.White, fontFamily = OutfitFamily
                                    )
                                }
                            }
                        }
                        // Camera badge
                        if (!isUploadingPhoto) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 4.dp, y = 4.dp)
                                    .size(32.dp)
                                    .background(t.accent, RoundedCornerShape(10.dp))
                                    .border(3.dp, t.card, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.CameraAlt, "Change photo", Modifier.size(14.dp), tint = Color.White)
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "TAP PHOTO TO CHANGE", fontSize = 10.sp,
                        color = t.textFade, fontFamily = SpaceMonoFamily
                    )
                }
            }

            // ── Color Picker ──
            HavenCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "PROFILE COLOR", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        color = t.textFade, fontFamily = SpaceMonoFamily, letterSpacing = 1.5.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HUE_OPTIONS.take(6).forEach { c ->
                            val color = Color(c)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(color)
                                    .border(
                                        if (selectedHue == c) 3.dp else 2.dp,
                                        if (selectedHue == c) t.text else Color.Transparent,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedHue = c },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedHue == c) {
                                    Icon(Icons.Outlined.Check, "Selected", Modifier.size(14.dp), tint = Color.White)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HUE_OPTIONS.drop(6).forEach { c ->
                            val color = Color(c)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(color)
                                    .border(
                                        if (selectedHue == c) 3.dp else 2.dp,
                                        if (selectedHue == c) t.text else Color.Transparent,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedHue = c },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedHue == c) {
                                    Icon(Icons.Outlined.Check, "Selected", Modifier.size(14.dp), tint = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // ── Avatar Icon Picker ──
            HavenCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showIconPicker = !showIconPicker }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "AVATAR ICON", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            color = t.textFade, fontFamily = SpaceMonoFamily, letterSpacing = 1.5.sp
                        )
                        Icon(
                            if (showIconPicker) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            "Toggle", Modifier.size(18.dp), tint = t.textFade
                        )
                    }
                    if (showIconPicker) {
                        Spacer(Modifier.height(12.dp))
                        // Icon grid (4 columns)
                        val rows = AVATAR_ICONS.chunked(4)
                        rows.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { (name, icon) ->
                                    val isSelected = selectedIcon == name
                                    val hueColor = Color(selectedHue)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) hueColor.copy(alpha = 0.12f)
                                                else if (t.isDark) t.surfaceAlt else t.bgSub
                                            )
                                            .border(
                                                if (isSelected) 2.dp else 1.dp,
                                                if (isSelected) hueColor else t.border,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable { selectedIcon = name },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                icon, name,
                                                modifier = Modifier.size(24.dp),
                                                tint = if (isSelected) hueColor else t.textMid
                                            )
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                name, fontSize = 8.sp,
                                                color = if (isSelected) hueColor else t.textFade,
                                                fontFamily = SpaceMonoFamily
                                            )
                                        }
                                    }
                                }
                                // Fill remaining slots if row is incomplete
                                repeat(4 - row.size) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                        // Use photo / initial option
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (t.isDark) t.surfaceAlt else t.bgSub)
                                .border(1.dp, t.border, RoundedCornerShape(12.dp))
                                .clickable { selectedIcon = "" }
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (photoUrl.isNotEmpty()) "Use Photo Instead" else "Use Initial Instead",
                                fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                                color = t.textMid, fontFamily = OutfitFamily
                            )
                        }
                    }
                }
            }

            // ── Name Field ──
            Column {
                Text(
                    "DISPLAY NAME", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    color = t.textFade, fontFamily = SpaceMonoFamily, letterSpacing = 1.5.sp
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = nameDraft,
                    onValueChange = { nameDraft = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Your name") },
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
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                )
            }

            // ── Phone Number Field ──
            Column {
                Text(
                    "PHONE NUMBER", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    color = t.textFade, fontFamily = SpaceMonoFamily, letterSpacing = 1.5.sp
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = phoneDraft,
                    onValueChange = { phoneDraft = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Your phone number") },
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
                    leadingIcon = { Icon(Icons.Outlined.Phone, "Phone", Modifier.size(18.dp), tint = t.textFade) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Other family members can use this to call you",
                    fontSize = 10.sp, color = t.textFade, fontFamily = OutfitFamily
                )
            }

            // ── Save Button ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .then(
                        if (saved)
                            Modifier
                                .background(t.ok.copy(alpha = 0.07f))
                                .border(1.5.dp, t.ok.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        else
                            Modifier.background(Brush.linearGradient(listOf(t.accent, t.accentMid)))
                    )
                    .clickable {
                        if (nameDraft.isNotBlank()) {
                            viewModel.updateUserName(nameDraft)
                        }
                        if (phoneDraft.isNotBlank()) {
                            viewModel.updatePhoneNumber(phoneDraft)
                        }
                        viewModel.updateMemberColor(selectedHue)
                        viewModel.updateAvatarIcon(selectedIcon)
                        saved = true
                    }
                    .padding(15.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (saved) "Saved" else "Save Changes",
                    fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    color = if (saved) t.ok else Color.White, fontFamily = OutfitFamily
                )
            }

            // ── Emergency Contacts ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "EMERGENCY CONTACTS", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    color = t.textFade, fontFamily = SpaceMonoFamily, letterSpacing = 1.5.sp
                )
                Text(
                    "+ ADD", fontSize = 9.sp, fontWeight = FontWeight.Bold,
                    color = t.accent, fontFamily = SpaceMonoFamily,
                    modifier = Modifier.clickable { showAddContact = true }
                )
            }

            Text(
                "These contacts receive SMS alerts when you activate SOS.",
                fontSize = 11.sp, color = t.textMid, fontFamily = OutfitFamily
            )

            if (emergencyContacts.isEmpty()) {
                HavenCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "No emergency contacts.\nAdd contacts who should be notified during SOS.",
                        modifier = Modifier.padding(20.dp),
                        fontSize = 13.sp, color = t.textMid, fontFamily = OutfitFamily, lineHeight = 20.sp
                    )
                }
            } else {
                emergencyContacts.forEach { contact ->
                    HavenCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(t.danger.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.LocalPhone, "Phone", Modifier.size(18.dp), tint = t.danger)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    contact.name, fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold, color = t.text, fontFamily = OutfitFamily
                                )
                                Text(
                                    "${contact.phoneNumber}${if (contact.relationship.isNotEmpty()) " - ${contact.relationship}" else ""}",
                                    fontSize = 10.sp, color = t.textFade, fontFamily = SpaceMonoFamily
                                )
                            }
                            Icon(
                                Icons.Outlined.Delete, "Remove",
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { viewModel.removeEmergencyContact(contact) },
                                tint = t.danger
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }

    // Add contact dialog
    if (showAddContact) {
        AlertDialog(
            onDismissRequest = { showAddContact = false },
            title = { Text("Add Emergency Contact", fontFamily = OutfitFamily, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newContactName, onValueChange = { newContactName = it },
                        label = { Text("Name") }, singleLine = true,
                        shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newContactPhone, onValueChange = { newContactPhone = it },
                        label = { Text("Phone Number") }, singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newContactRelation, onValueChange = { newContactRelation = it },
                        label = { Text("Relationship (optional)") }, singleLine = true,
                        shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newContactName.isNotBlank() && newContactPhone.isNotBlank()) {
                        viewModel.addEmergencyContact(newContactName.trim(), newContactPhone.trim(), newContactRelation.trim())
                        newContactName = ""; newContactPhone = ""; newContactRelation = ""
                        showAddContact = false
                    }
                }) { Text("Add", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showAddContact = false }) { Text("Cancel") }
            }
        )
    }
}
