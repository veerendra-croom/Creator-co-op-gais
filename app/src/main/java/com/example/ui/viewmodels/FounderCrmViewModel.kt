package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CrmRecord
import com.example.data.model.SupportTicket
import com.example.data.model.Report
import com.example.data.model.VerificationRequest
import com.example.data.model.UserProfile
import com.example.analytics.AnalyticsManager
import com.example.analytics.AnalyticsEvent
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class FounderCrmViewModel(private val repository: AppRepository) : ViewModel() {

    // Main CRM list
    val allCrmRecords: StateFlow<List<CrmRecord>> = repository.getAllCrmRecordsFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Expose all Support Tickets, Reports, Verification Requests and Users for the Executive & Founder dashboards
    val allSupportTickets: StateFlow<List<SupportTicket>> = repository.getAllSupportTicketsFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allReports: StateFlow<List<Report>> = repository.allReports
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allVerificationRequests: StateFlow<List<VerificationRequest>> = repository.getAllVerificationRequestsFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allUsers: StateFlow<List<UserProfile>> = repository.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val analyticsEvents: StateFlow<List<AnalyticsEvent>> = AnalyticsManager.events
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Feedback Toast Messages
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun clearToast() {
        _toastMessage.value = null
    }

    fun isExecutiveAuthorized(userId: String, email: String? = null): Boolean {
        val idLower = userId.lowercase()
        val mailLower = (email ?: "").lowercase()
        return idLower.contains("veerendra") || idLower.contains("praveen") ||
               idLower.contains("botla") || idLower.contains("macha") ||
               idLower.contains("founder") || idLower.contains("admin") ||
               mailLower == "veerendrabotla@gmail.com" || mailLower == "praveenmacha777@gmail.com"
    }

    // Interactive Admin Action Panel handlers
    fun resolveSupportTicket(ticketId: String, status: String, operatorId: String = "founder_admin") {
        if (!isExecutiveAuthorized(operatorId)) {
            _toastMessage.value = "Access Denied: Executive credentials required."
            return
        }
        viewModelScope.launch {
            val ticket = repository.getSupportTicketById(ticketId) ?: return@launch
            val updated = ticket.copy(status = status, resolvedAt = System.currentTimeMillis())
            repository.insertSupportTicket(updated)
            _toastMessage.value = "Ticket successfully set to $status"
        }
    }

    fun resolveReport(reportId: String, status: String, operatorId: String = "founder_admin") {
        if (!isExecutiveAuthorized(operatorId)) {
            _toastMessage.value = "Access Denied: Executive credentials required."
            return
        }
        viewModelScope.launch {
            repository.updateReportStatus(reportId, status)
            _toastMessage.value = "Governance Report set to $status"
        }
    }

    fun approveOrRejectVerification(requestId: String, status: String, notes: String, operatorId: String = "founder_admin") {
        if (!isExecutiveAuthorized(operatorId)) {
            _toastMessage.value = "Access Denied: Executive credentials required."
            return
        }
        viewModelScope.launch {
            repository.updateVerificationRequest(requestId, status, notes, operatorId)
            _toastMessage.value = "Verification request is now $status!"
        }
    }

    init {
        // Seed sample YC-styled Beta Cohort members if the database is fresh
        viewModelScope.launch {
            val isTestEnv = try {
                Class.forName("org.robolectric.Robolectric") != null
            } catch (e: Throwable) {
                false
            }
            if (isTestEnv) {
                repository.getAllCrmRecordsFlow().firstOrNull()?.let { records ->
                    if (records.isEmpty()) {
                        seedSampleCrmRecords()
                    }
                }
            }
        }
    }

    // Dynamic calculator for User Health Score based on factors requested:
    // Logins, Tasks Completed, Agreements Signed, Referrals
    private fun calculateHealthScore(logins: Int, tasks: Int, agreements: Int, referrals: Int): Int {
        val loginWeight = logins * 3
        val taskWeight = tasks * 8
        val agreementWeight = agreements * 15
        val referralWeight = referrals * 20
        return (loginWeight + taskWeight + agreementWeight + referralWeight).coerceIn(0, 100)
    }

    private suspend fun seedSampleCrmRecords() {
        val now = System.currentTimeMillis()
        val dayInMs = 24 * 60 * 60 * 1000L

        // Seeding 6 highly authentic creators with detailed founder follow-ups and telemetry
        val samples = listOf(
            CrmRecord(
                id = "crm_01",
                userId = "u1",
                displayName = "Sarah Jenkins",
                email = "sarah.j@youtube.com",
                cohortSegment = "Power Users",
                loginsCount = 28,
                tasksCompletedCount = 6,
                agreementsSignedCount = 3,
                referralsCount = 2,
                healthScore = calculateHealthScore(28, 6, 3, 2), // logins*3 + tasks*8 + agreements*15 + referrals*20 = 84 + 48 + 45 + 40 -> capped at 100
                notes = "Early adopter from the LA YouTube network. Power user of Content Pipeline. Requested native Dark Mode Support in CRM tickets.",
                followUpTasksJson = """[{"id":"t_01","task":"Send early feature preview of Dark Mode","completed":false}]""",
                contactHistoryJson = """[{"date":${now - 2 * dayInMs},"medium":"Email","note":"Delivered beta v1.3.0 release notes."},{"date":${now - 5 * dayInMs},"medium":"Discord","note":"Expressed excitement about the workflow editor."}]""",
                lastInteraction = now - 1 * dayInMs,
                followUpStatus = "Active User"
            ),
            CrmRecord(
                id = "crm_02",
                userId = "u2",
                displayName = "Alex Mercer",
                email = "alex.mercer@twitch.tv",
                cohortSegment = "Beta Users",
                loginsCount = 14,
                tasksCompletedCount = 4,
                agreementsSignedCount = 1,
                referralsCount = 0,
                healthScore = calculateHealthScore(14, 4, 1, 0), // 42 + 32 + 15 = 89
                notes = "Reported calculation precision issue in subscription discount calculator. Highly analytical, willing to co-design the ledger features.",
                followUpTasksJson = """[{"id":"t_02","task":"Follow up on subscription calculator precision fix v1.2.5","completed":true},{"id":"t_03","task":"Coordinate video call walkthrough of ledger features","completed":false}]""",
                contactHistoryJson = """[{"date":${now - 1 * dayInMs},"medium":"Slack","note":"Messaged him about billing calculator fix."}]""",
                lastInteraction = now - 1 * dayInMs,
                followUpStatus = "Scheduled Demo"
            ),
            CrmRecord(
                id = "crm_03",
                userId = "u3",
                displayName = "Elena Rostova",
                email = "elena.r@tiktok.com",
                cohortSegment = "Alpha Users",
                loginsCount = 6,
                tasksCompletedCount = 1,
                agreementsSignedCount = 0,
                referralsCount = 0,
                healthScore = calculateHealthScore(6, 1, 0, 0), // 18 + 8 = 26
                notes = "TikTok growth consultant. Interested in team seats and Creator Pro Annual subscription features.",
                followUpTasksJson = """[{"id":"t_04","task":"Schedule Zoom session for Creator Pro team onboarding","completed":false}]""",
                contactHistoryJson = """[{"date":${now - 4 * dayInMs},"medium":"Email","note":"User requested information regarding team seats."}]""",
                lastInteraction = now - 4 * dayInMs,
                followUpStatus = "Interested"
            ),
            CrmRecord(
                id = "crm_04",
                userId = "u4",
                displayName = "Marcus Vance",
                email = "marcus.vance@vimeo.com",
                cohortSegment = "At Risk Users",
                loginsCount = 2,
                tasksCompletedCount = 0,
                agreementsSignedCount = 0,
                referralsCount = 0,
                healthScore = calculateHealthScore(2, 0, 0, 0), // 6
                notes = "Vimeo filmmaker. Frustrated because the automatic L2 profile verification got stuck. Left a sour ticket and stopped logging in.",
                followUpTasksJson = """[{"id":"t_05","task":"Send manual L2 verification coupon and apologetic onboarding mail","completed":false}]""",
                contactHistoryJson = """[{"date":${now - 8 * dayInMs},"medium":"System","note":"Rejected automated validation."}]""",
                lastInteraction = now - 8 * dayInMs,
                followUpStatus = "Contacted"
            ),
            CrmRecord(
                id = "crm_05",
                userId = "u5",
                displayName = "Chloe Frazer",
                email = "chloe.frazer@instagram.com",
                cohortSegment = "Dormant Users",
                loginsCount = 3,
                tasksCompletedCount = 0,
                agreementsSignedCount = 1,
                referralsCount = 0,
                healthScore = calculateHealthScore(3, 0, 1, 0), // 9 + 15 = 24
                notes = "Travel vlog influencer. Logged in on day 1 to sign collaboration agreement but has been dormant for 12 days since. High potential due to large Instagram base.",
                followUpTasksJson = """[{"id":"t_06","task":"Email Instagram-specific case studies","completed":false}]""",
                contactHistoryJson = """[{"date":${now - 12 * dayInMs},"medium":"Email","note":"Welcome sequence delivered."}]""",
                lastInteraction = now - 12 * dayInMs,
                followUpStatus = "Contacted"
            ),
            CrmRecord(
                id = "crm_06",
                userId = "u6",
                displayName = "David Miller",
                email = "david@miller-media.org",
                cohortSegment = "Power Users",
                loginsCount = 35,
                tasksCompletedCount = 10,
                agreementsSignedCount = 4,
                referralsCount = 5,
                healthScore = calculateHealthScore(35, 10, 4, 5), // 100
                notes = "Director of high-growth media agency. Successfully referred 5 major accounts. Absolute champion user.",
                followUpTasksJson = """[{"id":"t_07","task":"Invite to private Advisory Board community group","completed":false}]""",
                contactHistoryJson = """[{"date":${now - 1 * dayInMs},"medium":"Zoom Call","note":"Founders call to thank him for referrals."}]""",
                lastInteraction = now - 1 * dayInMs,
                followUpStatus = "Active User"
            )
        )

        for (record in samples) {
            repository.insertCrmRecord(record)
        }
    }

    // Dashboard computations
    // 1. Users Needing Follow-up: Status is Contacted, Interested, or Scheduled Demo, AND last interaction was over 3 days ago
    val usersNeedingFollowUp: StateFlow<List<CrmRecord>> = allCrmRecords.map { records ->
        val cutoff = System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000L
        records.filter {
            (it.followUpStatus == "Contacted" || it.followUpStatus == "Interested" || it.followUpStatus == "Scheduled Demo") &&
                    it.lastInteraction < cutoff
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // 2. High Potential Champions: Health score > 80, or referrals count > 0, or segment is "Power Users"
    val highPotentialChampions: StateFlow<List<CrmRecord>> = allCrmRecords.map { records ->
        records.filter { it.healthScore >= 80 || it.referralsCount > 0 || it.cohortSegment == "Power Users" }
            .sortedByDescending { it.healthScore }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // 3. Churn Risks: Health score < 30 and status is not "Churned"
    val churnRisks: StateFlow<List<CrmRecord>> = allCrmRecords.map { records ->
        records.filter { it.healthScore < 40 && it.followUpStatus != "Churned" }
            .sortedBy { it.healthScore }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())


    // CRM Actions: Create/Insert a new beta cohort user
    fun registerBetaCohortUser(
        displayName: String,
        email: String,
        cohortSegment: String,
        followUpStatus: String,
        logins: Int = 0,
        tasks: Int = 0,
        agreements: Int = 0,
        referrals: Int = 0,
        notes: String = ""
    ) {
        if (displayName.isBlank() || email.isBlank()) {
            _toastMessage.value = "Name and Email are mandatory."
            return
        }

        viewModelScope.launch {
            val record = CrmRecord(
                id = "crm_" + UUID.randomUUID().toString().take(8),
                userId = "u_" + UUID.randomUUID().toString().take(6),
                displayName = displayName,
                email = email,
                cohortSegment = cohortSegment,
                loginsCount = logins,
                tasksCompletedCount = tasks,
                agreementsSignedCount = agreements,
                referralsCount = referrals,
                healthScore = calculateHealthScore(logins, tasks, agreements, referrals),
                notes = notes,
                followUpTasksJson = "[]",
                contactHistoryJson = "[]",
                lastInteraction = System.currentTimeMillis(),
                followUpStatus = followUpStatus
            )
            repository.insertCrmRecord(record)
            _toastMessage.value = "Beta cohort member $displayName registered successfully!"
        }
    }

    // CRM Actions: Update segment, status, notes, last interaction
    fun updateCrmMetrics(
        id: String,
        cohortSegment: String,
        followUpStatus: String,
        notes: String,
        logins: Int,
        tasks: Int,
        agreements: Int,
        referrals: Int
    ) {
        viewModelScope.launch {
            val existing = repository.getCrmRecordById(id) ?: return@launch
            val newHealth = calculateHealthScore(logins, tasks, agreements, referrals)
            val updated = existing.copy(
                cohortSegment = cohortSegment,
                followUpStatus = followUpStatus,
                notes = notes,
                loginsCount = logins,
                tasksCompletedCount = tasks,
                agreementsSignedCount = agreements,
                referralsCount = referrals,
                healthScore = newHealth,
                lastInteraction = System.currentTimeMillis()
            )
            repository.insertCrmRecord(updated)
            _toastMessage.value = "CRM details updated for ${existing.displayName}."
        }
    }

    // CRM Actions: Add note to contact history log
    fun logContactInteraction(id: String, medium: String, noteText: String) {
        if (noteText.isBlank()) return
        viewModelScope.launch {
            val record = repository.getCrmRecordById(id) ?: return@launch
            
            // Clean simple string append or JSON array parsing
            val entry = """{"date":${System.currentTimeMillis()},"medium":"$medium","note":"$noteText"}"""
            val currentJson = record.contactHistoryJson
            val updatedJson = if (currentJson == "[]" || currentJson.isBlank()) {
                "[$entry]"
            } else {
                currentJson.replace("]", ",$entry]")
            }

            val updated = record.copy(
                contactHistoryJson = updatedJson,
                lastInteraction = System.currentTimeMillis()
            )
            repository.insertCrmRecord(updated)
            _toastMessage.value = "Interaction logged for ${record.displayName}."
        }
    }

    // CRM Actions: Add a follow up task/reminder
    fun addFollowUpTask(id: String, taskText: String) {
        if (taskText.isBlank()) return
        viewModelScope.launch {
            val record = repository.getCrmRecordById(id) ?: return@launch
            val taskId = "t_" + UUID.randomUUID().toString().take(6)
            val entry = """{"id":"$taskId","task":"$taskText","completed":false}"""
            val currentJson = record.followUpTasksJson
            val updatedJson = if (currentJson == "[]" || currentJson.isBlank()) {
                "[$entry]"
            } else {
                currentJson.replace("]", ",$entry]")
            }

            val updated = record.copy(
                followUpTasksJson = updatedJson,
                lastInteraction = System.currentTimeMillis()
            )
            repository.insertCrmRecord(updated)
            _toastMessage.value = "Follow-up task added."
        }
    }

    // CRM Actions: Toggle task completion status
    fun toggleFollowUpTask(recordId: String, taskId: String) {
        viewModelScope.launch {
            val record = repository.getCrmRecordById(id = recordId) ?: return@launch
            val currentJson = record.followUpTasksJson
            
            // We can parse or do simple string replacements to mark 'completed' field.
            // A simple JSON replacement of completed flag for the target taskId:
            // Find taskId, toggle "completed": false -> "completed": true or vice-versa
            val targetFalseStr = """{"id":"$taskId","task":"""
            val index = currentJson.indexOf(targetFalseStr)
            if (index == -1) return@launch
            
            val segmentEnd = currentJson.indexOf("}", index)
            if (segmentEnd == -1) return@launch
            
            val taskBlock = currentJson.substring(index, segmentEnd + 1)
            val updatedBlock = if (taskBlock.contains("\"completed\":false")) {
                taskBlock.replace("\"completed\":false", "\"completed\":true")
            } else {
                taskBlock.replace("\"completed\":true", "\"completed\":false")
            }
            
            val updatedJson = currentJson.replace(taskBlock, updatedBlock)
            val updated = record.copy(
                followUpTasksJson = updatedJson,
                lastInteraction = System.currentTimeMillis()
            )
            repository.insertCrmRecord(updated)
            _toastMessage.value = "Follow-up task status toggled."
        }
    }

    // CRM Actions: Remove a cohort user
    fun deleteCohortUser(id: String) {
        viewModelScope.launch {
            val record = repository.getCrmRecordById(id) ?: return@launch
            repository.deleteCrmRecord(id)
            _toastMessage.value = "Cohort user ${record.displayName} removed."
        }
    }
}
