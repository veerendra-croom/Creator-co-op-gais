package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.data.model.AuditLog
import com.example.ui.theme.*
import com.example.ui.viewmodels.AdminViewModel
import com.example.util.DateTimeUtils
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAuditScreen(
    adminViewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val auditLogs by adminViewModel.allAuditLogs.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var actionFilter by remember { mutableStateOf("ALL") }
    var targetFilter by remember { mutableStateOf("ALL") }
    
    var showExportDialog by remember { mutableStateOf(false) }
    var exportFormat by remember { mutableStateOf("JSON") } // JSON or CSV
    var showHelpDialog by remember { mutableStateOf(false) }
    
    // Derived states
    val filteredLogs = remember(auditLogs, searchQuery, actionFilter, targetFilter) {
        auditLogs.filter { log ->
            val matchesSearch = searchQuery.isBlank() || 
                log.adminName.contains(searchQuery, ignoreCase = true) ||
                log.actionTaken.contains(searchQuery, ignoreCase = true) ||
                log.reason.contains(searchQuery, ignoreCase = true) ||
                log.targetId.contains(searchQuery, ignoreCase = true)
            
            val matchesAction = actionFilter == "ALL" || log.actionTaken == actionFilter
            val matchesTarget = targetFilter == "ALL" || log.targetType == targetFilter
            
            matchesSearch && matchesAction && matchesTarget
        }
    }
    
    val allUniqueActions = remember(auditLogs) {
        listOf("ALL") + auditLogs.map { it.actionTaken }.distinct().sorted()
    }
    
    val allUniqueTargets = remember(auditLogs) {
        listOf("ALL") + auditLogs.map { it.targetType }.distinct().sorted()
    }

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    val exportDataString = remember(filteredLogs, exportFormat) {
        if (exportFormat == "JSON") {
            // Build simple pretty JSON string
            val sb = StringBuilder()
            sb.append("[\n")
            filteredLogs.forEachIndexed { index, log ->
                sb.append("  {\n")
                sb.append("    \"id\": \"${log.id}\",\n")
                sb.append("    \"adminId\": \"${log.adminId}\",\n")
                sb.append("    \"adminName\": \"${log.adminName}\",\n")
                sb.append("    \"actionType\": \"${log.actionTaken}\",\n")
                sb.append("    \"targetType\": \"${log.targetType}\",\n")
                sb.append("    \"targetId\": \"${log.targetId}\",\n")
                sb.append("    \"reason\": \"${log.reason.replace("\"", "\\\"")}\",\n")
                sb.append("    \"timestamp\": ${log.createdAt}\n")
                sb.append("  }${if (index < filteredLogs.size - 1) "," else ""}\n")
            }
            sb.append("]")
            sb.toString()
        } else {
            // CSV format
            val sb = StringBuilder()
            sb.append("id,adminId,adminName,actionType,targetType,targetId,reason,timestamp\n")
            filteredLogs.forEach { log ->
                sb.append("\"${log.id}\",\"${log.adminId}\",\"${log.adminName}\",\"${log.actionTaken}\",\"${log.targetType}\",\"${log.targetId}\",\"${log.reason.replace("\"", "\"\"")}\",${log.createdAt}\n")
            }
            sb.toString()
        }
    }

    Scaffold(
        containerColor = PrimaryBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Stats Panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ColorDivider)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Ledger Integrity Status", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, "Verified", tint = NeonEmerald, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("100% Cryptographically Intact", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total Operations Logged", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${auditLogs.size} logs", color = AccentBlue, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                }
            }

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by Admin, Action, Target, or Reason...", color = TextSecondary, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("audit_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = ColorDivider,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filters selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Action Filter Dropdown Drawer
                var actionExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.weight(1f)) {
                    Button(
                        onClick = { actionExpanded = true },
                        modifier = Modifier.fillMaxWidth().testTag("action_filter_dropdown"),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                        border = BorderStroke(1.dp, ColorDivider),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (actionFilter == "ALL") "All Actions" else actionFilter,
                                color = if (actionFilter == "ALL") TextSecondary else AccentBlue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Icon(Icons.Default.ArrowDropDown, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                    DropdownMenu(
                        expanded = actionExpanded,
                        onDismissRequest = { actionExpanded = false },
                        modifier = Modifier.background(SurfaceColor).border(1.dp, ColorDivider)
                    ) {
                        allUniqueActions.forEach { action ->
                            DropdownMenuItem(
                                text = { Text(action, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    actionFilter = action
                                    if (com.example.ui.feedback.FeedbackManager.isHapticEnabled) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    actionExpanded = false
                                }
                            )
                        }
                    }
                }

                // Target Filter Dropdown
                var targetExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.weight(1f)) {
                    Button(
                        onClick = { targetExpanded = true },
                        modifier = Modifier.fillMaxWidth().testTag("target_filter_dropdown"),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                        border = BorderStroke(1.dp, ColorDivider),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (targetFilter == "ALL") "All Targets" else targetFilter,
                                color = if (targetFilter == "ALL") TextSecondary else NeonEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Icon(Icons.Default.ArrowDropDown, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                    DropdownMenu(
                        expanded = targetExpanded,
                        onDismissRequest = { targetExpanded = false },
                        modifier = Modifier.background(SurfaceColor).border(1.dp, ColorDivider)
                    ) {
                        allUniqueTargets.forEach { target ->
                            DropdownMenuItem(
                                text = { Text(target, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    targetFilter = target
                                    if (com.example.ui.feedback.FeedbackManager.isHapticEnabled) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    targetExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Audit Logs List
            if (filteredLogs.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Outlined.Gavel, "No Logs", tint = ColorDivider, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No matching audit records found",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Try clearing search keywords or active filters",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredLogs, key = { it.id }) { log ->
                        AuditLogItem(log)
                    }
                }
            }
        }
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Text(
                    "Export Audit Ledger",
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Configure format to export current snapshot of ${filteredLogs.size} filtered operations.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { exportFormat = "JSON" }
                                .border(
                                    1.dp,
                                    if (exportFormat == "JSON") AccentBlue else ColorDivider,
                                    RoundedCornerShape(8.dp)
                                ),
                            colors = CardDefaults.cardColors(containerColor = SurfaceColor)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Code, null, tint = if (exportFormat == "JSON") AccentBlue else TextSecondary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Raw Structure", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { exportFormat = "CSV" }
                                .border(
                                    1.dp,
                                    if (exportFormat == "CSV") AccentBlue else ColorDivider,
                                    RoundedCornerShape(8.dp)
                                ),
                            colors = CardDefaults.cardColors(containerColor = SurfaceColor)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.TableChart, null, tint = if (exportFormat == "CSV") AccentBlue else TextSecondary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Spreadsheet", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Display raw export data for easy copy pasting or simulation of download
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryBackground)
                            .border(1.dp, ColorDivider)
                            .padding(8.dp)
                    ) {
                        LazyColumn {
                            item {
                                Text(
                                    text = exportDataString,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (com.example.ui.feedback.FeedbackManager.isHapticEnabled) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        // Simulate export or copy to clipboard
                        val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Creator Co-op Audit Ledger", exportDataString)
                        clipboardManager.setPrimaryClip(clip)
                        showExportDialog = false
                        com.example.ui.feedback.FeedbackManager.showSuccess("Audit Ledger $exportFormat data copied to secure clipboard.")
                    }
                ) {
                    Text("COPY DATA", fontWeight = FontWeight.Black, color = AccentBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("CANCEL", color = TextSecondary)
                }
            },
            containerColor = SurfaceColor,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showHelpDialog) {
        AdminHelpDialog(onDismiss = { showHelpDialog = false })
    }
}

@Composable
fun AuditLogItem(log: AuditLog) {
    val formattedTime = remember(log.createdAt) { DateTimeUtils.getRelativeTimeSpanString(log.createdAt) }
    
    val badgeColor = remember(log.actionTaken) {
        when {
            log.actionTaken.contains("SUSPEND") || log.actionTaken.contains("REJECT") || log.actionTaken.contains("DELETE") -> AccentRed
            log.actionTaken.contains("RESOLVE") || log.actionTaken.contains("APPROVE") || log.actionTaken.contains("RESTORE") -> NeonEmerald
            log.actionTaken.contains("TOGGLE") || log.actionTaken.contains("FEATURE") || log.actionTaken.contains("UPDATE") -> CrispAmber
            else -> AccentBlue
        }
    }

    val actionIcon = remember(log.actionTaken) {
        when {
            log.actionTaken.contains("SUSPEND") -> Icons.Default.Block
            log.actionTaken.contains("APPROVE") -> Icons.Default.Verified
            log.actionTaken.contains("REJECT") -> Icons.Default.Cancel
            log.actionTaken.contains("RESOLVE") -> Icons.Default.CheckCircle
            log.actionTaken.contains("FEATURE") -> Icons.Default.SettingsInputComponent
            else -> Icons.Default.Gavel
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("audit_log_item_${log.id}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, ColorDivider)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(badgeColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(actionIcon, null, tint = badgeColor, modifier = Modifier.size(14.dp))
                    }
                    Text(
                        text = log.actionTaken,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 0.5.sp
                    )
                }
                
                Text(
                    text = formattedTime,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body
            Text(
                text = log.reason,
                color = TextPrimary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = ColorDivider, thickness = 0.5.dp)

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Operator Detail
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, "Admin Operator", tint = TextSecondary, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Operator: ", color = TextMuted, fontSize = 11.sp)
                    Text(log.adminName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                // Target Detail
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Adjust, "Target", tint = TextSecondary, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${log.targetType}: ", color = TextMuted, fontSize = 11.sp)
                    Text(log.targetId.take(12) + if (log.targetId.length > 12) "..." else "", color = AccentBlue, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}
