package com.example.ui.screens.workspace

import androidx.compose.foundation.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.ui.components.*
import com.example.ui.viewmodels.WorkspaceViewModel

@Composable
fun PersonalSpaceScreen(viewModel: WorkspaceViewModel, workspaceId: String, userId: String) {
    val tasks by viewModel.activeTasks.collectAsState()
    val sandboxTasks = tasks.filter { it.stateScope == "ROUGH_SANDBOX" }
    val isAISummarizing by viewModel.isAISummarizing.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var draftTitle by remember { mutableStateOf("") }
    var draftBody by remember { mutableStateOf("") }
    var draftCategory by remember { mutableStateOf("Hook Idea") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
    ) {
        // Safe private sandbox banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceColor)
                .border(width = 1.dp, color = ColorDivider.copy(alpha = 0.3f))
                .padding(DS.Space16)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AccentBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = AccentBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(DS.Space12))
                Column {
                    Text(
                        text = "PERSONAL DRAFTING PAD",
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Encrypted private drafts. Promote to the Team Space to collaborate.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        
        if (sandboxTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(DS.Space24),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    message = "Your sandbox drafting pad is currently empty.",
                    icon = Icons.Default.NoteAdd,
                    actionText = "Create Private Concept Draft",
                    onAction = {
                        draftTitle = ""
                        draftBody = ""
                        draftCategory = "Hook Idea"
                        showAddDialog = true
                    }
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = DS.Space16),
                contentPadding = PaddingValues(vertical = DS.Space16),
                verticalArrangement = Arrangement.spacedBy(DS.Space16)
            ) {
                items(sandboxTasks) { task ->
                    val categoryText = if (task.title.contains("[")) {
                        task.title.substringBefore("]").removePrefix("[")
                    } else {
                        "CONCEPT"
                    }
                    val categoryColor = when(categoryText.trim().uppercase()) {
                        "HOOK IDEA" -> NeonEmerald
                        "SCRIPT BLUEPRINT" -> CrispAmber
                        "VISUAL GUIDE" -> AccentBlue
                        "THUMBNAIL PLAN" -> AccentRed
                        else -> AccentBlue
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                        shape = DS.RadiusMedium,
                        border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(DS.Space16)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = categoryColor.copy(alpha = 0.15f),
                                    contentColor = categoryColor,
                                    shape = DS.RadiusSmall,
                                    border = BorderStroke(1.dp, categoryColor.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = categoryText.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = DS.Space8, vertical = DS.Space4),
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                
                                IconButton(
                                    onClick = { viewModel.deleteTask(task.id, userId) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Draft",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(DS.Space8))
                            
                            val displayTitle = if (task.title.contains("]")) {
                                task.title.substringAfter("]").trim()
                            } else {
                                task.title
                             }
                            
                            Text(
                                text = displayTitle,
                                color = Color.White,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(DS.Space4))
                            
                            Text(
                                text = task.contentBody,
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 18.sp
                            )
                            
                            Spacer(modifier = Modifier.height(DS.Space12))
                            HorizontalDivider(color = ColorDivider.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(DS.Space12))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = AccentBlue,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(DS.Space4))
                                    Text(
                                        text = "Private Draft",
                                        color = AccentBlue,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(DS.Space8),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { viewModel.promoteTaskToProduction(task.id, userId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                                        shape = DS.RadiusSmall,
                                        contentPadding = PaddingValues(horizontal = DS.Space12, vertical = DS.Space6),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text(
                                            text = "PROMOTE",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Button(
                                        onClick = { viewModel.promoteTaskToProductionWithAI(task.id, userId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                                        shape = DS.RadiusSmall,
                                        contentPadding = PaddingValues(horizontal = DS.Space12, vertical = DS.Space6),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(DS.Space4))
                                        Text(
                                            text = "AI SYNTHESIZE",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DS.Space16)
        ) {
            Button(
                onClick = { 
                    draftTitle = ""
                    draftBody = ""
                    draftCategory = "Hook Idea"
                    showAddDialog = true 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.06f)),
                shape = DS.RadiusMedium,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = Icons.Default.NoteAdd,
                    contentDescription = "New Draft",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(DS.Space8))
                Text(
                    text = "Add Private Concept Draft",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    
    if (isAISummarizing) {
        Dialog(onDismissRequest = {}) {
            Surface(
                color = SurfaceColor,
                shape = DS.RadiusLarge,
                border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.5f)),
                modifier = Modifier.padding(DS.Space16).fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(DS.Space24),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(DS.Space16)
                ) {
                    CircularProgressIndicator(color = AccentBlue)
                    Text(
                        text = "GEMINI CO-PILOT ENGAGED",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Synthesizing your raw concept notes, scheduling hooks, and designing complete interactive tasks onto the Kanban board...",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
    
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = SurfaceColor,
            title = {
                Text(
                    text = "New Private Sandbox Draft",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(DS.Space16),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Secure local drafting pad. Promote with or without Gemini AI orchestration.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    OutlinedTextField(
                        value = draftTitle,
                        onValueChange = { draftTitle = it },
                        label = { Text("Draft Title", style = MaterialTheme.typography.bodyMedium) },
                        placeholder = { Text("e.g. 10 Critical Hooks for Editing", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = ColorDivider.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = draftBody,
                        onValueChange = { draftBody = it },
                        label = { Text("Raw Notes & Outline Beats", style = MaterialTheme.typography.bodyMedium) },
                        placeholder = { Text("List hooks, narrative visual guidelines, camera pacing...", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = ColorDivider.copy(alpha = 0.5f)
                        )
                    )
                    
                    Text(
                        text = "CONCEPT TYPE",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    val categories = listOf("Hook Idea", "Script Blueprint", "Visual Guide", "Thumbnail Plan")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(DS.Space6)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = draftCategory == cat
                            Surface(
                                modifier = Modifier.clickable { draftCategory = cat },
                                color = if (isSelected) AccentBlue.copy(alpha = 0.15f) else Color.Transparent,
                                shape = DS.RadiusSmall,
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) AccentBlue else ColorDivider.copy(alpha = 0.5f)
                                )
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSelected) Color.White else TextSecondary,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = DS.Space12, vertical = DS.Space6)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        val formattedTitle = "[$draftCategory] $draftTitle"
                        viewModel.submitTask(formattedTitle, draftBody, "ROUGH_SANDBOX", userId)
                        showAddDialog = false 
                    },
                    enabled = draftTitle.isNotBlank() && draftBody.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = DS.RadiusMedium
                ) {
                    Text("Secure Draft", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { 
                    Text("Cancel", color = TextSecondary) 
                }
            }
        )
    }
}
