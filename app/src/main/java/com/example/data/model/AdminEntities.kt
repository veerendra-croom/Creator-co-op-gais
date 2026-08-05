package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "reports", indices = [Index(value = ["targetId", "status"])])
data class Report(
    @PrimaryKey val id: String,
    val reporterId: String,
    val targetType: String = "USER",
    val targetId: String,
    val reason: String,
    val category: String = "GENERAL",
    val evidence: String = "",
    val status: String = "PENDING",
    val workspaceId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "admin_audit_logs")
data class AuditLog(
    @PrimaryKey val id: String,
    val adminId: String,
    val adminName: String = "",
    val actionTaken: String,
    val targetType: String,
    val targetId: String,
    val reason: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "sync_queue")
data class SyncEntity(
    @PrimaryKey val id: String,
    val entityType: String,
    val entityJson: String,
    val actionType: String,
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING",
    val retryCount: Int = 0,
    val lastAttemptedAt: Long = 0
)

@Serializable
@Entity(tableName = "global_settings_table")
data class GlobalSetting(
    @PrimaryKey val settingKey: String,
    val settingValue: String
)

@Serializable
@Entity(tableName = "feature_flags_table")
data class FeatureFlag(
    @PrimaryKey @androidx.room.ColumnInfo(name = "flag_key") val flagKey: String,
    @androidx.room.ColumnInfo(name = "is_enabled") val isEnabled: Boolean = false,
    val description: String? = null,
    val lastModifiedAt: Long = System.currentTimeMillis(),
    val lastModifiedByAdminId: String = ""
)

@Serializable
@Entity(tableName = "changelog_entries_table")
data class ChangelogEntry(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val versionName: String,
    val created_at: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "verification_requests")
data class VerificationRequest(
    @PrimaryKey val id: String,
    val userId: String,
    val userDisplayName: String = "",
    val userRole: String = "",
    val portfolioUrl: String = "",
    val status: String = "PENDING",
    val notes: String = "",
    val reviewedBy: String? = null,
    val reviewedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "user_audit_logs")
data class UserAuditLog(
    @PrimaryKey val id: String,
    val userId: String = "",
    val adminId: String = "",
    val targetUserId: String = "",
    val action: String = "",
    val actionTaken: String = "",
    val reason: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "platform_settings")
data class PlatformSettings(
    @PrimaryKey val id: String = "singleton_settings",
    val maintenanceMode: Boolean = false,
    val betaMode: Boolean = false,
    val registrationEnabled: Boolean = true,
    val inviteOnlyEnabled: Boolean = false,
    val lastModifiedAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "founder_notes")
data class FounderNote(
    @PrimaryKey val id: String,
    val entityType: String = "",
    val entityId: String = "",
    val authorName: String = "",
    val noteContent: String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "dispute_notes_table")
data class DisputeNote(
    @PrimaryKey val id: String,
    val workspaceId: String,
    val authorId: String,
    val targetUserId: String,
    val content: String,
    val noteText: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
