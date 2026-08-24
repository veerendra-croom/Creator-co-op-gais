package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.data.model.Post
import com.example.ui.theme.*
import com.example.ui.viewmodels.*
import com.example.ui.components.AdBanner

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CreatorCommonsScreen(
    feedViewModel: CommunityFeedViewModel,
    adminViewModel: AdminViewModel,
    globalViewModel: GlobalViewModel,
    authViewModel: AuthViewModel,
    userProfile: com.example.data.model.UserProfile?,
    modifier: Modifier = Modifier
) {
    val posts = feedViewModel.postsList.collectAsLazyPagingItems()
    val placements by globalViewModel.allAdPlacements.collectAsState()
    val settings by globalViewModel.globalSettings.collectAsState()
    val isPremium = userProfile?.isVerifiedPro ?: false
    val currentTab by feedViewModel.currentFeedTab.collectAsState()
    val selectedSpace by feedViewModel.selectedSpaceName.collectAsState()
    
    var isChecklistDismissed by remember { mutableStateOf(false) }
    var hasAppliedReferral by remember { mutableStateOf(false) }

    LaunchedEffect(userProfile) {
        if (userProfile != null) {
            isChecklistDismissed = globalViewModel.getOnboardingChecklistDismissed(userProfile.id)
            hasAppliedReferral = globalViewModel.hasAppliedReferralCode(userProfile.id)
        }
    }
    
    val categories = listOf("All", "Cinematography", "3D Modeling", "VFX Hardware", "Agreements", "Storytelling")
    var showCreatePostDialog by remember { mutableStateOf(false) }
    var selectedPost by remember { mutableStateOf<Post?>(null) }
    var reportTarget by remember { mutableStateOf<Pair<String, String>?>(null) }

    Scaffold(
        containerColor = PrimaryBackground,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreatePostDialog = true },
                containerColor = AccentBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, "Post") },
                text = { Text("Share Idea", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("create_post_fab")
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Header Section
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        "CREATOR COMMONS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = AccentBlue,
                        letterSpacing = 2.sp
                    )
                    Text(
                        "Digital Forge",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            // Onboarding Checklist
            if (userProfile != null && !isChecklistDismissed) {
                item {
                    val isEmailConfirmed = true
                    val isProfileCompleted = userProfile.displayName.isNotBlank() && 
                                             userProfile.primarySpecialty.isNotBlank() && 
                                             userProfile.bio.isNotBlank()
                    val completedCount = (if (isEmailConfirmed) 1 else 0) + (if (isProfileCompleted) 1 else 0) + (if (hasAppliedReferral) 1 else 0)
                    val progressFraction = completedCount / 3f
                    val onboardingFinished = isProfileCompleted && hasAppliedReferral
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ColorDivider),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("onboarding_checklist_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Onboarding Checklist",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 15.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "$completedCount of 3 tasks completed",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                                if (onboardingFinished) {
                                    IconButton(
                                        onClick = {
                                            globalViewModel.dismissOnboardingChecklist(userProfile.id)
                                            isChecklistDismissed = true
                                        },
                                        modifier = Modifier.testTag("dismiss_checklist_button")
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = TextSecondary, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))

                            // Animated Setup Progress
                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (onboardingFinished) ColorSuccess else AccentBlue,
                                trackColor = SurfaceLightColor
                            )
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            OnboardingChecklistItem(
                                title = "Confirm Email",
                                description = "Email connection active.",
                                isChecked = true,
                                actionLabel = null,
                                onAction = {}
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OnboardingChecklistItem(
                                title = "Complete Profile",
                                description = "Add your display name, bio, and main specialty in Dashboard.",
                                isChecked = isProfileCompleted,
                                actionLabel = if (!isProfileCompleted) "EDIT" else null,
                                onAction = { globalViewModel.navigateToTab("PROFILE") }
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OnboardingChecklistItem(
                                title = "Apply Referral Code",
                                description = "Enter a friend's referral code in your Dashboard.",
                                isChecked = hasAppliedReferral,
                                actionLabel = if (!hasAppliedReferral) "APPLY" else null,
                                onAction = { globalViewModel.navigateToTab("PROFILE") }
                            )
                            
                            if (onboardingFinished) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    color = ColorSuccess.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ColorSuccess.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ColorSuccess, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                "🎉 Fully set up!",
                                                fontWeight = FontWeight.Bold,
                                                color = ColorSuccess,
                                                fontSize = 13.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "You're ready for co-op success. Tap the close icon to dismiss this widget.",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Trending Carousel
            item {
                TrendingSection()
            }

            // Sticky Category & Sort Tabs
            stickyHeader {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryBackground)
                    .padding(vertical = 12.dp)
                ) {
                    TabRow(
                        selectedTabIndex = if (currentTab == "TRENDING") 0 else if (currentTab == "NEW") 1 else 2,
                        containerColor = Color.Transparent,
                        contentColor = AccentBlue,
                        divider = {},
                        indicator = { tabPositions ->
                            if (tabPositions.isNotEmpty()) {
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[if (currentTab == "TRENDING") 0 else if (currentTab == "NEW") 1 else 2]),
                                    color = AccentBlue
                                )
                            }
                        }
                    ) {
                        Tab(
                            selected = currentTab == "TRENDING",
                            onClick = { feedViewModel.currentFeedTab.value = "TRENDING" },
                            text = { Text("Trending", fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = currentTab == "NEW",
                            onClick = { feedViewModel.currentFeedTab.value = "NEW" },
                            text = { Text("Recent", fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = currentTab == "TOP",
                            onClick = { feedViewModel.currentFeedTab.value = "TOP" },
                            text = { Text("Legendary", fontWeight = FontWeight.Bold) }
                        )
                    }

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedSpace == cat,
                                onClick = { feedViewModel.selectedSpaceName.value = cat },
                                label = { Text(cat) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentBlue.copy(alpha = 0.2f),
                                    selectedLabelColor = AccentBlue,
                                    containerColor = SurfaceColor,
                                    labelColor = TextSecondary
                                ),
                                border = if (selectedSpace == cat) BorderStroke(1.dp, AccentBlue) else null
                            )
                        }
                    }
                }
            }

            item {
                AdBanner("commons_top", "COMMONS", isPremium, placements, settings)
                Spacer(modifier = Modifier.height(20.dp))
            }

            if (posts.itemCount == 0 && posts.loadState.refresh is androidx.paging.LoadState.NotLoading) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceColor.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 32.dp)
                            .testTag("commons_empty_state_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forum,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "No ideas or announcements in this space yet.",
                                color = TextMuted,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Be the first to share an idea with the co-op by tapping 'Share Idea' below!",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Feed Items
            items(
                count = posts.itemCount,
                key = posts.itemKey { it.id }
            ) { index ->
                val post = posts[index]
                if (post != null) {
                    CommonsPostCard(
                        post = post,
                        onVote = { type -> feedViewModel.votePost(post.id, type) },
                        onClick = { selectedPost = post },
                        onReport = { reportTarget = "POST" to post.id }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            item {
                if (posts.loadState.append is LoadState.Loading) {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentBlue)
                    }
                }
            }
        }
    }

    // Modal Triggers
    if (reportTarget != null) {
        ReportDialog(
            targetType = reportTarget!!.first,
            onDismiss = { reportTarget = null },
            onSubmit = { reason ->
                val reporterId = authViewModel.currentUserId.value ?: "me"
                adminViewModel.reportContent(reportTarget!!.first, reportTarget!!.second, reason, reporterId)
                reportTarget = null
            }
        )
    }

    val featureFlags by globalViewModel.featureFlags.collectAsState(initial = emptyList())

    if (showCreatePostDialog) {
        CreatePostDialog(
            onDismiss = { showCreatePostDialog = false },
            featureFlags = featureFlags,
            userRole = userProfile?.systemRole ?: "PARTICIPANT",
            onPost = { title, body ->
                feedViewModel.submitPost(title, body, if (selectedSpace == "All") "General" else selectedSpace, userProfile)
                showCreatePostDialog = false
            }
        )
    }

    if (selectedPost != null) {
        PostDetailDialog(
            post = selectedPost!!,
            feedViewModel = feedViewModel,
            adminViewModel = adminViewModel,
            authViewModel = authViewModel,
            userProfile = userProfile,
            onDismiss = { selectedPost = null }
        )
    }
}

