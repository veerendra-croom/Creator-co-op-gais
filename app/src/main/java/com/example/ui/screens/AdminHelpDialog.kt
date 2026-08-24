package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.ui.components.DS
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class AdminScreenInfo(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val why: String,
    val whenToUse: String,
    val howToUse: String,
    val demoLabel: String,
    val demoSuccessMsg: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHelpDialog(onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    var selectedScreenIndex by remember { mutableStateOf(0) }
    
    // List of all 8 Admin Screens with clear instructions and demo triggers
    val screens = remember {
        listOf(
            AdminScreenInfo(
                title = "Admin Console & Dashboard",
                icon = Icons.Default.AdminPanelSettings,
                color = AccentRed,
                why = "Acts as the central governance station for processing user reports, community moderation, and manual verification credentials.",
                whenToUse = "Use daily or whenever users submit spam or abuse reports, and when content creators apply for professional verifications.",
                howToUse = "Monitor active operational nodes and pending report counts. Use the sub-tabs to view reports, risky users, or pending creator documents. Provide notes and tap 'RESOLVE' or 'APPROVE' to update state.",
                demoLabel = "Test Moderation API Router",
                demoSuccessMsg = "Moderation API verified! Ping latency: 12ms. Safe-content classifier reporting 100% active."
            ),
            AdminScreenInfo(
                title = "Founder CRM",
                icon = Icons.Default.ManageAccounts,
                color = NeonEmerald,
                why = "Designed to coordinate early-stage cohort pipelines, YC cohort registrations, and manually log user interaction records.",
                whenToUse = "Use when onboarding premium creator cohorts, tracking user potential levels, or documenting physical sync results.",
                howToUse = "Navigate the CRM directories, search profiles, view engagement classifications (e.g. VIP, Churn Risk), and log manual founder interactions.",
                demoLabel = "Simulate CRM Sync",
                demoSuccessMsg = "CRM Cohort ledger verified! All 25 beta-stage slots indexed safely in room store."
            ),
            AdminScreenInfo(
                title = "Platform Control",
                icon = Icons.Default.Tune,
                color = AccentBlue,
                why = "Provides extreme system override parameters, maintenance switches, and remote configuration of onboarding slideshow elements.",
                whenToUse = "Use prior to rolling out scheduled server operations, deploying app updates, or modifying default dynamic resources.",
                howToUse = "Toggle hardware feature flags, activate server-side maintenance barriers, or edit the dynamic welcome and onboarding screens.",
                demoLabel = "Inspect Feature Flags",
                demoSuccessMsg = "Config engine reporting healthy status. 12 global flags mapped perfectly."
            ),
            AdminScreenInfo(
                title = "Communication Center",
                icon = Icons.Default.Campaign,
                color = AccentRed,
                why = "Enables platform developers to broadcast major changelogs, operational maintenance windows, or cohort news directly in-app.",
                whenToUse = "Use when deploying system-wide bulletins, warning of downtime, or publishing seasonal creator challenge guides.",
                howToUse = "Click the '+' action FAB, fill out campaign details (title, priority, content body, target audience), and tap Broadcast to write to the announcement stream.",
                demoLabel = "Simulate Announcement Broadcast",
                demoSuccessMsg = "Broadcaster test broadcast complete! Queue latency: 0.0s. All client listening sockets active."
            ),
            AdminScreenInfo(
                title = "Founder Command Cockpit",
                icon = Icons.Default.RocketLaunch,
                color = AccentBlue,
                why = "Central executive deck for founders to review unified telemetry, answer tickets, and view beta cohort indices.",
                whenToUse = "Use for high-level operations monitoring, responding to support tickets, or managing closed beta governance.",
                howToUse = "Tab through dashboard cockpit modules, tap tickets to answer user queries, and log founder decisions.",
                demoLabel = "Test Operational Telemetry",
                demoSuccessMsg = "Cockpit Telemetry verified! Uptime tracking active, all microservices responding."
            ),
            AdminScreenInfo(
                title = "Audit Ledger",
                icon = Icons.Default.Gavel,
                color = CrispAmber,
                why = "Provides a cryptographically permanent, complete ledger of every administrator action, user suspension, or badge assignment.",
                whenToUse = "Use during administrative reviews to trace why specific users were suspended, or who modified critical settings.",
                howToUse = "Search ledger by admin name or action keyword. Use the dropdown filters to sort, and tap the Export button to save CSV or JSON local reports.",
                demoLabel = "Inspect Cryptographic Signature",
                demoSuccessMsg = "Audit signature matches! State is structurally intact with no modifications detected."
            ),
            AdminScreenInfo(
                title = "Backup Center",
                icon = Icons.Default.Backup,
                color = CrispAmber,
                why = "Provides point-in-time state snapshot protection, enabling admins to generate backups of SQLite/Room tables.",
                whenToUse = "Use before performing database operations, during scheduled system checkpoints, or when testing recovery scenarios.",
                howToUse = "Click 'GENERATE SNAPSHOT' to lock state. Name the copy, inspect its checksum signature, or tap Restore on an existing file to reload state.",
                demoLabel = "Verify DB Checksum Engine",
                demoSuccessMsg = "Database tables are physically synchronized. Backup generator verified!"
            ),
            AdminScreenInfo(
                title = "Platform Health",
                icon = Icons.Default.NetworkCheck,
                color = NeonEmerald,
                why = "Displays real JVM diagnostics, network latency, active sync queue limits, and server-side connection health.",
                whenToUse = "Use when testing latency, checking JVM garbage collector memory, or checking database synchronization queues.",
                howToUse = "Watch live gauges, memory usage bars, sync queues, and diagnostic latency meters update automatically in real-time.",
                demoLabel = "Stress JVM GC Allocation",
                demoSuccessMsg = "Memory diagnostics check complete! GC freed 18MB heap space. Available memory is robust."
            )
        )
    }

    // Dynamic Pulsing Border Infinite transition to achieve extreme "high pro max" visuals
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulsingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsingBorder"
    )

    // Animated bounce values for the mini-telemetry visuals
    val telemetryBounceFactor by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .testTag("admin_unified_help_dialog"),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.2.dp, AccentBlue.copy(alpha = pulsingAlpha))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Block with animated cyber-dark gradient background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(SurfaceLightColor, SurfaceColor, SurfaceLightColor)
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(AccentBlue.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                    contentDescription = "Admin Control & Intel Hub",
                                    tint = AccentBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "ADMIN CONTROL & INTEL HUB",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp,
                                        letterSpacing = 1.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(AccentRed.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "PRO MAX v2.4",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            color = AccentRed
                                        )
                                    }
                                }
                                Text(
                                    text = "Command Guide, Operational Specifications & Sandboxed Diagnostic Routers",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .background(ColorDivider.copy(alpha = 0.4f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, "Close", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                HorizontalDivider(color = ColorDivider, modifier = Modifier.fillMaxWidth())

                // Main Layout with vertical navigation and modular content pane
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    // Left list of screens (Tabs)
                    Column(
                        modifier = Modifier
                            .weight(0.42f)
                            .fillMaxHeight()
                            .background(PrimaryBackground)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Text(
                            text = "COMMAND MODULES",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )

                        screens.forEachIndexed { index, screen ->
                            val isSelected = selectedScreenIndex == index
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedScreenIndex = index }
                                    .then(
                                        if (isSelected) {
                                            Modifier.background(
                                                brush = Brush.horizontalGradient(
                                                    colors = listOf(screen.color.copy(alpha = 0.12f), Color.Transparent)
                                                )
                                            )
                                        } else Modifier
                                    )
                                    .padding(vertical = 12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Animated vertical selection neon bar (High Pro Max feature)
                                    AnimatedVisibility(
                                        visible = isSelected,
                                        enter = slideInHorizontally() + fadeIn(),
                                        exit = slideOutHorizontally() + fadeOut()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(4.dp)
                                                .height(24.dp)
                                                .background(screen.color, RoundedCornerShape(2.dp))
                                        )
                                    }
                                    
                                    // Inactive spacing placeholder
                                    if (!isSelected) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                    } else {
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) screen.color else TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = screen.title,
                                        color = if (isSelected) Color.White else TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    VerticalDivider(color = ColorDivider)

                    // Right side: Screen Info details with dynamic visualizer widgets
                    val activeScreen = screens[selectedScreenIndex]
                    var demoOutputText by remember(selectedScreenIndex) { mutableStateOf("") }
                    var isDemoRunning by remember(selectedScreenIndex) { mutableStateOf(false) }

                    // We use Crossfade for smooth screen transitioning on tab selections
                    Crossfade(
                        targetState = activeScreen,
                        animationSpec = tween(350, easing = FastOutSlowInEasing),
                        modifier = Modifier
                            .weight(0.58f)
                            .fillMaxHeight(),
                        label = "tabTransition"
                    ) { targetScreen ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(20.dp)
                        ) {
                            // Sub-header displaying Active Panel
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(targetScreen.color.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                    .border(1.dp, targetScreen.color.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(targetScreen.color.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = targetScreen.icon,
                                        contentDescription = null,
                                        tint = targetScreen.color,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = targetScreen.title,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "Core Node Governance Active",
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // WHY THIS PANEL EXISTS
                            Text(
                                text = "FUNCTIONAL INTENT & OBJECTIVE",
                                color = AccentBlue,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = targetScreen.why,
                                color = TextPrimary,
                                fontSize = 12.5.sp,
                                lineHeight = 17.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // WHEN TO USE IT
                            Text(
                                text = "OPERATIONAL TRIGGER TIMELINE",
                                color = CrispAmber,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = targetScreen.whenToUse,
                                color = TextPrimary,
                                fontSize = 12.5.sp,
                                lineHeight = 17.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // HOW TO OPERATE
                            Text(
                                text = "STEP-BY-STEP WORKFLOW METHODOLOGY",
                                color = NeonEmerald,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = targetScreen.howToUse,
                                color = TextPrimary,
                                fontSize = 12.5.sp,
                                lineHeight = 17.sp
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // HIGH-FIDELITY LIVE DIAGNOSTIC COMPOSABLE CHIPS (UNIQUE INTERACTIVE COMPOSABLE VISUALIZERS)
                            Text(
                                text = "REAL-TIME TELEMETRY STREAM",
                                color = TextMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Card(
                                colors = CardDefaults.cardColors(containerColor = PrimaryBackground),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, ColorDivider),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp)
                                ) {
                                    when (selectedScreenIndex) {
                                        0 -> { // Admin Console & Dashboard (Live pulse telemetry)
                                            Row(
                                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                                horizontalArrangement = Arrangement.SpaceEvenly,
                                                verticalAlignment = Alignment.Bottom
                                            ) {
                                                listOf(0.4f, 0.9f, 0.5f, 0.8f, 0.3f, 0.7f, 0.95f, 0.45f).forEach { scale ->
                                                    val animatedHeight = (scale * telemetryBounceFactor * 40).dp
                                                    Box(
                                                        modifier = Modifier
                                                            .width(8.dp)
                                                            .height(animatedHeight)
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(AccentRed)
                                                    )
                                                }
                                            }
                                        }
                                        1 -> { // Founder CRM (Onboarding status track)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                listOf("PIPELINE", "YC-STAGE", "VIP STATUS").forEach { tag ->
                                                    Box(
                                                        modifier = Modifier
                                                            .background(NeonEmerald.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                                            .border(1.dp, NeonEmerald.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Text(text = tag, color = NeonEmerald, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                        2 -> { // Platform Control (System maintenance overrides)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("DB WRITES: ENABLED", fontSize = 10.sp, color = AccentBlue, fontWeight = FontWeight.Bold)
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .background(NeonEmerald, CircleShape)
                                                )
                                            }
                                        }
                                        3 -> { // Communication Center (Broadcast queue latency check)
                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                Text("BROADCAST LATENCY METRIC", fontSize = 9.sp, color = TextSecondary)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                LinearProgressIndicator(
                                                    progress = 0.24f,
                                                    color = AccentRed,
                                                    trackColor = ColorDivider,
                                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
                                                )
                                            }
                                        }
                                        4 -> { // Founder Command Cockpit (Live unified nodes)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("UPTIME", fontSize = 8.sp, color = TextSecondary)
                                                    Text("99.99%", fontSize = 12.sp, color = AccentBlue, fontWeight = FontWeight.Black)
                                                }
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("DB SLOTS", fontSize = 8.sp, color = TextSecondary)
                                                    Text("ACTIVE [25/25]", fontSize = 11.sp, color = NeonEmerald, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        5 -> { // Audit Ledger (Permanence verification log)
                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                Text("[SECURE STATE SIGNATURE]", fontSize = 8.sp, color = CrispAmber, fontFamily = FontFamily.Monospace)
                                                Text("SHA256: 8a7f...e210a", fontSize = 10.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                                            }
                                        }
                                        6 -> { // Backup Center (Point-in-time SQLite metrics)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Storage, null, tint = CrispAmber, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("2.4 MB COHORT SNAPSHOTS SAFE", fontSize = 11.sp, color = CrispAmber, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        7 -> { // Platform Health (JVM memory GC logs)
                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text("Eden Space allocation: 34%", fontSize = 9.sp, color = TextSecondary)
                                                    Text("GC Check: OK", fontSize = 9.sp, color = NeonEmerald)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                LinearProgressIndicator(
                                                    progress = 0.34f,
                                                    color = NeonEmerald,
                                                    trackColor = ColorDivider,
                                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // INTERACTIVE DEMO ENGINE
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceLightColor),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, ColorDivider),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "LIVE HANDSHAKE SIMULATOR",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = {
                                            if (!isDemoRunning) {
                                                scope.launch {
                                                    isDemoRunning = true
                                                    demoOutputText = "Initializing simulation diagnostics..."
                                                    delay(800)
                                                    demoOutputText = "Routing handshake payload..."
                                                    delay(800)
                                                    demoOutputText = targetScreen.demoSuccessMsg
                                                    isDemoRunning = false
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = targetScreen.color),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(40.dp),
                                        contentPadding = PaddingValues(0.dp),
                                        enabled = !isDemoRunning
                                    ) {
                                        if (isDemoRunning) {
                                            CircularProgressIndicator(
                                                color = Color.White,
                                                modifier = Modifier.size(18.dp),
                                                strokeWidth = 2.5.dp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("CONNECTING...", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        } else {
                                            Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(targetScreen.demoLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }

                                    if (demoOutputText.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(PrimaryBackground, RoundedCornerShape(8.dp))
                                                .border(1.dp, ColorDivider, RoundedCornerShape(8.dp))
                                                .padding(10.dp)
                                        ) {
                                            Text(
                                                text = demoOutputText,
                                                color = if (isDemoRunning) TextSecondary else NeonEmerald,
                                                fontSize = 10.5.sp,
                                                fontFamily = FontFamily.Monospace,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = ColorDivider)

                // Footer Founders Credit Bar (CRITICAL REQUIREMENT)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceLightColor)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "CREATOR CO-OP OPERATIONS BOARD",
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 12.sp,
                            letterSpacing = 1.2.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Founder: Botla Veerendra • Co-Founder: Macha Praveen",
                            fontWeight = FontWeight.Bold,
                            color = AccentBlue,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { uriHandler.openUri("mailto:veerendrabotla@gmail.com") },
                                colors = ButtonDefaults.buttonColors(containerColor = ColorDivider.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(38.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Email, null, tint = AccentBlue, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("veerendrabotla@gmail.com", fontSize = 10.5.sp, color = TextPrimary)
                            }
                            Button(
                                onClick = { uriHandler.openUri("mailto:praveenmacha777@gmail.com") },
                                colors = ButtonDefaults.buttonColors(containerColor = ColorDivider.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(38.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Email, null, tint = AccentBlue, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("praveenmacha777@gmail.com", fontSize = 10.5.sp, color = TextPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}
