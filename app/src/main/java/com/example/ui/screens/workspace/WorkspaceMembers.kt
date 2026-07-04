package com.example.ui.screens.workspace

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodels.WorkspaceViewModel
import com.example.ui.components.*
import com.example.ui.feedback.FeedbackManager

@Composable
fun WorkspaceMembers(
    viewModel: WorkspaceViewModel,
    workspaceId: String,
    workspace: Workspace,
    userId: String
) {
    val members by viewModel.activeWorkspaceMembers.collectAsStateWithLifecycle()
    val myMember = members.find { it.userId == userId }
    val isAdmin = myMember?.assignedRoleTitle in listOf("Lead Creator", "Head") || workspace.createdBy == userId
    var showEndorseDialogByUserId by remember { mutableStateOf<String?>(null) }
    var selectedRoleToChangeByUserId by remember { mutableStateOf<String?>(null) }

    val leadership = members.filter { it.assignedRoleTitle in listOf("Lead Creator", "Head") || workspace.createdBy == it.userId }
    val coreTeam = members.filter { it.assignedRoleTitle in listOf("Video Editor", "VFX Artist", "Senior") && !leadership.contains(it) }
    val specialists = members.filter { !leadership.contains(it) && !coreTeam.contains(it) }

    var showInviteDialog by remember { mutableStateOf(false) }

    if (showInviteDialog) {
        var inviteEmail by remember { mutableStateOf("") }
        var inviteRole by remember { mutableStateOf("Video Editor") }
        
        AlertDialog(
            onDismissRequest = { showInviteDialog = false },
            containerColor = SurfaceColor,
            shape = DS.RadiusLarge,
            title = { Text("Invite Shard Member", color = Color.White, fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(DS.Space16)) {
                    Text("Invite creators to this workspace shard. They must acknowledge IP clauses before joining.", color = TextSecondary, fontSize = 12.sp)
                    OutlinedTextField(
                        value = inviteEmail,
                        onValueChange = { inviteEmail = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Text("Select Primary Role", color = TextSecondary, fontSize = 11.sp)
                    val roles = listOf("Lead Creator", "Video Editor", "VFX Artist", "Scriptwriter", "Specialist")
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        roles.forEach { r ->
                            FilterChip(
                                selected = inviteRole == r,
                                onClick = { inviteRole = r },
                                label = { Text(r) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentBlue.copy(alpha = 0.2f), selectedLabelColor = Color.White)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Simulate sending invite
                        showInviteDialog = false
                        FeedbackManager.showSuccess("Shard invitation dispatched to $inviteEmail")
                    },
                    enabled = inviteEmail.contains("@"),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("DISPATCH INVITE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInviteDialog = false }) {
                    Text("CANCEL", color = TextSecondary)
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground),
        contentPadding = PaddingValues(DS.Space16),
        verticalArrangement = Arrangement.spacedBy(DS.Space16)
    ) {
        if (members.isEmpty()) {
            item {
                EmptyState(
                    title = "No Registered Members",
                    description = "This production shard is currently unstaffed. Dispatch invitations to lead creators or editors to begin collaboration.",
                    icon = Icons.Default.PersonSearch,
                    actionText = "INVITE FIRST MEMBER",
                    onAction = { if (isAdmin) showInviteDialog = true }
                )
            }
        } else {
            item {
            Column(modifier = Modifier.padding(bottom = DS.Space8)) {
                Text(
                    text = "TEAM TOPOLOGY",
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentRed,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Workspace Shard Organogram",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Directing and orchestrating role allocations, private co-op records, and secure communication.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // --- SECTION 1: Leadership ---
        if (leadership.isNotEmpty()) {
            item {
                SectionHeader(title = "Leadership & Shard Owners")
            }
            items(leadership) { member ->
                MemberCard(
                    member = member,
                    currentUserId = userId,
                    workspace = workspace,
                    isAdmin = isAdmin,
                    viewModel = viewModel,
                    score = "99.5/100",
                    joinDate = "Joined May 2026",
                    status = "ACTIVE",
                    statusColor = NeonEmerald,
                    skills = listOf("Strategic Vision", "Contract Enforcement", "Creative Directing"),
                    onEndorse = { showEndorseDialogByUserId = member.userId },
                    onChangeRole = { selectedRoleToChangeByUserId = member.userId }
                )
            }
        }

        // --- SECTION 2: Core Contributors ---
        if (coreTeam.isNotEmpty()) {
            item(key = "header_core") {
                SectionHeader(title = "Core Contributors")
            }
            items(coreTeam, key = { it.userId }) { member ->
                MemberCard(
                    member = member,
                    currentUserId = userId,
                    workspace = workspace,
                    isAdmin = isAdmin,
                    viewModel = viewModel,
                    score = "94.2/100",
                    joinDate = "Joined June 2026",
                    status = "IN PIPELINE",
                    statusColor = AccentBlue,
                    skills = listOf("VFX Compositing", "Dynamic Flow pacing", "Lottie animation"),
                    onEndorse = { showEndorseDialogByUserId = member.userId },
                    onChangeRole = { selectedRoleToChangeByUserId = member.userId }
                )
            }
        }

        // --- SECTION 3: Specialists ---
        if (specialists.isNotEmpty()) {
            item(key = "header_specialists") {
                SectionHeader(title = "Creative Specialists")
            }
            items(specialists, key = { it.userId }) { member ->
                MemberCard(
                    member = member,
                    currentUserId = userId,
                    workspace = workspace,
                    isAdmin = isAdmin,
                    viewModel = viewModel,
                    score = "88.0/100",
                    joinDate = "Joined June 15, 2026",
                    status = "IDLE",
                    statusColor = CrispAmber,
                    skills = listOf("Script blueprinting", "Narrative Hook research", "AI synthesis"),
                    onEndorse = { showEndorseDialogByUserId = member.userId },
                    onChangeRole = { selectedRoleToChangeByUserId = member.userId }
                )
            }
        }

        // --- SECTION 4: Pending Invites ---
        item {
            SectionHeader(
                title = "Awaiting Shard Join Invites",
                action = {
                    if (isAdmin) {
                        TextButton(onClick = { showInviteDialog = true }) {
                            Icon(Icons.Default.PersonAdd, null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(DS.Space4))
                            Text("INVITE", color = AccentBlue, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            )
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                shape = DS.RadiusLarge,
                border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(DS.Space16)) {
                    PendingInviteRow(
                        name = "Maya Lin",
                        role = "3D Asset Designer",
                        date = "Invited 2 days ago",
                        skills = listOf("Blender", "Unreal Engine 5")
                    )
                    Spacer(modifier = Modifier.height(DS.Space12))
                    HorizontalDivider(color = ColorDivider.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(DS.Space12))
                    PendingInviteRow(
                        name = "Thomas Wright",
                        role = "Music/Foley Supervisor",
                        date = "Invited 5 days ago",
                        skills = listOf("Ableton", "Sound synthesis")
                    )
                }
            }
        }

        // --- Actions Footer ---
        item {
            Spacer(modifier = Modifier.height(DS.Space12))
            Button(
                onClick = { viewModel.leaveWorkspace(workspaceId, userId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = DS.RadiusMedium,
                border = BorderStroke(1.dp, AccentRed.copy(alpha = 0.4f))
            ) {
                Text("Leave Workspace Shard", color = AccentRed, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            }
            
            if (isAdmin) {
                Spacer(modifier = Modifier.height(DS.Space12))
                Button(
                    onClick = { viewModel.archiveWorkspace(workspaceId, userId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed.copy(alpha = 0.1f)),
                    shape = DS.RadiusMedium,
                    border = BorderStroke(1.dp, AccentRed)
                ) {
                    Text("Archive Workspace Shard", color = AccentRed, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

    if (showEndorseDialogByUserId != null) {
        val receiverId = showEndorseDialogByUserId!!
        val availableTags = listOf("Reliable", "Fast Turnaround", "Great Communicator", "Creative", "Organized")
        var selectedTags by remember { mutableStateOf(emptySet<String>()) }
        AlertDialog(
            onDismissRequest = { showEndorseDialogByUserId = null },
            title = { Text("Endorse Teammate", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select tags that describe this teammate's work in this completed workspace:", color = TextSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    availableTags.forEach { tag ->
                        val isChecked = tag in selectedTags
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedTags = if (isChecked) selectedTags - tag else selectedTags + tag
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    selectedTags = if (isChecked) selectedTags - tag else selectedTags + tag
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = AccentBlue,
                                    uncheckedColor = TextSecondary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(tag, color = Color.White, fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.endorseTeammate(
                            giverId = userId,
                            receiverId = receiverId,
                            workspaceId = workspaceId,
                            tags = selectedTags.toList()
                        )
                        showEndorseDialogByUserId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Endorse", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndorseDialogByUserId = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceColor
        )
    }

    if (selectedRoleToChangeByUserId != null) {
        val targetUserId = selectedRoleToChangeByUserId!!
        var expandedRoleInput by remember { mutableStateOf("Video Editor") }
        val roles = listOf("Lead Creator", "Video Editor", "VFX Artist", "Senior Supervisor", "Scriptwriter")

        AlertDialog(
            onDismissRequest = { selectedRoleToChangeByUserId = null },
            containerColor = SurfaceColor,
            title = {
                Text("Modify Team Role Permissions", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(DS.Space12)) {
                    Text("Select the target role and compliance authority for this teammate.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    roles.forEach { r ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(DS.RadiusMedium)
                                .background(if (expandedRoleInput == r) AccentBlue.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable { expandedRoleInput = r }
                                .padding(DS.Space12),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = expandedRoleInput == r,
                                onClick = { expandedRoleInput = r },
                                colors = RadioButtonDefaults.colors(selectedColor = AccentBlue)
                            )
                            Spacer(modifier = Modifier.width(DS.Space8))
                            Text(r, color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateMemberRole(workspaceId, targetUserId, expandedRoleInput, userId)
                        selectedRoleToChangeByUserId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("Confirm Role Change", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedRoleToChangeByUserId = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun MemberCard(
    member: WorkspaceMember,
    currentUserId: String,
    workspace: Workspace,
    isAdmin: Boolean,
    viewModel: WorkspaceViewModel,
    score: String,
    joinDate: String,
    status: String,
    statusColor: Color,
    skills: List<String>,
    onEndorse: () -> Unit,
    onChangeRole: () -> Unit
) {
    var cardExpanded by remember { mutableStateOf(false) }
    val sharedDisputeNotes by viewModel.getDisputeNotesAboutUser(member.userId, currentUserId).collectAsStateWithLifecycle(initialValue = emptyList<com.example.data.model.DisputeNote>())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { cardExpanded = !cardExpanded }
            .testTag("member_card_${member.userId}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = DS.RadiusLarge,
        border = BorderStroke(1.dp, if (cardExpanded) AccentBlue else ColorDivider.copy(alpha = 0.5f))
    ) {
        Column {
            Row(
                modifier = Modifier.padding(DS.Space16),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stylish Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(AccentBlue.copy(alpha = 0.12f))
                        .border(1.dp, AccentBlue.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = member.userId.take(2).uppercase(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Black,
                        color = AccentBlue
                    )
                }
                Spacer(modifier = Modifier.width(DS.Space12))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(DS.Space8)) {
                        Text(
                            text = if (member.userId == currentUserId) "You (${member.userId})" else "Team Member: ${member.userId}",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TrustBadge(score = 99) // In real app, derived from member.trustScore
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DS.Space6)
                    ) {
                        Text(
                            text = member.assignedRoleTitle.uppercase(),
                            color = AccentBlue,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Text(
                            text = status,
                            color = statusColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Quick Actions Menu trigger
                if (member.userId != currentUserId) {
                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, null, tint = TextSecondary)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(SurfaceColor)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Direct Message", color = Color.White) },
                                onClick = { showMenu = false /* Slack chat redirect */ }
                            )
                            if (isAdmin) {
                                DropdownMenuItem(
                                    text = { Text("Promote / Change Role", color = Color.White) },
                                    onClick = { onChangeRole(); showMenu = false }
                                )
                                Divider(color = ColorDivider.copy(alpha = 0.4f))
                                DropdownMenuItem(
                                    text = { Text("Remove from Shard", color = AccentRed) },
                                    onClick = { viewModel.removeMember(workspace.id, member.userId, currentUserId); showMenu = false }
                                )
                            }
                        }
                    }
                }
            }

            // Expanded Layer featuring join date, contribution metrics, and skills list
            if (cardExpanded) {
                HorizontalDivider(color = ColorDivider.copy(alpha = 0.3f))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.01f))
                        .padding(DS.Space16),
                    verticalArrangement = Arrangement.spacedBy(DS.Space12)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("CONTRIBUTION SCORE", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.Bold)
                            Text(score, style = MaterialTheme.typography.bodyLarge, color = Color.White, fontWeight = FontWeight.ExtraBold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("JOIN SHARD DATE", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.Bold)
                            Text(joinDate, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                    }

                    Column {
                        Text("MEMBER SPECIALTIES", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(DS.Space4))
                        Row(horizontalArrangement = Arrangement.spacedBy(DS.Space6)) {
                            skills.forEach { skill ->
                                Surface(
                                    color = SurfaceColor,
                                    border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.3f)),
                                    shape = DS.RadiusSmall
                                ) {
                                    Text(
                                        text = skill,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(horizontal = DS.Space8, vertical = DS.Space4)
                                    )
                                }
                            }
                        }
                    }

                    if (workspace.isArchived && member.userId != currentUserId) {
                        Button(
                            onClick = onEndorse,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue.copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.4f)),
                            shape = DS.RadiusMedium
                        ) {
                            Icon(Icons.Default.ThumbUp, null, tint = AccentBlue, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(DS.Space8))
                            Text("Endorse Teammate Compliance", color = AccentBlue, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Private security-enforced dispute records
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(DS.RadiusMedium)
                            .background(PrimaryBackground.copy(alpha = 0.8f))
                            .border(1.dp, ColorDivider.copy(alpha = 0.4f), DS.RadiusMedium)
                            .padding(DS.Space12)
                            .testTag("member_private_disputes_${member.userId}"),
                        verticalArrangement = Arrangement.spacedBy(DS.Space8)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "CO-OP SECURITY RECORD REFERENCE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = AccentBlue
                            )
                        }
                        
                        if (sharedDisputeNotes.isEmpty()) {
                            Text(
                                text = "Zero dispute infractions logged under this creator ID across current or historic team collaborations.",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        } else {
                            for (note in sharedDisputeNotes) {
                                Card(
                                    modifier = Modifier.fillMaxWidth().testTag("shared_dispute_item_${note.id}"),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                    border = BorderStroke(1.dp, ColorDivider),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Text(
                                                text = "Workspace ID: ${note.workspaceId}",
                                                fontSize = 9.sp,
                                                color = AccentBlue,
                                                fontWeight = FontWeight.Bold
                                            )
                                            val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(note.createdAt))
                                            Text(text = date, fontSize = 9.sp, color = TextSecondary)
                                        }
                                        Text(
                                            text = note.noteText,
                                            fontSize = 12.sp,
                                            color = Color.White
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
}

@Composable
fun PendingInviteRow(
    name: String,
    role: String,
    date: String,
    skills: List<String>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(ColorDivider.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.HourglassEmpty, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(DS.Space12))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DS.Space6)
            ) {
                Text(role.uppercase(), style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.Bold)
                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(TextSecondary))
                Text(date, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }
        Surface(
            color = CrispAmber.copy(alpha = 0.15f),
            contentColor = CrispAmber,
            shape = DS.RadiusSmall,
            border = BorderStroke(1.dp, CrispAmber.copy(alpha = 0.3f))
        ) {
            Text(
                text = "PENDING",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(horizontal = DS.Space8, vertical = DS.Space4)
            )
        }
    }
}
