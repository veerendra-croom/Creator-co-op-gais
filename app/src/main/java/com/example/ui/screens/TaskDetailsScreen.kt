package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.CreatorCoopApp
import com.example.data.model.ProductionTask
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TaskDetailsScreen(
    taskId: String,
    onBack: () -> Unit
) {
    val application = LocalContext.current.applicationContext as CreatorCoopApp
    val repository = remember { application.container.repository }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val taskFlow = remember(taskId) { repository.getTaskById(taskId) }
    val taskState by taskFlow.collectAsState(initial = null)
    val task = taskState
    
    val creatorProfile by remember(task?.creatorId) { 
        if (task?.creatorId != null) repository.getUserById(task.creatorId) else kotlinx.coroutines.flow.flowOf(null)
    }.collectAsState(initial = null)

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    
    if (task == null) {
        Box(modifier = Modifier.fillMaxSize().background(PrimaryBackground), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = NeonEmerald)
        }
        return
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = SurfaceColor,
            title = { Text("Delete Task?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently remove '${task.title}'. This action cannot be undone.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        scope.launch {
                            repository.deleteTask(task.id)
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    if (showEditDialog) {
        var editTitle by remember { mutableStateOf(task.title) }
        var editDescription by remember { mutableStateOf(task.contentBody) }
        var editPriority by remember { mutableStateOf(task.priority) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = SurfaceColor,
            title = { Text("Edit Task Details", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Task Title") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = ColorDivider
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editDescription,
                        onValueChange = { editDescription = it },
                        label = { Text("Description") },
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = ColorDivider
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Priority", color = TextSecondary, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("LOW", "MEDIUM", "HIGH").forEach { p ->
                            FilterChip(
                                selected = editPriority == p,
                                onClick = { editPriority = p },
                                label = { Text(p, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentBlue.copy(alpha = 0.2f),
                                    selectedLabelColor = AccentBlue
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = task.copy(
                            title = editTitle.trim().ifEmpty { task.title },
                            contentBody = editDescription.trim(),
                            priority = editPriority
                        )
                        scope.launch {
                            repository.insertTask(updated)
                            snackbarHostState.showSnackbar("Task details updated")
                        }
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = PrimaryBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("Task-${task.id.take(6).uppercase()}", color = TextSecondary, fontSize = 14.sp)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        color = when (task.kanbanLane) {
                            "COMPLETED" -> NeonEmerald.copy(alpha = 0.15f)
                            "IN_PROGRESS" -> AccentBlue.copy(alpha = 0.15f)
                            else -> AccentRed.copy(alpha = 0.15f)
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, when (task.kanbanLane) {
                            "COMPLETED" -> NeonEmerald
                            "IN_PROGRESS" -> AccentBlue
                            else -> AccentRed
                        })
                    ) {
                        Text(
                            text = task.kanbanLane.replace("_", " "),
                            color = when (task.kanbanLane) {
                                "COMPLETED" -> NeonEmerald
                                "IN_PROGRESS" -> AccentBlue
                                else -> AccentRed
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Task", tint = TextSecondary)
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Task", tint = TextMuted)
                    }
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.weight(1f)) {
                item {
                    Text(
                        text = task.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
                
                item {
                    val creatorDisplay = creatorProfile?.username?.let { "@$it" } ?: (task.creatorId.takeIf { it.isNotBlank() }?.take(10) ?: "Unassigned")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        TaskMetaBadge(icon = Icons.Default.Person, title = "Creator", value = creatorDisplay)
                        TaskMetaBadge(
                            icon = Icons.Default.CalendarToday, 
                            title = "Due", 
                            value = task.deadline?.let { SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(it)) } ?: "No due date"
                        )
                        TaskMetaBadge(
                            icon = Icons.Default.Flag,
                            title = "Priority",
                            value = task.priority
                        )
                    }
                }

                item {
                    HorizontalDivider(color = ColorDivider)
                }

                item {
                    Text("DESCRIPTION", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = task.contentBody.ifBlank { "No description provided." },
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (task.kanbanLane != "COMPLETED") {
                            Button(
                                onClick = {
                                    scope.launch {
                                        repository.updateTaskStatus(taskId, "COMPLETED")
                                        snackbarHostState.showSnackbar("Task marked as completed! (+2 Reputation)")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Mark Complete", color = PrimaryBackground, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (task.kanbanLane != "IN_PROGRESS" && task.kanbanLane != "COMPLETED") {
                            Button(
                                onClick = {
                                    scope.launch {
                                        repository.updateTaskStatus(taskId, "IN_PROGRESS")
                                        snackbarHostState.showSnackbar("Task moved to In Progress")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Start Work", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item {
                    HorizontalDivider(color = ColorDivider, modifier = Modifier.padding(vertical = 8.dp))
                }

                item {
                    Text("ACTIVITY", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    TaskActivityItem(author = "System", action = "created the task", time = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(task.createdAt)))
                    if (task.kanbanLane == "COMPLETED") {
                        TaskActivityItem(author = "System", action = "marked task as completed", time = "Recently")
                    } else if (task.kanbanLane == "IN_PROGRESS") {
                        TaskActivityItem(author = "System", action = "started work on task", time = "In progress")
                    }
                }
            }
        }
    }
}

@Composable
fun TaskMetaBadge(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape).background(SurfaceColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        }
        Column {
            Text(title, color = TextSecondary, fontSize = 10.sp)
            Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TaskActivityItem(author: String, action: String, time: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(24.dp).clip(CircleShape).background(SurfaceColor),
            contentAlignment = Alignment.Center
        ) {
            Text(author.take(1), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(author, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(action, color = TextSecondary, fontSize = 13.sp)
            }
            Text(time, color = TextSecondary, fontSize = 11.sp)
        }
    }
}
