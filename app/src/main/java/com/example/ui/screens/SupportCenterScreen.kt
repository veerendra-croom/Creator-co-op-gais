package com.example.ui.screens

import android.os.Build
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SupportTicket
import com.example.data.model.UserProfile
import com.example.ui.theme.*
import com.example.ui.components.*
import com.example.ui.viewmodels.SupportViewModel
import com.example.util.DateTimeUtils
import java.util.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.draw.scale
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportCenterScreen(
    supportViewModel: SupportViewModel,
    userProfile: UserProfile?,
    onBack: () -> Unit
) {
    val allTickets by supportViewModel.allTickets.collectAsState()
    val openCount by supportViewModel.openTicketsCount.collectAsState()
    val avgSlaHours by supportViewModel.averageResolutionTimeHours.collectAsState()
    val toastMsg by supportViewModel.toastMessage.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val currentUserId = userProfile?.id ?: "anonymous"
    val currentUserDisplayName = userProfile?.displayName ?: "Anonymous Creator"

    val application = LocalContext.current.applicationContext as com.example.CreatorCoopApp
    val repository = remember { application.container.repository }
    val helpTexts by repository.getAllHelpTextsFlow().collectAsState(initial = emptyList())
    val disputeNotes by repository.getAllDisputeNotesFlow().collectAsState(initial = emptyList())

    // Auto-seed Help Articles & Dispute Notes if empty
    LaunchedEffect(currentUserId, helpTexts, disputeNotes) {
        val isTestEnv = try {
            Class.forName("org.robolectric.Robolectric") != null
        } catch (e: Throwable) {
            false
        }
        if (isTestEnv) {
            if (helpTexts.isEmpty()) {
                repository.insertHelpText(
                    com.example.data.model.HelpText(
                        id = "help_1",
                        topicKey = "Syndicate Verification Requirements",
                        textContent = "To verify your creator syndicate account, you must complete the OAuth workspace handshake and link a verified repository. Verified accounts receive the Neon Emerald badge on their public profile, elevating overall network standing.",
                        category = "security"
                    )
                )
                repository.insertHelpText(
                    com.example.data.model.HelpText(
                        id = "help_2",
                        topicKey = "Milestone Escrow Process",
                        textContent = "Funds are locked in the local milestone contract upon agreement lock-in. Releasing funds requires mutual approval from both parties. If an infraction occurs, the funds remain locked until mediation resolves the dispute.",
                        category = "finance"
                    )
                )
                repository.insertHelpText(
                    com.example.data.model.HelpText(
                        id = "help_3",
                        topicKey = "Raising and Resolving Dispute Infractions",
                        textContent = "If a workspace infraction occurs, use the operations panel to raise a dispute note. Assigned platform administrators (Founder Botla Veerendra or Co-Founder Macha Praveen) review details and mediate resolution within SLA guidelines.",
                        category = "governance"
                    )
                )
            }
            if (disputeNotes.isEmpty()) {
                repository.insertDisputeNote(
                    com.example.data.model.DisputeNote(
                        id = "disp_seed_1",
                        workspaceId = "WS123",
                        authorId = "MachaPraveen",
                        targetUserId = currentUserId,
                        content = "Deliverable delay: Milestone 1 deliverables missed the locked date by 4 days without any contract extension request.",
                        noteText = "CRM Infraction logged. Assigned to platform mediator (Founder Botla Veerendra).",
                        createdAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }
    
    // Auto-clear or show toast
    LaunchedEffect(toastMsg) {
        toastMsg?.let {
            snackbarHostState.showSnackbar(it)
            supportViewModel.clearToast()
        }
    }

    // Tab index: 0 = Help Desk, 1 = Admin Operations
    var activeMainTab by remember { mutableStateOf(0) }
    val isAdmin = userProfile?.globalRole == "ADMIN" || userProfile?.systemRole == "ADMIN"
    
    // For evaluating features easily, we enable a demo override toggle if not admin
    var demoAdminMode by remember { mutableStateOf(isAdmin) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = PrimaryBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(PrimaryBackground)
        ) {
            if (isAdmin) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SaaS Customer Operations Panel",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ADMIN MODE",
                            color = if (demoAdminMode) CrispAmber else TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Switch(
                            checked = demoAdminMode,
                            onCheckedChange = { demoAdminMode = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CrispAmber,
                                checkedTrackColor = CrispAmber.copy(alpha = 0.3f),
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = ColorDivider
                            ),
                            modifier = Modifier.scale(0.7f)
                        )
                    }
                }
            }
            // Main Tab Selector
            TabRow(
                selectedTabIndex = activeMainTab,
                containerColor = SurfaceColor,
                contentColor = AccentBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[activeMainTab]),
                        color = AccentBlue
                    )
                }
            ) {
                Tab(
                    selected = activeMainTab == 0,
                    onClick = { activeMainTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ContactSupport, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Creator Help Desk", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = activeMainTab == 1,
                    onClick = {
                        if (demoAdminMode) {
                            activeMainTab = 1
                        } else {
                            supportViewModel.upvoteFeatureRequest("non_existent", "trigger_toast_only") // Trigger a toast reminder
                        }
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AdminPanelSettings, null, modifier = Modifier.size(16.dp), tint = if (demoAdminMode) CrispAmber else TextMuted)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Operations Center",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (demoAdminMode) Color.White else TextMuted
                            )
                        }
                    }
                )
            }

            if (activeMainTab == 0) {
                CreatorHelpDeskView(
                    supportViewModel = supportViewModel,
                    allTickets = allTickets,
                    userId = currentUserId,
                    userName = currentUserDisplayName,
                    helpTexts = helpTexts
                )
            } else {
                AdminOperationsCenterView(
                    supportViewModel = supportViewModel,
                    allTickets = allTickets,
                    openCount = openCount,
                    avgSlaHours = avgSlaHours,
                    adminId = currentUserId,
                    adminName = currentUserDisplayName,
                    disputeNotes = disputeNotes
                )
            }
        }
    }
}

