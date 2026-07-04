package com.example.ui.screens.workspace

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodels.GlobalViewModel
import com.example.ui.viewmodels.WorkspaceViewModel

@Composable
fun WorkspaceInvitationScreen(
    globalViewModel: GlobalViewModel,
    workspaceViewModel: WorkspaceViewModel,
    onBack: () -> Unit
) {
    val pendingWsId by globalViewModel.pendingInvitationWorkspaceId.collectAsState()
    val workspaces by workspaceViewModel.workspaces.collectAsState()
    
    // Fallback or fetched workspace
    val targetWs = remember(pendingWsId, workspaces) {
        workspaces.find { it.id == pendingWsId }
    }
    
    val workspaceName = targetWs?.name ?: "TechPulse Main Channel"
    val workspacePlatform = targetWs?.platformType ?: "YOUTUBE"
    val workspaceId = targetWs?.id ?: "ws_youtube_main"
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(80.dp).clip(CircleShape).background(AccentBlue.copy(alpha = 0.2f)).border(2.dp, AccentBlue, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.FolderSpecial, null, tint = AccentBlue, modifier = Modifier.size(40.dp))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("You have been invited to join", color = TextSecondary, fontSize = 14.sp)
        Text(workspaceName, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text("Invited to Collaborate on $workspacePlatform", color = AccentBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, ColorDivider)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("WORKSPACE DETAILS", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Text("Access real-time production boards, shared content calendars, automated agreements, and team communication shards.", color = Color.White, fontSize = 14.sp, lineHeight = 20.sp)
                Divider(color = ColorDivider)
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Role Offered", color = TextSecondary, fontSize = 13.sp)
                    Text("Collaborator", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                workspaceViewModel.joinWorkspace(workspaceId, "me")
                workspaceViewModel.selectedWorkspaceId.value = workspaceId
                workspaceViewModel.workspaceViewMode.value = "VIEW"
                globalViewModel.pendingInvitationWorkspaceId.value = null
                globalViewModel.currentTab.value = "WORKSPACES"
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Accept Invitation & Join Team", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = {
            globalViewModel.pendingInvitationWorkspaceId.value = null
            onBack()
        }) {
            Text("Decline", color = TextSecondary)
        }
    }
}
