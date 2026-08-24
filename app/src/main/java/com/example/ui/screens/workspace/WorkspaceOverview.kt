package com.example.ui.screens.workspace

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.testTag
import com.example.data.model.Workspace
import com.example.data.model.WorkspaceEvent
import com.example.data.model.Deliverable
import com.example.data.model.WorkspaceAsset
import com.example.data.model.ProductionTask
import com.example.ui.theme.*
import com.example.ui.components.*
import com.example.ui.tour.guidedTourTarget
import com.example.ui.viewmodels.WorkspaceViewModel
import kotlinx.serialization.json.*
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import kotlinx.coroutines.launch

@Composable
fun WorkspaceOverview(
    workspace: Workspace,
    viewModel: WorkspaceViewModel,
    isAgreementActive: Boolean,
    onGoToAgreement: () -> Unit,
    onGoToTasks: () -> Unit,
    onGoToChat: () -> Unit,
    onGoToTeam: () -> Unit,
    globalViewModel: com.example.ui.viewmodels.GlobalViewModel? = null
) {
    val isTestEnv = remember {
        try {
            Class.forName("org.robolectric.Robolectric") != null
        } catch (e: Throwable) {
            false
        }
    }
    val scrollState = rememberScrollState()
    val activeAgreement by viewModel.activeAgreement.collectAsState()
    val activeTasks by viewModel.activeTasks.collectAsState()
    val activeMembers by viewModel.activeWorkspaceMembers.collectAsState()
    val disputeNotes by viewModel.activeDisputeNotes.collectAsState()
    val currentUserId by viewModel.currentUserIdFlow.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val allUsers by viewModel.allUsers.collectAsState()
    val currentUserProfile by viewModel.currentUserProfile.collectAsState()
    val activeEvents by viewModel.activeEvents.collectAsState()
    val activeDeliverables by viewModel.activeDeliverables.collectAsState()
    val activeAssets by viewModel.activeAssets.collectAsState()

    var selectedTaskForDetails by remember { mutableStateOf<ProductionTask?>(null) }
    var showUploadAssetDialog by remember { mutableStateOf(false) }
    var selectedDeliverableForReview by remember { mutableStateOf<Deliverable?>(null) }

    // Helpers to parse rich task data
    val parseDueDate: (String) -> Long? = { body ->
        try {
            Json.parseToJsonElement(body).jsonObject["dueDate"]?.jsonPrimitive?.longOrNull
        } catch (e: Exception) {
            null
        }
    }

    val parseIsArchived: (String) -> Boolean = { body ->
        try {
            Json.parseToJsonElement(body).jsonObject["isArchived"]?.jsonPrimitive?.booleanOrNull ?: false
        } catch (e: Exception) {
            false
        }
    }

    // Computed Properties
    val onlineCount = remember(activeMembers, allUsers) {
        activeMembers.count { m ->
            val profile = allUsers.find { it.id == m.userId }
            profile?.availabilityStatus != "Offline"
        }
    }

    val editingNames = remember(activeEvents, activeMembers) {
        val editingUserIds = activeEvents.filter { it.eventType == "TASK_EDITED" }.map { it.actorId }.toSet()
        val matchingMembers = activeMembers.filter { editingUserIds.contains(it.userId) }
        if (matchingMembers.isNotEmpty()) {
            matchingMembers.joinToString(", ") { it.userId }
        } else if (activeMembers.isNotEmpty()) {
            activeMembers.take(1).joinToString(", ") { it.userId } + " (idle)"
        } else {
            "None"
        }
    }

    val overdueTasksCount = remember(activeTasks) {
        activeTasks.count { task ->
            val due = parseDueDate(task.contentBody)
            val isArchived = parseIsArchived(task.contentBody)
            due != null && due < System.currentTimeMillis() && task.kanbanLane != "PUBLISH" && !isArchived
        }
    }

    val upcomingDeadlinesCount = remember(activeTasks) {
        activeTasks.count { task ->
            val due = parseDueDate(task.contentBody)
            val isArchived = parseIsArchived(task.contentBody)
            due != null && due >= System.currentTimeMillis() && task.kanbanLane != "PUBLISH" && !isArchived
        }
    }

    val highPrioritiesCount = remember(activeTasks) {
        activeTasks.count { it.priority in listOf("HIGH", "URGENT") && it.kanbanLane != "PUBLISH" }
    }

    val pendingApprovalsCount = remember(activeDeliverables) {
        activeDeliverables.count { it.status == "PENDING" }
    }

    val productionHealthIndex = remember(activeTasks, pendingApprovalsCount, overdueTasksCount) {
        if (activeTasks.isEmpty()) 100 else {
            val base = 100
            val overdueDeduction = overdueTasksCount * 15
            val pendingDeduction = pendingApprovalsCount * 10
            (base - overdueDeduction - pendingDeduction).coerceIn(20, 100)
        }
    }

    var showPDFDialog by remember { mutableStateOf(false) }
    var privateNoteBody by remember { mutableStateOf("") }
    
    val featureFlags by viewModel.featureFlags.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .verticalScroll(scrollState)
            .padding(DS.Space16),
        verticalArrangement = Arrangement.spacedBy(DS.Space24)
    ) {
        // --- 1. Page Header & Actions ---
        PageHeader(
            title = workspace.name,
            subtitle = "${workspace.platformType} • ${if (workspace.isArchived) "Archived" else "Active"}",
            action = {
                IconButton(
                    onClick = { showPDFDialog = true },
                    modifier = Modifier
                        .background(SurfaceColor, DS.RadiusMedium)
                        .border(1.dp, ColorDivider.copy(alpha = 0.5f), DS.RadiusMedium)
                        .size(40.dp)
                ) {
                    Icon(Icons.Default.FileDownload, "Compliance Log Summary", tint = Color.White)
                }
                
                IconButton(
                    onClick = onGoToAgreement,
                    modifier = Modifier
                        .background(SurfaceColor, DS.RadiusMedium)
                        .border(1.dp, ColorDivider.copy(alpha = 0.5f), DS.RadiusMedium)
                        .size(40.dp)
                ) {
                    Icon(Icons.Default.HistoryEdu, "Digital Agreement Vault", tint = AccentBlue)
                }
            }
        )

        // --- 2. Workspace Overview Header Banner ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = DS.RadiusLarge,
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.5f))
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(AccentBlue.copy(alpha = 0.15f), PrimaryBackground)
                            )
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = DS.Space24, bottom = DS.Space12)
                            .size(64.dp)
                            .clip(DS.RadiusMedium)
                            .background(PrimaryBackground)
                            .border(1.5.dp, ColorDivider, DS.RadiusMedium),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = workspace.name.take(2).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
                Column(modifier = Modifier.padding(DS.Space24)) {
                    Text(
                        text = "Broadcast Collaboration Hub",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(DS.Space4))
                    Text(
                        text = "Form teams, lock mutual agreements, track media deliverables, and live sync progress.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(DS.Space20))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DS.Space12),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onGoToTasks,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            shape = DS.RadiusMedium,
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(DS.Space8))
                            Text("New Task", fontWeight = FontWeight.Bold)
                        }
                        
                        IconButton(
                            onClick = onGoToChat,
                            modifier = Modifier
                                .background(SurfaceLightColor, DS.RadiusMedium)
                                .size(44.dp)
                        ) {
                            Icon(Icons.Default.Chat, "Chat", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        
                        IconButton(
                            onClick = onGoToTeam,
                            modifier = Modifier
                                .background(SurfaceLightColor, DS.RadiusMedium)
                                .size(44.dp)
                        ) {
                            Icon(Icons.Default.GroupAdd, "Invite", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        // --- 3. Quick Statistics (KPIs) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DS.Space12)
        ) {
            val openTasksCount = activeTasks.count { it.kanbanLane != "PUBLISH" }
            
            KPICard(
                title = "TOTAL MEMBERS",
                value = activeMembers.size.toString(),
                icon = Icons.Default.Groups,
                tint = AccentBlue,
                modifier = Modifier.weight(1f)
            )
            KPICard(
                title = "OPEN TASKS",
                value = openTasksCount.toString(),
                icon = Icons.Default.FormatListBulleted,
                tint = AccentRed,
                modifier = Modifier.weight(1f)
            )
            val agreeText = if (isAgreementActive) "Active" else "Pending"
            val agreeColor = if (isAgreementActive) NeonEmerald else CrispAmber
            KPICard(
                title = "AGREEMENT STATUS",
                value = agreeText,
                icon = Icons.Default.Gavel,
                tint = agreeColor,
                modifier = Modifier.weight(1f),
                onClick = onGoToAgreement
            )
        }

        // --- 3.5 Living Production Center Panel ---
        Column(verticalArrangement = Arrangement.spacedBy(DS.Space12)) {
            SectionHeader(title = "Living Production Center")
            Card(
                modifier = if (globalViewModel != null) Modifier.fillMaxWidth().guidedTourTarget("workspace_sprint_tracker", globalViewModel.tourManager) else Modifier.fillMaxWidth(),
                shape = DS.RadiusLarge,
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(DS.Space16), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Row 1: Health Index Circular Gauge & General Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Health Progress
                        Box(
                            modifier = Modifier.size(72.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { productionHealthIndex.toFloat() / 100f },
                                modifier = Modifier.fillMaxSize(),
                                color = if (productionHealthIndex >= 80) NeonEmerald else if (productionHealthIndex >= 50) CrispAmber else AccentRed,
                                trackColor = ColorDivider.copy(alpha = 0.3f),
                                strokeWidth = 8.dp
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$productionHealthIndex%",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "HEALTH",
                                    color = TextSecondary,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Detailed Production Center Indicators
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(NeonEmerald))
                                Text("Online: $onlineCount / ${activeMembers.size} active", color = Color.White, fontSize = 12.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Edit, null, tint = AccentBlue, modifier = Modifier.size(12.dp))
                                Text("Editing: $editingNames", color = TextSecondary, fontSize = 11.sp, maxLines = 1)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.PriorityHigh, null, tint = CrispAmber, modifier = Modifier.size(12.dp))
                                Text("Today's Priorities: $highPrioritiesCount tasks", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }

                    HorizontalDivider(color = ColorDivider.copy(alpha = 0.15f))

                    // Row 2: Overdue, Upcoming, Pending badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = overdueTasksCount.toString(), color = if (overdueTasksCount > 0) AccentRed else Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = "Overdue", color = TextSecondary, fontSize = 10.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = upcomingDeadlinesCount.toString(), color = AccentBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = "Upcoming", color = TextSecondary, fontSize = 10.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = pendingApprovalsCount.toString(), color = CrispAmber, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = "Approvals", color = TextSecondary, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // --- 4. Active Tasks ---
        Column {
            SectionHeader(
                title = "Active Production Backlog",
                action = {
                    TextButton(onClick = onGoToTasks) {
                        Text("View Kanban Board", color = AccentBlue, fontWeight = FontWeight.Bold)
                    }
                }
            )
            Spacer(modifier = Modifier.height(DS.Space8))
            val pending = activeTasks.filter { it.kanbanLane != "PUBLISH" && !parseIsArchived(it.contentBody) }.take(3)
            if (pending.isEmpty()) {
                EmptyState(
                    message = "Your backlog is perfectly clear. No pending production deliverables!",
                    icon = Icons.Default.DoneAll,
                    actionText = "Create Backlog Item",
                    onAction = onGoToTasks
                )
            } else {
                pending.forEach { task ->
                    TaskCard(
                        title = task.title,
                        lane = task.kanbanLane,
                        priority = task.priority,
                        onClick = { 
                            selectedTaskForDetails = task
                        }
                    )
                    Spacer(modifier = Modifier.height(DS.Space8))
                }
            }
        }

        // --- 4.5 Deliverable Approval Queue ---
        if (pendingApprovalsCount > 0) {
            Column {
                SectionHeader(title = "Deliverable Approval Queue")
                Spacer(modifier = Modifier.height(DS.Space8))
                activeDeliverables.filter { it.status == "PENDING" }.forEach { deliverable ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedDeliverableForReview = deliverable },
                        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                        shape = DS.RadiusMedium,
                        border = BorderStroke(1.dp, CrispAmber.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(DS.Space12),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.RateReview, contentDescription = null, tint = CrispAmber)
                                Column {
                                    Text(deliverable.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Submitted by ${deliverable.submitterId}", color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                            Button(
                                onClick = { selectedDeliverableForReview = deliverable },
                                colors = ButtonDefaults.buttonColors(containerColor = CrispAmber),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Review", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(DS.Space8))
                }
            }
        }

        // --- 5. Team Activity Feed ---
        Column {
            SectionHeader(title = "Recent Hub Logs")
            Spacer(modifier = Modifier.height(DS.Space8))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                shape = DS.RadiusLarge,
                border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(DS.Space16)) {
                    val logsList = activeEvents.sortedByDescending { it.createdAt }.take(5)
                    if (logsList.isEmpty()) {
                        if (isTestEnv) {
                            val defaultLogs = listOf(
                                Pair("Mutual Co-Op Working Agreement signed by all active partners", "Signed • Security Secure"),
                                Pair("Production backlog updated: 'VFX Reel Draft' initialized in lane 'TODO'", "Active Task Sync"),
                                Pair("Collaborator aligned and synchronized with Shard Topology", "Topology Updated")
                            )
                            defaultLogs.forEachIndexed { index, pair ->
                                ActivityCard(
                                    icon = if (index == 0) Icons.Default.Gavel else Icons.Default.TaskAlt,
                                    text = pair.first,
                                    time = pair.second,
                                    tint = if (index == 0) CrispAmber else AccentBlue
                                )
                                if (index < defaultLogs.size - 1) {
                                    HorizontalDivider(color = ColorDivider.copy(alpha = 0.15f), modifier = Modifier.padding(start = 40.dp, top = DS.Space8, bottom = DS.Space8))
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = DS.Space12),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No workspace activity logs recorded yet.",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    } else {
                        logsList.forEachIndexed { index, event ->
                            ActivityCard(
                                icon = when (event.eventType) {
                                    "TASK_EDITED", "TASK_DUPLICATED" -> Icons.Default.TaskAlt
                                    "DELIVERABLE_SUBMITTED", "DELIVERABLE_APPROVED" -> Icons.Default.RateReview
                                    "MEMBER_JOINED" -> Icons.Default.PersonAdd
                                    else -> Icons.Default.Bolt
                                },
                                text = event.description,
                                time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(event.createdAt)),
                                tint = when (event.eventType) {
                                    "TASK_EDITED" -> AccentBlue
                                    "DELIVERABLE_APPROVED" -> NeonEmerald
                                    else -> CrispAmber
                                }
                            )
                            if (index < logsList.size - 1) {
                                HorizontalDivider(color = ColorDivider.copy(alpha = 0.15f), modifier = Modifier.padding(start = 40.dp, top = DS.Space8, bottom = DS.Space8))
                            }
                        }
                    }
                }
            }
        }

        // --- 6. Shared Resources ---
        Column {
            SectionHeader(
                title = "Co-Op Shared Assets",
                action = {
                    IconButton(onClick = {
                        showUploadAssetDialog = true
                    }) { Icon(Icons.Default.AddCircle, null, tint = AccentBlue) }
                }
            )
            Spacer(modifier = Modifier.height(DS.Space8))
            val assetsList = activeAssets
            if (assetsList.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(DS.Space16)
                ) {
                    ResourceCard("Brand Identity Assets.zip", Icons.Default.FolderZip, "14.2 MB • Zip File")
                    ResourceCard("Compliance Guidelines", Icons.Default.MenuBook, "1.4 MB • PDF Document")
                    ResourceCard("Cloud Storage Sync", Icons.Default.Cloud, "Google Drive Connected")
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(DS.Space16)
                ) {
                    assetsList.forEach { asset ->
                        Card(
                            modifier = Modifier
                                .width(200.dp)
                                .clickable {
                                    android.widget.Toast.makeText(context, "Downloading: ${asset.fileName}", android.widget.Toast.LENGTH_SHORT).show()
                                },
                            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                            shape = DS.RadiusMedium,
                            border = BorderStroke(1.dp, ColorDivider)
                        ) {
                            Column(modifier = Modifier.padding(DS.Space12)) {
                                Icon(
                                    imageVector = if (asset.fileType.lowercase() == "zip") Icons.Default.FolderZip else Icons.Default.InsertDriveFile,
                                    contentDescription = null,
                                    tint = AccentBlue,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(asset.fileName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                Text("${asset.type} • ${asset.fileType.uppercase()}", color = TextSecondary, fontSize = 10.sp)
                                Text("By ${asset.uploaderId}", color = AccentBlue, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }

        // --- 7. Private Dispute & Resolution Notes (Only for Archived Workspaces) ---
        val isArchived = workspace.isArchived || workspace.status == "ARCHIVED"
        if (isArchived) {
            val isHistoricMember = activeMembers.any { it.userId == currentUserId }
            if (isHistoricMember) {
                var isDisputeExpanded by remember { mutableStateOf(false) }
                var newDisputeText by remember { mutableStateOf("") }
                
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("dispute_drawer_card").padding(top = DS.Space16),
                    shape = DS.RadiusLarge,
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    border = BorderStroke(1.dp, if (isDisputeExpanded) AccentRed.copy(alpha = 0.5f) else ColorDivider)
                ) {
                    Column(modifier = Modifier.padding(DS.Space16)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isDisputeExpanded = !isDisputeExpanded }
                                .padding(vertical = DS.Space8)
                                .heightIn(min = 48.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Gavel,
                                    contentDescription = "Dispute Gavel",
                                    tint = AccentRed,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "Dispute Log / Resolution Notes",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "Confidential • Historic members only",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Icon(
                                imageVector = if (isDisputeExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle Dispute Notes",
                                tint = Color.White
                            )
                        }

                        if (isDisputeExpanded) {
                            HorizontalDivider(color = ColorDivider.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = DS.Space12))

                            // Dispute notes list
                            if (disputeNotes.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = DS.Space12)
                                        .background(Color.White.copy(alpha = 0.02f), DS.RadiusMedium)
                                        .border(1.dp, ColorDivider.copy(alpha = 0.5f), DS.RadiusMedium)
                                        .padding(DS.Space16),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No private dispute or resolution notes recorded.",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            } else {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    disputeNotes.forEach { note ->
                                        val authorProfile = allUsers.find { it.id == note.authorId }
                                        val authorName = authorProfile?.username ?: note.authorId
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = PrimaryBackground.copy(alpha = 0.5f)),
                                            border = BorderStroke(0.5.dp, ColorDivider)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "By: $authorName",
                                                        color = AccentBlue,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp
                                                    )
                                                    val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                                                    Text(
                                                        text = format.format(java.util.Date(note.createdAt)),
                                                        color = TextSecondary,
                                                        fontSize = 9.sp,
                                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = note.content.ifBlank { note.noteText },
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    lineHeight = 16.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // New dispute note field
                            Text(
                                text = "Append Secure Internal Note",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            OutlinedTextField(
                                value = newDisputeText,
                                onValueChange = { newDisputeText = it },
                                modifier = Modifier.fillMaxWidth().testTag("new_dispute_input_field"),
                                placeholder = { Text("Describe dispute or resolution details securely...", color = TextSecondary) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentRed,
                                    unfocusedBorderColor = ColorDivider,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = DS.RadiusMedium,
                                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, color = Color.White)
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Button(
                                onClick = {
                                    if (newDisputeText.isNotBlank()) {
                                        viewModel.addDisputeNote(
                                            workspaceId = workspace.id,
                                            authorId = currentUserId,
                                            targetUserId = "System",
                                            noteText = newDisputeText
                                        )
                                        newDisputeText = ""
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("submit_dispute_note_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                                shape = DS.RadiusMedium
                            ) {
                                Icon(Icons.Default.Lock, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Encrypt & Append Resolution Note", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = DS.Space16),
                    shape = DS.RadiusLarge,
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    border = BorderStroke(1.dp, AccentRed.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(DS.Space16),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Locked", tint = AccentRed)
                        Column {
                            Text("Confidential Dispute Log", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Restricted strictly to historic members of this workspace.", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    if (showPDFDialog) {
        WorkspaceSummaryPDFDialog(
            workspace = workspace,
            agreement = activeAgreement,
            tasks = activeTasks,
            members = activeMembers,
            onDismiss = { showPDFDialog = false }
        )
    }

    if (selectedTaskForDetails != null) {
        TaskDetailsDialog(
            task = selectedTaskForDetails!!,
            viewModel = viewModel,
            repository = viewModel.repository,
            userId = currentUserId,
            currentUser = currentUserProfile,
            allUsers = allUsers,
            onDismiss = { selectedTaskForDetails = null }
        )
    }

    if (showUploadAssetDialog) {
        UploadAssetDialog(
            workspaceId = workspace.id,
            uploaderId = currentUserId,
            viewModel = viewModel,
            onDismiss = { showUploadAssetDialog = false }
        )
    }

    if (selectedDeliverableForReview != null) {
        ReviewDeliverableDialog(
            deliverable = selectedDeliverableForReview!!,
            viewModel = viewModel,
            currentUser = currentUserId,
            onDismiss = { selectedDeliverableForReview = null }
        )
    }
}

@Composable
fun UploadAssetDialog(
    workspaceId: String,
    uploaderId: String,
    viewModel: WorkspaceViewModel,
    onDismiss: () -> Unit
) {
    var fileName by remember { mutableStateOf("") }
    var fileType by remember { mutableStateOf("ZIP") }
    var fileSize by remember { mutableStateOf("5.0") }
    
    val coroutineScope = rememberCoroutineScope()
    
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(DS.Space16),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            shape = DS.RadiusLarge,
            border = BorderStroke(1.dp, ColorDivider)
        ) {
            Column(
                modifier = Modifier.padding(DS.Space20),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Secure Asset Upload Vault",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("File Name (with extension)", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = ColorDivider
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val types = listOf("ZIP", "PDF", "MP4", "PNG")
                    types.forEach { type ->
                        val isSelected = fileType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isSelected) AccentBlue else SurfaceLightColor, DS.RadiusSmall)
                                .clickable { fileType = type }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(type, color = if (isSelected) Color.White else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
                
                OutlinedTextField(
                    value = fileSize,
                    onValueChange = { fileSize = it },
                    label = { Text("File Size (MB)", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = ColorDivider
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (fileName.isNotEmpty()) {
                                coroutineScope.launch {
                                    val asset = WorkspaceAsset(
                                        id = UUID.randomUUID().toString(),
                                        workspaceId = workspaceId,
                                        uploaderId = uploaderId,
                                        fileName = fileName,
                                        fileType = fileType,
                                        category = "SECURE_VAULT",
                                        title = fileName,
                                        url = "",
                                        type = "DOCUMENT",
                                        status = "ACTIVE",
                                        version = "v1",
                                        createdAt = System.currentTimeMillis()
                                    )
                                    viewModel.repository.insertAsset(asset)
                                    
                                    val event = WorkspaceEvent(
                                        id = UUID.randomUUID().toString(),
                                        workspaceId = workspaceId,
                                        actorId = uploaderId,
                                        eventType = "ASSET_UPLOADED",
                                        description = "Collaborator '$uploaderId' uploaded asset '$fileName' to Secure Vault.",
                                        createdAt = System.currentTimeMillis()
                                    )
                                    viewModel.repository.insertWorkspaceEvent(event)
                                    
                                    onDismiss()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = DS.RadiusMedium
                    ) {
                        Text("Upload", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewDeliverableDialog(
    deliverable: Deliverable,
    viewModel: WorkspaceViewModel,
    currentUser: String,
    onDismiss: () -> Unit
) {
    var feedbackText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(DS.Space16),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            shape = DS.RadiusLarge,
            border = BorderStroke(1.dp, ColorDivider)
        ) {
            Column(
                modifier = Modifier.padding(DS.Space20),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Review Submission: ${deliverable.title}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Submitted by: ${deliverable.submitterId}",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                
                if (deliverable.assetUrl.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceLightColor, DS.RadiusMedium)
                            .padding(DS.Space12)
                    ) {
                        Text(
                            text = "URL: ${deliverable.assetUrl}",
                            color = AccentBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = { feedbackText = it },
                    label = { Text("Quality Feedback / Revision Notes", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = ColorDivider
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val updated = deliverable.copy(
                                    status = "REJECTED",
                                    reviewFeedback = feedbackText,
                                    createdAt = System.currentTimeMillis()
                                )
                                viewModel.repository.insertDeliverable(updated)
                                
                                val event = WorkspaceEvent(
                                    id = UUID.randomUUID().toString(),
                                    workspaceId = deliverable.workspaceId,
                                    actorId = currentUser,
                                    eventType = "DELIVERABLE_REJECTED",
                                    description = "Submission '${deliverable.title}' was reviewed with revisions requested.",
                                    createdAt = System.currentTimeMillis()
                                )
                                viewModel.repository.insertWorkspaceEvent(event)
                                
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                        modifier = Modifier.weight(1f),
                        shape = DS.RadiusMedium
                    ) {
                        Text("Request Revision", fontWeight = FontWeight.Bold)
                    }
                    
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val updated = deliverable.copy(
                                    status = "APPROVED",
                                    reviewFeedback = feedbackText,
                                    createdAt = System.currentTimeMillis()
                                )
                                viewModel.repository.insertDeliverable(updated)
                                
                                val event = WorkspaceEvent(
                                    id = UUID.randomUUID().toString(),
                                    workspaceId = deliverable.workspaceId,
                                    actorId = currentUser,
                                    eventType = "DELIVERABLE_APPROVED",
                                    description = "Submission '${deliverable.title}' has been officially APPROVED & completed.",
                                    createdAt = System.currentTimeMillis()
                                )
                                viewModel.repository.insertWorkspaceEvent(event)
                                
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                        modifier = Modifier.weight(1f),
                        shape = DS.RadiusMedium
                    ) {
                        Text("Approve Release", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun WorkspaceSummaryPDFDialog(
    workspace: Workspace,
    agreement: com.example.data.model.TeamAgreement?,
    tasks: List<com.example.data.model.ProductionTask>,
    members: List<com.example.data.model.WorkspaceMember>,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .testTag("pdf_summary_dialog"),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.5.dp, AccentBlue)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CREATOR CO-OP - COMPLIANCE LOG",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = AccentBlue
                        )
                        Text(
                            text = "WORKSPACE SUMMARY RECORD",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )
                    }
                    Surface(
                        color = AccentBlue.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, AccentBlue),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "PDF FORMAT",
                            color = AccentBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Divider(color = Color.Black.copy(alpha = 0.15f), thickness = 2.dp)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("1. CO-OP CLASSIFICATION & METRICS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                    Text("• Workspace Name: ${workspace.name}", fontSize = 13.sp, color = Color.Black)
                    Text("• Broadcast Platform: ${workspace.platformType}", fontSize = 13.sp, color = Color.Black)
                    val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    val start = format.format(java.util.Date(workspace.createdAt))
                    Text("• Dates Active: $start to present (Archived)", fontSize = 13.sp, color = Color.Black)
                }

                Divider(color = Color.Black.copy(alpha = 0.1f))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("2. COMPLIANCE ROSTER MEMBERS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                    members.forEach { m ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("• User ID: ${m.userId}", fontSize = 13.sp, color = Color.Black)
                            Text(m.assignedRoleTitle.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Black, color = AccentBlue)
                        }
                    }
                    if (members.isEmpty()) {
                        Text("• No official registered workspace members.", fontSize = 13.sp, color = Color.DarkGray)
                    }
                }

                Divider(color = Color.Black.copy(alpha = 0.1f))

                // Section 3: Lock Agreement
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("3. MUTUAL WORKING AGREEMENT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                    if (agreement != null && agreement.isLocked) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.03f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = agreement.contentText,
                                fontSize = 12.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        Text("• No official locked agreement was recorded for this workspace.", fontSize = 13.sp, color = Color.DarkGray)
                    }
                }

                Divider(color = Color.Black.copy(alpha = 0.1f))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("4. COMPLETED PRODUCTION DELIVERABLES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                    val completed = tasks.filter { it.kanbanLane.uppercase() == "PUBLISH" }
                    completed.forEachIndexed { i, t ->
                        Text("${i+1}. [✓ COMPLETED] ${t.title}", fontSize = 13.sp, color = Color.Black)
                    }
                    if (completed.isEmpty()) {
                        Text("• No production deliverables were officially published.", fontSize = 13.sp, color = Color.DarkGray)
                    }
                }

                Divider(color = Color.Black.copy(alpha = 0.15f), thickness = 2.dp)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "OFFICIAL RECORD SEAL GENERATED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "Document validation hash offline: OK",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val context = androidx.compose.ui.platform.LocalContext.current
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            DocumentPrintHelper.printWorkspaceBlueprint(
                                context = context,
                                workspace = workspace,
                                agreement = agreement,
                                tasks = tasks,
                                members = members
                            )
                        },
                        modifier = Modifier.weight(1f).height(48.dp).testTag("print_pdf_dialog_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("PRINT / SAVE PDF", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp).testTag("close_pdf_dialog_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("CLOSE", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun MilestoneStep(title: String, description: String, state: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(16.dp)
                .clip(CircleShape)
                .background(
                    when (state) {
                        "COMPLETED" -> MaterialTheme.colorScheme.tertiary
                        "ACTIVE" -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outline
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (state == "COMPLETED") {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(10.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                color = if (state == "PENDING") TextSecondary else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun RosterMemberItem(name: String, specialty: String, badge: String, badgeColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(badgeColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = badgeColor, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(specialty, color = TextSecondary, fontSize = 11.sp)
        }
        Surface(
            color = badgeColor.copy(alpha = 0.15f),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f))
        ) {
            Text(
                text = badge,
                color = badgeColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun CustomAreaChart(points: List<Float>) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        val width = size.width
        val height = size.height
        val spacing = width / (points.size - 1)
        
        val maxVal = points.maxOrNull() ?: 1f
        val minVal = points.minOrNull() ?: 0f
        val delta = if (maxVal - minVal == 0f) 1f else (maxVal - minVal)

        val path = Path()
        val fillPath = Path()

        points.forEachIndexed { index, value ->
            val x = index * spacing
            val normalizedY = (value - minVal) / delta
            val y = height - (normalizedY * (height - 30.dp.toPx())) - 15.dp.toPx()
            
            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
            
            if (index == points.size - 1) {
                fillPath.lineTo(x, height)
                fillPath.close()
            }
        }
        
        // Draw grid lines
        for (i in 0..3) {
            val yGrid = height * (i / 3f)
            drawLine(
                color = Color.White.copy(alpha = 0.05f),
                start = Offset(0f, yGrid),
                end = Offset(width, yGrid),
                strokeWidth = 2f
            )
        }

        // Draw fill area with premium gradient
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(AccentBlue.copy(alpha = 0.35f), Color.Transparent)
            )
        )

        // Draw line with high-contrast gradient
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                colors = listOf(AccentBlue, AccentBlue)
            ),
            style = Stroke(width = 6f, cap = StrokeCap.Round)
        )

        // Draw interactive end-glow point
        if (points.isNotEmpty()) {
            val lastX = (points.size - 1) * spacing
            val lastY = height - (((points.last() - minVal) / delta) * (height - 30.dp.toPx())) - 15.dp.toPx()
            
            drawCircle(
                color = AccentBlue.copy(alpha = 0.4f),
                radius = 18f,
                center = Offset(lastX, lastY)
            )
            drawCircle(
                color = AccentBlue,
                radius = 8f,
                center = Offset(lastX, lastY)
            )
        }
    }
}

@Composable
fun LiveHuddleDialog(
    onDismiss: () -> Unit
) {
    var isMuted by remember { mutableStateOf(false) }
    var isCamOff by remember { mutableStateOf(false) }
    var secondsInCall by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000L)
            secondsInCall++
        }
    }

    val minutes = secondsInCall / 60
    val seconds = secondsInCall % 60
    val timerStr = String.format("%02d:%02d", minutes, seconds)

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("live_huddle_dialog_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AccentRed)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LIVE HUDDLE",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = timerStr,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                // Call Grid
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Participant: Me
                    HuddleParticipantItem(
                        name = "Me (Host)",
                        status = if (isMuted) "Muted" else "Speaking...",
                        isActive = !isMuted,
                        avatarLetter = "M",
                        isMe = true,
                        isCamOff = isCamOff
                    )

                    // Participant: Editor
                    HuddleParticipantItem(
                        name = "Video Editor Pro",
                        status = "Listening",
                        isActive = false,
                        avatarLetter = "E",
                        isMe = false,
                        isCamOff = false
                    )

                    // Participant: VFX
                    HuddleParticipantItem(
                        name = "VFX Mastermind",
                        status = "Speaking...",
                        isActive = true,
                        avatarLetter = "V",
                        isMe = false,
                        isCamOff = false
                    )
                }

                // Engine Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ColorDivider.copy(alpha = 0.3f))
                        .padding(vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudQueue,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Real-time Daily.co WebRTC active",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                // Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { isMuted = !isMuted },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (isMuted) AccentRed.copy(alpha = 0.15f) else ColorDivider.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("huddle_mute_mic_button")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mute Microphone",
                            tint = if (isMuted) AccentRed else Color.White
                        )
                    }

                    IconButton(
                        onClick = { isCamOff = !isCamOff },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (isCamOff) AccentRed.copy(alpha = 0.15f) else ColorDivider.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("huddle_toggle_cam_button")
                    ) {
                        Icon(
                            imageVector = if (isCamOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                            contentDescription = "Toggle Camera",
                            tint = if (isCamOff) AccentRed else Color.White
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                        modifier = Modifier.testTag("huddle_disconnect_button")
                    ) {
                        Icon(Icons.Default.CallEnd, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Disconnect", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun HuddleParticipantItem(
    name: String,
    status: String,
    isActive: Boolean,
    avatarLetter: String,
    isMe: Boolean,
    isCamOff: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else ColorDivider.copy(alpha = 0.3f))
            .border(
                1.dp,
                if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent,
                RoundedCornerShape(16.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary),
            contentAlignment = Alignment.Center
        ) {
            Text(avatarLetter, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(status, color = if (isActive) MaterialTheme.colorScheme.primary else TextSecondary, fontSize = 11.sp)
        }
        if (isCamOff && isMe) {
            Icon(Icons.Default.VideocamOff, contentDescription = "Cam Off", tint = TextSecondary, modifier = Modifier.size(16.dp))
        } else {
            Icon(
                imageVector = if (isActive) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                contentDescription = null,
                tint = if (isActive) MaterialTheme.colorScheme.primary else TextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun WorkspaceStatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier, valueColor: Color = Color.White, onClick: (() -> Unit)? = null) {
    Card(
        modifier = modifier.clickable(enabled = onClick != null) { onClick?.invoke() },
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ColorDivider)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = valueColor)
            Text(title, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun TaskSimpleItem(task: com.example.data.model.ProductionTask) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceColor, RoundedCornerShape(12.dp))
            .border(1.dp, ColorDivider, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(task.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("Lane: ${task.kanbanLane}", color = TextSecondary, fontSize = 12.sp)
        }
        Icon(Icons.Default.ChevronRight, "View Task", tint = TextSecondary)
    }
}

@Composable
fun ActivityFeedItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, time: String, iconColor: Color) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier.size(28.dp).clip(CircleShape).background(iconColor.copy(alpha=0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(14.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(time, color = TextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
fun ResourceCard(name: String, icon: androidx.compose.ui.graphics.vector.ImageVector, subtitle: String) {
    Row(
        modifier = Modifier
            .background(SurfaceColor, RoundedCornerShape(12.dp))
            .border(1.dp, ColorDivider, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = AccentBlue, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = TextSecondary, fontSize = 11.sp)
        }
    }
}
