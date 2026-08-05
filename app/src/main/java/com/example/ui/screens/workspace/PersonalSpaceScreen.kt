package com.example.ui.screens.workspace

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.*
import com.example.ui.components.*
import com.example.ui.viewmodels.WorkspaceViewModel

@Composable
fun PersonalSpaceScreen(viewModel: WorkspaceViewModel, workspaceId: String, userId: String) {
    val tasks by viewModel.activeTasks.collectAsState()
    val sandboxTasks = tasks.filter { it.stateScope == "ROUGH_SANDBOX" }
    val isAISummarizing by viewModel.isAISummarizing.collectAsState()
    val aiError by viewModel.aiSummarizationError.collectAsState()
    
    val haptic = LocalHapticFeedback.current
    var showAddDialog by remember { mutableStateOf(false) }
    var draftTitle by remember { mutableStateOf("") }
    var draftBody by remember { mutableStateOf("") }
    var draftCategory by remember { mutableStateOf("Hook Idea") }

    LaunchedEffect(userId) {
        val savedTitle = viewModel.getPersonalDraftTitle(userId) ?: ""
        val savedBody = viewModel.getPersonalDraftBody(userId) ?: ""
        if (savedTitle.isNotEmpty() || savedBody.isNotEmpty()) {
            draftTitle = savedTitle
            draftBody = savedBody
        }
    }

    LaunchedEffect(draftTitle, draftBody) {
        viewModel.savePersonalDraft(userId, draftTitle, draftBody)
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var editingTaskId by remember { mutableStateOf<String?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var activeAISynthesisTaskId by remember { mutableStateOf<String?>(null) }

    val filteredSandboxTasks = sandboxTasks.filter { task ->
        val matchesSearch = task.title.contains(searchQuery, ignoreCase = true) || task.contentBody.contains(searchQuery, ignoreCase = true)
        val matchesCategory = if (selectedCategoryFilter == "All") {
            true
        } else {
            task.title.contains("[$selectedCategoryFilter]", ignoreCase = true)
        }
        matchesSearch && matchesCategory
    }
    
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

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search drafts...", color = TextSecondary, style = MaterialTheme.typography.bodyMedium) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = DS.Space16, vertical = DS.Space8),
            leadingIcon = { Icon(Icons.Default.Search, null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = ColorDivider.copy(alpha = 0.5f),
                focusedContainerColor = SurfaceColor,
                unfocusedContainerColor = SurfaceColor
            ),
            singleLine = true
        )

        // Category tags filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = DS.Space16, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Hook Idea", "Script Blueprint", "Visual Guide", "Thumbnail Plan").forEach { cat ->
                val isSel = selectedCategoryFilter == cat
                Surface(
                    modifier = Modifier.clickable { selectedCategoryFilter = cat },
                    color = if (isSel) AccentBlue.copy(alpha = 0.15f) else Color.Transparent,
                    shape = DS.RadiusSmall,
                    border = BorderStroke(1.dp, if (isSel) AccentBlue else ColorDivider.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = cat.uppercase(),
                        color = if (isSel) Color.White else TextSecondary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = DS.Space12, vertical = DS.Space6)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(DS.Space8))
        
        AnimatedContent(
            targetState = filteredSandboxTasks.isEmpty(),
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            modifier = Modifier.weight(1f).fillMaxWidth(),
            label = "sandbox_content_transition"
        ) { isEmpty ->
            if (isEmpty) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(DS.Space24),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        message = if (searchQuery.isNotEmpty() || selectedCategoryFilter != "All") "No drafts match your filters." else "Your sandbox drafting pad is currently empty.",
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
                        .fillMaxSize()
                        .padding(horizontal = DS.Space16),
                    contentPadding = PaddingValues(vertical = DS.Space16),
                    verticalArrangement = Arrangement.spacedBy(DS.Space16)
                ) {
                    items(filteredSandboxTasks, key = { it.id }) { task ->
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .bounceClick {
                                    if (com.example.ui.feedback.FeedbackManager.isHapticEnabled) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    editingTaskId = task.id
                                    val titleWithoutCategory = if (task.title.contains("]")) {
                                        task.title.substringAfter("]").trim()
                                    } else {
                                        task.title
                                    }
                                    draftTitle = titleWithoutCategory
                                    draftBody = task.contentBody
                                    draftCategory = if (task.title.contains("[")) {
                                        task.title.substringBefore("]").removePrefix("[").trim()
                                    } else {
                                        "Hook Idea"
                                    }
                                    showEditDialog = true
                                },
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
                                    
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        IconButton(
                                            onClick = { 
                                                if (com.example.ui.feedback.FeedbackManager.isHapticEnabled) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                }
                                                editingTaskId = task.id
                                                val titleWithoutCategory = if (task.title.contains("]")) {
                                                    task.title.substringAfter("]").trim()
                                                } else {
                                                    task.title
                                                }
                                                draftTitle = titleWithoutCategory
                                                draftBody = task.contentBody
                                                draftCategory = if (task.title.contains("[")) {
                                                    task.title.substringBefore("]").removePrefix("[").trim()
                                                } else {
                                                    "Hook Idea"
                                                }
                                                showEditDialog = true
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Draft",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { 
                                                if (com.example.ui.feedback.FeedbackManager.isHapticEnabled) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                }
                                                viewModel.deleteTask(task.id, userId) 
                                            },
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
                                            onClick = { 
                                                if (com.example.ui.feedback.FeedbackManager.isHapticEnabled) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                }
                                                viewModel.promoteTaskToProduction(task.id, userId) 
                                            },
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
                                            onClick = { 
                                                if (com.example.ui.feedback.FeedbackManager.isHapticEnabled) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                }
                                                activeAISynthesisTaskId = task.id
                                                viewModel.promoteTaskToProductionWithAI(task.id, userId) 
                                            },
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
    
    if (isAISummarizing || aiError != null) {
        Dialog(onDismissRequest = {}) {
            Surface(
                color = SurfaceColor,
                shape = DS.RadiusLarge,
                border = BorderStroke(1.dp, if (isAISummarizing) AccentBlue.copy(alpha = 0.5f) else AccentRed.copy(alpha = 0.5f)),
                modifier = Modifier.padding(DS.Space16).fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(DS.Space24),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(DS.Space16)
                ) {
                    if (isAISummarizing) {
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
                    } else if (aiError != null) {
                        Icon(Icons.Default.ErrorOutline, null, tint = AccentRed, modifier = Modifier.size(48.dp))
                        Text(
                            text = "AI SYNTHESIS FAULT",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = aiError ?: "Security policies or network fault detected.",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 16.sp
                        )
                        Button(
                            onClick = { 
                                activeAISynthesisTaskId?.let { tid ->
                                    viewModel.promoteTaskToProductionWithAI(tid, userId)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                            modifier = Modifier.fillMaxWidth().height(44.dp).testTag("retry_ai_generation_button"),
                            shape = DS.RadiusMedium
                        ) {
                            Text("RETRY AI GENERATION", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = { 
                                activeAISynthesisTaskId?.let { tid ->
                                    viewModel.promoteTaskToProductionWithFallback(tid, userId)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            modifier = Modifier.fillMaxWidth().height(44.dp).testTag("use_offline_template_button"),
                            shape = DS.RadiusMedium
                        ) {
                            Text("USE OFFLINE STRUCTURAL TEMPLATE", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = { viewModel.aiSummarizationError.value = null },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceLightColor),
                            modifier = Modifier.fillMaxWidth().height(44.dp).testTag("acknowledge_dismiss_button"),
                            shape = DS.RadiusMedium
                        ) {
                            Text("ACKNOWLEDGE & DISMISS", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Secure local drafting pad. Promote with or without Gemini AI orchestration.",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(DS.Space8))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.CloudDone, null, tint = NeonEmerald, modifier = Modifier.size(14.dp))
                            Text("Saved", color = NeonEmerald, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    
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
                        draftTitle = ""
                        draftBody = ""
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

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = SurfaceColor,
            title = {
                Text(
                    text = "Edit Private Concept Draft",
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
                    OutlinedTextField(
                        value = draftTitle,
                        onValueChange = { draftTitle = it },
                        label = { Text("Draft Title", style = MaterialTheme.typography.bodyMedium) },
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
                        editingTaskId?.let { tid ->
                            val formattedTitle = "[$draftCategory] $draftTitle"
                            viewModel.updateTask(tid, formattedTitle, draftBody)
                        }
                        showEditDialog = false 
                    },
                    enabled = draftTitle.isNotBlank() && draftBody.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = DS.RadiusMedium
                ) {
                    Text("Save Draft", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { 
                    Text("Cancel", color = TextSecondary) 
                }
            }
        )
    }
}
