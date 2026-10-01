package com.example.data.model

import androidx.room.*
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "project_proposals")
data class ProjectProposal(
    @PrimaryKey val id: String,
    val title: String,
    val niche: String,
    val brief: String,
    val authorId: String,
    val authorName: String,
    @androidx.room.ColumnInfo(name = "boosted_until") val boostedUntil: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)

data class ProjectProposalWithData(
    @Embedded val proposal: ProjectProposal,
    @Relation(
        parentColumn = "id",
        entityColumn = "projectId"
    )
    val pitches: List<TalentPitch> = emptyList(),
    @Relation(
        parentColumn = "authorId",
        entityColumn = "id"
    )
    val author: UserProfile? = null
)

@Serializable
@Entity(tableName = "talent_pitches")
data class TalentPitch(
    @PrimaryKey val id: String,
    val projectId: String,
    val senderId: String,
    val senderName: String = "",
    val senderSpecialty: String = "",
    val coverMessage: String = "",
    val portfolioUrl: String = "",
    val status: String = "PENDING",
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "ad_placements_table")
data class AdPlacement(
    @PrimaryKey val id: String,
    val slotId: String = "",
    val slotName: String = "",
    val screenLocation: String = "",
    val allowedLocations: List<String> = emptyList(),
    val isEnabled: Boolean = true,
    val lastModifiedAt: Long = System.currentTimeMillis(),
    val lastModifiedByAdminId: String = ""
)

@Serializable
@Entity(tableName = "saved_searches")
data class SavedSearch(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String = "",
    val query: String = "",
    val filterJson: String = "",
    val lastNotifiedAt: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "content_calendar_items")
data class ContentCalendarItem(
    @PrimaryKey val id: String,
    val workspaceId: String,
    val title: String,
    val scheduledDate: String = "",
    val linkedTaskId: String? = null,
    val createdBy: String = "",
    val status: String = "Drafting",
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "referrals")
data class Referral(
    @PrimaryKey val id: String,
    val referrerId: String,
    val referredUserId: String,
    val rewardGranted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "crm_records")
data class CrmRecord(
    @PrimaryKey val id: String,
    val userId: String = "",
    val name: String = "",
    val displayName: String = "",
    val email: String = "",
    val healthScore: Int = 100,
    val tasksCompletedCount: Int = 0,
    val agreementsSignedCount: Int = 0,
    val referralsCount: Int = 0,
    val loginsCount: Int = 0,
    val notes: String = "",
    val cohortSegment: String = "NEW",
    val followUpStatus: String = "PENDING",
    val lastInteraction: Long = System.currentTimeMillis(),
    val followUpTasksJson: String = "[]",
    val contactHistoryJson: String = "[]",
    val lastContacted: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "looking_for_work")
data class LookingForWork(
    @PrimaryKey val userId: String,
    val title: String = "",
    val description: String = "",
    val skills: List<String> = emptyList(),
    val detailsJson: String = "{}",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "search_filters")
data class SearchFilterEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val alertName: String,
    val queryText: String,
    val nicheFilter: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

