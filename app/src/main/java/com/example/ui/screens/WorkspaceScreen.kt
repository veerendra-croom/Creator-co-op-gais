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

        // Sub Tab Selector: Chat, Assets, Contract Vault
        TabRow(
            selectedTabIndex = when (subTab) {
                "ASSETS" -> 1
                "VAULT" -> 2
                else -> 0
            },
            containerColor = PrimaryBackground,
            contentColor = AccentRed,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[when (subTab) {
                        "ASSETS" -> 1
                        "VAULT" -> 2
                        else -> 0
                    }]),
                    color = AccentRed
                )
            }
        ) {
            listOf("CHAT" to "Team Chat", "ASSETS" to "Asset Pipeline", "VAULT" to "Contract Vault").forEach { (tabId, label) ->
                Tab(
                    selected = subTab == tabId,
                    onClick = { viewModel.workspaceSubTab.value = tabId },
                    text = { Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (subTab) {
                "CHAT" -> ChatSubTabView(viewModel, isLocked)
                "ASSETS" -> AssetsSubTabView(viewModel, isLocked)
                "VAULT" -> ContractVaultSubTabView(viewModel, contract, workspace)
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
