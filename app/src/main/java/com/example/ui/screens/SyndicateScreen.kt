package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.MainViewModel
import com.example.data.model.Project
import com.example.ui.theme.*
import java.util.UUID

@Composable
fun SyndicateScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val mode by viewModel.syndicateViewMode.collectAsState()
    val selectedId by viewModel.selectedProjectId.collectAsState()

    Box(modifier = modifier.fillMaxSize().background(PrimaryBackground)) {
        AnimatedContent(
            targetState = if (selectedId != null) "DETAILS" else mode,
            transitionSpec = {
                slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width } + fadeOut()
            },
            label = "syndicate_mode_view"
        ) { state ->
            when (state) {
                "PROJECTS" -> ProjectsFeedView(viewModel)
                "SWIPER" -> SwiperMatchingView(viewModel)
                "CREATE" -> CreateProjectPostingForm(viewModel)
                "DETAILS" -> ProjectDetailsView(viewModel)
            }
        }
    }
}

// TOGGLE SELECTOR SUB-COMPONENT
@Composable
fun ViewModeToggle(viewModel: MainViewModel) {
    val mode by viewModel.syndicateViewMode.collectAsState()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceColor)
            .padding(4.dp)
    ) {
        listOf("PROJECTS" to "Browse Board", "SWIPER" to "Co-Op Match", "CREATE" to "Post Project").forEach { (m, label) ->
            val isActive = mode == m
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isActive) AccentRed else Color.Transparent)
                    .clickable {
                        viewModel.clearProjectFormError()
                        viewModel.syndicateViewMode.value = m
                        viewModel.selectedProjectId.value = null
                    }
                    .padding(vertical = 10.dp)
                    .testTag("toggle_syndicate_mode_$m"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// BROWSE BOARD FEED
@Composable
fun ProjectsFeedView(viewModel: MainViewModel) {
    val projects by viewModel.filteredProjects.collectAsState()
    val activeFilter by viewModel.projectNicheFilter.collectAsState()
    val niches = listOf("All", "Tech", "Gaming", "Finance")

    Column(modifier = Modifier.fillMaxSize()) {
        ViewModeToggle(viewModel)

        // Niche Filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            niches.forEach { niche ->
                val isSelected = activeFilter == niche
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) AccentBlue else SurfaceColor)
                        .clickable { viewModel.projectNicheFilter.value = niche }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(niche, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (projects.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No dynamic projects found. Post one!", color = TextSecondary, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(projects) { project ->
                    ProjectItemCard(project = project, onClick = {
                        viewModel.selectedProjectId.value = project.id
                    })
                }
            }
        }
    }
}

