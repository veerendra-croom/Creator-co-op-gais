package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.*
import com.example.ui.components.*
import com.example.ui.feedback.*
import com.example.ui.viewmodels.GlobalViewModel
import com.example.analytics.AnalyticsManager

@Composable
fun PublicProfileScreen(
    userId: String,
    globalViewModel: GlobalViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { (context.applicationContext as com.example.CreatorCoopApp).container.repository }
    var isConnected by remember { mutableStateOf(false) }
    var connectionCount by remember { mutableIntStateOf(142) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var selectedWorkspaceForInvite by remember { mutableStateOf("Co-Op Video Production Shard") }

    val profiles by globalViewModel.allUsers.collectAsState(initial = emptyList())
    val profile = remember(userId, profiles) {
        profiles.find { it.id == userId || (userId == "me" && it.id == "me") } ?: profiles.find { it.id == "me" }
    }

    val actualUserId = profile?.id ?: userId
    val completedWorkspacesCount by remember(actualUserId) {
        globalViewModel.getCompletedWorkspacesCountForUser(actualUserId)
    }.collectAsState(initial = 0)
    
    val endorsements by remember(actualUserId) {
        globalViewModel.getEndorsementsForUser(actualUserId)
    }.collectAsState(initial = emptyList())

    val tagCounts = remember(endorsements) {
        val counts = mutableMapOf<String, Int>()
        endorsements.forEach { endorsement ->
            endorsement.tags.forEach { tag ->
                counts[tag] = (counts[tag] ?: 0) + 1
            }
        }
        counts
    }

    val name = profile?.displayName ?: profile?.email?.substringBefore("@") ?: "Anonymous User"
    val bio = profile?.bio ?: "No bio provided yet."
    val specialty = profile?.primarySpecialty ?: "Creator"
    val reputation = profile?.reputationScore ?: 100
    val reliability = profile?.reliabilityBadge ?: "Standard"
    val level = profile?.verificationLevel ?: "L1 Standard"
    val completedProjects = profile?.completedProjectsCount ?: 0
    val signedAgreements = profile?.signedAgreementsCount ?: 0
    val onTimeRate = profile?.onTimeDeliveryRate ?: 100
    val peerRating = profile?.peerRating ?: 5.0

    val initials = name.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").take(2).uppercase()

    LaunchedEffect(userId) {
        AnalyticsManager.trackEvent("portfolio_viewed", mapOf("profile_id" to userId))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
    ) {
        // Top navigation bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DS.Space12),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(DS.Space8))
            Text(
                text = "Public Portfolio",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = {
                    val usernameToShare = if (!profile?.username.isNullOrBlank()) profile?.username else actualUserId
                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_SUBJECT, "Check out $name on Creator Co-Op")
                        putExtra(android.content.Intent.EXTRA_TEXT, "View $name's public portfolio on Creator Co-Op: https://creatorcoop.app/u/$usernameToShare")
                    }
                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Profile"))
                },
                modifier = Modifier.testTag("share_profile_button")
            ) {
                Icon(Icons.Default.Share, contentDescription = "Share Profile", tint = Color.White)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = DS.Space32)
        ) {
            // --- LAYER 1: Creator Identity (Banner, Avatar, Info, Verified Indicators) ---
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    // Profile Banner Gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(AccentRed.copy(alpha = 0.8f), AccentBlue.copy(alpha = 0.6f))
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.15f))
                    )
                }

                // Avatar and Quick Actions row overlapping banner
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DS.Space16)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-40).dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Avatar with verified badge
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceColor)
                                    .border(4.dp, PrimaryBackground, CircleShape)
                                    .border(6.dp, AccentRed.copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials.ifEmpty { "ME" },
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AccentRed
                                )
                            }
                            // Verified indicator badge
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(NeonEmerald)
                                    .border(2.dp, PrimaryBackground, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Verified Creator",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Quick Actions
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(DS.Space8),
                            modifier = Modifier.padding(bottom = DS.Space4)
                        ) {
                            Button(
                                onClick = {
                                    isConnected = !isConnected
                                    if (isConnected) {
                                        connectionCount++
                                        AnalyticsManager.trackEvent("connection_request_sent", mapOf("target_id" to userId))
                                        FeedbackManager.showSuccess("Connected with $name!")
                                    } else {
                                        connectionCount--
                                        FeedbackManager.showInfo("Disconnected.")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isConnected) SurfaceColor else AccentRed
                                ),
                                border = if (isConnected) BorderStroke(1.dp, ColorDivider) else null,
                                shape = DS.RadiusMedium,
                                contentPadding = PaddingValues(horizontal = DS.Space12, vertical = DS.Space8)
                            ) {
                                Icon(
                                    imageVector = if (isConnected) Icons.Default.Done else Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(DS.Space6))
                                Text(
                                    text = if (isConnected) "Connected" else "Connect",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = { showInviteDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                                border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.5f)),
                                shape = DS.RadiusMedium,
                                contentPadding = PaddingValues(horizontal = DS.Space12, vertical = DS.Space8)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MailOutline,
                                    contentDescription = "Invite to Workspace",
                                    tint = AccentBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(DS.Space6))
                                Text(
                                    text = "Invite",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Creator info & tagline
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-30).dp)
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(DS.Space2))
                        Text(
                            text = bio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AccentBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Specialty: $specialty",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Verification Indicators & Badges
                        @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                        androidx.compose.foundation.layout.FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(DS.Space8),
                            verticalArrangement = Arrangement.spacedBy(DS.Space8),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            UserRoleBadge(role = profile?.globalRole ?: "CREATOR", isPro = profile?.isVerifiedPro == true)

                            SubscriptionTierBadge(isPro = profile?.isVerifiedPro == true)

                            ReliabilityBadge(badge = reliability)

                            Surface(
                                color = AccentBlue.copy(alpha = 0.15f),
                                contentColor = AccentBlue,
                                shape = DS.RadiusSmall,
                                border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = DS.Space8, vertical = DS.Space4),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.WorkspacePremium, null, modifier = Modifier.size(12.dp), tint = AccentBlue)
                                    Spacer(modifier = Modifier.width(DS.Space4))
                                    Text("Workspaces Completed: $completedWorkspacesCount", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }

                            val isAvailable = profile?.availabilityStatus != "NOT_AVAILABLE"
                            Surface(
                                color = if (isAvailable) NeonEmerald.copy(alpha = 0.15f) else AccentRed.copy(alpha = 0.15f),
                                contentColor = if (isAvailable) NeonEmerald else AccentRed,
                                shape = DS.RadiusSmall,
                                border = BorderStroke(1.dp, if (isAvailable) NeonEmerald.copy(alpha = 0.3f) else AccentRed.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = DS.Space8, vertical = DS.Space4),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (isAvailable) NeonEmerald else AccentRed)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isAvailable) "OPEN" else "NOT AVAILABLE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- LAYER 2: Trust Metrics ---
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DS.Space16)
                        .offset(y = (-10).dp)
                ) {
                    SectionHeader(title = "Verified Reputation Metrics")
                    Spacer(modifier = Modifier.height(DS.Space8))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DS.Space8)
                    ) {
                        KPICard(
                            title = "Trust Score",
                            value = "$reputation Score",
                            icon = Icons.Default.Shield,
                            tint = NeonEmerald,
                            modifier = Modifier.weight(1f)
                        )
                        KPICard(
                            title = "On-Time Rate",
                            value = "$onTimeRate%",
                            icon = Icons.Default.Schedule,
                            tint = AccentBlue,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(DS.Space8))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DS.Space8)
                    ) {
                        KPICard(
                            title = "Workspaces Completed",
                            value = "$completedWorkspacesCount Completed",
                            icon = Icons.Default.TaskAlt,
                            tint = AccentRed,
                            modifier = Modifier.weight(1f)
                        )
                        KPICard(
                            title = "Peer Rating",
                            value = "$peerRating ★",
                            icon = Icons.Default.Star,
                            tint = CrispAmber,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Shareable URL Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DS.Space16, vertical = DS.Space8),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    shape = DS.RadiusLarge,
                    border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(DS.Space16)) {
                        Text("SHAREABLE VERIFIED PORTFOLIO LINK", style = MaterialTheme.typography.labelMedium, color = AccentBlue, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(DS.Space8))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val profileUrl = "https://creatorcoop.com/profile/${profile?.id ?: "me"}"
                            Text(
                                text = profileUrl,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Creator Portfolio Link", profileUrl)
                                    clipboard.setPrimaryClip(clip)
                                    AnalyticsManager.trackPortfolioShared()
                                    FeedbackManager.showSuccess("Portfolio URL copied!")
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, "Copy Portfolio Link", tint = AccentBlue)
                            }
                        }
                    }
                }
            }

            // --- LAYER 3: Skills Matrix ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DS.Space16, vertical = DS.Space8),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    shape = DS.RadiusLarge,
                    border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(DS.Space16)) {
                        SectionHeader(title = "Expertise Skills Matrix")
                        Spacer(modifier = Modifier.height(DS.Space8))

                        Text("PRIMARY FOCUS", style = MaterialTheme.typography.labelMedium, color = AccentRed, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(DS.Space6))
                        SkillItem(name = specialty, level = "Expert", percentage = 0.95f, color = AccentRed)
                        SkillItem(name = "Short-Form Pacing Flow", level = "Expert", percentage = 0.90f, color = AccentRed)
                        SkillItem(name = "Thumbnail Narrative Hook", level = "Expert", percentage = 0.88f, color = AccentRed)

                        Spacer(modifier = Modifier.height(DS.Space12))
                        Text("SECONDARY SPECIALTIES", style = MaterialTheme.typography.labelMedium, color = CrispAmber, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(DS.Space6))
                        SkillItem(name = "Retention Mechanics", level = "Advanced", percentage = 0.82f, color = AccentBlue)
                        SkillItem(name = "Cinematography Blueprints", level = "Advanced", percentage = 0.78f, color = AccentBlue)
                    }
                }
            }

            // --- Teammate Endorsements Section ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DS.Space16, vertical = DS.Space8),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    shape = DS.RadiusLarge,
                    border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(DS.Space16)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ThumbUp, null, tint = AccentBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(DS.Space8))
                            Text(
                                text = "Teammate Endorsements",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(modifier = Modifier.height(DS.Space12))
                        
                        if (endorsements.isEmpty()) {
                            Text(
                                text = "No endorsements logged yet. Complete workspaces with other co-op creators to receive endorsements.",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                            androidx.compose.foundation.layout.FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val allPossibleTags = listOf("Reliable", "Fast Turnaround", "Great Communicator", "Creative", "Organized")
                                allPossibleTags.forEach { tag ->
                                    val count = tagCounts[tag] ?: 0
                                    if (count > 0) {
                                        Surface(
                                            color = AccentBlue.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(24.dp),
                                            border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.3f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = DS.Space12, vertical = DS.Space6),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DoneAll,
                                                    contentDescription = null,
                                                    tint = AccentBlue,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "$tag ($count)",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
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

            // --- LAYER 4: Featured Projects Showcase ---
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DS.Space16, vertical = DS.Space8)
                ) {
                    SectionHeader(title = "Verified Contribution History")
                    Spacer(modifier = Modifier.height(DS.Space8))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DS.Space12)
                    ) {
                        PortfolioPreviewCard(
                            title = "High Retention Shorts Blueprint",
                            views = "2.4M Views",
                            tint = AccentRed,
                            modifier = Modifier.weight(1f)
                        )
                        PortfolioPreviewCard(
                            title = "Tech Narratives Production",
                            views = "1.8M Views",
                            tint = AccentBlue,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(DS.Space12))
                    PortfolioCaseStudyItem(
                        title = "Enterprise Masterclass Suite",
                        desc = "Designed, scheduled, and compiled entirely within verified Creator Co-Op workspaces with cryptographically bound deliverables.",
                        metrics = "98% Client Satisfaction • On-Time Delivery"
                    )
                }
            }

            // --- LAYER 5: Verified Collaboration Network (P1) ---
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DS.Space16, vertical = DS.Space8)
                ) {
                    SectionHeader(title = "Verified Network Nodes")
                    Spacer(modifier = Modifier.height(DS.Space4))
                    Text(
                        text = "Creators and editors anchored in this user's trusted ecosystem.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = DS.Space12)
                    )

                    val allUsersList by remember {
                        repository.getAllUsersFlow()
                    }.collectAsState(initial = emptyList())
                    val networkNodes = remember(allUsersList, actualUserId) {
                        allUsersList.filter { it.id != actualUserId && it.displayName.isNotEmpty() }
                    }

                    if (networkNodes.isEmpty()) {
                        Text(
                            text = "No other network nodes connected in the system.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = DS.Space8)
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            networkNodes.forEach { node ->
                                val netName = node.displayName
                                val netInitials = netName.split(" ").map { it.take(1) }.joinToString("")
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(SurfaceColor)
                                            .border(1.dp, ColorDivider, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = netInitials,
                                            color = AccentBlue,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 16.sp
                                        )
                                        if (node.isVerifiedPro) {
                                            // Mini Verified Badge
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(CircleShape)
                                                    .background(NeonEmerald)
                                                    .border(1.dp, SurfaceColor, CircleShape)
                                                    .align(Alignment.BottomEnd),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(10.dp))
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(netName, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showInviteDialog) {
        AlertDialog(
            onDismissRequest = { showInviteDialog = false },
            containerColor = SurfaceColor,
            title = {
                Text(
                    text = "Invite $name to Workspace",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(DS.Space12)) {
                    Text(
                        text = "$name will receive a secure collaboration request, role expectations, and standard team agreements.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "SELECT TARGET WORKSPACE SHARD",
                        color = AccentBlue,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    val realWorkspaces by remember {
                        repository.allWorkspaces
                    }.collectAsState(initial = emptyList())

                    if (realWorkspaces.isEmpty()) {
                        Text(
                            text = "No workspaces available. Please create a workspace first.",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(DS.Space12)
                        )
                    } else {
                        realWorkspaces.forEach { ws ->
                            val wsName = ws.name
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(DS.RadiusMedium)
                                    .background(if (selectedWorkspaceForInvite == wsName) AccentBlue.copy(alpha = 0.15f) else Color.Transparent)
                                    .clickable { selectedWorkspaceForInvite = wsName }
                                    .padding(DS.Space12),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedWorkspaceForInvite == wsName,
                                    onClick = { selectedWorkspaceForInvite = wsName },
                                    colors = RadioButtonDefaults.colors(selectedColor = AccentBlue)
                                )
                                Spacer(modifier = Modifier.width(DS.Space8))
                                Text(
                                    text = wsName,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showInviteDialog = false
                        // Send active in-app notification to the target user with deepLinkTarget!
                        globalViewModel.sendNotification(
                            userId = if (userId == "me") "DemoUser" else userId,
                            title = "Workspace Invite",
                            body = "You have been invited to join '$selectedWorkspaceForInvite' by ${profile?.displayName ?: "Creator"}",
                            type = "INVITES",
                            deepLinkTarget = "WORKSPACE_INVITE:ws_youtube_main"
                        )
                        AnalyticsManager.trackEvent("member_invited", mapOf("workspace_id" to "ws_youtube_main", "role" to "Collaborator"))
                        FeedbackManager.showSuccess("Secure Workspace Invitation Sent!")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("Send Secure Invitation", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showInviteDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun SkillItem(name: String, level: String, percentage: Float, color: Color) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(level, color = TextSecondary, fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(ColorDivider.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage)
                    .fillMaxHeight()
                    .background(color)
            )
        }
    }
}

@Composable
fun PortfolioPreviewCard(title: String, views: String, tint: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceLightColor),
        shape = DS.RadiusMedium,
        border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(DS.Space12)) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayCircle, null, tint = tint, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(DS.Space12))
            Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(DS.Space6))
            Text(views, color = NeonEmerald, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun PortfolioCaseStudyItem(title: String, desc: String, metrics: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = DS.RadiusMedium,
        border = BorderStroke(1.dp, ColorDivider)
    ) {
        Column(modifier = Modifier.padding(DS.Space16)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WorkspacePremium, null, tint = CrispAmber, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(DS.Space8))
                Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(modifier = Modifier.height(DS.Space8))
            Text(desc, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
            Spacer(modifier = Modifier.height(DS.Space8))
            Divider(color = ColorDivider.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(metrics, color = NeonEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}
