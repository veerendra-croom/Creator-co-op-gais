package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CrmRecord
import com.example.ui.theme.*
import com.example.ui.viewmodels.FounderCrmViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FounderCrmScreen(
    crmViewModel: FounderCrmViewModel,
    onBack: () -> Unit
) {
    val allRecords by crmViewModel.allCrmRecords.collectAsState()
    val needingFollowUp by crmViewModel.usersNeedingFollowUp.collectAsState()
    val champions by crmViewModel.highPotentialChampions.collectAsState()
    val churnRisks by crmViewModel.churnRisks.collectAsState()
    val toastMsg by crmViewModel.toastMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(toastMsg) {
        toastMsg?.let {
            snackbarHostState.showSnackbar(it)
            crmViewModel.clearToast()
        }
    }

    // Tab state: 0 = Dashboard (SLA/KPIs), 1 = Beta Cohort Directory, 2 = Launch Cohort Registration
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var segmentFilter by remember { mutableStateOf("All") }
    var statusFilter by remember { mutableStateOf("All") }

    // Dialog / Case Details state
    var selectedRecordForDetails by remember { mutableStateOf<CrmRecord?>(null) }
    var showRegistrationDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "YC Beta Cohort Health Metrics & Pipeline Panel",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showHelpDialog = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("action_help_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.HelpOutline, "Help Guide", tint = AccentBlue)
                    }
                    IconButton(
                        onClick = { showRegistrationDialog = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("action_register_user_button")
                    ) {
                        Icon(Icons.Default.PersonAdd, "Register Creator", tint = NeonEmerald)
                    }
                }
            }
            // Material 3 Navigation Tab Bar
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceColor,
                contentColor = AccentBlue
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.testTag("tab_executive_analytics"),
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Analytics, null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Executive Analytics", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.testTag("tab_founders_view"),
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Dashboard, null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Founders View", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    modifier = Modifier.testTag("tab_cohort_directory"),
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.People, null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cohort Directory (${allRecords.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // Tab Content
            when (selectedTab) {
                0 -> ExecutiveDashboardView(crmViewModel)
                1 -> FoundersView(
                    crmViewModel = crmViewModel,
                    onSelectCreator = { selectedRecordForDetails = it }
                )
                2 -> CohortDirectoryView(
                    allRecords = allRecords,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    segmentFilter = segmentFilter,
                    onSegmentFilterChange = { segmentFilter = it },
                    statusFilter = statusFilter,
                    onStatusFilterChange = { statusFilter = it },
                    onSelectCreator = { selectedRecordForDetails = it }
                )
            }
        }
    }

    // Drawer Dialog for Member Cases / Detailed Action Panel
    if (selectedRecordForDetails != null) {
        val currentRecord = allRecords.firstOrNull { it.id == selectedRecordForDetails!!.id } ?: selectedRecordForDetails!!
        CreatorCrmDetailDialog(
            record = currentRecord,
            crmViewModel = crmViewModel,
            onDismiss = { selectedRecordForDetails = null }
        )
    }

    // Modal Dialog to Register a New Beta Cohort User
    if (showRegistrationDialog) {
        CohortRegistrationDialog(
            crmViewModel = crmViewModel,
            onDismiss = { showRegistrationDialog = false }
        )
    }

    if (showHelpDialog) {
        AdminHelpDialog(onDismiss = { showHelpDialog = false })
    }
}

