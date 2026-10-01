package com.example.ui.screens.workspace

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContentCalendarItem
import com.example.data.model.ProductionTask
import com.example.data.model.TaskTemplate
import com.example.data.model.TemplateTask
import com.example.ui.theme.*
import com.example.ui.components.*
import com.example.ui.tour.guidedTourTarget
import com.example.ui.viewmodels.WorkspaceViewModel
import java.util.Calendar
import java.util.GregorianCalendar

@Composable
fun TeamSpaceScreen(
    viewModel: WorkspaceViewModel,
    workspaceId: String,
    userId: String,
    isAgreementActive: Boolean = true,
    onGoToAgreement: () -> Unit = {},
    globalViewModel: com.example.ui.viewmodels.GlobalViewModel? = null
) {
    if (!isAgreementActive) {
        Box(
            modifier = Modifier.fillMaxSize().background(PrimaryBackground).padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = AccentRed
                    )
                    Text(
                        "Workspace Tasks Locked",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                    Text(
                        "Team tasks and collaborative assets are disabled until the team agreement is acknowledged by all parties.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Button(
                        onClick = onGoToAgreement,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.HistoryEdu, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Review & Acknowledge Agreement", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
        return
    }

    val tasks by viewModel.activeTasks.collectAsState()
    val prodTasks = tasks.filter { it.stateScope == "PRODUCTION_READY" }
    val lanes = listOf("IDEAS", "RESEARCH", "SCRIPT", "RECORD", "EDIT", "REVIEW", "PUBLISH")
    
    val members by viewModel.activeWorkspaceMembers.collectAsState()
    val myMember = members.find { it.userId == userId }
    val workspace by viewModel.selectedWorkspace.collectAsState()
    val canModify = myMember?.canModifyProduction == true || myMember?.assignedRoleTitle in listOf("Lead Creator", "Head")
    val isHead = myMember?.assignedRoleTitle in listOf("Lead Creator", "Head") || workspace?.createdBy == userId

    val calendarItems by viewModel.activeCalendarItems.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val currentUserProfile by viewModel.currentUserProfile.collectAsState()
    val haptics = LocalHapticFeedback.current

    var activeTab by remember { mutableStateOf("BOARD") } // "BOARD", "CALENDAR"
    var showCreateTaskDialog by remember { mutableStateOf(false) }
    var taskCreationLane by remember { mutableStateOf("IDEAS") }
    var selectedTask by remember { mutableStateOf<ProductionTask?>(null) }
    var showSaveTemplateDialog by remember { mutableStateOf(false) }
    var showApplyTemplateDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(PrimaryBackground)) {
        // Tab row header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DS.Space16, vertical = DS.Space12),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Segmented controller tabs
            Row(
                modifier = Modifier
                    .background(SurfaceColor, DS.RadiusMedium)
                    .border(1.dp, ColorDivider.copy(alpha = 0.5f), DS.RadiusMedium)
                    .padding(DS.Space4),
                horizontalArrangement = Arrangement.spacedBy(DS.Space4),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("BOARD" to "Production Board", "CALENDAR" to "Content Calendar").forEach { (tabId, label) ->
                    val isSelected = activeTab == tabId
                    Box(
                        modifier = Modifier
                            .clip(DS.RadiusMedium)
                            .background(if (isSelected) AccentRed.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { activeTab = tabId }
                            .padding(horizontal = DS.Space12, vertical = DS.Space6),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) AccentRed else TextSecondary,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Template actions if Head
            if (activeTab == "BOARD" && isHead) {
                Row(horizontalArrangement = Arrangement.spacedBy(DS.Space8)) {
                    IconButton(
                        onClick = { showSaveTemplateDialog = true },
                        modifier = Modifier
                            .background(SurfaceColor, DS.RadiusMedium)
                            .border(1.dp, ColorDivider.copy(alpha = 0.5f), DS.RadiusMedium)
                            .size(36.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save Board as Template", tint = AccentRed, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = { showApplyTemplateDialog = true },
                        modifier = Modifier
                            .background(SurfaceColor, DS.RadiusMedium)
                            .border(1.dp, ColorDivider.copy(alpha = 0.5f), DS.RadiusMedium)
                            .size(36.dp)
                    ) {
                        Icon(Icons.Default.DynamicFeed, contentDescription = "Apply Template", tint = AccentRed, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        HorizontalDivider(color = ColorDivider.copy(alpha = 0.3f))

        AnimatedContent(
            targetState = activeTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "team_space_subtab",
            modifier = Modifier.weight(1f)
        ) { tab ->
            when (tab) {
                "BOARD" -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        AnimatedContent(
                            targetState = prodTasks.isEmpty(),
                            transitionSpec = { fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300)) },
                            label = "team_board_empty_transition",
                            modifier = Modifier.fillMaxSize()
                        ) { isEmpty ->
                            if (isEmpty) {
                                Box(
                                    modifier = Modifier.fillMaxSize().padding(DS.Space24),
                                    contentAlignment = Alignment.Center
                                ) {
                                    EmptyState(
                                        message = "No active production deliverables deployed yet.",
                                        icon = Icons.Default.Task,
                                        actionText = if (canModify) "Create Backlog Task" else null,
                                        onAction = { showCreateTaskDialog = true }
                                    )
                                }
                            } else {
                                LazyRow(
                                    modifier = if (globalViewModel != null) Modifier.fillMaxSize().guidedTourTarget("workspace_kanban_columns", globalViewModel.tourManager) else Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(horizontal = DS.Space16, vertical = DS.Space16),
                                    horizontalArrangement = Arrangement.spacedBy(DS.Space16)
                                ) {
                                items(lanes, key = { it }) { lane ->
                                    val laneTasks = prodTasks.filter { it.kanbanLane == lane }
                                    
                                    val (laneColor, laneIcon) = when(lane) {
                                        "IDEAS" -> Pair(Color.White, Icons.Outlined.Lightbulb)
                                        "RESEARCH" -> Pair(AccentBlue, Icons.Default.Search)
                                        "SCRIPT" -> Pair(CrispAmber, Icons.Default.HistoryEdu)
                                        "RECORD" -> Pair(AccentRed, Icons.Default.Videocam)
                                        "EDIT" -> Pair(AccentBlue, Icons.Default.VideoLibrary)
                                        "REVIEW" -> Pair(CrispAmber, Icons.Default.RateReview)
                                        "PUBLISH" -> Pair(NeonEmerald, Icons.Default.Verified)
                                        else -> Pair(AccentBlue, Icons.Default.AccountTree)
                                    }

                                    Column(modifier = Modifier.width(280.dp).fillMaxHeight()) {
                                        // Lane Header Card
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = DS.Space12),
                                            color = SurfaceColor,
                                            shape = DS.RadiusMedium,
                                            border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.5f))
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = DS.Space12, vertical = DS.Space8),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .clip(CircleShape)
                                                            .background(laneColor.copy(alpha = 0.15f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = laneIcon,
                                                            contentDescription = null,
                                                            tint = laneColor,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(DS.Space8))
                                                    Text(
                                                        text = lane,
                                                        color = Color.White,
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.ExtraBold
                                                    )
                                                }
                                                
                                                Surface(
                                                    color = Color.White.copy(alpha = 0.1f),
                                                    shape = CircleShape
                                                ) {
                                                    Text(
                                                        text = laneTasks.size.toString(),
                                                        modifier = Modifier.padding(horizontal = DS.Space6, vertical = DS.Space2),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }

                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .verticalScroll(rememberScrollState()),
                                            verticalArrangement = Arrangement.spacedBy(DS.Space12)
                                        ) {
                                            laneTasks.forEach { task ->
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable { selectedTask = task },
                                                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                                    shape = DS.RadiusMedium,
                                                    border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.5f))
                                                ) {
                                                    Column(modifier = Modifier.padding(DS.Space12)) {
                                                        Text(
                                                            text = task.title,
                                                            color = Color.White,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Spacer(modifier = Modifier.height(DS.Space4))
                                                        Text(
                                                            text = task.contentBody,
                                                            color = TextSecondary,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            maxLines = 2
                                                        )
                                                        
                                                        if (canModify) {
                                                            Spacer(modifier = Modifier.height(DS.Space8))
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Row(
                                                                    horizontalArrangement = Arrangement.spacedBy(DS.Space4),
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    val prevIndex = lanes.indexOf(lane) - 1
                                                                    if (prevIndex >= 0) {
                                                                        val prevLane = lanes[prevIndex]
                                                                        IconButton(
                                                                            onClick = { 
                                                                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                                                viewModel.moveTaskLane(task.id, prevLane, userId) 
                                                                            },
                                                                            modifier = Modifier
                                                                                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                                                                                .size(24.dp)
                                                                        ) {
                                                                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Move Left", tint = TextSecondary, modifier = Modifier.size(12.dp))
                                                                        }
                                                                    }
                                                                    
                                                                    val nextIndex = lanes.indexOf(lane) + 1
                                                                    if (nextIndex < lanes.size) {
                                                                        val nextLane = lanes[nextIndex]
                                                                        IconButton(
                                                                            onClick = { 
                                                                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                                viewModel.moveTaskLane(task.id, nextLane, userId) 
                                                                            },
                                                                            modifier = Modifier
                                                                                .background(laneColor.copy(alpha = 0.15f), CircleShape)
                                                                                .size(24.dp)
                                                                        ) {
                                                                            Icon(Icons.AutoMirrored.Filled.ArrowForward, "Move Right", tint = laneColor, modifier = Modifier.size(12.dp))
                                                                        }
                                                                    }
                                                                }

                                                                // Simplification 4: Contextual Task Threading button
                                                                IconButton(
                                                                    onClick = { selectedTask = task },
                                                                    modifier = Modifier.size(24.dp)
                                                                ) {
                                                                    Icon(Icons.Outlined.ChatBubbleOutline, "Task Discussion", tint = AccentBlue, modifier = Modifier.size(14.dp))
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        
                                        if (canModify) {
                                            Spacer(modifier = Modifier.height(DS.Space12))
                                            FeatureGate(
                                                flagKey = "TASK_CREATION",
                                                userRole = currentUserProfile?.systemRole ?: "CREATOR",
                                                repository = viewModel.repository,
                                                fallback = {
                                                    FeatureRestrictedBanner(
                                                        featureTitle = "Task Creation",
                                                        restrictedMessage = "Task drafting is temporarily restricted by platform governance."
                                                    )
                                                }
                                            ) {
                                                Button(
                                                    onClick = { 
                                                        taskCreationLane = lane
                                                        showCreateTaskDialog = true 
                                                    },
                                                    modifier = Modifier.fillMaxWidth().height(44.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                                                    shape = DS.RadiusMedium,
                                                    border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.5f))
                                                ) {
                                                    Icon(Icons.Default.Add, null, tint = laneColor, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(DS.Space6))
                                                    Text("Add to $lane", color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                "CALENDAR" -> {
                    CalendarView(
                        viewModel = viewModel,
                        userId = userId,
                        isHead = isHead,
                        prodTasks = prodTasks,
                        calendarItems = calendarItems
                    )
                }
            }
        }
    }

    if (showCreateTaskDialog) {
        var tTitle by remember { mutableStateOf("") }
        var tBody by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateTaskDialog = false },
            containerColor = SurfaceColor,
            title = { Text("Launch Shared Task", color = Color.White, fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = tTitle,
                        onValueChange = { tTitle = it },
                        label = { Text("Task Title") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AccentRed)
                    )
                    OutlinedTextField(
                        value = tBody,
                        onValueChange = { tBody = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth().height(90.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AccentRed)
                    )
                    Text("Target Kanban Lane", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        lanes.forEach { l ->
                            FilterChip(
                                selected = taskCreationLane == l,
                                onClick = { taskCreationLane = l },
                                label = { Text(l, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentBlue.copy(alpha = 0.2f),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.submitTask(tTitle, tBody, "PRODUCTION_READY", userId, kanbanLane = taskCreationLane)
                        showCreateTaskDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                    enabled = tTitle.isNotBlank()
                ) {
                    Text("Deploy Task")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTaskDialog = false }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }

    if (selectedTask != null) {
        TaskDetailsDialog(
            task = selectedTask!!,
            viewModel = viewModel,
            repository = viewModel.repository,
            userId = userId,
            currentUser = currentUserProfile,
            allUsers = allUsers,
            onDismiss = { selectedTask = null },
            globalViewModel = globalViewModel
        )
    }

    if (showSaveTemplateDialog) {
        var templateName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSaveTemplateDialog = false },
            containerColor = SurfaceColor,
            title = { Text("Save Board as Template", color = Color.White, fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Capture current lanes and task structures of the active production board as a template. Only available to Heads.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = templateName,
                        onValueChange = { templateName = it },
                        label = { Text("Template Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AccentRed)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveWorkspaceToTemplate(templateName, userId)
                        showSaveTemplateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                    enabled = templateName.isNotBlank()
                ) {
                    Text("Save Template")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveTemplateDialog = false }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }

    if (showApplyTemplateDialog) {
        val templates by viewModel.activeTemplates.collectAsState()
        AlertDialog(
            onDismissRequest = { showApplyTemplateDialog = false },
            containerColor = SurfaceColor,
            title = { Text("Apply Task Template", color = Color.White, fontWeight = FontWeight.Black) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Applying a template will spawn a fresh set of all stored tasks onto the Kanban board under their default lanes.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    if (templates.isEmpty()) {
                        Text("No templates saved for this workspace.", color = AccentRed, fontWeight = FontWeight.Bold)
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            items(templates, key = { it.id }) { template ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.applyTemplateToWorkspace(template.id, userId)
                                            showApplyTemplateDialog = false
                                        },
                                    colors = CardDefaults.cardColors(containerColor = PrimaryBackground),
                                    border = BorderStroke(1.dp, ColorDivider)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = template.templateName,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        IconButton(
                                            onClick = { viewModel.deleteTemplate(template.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Template", tint = AccentRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showApplyTemplateDialog = false }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }
}

@Composable
fun CalendarView(
    viewModel: WorkspaceViewModel,
    userId: String,
    isHead: Boolean,
    prodTasks: List<ProductionTask>,
    calendarItems: List<ContentCalendarItem>
) {
    var calendarYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var calendarMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) } // 0-indexed

    val itemsByDate = remember(calendarItems) {
        calendarItems.groupBy { it.scheduledDate }
    }

    val calendar = remember(calendarYear, calendarMonth) {
        GregorianCalendar(calendarYear, calendarMonth, 1)
    }
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday...
    val maxDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    val paddingCells = firstDayOfWeek - 1
    val monthNames = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val currentMonthName = monthNames[calendarMonth]

    var selectedDateForDetails by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Month navigation
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (calendarMonth == 0) {
                    calendarMonth = 11
                    calendarYear--
                } else {
                    calendarMonth--
                }
            }) {
                Icon(Icons.Default.ChevronLeft, "Previous Month", tint = Color.White)
            }

            Text(
                text = "$currentMonthName $calendarYear",
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = Color.White
            )

            IconButton(onClick = {
                if (calendarMonth == 11) {
                    calendarMonth = 0
                    calendarYear++
                } else {
                    calendarMonth++
                }
            }) {
                Icon(Icons.Default.ChevronRight, "Next Month", tint = Color.White)
            }
        }

        // Days of week header
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { dayName ->
                Text(
                    text = dayName,
                    modifier = Modifier.weight(1f),
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        // Calendar Grid
        val totalCells = paddingCells + maxDaysInMonth
        val rows = (totalCells + 6) / 7

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
        ) {
            for (r in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (c in 0 until 7) {
                        val cellIndex = r * 7 + c
                        if (cellIndex < paddingCells || cellIndex >= totalCells) {
                            // Empty pads
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                        } else {
                            val dayNum = cellIndex - paddingCells + 1
                            val dateStr = String.format("%04d-%02d-%02d", calendarYear, calendarMonth + 1, dayNum)
                            val dayItems = itemsByDate[dateStr] ?: emptyList()

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.9f)
                                    .clickable { selectedDateForDetails = dateStr },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (dayNum == Calendar.getInstance().get(Calendar.DAY_OF_MONTH) &&
                                        calendarMonth == Calendar.getInstance().get(Calendar.MONTH) &&
                                        calendarYear == Calendar.getInstance().get(Calendar.YEAR)) {
                                        AccentRed.copy(alpha = 0.2f)
                                    } else {
                                        SurfaceColor
                                    }
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (dayItems.isNotEmpty()) {
                                        val firstItem = dayItems.first()
                                        when (firstItem.status) {
                                            "Published" -> NeonEmerald.copy(alpha = 0.5f)
                                            "Scheduled" -> CrispAmber.copy(alpha = 0.5f)
                                            "Editing" -> AccentBlue.copy(alpha = 0.5f)
                                            else -> ColorDivider
                                        }
                                    } else ColorDivider
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(4.dp).fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = dayNum.toString(),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        color = if (dayItems.isNotEmpty()) {
                                            val firstItem = dayItems.first()
                                            when (firstItem.status) {
                                                "Published" -> NeonEmerald
                                                "Scheduled" -> CrispAmber
                                                "Editing" -> AccentBlue
                                                else -> Color.White
                                            }
                                        } else Color.White
                                    )

                                    if (dayItems.isNotEmpty()) {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(2.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            dayItems.take(2).forEach { item ->
                                                val statusColor = when (item.status) {
                                                    "Published" -> NeonEmerald
                                                    "Scheduled" -> CrispAmber
                                                    "Editing" -> AccentBlue
                                                    else -> TextSecondary
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                        .border(0.5.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = item.title,
                                                        color = Color.White,
                                                        fontSize = 10.sp,
                                                        maxLines = 1,
                                                        fontWeight = FontWeight.SemiBold,
                                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                            if (dayItems.size > 2) {
                                                Text(
                                                    text = "+${dayItems.size - 2} more",
                                                    fontSize = 10.sp,
                                                    color = TextSecondary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.height(1.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedDateForDetails != null) {
        val dateStr = selectedDateForDetails!!
        val dayItems = itemsByDate[dateStr] ?: emptyList()
        var showCreateItemDialog by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { selectedDateForDetails = null },
            containerColor = SurfaceColor,
            title = {
                Text(
                    text = "Details: $dateStr",
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (dayItems.isEmpty()) {
                        Text(
                            text = "No production items scheduled for this date.",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    } else {
                        Text(
                            text = "Scheduled Items:",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            items(dayItems) { item ->
                                val linkedTask = prodTasks.find { it.id == item.linkedTaskId }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = PrimaryBackground),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, ColorDivider),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = item.title,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    modifier = Modifier.weight(1f, fill = false)
                                                )
                                                val statusColor = when (item.status) {
                                                    "Published" -> NeonEmerald
                                                    "Scheduled" -> CrispAmber
                                                    "Editing" -> AccentBlue
                                                    else -> TextSecondary
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                        .border(0.5.dp, statusColor, RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = item.status,
                                                        color = statusColor,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            if (linkedTask != null) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Linked to: ${linkedTask.title}",
                                                    color = AccentBlue,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        val isItemEditable = item.createdBy == userId || isHead
                                        if (isItemEditable) {
                                            IconButton(
                                                onClick = {
                                                    viewModel.deleteCalendarItem(item.id)
                                                    selectedDateForDetails = null
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete item",
                                                    tint = AccentRed,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Add Event or Link Task",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCreateItemDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("Add Event / Link Task")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedDateForDetails = null }) {
                    Text("Close", color = TextSecondary)
                }
            }
        )

        if (showCreateItemDialog) {
            var itemTitle by remember { mutableStateOf("") }
            var selectedTaskIdToLink by remember { mutableStateOf<String?>(null) }
            var isLinkingTaskMode by remember { mutableStateOf(false) }
            var selectedStatus by remember { mutableStateOf("Drafting") }

            val linkableTasks = prodTasks

            AlertDialog(
                onDismissRequest = { showCreateItemDialog = false },
                containerColor = SurfaceColor,
                title = {
                    Text(
                        text = "Schedule New Event",
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { isLinkingTaskMode = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!isLinkingTaskMode) AccentRed else Color.White.copy(alpha = 0.05f)
                                ),
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = DS.RadiusMedium
                            ) {
                                Text("New Event", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Button(
                                onClick = { isLinkingTaskMode = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isLinkingTaskMode) AccentRed else Color.White.copy(alpha = 0.05f)
                                ),
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = DS.RadiusMedium
                            ) {
                                Text("Link Existing Task", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        if (!isLinkingTaskMode) {
                            OutlinedTextField(
                                value = itemTitle,
                                onValueChange = { itemTitle = it },
                                label = { Text("Event Title") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = AccentRed
                                )
                            )
                        } else {
                            Text(
                                "Select Task to Link:",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                            if (linkableTasks.isEmpty()) {
                                Text("No tasks on board.", color = TextSecondary)
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.heightIn(max = 160.dp)
                                ) {
                                    items(linkableTasks, key = { it.id }) { task ->
                                        val isSelected = selectedTaskIdToLink == task.id
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { selectedTaskIdToLink = task.id },
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isSelected) AccentRed.copy(alpha = 0.15f) else PrimaryBackground
                                            ),
                                            border = BorderStroke(
                                                1.dp,
                                                if (isSelected) AccentRed else ColorDivider
                                            )
                                        ) {
                                            Text(
                                                text = task.title,
                                                modifier = Modifier.padding(12.dp),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Select Status:",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Drafting", "Editing", "Scheduled", "Published").forEach { status ->
                                val isSelected = selectedStatus == status
                                val statusColor = when (status) {
                                    "Published" -> NeonEmerald
                                    "Scheduled" -> CrispAmber
                                    "Editing" -> AccentBlue
                                    else -> NeutralColor
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) statusColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.03f))
                                        .border(1.dp, if (isSelected) statusColor else ColorDivider, RoundedCornerShape(8.dp))
                                        .clickable { selectedStatus = status },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = status,
                                        color = if (isSelected) statusColor else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val finalTitle = if (isLinkingTaskMode) {
                                prodTasks.find { it.id == selectedTaskIdToLink }?.title ?: "Linked Task"
                            } else {
                                itemTitle
                            }
                            if (finalTitle.isNotBlank()) {
                                viewModel.addCalendarItem(
                                    title = finalTitle,
                                    scheduledDate = dateStr,
                                    linkedTaskId = if (isLinkingTaskMode) selectedTaskIdToLink else null,
                                    userId = userId,
                                    status = selectedStatus
                                )
                            }
                            showCreateItemDialog = false
                            selectedDateForDetails = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                        enabled = if (isLinkingTaskMode) selectedTaskIdToLink != null else itemTitle.isNotBlank(),
                        modifier = Modifier.height(48.dp),
                        shape = DS.RadiusMedium
                    ) {
                        Text("Add Event", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateItemDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }
    }
}
