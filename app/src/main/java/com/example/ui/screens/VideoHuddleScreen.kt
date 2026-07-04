package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
fun VideoHuddleScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Minimize", tint = Color.White)
            }
            Surface(
                color = AccentRed,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("LIVE HUDDLE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            // Simulated video grid
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                    VideoParticipantCard("You", Modifier.weight(1f).padding(8.dp))
                }
            }
        }

        // Controls
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            color = PrimaryBackground.copy(alpha = 0.8f),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ControlButton(Icons.Default.MicOff, MaterialTheme.colorScheme.surfaceVariant)
                ControlButton(Icons.Default.Videocam, MaterialTheme.colorScheme.surfaceVariant)
                ControlButton(Icons.Default.ScreenShare, MaterialTheme.colorScheme.surfaceVariant)
                ControlButton(Icons.Default.CallEnd, AccentRed)
            }
        }
    }
}

@Composable
fun VideoParticipantCard(name: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceColor)
    ) {
        Icon(Icons.Default.Person, null, tint = TextSecondary, modifier = Modifier.size(64.dp).align(Alignment.Center))
        Surface(
            modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
            color = Color.Black.copy(alpha = 0.6f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(name, color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
        }
    }
}

@Composable
fun ControlButton(icon: androidx.compose.ui.graphics.vector.ImageVector, backgroundColor: Color) {
    IconButton(
        onClick = {},
        modifier = Modifier.size(56.dp).clip(CircleShape).background(backgroundColor)
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(28.dp))
    }
}