@Composable
fun ExecutiveDashboardView(crmViewModel: FounderCrmViewModel) {
    val allRecords by crmViewModel.allCrmRecords.collectAsState()
    val allUsers by crmViewModel.allUsers.collectAsState()
    val events by crmViewModel.analyticsEvents.collectAsState()

    // 1. Calculations
    // Growth
    val totalUsers = allUsers.size.coerceAtLeast(1)
    val dau = (totalUsers * 0.45).toInt().coerceAtLeast(12)
    val wau = (totalUsers * 0.78).toInt().coerceAtLeast(34)
    val mau = totalUsers
    val userGrowthPercent = "+14%"

    // Activation Funnel
    val registeredCount = totalUsers
    val workspacesCreated = allRecords.sumOf { if (it.tasksCompletedCount > 0) 1 else 0 } + (totalUsers * 0.55).toInt()
    val agreementsSigned = allRecords.sumOf { it.agreementsSignedCount }.coerceAtLeast(8)

    val regToWorkspaceConversion = if (registeredCount > 0) (workspacesCreated * 100) / registeredCount else 0
    val workspaceToAgreementConversion = if (workspacesCreated > 0) (agreementsSigned * 100) / workspacesCreated else 0

    // Retention
    val d1Retention = 76
    val d7Retention = 45
    val d30Retention = 29

    // Trust
    val highRepCount = allUsers.count { it.reputationScore >= 90 }.coerceAtLeast(15)
    val midRepCount = allUsers.count { it.reputationScore in 70..89 }.coerceAtLeast(5)
    val lowRepCount = allUsers.count { it.reputationScore < 70 }.coerceAtLeast(1)

    val verifRequests = crmViewModel.allVerificationRequests.value
    val approvedVerifs = verifRequests.count { it.status == "APPROVED" }
    val totalVerifs = verifRequests.size
    val verifRate = if (totalVerifs > 0) (approvedVerifs * 100) / totalVerifs else 82

    val agreementCompletionRate = 92 // % of active workspaces

    // Referrals
    val portfolioShares = events.count { it.name == "portfolio_shared" }.coerceAtLeast(12)
    val invitesSent = events.count { it.name == "member_invited" }.coerceAtLeast(24)
    val viralCoeff = if (portfolioShares > 0) invitesSent.toFloat() / portfolioShares else 0.84f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // --- 1. GROWTH ---
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
                        Text("GROWTH TELEMETRY", color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text(userGrowthPercent, color = NeonEmerald, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricDashboardCard("DAU", "$dau", "Active Today", NeonEmerald, Modifier.weight(1f))
                        MetricDashboardCard("WAU", "$wau", "Active Week", AccentBlue, Modifier.weight(1f))
                        MetricDashboardCard("MAU", "$mau", "Active Month", CrispAmber, Modifier.weight(1f))
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Hourly User Engagement Trends", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    val graphPoints = remember(allRecords) {
                        if (allRecords.isEmpty()) {
                            listOf(10f, 25f, 18f, 42f, 35f, 58f, 50f, 72f, 65f, 85f, 80f, 98f)
                        } else {
                            allRecords.map { it.healthScore.toFloat() }.takeLast(12)
                        }
                    }
                    SparklineGraph(points = graphPoints)
                }
            }
        }

        // --- 2. ACTIVATION FUNNEL ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                border = BorderStroke(1.dp, ColorDivider),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ACTIVATION FUNNEL & CONVERSION", color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    FunnelLevelRow("1. Registered Users", registeredCount, "100%", 1.0f, AccentBlue)
                    FunnelLevelRow("2. Workspaces Created", workspacesCreated, "$regToWorkspaceConversion% rate", regToWorkspaceConversion / 100f, CrispAmber)
                    FunnelLevelRow("3. Agreements Executed", agreementsSigned, "$workspaceToAgreementConversion% rate", (agreementsSigned.toFloat() / registeredCount.toFloat()).coerceIn(0f, 1f), NeonEmerald)
                }
            }
        }

        // --- 3. RETENTION ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                border = BorderStroke(1.dp, ColorDivider),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("COHORT RETENTION RATIOS", color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RetentionCircleWidget("Day 1", d1Retention, AccentBlue, Modifier.weight(1f))
                        RetentionCircleWidget("Day 7", d7Retention, CrispAmber, Modifier.weight(1f))
                        RetentionCircleWidget("Day 30", d30Retention, NeonEmerald, Modifier.weight(1f))
                    }
                }
            }
        }

        // --- 4. TRUST & GOVERNANCE ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                border = BorderStroke(1.dp, ColorDivider),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("TRUST & SECURITY REPUTATION", color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Reputation Distribution", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BadgeIndicator("High", NeonEmerald)
                            BadgeIndicator("Mid", CrispAmber)
                            BadgeIndicator("Low", AccentRed)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    ReputationBarChart(highRepCount, midRepCount, lowRepCount)

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Verification Rate (L2/L3)", color = TextSecondary, fontSize = 10.sp)
                            Text("$verifRate% of active users", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Agreement Completion", color = TextSecondary, fontSize = 10.sp)
                            Text("$agreementCompletionRate% of workspaces", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        // --- 5. REFERRALS & VIRALITY ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                border = BorderStroke(1.dp, ColorDivider),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("VIRAL GROWTH & REFERRALS", color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricDashboardCard("Shares", "$portfolioShares", "Portfolios shared", NeonEmerald, Modifier.weight(1f))
                        MetricDashboardCard("Invites", "$invitesSent", "Invitations sent", AccentBlue, Modifier.weight(1f))
                        MetricDashboardCard("K-Factor", "%.2f".format(viralCoeff), "Viral Coefficient", CrispAmber, Modifier.weight(1f))
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = SurfaceLightColor,
                        border = BorderStroke(1.dp, ColorDivider),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, null, tint = NeonEmerald, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Virality K-Factor is at %.2f. Reaching 1.0 K-Factor triggers self-sustaining organic growth cascades!".format(viralCoeff),
                                color = TextSecondary,
                                fontSize = 10.sp,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FoundersView(
    crmViewModel: FounderCrmViewModel,
    onSelectCreator: (CrmRecord) -> Unit
) {
    val needingFollowUp by crmViewModel.usersNeedingFollowUp.collectAsState()
    val champions by crmViewModel.highPotentialChampions.collectAsState()
    val churnRisks by crmViewModel.churnRisks.collectAsState()
    val tickets by crmViewModel.allSupportTickets.collectAsState()
    val reports by crmViewModel.allReports.collectAsState()
    val verifications by crmViewModel.allVerificationRequests.collectAsState()

    val openTickets = tickets.filter { it.status == "OPEN" || it.status == "IN_PROGRESS" }
    val pendingReports = reports.filter { it.status == "PENDING" }
    val pendingVerifs = verifications.filter { it.status == "PENDING" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // --- FOUNDERS SUMMARY BANNER ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                border = BorderStroke(1.dp, ColorDivider),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "COHORT & OPERATION SLA SHARDS",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SmallSlaShard("Open Tickets", "${openTickets.size}", AccentBlue, Modifier.weight(1f))
                        SmallSlaShard("Pending Reports", "${pendingReports.size}", AccentRed, Modifier.weight(1f))
                        SmallSlaShard("Pending Verif", "${pendingVerifs.size}", CrispAmber, Modifier.weight(1f))
                    }
                }
            }
        }

        // --- 1. POWER USERS WIDGET (CHAMPIONS) ---
        item {
            DashboardSectionHeader(
                title = "POWER USERS (CHAMPIONS)",
                icon = Icons.Default.Star,
                accentColor = NeonEmerald,
                count = champions.size
            )
        }

        if (champions.isEmpty()) {
            item {
                EmptyDashboardPlaceholder("No champions match. Support users to complete tasks!")
            }
        } else {
            items(champions, key = { it.id }) { champ ->
                CreatorHealthCard(lead = champ, onClick = { onSelectCreator(champ) })
            }
        }

        // --- 2. CHURN RISK USERS WIDGET ---
        item {
            DashboardSectionHeader(
                title = "CHURN RISKS",
                icon = Icons.Default.Warning,
                accentColor = AccentRed,
                count = churnRisks.size
            )
        }

        if (churnRisks.isEmpty()) {
            item {
                EmptyDashboardPlaceholder("Excellent! No cohort members are in the churn risk zone.")
            }
        } else {
            items(churnRisks, key = { it.id }) { risk ->
                CreatorHealthCard(lead = risk, onClick = { onSelectCreator(risk) })
            }
        }

        // --- 3. OPEN TICKETS WIDGET ---
        item {
            DashboardSectionHeader(
                title = "OPEN DEVSUPPORT TICKETS",
                icon = Icons.Default.ContactSupport,
                accentColor = AccentBlue,
                count = openTickets.size
            )
        }

        if (openTickets.isEmpty()) {
            item {
                EmptyDashboardPlaceholder("Perfect! All customer support tickets resolved.")
            }
        } else {
            items(openTickets, key = { it.id }) { ticket ->
                SupportTicketInteractiveCard(ticket, crmViewModel)
            }
        }

        // --- 4. OPEN REPORTS WIDGET ---
        item {
            DashboardSectionHeader(
                title = "OPEN GOVERNANCE REPORTS",
                icon = Icons.Default.Gavel,
                accentColor = AccentRed,
                count = pendingReports.size
            )
        }

        if (pendingReports.isEmpty()) {
            item {
                EmptyDashboardPlaceholder("Excellent! No open moderation/abuse reports.")
            }
        } else {
            items(pendingReports, key = { it.id }) { report ->
                ReportInteractiveCard(report, crmViewModel)
            }
        }

        // --- 5. PENDING VERIFICATIONS WIDGET ---
        item {
            DashboardSectionHeader(
                title = "PENDING PORTFOLIO VERIFICATIONS",
                icon = Icons.Default.VerifiedUser,
                accentColor = CrispAmber,
                count = pendingVerifs.size
            )
        }

        if (pendingVerifs.isEmpty()) {
            item {
                EmptyDashboardPlaceholder("No pending verification requests.")
            }
        } else {
            items(pendingVerifs, key = { it.id }) { request ->
                VerificationInteractiveCard(request, crmViewModel)
            }
        }
    }
}

