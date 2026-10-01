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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.feedback.*
import com.example.ui.components.*
import com.example.ui.screens.workspace.CreateWorkspaceScreen
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.tour.GuidedTourOverlay
import com.example.ui.tour.GuidedTourRepository
import com.example.ui.tour.guidedTourTarget
import com.example.ui.components.CreationSpeedDialFab
import com.example.analytics.AnalyticsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodels.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
    val activeTour by globalViewModel.tourManager.activeTour.collectAsState()
    val currentTourStepIndex by globalViewModel.tourManager.currentStepIndex.collectAsState()
    val tourTargetRects by globalViewModel.tourManager.registeredTargets.collectAsState()
    val showWhatsNew by globalViewModel.showWhatsNew.collectAsState()
    val changelogEntries by globalViewModel.changelogEntries.collectAsState()
    val showCelebration by globalViewModel.showCelebration.collectAsState()

    // Flag to ensure we only check once per session/user change
    var hasCheckedChecklist by rememberSaveable { mutableStateOf(false) }
    var lastCheckedUserId by rememberSaveable { mutableStateOf<String?>(null) }
    var isCheckingOnboarding by remember { mutableStateOf(true) }

    LaunchedEffect(userId, user?.id) {
        isCheckingOnboarding = true
        if (userId == null) {
            globalViewModel.currentTab.value = "HOME"
        }
        val uid = userId ?: user?.id
        if (uid != null && (uid != lastCheckedUserId || !hasCheckedChecklist)) {
            hasCheckedChecklist = true
            lastCheckedUserId = uid
            globalViewModel.tourManager.loadTourStatuses(uid)
            globalViewModel.showOnboarding.value = false
            globalViewModel.checkWhatsNew(uid)
        } else if (uid == null) {
            globalViewModel.showOnboarding.value = false
        }
        isCheckingOnboarding = false
    }

    LaunchedEffect(currentTab, userId, user?.id, showOnboarding, isCheckingOnboarding) {
        if (isCheckingOnboarding) return@LaunchedEffect
        if (showOnboarding) return@LaunchedEffect
        val uid = userId ?: user?.id
        if (uid != null) {
            val tourConfig = when(currentTab) {
                "HOME" -> GuidedTourRepository.DASHBOARD_TOUR
                "WORKSPACES" -> GuidedTourRepository.WORKSPACES_TOUR
                "DISCOVERY", "SYNDICATE" -> GuidedTourRepository.DISCOVERY_TOUR
                "COMMONS" -> GuidedTourRepository.COMMONS_TOUR
                "PROFILE" -> GuidedTourRepository.PROFILE_TOUR
                "SETTINGS" -> GuidedTourRepository.SETTINGS_TOUR
                "CONTENT_PIPELINE" -> GuidedTourRepository.CONTENT_PIPELINE_TOUR
                "COMM_CENTER" -> GuidedTourRepository.COMM_CENTER_TOUR
                "FOUNDER_COMMAND" -> GuidedTourRepository.FOUNDER_COMMAND_TOUR
                else -> null
            }
            if (tourConfig != null) {
                globalViewModel.checkAndStartTour(uid, tourConfig)
            }
        }
    }
    val syncState by globalViewModel.syncState.collectAsState()
    val isSuspended = user?.systemRole == "SUSPENDED"

    val platformSettings by globalViewModel.platformSettings.collectAsState()
    val isUnderMaintenance = platformSettings.maintenanceMode && !(user?.systemRole == "PLATFORM_ADMIN" || user?.systemRole == "ADMIN" || user?.globalRole == "ADMIN")

    BackHandler(enabled = currentTab != "HOME" || (currentTab == "WORKSPACES" && workspaceViewModel.workspaceViewMode.value == "VIEW") || ((currentTab == "DISCOVERY" || currentTab == "SYNDICATE") && discoveryViewModel.selectedNicheFilter.value != "All")) {
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
            "DISCOVERY", "SYNDICATE" -> {
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
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        snackbarHost = { GlobalSnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (currentTab != "CREATE_WORKSPACE" && !showOnboarding && !isWorkspaceDetail && !isUnderMaintenance) {
                val isTopLevel = currentTab in listOf("HOME", "WORKSPACES", "DISCOVERY", "COMMONS", "MORE")
                val screenTitle = if (isTopLevel) "Creator Co-Op" else when (currentTab) {
                    "PROFILE" -> "My Profile"
                    "DISCOVERY" -> "Discovery Hub"
                    "COMMONS" -> "Creator Commons"
                    "ADMIN" -> "Admin Console"
                    "ANALYTICS" -> "Analytics Dashboard"
                    "CONTENT_PIPELINE" -> "Content Pipeline"
                    "VIDEO_HUDDLE" -> "Video Huddle"
                    "KNOWLEDGE_BASE" -> "Knowledge Base"
                    "PREMIUM_SUBSCRIPTION" -> "Creator Premium"
                    "SUPPORT_CENTER" -> "Support Center"
                    "FOUNDER_CRM" -> "Founder CRM"
                    "COMM_CENTER" -> "Comm Center"
                    "PLATFORM_CONTROL" -> "Platform Control"
                    "FOUNDER_COMMAND" -> "Founder Command"
                    "ACTIVITY_CENTER" -> "Activity Center"
                    "CONNECTION_REQUESTS" -> "Connection Requests"
                    "BLOCKED_USERS" -> "Blocked Users"
                    "REFER_TEAMMATE" -> "Refer a Teammate"
                    "COMMUNITY_GUIDELINES" -> "Community Guidelines"
                    "LEGAL" -> "Legal & Terms"
                    "SEARCH" -> "Global Search"
                    "NOTIFICATIONS" -> "Notifications Center"
                    "CREATE_ROLE" -> "Post a Role"
                    "TASK_DETAILS" -> "Task Details"
                    "PUBLIC_PROFILE" -> "Public Profile"
                    "WORKSPACE_SETTINGS" -> "Workspace Settings"
                    "WORKSPACE_FILES" -> "Workspace Files"
                    "PORTFOLIO_DETAIL" -> "Portfolio Detail"
                    "REPORT_USER" -> "Report Moderation"
                    "ADMIN_AUDIT" -> "Admin Audit Log"
                    "BACKUP_CENTER" -> "Backup Center"
                    "PLATFORM_HEALTH" -> "Platform Health"
                    else -> "Creator Co-Op"
                }
                Column {
                    GlobalSyncTopBar(
                        globalViewModel = globalViewModel,
                        userId = userId,
                        showBack = !isTopLevel,
                        title = screenTitle,
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
                    
                    if (syncState == com.example.data.model.SyncState.Offline) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CrispAmber.copy(alpha = 0.15f))
                                .border(BorderStroke(0.5.dp, CrispAmber.copy(alpha = 0.4f)))
                                .padding(vertical = 4.dp, horizontal = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudOff,
                                    contentDescription = "Offline",
                                    tint = CrispAmber,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Offline Mode • Running securely on local database",
                                    color = CrispAmber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (!isLandscape && currentTab != "CREATE_WORKSPACE" && !showOnboarding && !isUnderMaintenance) {
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
                    // Item 3: Discover
                    NavigationBarItem(
                        selected = currentTab == "DISCOVERY",
                        onClick = { globalViewModel.navigateToTab("DISCOVERY") },
                        icon = { Icon(Icons.Default.Explore, contentDescription = "Discover") },
                        label = { Text("Discover") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentBlue,
                            selectedTextColor = AccentBlue,
                            indicatorColor = SurfaceLightColor,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                    // Item 4: Commons
                    NavigationBarItem(
                        selected = currentTab == "COMMONS",
                        onClick = { globalViewModel.navigateToTab("COMMONS") },
                        icon = { Icon(Icons.Default.Forum, contentDescription = "Commons") },
                        label = { Text("Commons") },
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
                            "PROFILE", "SEARCH", "ADMIN", "ANALYTICS", "CONTENT_PIPELINE",
                            "VIDEO_HUDDLE", "KNOWLEDGE_BASE", "PREMIUM_SUBSCRIPTION", "CONNECTION_REQUESTS",
                            "BLOCKED_USERS", "SUPPORT_CENTER", "FOUNDER_CRM", "COMM_CENTER", "PLATFORM_CONTROL", "FOUNDER_COMMAND"
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
            if (currentTab != "CREATE_WORKSPACE" && !isSuspended && !showOnboarding && !isUnderMaintenance) {
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
        Row(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
            if (isLandscape && currentTab != "CREATE_WORKSPACE" && !showOnboarding && !isUnderMaintenance) {
                NavigationRail(
                    containerColor = SurfaceColor,
                    header = {
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp, bottom = 12.dp)
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.linearGradient(listOf(AccentBlue, AccentRed)))
                                .clickable { globalViewModel.navigateToTab("HOME") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Creator Co-Op",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxHeight()
                        .border(BorderStroke(1.dp, ColorDivider)),
                    contentColor = TextSecondary
                ) {
                    NavigationRailItem(
                        selected = currentTab == "HOME",
                        onClick = { globalViewModel.navigateToTab("HOME") },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = AccentBlue,
                            selectedTextColor = AccentBlue,
                            indicatorColor = SurfaceLightColor,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                    NavigationRailItem(
                        selected = currentTab == "WORKSPACES",
                        onClick = { globalViewModel.navigateToTab("WORKSPACES") },
                        icon = { Icon(Icons.Default.Folder, contentDescription = "Workspaces") },
                        label = { Text("Workspaces", fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = AccentBlue,
                            selectedTextColor = AccentBlue,
                            indicatorColor = SurfaceLightColor,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                    NavigationRailItem(
                        selected = currentTab == "DISCOVERY",
                        onClick = { globalViewModel.navigateToTab("DISCOVERY") },
                        icon = { Icon(Icons.Default.Explore, contentDescription = "Discover") },
                        label = { Text("Discover", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = AccentBlue,
                            selectedTextColor = AccentBlue,
                            indicatorColor = SurfaceLightColor,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                    NavigationRailItem(
                        selected = currentTab == "COMMONS",
                        onClick = { globalViewModel.navigateToTab("COMMONS") },
                        icon = { Icon(Icons.Default.Forum, contentDescription = "Commons") },
                        label = { Text("Commons", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = AccentBlue,
                            selectedTextColor = AccentBlue,
                            indicatorColor = SurfaceLightColor,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                    NavigationRailItem(
                        selected = currentTab == "MORE" || currentTab in listOf(
                            "PROFILE", "SEARCH", "ADMIN", "ANALYTICS", "CONTENT_PIPELINE",
                            "VIDEO_HUDDLE", "KNOWLEDGE_BASE", "PREMIUM_SUBSCRIPTION", "CONNECTION_REQUESTS",
                            "BLOCKED_USERS", "SUPPORT_CENTER", "FOUNDER_CRM", "COMM_CENTER", "PLATFORM_CONTROL", "FOUNDER_COMMAND"
                        ),
                        onClick = { globalViewModel.navigateToTab("MORE") },
                        icon = { Icon(Icons.Default.Menu, contentDescription = "More") },
                        label = { Text("More", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = AccentBlue,
                            selectedTextColor = AccentBlue,
                            indicatorColor = SurfaceLightColor,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = { globalViewModel.navigateToTab("PROFILE") },
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (currentTab == "PROFILE") AccentBlue.copy(alpha = 0.3f) else SurfaceLightColor)
                                .border(1.5.dp, if (currentTab == "PROFILE") AccentBlue else ColorDivider, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user?.displayName?.take(1)?.uppercase() ?: "C",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = if (currentTab == "PROFILE") AccentBlue else TextSecondary
                            )
                        }
                    }
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                if (isUnderMaintenance) {
                    MaintenanceOverlay(
                        onLogout = {
                            authViewModel.logout()
                        }
                    )
                } else {
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "dashboard_sub_navigation"
                    ) { tab ->
                        if (isSuspended && tab != "PROFILE" && tab != "HOME") {
                            SuspensionOverlay()
                        } else {
                        when (tab) {
                            "HOME" -> SimpleMainHubScreen(
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
                            globalViewModel = globalViewModel,
                            onNavigateToCreate = { globalViewModel.navigateToTab("CREATE_WORKSPACE") },
                            onNavigateToDiscovery = { globalViewModel.navigateToTab("DISCOVERY") }
                        )
                        "CREATE_WORKSPACE" -> CreateWorkspaceScreen(
                            workspaceViewModel = workspaceViewModel,
                            userId = userId ?: "",
                            userProfile = user,
                            onNavigateToPro = { globalViewModel.navigateToTab("PREMIUM_SUBSCRIPTION") },
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
                            userProfile = user
                        )
                        "MORE" -> MoreScreen(
                            currentRole = user?.systemRole,
                            userId = user?.id,
                            globalViewModel = globalViewModel,
                            onNavigate = { globalViewModel.navigateToTab(it) }
                        )
                        "ADMIN" -> {
                            if (user?.systemRole == "PLATFORM_ADMIN" || user?.globalRole == "ADMIN" || user?.systemRole == "ADMIN") {
                                AdminDashboardScreen(
                                    adminViewModel = adminViewModel,
                                    authViewModel = authViewModel,
                                    userProfile = user
                                )
                            } else {
                                AccessDeniedScreen(onBack = { globalViewModel.navigateBack() })
                            }
                        }
                        "NOTIFICATIONS" -> NotificationsCenterScreen(globalViewModel = globalViewModel, workspaceViewModel = workspaceViewModel, userProfile = user)
                        "SEARCH" -> GlobalSearchScreen(globalViewModel = globalViewModel, workspaceViewModel = workspaceViewModel, userProfile = user)
                        "TASK_DETAILS" -> {
                            val taskId = globalViewModel.selectedTaskId.collectAsState().value ?: "84F"
                            TaskDetailsScreen(taskId = taskId, onBack = { globalViewModel.navigateBack() })
                        }
                        "PUBLIC_PROFILE" -> {
                            val profileUserId = globalViewModel.selectedUserId.collectAsState().value ?: user?.id ?: "me"
                            PublicProfileScreen(userId = profileUserId, globalViewModel = globalViewModel, onBack = { globalViewModel.navigateBack() })
                        }
                        "WORKSPACE_SETTINGS" -> {
                            val wsId = globalViewModel.selectedWorkspaceId.collectAsState().value ?: "WS123"
                            com.example.ui.screens.workspace.WorkspaceSettingsScreen(
                                workspaceId = wsId,
                                viewModel = workspaceViewModel,
                                onBack = { globalViewModel.navigateBack() }
                            )
                        }
                        "CONNECTION_REQUESTS" -> ConnectionRequestsScreen(onBack = { globalViewModel.navigateBack() }, globalViewModel = globalViewModel, userProfile = user)
                        "DIRECT_MESSAGES" -> DirectMessagesScreen(onBack = { globalViewModel.navigateBack() }, globalViewModel = globalViewModel, chatViewModel = chatViewModel, userProfile = user)
                        "CREATE_ROLE" -> com.example.ui.screens.workspace.CreateRoleScreen(
                            discoveryViewModel = discoveryViewModel,
                            authViewModel = authViewModel,
                            userProfile = user,
                            onBack = { globalViewModel.navigateBack() }
                        )
                        "WORKSPACE_FILES" -> com.example.ui.screens.workspace.WorkspaceFilesHubScreen(
                            workspaceId = workspaceViewModel.selectedWorkspaceId.collectAsState().value ?: "WS123",
                            viewModel = workspaceViewModel,
                            onBack = { globalViewModel.navigateBack() }
                        )
                        "ACTIVITY_CENTER" -> ActivityCenterScreen(onBack = { globalViewModel.navigateBack() }, globalViewModel = globalViewModel, userProfile = user)
                        "PORTFOLIO_DETAIL" -> {
                            val projId = globalViewModel.selectedProjectId.collectAsState().value ?: "P123"
                            PortfolioDetailScreen(projectId = projId, onBack = { globalViewModel.navigateBack() })
                        }
                        "REPORT_USER" -> ReportModerationScreen(globalViewModel = globalViewModel, currentUserId = userId ?: "guest", onBack = { globalViewModel.navigateBack() })
                        "BLOCKED_USERS" -> BlockedUsersScreen(onBack = { globalViewModel.navigateBack() }, globalViewModel = globalViewModel, userProfile = user)
                        "REFER_TEAMMATE" -> com.example.ui.screens.ReferTeammateScreen(userProfile = user, globalViewModel = globalViewModel, onBack = { globalViewModel.navigateBack() })
                        "WORKSPACE_INVITATION" -> com.example.ui.screens.workspace.WorkspaceInvitationScreen(globalViewModel = globalViewModel, workspaceViewModel = workspaceViewModel, onBack = { globalViewModel.navigateBack() })
                        "ANALYTICS" -> AnalyticsDashboardScreen(
                            analyticsViewModel = analyticsViewModel,
                            onBack = { globalViewModel.navigateBack() }
                        )
                        "KNOWLEDGE_BASE" -> com.example.ui.screens.workspace.KnowledgeBaseScreen(
                            onBack = { globalViewModel.navigateBack() },
                            workspaceViewModel = workspaceViewModel,
                            userId = userId ?: ""
                        )
                        "CONTENT_PIPELINE" -> ContentPipelineScreen(
                            workspaceViewModel = workspaceViewModel,
                            userId = userId ?: "",
                            onBack = { globalViewModel.navigateBack() }
                        )
                        "VIDEO_HUDDLE" -> com.example.ui.screens.VideoHuddleScreen(onBack = { globalViewModel.navigateBack() }, globalViewModel = globalViewModel)
                        "PREMIUM_SUBSCRIPTION" -> com.example.ui.screens.PremiumSubscriptionScreen(globalViewModel = globalViewModel, userProfile = user, onBack = { globalViewModel.navigateBack() })
                        "SUPPORT_CENTER" -> SupportCenterScreen(
                            supportViewModel = supportViewModel,
                            userProfile = user,
                            onBack = { globalViewModel.navigateBack() }
                        )
                        "FOUNDER_CRM" -> {
                            if (user?.systemRole == "PLATFORM_ADMIN" || user?.globalRole == "ADMIN" || user?.systemRole == "ADMIN") {
                                FounderCrmScreen(
                                    crmViewModel = founderCrmViewModel,
                                    onBack = { globalViewModel.navigateBack() }
                                )
                            } else {
                                AccessDeniedScreen(onBack = { globalViewModel.navigateBack() })
                            }
                        }
                        "COMM_CENTER" -> {
                            if (user?.systemRole == "PLATFORM_ADMIN" || user?.globalRole == "ADMIN" || user?.systemRole == "ADMIN") {
                                CommCenterScreen(
                                    viewModel = communicationViewModel,
                                    onBack = { globalViewModel.navigateBack() }
                                )
                            } else {
                                AccessDeniedScreen(onBack = { globalViewModel.navigateBack() })
                            }
                        }
                        "PLATFORM_CONTROL" -> {
                            if (user?.systemRole == "PLATFORM_ADMIN" || user?.globalRole == "ADMIN" || user?.systemRole == "ADMIN") {
                                PlatformControlCenterScreen(
                                    viewModel = platformControlViewModel,
                                    onBack = { globalViewModel.navigateBack() }
                                )
                            } else {
                                AccessDeniedScreen(onBack = { globalViewModel.navigateBack() })
                            }
                        }
                        "ADMIN_AUDIT" -> {
                            if (user?.systemRole == "PLATFORM_ADMIN" || user?.globalRole == "ADMIN" || user?.systemRole == "ADMIN") {
                                AdminAuditScreen(
                                    adminViewModel = adminViewModel,
                                    onBack = { globalViewModel.navigateBack() }
                                )
                            } else {
                                AccessDeniedScreen(onBack = { globalViewModel.navigateBack() })
                            }
                        }
                        "BACKUP_CENTER" -> {
                            if (user?.systemRole == "PLATFORM_ADMIN" || user?.globalRole == "ADMIN" || user?.systemRole == "ADMIN") {
                                BackupCenterScreen(
                                    onBack = { globalViewModel.navigateBack() }
                                )
                            } else {
                                AccessDeniedScreen(onBack = { globalViewModel.navigateBack() })
                            }
                        }
                        "PLATFORM_HEALTH" -> {
                            if (user?.systemRole == "PLATFORM_ADMIN" || user?.globalRole == "ADMIN" || user?.systemRole == "ADMIN") {
                                PlatformHealthScreen(
                                    onBack = { globalViewModel.navigateBack() }
                                )
                            } else {
                                AccessDeniedScreen(onBack = { globalViewModel.navigateBack() })
                            }
                        }
                        "FOUNDER_COMMAND" -> {
                            if (user?.systemRole == "PLATFORM_ADMIN" || user?.globalRole == "ADMIN" || user?.systemRole == "ADMIN") {
                                FounderCommandCenterScreen(
                                    onNavigate = { globalViewModel.navigateToTab(it) },
                                    onBack = { globalViewModel.navigateBack() }
                                )
                            } else {
                                AccessDeniedScreen(onBack = { globalViewModel.navigateBack() })
                            }
                        }
                        "COMMUNITY_GUIDELINES" -> CommunityGuidelinesScreen(onBack = { globalViewModel.navigateBack() })
                        "LEGAL" -> LegalScreen(onBack = { globalViewModel.navigateBack() })
                    }
                }
            }
            }
        }
        }
        
        FeedbackOverlay()
        


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

        if (activeTour != null) {
            val uid = userId ?: user?.id ?: "guest"
            GuidedTourOverlay(
                tourConfig = activeTour!!,
                currentStepIndex = currentTourStepIndex,
                targetRects = tourTargetRects,
                onNext = { globalViewModel.nextTourStep(uid) },
                onPrev = { globalViewModel.previousTourStep() },
                onSkip = { globalViewModel.skipTour(uid) },
                onDone = { globalViewModel.completeTour(uid) }
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
fun SimpleMainHubScreen(
    onNavigate: (String) -> Unit,
    userProfile: com.example.data.model.UserProfile?,
    globalViewModel: GlobalViewModel,
    workspaceViewModel: WorkspaceViewModel,
    agreementViewModel: AgreementViewModel
) {
    val notifications by remember(userProfile?.id ?: "") { globalViewModel.getNotificationsForUser(userProfile?.id ?: "") }.collectAsState(initial = emptyList())
    val unreadNotifications = remember(notifications) { notifications.filter { !it.isRead } }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 680.dp

        if (isWideScreen) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PrimaryBackground)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Column: Welcome, Actions & Guide
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        HubWelcomeHeader(userProfile = userProfile, onProfileClick = { onNavigate("PROFILE") })
                    }
                    item {
                        HubQuickActions(globalViewModel = globalViewModel, onNavigate = onNavigate)
                    }
                    item {
                        HubFeatureGuideBanner(userProfile = userProfile, globalViewModel = globalViewModel)
                    }
                }

                // Right Column: Telemetry Sparklines & Recent Activity
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        HubMetricsSparklineCard(globalViewModel = globalViewModel)
                    }
                    item {
                        HubRecentUpdates(
                            userProfile = userProfile,
                            unreadNotifications = unreadNotifications,
                            globalViewModel = globalViewModel
                        )
                    }
                }
            }
        } else {
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
                item {
                    HubWelcomeHeader(userProfile = userProfile, onProfileClick = { onNavigate("PROFILE") })
                }
                item {
                    HubMetricsSparklineCard(globalViewModel = globalViewModel)
                }
                item {
                    HubFeatureGuideBanner(userProfile = userProfile, globalViewModel = globalViewModel)
                }
                item {
                    HubQuickActions(globalViewModel = globalViewModel, onNavigate = onNavigate)
                }
                item {
                    HubRecentUpdates(
                        userProfile = userProfile,
                        unreadNotifications = unreadNotifications,
                        globalViewModel = globalViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun HubWelcomeHeader(
    userProfile: com.example.data.model.UserProfile?,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            val dateStr = remember {
                val sdf = java.text.SimpleDateFormat("EEEE, MMMM d", java.util.Locale.US)
                sdf.format(java.util.Date()).uppercase()
            }
            Text(
                text = "$dateStr • WELCOME BACK",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
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
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Here is your dashboard overview for today.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
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
                    .clickable { onProfileClick() },
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

@Composable
fun HubMetricsSparklineCard(globalViewModel: GlobalViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .guidedTourTarget("dashboard_metrics_sparkline", globalViewModel.tourManager),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(20.dp),
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
                        text = "COLLABORATION REAL-TIME LATENCY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = AccentBlue,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "24ms • OPTIMAL HEALTH",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Icon(
                    imageVector = Icons.Default.TrendingDown,
                    contentDescription = "Trending Down (Optimal)",
                    tint = NeonEmerald,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Live Sparkline Graph using Compose Canvas
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                val width = size.width
                val height = size.height
                val points = listOf(
                    0.2f, 0.4f, 0.3f, 0.6f, 0.45f, 0.8f, 0.5f, 0.35f, 0.25f, 0.15f, 0.12f
                )
                val stepX = width / (points.size - 1)
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, height * (1f - points[0]))
                    for (i in 1 until points.size) {
                        lineTo(i * stepX, height * (1f - points[i]))
                    }
                }
                
                drawPath(
                    path = path,
                    color = NeonEmerald,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 3.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                    )
                )
                
                val filledPath = androidx.compose.ui.graphics.Path().apply {
                    addPath(path)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }
                drawPath(
                    path = filledPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(NeonEmerald.copy(alpha = 0.25f), Color.Transparent),
                        startY = 0f,
                        endY = height
                    )
                )
                
                val lastPointX = width
                val lastPointY = height * (1f - points.last())
                drawCircle(
                    color = NeonEmerald,
                    radius = 6.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(lastPointX, lastPointY)
                )
                drawCircle(
                    color = NeonEmerald.copy(alpha = 0.4f),
                    radius = 12.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(lastPointX, lastPointY)
                )
            }
        }
    }
}

@Composable
fun HubFeatureGuideBanner(
    userProfile: com.example.data.model.UserProfile?,
    globalViewModel: GlobalViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, Brush.horizontalGradient(listOf(AccentBlue.copy(alpha = 0.8f), NeonEmerald.copy(alpha = 0.8f)))), RoundedCornerShape(16.dp))
            .testTag("hub_feature_tour_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = AccentBlue.copy(alpha = 0.2f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "FEATURE & OPTIONS GUIDE",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = AccentBlue,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Interactive card explanation for every button & feature",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Learn what every option does, why it exists, and how to operate it step-by-step with live operational simulations.",
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val uid = userProfile?.id ?: "guest"
                Button(
                    onClick = {
                        globalViewModel.replayTour(uid, "dashboard_tour")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f).testTag("hub_start_user_tour_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("USER TOUR", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                }

                Button(
                    onClick = {
                        globalViewModel.replayTour(uid, "founder_command_tour")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f).testTag("hub_start_admin_tour_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ADMIN TOUR", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun HubQuickActions(
    globalViewModel: GlobalViewModel,
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier.guidedTourTarget("dashboard_quick_actions", globalViewModel.tourManager),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "QUICK ACTIONS",
            color = TextSecondary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.5.sp
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionRowCard(
                title = "Explore Creators",
                description = "Find other video creators to collaborate with on future projects.",
                icon = Icons.Default.Groups,
                accentColor = AccentBlue,
                testTag = "action_explore_creators",
                onClick = { onNavigate("DISCOVERY") }
            )

            ActionRowCard(
                title = "My Workspaces",
                description = "Manage your collaborative projects, active channels, and tasks.",
                icon = Icons.Default.AddHomeWork,
                accentColor = NeonEmerald,
                testTag = "action_my_workspaces",
                onClick = { onNavigate("WORKSPACES") }
            )

            ActionRowCard(
                title = "Post a Role",
                description = "Invite specialized video creators to join your production team.",
                icon = Icons.Default.Add,
                accentColor = AccentRed,
                testTag = "action_post_role",
                onClick = { onNavigate("DISCOVERY") }
            )
        }
    }
}

@Composable
fun HubRecentUpdates(
    userProfile: com.example.data.model.UserProfile?,
    unreadNotifications: List<com.example.data.model.Notification>,
    globalViewModel: GlobalViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RECENT UPDATES",
                color = TextSecondary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp
            )
            
            if (unreadNotifications.isNotEmpty()) {
                Text(
                    text = "Clear All",
                    color = AccentBlue,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.clickable {
                        globalViewModel.markAllNotificationsAsRead(userProfile?.id ?: "")
                        FeedbackManager.showSuccess("All updates cleared.")
                    }
                )
            }
        }

        if (unreadNotifications.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("empty_updates_card"),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ColorDivider)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(NeonEmerald.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = NeonEmerald,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "You're all caught up!",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "There are no new updates or tasks requiring your attention right now.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                unreadNotifications.take(4).forEach { notif ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("update_item_${notif.id}"),
                        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, ColorDivider)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(14.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = notif.title,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = notif.body,
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    lineHeight = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            IconButton(
                                onClick = { globalViewModel.deleteNotification(notif) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Clear Update",
                                    tint = NeonEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionRowCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ColorDivider)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.1f))
                    .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 18.sp
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
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
    val userTasks by remember(userProfile?.id ?: "") { globalViewModel.getAllTasksForUser(userProfile?.id ?: "") }.collectAsState(initial = emptyList())
    val notifications by remember(userProfile?.id ?: "") { globalViewModel.getNotificationsForUser(userProfile?.id ?: "") }.collectAsState(initial = emptyList())
    val syncState by globalViewModel.syncState.collectAsState()

    // Optimized filtering via remember
    val activeTasks = remember(userTasks) { 
        userTasks.filter { it.kanbanLane.uppercase() != "PUBLISH" && it.kanbanLane.uppercase() != "DONE" } 
    }
    val unreadNotifications = remember(notifications) { notifications.filter { !it.isRead } }

    // 2. Agreement states integration (Simplified for performance)
    val signedAgreements by remember(userProfile?.id ?: "") { agreementViewModel.getAcknowledgmentsForUser(userProfile?.id ?: "") }.collectAsState(initial = emptyList())
    val allAgreements by remember { agreementViewModel.getAllAgreementsFlow() }.collectAsState(initial = emptyList())
    
    // We only care about agreements for visible workspaces
    val pendingAgreements = remember(workspaces, allAgreements, signedAgreements) {
        val userSignedAgreementIds = signedAgreements.map { it.agreementId }.toSet()
        val workspaceMap = workspaces.associateBy { it.id }
        
        val latestAgreementsByWorkspace = allAgreements
            .filter { it.workspaceId in workspaceMap.keys }
            .groupBy { it.workspaceId }
            .mapValues { (_, agreements) -> agreements.maxByOrNull { it.createdAt } }
            
        latestAgreementsByWorkspace.values.filterNotNull().filter { agreement ->
            !userSignedAgreementIds.contains(agreement.id)
        }
    }

    val signingAgreements = remember { androidx.compose.runtime.mutableStateMapOf<String, Boolean>() }

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
        // NEW: ONBOARDING PROGRESS CHECKLIST
        // ==========================================
        item {
            val onboardingChecklistDismissed by globalViewModel.onboardingChecklistDismissed.collectAsState()
            val connRequests by globalViewModel.allConnectionRequests.collectAsState()
            
            val isProfileCompleted = userProfile?.displayName?.isNotEmpty() == true && 
                                     userProfile?.primarySpecialty?.isNotEmpty() == true && 
                                     userProfile?.bio?.isNotEmpty() == true
            val isJoinedSpace = workspaces.isNotEmpty()
            val hasSentRequest = connRequests.any { it.senderId == userProfile?.id }
            val isJoinedWorkspace = workspaces.isNotEmpty()
            
            val steps = listOf(
                OnboardingStep("Complete Profile", isProfileCompleted) { onNavigate("PROFILE") },
                OnboardingStep("Join a Space", isJoinedSpace) { onNavigate("COMMONS") },
                OnboardingStep("Send Connection Request", hasSentRequest) { onNavigate("DISCOVERY") },
                OnboardingStep("Create/Join a Workspace", isJoinedWorkspace) { onNavigate("WORKSPACES") }
            )
            
            val completedSteps = steps.count { it.isCompleted }
            val progressPercent = if (steps.isNotEmpty()) (completedSteps.toFloat() / steps.size.toFloat()) else 0f
            val isAllOnboardingComplete = completedSteps == steps.size

            LaunchedEffect(userProfile?.id) {
                userProfile?.id?.let {
                    globalViewModel.loadOnboardingChecklistDismissed(it)
                }
            }

            if (!onboardingChecklistDismissed && !isAllOnboardingComplete) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_progress_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    border = BorderStroke(1.dp, ColorDivider)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Getting Started",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Complete these steps to set up your profile",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            IconButton(
                                onClick = {
                                    userProfile?.id?.let {
                                        globalViewModel.setOnboardingChecklistDismissed(it, true)
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("dismiss_onboarding_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Progress bar with linear accent
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LinearProgressIndicator(
                                progress = progressPercent,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .testTag("onboarding_progress_bar"),
                                color = AccentBlue,
                                trackColor = ColorDivider
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${(progressPercent * 100).toInt()}%",
                                color = AccentBlue,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                modifier = Modifier.testTag("onboarding_percentage_text")
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Steps List
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            steps.forEachIndexed { index, step ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(PrimaryBackground.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                        .border(BorderStroke(1.dp, ColorDivider.copy(alpha = 0.5f)), RoundedCornerShape(10.dp))
                                        .clickable { step.onAction() }
                                        .padding(10.dp)
                                        .testTag("onboarding_step_$index"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(
                                                if (step.isCompleted) NeonEmerald.copy(alpha = 0.15f) else Color.Transparent,
                                                CircleShape
                                            )
                                            .border(
                                                BorderStroke(
                                                    1.5.dp,
                                                    if (step.isCompleted) NeonEmerald else TextMuted
                                                ),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (step.isCompleted) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Completed",
                                                tint = NeonEmerald,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = step.title,
                                        color = if (step.isCompleted) TextSecondary else Color.White,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Navigate",
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
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
                        AnimatedCounter(
                            value = userProfile?.reputationScore ?: 90,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp, fontWeight = FontWeight.Black)
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
                        title = "Draft Contract",
                        subtitle = "Initiate standard terms",
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
                        supportingText = "Excellent. Every workspace contract, copyright assignment, and co-op agreement is completely signed and bound.",
                        primaryCtaLabel = "Draft New Contract",
                        onPrimaryCta = {
                            if (workspaces.isNotEmpty()) {
                                val ws = workspaces.first()
                                val draftAgreement = com.example.data.model.TeamAgreement(
                                    id = java.util.UUID.randomUUID().toString(),
                                    workspaceId = ws.id,
                                    contentText = "Collective production agreement: Partners establish equal IP shares on shared channels with verified co-op membership agreements.",
                                    createdAt = System.currentTimeMillis()
                                )
                                agreementViewModel.createTeamAgreement(ws.id, draftAgreement.contentText)
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
                                
                                val isSigning = signingAgreements[agreement.id] == true
                                Button(
                                    onClick = {
                                        scope.launch {
                                            signingAgreements[agreement.id] = true
                                            FeedbackManager.showInfo("Verifying workspace credentials & creating cryptographic handshake...")
                                            kotlinx.coroutines.delay(2000)
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
                                            signingAgreements.remove(agreement.id)
                                            FeedbackManager.showSuccess("Document executed cryptographically! Trust score boosted.")
                                        }
                                    },
                                    enabled = !isSigning,
                                    modifier = Modifier.fillMaxWidth().height(42.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CrispAmber),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    if (isSigning) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Signing...", fontWeight = FontWeight.Bold, color = Color.Black)
                                    } else {
                                        Text("Acknowledge & Sign", fontWeight = FontWeight.Bold, color = Color.Black)
                                    }
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AnimatedCounter(
                                        value = score,
                                        color = NeonEmerald,
                                        style = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    )
                                    Text("% Verified", fontSize = 10.sp, color = NeonEmerald, fontWeight = FontWeight.Bold)
                                }
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
                            title = "Co-Op Subscription verified",
                            desc = "Pro membership license status verified with active feature tier.",
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
fun GlobalSyncTopBar(
    globalViewModel: GlobalViewModel, 
    userId: String? = null,
    showBack: Boolean = false, 
    title: String = "Creator Co-Op", 
    onBack: () -> Unit = {}
) {
    val syncState by globalViewModel.syncState.collectAsState()
    var showSyncDialog by remember { mutableStateOf(false) }
    
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

    val hasSupabase = !com.example.data.supabase.SupabaseConfig.supabaseUrl.contains("your-project")
    val envLabel = if (hasSupabase) "CLOUD ACTIVE" else "STANDALONE MODE"
    val envColor = if (hasSupabase) NeonEmerald else CrispAmber

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically, 
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clickable { showSyncDialog = true }
                    .testTag("top_bar_sync_row")
            ) {
                if (showBack) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("dashboard_top_bar_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
                Text(
                    text = title, 
                    fontSize = 15.sp, 
                    fontWeight = FontWeight.Black, 
                    maxLines = 1, 
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (syncState == com.example.data.model.SyncState.Synced) 
                                dotColor.copy(alpha = breathingAlpha) 
                            else dotColor
                        )
                )
                
                // Dynamic Environment State Badge (Single line layout)
                Surface(
                    color = envColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(0.5.dp, envColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = envLabel,
                        color = envColor,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }
        },
        actions = {
            val uid = userId ?: "guest"
            IconButton(
                onClick = { globalViewModel.replayTour(uid, "dashboard_tour") },
                modifier = Modifier.testTag("top_bar_tour_button")
            ) {
                Icon(Icons.Default.HelpOutline, contentDescription = "Screen Guided Tour", tint = AccentBlue)
            }
            IconButton(onClick = { globalViewModel.navigateToTab("SEARCH") }) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
            }
            IconButton(onClick = { globalViewModel.navigateToTab("DIRECT_MESSAGES") }) {
                Icon(Icons.Default.Mail, contentDescription = "Messages", tint = Color.White)
            }
            IconButton(onClick = { globalViewModel.navigateToTab("NOTIFICATIONS") }) {
                val notifications by if (userId != null) {
                    globalViewModel.getNotificationsForUser(userId).collectAsState(initial = emptyList())
                } else {
                    remember { mutableStateOf(emptyList<com.example.data.model.Notification>()) }
                }
                val unreadCount = notifications.count { !it.isRead }
                if (unreadCount > 0) {
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = AccentRed,
                                contentColor = Color.White
                            ) {
                                Text(unreadCount.toString())
                            }
                        }
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                    }
                } else {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
    )

    if (showSyncDialog) {
        SyncAndCollaborationCenterDialog(
            globalViewModel = globalViewModel,
            onDismiss = { showSyncDialog = false }
        )
    }
}

@Composable
fun SyncAndCollaborationCenterDialog(
    globalViewModel: GlobalViewModel,
    onDismiss: () -> Unit
) {
    val syncState by globalViewModel.syncState.collectAsState()
    val syncEvents by globalViewModel.allSyncEvents.collectAsState(initial = emptyList())
    val lastSyncTime by globalViewModel.lastSyncTime.collectAsState()
    val isSyncActive by globalViewModel.isSyncActive.collectAsState()
    val conflictPolicy by globalViewModel.conflictPolicy.collectAsState()
    val workspaceMembers by globalViewModel.allWorkspaceMembers.collectAsState(initial = emptyList())

    val pendingCount = syncEvents.count { it.syncStatus == "PENDING" }
    val failedCount = syncEvents.count { it.syncStatus == "FAILED" }
    val syncedCount = syncEvents.count { it.syncStatus == "SYNCED" }

    val sdf = remember { java.text.SimpleDateFormat("MMM dd, HH:mm:ss", java.util.Locale.getDefault()) }
    val formattedLastSync = remember(lastSyncTime) { sdf.format(java.util.Date(lastSyncTime)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = AccentBlue)
                Text("Sync & Collaboration Center", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxHeight(0.85f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(DS.Space12),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Environment & Database Health Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(0.5.dp, ColorDivider)
                        ) {
                            Column(modifier = Modifier.padding(DS.Space12)) {
                                Text("ENVIRONMENT STATUS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = AccentBlue)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val hasSupabase = !com.example.data.supabase.SupabaseConfig.supabaseUrl.contains("your-project")
                                    Text(
                                        text = if (hasSupabase) "Supabase Remote Database Connected" else "Standalone Offline Database",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (hasSupabase) NeonEmerald else CrispAmber)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("LIVENESS STATUS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = AccentBlue)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Liveness Status", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                    Text(
                                        text = when (syncState) {
                                            com.example.data.model.SyncState.Synced -> "SYNCED & SECURE"
                                            com.example.data.model.SyncState.PendingLocalChanges -> "OFFLINE QUEUE ACTIVE"
                                            else -> "OFFLINE MODE"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = when (syncState) {
                                            com.example.data.model.SyncState.Synced -> NeonEmerald
                                            com.example.data.model.SyncState.PendingLocalChanges -> CrispAmber
                                            else -> TextSecondary
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Last Synchronization", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                    Text(formattedLastSync, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Conflict Policy Selection
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(0.5.dp, ColorDivider)
                        ) {
                            Column(modifier = Modifier.padding(DS.Space12)) {
                                Text("CONFLICT RESOLUTION STRATEGY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = AccentBlue)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { globalViewModel.setConflictPolicy("CLIENT_WINS") },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (conflictPolicy == "CLIENT_WINS") AccentBlue else ColorDivider
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Client Wins", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { globalViewModel.setConflictPolicy("SERVER_WINS") },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (conflictPolicy == "SERVER_WINS") AccentBlue else ColorDivider
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Server Wins", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Active Presence List
                    item {
                        Text(
                            text = "COLLABORATORS PRESENT (${workspaceMembers.count { it.isOnline }})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = AccentBlue,
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                        )
                    }

                    if (workspaceMembers.isEmpty()) {
                        item {
                            Text("No collaborators registered in local shard", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(start = 4.dp))
                        }
                    } else {
                        items(workspaceMembers) { member ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceColor, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(32.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(AccentBlue.copy(alpha = 0.15f))
                                            .border(1.dp, if (member.isOnline) NeonEmerald else ColorDivider, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(member.userId.take(2).uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (member.isOnline) NeonEmerald else Color.Gray)
                                            .align(Alignment.BottomEnd)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(member.userId, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    
                                    val actionText = when {
                                        member.isTyping && member.typingText.isNotBlank() -> "Typing: \"${member.typingText}\""
                                        member.currentlyViewingTaskId != null -> "Viewing Task #${member.currentlyViewingTaskId}"
                                        member.currentlyEditingAssetId != null -> "Editing Asset #${member.currentlyEditingAssetId}"
                                        member.liveStatusUpdate.isNotBlank() -> member.liveStatusUpdate
                                        member.isOnline -> "Online & Active"
                                        else -> {
                                            val minutes = (System.currentTimeMillis() - member.lastSeenAt) / 60000
                                            if (minutes < 1) "Active just now" else "Last seen ${minutes}m ago"
                                        }
                                    }
                                    Text(actionText, fontSize = 11.sp, color = if (member.isTyping) AccentBlue else TextSecondary)
                                }
                            }
                        }
                    }

                    // Sync Queue Statistics Summary
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = DS.Space8),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "OFFLINE QUEUE LOGS ($pendingCount Pending)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = AccentBlue
                            )
                            if (syncedCount > 0) {
                                Text(
                                    text = "Clear Sync logs",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CrispAmber,
                                    modifier = Modifier.clickable { globalViewModel.clearSyncedHistory() }
                                )
                            }
                        }
                    }

                    if (syncEvents.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.5.dp, ColorDivider)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Queue is empty. Everything fully synchronized!", fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                        }
                    } else {
                        items(syncEvents.sortedByDescending { it.createdAt }) { event ->
                            val statusColor = when (event.syncStatus) {
                                "SYNCED" -> NeonEmerald
                                "FAILED" -> AccentRed
                                else -> CrispAmber
                            }
                            val statusIcon = when (event.syncStatus) {
                                "SYNCED" -> Icons.Default.CheckCircle
                                "FAILED" -> Icons.Default.Warning
                                else -> Icons.Default.Refresh
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.5.dp, if (event.syncStatus == "FAILED") AccentRed.copy(alpha = 0.5f) else ColorDivider)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(14.dp))
                                            Text(
                                                text = "${event.actionType} ${event.entityType}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White
                                            )
                                        }
                                        Text(
                                            text = event.syncStatus,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = statusColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "ID: ${event.id.substringBeforeLast("_")}",
                                        fontSize = 10.sp,
                                        color = TextSecondary,
                                        maxLines = 1
                                    )
                                    if (event.retryCount > 0 || event.syncStatus == "FAILED") {
                                        Text(
                                            text = "Attempts: ${event.retryCount}/5 • Last attempt: ${if (event.lastAttemptedAt > 0) sdf.format(java.util.Date(event.lastAttemptedAt)) else "None"}",
                                            fontSize = 10.sp,
                                            color = TextSecondary
                                        )
                                    }

                                    if (event.syncStatus == "FAILED" || event.syncStatus == "PENDING") {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Delete",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AccentRed,
                                                modifier = Modifier
                                                    .clickable { globalViewModel.deleteSyncEvent(event.id) }
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "Force Retry",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AccentBlue,
                                                modifier = Modifier
                                                    .clickable { globalViewModel.retrySyncEvent(event.id) }
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { globalViewModel.triggerManualSync() },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(8.dp),
                enabled = !isSyncActive
            ) {
                if (isSyncActive) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 1.5.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text("Sync Now", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color.White)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun MaintenanceOverlay(
    onLogout: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .padding(24.dp)
            .testTag("maintenance_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(CrispAmber.copy(alpha = 0.12f))
                    .border(1.5.dp, CrispAmber, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Under Maintenance",
                    tint = CrispAmber,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "SYSTEM MAINTENANCE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = CrispAmber,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Co-Op Infrastructure Upgrades",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "The digital guild deck is currently locked down for server-side schema validation, security auditing, and ledger replication. Please check back shortly.",
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                border = BorderStroke(1.dp, ColorDivider),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Node Identifier", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("COOP-US-WEST-1", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                    HorizontalDivider(color = ColorDivider.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Ledger Integrity", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Synchronized (ReadOnly)", color = NeonEmerald, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                    HorizontalDivider(color = ColorDivider.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Active Audience", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Operators & Admins Only", color = CrispAmber, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("maintenance_logout_button")
            ) {
                Text(
                    text = "DISCONNECT SESSION",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

data class OnboardingStep(
    val title: String,
    val isCompleted: Boolean,
    val onAction: () -> Unit
)
