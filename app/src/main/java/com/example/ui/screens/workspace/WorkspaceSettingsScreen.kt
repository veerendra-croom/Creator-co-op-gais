package com.example.ui.screens.workspace

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodels.WorkspaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceSettingsScreen(
    workspaceId: String,
    viewModel: WorkspaceViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val workspace by viewModel.selectedWorkspace.collectAsState()
    val members by viewModel.activeWorkspaceMembers.collectAsState()
    val agreement by viewModel.activeAgreement.collectAsState()
    val tasks by viewModel.activeTasks.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Workspace Settings", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                SettingsSectionTitle("GENERAL")
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Default.Edit,
                        title = "Workspace Profile",
                        subtitle = "Name, Description, Banner",
                        onClick = { Toast.makeText(context, "Workspace Profile customization is managed via Admin portal.", Toast.LENGTH_SHORT).show() }
                    )
                    HorizontalDivider(color = ColorDivider)
                    SettingsRow(
                        icon = Icons.Default.Public,
                        title = "Visibility",
                        subtitle = "Public (Listed in Discovery)",
                        onClick = { Toast.makeText(context, "Visibility is currently locked to active co-op scope.", Toast.LENGTH_SHORT).show() }
                    )
                }
            }

            item {
                SettingsSectionTitle("PERMISSIONS & ROLES")
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Default.AdminPanelSettings,
                        title = "Member Roles",
                        subtitle = "Manage Admins, Editors, Viewers",
                        onClick = { Toast.makeText(context, "Role allocations are governed by signed co-op contracts.", Toast.LENGTH_SHORT).show() }
                    )
                    HorizontalDivider(color = ColorDivider)
                    SettingsRow(
                        icon = Icons.Default.Lock,
                        title = "Access Restrictions",
                        subtitle = "Require Agreement Signing",
                        onClick = { Toast.makeText(context, "Mutual contracts are permanently active.", Toast.LENGTH_SHORT).show() }
                    )
                }
            }

            item {
                SettingsSectionTitle("EXPORT & COMPLIANCE")
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Default.PictureAsPdf,
                        title = "Export Workspace Blueprint",
                        subtitle = "Generate a formatted compliance summary and contract PDF",
                        onClick = {
                            val ws = workspace
                            if (ws != null) {
                                DocumentPrintHelper.printWorkspaceBlueprint(
                                    context = context,
                                    workspace = ws,
                                    agreement = agreement,
                                    tasks = tasks,
                                    members = members
                                )
                            } else {
                                Toast.makeText(context, "Workspace data is still loading...", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }

            item {
                SettingsSectionTitle("PREFERENCES")
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Default.Notifications,
                        title = "Notifications",
                        subtitle = "Configure alerts for this workspace",
                        onClick = { Toast.makeText(context, "Real-time sync push alerts are enabled.", Toast.LENGTH_SHORT).show() }
                    )
                    HorizontalDivider(color = ColorDivider)
                    SettingsRow(
                        icon = Icons.Default.Archive,
                        title = "Archive Workspace",
                        subtitle = "Hide from active lists",
                        isDestructive = true,
                        onClick = { Toast.makeText(context, "Workspace archive states can be finalized by co-op heads.", Toast.LENGTH_SHORT).show() }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        color = AccentRed,
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = BorderStroke(1.dp, ColorDivider),
        content = content
    )
}

@Composable
fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isDestructive: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val contentColor = if (isDestructive) MaterialTheme.colorScheme.error else Color.White
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(24.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = contentColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = TextSecondary, fontSize = 11.sp)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
    }
}
