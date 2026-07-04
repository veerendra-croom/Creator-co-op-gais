package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.window.Dialog
import com.example.ui.feedback.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.collectAsState
import com.example.data.model.ProjectProposalWithData
import com.example.data.model.Post
import com.example.data.model.SyncState
import com.example.data.model.Workspace
import com.example.ui.theme.*
import com.example.ui.components.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.builtins.ListSerializer
import java.text.SimpleDateFormat
import java.util.*

import com.example.ui.viewmodels.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    globalViewModel: GlobalViewModel,
    authViewModel: AuthViewModel,
    adminViewModel: AdminViewModel,
    adManagementViewModel: AdManagementViewModel,
    userProfile: com.example.data.model.UserProfile?,
    modifier: Modifier = Modifier
) {
    val isAdmin = userProfile?.systemRole == "ADMIN"
    var showAdmin by remember { mutableStateOf(false) }
    var showAdManagement by remember { mutableStateOf(false) }
    var showLegal by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showEditProfile by remember { mutableStateOf(false) }
    var showCommunityGuidelines by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var showLicenses by remember { mutableStateOf(false) }
    var showPaywall by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    val exportedData by globalViewModel.exportedData.collectAsState()

    if (exportedData != null) {
        AlertDialog(
            onDismissRequest = { globalViewModel.exportedData.value = null },
            containerColor = SurfaceColor,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Your Data Export (JSON)", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Box(modifier = Modifier.height(300.dp).verticalScroll(rememberScrollState())) {
                    Text(exportedData!!, color = TextPrimary, fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                }
            },
            confirmButton = {
                TextButton(onClick = { globalViewModel.exportedData.value = null }) {
                    Text("CLOSE", fontWeight = FontWeight.Bold, color = AccentBlue)
                }
            }
        )
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(userProfile) {
        if (userProfile != null) {
            globalViewModel.generateOrGetReferralCode(userProfile)
            globalViewModel.loadWeeklyDigestSetting(userProfile.id)
        }
    }

    if (showDeleteConfirmation && userProfile != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = {
                Text(
                    text = "Delete Your Account?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Column {
                    Text(
                        text = "This action is permanent and cannot be undone.",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Under GDPR and compliance regulations, confirming deletion will execute the following cascade operations:\n\n" +
                               "• Your personal user profile is permanently deleted.\n" +
                               "• All private drafts and sandbox items are completely wiped.\n" +
                               "• Workspace member links will be deleted. (Leads are auto-resolved to preserve business flow).\n" +
                               "• Forum threads and public chat entries are anonymized under 'Deleted User' to retain group context.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        authViewModel.deleteAccountAndCascade(userProfile.id)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.testTag("confirm_delete_account_button")
                ) {
                    Text("PROCEED", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text("CANCEL", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }

    if (showLegal) {
        LegalScreen(onBack = { showLegal = false })
        BackHandler { showLegal = false }
        return
    }

    if (showCommunityGuidelines) {
        CommunityGuidelinesScreen(onBack = { showCommunityGuidelines = false })
        BackHandler { showCommunityGuidelines = false }
        return
    }

    if (showAddTaskDialog && userProfile != null) {
        val workspaces by globalViewModel.allWorkspaces.collectAsState()
        var taskTitle by remember { mutableStateOf("") }
        var taskDesc by remember { mutableStateOf("") }
        var selectedWsId by remember { mutableStateOf(workspaces.firstOrNull()?.id ?: "") }
        
        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            containerColor = SurfaceColor,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Quick Task Add", color = Color.White, fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Select Target Workspace", color = TextSecondary, fontSize = 12.sp)
                    LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) {
                        items(workspaces) { ws ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedWsId = ws.id }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedWsId == ws.id,
                                    onClick = { selectedWsId = ws.id },
                                    colors = RadioButtonDefaults.colors(selectedColor = AccentRed)
                                )
                                Text(ws.name, color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                    
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Task Title") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    OutlinedTextField(
                        value = taskDesc,
                        onValueChange = { taskDesc = it },
                        label = { Text("Task Details (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val task = com.example.data.model.ProductionTask(
                            id = UUID.randomUUID().toString(),
                            workspaceId = selectedWsId,
                            creatorId = userProfile.id,
                            title = taskTitle,
                            contentBody = taskDesc,
                            stateScope = "PRODUCTION_READY",
                            kanbanLane = "TODO",
                            createdAt = System.currentTimeMillis()
                        )
                        globalViewModel.insertTask(task)
                        showAddTaskDialog = false
                    },
                    enabled = taskTitle.isNotBlank() && selectedWsId.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("CREATE TASK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text("CANCEL", color = TextSecondary)
                }
            }
        )
    }

    if (showAbout) {
        AboutDialog(onDismiss = { showAbout = false })
    }

    if (showLicenses) {
        LicensesDialog(onDismiss = { showLicenses = false })
    }

    if (showPaywall) {
        com.example.ui.components.PaywallDialog(
            onDismiss = { showPaywall = false },
            onPurchaseMonthly = {
                if (userProfile != null) globalViewModel.purchaseProMonthly(userProfile.id)
                showPaywall = false
            },
            onPurchaseAnnual = {
                if (userProfile != null) globalViewModel.purchaseProAnnual(userProfile.id)
                showPaywall = false
            }
        )
    }

    if (showEditProfile && userProfile != null) {
        EditProfileScreen(
            userProfile = userProfile,
            globalViewModel = globalViewModel,
            onBack = { showEditProfile = false }
        )
        BackHandler { showEditProfile = false }
        return
    }

    if (showAdmin) {
        AdminDashboardScreen(
            adminViewModel = adminViewModel,
            authViewModel = authViewModel,
            userProfile = userProfile
        )
        BackHandler { showAdmin = false }
        return
    }

    if (showAdManagement) {
        AdManagementScreen(
            adminId = userProfile?.id ?: "admin",
            viewModel = adManagementViewModel
        )
        BackHandler { showAdManagement = false }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .padding(DS.Space16)
    ) {
        if (userProfile != null) {
            val workspaces by globalViewModel.allWorkspaces.collectAsState()
            val userTasks by globalViewModel.getAllTasksForUser(userProfile.id).collectAsState(initial = emptyList())
            val notifications by globalViewModel.getNotificationsForUser(userProfile.id).collectAsState(initial = emptyList())
            
            val activeWorkspacesCount = workspaces.size
            val pendingTasks = userTasks.filter { it.kanbanLane != "PUBLISH" }
            val unreadNotifsCount = notifications.count { !it.isRead }

            // --- LAYER 1: Context Header ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = DS.Space20, top = DS.Space8),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Welcome Back, ${userProfile.displayName.split(" ").firstOrNull() ?: "Creator"}",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Today's Overview",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { globalViewModel.navigateToTab("SEARCH") }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                    }
                    IconButton(onClick = { globalViewModel.navigateToTab("NOTIFICATIONS") }) {
                        BadgedBox(
                            badge = { 
                                if (unreadNotifsCount > 0) {
                                    Badge(containerColor = AccentRed) { 
                                        Text(
                                            text = unreadNotifsCount.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White
                                        ) 
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.width(DS.Space8))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AccentBlue)
                            .clickable { showEditProfile = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userProfile.displayName.take(1).uppercase(),
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(DS.Space24),
                contentPadding = PaddingValues(bottom = 100.dp),
                modifier = Modifier.weight(1f)
            ) {
                // --- LAYER 2: Metrics (KPI Strip) ---
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DS.Space12)
                    ) {
                        KPICard(
                            title = "Workspaces",
                            value = activeWorkspacesCount.toString(),
                            icon = Icons.Default.Workspaces,
                            tint = AccentBlue,
                            modifier = Modifier.weight(1f)
                        )
                        KPICard(
                            title = "Pending Tasks",
                            value = pendingTasks.size.toString(),
                            icon = Icons.Default.Task,
                            tint = AccentRed,
                            modifier = Modifier.weight(1f)
                        )
                        KPICard(
                            title = "Unread Alerts",
                            value = unreadNotifsCount.toString(),
                            icon = Icons.Default.Forum,
                            tint = NeonEmerald,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // --- NEW: SaaS Creator Identity & Profile Completeness Layer ---
                item {
                    SectionHeader(title = "Identity & Profile Completeness")
                    Spacer(modifier = Modifier.height(DS.Space8))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                        shape = DS.RadiusLarge,
                        border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(DS.Space16)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Completeness Score",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "85% - Advanced Partner Status",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AccentBlue
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(AccentBlue.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "85%",
                                        color = AccentBlue,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(DS.Space12))
                            LinearProgressIndicator(
                                progress = { 0.85f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape),
                                color = AccentBlue,
                                trackColor = ColorDivider.copy(alpha = 0.2f)
                            )
                            Spacer(modifier = Modifier.height(DS.Space12))
                            
                            // Checklist items
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(DS.Space12)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.CheckCircle, null, tint = NeonEmerald, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(DS.Space4))
                                    Text("Verify ID", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.CheckCircle, null, tint = NeonEmerald, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(DS.Space4))
                                    Text("Add Portfolio", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.RadioButtonUnchecked, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(DS.Space4))
                                    Text("Complete Appeal", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                }
                            }
                        }
                    }
                }

                // --- NEW: Portfolio Health & Reputation Tracking ---
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DS.Space12)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                            shape = DS.RadiusMedium,
                            border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(DS.Space12)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.DoneOutline, null, tint = NeonEmerald, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(DS.Space4))
                                    Text("Reliability", style = MaterialTheme.typography.labelMedium, color = TextSecondary, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(DS.Space8))
                                Text("${userProfile.reliabilityBadge} Badge", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Black)
                                Spacer(modifier = Modifier.height(DS.Space2))
                                Text("${userProfile.completedProjectsCount} verified projects", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                            shape = DS.RadiusMedium,
                            border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(DS.Space12)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Shield, null, tint = CrispAmber, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(DS.Space4))
                                    Text("Trust Score", style = MaterialTheme.typography.labelMedium, color = TextSecondary, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(DS.Space8))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(DS.Space8)) {
                                    Text("${userProfile.reputationScore} Score", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Black)
                                    TrustBadge(score = userProfile.reputationScore)
                                }
                                Spacer(modifier = Modifier.height(DS.Space2))
                                Text(userProfile.verificationLevel, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                        }
                    }
                }

                // --- NEW: Workspace Membership Overview ---
                item {
                    SectionHeader(title = "My Workspace Memberships")
                    Spacer(modifier = Modifier.height(DS.Space8))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                        shape = DS.RadiusLarge,
                        border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(DS.Space16)) {
                            // Workspace 1
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Co-Op Video Production Shard", style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text("Joined May 12, 2026", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                }
                                Surface(
                                    color = AccentRed.copy(alpha = 0.15f),
                                    contentColor = AccentRed,
                                    shape = DS.RadiusSmall,
                                    border = BorderStroke(1.dp, AccentRed.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = "LEADER / ADMIN",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = DS.Space8, vertical = DS.Space4)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(DS.Space12))
                            HorizontalDivider(color = ColorDivider.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(DS.Space12))

                            // Workspace 2
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Short Form Syndicate Hub", style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text("Joined June 02, 2026", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                }
                                Surface(
                                    color = AccentBlue.copy(alpha = 0.15f),
                                    contentColor = AccentBlue,
                                    shape = DS.RadiusSmall,
                                    border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = "SPECIALIST",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = DS.Space8, vertical = DS.Space4)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // --- LAYER 3: Priority Tasks ---
                item {
                    SectionHeader(
                        title = "Priority Tasks",
                        action = {
                            TextButton(onClick = { showAddTaskDialog = true }) {
                                Text(
                                    text = "+ Add Task",
                                    color = AccentBlue,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(DS.Space12))
                    val priorityTasks = pendingTasks.take(3)
                    if (priorityTasks.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                            border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.5f)),
                            shape = DS.RadiusMedium
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().padding(DS.Space24), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "No pending tasks. You're all caught up!",
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        for (task in priorityTasks) {
                            val ws = workspaces.find { it.id == task.workspaceId }
                            DashboardTaskItem(
                                task = task, 
                                workspaceName = ws?.name ?: "Personal Task",
                                onClick = { globalViewModel.navigateToTab("TASK_DETAILS") }
                            )
                        }
                    }
                }

                // --- LAYER 4: Recent Workspace Activity ---
                item {
                    SectionHeader(
                        title = "Recent Activity",
                        action = {
                            TextButton(onClick = { globalViewModel.navigateToTab("NOTIFICATIONS") }) {
                                Text(
                                    text = "View Alerts",
                                    color = AccentBlue,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(DS.Space12))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                        border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.5f)),
                        shape = DS.RadiusMedium
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            val recentNotifs = notifications.take(3)
                            if (recentNotifs.isEmpty()) {
                                Box(modifier = Modifier.fillMaxWidth().padding(DS.Space24), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "No recent activity.",
                                        color = TextSecondary,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            } else {
                                for ((index, notif) in recentNotifs.withIndex()) {
                                    ActivityTimelineItem(notif.title, notif.body, notif.createdAt)
                                    if (index < recentNotifs.size - 1) {
                                        HorizontalDivider(
                                            color = ColorDivider.copy(alpha = 0.3f),
                                            modifier = Modifier.padding(start = 56.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // --- SYSTEM 4: Stripe Connect Payouts ---
                item {
                    SectionHeader(title = "Stripe Connect Payouts")
                    Spacer(modifier = Modifier.height(DS.Space8))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                        shape = DS.RadiusLarge,
                        border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(DS.Space16)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Payment & Escrow Readiness",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val statusText = if (userProfile.stripeOnboardingCompleted) {
                                        "Account Linked: ${userProfile.stripeConnectedAccountId}"
                                    } else {
                                        "No Stripe Connected Account Linked"
                                    }
                                    val statusColor = if (userProfile.stripeOnboardingCompleted) NeonEmerald else CrispAmber
                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = statusColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (userProfile.stripeOnboardingCompleted) NeonEmerald.copy(alpha = 0.15f) else CrispAmber.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (userProfile.stripeOnboardingCompleted) Icons.Default.Payments else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (userProfile.stripeOnboardingCompleted) NeonEmerald else CrispAmber,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(DS.Space12))
                            Text(
                                text = "To receive team payouts, escrow distributions, and secure splits, connect your external Stripe payout account.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(DS.Space16))
                            
                            if (!userProfile.stripeOnboardingCompleted) {
                                Button(
                                    onClick = {
                                        val updated = userProfile.copy(
                                            stripeConnectedAccountId = "acct_1M" + (1000..9999).random() + "XP",
                                            stripeOnboardingCompleted = true,
                                            stripeVerificationStatus = "VERIFIED"
                                        )
                                        globalViewModel.updateUserProfile(updated)
                                        FeedbackManager.showSuccess("Stripe account successfully linked!")
                                        globalViewModel.sendNotification(
                                            userId = userProfile.id,
                                            title = "Stripe Connect Onboarded",
                                            body = "Your payout wallet was successfully linked and verified. Account: ${updated.stripeConnectedAccountId}",
                                            type = "INFO"
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth().height(44.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                                    shape = DS.RadiusMedium
                                ) {
                                    Icon(Icons.Default.Link, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(DS.Space8))
                                    Text("Simulate Stripe Account Linking", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(DS.Space12)
                                ) {
                                    Surface(
                                        color = NeonEmerald.copy(alpha = 0.15f),
                                        contentColor = NeonEmerald,
                                        shape = DS.RadiusSmall,
                                        border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.3f)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(DS.Space8))
                                            Text("Identity Verified", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Surface(
                                        color = AccentBlue.copy(alpha = 0.15f),
                                        contentColor = AccentBlue,
                                        shape = DS.RadiusSmall,
                                        border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.3f)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.AccountBalanceWallet, null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(DS.Space8))
                                            Text("Payouts Enabled", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(DS.Space12))
                                TextButton(
                                    onClick = {
                                        val updated = userProfile.copy(
                                            stripeConnectedAccountId = null,
                                            stripeOnboardingCompleted = false,
                                            stripeVerificationStatus = "UNLINKED"
                                        )
                                        globalViewModel.updateUserProfile(updated)
                                        FeedbackManager.showInfo("Stripe account unlinked.")
                                    },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Unlink Stripe Account", color = AccentBlue, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(DS.Space16))
                }

                // --- SYSTEM 1: Reputation Engine V1 Metrics & Simulator ---
                item {
                    SectionHeader(title = "SaaS Reputation Engine Simulator")
                    Spacer(modifier = Modifier.height(DS.Space8))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                        shape = DS.RadiusLarge,
                        border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(DS.Space16)) {
                            Text(
                                text = "Simulate Workspace Contributions",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(DS.Space4))
                            Text(
                                text = "Perform verified actions to dynamically grow your trust score, verification level, and reliability badge status.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(DS.Space16))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(DS.Space8)
                            ) {
                                Button(
                                    onClick = {
                                        val nextCompleted = userProfile.completedProjectsCount + 1
                                        val nextScore = minOf(100, userProfile.reputationScore + 2)
                                        val nextBadge = if (nextScore >= 98) "Platinum" else if (nextScore >= 95) "Gold" else "Silver"
                                        val updated = userProfile.copy(
                                            completedProjectsCount = nextCompleted,
                                            reputationScore = nextScore,
                                            reliabilityBadge = nextBadge
                                        )
                                        globalViewModel.updateUserProfile(updated)
                                        globalViewModel.sendNotification(
                                            userId = userProfile.id,
                                            title = "Workspace Milestone Completed",
                                            body = "Project #$nextCompleted successfully delivered! Reputation Score upgraded to $nextScore ($nextBadge).",
                                            type = "TASKS"
                                        )
                                    },
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceLightColor),
                                    shape = DS.RadiusMedium,
                                    border = BorderStroke(1.dp, ColorDivider)
                                ) {
                                    Icon(Icons.Default.DoneAll, null, tint = AccentRed, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(DS.Space4))
                                    Text("Deliver Project", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        val nextAgreements = userProfile.signedAgreementsCount + 1
                                        val nextScore = minOf(100, userProfile.reputationScore + 1)
                                        val nextBadge = if (nextScore >= 98) "Platinum" else if (nextScore >= 95) "Gold" else "Silver"
                                        val updated = userProfile.copy(
                                            signedAgreementsCount = nextAgreements,
                                            reputationScore = nextScore,
                                            reliabilityBadge = nextBadge
                                        )
                                        globalViewModel.updateUserProfile(updated)
                                        globalViewModel.sendNotification(
                                            userId = userProfile.id,
                                            title = "Agreement Bound",
                                            body = "Cryptographic Agreement #$nextAgreements executed and archived in Agreement Vault.",
                                            type = "AGREEMENTS",
                                            deepLinkTarget = "AGREEMENT_VAULT:ws_youtube_main"
                                        )
                                    },
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceLightColor),
                                    shape = DS.RadiusMedium,
                                    border = BorderStroke(1.dp, ColorDivider)
                                ) {
                                    Icon(Icons.Default.Gavel, null, tint = AccentBlue, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(DS.Space4))
                                    Text("Sign Contract", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(DS.Space24))
                }

                // --- Settings & Admin Actions ---
                item {
                    Text(
                        text = "System Hub",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(DS.Space12))
                    
                    if (isAdmin) {
                        Button(
                            onClick = { showAdmin = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                            shape = DS.RadiusMedium,
                            border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin Settings",
                                tint = AccentBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(DS.Space12))
                            Text(
                                text = "OPEN ADMIN SETTINGS",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(DS.Space12))
                    }
                    
                    Button(
                        onClick = { authViewModel.logout() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("logout_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentRed.copy(alpha = 0.15f),
                            contentColor = AccentRed
                        ),
                        shape = DS.RadiusMedium,
                        border = BorderStroke(1.dp, AccentRed.copy(alpha = 0.3f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Log Out",
                            tint = AccentRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(DS.Space12))
                        Text(
                            text = "LOG OUT",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = AccentRed
                        )
                    }
                }
            }
        }
    }
}



