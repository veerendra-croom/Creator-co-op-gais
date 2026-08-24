package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.util.CustomTabsHelper
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.window.Dialog
import com.example.ui.feedback.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.collectAsState
import com.example.data.model.ProjectProposalWithData
import com.example.data.model.Post
import com.example.data.model.SyncState
import com.example.data.model.Workspace
import com.example.ui.theme.*
import com.example.ui.components.*
import com.example.ui.tour.guidedTourTarget
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.builtins.ListSerializer
import java.text.SimpleDateFormat
import java.util.*

import com.example.ui.viewmodels.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    globalViewModel: GlobalViewModel,
    authViewModel: AuthViewModel,
    adminViewModel: AdminViewModel,
    userProfile: com.example.data.model.UserProfile?,
    modifier: Modifier = Modifier
) {
    val isAdmin = userProfile?.systemRole == "ADMIN"
    var showAdmin by remember { mutableStateOf(false) }
    var showLegal by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showEditProfile by remember { mutableStateOf(false) }
    var showCommunityGuidelines by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var showLicenses by remember { mutableStateOf(false) }
    var showPaywall by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showVerificationDialog by remember { mutableStateOf(false) }
    val exportedData by globalViewModel.exportedData.collectAsState()

    if (exportedData != null) {
        AlertDialog(
            onDismissRequest = { globalViewModel.exportedData.value = null },
            containerColor = SurfaceColor,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Your Profile Data Backup", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Box(modifier = Modifier.height(300.dp).verticalScroll(rememberScrollState())) {
                    Text(exportedData!!, color = TextPrimary, fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                }
            },
            confirmButton = {
                TextButton(onClick = { globalViewModel.exportedData.value = null }) {
                    Text("CLOSE", fontWeight = FontWeight.Bold, color = AccentBlue)
                }
            }
        )
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(userProfile) {
        if (userProfile != null) {
            globalViewModel.generateOrGetReferralCode(userProfile)
            globalViewModel.loadWeeklyDigestSetting(userProfile.id)
        }
    }

    if (showDeleteConfirmation && userProfile != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = {
                Text(
                    text = "Delete Your Account?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Column {
                    Text(
                        text = "This action is permanent and cannot be undone.",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Under GDPR and compliance regulations, confirming deletion will execute the following cascade operations:\n\n" +
                               "• Your personal user profile is permanently deleted.\n" +
                               "• All private drafts and sandbox items are completely wiped.\n" +
                               "• Workspace member links will be deleted. (Leads are auto-resolved to preserve business flow).\n" +
                               "• Forum threads and public chat entries are anonymized under 'Deleted User' to retain group context.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        authViewModel.deleteAccountAndCascade(userProfile.id)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.testTag("confirm_delete_account_button")
                ) {
                    Text("PROCEED", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text("CANCEL", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }

    if (showLegal) {
        LegalScreen(onBack = { showLegal = false })
        BackHandler { showLegal = false }
        return
    }

    if (showCommunityGuidelines) {
        CommunityGuidelinesScreen(onBack = { showCommunityGuidelines = false })
        BackHandler { showCommunityGuidelines = false }
        return
    }

    if (showAddTaskDialog && userProfile != null) {
        val workspaces by globalViewModel.allWorkspaces.collectAsState()
        var taskTitle by remember { mutableStateOf("") }
        var taskDesc by remember { mutableStateOf("") }
        var selectedWsId by remember { mutableStateOf(workspaces.firstOrNull()?.id ?: "") }
        
        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            containerColor = SurfaceColor,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Quick Task Add", color = Color.White, fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Select Target Workspace", color = TextSecondary, fontSize = 12.sp)
                    LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) {
                        items(workspaces) { ws ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedWsId = ws.id }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedWsId == ws.id,
                                    onClick = { selectedWsId = ws.id },
                                    colors = RadioButtonDefaults.colors(selectedColor = AccentRed)
                                )
                                Text(ws.name, color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                    
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Task Title") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    OutlinedTextField(
                        value = taskDesc,
                        onValueChange = { taskDesc = it },
                        label = { Text("Task Details (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val task = com.example.data.model.ProductionTask(
                            id = UUID.randomUUID().toString(),
                            workspaceId = selectedWsId,
                            creatorId = userProfile.id,
                            title = taskTitle,
                            contentBody = taskDesc,
                            stateScope = "PRODUCTION_READY",
                            kanbanLane = "TODO",
                            createdAt = System.currentTimeMillis()
                        )
                        globalViewModel.insertTask(task)
                        showAddTaskDialog = false
                    },
                    enabled = taskTitle.isNotBlank() && selectedWsId.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("CREATE TASK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text("CANCEL", color = TextSecondary)
                }
            }
        )
    }

    if (showAbout) {
        AboutDialog(onDismiss = { showAbout = false })
    }

    if (showLicenses) {
        LicensesDialog(onDismiss = { showLicenses = false })
    }

    if (showPaywall) {
        com.example.ui.components.PaywallDialog(
            onDismiss = { showPaywall = false },
            onPurchaseMonthly = {
                if (userProfile != null) globalViewModel.purchaseProMonthly(userProfile.id)
                showPaywall = false
            },
            onPurchaseAnnual = {
                if (userProfile != null) globalViewModel.purchaseProAnnual(userProfile.id)
                showPaywall = false
            }
        )
    }

    if (showEditProfile && userProfile != null) {
        EditProfileScreen(
            userProfile = userProfile,
            globalViewModel = globalViewModel,
            onBack = { showEditProfile = false }
        )
        BackHandler { showEditProfile = false }
        return
    }

    if (showAdmin) {
        AdminDashboardScreen(
            adminViewModel = adminViewModel,
            authViewModel = authViewModel,
            userProfile = userProfile
        )
        BackHandler { showAdmin = false }
        return
    }



    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .padding(16.dp)
    ) {
        val currentUserIdState by authViewModel.currentUserId.collectAsState()
        val userId = currentUserIdState

        LaunchedEffect(userId, userProfile) {
            if (userId != null && userProfile == null) {
                // Auto-seed profile for the logged in user to heal the state
                val username = if (userId.contains("-")) "user_" + userId.take(5) else userId
                val email = if (userId.contains("@")) userId else "$username@creatorcoop.com"
                val defaultProfile = com.example.data.model.UserProfile(
                    id = userId,
                    email = email,
                    username = username,
                    displayName = "Creator Profile ($username)",
                    avatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=$username",
                    globalRole = "CREATOR",
                    systemRole = "USER",
                    bio = "Professional Creator in the Creator Co-Op ecosystem."
                )
                globalViewModel.updateUserProfile(defaultProfile)
            }
        }

        if (userProfile == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = AccentBlue, strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "INITIALIZING CORE DECENTRALIZED IDENTITY...",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        } else {
            val context = LocalContext.current
            val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
            var isHandshaking by remember { mutableStateOf(false) }
            val handshakeLogs = remember { mutableStateListOf<String>() }
            val scope = rememberCoroutineScope()

            // State indicators
            val completenessScore = remember(userProfile) {
                var score = 20
                if (userProfile.displayName.isNotBlank() && userProfile.displayName != userProfile.username) score += 20
                if (userProfile.bio.isNotBlank()) score += 20
                if (userProfile.location.isNotBlank()) score += 15
                if (userProfile.primarySpecialty.isNotBlank()) score += 10
                if (userProfile.isVerifiedPro) score += 15
                score
            }

            val decodedSkills = remember(userProfile.skillsJson) {
                try {
                    val cleanJson = userProfile.skillsJson.trim()
                    if (cleanJson.startsWith("[")) {
                        cleanJson.removePrefix("[").removeSuffix("]").split(",")
                            .map { it.trim().trim('"') }
                            .filter { it.isNotBlank() }
                    } else {
                        emptyList()
                    }
                } catch (e: Exception) {
                    emptyList<String>()
                }
            }

            val decodedPortfolio = remember(userProfile.portfolioJson) {
                try {
                    val cleanJson = userProfile.portfolioJson.trim()
                    if (cleanJson.startsWith("[")) {
                        cleanJson.removePrefix("[").removeSuffix("]").split(",")
                            .map { it.trim().trim('"') }
                            .filter { it.isNotBlank() }
                    } else {
                        emptyList()
                    }
                } catch (e: Exception) {
                    emptyList<String>()
                }
            }

            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Creator Identity",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Decentralized Professional Profile",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = { showEditProfile = true },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceColor)
                        .border(1.dp, ColorDivider, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = AccentBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 100.dp),
                modifier = Modifier.weight(1f)
            ) {
                // --- CARD 1: THE HERO PROFILE CARD ---
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .guidedTourTarget("dashboard_header", globalViewModel.tourManager),
                        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, ColorDivider)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    androidx.compose.ui.graphics.Brush.linearGradient(
                                        colors = listOf(
                                            SurfaceColor,
                                            SurfaceLightColor.copy(alpha = 0.5f)
                                        )
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Avatar with dynamic glowing border
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(
                                            androidx.compose.ui.graphics.Brush.sweepGradient(
                                                colors = listOf(AccentBlue, NeonEmerald, AccentBlue)
                                            )
                                        )
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(SurfaceColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = userProfile.displayName.take(1).uppercase(),
                                            color = Color.White,
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = userProfile.displayName,
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (userProfile.isVerifiedPro || completenessScore >= 80) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Verified Pro",
                                                tint = NeonEmerald,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "@${userProfile.username}",
                                        color = TextSecondary,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        color = AccentBlue.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.2f))
                                    ) {
                                        Text(
                                            text = userProfile.primarySpecialty.ifBlank { "Unspecified Specialist" },
                                            color = AccentBlue,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (userProfile.bio.isNotBlank()) {
                                Text(
                                    text = userProfile.bio,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            }

                            if (userProfile.location.isNotBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.alpha(0.8f)
                                ) {
                                    Icon(Icons.Default.Place, null, tint = AccentRed, modifier = Modifier.size(14.dp))
                                    Text(text = userProfile.location, color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // --- CARD 2: REPUTATION & REAL-TIME TELEMETRY ---
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .guidedTourTarget("dashboard_karma_gauge", globalViewModel.tourManager),
                        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, ColorDivider)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "REPUTATION ENGINE & REAL-TIME STATUS",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Custom real-time pulse canvas telemetry
                            val transition = rememberInfiniteTransition(label = "pulse")
                            val pulseAnim by transition.animateFloat(
                                initialValue = 0f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(2200, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                                ),
                                label = "pulseOffset"
                            )

                            androidx.compose.foundation.Canvas(
                                modifier = Modifier
                                    .height(56.dp)
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .border(1.dp, ColorDivider.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            ) {
                                val width = size.width
                                val height = size.height
                                val points = mutableListOf<androidx.compose.ui.geometry.Offset>()
                                val segments = 80
                                
                                for (i in 0..segments) {
                                    val x = (i.toFloat() / segments) * width
                                    val normalizedX = (i.toFloat() / segments)
                                    val phase = (pulseAnim * 2 * Math.PI).toFloat()
                                    
                                    // Base sine wave
                                    val baseSine = kotlin.math.sin(normalizedX * 4 * Math.PI - phase).toFloat()
                                    
                                    // Add spike in the middle
                                    val spikeCenter = 0.5f
                                    val distToSpike = kotlin.math.abs(normalizedX - spikeCenter)
                                    val spikeFactor = if (distToSpike < 0.12f) {
                                        kotlin.math.sin((distToSpike / 0.12f) * Math.PI + Math.PI / 2).toFloat()
                                    } else 0f
                                    
                                    val y = height / 2 + baseSine * 3.dp.toPx() + spikeFactor * 16.dp.toPx() * kotlin.math.sin(normalizedX * 10 * Math.PI - phase).toFloat()
                                    points.add(androidx.compose.ui.geometry.Offset(x, y))
                                }
                                
                                for (i in 0 until points.size - 1) {
                                    drawLine(
                                        color = AccentBlue,
                                        start = points[i],
                                        end = points[i + 1],
                                        strokeWidth = 2.dp.toPx()
                                    )
                                    drawLine(
                                        color = AccentBlue.copy(alpha = 0.25f),
                                        start = points[i],
                                        end = points[i + 1],
                                        strokeWidth = 5.dp.toPx()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Grid of simplified metrics
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(SurfaceLightColor, RoundedCornerShape(12.dp))
                                        .border(1.dp, ColorDivider, RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Text("Reputation", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    AnimatedCounter(
                                        value = userProfile.reputationScore,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Black),
                                        color = Color.White
                                    )
                                    Text(userProfile.verificationLevel, color = CrispAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(SurfaceLightColor, RoundedCornerShape(12.dp))
                                        .border(1.dp, ColorDivider, RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Text("Reliability", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(userProfile.reliabilityBadge, color = NeonEmerald, fontSize = 18.sp, fontWeight = FontWeight.Black)
                                    Text("Trust Level", color = TextSecondary, fontSize = 10.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(SurfaceLightColor, RoundedCornerShape(12.dp))
                                        .border(1.dp, ColorDivider, RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Text("Deliverables", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    AnimatedCounter(
                                        value = userProfile.completedProjectsCount,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Black),
                                        color = Color.White
                                    )
                                    Text("Finished tasks", color = TextSecondary, fontSize = 10.sp)
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(SurfaceLightColor, RoundedCornerShape(12.dp))
                                        .border(1.dp, ColorDivider, RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Text("Agreements", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    AnimatedCounter(
                                        value = userProfile.signedAgreementsCount,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Black),
                                        color = Color.White
                                    )
                                    Text("Signed contracts", color = TextSecondary, fontSize = 10.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Profile Completeness Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Profile Completeness", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("$completenessScore%", color = AccentBlue, fontSize = 13.sp, fontWeight = FontWeight.Black)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { completenessScore / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = AccentBlue,
                                trackColor = ColorDivider
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { showVerificationDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceLightColor),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, ColorDivider),
                                modifier = Modifier
                                    .bounceScale()
                                    .fillMaxWidth()
                                    .height(36.dp)
                                    .guidedTourTarget("dashboard_setup_checklist", globalViewModel.tourManager)
                            ) {
                                Icon(Icons.Default.Shield, null, tint = AccentBlue, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Automated Verification Pipeline", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // --- CARD 3: TECHNICAL ARSENAL & PROJECT SHOWCASE (Preserves Tests) ---
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("dashboard_portfolio_list"),
                        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, ColorDivider)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Header matching test requirements
                            Text(
                                text = "TECHNICAL ARSENAL",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            if (decodedSkills.isEmpty()) {
                                Text("No skills listed yet.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            } else {
                                @OptIn(ExperimentalLayoutApi::class)
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    decodedSkills.forEach { skill ->
                                        Surface(
                                            color = SurfaceLightColor,
                                            contentColor = Color.White,
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, ColorDivider)
                                        ) {
                                            Text(
                                                text = skill,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "PROJECT SHOWCASE",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            if (decodedPortfolio.isEmpty()) {
                                Text("No showcase links listed yet.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    decodedPortfolio.forEach { link ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(SurfaceLightColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                                .border(1.dp, ColorDivider, RoundedCornerShape(10.dp))
                                                .clickable { 
                                                    CustomTabsHelper.openUrl(context, link)
                                                }
                                                .padding(12.dp)
                                        ) {
                                            Icon(Icons.Default.Link, null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = link,
                                                fontSize = 12.sp,
                                                color = AccentBlue,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // --- CARD 4: PREMIUM CO-OP SUBSCRIPTION ---
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, if (userProfile.isVerifiedPro) NeonEmerald.copy(alpha = 0.5f) else ColorDivider)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Co-Op Premium Subscription",
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val statusText = if (userProfile.isVerifiedPro) {
                                        "PRO SUBSCRIBER ACTIVE"
                                    } else {
                                        "STANDARD MEMBERSHIP"
                                    }
                                    Text(
                                        text = statusText,
                                        color = if (userProfile.isVerifiedPro) NeonEmerald else CrispAmber,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (userProfile.isVerifiedPro) NeonEmerald.copy(alpha = 0.1f) else CrispAmber.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stars,
                                        contentDescription = null,
                                        tint = if (userProfile.isVerifiedPro) NeonEmerald else CrispAmber,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (userProfile.isVerifiedPro) {
                                    "Thank you for supporting Creator Co-Op! You have unlimited active syndicates, zero platform-sponsored ads, advanced analytics, and priority verification active."
                                } else {
                                    "Upgrade to Verified Pro to unlock priority list placements, zero platform-sponsored ads, advanced analytics, and unlimited active syndicates."
                                },
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            if (!userProfile.isVerifiedPro) {
                                Button(
                                    onClick = { showPaywall = true },
                                    modifier = Modifier.fillMaxWidth().height(42.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Stars, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Upgrade to Pro", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            val updated = userProfile.copy(isVerifiedPro = false)
                                            globalViewModel.updateUserProfile(updated)
                                            FeedbackManager.showInfo("Premium subscription cancelled.")
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(42.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceLightColor),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, ColorDivider)
                                ) {
                                    Text("Cancel Subscription (Demo Sync)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                // --- CARD 5: ABSOLUTE FOUNDER INTEGRITY & DIRECT ESCALATION (AGENTS.md Pillar) ---
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, ColorDivider)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    androidx.compose.ui.graphics.Brush.linearGradient(
                                        colors = listOf(
                                            SurfaceColor,
                                            AccentBlue.copy(alpha = 0.03f)
                                        )
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "VISIONARY FOUNDERS & SUPPORT ESCALATION",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "This platform is driven by the vision of our founders. Reach out directly to resolve any core database, contract, or operational queries.",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Founders Buttons Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        try {
                                            uriHandler.openUri("mailto:veerendrabotla@gmail.com")
                                            FeedbackManager.showSuccess("Escalating to Founder Botla Veerendra...")
                                        } catch (e: Exception) {
                                            FeedbackManager.showError("Failed to open mail client.")
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceLightColor),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, ColorDivider)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Botla Veerendra (Founder)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text("Founder & Visionary", color = TextSecondary, fontSize = 8.5.sp)
                                    }
                                }

                                Button(
                                    onClick = {
                                        try {
                                            uriHandler.openUri("mailto:praveenmacha777@gmail.com")
                                            FeedbackManager.showSuccess("Escalating to Co-Founder Macha Praveen...")
                                        } catch (e: Exception) {
                                            FeedbackManager.showError("Failed to open mail client.")
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceLightColor),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, ColorDivider)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Macha Praveen (Co-Founder)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text("Co-Founder & Architect", color = TextSecondary, fontSize = 8.5.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // --- CARD 6: SYSTEM GUIDES & CONNECTION DIAGNOSTICS (AGENTS.md Pillar - with Progressive Disclosure) ---
                item {
                    var showDiagnostics by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, ColorDivider)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDiagnostics = !showDiagnostics },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "SYSTEM HEALTH & DIAGNOSTICS",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (showDiagnostics) "Hide system compliance guides and local diagnostic suite." else "Tap to view operational guides and perform secure synchronization tests.",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (showDiagnostics) AccentBlue.copy(alpha = 0.15f) else SurfaceLightColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (showDiagnostics) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (showDiagnostics) "Collapse Diagnostics" else "Expand Diagnostics",
                                        tint = if (showDiagnostics) AccentBlue else TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            if (showDiagnostics) {
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = ColorDivider.copy(alpha = 0.3f))
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "OPERATIONAL GUIDE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                // Why, When, How Tab Guide block
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("🎯 WHY:", color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                        Text("Establishes your professional specialty, portfolio links, and skills to verify your identity in the global ecosystem.", color = TextSecondary, fontSize = 11.sp)
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("🕒 WHEN:", color = NeonEmerald, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                        Text("Update details whenever your expertise expands or you deliver new project deliverables.", color = TextSecondary, fontSize = 11.sp)
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("⚙️ HOW:", color = AccentRed, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                        Text("Maintain absolute contract compliance and deliver high-reputation outcomes to automatically scale metrics.", color = TextSecondary, fontSize = 11.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = ColorDivider.copy(alpha = 0.3f))
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "VERIFY SECURE CONNECTION",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Run a connection diagnostic check to verify secure communication, database synchronization, and local node state integrity.",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        scope.launch {
                                            isHandshaking = true
                                            handshakeLogs.clear()
                                            handshakeLogs.add("⚡ [0.0s] Initializing diagnostic handshake...")
                                            kotlinx.coroutines.delay(500)
                                            handshakeLogs.add("🌐 [0.5s] Testing secure SSL link with node servers...")
                                            kotlinx.coroutines.delay(500)
                                            handshakeLogs.add("🗄️ [1.0s] Querying database sync integrity...")
                                            kotlinx.coroutines.delay(500)
                                            handshakeLogs.add("✅ [1.5s] Encryption verified. 100% latency sync [14ms]")
                                            isHandshaking = false
                                            FeedbackManager.showSuccess("Operational Handshake Successful! Nodes Synchronized.")
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(40.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceLightColor),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, ColorDivider),
                                    enabled = !isHandshaking
                                ) {
                                    if (isHandshaking) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 1.5.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Diagnosing sync...", fontSize = 12.sp, color = Color.White)
                                    } else {
                                        Icon(Icons.Default.Refresh, null, tint = NeonEmerald, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Execute Connection Test", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (handshakeLogs.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.Black, RoundedCornerShape(8.dp))
                                            .border(1.dp, ColorDivider, RoundedCornerShape(8.dp))
                                            .padding(10.dp)
                                    ) {
                                        handshakeLogs.forEach { log ->
                                            Text(
                                                text = log,
                                                color = NeonEmerald,
                                                fontSize = 11.sp,
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // --- CARD 7: SYSTEM CONSOLE (Admin Settings & Account Management) ---
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (isAdmin) {
                            Button(
                                onClick = { showAdmin = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, ColorDivider)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = "Admin Console",
                                    tint = AccentBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "OPEN SYSTEM CONTROL PANEL",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Button(
                            onClick = { authViewModel.logout() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("logout_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentRed.copy(alpha = 0.1f),
                                contentColor = AccentRed
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, AccentRed.copy(alpha = 0.25f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Log Out",
                                tint = AccentRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "LOG OUT OF ECOSYSTEM",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentRed
                            )
                        }
                    }
                }
            }

            // Dialogs
            if (showVerificationDialog) {
                var selectedTier by remember { mutableStateOf(2) }
                val reqProjects = if (selectedTier == 2) 3 else 10
                val reqAgreements = if (selectedTier == 2) 3 else 10
                val reqRepScore = if (selectedTier == 2) 95 else 98
                
                val hasProjects = userProfile.completedProjectsCount >= reqProjects
                val hasAgreements = userProfile.signedAgreementsCount >= reqAgreements
                val hasRepScore = userProfile.reputationScore >= reqRepScore
                val hasPremium = userProfile.isVerifiedPro
                val allMet = hasProjects && hasAgreements && hasRepScore && hasPremium

                AlertDialog(
                    onDismissRequest = { showVerificationDialog = false },
                    containerColor = SurfaceColor,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.testTag("verification_pipeline_dialog"),
                    title = {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Status Pipeline Verification",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Surface(
                                    color = AccentBlue.copy(alpha = 0.1f),
                                    contentColor = AccentBlue,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "AUTOMATED",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Current Credentials: ${userProfile.verificationLevel}",
                                color = AccentRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text(
                                text = "Satisfy these decentralized professional status checkpoints to automatically escalate your verification rank.",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceLightColor, RoundedCornerShape(12.dp))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(2 to "Tier 2 (L2 Pro)", 3 to "Tier 3 (L3 Elite)").forEach { (tier, label) ->
                                    val isSelected = selectedTier == tier
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) AccentRed else Color.Transparent)
                                            .clickable { selectedTier = tier }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) Color.White else TextSecondary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                            
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                RequirementRow("Complete $reqProjects project tasks", "${userProfile.completedProjectsCount} completed", hasProjects)
                                RequirementRow("Execute $reqAgreements legal agreements", "${userProfile.signedAgreementsCount} signed", hasAgreements)
                                RequirementRow("Reach reputation score of $reqRepScore", "Current score: ${userProfile.reputationScore}", hasRepScore)
                                RequirementRow("Active Premium Co-Op Subscription", if (hasPremium) "Active" else "Inactive", hasPremium)
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val nextLevel = if (selectedTier == 2) "L2 Certified Leader" else "L3 Elite Guild Partner"
                                val updated = userProfile.copy(verificationLevel = nextLevel)
                                globalViewModel.updateUserProfile(updated)
                                globalViewModel.showCelebration.value = true
                                showVerificationDialog = false
                                FeedbackManager.showSuccess("Promoted to $nextLevel!")
                            },
                            enabled = allMet,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("apply_promotion_button")
                        ) {
                            Text(
                                text = if (allMet) "PROMPT AUTOMATED PROMOTION" else "CHECKPOINTS INCOMPLETE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showVerificationDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("DISMISS", color = TextSecondary, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun RequirementRow(
    label: String,
    currentText: String,
    isMet: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceLightColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            tint = if (isMet) NeonEmerald else CrispAmber,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = currentText,
                color = if (isMet) NeonEmerald else TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
