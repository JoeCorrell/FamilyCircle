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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
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
import java.util.Calendar

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel()
) {
    val t = LocalHavenColors.current
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val memberColors by viewModel.memberColors.collectAsStateWithLifecycle()
    val errands by viewModel.errands.collectAsStateWithLifecycle()
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

    // Group messages for display
    val groupedMessages = remember(messages) { groupMessages(messages) }

    Column(modifier = Modifier.fillMaxSize().imePadding()) {
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
                    AcceptedErrandCard(errand, viewModel, t) { viewModel.dismissErrand(errand.id) }
                }
            }
        }

        // ── Messages ──
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            contentPadding = PaddingValues(bottom = 6.dp, top = 6.dp)
        ) {
            groupedMessages.forEach { item ->
                when (item) {
                    is ChatItem.DateHeader -> {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f).height(1.dp).background(t.border))
                                Text(
                                    item.label, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                    color = t.textFade, fontFamily = SpaceMonoFamily, letterSpacing = 1.sp
                                )
                                Box(modifier = Modifier.weight(1f).height(1.dp).background(t.border))
                            }
                        }
                    }
                    is ChatItem.MessageItem -> {
                        item(key = "${item.message.id}_${item.message.timestamp}") {
                            MessageBubble(
                                message = item.message,
                                memberColors = memberColors,
                                t = t,
                                showAvatar = item.showAvatar,
                                showName = item.showName
                            )
                        }
                    }
                }
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
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(18.dp)
                                .drawBehind { drawErrandIcon(t.warn) }
                        )
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
                    Box(
                        modifier = Modifier.size(18.dp)
                            .drawBehind { drawErrandIcon(t.warn) }
                    )
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

// ── Message Grouping Logic ──

private sealed class ChatItem {
    data class DateHeader(val label: String) : ChatItem()
    data class MessageItem(val message: Message, val showAvatar: Boolean, val showName: Boolean) : ChatItem()
}

private fun groupMessages(messages: List<Message>): List<ChatItem> {
    if (messages.isEmpty()) return emptyList()
    val items = mutableListOf<ChatItem>()
    var lastDateLabel = ""
    var lastSender = ""
    var lastTimestamp = 0L

    messages.forEach { message ->
        val dateLabel = getDateLabel(message.timestamp)
        if (dateLabel != lastDateLabel) {
            items.add(ChatItem.DateHeader(dateLabel))
            lastDateLabel = dateLabel
            lastSender = ""
            lastTimestamp = 0
        }

        val sameSender = message.senderName == lastSender && !message.isFromCurrentUser == !(messages.firstOrNull { it.senderName == lastSender }?.isFromCurrentUser ?: false)
        val withinWindow = (message.timestamp - lastTimestamp) < 120_000 // 2 minutes
        val showAvatar = !message.isFromCurrentUser && !(sameSender && withinWindow)
        val showName = !message.isFromCurrentUser && !(sameSender && withinWindow)

        items.add(ChatItem.MessageItem(message, showAvatar, showName))
        lastSender = message.senderName
        lastTimestamp = message.timestamp
    }
    return items
}

private fun getDateLabel(timestamp: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

    return when {
        cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "TODAY"
        cal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> "YESTERDAY"
        else -> {
            val month = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, java.util.Locale.getDefault())?.uppercase() ?: ""
            "$month ${cal.get(Calendar.DAY_OF_MONTH)}"
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
private fun MessageBubble(
    message: Message, memberColors: Map<String, Long>, t: HavenColors,
    showAvatar: Boolean = true, showName: Boolean = true
) {
    val isMe = message.isFromCurrentUser
    val senderColor = Color(memberColors[message.senderName] ?: 0xFF999999)

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = if (showAvatar) 2.dp else 0.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        // Avatar for others
        if (!isMe) {
            if (showAvatar) {
                Box(
                    modifier = Modifier.size(32.dp)
                        .background(senderColor.copy(alpha = 0.12f), CircleShape)
                        .border(2.dp, senderColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(message.senderName.take(1), fontSize = 13.sp, fontWeight = FontWeight.Black, color = senderColor, fontFamily = OutfitFamily)
                }
            } else {
                Spacer(Modifier.width(32.dp))
            }
            Spacer(Modifier.width(8.dp))
        }

        // Bubble
        Column(
            modifier = Modifier.widthIn(max = 280.dp)
                .clip(RoundedCornerShape(
                    topStart = 20.dp, topEnd = 20.dp,
                    bottomStart = if (isMe) 20.dp else if (showAvatar) 4.dp else 16.dp,
                    bottomEnd = if (isMe) if (showAvatar) 4.dp else 16.dp else 20.dp
                ))
                .then(
                    if (isMe) Modifier.background(Brush.linearGradient(listOf(t.accent, t.accentMid)))
                    else Modifier.background(t.card).border(1.dp, t.border, RoundedCornerShape(
                        topStart = 20.dp, topEnd = 20.dp,
                        bottomStart = if (showAvatar) 4.dp else 16.dp, bottomEnd = 20.dp
                    ))
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            if (!isMe && showName) {
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
private fun SwipeDismissible(onDismiss: () -> Unit, content: @Composable () -> Unit) {
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
    ) { content() }
}

@Composable
private fun ErrandCard(errand: com.haven.app.data.api.ErrandData, viewModel: ChatViewModel, t: HavenColors, onDismiss: () -> Unit) {
    SwipeDismissible(onDismiss) {
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
                ) {
                    Box(
                        modifier = Modifier.size(20.dp)
                            .drawBehind { drawErrandIcon(t.warn) }
                    )
                }
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
                                .border(1.dp, t.border, RoundedCornerShape(12.dp))
                                .clickable { onDismiss() },
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
private fun AcceptedErrandCard(errand: com.haven.app.data.api.ErrandData, viewModel: ChatViewModel, t: HavenColors, onDismiss: () -> Unit) {
    SwipeDismissible(onDismiss) {
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
                // Complete button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(t.ok.copy(alpha = 0.1f))
                        .border(1.dp, t.ok.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .clickable { viewModel.completeErrand(errand.id) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Done", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = t.ok, fontFamily = SpaceMonoFamily)
                }
            }
        }
    }
}

// ── Custom drawn icons ──

private fun DrawScope.drawErrandIcon(color: Color) {
    val w = size.width; val h = size.height
    val s = Stroke(width = w * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    // Clipboard body
    drawRoundRect(color, Offset(w * 0.14f, h * 0.22f),
        Size(w * 0.72f, h * 0.72f),
        cornerRadius = CornerRadius(w * 0.1f), style = s)
    // Clipboard clip top
    drawRoundRect(color, Offset(w * 0.3f, h * 0.06f),
        Size(w * 0.4f, h * 0.22f),
        cornerRadius = CornerRadius(w * 0.08f), style = s)
    // Checkmark inside
    val check = Path().apply {
        moveTo(w * 0.3f, h * 0.56f)
        lineTo(w * 0.44f, h * 0.7f)
        lineTo(w * 0.7f, h * 0.44f)
    }
    drawPath(check, color, style = Stroke(width = w * 0.1f, cap = StrokeCap.Round, join = StrokeJoin.Round))
}
