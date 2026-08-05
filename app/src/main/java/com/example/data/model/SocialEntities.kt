package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "posts", indices = [Index(value = ["authorId"])])
data class Post(
    @PrimaryKey val id: String,
    val authorId: String = "",
    val authorName: String = "",
    val authorAvatarUrl: String = "",
    val authorRole: String = "",
    val spaceName: String = "",
    val title: String = "",
    val content: String = "",
    val body: String = "",
    val mediaUrl: String? = null,
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val userVote: String = "none", // up, down, none
    val isLocked: Boolean = false,
    val commentCount: Int = 0,
    val tags: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "comments", indices = [Index(value = ["postId", "authorId"])])
data class Comment(
    @PrimaryKey val id: String,
    val postId: String,
    val authorId: String = "",
    val authorName: String = "",
    val authorRole: String = "",
    val text: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val parentCommentId: String? = null,
    val isEdited: Boolean = false,
    val emojiReactionsJson: String = "{}" // e.g. {"👍":["userId1"], "🔥":["userId2"]}
)

@Serializable
@Entity(tableName = "endorsements")
data class Endorsement(
    @PrimaryKey val id: String,
    val workspaceId: String,
    val giverId: String,
    val receiverId: String,
    val tags: List<String> = emptyList(),
    val skill: String = "", // Keep for compatibility if needed
    val comment: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
