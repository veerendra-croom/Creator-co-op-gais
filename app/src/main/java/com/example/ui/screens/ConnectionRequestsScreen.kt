package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

import androidx.compose.foundation.lazy.items
import com.example.ui.viewmodels.GlobalViewModel
import com.example.data.model.ConnectionRequest
import com.example.data.model.UserProfile
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ConnectionRequestsScreen(
    onBack: () -> Unit,
    globalViewModel: GlobalViewModel,
    userProfile: UserProfile?
) {
    if (userProfile == null) return

    val pendingRequests by remember(userProfile.id) { globalViewModel.getPendingConnectionRequests(userProfile.id) }.collectAsState(initial = emptyList())
    val resolvedRequests by remember(userProfile.id) { globalViewModel.getResolvedConnectionRequests(userProfile.id) }.collectAsState(initial = emptyList())

    var selectedTab by remember { mutableStateOf("Pending") }
    val tabs = listOf("Pending", "Accepted", "Declined")

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
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Connection Requests", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }

        TabRow(
            selectedTabIndex = tabs.indexOf(selectedTab),
            containerColor = Color.Transparent,
            contentColor = AccentBlue,
            indicator = {},
            divider = {}
        ) {
            tabs.forEach { tab ->
                val isSelected = selectedTab == tab
                Tab(
                    selected = isSelected,
                    onClick = { selectedTab = tab },
                    text = {
                        Text(
                            text = tab,
                            color = if (isSelected) AccentBlue else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                        )
                    }
                )
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (selectedTab == "Pending") {
                if (pendingRequests.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxSize().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                            Text("No pending requests.", color = TextSecondary)
                        }
                    }
                } else {
                    items(pendingRequests, key = { it.id }) { req ->
                        ConnectionRequestItem(
                            request = req,
                            onAccept = { globalViewModel.acceptConnectionRequest(req.id) },
                            onDecline = { globalViewModel.declineConnectionRequest(req.id) }
                        )
                    }
                }
            } else {
                val filtered = resolvedRequests.filter { it.status.equals(selectedTab, ignoreCase = true) }
                if (filtered.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxSize().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                            Text("No requests found in this category.", color = TextSecondary)
                        }
                    }
                } else {
                    items(filtered, key = { it.id }) { req ->
                        ConnectionRequestItem(
                            request = req,
                            onAccept = {},
                            onDecline = {}
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectionRequestItem(
    request: ConnectionRequest,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val isIncoming = request.status == "PENDING"
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, ColorDivider)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(AccentBlue.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(request.senderName.take(1).uppercase(), color = AccentBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(request.senderName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(request.senderRole, color = TextSecondary, fontSize = 13.sp)
            }
            if (isIncoming) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onAccept, modifier = Modifier.size(36.dp).background(AccentBlue, CircleShape)) {
                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDecline, modifier = Modifier.size(36.dp).background(SurfaceColor, CircleShape).border(1.dp, ColorDivider, CircleShape)) {
                        Icon(Icons.Default.Close, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                    }
                }
            } else {
                Text(if (request.status == "ACCEPTED") "Accepted" else "Declined", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
