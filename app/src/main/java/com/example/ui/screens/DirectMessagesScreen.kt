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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun DirectMessagesScreen(onBack: () -> Unit) {
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
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("Messages", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.AddComment, tint = AccentBlue, contentDescription = "New Message")
            }
        }

        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Search messages...", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, tint = TextSecondary, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = ColorDivider,
            ),
            shape = RoundedCornerShape(12.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text("DIRECT MESSAGES", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("No direct messages yet.", color = TextSecondary, fontSize = 14.sp)
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text("GROUP CHATS", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("No group chats yet.", color = TextSecondary, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun MessageThreadItem(name: String, lastMessage: String, time: String, unread: Boolean, tint: Color, isGroup: Boolean = false) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (unread) AccentBlue else ColorDivider)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(tint.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(if (isGroup) Icons.Default.Groups else Icons.Default.Person, null, tint = tint)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(name, color = Color.White, fontSize = 16.sp, fontWeight = if (unread) FontWeight.Black else FontWeight.Bold)
                    Text(time, color = if (unread) AccentBlue else TextSecondary, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = lastMessage,
                    color = if (unread) Color.White else TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (unread) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(AccentBlue))
            }
        }
    }
}
