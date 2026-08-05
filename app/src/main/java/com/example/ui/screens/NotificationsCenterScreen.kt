package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Notification
import com.example.data.model.UserProfile
import com.example.ui.theme.*
import com.example.ui.viewmodels.GlobalViewModel
import com.example.ui.viewmodels.WorkspaceViewModel
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsCenterScreen(
    globalViewModel: GlobalViewModel,
    workspaceViewModel: WorkspaceViewModel,
    userProfile: UserProfile?
) {
    if (userProfile == null) return

    val context = LocalContext.current
    val application = context.applicationContext as com.example.CreatorCoopApp
    val repository = remember { application.container.repository }
    val coroutineScope = rememberCoroutineScope()

    val notifications by remember(userProfile.id) { globalViewModel.getNotificationsForUser(userProfile.id) }.collectAsState(initial = emptyList())
    val announcements by repository.getAllAnnouncementsFlow().collectAsState(initial = emptyList())
    val interactions by repository.getUserAnnouncementInteractionsFlow(userProfile.id).collectAsState(initial = emptyList())
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") } // ALL, MENTIONS, WORKSPACE, APPROVALS, SUPPORT, VERIFICATION, SYSTEM, FOUNDER, ADMIN
    var selectedStatusTab by remember { mutableStateOf("ACTIVE") } // ACTIVE, UNREAD, PINNED, ARCHIVED

    val categories = listOf(
        "ALL" to "All Streams",
        "MENTIONS" to "Mentions",
        "WORKSPACE" to "Workspace",
        "APPROVALS" to "Approvals",
        "SUPPORT" to "Support",
        "VERIFICATION" to "Verification",
        "SYSTEM" to "System",
        "FOUNDER" to "Founder",
        "ADMIN" to "Admin"
    )

    val statusTabs = listOf(
        "ACTIVE" to "Active Inbox",
        "UNREAD" to "Unread",
        "ANNOUNCEMENTS" to "Announcements",
        "ARCHIVED" to "Archive"
    )

    // Advanced search, status, and category filtering logic
    val filteredNotifications = remember(notifications, searchQuery, selectedCategoryFilter, selectedStatusTab) {
        notifications.filter { notif ->
            // 1. Status filters
            val statusMatches = when (selectedStatusTab) {
                "ACTIVE" -> !notif.isArchived
                "UNREAD" -> !notif.isRead && !notif.isArchived
                "PINNED" -> notif.isPinned && !notif.isArchived
                "ARCHIVED" -> notif.isArchived
                else -> true
            }

            // 2. Category / Type filters
            val categoryMatches = if (selectedCategoryFilter == "ALL") {
                true
            } else {
                notif.type.equals(selectedCategoryFilter, ignoreCase = true)
            }

            // 3. Search query filters
            val searchMatches = if (searchQuery.isBlank()) {
                true
            } else {
                notif.title.contains(searchQuery, ignoreCase = true) ||
                notif.body.contains(searchQuery, ignoreCase = true)
            }

            statusMatches && categoryMatches && searchMatches
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header Section ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Unified Inbox",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "Consolidated stream of collaboration events, alerts, and mentions",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            IconButton(
                onClick = { globalViewModel.markAllNotificationsAsRead(userProfile.id) },
                colors = IconButtonDefaults.iconButtonColors(containerColor = SurfaceColor)
            ) {
                Icon(Icons.Default.DoneAll, contentDescription = "Mark all read", tint = AccentBlue)
            }
        }

        // --- Search bar ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search notification subject, details...", color = TextSecondary, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = SurfaceColor,
                unfocusedContainerColor = SurfaceColor,
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = ColorDivider
            ),
            singleLine = true
        )

        // --- Status Tabs (Active, Unread, Pinned, Archived) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            statusTabs.forEach { (tabKey, tabLabel) ->
                val isSelected = selectedStatusTab == tabKey
                val count = when (tabKey) {
                    "ACTIVE" -> notifications.count { !it.isArchived }
                    "UNREAD" -> notifications.count { !it.isRead && !it.isArchived }
                    "ANNOUNCEMENTS" -> {
                        announcements.count { ann ->
                            val interaction = interactions.find { it.announcementId == ann.id }
                            interaction == null || !interaction.isOpened
                        }
                    }
                    "ARCHIVED" -> notifications.count { it.isArchived }
                    else -> 0
                }

                Surface(
                    color = if (isSelected) AccentBlue.copy(alpha = 0.15f) else SurfaceColor,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isSelected) AccentBlue else ColorDivider),
                    modifier = Modifier.weight(1f),
                    onClick = { selectedStatusTab = tabKey }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = tabLabel,
                            color = if (isSelected) Color.White else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .background(if (count > 0 && tabKey == "UNREAD") AccentRed else ColorDivider, CircleShape)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = count.toString(),
                                color = if (count > 0 && tabKey == "UNREAD") Color.White else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }

        // --- Category Filters ---
        ScrollableTabRow(
            selectedTabIndex = categories.indexOfFirst { it.first == selectedCategoryFilter }.coerceAtLeast(0),
            containerColor = Color.Transparent,
            edgePadding = 0.dp,
            indicator = {},
            divider = {}
        ) {
            categories.forEach { (catKey, catLabel) ->
                val isSelected = selectedCategoryFilter == catKey
                val countInCat = if (selectedStatusTab == "ANNOUNCEMENTS") 0 else notifications.count {
                    val statusMatches = when (selectedStatusTab) {
                        "ACTIVE" -> !it.isArchived
                        "UNREAD" -> !it.isRead && !it.isArchived
                        "ARCHIVED" -> it.isArchived
                        else -> true
                    }
                    val catMatches = if (catKey == "ALL") true else it.type.equals(catKey, ignoreCase = true)
                    statusMatches && catMatches
                }

                Tab(
                    selected = isSelected,
                    onClick = { selectedCategoryFilter = catKey },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .background(
                                    if (isSelected) AccentBlue.copy(alpha = 0.12f) else SurfaceColor,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) AccentBlue else ColorDivider,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = catLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) AccentBlue else TextSecondary
                            )
                            if (countInCat > 0) {
                                Box(
                                    modifier = Modifier
                                        .background(if (isSelected) AccentBlue else ColorDivider, CircleShape)
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = countInCat.toString(),
                                        color = if (isSelected) Color.White else TextSecondary,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }

        // --- Notifications Stream ---
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (selectedStatusTab == "ANNOUNCEMENTS") {
                if (announcements.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Campaign,
                                    contentDescription = null,
                                    tint = ColorDivider,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No announcements",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Platform-wide updates will appear here.",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                } else {
                    items(announcements, key = { it.id }) { ann ->
                        val interaction = interactions.find { it.announcementId == ann.id }
                        val isOpened = interaction?.isOpened ?: false
                        
                        UnifiedAnnouncementItem(
                            announcement = ann,
                            isOpened = isOpened,
                            onClick = {
                                coroutineScope.launch {
                                    val currentInteraction = repository.getAnnouncementInteraction(ann.id, userProfile.id)
                                    if (currentInteraction == null) {
                                        repository.insertAnnouncementInteraction(
                                            com.example.data.model.AnnouncementInteraction(
                                                id = "${userProfile.id}_${ann.id}",
                                                announcementId = ann.id,
                                                userId = userProfile.id,
                                                isOpened = true,
                                                isClicked = false,
                                                isDismissed = false,
                                                timestamp = System.currentTimeMillis(),
                                                readAt = System.currentTimeMillis()
                                            )
                                        )
                                    } else {
                                        repository.insertAnnouncementInteraction(
                                            currentInteraction.copy(isOpened = true, readAt = System.currentTimeMillis())
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            } else {
                if (filteredNotifications.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Inbox,
                                    contentDescription = null,
                                    tint = ColorDivider,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Your inbox is clear",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "No items match your selected filters.",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                } else {
                    items(filteredNotifications, key = { it.id }) { notif ->
                        UnifiedInboxItem(
                            notification = notif,
                            onClick = { globalViewModel.handleNotificationClick(notif, workspaceViewModel) },
                            onToggleRead = { globalViewModel.updateNotificationReadState(notif.id, !notif.isRead) },
                            onTogglePin = { globalViewModel.pinNotification(notif.id, !notif.isPinned) },
                            onToggleArchive = { globalViewModel.archiveNotification(notif.id, !notif.isArchived) },
                            onDelete = { globalViewModel.deleteNotification(notif) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UnifiedInboxItem(
    notification: Notification,
    onClick: () -> Unit,
    onToggleRead: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleArchive: () -> Unit,
    onDelete: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()) }
    val timeStr = formatter.format(Date(notification.createdAt))

    // Match icons based on type categories
    val icon = when (notification.type.uppercase()) {
        "MENTIONS" -> Icons.Default.AlternateEmail
        "WORKSPACE" -> Icons.Default.FolderOpen
        "APPROVALS" -> Icons.Default.FactCheck
        "SUPPORT" -> Icons.Default.SupportAgent
        "VERIFICATION" -> Icons.Default.Verified
        "SYSTEM" -> Icons.Default.SystemUpdateAlt
        "FOUNDER" -> Icons.Default.Star
        "ADMIN" -> Icons.Default.Security
        "TASKS" -> Icons.Default.Assignment
        "MESSAGES" -> Icons.Default.Chat
        "INVITES" -> Icons.Default.PersonAdd
        "AGREEMENTS" -> Icons.Default.Gavel
        else -> Icons.Default.Notifications
    }

    // Match color schema
    val tint = when (notification.type.uppercase()) {
        "MENTIONS" -> AccentBlue
        "WORKSPACE" -> CrispAmber
        "APPROVALS" -> NeonEmerald
        "SUPPORT" -> AccentBlue
        "VERIFICATION" -> NeonEmerald
        "SYSTEM" -> CrispAmber
        "FOUNDER" -> CrispAmber
        "ADMIN" -> AccentRed
        "TASKS" -> AccentBlue
        "MESSAGES" -> AccentBlue
        "INVITES" -> CrispAmber
        "AGREEMENTS" -> NeonEmerald
        else -> AccentBlue
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (notification.isRead) ColorDivider else tint.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (notification.isRead) SurfaceLightColor else tint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (notification.isRead) TextSecondary else tint,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = notification.title,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            
                            // Category badge
                            Box(
                                modifier = Modifier
                                    .background(tint.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                    .border(0.5.dp, tint.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = notification.type.uppercase(),
                                    color = tint,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 7.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        Text(
                            text = timeStr,
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = notification.body,
                        color = if (notification.isRead) TextSecondary else TextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = ColorDivider, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(6.dp))

            // Action strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mark Read / Unread
                    Row(
                        modifier = Modifier.clickable { onToggleRead() },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (notification.isRead) Icons.Default.Drafts else Icons.Default.Mail,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (notification.isRead) "Mark Unread" else "Mark Read",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Pin / Unpin
                    Row(
                        modifier = Modifier.clickable { onTogglePin() },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (notification.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                            contentDescription = null,
                            tint = if (notification.isPinned) CrispAmber else TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (notification.isPinned) "Unpin" else "Pin",
                            color = if (notification.isPinned) CrispAmber else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Archive / Unarchive
                    Row(
                        modifier = Modifier.clickable { onToggleArchive() },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (notification.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (notification.isArchived) "Send Inbox" else "Archive",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = AccentRed.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun UnifiedAnnouncementItem(
    announcement: com.example.data.model.Announcement,
    isOpened: Boolean,
    onClick: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()) }
    val timeStr = formatter.format(Date(announcement.createdAt))

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                onClick()
                expanded = !expanded
            },
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (isOpened) ColorDivider else CrispAmber.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isOpened) SurfaceLightColor else CrispAmber.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "Announcement",
                        tint = if (isOpened) TextSecondary else CrispAmber,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "OFFICIAL ANNOUNCEMENT",
                            color = CrispAmber,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = timeStr,
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = announcement.title,
                        color = if (isOpened) TextPrimary else Color.White,
                        fontSize = 13.sp,
                        fontWeight = if (isOpened) FontWeight.Bold else FontWeight.Black,
                        maxLines = if (expanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = announcement.content,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        maxLines = if (expanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
