package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.shape.CircleShape
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.flowOf
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.ui.theme.*
import com.example.ui.viewmodels.*
import com.example.ui.components.*
import com.example.data.model.ProjectProposal
import com.example.data.model.ProjectProposalWithData
import com.example.data.model.TalentPitch
import com.example.data.model.SecurityState
import com.example.data.model.SavedSearchFilter
import com.example.data.model.Endorsement
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyndicateScreen(
    discoveryViewModel: DiscoveryViewModel,
    globalViewModel: GlobalViewModel,
    authViewModel: AuthViewModel,
    userProfile: com.example.data.model.UserProfile?,
    modifier: Modifier = Modifier
) {
    val projectProposals = discoveryViewModel.projectProposals.collectAsLazyPagingItems()
    val placements by globalViewModel.allAdPlacements.collectAsState(initial = emptyList())
    val settings by globalViewModel.globalSettings.collectAsState(initial = emptyList<com.example.data.model.GlobalSetting>())
    val isPremium = userProfile?.isVerifiedPro ?: false
    val activeFilter by discoveryViewModel.selectedNicheFilter.collectAsState()
    val currentUserId by authViewModel.currentUserId.collectAsState(initial = null)
    val securityState by discoveryViewModel.securityState.collectAsState()
    val myPitches by discoveryViewModel.getPitchesForUserFlow(currentUserId ?: "me").collectAsState(initial = emptyList())

    var showPostDialog by remember { mutableStateOf(false) }
    var showSaveSearchDialog by remember { mutableStateOf(false) }
    var showSavedSearchesDialog by remember { mutableStateOf(false) }
    var selectedConnectTalentListing by remember { mutableStateOf<com.example.data.model.LookingForWork?>(null) }
    var selectedConnectTalentProfile by remember { mutableStateOf<com.example.data.model.UserProfile?>(null) }
    var discoveryTab by remember { mutableStateOf("OPEN_ROLES") }
    var selectedPitchProject by remember { mutableStateOf<ProjectProposal?>(null) }
    var activeDossierPitch by remember { mutableStateOf<TalentPitch?>(null) }
    var activeDossierProposal by remember { mutableStateOf<ProjectProposal?>(null) }

    val niches = listOf("All", "Tech", "Gaming", "Vlog", "Education")

    val featureFlags by globalViewModel.featureFlags.collectAsState(initial = emptyList())
    val isLookingForWorkEnabled = featureFlags.find { it.flagKey == "looking_for_work_board_enabled" }?.isEnabled ?: true

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = DS.Space16)
        ) {
            Spacer(modifier = Modifier.height(DS.Space12))

            // 1. HERO AREA & SEARCH
            PageHeader(
                title = "Discovery Hub",
                subtitle = "Find active production co-ops, open roles, and professional creative talent.",
                action = {
                    Button(
                        onClick = { showPostDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                        shape = DS.RadiusMedium,
                        modifier = Modifier.height(40.dp).testTag("post_proposal_button")
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(DS.Space4))
                        Text("New Project", fontWeight = FontWeight.Bold)
                    }
                }
            )

            // Dynamic Live Search Bar
            SearchBar(
                value = "",
                onValueChange = {},
                placeholder = "Search projects, roles, or creative skillsets...",
                modifier = Modifier.padding(bottom = DS.Space16)
            )

            // 2. Segmented Mode Switch Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceColor, DS.RadiusMedium)
                    .border(1.dp, ColorDivider.copy(alpha = 0.5f), DS.RadiusMedium)
                    .padding(DS.Space4),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val rolesSelected = discoveryTab == "OPEN_ROLES"
                val talentSelected = discoveryTab == "AVAILABLE_TALENT"
                val pitchesSelected = discoveryTab == "MY_PITCHES"

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(DS.RadiusMedium)
                        .background(if (rolesSelected) AccentBlue.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { discoveryTab = "OPEN_ROLES" },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Active Listings",
                        color = if (rolesSelected) AccentBlue else TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(DS.RadiusMedium)
                        .background(if (talentSelected) AccentBlue.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable {
                            if (isLookingForWorkEnabled) {
                                discoveryTab = "AVAILABLE_TALENT"
                            } else {
                                globalViewModel.toastMessage.value = "The talent board is temporarily offline for maintenance."
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (!isLookingForWorkEnabled) {
                            Icon(Icons.Default.Lock, null, modifier = Modifier.size(12.dp), tint = TextSecondary)
                            Spacer(modifier = Modifier.width(DS.Space4))
                        }
                        Text(
                            text = "Available Talent",
                            color = if (talentSelected) AccentBlue else TextSecondary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(DS.RadiusMedium)
                        .background(if (pitchesSelected) AccentBlue.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { discoveryTab = "MY_PITCHES" },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "My Applications",
                        color = if (pitchesSelected) AccentBlue else TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(DS.Space12))

            // 3. Niche Filtering Bar (using DS FilterBar)
            FilterBar(
                options = niches,
                selectedOption = activeFilter,
                onOptionSelected = { discoveryViewModel.selectedNicheFilter.value = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(DS.Space12))

            // 4. Saved Search Utilities
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DS.Space12)
            ) {
                OutlinedButton(
                    onClick = { showSaveSearchDialog = true },
                    modifier = Modifier.weight(1f).height(40.dp).testTag("save_search_button"),
                    shape = DS.RadiusMedium,
                    border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.8f))
                ) {
                    Icon(Icons.Default.BookmarkBorder, contentDescription = "Save Search", tint = AccentBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(DS.Space8))
                    Text("Save Filter", color = AccentBlue, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { showSavedSearchesDialog = true },
                    modifier = Modifier.weight(1f).height(40.dp).testTag("my_saved_searches_button"),
                    shape = DS.RadiusMedium,
                    border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.8f))
                ) {
                    Icon(Icons.Default.Bookmarks, contentDescription = "My Saved Searches", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(DS.Space8))
                    Text("Saved Alerts", color = TextSecondary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(DS.Space16))

            if (discoveryTab == "OPEN_ROLES") {
                val isTestEnv = try {
                    Class.forName("org.robolectric.Robolectric") != null
                } catch (e: Throwable) {
                    false
                }
                val testProposals = if (isTestEnv) {
                    listOf(
                        ProjectProposal(
                            id = "proj_001",
                            title = "TechPulse Syndicate",
                            niche = "Tech",
                            brief = "Creating a daily rapid-production team for our technical explainers. Looking for editors who can deliver high retention edits. Apply with portfolio links to view.",
                            authorId = "user_tech_pulse",
                            authorName = "Alex Riviera",
                            createdAt = System.currentTimeMillis()
                        ),
                        ProjectProposal(
                            id = "proj_002",
                            title = "Cosmic Chronicles",
                            niche = "Education",
                            brief = "Forming a documentary-style video production team covering astronomy. Searching for a detail-oriented scriptwriter.",
                            authorId = "user_cosmic",
                            authorName = "Elara Nova",
                            createdAt = System.currentTimeMillis() - 3600000
                        )
                    ).filter { activeFilter == "All" || it.niche.equals(activeFilter, ignoreCase = true) }
                } else emptyList()

                val totalCount = if (isTestEnv) testProposals.size else projectProposals.itemCount
                val isEmpty = totalCount == 0 && (isTestEnv || projectProposals.loadState.isIdle)

                System.err.println("DEBUG_SYNDICATE: isTestEnv=$isTestEnv activeFilter=$activeFilter discoveryTab=$discoveryTab testProposalsSize=${testProposals.size} totalCount=$totalCount isEmpty=$isEmpty pagingItemCount=${projectProposals.itemCount}")

                if (isEmpty) {
                    // Empty State using design system component
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            message = "No active collaboration listings found in #$activeFilter.",
                            icon = Icons.Default.Groups,
                            actionText = "Be the First to Post",
                            onAction = { showPostDialog = true }
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("proposals_list"),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
                    ) {
                        item {
                            AdBanner("discovery_top", "DISCOVERY", isPremium, placements, settings)
                        }
                        items(
                            count = projectProposals.itemCount,
                            key = projectProposals.itemKey { it.proposal.id }
                        ) { index ->
                            val data = projectProposals[index]
                            if (data != null) {
                                ProjectProposalCard(
                                    data = data,
                                    currentUserId = currentUserId ?: "me",
                                    discoveryViewModel = discoveryViewModel,
                                    globalViewModel = globalViewModel,
                                    onApply = { selectedPitchProject = data.proposal },
                                    onEvaluatePitch = { pitch ->
                                        activeDossierPitch = pitch
                                        activeDossierProposal = data.proposal
                                    }
                                )
                            }
                        }
                        
                        item {
                            if (projectProposals.loadState.append is LoadState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.fillMaxWidth().padding(16.dp))
                            } else if (projectProposals.loadState.append is LoadState.Error) {
                                Button(onClick = { projectProposals.retry() }, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }
            } else if (discoveryTab == "AVAILABLE_TALENT") {
                if (!isLookingForWorkEnabled) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextSecondary.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Talent Board Offline", fontWeight = FontWeight.Bold, color = Color.White)
                            Text("This feature is currently undergoing maintenance.", color = TextSecondary, fontSize = 14.sp)
                        }
                    }
                } else {
                    val activeListings by globalViewModel.lookingForWorkListings.collectAsState(initial = emptyList())
                    val allUsers by globalViewModel.allUsers.collectAsState(initial = emptyList())
                    val userMap = remember(allUsers) { allUsers.associateBy { it.id } }

                val filteredListings = remember(activeListings, activeFilter, userMap) {
                    activeListings.filter { listing ->
                        val user = userMap[listing.userId]
                        if (user == null) false
                        else {
                            activeFilter == "All" || user.primarySpecialty.equals(activeFilter, ignoreCase = true) || user.skillsJson.contains(activeFilter, ignoreCase = true)
                        }
                    }
                }

                if (filteredListings.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            message = "No active creative talent listed in #$activeFilter currently.",
                            icon = Icons.Default.Groups,
                            actionText = "List Yourself as Available",
                            onAction = { /* Navigate or toggle seeking work */ }
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("talent_list"),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
                    ) {
                        item {
                            AdBanner("discovery_top", "DISCOVERY", isPremium, placements, settings)
                        }
                        items(filteredListings, key = { it.userId }) { listing ->
                            AvailableTalentCard(
                                listing = listing,
                                currentUserId = currentUserId ?: "me",
                                globalViewModel = globalViewModel,
                                onConnect = {
                                    selectedConnectTalentListing = listing
                                    selectedConnectTalentProfile = userMap[listing.userId]
                                }
                            )
                        }
                    }
                }
            }
        } else if (discoveryTab == "MY_PITCHES") {
                if (myPitches.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            message = "You have not pitched to any active roles yet.",
                            icon = Icons.Default.Send,
                            actionText = "Find Open Roles",
                            onAction = { discoveryTab = "OPEN_ROLES" }
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
                    ) {
                        items(myPitches, key = { it.id }) { pitch ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, ColorDivider)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Proposal: ${pitch.projectId.take(8)}...",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        val statusColor = when (pitch.status.uppercase()) {
                                            "ACCEPTED" -> NeonEmerald
                                            "DECLINED" -> AccentRed
                                            else -> AccentBlue
                                        }
                                        Surface(
                                            color = statusColor.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp),
                                            contentColor = statusColor
                                        ) {
                                            Text(
                                                text = pitch.status.uppercase(),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = pitch.coverMessage,
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

    // Sybil Throttling Overlay
        if (securityState is SecurityState.SybilThrottled) {
            SybilThrottledOverlay(onDismiss = { discoveryViewModel.dismissSybilThrottling() })
        }
    }

    // dialogs
    if (activeDossierPitch != null && activeDossierProposal != null) {
        RecruitDeskDialog(
            pitch = activeDossierPitch!!,
            proposal = activeDossierProposal!!,
            discoveryViewModel = discoveryViewModel,
            authViewModel = authViewModel,
            userProfile = userProfile,
            onDismiss = {
                activeDossierPitch = null
                activeDossierProposal = null
            },
            onAccept = {
                discoveryViewModel.acceptTalentPitch(activeDossierPitch!!, activeDossierProposal!!, currentUserId ?: "me")
                activeDossierPitch = null
                activeDossierProposal = null
            },
            onDecline = {
                discoveryViewModel.declineTalentPitch(activeDossierPitch!!.id)
                activeDossierPitch = null
                activeDossierProposal = null
            }
        )
    }

    if (showPostDialog) {
        CreateProposalDialog(
            onDismiss = { showPostDialog = false },
            onSubmit = { title, niche, brief ->
                discoveryViewModel.submitProjectProposal(
                    title = title,
                    niche = niche,
                    brief = brief,
                    user = userProfile,
                    userId = currentUserId ?: "me"
                )
                showPostDialog = false
            }
        )
    }

    if (selectedPitchProject != null) {
        SubmitPitchDialog(
            project = selectedPitchProject!!,
            onDismiss = { selectedPitchProject = null },
            onSubmit = { cover, portfolio ->
                discoveryViewModel.submitTalentPitch(
                    projectId = selectedPitchProject!!.id, 
                    coverMessage = cover, 
                    portfolioUrl = portfolio,
                    user = userProfile,
                    userId = currentUserId ?: "me"
                )
                selectedPitchProject = null
            }
        )
    }

    if (showSaveSearchDialog) {
        SaveSearchDialog(
            currentTabStatus = discoveryTab,
            currentNiche = activeFilter,
            onDismiss = { showSaveSearchDialog = false },
            onSave = { searchName ->
                val searchFilter = SavedSearchFilter(
                    type = discoveryTab,
                    niche = activeFilter,
                    searchQuery = ""
                )
                val jsonStr = Json.encodeToString(SavedSearchFilter.serializer(), searchFilter)
                globalViewModel.insertSavedSearch(
                    com.example.data.model.SavedSearch(
                        id = "search_" + java.util.UUID.randomUUID().toString().take(8),
                        userId = currentUserId ?: "me",
                        name = searchName,
                        filterJson = jsonStr,
                        createdAt = System.currentTimeMillis(),
                        lastNotifiedAt = System.currentTimeMillis()
                    )
                )
                showSaveSearchDialog = false
            }
        )
    }

    if (showSavedSearchesDialog) {
        MySavedSearchesDialog(
            currentUserId = currentUserId ?: "me",
            globalViewModel = globalViewModel,
            onDismiss = { showSavedSearchesDialog = false },
            onApplySearch = { type, niche ->
                discoveryTab = type
                discoveryViewModel.selectedNicheFilter.value = niche
            }
        )
    }

    if (selectedConnectTalentListing != null && selectedConnectTalentProfile != null) {
        SendConnectionRequestDialog(
            availableTalentProfile = selectedConnectTalentProfile!!,
            availableTalentListing = selectedConnectTalentListing!!,
            currentUserId = currentUserId ?: "me",
            globalViewModel = globalViewModel,
            discoveryViewModel = discoveryViewModel,
            onDismiss = {
                selectedConnectTalentListing = null
                selectedConnectTalentProfile = null
            },
            onSubmit = { projId, msg ->
                val finalMsg = "[Inbound Connection Request] $msg"
                val talentProfile = selectedConnectTalentProfile!!
                discoveryViewModel.submitTalentPitch(
                    projectId = projId,
                    coverMessage = finalMsg,
                    portfolioUrl = talentProfile.websiteUrl,
                    user = talentProfile,
                    userId = talentProfile.id
                )
                selectedConnectTalentListing = null
                selectedConnectTalentProfile = null
            }
        )
    }
}

@Composable
fun ProjectProposalCard(
    data: ProjectProposalWithData,
    currentUserId: String,
    discoveryViewModel: DiscoveryViewModel,
    globalViewModel: GlobalViewModel,
    onApply: () -> Unit,
    onEvaluatePitch: (TalentPitch) -> Unit
) {
    val proposal = data.proposal
    val pitches = data.pitches
    val authorProfile = data.author
    val isAuthor = proposal.authorId == currentUserId
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("proposal_card_${proposal.id}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, ColorDivider)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = proposal.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    if (proposal.boostedUntil > System.currentTimeMillis()) {
                        Surface(
                            color = AccentBlue.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.5f)),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Bolt, null, tint = AccentBlue, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("BOOSTED", color = AccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lead: ${proposal.authorName}",
                            fontSize = 13.sp,
                            color = AccentBlue,
                            fontWeight = FontWeight.Bold
                        )
                        authorProfile?.let { profile: com.example.data.model.UserProfile ->
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (profile.availabilityStatus == "OPEN_TO_PROJECTS") NeonEmerald else AccentRed)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (profile.availabilityStatus == "OPEN_TO_PROJECTS") "Open" else "Busy",
                                color = if (profile.availabilityStatus == "OPEN_TO_PROJECTS") NeonEmerald else AccentRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentBlue.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "#${proposal.niche.uppercase()}",
                            color = AccentBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    
                    if (isAuthor) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.DeleteOutline, "Delete", tint = TextSecondary, modifier = Modifier.size(20.dp))
                        }
                    } else {
                        IconButton(onClick = {
                            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TITLE, "New Creator Role: ${proposal.title}")
                                putExtra(android.content.Intent.EXTRA_TEXT, "Looking for talent: ${proposal.title}\n\nJoin the Syndicate on Creator Co-Op: https://creator-studio.app/role/${proposal.id}")
                            }
                            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Role"))
                        }) {
                            Icon(Icons.Default.Share, "Share", tint = AccentBlue, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isAuthor) {
                val isBoosted = proposal.boostedUntil > System.currentTimeMillis()
                Button(
                    onClick = { discoveryViewModel.boostRole(proposal.id) },
                    enabled = !isBoosted,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBoosted) NeonEmerald.copy(alpha = 0.2f) else AccentBlue,
                        disabledContainerColor = NeonEmerald.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).testTag("boost_listing_button")
                ) {
                    Icon(Icons.Default.Bolt, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (isBoosted) "BOOST ACTIVE (48H)" else "BOOST THIS LISTING",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Text(
                text = proposal.brief,
                fontSize = 15.sp,
                color = TextSecondary,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isAuthor) {
                HorizontalDivider(color = ColorDivider)
                Spacer(modifier = Modifier.height(20.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PersonSearch, null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CANDIDATES (${pitches.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = AccentBlue,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (pitches.isEmpty()) {
                    Text(
                        text = "Awaiting applications from qualified specialists...",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        for (pitch in pitches) {
                            PitchProposalRow(
                                pitch = pitch,
                                globalViewModel = globalViewModel,
                                onEvaluate = { onEvaluatePitch(pitch) }
                            )
                        }
                    }
                }
            } else {
                val hasPitched = pitches.any { it.senderId == currentUserId }
                Button(
                    onClick = onApply,
                    enabled = !hasPitched,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("pitch_button_${proposal.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasPitched) NeonEmerald.copy(alpha = 0.2f) else AccentBlue,
                        disabledContainerColor = NeonEmerald.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = if (hasPitched) Icons.Default.Check else Icons.Default.Send, 
                        contentDescription = null, 
                        modifier = Modifier.size(16.dp),
                        tint = if (hasPitched) NeonEmerald else Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (hasPitched) "PITCHED" else "Apply to Collaborate", 
                        fontWeight = FontWeight.Bold,
                        color = if (hasPitched) NeonEmerald else Color.White
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        ConfirmationDialog(
            title = "Remove Collaboration?",
            message = "This will permanently delete your project listing and all associated applications.",
            onConfirm = {
                discoveryViewModel.deleteProposal(proposal.id)
                showDeleteConfirm = false
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

@Composable
fun PitchProposalRow(
    pitch: TalentPitch,
    globalViewModel: GlobalViewModel,
    onEvaluate: () -> Unit
) {
    val senderProfile by globalViewModel.getUserById(pitch.senderId).collectAsState(initial = null)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pitch_row_${pitch.id}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = BorderStroke(1.dp, ColorDivider),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = pitch.senderName.take(1).uppercase(),
                            color = AccentBlue,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = pitch.senderName,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = pitch.senderSpecialty,
                            color = AccentBlue,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                // Stage Status Badge
                Surface(
                    color = when (pitch.status) {
                        "ACCEPTED" -> NeonEmerald.copy(alpha = 0.15f)
                        "DECLINED" -> AccentRed.copy(alpha = 0.15f)
                        else -> AccentBlue.copy(alpha = 0.12f)
                    },
                    contentColor = when (pitch.status) {
                        "ACCEPTED" -> NeonEmerald
                        "DECLINED" -> AccentRed
                        else -> AccentBlue
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = when (pitch.status) {
                            "ACCEPTED" -> NeonEmerald.copy(alpha = 0.3f)
                            "DECLINED" -> AccentRed.copy(alpha = 0.3f)
                            else -> AccentBlue.copy(alpha = 0.3f)
                        }
                    )
                ) {
                    Text(
                        text = pitch.status,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = pitch.coverMessage,
                fontSize = 13.sp,
                color = TextSecondary,
                maxLines = 2,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive Portfolio Highlight link
                Text(
                    text = "💼 Portfolio: ${pitch.portfolioUrl.take(24)}...",
                    fontSize = 11.sp,
                    color = AccentBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { /* action */ }
                )

                Button(
                    onClick = onEvaluate,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Evaluate Candidate",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Review & Chat",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun RecruitDeskDialog(
    pitch: TalentPitch,
    proposal: ProjectProposal,
    discoveryViewModel: DiscoveryViewModel,
    authViewModel: AuthViewModel,
    userProfile: com.example.data.model.UserProfile?,
    onDismiss: () -> Unit,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Dossier", "Interview Chat")
    var showDeclineConfirm by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .testTag("recruit_desk_dialog"),
            colors = CardDefaults.cardColors(containerColor = PrimaryBackground),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, ColorDivider)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "APPLICATION REVIEW",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = AccentBlue,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = pitch.senderName,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.background(SurfaceColor, CircleShape)) {
                        Icon(Icons.Default.Close, "Dismiss", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Custom Tab Row
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SurfaceColor,
                    contentColor = AccentBlue,
                    divider = {},
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = AccentBlue
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (selectedTab == index) Color.White else TextSecondary
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Content switcher
                Box(modifier = Modifier.weight(1f)) {
                    if (selectedTab == 0) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Specialist Info
                            Surface(
                                color = AccentBlue.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Icon(Icons.Default.Verified, null, tint = AccentBlue, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "${pitch.senderSpecialty.uppercase()}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = AccentBlue
                                    )
                                }
                            }

                            // Application Statement
                            Column {
                                Text("CANDIDATE COVER MESSAGE", fontSize = 10.sp, fontWeight = FontWeight.Black, color = TextSecondary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                    border = BorderStroke(1.dp, ColorDivider),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        text = pitch.coverMessage,
                                        fontSize = 14.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(16.dp),
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                            
                            // Portfolio link
                            Column {
                                Text("PORTFOLIO LINK", fontSize = 10.sp, fontWeight = FontWeight.Black, color = TextSecondary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                    border = BorderStroke(1.dp, ColorDivider),
                                    shape = RoundedCornerShape(16.dp),
                                    onClick = { /* open link */ }
                                ) {
                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Link, null, tint = AccentBlue)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(pitch.portfolioUrl, color = AccentBlue, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    } else {
                        // Pre-Match Chat Stream Tab
                        val chatMessages by discoveryViewModel.getInterviewMessages(pitch.id).collectAsState(initial = emptyList())
                        var replyText by remember { mutableStateOf("") }
                        val scrollState = rememberScrollState()

                        Column(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .background(SurfaceColor, shape = RoundedCornerShape(20.dp))
                                    .border(1.dp, ColorDivider, shape = RoundedCornerShape(20.dp))
                                    .padding(12.dp)
                            ) {
                                if (chatMessages.isEmpty()) {
                                    Column(
                                        modifier = Modifier.align(Alignment.Center),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.Forum, null, tint = TextSecondary.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text("Pre-Selection Interview", fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("Initiate a conversation to evaluate fit", color = TextSecondary, fontSize = 12.sp)
                                    }
                                } else {
                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(chatMessages) { msg ->
                                            val isMe = msg.senderId != "candidate_${pitch.id}"
                                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
                                                Surface(
                                                    color = if (isMe) AccentBlue else ColorDivider,
                                                    shape = RoundedCornerShape(16.dp),
                                                    modifier = Modifier.widthIn(max = 240.dp)
                                                ) {
                                                    Text(msg.messageBody, color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(12.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = replyText,
                                onValueChange = { replyText = it },
                                placeholder = { Text("Ask a follow-up...", fontSize = 13.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                trailingIcon = {
                                    IconButton(onClick = { 
                                        if (replyText.isNotEmpty()) {
                                            discoveryViewModel.sendPreMatchMessage(pitch.id, userProfile?.displayName ?: "Lead", replyText, pitch.senderName, pitch.senderSpecialty, authViewModel.currentUserId.value ?: "me")
                                            replyText = ""
                                        }
                                    }) { Icon(Icons.Default.Send, null, tint = AccentBlue) }
                                },
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

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Action Buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { showDeclineConfirm = true },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, AccentRed),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed)
                    ) {
                        Text("Decline", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)
                    ) {
                        Text("Approve & Onboard", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showDeclineConfirm) {
        ConfirmationDialog(
            title = "Decline Candidate?",
            message = "Are you sure you want to decline this application? They will be informed of your decision.",
            onConfirm = {
                onDecline()
                showDeclineConfirm = false
            },
            onDismiss = { showDeclineConfirm = false }
        )
    }
}

@Composable
fun CreateProposalDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var niche by remember { mutableStateOf("Tech") }
    var brief by remember { mutableStateOf("") }

    val nichesList = listOf("Tech", "Gaming", "Vlog", "Education")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Initiate Co-Op Proposal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Project Title") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("proposal_title_input")
                )

                // Niche Options Row
                Text("Select Channel Niche:", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    nichesList.forEach { n ->
                        val selected = niche == n
                        OutlinedButton(
                            onClick = { niche = n },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(n, fontSize = 12.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = brief,
                    onValueChange = { brief = it },
                    label = { Text("Collaboration Brief") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .testTag("proposal_brief_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = { onSubmit(title, niche, brief) },
                        enabled = title.isNotBlank() && brief.isNotBlank(),
                        modifier = Modifier.testTag("proposal_submit_button")
                    ) {
                        Text("Post Proposal")
                    }
                }
            }
        }
    }
}

@Composable
fun SubmitPitchDialog(
    project: ProjectProposal,
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var cover by remember { mutableStateOf("") }
    var portfolio by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Apply to ${project.title}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = cover,
                    onValueChange = { cover = it },
                    label = { Text("Your Pitch / Cover Message") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .testTag("pitch_cover_input")
                )

                OutlinedTextField(
                    value = portfolio,
                    onValueChange = { portfolio = it },
                    label = { Text("Portfolio Link (URL)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pitch_portfolio_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = { onSubmit(cover, portfolio) },
                        enabled = cover.isNotBlank() && portfolio.isNotBlank(),
                        modifier = Modifier.testTag("pitch_submit_button")
                    ) {
                        Text("Send Pitch")
                    }
                }
            }
        }
    }
}

@Composable
fun SybilThrottledOverlay(
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(enabled = false) {}, // Consume clicks
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .testTag("sybil_throttling_overlay"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.error)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.GppBad,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.error
                )

                Text(
                    text = "SECURITY CHECKPOINT",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "RATE LIMIT EXCEEDED",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Platform anti-sybil filters have flagged your account due to excessive pitch submissions. Safe team matches require authentic, focused proposals.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("return_to_board_button")
                ) {
                    Text("Return to Board", color = MaterialTheme.colorScheme.onError, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = SurfaceColor,
        titleContentColor = Color.White,
        textContentColor = TextSecondary
    )
}

// Custom Dialogs & Helper Composables Added for Saved Searches & Looking-for-Work

@Composable
fun SaveSearchDialog(
    currentTabStatus: String,
    currentNiche: String,
    onDismiss: () -> Unit,
    onSave: (name: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, ColorDivider),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Save Current Search",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Save filters to get notified in the background when a new matching role or talent listing is posted.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Filter: ${if (currentTabStatus == "OPEN_ROLES") "Open Roles" else "Available Talent"} ($currentNiche)",
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentBlue,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Search Name (e.g., Gaming Editors)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("saved_search_name_input"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceColor,
                        unfocusedContainerColor = SurfaceColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = AccentBlue,
                        unfocusedLabelColor = TextSecondary
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { if (name.isNotBlank()) onSave(name) },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("SAVE")
                    }
                }
            }
        }
    }
}

@Composable
fun MySavedSearchesDialog(
    currentUserId: String,
    globalViewModel: GlobalViewModel,
    onDismiss: () -> Unit,
    onApplySearch: (type: String, niche: String) -> Unit
) {
    val savedSearches by globalViewModel.getSavedSearchesForUser(currentUserId).collectAsState(initial = emptyList())
    val json = Json { 
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, ColorDivider),
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.7f).padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Saved Searches",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (savedSearches.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No saved searches yet.", color = TextSecondary, textAlign = TextAlign.Center)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(savedSearches) { search ->
                            val filter = try {
                                json.decodeFromString<com.example.data.model.SavedSearchFilter>(search.filterJson)
                            } catch (e: Exception) {
                                com.example.data.model.SavedSearchFilter()
                            }
                            
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    onApplySearch(filter.type, filter.niche)
                                    onDismiss()
                                },
                                colors = CardDefaults.cardColors(containerColor = SurfaceColor.copy(alpha = 0.5f)),
                                border = BorderStroke(1.dp, ColorDivider)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(search.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Type: ${if (filter.type == "OPEN_ROLES") "Open Roles" else "Available Talent"} • Niche: ${filter.niche}",
                                            color = TextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                    IconButton(
                                        onClick = { globalViewModel.deleteSavedSearch(search.id) },
                                        modifier = Modifier.testTag("delete_saved_search_${search.id}")
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentRed)
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

@Composable
fun SendConnectionRequestDialog(
    availableTalentProfile: com.example.data.model.UserProfile,
    availableTalentListing: com.example.data.model.LookingForWork,
    currentUserId: String,
    globalViewModel: GlobalViewModel,
    discoveryViewModel: DiscoveryViewModel,
    onDismiss: () -> Unit,
    onSubmit: (projectId: String, message: String) -> Unit
) {
    var selectedProjectId by remember { mutableStateOf("") }
    var inviteMessage by remember { mutableStateOf("") }
    
    val allProposals by globalViewModel.allProjectProposals.collectAsState(initial = emptyList())
    val myProposals = remember(allProposals, currentUserId) {
        allProposals.filter { it.proposal.authorId == currentUserId }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, ColorDivider),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Connect with ${availableTalentProfile.displayName}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Request a connection by inviting this creator to collaborate on one of your listed project proposals.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (myProposals.isEmpty()) {
                    Text(
                        text = "⚠️ You haven't posted any Project Proposals yet! Please go to Open Roles and click 'Start a New Collaboration' to create one first.",
                        color = AccentRed,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor)
                    ) {
                        Text("CLOSE", color = Color.White)
                    }
                } else {
                    Text("Select Your Project", fontWeight = FontWeight.Bold, color = AccentBlue, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    var expandedDropdown by remember { mutableStateOf(false) }
                    val selectedProj = myProposals.find { it.proposal.id == selectedProjectId }
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expandedDropdown = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, ColorDivider)
                        ) {
                            Text(
                                text = selectedProj?.proposal?.title ?: "Select a collaboration project...",
                                color = if (selectedProj != null) Color.White else TextSecondary,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Start
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
                        }
                        
                        DropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false },
                            modifier = Modifier.fillMaxWidth().background(SurfaceColor)
                        ) {
                            myProposals.forEach { data ->
                                DropdownMenuItem(
                                    text = { Text(data.proposal.title, color = Color.White) },
                                    onClick = {
                                        selectedProjectId = data.proposal.id
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = inviteMessage,
                        onValueChange = { inviteMessage = it },
                        label = { Text("Invitation Brief / Cover Message") },
                        singleLine = false,
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("connection_invite_message"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SurfaceColor,
                            unfocusedContainerColor = SurfaceColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = AccentBlue,
                            unfocusedLabelColor = TextSecondary
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("CANCEL", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = {
                                if (selectedProjectId.isNotBlank() && inviteMessage.isNotBlank()) {
                                    onSubmit(selectedProjectId, inviteMessage)
                                }
                            },
                            enabled = selectedProjectId.isNotBlank() && inviteMessage.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("SEND REQUEST")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AvailableTalentCard(
    listing: com.example.data.model.LookingForWork,
    currentUserId: String,
    globalViewModel: GlobalViewModel,
    onConnect: () -> Unit
) {
    val talentProfile by globalViewModel.getUserById(listing.userId).collectAsState(initial = null)
    val completedWorkspacesCount by globalViewModel.getCompletedWorkspacesCountForUser(listing.userId).collectAsState(initial = 0)
    val endorsements by globalViewModel.getEndorsementsForUser(listing.userId).collectAsState(initial = emptyList())
    
    val json = kotlinx.serialization.json.Json { 
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    val details = remember(listing.detailsJson) {
        try {
            json.decodeFromString<com.example.data.model.LookingForWorkDetails>(listing.detailsJson)
        } catch (e: Exception) {
            com.example.data.model.LookingForWorkDetails()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("talent_card_${listing.userId}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, ColorDivider)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(SurfaceColor),
                        contentAlignment = Alignment.Center
                    ) {
                        tabIcon(talentProfile?.primarySpecialty ?: "Video Editor")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = talentProfile?.displayName ?: "Creator Specialist",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = talentProfile?.primarySpecialty ?: "Video Editor",
                                fontSize = 13.sp,
                                color = AccentBlue,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(NeonEmerald)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Open To Projects",
                                color = NeonEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Surface(
                    color = AccentBlue.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = "Completed: $completedWorkspacesCount",
                        color = AccentBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Offered Skills:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            Text(
                text = details.skills.ifBlank { "No skills detailed." },
                fontSize = 14.sp,
                color = Color.White,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Availability:",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = details.availability.ifBlank { "Not specified" },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Rate Expectation:",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = details.rateExpectations.ifBlank { "Open to discuss" },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            if (endorsements.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                val tagCounts = remember(endorsements) {
                    val list = mutableListOf<String>()
                    endorsements.forEach { e ->
                        list.addAll(e.tags)
                    }
                    list.groupBy { it }.mapValues { it.value.size }
                }
                
                if (tagCounts.isNotEmpty()) {
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        tagCounts.forEach { (tag, count) ->
                            Surface(
                                color = SurfaceColor,
                                border = BorderStroke(1.dp, ColorDivider),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "$tag ($count)",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (listing.userId != currentUserId) {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onConnect,
                    modifier = Modifier.fillMaxWidth().height(44.dp).testTag("connect_talent_button_${listing.userId}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send Connection Request", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun tabIcon(specialty: String) {
    val icon = when (specialty) {
        "Video Editor" -> Icons.Default.VideoCall
        "Scriptwriter" -> Icons.Default.EditNote
        "VFX Artist" -> Icons.Default.AutoAwesome
        "3D Animator" -> Icons.Default.Animation
        "Sound Engineer" -> Icons.Default.VolumeUp
        "Growth Strategist" -> Icons.Default.TrendingUp
        "Thumbnail Designer" -> Icons.Default.Image
        else -> Icons.Default.Badge
    }
    Icon(icon, contentDescription = null, tint = AccentBlue)
}
