package com.example.data.local

import android.content.Context
import androidx.room.*
import com.example.data.model.*

@Database(
    entities = [
        UserProfile::class,
        Workspace::class,
        WorkspaceMember::class,
        ProductionTask::class,
        TeamAgreement::class,
        AgreementAcknowledgment::class,
        Message::class,
        Post::class,
        Comment::class,
        ProjectProposal::class,
        TalentPitch::class,
        Report::class,
        AuditLog::class,
        SyncEntity::class,
        AdPlacement::class,
        GlobalSetting::class,
        UserSetting::class,
        Endorsement::class,
        SavedSearch::class,
        LookingForWork::class,
        ContentCalendarItem::class,
        TaskTemplate::class,
        DisputeNote::class,
        Referral::class,
        FeatureFlag::class,
        ChangelogEntry::class,
        ConnectionRequest::class,
        Notification::class,
        VerificationRequest::class,
        UserAuditLog::class,
        SupportTicket::class,
        CrmRecord::class,
        Announcement::class,
        AnnouncementInteraction::class,
        PlatformSettings::class,
        OnboardingSlide::class,
        WelcomeMessage::class,
        EmptyStateConfig::class,
        HelpText::class,
        FounderNote::class,
        WorkspaceAsset::class,
        Deliverable::class,
        WorkspaceEvent::class
    ],
    version = 36,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun founderNoteDao(): FounderNoteDao
    abstract fun workspaceAssetDao(): WorkspaceAssetDao
    abstract fun deliverableDao(): DeliverableDao
    abstract fun workspaceEventDao(): WorkspaceEventDao
    abstract fun featureFlagDao(): FeatureFlagDao
    abstract fun referralDao(): ReferralDao
    abstract fun disputeNoteDao(): DisputeNoteDao
    abstract fun userDao(): UserDao
    abstract fun workspaceDao(): WorkspaceDao
    abstract fun workspaceMemberDao(): WorkspaceMemberDao
    abstract fun productionTaskDao(): ProductionTaskDao
    abstract fun agreementDao(): AgreementDao
    abstract fun messageDao(): MessageDao
    abstract fun postDao(): PostDao
    abstract fun commentDao(): CommentDao
    abstract fun adminDao(): AdminDao
    abstract fun reportDao(): ReportDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun syncDao(): SyncDao
    abstract fun adDao(): AdDao
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun projectProposalDao(): ProjectProposalDao
    abstract fun talentPitchDao(): TalentPitchDao
    abstract fun endorsementDao(): EndorsementDao
    abstract fun savedSearchDao(): SavedSearchDao
    abstract fun lookingForWorkDao(): LookingForWorkDao
    abstract fun contentCalendarItemDao(): ContentCalendarItemDao
    abstract fun taskTemplateDao(): TaskTemplateDao
    abstract fun changelogDao(): ChangelogDao
    abstract fun connectionRequestDao(): ConnectionRequestDao
    abstract fun notificationDao(): NotificationDao
    abstract fun verificationRequestDao(): VerificationRequestDao
    abstract fun userAuditLogDao(): UserAuditLogDao
    abstract fun supportTicketDao(): SupportTicketDao
    abstract fun crmRecordDao(): CrmRecordDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun platformControlDao(): PlatformControlDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "creator_coop_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
