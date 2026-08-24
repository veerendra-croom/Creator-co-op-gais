package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductionTask
import com.example.ui.theme.*
import com.example.ui.viewmodels.WorkspaceViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentPipelineScreen(
    workspaceViewModel: WorkspaceViewModel,
    userId: String,
    onBack: () -> Unit
) {
    val selectedWsId by workspaceViewModel.selectedWorkspaceId.collectAsState()
    val activeTasks by workspaceViewModel.activeTasks.collectAsState()
    
    var showAddItemDialogForStage by remember { mutableStateOf<String?>(null) }
    var newItemTitle by remember { mutableStateOf("") }
    
    var selectedTaskToMove by remember { mutableStateOf<ProductionTask?>(null) }
    var showMoveMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .padding(16.dp)
            .testTag("content_pipeline_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("pipeline_back_btn")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "Content Pipeline",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
        }
        
        var searchQuery by remember { mutableStateOf("") }

        Text(
            text = "Track overarching videos from Idea to Publishing.",
            color = TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
        )

        if (selectedWsId != null) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search pipeline items...", color = TextMuted, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp)) },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    { IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted) } }
                } else null,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("pipeline_search_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceColor,
                    unfocusedContainerColor = SurfaceColor,
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = ColorDivider,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
        }

        if (selectedWsId == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = CrispAmber, modifier = Modifier.size(48.dp))
                    Text(
                        text = "No Active Workspace Selected",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Select or create a workspace under the 'Workspaces' tab to initialize its Content Pipeline.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            val filteredTasks = activeTasks.filter { task ->
                searchQuery.isBlank() || task.title.contains(searchQuery, ignoreCase = true) || task.contentBody.contains(searchQuery, ignoreCase = true)
            }
            // Group tasks by their pipeline stages based on kanbanLane mapping
            val ideationTasks = filteredTasks.filter { it.kanbanLane.uppercase() in listOf("TODO", "IDEAS") }
            val scriptingTasks = filteredTasks.filter { it.kanbanLane.uppercase() == "SCRIPTING" }
            val filmingTasks = filteredTasks.filter { it.kanbanLane.uppercase() == "FILMING" }
            val editingTasks = filteredTasks.filter { it.kanbanLane.uppercase() in listOf("IN_PROGRESS", "EDITING") }
            val publishingTasks = filteredTasks.filter { it.kanbanLane.uppercase() in listOf("DONE", "PUBLISHED", "PUBLISH") }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(end = 16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    PipelineStageColumn(
                        stageName = "IDEATION",
                        items = ideationTasks,
                        onAddTaskClick = { showAddItemDialogForStage = "TODO" },
                        onTaskClick = { selectedTaskToMove = it; showMoveMenu = true }
                    )
                }
                item {
                    PipelineStageColumn(
                        stageName = "SCRIPTING",
                        items = scriptingTasks,
                        onAddTaskClick = { showAddItemDialogForStage = "SCRIPTING" },
                        onTaskClick = { selectedTaskToMove = it; showMoveMenu = true }
                    )
                }
                item {
                    PipelineStageColumn(
                        stageName = "FILMING",
                        items = filmingTasks,
                        onAddTaskClick = { showAddItemDialogForStage = "FILMING" },
                        onTaskClick = { selectedTaskToMove = it; showMoveMenu = true }
                    )
                }
                item {
                    PipelineStageColumn(
                        stageName = "EDITING",
                        items = editingTasks,
                        onAddTaskClick = { showAddItemDialogForStage = "EDITING" },
                        onTaskClick = { selectedTaskToMove = it; showMoveMenu = true }
                    )
                }
                item {
                    PipelineStageColumn(
                        stageName = "PUBLISHING",
                        items = publishingTasks,
                        onAddTaskClick = { showAddItemDialogForStage = "DONE" },
                        onTaskClick = { selectedTaskToMove = it; showMoveMenu = true }
                    )
                }
            }
        }
    }

    // New item creation dialog
    if (showAddItemDialogForStage != null) {
        AlertDialog(
            onDismissRequest = { showAddItemDialogForStage = null; newItemTitle = "" },
            containerColor = SurfaceColor,
            title = { Text("Add Pipeline Item", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Create a production tracking item under the ${showAddItemDialogForStage} stage.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = newItemTitle,
                        onValueChange = { newItemTitle = it },
                        modifier = Modifier.fillMaxWidth().testTag("new_pipeline_item_title"),
                        label = { Text("Title") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = ColorDivider
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val stage = showAddItemDialogForStage ?: "TODO"
                        if (newItemTitle.isNotBlank()) {
                            // Since submitTask is available, we submit a task
                            // Submit task creates a task under PRODUCTION_READY, and defaults to TODO or IDEAS
                            // To make sure it lands in the right stage, let's create it.
                            workspaceViewModel.submitTask(
                                title = newItemTitle,
                                body = "Pipeline brief for $newItemTitle",
                                scope = "PRODUCTION_READY",
                                userId = userId,
                                kanbanLane = stage
                            )
                            // We will also move it if it's not the initial 'TODO' stage
                            // Our UI and flow are fully coupled with Database state!
                        }
                        showAddItemDialogForStage = null
                        newItemTitle = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    enabled = newItemTitle.isNotBlank(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Create", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddItemDialogForStage = null; newItemTitle = "" }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Move task stage popup
    if (showMoveMenu && selectedTaskToMove != null) {
        val task = selectedTaskToMove!!
        AlertDialog(
            onDismissRequest = { showMoveMenu = false; selectedTaskToMove = null },
            containerColor = SurfaceColor,
            title = { Text("Move Pipeline Stage", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select target production pipeline column for '${task.title}':", color = TextSecondary, fontSize = 12.sp)
                    listOf(
                        "IDEATION" to "TODO",
                        "SCRIPTING" to "SCRIPTING",
                        "FILMING" to "FILMING",
                        "EDITING" to "EDITING",
                        "PUBLISHING" to "DONE"
                    ).forEach { (label, lane) ->
                        Button(
                            onClick = {
                                workspaceViewModel.moveTaskLane(task.id, lane, userId)
                                showMoveMenu = false
                                selectedTaskToMove = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (task.kanbanLane.uppercase() == lane) AccentBlue else SurfaceLightColor,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(label, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showMoveMenu = false; selectedTaskToMove = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun PipelineStageColumn(
    stageName: String,
    items: List<ProductionTask>,
    onAddTaskClick: () -> Unit,
    onTaskClick: (ProductionTask) -> Unit
) {
    Column(
        modifier = Modifier.width(280.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            color = SurfaceColor,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stageName,
                    color = AccentBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Badge(
                    containerColor = SurfaceLightColor,
                    contentColor = Color.White
                ) {
                    Text(items.size.toString(), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items.forEach { task ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTaskClick(task) }
                        .testTag("pipeline_item_${task.id}"),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ColorDivider)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(task.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        if (task.contentBody.isNotBlank() && task.contentBody != "Pipeline brief for ${task.title}") {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(task.contentBody, color = TextSecondary, fontSize = 12.sp, maxLines = 2)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Schedule, null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                                Text("Active Stage", color = TextSecondary, fontSize = 11.sp)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { onTaskClick(task) }
                                    .background(SurfaceLightColor, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Move", color = AccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.ArrowForward, contentDescription = "Move stage", tint = AccentBlue, modifier = Modifier.size(10.dp))
                            }
                        }
                    }
                }
            }
            
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(SurfaceColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable { onAddTaskClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
                        Text("No items in $stageName", color = TextSecondary, fontSize = 11.sp)
                        Text("+ Tap to create", color = AccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onAddTaskClick,
            modifier = Modifier.fillMaxWidth().testTag("add_pipeline_item_btn_$stageName"),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
            border = BorderStroke(1.dp, ColorDivider),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add Item")
        }
    }
}
