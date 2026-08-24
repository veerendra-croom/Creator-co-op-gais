package com.example.ui.screens.workspace

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.AppRepository
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.tour.guidedTourTarget
import com.example.ui.viewmodels.WorkspaceViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class RichTaskData(
    val description: String = "",
    val assigneeId: String? = null,
    val labels: List<String> = emptyList(),
    val attachments: List<String> = emptyList(),
    val followers: List<String> = emptyList(),
    val dependencies: List<String> = emptyList(),
    val subtasks: List<SubtaskItem> = emptyList(),
    val estimatedDurationHours: Int = 0,
    val dueDate: Long? = null,
    val checklist: List<ChecklistItem> = emptyList(),
    val isArchived: Boolean = false,
    val isMilestone: Boolean = false,
    val watchers: List<String> = emptyList()
)

@Serializable
data class SubtaskItem(
    val id: String,
    val title: String,
    val isDone: Boolean = false
)

@Serializable
data class ChecklistItem(
    val id: String,
    val text: String,
    val isChecked: Boolean = false
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskDetailsDialog(
    task: ProductionTask,
    viewModel: WorkspaceViewModel,
    repository: AppRepository,
    userId: String,
    currentUser: UserProfile?,
    allUsers: List<UserProfile>,
    onDismiss: () -> Unit,
    globalViewModel: com.example.ui.viewmodels.GlobalViewModel? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val activeTasks by viewModel.activeTasks.collectAsState()
    val activeMembers by viewModel.activeWorkspaceMembers.collectAsState()
    
    // Parse initial rich task data
    var richData by remember(task.contentBody) {
        mutableStateOf(parseRichTaskData(task.contentBody))
    }

    var editModeDescription by remember { mutableStateOf(false) }
    var descText by remember { mutableStateOf(richData.description) }
    
    var newSubtaskText by remember { mutableStateOf("") }
    var newChecklistItemText by remember { mutableStateOf("") }
    var newLabelText by remember { mutableStateOf("") }
    var newAttachmentUrl by remember { mutableStateOf("") }

    // Helper to save all edits back to repository
    val saveTaskUpdates: (RichTaskData) -> Unit = { updatedRich ->
        richData = updatedRich
        coroutineScope.launch {
            val jsonStr = Json.encodeToString(RichTaskData.serializer(), updatedRich)
            val updatedTask = task.copy(contentBody = jsonStr)
            repository.insertTask(updatedTask)
            
            // Log Event
            val event = WorkspaceEvent(
                id = UUID.randomUUID().toString(),
                workspaceId = task.workspaceId,
                actorId = userId,
                eventType = "TASK_EDITED",
                entityId = task.id,
                title = "Task Edited",
                description = "Task '${task.title}' was updated by ${currentUser?.displayName ?: "a member"}."
            )
            repository.insertWorkspaceEvent(event)
        }
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .testTag("task_details_dialog"),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, ColorDivider)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(DS.Space16)
            ) {
                // --- 1. HEADER SECTION ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = if (richData.isMilestone) Icons.Default.Flag else Icons.AutoMirrored.Filled.Assignment,
                            contentDescription = if (richData.isMilestone) "Milestone Task" else "Standard Task",
                            tint = if (richData.isMilestone) CrispAmber else AccentBlue
                        )
                        Column {
                            Text(
                                text = task.title + if (richData.isArchived) " (Archived)" else "",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Lane: ${task.kanbanLane} • Priority: ${task.priority}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Watch Toggle
                        val isWatching = richData.watchers.contains(userId)
                        IconButton(
                            onClick = {
                                val watchers = richData.watchers.toMutableList()
                                if (isWatching) watchers.remove(userId) else watchers.add(userId)
                                saveTaskUpdates(richData.copy(watchers = watchers))
                            }
                        ) {
                            Icon(
                                imageVector = if (isWatching) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Watch Task",
                                tint = if (isWatching) AccentBlue else TextSecondary
                            )
                        }

                        // Duplicate Task Action
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    val dupTask = task.copy(
                                        id = UUID.randomUUID().toString(),
                                        title = "${task.title} (Copy)",
                                        createdAt = System.currentTimeMillis()
                                    )
                                    repository.insertTask(dupTask)
                                    // Event
                                    repository.insertWorkspaceEvent(WorkspaceEvent(
                                        id = UUID.randomUUID().toString(),
                                        workspaceId = task.workspaceId,
                                        actorId = userId,
                                        eventType = "TASK_DUPLICATED",
                                        title = "Task Duplicated",
                                        description = "Task '${task.title}' was duplicated."
                                    ))
                                    onDismiss()
                                }
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate Task", tint = Color.White)
                        }

                        // Archive Toggle
                        IconButton(
                            onClick = {
                                viewModel.deleteTask(task.id, userId)
                                onDismiss()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Task",
                                tint = AccentRed
                            )
                        }

                        IconButton(
                            onClick = {
                                saveTaskUpdates(richData.copy(isArchived = !richData.isArchived))
                            }
                        ) {
                            Icon(
                                imageVector = if (richData.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                                contentDescription = if (richData.isArchived) "Restore Task" else "Archive Task",
                                tint = if (richData.isArchived) NeonEmerald else AccentRed
                            )
                        }

                        // Close Button
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                HorizontalDivider(color = ColorDivider, thickness = 1.dp, modifier = Modifier.padding(vertical = DS.Space12))

                // --- 2. MAIN SCROLLABLE BODY ---
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DS.Space16)
                ) {
                    // Left Column (Description, Subtasks, Checklist, Attachments, Comments)
                    Column(
                        modifier = Modifier
                            .weight(1.5f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(DS.Space20)
                    ) {
                        // Description Segment
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Description", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                TextButton(onClick = { 
                                    if (editModeDescription) {
                                        saveTaskUpdates(richData.copy(description = descText))
                                    }
                                    editModeDescription = !editModeDescription 
                                }) {
                                    Text(if (editModeDescription) "Save" else "Edit", color = AccentBlue)
                                }
                            }

                            if (editModeDescription) {
                                OutlinedTextField(
                                    value = descText,
                                    onValueChange = { descText = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = SurfaceLightColor,
                                        unfocusedContainerColor = SurfaceLightColor
                                    )
                                )
                            } else {
                                Text(
                                    text = if (richData.description.isBlank()) "No description provided." else richData.description,
                                    color = if (richData.description.isBlank()) TextSecondary else Color.White,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // Checklist Segment
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Checklist", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            
                            richData.checklist.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Checkbox(
                                            checked = item.isChecked,
                                            onCheckedChange = { checked ->
                                                val list = richData.checklist.map { 
                                                    if (it.id == item.id) it.copy(isChecked = checked) else it
                                                }
                                                saveTaskUpdates(richData.copy(checklist = list))
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = NeonEmerald)
                                        )
                                        Text(item.text, color = Color.White, fontSize = 13.sp)
                                    }
                                    IconButton(
                                        onClick = {
                                            val list = richData.checklist.filter { it.id != item.id }
                                            saveTaskUpdates(richData.copy(checklist = list))
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, "Delete checklist item", tint = AccentRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextField(
                                    value = newChecklistItemText,
                                    onValueChange = { newChecklistItemText = it },
                                    placeholder = { Text("Add item to checklist...", fontSize = 12.sp, color = TextSecondary) },
                                    colors = TextFieldDefaults.colors(focusedContainerColor = SurfaceLightColor, unfocusedContainerColor = SurfaceLightColor, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Button(
                                    onClick = {
                                        if (newChecklistItemText.isNotBlank()) {
                                            val list = richData.checklist + ChecklistItem(UUID.randomUUID().toString(), newChecklistItemText)
                                            saveTaskUpdates(richData.copy(checklist = list))
                                            newChecklistItemText = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                                ) {
                                    Text("Add")
                                }
                            }
                        }

                        // Subtasks Segment
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Subtasks", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            
                            richData.subtasks.forEach { sub ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Checkbox(
                                            checked = sub.isDone,
                                            onCheckedChange = { done ->
                                                val subs = richData.subtasks.map { 
                                                    if (it.id == sub.id) it.copy(isDone = done) else it
                                                }
                                                saveTaskUpdates(richData.copy(subtasks = subs))
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = AccentBlue)
                                        )
                                        Text(sub.title, color = Color.White, fontSize = 13.sp)
                                    }
                                    IconButton(
                                        onClick = {
                                            val subs = richData.subtasks.filter { it.id != sub.id }
                                            saveTaskUpdates(richData.copy(subtasks = subs))
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, "Delete subtask", tint = AccentRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextField(
                                    value = newSubtaskText,
                                    onValueChange = { newSubtaskText = it },
                                    placeholder = { Text("Add a subtask...", fontSize = 12.sp, color = TextSecondary) },
                                    colors = TextFieldDefaults.colors(focusedContainerColor = SurfaceLightColor, unfocusedContainerColor = SurfaceLightColor, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Button(
                                    onClick = {
                                        if (newSubtaskText.isNotBlank()) {
                                            val subs = richData.subtasks + SubtaskItem(UUID.randomUUID().toString(), newSubtaskText)
                                            saveTaskUpdates(richData.copy(subtasks = subs))
                                            newSubtaskText = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                                ) {
                                    Text("Add")
                                }
                            }
                        }

                        // Attachments Segment
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Attachments", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            
                            richData.attachments.forEach { url ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SurfaceLightColor, RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.AttachFile, contentDescription = null, tint = AccentBlue)
                                        Text(url, color = AccentBlue, fontSize = 12.sp, modifier = Modifier.clickable {
                                            // Handle attachment view
                                        })
                                    }
                                    IconButton(
                                        onClick = {
                                            val atts = richData.attachments.filter { it != url }
                                            saveTaskUpdates(richData.copy(attachments = atts))
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, "Delete attachment", tint = AccentRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            FeatureGate(
                                flagKey = "FILE_UPLOADS",
                                userRole = currentUser?.systemRole ?: "CREATOR",
                                repository = repository,
                                fallback = {
                                    FeatureRestrictedBanner(
                                        featureTitle = "File & Asset Uploads",
                                        restrictedMessage = "Attachment uploads are currently restricted by platform governance."
                                    )
                                }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TextField(
                                        value = newAttachmentUrl,
                                        onValueChange = { newAttachmentUrl = it },
                                        placeholder = { Text("Add attachment URL...", fontSize = 12.sp, color = TextSecondary) },
                                        colors = TextFieldDefaults.colors(focusedContainerColor = SurfaceLightColor, unfocusedContainerColor = SurfaceLightColor, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    Button(
                                        onClick = {
                                            if (newAttachmentUrl.isNotBlank()) {
                                                val atts = richData.attachments + newAttachmentUrl
                                                saveTaskUpdates(richData.copy(attachments = atts))
                                                newAttachmentUrl = ""
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                                    ) {
                                        Text("Add")
                                    }
                                }
                            }
                        }

                        // Unified Comments Section
                        CommentEngine(
                            targetId = task.id,
                            repository = repository,
                            currentUser = currentUser,
                            allUsers = allUsers,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Right Column (Assignee, Priority, Estimation, Due Date, Labels, Dependencies)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(SurfaceLightColor, DS.RadiusMedium)
                            .padding(DS.Space12)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(DS.Space16)
                    ) {
                        Text("Task Details & Assignment", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)

                        // 1. Assignee
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Assignee", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            var showAssigneeDropdown by remember { mutableStateOf(false) }
                            val currentAssignee = activeMembers.find { it.userId == richData.assigneeId }
                            val assigneeName = currentAssignee?.userId ?: "Unassigned"
                            
                            Box(
                                modifier = if (globalViewModel != null) Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceColor, RoundedCornerShape(8.dp))
                                    .border(1.dp, ColorDivider, RoundedCornerShape(8.dp))
                                    .clickable { showAssigneeDropdown = true }
                                    .guidedTourTarget("workspace_task_assignment", globalViewModel.tourManager)
                                    .padding(12.dp)
                                else Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceColor, RoundedCornerShape(8.dp))
                                    .border(1.dp, ColorDivider, RoundedCornerShape(8.dp))
                                    .clickable { showAssigneeDropdown = true }
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text(assigneeName, color = Color.White, fontSize = 13.sp)
                                    Icon(Icons.Default.ArrowDropDown, null, tint = Color.White)
                                }

                                DropdownMenu(
                                    expanded = showAssigneeDropdown,
                                    onDismissRequest = { showAssigneeDropdown = false },
                                    modifier = Modifier.background(SurfaceColor)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Unassigned", color = Color.White) },
                                        onClick = {
                                            saveTaskUpdates(richData.copy(assigneeId = null))
                                            showAssigneeDropdown = false
                                        }
                                    )
                                    activeMembers.forEach { member ->
                                        DropdownMenuItem(
                                            text = { Text(member.userId, color = Color.White) },
                                            onClick = {
                                                saveTaskUpdates(richData.copy(assigneeId = member.userId))
                                                showAssigneeDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Milestone / Watch Task Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Convert into Milestone", color = TextSecondary, fontSize = 12.sp)
                            Switch(
                                checked = richData.isMilestone,
                                onCheckedChange = { isMilestone ->
                                    saveTaskUpdates(richData.copy(isMilestone = isMilestone))
                                }
                            )
                        }

                        // 3. Priority Toggle
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Priority", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("LOW", "MEDIUM", "HIGH", "URGENT").forEach { priority ->
                                    val isSel = task.priority == priority
                                    val bg = if (isSel) {
                                        when (priority) {
                                            "URGENT" -> AccentRed
                                            "HIGH" -> CrispAmber
                                            "MEDIUM" -> AccentBlue
                                            else -> NeonEmerald
                                        }
                                    } else Color.Transparent

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(bg)
                                            .border(1.dp, ColorDivider, RoundedCornerShape(6.dp))
                                            .clickable {
                                                coroutineScope.launch {
                                                    repository.insertTask(task.copy(priority = priority))
                                                }
                                            }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(priority, fontSize = 10.sp, color = if (isSel) Color.White else TextSecondary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // 4. Estimated Duration
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Est. Duration (hours)", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            var durationVal by remember { mutableStateOf(richData.estimatedDurationHours.toString()) }
                            OutlinedTextField(
                                value = durationVal,
                                onValueChange = {
                                    durationVal = it
                                    val hours = it.toIntOrNull() ?: 0
                                    saveTaskUpdates(richData.copy(estimatedDurationHours = hours))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentBlue)
                            )
                        }

                        // 5. Due Date
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Due Date", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            val format = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
                            val dateStr = richData.dueDate?.let { format.format(Date(it)) } ?: "No due date set"
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceColor, RoundedCornerShape(8.dp))
                                    .border(1.dp, ColorDivider, RoundedCornerShape(8.dp))
                                    .clickable {
                                        // Set due date to today as default picker action
                                        saveTaskUpdates(richData.copy(dueDate = System.currentTimeMillis() + 86400000L * 7L)) // 1 week out
                                    }
                                    .padding(12.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text(dateStr, color = Color.White, fontSize = 13.sp)
                                    Icon(Icons.Default.CalendarToday, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        // 6. Labels/Tags
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Labels", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                richData.labels.forEach { label ->
                                    Surface(
                                        color = AccentBlue.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.5f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove Label",
                                                tint = Color.White,
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clickable {
                                                        val labels = richData.labels.filter { it != label }
                                                        saveTaskUpdates(richData.copy(labels = labels))
                                                    }
                                            )
                                        }
                                    }
                                }
                            }
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextField(
                                    value = newLabelText,
                                    onValueChange = { newLabelText = it },
                                    placeholder = { Text("Add tag...", fontSize = 11.sp, color = TextSecondary) },
                                    colors = TextFieldDefaults.colors(focusedContainerColor = SurfaceColor, unfocusedContainerColor = SurfaceColor, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Button(
                                    onClick = {
                                        if (newLabelText.isNotBlank()) {
                                            val labels = richData.labels + newLabelText
                                            saveTaskUpdates(richData.copy(labels = labels))
                                            newLabelText = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                                ) {
                                    Text("+")
                                }
                            }
                        }

                        // 7. Dependencies (Link to other tasks)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Dependencies", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            
                            richData.dependencies.forEach { depId ->
                                val linked = activeTasks.find { it.id == depId }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SurfaceColor, RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(linked?.title ?: "Unknown Task ID", color = Color.White, fontSize = 12.sp)
                                    IconButton(
                                        onClick = {
                                            val list = richData.dependencies.filter { it != depId }
                                            saveTaskUpdates(richData.copy(dependencies = list))
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, "Delete dependency", tint = AccentRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            var showDepsDropdown by remember { mutableStateOf(false) }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceColor, RoundedCornerShape(8.dp))
                                    .clickable { showDepsDropdown = true }
                                    .padding(8.dp)
                            ) {
                                Text("Add dependency task...", color = TextSecondary, fontSize = 12.sp)

                                DropdownMenu(
                                    expanded = showDepsDropdown,
                                    onDismissRequest = { showDepsDropdown = false },
                                    modifier = Modifier.background(SurfaceColor)
                                ) {
                                    val availableTasks = activeTasks.filter { it.id != task.id && !richData.dependencies.contains(it.id) }
                                    if (availableTasks.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("No other tasks available", color = TextSecondary) },
                                            onClick = {}
                                        )
                                    } else {
                                        availableTasks.forEach { available ->
                                            DropdownMenuItem(
                                                text = { Text(available.title, color = Color.White) },
                                                onClick = {
                                                    val deps = richData.dependencies + available.id
                                                    saveTaskUpdates(richData.copy(dependencies = deps))
                                                    showDepsDropdown = false
                                                }
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
    }
}

private fun parseRichTaskData(jsonStr: String): RichTaskData {
    return try {
        Json.decodeFromString(RichTaskData.serializer(), jsonStr)
    } catch (e: Exception) {
        // Plain text fallback
        RichTaskData(description = jsonStr)
    }
}
