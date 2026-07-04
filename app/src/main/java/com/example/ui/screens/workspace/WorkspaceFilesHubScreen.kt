package com.example.ui.screens.workspace

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
fun WorkspaceFilesHubScreen(
    workspaceId: String,
    viewModel: com.example.ui.viewmodels.WorkspaceViewModel,
    onBack: () -> Unit
) {
    val assets by viewModel.activeAssets.collectAsState()
    val events by viewModel.activeEvents.collectAsState()
    val currentUserId by viewModel.currentUserIdFlow.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showUploadDialog by remember { mutableStateOf(false) }

    val categories = listOf("RAW_FOOTAGE", "GRAPHICS", "AUDIO_STEMS", "EXPORTS")
    val filteredAssets = assets.filter { it.fileName.contains(searchQuery, ignoreCase = true) }

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
                Text("Asset Pipeline", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
            Button(
                onClick = { showUploadDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.FileUpload, tint = Color.White, contentDescription = "Upload")
                Spacer(Modifier.width(4.dp))
                Text("Upload Asset", fontWeight = FontWeight.Bold)
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search assets...", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, tint = TextSecondary, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentRed,
                unfocusedBorderColor = ColorDivider,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            categories.forEach { category ->
                val categoryAssets = filteredAssets.filter { it.category == category }
                if (categoryAssets.isNotEmpty()) {
                    item {
                        Text(category.replace("_", " "), color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    items(categoryAssets.size) { index ->
                        val asset = categoryAssets[index]
                        FileItem(
                            title = asset.fileName,
                            type = asset.fileType,
                            size = "v${asset.version}",
                            date = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(asset.createdAt))
                        )
                    }
                    item { Spacer(modifier = Modifier.height(12.dp)) }
                }
            }
            if (filteredAssets.isEmpty()) {
                item {
                    Text("No assets uploaded.", color = TextSecondary, fontSize = 14.sp)
                }
            }
            
            // Events log
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text("ASSET LOG", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }
            val assetEvents = events.filter { it.eventType == "ASSET_UPLOADED" || it.eventType == "DELIVERABLE_SUBMITTED" }.take(5)
            items(assetEvents.size) { index ->
                val evt = assetEvents[index]
                Text("• ${evt.eventType.replace("_", " ")} at ${java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(evt.createdAt))}", color = TextSecondary, fontSize = 12.sp)
            }
        }
    }

    if (showUploadDialog) {
        var uploadName by remember { mutableStateOf("") }
        var uploadCategory by remember { mutableStateOf(categories.first()) }
        var uploadType by remember { mutableStateOf("VIDEO") }

        AlertDialog(
            onDismissRequest = { showUploadDialog = false },
            title = { Text("Upload Asset") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = uploadName,
                        onValueChange = { uploadName = it },
                        label = { Text("Asset Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Simplify category selection for now
                    OutlinedTextField(
                        value = uploadCategory,
                        onValueChange = { uploadCategory = it },
                        label = { Text("Category (e.g., RAW_FOOTAGE, EXPORTS)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.uploadAsset(
                        workspaceId = workspaceId,
                        uploaderId = currentUserId,
                        fileName = uploadName.takeIf { it.isNotBlank() } ?: "Untitled Asset",
                        fileType = uploadType,
                        category = uploadCategory,
                        taskId = null
                    )
                    showUploadDialog = false
                }) {
                    Text("Upload")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUploadDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun FolderItem(title: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, ColorDivider)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Folder, null, tint = AccentBlue, modifier = Modifier.size(24.dp))
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FileItem(title: String, type: String, size: String, date: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, ColorDivider)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(ColorDivider),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when(type) {
                        "Image" -> Icons.Default.Image
                        "Video" -> Icons.Default.Movie
                        else -> Icons.Default.Description
                    },
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(type, color = TextSecondary, fontSize = 11.sp)
                    Text("•", color = TextSecondary, fontSize = 11.sp)
                    Text(size, color = TextSecondary, fontSize = 11.sp)
                }
            }
            Text(date, color = TextSecondary, fontSize = 12.sp)
        }
    }
}
