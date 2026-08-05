package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ProductionTask
import com.example.data.model.TeamAgreement
import com.example.data.model.Workspace
import com.example.ui.theme.*
import com.example.ui.viewmodels.AgreementViewModel
import com.example.ui.viewmodels.GlobalViewModel
import com.example.ui.viewmodels.WorkspaceViewModel
import com.example.ui.feedback.FeedbackManager
import kotlinx.coroutines.launch

@Composable
fun CreationSpeedDialFab(
    globalViewModel: GlobalViewModel,
    workspaceViewModel: WorkspaceViewModel,
    agreementViewModel: AgreementViewModel,
    userId: String,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    
    // Dialog states
    var showCreateWorkspace by remember { mutableStateOf(false) }
    var showCreateAgreement by remember { mutableStateOf(false) }
    var showInviteMember by remember { mutableStateOf(false) }
    var showCreateTask by remember { mutableStateOf(false) }

    val workspaces by globalViewModel.allWorkspaces.collectAsState()

    // Motion Specs: Main FAB Rotation and Background Dim
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 135f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "fab_rotation"
    )

    val dimAlpha by animateFloatAsState(
        targetValue = if (isExpanded) 0.65f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "dim_overlay"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        // 1. Semi-transparent interactive dim overlay when expanded
        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(dimAlpha)
                    .background(PrimaryBackground)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (com.example.ui.feedback.FeedbackManager.isHapticEnabled) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        isExpanded = false
                    }
            )
        }

        // 2. Vertical Staggered Speed Dial Options
        Column(
            modifier = Modifier
                .padding(end = 16.dp, bottom = 90.dp) // Float above bottom bar
                .wrapContentSize(),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val speedDialItems = listOf(
                SpeedDialActionItem(
                    label = "Queue Production Task",
                    icon = Icons.Default.AddTask,
                    tint = AccentRed,
                    onClick = {
                        showCreateTask = true
                        isExpanded = false
                    }
                ),
                SpeedDialActionItem(
                    label = "Dispatch Member Invitation",
                    icon = Icons.Default.PersonAdd,
                    tint = NeonEmerald,
                    onClick = {
                        showInviteMember = true
                        isExpanded = false
                    }
                ),
                SpeedDialActionItem(
                    label = "Execute IP Agreement",
                    icon = Icons.Default.Gavel,
                    tint = CrispAmber,
                    onClick = {
                        showCreateAgreement = true
                        isExpanded = false
                    }
                ),
                SpeedDialActionItem(
                    label = "Establish Workspace Node",
                    icon = Icons.Default.AddHomeWork,
                    tint = AccentBlue,
                    onClick = {
                        showCreateWorkspace = true
                        isExpanded = false
                    }
                )
            )

            speedDialItems.forEachIndexed { index, item ->
                val scale by animateFloatAsState(
                    targetValue = if (isExpanded) 1f else 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMedium - (index * 50f)
                    ),
                    label = "item_scale_$index"
                )

                val alpha by animateFloatAsState(
                    targetValue = if (isExpanded) 1f else 0f,
                    animationSpec = tween(
                        durationMillis = 150,
                        delayMillis = index * 40
                    ),
                    label = "item_alpha_$index"
                )

                if (scale > 0.05f) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                this.alpha = alpha
                            }
                    ) {
                        // Action Text Label
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, ColorDivider),
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .clickable {
                                    if (com.example.ui.feedback.FeedbackManager.isHapticEnabled) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    item.onClick()
                                }
                        ) {
                            Text(
                                text = item.label.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        // Circular Mini Action Button
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(SurfaceColor)
                                .border(1.5.dp, item.tint.copy(alpha = 0.8f), CircleShape)
                                .clickable {
                                    if (com.example.ui.feedback.FeedbackManager.isHapticEnabled) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    item.onClick()
                                }
                                .semantics { contentDescription = item.label },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = item.tint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Central Primary Expansion FAB
        Box(
            modifier = Modifier
                .padding(end = 16.dp, bottom = 16.dp)
                .size(58.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            if (isExpanded) AccentRed else AccentBlue,
                            PrimaryBackground
                        ),
                        radius = 180f
                    )
                )
                .border(2.dp, if (isExpanded) AccentRed else AccentBlue, CircleShape)
                .clickable {
                    if (com.example.ui.feedback.FeedbackManager.isHapticEnabled) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    isExpanded = !isExpanded
                }
                .semantics { contentDescription = if (isExpanded) "Collapse Creator Actions" else "Expand Creator Actions" },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .rotate(rotationAngle)
            )
        }
    }

    // ====================================================
    // FULLY FUNCTIONAL MODAL CREATION FORMS
    // ====================================================

    // 1. CREATE WORKSPACE DIALOG
    if (showCreateWorkspace) {
        var wsName by remember { mutableStateOf("") }
        var selectedPlatform by remember { mutableStateOf("YouTube") }
        val platforms = listOf("YouTube", "TikTok", "Twitch", "Instagram", "Syndicate Channel")

        PremiumCreationDialog(
            title = "ESTABLISH COLLABORATION NODE",
            accentColor = AccentBlue,
            onDismiss = { showCreateWorkspace = false },
            onSubmit = {
                if (wsName.isNotBlank()) {
                    workspaceViewModel.createWorkspace(wsName, selectedPlatform, userId)
                    showCreateWorkspace = false
                    FeedbackManager.showSuccess("Workspace node '$wsName' initiated successfully!")
                }
            },
            submitEnabled = wsName.isNotBlank()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Workspace Name", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = wsName,
                        onValueChange = { wsName = it },
                        placeholder = { Text("e.g. Media Syndicate Alpha", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = ColorDivider
                        )
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Target Syndication Platform", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        platforms.forEach { plat ->
                            val isSelected = selectedPlatform == plat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedPlatform = plat },
                                label = { Text(plat) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentBlue.copy(alpha = 0.2f),
                                    selectedLabelColor = Color.White,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    // 2. CREATE AGREEMENT DIALOG
    if (showCreateAgreement) {
        var selectedWsId by remember { mutableStateOf(workspaces.firstOrNull()?.id ?: "") }
        var agreementText by remember { mutableStateOf("") }

        PremiumCreationDialog(
            title = "DEPLOY COLLECTIVE IP CONTRACT",
            accentColor = CrispAmber,
            onDismiss = { showCreateAgreement = false },
            onSubmit = {
                if (selectedWsId.isNotBlank() && agreementText.isNotBlank()) {
                    agreementViewModel.createTeamAgreement(selectedWsId, agreementText)
                    showCreateAgreement = false
                    FeedbackManager.showSuccess("IP Contract deployed for compliance verification!")
                }
            },
            submitEnabled = selectedWsId.isNotBlank() && agreementText.isNotBlank()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (workspaces.isEmpty()) {
                    Text("No active workspace nodes found. Establish a workspace first.", color = AccentRed, fontSize = 12.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Select Target Workspace", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            workspaces.forEach { ws ->
                                val isSelected = selectedWsId == ws.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedWsId = ws.id },
                                    label = { Text(ws.name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CrispAmber.copy(alpha = 0.2f),
                                        selectedLabelColor = Color.White,
                                        labelColor = TextSecondary
                                    )
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Agreement Clauses", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = agreementText,
                            onValueChange = { agreementText = it },
                            placeholder = { Text("Define revenue splits, IP ownership percentages, and channel roles...", color = TextMuted) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            maxLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = CrispAmber,
                                unfocusedBorderColor = ColorDivider
                            )
                        )
                    }
                }
            }
        }
    }

    // 3. INVITE MEMBER DIALOG
    if (showInviteMember) {
        var selectedWsId by remember { mutableStateOf(workspaces.firstOrNull()?.id ?: "") }
        var inviteEmail by remember { mutableStateOf("") }
        var selectedRole by remember { mutableStateOf("Co-Op Collaborator") }
        val roles = listOf("Co-Op Collaborator", "Lead Creator", "Chief Editor", "Syndicate Manager")

        PremiumCreationDialog(
            title = "DISPATCH PEER INVITATION",
            accentColor = NeonEmerald,
            onDismiss = { showInviteMember = false },
            onSubmit = {
                if (selectedWsId.isNotBlank() && inviteEmail.isNotBlank()) {
                    workspaceViewModel.inviteMember(selectedWsId, inviteEmail, selectedRole)
                    
                    // Dispatch platform notification as well
                    val wsName = workspaces.find { it.id == selectedWsId }?.name ?: "Node"
                    globalViewModel.sendNotification(
                        userId = userId,
                        title = "Invitation Dispatched",
                        body = "Sent secure invite to $inviteEmail for role '$selectedRole' in $wsName.",
                        type = "CONNECTIONS"
                    )
                    
                    showInviteMember = false
                }
            },
            submitEnabled = selectedWsId.isNotBlank() && inviteEmail.isNotBlank()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (workspaces.isEmpty()) {
                    Text("No active workspace nodes found. Establish a workspace first.", color = AccentRed, fontSize = 12.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Select Target Workspace", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            workspaces.forEach { ws ->
                                val isSelected = selectedWsId == ws.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedWsId = ws.id },
                                    label = { Text(ws.name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonEmerald.copy(alpha = 0.2f),
                                        selectedLabelColor = Color.White,
                                        labelColor = TextSecondary
                                    )
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Peer Email or Handle", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = inviteEmail,
                            onValueChange = { inviteEmail = it },
                            placeholder = { Text("e.g. partner@creatorcoop.com", color = TextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonEmerald,
                                unfocusedBorderColor = ColorDivider
                            )
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Assigned Operational Role", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            roles.forEach { r ->
                                val isSelected = selectedRole == r
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedRole = r },
                                    label = { Text(r) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonEmerald.copy(alpha = 0.2f),
                                        selectedLabelColor = Color.White,
                                        labelColor = TextSecondary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 4. CREATE TASK DIALOG
    if (showCreateTask) {
        var selectedWsId by remember { mutableStateOf(workspaces.firstOrNull()?.id ?: "") }
        var taskTitle by remember { mutableStateOf("") }
        var taskDesc by remember { mutableStateOf("") }

        PremiumCreationDialog(
            title = "QUEUE PRODUCTION DELIVERABLE",
            accentColor = AccentRed,
            onDismiss = { showCreateTask = false },
            onSubmit = {
                if (selectedWsId.isNotBlank() && taskTitle.isNotBlank()) {
                    val task = ProductionTask(
                        id = java.util.UUID.randomUUID().toString(),
                        workspaceId = selectedWsId,
                        creatorId = userId,
                        title = taskTitle,
                        contentBody = taskDesc.ifBlank { "Production Deliverable Task" },
                        stateScope = "PRODUCTION_READY",
                        kanbanLane = "TODO",
                        createdAt = System.currentTimeMillis()
                    )
                    globalViewModel.insertTask(task)
                    showCreateTask = false
                    FeedbackManager.showSuccess("Task enqueued to workspace pipeline successfully!")
                }
            },
            submitEnabled = selectedWsId.isNotBlank() && taskTitle.isNotBlank()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (workspaces.isEmpty()) {
                    Text("No active workspace nodes found. Establish a workspace first.", color = AccentRed, fontSize = 12.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Select Target Workspace", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            workspaces.forEach { ws ->
                                val isSelected = selectedWsId == ws.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedWsId = ws.id },
                                    label = { Text(ws.name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AccentRed.copy(alpha = 0.2f),
                                        selectedLabelColor = Color.White,
                                        labelColor = TextSecondary
                                    )
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Task Title", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = taskTitle,
                            onValueChange = { taskTitle = it },
                            placeholder = { Text("e.g. Edit Weekly Vlog Cut", color = TextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = AccentRed,
                                unfocusedBorderColor = ColorDivider
                            )
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Content / Deliverable Scope", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = taskDesc,
                            onValueChange = { taskDesc = it },
                            placeholder = { Text("Describe editing parameters, asset links, or export settings...", color = TextMuted) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = AccentRed,
                                unfocusedBorderColor = ColorDivider
                            )
                        )
                    }
                }
            }
        }
    }
}

// Data class representation for speed dial actions
data class SpeedDialActionItem(
    val label: String,
    val icon: ImageVector,
    val tint: Color,
    val onClick: () -> Unit
)

// Premium reusable dialog layout styled like Linear / Notion dark console
@Composable
fun PremiumCreationDialog(
    title: String,
    accentColor: Color,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    submitEnabled: Boolean,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .border(1.dp, ColorDivider, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header with glowing accent badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = accentColor,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "CREATOR CO-OP OPERATIONS MODULE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 0.8.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Dialog",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Inner content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    content()
                }

                // Action Footer Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text(
                            text = "DISMISS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onSubmit,
                        enabled = submitEnabled,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = if (accentColor == CrispAmber) Color.Black else Color.White,
                            disabledContainerColor = ColorDivider,
                            disabledContentColor = TextMuted
                        ),
                        modifier = Modifier
                            .height(44.dp)
                            .padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "DEPLOY ARTIFACT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }
        }
    }
}
