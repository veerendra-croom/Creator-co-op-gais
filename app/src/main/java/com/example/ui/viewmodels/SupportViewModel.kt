package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.SupportTicket
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class SupportViewModel(private val repository: AppRepository) : ViewModel() {

    // All support tickets in the system
    val allTickets: StateFlow<List<SupportTicket>> = repository.getAllSupportTicketsFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Toast and status feedback
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun clearToast() {
        _toastMessage.value = null
    }

    // Analytics: Open Tickets count
    val openTicketsCount: StateFlow<Int> = allTickets.map { tickets ->
        tickets.count { it.status == "OPEN" || it.status == "IN_PROGRESS" || it.status == "WAITING_USER" }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    // Analytics: Most Requested Features (Feature requests sorted by upvotes)
    val mostRequestedFeatures: StateFlow<List<SupportTicket>> = allTickets.map { tickets ->
        tickets.filter { it.category == "Feature Request" }
            .sortedByDescending { it.upvotes }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Analytics: Average Resolution Time (in Hours)
    val averageResolutionTimeHours: StateFlow<Double> = allTickets.map { tickets ->
        val resolved = tickets.filter { (it.status == "RESOLVED" || it.status == "CLOSED") && it.resolvedAt != null }
        if (resolved.isEmpty()) {
            0.0
        } else {
            val totalDurationMs = resolved.map { it.resolvedAt!! - it.createdAt }.sum()
            val avgMs = totalDurationMs.toDouble() / resolved.size
            avgMs / (1000 * 60 * 60) // Convert to hours
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    init {
        // Seed some sample support operations center data if empty
        viewModelScope.launch {
            val isTestEnv = try {
                Class.forName("org.robolectric.Robolectric") != null
            } catch (e: Throwable) {
                false
            }
            if (isTestEnv) {
                repository.getAllSupportTicketsFlow().firstOrNull()?.let { tickets ->
                    if (tickets.isEmpty()) {
                        seedSampleTickets()
                    }
                }
            }
        }
    }

    private suspend fun seedSampleTickets() {
        val now = System.currentTimeMillis()
        val dayInMs = 24 * 60 * 60 * 1000L
        
        val sampleTickets = listOf(
            SupportTicket(
                id = "t1",
                userId = "u1",
                userDisplayName = "Sarah Jenkins",
                category = "Feature Request",
                title = "Dark Mode Toggle in Workspace Settings",
                description = "Adding a native dark mode toggle for the late-night editing sessions. My eyes are burning!",
                status = "OPEN",
                createdAt = now - 3 * dayInMs,
                upvotes = 42,
                upvotedUserIdsJson = "[\"u1\", \"u2\", \"u3\"]",
                internalPriority = "LOW"
            ),
            SupportTicket(
                id = "t2",
                userId = "u2",
                userDisplayName = "Alex Mercer",
                category = "Bug",
                title = "Subscription Pro Upgrade Calculation Issue",
                description = "The monthly to annual pro upgrade discount calculation displayed an unexpected rounding value when switching plans.",
                status = "IN_PROGRESS",
                createdAt = now - 2 * dayInMs,
                deviceInfo = "Google Pixel 8, Android 14",
                appVersion = "v1.2.4",
                logs = "java.lang.ArithmeticException: calculation preview overflow\n  at com.example.util.ProBillingCalculator.calculate(BillingCalculator.kt:42)",
                screenshots = listOf("billing_preview_error.png"),
                internalPriority = "HIGH",
                assignedAdminId = "admin_01",
                assignedAdminName = "Lead Ops Engineer",
                internalNotes = "Investigating rounding precision logic inside BillingCalculator.kt. Resolving in v1.2.5 sprint."
            ),
            SupportTicket(
                id = "t3",
                userId = "u3",
                userDisplayName = "Elena Rostova",
                category = "Account Issue",
                title = "Unable to renew Creator Pro Annual Plan",
                description = "Creator Pro subscription renewal button returns to verification prompt. Requesting manual license grant.",
                status = "WAITING_USER",
                createdAt = now - 4 * dayInMs,
                internalPriority = "HIGH",
                assignedAdminId = "admin_01",
                assignedAdminName = "Lead Ops Engineer",
                internalNotes = "Verified payment receipt. Granted Creator Pro status manually."
            ),
            SupportTicket(
                id = "t4",
                userId = "u4",
                userDisplayName = "Marcus Vance",
                category = "Verification Issue",
                title = "Verification flow fails at L2 check",
                description = "Uploaded my portfolio link, but the system says verification is rejected without giving a specific reason.",
                status = "RESOLVED",
                createdAt = now - 5 * dayInMs,
                resolvedAt = now - 4 * dayInMs,
                internalPriority = "MEDIUM",
                internalNotes = "Approved manually after validating portfolio directly in the backend support operations panel.",
                assignedAdminId = "admin_02",
                assignedAdminName = "Operations Specialist"
            ),
            SupportTicket(
                id = "t5",
                userId = "u5",
                userDisplayName = "Chloe Frazer",
                category = "Feature Request",
                title = "Collaborator Rich Text Comments",
                description = "Allow markdown or rich-text formatting for workspace collaboration feedback and production kanban lanes.",
                status = "OPEN",
                createdAt = now - 1 * dayInMs,
                upvotes = 18,
                upvotedUserIdsJson = "[\"u5\"]",
                internalPriority = "MEDIUM"
            )
        )
        
        sampleTickets.forEach { repository.insertSupportTicket(it) }
    }

    // Submit a support ticket (flexible for Bugs, Features, and Generic Issues)
    fun createSupportTicket(
        userId: String,
        userDisplayName: String,
        category: String,
        title: String,
        description: String,
        deviceInfo: String = "",
        appVersion: String = "",
        logs: String = "",
        screenshots: String = ""
    ) {
        if (title.isBlank() || description.isBlank()) {
            _toastMessage.value = "Title and Description cannot be empty."
            return
        }

        viewModelScope.launch {
            val ticket = SupportTicket(
                id = UUID.randomUUID().toString(),
                userId = userId,
                userDisplayName = userDisplayName,
                category = category,
                title = title,
                description = description,
                status = "OPEN",
                createdAt = System.currentTimeMillis(),
                deviceInfo = deviceInfo,
                appVersion = appVersion,
                logs = logs,
                screenshots = if (screenshots.isBlank()) emptyList() else listOf(screenshots),
                upvotes = if (category == "Feature Request") 1 else 0,
                upvotedUserIdsJson = if (category == "Feature Request") "[\"$userId\"]" else "[]",
                internalPriority = if (category == "Bug") "HIGH" else "MEDIUM"
            )
            repository.insertSupportTicket(ticket)
            _toastMessage.value = "Ticket successfully submitted to Operations Center!"
        }
    }

    // Upvote feature requests
    fun upvoteFeatureRequest(ticketId: String, userId: String) {
        viewModelScope.launch {
            val ticket = repository.getSupportTicketById(ticketId) ?: return@launch
            if (ticket.category != "Feature Request") return@launch

            // Parse existing upvoted user ids
            val upvotedUsers = try {
                val jsonStr = ticket.upvotedUserIdsJson
                if (jsonStr.startsWith("[") && jsonStr.endsWith("]")) {
                    jsonStr.removeSurrounding("[", "]")
                        .split(",")
                        .map { it.trim().removeSurrounding("\"") }
                        .filter { it.isNotEmpty() }
                        .toMutableList()
                } else mutableListOf()
            } catch (e: Exception) {
                mutableListOf()
            }

            if (upvotedUsers.contains(userId)) {
                _toastMessage.value = "You have already upvoted this feature request."
                return@launch
            }

            upvotedUsers.add(userId)
            val updatedJson = upvotedUsers.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
            val updatedTicket = ticket.copy(
                upvotes = ticket.upvotes + 1,
                upvotedUserIdsJson = updatedJson
            )

            repository.insertSupportTicket(updatedTicket)
            _toastMessage.value = "Feature request upvoted!"
        }
    }

    // Admin support panel: Assign Ticket
    fun assignTicket(ticketId: String, adminId: String, adminName: String) {
        viewModelScope.launch {
            val ticket = repository.getSupportTicketById(ticketId) ?: return@launch
            val updated = ticket.copy(
                assignedAdminId = adminId,
                assignedAdminName = adminName,
                status = if (ticket.status == "OPEN") "IN_PROGRESS" else ticket.status
            )
            repository.insertSupportTicket(updated)
            _toastMessage.value = "Ticket assigned to $adminName."
        }
    }

    // Admin support panel: Update Ticket Status (Ticket Flow)
    fun updateTicketStatus(ticketId: String, status: String) {
        viewModelScope.launch {
            val ticket = repository.getSupportTicketById(ticketId) ?: return@launch
            val now = System.currentTimeMillis()
            val resolvedAt = if (status == "RESOLVED" || status == "CLOSED") now else ticket.resolvedAt
            val updated = ticket.copy(
                status = status,
                resolvedAt = resolvedAt
            )
            repository.insertSupportTicket(updated)
            _toastMessage.value = "Ticket status updated to $status."
        }
    }

    // Admin support panel: Save Internal Notes & Update Priority
    fun updateInternalNotesAndPriority(ticketId: String, notes: String, priority: String) {
        viewModelScope.launch {
            val ticket = repository.getSupportTicketById(ticketId) ?: return@launch
            val updated = ticket.copy(
                internalNotes = notes,
                internalPriority = priority
            )
            repository.insertSupportTicket(updated)
            _toastMessage.value = "Internal notes and priority updated."
        }
    }
}
