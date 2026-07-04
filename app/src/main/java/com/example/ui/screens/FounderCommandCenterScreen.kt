package com.example.ui.screens

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.example.CreatorCoopApp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodels.AdminViewModel
import com.example.ui.viewmodels.FounderCrmViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FounderCommandCenterScreen(
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as CreatorCoopApp
    val repository = remember { application.container.repository }
    val scope = rememberCoroutineScope()

    // Database states collected
    val users by repository.getAllUsersFlow().collectAsState(initial = emptyList())
    val workspaces by repository.allWorkspaces.collectAsState(initial = emptyList())
    val tickets by repository.getAllSupportTicketsFlow().collectAsState(initial = emptyList())
    val reports by repository.allReports.collectAsState(initial = emptyList())
    val verifications by repository.getAllVerificationRequestsFlow().collectAsState(initial = emptyList())
    val auditLogs by repository.allAuditLogs.collectAsState(initial = emptyList())

    // UI Interactive States
    var selectedEntityForNotes by remember { mutableStateOf<Pair<String, String>?>(null) } // Pair(EntityType, EntityId)
    var noteInputText by remember { mutableStateOf("") }
    
    // Notes Flow for the selected entity
    val activeNotes = remember(selectedEntityForNotes) {
        selectedEntityForNotes?.let { (type, id) ->
            repository.getNotesForEntity(type, id)
        }
    }?.collectAsState(initial = emptyList())

    // Tab control
    var activeSubTab by remember { mutableStateOf(0) } // 0 = Cockpit Dashboard, 1 = Support Tickets, 2 = Moderation, 3 = Verifications

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "FOUNDER COMMAND COCKPIT",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            letterSpacing = 1.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Closed Beta Governance & Executive Deck",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onNavigate("SEARCH") },
                        modifier = Modifier.testTag("spotlight_search_launcher")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search Everything", tint = AccentBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBackground)
            )
        },
        containerColor = PrimaryBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Navigation Sub-tab row
            TabRow(
                selectedTabIndex = activeSubTab,
                containerColor = SurfaceColor,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeSubTab]),
                        color = AccentBlue
                    )
                },
                modifier = Modifier.fillMaxWidth().border(BorderStroke(0.5.dp, ColorDivider))
            ) {
                Tab(
                    selected = activeSubTab == 0,
                    onClick = { activeSubTab = 0 },
                    text = { Text("COCKPIT", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Leaderboard, null, modifier = Modifier.size(14.dp)) }
                )
                Tab(
                    selected = activeSubTab == 1,
                    onClick = { activeSubTab = 1 },
                    text = { Text("TICKETS", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.SupportAgent, null, modifier = Modifier.size(14.dp)) }
                )
                Tab(
                    selected = activeSubTab == 2,
                    onClick = { activeSubTab = 2 },
                    text = { Text("MODERATION", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Shield, null, modifier = Modifier.size(14.dp)) }
                )
                Tab(
                    selected = activeSubTab == 3,
                    onClick = { activeSubTab = 3 },
                    text = { Text("VERIFY", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.VerifiedUser, null, modifier = Modifier.size(14.dp)) }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (activeSubTab) {
                    0 -> { // Executive Cockpit
                        // Quick Links Drawer
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                QuickLaunchButton("LEDGER", Icons.Default.Gavel, AccentBlue, Modifier.weight(1f)) { onNavigate("ADMIN_AUDIT") }
                                QuickLaunchButton("BACKUPS", Icons.Default.Backup, CrispAmber, Modifier.weight(1f)) { onNavigate("BACKUP_CENTER") }
                                QuickLaunchButton("HEALTH", Icons.Default.NetworkCheck, NeonEmerald, Modifier.weight(1f)) { onNavigate("PLATFORM_HEALTH") }
                            }
                        }

                        // Beta CRM Cohort Summary
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, ColorDivider)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("BETA COHORT SUMMARY", color = AccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                                    Spacer(modifier = Modifier.height(14.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        CohortMetric("Registered", "${users.size}/25", NeonEmerald)
                                        CohortMetric("Spaces", "${workspaces.size}", AccentBlue)
                                        CohortMetric("Agreements", "${workspaces.count { !it.isArchived }}", CrispAmber)
                                        CohortMetric("Actions", "${auditLogs.size}", TextSecondary)
                                    }
                                }
                            }
                        }

                        // Power Users Widget
                        item {
                            Text(
                                text = "COHORT INSIGHTS & SIGNALS",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Power Users Card
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, ColorDivider)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("POWER USERS", color = NeonEmerald, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        val powerUsers = users.filter { it.reputationScore >= 80 }.take(3)
                                        if (powerUsers.isEmpty()) {
                                            Text("No power scores yet", color = TextSecondary, fontSize = 10.sp)
                                        } else {
                                            powerUsers.forEach { pu ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(pu.displayName, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    Text("⭐ ${pu.reputationScore}", color = CrispAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }

                                // Risk Users Card
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, ColorDivider)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("RISK SIGNALS", color = AccentRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        val riskUsers = users.filter { it.systemRole == "SUSPENDED" || it.reputationScore < 30 }.take(3)
                                        if (riskUsers.isEmpty()) {
                                            Text("0 users flags raised", color = TextSecondary, fontSize = 10.sp)
                                        } else {
                                            riskUsers.forEach { ru ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(ru.displayName, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    Text(ru.systemRole, color = AccentRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Audit Ticker Widget
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, ColorDivider)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("RECENT LEDGER LOGS", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    if (auditLogs.isEmpty()) {
                                        Text("No logs captured.", color = TextMuted, fontSize = 11.sp)
                                    } else {
                                        auditLogs.take(3).forEach { log ->
                                            Text(
                                                text = "• [${log.actionTaken}] ${log.reason}",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                lineHeight = 14.sp,
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> { // Support Ticket Desk
                        val openTickets = tickets.filter { it.status == "OPEN" }
                        if (openTickets.isEmpty()) {
                            item {
                                EmptyStateWidget("support agent", "Pristine Support Queue", "All tickets successfully closed.")
                            }
                        } else {
                            items(openTickets) { ticket ->
                                RowItemCard(
                                    title = ticket.title,
                                    subtitle = ticket.description,
                                    status = ticket.status,
                                    badgeColor = NeonEmerald,
                                    onAddNote = { selectedEntityForNotes = "TICKET" to ticket.id }
                                )
                            }
                        }
                    }

                    2 -> { // Moderation Queue
                        val pendingReports = reports.filter { it.status == "PENDING" }
                        if (pendingReports.isEmpty()) {
                            item {
                                EmptyStateWidget("shield", "Moderation Clear", "0 reports requiring immediate administrative review.")
                            }
                        } else {
                            items(pendingReports) { report ->
                                RowItemCard(
                                    title = "Report: ${report.targetType}",
                                    subtitle = report.reason,
                                    status = report.status,
                                    badgeColor = AccentRed,
                                    onAddNote = { selectedEntityForNotes = "REPORT" to report.id }
                                )
                            }
                        }
                    }

                    3 -> { // Verification Queue
                        val pendingVerifications = verifications.filter { it.status == "PENDING" }
                        if (pendingVerifications.isEmpty()) {
                            item {
                                EmptyStateWidget("verified user", "Queue Clear", "No verification applications pending cohort review.")
                            }
                        } else {
                            items(pendingVerifications) { v ->
                                RowItemCard(
                                    title = "Application: ${v.userId}",
                                    subtitle = "Display: ${v.userDisplayName} • Docs verified",
                                    status = v.status,
                                    badgeColor = CrispAmber,
                                    onAddNote = { selectedEntityForNotes = "VERIFY" to v.id }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Interactive Notes Drawer/Dialog
    selectedEntityForNotes?.let { (type, id) ->
        AlertDialog(
            onDismissRequest = { selectedEntityForNotes = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FOUNDER NOTES - $type",
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                    IconButton(onClick = { selectedEntityForNotes = null }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Admin-only immutable record notes.", color = TextMuted, fontSize = 10.sp)
                    
                    // Notes List
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryBackground)
                            .border(1.dp, ColorDivider)
                            .padding(8.dp)
                    ) {
                        if (activeNotes?.value.isNullOrEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No internal notes registered.", color = TextMuted, fontSize = 11.sp)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(activeNotes!!.value) { note ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(SurfaceColor, RoundedCornerShape(6.dp))
                                            .border(0.5.dp, ColorDivider, RoundedCornerShape(6.dp))
                                            .padding(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(note.authorName, color = AccentBlue, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                            IconButton(
                                                onClick = {
                                                    scope.launch {
                                                        repository.deleteFounderNote(note.id)
                                                    }
                                                },
                                                modifier = Modifier.size(16.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, null, tint = AccentRed, modifier = Modifier.size(12.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(note.noteContent, color = Color.White, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Notes Input
                    OutlinedTextField(
                        value = noteInputText,
                        onValueChange = { noteInputText = it },
                        placeholder = { Text("Write internal founder note...", color = TextSecondary, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("founder_note_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = ColorDivider,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteInputText.isNotBlank()) {
                            scope.launch {
                                val note = FounderNote(
                                    id = UUID.randomUUID().toString(),
                                    entityType = type,
                                    entityId = id,
                                    noteContent = noteInputText,
                                    createdAt = System.currentTimeMillis()
                                )
                                repository.insertFounderNote(note)
                                noteInputText = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("SAVE NOTE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SurfaceColor,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun QuickLaunchButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(54.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, ColorDivider)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            Text(label, color = Color.White, fontWeight = FontWeight.Black, fontSize = 10.sp)
        }
    }
}

@Composable
fun CohortMetric(label: String, valStr: String, color: Color) {
    Column {
        Text(label, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(valStr, color = color, fontSize = 14.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun RowItemCard(
    title: String,
    subtitle: String,
    status: String,
    badgeColor: Color,
    onAddNote: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, ColorDivider)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Box(
                        modifier = Modifier
                            .background(badgeColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                            .border(0.5.dp, badgeColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(status, color = badgeColor, fontSize = 8.sp, fontWeight = FontWeight.Black)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, color = TextSecondary, fontSize = 11.sp)
            }
            
            Button(
                onClick = onAddNote,
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceLightColor),
                border = BorderStroke(1.dp, ColorDivider),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Icon(Icons.Default.NoteAdd, null, tint = AccentBlue, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("NOTES", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EmptyStateWidget(iconName: String, title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Verified, null, tint = ColorDivider, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
        Text(subtitle, color = TextSecondary, fontSize = 11.sp)
    }
}
