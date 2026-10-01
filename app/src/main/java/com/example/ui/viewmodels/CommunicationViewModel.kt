package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Announcement
import com.example.data.model.AnnouncementInteraction
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class CommunicationViewModel(private val repository: AppRepository) : ViewModel() {

    // Announcements Flow
    val allAnnouncements: StateFlow<List<Announcement>> = repository.getAllAnnouncementsFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Feedback message
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun clearToast() {
        _toastMessage.value = null
    }

    init {
        viewModelScope.launch {
            repository.getAllAnnouncementsFlow().firstOrNull()?.let { list ->
                if (list.isEmpty()) {
                    seedSampleAnnouncements()
                }
            }
        }
    }

    private suspend fun seedSampleAnnouncements() {
        val now = System.currentTimeMillis()
        val dayMs = 24 * 60 * 60 * 1000L

        val samples = listOf(
            Announcement(
                id = "ann_01",
                title = "Beta v1.5.0 Release - CRM Dashboard is Live!",
                content = "We have deployed a fully integrated Founder CRM & Beta Cohort Manager. Track health scores based on logins, contracts, and referrals.",
                targetAudience = "Beta Cohorts",
                campaignType = "Product Update",
                status = "Active",
                scheduledTime = 0L,
                createdAt = now - 5 * dayMs,
                opensCount = 42,
                clicksCount = 18,
                dismissalsCount = 3,
                targetReach = 50
            ),
            Announcement(
                id = "ann_02",
                title = "Scheduled Core System Maintenance",
                content = "Our servers will undergo scheduled maintenance to optimize secure ledger synchronization. Expect 15 minutes of downtime.",
                targetAudience = "All Users",
                campaignType = "Maintenance Notice",
                status = "Scheduled",
                scheduledTime = now + 1 * dayMs,
                createdAt = now - 1 * dayMs,
                opensCount = 0,
                clicksCount = 0,
                dismissalsCount = 0,
                targetReach = 120
            ),
            Announcement(
                id = "ann_03",
                title = "Exclusive Invite: Creator Advisory Council",
                content = "We are seeking elite verified content creators to co-design the next generation of automated milestone splits.",
                targetAudience = "Verified Creators",
                campaignType = "Beta Invite",
                status = "Active",
                scheduledTime = 0L,
                createdAt = now - 3 * dayMs,
                opensCount = 15,
                clicksCount = 11,
                dismissalsCount = 1,
                targetReach = 20
            ),
            Announcement(
                id = "ann_04",
                title = "Dynamic L2 Verification Automated Flows",
                content = "No more waiting for manual audit checks. Upload your portfolio URLs and sign peer agreements for instant verification.",
                targetAudience = "Workspace Owners",
                campaignType = "New Feature Launch",
                status = "Archived",
                scheduledTime = 0L,
                createdAt = now - 12 * dayMs,
                opensCount = 35,
                clicksCount = 5,
                dismissalsCount = 14,
                targetReach = 45
            ),
            Announcement(
                id = "ann_05",
                title = "Upcoming Workflow Automation Templates",
                content = "Draft placeholder for our upcoming template release. Editors and production workspace members can pre-configure templates.",
                targetAudience = "Editors",
                campaignType = "New Feature Launch",
                status = "Draft",
                scheduledTime = 0L,
                createdAt = now - 2 * dayMs,
                opensCount = 0,
                clicksCount = 0,
                dismissalsCount = 0,
                targetReach = 30
            )
        )

        for (ann in samples) {
            repository.insertAnnouncement(ann)
            
            // Seed randomized detailed interaction logs for rich analytics representation
            if (ann.opensCount > 0) {
                for (i in 1..ann.opensCount) {
                    val userId = "user_sim_$i"
                    val isClicked = i <= ann.clicksCount
                    val isDismissed = !isClicked && (i <= (ann.clicksCount + ann.dismissalsCount))
                    repository.insertAnnouncementInteraction(
                        AnnouncementInteraction(
                            id = "${ann.id}_$userId",
                            announcementId = ann.id,
                            userId = userId,
                            isOpened = true,
                            isClicked = isClicked,
                            isDismissed = isDismissed,
                            timestamp = ann.createdAt + (i * 10 * 60 * 1000L)
                        )
                    )
                }
            }
        }
    }

    // CRUD/Status mutations
    fun createAnnouncement(
        title: String,
        content: String,
        targetAudience: String,
        campaignType: String,
        status: String,
        scheduledDaysOffset: Int = 0
    ) {
        if (title.isBlank() || content.isBlank()) {
            _toastMessage.value = "Title and Content are mandatory."
            return
        }

        viewModelScope.launch {
            val id = "ann_" + UUID.randomUUID().toString().take(8)
            val schedTime = if (status == "Scheduled") {
                System.currentTimeMillis() + (scheduledDaysOffset * 24 * 60 * 60 * 1000L)
            } else {
                0L
            }

            // Estimate potential targeted reach based on segment choice
            val estimatedReach = when (targetAudience) {
                "All Users" -> 150
                "Verified Creators" -> 40
                "Editors" -> 25
                "Workspace Owners" -> 35
                "Beta Cohorts" -> 12
                else -> 10
            }

            val ann = Announcement(
                id = id,
                title = title,
                content = content,
                targetAudience = targetAudience,
                campaignType = campaignType,
                status = status,
                scheduledTime = schedTime,
                createdAt = System.currentTimeMillis(),
                opensCount = 0,
                clicksCount = 0,
                dismissalsCount = 0,
                targetReach = estimatedReach
            )

            repository.insertAnnouncement(ann)
            _toastMessage.value = "Announcement '$title' created successfully as $status."
        }
    }

    fun updateAnnouncementStatus(id: String, newStatus: String) {
        viewModelScope.launch {
            val existing = repository.getAnnouncementById(id) ?: return@launch
            val updated = existing.copy(
                status = newStatus,
                scheduledTime = if (newStatus == "Scheduled" && existing.scheduledTime == 0L) {
                    System.currentTimeMillis() + (2 * 24 * 60 * 60 * 1000L) // Default 2 days schedule
                } else existing.scheduledTime
            )
            repository.insertAnnouncement(updated)
            _toastMessage.value = "Announcement status updated to $newStatus."
        }
    }

    fun deleteAnnouncement(id: String) {
        viewModelScope.launch {
            repository.deleteAnnouncement(id)
            _toastMessage.value = "Announcement deleted."
        }
    }

    // Simulate standard user responses (for testing dashboard interaction feedback in visualizer)
    fun simulateUserInteraction(announcementId: String, interactionType: String) {
        viewModelScope.launch {
            val ann = repository.getAnnouncementById(announcementId) ?: return@launch
            val updated = when (interactionType) {
                "OPEN" -> ann.copy(opensCount = ann.opensCount + 1)
                "CLICK" -> ann.copy(
                    opensCount = if (ann.opensCount <= ann.clicksCount) ann.clicksCount + 1 else ann.opensCount,
                    clicksCount = ann.clicksCount + 1
                )
                "DISMISS" -> ann.copy(
                    opensCount = if (ann.opensCount <= ann.dismissalsCount) ann.dismissalsCount + 1 else ann.opensCount,
                    dismissalsCount = ann.dismissalsCount + 1
                )
                else -> ann
            }
            repository.insertAnnouncement(updated)
            
            // Add interaction record too
            val userId = "sim_user_" + UUID.randomUUID().toString().take(4)
            repository.insertAnnouncementInteraction(
                AnnouncementInteraction(
                    id = "${ann.id}_$userId",
                    announcementId = ann.id,
                    userId = userId,
                    isOpened = true,
                    isClicked = interactionType == "CLICK",
                    isDismissed = interactionType == "DISMISS"
                )
            )

            _toastMessage.value = "Simulated dynamic interaction: $interactionType"
        }
    }
}
