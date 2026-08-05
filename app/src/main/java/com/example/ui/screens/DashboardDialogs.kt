package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChangelogEntry
import com.example.ui.theme.*

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceColor,
        shape = RoundedCornerShape(24.dp),
        title = { Text("About Creator Co-Op", fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            Column {
                Text("Version: 1.0.0 (Build 2026.06.22)", color = TextPrimary, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Creator Co-Op is a professional collaboration platform designed to unify digital media talent through structured workspaces, syndicate agreements, and real-time coordination tools.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Leadership", fontWeight = FontWeight.Bold, color = AccentBlue, fontSize = 14.sp)
                Text("Botla Veerendra (Founder)\nMacha Praveen (Co-Founder)", color = TextPrimary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Contact", fontWeight = FontWeight.Bold, color = AccentBlue, fontSize = 14.sp)
                Text("veerendrabotla@gmail.com\npraveenmacha777@gmail.com", color = TextSecondary, fontSize = 13.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", fontWeight = FontWeight.Bold, color = AccentBlue)
            }
        }
    )
}

@Composable
fun WhatsNewDialog(entry: ChangelogEntry, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceColor,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Celebration, null, tint = AccentBlue)
                Spacer(modifier = Modifier.width(12.dp))
                Text(entry.title, fontWeight = FontWeight.Black, color = Color.White)
            }
        },
        text = {
            Column {
                Text("Version ${entry.versionName}", color = AccentBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    entry.description,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("GOT IT!", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun LicensesDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceColor,
        shape = RoundedCornerShape(24.dp),
        title = { Text("Open Source Licenses", fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            Box(modifier = Modifier.height(300.dp)) {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    val licenses = listOf(
                        "Jetpack Compose - Apache 2.0",
                        "Room Persistence Library - Apache 2.0",
                        "Kotlin Coroutines - Apache 2.0",
                        "Cloud Gateway SDK - MIT",
                        "Ktor Client - Apache 2.0",
                        "Coil Image Loading - Apache 2.0",
                        "Material Design 3 - Apache 2.0",
                        "Retrofit - Apache 2.0",
                        "OkHttp - Apache 2.0",
                        "Kotlinx Serialization - Apache 2.0"
                    )
                    items(licenses) { license ->
                        Text(
                            text = license,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        HorizontalDivider(color = ColorDivider)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", fontWeight = FontWeight.Bold, color = AccentBlue)
            }
        }
    )
}