@Composable
fun ReportDialog(targetType: String, onDismiss: () -> Unit, onSubmit: (String) -> Unit) {
    var reason by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceColor,
        title = { Text("Report $targetType", color = Color.White) },
        text = {
            Column {
                Text("Why are you reporting this content?", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, 
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentBlue
                    ),
                    placeholder = { Text("Enter reason...", color = TextSecondary) }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(reason) }, 
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed), 
                enabled = reason.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Submit Report")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
fun PostDetailDialog(
    post: Post, 
    feedViewModel: CommunityFeedViewModel, 
    adminViewModel: AdminViewModel, 
    authViewModel: AuthViewModel, 
    userProfile: com.example.data.model.UserProfile?, 
    onDismiss: () -> Unit
) {
    val comments by remember(post.id) { feedViewModel.getCommentsForPost(post.id) }.collectAsState(initial = emptyList())
    var commentText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PrimaryBackground,
        modifier = Modifier.fillMaxSize(),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onDismiss) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                Text("Discussion", color = Color.White, fontWeight = FontWeight.Black)
                IconButton(onClick = { /* Share */ }) { Icon(Icons.Default.Share, null, tint = Color.White) }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    contentPadding = PaddingValues(vertical = 16.dp, horizontal = 4.dp)
                ) {
                    item {
                        CommonsPostCard(
                            post = post, 
                            onVote = { feedViewModel.votePost(post.id, it) }, 
                            onClick = {}, 
                            onReport = { 
                                val reporterId = authViewModel.currentUserId.value ?: "me"
                                adminViewModel.reportContent("POST", post.id, "Reported from Detail", reporterId) 
                            }
                        )
                    }
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 20.dp)) {
                            Text("COMMENTS", fontSize = 12.sp, fontWeight = FontWeight.Black, color = AccentBlue, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.height(1.dp).weight(1f).background(ColorDivider))
                        }
                    }
                    items(comments) { comment ->
                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(AccentBlue.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Person, null, tint = AccentBlue, modifier = Modifier.size(14.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(comment.authorName, color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(comment.authorRole, color = AccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(onClick = { 
                                    val reporterId = authViewModel.currentUserId.value ?: "me"
                                    adminViewModel.reportContent("COMMENT", comment.id, "Reported from discussion", reporterId) 
                                }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Flag, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                                }
                            }
                            Text(comment.text, color = Color.White, fontSize = 15.sp, modifier = Modifier.padding(top = 8.dp, start = 34.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(modifier = Modifier.padding(start = 34.dp).height(0.5.dp).fillMaxWidth().background(ColorDivider))
                        }
                    }
                }
                
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    color = SurfaceColor,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, ColorDivider)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            placeholder = { Text("Contribute to the forge...", fontSize = 14.sp) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, 
                                unfocusedTextColor = Color.White, 
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            maxLines = 3
                        )
                        IconButton(
                            onClick = {
                                if (commentText.isNotBlank()) {
                                    feedViewModel.submitComment(post.id, commentText, userProfile)
                                    commentText = ""
                                }
                            },
                            enabled = commentText.isNotBlank(),
                            modifier = Modifier.background(if(commentText.isNotBlank()) AccentBlue else SurfaceLightColor, CircleShape)
                        ) {
                            Icon(Icons.Default.Send, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun CreatePostDialog(
    onDismiss: () -> Unit,
    onPost: (String, String) -> Unit,
    featureFlags: List<com.example.data.model.FeatureFlag> = emptyList(),
    userRole: String = "PARTICIPANT"
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PrimaryBackground,
        modifier = Modifier.fillMaxSize(),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, tint = Color.White) }
                Text("Craft New Update", color = Color.White, fontWeight = FontWeight.Black)
                TextButton(
                    onClick = { onPost(title, body) },
                    enabled = title.isNotBlank() && body.isNotBlank()
                ) {
                    Text("PUBLISH", fontWeight = FontWeight.Black, color = if(title.isNotBlank() && body.isNotBlank()) AccentBlue else TextSecondary)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                com.example.ui.components.FeatureGate(
                    flagKey = "COMMUNITY_FORUM_POSTING",
                    featureFlags = featureFlags,
                    userRole = userRole,
                    showBannerOnRestricted = true,
                    customRestrictedNotice = "Forum posting and publishing is temporarily restricted by platform administration."
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        TextField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = { Text("Catchy Title", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = AccentBlue
                            ),
                            textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
                        )
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = ColorDivider)
                        
                        TextField(
                            value = body,
                            onValueChange = { body = it },
                            placeholder = { Text("What's on the workbench?", fontSize = 16.sp) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = AccentBlue
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun TrendingSection() {
    val context = androidx.compose.ui.platform.LocalContext.current
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Whatshot, null, tint = AccentRed, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("VIRAL DISCUSSIONS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = TextSecondary)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(listOf(
                "Best 4K Camera for 2026 Indie Shoots",
                "How to negotiate equity with VFX houses",
                "New AI upscaling plugin released!"
            )) { title ->
                Box(
                    modifier = Modifier
                        .width(280.dp)
                        .height(140.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(SurfaceColor, AdminObsidian)
                            )
                        )
                        .border(1.dp, ColorDivider, RoundedCornerShape(24.dp))
                        .clickable { 
                            android.widget.Toast.makeText(context, "Joining the viral discussion: $title", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                        Text(
                            title,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, null, tint = AccentBlue, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("High Engagement", fontSize = 10.sp, color = AccentBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommonsPostCard(post: Post, onVote: (String) -> Unit, onClick: () -> Unit, onReport: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, ColorDivider)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AccentBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, tint = AccentBlue, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(post.authorName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(post.authorRole, color = AccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(" • ", color = TextSecondary)
                        Text(com.example.util.DateTimeUtils.getRelativeTimeSpanString(post.timestamp), color = TextSecondary, fontSize = 10.sp)
                    }
                }
                
                var showMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.MoreVert, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(SurfaceColor)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Report Content", color = AccentRed) },
                            onClick = {
                                showMenu = false
                                onReport()
                            },
                            leadingIcon = { Icon(Icons.Default.Flag, null, tint = AccentRed) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                post.title,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp,
                lineHeight = 24.sp
            )
            
            val badgeColor = when(post.spaceName) {
                "Agreements" -> ColorError
                "VFX Hardware" -> CrispAmber
                else -> AccentBlue
            }
            
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("#${post.spaceName.uppercase()}", color = badgeColor, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
            }
            
            Text(
                post.body,
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                ) {
                    IconButton(onClick = { onVote("up") }, modifier = Modifier.size(44.dp)) {
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            null,
                            tint = if (post.userVote == "up") AccentBlue else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        "${post.upvotes - post.downvotes}",
                        color = if (post.userVote != "none") Color.White else TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                    IconButton(onClick = { onVote("down") }, modifier = Modifier.size(44.dp)) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            null,
                            tint = if (post.userVote == "down") AccentRed else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    IconButton(onClick = onClick) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.ChatBubbleOutline, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                            if (post.commentCount > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(post.commentCount.toString(), color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                        val context = androidx.compose.ui.platform.LocalContext.current
                        IconButton(
                            onClick = { 
                                val sendIntent = android.content.Intent().apply {
                                    action = android.content.Intent.ACTION_SEND
                                    putExtra(android.content.Intent.EXTRA_TITLE, post.title)
                                    putExtra(android.content.Intent.EXTRA_TEXT, "✨ ${post.title}\n\n${post.body}\n\nShared via Creator Co-Op (${post.spaceName})")
                                    type = "text/plain"
                                }
                                val shareIntent = android.content.Intent.createChooser(sendIntent, "Share via Creator Co-Op")
                                context.startActivity(shareIntent)
                            }
                        ) {
                            Icon(Icons.Outlined.Share, contentDescription = "Share Post", tint = TextSecondary, modifier = Modifier.size(20.dp))
                        }
                }
            }
        }
    }
}

@Composable
fun OnboardingChecklistItem(
    title: String,
    description: String,
    isChecked: Boolean,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = if (isChecked) "Completed" else "Incomplete",
                tint = if (isChecked) ColorSuccess else TextSecondary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = if (isChecked) TextSecondary else Color.White,
                    fontSize = 13.sp,
                    style = TextStyle(textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None)
                )
                Text(
                    text = description,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        if (!isChecked && actionLabel != null) {
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceLightColor),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text(
                    text = actionLabel,
                    color = AccentBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }
    }
}
