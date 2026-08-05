package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingSource

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: String): Flow<UserProfile?>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserByIdSuspend(id: String): UserProfile?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserProfile?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserProfile)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)
}

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY (upvotes - downvotes) DESC, timestamp DESC")
    fun getAllPostsSortedByTrending(): PagingSource<Int, Post>

    @Query("SELECT * FROM posts ORDER BY (upvotes - downvotes) DESC, timestamp DESC")
    fun getAllPostsSortedByTrendingList(): Flow<List<Post>>

    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPostsSortedByNew(): PagingSource<Int, Post>

    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPostsSortedByNewList(): Flow<List<Post>>

    @Query("SELECT * FROM posts ORDER BY upvotes DESC")
    fun getAllPostsSortedByTop(): PagingSource<Int, Post>

    @Query("SELECT * FROM posts ORDER BY upvotes DESC")
    fun getAllPostsSortedByTopList(): Flow<List<Post>>

    @Query("SELECT * FROM posts WHERE spaceName = :spaceName ORDER BY timestamp DESC")
    fun getPostsBySpace(spaceName: String): Flow<List<Post>>

    @Query("SELECT * FROM posts WHERE id = :id")
    fun getPostById(id: String): Flow<Post?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post)

    @Query("SELECT COUNT(*) FROM posts WHERE authorId = :authorId AND timestamp >= :sinceTime")
    suspend fun getPostCountSince(authorId: String, sinceTime: Long): Int

    @Query("SELECT * FROM posts WHERE authorId = :userId")
    fun getPostsByAuthor(userId: String): Flow<List<Post>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<Post>)

    @Query("DELETE FROM posts WHERE id = :id")
    suspend fun deletePost(id: String)

    @Query("UPDATE posts SET authorName = 'Deleted User', authorRole = 'Former Member', authorAvatarUrl = '' WHERE authorId = :userId")
    suspend fun anonymizePosts(userId: String)
}

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsForPost(postId: String): Flow<List<Comment>>

    @Query("SELECT * FROM comments ORDER BY timestamp DESC")
    fun getAllCommentsFlow(): Flow<List<Comment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment)

    @Query("DELETE FROM comments WHERE id = :id")
    suspend fun deleteComment(id: String)

    @Query("SELECT * FROM comments WHERE id = :id")
    suspend fun getCommentById(id: String): Comment?

    @Query("UPDATE comments SET authorName = 'Deleted User', authorRole = 'Former Member' WHERE authorId = :userId")
    suspend fun anonymizeComments(userId: String)

    @Query("SELECT * FROM comments WHERE authorId = :userId")
    fun getCommentsByAuthor(userId: String): Flow<List<Comment>>
}

@Dao
interface WorkspaceDao {
    @Query("SELECT * FROM workspaces ORDER BY createdAt DESC")
    fun getAllWorkspaces(): Flow<List<Workspace>>

    @Query("SELECT * FROM workspaces WHERE id = :id")
    fun getWorkspaceById(id: String): Flow<Workspace?>

    @Query("SELECT * FROM workspaces WHERE createdBy = :userId")
    fun getWorkspacesByOwner(userId: String): Flow<List<Workspace>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkspace(workspace: Workspace)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkspaces(workspaces: List<Workspace>)

    @Query("DELETE FROM workspaces WHERE id = :id")
    suspend fun deleteWorkspaceById(id: String)

    @Query("UPDATE workspaces SET isArchived = 1 WHERE id = :id")
    suspend fun archiveWorkspace(id: String)
}

@Dao
interface WorkspaceMemberDao {
    @Query("SELECT * FROM workspace_members WHERE workspaceId = :workspaceId")
    fun getMembersForWorkspace(workspaceId: String): Flow<List<WorkspaceMember>>

    @Query("SELECT * FROM workspace_members WHERE userId = :userId")
    fun getWorkspacesForUser(userId: String): Flow<List<WorkspaceMember>>
    
