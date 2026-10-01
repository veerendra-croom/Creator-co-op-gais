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
import kotlinx.coroutines.launch

@Composable
fun WorkspaceInvitationScreen(
    globalViewModel: GlobalViewModel,
    workspaceViewModel: WorkspaceViewModel,
    onBack: () -> Unit
) {
    val pendingWsId by globalViewModel.pendingInvitationWorkspaceId.collectAsState()
    val workspaces by workspaceViewModel.workspaces.collectAsState()
    val scope = rememberCoroutineScope()
    
    var inviteCode by remember { mutableStateOf("") }
    var inviteDetails by remember { mutableStateOf<com.example.data.model.WorkspaceInviteEntity?>(null) }
    var inviteError by remember { mutableStateOf<String?>(null) }
    
    // Automatically prefill if we received a deep link style code in pendingWsId
    LaunchedEffect(pendingWsId) {
        if (pendingWsId != null && pendingWsId!!.startsWith("COOP-")) {
            inviteCode = pendingWsId!!
        }
    }
    
    // Validate invite code reactively
    LaunchedEffect(inviteCode) {
        val trimmed = inviteCode.trim().uppercase()
        if (trimmed.startsWith("COOP-") && trimmed.length >= 10) {
            val result = workspaceViewModel.repository.getInviteByCode(trimmed)
            if (result != null) {
                // Compute and verify SHA-256 token
                val digest = java.security.MessageDigest.getInstance("SHA-256").digest(trimmed.toByteArray())
                val sha256Token = digest.joinToString("") { "%02x".format(it) }
                
                if (sha256Token != result.sha256Token) {
                    inviteError = "Token payload mismatch! Security violation detected."
                    inviteDetails = null
                } else if (result.isUsed) {
                    inviteError = "This invitation code has already been used!"
                    inviteDetails = null
                } else if (result.expirationTimestamp < System.currentTimeMillis()) {
                    inviteError = "This invitation has expired!"
                    inviteDetails = null
                } else {
                    inviteError = null
                    inviteDetails = result
                }
            } else {
                inviteError = "Invitation code not found in security database."
                inviteDetails = null
            }
        } else {
            inviteDetails = null
            inviteError = null
        }
    }
    
    // Fallback or fetched workspace based on invite details or pendingWsId
    val targetWs = remember(pendingWsId, inviteDetails, workspaces) {
        val targetId = inviteDetails?.workspaceId ?: pendingWsId
        workspaces.find { it.id == targetId }
    }
    
    val workspaceName = targetWs?.name ?: "TechPulse Main Channel"
    val workspacePlatform = targetWs?.platformType ?: "YOUTUBE"
    val workspaceId = targetWs?.id ?: "ws_youtube_main"
    val assignedRole = inviteDetails?.roleTitle ?: "Co-Op Collaborator"
    
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
        
        Text("SECURE DEEP LINK INVITATION", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text(workspaceName, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text("Enclave Node: $workspacePlatform", color = AccentBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(24.dp))

        // Invite code text field
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Enter Security Invite Code", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = inviteCode,
                onValueChange = { inviteCode = it },
                placeholder = { Text("e.g. COOP-AB12CD", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = if (inviteDetails != null) NeonEmerald else if (inviteError != null) AccentRed else AccentBlue,
                    unfocusedBorderColor = ColorDivider
                )
            )
            if (inviteError != null) {
                Text(inviteError!!, color = AccentRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            } else if (inviteDetails != null) {
                Text("✓ Secure invitation validated successfully via SHA-256 protocol.", color = NeonEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, ColorDivider)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("WORKSPACE ACCESS LEVEL", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Text("Access real-time production boards, shared content calendars, automated agreements, and team communication shards.", color = Color.White, fontSize = 13.sp, lineHeight = 18.sp)
                Divider(color = ColorDivider)
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Role Offered", color = TextSecondary, fontSize = 13.sp)
                    Text(assignedRole, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                if (inviteDetails != null) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Expiration Window", color = TextSecondary, fontSize = 13.sp)
                        val relativeText = com.example.util.DateTimeUtils.getRelativeTimeSpanString(inviteDetails!!.expirationTimestamp)
                        Text(relativeText, color = CrispAmber, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (inviteDetails != null) {
                    workspaceViewModel.acceptInviteByCode(inviteCode.trim().uppercase(), "me") {
                        workspaceViewModel.selectedWorkspaceId.value = inviteDetails!!.workspaceId
                        workspaceViewModel.workspaceViewMode.value = "VIEW"
                        globalViewModel.pendingInvitationWorkspaceId.value = null
                        globalViewModel.currentTab.value = "WORKSPACES"
                    }
                } else {
                    // Fallback direct join (backward-compatible)
                    workspaceViewModel.joinWorkspace(workspaceId, "me", assignedRole)
                    workspaceViewModel.selectedWorkspaceId.value = workspaceId
                    workspaceViewModel.workspaceViewMode.value = "VIEW"
                    globalViewModel.pendingInvitationWorkspaceId.value = null
                    globalViewModel.currentTab.value = "WORKSPACES"
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (inviteDetails != null) NeonEmerald else AccentRed),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (inviteDetails != null) "Accept Protocol & Join" else "Direct Connect (No Code)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = {
            globalViewModel.pendingInvitationWorkspaceId.value = null
            onBack()
        }) {
            Text("Decline Protocol", color = TextSecondary)
        }
    }
}
