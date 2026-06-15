package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.MainViewModel
import com.example.data.model.Post
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SquareScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val selectedPostId by viewModel.selectedPostId.collectAsState()

    AnimatedContent(
        targetState = selectedPostId,
        transitionSpec = {
            slideInHorizontally { width -> if (selectedPostId != null) width else -width } + fadeIn() togetherWith
                    slideOutHorizontally { width -> if (selectedPostId != null) -width else width } + fadeOut()
        },
        label = "post_view_depth"
    ) { targetPostId ->
        if (targetPostId == null) {
            ForumFeedView(viewModel, modifier)
        } else {
            PostDetailView(viewModel, modifier)
        }
    }
}

// MAIN FEED VIEW (LIST & CREATE MODAL)
@Composable
fun ForumFeedView(viewModel: MainViewModel, modifier: Modifier) {
    val posts by viewModel.postsList.collectAsState()
    val currentTab by viewModel.currentFeedTab.collectAsState()
    val activeSpace by viewModel.selectedSpaceName.collectAsState()
    
    var showCreateDialog by remember { mutableStateOf(false) }

    val spaces = listOf("All", "Editing", "Gaming", "Writing")

    Box(modifier = modifier.fillMaxSize().background(PrimaryBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "The Square Forum",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                        )
                        Text(
                            text = "Supabase Secured",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                IconButton(
                    onClick = { viewModel.syncWithSupabase() },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = SurfaceColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = "Sync with Supabase",
                        tint = Color.White
                    )
                }
            }

            // Top Bar selector: Trending, New, Top
            TabRow(
                selectedTabIndex = when (currentTab) {
                    "NEW" -> 1
                    "TOP" -> 2
                    else -> 0
                },
                containerColor = PrimaryBackground,
                contentColor = AccentRed,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[when (currentTab) {
                            "NEW" -> 1
                            "TOP" -> 2
                            else -> 0
                        }]),
                        color = AccentRed
                    )
                }
            ) {
                listOf("TRENDING" to "Trending", "NEW" to "New", "TOP" to "Top").forEach { (tabId, label) ->
                    Tab(
                        selected = currentTab == tabId,
                        onClick = { viewModel.currentFeedTab.value = tabId },
                        text = { Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                    )
                }
            }

            // Spaces Horizontal Scroll Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(spaces) { space ->
                    val isSelected = activeSpace == space
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) AccentRed else SurfaceColor)
                            .clickable { viewModel.selectedSpaceName.value = space }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("space_filter_$space")
                    ) {
                        Text(
                            text = if (space == "All") "🏠 All Spaces" else "#$space",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Central Feed
            if (posts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Menu, contentDescription = "Empty", tint = TextSecondary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No matching posts yet.", color = TextSecondary, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(posts) { post ->
                        PostCard(post, viewModel)
                    }
                }
            }
        }

        // Floating Action Button to post in forum
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = AccentRed,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("create_post_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Create Post")
        }

        if (showCreateDialog) {
            CreateForumPostDialog(
                viewModel = viewModel,
                onDismiss = { showCreateDialog = false }
            )
        }
    }
}