    @Query("SELECT * FROM workspace_members WHERE workspaceId = :workspaceId AND userId = :userId LIMIT 1")
    fun getMemberInfo(workspaceId: String, userId: String): Flow<WorkspaceMember?>

    @Query("SELECT * FROM workspace_members WHERE workspaceId = :workspaceId AND userId = :userId LIMIT 1")
    suspend fun getMemberInfoSuspend(workspaceId: String, userId: String): WorkspaceMember?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: WorkspaceMember)

    @Query("DELETE FROM workspace_members WHERE workspaceId = :workspaceId AND userId = :userId")
    suspend fun deleteMember(workspaceId: String, userId: String)

    @Query("UPDATE workspace_members SET assignedRoleTitle = :role WHERE workspaceId = :workspaceId AND userId = :userId")
    suspend fun updateMemberRole(workspaceId: String, userId: String, role: String)

    @Query("SELECT * FROM workspace_members WHERE userId = :userId")
    suspend fun getWorkspacesForUserList(userId: String): List<WorkspaceMember>

    @Query("SELECT * FROM workspace_members WHERE workspaceId = :workspaceId")
    suspend fun getMembersForWorkspaceList(workspaceId: String): List<WorkspaceMember>

    @Query("SELECT * FROM workspace_members")
    fun getAllMembersFlow(): Flow<List<WorkspaceMember>>

    @Query("UPDATE workspace_members SET isOnline = :isOnline, lastSeenAt = :lastSeenAt WHERE workspaceId = :workspaceId AND userId = :userId")
    suspend fun updateMemberPresence(workspaceId: String, userId: String, isOnline: Boolean, lastSeenAt: Long)

    @Query("UPDATE workspace_members SET isTyping = :isTyping, typingText = :typingText WHERE workspaceId = :workspaceId AND userId = :userId")
    suspend fun updateMemberTyping(workspaceId: String, userId: String, isTyping: Boolean, typingText: String)

    @Query("UPDATE workspace_members SET currentlyViewingTaskId = :taskId WHERE workspaceId = :workspaceId AND userId = :userId")
    suspend fun updateMemberViewingTask(workspaceId: String, userId: String, taskId: String?)

    @Query("UPDATE workspace_members SET currentlyEditingAssetId = :assetId WHERE workspaceId = :workspaceId AND userId = :userId")
    suspend fun updateMemberEditingAsset(workspaceId: String, userId: String, assetId: String?)

    @Query("UPDATE workspace_members SET liveStatusUpdate = :statusUpdate, lastSeenAt = :lastSeenAt WHERE workspaceId = :workspaceId AND userId = :userId")
    suspend fun updateMemberLiveStatus(workspaceId: String, userId: String, statusUpdate: String, lastSeenAt: Long)
}

@Dao
interface ProductionTaskDao {
    @Query("SELECT * FROM production_tasks WHERE workspaceId = :workspaceId AND stateScope = 'PRODUCTION_READY' ORDER BY createdAt DESC")
    fun getProductionReadyTasks(workspaceId: String): Flow<List<ProductionTask>>

    @Query("SELECT * FROM production_tasks WHERE workspaceId = :workspaceId AND creatorId = :userId AND stateScope = 'ROUGH_SANDBOX' ORDER BY createdAt DESC")
    fun getRoughSandboxTasks(workspaceId: String, userId: String): Flow<List<ProductionTask>>

    @Query("SELECT * FROM production_tasks WHERE id = :id")
    fun getTaskById(id: String): Flow<ProductionTask?>

