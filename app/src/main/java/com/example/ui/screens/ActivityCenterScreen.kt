package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserAuditLog
import com.example.data.model.UserProfile
import com.example.ui.theme.*
import com.example.ui.viewmodels.GlobalViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ActivityCenterScreen(
    onBack: () -> Unit,
    globalViewModel: GlobalViewModel,
    userProfile: UserProfile?
) {
    val application = LocalContext.current.applicationContext as com.example.CreatorCoopApp
    val repository = remember { application.container.repository }
    val currentUserId = userProfile?.id.orEmpty()

    val auditLogs by repository.getAllUserAuditLogsFlow().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    // Filter logs for this user
    val userLogs = remember(auditLogs, currentUserId) {
        if (currentUserId.isBlank()) emptyList()
        else auditLogs.filter { it.userId == currentUserId || it.targetUserId == currentUserId }
            .sortedByDescending { it.createdAt }
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("activity_center_back_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Activity Center", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }

        Text(
            text = "A complete cryptographically ordered local audit log tracking your workspace operations, role handshakes, and ledger events.",
            color = TextSecondary,
            fontSize = 13.sp
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            item {
                Text(
                    text = "AUDIT LOG HISTORY",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            if (userLogs.isEmpty()) {
                item {
                    Text("No activity recorded yet.", color = TextMuted, fontSize = 14.sp)
                }
            } else {
                items(userLogs, key = { it.id }) { log ->
                    val (icon, color) = when (log.action) {
                        "WORKSPACE_CREATED" -> Pair(Icons.Default.GroupAdd, AccentBlue)
                        "ROLE_ASSIGNED" -> Pair(Icons.Default.Badge, CrispAmber)
                        "CONTRACT_LOCKED" -> Pair(Icons.Default.Lock, NeonEmerald)
                        else -> Pair(Icons.Default.Info, TextSecondary)
                    }

                    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    val timeStr = sdf.format(Date(log.createdAt))

                    ActivityLogItem(
                        action = log.actionTaken,
                        target = log.reason,
                        time = timeStr,
                        icon = icon,
                        tint = color
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityLogItem(
    action: String,
    target: String,
    time: String,
    icon: ImageVector,
    tint: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, ColorDivider),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(action, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(target, color = TextSecondary, fontSize = 13.sp)
            }
            Text(time, color = TextMuted, fontSize = 11.sp)
        }
    }
}
