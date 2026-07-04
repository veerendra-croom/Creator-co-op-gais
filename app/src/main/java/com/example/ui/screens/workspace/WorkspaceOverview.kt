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
import com.example.ui.theme.*
import com.example.ui.components.*
import com.example.ui.viewmodels.WorkspaceViewModel

@Composable
fun WorkspaceOverview(
    workspace: Workspace,
    viewModel: WorkspaceViewModel,
    isAgreementActive: Boolean,
    onGoToAgreement: () -> Unit
) {
    val scrollState = rememberScrollState()
    val activeAgreement by viewModel.activeAgreement.collectAsState()
    val activeTasks by viewModel.activeTasks.collectAsState()
    val activeMembers by viewModel.activeWorkspaceMembers.collectAsState()
    val disputeNotes by viewModel.activeDisputeNotes.collectAsState()
    val currentUserId by viewModel.currentUserIdFlow.collectAsState()

    var showPDFDialog by remember { mutableStateOf(false) }
    var privateNoteBody by remember { mutableStateOf("") }
    
    val featureFlags by viewModel.featureFlags.collectAsState()
    val isHuddlesEnabled = featureFlags.find { it.flagKey == "huddles_enabled" }?.isEnabled ?: true
    var showHuddleDialog by remember { mutableStateOf(false) }
    
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
                
                if (isHuddlesEnabled) {
                    IconButton(
                        onClick = { showHuddleDialog = true },
                        modifier = Modifier
                            .background(SurfaceColor, DS.RadiusMedium)
                            .border(1.dp, ColorDivider.copy(alpha = 0.5f), DS.RadiusMedium)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Default.VideoCall, "Live Video Huddle", tint = AccentBlue)
                    }
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
                            onClick = { /* Create Task Action */ },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            shape = DS.RadiusMedium,
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(DS.Space8))
                            Text("New Task", fontWeight = FontWeight.Bold)
                        }
                        
                        IconButton(
                            onClick = { /* Open Chat */ },
                            modifier = Modifier
                                .background(SurfaceLightColor, DS.RadiusMedium)
                                .size(44.dp)
                        ) {
                            Icon(Icons.Default.Chat, "Chat", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        
                        IconButton(
                            onClick = { /* Invite */ },
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

        // --- 4. Active Tasks ---
        Column {
            SectionHeader(
                title = "Active Production Backlog",
                action = {
                    TextButton(onClick = { /* Go to Kanban */ }) {
                        Text("View Kanban Board", color = AccentBlue, fontWeight = FontWeight.Bold)
                    }
                }
            )
            Spacer(modifier = Modifier.height(DS.Space8))
            val pending = activeTasks.filter { it.kanbanLane != "PUBLISH" }.take(3)
            if (pending.isEmpty()) {
                EmptyState(
                    message = "Your backlog is perfectly clear. No pending production deliverables!",
                    icon = Icons.Default.DoneAll,
                    actionText = "Create Backlog Item",
                    onAction = { /* Create Task Action */ }
                )
            } else {
                pending.forEach { task ->
                    TaskCard(
                        title = task.title,
                        lane = task.kanbanLane,
                        priority = if (task.kanbanLane == "REVIEW") "High" else "Medium",
                        onClick = { /* Task details */ }
                    )
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
                    ActivityCard(Icons.Default.Bolt, "Workspace integration initialized", "2 days ago", AccentBlue)
                    HorizontalDivider(color = ColorDivider.copy(alpha = 0.3f), modifier = Modifier.padding(start = 40.dp, top = DS.Space8, bottom = DS.Space8))
                    ActivityCard(Icons.Default.TaskAlt, "Created the production backlog schema", "1 day ago", NeonEmerald)
                    HorizontalDivider(color = ColorDivider.copy(alpha = 0.3f), modifier = Modifier.padding(start = 40.dp, top = DS.Space8, bottom = DS.Space8))
                    ActivityCard(Icons.Default.Gavel, "Working Agreement distributed for digital signatures", "4 hours ago", CrispAmber)
                }
            }
        }

        // --- 6. Shared Resources ---
        Column {
            SectionHeader(
                title = "Co-Op Shared Assets",
                action = {
                    IconButton(onClick = {}) { Icon(Icons.Default.AddCircle, null, tint = AccentBlue) }
                }
            )
            Spacer(modifier = Modifier.height(DS.Space8))
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

    if (showHuddleDialog) {
        LiveHuddleDialog(onDismiss = { showHuddleDialog = false })
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
                            fontSize = 9.sp,
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
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "Document validation hash offline: OK",
                        fontSize = 9.sp,
                        color = Color.DarkGray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("close_pdf_dialog_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("CLOSE PDF PREVIEW", fontWeight = FontWeight.Bold, color = Color.White)
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
                fontSize = 9.sp,
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
                        fontSize = 10.sp
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