    @Query("SELECT * FROM production_tasks WHERE id = :id")
    suspend fun getTaskByIdSuspend(id: String): ProductionTask?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: ProductionTask)

    @Query("DELETE FROM production_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)

    @Query("DELETE FROM production_tasks WHERE creatorId = :userId AND stateScope = 'ROUGH_SANDBOX'")
    suspend fun deleteRoughSandboxTasks(userId: String)

    @Query("""
        SELECT * FROM production_tasks 
        WHERE workspaceId IN (SELECT workspaceId FROM workspace_members WHERE userId = :userId) 
          AND stateScope = 'PRODUCTION_READY' 
        ORDER BY createdAt DESC
    """)
    fun getAllTasksForUser(userId: String): Flow<List<ProductionTask>>

    @Query("SELECT * FROM production_tasks ORDER BY createdAt DESC")
    fun getAllProductionTasks(): Flow<List<ProductionTask>>

    @Query("UPDATE production_tasks SET creatorId = 'deleted_user' WHERE creatorId = :userId AND stateScope = 'PRODUCTION_READY'")
    suspend fun anonymizeProductionTasks(userId: String)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE workspaceId = :workspaceId ORDER BY timestamp ASC")
    fun getMessagesForWorkspace(workspaceId: String): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE workspaceId = :workspaceId AND (senderId = :myId OR recipientId = :myId) ORDER BY timestamp ASC")
    fun getDMsForWorkspace(workspaceId: String, myId: String): Flow<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessage(id: String)

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getMessageById(id: String): Message?

    @Query("UPDATE messages SET senderName = 'Deleted User', senderRole = 'Former Member' WHERE senderId = :userId")
    suspend fun anonymizeMessages(userId: String)
}

@Dao
interface AgreementDao {
    @Query("SELECT * FROM team_agreements WHERE workspaceId = :workspaceId ORDER BY version DESC")
    fun getAllAgreementsForWorkspace(workspaceId: String): Flow<List<TeamAgreement>>

    @Query("SELECT * FROM team_agreements ORDER BY createdAt DESC")
    fun getAllAgreementsFlow(): Flow<List<TeamAgreement>>

    @Query("SELECT * FROM team_agreements WHERE workspaceId = :workspaceId ORDER BY version DESC LIMIT 1")
    fun getLatestAgreementFlow(workspaceId: String): Flow<TeamAgreement?>

    @Query("SELECT * FROM team_agreements WHERE workspaceId = :workspaceId ORDER BY version DESC LIMIT 1")
    suspend fun getLatestAgreement(workspaceId: String): TeamAgreement?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgreement(agreement: TeamAgreement)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAcknowledgment(acknowledgment: AgreementAcknowledgment)

    @Query("SELECT * FROM agreement_acknowledgments WHERE agreementId = :agreementId")
    fun getAcknowledgmentsFlow(agreementId: String): Flow<List<AgreementAcknowledgment>>

    @Query("SELECT * FROM agreement_acknowledgments WHERE userId = :userId")
    fun getAcknowledgmentsForUser(userId: String): Flow<List<AgreementAcknowledgment>>

    @Query("SELECT * FROM team_agreements WHERE id = :id")
    suspend fun getAgreementByIdSuspend(id: String): TeamAgreement?
}

@Dao
interface AdminDao {
    @Query("SELECT * FROM users WHERE systemRole = 'SUSPENDED'")
    fun getFlaggedUsers(): Flow<List<UserProfile>>

    @Query("UPDATE users SET systemRole = :role WHERE id = :userId")
    suspend fun setUserRole(userId: String, role: String)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Query("SELECT COUNT(*) FROM workspaces")
    suspend fun getWorkspaceCount(): Int

    @Query("SELECT COUNT(*) FROM posts")
    suspend fun getPostCount(): Int

    @Query("SELECT * FROM workspaces ORDER BY createdAt DESC")
    fun getAllWorkspacesForAdmin(): Flow<List<Workspace>>

    @Query("UPDATE workspaces SET isSponsored = :isSponsored, sponsorName = :sponsorName, sponsorLogoUrl = :sponsorLogoUrl WHERE id = :workspaceId")
    suspend fun updateWorkspaceSponsorship(workspaceId: String, isSponsored: Boolean, sponsorName: String?, sponsorLogoUrl: String?)
}

@Dao
interface ProjectProposalDao {
    @Transaction
    @Query("SELECT * FROM project_proposals ORDER BY CASE WHEN boosted_until > :now THEN 1 ELSE 0 END DESC, createdAt DESC")
    fun getAllProjectProposals(now: Long): PagingSource<Int, ProjectProposalWithData>

