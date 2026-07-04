package com.example.data.model

import androidx.room.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

@Serializable
@Entity(tableName = "users")
data class UserProfile(
    @PrimaryKey @SerialName("id") val id: String = "",
    @SerialName("email") val email: String = "",
    @SerialName("username") val username: String = "",
    @SerialName("display_name") val displayName: String = "",
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("global_role") val globalRole: String = "APP_USER", // ADMIN or APP_USER
    @SerialName("primary_specialty") val primarySpecialty: String = "",
    @SerialName("is_verified_pro") val isVerifiedPro: Boolean = false,
    @SerialName("system_role") val systemRole: String = "REGISTERED_USER",
    @SerialName("bio") val bio: String = "",
    @SerialName("website_url") val websiteUrl: String = "",
    @SerialName("skills_json") val skillsJson: String = "[]",
    @SerialName("portfolio_json") val portfolioJson: String = "[]",
    @SerialName("social_links_json") val socialLinksJson: String = "{}",
    @SerialName("availability_status") val availabilityStatus: String = "OPEN_TO_PROJECTS",
    @SerialName("referral_code") val referralCode: String = "",
    @SerialName("premium_trial_extension_days") val premiumTrialExtensionDays: Int = 0,
    @SerialName("device_id") val deviceId: String = "simulated_device_id",
    @SerialName("reputation_score") val reputationScore: Int = 92,
    @SerialName("reliability_badge") val reliabilityBadge: String = "Gold",
    @SerialName("verification_level") val verificationLevel: String = "L2 Pro Verified",
    @SerialName("completed_projects_count") val completedProjectsCount: Int = 14,
    @SerialName("signed_agreements_count") val signedAgreementsCount: Int = 8,
    @SerialName("on_time_delivery_rate") val onTimeDeliveryRate: Int = 98,
    @SerialName("peer_rating") val peerRating: Double = 4.9,
    @SerialName("stripe_connected_account_id") val stripeConnectedAccountId: String? = null,
    @SerialName("stripe_onboarding_completed") val stripeOnboardingCompleted: Boolean = false,
    @SerialName("stripe_verification_status") val stripeVerificationStatus: String = "UNLINKED", // UNLINKED, PENDING, VERIFIED
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L
) : JavaSerializable

