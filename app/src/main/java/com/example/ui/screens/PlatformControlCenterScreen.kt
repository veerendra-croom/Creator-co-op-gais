package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodels.PlatformControlViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformControlCenterScreen(
    viewModel: PlatformControlViewModel,
    onBack: () -> Unit
) {
    var selectedMainTab by remember { mutableStateOf(0) } // 0: Settings & Flags, 1: Dynamic Content
    val settings by viewModel.platformSettings.collectAsState()
    val featureFlags by viewModel.featureFlags.collectAsState()
    
    val onboardingSlides by viewModel.onboardingSlides.collectAsState()
    val welcomeMessages by viewModel.welcomeMessages.collectAsState()
    val emptyStates by viewModel.emptyStates.collectAsState()
    val helpTexts by viewModel.helpTexts.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("platform_control_center_scaffold"),
        containerColor = PrimaryBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Platform Control",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Creator Co-Op Infrastructure",
                            color = AccentBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceColor)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main Navigation Tabs
            TabRow(
                selectedTabIndex = selectedMainTab,
                containerColor = SurfaceColor,
                contentColor = AccentBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedMainTab]),
                        color = AccentBlue
                    )
                }
            ) {
                Tab(
                    selected = selectedMainTab == 0,
                    onClick = { selectedMainTab = 0 },
                    text = { Text("Settings & Flags", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    selectedContentColor = AccentBlue,
                    unselectedContentColor = TextSecondary,
                    modifier = Modifier.testTag("tab_settings_flags")
                )
                Tab(
                    selected = selectedMainTab == 1,
                    onClick = { selectedMainTab = 1 },
                    text = { Text("Dynamic Content", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    selectedContentColor = AccentBlue,
                    unselectedContentColor = TextSecondary,
                    modifier = Modifier.testTag("tab_dynamic_content")
                )
            }

            if (selectedMainTab == 0) {
                // Settings and Flags View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Platform Settings",
                            tint = NeonEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SYSTEM SETTINGS",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }

                    // Platform Settings Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("platform_settings_card"),
                        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                        border = BorderStroke(1.dp, SurfaceLightColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            SettingToggleRow(
                                title = "Maintenance Mode",
                                description = "Locks down production workflows and displays a service interruption board for all non-admin profiles.",
                                checked = settings.maintenanceMode,
                                onCheckedChange = { viewModel.updateMaintenanceMode(it) },
                                tag = "toggle_maintenance"
                            )
                            Divider(color = SurfaceLightColor, modifier = Modifier.padding(vertical = 12.dp))
                            SettingToggleRow(
                                title = "Beta Mode",
                                description = "Enables beta cohort verification flags and unlocks candidate metrics portals.",
                                checked = settings.betaMode,
                                onCheckedChange = { viewModel.updateBetaMode(it) },
                                tag = "toggle_beta"
                            )
                            Divider(color = SurfaceLightColor, modifier = Modifier.padding(vertical = 12.dp))
                            SettingToggleRow(
                                title = "Public Registration",
                                description = "Allows new guest creators to establish co-op profiles without prior referrals.",
                                checked = settings.registrationEnabled,
                                onCheckedChange = { viewModel.updateRegistrationToggle(it) },
                                tag = "toggle_registration"
                            )
                            Divider(color = SurfaceLightColor, modifier = Modifier.padding(vertical = 12.dp))
                            SettingToggleRow(
                                title = "Invite-Only Code Restriction",
                                description = "Enforces valid onboarding referral code check during registration.",
                                checked = settings.inviteOnlyEnabled,
                                onCheckedChange = { viewModel.updateInviteOnlyToggle(it) },
                                tag = "toggle_invite_only"
                            )
                        }
                    }

                    // Feature Flags Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "Feature Toggles",
                            tint = CrispAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PLATFORM FEATURE FLAGS",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }

                    // Feature Flags Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("feature_flags_card"),
                        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                        border = BorderStroke(1.dp, SurfaceLightColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (featureFlags.isEmpty()) {
                                Text(
                                    text = "No feature flags loaded in database repository.",
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                            } else {
                                featureFlags.forEachIndexed { index, flag ->
                                    FeatureFlagRow(
                                        flag = flag,
                                        onToggle = { enabled -> viewModel.toggleFeatureFlag(flag.flagKey, enabled) }
                                    )
                                    if (index < featureFlags.size - 1) {
                                        Divider(color = SurfaceLightColor, modifier = Modifier.padding(vertical = 12.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Dynamic Content View
                DynamicContentManagementView(
                    onboardingSlides = onboardingSlides,
                    welcomeMessages = welcomeMessages,
                    emptyStates = emptyStates,
                    helpTexts = helpTexts,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(tag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = NeonEmerald,
                uncheckedThumbColor = Color.LightGray,
                uncheckedTrackColor = SurfaceLightColor
            )
        )
    }
}

@Composable
fun FeatureFlagRow(
    flag: FeatureFlag,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = flag.flagKey,
                    color = CrispAmber,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .background(
                            if (flag.isEnabled) NeonEmerald.copy(alpha = 0.15f) else TextMuted.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (flag.isEnabled) "ACTIVE" else "DISABLED",
                        color = if (flag.isEnabled) NeonEmerald else TextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = flag.description,
                color = TextPrimary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
            if (flag.lastModifiedAt > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Last sync interval: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(flag.lastModifiedAt))}",
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }
        }
        Switch(
            checked = flag.isEnabled,
            onCheckedChange = onToggle,
            modifier = Modifier.testTag("toggle_flag_${flag.flagKey}"),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = CrispAmber,
                uncheckedThumbColor = Color.LightGray,
                uncheckedTrackColor = SurfaceLightColor
            )
        )
    }
}

@Composable
fun DynamicContentManagementView(
    onboardingSlides: List<OnboardingSlide>,
    welcomeMessages: List<WelcomeMessage>,
    emptyStates: List<EmptyStateConfig>,
    helpTexts: List<HelpText>,
    viewModel: PlatformControlViewModel
) {
    var selectedContentTab by remember { mutableStateOf(0) } // 0: Slides, 1: Welcome, 2: Empty States, 3: Help Text
    
    // Dialog control states
    var showSlideDialog by remember { mutableStateOf(false) }
    var editingSlide by remember { mutableStateOf<OnboardingSlide?>(null) }

    var showWelcomeDialog by remember { mutableStateOf(false) }
    var editingWelcome by remember { mutableStateOf<WelcomeMessage?>(null) }

    var showEmptyDialog by remember { mutableStateOf(false) }
    var editingEmpty by remember { mutableStateOf<EmptyStateConfig?>(null) }

    var showHelpDialog by remember { mutableStateOf(false) }
    var editingHelp by remember { mutableStateOf<HelpText?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Sub-tabs
        ScrollableTabRow(
            selectedTabIndex = selectedContentTab,
            containerColor = SurfaceColor,
            contentColor = AccentBlue,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedContentTab]),
                    color = AccentBlue
                )
            }
        ) {
            Tab(
                selected = selectedContentTab == 0,
                onClick = { selectedContentTab = 0 },
                text = { Text("Onboarding Slides (${onboardingSlides.size})", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedContentTab == 1,
                onClick = { selectedContentTab = 1 },
                text = { Text("Welcome Banners (${welcomeMessages.size})", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedContentTab == 2,
                onClick = { selectedContentTab = 2 },
                text = { Text("Empty States (${emptyStates.size})", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedContentTab == 3,
                onClick = { selectedContentTab = 3 },
                text = { Text("Help Manuals (${helpTexts.size})", fontSize = 12.sp) }
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            when (selectedContentTab) {
                0 -> {
                    // Onboarding Slides List
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ONBOARDING FLOW MANUAL", color = AccentBlue, fontWeight = FontWeight.Black, fontSize = 12.sp)
                            Button(
                                onClick = {
                                    editingSlide = null
                                    showSlideDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.testTag("add_onboarding_slide_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Slide", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(onboardingSlides) { slide ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                    border = BorderStroke(1.dp, SurfaceLightColor)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(AccentBlue.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = (slide.stepIndex + 1).toString(),
                                                color = AccentBlue,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(slide.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(slide.description, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Vector Tag: ${slide.iconName}", color = CrispAmber, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                                        }
                                        IconButton(onClick = {
                                            editingSlide = slide
                                            showSlideDialog = true
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(onClick = { viewModel.deleteOnboardingSlide(slide.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentRed, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Welcome Messages List
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("DYNAMIC CAMPAIGN BANNERS", color = AccentBlue, fontWeight = FontWeight.Black, fontSize = 12.sp)
                            Button(
                                onClick = {
                                    editingWelcome = null
                                    showWelcomeDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.testTag("add_welcome_banner_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Banner", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(welcomeMessages) { msg ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                    border = BorderStroke(1.dp, if (msg.isActive) NeonEmerald else SurfaceLightColor)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(
                                                    if (msg.isActive) NeonEmerald.copy(alpha = 0.15f) else TextMuted.copy(alpha = 0.15f),
                                                    RoundedCornerShape(6.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Campaign,
                                                contentDescription = null,
                                                tint = if (msg.isActive) NeonEmerald else TextSecondary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(msg.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                if (msg.isActive) {
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .background(NeonEmerald.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("LIVE", color = NeonEmerald, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(msg.greeting, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
                                            if (msg.bannerImageUrl.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("URL: ${msg.bannerImageUrl}", color = AccentBlue, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                        IconButton(onClick = {
                                            editingWelcome = msg
                                            showWelcomeDialog = true
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(onClick = { viewModel.deleteWelcomeMessage(msg.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentRed, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Empty States List
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("SCREEN PLACEHOLDER DEFAULTS", color = AccentBlue, fontWeight = FontWeight.Black, fontSize = 12.sp)
                            Button(
                                onClick = {
                                    editingEmpty = null
                                    showEmptyDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.testTag("add_empty_state_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Template", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(emptyStates) { state ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                    border = BorderStroke(1.dp, SurfaceLightColor)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(CrispAmber.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Inbox,
                                                contentDescription = null,
                                                tint = CrispAmber,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("${state.screenName} Placeholder", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(state.title, color = CrispAmber, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                            Text(state.suggestion, color = TextSecondary, fontSize = 11.sp, lineHeight = 14.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Asset ID: ${state.imageTag}", color = TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 9.sp)
                                        }
                                        IconButton(onClick = {
                                            editingEmpty = state
                                            showEmptyDialog = true
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(onClick = { viewModel.deleteEmptyState(state.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentRed, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // Help Texts List
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("KNOWLEDGE BASE DRILLS", color = AccentBlue, fontWeight = FontWeight.Black, fontSize = 12.sp)
                            Button(
                                onClick = {
                                    editingHelp = null
                                    showHelpDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.testTag("add_help_text_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Topic", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(helpTexts) { help ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                    border = BorderStroke(1.dp, SurfaceLightColor)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(AccentBlue.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.AutoStories,
                                                contentDescription = null,
                                                tint = AccentBlue,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(help.topicKey, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .background(SurfaceLightColor, RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text(help.category, color = TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(help.textContent, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
                                        }
                                        IconButton(onClick = {
                                            editingHelp = help
                                            showHelpDialog = true
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(onClick = { viewModel.deleteHelpText(help.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentRed, modifier = Modifier.size(18.dp))
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

    // Dialogue Boxes for CRUD Editing
    
    if (showSlideDialog) {
        var title by remember { mutableStateOf(editingSlide?.title ?: "") }
        var description by remember { mutableStateOf(editingSlide?.description ?: "") }
        var stepIndex by remember { mutableStateOf(editingSlide?.stepIndex?.toString() ?: "0") }
        var iconName by remember { mutableStateOf(editingSlide?.iconName ?: "Star") }

        Dialog(onDismissRequest = { showSlideDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                border = BorderStroke(1.dp, SurfaceLightColor),
                modifier = Modifier.padding(16.dp).fillMaxWidth().testTag("slide_crud_dialog")
            ) {
                Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = if (editingSlide == null) "New Onboarding Slide" else "Edit Onboarding Slide", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Slide Title") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentBlue, focusedLabelColor = AccentBlue),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Slide Description") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentBlue, focusedLabelColor = AccentBlue),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    OutlinedTextField(
                        value = stepIndex,
                        onValueChange = { stepIndex = it },
                        label = { Text("Step Order Index (0, 1, 2...)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentBlue, focusedLabelColor = AccentBlue),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = iconName,
                        onValueChange = { iconName = it },
                        label = { Text("Vector Graphic Tag (e.g. Star, Hub)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentBlue, focusedLabelColor = AccentBlue),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showSlideDialog = false }) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val indexVal = stepIndex.toIntOrNull() ?: 0
                                viewModel.upsertOnboardingSlide(
                                    OnboardingSlide(
                                        id = editingSlide?.id ?: "",
                                        title = title,
                                        description = description,
                                        stepIndex = indexVal,
                                        iconName = iconName
                                    )
                                )
                                showSlideDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }

    if (showWelcomeDialog) {
        var title by remember { mutableStateOf(editingWelcome?.title ?: "") }
        var greeting by remember { mutableStateOf(editingWelcome?.greeting ?: "") }
        var bannerImageUrl by remember { mutableStateOf(editingWelcome?.bannerImageUrl ?: "") }
        var isActive by remember { mutableStateOf(editingWelcome?.isActive ?: true) }

        Dialog(onDismissRequest = { showWelcomeDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                border = BorderStroke(1.dp, SurfaceLightColor),
                modifier = Modifier.padding(16.dp).fillMaxWidth().testTag("welcome_crud_dialog")
            ) {
                Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = if (editingWelcome == null) "New Banner Message" else "Edit Banner Message", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Campaign Header") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentBlue, focusedLabelColor = AccentBlue),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = greeting,
                        onValueChange = { greeting = it },
                        label = { Text("Body Greeting / Alert text") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentBlue, focusedLabelColor = AccentBlue),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    OutlinedTextField(
                        value = bannerImageUrl,
                        onValueChange = { bannerImageUrl = it },
                        label = { Text("Unsplash Decorative Banner Image URL") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentBlue, focusedLabelColor = AccentBlue),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active Campaign", color = TextPrimary, fontSize = 13.sp)
                        Switch(checked = isActive, onCheckedChange = { isActive = it })
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showWelcomeDialog = false }) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.upsertWelcomeMessage(
                                    WelcomeMessage(
                                        id = editingWelcome?.id ?: "",
                                        title = title,
                                        greeting = greeting,
                                        bannerImageUrl = bannerImageUrl,
                                        isActive = isActive
                                    )
                                )
                                showWelcomeDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)
                        ) {
                            Text("Publish")
                        }
                    }
                }
            }
        }
    }

    if (showEmptyDialog) {
        var screenName by remember { mutableStateOf(editingEmpty?.screenName ?: "") }
        var imageTag by remember { mutableStateOf(editingEmpty?.imageTag ?: "") }
        var title by remember { mutableStateOf(editingEmpty?.title ?: "") }
        var suggestion by remember { mutableStateOf(editingEmpty?.suggestion ?: "") }

        Dialog(onDismissRequest = { showEmptyDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                border = BorderStroke(1.dp, SurfaceLightColor),
                modifier = Modifier.padding(16.dp).fillMaxWidth().testTag("empty_crud_dialog")
            ) {
                Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = if (editingEmpty == null) "New Empty Placeholder" else "Edit Empty Placeholder", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    
                    OutlinedTextField(
                        value = screenName,
                        onValueChange = { screenName = it },
                        label = { Text("Screen Identity (e.g. Commons, Portfolios)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentBlue, focusedLabelColor = AccentBlue),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Placeholder Heading Text") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentBlue, focusedLabelColor = AccentBlue),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = suggestion,
                        onValueChange = { suggestion = it },
                        label = { Text("Action Hint / Suggested next step") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentBlue, focusedLabelColor = AccentBlue),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    OutlinedTextField(
                        value = imageTag,
                        onValueChange = { imageTag = it },
                        label = { Text("Decorative Asset Name") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentBlue, focusedLabelColor = AccentBlue),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showEmptyDialog = false }) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.upsertEmptyState(
                                    EmptyStateConfig(
                                        id = editingEmpty?.id ?: "",
                                        screenName = screenName,
                                        imageTag = imageTag,
                                        title = title,
                                        suggestion = suggestion
                                    )
                                )
                                showEmptyDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)
                        ) {
                            Text("Deploy")
                        }
                    }
                }
            }
        }
    }

    if (showHelpDialog) {
        var topicKey by remember { mutableStateOf(editingHelp?.topicKey ?: "") }
        var textContent by remember { mutableStateOf(editingHelp?.textContent ?: "") }
        var category by remember { mutableStateOf(editingHelp?.category ?: "") }

        Dialog(onDismissRequest = { showHelpDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                border = BorderStroke(1.dp, SurfaceLightColor),
                modifier = Modifier.padding(16.dp).fillMaxWidth().testTag("help_crud_dialog")
            ) {
                Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = if (editingHelp == null) "New Help Topic" else "Edit Help Topic", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    
                    OutlinedTextField(
                        value = topicKey,
                        onValueChange = { topicKey = it },
                        label = { Text("Topic Header (e.g. Smart Splits)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentBlue, focusedLabelColor = AccentBlue),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = textContent,
                        onValueChange = { textContent = it },
                        label = { Text("Help / Instructional content description") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentBlue, focusedLabelColor = AccentBlue),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Manual Category (e.g. Syndicates, Security)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentBlue, focusedLabelColor = AccentBlue),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showHelpDialog = false }) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.upsertHelpText(
                                    HelpText(
                                        id = editingHelp?.id ?: "",
                                        topicKey = topicKey,
                                        textContent = textContent,
                                        category = category
                                    )
                                )
                                showHelpDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)
                        ) {
                            Text("Commit")
                        }
                    }
                }
            }
        }
    }
}
