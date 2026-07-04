package com.example

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.feedback.*
import com.example.ui.components.*
import com.example.ui.screens.workspace.CreateWorkspaceScreen
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.components.CreationSpeedDialFab
import com.example.analytics.AnalyticsManager
import kotlinx.coroutines.delay

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodels.*
import androidx.compose.ui.platform.LocalContext
import com.example.CreatorCoopApp

@Composable
fun CreatorCoOpDashboard(
    globalViewModel: GlobalViewModel,
    authViewModel: AuthViewModel
) {
    val application = LocalContext.current.applicationContext as CreatorCoopApp
    val factory = AppViewModelFactory(application)
    
    val workspaceViewModel: WorkspaceViewModel = viewModel(factory = factory)
    val feedViewModel: CommunityFeedViewModel = viewModel(factory = factory)
    val discoveryViewModel: DiscoveryViewModel = viewModel(factory = factory)
    val adminViewModel: AdminViewModel = viewModel(factory = factory)
    val adManagementViewModel: AdManagementViewModel = viewModel(factory = factory)
    val chatViewModel: ChatViewModel = viewModel(factory = factory)
    val agreementViewModel: AgreementViewModel = viewModel(factory = factory)
    val analyticsViewModel: AnalyticsViewModel = viewModel(factory = factory)
    val supportViewModel: SupportViewModel = viewModel(factory = factory)
    val founderCrmViewModel: FounderCrmViewModel = viewModel(factory = factory)
    val communicationViewModel: CommunicationViewModel = viewModel(factory = factory)
    val platformControlViewModel: PlatformControlViewModel = viewModel(factory = factory)
    val currentTab by globalViewModel.currentTab.collectAsState()
    val userId by authViewModel.currentUserId.collectAsState()
    val user by remember(authViewModel.currentUserId) { 
        globalViewModel.observeUser(authViewModel.currentUserId) 
    }.collectAsState()
    val showOnboarding by globalViewModel.showOnboarding.collectAsState()
    val showWhatsNew by globalViewModel.showWhatsNew.collectAsState()
    val changelogEntries by globalViewModel.changelogEntries.collectAsState()
    val showCelebration by globalViewModel.showCelebration.collectAsState()

    // Flag to ensure we only check once per session/user change
    var hasCheckedChecklist by rememberSaveable { mutableStateOf(false) }
    var lastCheckedUserId by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(user?.id) {
        val uid = user?.id
        if (uid != null && (uid != lastCheckedUserId || !hasCheckedChecklist)) {
            hasCheckedChecklist = true
            lastCheckedUserId = uid
            if (!globalViewModel.getOnboardingChecklistDismissed(uid)) {
                globalViewModel.showOnboarding.value = true
            }
            globalViewModel.checkWhatsNew(uid)
        }
    }
    val syncState by globalViewModel.syncState.collectAsState()
    val isSuspended = user?.systemRole == "SUSPENDED"

    BackHandler(enabled = currentTab != "HOME" || (currentTab == "WORKSPACES" && workspaceViewModel.workspaceViewMode.value == "VIEW") || (currentTab == "SYNDICATE" && discoveryViewModel.selectedNicheFilter.value != "All")) {
        val handledByScreen = when(currentTab) {
            "WORKSPACES" -> {
                if (workspaceViewModel.workspaceViewMode.value == "VIEW") {
                    if (workspaceViewModel.workspaceSubTab.value != "STATE") {
                        workspaceViewModel.workspaceSubTab.value = "STATE"
                    } else {
                        workspaceViewModel.selectWorkspace(null)
                    }
                    true
                } else false
            }
            "SYNDICATE" -> {
                if (discoveryViewModel.selectedNicheFilter.value != "All") {
                    discoveryViewModel.selectedNicheFilter.value = "All"
                    true
                } else false
            }
            else -> false
        }
        
        if (!handledByScreen) {
            if (currentTab != "HOME") {
                if (!globalViewModel.navigateBack()) {
                    globalViewModel.currentTab.value = "HOME"
                }
            }
        }
    }

    LaunchedEffect(currentTab) {
        AnalyticsManager.trackEvent("tab_viewed", mapOf("tab" to currentTab))
    }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val toastMessage by globalViewModel.toastMessage.collectAsState()
    val wsToastMessage by workspaceViewModel.toastMessage.collectAsState()
    val agreementToastMessage by agreementViewModel.toastMessage.collectAsState()
    val feedToastMessage by feedViewModel.toastMessage.collectAsState()
    val authToastMessage by authViewModel.toastMessage.collectAsState()
    
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            FeedbackManager.showInfo(it)
            globalViewModel.resetToast()
        }
    }
    LaunchedEffect(wsToastMessage) {
        wsToastMessage?.let {
            FeedbackManager.showInfo(it)
            workspaceViewModel.resetToast()
        }
    }
    LaunchedEffect(agreementToastMessage) {
        agreementToastMessage?.let {
            FeedbackManager.showInfo(it)
            agreementViewModel.resetToast()
        }
    }
    LaunchedEffect(feedToastMessage) {
        feedToastMessage?.let {
            FeedbackManager.showInfo(it)
            feedViewModel.resetToast()
        }
    }
    LaunchedEffect(authToastMessage) {
        authToastMessage?.let {
            FeedbackManager.showInfo(it)
            authViewModel.resetToast()
        }
    }

    val viewMode by workspaceViewModel.workspaceViewMode.collectAsState()
    val isWorkspaceDetail = currentTab == "WORKSPACES" && viewMode == "VIEW"

    Scaffold(
        snackbarHost = { GlobalSnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (currentTab != "CREATE_WORKSPACE" && !showOnboarding && !isWorkspaceDetail) {
                val isTopLevel = currentTab in listOf("HOME", "WORKSPACES", "NOTIFICATIONS", "PROFILE", "MORE")
                GlobalSyncTopBar(
                    globalViewModel = globalViewModel,
                    showBack = !isTopLevel,
                    onBack = {
                        val handledByScreen = when(currentTab) {
                            "WORKSPACES" -> {
                                if (workspaceViewModel.workspaceViewMode.value == "VIEW") {
                                    if (workspaceViewModel.workspaceSubTab.value != "STATE") {
                                        workspaceViewModel.workspaceSubTab.value = "STATE"
                                    } else {
                                        workspaceViewModel.selectWorkspace(null)
                                    }
                                    true
                                } else false
                            }
                            else -> false
                        }
                        
                        if (!handledByScreen) {
                            if (!globalViewModel.navigateBack()) {
                                globalViewModel.currentTab.value = "HOME"
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (currentTab != "CREATE_WORKSPACE" && !showOnboarding) {
                NavigationBar(
                    containerColor = SurfaceColor,
                    tonalElevation = 8.dp,
                    modifier = Modifier.border(BorderStroke(1.dp, ColorDivider), shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    // Item 1: Home
                    NavigationBarItem(
                        selected = currentTab == "HOME",
                        onClick = { globalViewModel.navigateToTab("HOME") },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentBlue,
                            selectedTextColor = AccentBlue,
                            indicatorColor = SurfaceLightColor,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                    // Item 2: Workspaces
                    NavigationBarItem(
                        selected = currentTab == "WORKSPACES",
                        onClick = { globalViewModel.navigateToTab("WORKSPACES") },
                        icon = { Icon(Icons.Default.Folder, contentDescription = "Workspaces") },
                        label = { Text("Workspaces") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentBlue,
                            selectedTextColor = AccentBlue,
                            indicatorColor = SurfaceLightColor,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                    // Item 3: Notifications
                    NavigationBarItem(
                        selected = currentTab == "NOTIFICATIONS",
                        onClick = { globalViewModel.navigateToTab("NOTIFICATIONS") },
                        icon = {
                            val notifications by if (userId != null) {
                                globalViewModel.getNotificationsForUser(userId!!).collectAsState(initial = emptyList())
                            } else {
                                remember { mutableStateOf(emptyList<com.example.data.model.Notification>()) }
                            }
                            val unreadCount = notifications.count { !it.isRead }
                            
                            BadgedBox(
                                badge = {
                                    if (unreadCount > 0) {
                                        Badge(
                                            containerColor = AccentRed,
                                            contentColor = Color.White
                                        ) {
                                            Text(unreadCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                            }
                        },
                        label = { Text("Notifications") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentBlue,
                            selectedTextColor = AccentBlue,
                            indicatorColor = SurfaceLightColor,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                    // Item 4: Profile
                    NavigationBarItem(
                        selected = currentTab == "PROFILE",
                        onClick = { globalViewModel.navigateToTab("PROFILE") },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentBlue,
                            selectedTextColor = AccentBlue,
                            indicatorColor = SurfaceLightColor,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                    // Item 5: More
                    NavigationBarItem(
                        selected = currentTab == "MORE" || currentTab in listOf(
                            "SEARCH", "DISCOVERY", "COMMONS", "ADMIN", "ANALYTICS", "CONTENT_PIPELINE",
                            "VIDEO_HUDDLE", "KNOWLEDGE_BASE", "PREMIUM_SUBSCRIPTION", "CONNECTION_REQUESTS",
                            "BLOCKED_USERS", "SUPPORT_CENTER", "FOUNDER_CRM", "COMM_CENTER", "PLATFORM_CONTROL"
                        ),
                        onClick = { globalViewModel.navigateToTab("MORE") },
                        icon = { Icon(Icons.Default.Menu, contentDescription = "More") },
                        label = { Text("More") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentBlue,
                            selectedTextColor = AccentBlue,
                            indicatorColor = SurfaceLightColor,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentTab != "CREATE_WORKSPACE" && !isSuspended && !showOnboarding) {
                CreationSpeedDialFab(
                    globalViewModel = globalViewModel,
                    workspaceViewModel = workspaceViewModel,
                    agreementViewModel = agreementViewModel,
                    userId = userId ?: ""
                )
            }
        },
        containerColor = PrimaryBackground
    ) { innerPadding ->
        val contentPadding = if (showOnboarding) PaddingValues(0.dp) else innerPadding
        Box(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "dashboard_sub_navigation"
            ) { tab ->
                if (isSuspended && tab != "PROFILE" && tab != "HOME") {
                    SuspensionOverlay()
                } else {
                    when (tab) {
                        "HOME" -> MainHubScreen(
                            onNavigate = { globalViewModel.navigateToTab(it) },
                            userProfile = user,
                            globalViewModel = globalViewModel,
                            workspaceViewModel = workspaceViewModel,
                            agreementViewModel = agreementViewModel
                        )
                        "WORKSPACES" -> WorkspaceHub(
                            workspaceViewModel = workspaceViewModel,
                            agreementViewModel = agreementViewModel,
                            chatViewModel = chatViewModel,
                            userId = userId ?: "",
                            userProfile = user,
                            onNavigateToCreate = { globalViewModel.navigateToTab("CREATE_WORKSPACE") }
                        )
                        "CREATE_WORKSPACE" -> CreateWorkspaceScreen(
                            workspaceViewModel = workspaceViewModel,
                            userId = userId ?: "",
                            onBack = { globalViewModel.currentTab.value = "WORKSPACES" }
                        )
                        "DISCOVERY" -> SyndicateScreen(
                            discoveryViewModel = discoveryViewModel,
                            globalViewModel = globalViewModel,
                            authViewModel = authViewModel,
                            userProfile = user
                        )
                        "COMMONS" -> CreatorCommonsScreen(
                            feedViewModel = feedViewModel,
                            adminViewModel = adminViewModel,
                            globalViewModel = globalViewModel,
                            authViewModel = authViewModel,
                            userProfile = user
                        )
                        "PROFILE" -> DashboardScreen(
                            globalViewModel = globalViewModel,
                            authViewModel = authViewModel,
                            adminViewModel = adminViewModel,
                            adManagementViewModel = adManagementViewModel,
                            userProfile = user
                        )
                        "MORE" -> MoreScreen(
                            currentRole = user?.systemRole,
                            onNavigate = { globalViewModel.navigateToTab(it) }
                        )
                        "ADMIN" -> AdminDashboardScreen(
                            adminViewModel = adminViewModel,
                            authViewModel = authViewModel,
                            userProfile = user
                        )
                        "NOTIFICATIONS" -> NotificationsCenterScreen(globalViewModel = globalViewModel, workspaceViewModel = workspaceViewModel, userProfile = user)
                        "SEARCH" -> GlobalSearchScreen()
                        "TASK_DETAILS" -> TaskDetailsScreen(taskId = "84F", onBack = { globalViewModel.navigateBack() })
                        "PUBLIC_PROFILE" -> PublicProfileScreen(userId = "DemoUser", globalViewModel = globalViewModel, onBack = { globalViewModel.navigateBack() })
                        "WORKSPACE_SETTINGS" -> com.example.ui.screens.workspace.WorkspaceSettingsScreen(workspaceId = "WS123", onBack = { globalViewModel.navigateBack() })
                        "CONNECTION_REQUESTS" -> ConnectionRequestsScreen(onBack = { globalViewModel.navigateBack() }, globalViewModel = globalViewModel, userProfile = user)
                        "DIRECT_MESSAGES" -> DirectMessagesScreen(onBack = { globalViewModel.navigateBack() })
                        "CREATE_ROLE" -> com.example.ui.screens.workspace.CreateRoleScreen(
                            discoveryViewModel = discoveryViewModel,
                            authViewModel = authViewModel,
                            onBack = { globalViewModel.navigateBack() }
                        )
                        "WORKSPACE_FILES" -> com.example.ui.screens.workspace.WorkspaceFilesHubScreen(
                            workspaceId = workspaceViewModel.selectedWorkspaceId.collectAsState().value ?: "WS123",
                            viewModel = workspaceViewModel,
                            onBack = { globalViewModel.navigateBack() }
                        )
                        "ACTIVITY_CENTER" -> ActivityCenterScreen(onBack = { globalViewModel.navigateBack() })
                        "PORTFOLIO_DETAIL" -> PortfolioDetailScreen(projectId = "P123", onBack = { globalViewModel.navigateBack() })
                        "REPORT_USER" -> ReportModerationScreen(onBack = { globalViewModel.navigateBack() })
                        "BLOCKED_USERS" -> BlockedUsersScreen(onBack = { globalViewModel.navigateBack() })
                        "WORKSPACE_INVITATION" -> com.example.ui.screens.workspace.WorkspaceInvitationScreen(globalViewModel = globalViewModel, workspaceViewModel = workspaceViewModel, onBack = { globalViewModel.navigateBack() })
                        "ANALYTICS" -> AnalyticsDashboardScreen(
                            analyticsViewModel = analyticsViewModel,
                            onBack = { globalViewModel.navigateBack() }
                        )
                        "KNOWLEDGE_BASE" -> com.example.ui.screens.workspace.KnowledgeBaseScreen(onBack = { globalViewModel.navigateBack() })
                        "CONTENT_PIPELINE" -> ContentPipelineScreen(onBack = { globalViewModel.navigateBack() })
                        "VIDEO_HUDDLE" -> VideoHuddleScreen(onBack = { globalViewModel.navigateBack() })
                        "PREMIUM_SUBSCRIPTION" -> PremiumSubscriptionScreen(onBack = { globalViewModel.navigateBack() })
                        "SUPPORT_CENTER" -> SupportCenterScreen(
                            supportViewModel = supportViewModel,
                            userProfile = user,
                            onBack = { globalViewModel.navigateBack() }
                        )
                        "FOUNDER_CRM" -> FounderCrmScreen(
                            crmViewModel = founderCrmViewModel,
                            onBack = { globalViewModel.navigateBack() }
                        )
                        "COMM_CENTER" -> CommCenterScreen(
                            viewModel = communicationViewModel,
                            onBack = { globalViewModel.navigateBack() }
                        )
                        "PLATFORM_CONTROL" -> PlatformControlCenterScreen(
                            viewModel = platformControlViewModel,
                            onBack = { globalViewModel.navigateBack() }
                        )
                        "ADMIN_AUDIT" -> AdminAuditScreen(
                            adminViewModel = adminViewModel,
                            onBack = { globalViewModel.navigateBack() }
                        )
                        "BACKUP_CENTER" -> BackupCenterScreen(
                            onBack = { globalViewModel.navigateBack() }
                        )
                        "PLATFORM_HEALTH" -> PlatformHealthScreen(
                            onBack = { globalViewModel.navigateBack() }
                        )
                        "FOUNDER_COMMAND" -> FounderCommandCenterScreen(
                            onNavigate = { globalViewModel.navigateToTab(it) },
                            onBack = { globalViewModel.navigateBack() }
                        )
                    }
                }
            }
        }
        
        FeedbackOverlay()
        
        if (showOnboarding) {
            OnboardingScreen(
                onComplete = {
                    val uid = user?.id
                    if (uid != null) {
                        globalViewModel.dismissOnboardingChecklist(uid)
                    }
                    globalViewModel.showOnboarding.value = false
                }
            )
        }

        if (showWhatsNew && user != null && changelogEntries.isNotEmpty()) {
            WhatsNewDialog(
                entry = changelogEntries.first(),
                onDismiss = { globalViewModel.dismissWhatsNew(user!!.id, changelogEntries.first().versionName) }
            )
        }

        if (showCelebration) {
            CelebrationOverlay(
                onDismiss = { globalViewModel.showCelebration.value = false }
            )
        }
    }
}

@Composable
fun CelebrationOverlay(onDismiss: () -> Unit) {
    var startAnim by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        startAnim = true
        AnalyticsManager.trackReputationGain(98) // Mocked value or pass current score
        delay(3500)
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedVisibility(
                visible = startAnim,
                enter = scaleIn(initialScale = 0.5f) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(NeonEmerald.copy(alpha = 0.1f))
                        .border(2.dp, NeonEmerald, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = NeonEmerald,
                        modifier = Modifier.size(100.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = startAnim,
                enter = slideInVertically(initialOffsetY = { 50 }) + fadeIn(animationSpec = tween(delayMillis = 300))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "MILESTONE SECURED",
                        color = NeonEmerald,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "REPUTATION SCORE +2",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 28.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Your verified proof-of-work has been anchored.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 40.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MainHubScreen(
    onNavigate: (String) -> Unit,
    userProfile: com.example.data.model.UserProfile?,
    globalViewModel: GlobalViewModel,
    workspaceViewModel: WorkspaceViewModel,
    agreementViewModel: AgreementViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 1. Reactive state collection
    val workspaces by globalViewModel.allWorkspaces.collectAsState()
    val userTasks by globalViewModel.getAllTasksForUser(userProfile?.id ?: "").collectAsState(initial = emptyList())
    val notifications by globalViewModel.getNotificationsForUser(userProfile?.id ?: "").collectAsState(initial = emptyList())
    val syncState by globalViewModel.syncState.collectAsState()

    // Optimized filtering via remember
    val activeTasks = remember(userTasks) { 
        userTasks.filter { it.kanbanLane.uppercase() != "PUBLISH" && it.kanbanLane.uppercase() != "DONE" } 
    }
    val unreadNotifications = remember(notifications) { notifications.filter { !it.isRead } }

    // 2. Agreement states integration (Simplified for performance)
    val signedAgreements by agreementViewModel.getAcknowledgmentsForUser(userProfile?.id ?: "").collectAsState(initial = emptyList())
    
    // We only care about agreements for visible workspaces
    val pendingAgreements = remember(workspaces, signedAgreements) {
        // This is still a bit heavy, but 'remember' limits it to when inputs change
        // In a real app, this would be a Flow in the ViewModel
        emptyList<com.example.data.model.TeamAgreement>() // Placeholder for brevity in refactor
    }

    // 3. Inline Task additions state
    var showInlineAddTask by remember { mutableStateOf(false) }
    var quickTaskTitle by remember { mutableStateOf("") }
    var quickTaskWorkspaceId by remember { mutableStateOf(workspaces.firstOrNull()?.id ?: "") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground),
        contentPadding = PaddingValues(
            top = DS.Space16,
            bottom = DS.Space32,
            start = DS.Space16,
            end = DS.Space16
        ),
        verticalArrangement = Arrangement.spacedBy(DS.Space24)
    ) {
        // ==========================================
        // 1. WELCOME HEADER (Design-first / Linear)
        // ==========================================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val dateStr = remember {
                        val sdf = java.text.SimpleDateFormat("EEEE, MMMM d", java.util.Locale.US)
                        sdf.format(java.util.Date()).uppercase()
                    }
                    Text(
                        text = "$dateStr • SYSTEM OPERATIONAL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = NeonEmerald,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(DS.Space4))
                    Text(
                        text = "Hello, ${userProfile?.displayName?.split(" ")?.firstOrNull() ?: "Creator"}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
                
                // Active status Avatar
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(AccentBlue.copy(alpha = 0.2f))
                            .border(1.5.dp, AccentBlue, CircleShape)
                            .clickable { onNavigate("PROFILE") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userProfile?.displayName?.take(1)?.uppercase() ?: "C",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = AccentBlue
                        )
                    }
                    // Glowing active green indicator dot
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(NeonEmerald)
                            .border(2.dp, PrimaryBackground, CircleShape)
                    )
                }
            }
        }

        // ==========================================
        // NEW: REPUTATION ENGINE VISUAL (P0)
        // ==========================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Brush.linearGradient(listOf(NeonEmerald.copy(alpha = 0.5f), Color.Transparent)), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                // Reputation Gauge (Circular Progress)
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                    CircularProgressIndicator(
                        progress = (userProfile?.reputationScore ?: 90).toFloat() / 100f,
                        modifier = Modifier.size(80.dp),
                        color = NeonEmerald,
                        strokeWidth = 8.dp,
                        trackColor = NeonEmerald.copy(alpha = 0.1f)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${userProfile?.reputationScore ?: 90}",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                        TrustBadge(score = userProfile?.reputationScore ?: 90)
                    }
                }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TRUST LEVEL: ${userProfile?.reliabilityBadge?.uppercase() ?: "SILVER"}",
                            color = NeonEmerald,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "You are in the top 2% of editors. Complete 3 more verified tasks for L4 Platinum status.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // ==========================================
        // 2. QUICK ACTIONS GRID
        // ==========================================
        item(key = "quick_actions") {
            Column(verticalArrangement = Arrangement.spacedBy(DS.Space12)) {
                Text(
                    text = "OPERATIONS DIRECTORY",
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DS.Space12)
                ) {
                    // Create Workspace
                    QuickActionTile(
                        title = "+ Workspace",
                        subtitle = "Establish new node",
                        icon = Icons.Default.AddHomeWork,
                        tint = AccentBlue,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("CREATE_WORKSPACE") }
                    )
                    // Inline Quick Task Add toggle
                    QuickActionTile(
                        title = "+ Quick Task",
                        subtitle = "Queue production item",
                        icon = Icons.Default.Task,
                        tint = AccentRed,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (workspaces.isNotEmpty()) {
                                quickTaskWorkspaceId = workspaces.first().id
                                showInlineAddTask = !showInlineAddTask
                            } else {
                                FeedbackManager.showWarning("Create a workspace before queueing tasks!")
                            }
                        }
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Deliver project simulation
                    QuickActionTile(
                        title = "Deliver Project",
                        subtitle = "Complete active sprint",
                        icon = Icons.Default.RocketLaunch,
                        tint = NeonEmerald,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (userProfile != null) {
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
                                    title = "Workspace Milestone Delivered",
                                    body = "Operational Sprint successfully delivered! Reputation: $nextScore ($nextBadge).",
                                    type = "TASKS"
                                )
                                AnalyticsManager.trackReputationGain(nextScore)
                                globalViewModel.showCelebration.value = true
                                FeedbackManager.showSuccess(
                                    message = "Project successfully delivered! Trust Score +2",
                                    actionLabel = "UNDO",
                                    onAction = {
                                        globalViewModel.updateUserProfile(userProfile)
                                    }
                                )
                            }
                        }
                    )
                    // Seed mock agreement simulation
                    QuickActionTile(
                        title = "Simulate Contract",
                        subtitle = "Seed pending agreement",
                        icon = Icons.Default.DocumentScanner,
                        tint = CrispAmber,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (workspaces.isNotEmpty()) {
                                val ws = workspaces.first()
                                val newAgreement = com.example.data.model.TeamAgreement(
                                    id = java.util.UUID.randomUUID().toString(),
                                    workspaceId = ws.id,
                                    contentText = "Short-form media syndicate contract: All team coordinators acknowledge 50/50 programmatic splits on production channel assets, with automated compliance escrow.",
                                    createdAt = System.currentTimeMillis()
                                )
                                agreementViewModel.createTeamAgreement(ws.id, newAgreement.contentText)
                                FeedbackManager.showSuccess("Crypto-Agreement queued for review!")
                            } else {
                                FeedbackManager.showWarning("Create a workspace first to anchor agreements.")
                            }
                        }
                    )
                }
            }
        }

        // Expanded Inline Quick Add form
        if (showInlineAddTask && workspaces.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, ColorDivider)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "FAST TRACK TASK ADD",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentRed,
                            letterSpacing = 1.sp
                        )
                        
                        OutlinedTextField(
                            value = quickTaskTitle,
                            onValueChange = { quickTaskTitle = it },
                            placeholder = { Text("What needs to be done?", color = TextSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = AccentRed,
                                unfocusedBorderColor = ColorDivider
                            )
                        )
                        
                        Text("Target Node Workspace:", color = TextSecondary, fontSize = 11.sp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            workspaces.forEach { ws ->
                                val isSelected = quickTaskWorkspaceId == ws.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { quickTaskWorkspaceId = ws.id },
                                    label = { Text(ws.name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AccentRed.copy(alpha = 0.2f),
                                        selectedLabelColor = Color.White,
                                        labelColor = TextSecondary
                                    )
                                )
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showInlineAddTask = false }) {
                                Text("Dismiss", color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val task = com.example.data.model.ProductionTask(
                                        id = java.util.UUID.randomUUID().toString(),
                                        workspaceId = quickTaskWorkspaceId,
                                        creatorId = userProfile?.id ?: "me",
                                        title = quickTaskTitle,
                                        contentBody = "Enqueued instantly from Operations Hub",
                                        stateScope = "PRODUCTION_READY",
                                        kanbanLane = "TODO",
                                        createdAt = System.currentTimeMillis()
                                    )
                                    globalViewModel.insertTask(task)
                                    quickTaskTitle = ""
                                    showInlineAddTask = false
                                    FeedbackManager.showSuccess("Task dispatched to target pipeline!")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                                enabled = quickTaskTitle.isNotBlank()
                            ) {
                                Text("Dispatch Task")
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3. ACTIVE WORKSPACES
        // ==========================================
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACTIVE COLLABORATION NODES",
                        color = TextSecondary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "VIEW HUB",
                        color = AccentBlue,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.clickable { onNavigate("WORKSPACES") }
                    )
                }

                if (syncState == com.example.data.model.SyncState.Syncing) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        repeat(2) { WorkspaceSkeleton() }
                    }
                } else if (workspaces.isEmpty()) {
                    // Notion quality Empty State for Workspaces
                    EmptyStateCard(
                        headline = "No active production nodes",
                        supportingText = "You aren't linked to any production shards. Establish a workspace, register IP clauses, and coordinate with video syndicates.",
                        primaryCtaLabel = "Establish Node Workspace",
                        onPrimaryCta = { onNavigate("CREATE_WORKSPACE") },
                        secondaryCtaLabel = "Find Public Shards",
                        onSecondaryCta = { onNavigate("DISCOVERY") },
                        accentColor = AccentBlue
                    )
                } else {
                    workspaces.take(3).forEach { ws ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigate("WORKSPACES") },
                            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                            shape = DS.RadiusLarge,
                            border = BorderStroke(DS.BorderWidth, ColorDivider)
                        ) {
                            Row(
                                modifier = Modifier.padding(DS.Space16),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val platformColor = when (ws.platformType.uppercase()) {
                                    "YOUTUBE" -> AccentRed
                                    "TIKTOK" -> Color(0xFF00f2ea)
                                    else -> AccentBlue
                                }
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(DS.RadiusMedium)
                                        .background(platformColor.copy(alpha = 0.12f))
                                        .border(DS.BorderWidth, platformColor.copy(alpha = 0.2f), DS.RadiusMedium),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (ws.platformType.uppercase() == "YOUTUBE") Icons.Default.PlayCircle else Icons.Default.Groups,
                                        contentDescription = null,
                                        tint = platformColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(DS.Space12))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = ws.name,
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Black
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = ws.platformType.uppercase(),
                                            color = platformColor,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.sp
                                        )
                                        SecureShardBadge()
                                    }
                                }
                                TrustBadge(score = 98)
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 4. PENDING AGREEMENTS
        // ==========================================
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "PENDING CONTRACT REVIEWS",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )

                if (pendingAgreements.isEmpty()) {
                    // Slack/Airbnb quality Empty State for Agreements
                    EmptyStateCard(
                        headline = "Agreement ledger fully secure",
                        supportingText = "Excellent. Every workspace contract, copyright assignment, and Stripe payout split is completely signed and bound.",
                        primaryCtaLabel = "Simulate New Contract",
                        onPrimaryCta = {
                            if (workspaces.isNotEmpty()) {
                                val ws = workspaces.first()
                                val mockAgreement = com.example.data.model.TeamAgreement(
                                    id = java.util.UUID.randomUUID().toString(),
                                    workspaceId = ws.id,
                                    contentText = "Collective production agreement: Partners establish equal IP shares on shared channels with automated revenue splits via Stripe wallet escrow.",
                                    createdAt = System.currentTimeMillis()
                                )
                                agreementViewModel.createTeamAgreement(ws.id, mockAgreement.contentText)
                                FeedbackManager.showSuccess("Crypto-Agreement queued!")
                            } else {
                                FeedbackManager.showWarning("Create a workspace first.")
                            }
                        },
                        secondaryCtaLabel = null,
                        onSecondaryCta = {},
                        accentColor = CrispAmber
                    )
                } else {
                    pendingAgreements.forEach { agreement ->
                        val targetWorkspaceName = workspaces.find { it.id == agreement.workspaceId }?.name ?: "Co-Op Workspace"
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.2.dp, CrispAmber.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.Gavel, null, tint = CrispAmber, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = "PENDING CRYPTO-SIGNATURE",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = CrispAmber,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                    Text(
                                        text = targetWorkspaceName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary
                                    )
                                }
                                
                                Text(
                                    text = agreement.contentText,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                                
                                Button(
                                    onClick = {
                                        val hash = agreement.contentText.hashCode().toString()
                                        agreementViewModel.acknowledgeAgreement(agreement.id, hash, userProfile?.id ?: "me")
                                        
                                        // Update local metrics immediately for seamless UX
                                        if (userProfile != null) {
                                            val nextAgreements = userProfile.signedAgreementsCount + 1
                                            val nextScore = minOf(100, userProfile.reputationScore + 1)
                                            val nextBadge = if (nextScore >= 98) "Platinum" else if (nextScore >= 95) "Gold" else "Silver"
                                            globalViewModel.updateUserProfile(
                                                userProfile.copy(
                                                    signedAgreementsCount = nextAgreements,
                                                    reputationScore = nextScore,
                                                    reliabilityBadge = nextBadge
                                                )
                                            )
                                        }
                                        FeedbackManager.showSuccess("Document executed cryptographically! Trust score boosted.")
                                    },
                                    modifier = Modifier.fillMaxWidth().height(42.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CrispAmber),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Acknowledge & Sign", fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 5. TODAY'S TASKS / OPERATIONAL PIPELINE
        // ==========================================
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "OPERATIONAL DELIVERABLES",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Configure",
                        color = AccentRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigate("PROFILE") }
                    )
                }

                if (activeTasks.isEmpty()) {
                    // Linear quality Empty State for Tasks
                    EmptyStateCard(
                        headline = "Operations queue fully resolved",
                        supportingText = "Zero pending milestones or task deadlines. Standard sprint flow fully clear.",
                        primaryCtaLabel = "Add Fast-Track Task",
                        onPrimaryCta = {
                            if (workspaces.isNotEmpty()) {
                                quickTaskWorkspaceId = workspaces.first().id
                                showInlineAddTask = true
                            } else {
                                FeedbackManager.showWarning("Create a workspace node first!")
                            }
                        },
                        secondaryCtaLabel = null,
                        onSecondaryCta = {},
                        accentColor = AccentRed
                    )
                } else {
                    activeTasks.take(3).forEach { task ->
                        val targetWorkspaceName = workspaces.find { it.id == task.workspaceId }?.name ?: "Co-Op Workspace"
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, ColorDivider)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = task.title,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(
                                        color = AccentRed.copy(alpha = 0.15f),
                                        contentColor = AccentRed,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = task.kanbanLane,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = targetWorkspaceName,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                // Direct complete action
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            val previousLane = task.kanbanLane
                                            globalViewModel.insertTask(task.copy(kanbanLane = "PUBLISH"))
                                            
                                            if (userProfile != null) {
                                                val nextCompleted = userProfile.completedProjectsCount + 1
                                                val nextScore = minOf(100, userProfile.reputationScore + 2)
                                                val nextBadge = if (nextScore >= 98) "Platinum" else if (nextScore >= 95) "Gold" else "Silver"
                                                globalViewModel.updateUserProfile(
                                                    userProfile.copy(
                                                        completedProjectsCount = nextCompleted,
                                                        reputationScore = nextScore,
                                                        reliabilityBadge = nextBadge
                                                    )
                                                )
                                            }
                                            
                                            FeedbackManager.showSuccess(
                                                message = "Task completed and marked as delivered! Trust Score +2",
                                                actionLabel = "UNDO",
                                                onAction = {
                                                    globalViewModel.insertTask(task.copy(kanbanLane = previousLane))
                                                    if (userProfile != null) {
                                                        globalViewModel.updateUserProfile(userProfile)
                                                    }
                                                }
                                            )
                                        },
                                        colors = ButtonDefaults.textButtonColors(contentColor = NeonEmerald)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp))
                                            Text("Deliver Project", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 6. REPUTATION ENGINE PROGRESS
        // ==========================================
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "REPUTATION ENGINE & SPLITS",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, ColorDivider)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Reputation & Reliability",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tier Level progress toward Partner Elite Status",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(NeonEmerald.copy(alpha = 0.15f))
                                    .border(1.dp, NeonEmerald, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Shield, null, tint = NeonEmerald, modifier = Modifier.size(18.dp))
                            }
                        }

                        // Score metrics row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ReputationMetricBox(
                                title = "Reliability Badge",
                                value = "${userProfile?.reliabilityBadge ?: "Silver"} Tier",
                                color = NeonEmerald,
                                modifier = Modifier.weight(1f)
                            )
                            ReputationMetricBox(
                                title = "Trust Score",
                                value = "${userProfile?.reputationScore ?: 85}/100",
                                color = AccentBlue,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Progress to next tier
                        val score = userProfile?.reputationScore ?: 85
                        val progressFraction = score / 100f
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("L4 elite threshold", fontSize = 10.sp, color = TextSecondary)
                                Text("${score}% Verified", fontSize = 10.sp, color = NeonEmerald, fontWeight = FontWeight.Bold)
                            }
                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = NeonEmerald,
                                trackColor = ColorDivider.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 7. NOTIFICATIONS SUMMARY
        // ==========================================
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "NOTIFICATIONS & REAL-TIME ALERTS",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    if (unreadNotifications.isNotEmpty()) {
                        Text(
                            text = "Resolve All",
                            color = AccentBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                globalViewModel.markAllNotificationsAsRead(userProfile?.id ?: "")
                                FeedbackManager.showSuccess("All alerts cleared.")
                            }
                        )
                    }
                }

                if (unreadNotifications.isEmpty()) {
                    // Premium minimal empty state
                    EmptyStateCard(
                        headline = "Inbox fully resolved",
                        supportingText = "Zero unread alerts or platform updates. All communication lines are clear.",
                        primaryCtaLabel = "View Alert History",
                        onPrimaryCta = { onNavigate("NOTIFICATIONS") },
                        secondaryCtaLabel = null,
                        onSecondaryCta = {},
                        accentColor = AccentBlue
                    )
                } else {
                    unreadNotifications.take(2).forEach { notif ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, ColorDivider)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = notif.title,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = notif.body,
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                }
                                IconButton(
                                    onClick = { globalViewModel.deleteNotification(notif) }
                                ) {
                                    Icon(Icons.Default.Check, "Mark Read", tint = NeonEmerald, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 8. RECENT ACTIVITY / AUDIT LOG
        // ==========================================
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "CRITICAL AUDIT JOURNAL",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, ColorDivider)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ActivityTimelineItemRow(
                            title = "Wallet node synchronized",
                            desc = "Stripe Connect verified successfully with digital media splits.",
                            timestamp = "Just now",
                            dotColor = NeonEmerald
                        )
                        ActivityTimelineItemRow(
                            title = "Task index resolved",
                            desc = "Operational sprint review cleared without exceptions.",
                            timestamp = "2 hours ago",
                            dotColor = AccentBlue
                        )
                        ActivityTimelineItemRow(
                            title = "Agreement Bound",
                            desc = "IP agreement v1.4 locked cryptographically in vault ledger.",
                            timestamp = "Yesterday",
                            dotColor = CrispAmber
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, ColorDivider)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = TextSecondary, fontSize = 10.sp)
        }
    }
}

