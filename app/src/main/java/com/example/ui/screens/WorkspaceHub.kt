package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.model.Workspace
import com.example.ui.screens.workspace.*
import com.example.ui.theme.*
import com.example.ui.viewmodels.*
import com.example.ui.components.*

@Composable
fun WorkspaceHub(
    workspaceViewModel: WorkspaceViewModel,
    agreementViewModel: AgreementViewModel,
    chatViewModel: ChatViewModel,
    userId: String,
    userProfile: UserProfile?,
    globalViewModel: GlobalViewModel? = null,
    onNavigateToCreate: () -> Unit = {},
    onNavigateToDiscovery: () -> Unit = {}
) {
    val workspaces by workspaceViewModel.workspaces.collectAsStateWithLifecycle()
    val selectedWorkspace by workspaceViewModel.selectedWorkspace.collectAsStateWithLifecycle()
    val viewMode by workspaceViewModel.workspaceViewMode.collectAsStateWithLifecycle()

    if (viewMode == "LIST" || selectedWorkspace == null) {
        WorkspaceList(
            workspaces = workspaces,
            onSelect = { workspaceViewModel.selectWorkspace(it) },
            onCreateClick = onNavigateToCreate,
            onDiscoveryClick = onNavigateToDiscovery
        )
    } else {
        WorkspaceDetailContainer(
            workspace = selectedWorkspace!!,
            workspaceViewModel = workspaceViewModel,
            agreementViewModel = agreementViewModel,
            chatViewModel = chatViewModel,
            userId = userId,
            userProfile = userProfile,
            globalViewModel = globalViewModel,
            onBack = { workspaceViewModel.selectWorkspace(null) }
        )
    }
}