    @Transaction
    @Query("SELECT * FROM project_proposals ORDER BY CASE WHEN boosted_until > :now THEN 1 ELSE 0 END DESC, createdAt DESC")
    fun getAllProjectProposalsList(now: Long): Flow<List<ProjectProposalWithData>>

    @Query("SELECT * FROM project_proposals WHERE id = :id")
    suspend fun getProjectProposalByIdSuspend(id: String): ProjectProposal?

    @Query("SELECT * FROM project_proposals WHERE id = :id")
    fun getProjectProposalById(id: String): Flow<ProjectProposal?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjectProposal(proposal: ProjectProposal)

    @Query("DELETE FROM project_proposals WHERE id = :id")
    suspend fun deleteProjectProposalById(id: String)
}

@Dao
interface TalentPitchDao {
    @Query("SELECT * FROM talent_pitches WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getPitchesForProject(projectId: String): Flow<List<TalentPitch>>

    @Query("SELECT * FROM talent_pitches WHERE senderId = :senderId ORDER BY createdAt DESC")
    fun getPitchesBySender(senderId: String): Flow<List<TalentPitch>>

    @Query("SELECT * FROM talent_pitches WHERE id = :id")
    fun getTalentPitchById(id: String): Flow<TalentPitch?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTalentPitch(pitch: TalentPitch)

    @Query("SELECT COUNT(*) FROM talent_pitches WHERE senderId = :senderId AND createdAt >= :sinceTime")
    suspend fun getPitchCountSince(senderId: String, sinceTime: Long): Int

    @Query("UPDATE talent_pitches SET status = :status WHERE id = :id")
    suspend fun updatePitchStatus(id: String, status: String)
}

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports WHERE status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingReports(): Flow<List<Report>>

    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    fun getAllReportsFlow(): Flow<List<Report>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: Report)

    @Query("UPDATE reports SET status = :status WHERE id = :reportId")
    suspend fun updateReportStatus(reportId: String, status: String)

    @Query("SELECT * FROM reports WHERE id = :reportId")
    suspend fun getReportById(reportId: String): Report?

    @Query("SELECT * FROM reports WHERE createdAt >= :sinceTime")
    suspend fun getReportsSince(sinceTime: Long): List<Report>
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM admin_audit_logs ORDER BY createdAt DESC")
    fun getAllLogs(): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLog)
}

@Dao
interface SyncDao {
    @Query("SELECT * FROM sync_queue ORDER BY createdAt DESC")
    fun getAllSyncEventsFlow(): kotlinx.coroutines.flow.Flow<List<SyncEntity>>

    @Query("SELECT * FROM sync_queue WHERE syncStatus != 'SYNCED' AND retryCount < 5 ORDER BY createdAt ASC")
    suspend fun getPendingSyncEventsSuspend(): List<SyncEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncEvent(event: SyncEntity)

    @Update
    suspend fun updateSyncEvent(event: SyncEntity)

    @Query("UPDATE sync_queue SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String)

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteSyncEvent(id: String)
    
    @Query("DELETE FROM sync_queue WHERE syncStatus = 'SYNCED'")
    suspend fun clearSyncedEvents()
}

@Dao
interface AdDao {
    @Query("SELECT * FROM ad_placements_table")
    fun getAllPlacements(): Flow<List<AdPlacement>>

    @Update
    suspend fun updatePlacement(placement: AdPlacement)

    @Query("SELECT * FROM global_settings_table")
    fun getSettings(): Flow<List<GlobalSetting>>

    @Query("SELECT settingValue FROM global_settings_table WHERE settingKey = :key LIMIT 1")
    suspend fun getSettingByKey(key: String): String?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSetting(setting: GlobalSetting)
}

@Dao
interface UserSettingsDao {
    @Query("SELECT value FROM user_settings_table WHERE userId = :userId AND key = :key LIMIT 1")
    suspend fun getSetting(userId: String, key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: UserSetting)
}

@Dao
interface EndorsementDao {
    @Query("SELECT * FROM endorsements WHERE receiverId = :userId")
    fun getEndorsementsForUser(userId: String): Flow<List<Endorsement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEndorsement(endorsement: Endorsement)