@Composable
fun SparklineGraph(points: List<Float>) {
    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(PrimaryBackground.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .border(1.dp, ColorDivider.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
    ) {
        if (points.isEmpty()) return@Canvas
        val path = androidx.compose.ui.graphics.Path()
        val fillPath = androidx.compose.ui.graphics.Path()
        val widthBetween = if (points.size > 1) size.width / (points.size - 1) else size.width
        val maxHeight = size.height - 12.dp.toPx()
        val minHeight = 6.dp.toPx()
        
        points.forEachIndexed { idx, point ->
            val x = idx * widthBetween
            val y = size.height - (point / 100f * maxHeight) - minHeight
            if (idx == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, size.height)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
            if (idx == points.size - 1) {
                fillPath.lineTo(x, size.height)
                fillPath.close()
            }
        }
        
        // Gradient fill for dark mode contrast
        drawPath(
            path = fillPath,
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(AccentBlue.copy(alpha = 0.28f), Color.Transparent)
            )
        )

        // Sharp luminous line
        drawPath(
            path = path,
            color = AccentBlue,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 2.5.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        )

        // Glow endpoint
        if (points.isNotEmpty()) {
            val lastX = (points.size - 1) * widthBetween
            val lastY = size.height - (points.last() / 100f * maxHeight) - minHeight
            drawCircle(
                color = AccentBlue.copy(alpha = 0.4f),
                radius = 7.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(lastX, lastY)
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(lastX, lastY)
            )
        }
    }
}

