package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CreatorCoopApp
import com.example.ui.theme.*
import com.example.ui.tour.guidedTourTarget
import com.example.data.model.AuditLog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun VideoHuddleScreen(
    onBack: () -> Unit,
    globalViewModel: com.example.ui.viewmodels.GlobalViewModel? = null
) {
    val context = LocalContext.current
    val application = context.applicationContext as CreatorCoopApp
    val repository = remember { application.container.repository }
    
    // Fetch user profiles from SQLite database and find the current logged-in user
    val allUsers by repository.userDao.getAllUsers().collectAsState(initial = emptyList())
    val userProfile = remember(allUsers) { allUsers.find { it.id == "me" } ?: allUsers.firstOrNull() }
    
    val coroutineScope = rememberCoroutineScope()
    val randomObj = remember { java.util.Random() }
    
    // Call Connection States
    var isJoined by remember { mutableStateOf(false) }
    var micEnabled by remember { mutableStateOf(true) }
    var cameraEnabled by remember { mutableStateOf(true) }
    var screenShareEnabled by remember { mutableStateOf(false) }
    
    // Call timers
    var callDurationSeconds by remember { mutableStateOf(0) }
    
    // Live talk volume indicator multipliers (for glowing pulse effect)
    var alexVoiceVolume by remember { mutableStateOf(0.1f) }
    var mayaVoiceVolume by remember { mutableStateOf(0.1f) }
    var thomasVoiceVolume by remember { mutableStateOf(0.1f) }
    var yourVoiceVolume by remember { mutableStateOf(0.1f) }

    // Sliding Huddle Chat
    var showChatDrawer by remember { mutableStateOf(false) }
    var chatMessageText by remember { mutableStateOf("") }
    val huddleChatMessages = remember {
        mutableStateListOf(
            HuddleChatMessage("alex", "Alex Mercer", "Let's review the storyboards for the intro hook.", System.currentTimeMillis() - 40000),
            HuddleChatMessage("maya", "Maya Lin", "Blender VFX models are fully compiled and rendering.", System.currentTimeMillis() - 20000)
        )
    }

    // Call Duration incrementer
    LaunchedEffect(isJoined) {
        if (isJoined) {
            callDurationSeconds = 0
            while (true) {
                delay(1000)
                callDurationSeconds++
            }
        }
    }

    // Voice speaking simulator (fluctuating volume levels for speakers)
    LaunchedEffect(isJoined, micEnabled) {
        if (isJoined) {
            if (!micEnabled) {
                yourVoiceVolume = 0f
            }
            while (true) {
                delay(800)
                alexVoiceVolume = if (randomObj.nextInt(10) > 4) 0.2f + randomObj.nextFloat() * 0.9f else 0.1f
                mayaVoiceVolume = if (randomObj.nextInt(10) > 6) 0.2f + randomObj.nextFloat() * 0.9f else 0.1f
                thomasVoiceVolume = if (randomObj.nextInt(10) > 5) 0.2f + randomObj.nextFloat() * 0.9f else 0.1f
                yourVoiceVolume = if (micEnabled && randomObj.nextInt(10) > 7) 0.2f + randomObj.nextFloat() * 0.8f else 0f
            }
        } else {
            yourVoiceVolume = 0f
            alexVoiceVolume = 0f
            mayaVoiceVolume = 0f
            thomasVoiceVolume = 0f
        }
    }

    // Interactive bot message simulator
    LaunchedEffect(isJoined) {
        if (isJoined) {
            delay(5000)
            huddleChatMessages.add(HuddleChatMessage("thomas", "Thomas Wright", "Audio stems are aligned. Ready to drop them in.", System.currentTimeMillis()))
            delay(12000)
            huddleChatMessages.add(HuddleChatMessage("alex", "Alex Mercer", "Excellent, let's coordinate the final export settings.", System.currentTimeMillis()))
        }
    }

    // Teardown resources when screen is disposed or user leaves
    DisposableEffect(Unit) {
        onDispose {
            isJoined = false
            micEnabled = false
            cameraEnabled = false
            screenShareEnabled = false
            yourVoiceVolume = 0f
            alexVoiceVolume = 0f
            mayaVoiceVolume = 0f
            thomasVoiceVolume = 0f
        }
    }

    // Main layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (!isJoined) {
            // --- STATE 1: PRE-JOIN LOBBY DECK ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("huddle_lobby_back")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit Lobby", tint = Color.White)
                    }
                    Text(
                        "CO-OP HUDDLE DECK",
                        color = AccentRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Surface(
                        color = NeonEmerald.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "FREE LOCAL LOBBY",
                            color = NeonEmerald,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Center WebCam Preview card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, ColorDivider)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (cameraEnabled) {
                            // Render simulated preview
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .background(AccentBlue.copy(alpha = 0.1f))
                                        .border(2.dp, AccentBlue, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Face,
                                        contentDescription = null,
                                        tint = AccentBlue,
                                        modifier = Modifier.size(56.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Your Camera is Streaming",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    "Simulated local video capture is active",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            // Camera Disabled Screen
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .background(ColorDivider.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VideocamOff,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Camera is Muted",
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        // Bottom hardware quick tests bar
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { micEnabled = !micEnabled }) {
                                Icon(
                                    imageVector = if (micEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                                    contentDescription = "Test Mic",
                                    tint = if (micEnabled) NeonEmerald else AccentRed
                                )
                            }
                            IconButton(onClick = { cameraEnabled = !cameraEnabled }) {
                                Icon(
                                    imageVector = if (cameraEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                    contentDescription = "Test Camera",
                                    tint = if (cameraEnabled) NeonEmerald else AccentRed
                                )
                            }
                        }
                    }
                }

                // Invitation list and join controls
                val featureFlags by repository.getAllFeatureFlagsFlow().collectAsState(initial = emptyList())
                val userRole = userProfile?.systemRole ?: "PARTICIPANT"

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Peers in Huddle: Alex M., Maya L., Thomas W.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    com.example.ui.components.FeatureGate(
                        flagKey = "VOICE_HUDDLE_BETA",
                        featureFlags = featureFlags,
                        userRole = userRole,
                        showBannerOnRestricted = true,
                        customRestrictedNotice = "Real-time Video Huddle is currently restricted for your tier by Platform Administration."
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    // Insert audit log of joining
                                    val log = AuditLog(
                                        id = UUID.randomUUID().toString(),
                                        adminId = userProfile?.id ?: "me",
                                        adminName = userProfile?.displayName ?: "Creator",
                                        actionTaken = "HUDDLE_SESSION_JOINED",
                                        targetType = "VIDEO_HUDDLE",
                                        targetId = "huddle_room",
                                        reason = "Entered active huddle with team. Video: $cameraEnabled, Audio: $micEnabled",
                                        createdAt = System.currentTimeMillis()
                                    )
                                    repository.insertAuditLog(log)
                                    isJoined = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("enter_huddle_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.VideoCall, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ENTER HUDDLE ROOM", fontWeight = FontWeight.Black, fontSize = 15.sp)
                        }
                    }
                }
            }
        } else {
            // --- STATE 2: ACTIVE MULTI-PEER VIDEO HUDDLE ---
            Column(modifier = Modifier.fillMaxSize()) {
                // Active Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(NeonEmerald)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val formattedTime = remember(callDurationSeconds) {
                            val mins = callDurationSeconds / 60
                            val secs = callDurationSeconds % 60
                            String.format("%02d:%02d", mins, secs)
                        }
                        Text(
                            "LIVE HUDDLE | $formattedTime",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        color = SurfaceLightColor,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "4 PARTICIPANTS",
                            color = AccentBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                // 2x2 Grid of Participants
                Column(
                    modifier = if (globalViewModel != null) Modifier
                        .weight(1f)
                        .guidedTourTarget("video_huddle_spatial_video", globalViewModel.tourManager)
                        .padding(horizontal = 12.dp)
                    else Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Card 1: You
                        val myRoleName = if (userProfile?.id == "me" && userProfile?.systemRole != null) userProfile?.systemRole else "CREATOR"
                        HuddleParticipantCard(
                            name = "You (${userProfile?.displayName ?: "Creator"})",
                            role = myRoleName ?: "CREATOR",
                            avatarLetters = "ME",
                            avatarColor = AccentRed,
                            speakingVolumeMultiplier = yourVoiceVolume,
                            cameraOn = cameraEnabled,
                            micOn = micEnabled,
                            isScreenSharing = screenShareEnabled,
                            modifier = Modifier.weight(1f)
                        )

                        // Card 2: Alex Mercer
                        HuddleParticipantCard(
                            name = "Alex Mercer",
                            role = "LEAD STORYTELLER",
                            avatarLetters = "AM",
                            avatarColor = NeonEmerald,
                            speakingVolumeMultiplier = alexVoiceVolume,
                            cameraOn = true,
                            micOn = true,
                            isScreenSharing = false,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Card 3: Maya Lin
                        HuddleParticipantCard(
                            name = "Maya Lin",
                            role = "3D VFX ARTIST",
                            avatarLetters = "ML",
                            avatarColor = AccentBlue,
                            speakingVolumeMultiplier = mayaVoiceVolume,
                            cameraOn = true,
                            micOn = true,
                            isScreenSharing = false,
                            modifier = Modifier.weight(1f)
                        )

                        // Card 4: Thomas Wright
                        HuddleParticipantCard(
                            name = "Thomas Wright",
                            role = "SOUND SUPERVISOR",
                            avatarLetters = "TW",
                            avatarColor = CrispAmber,
                            speakingVolumeMultiplier = thomasVoiceVolume,
                            cameraOn = false,
                            micOn = false,
                            isScreenSharing = false,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Expandable Text Chat Peek Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showChatDrawer = true }
                        .background(SurfaceColor)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ChatBubble, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        val lastMsg = huddleChatMessages.lastOrNull()
                        val chatPeekText = if (lastMsg != null) "${lastMsg.senderName}: ${lastMsg.messageText}" else "Swipe or tap to open huddle chat"
                        Text(
                            chatPeekText,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                    Icon(Icons.Default.KeyboardArrowUp, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                }

                // bottom controller board
                Surface(
                    modifier = if (globalViewModel != null) Modifier
                        .fillMaxWidth()
                        .guidedTourTarget("video_huddle_collaborator_controls", globalViewModel.tourManager)
                        .padding(16.dp)
                    else Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    color = PrimaryBackground.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Toggle Mic
                        IconButton(
                            onClick = {
                                micEnabled = !micEnabled
                                if (!micEnabled) {
                                    yourVoiceVolume = 0f
                                }
                                coroutineScope.launch {
                                    val log = AuditLog(
                                        id = UUID.randomUUID().toString(),
                                        adminId = userProfile?.id ?: "me",
                                        adminName = userProfile?.displayName ?: "Creator",
                                        actionTaken = if (micEnabled) "HUDDLE_MIC_UNMUTED" else "HUDDLE_MIC_MUTED",
                                        targetType = "VIDEO_HUDDLE",
                                        targetId = "huddle_room",
                                        reason = "Toggled microphone state to $micEnabled during live huddle.",
                                        createdAt = System.currentTimeMillis()
                                    )
                                    repository.insertAuditLog(log)
                                }
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(if (micEnabled) SurfaceLightColor else AccentRed.copy(alpha = 0.2f))
                                .border(1.dp, if (micEnabled) Color.Transparent else AccentRed.copy(alpha = 0.4f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (micEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                                contentDescription = "Toggle Mic",
                                tint = if (micEnabled) Color.White else AccentRed
                            )
                        }

                        // Toggle Camera
                        IconButton(
                            onClick = {
                                cameraEnabled = !cameraEnabled
                                coroutineScope.launch {
                                    val log = AuditLog(
                                        id = UUID.randomUUID().toString(),
                                        adminId = userProfile?.id ?: "me",
                                        adminName = userProfile?.displayName ?: "Creator",
                                        actionTaken = if (cameraEnabled) "HUDDLE_CAMERA_ENABLED" else "HUDDLE_CAMERA_DISABLED",
                                        targetType = "VIDEO_HUDDLE",
                                        targetId = "huddle_room",
                                        reason = "Toggled camera state to $cameraEnabled during live huddle.",
                                        createdAt = System.currentTimeMillis()
                                    )
                                    repository.insertAuditLog(log)
                                }
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(if (cameraEnabled) SurfaceLightColor else AccentRed.copy(alpha = 0.2f))
                                .border(1.dp, if (cameraEnabled) Color.Transparent else AccentRed.copy(alpha = 0.4f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (cameraEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                contentDescription = "Toggle Camera",
                                tint = if (cameraEnabled) Color.White else AccentRed
                            )
                        }

                        // Toggle Screenshare
                        IconButton(
                            onClick = {
                                screenShareEnabled = !screenShareEnabled
                                coroutineScope.launch {
                                    val log = AuditLog(
                                        id = UUID.randomUUID().toString(),
                                        adminId = userProfile?.id ?: "me",
                                        adminName = userProfile?.displayName ?: "Creator",
                                        actionTaken = if (screenShareEnabled) "HUDDLE_SCREENSHARE_ENABLED" else "HUDDLE_SCREENSHARE_DISABLED",
                                        targetType = "VIDEO_HUDDLE",
                                        targetId = "huddle_room",
                                        reason = "Toggled screen sharing state to $screenShareEnabled during live huddle.",
                                        createdAt = System.currentTimeMillis()
                                    )
                                    repository.insertAuditLog(log)
                                }
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(if (screenShareEnabled) AccentBlue.copy(alpha = 0.2f) else SurfaceLightColor)
                                .border(1.dp, if (screenShareEnabled) AccentBlue else Color.Transparent, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ScreenShare,
                                contentDescription = "Toggle Screenshare",
                                tint = if (screenShareEnabled) AccentBlue else Color.White
                            )
                        }

                        // End Call
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    val log = AuditLog(
                                        id = UUID.randomUUID().toString(),
                                        adminId = userProfile?.id ?: "me",
                                        adminName = userProfile?.displayName ?: "Creator",
                                        actionTaken = "HUDDLE_SESSION_LEFT",
                                        targetType = "VIDEO_HUDDLE",
                                        targetId = "huddle_room",
                                        reason = "Left live huddle room gracefully. Duration: ${callDurationSeconds}s",
                                        createdAt = System.currentTimeMillis()
                                    )
                                    repository.insertAuditLog(log)
                                    isJoined = false
                                    onBack()
                                }
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(AccentRed)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = "End Call",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // --- HUDDLE TEXT CHAT BOTTOM DRAWER (SHEET) ---
    if (showChatDrawer) {
        ModalBottomSheet(
            onDismissRequest = { showChatDrawer = false },
            containerColor = SurfaceColor,
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight(0.7f)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "HUDDLE TEXT DISCUSSIONS",
                    fontSize = 11.sp,
                    color = AccentRed,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Chat bubbles
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(huddleChatMessages, key = { "${it.senderId}_${it.timestamp}" }) { msg ->
                        val isMe = msg.senderId == "me"
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    msg.senderName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isMe) NeonEmerald else AccentBlue
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp)),
                                    fontSize = 8.sp,
                                    color = TextSecondary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .background(
                                        color = if (isMe) AccentBlue.copy(alpha = 0.15f) else PrimaryBackground,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(msg.messageText, fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Message input Tray
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = chatMessageText,
                        onValueChange = { chatMessageText = it },
                        placeholder = { Text("Say something in the huddle...", fontSize = 12.sp, color = TextSecondary) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = ColorDivider.copy(alpha = 0.5f),
                            focusedContainerColor = PrimaryBackground,
                            unfocusedContainerColor = PrimaryBackground
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (chatMessageText.isNotBlank()) {
                                huddleChatMessages.add(
                                    HuddleChatMessage(
                                        "me",
                                        userProfile?.displayName ?: "You",
                                        chatMessageText,
                                        System.currentTimeMillis()
                                    )
                                )
                                chatMessageText = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(AccentBlue)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HuddleParticipantCard(
    name: String,
    role: String,
    avatarLetters: String,
    avatarColor: Color,
    speakingVolumeMultiplier: Float,
    cameraOn: Boolean,
    micOn: Boolean,
    isScreenSharing: Boolean,
    modifier: Modifier = Modifier
) {
    // Speaking volume glow animation
    val glowWidth by animateFloatAsState(
        targetValue = if (micOn && speakingVolumeMultiplier > 0.3f) 4f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    Card(
        modifier = modifier
            .fillMaxSize()
            .border(
                glowWidth.dp,
                if (glowWidth > 0) avatarColor.copy(alpha = 0.8f) else Color.Transparent,
                RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (cameraOn && !isScreenSharing) {
                // Simulated camera capture frame
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(avatarColor.copy(alpha = 0.04f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(avatarColor.copy(alpha = 0.12f))
                                .border(1.dp, avatarColor.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(avatarLetters, color = avatarColor, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "CAM FEED ACTIVE",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            } else if (isScreenSharing) {
                // Screen sharing view overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AccentBlue.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ScreenShare, null, tint = AccentBlue, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("SHARING SCREEN", color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        Text("YouTube Draft Timeline", color = TextSecondary, fontSize = 9.sp)
                    }
                }
            } else {
                // Camera Muted
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(ColorDivider.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.VideocamOff, null, tint = TextSecondary, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("CAMERA OFF", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Speaking glowing indicator dot
            if (micOn && speakingVolumeMultiplier > 0.3f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(NeonEmerald)
                )
            }

            // Info tags card at bottom
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                color = Color.Black.copy(alpha = 0.65f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (micOn) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = null,
                        tint = if (micOn) NeonEmerald else AccentRed,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

data class HuddleChatMessage(
    val senderId: String,
    val senderName: String,
    val messageText: String,
    val timestamp: Long
)
