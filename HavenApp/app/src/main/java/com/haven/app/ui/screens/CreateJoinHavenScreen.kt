package com.haven.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haven.app.ui.components.HavenCard
import com.haven.app.ui.theme.LocalHavenColors
import com.haven.app.ui.theme.OutfitFamily
import com.haven.app.ui.theme.SpaceMonoFamily

@Composable
fun CreateJoinHavenScreen(
    isLoading: Boolean,
    error: String?,
    onCreateHaven: (havenName: String, userName: String) -> Unit,
    onJoinHaven: (inviteCode: String, userName: String) -> Unit,
    onSignOut: () -> Unit,
    onClearError: () -> Unit
) {
    val t = LocalHavenColors.current
    val focusManager = LocalFocusManager.current
    var mode by remember { mutableStateOf<String?>(null) } // null, "create", "join"
    var userName by remember { mutableStateOf("") }
    var havenName by remember { mutableStateOf("") }
    var inviteCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(t.bg)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))

        // Logo
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(t.accent, t.accentMid))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "H", fontSize = 28.sp, fontWeight = FontWeight.Black,
                color = Color.White, fontFamily = OutfitFamily
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "Set Up Your Haven", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold,
            color = t.text, fontFamily = OutfitFamily
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Create a new family circle or join an existing one with an invite code.",
            fontSize = 13.sp, color = t.textMid, fontFamily = OutfitFamily,
            textAlign = TextAlign.Center, lineHeight = 20.sp
        )

        Spacer(Modifier.height(12.dp))

        // Your Name (always visible)
        OutlinedTextField(
            value = userName,
            onValueChange = { userName = it; onClearError() },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Your Name") },
            placeholder = { Text("How your family sees you") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = setupFieldColors(t)
        )

        Spacer(Modifier.height(20.dp))

        if (mode == null) {
            // Choice cards
            HavenCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { mode = "create" }
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Create a Haven", fontSize = 16.sp,
                        fontWeight = FontWeight.Bold, color = t.text,
                        fontFamily = OutfitFamily
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Start a new family circle. You'll get an invite code to share with your family.",
                        fontSize = 12.sp, color = t.textMid,
                        fontFamily = OutfitFamily, lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.linearGradient(listOf(t.accent, t.accentMid)))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Create New Circle", fontSize = 14.sp,
                            fontWeight = FontWeight.Bold, color = Color.White,
                            fontFamily = OutfitFamily
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            HavenCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { mode = "join" }
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Join a Haven", fontSize = 16.sp,
                        fontWeight = FontWeight.Bold, color = t.text,
                        fontFamily = OutfitFamily
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Enter the 6-character invite code shared by your family member.",
                        fontSize = 12.sp, color = t.textMid,
                        fontFamily = OutfitFamily, lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(t.card)
                            .border(1.5.dp, t.accent, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Join with Code", fontSize = 14.sp,
                            fontWeight = FontWeight.Bold, color = t.accent,
                            fontFamily = OutfitFamily
                        )
                    }
                }
            }
        }

        // Create flow
        AnimatedVisibility(visible = mode == "create") {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = havenName,
                    onValueChange = { havenName = it; onClearError() },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Family Name") },
                    placeholder = { Text("e.g. The Smiths") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = setupFieldColors(t)
                )

                errorSection(error, t)

                Spacer(Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(t.accent, t.accentMid)))
                        .clickable(enabled = !isLoading && userName.isNotBlank() && havenName.isNotBlank()) {
                            focusManager.clearFocus()
                            onCreateHaven(havenName.trim(), userName.trim())
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(Modifier.size(22.dp), Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(
                            "Create Haven", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                            color = Color.White, fontFamily = OutfitFamily
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    "Back", fontSize = 13.sp, color = t.textFade,
                    fontFamily = OutfitFamily,
                    modifier = Modifier
                        .clickable { mode = null; onClearError() }
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }

        // Join flow
        AnimatedVisibility(visible = mode == "join") {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = inviteCode,
                    onValueChange = { if (it.length <= 6) inviteCode = it.uppercase(); onClearError() },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Invite Code") },
                    placeholder = { Text("6-character code") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = SpaceMonoFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        if (inviteCode.length == 6 && userName.isNotBlank()) {
                            onJoinHaven(inviteCode, userName.trim())
                        }
                    }),
                    colors = setupFieldColors(t)
                )

                errorSection(error, t)

                Spacer(Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(t.accent, t.accentMid)))
                        .clickable(enabled = !isLoading && userName.isNotBlank() && inviteCode.length == 6) {
                            focusManager.clearFocus()
                            onJoinHaven(inviteCode, userName.trim())
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(Modifier.size(22.dp), Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(
                            "Join Haven", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                            color = Color.White, fontFamily = OutfitFamily
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    "Back", fontSize = 13.sp, color = t.textFade,
                    fontFamily = OutfitFamily,
                    modifier = Modifier
                        .clickable { mode = null; onClearError() }
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            "Sign Out", fontSize = 12.sp, color = t.textFade,
            fontFamily = OutfitFamily,
            modifier = Modifier
                .clickable { onSignOut() }
                .padding(8.dp)
        )

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun errorSection(error: String?, t: com.haven.app.ui.theme.HavenColors) {
    AnimatedVisibility(visible = error != null) {
        Text(
            error ?: "", fontSize = 12.sp, color = t.danger,
            fontFamily = OutfitFamily,
            modifier = Modifier.padding(top = 12.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun setupFieldColors(t: com.haven.app.ui.theme.HavenColors) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = t.accent,
    unfocusedBorderColor = t.border,
    focusedContainerColor = if (t.isDark) t.surfaceAlt else t.bgSub,
    unfocusedContainerColor = if (t.isDark) t.surfaceAlt else t.bgSub,
    cursorColor = t.accent,
    focusedTextColor = t.text,
    unfocusedTextColor = t.text,
    focusedLabelColor = t.accent,
    unfocusedLabelColor = t.textFade,
)
