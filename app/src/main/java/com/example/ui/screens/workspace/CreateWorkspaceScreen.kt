package com.example.ui.screens.workspace

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodels.WorkspaceViewModel
import com.example.data.model.Workspace
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CreateWorkspaceScreen(
    workspaceViewModel: WorkspaceViewModel,
    userId: String,
    userProfile: com.example.data.model.UserProfile? = null,
    onNavigateToPro: () -> Unit = {},
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val workspaces: List<Workspace> by workspaceViewModel.workspaces.collectAsState(initial = emptyList())
    val userWorkspacesCount = remember(workspaces, userId) {
        workspaces.count { it.createdBy == userId }
    }
    val isPro = userProfile?.isVerifiedPro ?: false
    val isLimitReached = !isPro && userWorkspacesCount >= 2
    
    // States for Form
    var step by rememberSaveable { mutableStateOf(1) } // 1: Core Details, 2: Aesthetics & Description, 3: Success
    var name by rememberSaveable { mutableStateOf("") }
    var selectedPlatform by rememberSaveable { mutableStateOf("YOUTUBE") }
    var pitch by rememberSaveable { mutableStateOf("") }
    var selectedAccent by rememberSaveable { mutableStateOf("BLUE") } // BLUE, EMERALD, AMBER, RED
    
    // Validation state
    var nameError by remember { mutableStateOf<String?>(null) }
    
    // Draft state
    var showDraftRestoreDialog by remember { mutableStateOf(false) }
    var draftJsonToRestore by remember { mutableStateOf("") }
    
    // Success Details
    val newlyCreatedWs by workspaceViewModel.newlyCreatedWorkspace.collectAsState()

    // Load draft on startup
    LaunchedEffect(userId) {
        val savedDraft = workspaceViewModel.getWorkspaceDraft(userId)
        if (!savedDraft.isNullOrBlank()) {
            draftJsonToRestore = savedDraft
            showDraftRestoreDialog = true
        }
    }

    // Function to validate name
    fun validateName(): Boolean {
        return when {
            name.isBlank() -> {
                nameError = "Workspace name cannot be empty"
                false
            }
            name.length < 3 -> {
                nameError = "Workspace name must be at least 3 characters"
                false
            }
            name.contains(Regex("[@#\\$%\\^&\\*]")) -> {
                nameError = "Special characters (@, #, $, %, etc.) are not allowed"
                false
            }
            else -> {
                nameError = null
                true
            }
        }
    }

    // Save Draft logic
    fun saveDraft() {
        val json = "{\"name\":\"$name\",\"platform\":\"$selectedPlatform\",\"pitch\":\"$pitch\",\"accent\":\"$selectedAccent\"}"
        coroutineScope.launch {
            workspaceViewModel.saveWorkspaceDraft(userId, json)
        }
    }

    // Restore Draft logic
    fun restoreDraft(json: String) {
        try {
            val restoredName = "name\":\"([^\"]*)\"".toRegex().find(json)?.groups?.get(1)?.value ?: ""
            val restoredPlatform = "platform\":\"([^\"]*)\"".toRegex().find(json)?.groups?.get(1)?.value ?: "YOUTUBE"
            val restoredPitch = "pitch\":\"([^\"]*)\"".toRegex().find(json)?.groups?.get(1)?.value ?: ""
            val restoredAccent = "accent\":\"([^\"]*)\"".toRegex().find(json)?.groups?.get(1)?.value ?: "BLUE"

            name = restoredName
            selectedPlatform = restoredPlatform
            pitch = restoredPitch
            selectedAccent = restoredAccent
        } catch (e: Exception) {
            // Fallback
        }
    }

    // Main Canvas
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
    ) {
        // Aesthetic ambient background glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            when (selectedAccent) {
                                "BLUE" -> AccentBlue.copy(alpha = 0.08f)
                                "EMERALD" -> NeonEmerald.copy(alpha = 0.08f)
                                "AMBER" -> CrispAmber.copy(alpha = 0.08f)
                                else -> AccentRed.copy(alpha = 0.08f)
                            },
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Form Steps Header (Only show if not in success state)
            if (step < 3) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (step > 1) {
                                step--
                            } else {
                                onBack()
                            }
                        },
                        modifier = Modifier
                            .background(SurfaceColor, CircleShape)
                            .border(1.dp, ColorDivider, CircleShape)
                            .size(40.dp)
                            .testTag("create_ws_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Horizontal Progress indicators
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(1, 2).forEach { s ->
                            val isActive = s == step
                            Box(
                                modifier = Modifier
                                    .height(6.dp)
                                    .width(if (isActive) 24.dp else 12.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (isActive) AccentBlue else ColorDivider)
                            )
                        }
                    }

                    // Save Draft Button
                    TextButton(
                        onClick = {
                            saveDraft()
                            // Show quick feedback
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = AccentBlue),
                        modifier = Modifier.testTag("save_ws_draft_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Draft", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Wizard Step Transition Container
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally(animationSpec = tween(300)) { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally(animationSpec = tween(300)) { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally(animationSpec = tween(300)) { width -> -width } + fadeIn() togetherWith
                                slideOutHorizontally(animationSpec = tween(300)) { width -> width } + fadeOut()
                    }
                },
                modifier = Modifier.weight(1f)
            ) { currentStep ->
                when (currentStep) {
                    1 -> {
                        val step1Scroll = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .imePadding()
                                .verticalScroll(step1Scroll),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Large Header
                            Column {
                                if (isLimitReached) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                        border = BorderStroke(1.dp, CrispAmber),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(Icons.Default.Star, null, tint = CrispAmber, modifier = Modifier.size(24.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Free Workspace Limit (2 Max)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text("Upgrade to Verified Pro for unlimited workspaces & zero ads.", color = TextSecondary, fontSize = 11.sp)
                                            }
                                            Button(
                                                onClick = onNavigateToPro,
                                                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text("Upgrade", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                Text(
                                    text = "ESTABLISH YOUR CO-OP",
                                    color = AccentBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.5.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "First, tell us about your brand",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Give your collaborative hub a distinct identity. This will serve as your digital home.",
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Workspace Name Card Input
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, if (nameError != null) AccentRed else ColorDivider),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Workspace Name",
                                        color = if (nameError != null) AccentRed else Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = name,
                                        onValueChange = {
                                            name = it
                                            if (nameError != null) validateName()
                                        },
                                        placeholder = { Text("e.g. Dream Team Productions", color = TextMuted) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("ws_name_input_field"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedContainerColor = SurfaceLightColor,
                                            unfocusedContainerColor = SurfaceLightColor,
                                            focusedBorderColor = AccentBlue,
                                            unfocusedBorderColor = Color.Transparent
                                        ),
                                        isError = nameError != null,
                                        singleLine = true
                                    )
                                    if (nameError != null) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Error, contentDescription = null, tint = AccentRed, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(nameError!!, color = AccentRed, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }

                            // Platform Selector Title
                            Text(
                                text = "PRIMARY DISTRIBUTION PLATFORM",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            // Horizontal Cards platform options
                            val platforms = listOf(
                                "YOUTUBE" to Icons.Default.PlayArrow,
                                "INSTAGRAM" to Icons.Default.CameraAlt,
                                "TIKTOK" to Icons.Default.MusicNote
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                platforms.forEach { (platform, icon) ->
                                    val isSelected = selectedPlatform == platform
                                    val platformColor = when (platform) {
                                        "YOUTUBE" -> AccentRed
                                        "INSTAGRAM" -> CrispAmber
                                        else -> AccentBlue
                                    }
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedPlatform = platform }
                                            .testTag("ws_platform_chip_${platform.lowercase()}"),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) platformColor.copy(alpha = 0.08f) else SurfaceColor
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = if (isSelected) platformColor else ColorDivider
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) platformColor.copy(alpha = 0.2f) else SurfaceLightColor),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = null,
                                                    tint = if (isSelected) platformColor else TextSecondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Text(
                                                text = platform,
                                                color = if (isSelected) Color.White else TextSecondary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // Bottom Navigation Action
                            Button(
                                onClick = {
                                    if (validateName()) {
                                        saveDraft()
                                        step = 2
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("ws_creation_next_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Continue to Settings", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.ChevronRight, contentDescription = null)
                            }
                        }
                    }

                    2 -> {
                        val step2Scroll = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .imePadding()
                                .verticalScroll(step2Scroll),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Large Header
                            Column {
                                Text(
                                    text = "BRAND PITCH & THEME",
                                    color = AccentBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.5.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Outline your co-op vision",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "State your co-op's creative mission and choose a brand accent style.",
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Description Pitch Field Card
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, ColorDivider),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Co-Op Mission Pitch",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = pitch,
                                        onValueChange = { pitch = it },
                                        placeholder = { Text("What is your team creating? (e.g. Let's build a weekly science tech docuseries with clean graphical animations)", color = TextMuted) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(110.dp)
                                            .testTag("ws_pitch_input_field"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedContainerColor = SurfaceLightColor,
                                            unfocusedContainerColor = SurfaceLightColor,
                                            focusedBorderColor = AccentBlue,
                                            unfocusedBorderColor = Color.Transparent
                                        ),
                                        maxLines = 4
                                    )
                                }
                            }

                            // Theme Selector Title
                            Text(
                                text = "CO-OP VISUAL ACCENT THEME",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            // Visual Theme selection
                            val themes = listOf(
                                Triple("BLUE", AccentBlue, "Sapphire Stream"),
                                Triple("EMERALD", NeonEmerald, "Emerald Focus"),
                                Triple("AMBER", CrispAmber, "Amber Glow"),
                                Triple("RED", AccentRed, "Crimson Fire")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                themes.forEach { (themeId, color, nameStr) ->
                                    val isSelected = selectedAccent == themeId
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedAccent = themeId }
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) color.copy(alpha = 0.15f) else SurfaceColor)
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) color else ColorDivider,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .padding(vertical = 12.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(color)
                                                    .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                            )
                                            Text(
                                                text = themeId,
                                                color = if (isSelected) Color.White else TextSecondary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // Action Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                TextButton(
                                    onClick = { step = 1 },
                                    modifier = Modifier
                                        .weight(0.4f)
                                        .height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
                                ) {
                                    Text("Back", fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        if (isLimitReached) {
                                            onNavigateToPro()
                                        } else {
                                            saveDraft()
                                            workspaceViewModel.createWorkspace(name, selectedPlatform, userId)
                                            step = 3
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                        .testTag("ws_creation_launch_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when (selectedAccent) {
                                            "BLUE" -> AccentBlue
                                            "EMERALD" -> NeonEmerald
                                            "AMBER" -> CrispAmber
                                            else -> AccentRed
                                        }
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Icon(Icons.Default.RocketLaunch, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Launch Workspace", fontWeight = FontWeight.Black, fontSize = 15.sp)
                                }
                            }
                        }
                    }

                    3 -> {
                        // Success state with beautiful completion
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            Spacer(modifier = Modifier.height(32.dp))

                            // Animated Success Circle
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                NeonEmerald,
                                                AccentBlue
                                            )
                                        )
                                    )
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(PrimaryBackground),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Success",
                                        tint = NeonEmerald,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }

                            // Congratulations heading
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "CO-OP LAUNCHED SUCCESSFULLY!",
                                    color = NeonEmerald,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Your workspace is live",
                                    color = Color.White,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Welcome to the future of collaborative creator hubs. Invite members and publish your masterpieces.",
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }

                            // Summary Review Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                shape = RoundedCornerShape(24.dp),
                                border = BorderStroke(1.dp, ColorDivider)
                            ) {
                                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Workspace Identity", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Surface(
                                            color = AccentBlue.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "PRO EDITION",
                                                color = AccentBlue,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    HorizontalDivider(color = ColorDivider)

                                    // Display Details
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val platformIcon = when (selectedPlatform) {
                                            "YOUTUBE" -> Icons.Default.PlayArrow
                                            "INSTAGRAM" -> Icons.Default.CameraAlt
                                            else -> Icons.Default.MusicNote
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(SurfaceLightColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = platformIcon,
                                                contentDescription = null,
                                                tint = AccentBlue,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = newlyCreatedWs?.name ?: name,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                            Text(
                                                text = "Platform: $selectedPlatform",
                                                color = TextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    if (pitch.isNotBlank()) {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = SurfaceLightColor),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "\"$pitch\"",
                                                color = TextSecondary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(12.dp),
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // Enter workspace button
                            Button(
                                onClick = {
                                    // Reset state, select workspace, go to workspaces overview
                                    newlyCreatedWs?.let { ws ->
                                        workspaceViewModel.selectWorkspace(ws)
                                    }
                                    // Also make sure newlyCreatedWorkspace flow is cleared
                                    workspaceViewModel.newlyCreatedWorkspace.value = null
                                    onBack()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("ws_enter_active_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Launch Into Co-Op", fontWeight = FontWeight.Black, fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.Launch, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
    }

    // Draft Found Restore Dialog
    if (showDraftRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showDraftRestoreDialog = false },
            containerColor = SurfaceColor,
            title = {
                Text(
                    "Resume Setup?",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    "We found an unfinished draft workspace on this device. Would you like to restore your progress and continue setup?",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        restoreDraft(draftJsonToRestore)
                        showDraftRestoreDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("Restore Progress")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDraftRestoreDialog = false
                    }
                ) {
                    Text("Discard Draft", color = AccentRed)
                }
            }
        )
    }
}
