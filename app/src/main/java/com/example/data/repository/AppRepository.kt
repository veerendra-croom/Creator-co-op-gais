package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import com.example.data.supabase.*
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

class AppRepository(private val db: AppDatabase, private val context: android.content.Context? = null) {
    private val json = Json { 
        ignoreUnknownKeys = true
        prettyPrint = true
        coerceInputValues = true
    }
    val userDao = db.userDao()

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
 
    init {
        // Pre-populate default feature flags
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val defaults = listOf(
                    FeatureFlag("huddles_enabled", true, "Enable real-time voice and video workspace huddles for active team creators."),
                    FeatureFlag("referral_system_enabled", true, "Enable user referrals, code sharing, and Premium trial extension rewards."),
                    FeatureFlag("weekly_digest_enabled", true, "Enable weekly summary digest notifications for matchmaking and active spaces."),
                    FeatureFlag("looking_for_work_board_enabled", true, "Enable the Looking for Work board for active freelancers and matching projects."),
                    FeatureFlag("creator_commons_enabled", true, "Control platform-wide access to the shared Creator Commons social arena."),
                    FeatureFlag("analytics_enabled", true, "Enable or disable aggregated team and syndicate metrics."),
                    FeatureFlag("portfolio_discovery_enabled", true, "Control search index visibility for creator profile discovery."),
                    FeatureFlag("verification_requests_enabled", true, "Enable or disable automated peer-to-peer verification pipelines."),
                    FeatureFlag("founder_crm_enabled", true, "Control access to automated founder crm, cohort tracking and health score logs."),
                    FeatureFlag("moderation_center_enabled", true, "Enable platform-wide profile audits, flag reviews, and moderation dashboards.")
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
                        platformControlDao.insertOnboardingSlide(OnboardingSlide("slide_01", "Welcome to Creator Co-Op", "The premier decentralized platform for joint media productions and creator milestone syndicates.", 0, "Hub"))
                        platformControlDao.insertOnboardingSlide(OnboardingSlide("slide_02", "Automated Milestone Splits", "Draft standard peer-to-peer agreement templates and let secure ledgers handle auto-payout allocations.", 1, "Handshake"))
                        platformControlDao.insertOnboardingSlide(OnboardingSlide("slide_03", "Real-Time Collaboration", "Connect via live video huddles, review work-in-progress materials, and sync pipelines instantly.", 2, "Groups"))
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
                        platformControlDao.insertEmptyState(EmptyStateConfig("empty_commons", "Commons", "empty_commons", "No Feed Discussions Yet", "Be the first to share an update, start a video huddle, or publish a milestone achievement."))
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

    suspend fun insertDisputeNote(note: DisputeNote) = 
        disputeNoteDao.insertDisputeNote(note)

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

    suspend fun updateAdPlacement(placement: AdPlacement) {
        adDao.updatePlacement(placement)
    }

    suspend fun updateUserProfile(profile: UserProfile) {
        userDao.insertUser(profile)
        val currentContext = context
        if (currentContext != null && SupabaseConfig.isNetworkAvailable(currentContext)) {
            try {
                SupabaseConfig.client.from("user_profiles").upsert(profile)
            } catch (e: Exception) {
                Log.e("Supabase", "Failed to sync profile update", e)
                syncDao.insertSyncEvent(SyncEntity(
                    id = UUID.randomUUID().toString(),
                    entityType = "USER_PROFILE",
                    entityJson = Json.encodeToString(UserProfile.serializer(), profile),
                    actionType = "UPSERT",
                    createdAt = System.currentTimeMillis()
                ))
            }
        }
    }

    suspend fun updateGlobalAdSetting(setting: GlobalSetting) {
        adDao.updateSetting(setting)
    }

    private suspend fun queueSync(entityType: String, entity: Any, action: String = "UPSERT") {
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
            else -> UUID.randomUUID().toString()
        }

        syncDao.insertSyncEvent(
            SyncEntity(
                id = "${entityType}_${id}_${action}_${System.currentTimeMillis()}",
                entityType = entityType,
                entityJson = jsonStr,
                actionType = action,
                createdAt = System.currentTimeMillis(),
                syncStatus = "PENDING",
                retryCount = 0,
                lastAttemptedAt = 0
            )
        )
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

    suspend fun updateReportStatus(reportId: String, status: String) {
        reportDao.updateReportStatus(reportId, status)
        val report = reportDao.getReportById(reportId)
        if (report != null) {
            context?.let { SupabaseSynchronizer.syncUpReport(it, report) }
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

    suspend fun deletePost(id: String) {
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

    suspend fun deleteComment(id: String) {
        commentDao.deleteComment(id)
        context?.let { SupabaseSynchronizer.syncDeleteComment(it, id) }
    }

    suspend fun deleteMessage(id: String) {
        messageDao.deleteMessage(id)
    }

    fun getPitchesForProject(projectId: String): Flow<List<TalentPitch>> = talentPitchDao.getPitchesForProject(projectId)
    fun getPitchesBySender(senderId: String): Flow<List<TalentPitch>> = talentPitchDao.getPitchesBySender(senderId)

    suspend fun insertProjectProposal(proposal: ProjectProposal) {
        projectProposalDao.insertProjectProposal(proposal)
    }

    suspend fun deleteProjectProposalById(id: String) {
        projectProposalDao.deleteProjectProposalById(id)
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

    suspend fun setUserRole(userId: String, role: String) {
        adminDao.setUserRole(userId, role)
        ClientSecurityInterceptor.wrap {
            SupabaseConfig.client.postgrest.from("users").update(mapOf("global_role" to role)) {
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
        ClientSecurityInterceptor.wrap {
            val response = SupabaseConfig.client.postgrest.from("users").select().data
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
    fun getProductionTasks(workspaceId: String) = productionTaskDao.getProductionReadyTasks(workspaceId)
    fun getAllTasksForUser(userId: String) = productionTaskDao.getAllTasksForUser(userId)
    fun getAllProductionTasksFlow() = productionTaskDao.getAllProductionTasks()
    fun getRoughSandboxTasks(workspaceId: String, userId: String) = productionTaskDao.getRoughSandboxTasks(workspaceId, userId)
    fun getTaskById(id: String) = productionTaskDao.getTaskById(id)
    fun getMessagesForWorkspace(workspaceId: String) = messageDao.getMessagesForWorkspace(workspaceId)
    fun getDMsForWorkspace(workspaceId: String, myId: String) = messageDao.getDMsForWorkspace(workspaceId, myId)
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
        queueSync("AGREEMENT", agreement)
        context?.let { SupabaseSynchronizer.syncUpAgreement(it, agreement) }
    }

    suspend fun insertAcknowledgment(acknowledgment: AgreementAcknowledgment) {
        agreementDao.insertAcknowledgment(acknowledgment)
        queueSync("AGREEMENT_ACK", acknowledgment)
        context?.let { SupabaseSynchronizer.syncUpAcknowledgment(it, acknowledgment) }
    }

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
                        val message = json.decodeFromString(Message.serializer(), event.entityJson)
                        SupabaseSynchronizer.syncUpMessage(context, message)
                        true
                    }
                    "TASK" -> {
                        val task = json.decodeFromString(ProductionTask.serializer(), event.entityJson)
                        SupabaseSynchronizer.syncUpProductionTask(context, task)
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
                        SupabaseConfig.client.postgrest.from("endorsements").upsert(endorsement)
                        true
                    }
                    else -> false
                }
                if (success) syncDao.deleteSyncEvent(event.id)
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
        queueSync("USER", user)
        context?.let { SupabaseSynchronizer.syncUpUser(it, user) }
    }

    suspend fun insertPost(post: Post) {
        postDao.insertPost(post)
        queueSync("POST", post)
        context?.let { SupabaseSynchronizer.syncUpPost(it, post) }
        checkPostRateLimit(post.authorId)
    }

    suspend fun updatePostVote(postId: String, voteType: String) {
        val currentPost = postDao.getPostById(postId).firstOrNull() ?: return
        var diffUp = 0
        var diffDown = 0

        // Handle vote logic changes
        val originalVote = currentPost.userVote
        val updatedPost = if (originalVote == voteType) {
            // Undo vote
            if (originalVote == "up") diffUp = -1
            if (originalVote == "down") diffDown = -1
            currentPost.copy(
                userVote = "none",
                upvotes = (currentPost.upvotes + diffUp).coerceAtLeast(0),
                downvotes = (currentPost.downvotes + diffDown).coerceAtLeast(0)
            )
        } else {
            // Undo old vote
            if (originalVote == "up") diffUp = -1
            if (originalVote == "down") diffDown = -1

            // Apply new vote
            if (voteType == "up") diffUp += 1
            if (voteType == "down") diffDown += 1

            currentPost.copy(
                userVote = voteType,
                upvotes = (currentPost.upvotes + diffUp).coerceAtLeast(0),
                downvotes = (currentPost.downvotes + diffDown).coerceAtLeast(0)
            )
        }
        postDao.insertPost(updatedPost)
        context?.let { SupabaseSynchronizer.syncUpPost(it, updatedPost) }
    }

    suspend fun insertComment(comment: Comment) {
        commentDao.insertComment(comment)
        queueSync("COMMENT", comment)
        context?.let { SupabaseSynchronizer.syncUpComment(it, comment) }
    }

    suspend fun insertWorkspace(workspace: Workspace) {
        workspaceDao.insertWorkspace(workspace)
        queueSync("WORKSPACE", workspace)
        context?.let { SupabaseSynchronizer.syncUpWorkspace(it, workspace) }
    }

    suspend fun insertMember(member: WorkspaceMember) {
        workspaceMemberDao.insertMember(member)
        queueSync("WORKSPACE_MEMBER", member)
        context?.let { SupabaseSynchronizer.syncUpWorkspaceMember(it, member) }
    }

    suspend fun insertTask(task: ProductionTask) {
        productionTaskDao.insertTask(task)
        queueSync("TASK", task)
        context?.let { SupabaseSynchronizer.syncUpProductionTask(it, task) }
    }

    suspend fun deleteWorkspace(id: String) {
        workspaceDao.deleteWorkspaceById(id)
    }

    suspend fun archiveWorkspace(id: String) {
        workspaceDao.archiveWorkspace(id)
    }

    suspend fun removeMember(workspaceId: String, userId: String) {
        workspaceMemberDao.deleteMember(workspaceId, userId)
        // Clean up private sandbox tasks for the removed member
        val tasks = productionTaskDao.getRoughSandboxTasks(workspaceId, userId).firstOrNull() ?: emptyList()
        tasks.forEach { productionTaskDao.deleteTaskById(it.id) }
    }

    suspend fun updateMemberRole(workspaceId: String, userId: String, role: String) {
        workspaceMemberDao.updateMemberRole(workspaceId, userId, role)
    }

    suspend fun deleteTask(id: String) {
        productionTaskDao.deleteTaskById(id)
    }

    suspend fun insertMessage(message: Message) {
        messageDao.insertMessage(message)
        queueSync("MESSAGE", message)
        context?.let { SupabaseSynchronizer.syncUpMessage(it, message) }
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
        if (currentContext != null && SupabaseConfig.isNetworkAvailable(currentContext)) {
            try {
                SupabaseConfig.client.postgrest.from("endorsements").upsert(endorsement)
            } catch (e: Exception) {
                Log.e("Supabase", "Failed to sync endorsement update", e)
            }
        }
    }

    suspend fun prepopulateIfEmpty() {
        val isTest = System.getProperty("robolectric.active") != null
        if (isTest && userDao.getAllUsers().firstOrNull()?.isEmpty() == true) {
            val myId = "admin_seed"
            val me = UserProfile(
                id = myId,
                email = "admin@creatorcoop.com",
                displayName = "Platform Admin",
                avatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=Admin",
                globalRole = "ADMIN",
                systemRole = "PLATFORM_ADMIN",
                primarySpecialty = "Platform Governance",
                isVerifiedPro = true,
                deviceId = "device_admin"
            )
            userDao.insertUser(me)

            val alex = UserProfile(
                id = "DemoUser",
                email = "alex.mercer@gmail.com",
                displayName = "Alex Mercer",
                avatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=Alex",
                globalRole = "APP_USER",
                primarySpecialty = "Visual Storytelling",
                isVerifiedPro = true,
                bio = "Lead Visual Storyteller & Script Blueprint Architect | 10M+ Combined Views",
                referralCode = "ALEX-7777",
                reputationScore = 98,
                reliabilityBadge = "Platinum",
                verificationLevel = "L3 Expert Verified",
                deviceId = "device_alex"
            )
            userDao.insertUser(alex)

            val workspaceId = "ws_youtube_main"
            val mainWorkspace = Workspace(
                id = workspaceId,
                name = "TechPulse Main Channel",
                platformType = "YOUTUBE",
                createdBy = myId,
                createdAt = System.currentTimeMillis()
            )
            workspaceDao.insertWorkspace(mainWorkspace)

            workspaceMemberDao.insertMember(WorkspaceMember(
                id = UUID.randomUUID().toString(),
                workspaceId = workspaceId,
                userId = myId,
                assignedRoleTitle = "Lead Creator",
                canModifyProduction = true
            ))

            productionTaskDao.insertTask(ProductionTask(
                id = UUID.randomUUID().toString(),
                workspaceId = workspaceId,
                creatorId = myId,
                title = "Future of AI Documentary",
                contentBody = "Exploring how AI affects future careers.",
                stateScope = "PRODUCTION_READY",
                kanbanLane = "SCRIPTING",
                createdAt = System.currentTimeMillis()
            ))

            productionTaskDao.insertTask(ProductionTask(
                id = UUID.randomUUID().toString(),
                workspaceId = workspaceId,
                creatorId = myId,
                title = "Intro Draft",
                contentBody = "Start with video of an empty room...",
                stateScope = "ROUGH_SANDBOX",
                createdAt = System.currentTimeMillis()
            ))

            messageDao.insertMessage(Message(
                id = UUID.randomUUID().toString(),
                workspaceId = workspaceId,
                senderId = "system",
                senderName = "Welcome Bot",
                messageBody = "Workspace started. Welcome!",
                timestamp = System.currentTimeMillis()
            ))

            val p1 = ProjectProposal(
                id = "proj_001",
                title = "TechPulse Syndicate",
                niche = "Tech",
                brief = "Creating a daily rapid-production team for our technical explainers. Looking for editors who can deliver high retention edits. Apply with portfolio links to view.",
                authorId = "user_tech_pulse",
                authorName = "Alex Riviera",
                createdAt = System.currentTimeMillis()
            )
            val p2 = ProjectProposal(
                id = "proj_002",
                title = "Cosmic Chronicles",
                niche = "Education",
                brief = "Forming a documentary-style video production team covering astronomy. Searching for a detail-oriented scriptwriter.",
                authorId = "user_cosmic",
                authorName = "Elara Nova",
                createdAt = System.currentTimeMillis() - 3600000
            )
            projectProposalDao.insertProjectProposal(p1)
            projectProposalDao.insertProjectProposal(p2)
        }
    }

    suspend fun deleteMyAccountCascade(userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. Delete Rough Sandbox tasks
            productionTaskDao.deleteRoughSandboxTasks(userId)
            
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

            // 2. Referrer
            val referrer = userDao.getUserById(referral.referrerId).firstOrNull()
            if (referrer != null) {
                val updatedReferrer = referrer.copy(
                    premiumTrialExtensionDays = referrer.premiumTrialExtensionDays + 7,
                    isVerifiedPro = true
                )
                userDao.insertUser(updatedReferrer)
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
        val count = talentPitchDao.getPitchCountSince(senderId, sinceTime)
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
    }

    suspend fun resolveConnectionRequest(requestId: String, accept: Boolean) {
        connectionRequestDao.updateRequestStatus(requestId, if (accept) "ACCEPTED" else "DECLINED")
    }

    // --- NOTIFICATIONS ---
    fun getNotificationsForUser(userId: String) = notificationDao.getNotificationsForUser(userId)

    suspend fun markNotificationAsRead(id: String) = notificationDao.markAsRead(id)
    suspend fun markAllNotificationsAsRead(userId: String) = notificationDao.markAllAsRead(userId)
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

    suspend fun getVerificationRequestById(id: String): VerificationRequest? = 
        verificationRequestDao.getVerificationRequestById(id)

    fun getAllVerificationRequestsFlow(): kotlinx.coroutines.flow.Flow<List<VerificationRequest>> = 
        verificationRequestDao.getAllVerificationRequests()

    suspend fun submitVerificationRequest(request: VerificationRequest) {
        verificationRequestDao.insertVerificationRequest(request)
    }

    suspend fun updateVerificationRequest(
        id: String, 
        status: String, 
        notes: String, 
        reviewedBy: String
    ) {
        verificationRequestDao.updateVerificationRequestStatus(id, status, notes, reviewedBy, System.currentTimeMillis())
        
        // Also update the actual UserProfile
        val request = verificationRequestDao.getVerificationRequestById(id)
        if (request != null && status == "APPROVED") {
            userDao.getUserById(request.userId).firstOrNull()?.let { user ->
                userDao.insertUser(user.copy(
                    isVerifiedPro = true,
                    verificationLevel = "L2 Pro Verified"
                ))
            }
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
    }

    suspend fun getSupportTicketById(id: String): SupportTicket? =
        supportTicketDao.getSupportTicketById(id)

    suspend fun deleteSupportTicket(id: String) {
        supportTicketDao.deleteSupportTicket(id)
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
    }

    suspend fun deleteFounderNote(id: String) {
        founderNoteDao.deleteNote(id)
    }

    // Workspace Assets
    fun getAssetsForWorkspace(workspaceId: String): Flow<List<WorkspaceAsset>> =
        workspaceAssetDao.getAssetsForWorkspace(workspaceId)
    
    suspend fun insertAsset(asset: WorkspaceAsset) = workspaceAssetDao.insertAsset(asset)
    
    suspend fun updateAssetStatus(assetId: String, status: String) = workspaceAssetDao.updateAssetStatus(assetId, status)
    
    suspend fun deleteAsset(assetId: String) = workspaceAssetDao.deleteAsset(assetId)

    // Deliverables
    fun getDeliverablesForWorkspace(workspaceId: String): Flow<List<Deliverable>> =
        deliverableDao.getDeliverablesForWorkspace(workspaceId)
        
    fun getDeliverablesForTask(taskId: String): Flow<List<Deliverable>> =
        deliverableDao.getDeliverablesForTask(taskId)
        
    suspend fun insertDeliverable(deliverable: Deliverable) = deliverableDao.insertDeliverable(deliverable)
    
    suspend fun updateDeliverableStatus(deliverableId: String, status: String, feedback: String? = null) = 
        deliverableDao.updateDeliverableStatus(deliverableId, status, feedback)
        
    suspend fun deleteDeliverable(deliverableId: String) = deliverableDao.deleteDeliverable(deliverableId)

    // Workspace Events
    fun getEventsForWorkspace(workspaceId: String): Flow<List<WorkspaceEvent>> =
        workspaceEventDao.getEventsForWorkspace(workspaceId)
        
    suspend fun insertWorkspaceEvent(event: WorkspaceEvent) = workspaceEventDao.insertEvent(event)
}

