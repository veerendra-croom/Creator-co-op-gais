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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun TaskDetailsScreen(
    taskId: String,
    onBack: () -> Unit
) {
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
            Text("Task-$taskId", color = TextSecondary, fontSize = 14.sp)
            Surface(
                color = AccentRed.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, AccentRed)
            ) {
                Text(
                    text = "IN PROGRESS",
                    color = AccentRed,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Text(
                    text = "Unknown Task",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
            
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TaskMetaBadge(icon = Icons.Default.Person, title = "Assignee", value = "Unassigned")
                    TaskMetaBadge(icon = Icons.Default.CalendarToday, title = "Due", value = "No due date")
                }
            }

            item {
                Divider(color = ColorDivider)
            }

            item {
                Text("DESCRIPTION", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No description provided.",
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            item {
                Divider(color = ColorDivider)
            }

            item {
                Text("ACTIVITY", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("No recent activity.", color = TextSecondary, fontSize = 14.sp)
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