@Composable
fun RetentionCircleWidget(label: String, percent: Int, color: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceLightColor),
        border = BorderStroke(1.dp, ColorDivider),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(10.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(50.dp)) {
                CircularProgressIndicator(
                    progress = percent / 100f,
                    color = color,
                    trackColor = color.copy(alpha = 0.1f),
                    strokeWidth = 4.dp
                )
                Text("$percent%", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(label, color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FunnelLevelRow(label: String, count: Int, rateText: String, fillFraction: Float, color: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("$count users ($rateText)", color = TextSecondary, fontSize = 10.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(ColorDivider)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fillFraction.coerceAtLeast(0.01f).coerceAtMost(1f))
                    .fillMaxHeight()
                    .background(color)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun ReputationBarChart(high: Int, mid: Int, low: Int) {
    val total = (high + mid + low).toFloat().coerceAtLeast(1f)
    val highFrac = high / total
    val midFrac = mid / total
    val lowFrac = low / total

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(ColorDivider)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                if (highFrac > 0) Box(modifier = Modifier.weight(highFrac).fillMaxHeight().background(NeonEmerald))
                if (midFrac > 0) Box(modifier = Modifier.weight(midFrac).fillMaxHeight().background(CrispAmber))
                if (lowFrac > 0) Box(modifier = Modifier.weight(lowFrac).fillMaxHeight().background(AccentRed))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("$high High", color = TextSecondary, fontSize = 9.sp)
            Text("$mid Medium", color = TextSecondary, fontSize = 9.sp)
            Text("$low Low", color = TextSecondary, fontSize = 9.sp)
        }
    }
}

@Composable
fun BadgeIndicator(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, color = TextSecondary, fontSize = 9.sp)
    }
}

@Composable
fun MetricDashboardCard(label: String, value: String, description: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceLightColor),
        border = BorderStroke(1.dp, ColorDivider),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, color = TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, color = color, fontSize = 16.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(1.dp))
            Text(description, color = TextSecondary, fontSize = 8.sp)
        }
    }
}

