package com.haven.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.haven.app.data.model.Message
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
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Chat",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = t.text,
                fontFamily = OutfitFamily,
                letterSpacing = (-0.5).sp
            )
            Box(
                modifier = Modifier
                    .background(t.accentBg, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Home, "Circle", Modifier.size(11.dp), tint = t.accent)
                    Text(
                        viewModel.familyName.collectAsStateWithLifecycle().value,
                        fontSize = 9.sp, fontWeight = FontWeight.Bold,
                        color = t.accent, fontFamily = SpaceMonoFamily
                    )
                }
            }
        }

        // Messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 6.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                androidx.compose.animation.AnimatedVisibility(
                    visible = true,
                    enter = androidx.compose.animation.fadeIn(
                        animationSpec = androidx.compose.animation.core.tween(300)
                    ) + androidx.compose.animation.slideInVertically(
                        animationSpec = androidx.compose.animation.core.tween(300)
                    ) { it / 2 }
                ) {
                    MessageBubble(message, memberColors, t)
                }
            }
        }

        // Errand Composer
        if (errandMode) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(t.warn.copy(alpha = 0.04f))
                    .border(
                        width = 1.dp,
                        color = t.warn.copy(alpha = 0.13f),
                        shape = RoundedCornerShape(0.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Assignment, "Errand", Modifier.size(14.dp), tint = t.warn)
                        Text("NEW ERRAND", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = t.warn, fontFamily = SpaceMonoFamily)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (t.isDark) t.surfaceAlt else t.bgSub)
                            .clickable { errandMode = false }
                            .padding(4.dp)
                    ) {
                        Icon(Icons.Outlined.Close, "Close", Modifier.size(12.dp), tint = t.textFade)
                    }
                }
                OutlinedTextField(
                    value = errandItem,
                    onValueChange = { errandItem = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("What do you need? (e.g. Milk, eggs)", fontSize = 14.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = t.warn.copy(alpha = 0.2f),
                        unfocusedBorderColor = t.warn.copy(alpha = 0.2f),
                        focusedContainerColor = t.card,
                        unfocusedContainerColor = t.card,
                        cursorColor = t.warn,
                        focusedTextColor = t.text,
                        unfocusedTextColor = t.text,
                    ),
                    textStyle = LocalTextStyle.current.copy(fontFamily = OutfitFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = errandAddr,
                        onValueChange = { errandAddr = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Store / address", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = t.border,
                            unfocusedBorderColor = t.border,
                            focusedContainerColor = if (t.isDark) t.surfaceAlt else t.bgSub,
                            unfocusedContainerColor = if (t.isDark) t.surfaceAlt else t.bgSub,
                            cursorColor = t.accent,
                            focusedTextColor = t.text,
                            unfocusedTextColor = t.text,
                        ),
                        textStyle = LocalTextStyle.current.copy(fontFamily = OutfitFamily, fontSize = 12.sp)
                    )
                    OutlinedTextField(
                        value = errandNote,
                        onValueChange = { errandNote = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Note (optional)", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = t.border,
                            unfocusedBorderColor = t.border,
                            focusedContainerColor = if (t.isDark) t.surfaceAlt else t.bgSub,
                            unfocusedContainerColor = if (t.isDark) t.surfaceAlt else t.bgSub,
                            cursorColor = t.accent,
                            focusedTextColor = t.text,
                            unfocusedTextColor = t.text,
                        ),
                        textStyle = LocalTextStyle.current.copy(fontFamily = OutfitFamily, fontSize = 12.sp)
                    )
                }
                val hasItem = errandItem.trim().isNotEmpty()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (hasItem) Brush.linearGradient(listOf(t.warn, Color(0xFFF59E0B)))
                            else Brush.linearGradient(
                                listOf(
                                    if (t.isDark) t.surfaceAlt else t.bgSub,
                                    if (t.isDark) t.surfaceAlt else t.bgSub
                                )
                            )
                        )
                        .clickable(enabled = hasItem) {
                            val text = buildString {
                                append("[Errand] ${errandItem.trim()}")
                                if (errandAddr.isNotBlank()) append("\nLocation: ${errandAddr.trim()}")
                                if (errandNote.isNotBlank()) append("\nNote: ${errandNote.trim()}")
                            }
                            viewModel.updateInput(text)
                            viewModel.sendMessage()
                            errandItem = ""; errandAddr = ""; errandNote = ""
                            errandMode = false
                        }
                        .padding(11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Send Errand to Circle",
                        fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        color = if (hasItem) Color.White else t.textFade,
                        fontFamily = OutfitFamily
                    )
                }
            }
        } else {
            // Regular Composer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Errand button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(t.warn.copy(alpha = 0.08f))
                        .border(1.5.dp, t.warn.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                        .clickable { errandMode = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Assignment, "Errand", Modifier.size(17.dp), tint = t.warn)
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { viewModel.updateInput(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text("Message...", color = t.textFade, fontFamily = OutfitFamily)
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = t.border,
                        unfocusedBorderColor = t.border,
                        focusedContainerColor = t.card,
                        unfocusedContainerColor = t.card,
                        cursorColor = t.accent,
                        focusedTextColor = t.text,
                        unfocusedTextColor = t.text,
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { viewModel.sendMessage() }),
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = OutfitFamily, fontSize = 14.sp
                    )
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (inputText.isNotBlank()) Brush.linearGradient(listOf(t.accent, t.accentMid))
                            else Brush.linearGradient(
                                listOf(
                                    if (t.isDark) t.surfaceAlt else t.bgSub,
                                    if (t.isDark) t.surfaceAlt else t.bgSub
                                )
                            )
                        )
                        .then(
                            if (inputText.isBlank()) Modifier.border(1.dp, t.border, RoundedCornerShape(14.dp))
                            else Modifier
                        )
                        .clickable { viewModel.sendMessage() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send, "Send",
                        modifier = Modifier.size(17.dp),
                        tint = if (inputText.isNotBlank()) Color.White else t.textFade
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: Message,
    memberColors: Map<String, Long>,
    t: com.haven.app.ui.theme.HavenColors
) {
    val isMe = message.isFromCurrentUser
    val senderColor = Color(memberColors[message.senderName] ?: 0xFF999999)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isMe) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(senderColor.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                    .border(2.dp, senderColor, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    message.senderName.take(1),
                    fontSize = 11.sp, fontWeight = FontWeight.Black,
                    color = senderColor, fontFamily = OutfitFamily
                )
            }
            Spacer(Modifier.width(6.dp))
        }

        Column(
            modifier = Modifier
                .widthIn(max = 260.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp, topEnd = 18.dp,
                        bottomStart = if (isMe) 18.dp else 6.dp,
                        bottomEnd = if (isMe) 6.dp else 18.dp
                    )
                )
                .then(
                    if (isMe) Modifier.background(Brush.linearGradient(listOf(t.accent, t.accentMid)))
                    else Modifier
                        .background(t.card)
                        .border(1.dp, t.border, RoundedCornerShape(
                            topStart = 18.dp, topEnd = 18.dp,
                            bottomStart = 6.dp, bottomEnd = 18.dp
                        ))
                )
                .padding(horizontal = 15.dp, vertical = 11.dp)
        ) {
            if (!isMe) {
                Text(
                    message.senderName.uppercase(),
                    fontSize = 9.5.sp, color = senderColor,
                    fontWeight = FontWeight.ExtraBold, fontFamily = SpaceMonoFamily
                )
                Spacer(Modifier.height(3.dp))
            }
            Text(
                message.text,
                fontSize = 13.5.sp,
                color = if (isMe) Color.White else t.text,
                lineHeight = 20.sp, fontFamily = OutfitFamily
            )
            Spacer(Modifier.height(3.dp))
            Text(
                message.formattedTime(),
                fontSize = 8.5.sp,
                color = if (isMe) Color.White.copy(alpha = 0.5f) else t.textFade,
                fontFamily = SpaceMonoFamily,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
