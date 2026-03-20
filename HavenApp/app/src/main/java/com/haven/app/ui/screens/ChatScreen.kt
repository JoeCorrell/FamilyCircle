package com.haven.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.haven.app.data.model.Message
import com.haven.app.ui.components.HavenCard
import com.haven.app.ui.theme.HavenColors
import com.haven.app.ui.theme.LocalHavenColors
import com.haven.app.ui.theme.OutfitFamily
import com.haven.app.ui.theme.SpaceMonoFamily
import com.haven.app.ui.viewmodel.ChatViewModel

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel()
) {
    val t = LocalHavenColors.current
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val memberColors by viewModel.memberColors.collectAsStateWithLifecycle()
    val errands by viewModel.errands.collectAsStateWithLifecycle()
    val familyName by viewModel.familyName.collectAsStateWithLifecycle()
    val dismissedErrandIds by viewModel.dismissedErrandIds.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    var errandMode by remember { mutableStateOf(false) }
    var errandItem by remember { mutableStateOf("") }
    var errandAddr by remember { mutableStateOf("") }
    var errandNote by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().imePadding()) {
        // ── Header ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(t.card)
                .border(width = 1.dp, color = t.border)
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Chat", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold,
                        color = t.text, fontFamily = OutfitFamily, letterSpacing = (-0.5).sp
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(t.ok, CircleShape))
                        Text(
                            familyName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                            color = t.textMid, fontFamily = OutfitFamily
                        )
                    }
                }
                // Member count badge
                val onlineCount = memberColors.size
                Box(
                    modifier = Modifier
                        .background(t.accentBg, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        "$onlineCount members", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        color = t.accent, fontFamily = SpaceMonoFamily
                    )
                }
            }
        }

        // ── Errands Banner ──
        val pendingErrands = errands.filter { it.status == "PENDING" && it.id !in dismissedErrandIds }
        val acceptedErrands = errands.filter { it.status == "ACCEPTED" && it.id !in dismissedErrandIds }

        if (pendingErrands.isNotEmpty() || acceptedErrands.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .background(t.warn.copy(alpha = 0.03f))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(pendingErrands, key = { "e_${it.id}" }) { errand ->
                    ErrandCard(errand, viewModel, t) { viewModel.dismissErrand(errand.id) }
                }
                items(acceptedErrands, key = { "ea_${it.id}" }) { errand ->
                    AcceptedErrandCard(errand, t) { viewModel.dismissErrand(errand.id) }
                }
            }
        }

        // ── Date Divider ──
        if (messages.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.weight(1f).height(1.dp).background(t.border))
                Text("TODAY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = t.textFade, fontFamily = SpaceMonoFamily, letterSpacing = 1.sp)
                Box(modifier = Modifier.weight(1f).height(1.dp).background(t.border))
            }
        }

        // ── Messages ──
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(bottom = 6.dp)
        ) {
            items(messages, key = { "${it.timestamp}_${it.senderName}" }) { message ->
                MessageBubble(message, memberColors, t)
            }
        }

        // ── Errand Composer ──
        AnimatedVisibility(
            visible = errandMode,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(t.card)
                    .border(1.dp, t.border)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Assignment, "Errand", Modifier.size(18.dp), tint = t.warn)
                        Text("New Errand", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = t.text, fontFamily = OutfitFamily)
                    }
                    IconButton(onClick = { errandMode = false }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.Close, "Close", Modifier.size(18.dp), tint = t.textFade)
                    }
                }
                OutlinedTextField(
                    value = errandItem, onValueChange = { errandItem = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("What do you need?", fontFamily = OutfitFamily) },
                    singleLine = true, shape = RoundedCornerShape(14.dp),
                    colors = errandFieldColors(t),
                    textStyle = LocalTextStyle.current.copy(fontFamily = OutfitFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = errandAddr, onValueChange = { errandAddr = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Where?", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Outlined.LocationOn, null, Modifier.size(16.dp), tint = t.textFade) },
                        singleLine = true, shape = RoundedCornerShape(12.dp),
                        colors = errandFieldColors(t),
                        textStyle = LocalTextStyle.current.copy(fontFamily = OutfitFamily, fontSize = 13.sp)
                    )
                    OutlinedTextField(
                        value = errandNote, onValueChange = { errandNote = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Note", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Outlined.Notes, null, Modifier.size(16.dp), tint = t.textFade) },
                        singleLine = true, shape = RoundedCornerShape(12.dp),
                        colors = errandFieldColors(t),
                        textStyle = LocalTextStyle.current.copy(fontFamily = OutfitFamily, fontSize = 13.sp)
                    )
                }
                val canSend = errandItem.trim().isNotEmpty()
                Box(
                    modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(14.dp))
                        .background(if (canSend) Brush.linearGradient(listOf(t.warn, Color(0xFFF59E0B))) else Brush.linearGradient(listOf(t.bgSub, t.bgSub)))
                        .clickable(enabled = canSend) {
                            viewModel.sendErrand(errandItem.trim(), errandAddr.trim(), errandNote.trim())
                            errandItem = ""; errandAddr = ""; errandNote = ""; errandMode = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Send Errand", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (canSend) Color.White else t.textFade, fontFamily = OutfitFamily)
                }
            }
        }

        // ── Composer ──
        if (!errandMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(t.card)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Errand button
                Box(
                    modifier = Modifier.size(42.dp).clip(CircleShape)
                        .background(t.warn.copy(alpha = 0.08f))
                        .border(1.dp, t.warn.copy(alpha = 0.15f), CircleShape)
                        .clickable { errandMode = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Assignment, "Errand", Modifier.size(18.dp), tint = t.warn)
                }

                // Text input
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { viewModel.updateInput(it) },
                    modifier = Modifier.weight(1f).heightIn(min = 42.dp),
                    placeholder = { Text("Message...", color = t.textFade, fontFamily = OutfitFamily, fontSize = 14.sp) },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = t.accent.copy(alpha = 0.3f),
                        unfocusedBorderColor = t.border,
                        focusedContainerColor = if (t.isDark) t.surfaceAlt else t.bgSub,
                        unfocusedContainerColor = if (t.isDark) t.surfaceAlt else t.bgSub,
                        cursorColor = t.accent, focusedTextColor = t.text, unfocusedTextColor = t.text,
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { viewModel.sendMessage() }),
                    textStyle = LocalTextStyle.current.copy(fontFamily = OutfitFamily, fontSize = 14.sp)
                )

                // Send button
                Box(
                    modifier = Modifier.size(42.dp).clip(CircleShape)
                        .background(
                            if (inputText.isNotBlank()) Brush.linearGradient(listOf(t.accent, t.accentMid))
                            else Brush.linearGradient(listOf(if (t.isDark) t.surfaceAlt else t.bgSub, if (t.isDark) t.surfaceAlt else t.bgSub))
                        )
                        .then(if (inputText.isBlank()) Modifier.border(1.dp, t.border, CircleShape) else Modifier)
                        .clickable { viewModel.sendMessage() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send, "Send", Modifier.size(18.dp),
                        tint = if (inputText.isNotBlank()) Color.White else t.textFade
                    )
                }
            }
        }
    }
}