    @Query("SELECT * FROM endorsements WHERE workspaceId = :workspaceId")
    fun getEndorsementsForWorkspace(workspaceId: String): Flow<List<Endorsement>>

    @Query("SELECT * FROM endorsements")
    fun getAllEndorsements(): Flow<List<Endorsement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEndorsements(endorsements: List<Endorsement>)
}

@Dao
interface SavedSearchDao {
    @Query("SELECT * FROM saved_searches WHERE userId = :userId ORDER BY createdAt DESC")
    fun getSavedSearchesForUser(userId: String): Flow<List<SavedSearch>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedSearch(savedSearch: SavedSearch)

    @Query("DELETE FROM saved_searches WHERE id = :id")
    suspend fun deleteSavedSearchById(id: String)

    @Query("SELECT * FROM saved_searches")
    suspend fun getAllSavedSearchesList(): List<SavedSearch>
}

@Dao
interface LookingForWorkDao {
    @Query("SELECT * FROM looking_for_work WHERE isActive = 1")
    fun getAllActiveListings(): Flow<List<LookingForWork>>

    @Query("SELECT * FROM looking_for_work WHERE userId = :userId LIMIT 1")
    fun getListingForUser(userId: String): Flow<LookingForWork?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: LookingForWork)

    @Query("DELETE FROM looking_for_work WHERE userId = :userId")
    suspend fun deleteListingForUser(userId: String)
}

@Dao
interface ContentCalendarItemDao {
    @Query("SELECT * FROM content_calendar_items WHERE workspaceId = :workspaceId ORDER BY scheduledDate ASC")
    fun getCalendarItemsForWorkspace(workspaceId: String): Flow<List<ContentCalendarItem>>

    @Query("SELECT * FROM content_calendar_items WHERE id = :id LIMIT 1")
    suspend fun getCalendarItemById(id: String): ContentCalendarItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendarItem(item: ContentCalendarItem)

    @Query("DELETE FROM content_calendar_items WHERE id = :id")
    suspend fun deleteCalendarItem(id: String)
}

@Dao
interface TaskTemplateDao {
    @Query("SELECT * FROM task_templates WHERE workspaceId = :workspaceId ORDER BY createdAt DESC")
    fun getTemplatesForWorkspace(workspaceId: String): Flow<List<TaskTemplate>>

    @Query("SELECT * FROM task_templates WHERE id = :id LIMIT 1")
    suspend fun getTemplateById(id: String): TaskTemplate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: TaskTemplate)

    @Query("DELETE FROM task_templates WHERE id = :id")
    suspend fun deleteTemplate(id: String)
}

@Dao
interface DisputeNoteDao {
    @Query("""
        SELECT * FROM dispute_notes_table 
        WHERE workspaceId = :workspaceId 
          AND EXISTS (SELECT 1 FROM workspace_members WHERE workspaceId = :workspaceId AND userId = :requestingUserId)
        ORDER BY createdAt DESC
    """)
    fun getDisputeNotesForWorkspace(workspaceId: String, requestingUserId: String): Flow<List<DisputeNote>>

    @Query("""
        SELECT * FROM dispute_notes_table 
        WHERE targetUserId = :targetUserId
          AND (
            authorId = :requestingUserId 
            OR EXISTS (SELECT 1 FROM workspace_members WHERE workspaceId = dispute_notes_table.workspaceId AND userId = :requestingUserId)
          )
        ORDER BY createdAt DESC
    """)
    fun getDisputeNotesAboutUser(targetUserId: String, requestingUserId: String): Flow<List<DisputeNote>>

    @Query("""
        SELECT * FROM dispute_notes_table 
        WHERE targetUserId IN (:targetUserIds)
          AND (
            authorId = :requestingUserId 
            OR EXISTS (SELECT 1 FROM workspace_members WHERE workspaceId = dispute_notes_table.workspaceId AND userId = :requestingUserId)
          )
        ORDER BY createdAt DESC
    """)
    fun getDisputeNotesAboutUsers(targetUserIds: List<String>, requestingUserId: String): Flow<List<DisputeNote>>

