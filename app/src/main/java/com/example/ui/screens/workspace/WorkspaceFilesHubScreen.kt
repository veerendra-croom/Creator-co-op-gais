package com.example.ui.screens.workspace

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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

    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var isMultiSelectMode by remember { mutableStateOf(false) }
    var selectedAssetIds by remember { mutableStateOf(setOf<String>()) }
    var showDeliverablePackagerDialog by remember { mutableStateOf(false) }

    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    val categories = listOf("RAW_FOOTAGE", "GRAPHICS", "AUDIO_STEMS", "EXPORTS")
    val allCategoryFilters = listOf("ALL") + categories
    
    val filteredAssets = assets.filter { asset ->
        (selectedCategoryFilter == "ALL" || asset.category == selectedCategoryFilter) &&
        asset.fileName.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showDeliverablePackagerDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Inventory, tint = Color.White, contentDescription = "Package Deliverables", modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Package Bundle", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                Button(
                    onClick = { showUploadDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.FileUpload, tint = Color.White, contentDescription = "Upload", modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Upload", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        // Search & Multi-Select Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search assets...", color = TextSecondary, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, tint = TextSecondary, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.weight(1f).height(48.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentRed,
                    unfocusedBorderColor = ColorDivider,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            )

            IconButton(
                onClick = { 
                    isMultiSelectMode = !isMultiSelectMode 
                    if (!isMultiSelectMode) selectedAssetIds = emptySet()
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(if (isMultiSelectMode) AccentBlue.copy(alpha = 0.2f) else SurfaceColor, RoundedCornerShape(10.dp))
                    .border(1.dp, if (isMultiSelectMode) AccentBlue else ColorDivider, RoundedCornerShape(10.dp))
            ) {
                Icon(
                    imageVector = if (isMultiSelectMode) Icons.Default.ChecklistRtl else Icons.Default.Checklist,
                    contentDescription = "Multi-Select",
                    tint = if (isMultiSelectMode) AccentBlue else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Simplification 5: Asset File Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(allCategoryFilters.size) { idx ->
                val cat = allCategoryFilters[idx]
                val isSelected = selectedCategoryFilter == cat
                Surface(
                    onClick = { selectedCategoryFilter = cat },
                    color = if (isSelected) AccentRed else SurfaceColor,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, if (isSelected) AccentRed else ColorDivider),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = if (cat == "ALL") "ALL ASSETS" else "#" + cat.replace("_", " "),
                        color = if (isSelected) Color.White else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        var assetToDelete by remember { mutableStateOf<com.example.data.model.WorkspaceAsset?>(null) }
        var showDeleteSelectedConfirm by remember { mutableStateOf(false) }

        if (assetToDelete != null) {
            AlertDialog(
                onDismissRequest = { assetToDelete = null },
                containerColor = SurfaceColor,
                shape = RoundedCornerShape(16.dp),
                title = { Text("Delete Asset?", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Are you sure you want to permanently delete \"${assetToDelete?.fileName}\"? This cannot be undone.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            assetToDelete?.let { viewModel.deleteAsset(it.id) }
                            assetToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                    ) {
                        Text("DELETE", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { assetToDelete = null }) {
                        Text("CANCEL", color = TextSecondary)
                    }
                }
            )
        }

        if (showDeleteSelectedConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteSelectedConfirm = false },
                containerColor = SurfaceColor,
                shape = RoundedCornerShape(16.dp),
                title = { Text("Delete Selected Assets?", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Are you sure you want to delete ${selectedAssetIds.size} selected assets? This cannot be undone.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            selectedAssetIds.forEach { viewModel.deleteAsset(it) }
                            selectedAssetIds = emptySet()
                            showDeleteSelectedConfirm = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                    ) {
                        Text("DELETE ALL", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteSelectedConfirm = false }) {
                        Text("CANCEL", color = TextSecondary)
                    }
                }
            )
        }

        // Batch Action Bar when Multi-Select Mode is Active
        if (isMultiSelectMode && selectedAssetIds.isNotEmpty()) {
            Surface(
                color = SurfaceColor,
                border = BorderStroke(1.dp, AccentBlue),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${selectedAssetIds.size} assets selected",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = {
                                showDeleteSelectedConfirm = true
                            }
                        ) {
                            Text("Delete Selected", color = AccentRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { showDeliverablePackagerDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Package", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

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
                            onDelete = { assetToDelete = asset }
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

    if (showDeliverablePackagerDialog) {
        var bundleName by remember { mutableStateOf("Co-Op Master Delivery Package v1") }
        var clientEmail by remember { mutableStateOf("client.review@coop.studio") }
        var attachContract by remember { mutableStateOf(true) }
        var isPackaging by remember { mutableStateOf(false) }
        var packageDone by remember { mutableStateOf(false) }
        val packageScope = rememberCoroutineScope()

        AlertDialog(
            onDismissRequest = { if (!isPackaging) showDeliverablePackagerDialog = false },
            containerColor = SurfaceColor,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Inventory, contentDescription = null, tint = AccentBlue)
                    Text("Production Deliverable Packager", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Compiles selected assets, creates signed client sign-off manifest, and links revenue split contracts from Agreement Vault.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = bundleName,
                        onValueChange = { bundleName = it },
                        label = { Text("Deliverable Bundle Title", color = TextSecondary, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = ColorDivider,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = clientEmail,
                        onValueChange = { clientEmail = it },
                        label = { Text("Client Delivery Email", color = TextSecondary, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = ColorDivider,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceLightColor, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Attach Vault Contract", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Auto-includes signed revenue split terms", color = TextSecondary, fontSize = 10.sp)
                        }
                        Switch(
                            checked = attachContract,
                            onCheckedChange = { attachContract = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AccentBlue)
                        )
                    }

                    if (packageDone) {
                        Surface(
                            color = NeonEmerald.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, NeonEmerald),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("BUNDLE READY FOR CLIENT SIGN-OFF", color = NeonEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Manifest Hash: SHA256-883F-COOP-DELIVER", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                Row(
                                    modifier = Modifier.clickable {
                                        try {
                                            uriHandler.openUri("mailto:veerendrabotla@gmail.com?subject=Co-Op%20Deliverable%20Escalation")
                                        } catch (e: Exception) {}
                                    },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Escalation Founder Contact: ", color = TextSecondary, fontSize = 9.sp)
                                    Text("veerendrabotla@gmail.com", color = AccentBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!packageDone) {
                            packageScope.launch {
                                isPackaging = true
                                delay(600)
                                isPackaging = false
                                packageDone = true
                            }
                        } else {
                            showDeliverablePackagerDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    enabled = !isPackaging
                ) {
                    Text(if (packageDone) "Done" else if (isPackaging) "Packaging..." else "Generate Package", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeliverablePackagerDialog = false }) {
                    Text("Close", color = TextSecondary)
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
                IconButton(onClick = onDelete, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete asset", tint = TextMuted, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
