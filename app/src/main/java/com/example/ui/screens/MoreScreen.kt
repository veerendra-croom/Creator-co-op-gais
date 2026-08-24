package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.ui.tour.guidedTourTarget

@Composable
fun MoreScreen(
    currentRole: String?,
    userId: String?,
    globalViewModel: com.example.ui.viewmodels.GlobalViewModel,
    onNavigate: (String) -> Unit
) {
    val featureFlags by globalViewModel.featureFlags.collectAsState()
    val weeklyDigestEnabled by globalViewModel.weeklyDigestEnabled.collectAsState()
    val uiTextSizeState by globalViewModel.uiTextSize.collectAsState()
    val syncFrequencyState by globalViewModel.syncFrequency.collectAsState()
    val hapticFeedbackEnabledState by globalViewModel.hapticFeedbackEnabled.collectAsState()
    var showLicenses by remember { mutableStateOf(false) }

    val effectiveUid = remember(userId) { if (!userId.isNullOrBlank()) userId else "guest_user" }

    if (showLicenses) {
        LicensesDialog(onDismiss = { showLicenses = false })
    }

    LaunchedEffect(effectiveUid) {
        globalViewModel.loadWeeklyDigestSetting(effectiveUid)
        globalViewModel.loadUiTextSizeSetting(effectiveUid)
        globalViewModel.loadSyncFrequencySetting(effectiveUid)
        globalViewModel.loadHapticFeedbackSetting(effectiveUid)
    }

    val filteredItems = remember(featureFlags, currentRole) {
        val items = mutableListOf(
            MoreMenuItem("My Profile", "Identity, skills & reputation", Icons.Default.Person, AccentBlue, "PROFILE"),
            MoreMenuItem("Analytics", "Growth & engagement insights", Icons.Default.Leaderboard, NeonEmerald, "ANALYTICS"),
            MoreMenuItem("Pipeline", "Production task funnel", Icons.Default.Analytics, AccentBlue, "CONTENT_PIPELINE"),
            MoreMenuItem("Video Huddle", "Live team video room", Icons.Default.VideoCall, AccentRed, "VIDEO_HUDDLE"),
            MoreMenuItem("Knowledge", "Co-op manuals & resources", Icons.Default.AutoStories, CrispAmber, "KNOWLEDGE_BASE"),
            MoreMenuItem("Premium Pro", "Manage subscription", Icons.Default.Star, CrispAmber, "PREMIUM_SUBSCRIPTION"),
            MoreMenuItem("Search", "Global index lookup", Icons.Default.Search, CrispAmber, "SEARCH"),
            MoreMenuItem("Refer a Teammate", "Invite friends & earn premium", Icons.Default.CardGiftcard, CrispAmber, "REFER_TEAMMATE"),
            MoreMenuItem("Connections", "Network requests", Icons.Default.PersonAdd, NeonEmerald, "CONNECTION_REQUESTS"),
            MoreMenuItem("Blocked Users", "Manage ignored profiles", Icons.Default.Block, TextMuted, "BLOCKED_USERS"),
            MoreMenuItem("Help Desk", "Contact developer support", Icons.Default.ContactSupport, TextSecondary, "SUPPORT_CENTER")
        )

        if (currentRole == "PLATFORM_ADMIN" || currentRole == "ADMIN") {
            items.add(0, MoreMenuItem("Admin Console", "System administration tools", Icons.Default.AdminPanelSettings, AccentRed, "ADMIN"))
            items.add(MoreMenuItem("Founder CRM", "Beta cohort & metrics manager", Icons.Default.ManageAccounts, NeonEmerald, "FOUNDER_CRM"))
            items.add(MoreMenuItem("Comm Center", "Announcements & campaigns", Icons.Default.Campaign, AccentRed, "COMM_CENTER"))
            items.add(MoreMenuItem("Platform Control", "Central settings & flags", Icons.Default.Tune, NeonEmerald, "PLATFORM_CONTROL"))
            items.add(MoreMenuItem("Founder Command", "Closed beta executive deck", Icons.Default.RocketLaunch, AccentBlue, "FOUNDER_COMMAND"))
        }

        items.filter { item ->
            when (item.route) {
                "ANALYTICS" -> featureFlags.find { it.flagKey == "analytics_enabled" }?.isEnabled ?: true
                "VIDEO_HUDDLE" -> featureFlags.find { it.flagKey == "huddles_enabled" }?.isEnabled ?: true
                "FOUNDER_CRM" -> featureFlags.find { it.flagKey == "founder_crm_enabled" }?.isEnabled ?: true
                "REFER_TEAMMATE" -> featureFlags.find { it.flagKey == "referral_system_enabled" }?.isEnabled ?: true
                else -> true
            }
        }
    }

    val currentThemeMode by globalViewModel.themeMode.collectAsState()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
    ) {
        // Contextual Screen-Specific Guided Tutorials Section
        item(span = { GridItemSpan(2) }) {
            val uid = userId ?: "guest"
            val tourStatuses by globalViewModel.tourManager.tourStatuses.collectAsState()
            var isExpanded by remember { mutableStateOf(true) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("more_screen_guided_tutorials_card")
                    .guidedTourTarget("settings_help_tutorials", globalViewModel.tourManager),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(AccentBlue, NeonEmerald, CrispAmber)))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = AccentBlue.copy(alpha = 0.2f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.HelpOutline,
                                        contentDescription = "Guided Tutorials",
                                        tint = AccentBlue,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = NeonEmerald.copy(alpha = 0.2f),
                                    border = BorderStroke(0.5.dp, NeonEmerald.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = "CONTEXTUAL IN-APP GUIDES",
                                        color = NeonEmerald,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.8.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Interactive Screen Walkthroughs",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Screen-specific spotlight tours with dynamic coach marks",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(onClick = { isExpanded = !isExpanded }) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle Guides List",
                                tint = TextSecondary
                            )
                        }
                    }

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = ColorDivider)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "AVAILABLE SCREEN TOURS (${com.example.ui.tour.GuidedTourRepository.ALL_TOURS.size})",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.6.sp,
                                color = TextMuted
                            )

                            OutlinedButton(
                                onClick = { globalViewModel.resetAllTours(uid) },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, AccentRed.copy(alpha = 0.6f)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = null,
                                    tint = AccentRed,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "RESET ALL TOURS",
                                    color = AccentRed,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            com.example.ui.tour.GuidedTourRepository.ALL_TOURS.forEach { tour ->
                                val status = tourStatuses[tour.tourId] ?: com.example.ui.tour.TourStatus.NOT_STARTED
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = PrimaryBackground,
                                    border = BorderStroke(1.dp, ColorDivider),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val statusColor = when(status) {
                                                com.example.ui.tour.TourStatus.COMPLETED -> NeonEmerald
                                                com.example.ui.tour.TourStatus.SKIPPED -> CrispAmber
                                                com.example.ui.tour.TourStatus.NOT_STARTED -> AccentBlue
                                            }
                                            val statusText = when(status) {
                                                com.example.ui.tour.TourStatus.COMPLETED -> "COMPLETED"
                                                com.example.ui.tour.TourStatus.SKIPPED -> "SKIPPED"
                                                com.example.ui.tour.TourStatus.NOT_STARTED -> "NEW"
                                            }

                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = tour.screenTitle,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = TextPrimary
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = statusColor.copy(alpha = 0.15f),
                                                        border = BorderStroke(0.5.dp, statusColor.copy(alpha = 0.5f))
                                                    ) {
                                                        Text(
                                                            text = statusText,
                                                            color = statusColor,
                                                            fontSize = 8.5.sp,
                                                            fontWeight = FontWeight.Black,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = "${tour.steps.size} interactive steps • Target Tab: ${tour.targetTab}",
                                                    fontSize = 10.5.sp,
                                                    color = TextMuted
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                globalViewModel.replayTour(uid, tour.tourId)
                                                onNavigate(tour.targetTab)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                tint = Color.Black,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = "REPLAY",
                                                color = Color.Black,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 10.sp
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

        // Theme Selector Card
        item(span = { GridItemSpan(2) }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ColorDivider)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Palette,
                                contentDescription = "Theme",
                                tint = AccentBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PLATFORM APPEARANCE",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp
                            )
                        }
                        Text(
                            text = currentThemeMode,
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val themes = listOf(
                            Triple("OBSIDIAN", "Obsidian", AccentBlue),
                            Triple("CYBER_EMERALD", "Emerald", NeonEmerald),
                            Triple("AMBER_SUNSET", "Amber", CrispAmber),
                            Triple("LIGHT", "Light", Color(0xFF0284C7))
                        )

                        themes.forEach { (mode, label, color) ->
                            val isSelected = currentThemeMode.equals(mode, ignoreCase = true)
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { globalViewModel.setTheme("me", mode) },
                                color = if (isSelected) color.copy(alpha = 0.2f) else SurfaceLightColor,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, if (isSelected) color else ColorDivider)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) color else TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Preferences and Settings Card
        item(span = { GridItemSpan(2) }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ColorDivider)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(CrispAmber.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.NotificationsActive,
                                    contentDescription = "Digest Settings",
                                    tint = CrispAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Weekly Digest Notification",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                val isWeeklyDigestFlagEnabled = featureFlags.find { it.flagKey == "weekly_digest_enabled" }?.isEnabled ?: true
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isWeeklyDigestFlagEnabled) {
                                        "Unread workspace activities & matched open roles compiled weekly"
                                    } else {
                                        "Unread workspace activities & matched open roles compiled weekly (Disabled by platform administrator)"
                                    },
                                    color = if (isWeeklyDigestFlagEnabled) TextSecondary else AccentRed.copy(alpha = 0.8f),
                                    fontSize = 10.sp,
                                    lineHeight = 12.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        val isWeeklyDigestFlagEnabled = featureFlags.find { it.flagKey == "weekly_digest_enabled" }?.isEnabled ?: true
                        Switch(
                            checked = weeklyDigestEnabled,
                            enabled = isWeeklyDigestFlagEnabled,
                            onCheckedChange = { isChecked ->
                                globalViewModel.setWeeklyDigestSetting(effectiveUid, isChecked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PrimaryBackground,
                                checkedTrackColor = CrispAmber,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = SurfaceLightColor
                            ),
                            modifier = Modifier.testTag("weekly_digest_switch")
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = ColorDivider)

                    // Haptic Feedback Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentBlue.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Vibration,
                                    contentDescription = "Haptic Feedback Icon",
                                    tint = AccentBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Haptic Vibration Feedback",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Enable subtle micro-vibrations and tactile pulses during actions & taps",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    lineHeight = 12.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = hapticFeedbackEnabledState,
                            onCheckedChange = { isChecked ->
                                globalViewModel.setHapticFeedbackSetting(effectiveUid, isChecked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PrimaryBackground,
                                checkedTrackColor = AccentBlue,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = SurfaceLightColor
                            ),
                            modifier = Modifier.testTag("haptic_feedback_switch")
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = ColorDivider)

                    // UI Text Size Selector
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 32.dp)
                        ) {
                            Icon(
                                Icons.Default.TextFields,
                                contentDescription = "UI Text Size",
                                tint = AccentBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "UI Text Size Presets",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val sizes = listOf("SMALL", "NORMAL", "LARGE")
                            sizes.forEach { size ->
                                val isSelected = uiTextSizeState == size
                                val sizeLabel = size.lowercase().replaceFirstChar { it.uppercase() }
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { globalViewModel.setUiTextSizeSetting(effectiveUid, size) }
                                        .testTag("text_size_$size"),
                                    color = if (isSelected) AccentBlue.copy(alpha = 0.2f) else SurfaceLightColor,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, if (isSelected) AccentBlue else ColorDivider)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = sizeLabel,
                                            color = if (isSelected) AccentBlue else TextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = ColorDivider)

                    // Sync Frequency Selector
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 32.dp)
                        ) {
                            Icon(
                                Icons.Default.Sync,
                                contentDescription = "Sync Frequency",
                                tint = NeonEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sync Frequency Presets",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val frequencies = listOf("REALTIME", "HOURLY", "DAILY")
                            frequencies.forEach { freq ->
                                val isSelected = syncFrequencyState == freq
                                val freqLabel = when (freq) {
                                    "REALTIME" -> "Real-time"
                                    else -> freq.lowercase().replaceFirstChar { it.uppercase() }
                                }
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { globalViewModel.setSyncFrequencySetting(effectiveUid, freq) }
                                        .testTag("sync_frequency_$freq"),
                                    color = if (isSelected) NeonEmerald.copy(alpha = 0.2f) else SurfaceLightColor,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, if (isSelected) NeonEmerald else ColorDivider)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = freqLabel,
                                            color = if (isSelected) NeonEmerald else TextSecondary,
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

        // Section Header
        item(span = { GridItemSpan(2) }) {
            Text(
                text = "CO-OP DIRECTORY",
                color = AccentBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
        }

        items(filteredItems) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clickable { onNavigate(item.route) }
                    .testTag("more_menu_item_${item.route.lowercase()}"),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ColorDivider)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(item.iconColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = item.iconColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = item.title,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.subtitle,
                            color = TextSecondary,
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            maxLines = 2
                        )
                    }
                }
            }
        }

        item(span = { GridItemSpan(2) }) {
            SettingsFooterSection(
                userId = userId,
                globalViewModel = globalViewModel,
                onNavigate = onNavigate,
                onShowLicenses = { showLicenses = true }
            )
        }
    }
}

