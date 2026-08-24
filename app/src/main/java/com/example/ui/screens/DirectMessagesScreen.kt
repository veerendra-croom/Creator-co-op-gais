package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Message
import com.example.data.model.UserProfile
import com.example.ui.theme.*
import com.example.ui.viewmodels.GlobalViewModel
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun DirectMessagesScreen(
    onBack: () -> Unit,
    globalViewModel: GlobalViewModel,
    chatViewModel: com.example.ui.viewmodels.ChatViewModel,
    userProfile: UserProfile?
) {
    val currentUserId = userProfile?.id ?: "DemoUser"

    val creators by chatViewModel.creatorsFlow.collectAsState(initial = emptyList())
    val allDMs by chatViewModel.getDMsForUser(currentUserId).collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var selectedUser by remember { mutableStateOf<UserProfile?>(null) }

    val filteredCreators = remember(creators, searchQuery, currentUserId) {
        creators.filter { creator ->
            creator.id != currentUserId && 
            (creator.displayName.contains(searchQuery, ignoreCase = true) ||
             creator.username.contains(searchQuery, ignoreCase = true))
        }
    }

    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
    ) {
        AnimatedContent(
            targetState = selectedUser,
            transitionSpec = {
                if (targetState != null) {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                } else {
                    slideInHorizontally { width -> -width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> width } + fadeOut()
                }
            },
            label = "dm_screen_navigation"
        ) { activeTargetUser ->
            if (activeTargetUser != null) {
                // ACTIVE CHAT PANEL
                val activeMessages = remember(allDMs, activeTargetUser) {
                    allDMs.filter { msg ->
                        (msg.senderId == currentUserId && msg.recipientId == activeTargetUser.id) ||
                        (msg.senderId == activeTargetUser.id && msg.recipientId == currentUserId)
                    }.sortedBy { it.timestamp }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Chat header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { selectedUser = null },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("close_chat_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close chat", tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AccentBlue.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, null, tint = AccentBlue, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = activeTargetUser.displayName,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "@${activeTargetUser.username} • Online",
                                    color = NeonEmerald,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Quick Action Buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = {
                                    chatViewModel.sendDirectMessage(
                                        workspaceId = "dm_general",
                                        recipientId = activeTargetUser.id,
                                        text = "📌 WORKSPACE INVITE: Hey @${activeTargetUser.username}, I'd like to invite you to collaborate on our active workspace! Tap to open Workspace Invitation.",
                                        senderId = currentUserId,
                                        user = null
                                    )
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(AccentBlue.copy(alpha = 0.15f), CircleShape)
                            ) {
                                Icon(Icons.Default.GroupAdd, contentDescription = "Invite to Workspace", tint = AccentBlue, modifier = Modifier.size(18.dp))
                            }

                            IconButton(
                                onClick = {
                                    chatViewModel.sendDirectMessage(
                                        workspaceId = "dm_general",
                                        recipientId = activeTargetUser.id,
                                        text = "🤝 SYNDICATE PROPOSAL: Hey @${activeTargetUser.username}, I submitted a project pitch for us to co-create! Let's discuss equity and terms.",
                                        senderId = currentUserId,
                                        user = null
                                    )
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(NeonEmerald.copy(alpha = 0.15f), CircleShape)
                            ) {
                                Icon(Icons.Default.Handshake, contentDescription = "Pitch Project", tint = NeonEmerald, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    // Chat messages body
                    val listState = rememberLazyListState()
                    LaunchedEffect(activeMessages.size) {
                        if (activeMessages.isNotEmpty()) {
                            listState.animateScrollToItem(activeMessages.size - 1)
                        }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        if (activeMessages.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(CircleShape)
                                                .background(AccentBlue.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Forum, null, tint = AccentBlue, modifier = Modifier.size(28.dp))
                                        }
                                        Text(
                                            text = "Encrypted Peer Handshake Ready",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "Start a direct conversation with @${activeTargetUser.username}. Messages are securely preserved on-device.",
                                            color = TextSecondary,
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Center
                                        )
                                        
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("QUICK PROMPTS", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                        
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            SuggestionChip(
                                                onClick = {
                                                    chatViewModel.sendDirectMessage(
                                                        workspaceId = "dm_general",
                                                        recipientId = activeTargetUser.id,
                                                        text = "👋 Hey @${activeTargetUser.username}! Let's connect on Creator Co-Op.",
                                                        senderId = currentUserId,
                                                        user = userProfile
                                                    )
                                                },
                                                label = { Text("👋 Say Hello", fontSize = 11.sp, color = Color.White) },
                                                colors = SuggestionChipDefaults.suggestionChipColors(containerColor = SurfaceColor),
                                                border = BorderStroke(1.dp, ColorDivider)
                                            )
                                            SuggestionChip(
                                                onClick = {
                                                    chatViewModel.sendDirectMessage(
                                                        workspaceId = "dm_general",
                                                        recipientId = activeTargetUser.id,
                                                        text = "⚡ Are you currently open for new project collaborations?",
                                                        senderId = currentUserId,
                                                        user = userProfile
                                                    )
                                                },
                                                label = { Text("⚡ Check Availability", fontSize = 11.sp, color = AccentBlue) },
                                                colors = SuggestionChipDefaults.suggestionChipColors(containerColor = SurfaceColor),
                                                border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.4f))
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            items(activeMessages, key = { it.id }) { msg ->
                                val isMe = msg.senderId == currentUserId
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                                ) {
                                    Card(
                                        shape = RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = if (isMe) 16.dp else 4.dp,
                                            bottomEnd = if (isMe) 4.dp else 16.dp
                                        ),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isMe) AccentBlue.copy(alpha = 0.25f) else SurfaceColor
                                        ),
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = if (isMe) AccentBlue.copy(alpha = 0.4f) else ColorDivider
                                        ),
                                        modifier = Modifier.widthIn(max = 280.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = msg.messageBody,
                                                    color = Color.White,
                                                    fontSize = 14.sp,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                if (isMe) {
                                                    IconButton(
                                                        onClick = {
                                                            chatViewModel.deleteMessage(msg.id)
                                                        },
                                                        modifier = Modifier.size(20.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Delete,
                                                            contentDescription = "Delete Message",
                                                            tint = TextMuted,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            val timeFormatted = remember(msg.timestamp) {
                                                java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(msg.timestamp))
                                            }
                                            Text(
                                                text = timeFormatted,
                                                color = TextSecondary,
                                                fontSize = 9.sp,
                                                modifier = Modifier.align(Alignment.End)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Input Box
                    var typedText by remember { mutableStateOf("") }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                typedText = if (typedText.isBlank()) "[Attachment: Asset_Draft.png] " else "$typedText [Attachment: Asset_Draft.png]"
                            },
                            modifier = Modifier.size(40.dp),
                            colors = IconButtonDefaults.iconButtonColors(containerColor = SurfaceLightColor)
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = "Attach file", tint = AccentBlue, modifier = Modifier.size(18.dp))
                        }

                        OutlinedTextField(
                            value = typedText,
                            onValueChange = { typedText = it },
                            placeholder = { Text("Write a message...", color = TextMuted) },
                            trailingIcon = if (typedText.isNotEmpty()) {
                                {
                                    IconButton(onClick = { typedText = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear text", tint = TextMuted, modifier = Modifier.size(16.dp))
                                    }
                                }
                            } else null,
                            modifier = Modifier.weight(1f).testTag("dm_text_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentBlue,
                                unfocusedBorderColor = ColorDivider,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        IconButton(
                            onClick = {
                                if (typedText.isNotBlank()) {
                                    chatViewModel.sendDirectMessage(
                                        workspaceId = "dm_${listOf(currentUserId, activeTargetUser.id).sorted().joinToString("_")}",
                                        recipientId = activeTargetUser.id,
                                        text = typedText.trim(),
                                        senderId = currentUserId,
                                        user = userProfile
                                    )
                                    typedText = ""
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = AccentBlue),
                            modifier = Modifier.testTag("dm_send_btn")
                        ) {
                            Icon(Icons.Default.Send, null, tint = Color.Black)
                        }
                    }
                }
            } else {
                // DIRECT MESSAGES OVERVIEW & CREATOR DIRECTORY
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("dm_directory_back_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                            Text("Direct Messages", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    // Search and filter directory
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search creators to chat...", color = TextSecondary) },
                        leadingIcon = { Icon(Icons.Default.Search, tint = TextSecondary, contentDescription = null) },
                        trailingIcon = if (searchQuery.isNotEmpty()) {
                            {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear search", tint = TextMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = ColorDivider,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Display active chats or directory list
                    Text(
                        text = "CREATORS ONLINE DIRECTORY",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (filteredCreators.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(CircleShape)
                                                .background(SurfaceColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                if (searchQuery.isNotEmpty()) Icons.Default.SearchOff else Icons.Default.Group,
                                                null,
                                                tint = TextSecondary,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                        Text(
                                            text = if (searchQuery.isNotEmpty()) "No creators found matching '$searchQuery'" else "No other creators in directory",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = if (searchQuery.isNotEmpty()) "Try adjusting your search terms or clearing the filter." else "Connect with other creators across workspaces or invite teammates.",
                                            color = TextSecondary,
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Center
                                        )
                                        if (searchQuery.isNotEmpty()) {
                                            Button(
                                                onClick = { searchQuery = "" },
                                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                                                border = BorderStroke(1.dp, ColorDivider),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("Clear Filter", fontSize = 12.sp, color = AccentBlue)
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            items(filteredCreators, key = { it.id }) { creator ->
                                // Look up last message if any to show context
                                val lastMsg = allDMs.filter { msg ->
                                    (msg.senderId == currentUserId && msg.recipientId == creator.id) ||
                                    (msg.senderId == creator.id && msg.recipientId == currentUserId)
                                }.maxByOrNull { it.timestamp }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedUser = creator },
                                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, ColorDivider)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(AccentBlue.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Person, null, tint = AccentBlue)
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(creator.displayName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                                Text(if (lastMsg != null) "Active" else "Online", color = NeonEmerald, fontSize = 11.sp)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = lastMsg?.messageBody ?: "Tap to establish private secure handshake",
                                                color = if (lastMsg != null) Color.White else TextSecondary,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
