package com.example.ui.screens.workspace

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceSettingsScreen(
    workspaceId: String,
    onBack: () -> Unit
) {
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
                    SettingsRow(icon = Icons.Default.Edit, title = "Workspace Profile", subtitle = "Name, Description, Banner")
                    Divider(color = ColorDivider)
                    SettingsRow(icon = Icons.Default.Public, title = "Visibility", subtitle = "Public (Listed in Discovery)")
                }
            }

            item {
                SettingsSectionTitle("PERMISSIONS & ROLES")
                SettingsCard {
                    SettingsRow(icon = Icons.Default.AdminPanelSettings, title = "Member Roles", subtitle = "Manage Admins, Editors, Viewers")
                    Divider(color = ColorDivider)
                    SettingsRow(icon = Icons.Default.Lock, title = "Access Restrictions", subtitle = "Require Agreement Signing")
                }
            }

            item {
                SettingsSectionTitle("PREFERENCES")
                SettingsCard {
                    SettingsRow(icon = Icons.Default.Notifications, title = "Notifications", subtitle = "Configure alerts for this workspace")
                    Divider(color = ColorDivider)
                    SettingsRow(icon = Icons.Default.Archive, title = "Archive Workspace", subtitle = "Hide from active lists", isDestructive = true)
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
    isDestructive: Boolean = false
) {
    val contentColor = if (isDestructive) MaterialTheme.colorScheme.error else Color.White
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(24.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = contentColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, color = TextSecondary, fontSize = 12.sp)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
    }
}