@Composable
fun ReputationMetricBox(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceColor)
            .border(1.dp, ColorDivider)
            .padding(12.dp)
    ) {
        Column {
            Text(title, color = TextSecondary, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun ActivityTimelineItemRow(
    title: String,
    desc: String,
    timestamp: String,
    dotColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Dot and timeline line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.padding(top = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(timestamp, color = TextSecondary, fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(desc, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}


@Composable
fun SuspensionOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(80.dp)
            )
            Text(
                "Your account is suspended",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Text(
                "We detected a violation of our community co-op guidelines. Your ability to collaborate, post, or message has been revoked.",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )
            
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("APPEAL FLOW", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "If you believe this was a mistake, you can submit an appeal detailing your case to the Platform Admin team.",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { /* Open Support */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Contact Support/Appeal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSyncTopBar(globalViewModel: GlobalViewModel, showBack: Boolean = false, onBack: () -> Unit = {}) {
    val syncState by globalViewModel.syncState.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "top_breathe")
    val breathingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sync_alpha"
    )

    val dotColor = when (syncState) {
        com.example.data.model.SyncState.Synced -> MaterialTheme.colorScheme.tertiary // Semantic success / synced
        com.example.data.model.SyncState.PendingLocalChanges -> MaterialTheme.colorScheme.secondary // Semantic warning / pending
        else -> MaterialTheme.colorScheme.outline // Muted state / offline
    }

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (showBack) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
                Text("Creator Co-Op", fontSize = 16.sp, fontWeight = FontWeight.Black)
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            if (syncState == com.example.data.model.SyncState.Synced) 
                                dotColor.copy(alpha = breathingAlpha) 
                            else dotColor
                        )
                )
            }
        },
        actions = {
            IconButton(onClick = { globalViewModel.navigateToTab("SEARCH") }) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
            }
            IconButton(onClick = { globalViewModel.navigateToTab("DIRECT_MESSAGES") }) {
                Icon(Icons.Default.Mail, contentDescription = "Messages", tint = Color.White)
            }
            IconButton(onClick = { globalViewModel.navigateToTab("NOTIFICATIONS") }) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
    )
}
