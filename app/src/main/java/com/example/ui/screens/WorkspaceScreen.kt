package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.data.model.Contract
import com.example.data.model.Message
import com.example.data.model.Project
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun WorkspaceScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val selectedWorkspaceId by viewModel.selectedWorkspaceId.collectAsState()

    AnimatedContent(
        targetState = selectedWorkspaceId,
        transitionSpec = {
            slideInHorizontally { width -> if (selectedWorkspaceId != null) width else -width } + fadeIn() togetherWith
                    slideOutHorizontally { width -> if (selectedWorkspaceId != null) -width else width } + fadeOut()
        },
        label = "workspace_view_depth"
    ) { wsId ->
        if (wsId == null) {
            WorkspacesListView(viewModel, modifier)
        } else {
            ActiveWorkspaceView(viewModel, modifier)
        }
    }
}

// BROWSE ACTIVE WORKSPACES
@Composable
fun WorkspacesListView(viewModel: MainViewModel, modifier: Modifier) {
    val projects by viewModel.allProjects.collectAsState()
    
    // Filter projects that are NEGOTIATING or CONTRACTED (meaning pitch matched!)
    val matchedWorkspaces = projects.filter { it.status == "NEGOTIATING" || it.status == "CONTRACTED" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "Your Shared Workspaces",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Active syndicates secured by digital signature and automated payouts.",
            color = TextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (matchedWorkspaces.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Workspaces,
                        contentDescription = "Empty Workspaces",
                        tint = TextSecondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Awaiting joint team matches",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Once you accept a pitch or get swiped right on the Syndicate Board, your joint team workspace unlocks immediately here.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.currentTab.value = "SYNDICATE" },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                    ) {
                        Text("Search Syndicates", color = Color.White)
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(matchedWorkspaces) { workspace ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectedWorkspaceId.value = workspace.id }
                            .testTag("workspace_card_${workspace.id}"),
                        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (workspace.status == "CONTRACTED") ColorSuccess.copy(alpha = 0.2f) else ColorWarning.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (workspace.status == "CONTRACTED") Icons.Default.CheckCircle else Icons.Default.BorderColor,
                                    contentDescription = "Status",
                                    tint = if (workspace.status == "CONTRACTED") ColorSuccess else ColorWarning
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(workspace.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (workspace.status == "CONTRACTED") "UNLOCKED & ACTIVE" else "AWAITING DIGITAL SIGNATURES",
                                        color = if (workspace.status == "CONTRACTED") ColorSuccess else ColorWarning,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Icon(Icons.Default.ChevronRight, contentDescription = "Enter", tint = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

// ACTIVE WORKSPACE VIEW: CHAT, ASSETS, CONTRACT VAULT
@Composable
fun ActiveWorkspaceView(viewModel: MainViewModel, modifier: Modifier) {
    val workspacesList by viewModel.allProjects.collectAsState()
    val wsId by viewModel.selectedWorkspaceId.collectAsState()
    val contract by viewModel.selectedWorkspaceContract.collectAsState()
    val subTab by viewModel.workspaceSubTab.collectAsState()

    val workspace = workspacesList.find { it.id == wsId } ?: return

    val isLocked = workspace.status != "CONTRACTED"

    val user by viewModel.myUser.collectAsState()

    val tabs = remember {
        listOf(
            "CHAT" to "Team Chat",
            "TASKS" to "Co-Op Tasks",
            "ASSETS" to "Asset Pipeline",
            "HUDDLE" to "Live Huddle Room",
            "VAULT" to "Contract Vault",
            "ADMIN" to "Disputes",
            "MCN" to "MCN Compliance"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PrimaryBackground)
    ) {
        // Workspace Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.selectedWorkspaceId.value = null }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Column {
                Text(workspace.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(
                    text = if (isLocked) "🚨 Locked Vault" else "🔒 Secured Escrow Room",
                    color = if (isLocked) ColorWarning else ColorSuccess,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Sub Tab Selector with ScrollableTabRow support for Material 3 responsive safety
        val activeIndex = when (subTab) {
            "CHAT" -> 0
            "TASKS" -> 1
            "ASSETS" -> 2
            "HUDDLE" -> 3
            "VAULT" -> 4
            "ADMIN" -> 5
            "MCN" -> 6
            else -> 0
        }

        ScrollableTabRow(
            selectedTabIndex = if (activeIndex >= 0) activeIndex else 0,
            containerColor = PrimaryBackground,
            contentColor = AccentRed,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                val pos = if (activeIndex >= 0 && activeIndex < tabPositions.size) activeIndex else 0
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[pos]),
                    color = AccentRed
                )
            }
        ) {
            tabs.forEach { (tabId, label) ->
                Tab(
                    selected = subTab == tabId,
                    onClick = { viewModel.workspaceSubTab.value = tabId },
                    text = { Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (subTab) {
                "CHAT" -> ChatSubTabView(viewModel, isLocked)
                "TASKS" -> TasksSubTabView(viewModel, isLocked)
                "ASSETS" -> AssetsSubTabView(viewModel, isLocked)
                "HUDDLE" -> LiveHuddleRoomSubTabView(viewModel, isLocked)
                "VAULT" -> ContractVaultSubTabView(viewModel, contract, workspace)
                "ADMIN" -> AdminConsoleSubTabView(viewModel, workspace)
                "MCN" -> McnSuiteSubTabView(viewModel)
            }
        }
    }
}

// SUB TAB: TEAM CHAT CHANNEL LOGS
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSubTabView(viewModel: MainViewModel, isLocked: Boolean) {
    val messages by viewModel.activeChatMessages.collectAsState()
    val activeChan by viewModel.activeChannel.collectAsState()
    var typedText by remember { mutableStateOf("") }

    val channels = listOf("general", "scripts", "video-drafts")

    if (isLocked) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(Icons.Default.Lock, contentDescription = "Locked", tint = ColorWarning, modifier = Modifier.size(56.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Chat Locked", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Digital smart contracts must be signed by ALL named co-op parties in the Contract Vault to unlock channels and start production.", color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
            }
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Channel Bar selectors
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(channels) { chan ->
                val isSelected = activeChan == chan
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) AccentRed else SurfaceColor)
                        .clickable { viewModel.activeChannel.value = chan }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("#$chan", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Messages Feed
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { msg ->
                Card(
                    modifier = Modifier.fillMaxWidth(0.9f).align(if (msg.senderId == "me") Alignment.End else Alignment.Start),
                    colors = CardDefaults.cardColors(containerColor = if (msg.senderId == "me") SurfaceLightColor else SurfaceColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(msg.senderName, color = AccentRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(AccentBlue).padding(horizontal = 4.dp, vertical = 1.dp)) {
                                Text(msg.senderRole, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(msg.text, color = Color.White, fontSize = 13.sp, lineHeight = 18.sp)
                        
                        if (msg.fileName != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PrimaryBackground)
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.InsertDriveFile, contentDescription = "Uploaded File", tint = AccentRed, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(msg.fileName, color = AccentRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Message input panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    viewModel.sendTeamChatMessage("Shared file: script_draft_v1.pdf", "script_draft_v1.pdf", "local/assets/script_draft_v1.pdf")
                    viewModel.toastMessage.value = "Cinematic script attached as PDF asset!"
                }) {
                    Icon(Icons.Default.AttachFile, contentDescription = "Attach File", tint = TextSecondary)
                }
                
                OutlinedTextField(
                    value = typedText,
                    onValueChange = { typedText = it },
                    modifier = Modifier.weight(1f).testTag("chat_input_text"),
                    placeholder = { Text("Compile team text message...", color = TextSecondary, fontSize = 13.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (typedText.isNotBlank()) {
                            viewModel.sendTeamChatMessage(typedText)
                            typedText = ""
                        }
                    },
                    modifier = Modifier.testTag("chat_send_button")
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = AccentRed)
                }
            }
        }
    }
}

// SUB TAB: ASSET PIPELINE GRID
@Composable
fun AssetsSubTabView(viewModel: MainViewModel, isLocked: Boolean) {
    var connectedOAuth by remember { mutableStateOf(false) }

    if (isLocked) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(Icons.Default.CloudOff, contentDescription = "Locked Assets", tint = ColorWarning, modifier = Modifier.size(56.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Pipeline Locked", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Smart contracts must be signed before pipeline resources unlock.", color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Creative Assets & Shared Drives", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text("Import materials directly from Google Drive or Dropbox integrations.", color = TextSecondary, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(20.dp))

        if (!connectedOAuth) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.CloudQueue, contentDescription = "Drive", tint = AccentBlue, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Connect Google Drive / Dropbox", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Auto-sync draft uploads and heavy cinematic raw video assets directly in-workspace.", color = TextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { connectedOAuth = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                    ) {
                        Text("Connect Cloud Pipelines", color = Color.White)
                    }
                }
            }
        } else {
            // Display simulated uploaded files grid
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("FILES PIPELINE (3)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = {connectedOAuth = false}) {
                    Text("Disconnect Vault", color = AccentRed, fontSize = 11.sp)
                }
            }

            listOf(
                "cinematic_trailer_draft_v1(1080p).mov" to "450 MB",
                "audio_voiceover_narrative.wav" to "22 MB",
                "brand_overlay_graphic_transparent.png" to "4.2 MB"
            ).forEach { (filename, size) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MovieFilter, contentDescription = "Movie", tint = AccentRed)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(filename, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        }
                        Text(size, color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// SUB TAB: CONTRACT VAULT & PAYOUT SPLIT SIMULATION
@Composable
fun ContractVaultSubTabView(
    viewModel: MainViewModel,
    contract: Contract?,
    workspace: Project
) {
    if (contract == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Icon(Icons.Default.ReceiptLong, contentDescription = "Draft Contract", tint = TextSecondary, modifier = Modifier.size(56.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Contract Draft Loading...", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .background(PrimaryBackground)
    ) {
        // FINTECH SIMULATION ZONE (Unlock on SUCCESSFUL Contracted status!)
        if (workspace.status == "CONTRACTED") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .border(1.dp, ColorSuccess.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🛠️ DEMO FINTECH SPLITTER SIMULATOR",
                        color = ColorSuccess,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Contract active! Simulate YouTube AdSense payout split triggers on escrow account.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { viewModel.triggerSimulatedPayoutSplit(4000.00, "YouTube AdSense") },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorSuccess),
                            modifier = Modifier.weight(1f).testTag("sim_adsense_button")
                        ) {
                            Text("Sim Adsense ($4K)", color = Color.White, fontSize = 11.sp)
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { viewModel.triggerSimulatedPayoutSplit(1500.00, "Sponsorship Deal") },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Sim Sponsor ($1.5K)", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Custom drawn legal document layout representation
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "SMART CONTRACT FOR CO-OP: ${contract.projectTitle.uppercase()}",
                            color = AccentRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Docusign Envelope ID: ${contract.docusignEnvelopeId}", color = TextSecondary, fontSize = 10.sp)

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Legal Jurisdiction: ${contract.jurisdiction}\n\n" +
                                    "Agreed Equity Snapshot:\n" +
                                    "- MANAGER HOST: ${contract.hostEquity}%\n" +
                                    "- VIDEO EDITOR: ${contract.editorEquity}%\n" +
                                    "- SCRIPT WRITER: ${contract.writerEquity}%\n\n" +
                                    "Platform Fee Directive:\n" +
                                    "2.5% platform fee deducted securely on split triggers before router distributes earnings.\n\n" +
                                    "Exit Clauses:\n" +
                                    "${contract.exitClauses}",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Digital Signatures Status Panel
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("AUTHENTIC DIGITAL SIGNATURES", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Manager Signature
                        val managerSigned = contract.status == "SIGNED_BY_MANAGER" || contract.status == "FULLY_SIGNED"
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Channel Manager [${contract.managerName}]", color = TextSecondary, fontSize = 12.sp)
                            if (managerSigned) {
                                Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(ColorSuccess.copy(alpha = 0.2f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                    Text("SIGNED ✓", color = ColorSuccess, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.signContract(workspace.id, true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("sign_manager_button")
                                ) {
                                    Text("Sign digitally", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Talent Signature
                        val talentSigned = contract.status == "SIGNED_BY_TALENT" || contract.status == "FULLY_SIGNED"
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("${contract.talentRole} [${contract.talentName}]", color = TextSecondary, fontSize = 12.sp)
                            if (talentSigned) {
                                Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(ColorSuccess.copy(alpha = 0.2f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                    Text("SIGNED ✓", color = ColorSuccess, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.signContract(workspace.id, false) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("sign_talent_button")
                                ) {
                                    Text("Sign digitally", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminConsoleSubTabView(viewModel: MainViewModel, workspace: Project) {
    var feePercentage by remember { mutableStateOf(2.5f) }
    var disputeMinecraftByPlatformResolved by remember { mutableStateOf(false) }
    var disputeTechByPlatformResolved by remember { mutableStateOf(false) }
    var accountStatusFrozen by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "⚖️ Platform Dispute & Mediation Console",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Access: PLATFORM_ADMIN_AUTHORIZED (Standard US-CA / Global Jurisdiction System)",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "MODIFY PLATFORM PROCESSING COMMISSION",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Standard MVP rate is 2.5% for all split routing operations.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Interactive Admin Variable Fee Rate", color = Color.White, fontSize = 12.sp)
                        Text("${String.format("%.1f", feePercentage)}%", color = AccentRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Slider(
                        value = feePercentage,
                        onValueChange = { 
                            feePercentage = it 
                            viewModel.toastMessage.value = "Platform commission variable updated to ${String.format("%.1f", it)}%"
                        },
                        valueRange = 0.5f..12.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = AccentRed,
                            activeTrackColor = AccentRed,
                            inactiveTrackColor = ColorDivider
                        )
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "DISPUTE BOARD MEDIATION",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Case 1
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = PrimaryBackground)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Case #301 - Minecraft Pro Splits", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (disputeMinecraftByPlatformResolved) ColorSuccess.copy(alpha = 0.2f) else ColorWarning.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (disputeMinecraftByPlatformResolved) "RESOLVED" else "WAITING MEDIATION",
                                        color = if (disputeMinecraftByPlatformResolved) ColorSuccess else ColorWarning,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Video editor jack@coop.com dropped work mid-way; channel manager claims 0% editor share. Jack claims 30% for delivered draft assets.",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                            if (!disputeMinecraftByPlatformResolved) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            disputeMinecraftByPlatformResolved = true
                                            viewModel.toastMessage.value = "Dispute #301 resolved in favor of Jack (Editor). Awarded proportional 15% equity."
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Award 15% Split", fontSize = 10.sp, color = Color.White)
                                    }
                                    Button(
                                        onClick = {
                                            disputeMinecraftByPlatformResolved = true
                                            viewModel.toastMessage.value = "Dispute #301 resolved in favor of Channel Host. Full 30% split revoked."
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ColorDivider),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Revoke Share", fontSize = 10.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Case 2
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = PrimaryBackground)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Case #302 - Tech review timeline critique", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (disputeTechByPlatformResolved) ColorSuccess.copy(alpha = 0.2f) else ColorWarning.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (disputeTechByPlatformResolved) "RESOLVED" else "UNDER REVIEW",
                                        color = if (disputeTechByPlatformResolved) ColorSuccess else ColorWarning,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Host claims sound asset timestamps violate original copyright agreements. Sound designer claims licensing is completely clean.",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                            if (!disputeTechByPlatformResolved) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        disputeTechByPlatformResolved = true
                                        viewModel.toastMessage.value = "Dispute #302 Mediation complete: sound assets cleared under Creative Commons."
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Verify & Validate Assets IP", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "SYSTEM MODERATION OVERRIDES",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Freeze escrow balances or suspend co-op smart contracts in case of suspected legal non-compliance.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            accountStatusFrozen = !accountStatusFrozen
                            viewModel.toastMessage.value = if (accountStatusFrozen) "Ecosystem Escrow secured channels FROZEN." else "Ecosystem Escrow secured channels UN-FROZEN & Active."
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (accountStatusFrozen) ColorSuccess else ColorWarning),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (accountStatusFrozen) "UN-FREEZE ACTIVE ESCROWS" else "TEMPORARY FREEZE CO-OP ESCROW SECURED BALANCES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun McnSuiteSubTabView(viewModel: MainViewModel) {
    var runAuditSuccessful by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "🏢 MCN Enterprise Compliance & Analytics Hub",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Authorized: MULTI-CHANNEL NETWORK (MCN) / ENTERPRISE MANAGER",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("ACTIVE SYNDICATES", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("14 Teams", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("NETWORK REACH", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("1.84M Subs", color = AccentBlue, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("CUMULATIVE GROSS", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$38,450", color = ColorSuccess, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "BULK REVENUE SHARE & CONTRACT COMPLIANCE",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    listOf(
                        "Minecraft Sketches" to "40% Host | 30% Editor | 30% Writer",
                        "Premium Tech reviews" to "40% Host | 30% Editor | 30% Writer",
                        "Viral Finance shorts" to "50% Host | 25% Editor | 25% Writer"
                    ).forEachIndexed { i, (name, split) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(split, color = TextSecondary, fontSize = 11.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ColorSuccess.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("ACTIVE & SPLITTING", color = ColorSuccess, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "ENTERPRISE OPERATIONS PANELS",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            runAuditSuccessful = true
                            viewModel.toastMessage.value = "Bulk audit completed successfully: 14/14 active channels compliant with 1099-K tax guidelines."
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("RUN ENTERPRISE IRS 1099-K COMPLIANCE AUDIT", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.toastMessage.value = "Downloaded consolidated statement of co-op payouts (PDF)."
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorDivider),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("DOWNLOAD CONSOLIDATED TAX STATEMENTS", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// SUB TAB: LIVE HUDDLE ROOM (DAILY.CO INTEGRATION SIMULATED OVER SFU WEBRTC BRIDGES)
@Composable
fun LiveHuddleRoomSubTabView(viewModel: MainViewModel, isLocked: Boolean) {
    var isConnected by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var isCameraOff by remember { mutableStateOf(false) }
    var isSharingScreen by remember { mutableStateOf(false) }

    // Periodically cycle conversational speaker indicators
    var activeSpeakerIndex by remember { mutableStateOf(1) }
    val participants = remember {
        listOf(
            "You (Pro Contributor)" to "https://images.unsplash.com/photo-1534528741775-53994a69daeb",
            "Lexi Manager" to "https://images.unsplash.com/photo-1494790108377-be9c29b29330",
            "Jack Editor" to "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d",
            "Alex Admin" to "https://images.unsplash.com/photo-1544005313-94ddf0286df2",
            "Sponsor Representative" to "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2",
            "Lead Writer" to "https://images.unsplash.com/photo-1500648767791-00dcc994a43e",
            "VFX Lead" to "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6",
            "Compliance Executive" to "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e"
        )
    }

    LaunchedEffect(isConnected) {
        if (isConnected) {
            while (true) {
                kotlinx.coroutines.delay(3800)
                activeSpeakerIndex = (1..7).random()
            }
        }
    }

    if (isLocked) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(Icons.Default.Videocam, contentDescription = "Locked Live Room", tint = ColorWarning, modifier = Modifier.size(56.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Huddle Room Locked", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Smart contracts must be signed before the live room and Daily.co video bridges unlock.", color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (!isConnected) {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("huddle_setup_card"),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Groups, contentDescription = "Huddle Setup", tint = AccentRed, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Daily.co Huddle Room", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Encrypted WebRTC call with dual stream HD video, screen sharing pipelines, and automatic noise cancellation filters.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = SurfaceLightColor)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Mic, contentDescription = "Mic Configured", tint = ColorSuccess, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mic Ready", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = SurfaceLightColor)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Videocam, contentDescription = "Cam Configured", tint = ColorSuccess, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Video Ready", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            isConnected = true
                            viewModel.toastMessage.value = "Joined Daily.co WebRTC securely!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                        modifier = Modifier.fillMaxWidth().testTag("join_huddle_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("LAUNCH LIVE HUDDLE CALL (8 ONLINE)", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Live huddle interface
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("🟢 LIVE HUDDLE ACTIVE", color = ColorSuccess, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    Text("Co-op encrypted channel connection via Daily.co WebRTC", color = TextSecondary, fontSize = 11.sp)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ColorSuccess.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NetworkCheck, contentDescription = "Network", tint = ColorSuccess, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("24ms", color = ColorSuccess, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Grid layout displaying up to 8 co-op team participants
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(participants.chunked(2)) { pair ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            pair.forEach { (name, avatar) ->
                                val listIndex = participants.indexOf(name to avatar)
                                val isMe = listIndex == 0
                                val isActiveSpeaker = listIndex == activeSpeakerIndex && !isMe
                                val isMutedUser = listIndex == 3 || listIndex == 5 // simulated states
                                
                                val activeBorderModifier = if (isActiveSpeaker) {
                                    Modifier.border(2.dp, ColorSuccess, RoundedCornerShape(12.dp))
                                } else if (isMe && !isMuted) {
                                    Modifier.border(2.dp, AccentRed, RoundedCornerShape(12.dp))
                                } else {
                                    Modifier
                                }

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(100.dp)
                                        .then(activeBorderModifier),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isActiveSpeaker) SurfaceLightColor else SurfaceColor
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isMe && isCameraOff) AccentRed.copy(alpha = 0.2f) else AccentBlue.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (isMe && isCameraOff) Icons.Default.Videocam else Icons.Default.Person,
                                                    contentDescription = name,
                                                    tint = if (isMe && isCameraOff) AccentRed else Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Text(
                                                text = if (isMe) "$name (You)" else name,
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                textAlign = TextAlign.Center
                                            )
                                        }

                                        // Mic mute icon or Speaking indicators overlay
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(8.dp),
                                            contentAlignment = Alignment.TopEnd
                                        ) {
                                            if (isMe && isMuted) {
                                                Icon(Icons.Default.Mic, contentDescription = "Muted", tint = AccentRed, modifier = Modifier.size(12.dp))
                                            } else if (isMutedUser) {
                                                Icon(Icons.Default.Mic, contentDescription = "Muted", tint = TextSecondary, modifier = Modifier.size(12.dp))
                                            } else if (isActiveSpeaker) {
                                                Icon(Icons.Default.VolumeUp, contentDescription = "Speaking", tint = ColorSuccess, modifier = Modifier.size(12.dp))
                                            }
                                        }

                                        if (isActiveSpeaker) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(8.dp),
                                                contentAlignment = Alignment.BottomStart
                                            ) {
                                                Text("SPEAKING", color = ColorSuccess, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Connection controls console
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute Audio toggle
                    IconButton(
                        onClick = {
                            isMuted = !isMuted
                            viewModel.toastMessage.value = if (isMuted) "Microphone muted" else "Microphone active!"
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isMuted) AccentRed else SurfaceLightColor)
                            .testTag("huddle_mute_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Mute Toggle",
                            tint = Color.White
                        )
                    }

                    // Camera turn-off toggle
                    IconButton(
                        onClick = {
                            isCameraOff = !isCameraOff
                            viewModel.toastMessage.value = if (isCameraOff) "Video camera paused" else "Video camera active!"
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isCameraOff) AccentRed else SurfaceLightColor)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Camera Toggle",
                            tint = Color.White
                        )
                    }

                    // Send screenshare toggle
                    IconButton(
                        onClick = {
                            isSharingScreen = !isSharingScreen
                            viewModel.toastMessage.value = if (isSharingScreen) "Broadcasting screenshare stream to WebRTC pipe!" else "Sharing stopped."
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isSharingScreen) AccentBlue else SurfaceLightColor)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Screenshare Toggle",
                            tint = Color.White
                        )
                    }

                    // Leave or disconnect call
                    IconButton(
                        onClick = {
                            isConnected = false
                            viewModel.toastMessage.value = "Disconnected from Daily.co conference room."
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AccentRed)
                            .testTag("huddle_disconnect_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Leave Huddle",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TasksSubTabView(viewModel: MainViewModel, isLocked: Boolean) {
    if (isLocked) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(Icons.Default.PendingActions, contentDescription = "Locked Tasks", tint = ColorWarning, modifier = Modifier.size(56.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Board Locked", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Co-op tasks are secured by legal escrow. Both named parties must sign the smart contract first to activate this Kanban board.", color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
            }
        }
        return
    }

    var tasksList by remember {
        mutableStateOf(
            listOf(
                LocalTask("1", "Draft Cinematic Script for Episode 5", "Lead Writer", "To Do"),
                LocalTask("2", "Source High-Energy VFX Overlays", "You (Editor)", "In Progress"),
                LocalTask("3", "Design Cinematic Click-Thru Thumbnail", "Thumbnail Designer", "Done"),
                LocalTask("4", "Coordinate Dynamic Music Licensing", "Sound Designer", "To Do"),
                LocalTask("5", "Final Sound Mixing & Level Balancing", "Sound Designer", "In Progress")
            )
        )
    }

    var newTaskTitle by remember { mutableStateOf("") }
    val newTaskAssignee = "You (Editor)"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Co-Op Work Board", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Track collaborative production tasks. Click any task card to transition status.", color = TextSecondary, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // Card to create a new task
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("ADD CO-OP TASK", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        placeholder = { Text("Task title...", color = TextSecondary, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AccentRed,
                            unfocusedBorderColor = ColorDivider
                        ),
                        modifier = Modifier.weight(1f).testTag("kanban_task_title_input"),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            if (newTaskTitle.isNotBlank()) {
                                tasksList = tasksList + LocalTask(
                                    id = System.currentTimeMillis().toString(),
                                    title = newTaskTitle,
                                    assignee = newTaskAssignee,
                                    status = "To Do"
                                )
                                newTaskTitle = ""
                                viewModel.toastMessage.value = "New production task issued to workspace Kanban! 📝"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("kanban_add_task_button")
                    ) {
                        Text("Add", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("To Do", "In Progress", "Done").forEach { columnStatus ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceColor)
                        .padding(8.dp)
                ) {
                    // Column Title
                    Text(
                        text = columnStatus.uppercase(),
                        color = when (columnStatus) {
                            "To Do" -> ColorWarning
                            "In Progress" -> AccentBlue
                            else -> ColorSuccess
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    val colTasks = tasksList.filter { it.status == columnStatus }
                    if (colTasks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .border(1.dp, ColorDivider.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Empty", color = TextSecondary, fontSize = 10.sp)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(colTasks) { item ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            // Click to transition status round-robin style
                                            val nextStatus = when (item.status) {
                                                "To Do" -> "In Progress"
                                                "In Progress" -> "Done"
                                                else -> "To Do"
                                            }
                                            tasksList = tasksList.map {
                                                if (it.id == item.id) it.copy(status = nextStatus) else it
                                            }
                                            viewModel.toastMessage.value = "Task status advanced: $nextStatus! 🚀"
                                        }
                                        .testTag("kanban_task_${item.id}"),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceLightColor)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = item.title,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            lineHeight = 14.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = item.assignee,
                                                color = TextSecondary,
                                                fontSize = 9.sp
                                            )
                                            Icon(
                                                imageVector = Icons.Default.DoubleArrow,
                                                contentDescription = "Status Cycle Indicator",
                                                tint = AccentRed.copy(alpha = 0.6f),
                                                modifier = Modifier.size(10.dp)
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

data class LocalTask(val id: String, val title: String, val assignee: String, val status: String)


