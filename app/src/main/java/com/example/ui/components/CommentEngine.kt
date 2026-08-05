package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Comment
import com.example.data.model.UserProfile
import com.example.data.repository.AppRepository
import com.example.ui.theme.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.serialization.json.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CommentEngine(
    targetId: String,
    repository: AppRepository,
    currentUser: UserProfile?,
    allUsers: List<UserProfile>,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val commentsState by repository.getCommentsForEntity(targetId).collectAsState(initial = emptyList())
    
    var commentInputText by remember { mutableStateOf("") }
    var replyingToCommentId by remember { mutableStateOf<String?>(null) }
    var editingCommentId by remember { mutableStateOf<String?>(null) }
    
    // For mentions dropdown
    var showMentionsDropdown by remember { mutableStateOf(false) }
    var mentionSearchQuery by remember { mutableStateOf("") }
    
    val filteredUsers = remember(mentionSearchQuery, allUsers) {
        if (mentionSearchQuery.isEmpty()) {
            allUsers
        } else {
            allUsers.filter { 
                it.displayName.contains(mentionSearchQuery, ignoreCase = true) || 
                it.username.contains(mentionSearchQuery, ignoreCase = true) 
            }
        }
    }

    LaunchedEffect(commentInputText) {
        val lastWord = commentInputText.split(" ").lastOrNull() ?: ""
        if (lastWord.startsWith("@") && lastWord.length > 1) {
            showMentionsDropdown = true
            mentionSearchQuery = lastWord.drop(1)
        } else {
            showMentionsDropdown = false
        }
    }

    // Organize comments into top-level and replies
    val rootComments = remember(commentsState) {
        commentsState.filter { it.parentCommentId == null }.sortedBy { it.timestamp }
    }
    
    val repliesByParent = remember(commentsState) {
        commentsState.filter { it.parentCommentId != null }.groupBy { it.parentCommentId!! }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceColor, DS.RadiusMedium)
            .border(1.dp, ColorDivider.copy(alpha = 0.5f), DS.RadiusMedium)
            .padding(DS.Space16),
        verticalArrangement = Arrangement.spacedBy(DS.Space16)
    ) {
        // Comments Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Comment, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(20.dp))
                Text(
                    text = "Discussion (${commentsState.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }

        // Empty state
        if (rootComments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = DS.Space24),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No comments yet. Start the conversation below!",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        // Comments List
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(DS.Space12)
        ) {
            rootComments.forEach { comment ->
                CommentCard(
                    comment = comment,
                    replies = repliesByParent[comment.id] ?: emptyList(),
                    currentUser = currentUser,
                    allUsers = allUsers,
                    onReplyClick = { replyingToCommentId = comment.id },
                    onEditClick = { 
                        editingCommentId = comment.id
                        commentInputText = comment.text
                    },
                    onDeleteClick = {
                        coroutineScope.launch {
                            repository.deleteComment(comment.id)
                        }
                    },
                    onReactClick = { emoji ->
                        coroutineScope.launch {
                            toggleEmojiReaction(comment, emoji, currentUser?.id ?: "", repository)
                        }
                    }
                )
            }
        }

        // Input Field Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (replyingToCommentId != null) {
                val parentComment = commentsState.find { it.id == replyingToCommentId }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AccentBlue.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Replying to @${parentComment?.authorName ?: "user"}",
                        fontSize = 12.sp,
                        color = AccentBlue,
                        fontWeight = FontWeight.Medium
                    )
                    IconButton(
                        onClick = { replyingToCommentId = null },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel reply", tint = AccentBlue, modifier = Modifier.size(14.dp))
                    }
                }
            } else if (editingCommentId != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CrispAmber.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Editing comment...",
                        fontSize = 12.sp,
                        color = CrispAmber,
                        fontWeight = FontWeight.Medium
                    )
                    IconButton(
                        onClick = { 
                            editingCommentId = null 
                            commentInputText = ""
                        },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel edit", tint = CrispAmber, modifier = Modifier.size(14.dp))
                    }
                }
            }

            // Mentions Dropdown
            if (showMentionsDropdown && filteredUsers.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 150.dp)
                        .border(1.dp, ColorDivider, RoundedCornerShape(8.dp)),
                    color = SurfaceLightColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    LazyColumn {
                        items(filteredUsers.size) { index ->
                            val user = filteredUsers[index]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val words = commentInputText.split(" ").toMutableList()
                                        if (words.isNotEmpty()) {
                                            words[words.lastIndex] = "@${user.username}"
                                        }
                                        commentInputText = words.joinToString(" ") + " "
                                        showMentionsDropdown = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(AccentBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(user.displayName.take(1).uppercase(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(user.displayName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("@${user.username}", color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Text Input Box
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = commentInputText,
                    onValueChange = { commentInputText = it },
                    placeholder = { Text("Write a comment... (use @ to mention)", color = TextSecondary, fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("comment_input_text_field"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceLightColor,
                        unfocusedContainerColor = SurfaceLightColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 4
                )

                Button(
                    onClick = {
                        if (commentInputText.isBlank()) return@Button
                        coroutineScope.launch {
                            val userDisplayName = currentUser?.displayName ?: "Collaborator"
                            val userRole = currentUser?.primarySpecialty ?: "Creator"
                            
                            if (editingCommentId != null) {
                                val currentComm = commentsState.find { it.id == editingCommentId }
                                if (currentComm != null) {
                                    repository.insertComment(currentComm.copy(
                                        text = commentInputText,
                                        content = commentInputText,
                                        isEdited = true
                                    ))
                                }
                                editingCommentId = null
                            } else {
                                val comment = Comment(
                                    id = UUID.randomUUID().toString(),
                                    postId = targetId,
                                    authorId = currentUser?.id ?: "",
                                    authorName = userDisplayName,
                                    authorRole = userRole,
                                    text = commentInputText,
                                    content = commentInputText,
                                    timestamp = System.currentTimeMillis(),
                                    parentCommentId = replyingToCommentId,
                                    isEdited = false,
                                    emojiReactionsJson = "{}"
                                )
                                repository.insertComment(comment)
                            }
                            
                            commentInputText = ""
                            replyingToCommentId = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("comment_submit_button")
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun CommentCard(
    comment: Comment,
    replies: List<Comment>,
    currentUser: UserProfile?,
    allUsers: List<UserProfile>,
    onReplyClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onReactClick: (String) -> Unit,
    isReplyCard: Boolean = false
) {
    val formatter = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()) }
    val displayTime = remember(comment.timestamp) { formatter.format(Date(comment.timestamp)) }
    
    // Parse reactions safely
    val parsedReactions = remember(comment.emojiReactionsJson) {
        parseReactions(comment.emojiReactionsJson)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = if (isReplyCard) 24.dp else 0.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(if (isReplyCard) 28.dp else 36.dp)
                .clip(CircleShape)
                .background(AccentBlue.copy(alpha = 0.2f))
                .border(1.dp, AccentBlue.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = comment.authorName.take(1).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = if (isReplyCard) 12.sp else 14.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Content Column
        Column(modifier = Modifier.weight(1f)) {
            // Author info and timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = comment.authorName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = comment.authorRole,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = displayTime,
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                    if (comment.isEdited) {
                        Text(
                            text = "(edited)",
                            color = CrispAmber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Comment text
            Text(
                text = comment.text,
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Emoji Reactions Row
            if (parsedReactions.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    parsedReactions.forEach { (emoji, userList) ->
                        val hasReacted = userList.contains(currentUser?.id ?: "")
                        Surface(
                            modifier = Modifier
                                .clickable { onReactClick(emoji) },
                            color = if (hasReacted) AccentBlue.copy(alpha = 0.2f) else SurfaceLightColor,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, if (hasReacted) AccentBlue else Color.Transparent)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(emoji, fontSize = 12.sp)
                                Text(
                                    text = userList.size.toString(),
                                    color = if (hasReacted) Color.White else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Quick Actions & Quick Emoji Picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reply/Edit/Delete actions
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (!isReplyCard) {
                        Text(
                            text = "Reply",
                            color = AccentBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onReplyClick() }
                        )
                    }
                    if (comment.authorId == currentUser?.id) {
                        Text(
                            text = "Edit",
                            color = CrispAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onEditClick() }
                        )
                        Text(
                            text = "Delete",
                            color = AccentRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onDeleteClick() }
                        )
                    }
                }

                // Quick Emojis
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("👍", "🔥", "❤️", "😂").forEach { emoji ->
                        Text(
                            text = emoji,
                            modifier = Modifier
                                .clickable { onReactClick(emoji) }
                                .padding(2.dp),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Render Nested Replies
            if (replies.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    replies.forEach { reply ->
                        CommentCard(
                            comment = reply,
                            replies = emptyList(),
                            currentUser = currentUser,
                            allUsers = allUsers,
                            onReplyClick = {},
                            onEditClick = onEditClick,
                            onDeleteClick = { onDeleteClick() },
                            onReactClick = { emoji -> onReactClick(emoji) },
                            isReplyCard = true
                        )
                    }
                }
            }
        }
    }
}

// Reactions parsing helpers
private fun parseReactions(jsonStr: String): Map<String, List<String>> {
    return try {
        val json = Json.parseToJsonElement(jsonStr).jsonObject
        json.mapValues { entry ->
            entry.value.jsonArray.map { it.jsonPrimitive.content }
        }
    } catch (e: Exception) {
        emptyMap()
    }
}

private suspend fun toggleEmojiReaction(
    comment: Comment,
    emoji: String,
    userId: String,
    repository: AppRepository
) {
    if (userId.isEmpty()) return
    val currentReactions = parseReactions(comment.emojiReactionsJson).toMutableMap()
    val users = currentReactions[emoji]?.toMutableList() ?: mutableListOf()
    
    if (users.contains(userId)) {
        users.remove(userId)
    } else {
        users.add(userId)
    }
    
    if (users.isEmpty()) {
        currentReactions.remove(emoji)
    } else {
        currentReactions[emoji] = users
    }
    
    // Convert back to JSON
    val newJson = buildJsonObject {
        currentReactions.forEach { (k, v) ->
            put(k, buildJsonArray { v.forEach { add(it) } })
        }
    }
    
    val updated = comment.copy(emojiReactionsJson = newJson.toString())
    repository.insertComment(updated)
}
