package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Announcement
import com.example.ui.theme.*
import com.example.ui.viewmodels.CommunicationViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommCenterScreen(
    viewModel: CommunicationViewModel,
    onBack: () -> Unit
) {
    val announcements by viewModel.allAnnouncements.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableStateOf(0) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Communications HQ", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Text("Broadcasts, direct channels & alerts", color = TextSecondary, fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "Help Guide", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceColor)
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = AccentBlue,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("create_announcement_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Announcement")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = PrimaryBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceColor,
                contentColor = AccentBlue
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Campaign Manager", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Campaign, contentDescription = "Campaigns") },
                    modifier = Modifier.testTag("tab_campaign_manager")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Analytics Hub", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Analytics") },
                    modifier = Modifier.testTag("tab_analytics_hub")
                )
            }

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "tab_animation"
            ) { targetTab ->
                when (targetTab) {
                    0 -> CampaignManagerTab(
                        announcements = announcements,
                        onSimulateInteraction = { id, type -> viewModel.simulateUserInteraction(id, type) },
                        onUpdateStatus = { id, status -> viewModel.updateAnnouncementStatus(id, status) },
                        onDelete = { id -> viewModel.deleteAnnouncement(id) }
                    )
                    1 -> AnalyticsHubTab(announcements = announcements)
                }
            }
        }

        if (showCreateDialog) {
            CreateAnnouncementDialog(
                onDismiss = { showCreateDialog = false },
                onSave = { title, content, target, type, status, offset ->
                    viewModel.createAnnouncement(title, content, target, type, status, offset)
                    showCreateDialog = false
                }
            )
        }

        if (showHelpDialog) {
            AdminHelpDialog(onDismiss = { showHelpDialog = false })
        }
    }
}

@Composable
fun CampaignManagerTab(
    announcements: List<Announcement>,
    onSimulateInteraction: (String, String) -> Unit,
    onUpdateStatus: (String, String) -> Unit,
    onDelete: (String) -> Unit
) {
    if (announcements.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Campaign,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No notification campaigns active.",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Create a new announcement to begin platform targeting.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Banner Card
            item {
                CommCenterHeroBanner()
            }

            items(announcements, key = { it.id }) { ann ->
                AnnouncementCampaignCard(
                    ann = ann,
                    onSimulateInteraction = onSimulateInteraction,
                    onUpdateStatus = onUpdateStatus,
                    onDelete = onDelete
                )
            }
        }
    }
}