@Composable
fun SettingsFooterSection(
    userId: String?,
    globalViewModel: com.example.ui.viewmodels.GlobalViewModel,
    onNavigate: (String) -> Unit,
    onShowLicenses: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 32.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ColorDivider)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // --- SECTION 1: ABOUT ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "About Platform",
                    tint = AccentBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ABOUT CREATOR CO-OP",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Creator Co-Op Mobile",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Enterprise-Grade Digital Media Sandbox",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
                
                Surface(
                    color = SurfaceLightColor,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, ColorDivider)
                ) {
                    Text(
                        text = "v2.4.0 (Build 108)",
                        color = AccentBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = ColorDivider)
            Spacer(modifier = Modifier.height(16.dp))

            // --- SECTION: DATABASE SEEDING & PRODUCTION TOGGLE ---
            val isDemoSandbox by globalViewModel.demoSandboxMode.collectAsState()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = "Database Mode",
                    tint = if (isDemoSandbox) CrispAmber else NeonEmerald,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DATA INITIALIZATION & SANDBOX",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isDemoSandbox) {
                    "Demo Sandbox Active: Includes pre-seeded creator profiles, demo channel workspaces, and sample syndicate proposals for sandbox testing."
                } else {
                    "Clean Production Mode: Running with a pristine local database for real production onboarding."
                },
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceLightColor, RoundedCornerShape(12.dp))
                    .border(1.dp, ColorDivider, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isDemoSandbox) "Demo Sandbox Data" else "Clean Production DB",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isDemoSandbox) "Prepopulated sandbox enabled" else "Zero mock records populated",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
                Switch(
                    checked = isDemoSandbox,
                    onCheckedChange = { globalViewModel.toggleDemoSandboxMode(it) },
                    modifier = Modifier.testTag("sandbox_data_toggle"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = CrispAmber,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = SurfaceColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = ColorDivider)
            Spacer(modifier = Modifier.height(16.dp))

            // --- SECTION 2: LEGAL & AGREEMENTS ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Gavel,
                    contentDescription = "Legal & Compliance",
                    tint = NeonEmerald,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LEGAL & COMPLIANCE",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legal access links
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Terms & Privacy Button
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable { onNavigate("LEGAL") },
                    color = SurfaceLightColor,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, ColorDivider)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Terms & Privacy",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Guidelines Button
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable { onNavigate("COMMUNITY_GUIDELINES") },
                    color = SurfaceLightColor,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, ColorDivider)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Guidelines",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Open Source Licenses Button
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable { onShowLicenses() },
                    color = SurfaceLightColor,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, ColorDivider)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "OS Licenses",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = ColorDivider)
            Spacer(modifier = Modifier.height(16.dp))

            // --- SECTION: DATA PORTABILITY & PRIVACY (GDPR/CCPA) ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Data Portability",
                    tint = AccentBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DATA PORTABILITY & PRIVACY",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Under GDPR (Article 20) and CCPA, you have the right to request and download a complete, machine-readable copy of your personal archives. This compile contains profile credentials, post catalog, conversation footprints, and signed agreements.",
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            var showExportDialog by remember { mutableStateOf(false) }

            if (showExportDialog) {
                ExportDataDialog(
                    userId = userId,
                    globalViewModel = globalViewModel,
                    onDismiss = { showExportDialog = false }
                )
            }

            Button(
                onClick = { showExportDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceLightColor),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, ColorDivider),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("export_data_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Export Data Icon",
                        tint = AccentBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Export My Personal Data",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = ColorDivider)
            Spacer(modifier = Modifier.height(16.dp))

            // --- SECTION 3: DIRECT SUPPORT & ESCALATION ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.ContactSupport,
                    contentDescription = "Contact Desk",
                    tint = CrispAmber,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "HELP DESK & ESCALATION",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Support Email Button
                Button(
                    onClick = { launchEmailIntent(context, "support@creatorcoop.app", "Creator Co-Op Support Request") },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceLightColor),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, ColorDivider),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Mail, null, tint = CrispAmber, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Support Desk", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Security Email Button
                Button(
                    onClick = { launchEmailIntent(context, "security@creatorcoop.app", "Vulnerability Report") },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceLightColor),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, ColorDivider),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, null, tint = AccentRed, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Security Team", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = ColorDivider)
            Spacer(modifier = Modifier.height(12.dp))

            // --- FOUNDER DIRECT ACCREDITATION ---
            Text(
                text = "FOUNDER ESCALATION CHANNELS",
                color = TextSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Veerendra Button
                TextButton(
                    onClick = { launchEmailIntent(context, "veerendrabotla@gmail.com", "Co-Op Escalation - Botla Veerendra") },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Botla Veerendra (Founder)", color = AccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Text("veerendrabotla@gmail.com", color = TextMuted, fontSize = 8.sp)
                    }
                }

                // Praveen Button
                TextButton(
                    onClick = { launchEmailIntent(context, "praveenmacha777@gmail.com", "Co-Op Escalation - Macha Praveen") },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Macha Praveen (Co-Founder)", color = AccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Text("praveenmacha777@gmail.com", color = TextMuted, fontSize = 8.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Executive Attribution Footer
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Co-Op vision directed by Founder Botla Veerendra & Co-Founder Macha Praveen.",
                    color = TextMuted,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

fun launchEmailIntent(context: android.content.Context, email: String, subject: String) {
    val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
        data = android.net.Uri.parse("mailto:$email")
        putExtra(android.content.Intent.EXTRA_SUBJECT, subject)
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "No email app found to contact $email", android.widget.Toast.LENGTH_SHORT).show()
    }
}

