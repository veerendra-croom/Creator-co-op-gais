package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.style.TextOverflow
import com.example.ui.viewmodels.GlobalViewModel
import com.example.ui.viewmodels.WorkspaceViewModel
import com.example.data.model.Notification
import com.example.data.model.UserProfile
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationsCenterScreen(
    globalViewModel: GlobalViewModel,
    workspaceViewModel: WorkspaceViewModel,
    userProfile: UserProfile?
) {
    if (userProfile == null) return

    val notifications by globalViewModel.getNotificationsForUser(userProfile.id).collectAsState(initial = emptyList())
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Tasks", "Messages", "Invites", "Agreements", "INFO")

    val filteredNotifications = if (selectedFilter == "All") {
        notifications
    } else {
        notifications.filter { it.type.equals(selectedFilter, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Notifications Center",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            IconButton(onClick = { globalViewModel.markAllNotificationsAsRead(userProfile.id) }) {
                Icon(Icons.Default.DoneAll, contentDescription = "Mark all read", tint = AccentBlue)
            }
        }
        
        // Filters
        ScrollableTabRow(
            selectedTabIndex = filters.indexOf(selectedFilter),
            containerColor = Color.Transparent,
            contentColor = AccentBlue,
            edgePadding = 0.dp,
            indicator = {},
            divider = {}
        ) {
            filters.forEach { filter ->
                val isSelected = selectedFilter == filter
                Surface(
                    color = if (isSelected) AccentBlue.copy(alpha = 0.15f) else SurfaceColor,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, if (isSelected) AccentBlue else ColorDivider),
                    modifier = Modifier.padding(end = 8.dp),
                    onClick = { selectedFilter = filter }
                ) {
                    Text(
                        text = filter,
                        color = if (isSelected) AccentBlue else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filteredNotifications.isEmpty()) {
                item {
                    Text(
                        text = "No notifications yet.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
                    )
                }
            } else {
                items(filteredNotifications, key = { it.id }) { notif ->
                    NotificationItem(
                        notification = notif,
                        onClick = { globalViewModel.handleNotificationClick(notif, workspaceViewModel) },
                        onDelete = { globalViewModel.deleteNotification(notif) }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    val timeStr = formatter.format(Date(notification.createdAt))

    val icon = when (notification.type) {
        "TASKS" -> Icons.Default.Assignment
        "MESSAGES" -> Icons.Default.Chat
        "INVITES" -> Icons.Default.PersonAdd
        "AGREEMENTS" -> Icons.Default.Gavel
        else -> Icons.Default.Info
    }

    val tint = if (notification.isRead) TextSecondary else AccentBlue

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (notification.isRead) ColorDivider else AccentBlue.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Black
                    )
                    Text(
                        text = timeStr,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.body,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Delete", tint = TextSecondary, modifier = Modifier.size(16.dp))
            }
        }
    }
}