@Serializable
@Entity(
    tableName = "endorsements",
    primaryKeys = ["giverId", "receiverId", "workspaceId"],
    foreignKeys = [
        ForeignKey(
            entity = UserProfile::class,
            parentColumns = ["id"],
            childColumns = ["giverId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserProfile::class,
            parentColumns = ["id"],
            childColumns = ["receiverId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Workspace::class,
            parentColumns = ["id"],
            childColumns = ["workspaceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("giverId"), Index("receiverId"), Index("workspaceId")]
)
data class Endorsement(
    @SerialName("giver_id") val giverId: String = "",
    @SerialName("receiver_id") val receiverId: String = "",
    @SerialName("workspace_id") val workspaceId: String = "",
    @SerialName("tags") val tags: List<String> = emptyList(),
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis()
) : JavaSerializable

@Serializable
@Entity(tableName = "workspaces")
data class Workspace(
    @PrimaryKey @SerialName("id") val id: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("platform_type") val platformType: String = "YOUTUBE", // YOUTUBE, INSTAGRAM, etc.
    @SerialName("created_by") val createdBy: String = "",
    @SerialName("is_archived") val isArchived: Boolean = false,
    @SerialName("created_at") val createdAt: Long = 0,
    @SerialName("is_sponsored") @ColumnInfo(name = "is_sponsored") val isSponsored: Boolean = false,
    @SerialName("sponsor_name") @ColumnInfo(name = "sponsor_name") val sponsorName: String? = null,
    @SerialName("sponsor_logo_url") @ColumnInfo(name = "sponsor_logo_url") val sponsorLogoUrl: String? = null
) : JavaSerializable

@Serializable
@Entity(
    tableName = "workspace_members",
    indices = [Index("workspaceId"), Index("userId")],
    foreignKeys = [
        ForeignKey(
            entity = Workspace::class,
            parentColumns = ["id"],
            childColumns = ["workspaceId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserProfile::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WorkspaceMember(
    @PrimaryKey val id: String = "",
    @SerialName("workspace_id") val workspaceId: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("assigned_role_title") val assignedRoleTitle: String = "Creator",
    @SerialName("can_modify_production") val canModifyProduction: Boolean = false
) : JavaSerializable

@Serializable
@Entity(
    tableName = "production_tasks",
    indices = [Index("workspaceId"), Index("creatorId")],
    foreignKeys = [
        ForeignKey(
            entity = Workspace::class,
            parentColumns = ["id"],
            childColumns = ["workspaceId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserProfile::class,
            parentColumns = ["id"],
            childColumns = ["creatorId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ProductionTask(
    @PrimaryKey @SerialName("id") val id: String = "",
    @SerialName("workspace_id") val workspaceId: String = "",
    @SerialName("creator_id") val creatorId: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("content_body") val contentBody: String = "",
    @SerialName("media_preview_url") val mediaPreviewUrl: String? = null,
    @SerialName("state_scope") val stateScope: String = "ROUGH_SANDBOX", // ROUGH_SANDBOX or PRODUCTION_READY
    @SerialName("kanban_lane") val kanbanLane: String = "IDEAS", // IDEAS, RESEARCH, SCRIPT, RECORD, EDIT, REVIEW, PUBLISH
    @SerialName("created_at") val createdAt: Long = 0
) : JavaSerializable

@Serializable
@Entity(
    tableName = "team_agreements",
    foreignKeys = [
        ForeignKey(
            entity = Workspace::class,
            parentColumns = ["id"],
            childColumns = ["workspaceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workspaceId")]
)
data class TeamAgreement(
    @PrimaryKey val id: String = "",
    @SerialName("workspace_id") val workspaceId: String = "",
    @SerialName("version") val version: Int = 1,
    @SerialName("content_text") val contentText: String = "",
    @SerialName("is_locked") val isLocked: Boolean = false,
    @SerialName("created_at") val createdAt: Long = 0
) : JavaSerializable

@Serializable
@Entity(
    tableName = "agreement_acknowledgments",
    foreignKeys = [
        ForeignKey(
            entity = TeamAgreement::class,
            parentColumns = ["id"],
            childColumns = ["agreementId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserProfile::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("agreementId"), Index("userId")]
)
data class AgreementAcknowledgment(
    @PrimaryKey val id: String = "",
    @SerialName("agreement_id") val agreementId: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("acknowledgment_hash") val acknowledgmentHash: String = "",
    @SerialName("acknowledged_at") val acknowledgedAt: Long = 0
) : JavaSerializable

@Serializable
@Entity(
    tableName = "messages",
    indices = [Index("workspaceId"), Index("senderId")],
    foreignKeys = [
        ForeignKey(
            entity = Workspace::class,
            parentColumns = ["id"],
            childColumns = ["workspaceId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserProfile::class,
            parentColumns = ["id"],
            childColumns = ["senderId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Message(
    @PrimaryKey val id: String = "",
    @SerialName("workspace_id") val workspaceId: String = "",
    @SerialName("sender_id") val senderId: String = "",
    @SerialName("recipient_id") val recipientId: String? = null,
    @SerialName("sender_name") val senderName: String = "",
    @SerialName("sender_role") val senderRole: String = "",
    @SerialName("message_body") val messageBody: String = "",
    @SerialName("attachment_json_meta") val attachmentJsonMeta: String? = null,
    @SerialName("timestamp") val timestamp: Long = 0
) : JavaSerializable

@Serializable
@Entity(tableName = "posts", indices = [Index("authorId"), Index("spaceName")])
data class Post(
    @PrimaryKey val id: String = "",
    val authorId: String = "",
    val title: String = "",
    val authorName: String = "",
    val authorRole: String = "",
    val authorAvatarUrl: String? = null,
    val body: String = "",
    val spaceName: String = "",
    val timestamp: Long = 0,
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val commentCount: Int = 0,
    val userVote: String = "none",
    val mediaUrl: String = ""
) : JavaSerializable

@Serializable
@Entity(
    tableName = "comments",
    indices = [Index("postId"), Index("authorId")],
    foreignKeys = [
        ForeignKey(
            entity = Post::class,
            parentColumns = ["id"],
            childColumns = ["postId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserProfile::class,
            parentColumns = ["id"],
            childColumns = ["authorId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Comment(
    @PrimaryKey val id: String = "",
    val authorId: String = "",
    val postId: String = "",
    val authorName: String = "",
    val authorRole: String = "",
    val text: String = "",
    val timestamp: Long = 0,
    val timestampMs: Long? = null
) : JavaSerializable

@Serializable
@Entity(tableName = "reports")
data class Report(
    @PrimaryKey val id: String = "",
    @SerialName("reporter_id") val reporterId: String = "",
    @SerialName("target_type") val targetType: String = "", // POST, COMMENT, MESSAGE, USER, WORKSPACE
    @SerialName("target_id") val targetId: String = "",
    @SerialName("reason") val reason: String = "",
    @SerialName("status") val status: String = "PENDING", // PENDING, RESOLVED, DISMISSED
    @SerialName("created_at") val createdAt: Long = 0,
    @SerialName("category") val category: String = "Spam", // Spam, Harassment, Fake Account, Portfolio Fraud, Workspace Abuse, Agreement Abuse
    @SerialName("evidence") val evidence: String = "",
    @SerialName("workspace_id") val workspaceId: String? = null
) : JavaSerializable

@Serializable
@Entity(tableName = "admin_audit_logs")
data class AuditLog(
    @PrimaryKey val id: String = "",
    @SerialName("admin_id") val adminId: String = "",
    @SerialName("admin_name") val adminName: String = "System Admin",
    @SerialName("action_taken") val actionTaken: String = "", // SUSPEND_USER, DELETE_POST, DISMISS_REPORT, etc.
    @SerialName("target_type") val targetType: String = "",
    @SerialName("target_id") val targetId: String = "",
    @SerialName("reason") val reason: String = "",
    @SerialName("created_at") val createdAt: Long = 0
) : JavaSerializable {
    val actionType: String get() = actionTaken
    val timestamp: Long get() = createdAt
}

@Serializable
@Entity(
    tableName = "project_proposals",
    indices = [Index("authorId"), Index("niche")],
    foreignKeys = [
        ForeignKey(
            entity = UserProfile::class,
            parentColumns = ["id"],
            childColumns = ["authorId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ProjectProposal(
    @PrimaryKey val id: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("niche") val niche: String = "",
    @SerialName("brief") val brief: String = "",
    @SerialName("author_id") val authorId: String = "",
    @SerialName("author_name") val authorName: String = "",
    @SerialName("created_at") val createdAt: Long = 0,
    @SerialName("boosted_until") @ColumnInfo(name = "boosted_until") val boostedUntil: Long = 0
) : JavaSerializable

data class ProjectProposalWithData(
    @Embedded val proposal: ProjectProposal,
    @Relation(
        parentColumn = "authorId",
        entityColumn = "id"
    )
    val author: UserProfile?,
    @Relation(
        parentColumn = "id",
        entityColumn = "projectId"
    )
    val pitches: List<TalentPitch> = emptyList()
)

@Serializable
@Entity(
    tableName = "talent_pitches",
    indices = [Index("projectId"), Index("senderId")],
    foreignKeys = [
        ForeignKey(
            entity = ProjectProposal::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserProfile::class,
            parentColumns = ["id"],
            childColumns = ["senderId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TalentPitch(
    @PrimaryKey val id: String = "",
    @SerialName("project_id") val projectId: String = "",
    @SerialName("sender_id") val senderId: String = "",
    @SerialName("sender_name") val senderName: String = "",
    @SerialName("sender_specialty") val senderSpecialty: String = "",
    @SerialName("cover_message") val coverMessage: String = "",
    @SerialName("portfolio_url") val portfolioUrl: String = "",
    @SerialName("created_at") val createdAt: Long = 0,
    @SerialName("status") val status: String = "PENDING" // PENDING, ACCEPTED, DECLINED
) : JavaSerializable

@Serializable
@Entity(tableName = "sync_queue")
data class SyncEntity(
    @PrimaryKey val id: String = "",
    @SerialName("entity_type") val entityType: String = "", // POST, MESSAGE, TASK, AGREEMENT_ACK
    @SerialName("entity_json") val entityJson: String = "",
    @SerialName("action_type") val actionType: String = "UPSERT", // UPSERT, DELETE
    @SerialName("created_at") val createdAt: Long = 0,
    @SerialName("sync_status") val syncStatus: String = "PENDING", // PENDING, SYNCED, FAILED
    @SerialName("retry_count") val retryCount: Int = 0,
    @SerialName("last_attempted_at") val lastAttemptedAt: Long = 0
) : JavaSerializable

// AD MANAGEMENT ENTITIES
@Entity(tableName = "ad_placements_table")
data class AdPlacement(
    @PrimaryKey @ColumnInfo(name = "slot_id") val slotId: String = "",
    @ColumnInfo(name = "slot_name") val slotName: String = "",
    @ColumnInfo(name = "screen_location") val screenLocation: String = "",
    @ColumnInfo(name = "ad_type") val adType: String = "",
    @ColumnInfo(name = "is_enabled") val isEnabled: Boolean = true,
    @ColumnInfo(name = "allowed_locations") val allowedLocations: List<String> = emptyList(),
    @ColumnInfo(name = "last_modified_by_admin_id") val lastModifiedByAdminId: String? = null,
    @ColumnInfo(name = "last_modified_at") val lastModifiedAt: Long = 0
) : JavaSerializable

@Entity(tableName = "global_settings_table")
data class GlobalSetting(
    @PrimaryKey @ColumnInfo(name = "setting_key") val settingKey: String = "",
    @ColumnInfo(name = "setting_value") val settingValue: String = ""
) : JavaSerializable

@Serializable
@Entity(tableName = "user_settings_table")
data class UserSetting(
    @PrimaryKey val id: String = "",
    val userId: String = "",
    val key: String = "",
    val value: String = ""
) : JavaSerializable

@Serializable
@Entity(tableName = "saved_searches")
data class SavedSearch(
    @PrimaryKey @SerialName("id") val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("filter_json") val filterJson: String = "{}",
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis(),
    @SerialName("last_notified_at") val lastNotifiedAt: Long = System.currentTimeMillis()
) : JavaSerializable

@Serializable
@Entity(tableName = "looking_for_work")
data class LookingForWork(
    @PrimaryKey @SerialName("user_id") val userId: String = "",
    @SerialName("details_json") val detailsJson: String = "{}",
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis()
) : JavaSerializable

@Serializable
data class LookingForWorkDetails(
    val skills: String = "",
    val availability: String = "",
    val rateExpectations: String = ""
) : JavaSerializable

@Serializable
data class SavedSearchFilter(
    val type: String = "OPEN_ROLES",
    val niche: String = "All",
    val searchQuery: String = ""
) : JavaSerializable


sealed class SecurityState {
    object Idle : SecurityState()
    object Authenticating : SecurityState()
    data class Authenticated(val userId: String) : SecurityState()
    data class Error(val message: String) : SecurityState()
    object SybilThrottled : SecurityState()
}

class SybilThrottledException(message: String = "Platform rate limit exceeded: Too many proposal attempts.") : Exception(message)

@Serializable
@Entity(tableName = "content_calendar_items")
data class ContentCalendarItem(
    @PrimaryKey @SerialName("id") val id: String = "",
    @SerialName("workspace_id") val workspaceId: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("scheduled_date") val scheduledDate: String = "",
    @SerialName("linked_task_id") val linkedTaskId: String? = null,
    @SerialName("created_by") val createdBy: String = "",
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis()
) : JavaSerializable

@Serializable
@Entity(tableName = "task_templates")
data class TaskTemplate(
    @PrimaryKey @SerialName("id") val id: String = "",
    @SerialName("workspace_id") val workspaceId: String = "",
    @SerialName("template_name") val templateName: String = "",
    @SerialName("tasks_json") val tasksJson: String = "",
    @SerialName("created_by") val createdBy: String = "",
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis()
) : JavaSerializable

@Serializable
data class TemplateTask(
    val title: String = "",
    val description: String = "",
    val defaultLane: String = "TODO"
) : JavaSerializable

@Serializable
@Entity(
    tableName = "dispute_notes_table",
    foreignKeys = [
        ForeignKey(
            entity = Workspace::class,
            parentColumns = ["id"],
            childColumns = ["workspaceId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserProfile::class,
            parentColumns = ["id"],
            childColumns = ["authorId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserProfile::class,
            parentColumns = ["id"],
            childColumns = ["targetUserId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workspaceId"), Index("authorId"), Index("targetUserId")]
)
data class DisputeNote(
    @PrimaryKey @SerialName("id") val id: String = "",
    @SerialName("workspace_id") val workspaceId: String = "",
    @SerialName("author_id") val authorId: String = "",
    @SerialName("target_user_id") val targetUserId: String = "",
    @SerialName("note_text") val noteText: String = "",
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis()
) : JavaSerializable

@Serializable
@Entity(
    tableName = "referrals",
    foreignKeys = [
        ForeignKey(
            entity = UserProfile::class,
            parentColumns = ["id"],
            childColumns = ["referrerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserProfile::class,
            parentColumns = ["id"],
            childColumns = ["referredUserId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("referrerId"), Index("referredUserId")]
)
data class Referral(
    @PrimaryKey val id: String = "",
    val referrerId: String = "",
    val referredUserId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val rewardGranted: Boolean = false
) : JavaSerializable

@Serializable
@Entity(tableName = "feature_flags_table")
data class FeatureFlag(
    @PrimaryKey @ColumnInfo(name = "flag_key") val flagKey: String = "",
    @ColumnInfo(name = "is_enabled") val isEnabled: Boolean = false,
    @ColumnInfo(name = "description") val description: String = "",
    @ColumnInfo(name = "last_modified_by_admin_id") val lastModifiedByAdminId: String? = null,
    @ColumnInfo(name = "last_modified_at") val lastModifiedAt: Long = 0
) : JavaSerializable

@Serializable
@Entity(tableName = "changelog_entries_table")
data class ChangelogEntry(
    @PrimaryKey @SerialName("id") @ColumnInfo(name = "id") val id: String = "",
    @SerialName("title") @ColumnInfo(name = "title") val title: String = "",
    @SerialName("description") @ColumnInfo(name = "description") val description: String = "",
    @SerialName("version_name") @ColumnInfo(name = "version_name") val versionName: String = "",
    @SerialName("created_at") @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
) : JavaSerializable

@Serializable
@Entity(tableName = "connection_requests")
data class ConnectionRequest(
    @PrimaryKey val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val senderName: String = "",
    val senderRole: String = "",
    val status: String = "PENDING", // PENDING, ACCEPTED, DECLINED
    val createdAt: Long = System.currentTimeMillis()
) : JavaSerializable

@Serializable
@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey val id: String = "",
    val userId: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "INFO", // MESSAGE, TASKS, INVITES, AGREEMENTS, INFO
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val deepLinkTarget: String? = null // Targets like "AGREEMENT_VAULT", "WORKSPACE_INVITE:<workspaceId>", "TASK_DETAILS:<taskId>:<workspaceId>", "CHAT_THREAD:<workspaceId>"
) : JavaSerializable

@Serializable
data class WorkspacePolicyConfig(
    val governanceRule: String = "MAJORITY_VOTE",
    val reviewStandard: String = "PEER_REVIEW",
    val discordWebhookUrl: String = "",
    val youtubeApiKey: String = "",
    val instagramAuthToken: String = "",
    val autoApprovePitch: Boolean = false,
    val minKarmaRequired: Int = 100,
    val minAvailabilityHours: Int = 10
)

@Serializable
data class RecruitmentAuditingConfig(
    val interviewResponsePersona: String = "PROFESSIONAL",
    val customInterviewDirective: String = ""
)

@Serializable
@Entity(tableName = "verification_requests")
data class VerificationRequest(
    @PrimaryKey @SerialName("id") val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("user_display_name") val userDisplayName: String = "",
    @SerialName("user_role") val userRole: String = "",
    @SerialName("portfolio_url") val portfolioUrl: String = "",
    @SerialName("status") val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    @SerialName("notes") val notes: String = "",
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis(),
    @SerialName("reviewed_by") val reviewedBy: String? = null,
    @SerialName("reviewed_at") val reviewedAt: Long? = null
) : JavaSerializable

@Serializable
@Entity(tableName = "user_audit_logs")
data class UserAuditLog(
    @PrimaryKey @SerialName("id") val id: String = "",
    @SerialName("admin_id") val adminId: String = "",
    @SerialName("target_user_id") val targetUserId: String = "",
    @SerialName("action_taken") val actionTaken: String = "", // SUSPEND, REACTIVATE, SOFT_DELETE, RESTORE, LOGOUT, VERIFICATION_CHANGE, TRUST_BADGE_CHANGE
    @SerialName("reason") val reason: String = "",
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis()
) : JavaSerializable

@Serializable
@Entity(tableName = "support_tickets")
data class SupportTicket(
    @PrimaryKey @SerialName("id") val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("user_display_name") val userDisplayName: String = "",
    @SerialName("category") val category: String = "Bug", // Bug, Feature Request, Account Issue, Workspace Issue, Verification Issue
    @SerialName("title") val title: String = "",
    @SerialName("description") val description: String = "",
    @SerialName("status") val status: String = "OPEN", // OPEN, IN_PROGRESS, WAITING_USER, RESOLVED, CLOSED
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis(),
    @SerialName("resolved_at") val resolvedAt: Long? = null,
    
    // Bug report specific
    @SerialName("device_info") val deviceInfo: String = "",
    @SerialName("app_version") val appVersion: String = "",
    @SerialName("screenshots") val screenshots: String = "", // comma-separated URLs or names
    @SerialName("logs") val logs: String = "",
    
    // Feature request specific
    @SerialName("upvotes") val upvotes: Int = 0,
    @SerialName("upvoted_user_ids_json") val upvotedUserIdsJson: String = "[]", // JSON array of user IDs
    @SerialName("internal_priority") val internalPriority: String = "MEDIUM", // LOW, MEDIUM, HIGH, CRITICAL
    
    // Admin support panel
    @SerialName("assigned_admin_id") val assignedAdminId: String? = null,
    @SerialName("assigned_admin_name") val assignedAdminName: String? = null,
    @SerialName("internal_notes") val internalNotes: String = ""
) : JavaSerializable

@Serializable
@Entity(tableName = "crm_records")
data class CrmRecord(
    @PrimaryKey @SerialName("id") val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("display_name") val displayName: String = "",
    @SerialName("email") val email: String = "",
    @SerialName("cohort_segment") val cohortSegment: String = "Beta Users", // Alpha Users, Beta Users, Power Users, Dormant Users, At Risk Users
    @SerialName("logins_count") val loginsCount: Int = 0,
    @SerialName("tasks_completed_count") val tasksCompletedCount: Int = 0,
    @SerialName("agreements_signed_count") val agreementsSignedCount: Int = 0,
    @SerialName("referrals_count") val referralsCount: Int = 0,
    @SerialName("health_score") val healthScore: Int = 0, // 0-100
    @SerialName("notes") val notes: String = "",
    @SerialName("follow_up_tasks_json") val followUpTasksJson: String = "[]", // JSON string of tasks
    @SerialName("contact_history_json") val contactHistoryJson: String = "[]", // JSON string of history entries
    @SerialName("last_interaction") val lastInteraction: Long = System.currentTimeMillis(),
    @SerialName("follow_up_status") val followUpStatus: String = "Contacted" // Contacted, Interested, Scheduled Demo, Active User, Churned
) : JavaSerializable

@Serializable
@Entity(tableName = "announcements")
data class Announcement(
    @PrimaryKey @SerialName("id") val id: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("content") val content: String = "",
    @SerialName("target_audience") val targetAudience: String = "All Users", // All Users, Verified Creators, Editors, Workspace Owners, Beta Cohorts
    @SerialName("campaign_type") val campaignType: String = "Product Update", // Product Update, Maintenance Notice, Beta Invite, New Feature Launch
    @SerialName("status") val status: String = "Draft", // Draft, Scheduled, Active, Archived
    @SerialName("scheduled_time") val scheduledTime: Long = 0L,
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis(),
    @SerialName("opens_count") val opensCount: Int = 0,
    @SerialName("clicks_count") val clicksCount: Int = 0,
    @SerialName("dismissals_count") val dismissalsCount: Int = 0,
    @SerialName("target_reach") val targetReach: Int = 0
) : JavaSerializable

@Serializable
@Entity(tableName = "announcement_interactions")
data class AnnouncementInteraction(
    @PrimaryKey @SerialName("id") val id: String = "", // announcementId + "_" + userId
    @SerialName("announcement_id") val announcementId: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("is_opened") val isOpened: Boolean = false,
    @SerialName("is_clicked") val isClicked: Boolean = false,
    @SerialName("is_dismissed") val isDismissed: Boolean = false,
    @SerialName("timestamp") val timestamp: Long = System.currentTimeMillis()
) : JavaSerializable

@Serializable
@Entity(tableName = "platform_settings")
data class PlatformSettings(
    @PrimaryKey @SerialName("id") val id: String = "singleton_settings",
    @SerialName("maintenance_mode") val maintenanceMode: Boolean = false,
    @SerialName("beta_mode") val betaMode: Boolean = false,
    @SerialName("registration_enabled") val registrationEnabled: Boolean = true,
    @SerialName("invite_only_enabled") val inviteOnlyEnabled: Boolean = false
) : JavaSerializable

@Serializable
@Entity(tableName = "onboarding_slides")
data class OnboardingSlide(
    @PrimaryKey @SerialName("id") val id: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("description") val description: String = "",
    @SerialName("step_index") val stepIndex: Int = 0,
    @SerialName("icon_name") val iconName: String = "Star"
) : JavaSerializable

@Serializable
@Entity(tableName = "welcome_messages")
data class WelcomeMessage(
    @PrimaryKey @SerialName("id") val id: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("greeting") val greeting: String = "",
    @SerialName("banner_image_url") val bannerImageUrl: String = "",
    @SerialName("is_active") val isActive: Boolean = false
) : JavaSerializable

@Serializable
@Entity(tableName = "empty_states")
data class EmptyStateConfig(
    @PrimaryKey @SerialName("id") val id: String = "",
    @SerialName("screen_name") val screenName: String = "",
    @SerialName("image_tag") val imageTag: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("suggestion") val suggestion: String = ""
) : JavaSerializable

@Serializable
@Entity(tableName = "help_texts")
data class HelpText(
    @PrimaryKey @SerialName("id") val id: String = "",
    @SerialName("topic_key") val topicKey: String = "",
    @SerialName("text_content") val textContent: String = "",
    @SerialName("category") val category: String = ""
) : JavaSerializable

@Serializable
@Entity(tableName = "founder_notes")
data class FounderNote(
    @PrimaryKey val id: String = "",
    val entityType: String = "", // USER, WORKSPACE, TICKET, REPORT, CRM
    val entityId: String = "",
    val noteContent: String = "",
    val authorId: String = "founder_admin",
    val authorName: String = "Founder Operator",
    val createdAt: Long = 0
) : JavaSerializable

@Serializable
@Entity(tableName = "workspace_assets", indices = [Index("workspaceId"), Index("uploaderId")])
data class WorkspaceAsset(
    @PrimaryKey val id: String = "",
    @SerialName("workspace_id") val workspaceId: String = "",
    @SerialName("uploader_id") val uploaderId: String = "",
    @SerialName("task_id") val taskId: String? = null,
    @SerialName("file_name") val fileName: String = "",
    @SerialName("file_url") val fileUrl: String = "",
    @SerialName("file_type") val fileType: String = "VIDEO", // VIDEO, IMAGE, AUDIO, PROJECT_FILE, DOCUMENT
    @SerialName("category") val category: String = "RAW_FOOTAGE", // RAW_FOOTAGE, GRAPHICS, AUDIO_STEMS, EXPORTS
    @SerialName("status") val status: String = "AVAILABLE", // UPLOADING, AVAILABLE, ARCHIVED
    @SerialName("version") val version: Int = 1,
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis()
) : JavaSerializable

@Serializable
@Entity(
    tableName = "deliverables",
    indices = [Index("workspaceId"), Index("taskId"), Index("submitterId")],
    foreignKeys = [
        ForeignKey(
            entity = Workspace::class,
            parentColumns = ["id"],
            childColumns = ["workspaceId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductionTask::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserProfile::class,
            parentColumns = ["id"],
            childColumns = ["submitterId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Deliverable(
    @PrimaryKey val id: String = "",
    @SerialName("workspace_id") val workspaceId: String = "",
    @SerialName("task_id") val taskId: String = "",
    @SerialName("submitter_id") val submitterId: String = "",
    @SerialName("asset_id") val assetId: String = "",
    @SerialName("version_notes") val versionNotes: String = "",
    @SerialName("status") val status: String = "PENDING_REVIEW", // PENDING_REVIEW, CHANGES_REQUESTED, APPROVED
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis(),
    @SerialName("reviewed_by") val reviewedBy: String? = null,
    @SerialName("reviewed_at") val reviewedAt: Long? = null,
    @SerialName("review_feedback") val reviewFeedback: String? = null
) : JavaSerializable

@Serializable
@Entity(
    tableName = "workspace_events",
    indices = [
        Index("workspaceId"),
        Index(value = ["workspaceId", "eventType", "entityId"], unique = true)
    ],
    foreignKeys = [
        ForeignKey(
            entity = Workspace::class,
            parentColumns = ["id"],
            childColumns = ["workspaceId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WorkspaceEvent(
    @PrimaryKey val id: String = "",
    @SerialName("workspace_id") val workspaceId: String = "",
    @SerialName("actor_id") val actorId: String = "",
    @SerialName("event_type") val eventType: String = "", // TASK_CREATED, ASSET_UPLOADED, DELIVERABLE_SUBMITTED, DELIVERABLE_APPROVED, MEMBER_JOINED
    @SerialName("entity_id") val entityId: String = "", // ID of the task/asset/deliverable/member
    @SerialName("event_metadata") val eventMetadata: String = "", // JSON blob
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis()
) : JavaSerializable