data class MoreMenuItem(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconColor: Color,
    val route: String
)

@Composable
fun ExportDataDialog(
    userId: String?,
    globalViewModel: com.example.ui.viewmodels.GlobalViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val exportedData by globalViewModel.exportedData.collectAsState()

    var simulatedProgress by remember { mutableStateOf(0f) }
    var simulatedSector by remember { mutableStateOf("Initializing decrypter...") }
    var simulationFinished by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (userId != null) {
            globalViewModel.exportUserData(userId)
        }
    }

    LaunchedEffect(exportedData) {
        if (exportedData != null) {
            val sectors = listOf(
                "Verifying authorization protocols...",
                "Decrypting SQLite user sectors...",
                "Collating published posts logs...",
                "Extracting Vault agreement hashes...",
                "Serializing JSON payload..."
            )
            for (i in 1..100) {
                kotlinx.coroutines.delay(15)
                simulatedProgress = i / 100f
                simulatedSector = sectors[minOf((i - 1) / 20, sectors.lastIndex)]
            }
            simulationFinished = true
        }
    }

    Dialog(onDismissRequest = {
        globalViewModel.exportedData.value = null
        onDismiss()
    }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("export_data_dialog_card"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)), // Deep obsidian theme!
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.5.dp, ColorDivider)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(AccentBlue.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "GDPR Icon",
                            tint = AccentBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "PORTABILITY ENGINE",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "GDPR Art. 20 & CCPA Compliance",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!simulationFinished) {
                    // Collation Progress UI
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            progress = { simulatedProgress },
                            color = AccentBlue,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "${(simulatedProgress * 100).toInt()}% COMPILING",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = simulatedSector,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Export Ready UI
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(SurfaceColor, RoundedCornerShape(12.dp))
                                .border(BorderStroke(1.dp, ColorDivider), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            val scrollState = rememberScrollState()
                            Text(
                                text = exportedData ?: "No data compiled.",
                                color = NeonEmerald,
                                fontSize = 11.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Share Button
                            Button(
                                onClick = {
                                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(android.content.Intent.EXTRA_TEXT, exportedData ?: "")
                                        putExtra(android.content.Intent.EXTRA_SUBJECT, "CreatorCoOp_PersonalDataExport.json")
                                    }
                                    val chooser = android.content.Intent.createChooser(shareIntent, "Save or Send Your Data Export")
                                    context.startActivity(chooser)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("share_export_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Share, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Text("Share File", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Copy Button
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("Creator Co-Op Data Export", exportedData ?: "")
                                    clipboard.setPrimaryClip(clip)
                                    android.widget.Toast.makeText(context, "Copied JSON to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceLightColor),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, ColorDivider),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("copy_export_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Text("Copy Text", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Close Button
                TextButton(
                    onClick = {
                        globalViewModel.exportedData.value = null
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("close_export_dialog_btn")
                ) {
                    Text(
                        text = "DISMISS",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