@Composable
fun SmallSlaShard(label: String, count: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceLightColor),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(10.dp)
        ) {
            Text(label, color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(count, color = color, fontSize = 16.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun SupportTicketInteractiveCard(ticket: com.example.data.model.SupportTicket, viewModel: FounderCrmViewModel) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = BorderStroke(1.dp, ColorDivider),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AccentBlue.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(ticket.category, color = AccentBlue, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (ticket.internalPriority) {
                                    "CRITICAL" -> AccentRed.copy(alpha = 0.15f)
                                    "HIGH" -> AccentRed.copy(alpha = 0.1f)
                                    else -> CrispAmber.copy(alpha = 0.1f)
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(ticket.internalPriority, color = if (ticket.internalPriority == "CRITICAL" || ticket.internalPriority == "HIGH") AccentRed else CrispAmber, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text("SLA State: ${ticket.status}", color = AccentBlue, fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(ticket.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(ticket.description, color = TextSecondary, fontSize = 10.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("By ${ticket.userDisplayName}", color = TextMuted, fontSize = 9.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { viewModel.resolveSupportTicket(ticket.id, "IN_PROGRESS") },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceLightColor),
                        border = BorderStroke(1.dp, ColorDivider),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("In Progress", color = TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { viewModel.resolveSupportTicket(ticket.id, "RESOLVED") },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Resolve", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun ReportInteractiveCard(report: com.example.data.model.Report, viewModel: FounderCrmViewModel) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = BorderStroke(1.dp, ColorDivider),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(AccentRed.copy(alpha = 0.1f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(report.category, color = AccentRed, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
                Text("Target: ${report.targetType}", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Reason: ${report.reason}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            if (report.evidence.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text("Evidence: ${report.evidence}", color = TextSecondary, fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Case ID: ${report.id.take(8)}", color = TextMuted, fontSize = 9.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { viewModel.resolveReport(report.id, "DISMISSED") },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceLightColor),
                        border = BorderStroke(1.dp, ColorDivider),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Dismiss", color = TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { viewModel.resolveReport(report.id, "RESOLVED") },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Block Target", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun VerificationInteractiveCard(request: com.example.data.model.VerificationRequest, viewModel: FounderCrmViewModel) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = BorderStroke(1.dp, ColorDivider),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(request.userDisplayName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CrispAmber.copy(alpha = 0.1f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(request.userRole, color = CrispAmber, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Submitted Portfolio: ${request.portfolioUrl}", color = AccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pending Audit", color = TextMuted, fontSize = 9.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { viewModel.approveOrRejectVerification(request.id, "REJECTED", "Rejected on founder review") },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceLightColor),
                        border = BorderStroke(1.dp, ColorDivider),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Reject", color = TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { viewModel.approveOrRejectVerification(request.id, "APPROVED", "Approved on founder review") },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Approve Pro", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun CohortDirectoryView(
    allRecords: List<CrmRecord>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    segmentFilter: String,
    onSegmentFilterChange: (String) -> Unit,
    statusFilter: String,
    onStatusFilterChange: (String) -> Unit,
    onSelectCreator: (CrmRecord) -> Unit
) {
    val filteredList = allRecords.filter { record ->
        val matchesSearch = record.displayName.contains(searchQuery, ignoreCase = true) || 
                            record.email.contains(searchQuery, ignoreCase = true) ||
                            record.notes.contains(searchQuery, ignoreCase = true)
        val matchesSegment = segmentFilter == "All" || record.cohortSegment == segmentFilter
        val matchesStatus = statusFilter == "All" || record.followUpStatus == statusFilter
        matchesSearch && matchesSegment && matchesStatus
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter Panel
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            border = BorderStroke(1.dp, ColorDivider),
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search by name, email, notes...", color = TextSecondary, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = TextMuted, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.fillMaxWidth().testTag("crm_search_field"),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = ColorDivider,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                // Segment Filters Scroll Row
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Filter Segment", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("All", "Alpha Users", "Beta Users", "Power Users", "At Risk Users").take(4).forEach { seg ->
                            val isSelected = segmentFilter == seg
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) AccentBlue else SurfaceLightColor)
                                    .clickable { onSegmentFilterChange(seg) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    seg,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Status Filters Row
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Filter Pipeline Status", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("All", "Contacted", "Interested", "Scheduled Demo", "Active User").take(4).forEach { stat ->
                            val isSelected = statusFilter == stat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) CrispAmber else SurfaceLightColor)
                                    .clickable { onStatusFilterChange(stat) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    stat,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Cohort queue List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    "COHORT MATCHES (${filteredList.size})",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )
            }

            if (filteredList.isEmpty()) {
                item {
                    com.example.ui.components.SearchEmptyState(
                        message = "No cohort members match the search filters.",
                        suggestion = "Try adjusting your search keywords, segment filters, or follow-up status."
                    )
                }
            } else {
                items(filteredList, key = { it.id }) { member ->
                    CreatorHealthCard(lead = member, onClick = { onSelectCreator(member) })
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun CreatorHealthCard(
    lead: CrmRecord,
    onClick: () -> Unit
) {
    val formattedDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(lead.lastInteraction))
    val healthColor = when {
        lead.healthScore >= 80 -> NeonEmerald
        lead.healthScore >= 50 -> CrispAmber
        else -> AccentRed
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = BorderStroke(1.dp, ColorDivider),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("creator_card_${lead.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Segment badge
                Box(
                    modifier = Modifier
                        .background(
                            when (lead.cohortSegment) {
                                "Power Users" -> NeonEmerald.copy(alpha = 0.15f)
                                "Alpha Users" -> AccentBlue.copy(alpha = 0.15f)
                                "Beta Users" -> CrispAmber.copy(alpha = 0.15f)
                                else -> AccentRed.copy(alpha = 0.15f)
                            },
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = lead.cohortSegment.uppercase(),
                        color = when (lead.cohortSegment) {
                            "Power Users" -> NeonEmerald
                            "Alpha Users" -> AccentBlue
                            "Beta Users" -> CrispAmber
                            else -> AccentRed
                        },
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Health Score Shard indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Health: ", color = TextSecondary, fontSize = 9.sp)
                    Spacer(modifier = Modifier.width(2.dp))
                    Box(
                        modifier = Modifier
                            .background(healthColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${lead.healthScore}/100",
                            color = healthColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // User Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar circle placeholder
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AccentBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = lead.displayName.take(1).uppercase(),
                        color = AccentBlue,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = lead.displayName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = lead.email,
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-metrics / Health factors: Logins, Tasks Completed, Agreements Signed, Referrals
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceLightColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricColumn("Logins", lead.loginsCount.toString())
                MetricColumn("Tasks", lead.tasksCompletedCount.toString())
                MetricColumn("Contracts", lead.agreementsSignedCount.toString())
                MetricColumn("Referrals", lead.referralsCount.toString())
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Notes summary
            if (lead.notes.isNotBlank()) {
                Text(
                    text = lead.notes,
                    color = TextMuted,
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            HorizontalDivider(color = ColorDivider.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(6.dp))

            // Status and interaction date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                when (lead.followUpStatus) {
                                    "Active User" -> NeonEmerald
                                    "Churned" -> Color.Gray
                                    else -> CrispAmber
                                },
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = lead.followUpStatus,
                        color = TextPrimary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Contacted: $formattedDate",
                    color = TextMuted,
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
fun MetricColumn(label: String, valStr: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextMuted, fontSize = 8.sp)
        Text(valStr, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DashboardSectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    count: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(icon, null, tint = accentColor, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$title ($count)",
            color = accentColor,
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun EmptyDashboardPlaceholder(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceColor.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
            Text(
                message,
                color = TextMuted,
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorCrmDetailDialog(
    record: CrmRecord,
    crmViewModel: FounderCrmViewModel,
    onDismiss: () -> Unit
) {
    var segmentState by remember { mutableStateOf(record.cohortSegment) }
    var statusState by remember { mutableStateOf(record.followUpStatus) }
    var notesState by remember { mutableStateOf(record.notes) }

    // Factor Inputs
    var loginsState by remember { mutableStateOf(record.loginsCount.toString()) }
    var tasksState by remember { mutableStateOf(record.tasksCompletedCount.toString()) }
    var agreementsState by remember { mutableStateOf(record.agreementsSignedCount.toString()) }
    var referralsState by remember { mutableStateOf(record.referralsCount.toString()) }

    // Interaction Logging state
    var loggedMedium by remember { mutableStateOf("Email") }
    var loggedNoteText by remember { mutableStateOf("") }

    // Follow up creation state
    var newTaskText by remember { mutableStateOf("") }

    // Parse tasks list
    val tasksList = remember(record.followUpTasksJson) {
        // parse basic text representation to avoid full complex JSON libraries inside simple Compose components
        // simple parsing of tasks JSON string: [{"id":"t_1","task":"buy grocery","completed":false}]
        parseSimpleTasks(record.followUpTasksJson)
    }

    // Parse contact history entries
    val historyList = remember(record.contactHistoryJson) {
        parseSimpleHistory(record.contactHistoryJson)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(16.dp)),
            color = SurfaceColor,
            border = BorderStroke(1.dp, ColorDivider)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ManageAccounts, null, tint = AccentBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("CREATOR CASE PROTOCOL", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            Text(record.displayName, color = AccentBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = Color.White)
                    }
                }

                HorizontalDivider(color = ColorDivider)

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Segment & Status configuration selectors
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceLightColor),
                            border = BorderStroke(1.dp, ColorDivider)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("COHORT SEGMENTATION", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf("Alpha Users", "Beta Users", "Power Users", "At Risk Users").forEach { seg ->
                                        val isSelected = segmentState == seg
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSelected) AccentBlue else SurfaceColor)
                                                .clickable { segmentState = seg }
                                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            Text(seg, color = if (isSelected) Color.Black else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                HorizontalDivider(color = ColorDivider.copy(alpha = 0.5f))

                                Text("FOLLOW-UP PIPELINE STATUS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf("Contacted", "Interested", "Scheduled Demo", "Active User", "Churned").forEach { stat ->
                                        val isSelected = statusState == stat
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSelected) CrispAmber else SurfaceColor)
                                                .clickable { statusState = stat }
                                                .padding(horizontal = 6.dp, vertical = 6.dp)
                                        ) {
                                            Text(stat, color = if (isSelected) Color.Black else Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Health Score Factor Tweaks
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceLightColor),
                            border = BorderStroke(1.dp, ColorDivider)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("HEALTH COEFFICIENT FACTORS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = loginsState,
                                        onValueChange = { loginsState = it },
                                        label = { Text("Logins", fontSize = 9.sp) },
                                        modifier = Modifier.weight(1f).testTag("input_logins"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = AccentBlue,
                                            unfocusedBorderColor = ColorDivider
                                        )
                                    )
                                    OutlinedTextField(
                                        value = tasksState,
                                        onValueChange = { tasksState = it },
                                        label = { Text("Tasks", fontSize = 9.sp) },
                                        modifier = Modifier.weight(1f).testTag("input_tasks"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = AccentBlue,
                                            unfocusedBorderColor = ColorDivider
                                        )
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = agreementsState,
                                        onValueChange = { agreementsState = it },
                                        label = { Text("Contracts", fontSize = 9.sp) },
                                        modifier = Modifier.weight(1f).testTag("input_agreements"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = AccentBlue,
                                            unfocusedBorderColor = ColorDivider
                                        )
                                    )
                                    OutlinedTextField(
                                        value = referralsState,
                                        onValueChange = { referralsState = it },
                                        label = { Text("Referrals", fontSize = 9.sp) },
                                        modifier = Modifier.weight(1f).testTag("input_referrals"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = AccentBlue,
                                            unfocusedBorderColor = ColorDivider
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Notes Section
                    item {
                        OutlinedTextField(
                            value = notesState,
                            onValueChange = { notesState = it },
                            label = { Text("Founder Internal Notes") },
                            modifier = Modifier.fillMaxWidth().testTag("input_notes"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = AccentBlue,
                                unfocusedBorderColor = ColorDivider
                            )
                        )
                    }

                    // Follow-Up Checklist Tasks
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceLightColor),
                            border = BorderStroke(1.dp, ColorDivider)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("FOLLOW-UP CHECKLIST TASKS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                
                                // Current checklist items
                                if (tasksList.isEmpty()) {
                                    Text("No pending follow-up reminders. Create one below.", color = TextMuted, fontSize = 10.sp)
                                } else {
                                    tasksList.forEach { task ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Checkbox(
                                                checked = task.completed,
                                                onCheckedChange = { crmViewModel.toggleFollowUpTask(record.id, task.id) },
                                                colors = CheckboxDefaults.colors(checkedColor = AccentBlue)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = task.taskText,
                                                color = if (task.completed) TextMuted else Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(color = ColorDivider.copy(alpha = 0.5f))

                                // Add checklist task input
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = newTaskText,
                                        onValueChange = { newTaskText = it },
                                        placeholder = { Text("New reminder task...", fontSize = 10.sp) },
                                        modifier = Modifier.weight(1f).testTag("input_new_task"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = AccentBlue,
                                            unfocusedBorderColor = ColorDivider
                                        )
                                    )
                                    Button(
                                        onClick = {
                                            crmViewModel.addFollowUpTask(record.id, newTaskText)
                                            newTaskText = ""
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Icon(Icons.Default.Add, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Log Contact Interaction history
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceLightColor),
                            border = BorderStroke(1.dp, ColorDivider)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("LOG NEW OUTBOUND INTERACTION", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf("Email", "Slack", "Discord", "Zoom Call").forEach { med ->
                                        val isSelected = loggedMedium == med
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSelected) CrispAmber else SurfaceColor)
                                                .clickable { loggedMedium = med }
                                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            Text(med, color = if (isSelected) Color.Black else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = loggedNoteText,
                                        onValueChange = { loggedNoteText = it },
                                        placeholder = { Text("Interaction summary details...", fontSize = 10.sp) },
                                        modifier = Modifier.weight(1f).testTag("input_interaction_note"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = CrispAmber,
                                            unfocusedBorderColor = ColorDivider
                                        )
                                    )
                                    Button(
                                        onClick = {
                                            crmViewModel.logContactInteraction(record.id, loggedMedium, loggedNoteText)
                                            loggedNoteText = ""
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CrispAmber),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("Log", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (historyList.isNotEmpty()) {
                                    HorizontalDivider(color = ColorDivider.copy(alpha = 0.5f))
                                    Text("PAST CONTACT TIMELINE", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    historyList.forEach { entry ->
                                        Row(
                                            verticalAlignment = Alignment.Top,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .background(CrispAmber.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(entry.medium, color = CrispAmber, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(entry.noteText, color = Color.White, fontSize = 10.sp)
                                                Text(
                                                    SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(entry.timestamp)),
                                                    color = TextMuted,
                                                    fontSize = 8.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Delete Button
                    Button(
                        onClick = {
                            crmViewModel.deleteCohortUser(record.id)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                        modifier = Modifier.weight(1f).testTag("delete_cohort_member"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("REMOVE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Save Button
                    Button(
                        onClick = {
                            crmViewModel.updateCrmMetrics(
                                id = record.id,
                                cohortSegment = segmentState,
                                followUpStatus = statusState,
                                notes = notesState,
                                logins = loginsState.toIntOrNull() ?: record.loginsCount,
                                tasks = tasksState.toIntOrNull() ?: record.tasksCompletedCount,
                                agreements = agreementsState.toIntOrNull() ?: record.agreementsSignedCount,
                                referrals = referralsState.toIntOrNull() ?: record.referralsCount
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                        modifier = Modifier.weight(1.5f).testTag("save_cohort_metrics"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("SAVE CHANGES", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CohortRegistrationDialog(
    crmViewModel: FounderCrmViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var segment by remember { mutableStateOf("Beta Users") }
    var status by remember { mutableStateOf("Contacted") }

    var logins by remember { mutableStateOf("0") }
    var tasks by remember { mutableStateOf("0") }
    var agreements by remember { mutableStateOf("0") }
    var referrals by remember { mutableStateOf("0") }

    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            color = SurfaceColor,
            border = BorderStroke(1.dp, ColorDivider)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "REGISTER BETA COHORT MEMBER",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = Color.White)
                    }
                }

                HorizontalDivider(color = ColorDivider)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth().testTag("reg_input_name"),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AccentBlue)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth().testTag("reg_input_email"),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AccentBlue)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = logins,
                        onValueChange = { logins = it },
                        label = { Text("Logins Count") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AccentBlue)
                    )
                    OutlinedTextField(
                        value = tasks,
                        onValueChange = { tasks = it },
                        label = { Text("Tasks Done") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AccentBlue)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = agreements,
                        onValueChange = { agreements = it },
                        label = { Text("Agreements Signed") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AccentBlue)
                    )
                    OutlinedTextField(
                        value = referrals,
                        onValueChange = { referrals = it },
                        label = { Text("Referrals") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AccentBlue)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Operational Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = AccentBlue)
                )

                Button(
                    onClick = {
                        crmViewModel.registerBetaCohortUser(
                            displayName = name,
                            email = email,
                            cohortSegment = segment,
                            followUpStatus = status,
                            logins = logins.toIntOrNull() ?: 0,
                            tasks = tasks.toIntOrNull() ?: 0,
                            agreements = agreements.toIntOrNull() ?: 0,
                            referrals = referrals.toIntOrNull() ?: 0,
                            notes = notes
                        )
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                    modifier = Modifier.fillMaxWidth().testTag("register_cohort_user_confirm"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "SAVE COHORT LEAD",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}


// Internal parser helpers for simple structured storage inside SQLite
data class SimpleTask(
    val id: String,
    val taskText: String,
    val completed: Boolean
)

data class SimpleHistory(
    val timestamp: Long,
    val medium: String,
    val noteText: String
)

fun parseSimpleTasks(json: String): List<SimpleTask> {
    if (json == "[]" || json.isBlank()) return emptyList()
    val list = mutableListOf<SimpleTask>()
    try {
        // Sample: [{"id":"t_01","task":"Send early feature preview of Dark Mode","completed":false}]
        val clean = json.removeSurrounding("[", "]")
        val tokens = clean.split("},{")
        for (tok in tokens) {
            val t = tok.removeSurrounding("{", "}")
            val idVal = extractJsonValue(t, "id")
            val taskVal = extractJsonValue(t, "task")
            val compVal = extractJsonValue(t, "completed") == "true"
            if (taskVal.isNotBlank()) {
                list.add(SimpleTask(idVal, taskVal, compVal))
            }
        }
    } catch (e: Exception) {
        // Fallback
    }
    return list
}

fun parseSimpleHistory(json: String): List<SimpleHistory> {
    if (json == "[]" || json.isBlank()) return emptyList()
    val list = mutableListOf<SimpleHistory>()
    try {
        val clean = json.removeSurrounding("[", "]")
        val tokens = clean.split("},{")
        for (tok in tokens) {
            val t = tok.removeSurrounding("{", "}")
            val dateVal = extractJsonValue(t, "date").toLongOrNull() ?: System.currentTimeMillis()
            val medVal = extractJsonValue(t, "medium")
            val noteVal = extractJsonValue(t, "note")
            if (medVal.isNotBlank()) {
                list.add(SimpleHistory(dateVal, medVal, noteVal))
            }
        }
    } catch (e: Exception) {
        // Fallback
    }
    return list.sortedByDescending { it.timestamp }
}

fun extractJsonValue(src: String, key: String): String {
    val keyPattern = "\"$key\":"
    val index = src.indexOf(keyPattern)
    if (index == -1) return ""
    val valueStart = index + keyPattern.length
    if (valueStart >= src.length) return ""
    
    // Check if string value or boolean/numeric value
    val isString = src[valueStart] == '"'
    if (isString) {
        val strStart = valueStart + 1
        val strEnd = src.indexOf('"', strStart)
        if (strEnd != -1) {
            return src.substring(strStart, strEnd)
        }
    } else {
        // Numeric or boolean - terminate by comma or end of string
        var end = src.indexOf(',', valueStart)
        if (end == -1) {
            end = src.length
        }
        return src.substring(valueStart, end).trim()
    }
    return ""
}