    @Query("SELECT * FROM dispute_notes_table ORDER BY createdAt DESC")
    fun getAllDisputeNotes(): Flow<List<DisputeNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDisputeNote(note: DisputeNote)
}

@Dao
interface ReferralDao {
    @Query("SELECT * FROM referrals WHERE referrerId = :referrerId")
    fun getReferralsByReferrer(referrerId: String): Flow<List<Referral>>

    @Query("SELECT * FROM referrals WHERE referredUserId = :referredUserId LIMIT 1")
    suspend fun getReferralForUser(referredUserId: String): Referral?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReferral(referral: Referral)

    @Query("SELECT COUNT(*) FROM referrals WHERE referrerId = :referrerId AND rewardGranted = 1")
    fun getSuccessfulReferralCount(referrerId: String): Flow<Int>

    @Query("SELECT * FROM users WHERE referralCode = :code LIMIT 1")
    suspend fun getUserByReferralCode(code: String): UserProfile?
}

@Dao
interface FeatureFlagDao {
    @Query("SELECT * FROM feature_flags_table")
    fun getAllFeatureFlagsFlow(): Flow<List<FeatureFlag>>

    @Query("SELECT * FROM feature_flags_table")
    suspend fun getAllFeatureFlags(): List<FeatureFlag>

    @Query("SELECT * FROM feature_flags_table WHERE flag_key = :key LIMIT 1")
    suspend fun getFeatureFlag(key: String): FeatureFlag?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeatureFlag(flag: FeatureFlag)
}

@Dao
interface ChangelogDao {
    @Query("SELECT * FROM changelog_entries_table ORDER BY created_at DESC")
    fun getAllChangelogEntries(): Flow<List<ChangelogEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChangelogEntry(entry: ChangelogEntry)

    @Query("SELECT * FROM changelog_entries_table ORDER BY created_at DESC LIMIT 1")
    suspend fun getLatestChangelogEntry(): ChangelogEntry?
}

@Dao
interface ConnectionRequestDao {
    @Query("SELECT * FROM connection_requests")
    fun getAllConnectionRequestsFlow(): Flow<List<ConnectionRequest>>

    @Query("SELECT * FROM connection_requests WHERE receiverId = :userId AND status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingRequestsForUser(userId: String): Flow<List<ConnectionRequest>>

    @Query("SELECT * FROM connection_requests WHERE receiverId = :userId AND status != 'PENDING' ORDER BY createdAt DESC")
    fun getResolvedRequestsForUser(userId: String): Flow<List<ConnectionRequest>>

    @Query("SELECT * FROM connection_requests WHERE receiverId = :userId AND status = :status ORDER BY createdAt DESC")
    fun getRequestsByStatus(userId: String, status: String): Flow<List<ConnectionRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: ConnectionRequest)
    
    @Query("SELECT COUNT(*) FROM connection_requests WHERE senderId = :senderId AND createdAt >= :sinceTime")
    suspend fun getRequestCountSince(senderId: String, sinceTime: Long): Int
    
    @Query("SELECT * FROM connection_requests WHERE id = :id")
    suspend fun getRequestById(id: String): ConnectionRequest?

    @Query("UPDATE connection_requests SET status = :status WHERE id = :id")
    suspend fun updateRequestStatus(id: String, status: String)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    fun getAllNotificationsFlow(): Flow<List<Notification>>

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun getNotificationsForUser(userId: String): Flow<List<Notification>>
    
    @Query("SELECT * FROM notifications WHERE userId = :userId AND type = :type ORDER BY createdAt DESC")
    fun getNotificationsByType(userId: String, type: String): Flow<List<Notification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notifications SET isRead = :isRead WHERE id = :id")
    suspend fun updateReadState(id: String, isRead: Int)
    
    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)
    
    @Query("UPDATE notifications SET isPinned = :isPinned WHERE id = :id")
    suspend fun updatePinnedState(id: String, isPinned: Int)

    @Query("UPDATE notifications SET isArchived = :isArchived WHERE id = :id")
    suspend fun updateArchivedState(id: String, isArchived: Int)
    
    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: String)
}

