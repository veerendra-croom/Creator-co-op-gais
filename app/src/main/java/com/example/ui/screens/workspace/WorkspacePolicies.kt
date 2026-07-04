package com.example.ui.screens.workspace

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WorkspacePolicyConfig
import com.example.ui.theme.*
import com.example.ui.viewmodels.WorkspaceViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WorkspacePolicies(viewModel: WorkspaceViewModel, workspaceId: String) {
    val policiesState by viewModel.workspacePolicies.collectAsState()
    val config = policiesState[workspaceId] ?: WorkspacePolicyConfig()
    
    var govRule by remember(workspaceId) { mutableStateOf(config.governanceRule) }
    var reviewStd by remember(workspaceId) { mutableStateOf(config.reviewStandard) }
    
    var webhookUrl by remember(workspaceId) { mutableStateOf(config.discordWebhookUrl) }
    var ytApiKey by remember(workspaceId) { mutableStateOf(config.youtubeApiKey) }
    var igToken by remember(workspaceId) { mutableStateOf(config.instagramAuthToken) }
    
    var autoApprove by remember(workspaceId) { mutableStateOf(config.autoApprovePitch) }
    var minKarma by remember(workspaceId) { mutableStateOf(config.minKarmaRequired.toFloat()) }
    var minAvail by remember(workspaceId) { mutableStateOf(config.minAvailabilityHours.toFloat()) }

    var isSimulatingHandshake by remember { mutableStateOf(false) }
    var handshakeStatus by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text("CO-OP WORKSPACE GENERAL POLICIES", fontSize = 10.sp, fontWeight = FontWeight.Black, color = AccentRed, letterSpacing = 1.sp)
            Text("Workspace Customisation Engine", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Program governance parameters, team agreement defaults, onboarding screening gates, and workspace preferences.", fontSize = 11.sp, color = TextSecondary, lineHeight = 16.sp)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            border = BorderStroke(1.dp, ColorDivider),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Description, null, tint = AccentRed, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Team Agreement Revision Policy", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                }

                val models = listOf(
                    "MAJORITY" to Pair("Majority Acknowledgment (51%+)", "New versions require a simple majority of active members to acknowledge."),
                    "UNANIMOUS" to Pair("Unanimous Consensus (100%)", "Requires every single active workspace member to acknowledge."),
                    "LEAD_ONLY" to Pair("Lead Creator Revision (Veto)", "The workspace founder can issue new versions immediately.")
                )

                models.forEach { item ->
                    val isSelected = govRule == item.first
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) AccentRed.copy(alpha = 0.08f) else Color.Transparent)
                            .border(1.dp, if (isSelected) AccentRed.copy(alpha = 0.4f) else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable {
                                govRule = item.first
                                viewModel.updateWorkspacePolicy(workspaceId, config.copy(governanceRule = item.first))
                            }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = isSelected, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = AccentRed))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(item.second.first, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else TextSecondary)
                            Text(item.second.second, fontSize = 9.sp, color = TextSecondary.copy(alpha = 0.7f), lineHeight = 12.sp)
                        }
                    }
                }
            }
        }

        // Additional simplified policy cards...
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceColor), border = BorderStroke(1.dp, ColorDivider), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FilterList, null, tint = AccentRed, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Auto-Screening Criteria Gate", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Permit Pitch Auto-Approval", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Switch(checked = autoApprove, onCheckedChange = { autoApprove = it; viewModel.updateWorkspacePolicy(workspaceId, config.copy(autoApprovePitch = it)) }, colors = SwitchDefaults.colors(checkedThumbColor = AccentRed))
                }

                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Minimum Co-op Karma Limit", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Text("${minKarma.toInt()}% Quality", fontSize = 11.sp, fontWeight = FontWeight.Black, color = AccentBlue)
                    }
                    Slider(value = minKarma, onValueChange = { minKarma = it; viewModel.updateWorkspacePolicy(workspaceId, config.copy(minKarmaRequired = it.toInt())) }, valueRange = 50f..100f, colors = SliderDefaults.colors(thumbColor = AccentRed, activeTrackColor = AccentRed))
                }
            }
        }

        Button(
            onClick = {
                coroutineScope.launch {
                    isSimulatingHandshake = true
                    handshakeStatus = "Initiating handshake..."
                    delay(1000)
                    handshakeStatus = "API Credentials Verified."
                    delay(1000)
                    isSimulatingHandshake = false
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (isSimulatingHandshake) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            else Text("Test API Connections", fontWeight = FontWeight.Bold)
        }
        
        if (handshakeStatus != null) {
            Text(handshakeStatus!!, color = AccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