@Composable
private fun errandFieldColors(t: HavenColors) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = t.warn.copy(alpha = 0.3f),
    unfocusedBorderColor = t.border,
    focusedContainerColor = if (t.isDark) t.surfaceAlt else t.bgSub,
    unfocusedContainerColor = if (t.isDark) t.surfaceAlt else t.bgSub,
    cursorColor = t.warn, focusedTextColor = t.text, unfocusedTextColor = t.text,
)

@Composable
private fun MessageBubble(message: Message, memberColors: Map<String, Long>, t: HavenColors) {
    val isMe = message.isFromCurrentUser
    val senderColor = Color(memberColors[message.senderName] ?: 0xFF999999)

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        // Avatar for others
        if (!isMe) {
            Box(
                modifier = Modifier.size(32.dp)
                    .background(senderColor.copy(alpha = 0.12f), CircleShape)
                    .border(2.dp, senderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(message.senderName.take(1), fontSize = 13.sp, fontWeight = FontWeight.Black, color = senderColor, fontFamily = OutfitFamily)
            }
            Spacer(Modifier.width(8.dp))
        }

        // Bubble
        Column(
            modifier = Modifier.widthIn(max = 280.dp)
                .clip(RoundedCornerShape(
                    topStart = 20.dp, topEnd = 20.dp,
                    bottomStart = if (isMe) 20.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 20.dp
                ))
                .then(
                    if (isMe) Modifier.background(Brush.linearGradient(listOf(t.accent, t.accentMid)))
                    else Modifier.background(t.card).border(1.dp, t.border, RoundedCornerShape(
                        topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 20.dp
                    ))
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            if (!isMe) {
                Text(
                    message.senderName, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    color = senderColor, fontFamily = OutfitFamily
                )
                Spacer(Modifier.height(2.dp))
            }
            Text(
                message.text, fontSize = 14.sp,
                color = if (isMe) Color.White else t.text,
                lineHeight = 20.sp, fontFamily = OutfitFamily
            )
            Spacer(Modifier.height(2.dp))
            Text(
                message.formattedTime(), fontSize = 9.sp,
                color = if (isMe) Color.White.copy(alpha = 0.5f) else t.textFade,
                fontFamily = SpaceMonoFamily,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
private fun ErrandCard(errand: com.haven.app.data.api.ErrandData, viewModel: ChatViewModel, t: HavenColors, onDismiss: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { it != SwipeToDismissBoxValue.Settled }
    )
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) onDismiss()
    }
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val alignment = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) Alignment.CenterEnd else Alignment.CenterStart
            Box(
                Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(Color(0xFFEF4444).copy(alpha = 0.1f)),
                contentAlignment = alignment
            ) {
                Icon(Icons.Outlined.Close, "Dismiss", Modifier.padding(horizontal = 20.dp).size(20.dp), tint = Color(0xFFEF4444))
            }
        }
    ) {
    val isMe = errand.senderUid == viewModel.myUserId
    HavenCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.linearGradient(listOf(t.warn.copy(alpha = 0.08f), t.warn.copy(alpha = 0.02f))))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier.size(40.dp).background(t.warn.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Outlined.Assignment, "Errand", Modifier.size(20.dp), tint = t.warn) }
                Column(modifier = Modifier.weight(1f)) {
                    Text("ERRAND", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = t.warn, fontFamily = SpaceMonoFamily, letterSpacing = 1.5.sp)
                    Text(errand.item, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = t.text, fontFamily = OutfitFamily, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Person, null, Modifier.size(14.dp), tint = t.textFade)
                    Text("${errand.senderName} needs this", fontSize = 12.sp, color = t.textMid, fontFamily = OutfitFamily)
                }
                if (errand.address.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.LocationOn, null, Modifier.size(14.dp), tint = t.textFade)
                        Text(errand.address, fontSize = 12.sp, color = t.textMid, fontFamily = OutfitFamily)
                    }
                }
                if (errand.note.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Notes, null, Modifier.size(14.dp), tint = t.textFade)
                        Text(errand.note, fontSize = 12.sp, color = t.textFade, fontFamily = OutfitFamily)
                    }
                }
                if (!isMe) {
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(12.dp))
                                .background(t.ok.copy(alpha = 0.1f))
                                .border(1.5.dp, t.ok.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                .clickable { viewModel.acceptErrand(errand.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Check, null, Modifier.size(16.dp), tint = t.ok)
                                Text("I'll Do It", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = t.ok, fontFamily = OutfitFamily)
                            }
                        }
                        Box(
                            modifier = Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(12.dp))
                                .background(if (t.isDark) t.surfaceAlt else t.bgSub)
                                .border(1.dp, t.border, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Can't", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = t.textMid, fontFamily = OutfitFamily)
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun AcceptedErrandCard(errand: com.haven.app.data.api.ErrandData, t: HavenColors, onDismiss: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { it != SwipeToDismissBoxValue.Settled }
    )
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) onDismiss()
    }
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val alignment = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) Alignment.CenterEnd else Alignment.CenterStart
            Box(
                Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(Color(0xFFEF4444).copy(alpha = 0.1f)),
                contentAlignment = alignment
            ) {
                Icon(Icons.Outlined.Close, "Dismiss", Modifier.padding(horizontal = 20.dp).size(20.dp), tint = Color(0xFFEF4444))
            }
        }
    ) {
        HavenCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(t.ok.copy(alpha = 0.05f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier.size(34.dp).background(t.ok.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Outlined.CheckCircle, "Done", Modifier.size(18.dp), tint = t.ok) }
                Column(modifier = Modifier.weight(1f)) {
                    Text(errand.item, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = t.text, fontFamily = OutfitFamily)
                    Text("${errand.acceptedName ?: "Someone"} is on it", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = t.ok, fontFamily = SpaceMonoFamily)
                }
            }
        }
    }
}