@Dao
interface VerificationRequestDao {
    @Query("SELECT * FROM verification_requests ORDER BY createdAt DESC")
    fun getAllVerificationRequests(): Flow<List<VerificationRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerificationRequest(request: VerificationRequest)

    @Query("SELECT * FROM verification_requests WHERE id = :id LIMIT 1")
    suspend fun getVerificationRequestById(id: String): VerificationRequest?

    @Query("UPDATE verification_requests SET status = :status, notes = :notes, reviewedBy = :reviewedBy, reviewedAt = :reviewedAt WHERE id = :id")
    suspend fun updateVerificationRequestStatus(id: String, status: String, notes: String, reviewedBy: String, reviewedAt: Long)
}

@Dao
interface UserAuditLogDao {
    @Query("SELECT * FROM user_audit_logs ORDER BY createdAt DESC")
    fun getAllUserAuditLogs(): Flow<List<UserAuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAuditLog(log: UserAuditLog)
}

@Dao
interface SupportTicketDao {
    @Query("SELECT * FROM support_tickets ORDER BY createdAt DESC")
    fun getAllSupportTickets(): Flow<List<SupportTicket>>

    @Query("SELECT * FROM support_tickets WHERE userId = :userId ORDER BY createdAt DESC")
    fun getSupportTicketsByUserId(userId: String): Flow<List<SupportTicket>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupportTicket(ticket: SupportTicket)

    @Query("SELECT * FROM support_tickets WHERE id = :id LIMIT 1")
    suspend fun getSupportTicketById(id: String): SupportTicket?

    @Query("DELETE FROM support_tickets WHERE id = :id")
    suspend fun deleteSupportTicket(id: String)
}

@Dao
interface CrmRecordDao {
    @Query("SELECT * FROM crm_records ORDER BY healthScore DESC")
    fun getAllCrmRecords(): Flow<List<CrmRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrmRecord(record: CrmRecord)

    @Query("SELECT * FROM crm_records WHERE id = :id LIMIT 1")
    suspend fun getCrmRecordById(id: String): CrmRecord?

    @Query("DELETE FROM crm_records WHERE id = :id")
    suspend fun deleteCrmRecord(id: String)
}

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements ORDER BY createdAt DESC")
    fun getAllAnnouncements(): Flow<List<Announcement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: Announcement)

    @Query("SELECT * FROM announcements WHERE id = :id LIMIT 1")
    suspend fun getAnnouncementById(id: String): Announcement?

    @Query("DELETE FROM announcements WHERE id = :id")
    suspend fun deleteAnnouncement(id: String)

    @Query("SELECT * FROM announcement_interactions WHERE announcementId = :announcementId")
    fun getInteractionsForAnnouncement(announcementId: String): Flow<List<AnnouncementInteraction>>

    @Query("SELECT * FROM announcement_interactions WHERE announcementId = :announcementId AND userId = :userId LIMIT 1")
    suspend fun getInteraction(announcementId: String, userId: String): AnnouncementInteraction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInteraction(interaction: AnnouncementInteraction)
    
    @Query("SELECT * FROM announcement_interactions WHERE userId = :userId")
    fun getUserInteractions(userId: String): Flow<List<AnnouncementInteraction>>
}

@Dao
interface PlatformControlDao {
    // Settings
    @Query("SELECT * FROM platform_settings WHERE id = 'singleton_settings' LIMIT 1")
    fun getPlatformSettingsFlow(): Flow<PlatformSettings?>

    @Query("SELECT * FROM platform_settings WHERE id = 'singleton_settings' LIMIT 1")
    suspend fun getPlatformSettings(): PlatformSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlatformSettings(settings: PlatformSettings)