// PROJECT ITEM COMPONENT (WITH CUSTOM CANVAS EQUITY DONUT CHART)
@Composable
fun ProjectItemCard(project: Project, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("project_card_${project.id}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Column: Custom Canva Donut Chart
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    var startAngle = -90f
                    val sweep1 = (project.hostEquity / 100f) * 360f
                    val sweep2 = (project.editorEquity / 100f) * 360f
                    val sweep3 = (project.writerEquity / 100f) * 360f

                    // Draw Host
                    if (sweep1 > 0f) {
                        drawArc(
                            color = AccentRed,
                            startAngle = startAngle,
                            sweepAngle = sweep1,
                            useCenter = false,
                            style = Stroke(width = 16f)
                        )
                        startAngle += sweep1
                    }
                    // Draw Editor
                    if (sweep2 > 0f) {
                        drawArc(
                            color = AccentBlue,
                            startAngle = startAngle,
                            sweepAngle = sweep2,
                            useCenter = false,
                            style = Stroke(width = 16f)
                        )
                        startAngle += sweep2
                    }
                    // Draw Writer / Animators
                    if (sweep3 > 0f) {
                        drawArc(
                            color = ColorSuccess,
                            startAngle = startAngle,
                            sweepAngle = sweep3,
                            useCenter = false,
                            style = Stroke(width = 16f)
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "EQUITY", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(text = "100%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Right Column: Brief details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AccentRed.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(project.niche.uppercase(), color = AccentRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${project.subscriberCount / 1000}K Subs",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = project.title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = project.contentStrategy,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Small legend panel representation
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Host: ${project.hostEquity}%", color = AccentRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("Edit: ${project.editorEquity}%", color = AccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("Write: ${project.writerEquity}%", color = ColorSuccess, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Tinder-Style Full Card Swiper Matching View
@Composable
fun SwiperMatchingView(viewModel: MainViewModel) {
    val projects by viewModel.allProjects.collectAsState()
    val index by viewModel.swiperIndex.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        ViewModeToggle(viewModel)

        if (projects.isEmpty() || index >= projects.size) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = "Done", tint = ColorSuccess, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("You've reviewed all active syndicates!", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Tap post project or check browse boards", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.swiperIndex.value = 0 }, colors = ButtonDefaults.buttonColors(containerColor = AccentRed)) {
                        Text("Reset Deck", color = Color.White)
                    }
                }
            }
        } else {
            val project = projects[index]

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                // Background overlapping styling representation
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 12.dp)
                        .testTag("swiper_card"),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AccentRed)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("#${project.niche.uppercase()}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("${project.subscriberCount / 1000}K subscribers", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = project.title,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 28.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = project.contentStrategy,
                            color = TextSecondary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Custom chart drawing inside swiper card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("HOST", color = AccentRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text("${project.hostEquity}%", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("EDITOR", color = AccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text("${project.editorEquity}%", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("WRITER", color = ColorSuccess, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text("${project.writerEquity}%", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Actions panel layout
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.swiperIndex.value = index + 1 },
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceLightColor)
                                    .testTag("swipe_left_button")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Pass", tint = AccentRed, modifier = Modifier.size(28.dp))
                            }

                            IconButton(
                                onClick = {
                                    viewModel.swiperIndex.value = index + 1
                                    // Record Match automatically triggers workspace
                                    coroutineScope.launch {
                                        val mockApplicant = "Applicant_" + UUID.randomUUID().toString().substring(0,4)
                                        viewModel.repository.insertPitch(com.example.data.model.Pitch(
                                            id = "autogen-pitch-${project.id}",
                                            projectId = project.id,
                                            applicantId = "me",
                                            applicantName = "You",
                                            applicantRole = "Editor",
                                            applicantAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb",
                                            message = "Automatically matched via Swiper UI interest! Ready to start immediate cinematic cutting.",
                                            portfolioLink = "vimeo.com/cinematic_matches",
                                            status = "ACCEPTED",
                                            submittedAt = System.currentTimeMillis()
                                        ))
                                        viewModel.repository.updatePitchStatus("autogen-pitch-${project.id}", "ACCEPTED")
                                    }
                                    viewModel.toastMessage.value = "MUTUAL MATCH DETECTED! 🎉 Contract generated for project '${project.title}'!"
                                    viewModel.selectedWorkspaceId.value = project.id
                                    viewModel.currentTab.value = "WORKSPACES"
                                    viewModel.workspaceSubTab.value = "VAULT"
                                },
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(AccentRed)
                                    .testTag("swipe_right_button")
                            ) {
                                Icon(Icons.Default.Favorite, contentDescription = "Match", tint = Color.White, modifier = Modifier.size(34.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// DETAILED BRIEFING SCREEN FOR SINGLE DRILLDOWN
@Composable
fun ProjectDetailsView(viewModel: MainViewModel) {
    val project by viewModel.selectedProjectFlow.collectAsState()
    var pitchMessage by remember { mutableStateOf("") }
    var portfolioLink by remember { mutableStateOf("") }

    if (project == null) return

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.selectedProjectId.value = null }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Project Briefing & Pitch", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(project!!.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(project!!.contentStrategy, color = TextSecondary, fontSize = 14.sp, lineHeight = 20.sp)
                    }
                }
            }

            // Equity breakdown visualizer
            item {
                Text("EQUITY SPLIT STRUCTURE", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceColor)) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Column {
                            Text("Manager Host", color = AccentRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("${project!!.hostEquity}%", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        }
                        Column {
                            Text("Video Editor", color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("${project!!.editorEquity}%", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        }
                        Column {
                            Text("Script Writer", color = ColorSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("${project!!.writerEquity}%", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            // Input form to submit proposal / pitch
            item {
                Text("PROPOSE YOUR CO-OP PITCH", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceColor)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = pitchMessage,
                            onValueChange = { pitchMessage = it },
                            label = { Text("Short introduction & samples *", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = AccentRed,
                                unfocusedBorderColor = ColorDivider
                            ),
                            modifier = Modifier.fillMaxWidth().height(100.dp).testTag("pitch_message_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = portfolioLink,
                            onValueChange = { portfolioLink = it },
                            label = { Text("Demonstrative portfolio URL link", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = AccentRed,
                                unfocusedBorderColor = ColorDivider
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("pitch_portfolio_input")
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (pitchMessage.isNotBlank()) {
                                    viewModel.submitProjectPitch(project!!.id, pitchMessage, portfolioLink)
                                } else {
                                    viewModel.toastMessage.value = "Introduction explanation is mandatory!"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                            modifier = Modifier.fillMaxWidth().testTag("submit_pitch_button")
                        ) {
                            Text("Transmit Pitch Proposal", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// CREATE PROJECT POSTING FORM
@Composable
fun CreateProjectPostingForm(viewModel: MainViewModel) {
    var title by remember { mutableStateOf("") }
    var strategy by remember { mutableStateOf("") }
    var subscribers by remember { mutableStateOf("45000") }
    var niche by remember { mutableStateOf("Tech") }
    
    // Equity slots
    var hostPct by remember { mutableStateOf("40") }
    var editorPct by remember { mutableStateOf("30") }
    var writerPct by remember { mutableStateOf("30") }

    val errorState by viewModel.projectFormError.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        ViewModeToggle(viewModel)

        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Post a New Syndicate Project",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Define your strategic needs and set standard legal equity pieces.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            if (errorState != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ColorError.copy(alpha = 0.15f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, contentDescription = "Error", tint = ColorError)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(errorState!!, color = ColorError, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Project Title *", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentRed,
                        unfocusedBorderColor = ColorDivider
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_project_title")
                )
            }

            item {
                OutlinedTextField(
                    value = strategy,
                    onValueChange = { strategy = it },
                    label = { Text("Content Production Strategy *", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentRed,
                        unfocusedBorderColor = ColorDivider
                    ),
                    modifier = Modifier.fillMaxWidth().height(100.dp).testTag("input_project_brief")
                )
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = subscribers,
                        onValueChange = { subscribers = it },
                        label = { Text("Current Subscribers", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AccentRed,
                            unfocusedBorderColor = ColorDivider
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = niche,
                        onValueChange = { niche = it },
                        label = { Text("Niche (Tech, Gaming...)", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AccentRed,
                            unfocusedBorderColor = ColorDivider
                        ),
                        modifier = Modifier.weight(1f).testTag("input_project_niche")
                    )
                }
            }

            // Equity breakdown selectors (Sum check!)
            item {
                Text("EQUITY PERCENTAGE SLOTS (Saves balance & sum MUST Equal 100%)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hostPct,
                        onValueChange = { hostPct = it },
                        label = { Text("Host %", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentRed, unfocusedBorderColor = ColorDivider),
                        modifier = Modifier.weight(1f).testTag("host_pct_input")
                    )
                    OutlinedTextField(
                        value = editorPct,
                        onValueChange = { editorPct = it },
                        label = { Text("Editor %", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentRed, unfocusedBorderColor = ColorDivider),
                        modifier = Modifier.weight(1f).testTag("editor_pct_input")
                    )
                    OutlinedTextField(
                        value = writerPct,
                        onValueChange = { writerPct = it },
                        label = { Text("Writer %", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentRed, unfocusedBorderColor = ColorDivider),
                        modifier = Modifier.weight(1f).testTag("writer_pct_input")
                    )
                }
            }

            // Business rule validation helper
            item {
                val host = hostPct.toIntOrNull() ?: 0
                val edit = editorPct.toIntOrNull() ?: 0
                val write = writerPct.toIntOrNull() ?: 0
                val sum = host + edit + write
                val isExactly100 = sum == 100

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isExactly100) ColorSuccess.copy(alpha = 0.15f) else ColorWarning.copy(alpha = 0.15f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Real-time sum calculation: $sum% / 100% " + if (isExactly100) "(Perfect Alignment!)" else "(Must total exactly 100%!)",
                        color = if (isExactly100) ColorSuccess else ColorWarning,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        val hostVal = hostPct.toIntOrNull() ?: 0
                        val editVal = editorPct.toIntOrNull() ?: 0
                        val writerVal = writerPct.toIntOrNull() ?: 0
                        viewModel.addProjectPosting(title, niche, strategy, subscribers, hostVal, editVal, writerVal)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("submit_project_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Publish Project to Board", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
