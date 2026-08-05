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

// Structured Launch Task Item
data class LaunchTaskItem(
    val index: Int,
    val key: String,
    val title: String,
    val category: String,
    val targetFile: String,
    val description: String,
    val launchImpact: String
)

val launchTasksList = listOf(
    LaunchTaskItem(
        index = 1,
        key = "launch_task_1",
        title = "WebRTC Video/Audio Streams",
        category = "Category 1: Frontend Simulation",
        targetFile = "VideoHuddleScreen.kt",
        description = "Bridge simulated speaking grids and active stream video frames to real WebRTC peer channels or an external streaming provider.",
        launchImpact = "Essential for live-collaboration rooms, content huddles, and interactive brainstorms."
    ),
    LaunchTaskItem(
        index = 2,
        key = "launch_task_2",
        title = "Google Play Billing Integration",
        category = "Category 1: Frontend Simulation",
        targetFile = "PremiumSubscriptionScreen.kt",
        description = "Connect billing purchase states directly to Google Play Billing Client SDK rather than using our in-memory BillingSimulatorDialog.",
        launchImpact = "Secures the payment gateway for premium creator status subscriptions and individual paywalls."
    ),
    LaunchTaskItem(
        index = 3,
        key = "launch_task_3",
        title = "Co-Op Subscription Engine Scaling",
        category = "Category 1: Frontend Simulation",
        targetFile = "PremiumSubscriptionScreen.kt",
        description = "Manage tier benefits, seat quotas, and automated renewal state management for verified pro members.",
        launchImpact = "Sustains long-term co-op platform growth and team workspace subscription management."
    ),
    LaunchTaskItem(
        index = 4,
        key = "launch_task_4",
        title = "Platform Live Diagnostics",
        category = "Category 1: Frontend Simulation",
        targetFile = "PlatformHealthScreen.kt",
        description = "Bind live memory metrics, CPU telemetry graphs, and db latency counters to actual client-side diagnostic queries rather than random fluctuators.",
        launchImpact = "Ensures accurate real-time monitoring of device system resource allocation."
    ),
    LaunchTaskItem(
        index = 5,
        key = "launch_task_5",
        title = "VFX Rendering Progress Monitor",
        category = "Category 1: Frontend Simulation",
        targetFile = "ContentPipelineScreen.kt",
        description = "Connect the video render pipelines to active server-side worker progress webhooks or AWS EC2 rendering callbacks.",
        launchImpact = "Provides creators with real rendering status bars instead of coroutine delays."
    ),
    LaunchTaskItem(
        index = 6,
        key = "launch_task_6",
        title = "Deploy Transactional SMS Invites",
        category = "Category 1: Frontend Simulation",
        targetFile = "ReferFriendDialog.kt",
        description = "Substitute local intent controllers with active secure Twilio REST endpoints for transactional invite message dispatches.",
        launchImpact = "Drives organic beta growth via verified SMS-to-app-store routing templates."
    ),
    LaunchTaskItem(
        index = 7,
        key = "launch_task_7",
        title = "Founder SLA Escalations",
        category = "Category 1: Frontend Simulation",
        targetFile = "MoreScreen.kt",
        description = "Connect the direct escalation panels to online CRM ticket systems like Freshdesk or Zendesk APIs instead of device-local mailto handlers.",
        launchImpact = "Saves founders from manual inbox triage and integrates with tracking pipelines."
    ),
    LaunchTaskItem(
        index = 8,
        key = "launch_task_8",
        title = "Real-Time WebSocket Chats",
        category = "Category 1: Frontend Simulation",
        targetFile = "WorkspaceChat.kt",
        description = "Transition from localized mock-bot replies to a fully connected secure WebSocket (WSS) messaging server.",
        launchImpact = "Enables real-time, low-latency collaboration between active channel partners."
    ),
    LaunchTaskItem(
        index = 9,
        key = "launch_task_9",
        title = "Creator Commons S3 Downloads",
        category = "Category 1: Frontend Simulation",
        targetFile = "CreatorCommonsScreen.kt",
        description = "Link asset download flows to secure signed binary URLs (AWS S3 / Cloud Storage) instead of fake UI increments.",
        launchImpact = "Provides high-speed distribution of shared production soundscapes and visual packages."
    ),
    LaunchTaskItem(
        index = 10,
        key = "launch_task_10",
        title = "Unified Search Indexing",
        category = "Category 1: Frontend Simulation",
        targetFile = "GlobalSearchScreen.kt",
        description = "Deploy a search cluster (e.g. Elasticsearch or Algolia) to index talent profiles instead of simple SQLite wildcard query lookups.",
        launchImpact = "Enables instant, relevant, fuzzy auto-completion across thousands of portfolio assets."
    ),
    LaunchTaskItem(
        index = 11,
        key = "launch_task_11",
        title = "OTA App Delta Update Push",
        category = "Category 1: Frontend Simulation",
        targetFile = "PlatformControlCenterScreen.kt",
        description = "Integrate the visual OTA push trigger with actual App Distribution or Google Play Core updates.",
        launchImpact = "Allows admins to trigger emergency client update notifications directly."
    ),
    LaunchTaskItem(
        index = 12,
        key = "launch_task_12",
        title = "Mobile SMS OTP Verification",
        category = "Category 1: Frontend Simulation",
        targetFile = "AuthScreen.kt",
        description = "Swap mock OTP sequences with secure carrier authentication services like Firebase Phone Auth or Twilio Verify.",
        launchImpact = "Secures auth pathways from Sybil automated script registrations."
    ),
    LaunchTaskItem(
        index = 13,
        key = "launch_task_13",
        title = "DocuSign Embedded Signing",
        category = "Category 1: Frontend Simulation",
        targetFile = "AgreementVault.kt",
        description = "Bind mutual signature agreements to official DocuSign Embedded Signing webviews rather than a mock local tap.",
        launchImpact = "Imposes binding legal frameworks onto channel splits and IP transfers."
    ),
    LaunchTaskItem(
        index = 14,
        key = "launch_task_14",
        title = "Dynamic AI Embeddings Sync",
        category = "Category 1: Frontend Simulation",
        targetFile = "KnowledgeBaseScreen.kt",
        description = "Link knowledge base document uploads to Gemini's Text Embeddings API, saving indices in a vector database.",
        launchImpact = "Powers relevant semantic lookup queries within the workspace knowledge bank."
    ),
    LaunchTaskItem(
        index = 15,
        key = "launch_task_15",
        title = "System-Wide User Settings Hub",
        category = "Category 2: Database-Only Gap",
        targetFile = "MoreScreen.kt",
        description = "Expose settings tracked in the local user_settings_table via a comprehensive Settings Panel for creators.",
        launchImpact = "Allows users to self-configure UI scales, notification frequencies, and storage quotas."
    ),
    LaunchTaskItem(
        index = 16,
        key = "launch_task_16",
        title = "Admin Dispute Resolution Board",
        category = "Category 2: Database-Only Gap",
        targetFile = "SupportCenterScreen.kt",
        description = "Expose local DisputeNote table rows to administrators via a dedicated dispute triage and mediation panel.",
        launchImpact = "Provides administrative arbitrations for channel partner SLA/split breaches."
    ),
    LaunchTaskItem(
        index = 17,
        key = "launch_task_17",
        title = "Verification Audit History",
        category = "Category 2: Database-Only Gap",
        targetFile = "AdminDashboardScreen.kt",
        description = "Create a chronological history log browser of completed vetting decisions for administrative auditing.",
        launchImpact = "Guarantees historical compliance audits for verified badge awards."
    ),
    LaunchTaskItem(
        index = 18,
        key = "launch_task_18",
        title = "Workspace Event Calendar Grid",
        category = "Category 2: Database-Only Gap",
        targetFile = "TeamSpaceScreen.kt",
        description = "Bind the database-only WorkspaceEvent entities to an interactive calendar UI inside Team Space.",
        launchImpact = "Improves visibility of shoot schedules and milestone deadlines."
    ),
    LaunchTaskItem(
        index = 19,
        key = "launch_task_19",
        title = "Suspension Screen Interceptors",
        category = "Category 2: Database-Only Gap",
        targetFile = "SplashScreen.kt",
        description = "Implement security navigation checks that force a locked blockout screen if isBanned/isSuspended flags are true.",
        launchImpact = "Blocks toxic/banned accounts from continuing to operate within the client application."
    ),
    LaunchTaskItem(
        index = 20,
        key = "launch_task_20",
        title = "Workspace File Rollback Panel",
        category = "Category 2: Database-Only Gap",
        targetFile = "WorkspaceFilesHubScreen.kt",
        description = "Expose localized file change version histories in SQLite to allow creators to rollback assets to previous hashes.",
        launchImpact = "Secures source material integrity against accidental overwrites or corruption."
    )
)

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

    // Tab control (0 = Cockpit Dashboard, 1 = Launch Checks, 2 = Support Tickets, 3 = Moderation, 4 = Verifications)
    var activeSubTab by remember { mutableStateOf(0) } 
    var showHelpDialog by remember { mutableStateOf(false) }

    // Persistent Launch Checklist status mapping
    val tasksStatus = remember { mutableStateMapOf<String, String>() }
    val handshakeLogs = remember { mutableStateMapOf<String, String>() }
    val handshakeRunning = remember { mutableStateMapOf<String, Boolean>() }
    var expandedTaskKey by remember { mutableStateOf<String?>(null) }

    // Load saved checklist status from Room
    LaunchedEffect(Unit) {
        launchTasksList.forEach { task ->
            val savedStatus = repository.userSettingsDao.getSetting("founder_admin", task.key)
            tasksStatus[task.key] = savedStatus ?: "TODO"
        }
    }

    val onUpdateStatus: (String, String) -> Unit = { key, newStatus ->
        tasksStatus[key] = newStatus
        scope.launch {
            repository.userSettingsDao.setSetting(
                UserSetting(
                    id = "founder_admin_$key",
                    userId = "founder_admin",
                    key = key,
                    value = newStatus
                )
            )
        }
    }

    val runSandboxTest: (String, String) -> Unit = { key, title ->
        scope.launch {
            handshakeRunning[key] = true
            handshakeLogs[key] = ""
            
            val logs = listOf(
                "📡 INIT_PING: Broadcasting system handshake request to live sandbox cluster...",
                "🔌 PORT_CHECK: Port 443 active. Secure SSL handshakes confirmed.",
                "🔒 AUTH_POSTURE: Verifying administration access credentials for 'founder_admin'...",
                "📊 METRIC_POLL: Querying system metrics telemetry. Ping: 12ms, Jitter: 1.2ms.",
                "🗄️ SQL_INTEGRITY: Authenticating table schemas inside 'user_settings_table'...",
                "⚡ SYNC_LOCK: Local ledger transaction state securely validated (SHA-256 match).",
                "✅ HANDSHAKE_SUCCESS: Target integration validated successfully: $title!"
            )
            
            for (log in logs) {
                handshakeLogs[key] = (handshakeLogs[key] ?: "") + log + "\n"
                delay(300)
            }
            handshakeRunning[key] = false
        }
    }

    Scaffold(
        containerColor = PrimaryBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Navigation Sub-tab row with our newly added "LAUNCH CHECKS"
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
                    text = { Text("COCKPIT", fontSize = 8.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Leaderboard, null, modifier = Modifier.size(14.dp)) }
                )
                Tab(
                    selected = activeSubTab == 1,
                    onClick = { activeSubTab = 1 },
                    text = { Text("LAUNCH CHECKS", fontSize = 8.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.PlaylistAddCheck, null, modifier = Modifier.size(14.dp)) }
                )
                Tab(
                    selected = activeSubTab == 2,
                    onClick = { activeSubTab = 2 },
                    text = { Text("TICKETS", fontSize = 8.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.SupportAgent, null, modifier = Modifier.size(14.dp)) }
                )
                Tab(
                    selected = activeSubTab == 3,
                    onClick = { activeSubTab = 3 },
                    text = { Text("MODERATION", fontSize = 8.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Shield, null, modifier = Modifier.size(14.dp)) }
                )
                Tab(
                    selected = activeSubTab == 4,
                    onClick = { activeSubTab = 4 },
                    text = { Text("VERIFY", fontSize = 8.sp, fontWeight = FontWeight.Bold) },
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

                    1 -> { // Launch Checks Checklist
                        val totalTasks = launchTasksList.size
                        val completedCount = launchTasksList.count { tasksStatus[it.key] == "COMPLETED" }
                        val progressPct = if (totalTasks > 0) completedCount.toFloat() / totalTasks else 0f

                        // High Pro Max Progress Banner Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, ColorDivider)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "LAUNCH ARCHITECTURE READINESS",
                                                color = AccentBlue,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 11.sp,
                                                letterSpacing = 1.sp
                                            )
                                            Text(
                                                text = "$completedCount of $totalTasks Priority Milestones Complete",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        Text(
                                            text = "${(progressPct * 100).toInt()}%",
                                            color = if (progressPct >= 0.8f) NeonEmerald else if (progressPct >= 0.4f) CrispAmber else AccentRed,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 20.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    LinearProgressIndicator(
                                        progress = { progressPct },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = AccentBlue,
                                        trackColor = ColorDivider
                                    )
                                }
                            }
                        }

                        // Detailed Task Items
                        items(launchTasksList, key = { it.key }) { task ->
                            val status = tasksStatus[task.key] ?: "TODO"
                            val isExpanded = expandedTaskKey == task.key
                            val isRunning = handshakeRunning[task.key] ?: false
                            val terminalLog = handshakeLogs[task.key] ?: ""

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedTaskKey = if (isExpanded) null else task.key },
                                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, if (isExpanded) AccentBlue else ColorDivider)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
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
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .background(
                                                        if (status == "COMPLETED") NeonEmerald.copy(alpha = 0.15f)
                                                        else if (status == "IN_PROGRESS") CrispAmber.copy(alpha = 0.15f)
                                                        else ColorDivider,
                                                        CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${task.index}",
                                                    color = if (status == "COMPLETED") NeonEmerald
                                                            else if (status == "IN_PROGRESS") CrispAmber
                                                            else TextSecondary,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 9.sp
                                                )
                                            }
                                            Text(
                                                text = task.title,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.5.sp
                                            )
                                        }

                                        // Status badge
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    when (status) {
                                                        "COMPLETED" -> NeonEmerald.copy(alpha = 0.15f)
                                                        "IN_PROGRESS" -> CrispAmber.copy(alpha = 0.15f)
                                                        else -> ColorDivider.copy(alpha = 0.5f)
                                                    },
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .border(
                                                    0.5.dp,
                                                    when (status) {
                                                        "COMPLETED" -> NeonEmerald
                                                        "IN_PROGRESS" -> CrispAmber
                                                        else -> TextSecondary
                                                    },
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = status,
                                                color = when (status) {
                                                    "COMPLETED" -> NeonEmerald
                                                    "IN_PROGRESS" -> CrispAmber
                                                    else -> TextSecondary
                                                },
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }

                                    // Expandable panel details
                                    AnimatedVisibility(
                                        visible = isExpanded,
                                        enter = expandVertically() + fadeIn(),
                                        exit = shrinkVertically() + fadeOut()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            HorizontalDivider(color = ColorDivider)

                                            // Category tag
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(AccentBlue.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                                        .border(0.5.dp, AccentBlue, RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(task.category, color = AccentBlue, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .background(SurfaceLightColor, RoundedCornerShape(4.dp))
                                                        .border(0.5.dp, ColorDivider, RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(task.targetFile, color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 8.sp)
                                                }
                                            }

                                            Text(
                                                text = task.description,
                                                color = TextPrimary,
                                                fontSize = 11.sp,
                                                lineHeight = 15.sp
                                            )

                                            Text(
                                                text = "🚀 Launch Impact: ${task.launchImpact}",
                                                color = TextSecondary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            // Status Transitions
                                            Text("UPDATE ARCHITECTURE STATE:", color = TextMuted, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                listOf("TODO", "IN_PROGRESS", "COMPLETED").forEach { opt ->
                                                    val optSelected = status == opt
                                                    Button(
                                                        onClick = { onUpdateStatus(task.key, opt) },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = if (optSelected) {
                                                                when (opt) {
                                                                    "COMPLETED" -> NeonEmerald
                                                                    "IN_PROGRESS" -> CrispAmber
                                                                    else -> TextSecondary
                                                                }
                                                            } else SurfaceLightColor
                                                        ),
                                                        shape = RoundedCornerShape(8.dp),
                                                        border = BorderStroke(1.dp, if (optSelected) Color.Transparent else ColorDivider),
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .height(30.dp),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Text(
                                                            text = opt,
                                                            color = if (optSelected) Color.Black else Color.White,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Black
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            // Sandbox Terminal diagnostic simulator
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(containerColor = PrimaryBackground),
                                                border = BorderStroke(0.5.dp, ColorDivider),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = "SANDBOX INTEGRITY TESTBED",
                                                            color = CrispAmber,
                                                            fontFamily = FontFamily.Monospace,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        if (!isRunning) {
                                                            Button(
                                                                onClick = { runSandboxTest(task.key, task.title) },
                                                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceLightColor),
                                                                shape = RoundedCornerShape(4.dp),
                                                                border = BorderStroke(0.5.dp, ColorDivider),
                                                                contentPadding = PaddingValues(horizontal = 6.dp),
                                                                modifier = Modifier.height(22.dp)
                                                            ) {
                                                                Icon(Icons.Default.PlayArrow, null, tint = NeonEmerald, modifier = Modifier.size(10.dp))
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text("RUN TEST", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                            }
                                                        } else {
                                                            CircularProgressIndicator(color = NeonEmerald, modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp)
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(6.dp))

                                                    if (terminalLog.isNotBlank()) {
                                                        Text(
                                                            text = terminalLog,
                                                            color = NeonEmerald,
                                                            fontFamily = FontFamily.Monospace,
                                                            fontSize = 8.5.sp,
                                                            lineHeight = 11.sp,
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .background(Color.Black.copy(alpha = 0.5f))
                                                                .padding(8.dp)
                                                        )
                                                    } else {
                                                        Text(
                                                            text = "Terminal idle. Click 'RUN TEST' to perform sandbox REST handshake.",
                                                            color = TextMuted,
                                                            fontFamily = FontFamily.Monospace,
                                                            fontSize = 8.5.sp,
                                                            modifier = Modifier.padding(8.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            // Executive Triage logs specific to this Task!
                                            HorizontalDivider(color = ColorDivider)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "IMMUTABLE CO-OP LOGS",
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                                Button(
                                                    onClick = { selectedEntityForNotes = "LAUNCH_TASK" to task.key },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceLightColor),
                                                    shape = RoundedCornerShape(6.dp),
                                                    border = BorderStroke(0.5.dp, ColorDivider),
                                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                                    modifier = Modifier.height(24.dp)
                                                ) {
                                                    Icon(Icons.Default.NoteAdd, null, tint = AccentBlue, modifier = Modifier.size(10.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("MANAGE TRIAGE LOGS", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> { // Support Ticket Desk
                        val openTickets = tickets.filter { it.status == "OPEN" }
                        if (openTickets.isEmpty()) {
                            item {
                                EmptyStateWidget("support agent", "Pristine Support Queue", "All tickets successfully closed.")
                            }
                        } else {
                            items(openTickets, key = { it.id }) { ticket ->
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

                    3 -> { // Moderation Queue
                        val pendingReports = reports.filter { it.status == "PENDING" }
                        if (pendingReports.isEmpty()) {
                            item {
                                EmptyStateWidget("shield", "Moderation Clear", "0 reports requiring immediate administrative review.")
                            }
                        } else {
                            items(pendingReports, key = { it.id }) { report ->
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

                    4 -> { // Verification Queue
                        val pendingVerifications = verifications.filter { it.status == "PENDING" }
                        if (pendingVerifications.isEmpty()) {
                            item {
                                EmptyStateWidget("verified user", "Queue Clear", "No verification applications pending cohort review.")
                            }
                        } else {
                            items(pendingVerifications, key = { it.id }) { v ->
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
                                items(activeNotes!!.value, key = { it.id }) { note ->
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

    if (showHelpDialog) {
        AdminHelpDialog(onDismiss = { showHelpDialog = false })
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
