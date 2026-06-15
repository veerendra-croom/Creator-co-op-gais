package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String = "me",
    val email: String,
    val phone: String,
    val displayName: String,
    val avatarUrl: String,
    val primaryRole: String,
    val secondaryRolesJson: String, // Stringified list of other roles
    val karmaScore: Int = 0,
    val isVerifiedPro: Boolean = false,
    val stripeAccountId: String = "",
    val portfolioLinksJson: String = "[]", // Stringified links
    val availableBalance: Double = 0.0,
    val pendingBalance: Double = 0.0,
    val treasuryBalance: Double = 0.0
) : Serializable

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey val id: String,
    val title: String,
    val authorName: String,
    val authorRole: String,
    val authorAvatarUrl: String,
    val body: String,
    val spaceName: String, // e.g. "Editing", "Shorts", "Writing"
    val timestamp: Long,
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val commentCount: Int = 0,
    val userVote: String = "none", // "up", "down", "none"
    val mediaUrl: String = "" // Video url for media feedback tool
) : Serializable

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey val id: String,
    val postId: String,
    val authorName: String,
    val authorRole: String,
    val text: String,
    val timestamp: Long,
    val timestampMs: Long? = null // For Video Feedback Tool timestamp
) : Serializable

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey val id: String,
    val managerId: String,
    val title: String,
    val niche: String, // e.g. "Tech", "Gaming", "Finance"
    val contentStrategy: String,
    val subscriberCount: Int,
    val status: String, // "PUBLISHED", "NEGOTIATING", "CONTRACTED", "PAUSED", "COMPLETED"
    val createdAt: Long,
    // Equity breakdown percentages
    val hostEquity: Int = 0,
    val editorEquity: Int = 0,
    val writerEquity: Int = 0,
    val animatorEquity: Int = 0,
    val thumbnailDesignerEquity: Int = 0
) : Serializable

@Entity(tableName = "pitches")
data class Pitch(
    @PrimaryKey val id: String,
    val projectId: String,
    val applicantId: String,
    val applicantName: String,
    val applicantRole: String,
    val applicantAvatarUrl: String,
    val message: String,
    val portfolioLink: String,
    val status: String, // "PENDING", "ACCEPTED", "DECLINED"
    val submittedAt: Long
) : Serializable

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey val id: String,
    val workspaceId: String,
    val channel: String, // "general", "scripts", "video-drafts"
    val senderId: String,
    val senderName: String,
    val senderRole: String,
    val text: String,
    val timestamp: Long,
    val fileName: String? = null,
    val fileUri: String? = null
) : Serializable

@Entity(tableName = "contracts")
data class Contract(
    @PrimaryKey val id: String,
    val projectId: String,
    val projectTitle: String,
    val managerName: String,
    val talentName: String,
    val talentId: String,
    val talentRole: String,
    val hostEquity: Int,
    val editorEquity: Int,
    val writerEquity: Int,
    val jurisdiction: String,
    val exitClauses: String, // Details about termination terms
    val docusignEnvelopeId: String,
    val status: String, // "SENT", "SIGNED_BY_TALENT", "SIGNED_BY_MANAGER", "FULLY_SIGNED"
    val signedAtMilli: Long = 0L
) : Serializable
