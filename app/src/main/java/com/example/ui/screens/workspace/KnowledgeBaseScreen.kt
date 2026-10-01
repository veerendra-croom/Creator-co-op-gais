package com.example.ui.screens.workspace

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.ui.theme.*
import com.example.ui.viewmodels.WorkspaceViewModel
import com.example.data.model.UserSetting

@Composable
fun KnowledgeBaseScreen(
    onBack: () -> Unit,
    workspaceViewModel: WorkspaceViewModel? = null,
    userId: String = ""
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var showTaskGeneratorDialog by remember { mutableStateOf(false) }
    var showCreateManualDialog by remember { mutableStateOf(false) }
    var selectedTemplateArticle by remember { mutableStateOf<ArticleItemData?>(null) }

    val allArticles = remember {
        listOf(
            ArticleItemData("Syndicate Onboarding", "Standard procedure for new shard members.", "GOVERNANCE"),
            ArticleItemData("Revenue Split Anchoring", "How to verify and lock programmatic splits.", "FINANCE"),
            ArticleItemData("Dispute Resolution", "Protocol for resolving creative differences.", "LEGAL"),
            ArticleItemData("Production Quality Gates", "Standards for moving tasks to PUBLISH lane.", "QUALITY"),
            ArticleItemData("Workflow Standards", "Ensuring deterministic pipeline execution across video shards.", "STANDARDS")
        )
    }

    val docTemplates = remember {
        listOf(
            ArticleItemData("Production Brief Template", "Pre-formatted creative brief with goals & shotlists", "TEMPLATE"),
            ArticleItemData("Sponsorship Contract Brief", "Legal terms & deliverable schedule outline", "TEMPLATE"),
            ArticleItemData("Daily Shoot Call Sheet", "Location, timeline, cast & gear setup checklist", "TEMPLATE")
        )
    }

    var customArticles by remember { mutableStateOf(emptyList<ArticleItemData>()) }

    // Load custom articles from Local Room Settings
    LaunchedEffect(userId) {
        if (userId.isNotEmpty() && workspaceViewModel != null) {
            try {
                val saved = workspaceViewModel.repository.userSettingsDao.getSetting(userId, "custom_articles")
                if (!saved.isNullOrBlank()) {
                    val list = saved.split("###").mapNotNull {
                        val parts = it.split("|||")
                        if (parts.size == 3) ArticleItemData(parts[0], parts[1], parts[2]) else null
                    }
                    customArticles = list
                }
            } catch (e: Exception) {
                // Ignore error, fallback to empty
            }
        }
    }

    // Save helper for custom articles
    val saveCustomArticles = { newList: List<ArticleItemData> ->
        customArticles = newList
        if (userId.isNotEmpty() && workspaceViewModel != null) {
            val serialized = newList.joinToString("###") { "${it.title}|||${it.subtitle}|||${it.category}" }
            scope.launch {
                try {
                    workspaceViewModel.repository.userSettingsDao.setSetting(
                        UserSetting(id = "${userId}_custom_articles", userId = userId, key = "custom_articles", value = serialized)
                    )
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    val articles = remember(searchQuery, customArticles) {
        val fullList = allArticles + docTemplates + customArticles
        if (searchQuery.isEmpty()) fullList
        else fullList.filter { 
            it.title.contains(searchQuery, ignoreCase = true) || 
            it.category.contains(searchQuery, ignoreCase = true) 
        }
    }

    val categories = remember(customArticles) {
        (allArticles + docTemplates + customArticles).map { it.category }.distinct()
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("knowledge_base_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("KNOWLEDGE BASE", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { showCreateManualDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceLightColor),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, tint = Color.White, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Manual", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                }

                Button(
                    onClick = { showTaskGeneratorDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, tint = Color.White, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Generate Plan", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search manuals, templates, protocols...", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, tint = TextSecondary, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentRed,
                unfocusedBorderColor = ColorDivider,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text("OPERATIONAL CATEGORIES", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        AssistChip(
                            onClick = { searchQuery = cat },
                            label = { Text(cat) },
                            colors = AssistChipDefaults.assistChipColors(labelColor = AccentBlue),
                            border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.3f))
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                Text("PROCEDURAL MANUALS", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (articles.isEmpty()) {
                item {
                    Text("No matching protocols found in archive.", color = TextSecondary, fontSize = 14.sp)
                }
            } else {
                items(articles, key = { it.title }) { article ->
                    ArticleItem(title = article.title, subtitle = article.subtitle, category = article.category)
                }
            }
        }
    }

    if (showTaskGeneratorDialog) {
        val formats = listOf("YouTube Long-Form Video", "Short-Form Reel/TikTok", "Podcast Episode", "Commercial Spot")
        var selectedFormat by remember { mutableStateOf(formats.first()) }
        var isGenerating by remember { mutableStateOf(false) }
        var generatedCount by remember { mutableStateOf(0) }
        val scope = rememberCoroutineScope()

        AlertDialog(
            onDismissRequest = { if (!isGenerating) showTaskGeneratorDialog = false },
            containerColor = SurfaceColor,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentBlue)
                    Text("Task Breakdown Generator", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Auto-generates structured sub-tasks, scene breakdown checklists, and estimated role timelines directly into TeamSpace Kanban.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Text("SELECT PRODUCTION FORMAT", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    formats.forEach { fmt ->
                        val isSelected = selectedFormat == fmt
                        Surface(
                            onClick = { selectedFormat = fmt },
                            color = if (isSelected) AccentBlue.copy(alpha = 0.15f) else SurfaceLightColor,
                            border = BorderStroke(1.dp, if (isSelected) AccentBlue else ColorDivider),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(fmt, color = Color.White, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                if (isSelected) Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    if (generatedCount > 0) {
                        Surface(
                            color = NeonEmerald.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, NeonEmerald),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "✔ $generatedCount production tasks successfully synced to TeamSpace Kanban!",
                                color = NeonEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (generatedCount == 0) {
                            scope.launch {
                                isGenerating = true
                                kotlinx.coroutines.delay(400)
                                val templates = com.example.data.engine.LocalProductionEngine.generateTaskBreakdown(selectedFormat)
                                workspaceViewModel?.let { vm ->
                                    templates.forEach { tmpl ->
                                        vm.submitTask(
                                            title = tmpl.title,
                                            body = "${tmpl.description} (Est: ${tmpl.estimatedHours}h)",
                                            scope = "PRODUCTION_READY",
                                            userId = userId,
                                            kanbanLane = tmpl.columnCategory
                                        )
                                    }
                                    generatedCount = templates.size
                                } ?: run {
                                    generatedCount = templates.size
                                }
                                isGenerating = false
                            }
                        } else {
                            showTaskGeneratorDialog = false
                            generatedCount = 0
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    enabled = !isGenerating
                ) {
                    Text(if (generatedCount > 0) "Done" else if (isGenerating) "Generating..." else "Auto-Generate Tasks", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTaskGeneratorDialog = false }) {
                    Text("Close", color = TextSecondary)
                }
            }
        )
    }

    if (showCreateManualDialog) {
        var manualTitle by remember { mutableStateOf("") }
        var manualSubtitle by remember { mutableStateOf("") }
        var manualCategory by remember { mutableStateOf("GOVERNANCE") }
        val categoryOptions = listOf("GOVERNANCE", "FINANCE", "LEGAL", "QUALITY", "TECH", "TEMPLATE")

        AlertDialog(
            onDismissRequest = { showCreateManualDialog = false },
            containerColor = SurfaceColor,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = AccentBlue)
                    Text("Add Custom Manual", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = manualTitle,
                        onValueChange = { manualTitle = it },
                        label = { Text("Title", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentRed,
                            unfocusedBorderColor = ColorDivider,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = manualSubtitle,
                        onValueChange = { manualSubtitle = it },
                        label = { Text("Description / Protocol Guideline", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentRed,
                            unfocusedBorderColor = ColorDivider,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Text("SELECT CATEGORY", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categoryOptions.forEach { opt ->
                            val isSelected = manualCategory == opt
                            FilterChip(
                                selected = isSelected,
                                onClick = { manualCategory = opt },
                                label = { Text(opt) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentBlue.copy(alpha = 0.2f),
                                    selectedLabelColor = AccentBlue,
                                    containerColor = SurfaceLightColor,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (manualTitle.isNotBlank() && manualSubtitle.isNotBlank()) {
                            val newItem = ArticleItemData(manualTitle, manualSubtitle, manualCategory)
                            val newList = customArticles + newItem
                            saveCustomArticles(newList)
                            showCreateManualDialog = false
                            Toast.makeText(context, "Manual successfully added to Knowledge Ledger!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("Save Protocol", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateManualDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

data class ArticleItemData(val title: String, val subtitle: String, val category: String)

@Composable
fun ArticleItem(title: String, subtitle: String, category: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ColorDivider)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(AccentBlue.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoStories, null, tint = AccentBlue, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(category, color = AccentBlue, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}
