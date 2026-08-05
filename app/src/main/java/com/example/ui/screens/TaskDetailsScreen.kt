package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
    
    val taskFlow = remember(taskId) { repository.getTaskById(taskId) }
    val taskState by taskFlow.collectAsState(initial = null)
    val task = taskState
    
    if (task == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = NeonEmerald)
        }
        return
    }

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
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Task-${task.id.take(6).uppercase()}", color = TextSecondary, fontSize = 14.sp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                IconButton(
                    onClick = {
                        scope.launch {
                            repository.deleteTask(task.id)
                            onBack()
                        }
                    }
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Task", tint = TextMuted)
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Text(
                    text = task.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
            
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TaskMetaBadge(icon = Icons.Default.Person, title = "Assignee", value = task.creatorId.take(12))
                    TaskMetaBadge(
                        icon = Icons.Default.CalendarToday, 
                        title = "Due", 
                        value = task.deadline?.let { SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(it)) } ?: "No due date"
                    )
                }
            }

            item {
                Divider(color = ColorDivider)
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
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (task.kanbanLane != "COMPLETED") {
                        Button(
                            onClick = {
                                scope.launch { repository.updateTaskStatus(taskId, "COMPLETED") }
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
                                scope.launch { repository.updateTaskStatus(taskId, "IN_PROGRESS") }
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
                Divider(color = ColorDivider, modifier = Modifier.padding(vertical = 16.dp))
            }

            item {
                Text("ACTIVITY", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                TaskActivityItem(author = "System", action = "created the task", time = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(task.createdAt)))
                if (task.kanbanLane == "COMPLETED") {
                    TaskActivityItem(author = "System", action = "marked task as completed", time = "Recently")
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