@Composable
fun WorkspaceList(
    workspaces: List<Workspace>,
    onSelect: (Workspace) -> Unit,
    onCreateClick: () -> Unit,
    onDiscoveryClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(PrimaryBackground)) {
        Box(modifier = Modifier.fillMaxWidth().height(84.dp)) {
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(AccentBlue.copy(alpha = 0.08f), PrimaryBackground))))
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(horizontal = DS.Space16, vertical = DS.Space12)) {
                Text(text = "MY WORKSPACES", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 1.sp)
                Text(text = "${workspaces.size} Active Collaborative Co-Ops", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.ExtraBold)
            }
        }

        Spacer(modifier = Modifier.height(DS.Space8))

        if (workspaces.isEmpty()) {
            Box(modifier = Modifier.weight(1f).padding(DS.Space16), contentAlignment = Alignment.Center) {
                EmptyStateCard(
                    headline = "NO COLLABORATION HUBS",
                    supportingText = "You haven't joined any workspace nodes yet. Every successful production starts with a secure collaborative environment.",
                    primaryCtaLabel = "ESTABLISH NEW NODE",
                    onPrimaryCta = onCreateClick,
                    secondaryCtaLabel = "BROWSE PUBLIC PROJECTS",
                    onSecondaryCta = onDiscoveryClick,
                    accentColor = AccentBlue,
                    showFabCue = true
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(DS.Space16),
                contentPadding = PaddingValues(horizontal = DS.Space16, vertical = DS.Space8),
                modifier = Modifier.weight(1f)
            ) {
                items(workspaces, key = { it.id }) { ws ->
                    WorkspaceListItem(ws = ws, onSelect = onSelect)
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth().padding(DS.Space24)) {
            Button(
                onClick = onCreateClick,
                modifier = Modifier.fillMaxWidth().height(56.dp).testTag("create_new_workspace_primary_button"),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = DS.RadiusLarge
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(DS.Space8))
                Text("CREATE NEW WORKSPACE", fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
fun WorkspaceListItem(ws: Workspace, onSelect: (Workspace) -> Unit) {
    val (platformColor, platformIcon) = when(ws.platformType.uppercase()) {
        "YOUTUBE" -> Pair(MaterialTheme.colorScheme.primary, Icons.Default.PlayArrow)
        "INSTAGRAM" -> Pair(MaterialTheme.colorScheme.error, Icons.Default.CameraAlt)
        "TIKTOK" -> Pair(MaterialTheme.colorScheme.secondary, Icons.Default.MusicNote)
        else -> Pair(MaterialTheme.colorScheme.secondary, Icons.Default.Groups)
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onSelect(ws) },
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = DS.RadiusLarge,
        border = BorderStroke(1.dp, ColorDivider)
    ) {
        Column(modifier = Modifier.padding(DS.Space16)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(DS.RadiusMedium)
                        .background(platformColor.copy(alpha = 0.12f))
                        .border(1.dp, platformColor.copy(alpha = 0.2f), DS.RadiusMedium),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = platformIcon, contentDescription = null, tint = platformColor, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(DS.Space12))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = ws.name, color = Color.White, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Black)
                    Text(text = ws.platformType, color = platformColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }
                
                // Trust Score Indicator
                Column(horizontalAlignment = Alignment.End) {
                    TrustBadge(score = 98) 
                    Spacer(modifier = Modifier.height(DS.Space4))
                    Text("ACTIVE NOW", color = NeonEmerald, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                }
            }
            
            Spacer(modifier = Modifier.height(DS.Space16))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(DS.Space16)) {
                    WorkspaceStat(label = "MEMBERS", value = "12", icon = Icons.Default.Person)
                    WorkspaceStat(label = "TASKS", value = "4", icon = Icons.Default.Assignment)
                }
                
                if (ws.isSponsored) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(AccentBlue.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "SPONSORED",
                            color = AccentBlue,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorkspaceStat(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun WorkspaceDetailContainer(
    workspace: Workspace,
    workspaceViewModel: WorkspaceViewModel,
    agreementViewModel: AgreementViewModel,
    chatViewModel: ChatViewModel,
    userId: String,
    userProfile: UserProfile?,
    globalViewModel: GlobalViewModel? = null,
    onBack: () -> Unit
) {
    LaunchedEffect(userId) {
        workspaceViewModel.currentUserIdFlow.value = userId
    }

    val members by workspaceViewModel.activeWorkspaceMembers.collectAsState()
    val isMember = members.any { it.userId == userId } || workspace.createdBy == userId
    
    if (!isMember) {
        Box(modifier = Modifier.fillMaxSize().background(PrimaryBackground), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(Icons.Default.Lock, null, tint = AccentRed, modifier = Modifier.size(64.dp))
                Text("Access Denied", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)) { Text("Go Back") }
            }
        }
        return
    }

    val activeModule by workspaceViewModel.workspaceSubTab.collectAsState()
    val activeAgreement by remember(workspace.id) { agreementViewModel.getActiveAgreement(workspace.id) }.collectAsState(initial = null)
    val isAgreementLocked = activeAgreement?.isLocked == true
    val isLead = members.any { it.userId == userId && it.assignedRoleTitle in listOf("Lead Creator", "Head") } || workspace.createdBy == userId
    
    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(SurfaceColor)) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            if (activeModule != "STATE") {
                                workspaceViewModel.workspaceSubTab.value = "STATE"
                            } else {
                                onBack()
                            }
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("workspace_hub_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        if (workspace.isSponsored) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Celebration, null, tint = AccentBlue, modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PRESENTED BY ${workspace.sponsorName ?: "PARTNER"}", color = AccentBlue, fontSize = 8.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        Text(workspace.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Active Project Hub", fontSize = 10.sp, color = TextSecondary)
                    }
                }
                
                ScrollableTabRow(
                    selectedTabIndex = when(activeModule) {
                        "STATE" -> 0
                        "SANDBOX" -> 1
                        "PRODUCTION" -> 2
                        "CHAT" -> 3
                        "TEAM" -> 4
                        "AGREEMENT" -> 5
                        else -> 0
                    },
                    containerColor = SurfaceColor,
                    contentColor = AccentBlue,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        val idx = when(activeModule) {
                            "STATE" -> 0
                            "SANDBOX" -> 1
                            "PRODUCTION" -> 2
                            "CHAT" -> 3
                            "TEAM" -> 4
                            "AGREEMENT" -> 5
                            else -> 0
                        }
                        if (idx < tabPositions.size) TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(tabPositions[idx]), color = AccentBlue)
                    },
                    divider = {}
                ) {
                    Tab(selected = activeModule == "STATE", onClick = { workspaceViewModel.workspaceSubTab.value = "STATE" }, text = { Text("Overview", fontSize = 12.sp, fontWeight = FontWeight.Bold) })
                    Tab(selected = activeModule == "SANDBOX", onClick = { workspaceViewModel.workspaceSubTab.value = "SANDBOX" }, text = { Text("My Drafts", fontSize = 12.sp, fontWeight = FontWeight.Bold) })
                    Tab(selected = activeModule == "PRODUCTION", onClick = { workspaceViewModel.workspaceSubTab.value = "PRODUCTION" }, text = { Text("Tasks", fontSize = 12.sp, fontWeight = FontWeight.Bold) })
                    Tab(selected = activeModule == "CHAT", onClick = { workspaceViewModel.workspaceSubTab.value = "CHAT" }, text = { Text("Chat", fontSize = 12.sp, fontWeight = FontWeight.Bold) })
                    Tab(selected = activeModule == "TEAM", onClick = { workspaceViewModel.workspaceSubTab.value = "TEAM" }, text = { Text("Team", fontSize = 12.sp, fontWeight = FontWeight.Bold) })
                    Tab(selected = activeModule == "AGREEMENT", onClick = { workspaceViewModel.workspaceSubTab.value = "AGREEMENT" }, text = { Text("Vault", fontSize = 12.sp, fontWeight = FontWeight.Bold) })
                }
                
                if (workspace.isSponsored) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.linearGradient(listOf(AccentBlue.copy(alpha = 0.15f), Color.Transparent)))
                            .padding(vertical = 10.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Celebration,
                                contentDescription = "Sponsor Badge",
                                tint = AccentBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Presented by ${workspace.sponsorName ?: "Premium Partner"}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        },
        containerColor = PrimaryBackground
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            AnimatedContent(
                targetState = activeModule,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "module_switch"
            ) { module ->
                when (module) {
                    "STATE" -> WorkspaceOverview(
                        workspace = workspace,
                        viewModel = workspaceViewModel,
                        isAgreementActive = isAgreementLocked,
                        onGoToAgreement = { workspaceViewModel.workspaceSubTab.value = "AGREEMENT" },
                        onGoToTasks = { workspaceViewModel.workspaceSubTab.value = "PRODUCTION" },
                        onGoToChat = { workspaceViewModel.workspaceSubTab.value = "CHAT" },
                        onGoToTeam = { workspaceViewModel.workspaceSubTab.value = "TEAM" },
                        globalViewModel = globalViewModel
                    )
                    "SANDBOX" -> PersonalSpaceScreen(workspaceViewModel, workspace.id, userId)
                    "PRODUCTION" -> TeamSpaceScreen(
                        viewModel = workspaceViewModel,
                        workspaceId = workspace.id,
                        userId = userId,
                        isAgreementActive = isAgreementLocked,
                        onGoToAgreement = { workspaceViewModel.workspaceSubTab.value = "AGREEMENT" },
                        globalViewModel = globalViewModel
                    )
                    "CHAT" -> WorkspaceChat(chatViewModel, workspace.id, userId, userProfile, isAgreementLocked)
                    "TEAM" -> WorkspaceMembers(workspaceViewModel, workspace.id, workspace, userId)
                    "AGREEMENT" -> {
                        val flags by workspaceViewModel.featureFlags.collectAsState()
                        AgreementVault(
                            viewModel = agreementViewModel,
                            workspaceId = workspace.id,
                            userId = userId,
                            isLead = isLead,
                            featureFlags = flags,
                            userRole = if (isLead) "ORGANIZER" else (userProfile?.systemRole ?: "PARTICIPANT"),
                            onBack = { workspaceViewModel.workspaceSubTab.value = "STATE" },
                            globalViewModel = globalViewModel
                        )
                    }
                }
            }
        }
    }
}
