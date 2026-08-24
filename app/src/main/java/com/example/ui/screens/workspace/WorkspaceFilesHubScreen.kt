package com.example.ui.screens.workspace

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.components.*

@OptIn(ExperimentalLayoutApi::class)
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
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("workspace_files_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                            date = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(asset.createdAt)),
                            onDelete = { viewModel.deleteAsset(asset.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(12.dp)) }
                }
            }
            if (filteredAssets.isEmpty()) {
                item {
                    EmptyState(
                        message = "No assets uploaded yet.",
                        icon = Icons.Default.Folder,
                        actionText = "Upload Asset",
                        onAction = { showUploadDialog = true }
                    )
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
        var uploadType by remember { mutableStateOf("Video") }
        val types = listOf("Video", "Image", "Audio", "Document")

        var isUploading by remember { mutableStateOf(false) }
        var uploadProgress by remember { mutableStateOf(0f) }
        val uploadScope = rememberCoroutineScope()

        AlertDialog(
            onDismissRequest = { if (!isUploading) showUploadDialog = false },
            title = {
                Text(
                    "Upload New Resource",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            },
            containerColor = SurfaceColor,
            tonalElevation = 6.dp,
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isUploading) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "UPLOADING TO CO-OP CLOUD...",
                                color = AccentRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                            LinearProgressIndicator(
                                progress = { uploadProgress },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                                color = AccentRed,
                                trackColor = ColorDivider
                            )
                            Text(
                                "Hashing payload & replicating state: ${(uploadProgress * 100).toInt()}%",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        OutlinedTextField(
                            value = uploadName,
                            onValueChange = { uploadName = it },
                            label = { Text("Asset Name", color = TextSecondary) },
                            placeholder = { Text("e.g. final_v3_cut", color = TextSecondary.copy(alpha = 0.5f)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentRed,
                                unfocusedBorderColor = ColorDivider,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "ASSET CATEGORY",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                categories.forEach { cat ->
                                    val isSelected = uploadCategory == cat
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) AccentRed.copy(alpha = 0.15f) else Color.Transparent)
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) AccentRed else ColorDivider,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { uploadCategory = cat }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = cat.replace("_", " "),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) Color.White else TextSecondary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "FILE TYPE",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                types.forEach { t ->
                                    val isSelected = uploadType == t
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) AccentBlue.copy(alpha = 0.15f) else Color.Transparent)
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) AccentBlue else ColorDivider,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { uploadType = t }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = t,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) Color.White else TextSecondary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        uploadScope.launch {
                            isUploading = true
                            uploadProgress = 0f
                            while (uploadProgress < 1f) {
                                delay(80)
                                uploadProgress += 0.05f
                            }
                            viewModel.uploadAsset(
                                workspaceId = workspaceId,
                                uploaderId = currentUserId,
                                fileName = uploadName.takeIf { it.isNotBlank() } ?: "Untitled Asset",
                                fileType = uploadType,
                                category = uploadCategory,
                                taskId = null
                            )
                            isUploading = false
                            showUploadDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isUploading
                ) {
                    Text("Upload", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showUploadDialog = false },
                    enabled = !isUploading
                ) {
                    Text("Cancel", color = TextSecondary)
                }
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
fun FileItem(title: String, type: String, size: String, date: String, onDelete: (() -> Unit)? = null) {
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
            if (onDelete != null) {
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete asset", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
