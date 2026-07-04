package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Report
import com.example.data.model.UserProfile
import com.example.data.model.VerificationRequest
import com.example.data.model.UserAuditLog
import com.example.ui.theme.*
import com.example.ui.viewmodels.*

@Composable
fun AdminDashboardScreen(
    adminViewModel: AdminViewModel,
    authViewModel: AuthViewModel,
    userProfile: UserProfile?
) {
    val nodeCount by adminViewModel.activeNodeCount.collectAsState()
    val queryFreq by adminViewModel.queryFrequency.collectAsState()
    val reports by adminViewModel.pendingReports.collectAsState()
    val allReports by adminViewModel.allReports.collectAsState()
    val allConnectionRequests by adminViewModel.allConnectionRequests.collectAsState()
    val userCount by adminViewModel.userCount.collectAsState()
    val workspaceCount by adminViewModel.workspaceCount.collectAsState()
    val postCount by adminViewModel.postCount.collectAsState()
    val logs by adminViewModel.allAuditLogs.collectAsState()

    // New management statescollected from flow
    val users by adminViewModel.allUsers.collectAsState()
    val verificationRequests by adminViewModel.allVerificationRequests.collectAsState()
    val userLogs by adminViewModel.userAuditLogs.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0 = Telemetry & Reports, 1 = User Management, 2 = Creator Verification

    var selectedReportForAction by remember { mutableStateOf<Report?>(null) }
    var resolutionReason by remember { mutableStateOf("") }
    var showFeatureFlags by remember { mutableStateOf(false) }
    var showSponsorships by remember { mutableStateOf(false) }

    // User management UI states
    var searchKeyword by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf("ALL") }
    var selectedVerificationFilter by remember { mutableStateOf("ALL") }
    var selectedTrustFilter by remember { mutableStateOf("ALL") }
    var selectedUserForDetail by remember { mutableStateOf<UserProfile?>(null) }

    // Verification Center UI states
    var selectedRequestForReview by remember { mutableStateOf<VerificationRequest?>(null) }
    var verificationNotes by remember { mutableStateOf("") }

    // Trust & Safety Command Center states
    var modSubTab by remember { mutableStateOf(0) } // 0 = Open Reports, 1 = Resolved/Dismissed Reports, 2 = Risky Users
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var selectedRiskFilter by remember { mutableStateOf("All") }

    val adminId = authViewModel.currentUserId.value ?: "admin"

    LaunchedEffect(Unit) {
        while (true) {
            adminViewModel.updateTelemetry()
            kotlinx.coroutines.delay(5000)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(PrimaryBackground).padding(16.dp)) {
        // Upper Title block
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("FOUNDER DASHBOARD", fontWeight = FontWeight.Black, color = Color.White, fontSize = 16.sp, letterSpacing = 1.sp)
                Text("Enterprise System Operations & Governance", color = TextSecondary, fontSize = 11.sp)
            }
            
            Row {
                Button(
                    onClick = { showFeatureFlags = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("open_feature_flags_button").padding(end = 6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.SettingsInputComponent, null, tint = AccentBlue, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("FLAGS", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showSponsorships = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("open_sponsorships_button"),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.CardGiftcard, null, tint = NeonEmerald, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SPONSORS", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Tabs (Telemetry, User Management, Creator Verification)
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = SurfaceColor,
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = AccentBlue
                )
            },
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("REPORTS & HEALTH", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Assessment, null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("USER MANAGER", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.SupervisedUserCircle, null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = activeTab == 2,
                onClick = { activeTab = 2 },
                text = { Text("VERIFICATION Q", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Rule, null, modifier = Modifier.size(18.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content Area based on tab
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (activeTab) {
                0 -> {
                    // TRUST & SAFETY MODERATION COMMAND CENTER
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Summary Metrics Cards Row
                        val openCount = allReports.count { it.status == "PENDING" }
                        val resolvedCount = allReports.count { it.status != "PENDING" }
                        val calculatedRiskyUsers = users.map { u ->
                            calculateRiskScore(u, allReports, allConnectionRequests, verificationRequests)
                        }
                        val riskyCount = calculatedRiskyUsers.count { it.level != "Low Risk" }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Open Tickets Metric Card
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(AccentRed.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.PendingActions, null, tint = AccentRed, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("OPEN REPORTS", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text(openCount.toString(), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }

                            // Resolved Tickets Metric Card
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(NeonEmerald.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.VerifiedUser, null, tint = NeonEmerald, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("RESOLVED", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text(resolvedCount.toString(), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }

                            // Risky Users Metric Card
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(CrispAmber.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Shield, null, tint = CrispAmber, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("RISKY USERS", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text(riskyCount.toString(), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Moderation Sub-tabs Selector
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceColor, RoundedCornerShape(10.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val subTabs = listOf(
                                "OPEN QUEUE ($openCount)",
                                "RESOLVED ($resolvedCount)",
                                "RISK ALERTS ($riskyCount)"
                            )
                            subTabs.forEachIndexed { index, title ->
                                Button(
                                    onClick = { modSubTab = index },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (modSubTab == index) PrimaryBackground else Color.Transparent
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = title,
                                        color = if (modSubTab == index) AccentBlue else Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        when (modSubTab) {
                            0 -> {
                                // --- OPEN REPORTS QUEUE ---
                                val categories = listOf("All", "Spam", "Harassment", "Fake Account", "Portfolio Fraud", "Workspace Abuse", "Agreement Abuse")
                                
                                // Category Filters scroll row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    categories.take(4).forEach { cat ->
                                        val isSelected = selectedCategoryFilter == cat
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (isSelected) AccentBlue.copy(alpha = 0.2f) else SurfaceColor,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isSelected) AccentBlue else Color.Transparent,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable { selectedCategoryFilter = cat }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = cat.uppercase(),
                                                color = if (isSelected) AccentBlue else Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    categories.drop(4).forEach { cat ->
                                        val isSelected = selectedCategoryFilter == cat
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (isSelected) AccentBlue.copy(alpha = 0.2f) else SurfaceColor,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isSelected) AccentBlue else Color.Transparent,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable { selectedCategoryFilter = cat }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = cat.uppercase(),
                                                color = if (isSelected) AccentBlue else Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                val pendingReports = allReports.filter { 
                                    it.status == "PENDING" && (selectedCategoryFilter == "All" || it.category == selectedCategoryFilter)
                                }

                                if (pendingReports.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .background(SurfaceColor, RoundedCornerShape(12.dp))
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.CheckCircle, null, tint = NeonEmerald, modifier = Modifier.size(40.dp))
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("Queue Cleared!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("No pending reports found for category: $selectedCategoryFilter", color = TextSecondary, fontSize = 11.sp)
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        items(pendingReports) { report ->
                                            val reporterUser = users.find { it.id == report.reporterId }
                                            val targetUser = users.find { it.id == report.targetId }
                                            
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        // Category Badge
                                                        val badgeColor = when (report.category) {
                                                            "Spam" -> CrispAmber
                                                            "Harassment" -> AccentRed
                                                            "Fake Account" -> Color(0xFFE91E63)
                                                            "Portfolio Fraud" -> Color(0xFF9C27B0)
                                                            "Workspace Abuse" -> Color(0xFF3F51B5)
                                                            "Agreement Abuse" -> Color(0xFF00BCD4)
                                                            else -> AccentBlue
                                                        }
                                                        Box(
                                                            modifier = Modifier
                                                                .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(
                                                                text = report.category.uppercase(),
                                                                color = badgeColor,
                                                                fontSize = 8.sp,
                                                                fontWeight = FontWeight.Black
                                                            )
                                                        }

                                                        Spacer(modifier = Modifier.width(8.dp))

                                                        Text(
                                                            text = "TICKET #${report.id.take(6).uppercase()}",
                                                            color = Color.White,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )

                                                        Spacer(modifier = Modifier.weight(1f))

                                                        Text(
                                                            text = java.text.SimpleDateFormat("MMM dd, HH:mm").format(java.util.Date(report.createdAt)),
                                                            color = TextSecondary,
                                                            fontSize = 9.sp
                                                        )
                                                    }

                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        text = "Reason: ${report.reason}",
                                                        color = Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )

                                                    Spacer(modifier = Modifier.height(6.dp))

                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                                    ) {
                                                        Text(
                                                            text = "Reporter: ${reporterUser?.displayName ?: "Anonymous (${report.reporterId.take(4)})"}",
                                                            color = TextSecondary,
                                                            fontSize = 10.sp
                                                        )
                                                        Text(
                                                            text = "Target: ${if (report.targetType == "WORKSPACE") "Workspace (${report.targetId})" else targetUser?.displayName ?: "User (${report.targetId.take(4)})"}",
                                                            color = TextSecondary,
                                                            fontSize = 10.sp
                                                        )
                                                    }

                                                    Spacer(modifier = Modifier.height(10.dp))

                                                    Button(
                                                        onClick = { selectedReportForAction = report },
                                                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.fillMaxWidth(),
                                                        contentPadding = PaddingValues(vertical = 6.dp)
                                                    ) {
                                                        Icon(Icons.Default.Security, null, tint = Color.White, modifier = Modifier.size(12.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("AUDIT TICKET & GOVERN ACTION", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            1 -> {
                                // --- RESOLVED/DISMISSED QUEUE ---
                                val resolvedReports = allReports.filter { it.status != "PENDING" }

                                if (resolvedReports.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .background(SurfaceColor, RoundedCornerShape(12.dp))
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No resolved reports found.", color = TextSecondary, fontSize = 12.sp)
                                    }
                                } else {
                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        items(resolvedReports) { report ->
                                            val reporterUser = users.find { it.id == report.reporterId }
                                            val targetUser = users.find { it.id == report.targetId }
                                            
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(containerColor = SurfaceColor.copy(alpha = 0.6f)),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        val isDismissed = report.status == "DISMISSED"
                                                        Box(
                                                            modifier = Modifier
                                                                .background(
                                                                    if (isDismissed) Color.Gray.copy(alpha = 0.15f) else NeonEmerald.copy(alpha = 0.15f),
                                                                    RoundedCornerShape(4.dp)
                                                                )
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(
                                                                text = report.status.uppercase(),
                                                                color = if (isDismissed) Color.LightGray else NeonEmerald,
                                                                fontSize = 8.sp,
                                                                fontWeight = FontWeight.Black
                                                            )
                                                        }

                                                        Spacer(modifier = Modifier.width(8.dp))

                                                        Text(
                                                            text = report.category.uppercase(),
                                                            color = TextSecondary,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )

                                                        Spacer(modifier = Modifier.weight(1f))

                                                        Text(
                                                            text = java.text.SimpleDateFormat("MMM dd").format(java.util.Date(report.createdAt)),
                                                            color = TextSecondary,
                                                            fontSize = 9.sp
                                                        )
                                                    }

                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(report.reason, color = TextSecondary, fontSize = 11.sp)
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "Reporter: ${reporterUser?.displayName ?: report.reporterId} • Target: ${targetUser?.displayName ?: report.targetId}",
                                                        color = TextSecondary.copy(alpha = 0.7f),
                                                        fontSize = 9.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            2 -> {
                                // --- ABUSE DETECTION / RISKY USERS DASHBOARD ---
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val riskFilters = listOf("All", "High Risk Only", "Medium Risk Only")
                                    riskFilters.forEach { f ->
                                        val isSelected = selectedRiskFilter == f
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (isSelected) CrispAmber.copy(alpha = 0.2f) else SurfaceColor,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isSelected) CrispAmber else Color.Transparent,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable { selectedRiskFilter = f }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = f.uppercase(),
                                                color = if (isSelected) CrispAmber else Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                val riskyUsers = users.map { u ->
                                    val risk = calculateRiskScore(u, allReports, allConnectionRequests, verificationRequests)
                                    Pair(u, risk)
                                }.filter { (_, r) ->
                                    when (selectedRiskFilter) {
                                        "High Risk Only" -> r.level == "High Risk"
                                        "Medium Risk Only" -> r.level == "Medium Risk"
                                        else -> true // Displays everyone
                                    }
                                }.sortedByDescending { it.second.score }

                                if (riskyUsers.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .background(SurfaceColor, RoundedCornerShape(12.dp))
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No risky users found.", color = TextSecondary, fontSize = 12.sp)
                                    }
                                } else {
                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        items(riskyUsers) { (u, risk) ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    val scoreColor = when (risk.level) {
                                                        "High Risk" -> AccentRed
                                                        "Medium Risk" -> CrispAmber
                                                        else -> NeonEmerald
                                                    }
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Column {
                                                            Text(u.displayName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                            Text("@${u.username} • Specialty: ${u.primarySpecialty}", color = TextSecondary, fontSize = 9.sp)
                                                        }

                                                        Spacer(modifier = Modifier.weight(1f))

                                                        Column(horizontalAlignment = Alignment.End) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .background(scoreColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                                            ) {
                                                                Text(
                                                                    text = risk.level.uppercase(),
                                                                    color = scoreColor,
                                                                    fontSize = 8.sp,
                                                                    fontWeight = FontWeight.Black
                                                                )
                                                            }
                                                            Spacer(modifier = Modifier.height(2.dp))
                                                            Text(
                                                                text = "Score: ${risk.score}/100",
                                                                color = scoreColor,
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }

                                                    if (risk.reasons.isNotEmpty()) {
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .background(PrimaryBackground, RoundedCornerShape(6.dp))
                                                                .padding(8.dp)
                                                        ) {
                                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                                Text("ABUSE PATTERN TRIGGERED:", color = TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                                risk.reasons.forEach { reason ->
                                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                                        Icon(Icons.Default.Warning, null, tint = scoreColor, modifier = Modifier.size(10.dp))
                                                                        Spacer(modifier = Modifier.width(4.dp))
                                                                        Text(reason, color = Color.White, fontSize = 9.sp)
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
                            }
                        }
                    }
                }

                1 -> {
                    // USER MANAGEMENT CENTER
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Search bar
                        OutlinedTextField(
                            value = searchKeyword,
                            onValueChange = { searchKeyword = it },
                            modifier = Modifier.fillMaxWidth().testTag("user_search_input"),
                            placeholder = { Text("Search by name, username, or email...", fontSize = 12.sp, color = TextSecondary) },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = TextSecondary, modifier = Modifier.size(16.dp)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceColor,
                                unfocusedContainerColor = SurfaceColor,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = AccentBlue,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Scrollable Filters Row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                var showRoleDropdown by remember { mutableStateOf(false) }
                                Button(
                                    onClick = { showRoleDropdown = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Role: $selectedRoleFilter", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(12.dp))
                                }
                                DropdownMenu(
                                    expanded = showRoleDropdown,
                                    onDismissRequest = { showRoleDropdown = false },
                                    modifier = Modifier.background(SurfaceColor)
                                ) {
                                    listOf("ALL", "REGISTERED_USER", "ADMIN", "SUSPENDED", "SOFT_DELETED").forEach { role ->
                                        DropdownMenuItem(
                                            text = { Text(role, color = Color.White, fontSize = 11.sp) },
                                            onClick = {
                                                selectedRoleFilter = role
                                                showRoleDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                var showVerifDropdown by remember { mutableStateOf(false) }
                                Button(
                                    onClick = { showVerifDropdown = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Verif: $selectedVerificationFilter", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(12.dp))
                                }
                                DropdownMenu(
                                    expanded = showVerifDropdown,
                                    onDismissRequest = { showVerifDropdown = false },
                                    modifier = Modifier.background(SurfaceColor)
                                ) {
                                    listOf("ALL", "VERIFIED", "UNVERIFIED").forEach { v ->
                                        DropdownMenuItem(
                                            text = { Text(v, color = Color.White, fontSize = 11.sp) },
                                            onClick = {
                                                selectedVerificationFilter = v
                                                showVerifDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                var showTrustDropdown by remember { mutableStateOf(false) }
                                Button(
                                    onClick = { showTrustDropdown = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Trust: $selectedTrustFilter", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(12.dp))
                                }
                                DropdownMenu(
                                    expanded = showTrustDropdown,
                                    onDismissRequest = { showTrustDropdown = false },
                                    modifier = Modifier.background(SurfaceColor)
                                ) {
                                    listOf("ALL", "Gold", "Silver", "Bronze").forEach { badge ->
                                        DropdownMenuItem(
                                            text = { Text(badge, color = Color.White, fontSize = 11.sp) },
                                            onClick = {
                                                selectedTrustFilter = badge
                                                showTrustDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // User list matching filters
                        val filteredUsers = users.filter { u ->
                            val matchesSearch = u.displayName.contains(searchKeyword, ignoreCase = true) ||
                                    u.email.contains(searchKeyword, ignoreCase = true) ||
                                    u.username.contains(searchKeyword, ignoreCase = true)
                            
                            val matchesRole = when (selectedRoleFilter) {
                                "ALL" -> true
                                "ADMIN" -> u.systemRole == "ADMIN" || u.systemRole == "PLATFORM_ADMIN"
                                "SUSPENDED" -> u.systemRole == "SUSPENDED"
                                "SOFT_DELETED" -> u.systemRole == "SOFT_DELETED"
                                "REGISTERED_USER" -> u.systemRole == "REGISTERED_USER"
                                else -> true
                            }

                            val matchesVerification = when (selectedVerificationFilter) {
                                "ALL" -> true
                                "VERIFIED" -> u.isVerifiedPro
                                "UNVERIFIED" -> !u.isVerifiedPro
                                else -> true
                            }

                            val matchesTrust = when (selectedTrustFilter) {
                                "ALL" -> true
                                else -> u.reliabilityBadge.equals(selectedTrustFilter, ignoreCase = true)
                            }

                            matchesSearch && matchesRole && matchesVerification && matchesTrust
                        }

                        Text("RESULTS (${filteredUsers.size} Users)", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        if (filteredUsers.isEmpty()) {
                            Box(
                                modifier = Modifier.weight(1f).fillMaxWidth().background(SurfaceColor, RoundedCornerShape(16.dp)).padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No user profiles match your search criteria.", color = TextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                                items(filteredUsers) { user ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().clickable { selectedUserForDetail = user },
                                        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Letter avatar with pro badge circle
                                            Box(contentAlignment = Alignment.BottomEnd) {
                                                Box(
                                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(AccentBlue.copy(alpha = 0.15f)).border(1.dp, AccentBlue.copy(alpha = 0.3f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        user.displayName.take(2).uppercase(),
                                                        color = AccentBlue,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp
                                                    )
                                                }
                                                if (user.isVerifiedPro) {
                                                    Box(
                                                        modifier = Modifier.size(14.dp).clip(CircleShape).background(NeonEmerald),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Default.Check, null, tint = Color.Black, modifier = Modifier.size(10.dp))
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(user.displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    // Trust Badge
                                                    Box(
                                                        modifier = Modifier
                                                            .background(
                                                                when(user.reliabilityBadge.lowercase()) {
                                                                    "gold" -> Color(0xFFFFD700).copy(alpha = 0.15f)
                                                                    "silver" -> Color(0xFFC0C0C0).copy(alpha = 0.15f)
                                                                    else -> Color(0xFFCD7F32).copy(alpha = 0.15f)
                                                                },
                                                                RoundedCornerShape(6.dp)
                                                            )
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            user.reliabilityBadge,
                                                            color = when(user.reliabilityBadge.lowercase()) {
                                                                "gold" -> Color(0xFFFFD700)
                                                                "silver" -> Color(0xFFE0E0E0)
                                                                else -> Color(0xFFCD7F32)
                                                            },
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Black
                                                        )
                                                    }
                                                }
                                                Text("@${user.username} • ${user.email}", color = TextSecondary, fontSize = 11.sp)
                                            }

                                            // Status Badge
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        when (user.systemRole) {
                                                            "SUSPENDED" -> AccentRed.copy(alpha = 0.15f)
                                                            "SOFT_DELETED" -> Color.Gray.copy(alpha = 0.15f)
                                                            "ADMIN", "PLATFORM_ADMIN" -> NeonEmerald.copy(alpha = 0.15f)
                                                            else -> AccentBlue.copy(alpha = 0.15f)
                                                        },
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    user.systemRole,
                                                    color = when (user.systemRole) {
                                                        "SUSPENDED" -> AccentRed
                                                        "SOFT_DELETED" -> Color.LightGray
                                                        "ADMIN", "PLATFORM_ADMIN" -> NeonEmerald
                                                        else -> AccentBlue
                                                    },
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // CREATOR VERIFICATION CENTER
                    val pendingRequests = verificationRequests.filter { it.status == "PENDING" }
                    val totalRequestsCount = verificationRequests.size
                    val approvedRequestsCount = verificationRequests.count { it.status == "APPROVED" }
                    val conversionRate = if (totalRequestsCount > 0) (approvedRequestsCount.toFloat() / totalRequestsCount * 100).toInt() else 0

                    Column(modifier = Modifier.fillMaxSize()) {
                        // Analytics cards row
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("VERIFIED USERS", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text(users.count { it.isVerifiedPro }.toString(), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("PENDING REVIEWS", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text(pendingRequests.size.toString(), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("CONVERSION RATE", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text("$conversionRate%", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("VERIFICATION REQUEST QUEUE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        if (verificationRequests.isEmpty()) {
                            Box(
                                modifier = Modifier.weight(1f).fillMaxWidth().background(SurfaceColor, RoundedCornerShape(16.dp)).padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No verification requests recorded in the database.", color = TextSecondary, fontSize = 12.sp)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                                items(verificationRequests) { req ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier.size(32.dp).clip(CircleShape).background(AccentBlue.copy(alpha = 0.15f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(req.userDisplayName.take(2).uppercase(), color = AccentBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(req.userDisplayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Text(req.userRole, color = TextSecondary, fontSize = 11.sp)
                                                }

                                                // Status badge
                                                Box(
                                                    modifier = Modifier
                                                        .background(
                                                            when (req.status) {
                                                                "APPROVED" -> NeonEmerald.copy(alpha = 0.15f)
                                                                "REJECTED" -> AccentRed.copy(alpha = 0.15f)
                                                                else -> AccentBlue.copy(alpha = 0.15f)
                                                            },
                                                            RoundedCornerShape(8.dp)
                                                        )
                                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        req.status,
                                                        color = when (req.status) {
                                                            "APPROVED" -> NeonEmerald
                                                            "REJECTED" -> AccentRed
                                                            else -> AccentBlue
                                                        },
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Black
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(req.notes, color = TextSecondary, fontSize = 12.sp)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            
                                            // Display portfolio url
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Link, null, tint = AccentBlue, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = req.portfolioUrl,
                                                    color = AccentBlue,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.clickable { /* Simulate Portfolio visit */ }
                                                )
                                            }

                                            if (req.status == "PENDING") {
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                                    Button(
                                                        onClick = {
                                                            selectedRequestForReview = req
                                                            verificationNotes = ""
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                                        shape = RoundedCornerShape(8.dp),
                                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                                    ) {
                                                        Text("REVIEW CANDIDATE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            } else {
                                                if (req.notes.isNotEmpty() && req.reviewedBy != null) {
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    Text(
                                                        "Reviewed by ${req.reviewedBy} (Internal notes: ${req.notes})",
                                                        color = TextSecondary,
                                                        fontSize = 10.sp,
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
                }
            }
        }
    }

    // --- REVIEWS & ACTIONS DIALOGS ---

    // 1. Resolve Report Dialog (Report Detail Screen)
    if (selectedReportForAction != null) {
        val report = selectedReportForAction!!
        val reporter = users.find { it.id == report.reporterId }
        
        // Resolve target user of this report
        val targetUser = when (report.targetType.uppercase()) {
            "USER" -> users.find { it.id == report.targetId }
            else -> users.find { it.id == report.targetId }
        }

        // Filter user history/audit logs
        val targetUserId = targetUser?.id ?: report.targetId
        val targetHistoryLogs = userLogs.filter { it.targetUserId == targetUserId }

        androidx.compose.ui.window.Dialog(onDismissRequest = { selectedReportForAction = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .clip(RoundedCornerShape(16.dp)),
                color = SurfaceColor
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Security, "Shield Icon", tint = AccentRed, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MODERATION DETAIL CASE",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { selectedReportForAction = null }) {
                            Icon(Icons.Default.Close, null, tint = Color.White)
                        }
                    }

                    Divider(color = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 10.dp))

                    // 1. Reporter Info Section
                    Text("1. REPORTER", color = AccentBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryBackground),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = reporter?.displayName ?: "System Flag / External User",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "User ID: ${report.reporterId}",
                                color = TextSecondary,
                                fontSize = 9.sp
                            )
                            if (reporter != null) {
                                Text(
                                    text = "Email: ${reporter.email} • Rating: ${reporter.peerRating}",
                                    color = TextSecondary,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }

                    // 2. Reported Entity Section
                    Text("2. REPORTED SUBJECT", color = AccentRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryBackground),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            if (report.targetType == "WORKSPACE") {
                                Text(
                                    text = "Workspace: ID ${report.targetId}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = targetUser?.displayName ?: "Unknown Subject",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Role: ${targetUser?.globalRole ?: "APP_USER"} • System status: ${targetUser?.systemRole ?: "ACTIVE"}",
                                    color = TextSecondary,
                                    fontSize = 9.sp
                                )
                                Text(
                                    text = "Reputation: ${targetUser?.reputationScore ?: 100}/100 • Reliability: ${targetUser?.reliabilityBadge ?: "None"}",
                                    color = TextSecondary,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }

                    // 3. Evidence / Reason Section
                    Text("3. INCIDENT EVIDENCE", color = CrispAmber, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryBackground),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Stated Reason: ${report.reason}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (report.evidence.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SurfaceColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = report.evidence,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    // 4. Governance History of Reported User
                    Text("4. TARGET HISTORY / PAST INCIDENTS", color = Color.LightGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    if (targetHistoryLogs.isEmpty()) {
                        Text(
                            text = "Clean slate: No past administrative logs for this subject.",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier.padding(vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            targetHistoryLogs.forEach { log ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(PrimaryBackground, RoundedCornerShape(4.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.History, null, tint = CrispAmber, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = "${log.actionTaken.uppercase()}: ${log.reason}",
                                            color = Color.White,
                                            fontSize = 9.sp
                                        )
                                        Text(
                                            text = "Logged at: ${java.text.SimpleDateFormat("MMM dd, yyyy").format(java.util.Date(log.createdAt))}",
                                            color = TextSecondary,
                                            fontSize = 8.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Divider(color = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 10.dp))

                    // 5. Admin Notes / Resolution Input
                    Text("REQUIRED INTERNAL AUDIT NOTES", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = resolutionReason,
                        onValueChange = { resolutionReason = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Describe reasoning for audit logs...", color = TextSecondary, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = PrimaryBackground,
                            unfocusedContainerColor = PrimaryBackground,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 6. Action governance buttons
                    Text("TAKE DISCIPLINARY / RESOLUTION ACTION", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Action 1: Dismiss Report
                        Button(
                            onClick = {
                                adminViewModel.resolveReportWithGovernanceAction(report.id, "DISMISS", resolutionReason, adminId)
                                selectedReportForAction = null
                                resolutionReason = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("DISMISS REPORT (NO VIOLATION)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Action 2: Send Warning
                        Button(
                            onClick = {
                                adminViewModel.resolveReportWithGovernanceAction(report.id, "WARNING", resolutionReason, adminId)
                                selectedReportForAction = null
                                resolutionReason = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CrispAmber),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("ISSUE OFFICIAL USER WARNING", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Action 3: Temporary Suspension
                        Button(
                            onClick = {
                                adminViewModel.resolveReportWithGovernanceAction(report.id, "TEMP_SUSPEND", resolutionReason, adminId)
                                selectedReportForAction = null
                                resolutionReason = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("TEMPORARY SUSPENSION (SUSPEND USER)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Action 4: Permanent Ban
                        Button(
                            onClick = {
                                adminViewModel.resolveReportWithGovernanceAction(report.id, "PERM_BAN", resolutionReason, adminId)
                                selectedReportForAction = null
                                resolutionReason = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B0000)), // Dark Red
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("PERMANENT BAN (BLOCK PROFILE)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Action 5: Flag Account
                        Button(
                            onClick = {
                                adminViewModel.resolveReportWithGovernanceAction(report.id, "FLAG_ACCOUNT", resolutionReason, adminId)
                                selectedReportForAction = null
                                resolutionReason = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("FLAG ACCOUNT FOR TRUST AUDIT", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // 2. User Detail & Administrative Management Dialog
    if (selectedUserForDetail != null) {
        val user = selectedUserForDetail!!
        var actionReasonInput by remember { mutableStateOf("") }
        var showVerificationLevelRow by remember { mutableStateOf(false) }
        var showBadgeSelectorRow by remember { mutableStateOf(false) }

        androidx.compose.ui.window.Dialog(onDismissRequest = { selectedUserForDetail = null }) {
            Surface(
                color = SurfaceColor,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().height(650.dp).padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(52.dp).clip(CircleShape).background(AccentBlue.copy(alpha = 0.15f)).border(1.5.dp, AccentBlue, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(user.displayName.take(2).uppercase(), color = AccentBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(user.displayName, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                if (user.isVerifiedPro) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(16.dp))
                                }
                            }
                            Text("@${user.username}", color = TextSecondary, fontSize = 12.sp)
                            Text(user.email, color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = ColorDivider.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // User Stats Section
                    Text("PLATFORM METRICS", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 10.sp, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f).background(PrimaryBackground, RoundedCornerShape(12.dp)).padding(10.dp)) {
                            Text("REPUTATION", color = TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            Text("${user.reputationScore} / 100", color = NeonEmerald, fontSize = 14.sp, fontWeight = FontWeight.Black)
                        }
                        Column(modifier = Modifier.weight(1f).background(PrimaryBackground, RoundedCornerShape(12.dp)).padding(10.dp)) {
                            Text("RELIABILITY", color = TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            Text(user.reliabilityBadge, color = AccentBlue, fontSize = 14.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f).background(PrimaryBackground, RoundedCornerShape(12.dp)).padding(10.dp)) {
                            Text("AGREEMENTS SIGNED", color = TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            Text(user.signedAgreementsCount.toString(), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                        }
                        Column(modifier = Modifier.weight(1f).background(PrimaryBackground, RoundedCornerShape(12.dp)).padding(10.dp)) {
                            Text("TASKS COMPLETED", color = TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            Text(user.completedProjectsCount.toString(), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("VERIFICATION LEVEL: ${user.verificationLevel}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    if (user.websiteUrl.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Link, null, tint = AccentBlue, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Portfolio: ${user.websiteUrl}", color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable {})
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = ColorDivider.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // User Audit Timeline
                    val specificUserLogs = userLogs.filter { it.targetUserId == user.id }
                    Text("SYSTEM AUDIT TRAIL (${specificUserLogs.size})", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 10.sp, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (specificUserLogs.isEmpty()) {
                        Text("No administrative modifications on record for this account.", color = TextSecondary, fontSize = 11.sp)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            specificUserLogs.take(5).forEach { log ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Box(modifier = Modifier.padding(top = 4.dp).size(6.dp).clip(CircleShape).background(AccentBlue))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            "[${log.actionTaken}] ${log.reason}",
                                            color = TextSecondary,
                                            fontSize = 10.sp,
                                            lineHeight = 13.sp
                                        )
                                        Text(
                                            "Admin: ${log.adminId} • ${java.text.SimpleDateFormat("MMM dd, HH:mm").format(java.util.Date(log.createdAt))}",
                                            color = TextSecondary.copy(alpha = 0.7f),
                                            fontSize = 8.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = ColorDivider.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // ADMIN ACTIONS SECTION
                    Text("ADMIN CONTROL ACTION CENTER", fontWeight = FontWeight.Black, color = AccentRed, fontSize = 11.sp, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = actionReasonInput,
                        onValueChange = { actionReasonInput = it },
                        placeholder = { Text("Reason for action (Mandatory for Audit logs)...", color = TextSecondary, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = PrimaryBackground,
                            unfocusedContainerColor = PrimaryBackground,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Buttons
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            // Suspend / Reactivate
                            if (user.systemRole == "SUSPENDED") {
                                Button(
                                    onClick = {
                                        adminViewModel.reactivateUser(user.id, adminId, actionReasonInput.ifBlank { "Reactivated by admin" })
                                        selectedUserForDetail = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Restore, null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("REACTIVATE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        adminViewModel.suspendUser(user.id, adminId, actionReasonInput.ifBlank { "Violation of TOS" })
                                        selectedUserForDetail = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Block, null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("SUSPEND", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Soft Delete / Restore
                            if (user.systemRole == "SOFT_DELETED") {
                                Button(
                                    onClick = {
                                        adminViewModel.restoreUser(user.id, adminId, actionReasonInput.ifBlank { "Restored from soft deletion" })
                                        selectedUserForDetail = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.SettingsBackupRestore, null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("RESTORE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        adminViewModel.softDeleteUser(user.id, adminId, actionReasonInput.ifBlank { "Soft deleted by Admin" })
                                        selectedUserForDetail = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("SOFT DELETE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            // Force Logout
                            Button(
                                onClick = {
                                    adminViewModel.forceLogout(user.id, adminId, actionReasonInput.ifBlank { "Forced logout by administrator" })
                                    selectedUserForDetail = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(Icons.Default.Logout, null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("FORCE LOGOUT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Promoted Verification Dropdowns triggers
                        Button(
                            onClick = { showVerificationLevelRow = !showVerificationLevelRow },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Promote Verification Level", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        AnimatedVisibility(visible = showVerificationLevelRow) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("L1 Standard", "L2 Pro Verified", "Executive Pro").forEach { lvl ->
                                    Button(
                                        onClick = {
                                            adminViewModel.promoteVerificationLevel(user.id, lvl, adminId, actionReasonInput.ifBlank { "Verification Promotion" })
                                            selectedUserForDetail = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.5f))
                                    ) {
                                        Text(lvl, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NeonEmerald)
                                    }
                                }
                            }
                        }

                        // Promoted Badge Dropdowns triggers
                        Button(
                            onClick = { showBadgeSelectorRow = !showBadgeSelectorRow },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Update Reliability Badge", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        AnimatedVisibility(visible = showBadgeSelectorRow) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Gold", "Silver", "Bronze").forEach { b ->
                                    Button(
                                        onClick = {
                                            adminViewModel.assignTrustBadge(user.id, b, adminId, actionReasonInput.ifBlank { "Reliability Score Modification" })
                                            selectedUserForDetail = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp, 
                                            when(b) {
                                                "Gold" -> Color(0xFFFFD700)
                                                "Silver" -> Color(0xFFC0C0C0)
                                                else -> Color(0xFFCD7F32)
                                            }
                                        )
                                    ) {
                                        Text(
                                            b, 
                                            fontSize = 9.sp, 
                                            fontWeight = FontWeight.Bold, 
                                            color = when(b) {
                                                "Gold" -> Color(0xFFFFD700)
                                                "Silver" -> Color(0xFFE0E0E0)
                                                else -> Color(0xFFCD7F32)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { selectedUserForDetail = null },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor)
                    ) {
                        Text("CLOSE PANEL", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // 3. Review Verification Candidate Dialog
    if (selectedRequestForReview != null) {
        val req = selectedRequestForReview!!

        androidx.compose.ui.window.Dialog(onDismissRequest = { selectedRequestForReview = null }) {
            Surface(
                color = SurfaceColor,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().height(480.dp).padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("VERIFICATION CANDIDATE REVIEW", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text("Audit portfolio and verify identity", color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(20.dp))

                    Text("CANDIDATE PROFILE", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(req.userDisplayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(req.userRole, color = AccentBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("PORTFOLIO LINK", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(req.portfolioUrl, color = AccentBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.clickable {})

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("APPLICANT COVER STATEMENT", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(req.notes, color = Color.White, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = ColorDivider.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("DECISION AUDIT NOTES", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = verificationNotes,
                        onValueChange = { verificationNotes = it },
                        placeholder = { Text("Reasoning or rejection feedback...", color = TextSecondary, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = PrimaryBackground,
                            unfocusedContainerColor = PrimaryBackground,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                adminViewModel.rejectVerification(req.id, adminId, verificationNotes.ifBlank { "Does not meet portfolio standards." })
                                selectedRequestForReview = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("REJECT", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                adminViewModel.approveVerification(req.id, adminId, verificationNotes.ifBlank { "Portfolio approved, meets criteria." })
                                selectedRequestForReview = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("APPROVE PRO", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Existing child dialogs
    if (showFeatureFlags) {
        AdminFeatureFlagsDialog(
            adminViewModel = adminViewModel,
            adminId = adminId,
            onDismiss = { showFeatureFlags = false }
        )
    }

    if (showSponsorships) {
        SponsorshipManagementDialog(
            adminViewModel = adminViewModel,
            adminId = adminId,
            onDismiss = { showSponsorships = false }
        )
    }
}

@Composable
fun SponsorshipManagementDialog(
    adminViewModel: AdminViewModel,
    adminId: String,
    onDismiss: () -> Unit
) {
    val workspaces by adminViewModel.allWorkspaces.collectAsState()
    var selectedWorkspaceForEdit by remember { mutableStateOf<com.example.data.model.Workspace?>(null) }
    var isSponsoredInput by remember { mutableStateOf(false) }
    var sponsorNameInput by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = SurfaceColor,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().height(500.dp).padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("WORKSPACE SPONSORSHIPS", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Text("Promote and sponsor workspaces", color = TextSecondary, fontSize = 11.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedWorkspaceForEdit == null) {
                    // List workspaces
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(workspaces) { ws ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    selectedWorkspaceForEdit = ws
                                    isSponsoredInput = ws.isSponsored
                                    sponsorNameInput = ws.sponsorName ?: ""
                                },
                                colors = CardDefaults.cardColors(containerColor = PrimaryBackground),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(ws.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Platform: ${ws.platformType}", color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }

                                    if (ws.isSponsored) {
                                        Box(
                                            modifier = Modifier
                                                .background(NeonEmerald.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("SPONSORED", color = NeonEmerald, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                        }
                                    } else {
                                        Text("STANDARD", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Edit form
                    val ws = selectedWorkspaceForEdit!!
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Editing: ${ws.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Sponsored Status", color = Color.White, fontSize = 12.sp)
                            Switch(
                                checked = isSponsoredInput,
                                onCheckedChange = { isSponsoredInput = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = NeonEmerald, checkedTrackColor = NeonEmerald.copy(alpha = 0.3f))
                            )
                        }

                        OutlinedTextField(
                            value = sponsorNameInput,
                            onValueChange = { sponsorNameInput = it },
                            placeholder = { Text("Sponsor Brand Name (e.g., Google for Startups)...", color = TextSecondary, fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Sponsor Name", color = AccentBlue, fontSize = 10.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = PrimaryBackground,
                                unfocusedContainerColor = PrimaryBackground,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { selectedWorkspaceForEdit = null },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("CANCEL", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    adminViewModel.updateSponsorship(ws.id, isSponsoredInput, sponsorNameInput.ifBlank { null }, adminId)
                                    selectedWorkspaceForEdit = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("SAVE", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

data class RiskCalculation(
    val score: Int,
    val level: String, // Low Risk, Medium Risk, High Risk
    val reasons: List<String>
)

fun calculateRiskScore(
    user: UserProfile,
    reports: List<Report>,
    requests: List<com.example.data.model.ConnectionRequest>,
    verifications: List<VerificationRequest>
): RiskCalculation {
    var score = 0
    val reasons = mutableListOf<String>()

    // 1. Excessive Invitations
    val userRequestsCount = requests.count { it.senderId == user.id }
    if (userRequestsCount > 10) {
        score += 40
        reasons.add("Excessive connection requests ($userRequestsCount sent)")
    } else if (userRequestsCount > 5) {
        score += 20
        reasons.add("High connection requests ($userRequestsCount sent)")
    }

    // 2. Multiple Reports against this user
    val reportsAgainstUser = reports.count { report ->
        report.targetId == user.id && report.status == "PENDING"
    }
    if (reportsAgainstUser >= 3) {
        score += 50
        reasons.add("Multiple pending reports ($reportsAgainstUser reports)")
    } else if (reportsAgainstUser > 0) {
        score += 25
        reasons.add("Pending report filed ($reportsAgainstUser report)")
    }

    // 3. Account Age
    val ageDays = (System.currentTimeMillis() - user.createdAt) / (24 * 60 * 60 * 1000L)
    if (ageDays < 2) {
        score += 30
        reasons.add("New account (created < 2 days ago)")
    } else if (ageDays < 7) {
        score += 15
        reasons.add("Recent account (created < 7 days ago)")
    }

    // 4. Failed Verification
    val hasFailedVerification = verifications.any { it.userId == user.id && it.status == "REJECTED" }
    if (hasFailedVerification) {
        score += 35
        reasons.add("Failed professional verification request")
    }

    val level = when {
        score >= 60 -> "High Risk"
        score >= 30 -> "Medium Risk"
        else -> "Low Risk"
    }

    return RiskCalculation(score = minOf(100, score), level = level, reasons = reasons)
}

