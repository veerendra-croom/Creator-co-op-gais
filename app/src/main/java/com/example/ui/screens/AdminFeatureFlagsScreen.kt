package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
    var showToggleConfirm by remember { mutableStateOf<FeatureFlag?>(null) }
    var reason by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp)
                .testTag("feature_flags_dialog_card"),
            colors = CardDefaults.cardColors(containerColor = PrimaryBackground),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("PLATFORM FEATURE FLAGS", fontWeight = FontWeight.Black, color = Color.White, fontSize = 16.sp)
                        Text("Enable or disable features instantly at runtime", color = TextSecondary, fontSize = 11.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(flags) { flag ->
                        FeatureFlagItem(
                            flag = flag,
                            onToggleRequest = { showToggleConfirm = it }
                        )
                    }
                }
            }
        }
    }

    if (showToggleConfirm != null) {
        AlertDialog(
            onDismissRequest = { showToggleConfirm = null; reason = "" },
            containerColor = SurfaceColor,
            title = { Text(if (showToggleConfirm!!.isEnabled) "Disable Feature?" else "Enable Feature?", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Identify why this change is being made (Audit Log):", fontSize = 12.sp, color = TextSecondary)
                    TextField(
                        value = reason,
                        onValueChange = { reason = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("E.g. Hotpatching, beta testing...") },
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
                        adminViewModel.toggleFeatureFlag(showToggleConfirm!!, adminId, reason)
                        showToggleConfirm = null
                        reason = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (showToggleConfirm!!.isEnabled) AccentRed else AccentBlue),
                    enabled = reason.isNotBlank(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Confirm Change", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showToggleConfirm = null; reason = "" }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun FeatureFlagItem(
    flag: FeatureFlag,
    onToggleRequest: (FeatureFlag) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("feature_flag_item_${flag.flagKey}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (flag.isEnabled) AccentBlue.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (flag.isEnabled) Icons.Default.Bolt else Icons.Default.Block,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (flag.isEnabled) AccentBlue else Color.Gray
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(flag.flagKey, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(flag.description, color = TextSecondary, fontSize = 11.sp, lineHeight = 14.sp)
                }
                Switch(
                    checked = flag.isEnabled,
                    onCheckedChange = { onToggleRequest(flag) },
                    modifier = Modifier.testTag("feature_flag_switch_${flag.flagKey}"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AccentBlue,
                        checkedTrackColor = AccentBlue.copy(alpha = 0.5f)
                    )
                )
            }
            
            if (flag.lastModifiedAt > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = ColorDivider, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, null, tint = TextSecondary, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Last modified by ${flag.lastModifiedByAdminId?.take(8) ?: "Unknown"} at ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(java.util.Date(flag.lastModifiedAt))}",
                        color = TextSecondary,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}