// FORUM POST ITEM CARD
@Composable
fun PostCard(post: Post, viewModel: MainViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.selectedPostId.value = post.id }
            .testTag("post_card_${post.id}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Author Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model = post.authorAvatarUrl,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .border(1.dp, AccentBlue, CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(post.authorName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(AccentBlue)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(post.authorRole, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Text(
                        text = "in #${post.spaceName}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post content
            Text(
                text = post.title,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = post.body,
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Video indicator if has feedback URL
            if (post.mediaUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceLightColor)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.PlayCircle, contentDescription = "Draft Video", tint = AccentRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Interactive Video draft feedback enabled", color = AccentRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action row: upvote, downvote, comments
            HorizontalDivider(color = ColorDivider)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vote panel
                val isUpvoted = post.userVote == "up"
                val isDownvoted = post.userVote == "down"
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.handleUpvote(post.id) },
                        modifier = Modifier.size(36.dp).testTag("upvote_${post.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = "Upvote",
                            tint = if (isUpvoted) AccentRed else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = (post.upvotes - post.downvotes).toString(),
                        color = if (isUpvoted) AccentRed else if (isDownvoted) AccentBlue else TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    IconButton(
                        onClick = { viewModel.handleDownvote(post.id) },
                        modifier = Modifier.size(36.dp).testTag("downvote_${post.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbDown,
                            contentDescription = "Downvote",
                            tint = if (isDownvoted) AccentBlue else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Comments badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comments",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "${post.commentCount} Comments", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// POST DETAIL AND FEEDBACK VIEW
@Composable
fun PostDetailView(viewModel: MainViewModel, modifier: Modifier) {
    val post by viewModel.selectedPost.collectAsState()
    val comments by viewModel.selectedPostComments.collectAsState()
    var newCommentText by remember { mutableStateOf("") }
    
    // Video player state simulation
    var isPlaying by remember { mutableStateOf(false) }
    var currentPlaybackSec by remember { mutableStateOf(0) }
    var videoSelectionSec by remember { mutableStateOf(5) } // Default slider selection for feedback link

    if (post == null) return

    Box(modifier = modifier.fillMaxSize().background(PrimaryBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.selectedPostId.value = null }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("Post Thread", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Post details
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceColor)
                            .padding(16.dp)
                    ) {
                        // Author
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = post!!.authorAvatarUrl,
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(post!!.authorName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Shared in #${post!!.spaceName}", color = TextSecondary, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(post!!.title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 26.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(post!!.body, color = TextSecondary, fontSize = 15.sp, lineHeight = 22.sp)

                        // INTERACTIVE VIDEO FEEDBACK TOOL (If mediaUrl exists!)
                        if (post!!.mediaUrl.isNotBlank()) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth().border(1.dp, ColorDivider, RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(containerColor = PrimaryBackground),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "📺 COLLABORATIVE FEEDBACK PLAYER",
                                            color = AccentRed,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "0:0$currentPlaybackSec / 0:30",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Dynamic simulated video layout
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(120.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            IconButton(onClick = { isPlaying = !isPlaying }) {
                                                Icon(
                                                    imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                                    contentDescription = "Simulated Playback",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(48.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Interactive slider to define video critique timestamp
                                    Text(
                                        text = "Pin critique feedback to timestamp: 0:0$videoSelectionSec",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Slider(
                                        value = videoSelectionSec.toFloat(),
                                        onValueChange = { videoSelectionSec = it.toInt() },
                                        valueRange = 1f..30f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = AccentRed,
                                            activeTrackColor = AccentRed,
                                            inactiveTrackColor = ColorDivider
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Nested reviews/comments section
                item {
                    Text("REVIEWS & DISCUSSION (${comments.size})", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                }

                items(comments) { comment ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceColor.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(comment.authorName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                if (comment.timestampMs != null) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(AccentRed.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("Critique @ 0:0${comment.timestampMs}", color = AccentRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(comment.text, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
                        }
                    }
                }
            }

            // Chat/Comment entry area at bottom
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        modifier = Modifier.weight(1f).testTag("new_comment_input"),
                        placeholder = { Text("Write comment review...", color = TextSecondary, fontSize = 13.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newCommentText.isNotBlank()) {
                                val timestampToAttach = if (post!!.mediaUrl.isNotBlank()) videoSelectionSec.toLong() else null
                                viewModel.addPostComment(post!!.id, newCommentText, timestampToAttach)
                                newCommentText = ""
                            }
                        },
                        modifier = Modifier.testTag("submit_comment_button")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = AccentRed)
                    }
                }
            }
        }
    }
}

// CREATE POST DIALOG MODEL
@Composable
fun CreateForumPostDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var space by remember { mutableStateOf("Editing") }
    var mediaUrl by remember { mutableStateOf("") } // Interactive feedback video URL (Optional)

    val subSpaces = listOf("Editing", "Gaming", "Writing")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceColor,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Publish to The Square Forum",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Discussion Title *", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentRed,
                        unfocusedBorderColor = ColorDivider
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_post_title")
                )

                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Discussion Body *", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentRed,
                        unfocusedBorderColor = ColorDivider
                    ),
                    modifier = Modifier.fillMaxWidth().height(100.dp).testTag("add_post_body")
                )

                Text("Target Sub-Forum Space *", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(subSpaces) { s ->
                        val isSelected = s == space
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) AccentRed else ColorDivider)
                                .clickable { space = s }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(s, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = mediaUrl,
                    onValueChange = { mediaUrl = it },
                    label = { Text("Interactive Video URL / Draft link (Optional)", color = TextSecondary) },
                    placeholder = { Text("https://example.com/video.mp4", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentRed,
                        unfocusedBorderColor = ColorDivider
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && body.isNotBlank()) {
                        viewModel.createForumPost(title, body, space, mediaUrl)
                        onDismiss()
                    } else {
                        viewModel.toastMessage.value = "Please complete all required fields."
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
            ) {
                Text("Publish Post", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
