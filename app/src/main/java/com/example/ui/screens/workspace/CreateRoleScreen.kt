package com.example.ui.screens.workspace

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.testTag
import com.example.data.model.UserProfile
import com.example.ui.feedback.FeedbackManager
import com.example.ui.theme.*

@Composable
fun CreateRoleScreen(
    discoveryViewModel: com.example.ui.viewmodels.DiscoveryViewModel,
    authViewModel: com.example.ui.viewmodels.AuthViewModel,
    userProfile: UserProfile?,
    onBack: () -> Unit
) {
    val currentUserId by authViewModel.currentUserId.collectAsState()
    var roleName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var requirements by remember { mutableStateOf("") }
    var workType by remember { mutableStateOf("Full Time") }

    var boostOpportunity by remember { mutableStateOf(false) }
    var showBillingDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("create_role_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("Create Opportunity", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
            Button(
                onClick = {
                    discoveryViewModel.submitProjectProposal(
                        title = roleName,
                        niche = requirements,
                        brief = description,
                        user = userProfile,
                        userId = currentUserId ?: "unknown",
                        isBoosted = boostOpportunity,
                        compensationType = workType
                    )
                    FeedbackManager.showSuccess("Opportunity posted successfully!")
                    onBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                shape = RoundedCornerShape(8.dp),
                enabled = roleName.isNotBlank() && description.isNotBlank()
            ) {
                Text("Post Opportunity")
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Text("OPPORTUNITY DETAILS", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = roleName,
                    onValueChange = { roleName = it },
                    label = { Text("Role / Opportunity Title") },
                    placeholder = { Text("e.g. Lead Editor for Gaming Channel") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentRed,
                        unfocusedBorderColor = ColorDivider,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Opportunity Description") },
                    placeholder = { Text("Describe the responsibilities and vision...") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentRed,
                        unfocusedBorderColor = ColorDivider,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 5
                )
            }

            item {
                Text("REQUIREMENTS", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = requirements,
                    onValueChange = { requirements = it },
                    label = { Text("Skills (comma separated)") },
                    placeholder = { Text("e.g. Premiere Pro, After Effects, Thumbnail Design") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentRed,
                        unfocusedBorderColor = ColorDivider,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                Text("TEAM ROLE FRAMEWORK / WORK TYPE", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val options = listOf("Full Time", "Contract", "Rev Share", "One-off Task")
                    options.forEach { opt ->
                        ChoiceChip(
                            text = opt, 
                            selected = workType == opt,
                            onClick = { workType = opt }
                        )
                    }
                }
            }

            item {
                Text("MONETIZATION & PROMOTION", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (!boostOpportunity) {
                                showBillingDialog = true
                            } else {
                                boostOpportunity = false
                            }
                        }
                        .testTag("boost_toggle_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (boostOpportunity) AccentBlue.copy(alpha = 0.15f) else SurfaceColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (boostOpportunity) AccentBlue else ColorDivider)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Boost",
                                tint = if (boostOpportunity) AccentBlue else TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Boost Opportunity (48 Hours)",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (boostOpportunity) "✓ Play Billing purchase successful! Active upon posting." else "Feature on top of discovery feeds instantly. ($4.99 SKU)",
                                    color = if (boostOpportunity) NeonEmerald else TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Switch(
                            checked = boostOpportunity,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    showBillingDialog = true
                                } else {
                                    boostOpportunity = false
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PrimaryBackground,
                                checkedTrackColor = AccentBlue,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = SurfaceColor
                            ),
                            modifier = Modifier.testTag("boost_switch_input")
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChoiceChip(text: String, selected: Boolean, onClick: () -> Unit = {}) {
    Surface(
        color = if (selected) AccentRed.copy(alpha = 0.2f) else SurfaceColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (selected) AccentRed else ColorDivider),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            color = if (selected) AccentRed else TextSecondary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