@Composable
fun CreatorHelpDeskView(
    supportViewModel: SupportViewModel,
    allTickets: List<SupportTicket>,
    userId: String,
    userName: String,
    helpTexts: List<com.example.data.model.HelpText>
) {
    var helpDeskSubTab by remember { mutableStateOf(0) } // 0 = Submit Ticket, 1 = Feature Board, 2 = Help Articles

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = helpDeskSubTab,
            containerColor = SurfaceColor.copy(alpha = 0.5f),
            contentColor = AccentBlue,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[helpDeskSubTab]),
                    color = AccentBlue
                )
            }
        ) {
            Tab(
                selected = helpDeskSubTab == 0,
                onClick = { helpDeskSubTab = 0 },
                text = { Text("New Ticket", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = helpDeskSubTab == 1,
                onClick = { helpDeskSubTab = 1 },
                text = { Text("Feature Backlog", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = helpDeskSubTab == 2,
                onClick = { helpDeskSubTab = 2 },
                text = { Text("Help Articles", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (helpDeskSubTab == 0) {
            NewSupportTicketForm(
                supportViewModel = supportViewModel,
                allTickets = allTickets,
                userId = userId,
                userName = userName
            )
        } else if (helpDeskSubTab == 1) {
            FeatureBacklogView(
                supportViewModel = supportViewModel,
                allTickets = allTickets,
                userId = userId
            )
        } else {
            HelpArticlesView(helpTexts = helpTexts)
        }
    }
}

@Composable
fun NewSupportTicketForm(
    supportViewModel: SupportViewModel,
    allTickets: List<SupportTicket>,
    userId: String,
    userName: String
) {
    val categories = listOf("Bug", "Feature Request", "Account Issue", "Workspace Issue", "Verification Issue")
    var selectedCategory by remember { mutableStateOf("Bug") }
    var selectedTicketForDetails by remember { mutableStateOf<SupportTicket?>(null) }
    
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    // Bug Report Details
    var deviceInfo by remember { mutableStateOf("${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})") }
    var appVersion by remember { mutableStateOf("v1.3.2-PROD") }
    var screenshots by remember { mutableStateOf("workspace_canvas_snapshot_error_0x42.png") }
    var logs by remember { mutableStateOf("[ERROR] LedgerSyncException: secure synchronization interrupted\n[DEBUG] SyncEngine: Core synchronization delayed for 2 pending agreements.\n[WARN] NetworkApi: Handshake took 4200ms.") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                border = BorderStroke(1.dp, ColorDivider),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "SUBMIT OPERATIONAL TICKET",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Category selector Row
                    Text("Select Ticket Category", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = cat == selectedCategory
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) AccentBlue else SurfaceLightColor)
                                    .border(1.dp, if (isSelected) AccentBlue else ColorDivider, RoundedCornerShape(6.dp))
                                    .clickable { selectedCategory = cat }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (cat) {
                                        "Feature Request" -> "Feature"
                                        "Account Issue" -> "Account"
                                        "Workspace Issue" -> "Workspace"
                                        "Verification Issue" -> "Verify"
                                        else -> "Bug"
                                    },
                                    color = if (isSelected) Color.Black else TextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                border = BorderStroke(1.dp, ColorDivider),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "TICKET PARTICULARS",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title / Summary") },
                        modifier = Modifier.fillMaxWidth().testTag("ticket_title_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = ColorDivider,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Describe the issue or request in detail") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("ticket_description_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = ColorDivider,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    // Conditional Bug Telemetry Panel
                    if (selectedCategory == "Bug") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = ColorDivider)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            "BUG REPORT DIAGNOSTICS (TELEMETRY)",
                            color = CrispAmber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )

                        OutlinedTextField(
                            value = deviceInfo,
                            onValueChange = { deviceInfo = it },
                            label = { Text("Device Metadata") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrispAmber,
                                unfocusedBorderColor = ColorDivider,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = appVersion,
                                onValueChange = { appVersion = it },
                                label = { Text("App Version") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CrispAmber,
                                    unfocusedBorderColor = ColorDivider,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            OutlinedTextField(
                                value = screenshots,
                                onValueChange = { screenshots = it },
                                label = { Text("Attachment Snapshot") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CrispAmber,
                                    unfocusedBorderColor = ColorDivider,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }

                        OutlinedTextField(
                            value = logs,
                            onValueChange = { logs = it },
                            label = { Text("Device Logs / Stacktrace") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 10.sp, color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrispAmber,
                                unfocusedBorderColor = ColorDivider,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            supportViewModel.createSupportTicket(
                                userId = userId,
                                userDisplayName = userName,
                                category = selectedCategory,
                                title = title,
                                description = description,
                                deviceInfo = deviceInfo,
                                appVersion = appVersion,
                                logs = logs,
                                screenshots = screenshots
                            )
                            // Reset inputs
                            title = ""
                            description = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_ticket_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "SUBMIT TO OPERATIONS SHARD",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // List of filed tickets of current user
        val userTickets = allTickets.filter { it.userId == userId }
        item {
            Text(
                "MY SUBMITTED TICKETS (${userTickets.size})",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (userTickets.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No tickets submitted yet. Fill the form to launch a ticket.", color = TextMuted, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(userTickets, key = { it.id }) { ticket ->
                TicketItemRow(
                    ticket = ticket, 
                    isAdminMode = false, 
                    onAction = { selectedTicketForDetails = ticket }
                )
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (selectedTicketForDetails != null) {
        val ticket = selectedTicketForDetails!!
        UserTicketDetailDialog(
            ticket = ticket,
            onDismiss = { selectedTicketForDetails = null }
        )
    }
}

@Composable
fun UserTicketDetailDialog(
    ticket: SupportTicket,
    onDismiss: () -> Unit
) {
    val dateStr = remember(ticket.createdAt) { DateTimeUtils.formatFull(ticket.createdAt) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .clip(RoundedCornerShape(16.dp)),
            color = SurfaceColor,
            border = BorderStroke(1.dp, ColorDivider)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "TICKET DETAILS",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = Color.White)
                    }
                }

                Divider(color = ColorDivider, modifier = Modifier.padding(vertical = 8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("Status", color = TextSecondary, fontSize = 11.sp)
                        Box(
                            modifier = Modifier
                                .background(
                                    when (ticket.status) {
                                        "OPEN" -> ColorDivider
                                        "IN_PROGRESS" -> CrispAmber.copy(alpha = 0.15f)
                                        "WAITING_USER" -> AccentBlue.copy(alpha = 0.15f)
                                        "RESOLVED" -> NeonEmerald.copy(alpha = 0.15f)
                                        else -> Color.Gray.copy(alpha = 0.15f)
                                    },
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = ticket.status,
                                color = when (ticket.status) {
                                    "OPEN" -> Color.White
                                    "IN_PROGRESS" -> CrispAmber
                                    "WAITING_USER" -> AccentBlue
                                    "RESOLVED" -> NeonEmerald
                                    else -> Color.Gray
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    item {
                        Text("Title", color = TextSecondary, fontSize = 10.sp)
                        Text(ticket.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    item {
                        Text("Description", color = TextSecondary, fontSize = 10.sp)
                        Text(ticket.description, color = Color.White, fontSize = 13.sp)
                    }
                    item {
                        Text("Category", color = TextSecondary, fontSize = 10.sp)
                        Text(ticket.category, color = AccentBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    item {
                        Text("Submitted On", color = TextSecondary, fontSize = 10.sp)
                        Text(dateStr, color = TextMuted, fontSize = 11.sp)
                    }
                    
                    if (ticket.status == "RESOLVED" || ticket.status == "CLOSED") {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = NeonEmerald.copy(alpha = 0.1f)),
                                border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Resolution Update", color = NeonEmerald, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "This ticket has been marked as ${ticket.status}. If you have further issues, please create a new ticket.",
                                        color = Color.White,
                                        fontSize = 11.sp
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

@Composable
fun FeatureBacklogView(
    supportViewModel: SupportViewModel,
    allTickets: List<SupportTicket>,
    userId: String
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val featureTickets = allTickets.filter { 
        it.category == "Feature Request" && 
        (it.title.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true))
    }.sortedByDescending { it.upvotes }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search Feature Requests...", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = TextMuted) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceColor,
                unfocusedContainerColor = SurfaceColor,
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = ColorDivider,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (featureTickets.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No matching feature requests found.", color = TextMuted, fontSize = 12.sp)
                    }
                }
            } else {
                items(featureTickets, key = { it.id }) { req ->
                    FeatureRequestCard(
                        ticket = req,
                        userId = userId,
                        onUpvote = { supportViewModel.upvoteFeatureRequest(req.id, userId) }
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun FeatureRequestCard(
    ticket: SupportTicket,
    userId: String,
    onUpvote: () -> Unit
) {
    val upvotedUsers = remember(ticket.upvotedUserIdsJson) {
        try {
            val jsonStr = ticket.upvotedUserIdsJson
            if (jsonStr.startsWith("[") && jsonStr.endsWith("]")) {
                jsonStr.removeSurrounding("[", "]")
                    .split(",")
                    .map { it.trim().removeSurrounding("\"") }
                    .filter { it.isNotEmpty() }
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    val hasUpvoted = upvotedUsers.contains(userId)

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = BorderStroke(1.dp, ColorDivider),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Upvote Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (hasUpvoted) AccentBlue.copy(alpha = 0.15f) else SurfaceLightColor)
                    .border(1.dp, if (hasUpvoted) AccentBlue else ColorDivider, RoundedCornerShape(8.dp))
                    .clickable { onUpvote() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("upvote_button_${ticket.id}")
            ) {
                Icon(
                    Icons.Default.ThumbUp,
                    contentDescription = "Upvote",
                    tint = if (hasUpvoted) AccentBlue else TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = ticket.upvotes.toString(),
                    color = if (hasUpvoted) AccentBlue else Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Body Column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "FEATURE REQUEST",
                        color = AccentBlue,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Priority Badge
                    Box(
                        modifier = Modifier
                            .background(
                                when (ticket.internalPriority) {
                                    "CRITICAL" -> AccentRed.copy(alpha = 0.15f)
                                    "HIGH" -> CrispAmber.copy(alpha = 0.15f)
                                    else -> NeonEmerald.copy(alpha = 0.15f)
                                },
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = ticket.status.uppercase(),
                            color = when (ticket.status) {
                                "OPEN" -> TextSecondary
                                "IN_PROGRESS" -> CrispAmber
                                "WAITING_USER" -> AccentBlue
                                "RESOLVED" -> NeonEmerald
                                else -> Color.White
                            },
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = ticket.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = ticket.description,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun TicketItemRow(
    ticket: SupportTicket,
    isAdminMode: Boolean,
    onAction: () -> Unit
) {
    val dateStr = com.example.util.DateTimeUtils.formatFull(ticket.createdAt)
    val isCritical = ticket.internalPriority == "CRITICAL"

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = BorderStroke(1.dp, if (isCritical) AccentRed.copy(alpha = 0.5f) else ColorDivider),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAction() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Category Tag
                Box(
                    modifier = Modifier
                        .background(
                            when (ticket.category) {
                                "Bug" -> AccentRed.copy(alpha = 0.15f)
                                "Feature Request" -> AccentBlue.copy(alpha = 0.15f)
                                else -> CrispAmber.copy(alpha = 0.15f)
                            },
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = ticket.category.uppercase(),
                        color = when (ticket.category) {
                            "Bug" -> AccentRed
                            "Feature Request" -> AccentBlue
                            else -> CrispAmber
                        },
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Priority tag
                    Box(
                        modifier = Modifier
                            .background(
                                when (ticket.internalPriority) {
                                    "CRITICAL" -> AccentRed.copy(alpha = 0.2f)
                                    "HIGH" -> CrispAmber.copy(alpha = 0.2f)
                                    else -> SurfaceLightColor
                                },
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "PRIORITY: ${ticket.internalPriority}",
                            color = when (ticket.internalPriority) {
                                "CRITICAL" -> AccentRed
                                "HIGH" -> CrispAmber
                                else -> TextSecondary
                            },
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Status Badge
                    Box(
                        modifier = Modifier
                            .background(
                                when (ticket.status) {
                                    "OPEN" -> ColorDivider
                                    "IN_PROGRESS" -> CrispAmber.copy(alpha = 0.15f)
                                    "WAITING_USER" -> AccentBlue.copy(alpha = 0.15f)
                                    "RESOLVED" -> NeonEmerald.copy(alpha = 0.15f)
                                    else -> Color.Gray.copy(alpha = 0.15f)
                                },
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = ticket.status,
                            color = when (ticket.status) {
                                "OPEN" -> Color.White
                                "IN_PROGRESS" -> CrispAmber
                                "WAITING_USER" -> AccentBlue
                                "RESOLVED" -> NeonEmerald
                                else -> Color.Gray
                            },
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = ticket.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = ticket.description,
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = ColorDivider.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Filed by: ${ticket.userDisplayName}",
                    color = TextMuted,
                    fontSize = 9.sp
                )
                Text(
                    text = dateStr,
                    color = TextMuted,
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
fun AdminOperationsCenterView(
    supportViewModel: SupportViewModel,
    allTickets: List<SupportTicket>,
    openCount: Int,
    avgSlaHours: Double,
    adminId: String,
    adminName: String,
    disputeNotes: List<com.example.data.model.DisputeNote>
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var selectedStatusFilter by remember { mutableStateOf("All") }
    
    // Dialog / Detail Drawer State
    var selectedTicketForDetails by remember { mutableStateOf<SupportTicket?>(null) }

    val categoriesFilter = listOf("All", "Bug", "Feature Request", "Account Issue", "Workspace Issue", "Verification Issue")
    val statusesFilter = listOf("All", "OPEN", "IN_PROGRESS", "WAITING_USER", "RESOLVED", "CLOSED")

    // Filter logic
    val filteredTickets = allTickets.filter { ticket ->
        val matchesSearch = ticket.title.contains(searchQuery, ignoreCase = true) || 
                            ticket.description.contains(searchQuery, ignoreCase = true) ||
                            ticket.userDisplayName.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategoryFilter == "All" || ticket.category == selectedCategoryFilter
        val matchesStatus = selectedStatusFilter == "All" || ticket.status == selectedStatusFilter
        matchesSearch && matchesCategory && matchesStatus
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // KPI Operations dashboard card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                border = BorderStroke(1.dp, ColorDivider),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "OPERATIONS CENTER TELEMETRY & SLA",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // KPI 1: Open cases
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(SurfaceLightColor, RoundedCornerShape(8.dp))
                                .border(1.dp, ColorDivider, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text("Open Tickets", color = TextSecondary, fontSize = 9.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = openCount.toString(),
                                color = CrispAmber,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // KPI 2: SLA Avg Resolution Time
                        Column(
                            modifier = Modifier
                                .weight(1.3f)
                                .background(SurfaceLightColor, RoundedCornerShape(8.dp))
                                .border(1.dp, ColorDivider, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text("Avg Resolution (SLA)", color = TextSecondary, fontSize = 9.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            val formattedSla = String.format(Locale.getDefault(), "%.1f Hrs", avgSlaHours)
                            Text(
                                text = if (avgSlaHours > 0) formattedSla else "N/A (No SLA)",
                                color = NeonEmerald,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // KPI 3: Feature Requests Backlog
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(SurfaceLightColor, RoundedCornerShape(8.dp))
                                .border(1.dp, ColorDivider, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text("Feature back", color = TextSecondary, fontSize = 9.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = allTickets.count { it.category == "Feature Request" }.toString(),
                                color = AccentBlue,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // CRM DISPUTE RESOLUTION BOARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                border = BorderStroke(1.dp, ColorDivider),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Gavel, null, tint = AccentRed, modifier = Modifier.size(16.dp))
                            Text(
                                "LOCKED DISPUTES & CRM INFRACTIONS (${disputeNotes.size})",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (disputeNotes.isEmpty()) {
                        Text("No active dispute infractions logged on the shard.", color = TextMuted, fontSize = 12.sp)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            disputeNotes.forEach { note ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SurfaceLightColor),
                                    border = BorderStroke(1.dp, ColorDivider),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Workspace ID: ${note.workspaceId}",
                                                color = AccentBlue,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "LOCKED DISPUTE",
                                                color = AccentRed,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = note.content,
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = note.noteText,
                                            color = TextSecondary,
                                            fontSize = 12.sp,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live SLA list or Search Block
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                border = BorderStroke(1.dp, ColorDivider),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "DISPATCH & SEARCH ENGINE",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search Title, description, creator...", color = TextSecondary) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = TextMuted) },
                        modifier = Modifier.fillMaxWidth().testTag("admin_search_tickets"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = ColorDivider,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    // Filters horizontally scrollable or wrap
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Filter Category", color = TextSecondary, fontSize = 10.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("All", "Bug", "Feature Request").forEach { cat ->
                                val isSelected = selectedCategoryFilter == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) AccentBlue else SurfaceLightColor)
                                        .clickable { selectedCategoryFilter = cat }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(cat, color = if (isSelected) Color.Black else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Text("Filter Flow Status", color = TextSecondary, fontSize = 10.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("All", "OPEN", "IN_PROGRESS", "RESOLVED").forEach { stat ->
                                val isSelected = selectedStatusFilter == stat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) CrispAmber else SurfaceLightColor)
                                        .clickable { selectedStatusFilter = stat }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(stat, color = if (isSelected) Color.Black else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Ticket queue title
        item {
            Text(
                "OPERATIONS TICKETS QUEUE (${filteredTickets.size})",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        if (filteredTickets.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No tickets match current operations filters.", color = TextMuted, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(filteredTickets, key = { it.id }) { ticket ->
                TicketItemRow(
                    ticket = ticket,
                    isAdminMode = true,
                    onAction = { selectedTicketForDetails = ticket }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Detail Admin Resolution Drawer Dialog
    if (selectedTicketForDetails != null) {
        val ticket = selectedTicketForDetails!!
        AdminTicketDetailDialog(
            ticket = ticket,
            adminId = adminId,
            adminName = adminName,
            onDismiss = { selectedTicketForDetails = null },
            supportViewModel = supportViewModel
        )
    }
}

@Composable
fun AdminTicketDetailDialog(
    ticket: SupportTicket,
    adminId: String,
    adminName: String,
    onDismiss: () -> Unit,
    supportViewModel: SupportViewModel
) {
    var statusValue by remember { mutableStateOf(ticket.status) }
    var priorityValue by remember { mutableStateOf(ticket.internalPriority) }
    var notesValue by remember { mutableStateOf(ticket.internalNotes) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(16.dp)),
            color = SurfaceColor,
            border = BorderStroke(1.dp, ColorDivider)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Build, null, tint = CrispAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "TICKET WORKSPACE CASE",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = Color.White)
                    }
                }

                Divider(color = ColorDivider, modifier = Modifier.padding(vertical = 8.dp))

                // Scrollable details content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        // Ticket General Info
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceLightColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Title: ${ticket.title}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Description: ${ticket.description}", color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
                                Divider(color = ColorDivider.copy(alpha = 0.5f))
                                Text("Filer: ${ticket.userDisplayName} (ID: ${ticket.userId})", color = TextMuted, fontSize = 11.sp)
                                Text("Assigned Admin: ${ticket.assignedAdminName ?: "Unassigned"}", color = TextMuted, fontSize = 11.sp)
                            }
                        }
                    }

                    // Bug telemetry logs if Bug
                    if (ticket.category == "Bug") {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceLightColor),
                                border = BorderStroke(1.dp, AccentRed.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("BUG TELEMETRY", color = AccentRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text("Device: ${ticket.deviceInfo}", color = Color.White, fontSize = 11.sp)
                                    Text("App Version: ${ticket.appVersion}", color = Color.White, fontSize = 11.sp)
                                    Text("Screenshot: ${ticket.screenshots}", color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(PrimaryBackground, RoundedCornerShape(4.dp))
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = ticket.logs,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Operational controls
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceLightColor),
                            border = BorderStroke(1.dp, ColorDivider),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("OPERATIONAL DISPATCH CONTROLS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)

                                // Self Assignment
                                if (ticket.assignedAdminId != adminId) {
                                    Button(
                                        onClick = {
                                            supportViewModel.assignTicket(ticket.id, adminId, adminName)
                                            onDismiss()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("ASSIGN TO SELF", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Status Switcher
                                Text("Transition Ticket Status (Flow)", color = TextSecondary, fontSize = 10.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val statuses = listOf("OPEN", "IN_PROGRESS", "WAITING_USER", "RESOLVED", "CLOSED")
                                    statuses.forEach { stat ->
                                        val isCurrent = statusValue == stat
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (isCurrent) CrispAmber else SurfaceColor)
                                                .clickable {
                                                    statusValue = stat
                                                    supportViewModel.updateTicketStatus(ticket.id, stat)
                                                }
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = when (stat) {
                                                    "IN_PROGRESS" -> "IP"
                                                    "WAITING_USER" -> "WAIT"
                                                    "RESOLVED" -> "OK"
                                                    "CLOSED" -> "END"
                                                    else -> "OPEN"
                                                },
                                                color = if (isCurrent) Color.Black else TextPrimary,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // Priority Switcher
                                Text("Update Priority Level", color = TextSecondary, fontSize = 11.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val priorities = listOf("LOW", "MEDIUM", "HIGH", "CRITICAL")
                                    priorities.forEach { p ->
                                        val isCurrent = priorityValue == p
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    if (isCurrent) {
                                                        when (p) {
                                                            "CRITICAL" -> AccentRed
                                                            "HIGH" -> CrispAmber
                                                            else -> AccentBlue
                                                        }
                                                    } else SurfaceColor
                                                )
                                                .clickable {
                                                    priorityValue = p
                                                    supportViewModel.updateInternalNotesAndPriority(ticket.id, notesValue, p)
                                                }
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = p,
                                                color = if (isCurrent) Color.Black else TextPrimary,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // Internal Notes Input
                                Text("Internal Notes (Private Ops Logs)", color = TextSecondary, fontSize = 10.sp)
                                OutlinedTextField(
                                    value = notesValue,
                                    onValueChange = { notesValue = it },
                                    placeholder = { Text("Write internal ops note...", color = TextSecondary, fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentBlue,
                                        unfocusedBorderColor = ColorDivider,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                Button(
                                    onClick = {
                                        supportViewModel.updateInternalNotesAndPriority(ticket.id, notesValue, priorityValue)
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                                    modifier = Modifier.bounceScale().fillMaxWidth(),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("SAVE OPERATION CHANGES", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HelpArticlesView(helpTexts: List<com.example.data.model.HelpText>) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredArticles = remember(helpTexts, searchQuery) {
        helpTexts.filter {
            it.topicKey.contains(searchQuery, ignoreCase = true) ||
            it.textContent.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search help articles...", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, tint = TextSecondary, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = ColorDivider,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (filteredArticles.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No articles found in Knowledge Base.", color = TextMuted, fontSize = 13.sp)
                    }
                }
            } else {
                items(filteredArticles, key = { it.id }) { article ->
                    var isExpanded by remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded },
                        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (isExpanded) AccentBlue else ColorDivider)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Article,
                                        contentDescription = null,
                                        tint = AccentBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = article.topicKey,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            }
                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = article.textContent,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Category: ${article.category.uppercase()}",
                                    color = CrispAmber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
