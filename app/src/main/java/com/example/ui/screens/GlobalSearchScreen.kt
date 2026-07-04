package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.CreatorCoopApp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodels.AppViewModelFactory
import kotlinx.coroutines.flow.map

// Search Result Data Structure
data class SearchResult(
    val id: String,
    val type: String, // "USER", "WORKSPACE", "TASK", "TICKET", "REPORT", "CAMPAIGN"
    val title: String,
    val subtitle: String,
    val status: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val accentColor: androidx.compose.ui.graphics.Color,
    val originalEntity: Any
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchScreen() {
    val application = LocalContext.current.applicationContext as CreatorCoopApp
    val repository = remember { application.container.repository }
    
    // Live database flows collected as states
    val users by repository.getAllUsersFlow().collectAsState(initial = emptyList())
    val workspaces by repository.allWorkspaces.collectAsState(initial = emptyList())
    val tasks by repository.getAllProductionTasksFlow().collectAsState(initial = emptyList())
    val tickets by repository.getAllSupportTicketsFlow().collectAsState(initial = emptyList())
    val reports by repository.allReports.collectAsState(initial = emptyList())
    val announcements by repository.getAllAnnouncementsFlow().collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("ALL") } // ALL, USERS, WORKSPACES, TASKS, TICKETS, REPORTS, CAMPAIGNS

    // Reactive search mapping
    val searchResults = remember(searchQuery, users, workspaces, tasks, tickets, reports, announcements) {
        if (searchQuery.isBlank()) return@remember emptyList<SearchResult>()
        
        val results = mutableListOf<SearchResult>()
        
        // 1. Map Users
        users.forEach { user ->
            if (user.displayName.contains(searchQuery, ignoreCase = true) ||
                user.username.contains(searchQuery, ignoreCase = true) ||
                user.bio.contains(searchQuery, ignoreCase = true) ||
                user.systemRole.contains(searchQuery, ignoreCase = true)) {
                results.add(
                    SearchResult(
                        id = user.id,
                        type = "USER",
                        title = user.displayName,
                        subtitle = "@${user.username} • ${user.bio.ifBlank { "No bio provided" }}",
                        status = user.systemRole,
                        icon = Icons.Default.Person,
                        accentColor = AccentBlue,
                        originalEntity = user
                    )
                )
            }
        }

        // 2. Map Workspaces
        workspaces.forEach { ws ->
            val statusString = if (ws.isArchived) "ARCHIVED" else "ACTIVE"
            if (ws.name.contains(searchQuery, ignoreCase = true) ||
                ws.platformType.contains(searchQuery, ignoreCase = true) ||
                statusString.contains(searchQuery, ignoreCase = true)) {
                results.add(
                    SearchResult(
                        id = ws.id,
                        type = "WORKSPACE",
                        title = ws.name,
                        subtitle = ws.platformType,
                        status = statusString,
                        icon = Icons.Default.Folder,
                        accentColor = CrispAmber,
                        originalEntity = ws
                    )
                )
            }
        }

        // 3. Map Tasks
        tasks.forEach { task ->
            if (task.title.contains(searchQuery, ignoreCase = true) ||
                task.contentBody.contains(searchQuery, ignoreCase = true) ||
                task.stateScope.contains(searchQuery, ignoreCase = true)) {
                results.add(
                    SearchResult(
                        id = task.id,
                        type = "TASK",
                        title = task.title,
                        subtitle = task.contentBody,
                        status = task.stateScope,
                        icon = Icons.Default.Assignment,
                        accentColor = AccentBlue,
                        originalEntity = task
                    )
                )
            }
        }

        // 4. Map Tickets
        tickets.forEach { ticket ->
            if (ticket.title.contains(searchQuery, ignoreCase = true) ||
                ticket.description.contains(searchQuery, ignoreCase = true) ||
                ticket.status.contains(searchQuery, ignoreCase = true)) {
                results.add(
                    SearchResult(
                        id = ticket.id,
                        type = "TICKET",
                        title = ticket.title,
                        subtitle = ticket.description,
                        status = ticket.status,
                        icon = Icons.Default.SupportAgent,
                        accentColor = NeonEmerald,
                        originalEntity = ticket
                    )
                )
            }
        }

        // 5. Map Reports
        reports.forEach { report ->
            if (report.reason.contains(searchQuery, ignoreCase = true) ||
                report.targetType.contains(searchQuery, ignoreCase = true) ||
                report.status.contains(searchQuery, ignoreCase = true)) {
                results.add(
                    SearchResult(
                        id = report.id,
                        type = "REPORT",
                        title = "Report: ${report.targetType}",
                        subtitle = report.reason,
                        status = report.status,
                        icon = Icons.Default.Report,
                        accentColor = AccentRed,
                        originalEntity = report
                    )
                )
            }
        }

        // 6. Map Campaigns/Announcements
        announcements.forEach { ann ->
            if (ann.title.contains(searchQuery, ignoreCase = true) ||
                ann.content.contains(searchQuery, ignoreCase = true) ||
                ann.status.contains(searchQuery, ignoreCase = true)) {
                results.add(
                    SearchResult(
                        id = ann.id,
                        type = "CAMPAIGN",
                        title = ann.title,
                        subtitle = ann.content,
                        status = ann.status,
                        icon = Icons.Default.Campaign,
                        accentColor = AccentRed,
                        originalEntity = ann
                    )
                )
            }
        }

        results
    }

    // Filtered results based on type tab
    val filteredResults = remember(searchResults, selectedTab) {
        if (selectedTab == "ALL") searchResults
        else searchResults.filter { it.type == selectedTab }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search Input Bar styled like Ctrl+K Spotlight Search
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, ColorDivider)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Search, tint = TextSecondary, contentDescription = null)
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search creators, tasks, rules, and logs...", color = TextSecondary, fontSize = 14.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("spotlight_search_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                } else {
                    // Modern Keyboard shortcut indicator
                    Box(
                        modifier = Modifier
                            .background(SurfaceLightColor, RoundedCornerShape(6.dp))
                            .border(1.dp, ColorDivider, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "⌘ K",
                            color = TextSecondary,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Live Filters Row
        if (searchQuery.isNotBlank()) {
            ScrollableTabRow(
                selectedTabIndex = when (selectedTab) {
                    "ALL" -> 0
                    "USER" -> 1
                    "WORKSPACE" -> 2
                    "TASK" -> 3
                    "TICKET" -> 4
                    "REPORT" -> 5
                    "CAMPAIGN" -> 6
                    else -> 0
                },
                containerColor = Color.Transparent,
                edgePadding = 0.dp,
                divider = {},
                indicator = {}
            ) {
                listOf(
                    "ALL" to "All Results",
                    "USER" to "Creators",
                    "WORKSPACE" to "Workspaces",
                    "TASK" to "Tasks",
                    "TICKET" to "Tickets",
                    "REPORT" to "Reports",
                    "CAMPAIGN" to "Campaigns"
                ).forEach { (tabKey, tabLabel) ->
                    val isSelected = selectedTab == tabKey
                    Tab(
                        selected = isSelected,
                        onClick = { selectedTab = tabKey },
                        text = {
                            Text(
                                text = tabLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) AccentBlue else TextSecondary,
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
                            )
                        }
                    )
                }
            }
        }

        // Search body
        if (searchQuery.isBlank()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.ManageSearch, contentDescription = null, tint = ColorDivider, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Search everything across Creator Co-op", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Instant index lookup for users, projects, support, and reports", color = TextSecondary, fontSize = 12.sp)
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Keyboard hints
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    KeyboardShortcutTip("⌘ K", "Open search bar")
                    KeyboardShortcutTip("ESC", "Close Spotlight")
                    KeyboardShortcutTip("↵", "Action item")
                }
            }
        } else {
            // Results list
            if (filteredResults.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Outlined.SearchOff, null, tint = ColorDivider, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No results matching '$searchQuery'", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Try checking spelling or adjusting scope tab filters", color = TextSecondary, fontSize = 12.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        Text(
                            text = "Found ${filteredResults.size} matches",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                    
                    items(filteredResults) { result ->
                        SearchResultRow(result)
                    }
                }
            }
        }
    }
}

@Composable
fun KeyboardShortcutTip(key: String, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .background(SurfaceColor, RoundedCornerShape(4.dp))
                .border(1.dp, ColorDivider, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(key, color = TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
        Text(label, color = TextMuted, fontSize = 10.sp)
    }
}

@Composable
fun SearchResultRow(result: SearchResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("search_result_item_${result.id}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, ColorDivider)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(result.accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(result.icon, null, tint = result.accentColor, modifier = Modifier.size(18.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = result.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // Type Tag
                    Box(
                        modifier = Modifier
                            .background(result.accentColor.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                            .border(0.5.dp, result.accentColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = result.type,
                            color = result.accentColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 8.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(3.dp))
                
                Text(
                    text = result.subtitle,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Status Tag
            if (result.status.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .background(SurfaceLightColor, RoundedCornerShape(6.dp))
                        .border(1.dp, ColorDivider, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = result.status,
                        color = if (result.status == "PENDING" || result.status == "SUSPENDED") AccentRed else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}
