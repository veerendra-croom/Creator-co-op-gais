package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.FeatureFlag
import com.example.ui.theme.*
import com.example.ui.viewmodels.AdminViewModel

@Composable
fun AdminFeatureFlagsDialog(
    adminViewModel: AdminViewModel,
    adminId: String,
    onDismiss: () -> Unit
) {
    val flags by adminViewModel.featureFlags.collectAsState()
    var pendingRbacChange by remember { mutableStateOf<Triple<FeatureFlag, String, Any>?>(null) }
    var pendingPresetToApply by remember { mutableStateOf<String?>(null) }
    var reason by remember { mutableStateOf("") }
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var showAuditLogDialog by remember { mutableStateOf(false) }

    val allAuditLogs by adminViewModel.allAuditLogs.collectAsState()

    val categories = listOf("ALL", "COLLABORATION", "GOVERNANCE", "COMMUNICATION", "MEDIA", "CORE")

    val filteredFlags = remember(flags, searchQuery, selectedCategory) {
        flags.filter { flag ->
            val matchesCategory = selectedCategory == "ALL" || flag.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() || 
                flag.flagKey.contains(searchQuery, ignoreCase = true) || 
                (flag.description?.contains(searchQuery, ignoreCase = true) == true)
            matchesCategory && matchesSearch
        }
    }

    val activeCount = flags.count { it.globalOverrideEnabled }
    val killedCount = flags.size - activeCount

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.94f)
                .padding(12.dp)
                .testTag("feature_flags_dialog_card"),
            colors = CardDefaults.cardColors(containerColor = PrimaryBackground),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, ColorDivider)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(18.dp)) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PLATFORM FEATURE MATRIX & RBAC", fontWeight = FontWeight.Black, color = Color.White, fontSize = 15.sp)
                        }
                        Text("Granular control for Organizers, Participants, and Master Overrides", color = TextSecondary, fontSize = 11.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilledTonalButton(
                            onClick = { showAuditLogDialog = true },
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = SurfaceLightColor, contentColor = CrispAmber),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("button_view_audit_logs")
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Audit Trail", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Telemetry summary bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceColor, RoundedCornerShape(12.dp))
                        .border(1.dp, ColorDivider, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(NeonEmerald, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("$activeCount Active", color = NeonEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(modifier = Modifier.size(8.dp).background(ColorError, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("$killedCount Killed", color = ColorError, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("${flags.size} Total Capabilities", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Presets Bar
                Text("ONE-CLICK PLATFORM PRESETS", color = CrispAmber, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf(
                        "OPEN_BETA" to "Open Beta (All Active)",
                        "ORGANIZER_FIRST" to "Organizer Priority",
                        "RESTRICTED_MAINTENANCE" to "Maintenance Lockdown",
                        "MEDIA_BANDWIDTH_FREEZE" to "Bandwidth Freeze"
                    )
                    items(presets) { (key, label) ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { pendingPresetToApply = key }
                                .testTag("preset_button_$key"),
                            color = SurfaceColor,
                            border = BorderStroke(1.dp, ColorDivider),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Bolt, null, tint = AccentBlue, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search & Filter Row
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search flags, keys, or descriptions...", fontSize = 12.sp, color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = TextSecondary, modifier = Modifier.size(16.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceColor,
                        unfocusedContainerColor = SurfaceColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = ColorDivider
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedCategory = cat },
                            color = if (isSelected) AccentBlue.copy(alpha = 0.2f) else SurfaceColor,
                            border = BorderStroke(1.dp, if (isSelected) AccentBlue else ColorDivider),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) AccentBlue else TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Flags List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (filteredFlags.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No feature flags match your search/filter.", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    } else {
                        items(filteredFlags) { flag ->
                            AdminFeatureFlagItem(
                                flag = flag,
                                onToggleOrganizer = { newOrg ->
                                    adminViewModel.updateFeatureFlagRbac(
                                        flag = flag,
                                        organizerEnabled = newOrg,
                                        participantEnabled = flag.participantEnabled,
                                        globalOverrideEnabled = flag.globalOverrideEnabled,
                                        adminId = adminId,
                                        reason = "Organizer toggle via Admin Console"
                                    )
                                },
                                onToggleParticipant = { newPart ->
                                    adminViewModel.updateFeatureFlagRbac(
                                        flag = flag,
                                        organizerEnabled = flag.organizerEnabled,
                                        participantEnabled = newPart,
                                        globalOverrideEnabled = flag.globalOverrideEnabled,
                                        adminId = adminId,
                                        reason = "Participant toggle via Admin Console"
                                    )
                                },
                                onToggleMaster = {
                                    pendingRbacChange = Triple(flag, "MASTER", !flag.globalOverrideEnabled)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialog for Master Killswitch toggle
    if (pendingRbacChange != null) {
        val targetFlag = pendingRbacChange!!.first
        val isEnabling = pendingRbacChange!!.third as Boolean

        AlertDialog(
            onDismissRequest = { pendingRbacChange = null; reason = "" },
            containerColor = SurfaceColor,
            title = {
                Text(
                    if (isEnabling) "Enable Master Switch: ${targetFlag.flagKey}?" else "Kill Master Switch: ${targetFlag.flagKey}?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Provide an audit log justification for toggling the platform-wide master override for this capability.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    TextField(
                        value = reason,
                        onValueChange = { reason = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("E.g., Emergency hotpatch, Maintenance release...") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = PrimaryBackground,
                            unfocusedContainerColor = PrimaryBackground,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        adminViewModel.toggleFeatureFlag(targetFlag, adminId, reason)
                        pendingRbacChange = null
                        reason = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isEnabling) AccentBlue else AccentRed),
                    enabled = reason.isNotBlank(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Confirm Change", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRbacChange = null; reason = "" }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Confirmation dialog for Preset Profile batch application
    if (pendingPresetToApply != null) {
        val preset = pendingPresetToApply!!
        AlertDialog(
            onDismissRequest = { pendingPresetToApply = null; reason = "" },
            containerColor = SurfaceColor,
            title = {
                Text("Apply Preset Profile: $preset?", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "This will batch update all platform feature flags and role matrices according to the '$preset' configuration profile.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    TextField(
                        value = reason,
                        onValueChange = { reason = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("E.g., Scheduled maintenance window, Public launch...") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = PrimaryBackground,
                            unfocusedContainerColor = PrimaryBackground,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        adminViewModel.applyFeatureFlagPresetProfile(preset, adminId, reason)
                        pendingPresetToApply = null
                        reason = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    enabled = reason.isNotBlank(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Apply Preset", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingPresetToApply = null; reason = "" }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    if (showAuditLogDialog) {
        AuditLogViewerDialog(
            auditLogs = allAuditLogs.filter { it.actionTaken.contains("FLAG", ignoreCase = true) || it.actionTaken.contains("PRESET", ignoreCase = true) || it.actionTaken.contains("RBAC", ignoreCase = true) || it.targetType.contains("FLAG", ignoreCase = true) },
            onDismiss = { showAuditLogDialog = false }
        )
    }
}

@Composable
fun AuditLogViewerDialog(
    auditLogs: List<com.example.data.model.AuditLog>,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .padding(8.dp)
                .testTag("audit_log_viewer_card"),
            colors = CardDefaults.cardColors(containerColor = PrimaryBackground),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, ColorDivider)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, tint = CrispAmber, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("RBAC AUDIT TRAIL STREAM", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
                
                Text(
                    text = "Cryptographically verifiable timestamped audit logs for all feature toggle & preset transitions.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (auditLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(SurfaceColor, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No RBAC state transitions recorded yet.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(auditLogs) { log ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(0.5.dp, ColorDivider)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = log.actionTaken,
                                            color = AccentBlue,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(log.createdAt)),
                                            color = TextSecondary,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = log.reason.ifBlank { "Policy updated" },
                                        color = Color.White,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Admin: ${log.adminName.ifBlank { log.adminId }} • Target: ${log.targetId}",
                                        color = TextSecondary,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceLightColor)
                ) {
                    Text("Close Audit Stream", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdminFeatureFlagItem(
    flag: FeatureFlag,
    onToggleOrganizer: (Boolean) -> Unit,
    onToggleParticipant: (Boolean) -> Unit,
    onToggleMaster: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("feature_flag_item_${flag.flagKey}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, ColorDivider)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Master Switch & Status
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (flag.globalOverrideEnabled) AccentBlue.copy(alpha = 0.15f) else ColorError.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (flag.globalOverrideEnabled) Icons.Default.Bolt else Icons.Default.Block,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (flag.globalOverrideEnabled) AccentBlue else ColorError
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(flag.flagKey, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    if (flag.globalOverrideEnabled) NeonEmerald.copy(alpha = 0.15f) else ColorError.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (flag.globalOverrideEnabled) "ACTIVE" else "KILLED",
                                color = if (flag.globalOverrideEnabled) NeonEmerald else ColorError,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    if (!flag.description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(flag.description, color = TextSecondary, fontSize = 11.sp, lineHeight = 14.sp)
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("MASTER", fontSize = 8.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                    Switch(
                        checked = flag.globalOverrideEnabled,
                        onCheckedChange = { onToggleMaster() },
                        modifier = Modifier.testTag("feature_flag_switch_${flag.flagKey}"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AccentBlue,
                            uncheckedThumbColor = Color.LightGray,
                            uncheckedTrackColor = SurfaceLightColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = ColorDivider, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Sub-Role Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Organizer Switch
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .background(PrimaryBackground, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("ORGANIZERS", color = CrispAmber, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Text(
                            text = if (flag.organizerEnabled && flag.globalOverrideEnabled) "ENABLED" else "DISABLED",
                            color = if (flag.organizerEnabled && flag.globalOverrideEnabled) NeonEmerald else TextSecondary,
                            fontSize = 9.sp
                        )
                    }
                    Switch(
                        checked = flag.organizerEnabled,
                        enabled = flag.globalOverrideEnabled,
                        onCheckedChange = onToggleOrganizer,
                        modifier = Modifier.testTag("switch_organizer_${flag.flagKey}"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CrispAmber,
                            uncheckedThumbColor = Color.LightGray,
                            uncheckedTrackColor = SurfaceLightColor
                        )
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Participant Switch
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .background(PrimaryBackground, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("PARTICIPANTS", color = AccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Text(
                            text = if (flag.participantEnabled && flag.globalOverrideEnabled) "ENABLED" else "DISABLED",
                            color = if (flag.participantEnabled && flag.globalOverrideEnabled) NeonEmerald else TextSecondary,
                            fontSize = 9.sp
                        )
                    }
                    Switch(
                        checked = flag.participantEnabled,
                        enabled = flag.globalOverrideEnabled,
                        onCheckedChange = onToggleParticipant,
                        modifier = Modifier.testTag("switch_participant_${flag.flagKey}"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AccentBlue,
                            uncheckedThumbColor = Color.LightGray,
                            uncheckedTrackColor = SurfaceLightColor
                        )
                    )
                }
            }

            if (flag.lastModifiedAt > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, null, tint = TextSecondary, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Last modified by ${flag.lastModifiedByAdminId.take(12).ifBlank { "admin" }} at ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(java.util.Date(flag.lastModifiedAt))}",
                        color = TextSecondary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}