    // Onboarding Slides
    @Query("SELECT * FROM onboarding_slides ORDER BY stepIndex ASC")
    fun getAllOnboardingSlidesFlow(): Flow<List<OnboardingSlide>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOnboardingSlide(slide: OnboardingSlide)

    @Query("DELETE FROM onboarding_slides WHERE id = :id")
    suspend fun deleteOnboardingSlide(id: String)

    // Welcome Messages
    @Query("SELECT * FROM welcome_messages")
    fun getAllWelcomeMessagesFlow(): Flow<List<WelcomeMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWelcomeMessage(msg: WelcomeMessage)

    @Query("DELETE FROM welcome_messages WHERE id = :id")
    suspend fun deleteWelcomeMessage(id: String)

    // Empty States
    @Query("SELECT * FROM empty_states")
    fun getAllEmptyStatesFlow(): Flow<List<EmptyStateConfig>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmptyState(config: EmptyStateConfig)

    @Query("DELETE FROM empty_states WHERE id = :id")
    suspend fun deleteEmptyState(id: String)

    // Help Texts
    @Query("SELECT * FROM help_texts")
    fun getAllHelpTextsFlow(): Flow<List<HelpText>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHelpText(helpText: HelpText)

    @Query("DELETE FROM help_texts WHERE id = :id")
    suspend fun deleteHelpText(id: String)
}

@Dao
interface FounderNoteDao {
    @Query("SELECT * FROM founder_notes WHERE entityType = :entityType AND entityId = :entityId ORDER BY createdAt DESC")
    fun getNotesForEntity(entityType: String, entityId: String): Flow<List<FounderNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: FounderNote)

    @Query("DELETE FROM founder_notes WHERE id = :id")
    suspend fun deleteNote(id: String)
}

@Dao
interface WorkspaceAssetDao {
    @Query("SELECT * FROM workspace_assets WHERE workspaceId = :workspaceId ORDER BY createdAt DESC")
    fun getAssetsForWorkspace(workspaceId: String): Flow<List<WorkspaceAsset>>

    @Query("SELECT * FROM workspace_assets ORDER BY createdAt DESC")
    fun getAllAssetsFlow(): Flow<List<WorkspaceAsset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: WorkspaceAsset)

    @Query("SELECT * FROM workspace_assets WHERE id = :assetId")
    suspend fun getAssetById(assetId: String): WorkspaceAsset?

    @Query("UPDATE workspace_assets SET status = :status WHERE id = :assetId")
    suspend fun updateAssetStatus(assetId: String, status: String)

    @Query("DELETE FROM workspace_assets WHERE id = :assetId")
    suspend fun deleteAsset(assetId: String)
}

@Dao
interface DeliverableDao {
    @Query("SELECT * FROM deliverables WHERE workspaceId = :workspaceId ORDER BY createdAt DESC")
    fun getDeliverablesForWorkspace(workspaceId: String): Flow<List<Deliverable>>

    @Query("SELECT * FROM deliverables ORDER BY createdAt DESC")
    fun getAllDeliverablesFlow(): Flow<List<Deliverable>>

    @Query("SELECT * FROM deliverables WHERE taskId = :taskId ORDER BY createdAt DESC")
    fun getDeliverablesForTask(taskId: String): Flow<List<Deliverable>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeliverable(deliverable: Deliverable)

    @Query("SELECT * FROM deliverables WHERE id = :deliverableId")
    suspend fun getDeliverableById(deliverableId: String): Deliverable?

    @Query("UPDATE deliverables SET status = :status, reviewFeedback = :feedback WHERE id = :deliverableId")
    suspend fun updateDeliverableStatus(deliverableId: String, status: String, feedback: String?)

    @Query("DELETE FROM deliverables WHERE id = :deliverableId")
    suspend fun deleteDeliverable(deliverableId: String)
}

@Dao
interface WorkspaceEventDao {
    @Query("SELECT * FROM workspace_events WHERE workspaceId = :workspaceId ORDER BY createdAt DESC")
    fun getEventsForWorkspace(workspaceId: String): Flow<List<WorkspaceEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: WorkspaceEvent)
}





