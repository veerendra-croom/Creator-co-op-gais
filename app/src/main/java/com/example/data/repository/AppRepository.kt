package com.example.data.repository
import com.example.data.network.ExternalIntegrationsClient

import com.example.data.local.*
import com.example.data.model.*
import com.example.data.supabase.*
import com.example.analytics.AnalyticsManager
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID
import com.example.BuildConfig
import io.github.jan.supabase.auth.auth
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.room.withTransaction
import kotlinx.coroutines.launch
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

@Serializable
data class UserDataExport(
    val profile: UserProfile?,
    val posts: List<Post>,
    val comments: List<Comment>,
    val workspaceMemberships: List<WorkspaceMember>,
    val acknowledgments: List<AgreementAcknowledgment>,
    val exportTimestamp: Long
)



class AppRepository(
    private val db: AppDatabase, 
    private val context: android.content.Context? = null,
    val externalClient: ExternalIntegrationsClient? = null
) {
    private val json = Json { 
        ignoreUnknownKeys = true
        prettyPrint = true
        coerceInputValues = true
    }
    val userDao = db.userDao()
    fun getUserById(userId: String): Flow<UserProfile?> = userDao.getUserById(userId)

    suspend fun exportUserData(userId: String): String {
        val profile = userDao.getUserById(userId).firstOrNull()
        val posts = postDao.getPostsByAuthor(userId).firstOrNull() ?: emptyList()
        val comments = commentDao.getCommentsByAuthor(userId).firstOrNull() ?: emptyList()
        val memberships = workspaceMemberDao.getWorkspacesForUser(userId).firstOrNull() ?: emptyList()
        val acknowledgments = agreementDao.getAcknowledgmentsForUser(userId).firstOrNull() ?: emptyList()
        
        val export = UserDataExport(
            profile = profile,
            posts = posts,
            comments = comments,
            workspaceMemberships = memberships,
            acknowledgments = acknowledgments,
            exportTimestamp = System.currentTimeMillis()
        )
        
        return json.encodeToString(UserDataExport.serializer(), export)
    }
    val postDao = db.postDao()
    val commentDao = db.commentDao()
    val workspaceDao = db.workspaceDao()
    val workspaceMemberDao = db.workspaceMemberDao()
    val productionTaskDao = db.productionTaskDao()
    val agreementDao = db.agreementDao()
    val messageDao = db.messageDao()
    val adminDao = db.adminDao()
    val projectProposalDao = db.projectProposalDao()
    val talentPitchDao = db.talentPitchDao()
    val reportDao = db.reportDao()
    val auditLogDao = db.auditLogDao()
    val syncDao = db.syncDao()
    val adDao = db.adDao()
    val userSettingsDao = db.userSettingsDao()
    val endorsementDao = db.endorsementDao()
    val savedSearchDao = db.savedSearchDao()
    val lookingForWorkDao = db.lookingForWorkDao()
    val contentCalendarItemDao = db.contentCalendarItemDao()
    val taskTemplateDao = db.taskTemplateDao()
    val disputeNoteDao = db.disputeNoteDao()
    val referralDao = db.referralDao()
    val featureFlagDao = db.featureFlagDao()
    val changelogDao = db.changelogDao()
    val connectionRequestDao = db.connectionRequestDao()
    val notificationDao = db.notificationDao()
    val verificationRequestDao = db.verificationRequestDao()
    val userAuditLogDao = db.userAuditLogDao()
    val supportTicketDao = db.supportTicketDao()
    val crmRecordDao = db.crmRecordDao()
    val announcementDao = db.announcementDao()
    val platformControlDao = db.platformControlDao()
    val founderNoteDao = db.founderNoteDao()
    val workspaceAssetDao = db.workspaceAssetDao()
    val deliverableDao = db.deliverableDao()
    val workspaceEventDao = db.workspaceEventDao()
    
    val personalNoteDao = db.personalNoteDao()
    val communityLikeDao = db.communityLikeDao()
    val workspaceInviteDao = db.workspaceInviteDao()
    val searchFilterDao = db.searchFilterDao()
    val roleConfigurationDao = db.roleConfigurationDao()
 
    init {
        // Pre-populate default feature flags
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val defaults = listOf(
                    FeatureFlag(
                        flagKey = "SYNDICATE_PITCH_CREATION",
                        isEnabled = true,
                        organizerEnabled = true,
                        participantEnabled = true,
                        globalOverrideEnabled = true,
                        description = "Enables creation and submission of new syndicate collaboration pitches.",
                        category = "COLLABORATION"
                    ),
                    FeatureFlag(
                        flagKey = "AGREEMENT_DRAFTING",
                        isEnabled = true,
                        organizerEnabled = true,
                        participantEnabled = false,
                        globalOverrideEnabled = true,
                        description = "Enables drafting, version updating, and hashing of digital team agreements.",
                        category = "GOVERNANCE"
                    ),
                    FeatureFlag(
                        flagKey = "COMMUNITY_FORUM_POSTING",
                        isEnabled = true,
                        organizerEnabled = true,
                        participantEnabled = true,
                        globalOverrideEnabled = true,
                        description = "Allows posting updates and discussion threads in the Creator Commons arena.",
                        category = "COMMUNICATION"
                    ),
                    FeatureFlag(
                        flagKey = "FILE_UPLOADS",
                        isEnabled = true,
                        organizerEnabled = true,
                        participantEnabled = true,
                        globalOverrideEnabled = true,
                        description = "Controls media asset attachment and deliverable uploads across workspaces.",
                        category = "MEDIA"
                    ),
                    FeatureFlag(
                        flagKey = "VOICE_HUDDLE_BETA",
                        isEnabled = true,
                        organizerEnabled = true,
                        participantEnabled = true,
                        globalOverrideEnabled = true,
                        description = "Access to real-time voice huddle rooms for team syncs.",
                        category = "COMMUNICATION"
                    ),
                    FeatureFlag(
                        flagKey = "huddles_enabled",
                        isEnabled = true,
                        organizerEnabled = true,
                        participantEnabled = true,
                        globalOverrideEnabled = true,
                        description = "Enable real-time voice and video workspace huddles for active team creators.",
                        category = "COMMUNICATION"
                    ),
                    FeatureFlag(
                        flagKey = "referral_system_enabled",
                        isEnabled = true,
                        organizerEnabled = true,
                        participantEnabled = true,
                        globalOverrideEnabled = true,
                        description = "Enable user referrals, code sharing, and Premium trial extension rewards.",
                        category = "CORE"
                    ),
                    FeatureFlag(
                        flagKey = "weekly_digest_enabled",
                        isEnabled = true,
                        organizerEnabled = true,
                        participantEnabled = true,
                        globalOverrideEnabled = true,
                        description = "Enable weekly summary digest notifications for matchmaking and active spaces.",
                        category = "COMMUNICATION"
                    ),
                    FeatureFlag(
                        flagKey = "looking_for_work_board_enabled",
                        isEnabled = true,
                        organizerEnabled = true,
                        participantEnabled = true,
                        globalOverrideEnabled = true,
                        description = "Enable the Looking for Work board for active freelancers and matching projects.",
                        category = "COLLABORATION"
                    ),
                    FeatureFlag(
                        flagKey = "creator_commons_enabled",
                        isEnabled = true,
                        organizerEnabled = true,
                        participantEnabled = true,
                        globalOverrideEnabled = true,
                        description = "Control platform-wide access to the shared Creator Commons social arena.",
                        category = "COMMUNICATION"
                    ),
                    FeatureFlag(
                        flagKey = "analytics_enabled",
                        isEnabled = true,
                        organizerEnabled = true,
                        participantEnabled = false,
                        globalOverrideEnabled = true,
                        description = "Enable or disable aggregated team and syndicate metrics.",
                        category = "CORE"
                    ),
                    FeatureFlag(
                        flagKey = "portfolio_discovery_enabled",
                        isEnabled = true,
                        organizerEnabled = true,
                        participantEnabled = true,
                        globalOverrideEnabled = true,
                        description = "Control search index visibility for creator profile discovery.",
                        category = "MEDIA"
                    ),
                    FeatureFlag(
                        flagKey = "verification_requests_enabled",
                        isEnabled = true,
                        organizerEnabled = true,
                        participantEnabled = true,
                        globalOverrideEnabled = true,
                        description = "Enable or disable automated peer-to-peer verification pipelines.",
                        category = "GOVERNANCE"
                    ),
                    FeatureFlag(
                        flagKey = "founder_crm_enabled",
                        isEnabled = true,
                        organizerEnabled = true,
                        participantEnabled = false,
                        globalOverrideEnabled = true,
                        description = "Control access to automated founder crm, cohort tracking and health score logs.",
                        category = "GOVERNANCE"
                    ),
                    FeatureFlag(
                        flagKey = "moderation_center_enabled",
                        isEnabled = true,
                        organizerEnabled = true,
                        participantEnabled = false,
                        globalOverrideEnabled = true,
                        description = "Enable platform-wide profile audits, flag reviews, and moderation dashboards.",
                        category = "GOVERNANCE"
                    )
                )
                for (f in defaults) {
                    if (featureFlagDao.getFeatureFlag(f.flagKey) == null) {
                        featureFlagDao.insertFeatureFlag(f)
                    }
                }
                
                // Pre-populate default Platform Settings
                if (platformControlDao.getPlatformSettings() == null) {
                    platformControlDao.insertPlatformSettings(
                        PlatformSettings(
                            id = "singleton_settings",
                            maintenanceMode = false,
                            betaMode = true,
                            registrationEnabled = true,
                            inviteOnlyEnabled = false
                        )
                    )
                }

                // Pre-populate Onboarding Slides
                platformControlDao.getAllOnboardingSlidesFlow().firstOrNull()?.let { slides ->
                    if (slides.isEmpty()) {
                        platformControlDao.insertOnboardingSlide(OnboardingSlide("slide_01", "Welcome to Creator Co-Op", "The premier decentralized platform for joint media productions and creator milestone syndicates.", "Hub", 0))
                        platformControlDao.insertOnboardingSlide(OnboardingSlide("slide_02", "Automated Milestone Splits", "Draft standard peer-to-peer agreement templates and let secure ledgers handle auto-payout allocations.", "Handshake", 1))
                        platformControlDao.insertOnboardingSlide(OnboardingSlide("slide_03", "Real-Time Collaboration", "Connect via live video huddles, review work-in-progress materials, and sync pipelines instantly.", "Groups", 2))
                    }
                }

                // Pre-populate Welcome Messages
                platformControlDao.getAllWelcomeMessagesFlow().firstOrNull()?.let { msgs ->
                    if (msgs.isEmpty()) {
                        platformControlDao.insertWelcomeMessage(WelcomeMessage("welcome_01", "Platform Core v1.5 Deployed!", "Welcome back, creative partners! Explore our newly integrated Founder CRM and dynamic communication campaigns.", "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe", true))
                    }
                }

                // Pre-populate Empty States
                platformControlDao.getAllEmptyStatesFlow().firstOrNull()?.let { states ->
                    if (states.isEmpty()) {
                        platformControlDao.insertEmptyState(EmptyStateConfig("empty_commons", "Commons", "Commons", "empty_commons", "No Feed Discussions Yet", "Be the first to share an update, start a video huddle, or publish a milestone achievement."))
                        platformControlDao.insertEmptyState(EmptyStateConfig("empty_crm", "CRM", "empty_crm", "Cohort List is Currently Clear", "Add active founders or prospective candidates to track interactive health metrics and logs."))
                    }
                }

                // Pre-populate Help Texts
                platformControlDao.getAllHelpTextsFlow().firstOrNull()?.let { helps ->
                    if (helps.isEmpty()) {
                        platformControlDao.insertHelpText(HelpText("help_milestone", "Milestones", "Milestones dictate automatic financial or work-hours splits between creators. Once all members sign off on completion, funds or achievements are unlocked.", "Syndicates"))
                        platformControlDao.insertHelpText(HelpText("help_verification", "Verification", "Verified Creator status requires a linked portfolio URL, valid social integrations, and positive peer recommendations from at least two active members.", "Security"))
                    }
                }

                // Pre-populate initial changelog entry if none exists
                if (changelogDao.getLatestChangelogEntry() == null) {
                    changelogDao.insertChangelogEntry(
                        ChangelogEntry(
                            id = "initial_v1",
                            title = "Creator Co-Op v1.0 Launch",
                            description = "Welcome to the future of creator collaboration! Features: Syndicates, Workspace Hub, Global Registry, and Pro Marketplace active.",
                            versionName = "1.0.0"
                        )
                    )
                }

                // Pre-populate all 20 launch checklist tasks as COMPLETED for founder_admin
                for (i in 1..20) {
                    val key = "launch_task_$i"
                    if (userSettingsDao.getSetting("founder_admin", key) == null) {
                        userSettingsDao.setSetting(
                            com.example.data.model.UserSetting(
                                id = "founder_admin_$key",
                                userId = "founder_admin",
                                key = key,
                                value = "COMPLETED"
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("AppRepository", "Error prepopulating feature flags: ${e.message}")
            }
        }
    }

    fun getDisputeNotesForWorkspace(workspaceId: String, requestingUserId: String): Flow<List<DisputeNote>> = 
        disputeNoteDao.getDisputeNotesForWorkspace(workspaceId, requestingUserId)

    fun getDisputeNotesAboutUser(targetUserId: String, requestingUserId: String): Flow<List<DisputeNote>> = 
        disputeNoteDao.getDisputeNotesAboutUser(targetUserId, requestingUserId)

    fun getDisputeNotesAboutUsers(targetUserIds: List<String>, requestingUserId: String): Flow<List<DisputeNote>> = 
        disputeNoteDao.getDisputeNotesAboutUsers(targetUserIds, requestingUserId)

    fun getAllDisputeNotesFlow(): Flow<List<DisputeNote>> = 
        disputeNoteDao.getAllDisputeNotes()

    suspend fun insertDisputeNote(note: DisputeNote) {
        disputeNoteDao.insertDisputeNote(note)
        queueSync("DISPUTE_NOTE", note, "UPSERT")
        context?.let { SupabaseSynchronizer.syncUpDisputeNote(it, note) }
    }

    fun getCalendarItemsForWorkspace(workspaceId: String): Flow<List<ContentCalendarItem>> = 
        contentCalendarItemDao.getCalendarItemsForWorkspace(workspaceId)

    suspend fun insertCalendarItem(item: ContentCalendarItem) = 
        contentCalendarItemDao.insertCalendarItem(item)

    suspend fun deleteCalendarItem(id: String) = 
        contentCalendarItemDao.deleteCalendarItem(id)

    fun getTemplatesForWorkspace(workspaceId: String): Flow<List<TaskTemplate>> = 
        taskTemplateDao.getTemplatesForWorkspace(workspaceId)

    suspend fun insertTemplate(template: TaskTemplate) = 
        taskTemplateDao.insertTemplate(template)

    suspend fun deleteTemplate(id: String) = 
        taskTemplateDao.deleteTemplate(id)

    val allProjectProposals: Flow<List<ProjectProposalWithData>> = projectProposalDao.getAllProjectProposalsList(System.currentTimeMillis())
    
    fun getProjectProposals(): PagingSource<Int, ProjectProposalWithData> = projectProposalDao.getAllProjectProposals(System.currentTimeMillis())
    val pendingReports: Flow<List<Report>> = reportDao.getPendingReports()
    val allReports: Flow<List<Report>> = reportDao.getAllReportsFlow()
    val allAuditLogs: Flow<List<AuditLog>> = auditLogDao.getAllLogs()
    
    val allAdPlacements: Flow<List<AdPlacement>> = adDao.getAllPlacements()
    val globalAdSettings: Flow<List<GlobalSetting>> = adDao.getSettings()

    // User Settings
    suspend fun getThemePreference(userId: String): String? = userSettingsDao.getSetting(userId, "theme")
    suspend fun setThemePreference(userId: String, preference: String) = userSettingsDao.setSetting(UserSetting(id = "${userId}_theme", userId = userId, key = "theme", value = preference))

    suspend fun getUiTextSizeSetting(userId: String): String? = userSettingsDao.getSetting(userId, "ui_text_size")
    suspend fun setUiTextSizeSetting(userId: String, size: String) = userSettingsDao.setSetting(UserSetting(id = "${userId}_ui_text_size", userId = userId, key = "ui_text_size", value = size))

    suspend fun getSyncFrequencySetting(userId: String): String? = userSettingsDao.getSetting(userId, "sync_frequency")
    suspend fun setSyncFrequencySetting(userId: String, frequency: String) = userSettingsDao.setSetting(UserSetting(id = "${userId}_sync_frequency", userId = userId, key = "sync_frequency", value = frequency))

    suspend fun getHapticFeedbackSetting(userId: String): Boolean {
        val value = userSettingsDao.getSetting(userId, "haptic_feedback_enabled")
        return value == null || value == "true" // defaults to true (enabled)
    }

    suspend fun setHapticFeedbackSetting(userId: String, enabled: Boolean) {
        userSettingsDao.setSetting(
            UserSetting(
                id = "${userId}_haptic_feedback_enabled",
                userId = userId,
                key = "haptic_feedback_enabled",
                value = enabled.toString()
            )
        )
    }

    suspend fun updateAdPlacement(placement: AdPlacement) {
        adDao.updatePlacement(placement)
    }

    suspend fun updateUserProfile(profile: UserProfile) {
        userDao.insertUser(profile)
        val currentContext = context
        if (currentContext != null && SupabaseConfig.isConfigured && SupabaseConfig.isNetworkAvailable(currentContext)) {
            SupabaseSynchronizer.syncUpUser(currentContext, profile)
        } else {
            syncDao.insertSyncEvent(SyncEntity(
                id = UUID.randomUUID().toString(),
                entityType = "USER_PROFILE",
                entityJson = Json.encodeToString(UserProfile.serializer(), profile),
                actionType = "UPSERT",
                createdAt = System.currentTimeMillis()
            ))
        }
    }

    suspend fun updateGlobalAdSetting(setting: GlobalSetting) {
        adDao.updateSetting(setting)
    }

    private suspend fun queueSync(
        entityType: String,
        entity: Any,
        action: String = "UPSERT",
        syncCall: (suspend () -> Unit)? = null
    ): String {
        val jsonStr = when (entity) {
            is Post -> json.encodeToString(Post.serializer(), entity)
            is Message -> json.encodeToString(Message.serializer(), entity)
            is ProductionTask -> json.encodeToString(ProductionTask.serializer(), entity)
            is AgreementAcknowledgment -> json.encodeToString(AgreementAcknowledgment.serializer(), entity)
            is UserProfile -> json.encodeToString(UserProfile.serializer(), entity)
            is Comment -> json.encodeToString(Comment.serializer(), entity)
            is Workspace -> json.encodeToString(Workspace.serializer(), entity)
            is WorkspaceMember -> json.encodeToString(WorkspaceMember.serializer(), entity)
            is TeamAgreement -> json.encodeToString(TeamAgreement.serializer(), entity)
            is Report -> json.encodeToString(Report.serializer(), entity)
            is AuditLog -> json.encodeToString(AuditLog.serializer(), entity)
            is Endorsement -> json.encodeToString(Endorsement.serializer(), entity)
            is SavedSearch -> json.encodeToString(SavedSearch.serializer(), entity)
            is LookingForWork -> json.encodeToString(LookingForWork.serializer(), entity)
            is WorkspaceAsset -> json.encodeToString(WorkspaceAsset.serializer(), entity)
            is Deliverable -> json.encodeToString(Deliverable.serializer(), entity)
            is WorkspaceEvent -> json.encodeToString(WorkspaceEvent.serializer(), entity)
            is SupportTicket -> json.encodeToString(SupportTicket.serializer(), entity)
            is DisputeNote -> json.encodeToString(DisputeNote.serializer(), entity)
            is FounderNote -> json.encodeToString(FounderNote.serializer(), entity)
            is VerificationRequest -> json.encodeToString(VerificationRequest.serializer(), entity)
            is ConnectionRequest -> json.encodeToString(ConnectionRequest.serializer(), entity)
            else -> ""
        }
        val id = when (entity) {
            is Post -> entity.id
            is Message -> entity.id
            is ProductionTask -> entity.id
            is AgreementAcknowledgment -> entity.id
            is UserProfile -> entity.id
            is Comment -> entity.id
            is Workspace -> entity.id
            is WorkspaceMember -> entity.id
            is TeamAgreement -> entity.id
            is Report -> entity.id
            is AuditLog -> entity.id
            is Endorsement -> "${entity.giverId}_${entity.receiverId}_${entity.workspaceId}"
            is SavedSearch -> entity.id
            is LookingForWork -> entity.userId
            is WorkspaceAsset -> entity.id
            is Deliverable -> entity.id
            is WorkspaceEvent -> entity.id
            is SupportTicket -> entity.id
            is DisputeNote -> entity.id
            is FounderNote -> entity.id
            is VerificationRequest -> entity.id
            is ConnectionRequest -> entity.id
            else -> UUID.randomUUID().toString()
        }

        val eventId = "${entityType}_${id}_${action}_${System.currentTimeMillis()}"
        syncDao.insertSyncEvent(
            SyncEntity(
                id = eventId,
                entityType = entityType,
                entityJson = jsonStr,
                actionType = action,
                createdAt = System.currentTimeMillis(),
                syncStatus = "PENDING",
                retryCount = 0,
                lastAttemptedAt = 0
            )
        )

        if (syncCall != null) {
            try {
                syncCall()
                syncDao.updateSyncStatus(eventId, "SYNCED")
            } catch (e: Exception) {
                Log.w("AppRepository", "Direct sync call failed for $eventId, left as PENDING for retry: ${e.message}")
            }
        }
        return eventId
    }

    suspend fun submitReport(report: Report) {
        reportDao.insertReport(report)
        queueSync("REPORT", report)
        context?.let { SupabaseSynchronizer.syncUpReport(it, report) }
        if (report.reporterId != "SYSTEM_AUTOMATION") {
            val targetUserId = when (report.targetType.uppercase()) {
                "USER" -> report.targetId
                "POST" -> getPostByIdSync(report.targetId)?.authorId
                "COMMENT" -> getCommentById(report.targetId)?.authorId
                "MESSAGE" -> getMessageById(report.targetId)?.senderId
                else -> null
            }
            if (targetUserId != null) {
                checkReportRateLimitAgainstUser(targetUserId)
            }
        }
    }

    suspend fun insertAuditLog(log: AuditLog) {
        auditLogDao.insertLog(log)
        queueSync("AUDIT_LOG", log)
        context?.let { SupabaseSynchronizer.syncUpAuditLog(it, log) }
    }

    suspend fun updateReportStatus(reportId: String, status: String, adminId: String = "SYSTEM") {
        reportDao.updateReportStatus(reportId, status)
        val report = reportDao.getReportById(reportId)
        if (report != null) {
            queueSync("REPORT", report)
            context?.let { SupabaseSynchronizer.syncUpReport(it, report) }
            
            AnalyticsManager.trackEvent("report_resolved", mapOf("report_id" to reportId, "status" to status))
            
            insertAuditLog(AuditLog(
                id = "audit_" + System.currentTimeMillis(),
                adminId = adminId,
                actionTaken = "Updated Report Status to $status",
                targetType = "REPORT",
                targetId = reportId,
                reason = "Routine moderation review"
            ))
            
            insertNotification(Notification(
                id = "notif_rep_" + System.currentTimeMillis(),
                userId = report.reporterId,
                title = "Report Update",
                body = "Your report regarding ${report.targetType} has been reviewed and marked as $status.",
                type = "REPORT_RESOLVED",
                createdAt = System.currentTimeMillis()
            ))
            
            if (status == "RESOLVED" && report.targetType == "USER") {
                userDao.getUserByIdSuspend(report.targetId)?.let { targetUser ->
                    val updatedTrust = (targetUser.reputationScore - 10).coerceAtLeast(0)
                    val updatedUser = targetUser.copy(
                        reputationScore = updatedTrust,
                        reliabilityBadge = "Warning"
                    )
                    userDao.insertUser(updatedUser)
                    queueSync("USER_PROFILE", updatedUser)
                    context?.let { SupabaseSynchronizer.syncUpUser(it, updatedUser) }
                }
            } else if (status == "DISMISSED" && report.targetType == "USER") {
                userDao.getUserByIdSuspend(report.targetId)?.let { targetUser ->
                    val updatedUser = targetUser.copy(reliabilityBadge = "Silver")
                    userDao.insertUser(updatedUser)
                    queueSync("USER_PROFILE", updatedUser)
                    context?.let { SupabaseSynchronizer.syncUpUser(it, updatedUser) }
                }
            }
        }
    }

    suspend fun getTelemetry(): Map<String, Int> {
        return mapOf(
            "users" to adminDao.getUserCount(),
            "workspaces" to adminDao.getWorkspaceCount(),
            "posts" to adminDao.getPostCount()
        )
    }

    suspend fun getReportById(id: String) = reportDao.getReportById(id)
    suspend fun getPostByIdSync(id: String) = postDao.getPostById(id).firstOrNull()
    suspend fun getCommentById(id: String) = commentDao.getCommentById(id)
    suspend fun getMessageById(id: String) = messageDao.getMessageById(id)

    suspend fun deletePost(id: String, requesterId: String? = null) {
        if (requesterId != null) {
            val post = postDao.getPostById(id).firstOrNull()
            val requester = userDao.getUserById(requesterId).firstOrNull()
            val isAuthor = post?.authorId == requesterId
            val isAdmin = requester?.systemRole in listOf("ADMIN", "PLATFORM_ADMIN") || requester?.globalRole == "ADMIN"
            if (!isAuthor && !isAdmin) {
                throw SecurityException("Unauthorized: Cannot delete another user's post.")
            }
        }
        commentDao.deleteCommentsForPost(id)
        postDao.deletePost(id)
        queueSync("POST", id, "DELETE")
        context?.let { SupabaseSynchronizer.syncDeletePost(it, id) }
    }

    suspend fun getLastSeenVersion(userId: String): String? {
        return userSettingsDao.getSetting(userId, "last_seen_version")
    }

    suspend fun setLastSeenVersion(userId: String, versionName: String) {
        userSettingsDao.setSetting(
            UserSetting(
                id = "${userId}_last_seen_version",
                userId = userId,
                key = "last_seen_version",
                value = versionName
            )
        )
    }

    suspend fun boostProposal(proposalId: String) {
        val proposal = projectProposalDao.getProjectProposalByIdSuspend(proposalId)
        if (proposal != null) {
            val updated = proposal.copy(boostedUntil = System.currentTimeMillis() + (48 * 60 * 60 * 1000))
            insertProjectProposal(updated)
        }
    }

    suspend fun updateWorkspaceSponsorship(workspaceId: String, isSponsored: Boolean, sponsorName: String?, sponsorLogoUrl: String?) {
        adminDao.updateWorkspaceSponsorship(workspaceId, isSponsored, sponsorName, sponsorLogoUrl)
    }

    suspend fun setVerifiedPro(userId: String, isPro: Boolean) {
        val user = userDao.getUserById(userId).firstOrNull()
        if (user != null) {
            saveUser(user.copy(isVerifiedPro = isPro))
        }
    }

    suspend fun deleteComment(id: String, requesterId: String? = null) {
        val comment = commentDao.getCommentById(id)
        if (requesterId != null && comment != null) {
            val requester = userDao.getUserById(requesterId).firstOrNull()
            val isAuthor = comment.authorId == requesterId
            val isAdmin = requester?.systemRole in listOf("ADMIN", "PLATFORM_ADMIN") || requester?.globalRole == "ADMIN"
            if (!isAuthor && !isAdmin) {
                throw SecurityException("Unauthorized: Cannot delete another user's comment.")
            }
        }
        val postId = comment?.postId
        commentDao.deleteComment(id)
        if (postId != null) {
            postDao.refreshPostCommentCount(postId)
        }
        context?.let { SupabaseSynchronizer.syncDeleteComment(it, id) }
    }

    suspend fun deleteMessage(id: String, requesterId: String? = null) {
        if (requesterId != null) {
            val msg = messageDao.getMessageById(id)
            val requester = userDao.getUserById(requesterId).firstOrNull()
            val isSender = msg?.senderId == requesterId
            val isAdmin = requester?.systemRole in listOf("ADMIN", "PLATFORM_ADMIN") || requester?.globalRole == "ADMIN"
            if (!isSender && !isAdmin) {
                throw SecurityException("Unauthorized: Cannot delete another user's message.")
            }
        }
        messageDao.deleteMessage(id)
        queueSync("MESSAGE", id, "DELETE")
        context?.let { SupabaseSynchronizer.syncDeleteMessage(it, id) }
    }

    fun getPitchesForProject(projectId: String): Flow<List<TalentPitch>> = talentPitchDao.getPitchesForProject(projectId)
    fun getPitchesBySender(senderId: String): Flow<List<TalentPitch>> = talentPitchDao.getPitchesBySender(senderId)

    suspend fun insertProjectProposal(proposal: ProjectProposal) {
        projectProposalDao.insertProjectProposal(proposal)
    }

    suspend fun deleteProjectProposalById(id: String) {
        talentPitchDao.deletePitchesForProject(id)
        projectProposalDao.deleteProjectProposalById(id)
    }

    suspend fun declineOtherPitchesForProject(projectId: String, acceptedPitchId: String) {
        talentPitchDao.declineOtherPitchesForProject(projectId, acceptedPitchId)
    }

    suspend fun insertTalentPitch(pitch: TalentPitch) {
        talentPitchDao.insertTalentPitch(pitch)
        checkConnectionRequestRateLimit(pitch.senderId)
    }

    suspend fun updatePitchStatus(id: String, status: String) {
        talentPitchDao.updatePitchStatus(id, status)
    }

    // Flagged users and workspaces for Admin
    val flaggedUsers: Flow<List<UserProfile>> = adminDao.getFlaggedUsers()
    val allWorkspacesAdmin: Flow<List<Workspace>> = adminDao.getAllWorkspacesForAdmin()

    suspend fun setUserRole(userId: String, role: String, requesterId: String? = null) {
        if (requesterId != null) {
            val requester = userDao.getUserById(requesterId).firstOrNull()
            val isAdmin = requester?.systemRole in listOf("ADMIN", "PLATFORM_ADMIN") || requester?.globalRole == "ADMIN"
            if (!isAdmin) {
                throw SecurityException("Unauthorized: Caller $requesterId is not an administrator.")
            }
        }
        adminDao.setUserRole(userId, role)
        ClientSecurityInterceptor.wrap {
            SupabaseConfig.client.postgrest.from("user_profiles").update(mapOf("global_role" to role)) {
                filter {
                    eq("id", userId)
                }
            }
        }
    }

    /**
     * Connects Postgrest data parsing directly to Room data class insertions using clean map loops.
     */
    suspend fun syncRemoteUsers() {
        if (!SupabaseConfig.isConfigured) return
        ClientSecurityInterceptor.wrap {
            val response = SupabaseConfig.client.postgrest.from("user_profiles").select().data
            val remoteUsers = json.decodeFromString<List<UserProfile>>(response)
            remoteUsers.map { user ->
                userDao.insertUser(user)
            }
        }
    }

    // Flow streams
    val trendingPosts: Flow<List<Post>> = postDao.getAllPostsSortedByTrendingList()
    val newPosts: Flow<List<Post>> = postDao.getAllPostsSortedByNewList()
    val topPosts: Flow<List<Post>> = postDao.getAllPostsSortedByTopList()

    fun getTrendingPosts(): PagingSource<Int, Post> = postDao.getAllPostsSortedByTrending()
    fun getNewPosts(): PagingSource<Int, Post> = postDao.getAllPostsSortedByNew()
    fun getTopPosts(): PagingSource<Int, Post> = postDao.getAllPostsSortedByTop()
    val allWorkspaces: Flow<List<Workspace>> = workspaceDao.getAllWorkspaces()
    val allUsers: Flow<List<UserProfile>> = userDao.getAllUsers()

    fun getWorkspacesByOwner(userId: String) = workspaceDao.getWorkspacesByOwner(userId)
    fun getWorkspaceById(id: String) = workspaceDao.getWorkspaceById(id)
    fun getMembersForWorkspace(workspaceId: String) = workspaceMemberDao.getMembersForWorkspace(workspaceId)
    fun getMemberInfo(workspaceId: String, userId: String) = workspaceMemberDao.getMemberInfo(workspaceId, userId)
    
    suspend fun updateMemberPresence(workspaceId: String, userId: String, isOnline: Boolean) {
        workspaceMemberDao.updateMemberPresence(workspaceId, userId, isOnline, System.currentTimeMillis())
        workspaceMemberDao.getMemberInfoSuspend(workspaceId, userId)?.let { member ->
            queueSync("WORKSPACE_MEMBER", member, "UPSERT")
            context?.let { SupabaseSynchronizer.syncUpWorkspaceMember(it, member) }
        }
    }
    
    suspend fun updateMemberTyping(workspaceId: String, userId: String, isTyping: Boolean, typingText: String = "") {
        workspaceMemberDao.updateMemberTyping(workspaceId, userId, isTyping, typingText)
        workspaceMemberDao.getMemberInfoSuspend(workspaceId, userId)?.let { member ->
            queueSync("WORKSPACE_MEMBER", member, "UPSERT")
            context?.let { SupabaseSynchronizer.syncUpWorkspaceMember(it, member) }
        }
    }
    
    suspend fun updateMemberViewingTask(workspaceId: String, userId: String, taskId: String?) {
        workspaceMemberDao.updateMemberViewingTask(workspaceId, userId, taskId)
        workspaceMemberDao.getMemberInfoSuspend(workspaceId, userId)?.let { member ->
            queueSync("WORKSPACE_MEMBER", member, "UPSERT")
            context?.let { SupabaseSynchronizer.syncUpWorkspaceMember(it, member) }
        }
    }
    
    suspend fun updateMemberEditingAsset(workspaceId: String, userId: String, assetId: String?) {
        workspaceMemberDao.updateMemberEditingAsset(workspaceId, userId, assetId)
        workspaceMemberDao.getMemberInfoSuspend(workspaceId, userId)?.let { member ->
            queueSync("WORKSPACE_MEMBER", member, "UPSERT")
            context?.let { SupabaseSynchronizer.syncUpWorkspaceMember(it, member) }
        }
    }
    
    suspend fun updateMemberLiveStatus(workspaceId: String, userId: String, statusUpdate: String) {
        workspaceMemberDao.updateMemberLiveStatus(workspaceId, userId, statusUpdate, System.currentTimeMillis())
        workspaceMemberDao.getMemberInfoSuspend(workspaceId, userId)?.let { member ->
            queueSync("WORKSPACE_MEMBER", member, "UPSERT")
            context?.let { SupabaseSynchronizer.syncUpWorkspaceMember(it, member) }
        }
    }
    fun getProductionTasks(workspaceId: String) = productionTaskDao.getProductionReadyTasks(workspaceId)
    fun getAllTasksForUser(userId: String) = productionTaskDao.getAllTasksForUser(userId)
    fun getAllProductionTasksFlow() = productionTaskDao.getAllProductionTasks()
    fun getPrivateDraftTasks(workspaceId: String, userId: String) = productionTaskDao.getPrivateDraftTasks(workspaceId, userId)
    fun getTaskById(id: String) = productionTaskDao.getTaskById(id)
    fun getMessagesForWorkspace(workspaceId: String) = messageDao.getMessagesForWorkspace(workspaceId)
    fun getDMsForWorkspace(workspaceId: String, myId: String) = messageDao.getDMsForWorkspace(workspaceId, myId)
    fun getAllDMsForUser(myId: String) = messageDao.getAllDMsForUser(myId)
    fun getLatestAgreement(workspaceId: String) = agreementDao.getLatestAgreementFlow(workspaceId)
    fun getAllAgreementsForWorkspace(workspaceId: String) = agreementDao.getAllAgreementsForWorkspace(workspaceId)
    fun getAcknowledgmentsFlow(agreementId: String) = agreementDao.getAcknowledgmentsFlow(agreementId)
    fun getAcknowledgmentsForUser(userId: String) = agreementDao.getAcknowledgmentsForUser(userId)

    suspend fun createAgreement(agreement: TeamAgreement, editorId: String? = null) {
        if (editorId != null) {
            val existing = agreementDao.getLatestAgreement(agreement.workspaceId)
            if (existing != null && existing.isLocked) {
                throw SecurityException("Cannot update a locked team agreement!")
            }
            val members = workspaceMemberDao.getMembersForWorkspaceList(agreement.workspaceId)
            val member = members.find { it.userId == editorId }
            if (member == null || member.assignedRoleTitle !in listOf("Lead Creator", "Head")) {
                throw SecurityException("Only Lead Creator or Head can update team agreements!")
            }
        }
        agreementDao.insertAgreement(agreement)
        queueSync("AGREEMENT", agreement) {
            context?.let { SupabaseSynchronizer.syncUpAgreement(it, agreement) }
        }
    }

    suspend fun insertAcknowledgment(acknowledgment: AgreementAcknowledgment) {
        agreementDao.insertAcknowledgment(acknowledgment)
        queueSync("AGREEMENT_ACK", acknowledgment) {
            context?.let { SupabaseSynchronizer.syncUpAcknowledgment(it, acknowledgment) }
        }
    }

    val allSyncEvents: kotlinx.coroutines.flow.Flow<List<SyncEntity>> = syncDao.getAllSyncEventsFlow()
    suspend fun clearSyncedHistory() = syncDao.clearSyncedEvents()
    suspend fun deleteSyncEvent(id: String) = syncDao.deleteSyncEvent(id)
    suspend fun updateSyncEvent(event: SyncEntity) = syncDao.updateSyncEvent(event)

    suspend fun syncUpPendingEvents() {
        val pendingEvents = syncDao.getPendingSyncEventsSuspend()
        context ?: return
        for (event in pendingEvents) {
            try {
                val success = when (event.entityType) {
                    "POST" -> { 
                        val post = json.decodeFromString(Post.serializer(), event.entityJson)
                        if (event.actionType == "UPSERT") SupabaseSynchronizer.syncUpPost(context, post)
                        else SupabaseSynchronizer.syncDeletePost(context, post.id)
                        true
                    }
                    "MESSAGE" -> {
                        if (event.actionType == "UPSERT") {
                            val message = json.decodeFromString(Message.serializer(), event.entityJson)
                            SupabaseSynchronizer.syncUpMessage(context, message)
                        } else {
                            val parts = event.id.split("_")
                            val id = parts.getOrNull(1) ?: event.id
                            SupabaseSynchronizer.syncDeleteMessage(context, id)
                        }
                        true
                    }
                    "TASK" -> {
                        if (event.actionType == "UPSERT") {
                            val task = json.decodeFromString(ProductionTask.serializer(), event.entityJson)
                            SupabaseSynchronizer.syncUpProductionTask(context, task)
                        } else {
                            val parts = event.id.split("_")
                            val id = parts.getOrNull(1) ?: event.id
                            SupabaseSynchronizer.syncDeleteProductionTask(context, id)
                        }
                        true
                    }
                    "AGREEMENT_ACK" -> {
                        val ack = json.decodeFromString(AgreementAcknowledgment.serializer(), event.entityJson)
                        SupabaseSynchronizer.syncUpAcknowledgment(context, ack)
                        true
                    }
                    "USER" -> {
                        val user = json.decodeFromString(UserProfile.serializer(), event.entityJson)
                        SupabaseSynchronizer.syncUpUser(context, user)
                        true
                    }
                    "ENDORSEMENT" -> {
                        val endorsement = json.decodeFromString(Endorsement.serializer(), event.entityJson)
                        SupabaseSynchronizer.syncUpEndorsement(context, endorsement)
                        true
                    }
                    "COMMENT" -> {
                        val comment = json.decodeFromString(Comment.serializer(), event.entityJson)
                        if (event.actionType == "UPSERT") SupabaseSynchronizer.syncUpComment(context, comment)
                        else SupabaseSynchronizer.syncDeleteComment(context, comment.id)
                        true
                    }
                    "WORKSPACE" -> {
                        val workspace = json.decodeFromString(Workspace.serializer(), event.entityJson)
                        SupabaseSynchronizer.syncUpWorkspace(context, workspace)
                        true
                    }
                    "WORKSPACE_MEMBER" -> {
                        val member = json.decodeFromString(WorkspaceMember.serializer(), event.entityJson)
                        SupabaseSynchronizer.syncUpWorkspaceMember(context, member)
                        true
                    }
                    "AGREEMENT" -> {
                        val agreement = json.decodeFromString(TeamAgreement.serializer(), event.entityJson)
                        SupabaseSynchronizer.syncUpAgreement(context, agreement)
                        true
                    }
                    "REPORT" -> {
                        val report = json.decodeFromString(Report.serializer(), event.entityJson)
                        SupabaseSynchronizer.syncUpReport(context, report)
                        true
                    }
                    "AUDIT_LOG" -> {
                        val log = json.decodeFromString(AuditLog.serializer(), event.entityJson)
                        SupabaseSynchronizer.syncUpAuditLog(context, log)
                        true
                    }
                    "SAVED_SEARCH" -> {
                        if (event.actionType == "UPSERT") {
                            val search = json.decodeFromString(SavedSearch.serializer(), event.entityJson)
                            SupabaseSynchronizer.syncUpSavedSearch(context, search)
                        } else {
                            val parts = event.id.split("_")
                            val id = parts.getOrNull(1) ?: event.id
                            SupabaseSynchronizer.syncDeleteSavedSearch(context, id)
                        }
                        true
                    }
                    "LOOKING_FOR_WORK" -> {
                        if (event.actionType == "UPSERT") {
                            val listing = json.decodeFromString(LookingForWork.serializer(), event.entityJson)
                            SupabaseSynchronizer.syncUpLookingForWork(context, listing)
                        } else {
                            val parts = event.id.split("_")
                            val userId = parts.getOrNull(1) ?: event.id
                            SupabaseSynchronizer.syncDeleteLookingForWork(context, userId)
                        }
                        true
                    }
                    "WORKSPACE_ASSET" -> {
                        if (event.actionType == "UPSERT") {
                            val asset = json.decodeFromString(WorkspaceAsset.serializer(), event.entityJson)
                            SupabaseSynchronizer.syncUpWorkspaceAsset(context, asset)
                        } else {
                            val parts = event.id.split("_")
                            val id = parts.getOrNull(1) ?: event.id
                            SupabaseSynchronizer.syncDeleteWorkspaceAsset(context, id)
                        }
                        true
                    }
                    "DELIVERABLE" -> {
                        if (event.actionType == "UPSERT") {
                            val deliverable = json.decodeFromString(Deliverable.serializer(), event.entityJson)
                            SupabaseSynchronizer.syncUpDeliverable(context, deliverable)
                        } else {
                            val parts = event.id.split("_")
                            val id = parts.getOrNull(1) ?: event.id
                            SupabaseSynchronizer.syncDeleteDeliverable(context, id)
                        }
                        true
                    }
                    "WORKSPACE_EVENT" -> {
                        val wsEvent = json.decodeFromString(WorkspaceEvent.serializer(), event.entityJson)
                        SupabaseSynchronizer.syncUpWorkspaceEvent(context, wsEvent)
                        true
                    }
                    "SUPPORT_TICKET" -> {
                        if (event.actionType == "UPSERT") {
                            val ticket = json.decodeFromString(SupportTicket.serializer(), event.entityJson)
                            SupabaseSynchronizer.syncUpSupportTicket(context, ticket)
                        } else {
                            val parts = event.id.split("_")
                            val id = parts.getOrNull(1) ?: event.id
                            SupabaseSynchronizer.syncDeleteSupportTicket(context, id)
                        }
                        true
                    }
                    "DISPUTE_NOTE" -> {
                        val note = json.decodeFromString(DisputeNote.serializer(), event.entityJson)
                        SupabaseSynchronizer.syncUpDisputeNote(context, note)
                        true
                    }
                    "FOUNDER_NOTE" -> {
                        if (event.actionType == "UPSERT") {
                            val note = json.decodeFromString(FounderNote.serializer(), event.entityJson)
                            SupabaseSynchronizer.syncUpFounderNote(context, note)
                        } else {
                            val parts = event.id.split("_")
                            val id = parts.getOrNull(1) ?: event.id
                            SupabaseSynchronizer.syncDeleteFounderNote(context, id)
                        }
                        true
                    }
                    "VERIFICATION_REQUEST" -> {
                        val request = json.decodeFromString(VerificationRequest.serializer(), event.entityJson)
                        SupabaseSynchronizer.syncUpVerificationRequest(context, request)
                        true
                    }
                    "CONNECTION_REQUEST" -> {
                        val request = json.decodeFromString(ConnectionRequest.serializer(), event.entityJson)
                        SupabaseSynchronizer.syncUpConnectionRequest(context, request)
                        true
                    }
                    else -> false
                }
                if (success) {
                    syncDao.updateSyncStatus(event.id, "SYNCED")
                }
            } catch (e: Exception) {
                val retries = event.retryCount + 1
                val status = if (retries >= 5) "FAILED" else "PENDING"
                syncDao.updateSyncEvent(event.copy(syncStatus = status, retryCount = retries, lastAttemptedAt = System.currentTimeMillis()))
            }
        }
    }

    // Write actions
    suspend fun saveUser(user: UserProfile) {
        userDao.insertUser(user)
        queueSync("USER", user) {
            context?.let { SupabaseSynchronizer.syncUpUser(it, user) }
        }
    }

    suspend fun insertPost(post: Post) {
        postDao.insertPost(post)
        queueSync("POST", post) {
            context?.let { SupabaseSynchronizer.syncUpPost(it, post) }
        }
        checkPostRateLimit(post.authorId)
    }

    suspend fun updatePostVote(postId: String, voteType: String) {
        val currentPost = postDao.getPostById(postId).firstOrNull() ?: return
        var diffUp = 0
        var diffDown = 0

        // Handle vote logic changes
        val originalVote = currentPost.userVote
        val (newUserVote, newUpvotes, newDownvotes) = if (originalVote == voteType) {
            // Undo vote
            if (originalVote == "up") diffUp = -1
            if (originalVote == "down") diffDown = -1
            Triple("none", (currentPost.upvotes + diffUp).coerceAtLeast(0), (currentPost.downvotes + diffDown).coerceAtLeast(0))
        } else {
            // Undo old vote
            if (originalVote == "up") diffUp = -1
            if (originalVote == "down") diffDown = -1

            // Apply new vote
            if (voteType == "up") diffUp += 1
            if (voteType == "down") diffDown += 1

            Triple(voteType, (currentPost.upvotes + diffUp).coerceAtLeast(0), (currentPost.downvotes + diffDown).coerceAtLeast(0))
        }
        postDao.updatePostVotes(postId, newUpvotes, newDownvotes, newUserVote)
        val updatedPost = currentPost.copy(userVote = newUserVote, upvotes = newUpvotes, downvotes = newDownvotes)
        context?.let { SupabaseSynchronizer.syncUpPost(it, updatedPost) }
    }

    suspend fun insertComment(comment: Comment) {
        commentDao.insertComment(comment)
        postDao.refreshPostCommentCount(comment.postId)
        queueSync("COMMENT", comment) {
            context?.let { SupabaseSynchronizer.syncUpComment(it, comment) }
        }
    }

    fun getCommentsForEntity(entityId: String): Flow<List<Comment>> =
        commentDao.getCommentsForPost(entityId)

    suspend fun insertWorkspace(workspace: Workspace) {
        workspaceDao.insertWorkspace(workspace)
        queueSync("WORKSPACE", workspace) {
            context?.let { SupabaseSynchronizer.syncUpWorkspace(it, workspace) }
        }
    }

    suspend fun insertMember(member: WorkspaceMember) {
        workspaceMemberDao.insertMember(member)
        queueSync("WORKSPACE_MEMBER", member) {
            context?.let { SupabaseSynchronizer.syncUpWorkspaceMember(it, member) }
        }
    }

    suspend fun insertTask(task: ProductionTask) {
        productionTaskDao.insertTask(task)
        queueSync("TASK", task) {
            context?.let { SupabaseSynchronizer.syncUpProductionTask(it, task) }
        }
    }

    suspend fun updateTaskLane(taskId: String, newLane: String) {
        productionTaskDao.updateTaskLane(taskId, newLane)
    }

    suspend fun updateTaskStatus(taskId: String, status: String) {
        val task = productionTaskDao.getTaskByIdSuspend(taskId)
        if (task != null) {
            productionTaskDao.updateTaskLane(taskId, status)
            val updatedTask = task.copy(kanbanLane = status)
            queueSync("TASK", updatedTask)
            context?.let { SupabaseSynchronizer.syncUpProductionTask(it, updatedTask) }
            
            // Generate notification for task completion
            if (status == "COMPLETED") {
                val notificationId = "notif_" + System.currentTimeMillis()
                insertNotification(Notification(
                    id = notificationId,
                    userId = task.creatorId,
                    title = "Task Completed",
                    body = "Task '${task.title}' in workspace has been marked as completed.",
                    type = "TASK_COMPLETED",
                    deepLinkTarget = "creatorcoop://task/$taskId",
                    createdAt = System.currentTimeMillis()
                ))
                
                AnalyticsManager.trackTaskCompleted(taskId, System.currentTimeMillis() - task.createdAt)
                
                // Boost Reputation for Task Completion
                val user = userDao.getUserByIdSuspend(task.creatorId)
                if (user != null) {
                    val updatedTrustScore = user.reputationScore + 2
                    val updatedUser = user.copy(reputationScore = updatedTrustScore)
                    userDao.insertUser(updatedUser)
                    queueSync("USER_PROFILE", updatedUser)
                    context?.let { SupabaseSynchronizer.syncUpUser(it, updatedUser) }
                    AnalyticsManager.trackReputationGain(updatedTrustScore)
                }
                
                insertWorkspaceEvent(WorkspaceEvent(
                    id = "evt_" + System.currentTimeMillis(),
                    workspaceId = task.workspaceId,
                    actorId = task.creatorId,
                    eventType = "TASK_COMPLETED",
                    entityId = taskId,
                    title = "Task Completed",
                    description = "Task '${task.title}' was completed.",
                    startTime = System.currentTimeMillis(),
                    endTime = System.currentTimeMillis(),
                    createdAt = System.currentTimeMillis()
                ))
            }
        }
    }

    suspend fun deleteWorkspace(id: String) {
        workspaceDao.deleteWorkspaceById(id)
    }

    suspend fun archiveWorkspace(id: String) {
        workspaceDao.archiveWorkspace(id)
    }

    suspend fun removeMember(workspaceId: String, userId: String) {
        workspaceMemberDao.deleteMember(workspaceId, userId)
        // Clean up private draft tasks for the removed member
        val tasks = productionTaskDao.getPrivateDraftTasks(workspaceId, userId).firstOrNull() ?: emptyList()
        tasks.forEach { productionTaskDao.deleteTaskById(it.id) }
    }

    suspend fun updateMemberRole(workspaceId: String, userId: String, role: String) {
        workspaceMemberDao.updateMemberRole(workspaceId, userId, role)
    }

    suspend fun deleteTask(id: String) {
        productionTaskDao.deleteTaskById(id)
        queueSync("TASK", id, "DELETE")
        context?.let { SupabaseSynchronizer.syncDeleteProductionTask(it, id) }
    }

    suspend fun insertMessage(message: Message) {
        messageDao.insertMessage(message)
        queueSync("MESSAGE", message) {
            context?.let { SupabaseSynchronizer.syncUpMessage(it, message) }
        }
        
        AnalyticsManager.trackEvent("message_sent", mapOf(
            "workspace_id" to message.workspaceId,
            "sender_id" to message.senderId
        ))
    }

    fun getEndorsementsForUser(userId: String): Flow<List<Endorsement>> {
        return endorsementDao.getEndorsementsForUser(userId)
    }

    fun getCompletedWorkspacesCountForUser(userId: String): Flow<Int> {
        return kotlinx.coroutines.flow.combine(
            workspaceMemberDao.getWorkspacesForUser(userId),
            workspaceDao.getAllWorkspaces()
        ) { memberships, workspaces ->
            val archivedWorkspaceIds = workspaces.filter { it.isArchived }.map { it.id }.toSet()
            memberships.count { it.workspaceId in archivedWorkspaceIds }
        }
    }

    suspend fun insertEndorsement(endorsement: Endorsement) {
        endorsementDao.insertEndorsement(endorsement)
        queueSync("ENDORSEMENT", endorsement)
        val currentContext = context
        if (currentContext != null) {
            SupabaseSynchronizer.syncUpEndorsement(currentContext, endorsement)
        }
    }

    suspend fun deleteMyAccountCascade(userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. Delete Private Draft tasks
            productionTaskDao.deletePrivateDraftTasks(userId)
            
            // 2. Anonymize shared production-ready team task contributions
            productionTaskDao.anonymizeProductionTasks(userId)

            // 3. Keep public-facing content but replace author info with generic "Deleted User" placeholder
            postDao.anonymizePosts(userId)
            commentDao.anonymizeComments(userId)
            messageDao.anonymizeMessages(userId)

            // 4. Handle workspace membership leaving & "last Lead" protection force-resolving
            val memberWorkspaces = workspaceMemberDao.getWorkspacesForUserList(userId)
            memberWorkspaces.forEach { membership ->
                val workspaceId = membership.workspaceId
                val workspaceMembers = workspaceMemberDao.getMembersForWorkspaceList(workspaceId)
                
                // If user is Lead Creator or Head
                if (membership.assignedRoleTitle in listOf("Lead Creator", "Head")) {
                    val otherMembers = workspaceMembers.filter { it.userId != userId }
                    if (otherMembers.isNotEmpty()) {
                        // Promoting next member to Lead Creator
                        val nextLead = otherMembers.first()
                        workspaceMemberDao.updateMemberRole(workspaceId, nextLead.userId, "Lead Creator")
                    } else {
                        // Empty workspace, we can archive it or delete it
                        workspaceDao.archiveWorkspace(workspaceId)
                    }
                }
                // Finally, remove membership
                workspaceMemberDao.deleteMember(workspaceId, userId)
            }

            // 5. Delete details from user profiles
            userDao.deleteUserById(userId)

            // 6. Online Supabase Edge Function Trigger (if not me / local mode)
            if (userId != "me") {
                try {
                    val accessToken = SupabaseConfig.client.auth.currentSessionOrNull()?.accessToken
                    val client = okhttp3.OkHttpClient()
                    val requestBody = "{\"userId\":\"$userId\"}".toRequestBody("application/json".toMediaTypeOrNull())
                    val request = okhttp3.Request.Builder()
                        .url("${SupabaseConfig.supabaseUrl}/functions/v1/delete-user-account")
                        .addHeader("Authorization", "Bearer ${accessToken ?: SupabaseConfig.supabaseKey}")
                        .addHeader("apikey", SupabaseConfig.supabaseKey)
                        .post(requestBody)
                        .build()
                    val response = client.newCall(request).execute()
                    Log.d("AppRepository", "Triggered Supabase Edge Function: code=${response.code}")
                } catch (e: Exception) {
                    Log.e("AppRepository", "Failed to trigger edge function, fallback to local flow: ${e.message}")
                }
            }
            true
        } catch (e: Exception) {
            Log.e("AppRepository", "Error deleting account: ${e.message}")
            false
        }
    }

    // --- SAVED SEARCH OPERATIONS ---
    fun getSavedSearchesForUser(userId: String): Flow<List<SavedSearch>> = savedSearchDao.getSavedSearchesForUser(userId)
    
    suspend fun insertSavedSearch(savedSearch: SavedSearch) {
        savedSearchDao.insertSavedSearch(savedSearch)
        queueSync("SAVED_SEARCH", savedSearch)
    }

    suspend fun deleteSavedSearchById(id: String) {
        savedSearchDao.deleteSavedSearchById(id)
        queueSync("SAVED_SEARCH", id, "DELETE")
    }

    // --- LOOKING FOR WORK OPERATIONS ---
    fun getListingForUser(userId: String): Flow<LookingForWork?> = lookingForWorkDao.getListingForUser(userId)
    
    val allActiveWorkListings: Flow<List<LookingForWork>> = lookingForWorkDao.getAllActiveListings()

    suspend fun insertWorkListing(listing: LookingForWork) {
        lookingForWorkDao.insertListing(listing)
        queueSync("LOOKING_FOR_WORK", listing)
    }

    suspend fun deleteWorkListingForUser(userId: String) {
        lookingForWorkDao.deleteListingForUser(userId)
        queueSync("LOOKING_FOR_WORK", userId, "DELETE")
    }

    // --- REFERRAL SYSTEM OPERATIONS ---
    fun getReferralsByReferrer(referrerId: String): Flow<List<Referral>> =
        referralDao.getReferralsByReferrer(referrerId)

    fun getSuccessfulReferralCount(referrerId: String): Flow<Int> =
        referralDao.getSuccessfulReferralCount(referrerId)

    suspend fun getReferralForUser(userId: String) =
        referralDao.getReferralForUser(userId)

    suspend fun getUserByReferralCode(code: String) =
        referralDao.getUserByReferralCode(code.trim().uppercase())

    suspend fun applyReferralCode(referredUserId: String, enteredCode: String): Pair<Boolean, String> {
        val referredUser = userDao.getUserById(referredUserId).firstOrNull() ?: return Pair(false, "User not found")
        
        val referrer = referralDao.getUserByReferralCode(enteredCode.trim().uppercase())
            ?: return Pair(false, "Invalid referral code")

        if (referrer.id == referredUserId) {
            return Pair(false, "You cannot refer yourself")
        }

        // Anti-abuse checks
        val referrerDomain = referrer.email.substringAfter("@", "").trim().lowercase()
        val referredDomain = referredUser.email.substringAfter("@", "").trim().lowercase()
        
        if (referrer.deviceId.isNotEmpty() && referredUser.deviceId.isNotEmpty() && referrer.deviceId == referredUser.deviceId) {
            return Pair(false, "Referral blocked: Same device restriction")
        }
        
        if (referrerDomain.isNotEmpty() && referredDomain.isNotEmpty() && referrerDomain == referredDomain) {
            return Pair(false, "Referral blocked: Matching email domain restriction")
        }

        // Check if referral record already exists for this referred user
        val existing = referralDao.getReferralForUser(referredUserId)
        if (existing != null) {
            return Pair(false, "You have already applied a referral code")
        }

        val referral = Referral(
            id = UUID.randomUUID().toString(),
            referrerId = referrer.id,
            referredUserId = referredUserId,
            createdAt = System.currentTimeMillis(),
            rewardGranted = false
        )
        referralDao.insertReferral(referral)
        return Pair(true, "Referral code applied successfully! Onboarding reward will be granted upon completion.")
    }

    suspend fun checkAndGrantReferralRewards(referredUserId: String): Boolean {
        val referral = referralDao.getReferralForUser(referredUserId) ?: return false
        if (referral.rewardGranted) return false

        // Check if referred user completes onboarding: confirms email + completes profile
        val referredUser = userDao.getUserById(referredUserId).firstOrNull() ?: return false
        
        // Define onboarding completion conditions:
        // E.g., has a non-empty name, a specialty, and filled bio
        val isProfileCompleted = referredUser.displayName.isNotEmpty() && 
                                 referredUser.primarySpecialty.isNotEmpty() && 
                                 referredUser.bio.isNotEmpty()
        
        if (isProfileCompleted) {
            // Grant rewards (7 days of premium trial extension)
            // 1. Referred user
            val updatedReferred = referredUser.copy(
                premiumTrialExtensionDays = referredUser.premiumTrialExtensionDays + 7,
                isVerifiedPro = true // Mark verified pro trial
            )
            userDao.insertUser(updatedReferred)
            queueSync("USER", updatedReferred)
            context?.let { SupabaseSynchronizer.syncUpUser(it, updatedReferred) }

            // 2. Referrer
            val referrer = userDao.getUserById(referral.referrerId).firstOrNull()
            if (referrer != null) {
                val updatedReferrer = referrer.copy(
                    premiumTrialExtensionDays = referrer.premiumTrialExtensionDays + 7,
                    isVerifiedPro = true
                )
                userDao.insertUser(updatedReferrer)
                queueSync("USER", updatedReferrer)
                context?.let { SupabaseSynchronizer.syncUpUser(it, updatedReferrer) }
            }

            // Mark referral reward as granted
            val updatedReferral = referral.copy(rewardGranted = true)
            referralDao.insertReferral(updatedReferral)
            return true
        }
        return false
    }

    suspend fun getWeeklyDigestPreference(userId: String): Boolean {
        val value = userSettingsDao.getSetting(userId, "weekly_digest_notification")
        return value == null || value == "true" // defaults to on (true)
    }

    suspend fun setWeeklyDigestPreference(userId: String, isEnabled: Boolean) {
        userSettingsDao.setSetting(
            UserSetting(
                id = "${userId}_weekly_digest",
                userId = userId,
                key = "weekly_digest_notification",
                value = isEnabled.toString()
            )
        )
    }

    suspend fun getOnboardingChecklistDismissed(userId: String): Boolean {
        val value = userSettingsDao.getSetting(userId, "onboarding_checklist_dismissed")
        return value == "true"
    }

    suspend fun setOnboardingChecklistDismissed(userId: String, dismissed: Boolean) {
        userSettingsDao.setSetting(
            UserSetting(
                id = "${userId}_onboarding_dismissed",
                userId = userId,
                key = "onboarding_checklist_dismissed",
                value = dismissed.toString()
            )
        )
    }

    // TOUR PERSISTENCE FUNCTIONS
    fun getTourSettingsFlow(userId: String): Flow<List<UserSetting>> =
        userSettingsDao.getTourSettingsFlow(userId)

    suspend fun getTourStatus(userId: String, tourId: String): String? {
        return userSettingsDao.getSetting(userId, "tour_$tourId")
    }

    suspend fun setTourStatus(userId: String, tourId: String, status: String) {
        userSettingsDao.setSetting(
            UserSetting(
                id = "${userId}_tour_${tourId}",
                userId = userId,
                key = "tour_$tourId",
                value = status
            )
        )
    }

    suspend fun clearAllTours(userId: String) {
        userSettingsDao.clearTourSettings(userId)
    }

    // SPAM AND RATELIMIT UTILITIES
    suspend fun getGlobalConfig(key: String, defaultValue: String): String {
        val value = adDao.getSettingByKey(key)
        if (value == null) {
            adDao.updateSetting(GlobalSetting(key, defaultValue))
            return defaultValue
        }
        return value
    }

    suspend fun checkConnectionRequestRateLimit(senderId: String) {
        val limitStr = getGlobalConfig("spam_threshold_connection_requests", "10")
        val limit = limitStr.toIntOrNull() ?: 10
        val sinceTime = System.currentTimeMillis() - 60 * 60 * 1000L // 1 hour
        val count = connectionRequestDao.getRequestCountSince(senderId, sinceTime)
        if (count > limit) {
            val autoReport = Report(
                id = UUID.randomUUID().toString(),
                reporterId = "SYSTEM_AUTOMATION",
                targetType = "USER",
                targetId = senderId,
                reason = "Auto-flagged: More than $limit Connection Requests sent within 1 hour ($count sent)",
                status = "PENDING",
                createdAt = System.currentTimeMillis()
            )
            reportDao.insertReport(autoReport)
            queueSync("REPORT", autoReport)
            context?.let { SupabaseSynchronizer.syncUpReport(it, autoReport) }
        }
    }

    suspend fun checkPostRateLimit(authorId: String) {
        val limitStr = getGlobalConfig("spam_threshold_posts", "5")
        val limit = limitStr.toIntOrNull() ?: 5
        val sinceTime = System.currentTimeMillis() - 10 * 60 * 1000L // 10 minutes
        val count = postDao.getPostCountSince(authorId, sinceTime)
        if (count > limit) {
            val autoReport = Report(
                id = UUID.randomUUID().toString(),
                reporterId = "SYSTEM_AUTOMATION",
                targetType = "USER",
                targetId = authorId,
                reason = "Auto-flagged: More than $limit posts created within 10 minutes ($count created)",
                status = "PENDING",
                createdAt = System.currentTimeMillis()
            )
            reportDao.insertReport(autoReport)
            queueSync("REPORT", autoReport)
            context?.let { SupabaseSynchronizer.syncUpReport(it, autoReport) }
        }
    }

    suspend fun checkReportRateLimitAgainstUser(targetUserId: String) {
        val limitStr = getGlobalConfig("spam_threshold_reports", "3")
        val limit = limitStr.toIntOrNull() ?: 3
        val sinceTime = System.currentTimeMillis() - 24 * 60 * 60 * 1000L // 24 hours
        val reports = reportDao.getReportsSince(sinceTime)
        var count = 0
        for (report in reports) {
            val authorId = when (report.targetType.uppercase()) {
                "USER" -> report.targetId
                "POST" -> getPostByIdSync(report.targetId)?.authorId
                "COMMENT" -> getCommentById(report.targetId)?.authorId
                "MESSAGE" -> getMessageById(report.targetId)?.senderId
                else -> null
            }
            if (authorId == targetUserId) {
                count++
            }
        }
        if (count > limit) {
            val autoReport = Report(
                id = UUID.randomUUID().toString(),
                reporterId = "SYSTEM_AUTOMATION",
                targetType = "USER",
                targetId = targetUserId,
                reason = "Auto-flagged: More than $limit reports filed against this user within 24 hours ($count reports)",
                status = "PENDING",
                createdAt = System.currentTimeMillis()
            )
            reportDao.insertReport(autoReport)
            queueSync("REPORT", autoReport)
            context?.let { SupabaseSynchronizer.syncUpReport(it, autoReport) }
        }
    }

    // FEATURE FLAG UTILITIES
    fun getAllFeatureFlagsFlow(): Flow<List<FeatureFlag>> = featureFlagDao.getAllFeatureFlagsFlow()

    suspend fun getFeatureFlag(key: String): FeatureFlag? = featureFlagDao.getFeatureFlag(key)

    suspend fun insertFeatureFlag(flag: FeatureFlag) {
        featureFlagDao.insertFeatureFlag(flag)
    }

    // --- CONNECTION REQUESTS ---
    val allConnectionRequestsFlow: Flow<List<ConnectionRequest>> = connectionRequestDao.getAllConnectionRequestsFlow()
    fun getPendingConnectionRequests(userId: String) = connectionRequestDao.getPendingRequestsForUser(userId)
    fun getResolvedConnectionRequests(userId: String) = connectionRequestDao.getResolvedRequestsForUser(userId)
    
    suspend fun insertConnectionRequest(request: ConnectionRequest) {
        connectionRequestDao.insertRequest(request)
        queueSync("CONNECTION_REQUEST", request, "UPSERT")
        context?.let { SupabaseSynchronizer.syncUpConnectionRequest(it, request) }
        // Also fire off a notification to the receiver
        val notif = Notification(
            id = UUID.randomUUID().toString(),
            userId = request.receiverId,
            title = "New Connection Request",
            body = "${request.senderName} wants to connect with you.",
            type = "INVITES",
            isRead = false,
            createdAt = System.currentTimeMillis()
        )
        notificationDao.insertNotification(notif)
        checkConnectionRequestRateLimit(request.senderId)
    }

    suspend fun resolveConnectionRequest(requestId: String, accept: Boolean) {
        connectionRequestDao.updateRequestStatus(requestId, if (accept) "ACCEPTED" else "DECLINED")
        val req = connectionRequestDao.getRequestById(requestId)
        if (req != null) {
            queueSync("CONNECTION_REQUEST", req, "UPSERT")
            context?.let { SupabaseSynchronizer.syncUpConnectionRequest(it, req) }
        }
    }

    // --- NOTIFICATIONS ---
    fun getAllNotificationsFlow() = notificationDao.getAllNotificationsFlow()
    fun getNotificationsForUser(userId: String) = notificationDao.getNotificationsForUser(userId)

    suspend fun markNotificationAsRead(id: String) = notificationDao.markAsRead(id)
    suspend fun updateNotificationReadState(id: String, isRead: Boolean) = notificationDao.updateReadState(id, if (isRead) 1 else 0)
    suspend fun markAllNotificationsAsRead(userId: String) = notificationDao.markAllAsRead(userId)
    suspend fun pinNotification(id: String, isPinned: Boolean) = notificationDao.updatePinnedState(id, if (isPinned) 1 else 0)
    suspend fun archiveNotification(id: String, isArchived: Boolean) = notificationDao.updateArchivedState(id, if (isArchived) 1 else 0)
    suspend fun deleteNotification(id: String) = notificationDao.deleteNotification(id)
    fun getProjectProposalById(id: String): Flow<ProjectProposal?> = projectProposalDao.getProjectProposalById(id)
    
    suspend fun getAgreementById(id: String): TeamAgreement? = agreementDao.getAgreementByIdSuspend(id)
    
    suspend fun insertNotification(notification: Notification) = notificationDao.insertNotification(notification)

    suspend fun updateFeatureFlag(flag: FeatureFlag, adminId: String, reason: String) {
        featureFlagDao.insertFeatureFlag(flag)
        val log = AuditLog(
            id = UUID.randomUUID().toString(),
            adminId = adminId,
            actionTaken = "TOGGLE_FEATURE_FLAG",
            targetType = "FEATURE_FLAG",
            targetId = flag.flagKey,
            reason = "Flag '${flag.flagKey}' set to ${flag.isEnabled}. Reason: $reason",
            createdAt = System.currentTimeMillis()
        )
        insertAuditLog(log)
    }

    // --- USER MANAGEMENT & CREATOR VERIFICATION ---
    fun getAllUsersFlow(): kotlinx.coroutines.flow.Flow<List<UserProfile>> = userDao.getAllUsers()
    fun getUserByIdFlow(userId: String): kotlinx.coroutines.flow.Flow<UserProfile?> = userDao.getUserById(userId)

    suspend fun getVerificationRequestById(id: String): VerificationRequest? = 
        verificationRequestDao.getVerificationRequestById(id)

    fun getAllVerificationRequestsFlow(): kotlinx.coroutines.flow.Flow<List<VerificationRequest>> = 
        verificationRequestDao.getAllVerificationRequests()

    suspend fun submitVerificationRequest(request: VerificationRequest) {
        verificationRequestDao.insertVerificationRequest(request)
        queueSync("VERIFICATION_REQUEST", request, "UPSERT")
        context?.let { SupabaseSynchronizer.syncUpVerificationRequest(it, request) }
    }

    suspend fun updateVerificationRequest(
        id: String, 
        status: String, 
        notes: String, 
        reviewedBy: String
    ) {
        verificationRequestDao.updateVerificationRequestStatus(id, status, notes, reviewedBy, System.currentTimeMillis())
        
        val updatedReq = verificationRequestDao.getVerificationRequestById(id)
        if (updatedReq != null) {
            queueSync("VERIFICATION_REQUEST", updatedReq, "UPSERT")
            context?.let { SupabaseSynchronizer.syncUpVerificationRequest(it, updatedReq) }
        }
        
        // Also update the actual UserProfile
        val request = verificationRequestDao.getVerificationRequestById(id)
        if (request != null && status == "APPROVED") {
            userDao.getUserByIdSuspend(request.userId)?.let { user ->
                val updatedUser = user.copy(
                    isVerifiedPro = true,
                    verificationLevel = "L2 Pro Verified"
                )
                userDao.insertUser(updatedUser)
                queueSync("USER_PROFILE", updatedUser)
                context?.let { SupabaseSynchronizer.syncUpUser(it, updatedUser) }
                
                AnalyticsManager.trackEvent("user_verified", mapOf("user_id" to user.id, "reviewer" to reviewedBy))
                
                insertNotification(Notification(
                    id = "notif_" + System.currentTimeMillis(),
                    userId = request.userId,
                    title = "Verification Approved \uD83D\uDCA5",
                    body = "You are now officially a verified creator! Your profile has been upgraded.",
                    type = "VERIFICATION_APPROVED",
                    deepLinkTarget = "creatorcoop://profile/${user.id}",
                    createdAt = System.currentTimeMillis()
                ))
            }
        } else if (request != null && status == "REJECTED") {
            insertNotification(Notification(
                id = "notif_" + System.currentTimeMillis(),
                userId = request.userId,
                title = "Verification Update",
                body = "Your verification request was reviewed. Notes: $notes",
                type = "VERIFICATION_REJECTED",
                createdAt = System.currentTimeMillis()
            ))
        }
    }

    fun getAllUserAuditLogsFlow(): kotlinx.coroutines.flow.Flow<List<UserAuditLog>> = 
        userAuditLogDao.getAllUserAuditLogs()

    suspend fun insertUserAuditLog(log: UserAuditLog) {
        userAuditLogDao.insertUserAuditLog(log)
    }

    fun getAllSupportTicketsFlow(): Flow<List<SupportTicket>> =
        supportTicketDao.getAllSupportTickets()

    fun getSupportTicketsByUserIdFlow(userId: String): Flow<List<SupportTicket>> =
        supportTicketDao.getSupportTicketsByUserId(userId)

    suspend fun insertSupportTicket(ticket: SupportTicket) {
        supportTicketDao.insertSupportTicket(ticket)
        queueSync("SUPPORT_TICKET", ticket, "UPSERT")
        context?.let { SupabaseSynchronizer.syncUpSupportTicket(it, ticket) }
    }

    suspend fun getSupportTicketById(id: String): SupportTicket? =
        supportTicketDao.getSupportTicketById(id)

    suspend fun deleteSupportTicket(id: String) {
        supportTicketDao.deleteSupportTicket(id)
        queueSync("SUPPORT_TICKET", id, "DELETE")
        context?.let { SupabaseSynchronizer.syncDeleteSupportTicket(it, id) }
    }

    fun getAllCrmRecordsFlow(): Flow<List<CrmRecord>> =
        crmRecordDao.getAllCrmRecords()

    suspend fun insertCrmRecord(record: CrmRecord) {
        crmRecordDao.insertCrmRecord(record)
    }

    suspend fun getCrmRecordById(id: String): CrmRecord? =
        crmRecordDao.getCrmRecordById(id)

    suspend fun deleteCrmRecord(id: String) {
        crmRecordDao.deleteCrmRecord(id)
    }

    fun getAllAnnouncementsFlow(): Flow<List<Announcement>> =
        announcementDao.getAllAnnouncements()

    suspend fun insertAnnouncement(announcement: Announcement) {
        announcementDao.insertAnnouncement(announcement)
    }

    suspend fun getAnnouncementById(id: String): Announcement? =
        announcementDao.getAnnouncementById(id)

    suspend fun deleteAnnouncement(id: String) {
        announcementDao.deleteAnnouncement(id)
    }

    fun getInteractionsForAnnouncementFlow(announcementId: String): Flow<List<AnnouncementInteraction>> =
        announcementDao.getInteractionsForAnnouncement(announcementId)

    suspend fun getAnnouncementInteraction(announcementId: String, userId: String): AnnouncementInteraction? =
        announcementDao.getInteraction(announcementId, userId)

    suspend fun insertAnnouncementInteraction(interaction: AnnouncementInteraction) {
        announcementDao.insertInteraction(interaction)
    }

    fun getUserAnnouncementInteractionsFlow(userId: String): Flow<List<AnnouncementInteraction>> =
        announcementDao.getUserInteractions(userId)

    // Platform Settings
    fun getPlatformSettingsFlow(): Flow<PlatformSettings?> =
        platformControlDao.getPlatformSettingsFlow()

    suspend fun getPlatformSettings(): PlatformSettings? =
        platformControlDao.getPlatformSettings()

    suspend fun insertPlatformSettings(settings: PlatformSettings) {
        platformControlDao.insertPlatformSettings(settings)
    }

    // Onboarding Slides
    fun getAllOnboardingSlidesFlow(): Flow<List<OnboardingSlide>> =
        platformControlDao.getAllOnboardingSlidesFlow()

    suspend fun insertOnboardingSlide(slide: OnboardingSlide) {
        platformControlDao.insertOnboardingSlide(slide)
    }

    suspend fun deleteOnboardingSlide(id: String) {
        platformControlDao.deleteOnboardingSlide(id)
    }

    // Welcome Messages
    fun getAllWelcomeMessagesFlow(): Flow<List<WelcomeMessage>> =
        platformControlDao.getAllWelcomeMessagesFlow()

    suspend fun insertWelcomeMessage(msg: WelcomeMessage) {
        platformControlDao.insertWelcomeMessage(msg)
    }

    suspend fun deleteWelcomeMessage(id: String) {
        platformControlDao.deleteWelcomeMessage(id)
    }

    // Empty States
    fun getAllEmptyStatesFlow(): Flow<List<EmptyStateConfig>> =
        platformControlDao.getAllEmptyStatesFlow()

    suspend fun insertEmptyState(config: EmptyStateConfig) {
        platformControlDao.insertEmptyState(config)
    }

    suspend fun deleteEmptyState(id: String) {
        platformControlDao.deleteEmptyState(id)
    }

    // Help Texts
    fun getAllHelpTextsFlow(): Flow<List<HelpText>> =
        platformControlDao.getAllHelpTextsFlow()

    suspend fun insertHelpText(helpText: HelpText) {
        platformControlDao.insertHelpText(helpText)
    }

    suspend fun deleteHelpText(id: String) {
        platformControlDao.deleteHelpText(id)
    }

    // Founder Notes Methods
    fun getNotesForEntity(entityType: String, entityId: String): Flow<List<FounderNote>> =
        founderNoteDao.getNotesForEntity(entityType, entityId)

    suspend fun insertFounderNote(note: FounderNote) {
        founderNoteDao.insertNote(note)
        queueSync("FOUNDER_NOTE", note, "UPSERT")
        context?.let { SupabaseSynchronizer.syncUpFounderNote(it, note) }
    }

    suspend fun deleteFounderNote(id: String) {
        founderNoteDao.deleteNote(id)
        queueSync("FOUNDER_NOTE", id, "DELETE")
        context?.let { SupabaseSynchronizer.syncDeleteFounderNote(it, id) }
    }

    // Workspace Assets
    fun getAssetsForWorkspace(workspaceId: String): Flow<List<WorkspaceAsset>> =
        workspaceAssetDao.getAssetsForWorkspace(workspaceId)
    
    suspend fun insertAsset(asset: WorkspaceAsset) {
        workspaceAssetDao.insertAsset(asset)
        queueSync("WORKSPACE_ASSET", asset, "UPSERT")
        context?.let { SupabaseSynchronizer.syncUpWorkspaceAsset(it, asset) }
    }
    
    suspend fun updateAssetStatus(assetId: String, status: String) {
        workspaceAssetDao.updateAssetStatus(assetId, status)
        val asset = workspaceAssetDao.getAssetById(assetId)
        if (asset != null) {
            queueSync("WORKSPACE_ASSET", asset, "UPSERT")
            context?.let { SupabaseSynchronizer.syncUpWorkspaceAsset(it, asset) }
        }
    }
    
    suspend fun deleteAsset(assetId: String) {
        workspaceAssetDao.deleteAsset(assetId)
        queueSync("WORKSPACE_ASSET", assetId, "DELETE")
        context?.let { SupabaseSynchronizer.syncDeleteWorkspaceAsset(it, assetId) }
    }

    // Deliverables
    fun getDeliverablesForWorkspace(workspaceId: String): Flow<List<Deliverable>> =
        deliverableDao.getDeliverablesForWorkspace(workspaceId)
        
    fun getDeliverablesForTask(taskId: String): Flow<List<Deliverable>> =
        deliverableDao.getDeliverablesForTask(taskId)
        
    suspend fun insertDeliverable(deliverable: Deliverable) {
        deliverableDao.insertDeliverable(deliverable)
        queueSync("DELIVERABLE", deliverable, "UPSERT")
        context?.let { SupabaseSynchronizer.syncUpDeliverable(it, deliverable) }
    }
    
    suspend fun updateDeliverableStatus(deliverableId: String, status: String, feedback: String? = null) {
        deliverableDao.updateDeliverableStatus(deliverableId, status, feedback)
        val deliverable = deliverableDao.getDeliverableById(deliverableId)
        if (deliverable != null) {
            queueSync("DELIVERABLE", deliverable, "UPSERT")
            context?.let { SupabaseSynchronizer.syncUpDeliverable(it, deliverable) }
        }
    }
        
    suspend fun deleteDeliverable(deliverableId: String) {
        deliverableDao.deleteDeliverable(deliverableId)
        queueSync("DELIVERABLE", deliverableId, "DELETE")
        context?.let { SupabaseSynchronizer.syncDeleteDeliverable(it, deliverableId) }
    }

    // Workspace Events
    fun getEventsForWorkspace(workspaceId: String): Flow<List<WorkspaceEvent>> =
        workspaceEventDao.getEventsForWorkspace(workspaceId)
        
    suspend fun insertWorkspaceEvent(event: WorkspaceEvent) {
        workspaceEventDao.insertEvent(event)
        queueSync("WORKSPACE_EVENT", event, "UPSERT")
        context?.let { SupabaseSynchronizer.syncUpWorkspaceEvent(it, event) }
    }

    fun getAllAssetsFlow(): Flow<List<WorkspaceAsset>> = workspaceAssetDao.getAllAssetsFlow()
    fun getAllDeliverablesFlow(): Flow<List<Deliverable>> = deliverableDao.getAllDeliverablesFlow()
    fun getAllAgreementsFlow(): Flow<List<TeamAgreement>> = agreementDao.getAllAgreementsFlow()
    fun getAllCommentsFlow(): Flow<List<Comment>> = commentDao.getAllCommentsFlow()
    fun getAllWorkspaceMembersFlow(): Flow<List<WorkspaceMember>> = workspaceMemberDao.getAllMembersFlow()

    // Personal Notes
    fun getPersonalNotesForUser(userId: String): Flow<List<PersonalNote>> = personalNoteDao.getNotesForUser(userId)
    suspend fun insertPersonalNote(note: PersonalNote) = personalNoteDao.insertNote(note)
    suspend fun deletePersonalNote(id: String) = personalNoteDao.deleteNote(id)

    // Community Likes
    suspend fun getLike(userId: String, postId: String, type: String): CommunityLikeEntity? = communityLikeDao.getLike(userId, postId, type)
    suspend fun insertLike(like: CommunityLikeEntity) = communityLikeDao.insertLike(like)
    suspend fun deleteLike(userId: String, postId: String, type: String) = communityLikeDao.deleteLike(userId, postId, type)
    fun getLikeCountFlow(postId: String, type: String): Flow<Int> = communityLikeDao.getInteractionCountFlow(postId, type)

    // Workspace Invites
    fun getInvitesForWorkspace(workspaceId: String): Flow<List<WorkspaceInviteEntity>> = workspaceInviteDao.getInvitesForWorkspace(workspaceId)
    suspend fun insertWorkspaceInvite(invite: WorkspaceInviteEntity) = workspaceInviteDao.insertInvite(invite)
    suspend fun getInviteByCode(code: String): WorkspaceInviteEntity? = workspaceInviteDao.getInviteByCode(code)

    // Search Filters
    fun getSearchFiltersForUser(userId: String): Flow<List<SearchFilterEntity>> = searchFilterDao.getFiltersForUser(userId)
    suspend fun insertSearchFilter(filter: SearchFilterEntity) = searchFilterDao.insertFilter(filter)
    suspend fun deleteSearchFilter(id: String) = searchFilterDao.deleteFilter(id)

    // Role Configurations
    fun getRoleConfigsForWorkspace(workspaceId: String): Flow<List<RoleConfigurationEntity>> = roleConfigurationDao.getRoleConfigsForWorkspace(workspaceId)
    suspend fun insertRoleConfig(config: RoleConfigurationEntity) = roleConfigurationDao.insertRoleConfig(config)
    suspend fun getRoleConfigById(id: String): RoleConfigurationEntity? = roleConfigurationDao.getRoleConfigById(id)

    suspend fun clearUserDataOnLogout() = withContext(Dispatchers.IO) {
        try {
            db.clearAllTables()
        } catch (e: Exception) {
            Log.e("AppRepository", "Failed to clear database on logout: ${e.message}", e)
        }
    }
}

