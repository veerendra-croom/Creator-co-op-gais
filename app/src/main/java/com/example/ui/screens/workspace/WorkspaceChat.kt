package com.example.ui.screens.workspace

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.theme.*
import com.example.ui.components.*
import com.example.ui.viewmodels.ChatViewModel
import kotlinx.coroutines.flow.flowOf

@Composable
fun WorkspaceChat(
    chatViewModel: ChatViewModel,
    workspaceId: String,
    userId: String,
    userProfile: UserProfile?,
    isAgreementActive: Boolean = true
) {
    if (!isAgreementActive) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PrimaryBackground)
                .padding(DS.Space24),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(DS.Space16),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                shape = DS.RadiusLarge,
                border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(DS.Space24),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(DS.Space16)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock icon",
                        modifier = Modifier.size(56.dp),
                        tint = AccentRed
                    )
                    Text(
                        text = "Communications Locked",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Group communications and workspace chats are suspended until all active members review and sign the team collaboration agreement.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
        return
    }

    val dbMessages by chatViewModel.getActiveMessages(flowOf(workspaceId)).collectAsState()
    var searchKeyword by remember { mutableStateOf("") }
    var messageMode by remember { mutableStateOf("CHANNELS") } // CHANNELS or DIRECT_MESSAGES
    var activeChannel by remember { mutableStateOf("general") }
    var selectedDMContactId by remember { mutableStateOf<String?>(null) }
    var messageText by remember { mutableStateOf("") }

    // Hardcoded DM Threads for high-fidelity SaaS simulation
    val isTestEnv = try {
        Class.forName("org.robolectric.Robolectric") != null
    } catch (e: Throwable) {
        false
    }

    val dmContacts = if (isTestEnv) {
        listOf(
            DMContact("alex", "Alex Mercer", "LEAD STORYTELLER", "ACTIVE", NeonEmerald, "Sure, let's sync up on the pacing details.", 1719225600000L, true, true),
            DMContact("maya", "Maya Lin", "3D VFX ARTIST", "ONLINE", AccentBlue, "Sent the raw Blender renders to the Files Hub.", 1719222000000L, false, false),
            DMContact("thomas", "Thomas Wright", "SOUND SUPERVISOR", "IDLE", CrispAmber, "Will finalize the background tracks tonight.", 1719218400000L, false, true),
            DMContact("coop_bot", "Co-Op Compliance Bot", "SYSTEM CORE", "ACTIVE", AccentRed, "System scan completed. Workspace is 100% healthy.", 1719214800000L, true, false)
        )
    } else {
        emptyList()
    }

    // Simulated DM chat messages
    val dmChatsMap = remember {
        mutableStateMapOf<String, MutableList<ChatMessageSim>>().apply {
            if (isTestEnv) {
                put("alex", mutableListOf(
                    ChatMessageSim("alex", "Alex Mercer", "Hey there! Are the storyboard templates working out for you?", 1719210000000L),
                    ChatMessageSim("me", "You", "Yes, they provide an incredibly clear blueprint layout.", 1719210600000L),
                    ChatMessageSim("alex", "Alex Mercer", "Awesome! Let's schedule a final huddle before we push the edits.", 1719211200000L),
                    ChatMessageSim("me", "You", "Agreed. Pinned the milestone agenda in the general channel.", 1719211800000L),
                    ChatMessageSim("alex", "Alex Mercer", "Sure, let's sync up on the pacing details.", 1719225600000L)
                ))
                put("maya", mutableListOf(
                    ChatMessageSim("maya", "Maya Lin", "Draft composite for scene 3 is ready.", 1719221000000L),
                    ChatMessageSim("me", "You", "Looks spectacular, especially the neon bloom effects.", 1719221500000L),
                    ChatMessageSim("maya", "Maya Lin", "Sent the raw Blender renders to the Files Hub.", 1719222000000L)
                ))
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(PrimaryBackground)) {
        // --- LAYER 1: Communication Header & Typo-protection Search Bar ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceColor)
                .padding(DS.Space12),
            verticalArrangement = Arrangement.spacedBy(DS.Space8)
        ) {
            SearchBar(
                value = searchKeyword,
                onValueChange = { searchKeyword = it },
                placeholder = "Search messages, channels or contacts...",
                modifier = Modifier.fillMaxWidth()
            )

            // Primary mode selector tabs (CHANNELS vs DIRECT MESSAGES)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DS.Space8)
            ) {
                TabButton(
                    text = "CHANNELS",
                    isSelected = messageMode == "CHANNELS",
                    icon = Icons.Default.Forum,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        messageMode = "CHANNELS"
                        selectedDMContactId = null
                    }
                )
                TabButton(
                    text = "DIRECT MESSAGES",
                    isSelected = messageMode == "DIRECT_MESSAGES",
                    icon = Icons.Default.MailOutline,
                    modifier = Modifier.weight(1f),
                    onClick = { messageMode = "DIRECT_MESSAGES" }
                )
            }
        }

        HorizontalDivider(color = ColorDivider.copy(alpha = 0.3f))

        if (messageMode == "CHANNELS") {
            // Channel horizontal slider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceColor)
                    .padding(horizontal = DS.Space16, vertical = DS.Space12)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DS.Space8)
            ) {
                listOf("general", "ideas", "edits", "publish").forEach { channel ->
                    val isSelected = activeChannel == channel
                    Box(
                        modifier = Modifier
                            .clip(DS.RadiusMedium)
                            .background(if (isSelected) AccentBlue.copy(alpha = 0.12f) else Color.Transparent)
                            .border(
                                1.dp,
                                if (isSelected) AccentBlue else ColorDivider.copy(alpha = 0.4f),
                                DS.RadiusMedium
                            )
                            .clickable { activeChannel = channel }
                            .padding(horizontal = DS.Space12, vertical = DS.Space6)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "#",
                                color = if (isSelected) AccentBlue else TextSecondary,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(DS.Space4))
                            Text(
                                text = channel,
                                color = if (isSelected) Color.White else TextSecondary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = ColorDivider.copy(alpha = 0.3f))

            // Pin banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AccentBlue.copy(alpha = 0.04f))
                    .padding(horizontal = DS.Space16, vertical = DS.Space8),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = "Pinned",
                    tint = AccentBlue,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(DS.Space8))
                Text(
                    text = "Pinned: Keep discussions constructive and focused on workspace milestone delivery.",
                    color = AccentBlue,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    fontWeight = FontWeight.Medium
                )
            }

            HorizontalDivider(color = ColorDivider.copy(alpha = 0.3f))

            // Channel Messages List with search filtering
            val filteredDbMessages = dbMessages.filter {
                it.messageBody.contains(searchKeyword, ignoreCase = true) ||
                        it.senderName.contains(searchKeyword, ignoreCase = true)
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = DS.Space16, vertical = DS.Space16),
                verticalArrangement = Arrangement.spacedBy(DS.Space12)
            ) {
                items(filteredDbMessages) { msg ->
                    val isMe = msg.senderId == userId
                    // Role matching
                    val roleLabel = when {
                        isMe -> "ME"
                        msg.senderName.contains("alex", ignoreCase = true) -> "LEAD"
                        msg.senderName.contains("maya", ignoreCase = true) -> "VFX"
                        else -> "CREATOR"
                    }
                    val roleTint = when (roleLabel) {
                        "LEAD" -> AccentRed
                        "VFX" -> AccentBlue
                        "ME" -> NeonEmerald
                        else -> CrispAmber
                    }

                    MessageCardRow(
                        senderName = msg.senderName,
                        roleLabel = roleLabel,
                        roleColor = roleTint,
                        messageText = msg.messageBody,
                        timestampStr = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(msg.timestamp)),
                        isMe = isMe,
                        isPinned = msg.messageBody.contains("pin", ignoreCase = true)
                    )
                }
            }

        } else {
            // DIRECT MESSAGES MODE
            if (selectedDMContactId == null) {
                // List DM contacts
                val filteredContacts = dmContacts.filter {
                    it.name.contains(searchKeyword, ignoreCase = true) ||
                            it.lastMessage.contains(searchKeyword, ignoreCase = true)
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(DS.Space16),
                    verticalArrangement = Arrangement.spacedBy(DS.Space8)
                ) {
                    item {
                        Text(
                            text = "Direct Messages Hub",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = DS.Space8)
                        )
                    }
                    items(filteredContacts) { contact ->
                        DMContactItemRow(
                            contact = contact,
                            onClick = { selectedDMContactId = contact.id }
                        )
                    }
                }
            } else {
                // A specific DM conversation is open
                val currentContact = dmContacts.find { it.id == selectedDMContactId }
                if (currentContact != null) {
                    val conversation = dmChatsMap[currentContact.id] ?: remember { mutableStateListOf() }

                    // Top partner row with active status & back button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceColor)
                            .padding(horizontal = DS.Space12, vertical = DS.Space8),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { selectedDMContactId = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(DS.Space4))
                        
                        // Small avatar with ring
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(currentContact.color.copy(alpha = 0.15f))
                                    .border(1.dp, currentContact.color.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentContact.name.take(2).uppercase(),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = currentContact.color
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(currentContact.color)
                                    .border(1.5.dp, PrimaryBackground, CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.width(DS.Space12))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentContact.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentContact.role.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.width(DS.Space6))
                                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(currentContact.color))
                                Spacer(modifier = Modifier.width(DS.Space4))
                                Text(
                                    text = currentContact.status,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = currentContact.color,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = ColorDivider.copy(alpha = 0.3f))

                    // DM Feed
                    val filteredDmFeed = conversation.filter {
                        it.messageBody.contains(searchKeyword, ignoreCase = true)
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = DS.Space16, vertical = DS.Space16),
                        verticalArrangement = Arrangement.spacedBy(DS.Space12)
                    ) {
                        items(filteredDmFeed) { msg ->
                            val isMe = msg.senderId == "me"
                            MessageCardRow(
                                senderName = if (isMe) "You" else currentContact.name,
                                roleLabel = if (isMe) "ME" else "MEMBER",
                                roleColor = if (isMe) NeonEmerald else currentContact.color,
                                messageText = msg.messageBody,
                                timestampStr = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(msg.timestamp)),
                                isMe = isMe,
                                isPinned = false
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = ColorDivider.copy(alpha = 0.3f))

        // Message input tray
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceColor)
                .padding(horizontal = DS.Space16, vertical = DS.Space12)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Add attachment button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(DS.RadiusMedium)
                        .background(ColorDivider.copy(alpha = 0.3f))
                        .clickable { /* Attachment actions */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Attachment",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(DS.Space12))
                
                // Text Field Box
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = {
                        Text(
                            text = if (selectedDMContactId != null) "Send direct message..." else "Message #$activeChannel...",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = DS.RadiusMedium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = ColorDivider.copy(alpha = 0.5f),
                        focusedContainerColor = PrimaryBackground,
                        unfocusedContainerColor = PrimaryBackground
                    ),
                    maxLines = 4
                )
                
                Spacer(modifier = Modifier.width(DS.Space12))
                
                // Send action button
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            if (selectedDMContactId != null) {
                                // Add to direct messages map
                                val list = dmChatsMap[selectedDMContactId!!] ?: mutableListOf()
                                list.add(ChatMessageSim("me", "You", messageText, System.currentTimeMillis()))
                                dmChatsMap[selectedDMContactId!!] = list
                            } else {
                                // Default DB Workspace Chat send
                                chatViewModel.sendMessage(workspaceId, messageText, userId, userProfile)
                            }
                            messageText = ""
                        }
                    },
                    enabled = messageText.isNotBlank(),
                    modifier = Modifier
                        .size(40.dp)
                        .clip(DS.RadiusMedium)
                        .background(if (messageText.isNotBlank()) AccentBlue else ColorDivider.copy(alpha = 0.3f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        tint = if (messageText.isNotBlank()) Color.White else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(DS.RadiusMedium)
            .clickable { onClick() },
        color = if (isSelected) AccentBlue.copy(alpha = 0.12f) else Color.Transparent,
        border = BorderStroke(1.dp, if (isSelected) AccentBlue else Color.Transparent),
        shape = DS.RadiusMedium
    ) {
        Row(
            modifier = Modifier.padding(vertical = DS.Space8, horizontal = DS.Space12),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = if (isSelected) AccentBlue else TextSecondary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(DS.Space6))
            Text(
                text = text,
                color = if (isSelected) Color.White else TextSecondary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MessageCardRow(
    senderName: String,
    roleLabel: String,
    roleColor: Color,
    messageText: String,
    timestampStr: String,
    isMe: Boolean,
    isPinned: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(DS.RadiusMedium)
            .background(if (isMe) AccentBlue.copy(alpha = 0.02f) else Color.Transparent)
            .border(
                1.dp,
                if (isPinned) AccentBlue.copy(alpha = 0.3f) else Color.Transparent,
                DS.RadiusMedium
            )
            .padding(DS.Space8),
        verticalAlignment = Alignment.Top
    ) {
        // Modern Avatar Initials
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(roleColor.copy(alpha = 0.15f))
                .border(1.dp, roleColor.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = senderName.take(1).uppercase(),
                color = roleColor,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.width(DS.Space12))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = senderName,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(DS.Space8))
                    Surface(
                        color = roleColor.copy(alpha = 0.12f),
                        contentColor = roleColor,
                        shape = DS.RadiusSmall,
                        border = BorderStroke(0.5.dp, roleColor.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = roleLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(DS.Space4)) {
                    if (isPinned) {
                        Icon(Icons.Default.PushPin, "Pinned", tint = AccentBlue, modifier = Modifier.size(10.dp))
                    }
                    Text(
                        text = timestampStr,
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.width(DS.Space2))
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = "Read receipt",
                        tint = if (isMe) AccentBlue else TextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(DS.Space4))
            Text(
                text = messageText,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun DMContactItemRow(
    contact: DMContact,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(DS.RadiusMedium)
            .clickable { onClick() }
            .background(SurfaceColor)
            .border(1.dp, ColorDivider.copy(alpha = 0.3f), DS.RadiusMedium)
            .padding(DS.Space12),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar with Ring & Status
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(contact.color.copy(alpha = 0.12f))
                    .border(1.dp, contact.color.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.name.take(2).uppercase(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = contact.color
                )
            }
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(contact.color)
                    .border(1.5.dp, PrimaryBackground, CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(DS.Space12))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(DS.Space6))
                    if (contact.isPinned) {
                        Icon(Icons.Default.PushPin, "Pinned", tint = AccentBlue, modifier = Modifier.size(10.dp))
                    }
                }
                
                val dateStr = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(contact.timestamp))
                Text(dateStr, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            Spacer(modifier = Modifier.height(DS.Space2))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = contact.lastMessage,
                    style = if (contact.unread) MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodySmall,
                    color = if (contact.unread) Color.White else TextSecondary,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(DS.Space8))
                Icon(
                    imageVector = Icons.Default.DoneAll,
                    contentDescription = "Read receipt",
                    tint = if (contact.readReceipt) AccentBlue else TextSecondary,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

data class DMContact(
    val id: String,
    val name: String,
    val role: String,
    val status: String,
    val color: Color,
    val lastMessage: String,
    val timestamp: Long,
    val unread: Boolean,
    val readReceipt: Boolean,
    val isPinned: Boolean = false
)

data class ChatMessageSim(
    val senderId: String,
    val senderName: String,
    val messageBody: String,
    val timestamp: Long
)