@Composable
fun CommCenterHeroBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("comm_center_hero_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = BorderStroke(1.dp, ColorDivider)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(AccentBlue.copy(alpha = 0.15f), AccentRed.copy(alpha = 0.05f))
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AccentBlue.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.RecordVoiceOver,
                        contentDescription = "Platform Voice",
                        tint = AccentBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        "Enterprise Communications Panel",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        "Broadcast messages, manage targeted notification cohorts, scheduled alerts, and analyze engagement rates live.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnnouncementCampaignCard(
    ann: Announcement,
    onSimulateInteraction: (String, String) -> Unit,
    onUpdateStatus: (String, String) -> Unit,
    onDelete: (String) -> Unit
) {
    // Colors based on Status/Target/Type
    val statusColor = when (ann.status) {
        "Active" -> NeonEmerald
        "Scheduled" -> CrispAmber
        "Draft" -> TextMuted
        "Archived" -> AccentBlue
        else -> TextSecondary
    }

    val typeColor = when (ann.campaignType) {
        "Product Update" -> AccentBlue
        "Maintenance Notice" -> AccentRed
        "Beta Invite" -> CrispAmber
        "New Feature Launch" -> NeonEmerald
        else -> TextSecondary
    }

    // Dynamic rates
    val openRate = if (ann.targetReach > 0) {
        (ann.opensCount.toFloat() / ann.targetReach * 100).coerceAtMost(100f)
    } else 0f

    val clickRate = if (ann.opensCount > 0) {
        (ann.clicksCount.toFloat() / ann.opensCount * 100).coerceAtMost(100f)
    } else 0f

    val dismissRate = if (ann.opensCount > 0) {
        (ann.dismissalsCount.toFloat() / ann.opensCount * 100).coerceAtMost(100f)
    } else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("campaign_card_${ann.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = BorderStroke(1.dp, if (ann.status == "Active") AccentBlue.copy(alpha = 0.4f) else ColorDivider)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Segment & Type Chips + Status Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Type Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(typeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            ann.campaignType.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = typeColor
                        )
                    }

                    // Target Audience Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AccentBlue.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(10.dp))
                            Text(
                                ann.targetAudience.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = AccentBlue
                            )
                        }
                    }
                }

                // Status Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Text(
                        ann.status.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = statusColor
                    )
                }
            }

            // Title and description
            Column {
                Text(
                    text = ann.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = ann.content,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }

            // Date / Schedule information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Created: " + SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(ann.createdAt)),
                    fontSize = 11.sp,
                    color = TextMuted
                )

                if (ann.status == "Scheduled" && ann.scheduledTime > 0L) {
                    Text(
                        text = "Alerts Scheduled: " + SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(ann.scheduledTime)),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CrispAmber
                    )
                }
            }

            HorizontalDivider(color = ColorDivider, thickness = 1.dp)

            // Dynamic Analytics Progress indicators
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "REAL-TIME ENGAGEMENT METRICS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )

                // Reach Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Target Audience Reach", fontSize = 11.sp, color = TextSecondary)
                    Text("${ann.opensCount} Opened / ${ann.targetReach} Target Users", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                // Open Rate row
                AnalyticsMetricBar(
                    label = "Open Rate",
                    value = openRate,
                    indicatorColor = AccentBlue,
                    rawCountLabel = "${ann.opensCount} opens"
                )

                // Click Rate row
                AnalyticsMetricBar(
                    label = "Click-Through Rate (CTR)",
                    value = clickRate,
                    indicatorColor = NeonEmerald,
                    rawCountLabel = "${ann.clicksCount} clicks"
                )

                // Dismiss Rate row
                AnalyticsMetricBar(
                    label = "Dismissal Rate",
                    value = dismissRate,
                    indicatorColor = AccentRed,
                    rawCountLabel = "${ann.dismissalsCount} dismissed"
                )
            }

            HorizontalDivider(color = ColorDivider, thickness = 1.dp)

            // Simulation & Operation Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive simulation actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Simulate:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    
                    IconButton(
                        onClick = { onSimulateInteraction(ann.id, "OPEN") },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("simulate_open_${ann.id}")
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = "Simulate Open", tint = AccentBlue, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = { onSimulateInteraction(ann.id, "CLICK") },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("simulate_click_${ann.id}")
                    ) {
                        Icon(Icons.Default.AdsClick, contentDescription = "Simulate Click", tint = NeonEmerald, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = { onSimulateInteraction(ann.id, "DISMISS") },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("simulate_dismiss_${ann.id}")
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = "Simulate Dismiss", tint = AccentRed, modifier = Modifier.size(16.dp))
                    }
                }

                // Management actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (ann.status == "Draft" || ann.status == "Scheduled") {
                        Button(
                            onClick = { onUpdateStatus(ann.id, "Active") },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald.copy(alpha = 0.2f), contentColor = NeonEmerald),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(28.dp)
                                .testTag("publish_button_${ann.id}")
                        ) {
                            Text("Publish Now", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (ann.status == "Active") {
                        Button(
                            onClick = { onUpdateStatus(ann.id, "Archived") },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue.copy(alpha = 0.2f), contentColor = AccentBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(28.dp)
                                .testTag("archive_button_${ann.id}")
                        ) {
                            Text("Archive", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    IconButton(
                        onClick = { onDelete(ann.id) },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_button_${ann.id}")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsMetricBar(
    label: String,
    value: Float,
    indicatorColor: Color,
    rawCountLabel: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 11.sp, color = TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(rawCountLabel, fontSize = 10.sp, color = TextMuted)
                Text(String.format(Locale.getDefault(), "%.1f%%", value), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = indicatorColor)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { value / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = indicatorColor,
            trackColor = ColorDivider
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnalyticsHubTab(announcements: List<Announcement>) {
    val totalReach = announcements.sumOf { it.targetReach }
    val totalOpens = announcements.sumOf { it.opensCount }
    val totalClicks = announcements.sumOf { it.clicksCount }
    val totalDismissals = announcements.sumOf { it.dismissalsCount }

    val globalOpenRate = if (totalReach > 0) (totalOpens.toFloat() / totalReach * 100) else 0f
    val globalClickRate = if (totalOpens > 0) (totalClicks.toFloat() / totalOpens * 100) else 0f
    val globalDismissRate = if (totalOpens > 0) (totalDismissals.toFloat() / totalOpens * 100) else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Aggregated Campaign Analytics",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Stats Cards Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatPanelCard(
                    title = "Total Reach",
                    value = "$totalReach",
                    subtitle = "Audience Users",
                    icon = Icons.Default.Groups,
                    color = AccentBlue,
                    modifier = Modifier.weight(1f)
                )
                StatPanelCard(
                    title = "Avg Open Rate",
                    value = String.format(Locale.getDefault(), "%.1f%%", globalOpenRate),
                    subtitle = "$totalOpens Opened",
                    icon = Icons.Default.Visibility,
                    color = NeonEmerald,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatPanelCard(
                    title = "Avg CTR",
                    value = String.format(Locale.getDefault(), "%.1f%%", globalClickRate),
                    subtitle = "$totalClicks Clicks",
                    icon = Icons.Default.AdsClick,
                    color = CrispAmber,
                    modifier = Modifier.weight(1f)
                )
                StatPanelCard(
                    title = "Dismissal Rate",
                    value = String.format(Locale.getDefault(), "%.1f%%", globalDismissRate),
                    subtitle = "$totalDismissals Dismissed",
                    icon = Icons.Default.Cancel,
                    color = AccentRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Segment Insights Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                border = BorderStroke(1.dp, ColorDivider)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "SEGMENT ENGAGEMENT ANALYSIS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = AccentBlue,
                        letterSpacing = 1.sp
                    )

                    val audiences = listOf("All Users", "Verified Creators", "Editors", "Workspace Owners", "Beta Cohorts")
                    audiences.forEach { aud ->
                        val audAnnouncements = announcements.filter { it.targetAudience == aud }
                        val reach = audAnnouncements.sumOf { it.targetReach }
                        val opens = audAnnouncements.sumOf { it.opensCount }
                        val rate = if (reach > 0) (opens.toFloat() / reach * 100) else 0f

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(aud, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("$reach targeted", fontSize = 11.sp, color = TextMuted)
                                Text(
                                    String.format(Locale.getDefault(), "%.1f%% Open", rate),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (rate > 50) NeonEmerald else CrispAmber
                                )
                            }
                        }
                        LinearProgressIndicator(
                            progress = { rate / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = AccentBlue,
                            trackColor = ColorDivider
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }

        // Notification Campaign Breakdown Charts (Visual Custom Bars)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                border = BorderStroke(1.dp, ColorDivider)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "NOTIFICATION CAMPAIGNS BREAKDOWN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = AccentBlue,
                        letterSpacing = 1.sp
                    )

                    val campaignTypes = listOf("Product Update", "Maintenance Notice", "Beta Invite", "New Feature Launch")
                    campaignTypes.forEach { type ->
                        val list = announcements.filter { it.campaignType == type }
                        val total = list.size
                        val active = list.count { it.status == "Active" }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(AccentBlue))
                                Text(type, fontSize = 13.sp, color = Color.White)
                            }
                            Text("$total launched ($active active)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatPanelCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = BorderStroke(1.dp, ColorDivider)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text(subtitle, fontSize = 10.sp, color = TextMuted)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateAnnouncementDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var targetAudience by remember { mutableStateOf("All Users") }
    var campaignType by remember { mutableStateOf("Product Update") }
    var status by remember { mutableStateOf("Active") }
    var scheduledDaysOffset by remember { mutableStateOf(2) }

    val audiences = listOf("All Users", "Verified Creators", "Editors", "Workspace Owners", "Beta Cohorts")
    val campaignTypes = listOf("Product Update", "Maintenance Notice", "Beta Invite", "New Feature Launch")
    val statuses = listOf("Active", "Draft", "Scheduled")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Create Target Campaign",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = Color.White
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Title Field
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Campaign Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("campaign_title_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = ColorDivider
                        ),
                        singleLine = true
                    )
                }

                // Content Body
                item {
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Notification Body Text") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("campaign_content_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = ColorDivider
                        ),
                        maxLines = 4
                    )
                }

                // Targeting Audience Choice Chips
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("TARGETING AUDIENCE COHORT", fontSize = 10.sp, fontWeight = FontWeight.Black, color = TextSecondary)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            audiences.forEach { aud ->
                                val selected = targetAudience == aud
                                FilterChip(
                                    selected = selected,
                                    onClick = { targetAudience = aud },
                                    label = { Text(aud, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AccentBlue,
                                        selectedLabelColor = Color.White,
                                        containerColor = SurfaceColor,
                                        labelColor = TextSecondary
                                    ),
                                    modifier = Modifier.testTag("targeting_chip_$aud")
                                )
                            }
                        }
                    }
                }

                // Notification Campaign Type Chips
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("NOTIFICATION CAMPAIGN TYPE", fontSize = 10.sp, fontWeight = FontWeight.Black, color = TextSecondary)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            campaignTypes.forEach { type ->
                                val selected = campaignType == type
                                FilterChip(
                                    selected = selected,
                                    onClick = { campaignType = type },
                                    label = { Text(type, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AccentBlue,
                                        selectedLabelColor = Color.White,
                                        containerColor = SurfaceColor,
                                        labelColor = TextSecondary
                                    ),
                                    modifier = Modifier.testTag("campaign_type_chip_$type")
                                )
                            }
                        }
                    }
                }

                // Status Choices
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("PUBLICATION STATUS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = TextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            statuses.forEach { st ->
                                val selected = status == st
                                FilterChip(
                                    selected = selected,
                                    onClick = { status = st },
                                    label = { Text(st, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AccentBlue,
                                        selectedLabelColor = Color.White,
                                        containerColor = SurfaceColor,
                                        labelColor = TextSecondary
                                    ),
                                    modifier = Modifier.testTag("status_chip_$st")
                                )
                            }
                        }
                    }
                }

                // Schedule Slide-offset selection if Scheduled
                if (status == "Scheduled") {
                    item {
                        Column {
                            Text("Schedule Offset: $scheduledDaysOffset days from now", fontSize = 11.sp, color = TextSecondary)
                            Slider(
                                value = scheduledDaysOffset.toFloat(),
                                onValueChange = { scheduledDaysOffset = it.toInt() },
                                valueRange = 1f..14f,
                                steps = 13,
                                colors = SliderDefaults.colors(
                                    thumbColor = AccentBlue,
                                    activeTrackColor = AccentBlue,
                                    inactiveTrackColor = ColorDivider
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, content, targetAudience, campaignType, status, scheduledDaysOffset) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                modifier = Modifier.testTag("save_campaign_button")
            ) {
                Text("Launch Campaign", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = SurfaceColor,
        shape = RoundedCornerShape(20.dp)
    )
}
