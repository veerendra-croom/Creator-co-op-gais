package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "workspaces")
data class Workspace(
    @PrimaryKey val id: String,
    val name: String,
    val description: String = "",
    val createdBy: String,
    val platformType: String = "YOUTUBE",
    val avatarUrl: String = "",
    val status: String = "ACTIVE",
    val isSponsored: Boolean = false,
    val sponsorName: String? = null,
    val sponsorLogoUrl: String? = null,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "workspace_members", indices = [Index(value = ["workspaceId", "userId"])])
data class WorkspaceMember(
    @PrimaryKey val id: String,
    val workspaceId: String,
    val userId: String,
    val assignedRoleTitle: String = "Member",
    val canModifyProduction: Boolean = false,
    val noteText: String = "",
    val joinedAt: Long = System.currentTimeMillis(),
    val isOnline: Boolean = false,
    val isTyping: Boolean = false,
    val typingText: String = "",
    val currentlyViewingTaskId: String? = null,
    val currentlyEditingAssetId: String? = null,
    val lastSeenAt: Long = System.currentTimeMillis(),
    val liveStatusUpdate: String = ""
)

@Serializable
@Entity(tableName = "team_agreements")
data class TeamAgreement(
    @PrimaryKey val id: String,
    val workspaceId: String,
    val title: String = "",
    val content: String = "",
    val contentText: String = "",
    val version: Int = 1,
    val isLocked: Boolean = false,
    val authorName: String = "",
    val acknowledgmentHash: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(
    tableName = "agreement_acknowledgments",
    indices = [Index(value = ["agreementId", "userId"], unique = true)]
)
data class AgreementAcknowledgment(
    @PrimaryKey val id: String,
    val agreementId: String,
    val userId: String,
    val acknowledgmentHash: String = "",
    val acknowledgedAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "workspace_assets", indices = [Index(value = ["workspaceId"])])
data class WorkspaceAsset(
    @PrimaryKey val id: String,
    val workspaceId: String,
    val uploaderId: String = "",
    val taskId: String? = null,
    val fileName: String = "",
    val fileUrl: String = "",
    val fileType: String = "",
    val category: String = "",
    val title: String = "",
    val url: String = "",
    val type: String = "DOCUMENT",
    val status: String = "ACTIVE",
    val version: String = "v1",
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "workspace_events", indices = [Index(value = ["workspaceId"])])
data class WorkspaceEvent(
    @PrimaryKey val id: String,
    val workspaceId: String,
    val actorId: String = "",
    val eventType: String = "SESSION",
    val entityId: String? = null,
    val eventMetadata: String? = null,
    val title: String = "",
    val description: String = "",
    val startTime: Long = 0,
    val endTime: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "workspace_invites")
data class WorkspaceInviteEntity(
    @PrimaryKey val id: String,
    val workspaceId: String,
    val inviteCode: String,
    val inviteeEmail: String,
    val roleTitle: String,
    val sha256Token: String,
    val expirationTimestamp: Long,
    val isUsed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "role_configurations")
data class RoleConfigurationEntity(
    @PrimaryKey val id: String,
    val workspaceId: String,
    val roleTitle: String,
    val compensationType: String, // "Rev Share", "One-off Task", "Full Time", "Contract"
    val description: String = "",
    val requirements: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

