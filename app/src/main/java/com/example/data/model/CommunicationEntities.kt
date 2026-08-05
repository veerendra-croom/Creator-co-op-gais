package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "messages")
data class Message(
    @PrimaryKey val id: String,
    val workspaceId: String,
    val senderId: String,
    val senderName: String,
    val senderRole: String = "MEMBER",
    val recipientId: String? = null,
    val messageBody: String = "",
    val attachmentJsonMeta: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "connection_requests")
data class ConnectionRequest(
    @PrimaryKey val id: String,
    val senderId: String,
    val senderName: String,
    val senderRole: String = "Creator",
    val receiverId: String,
    val status: String = "PENDING",
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val body: String,
    val type: String,
    val deepLinkTarget: String? = null,
    val isRead: Boolean = false,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "support_tickets")
data class SupportTicket(
    @PrimaryKey val id: String,
    val userId: String,
    val userDisplayName: String = "",
    val subject: String = "",
    val title: String = "",
    val message: String = "",
    val description: String = "",
    val category: String = "GENERAL",
    val upvotes: Int = 0,
    val internalPriority: String = "LOW",
    val internalNotes: String = "",
    val assignedAdminId: String? = null,
    val assignedAdminName: String? = null,
    val deviceInfo: String = "",
    val appVersion: String = "",
    val screenshots: List<String> = emptyList(),
    val logs: String = "",
    val contactHistoryJson: String = "[]",
    val followUpTasksJson: String = "[]",
    val upvotedUserIdsJson: String = "[]",
    val status: String = "OPEN",
    val resolvedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "announcements")
data class Announcement(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val category: String = "GLOBAL",
    val priority: String = "NORMAL",
    val targetAudience: String = "ALL",
    val targetReach: Int = 0,
    val opensCount: Int = 0,
    val clicksCount: Int = 0,
    val dismissalsCount: Int = 0,
    val status: String = "PUBLISHED",
    val scheduledTime: Long = 0,
    val campaignType: String = "INFO",
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "announcement_interactions")
data class AnnouncementInteraction(
    @PrimaryKey val id: String, // userId_announcementId
    val userId: String,
    val announcementId: String,
    val isOpened: Boolean = false,
    val isClicked: Boolean = false,
    val isDismissed: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val readAt: Long = System.currentTimeMillis()
)
