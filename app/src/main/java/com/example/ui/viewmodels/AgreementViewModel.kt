package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AgreementAcknowledgment
import com.example.data.model.TeamAgreement
import com.example.data.repository.AppRepository
import com.example.analytics.AnalyticsManager

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID



class AgreementViewModel constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun resetToast() { _toastMessage.value = null }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getActiveAgreement(workspaceId: String): Flow<TeamAgreement?> {
        return repository.getLatestAgreement(workspaceId)
    }

    fun getActiveAgreementFromFlow(workspaceId: Flow<String?>): StateFlow<TeamAgreement?> {
        return workspaceId.flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.getLatestAgreement(id)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAcknowledgments(agreementId: String?): Flow<List<AgreementAcknowledgment>> {
        if (agreementId == null) return flowOf(emptyList())
        return repository.getAcknowledgmentsFlow(agreementId)
    }

    fun getAcknowledgmentsFromFlow(agreement: StateFlow<TeamAgreement?>): StateFlow<List<AgreementAcknowledgment>> {
        return agreement.flatMapLatest { a ->
            if (a == null) flowOf(emptyList()) else repository.getAcknowledgmentsFlow(a.id)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAgreementHistory(workspaceId: String): Flow<List<TeamAgreement>> {
        return repository.getAllAgreementsForWorkspace(workspaceId)
    }

    fun createTeamAgreement(workspaceId: String, content: String) {
        viewModelScope.launch {
            val agreement = TeamAgreement(
                id = UUID.randomUUID().toString(),
                workspaceId = workspaceId,
                contentText = content,
                createdAt = System.currentTimeMillis()
            )
            repository.createAgreement(agreement)
            AnalyticsManager.trackEvent("agreement_drafted", mapOf("workspace_id" to workspaceId))
            _toastMessage.value = "Agreement created."
        }
    }

    fun updateTeamAgreement(workspaceId: String, content: String) {
        viewModelScope.launch {
            val latest = repository.getLatestAgreement(workspaceId).firstOrNull()
            val nextVersion = (latest?.version ?: 1) + 1
            val agreement = TeamAgreement(
                id = UUID.randomUUID().toString(),
                workspaceId = workspaceId,
                version = nextVersion,
                contentText = content,
                createdAt = System.currentTimeMillis()
            )
            repository.createAgreement(agreement)
            _toastMessage.value = "Agreement updated to version $nextVersion. All team signatures reset."
        }
    }

    fun updateSplits(workspaceId: String, leadPercent: Int, editorPercent: Int, vfxPercent: Int) {
        viewModelScope.launch {
            val latest = repository.getLatestAgreement(workspaceId).firstOrNull()
            val baseContent = latest?.contentText ?: """
                [PREAMBLE]
                This Co-Op Production Contract is entered into secure block registers on this day, governing intellectual properties and compliance standards in Shard #$workspaceId.
                
                [SCOPE OF WORK]
                Collaborators shall execute assigned production timeline units in accordance with Kanban milestones. Milestone completion is validated via decentralized peer review.
                
                [IP ASSIGNMENT]
                All generated creative resources, video timeline layers, thumbnail scripts, and associated assets are held in secure Co-Op commons with a joint-ownership allocation model.
                
                [REVENUE SPLIT]
                Gross earnings from published media streams are allocated dynamically based on verified contribution points: Lead Director (40%), Editors (30%), VFX Specialists (30%).
            """.trimIndent()

            val updatedRevenueLine = "Gross earnings from published media streams are allocated dynamically based on verified contribution points: Lead Director ($leadPercent%), Editors ($editorPercent%), VFX Specialists ($vfxPercent%)."
            
            val updatedContent = if (baseContent.contains("[REVENUE SPLIT]")) {
                val parts = baseContent.split("[REVENUE SPLIT]")
                parts[0] + "[REVENUE SPLIT]\n" + updatedRevenueLine
            } else {
                baseContent + "\n\n[REVENUE SPLIT]\n" + updatedRevenueLine
            }

            val nextVersion = (latest?.version ?: 1) + 1
            val agreement = TeamAgreement(
                id = UUID.randomUUID().toString(),
                workspaceId = workspaceId,
                version = nextVersion,
                contentText = updatedContent,
                createdAt = System.currentTimeMillis()
            )
            repository.createAgreement(agreement)
            _toastMessage.value = "Revenue splits updated to $leadPercent% / $editorPercent% / $vfxPercent%. Team signatures reset."
        }
    }

    fun acknowledgeAgreement(agreementId: String, contentHash: String, userId: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val agreement = repository.getAgreementById(agreementId) ?: repository.getLatestAgreement(agreementId).firstOrNull()
            if (agreement == null) {
                _toastMessage.value = "Error: Agreement record not found."
                return@launch
            }
            val expectedHash = try {
                val bytes = java.security.MessageDigest.getInstance("SHA-256").digest(agreement.contentText.toByteArray())
                bytes.joinToString("") { "%02x".format(it) }
            } catch (e: Exception) {
                agreement.contentText.hashCode().toString()
            }

            if (expectedHash != contentHash && agreement.contentText.hashCode().toString() != contentHash && contentHash != "HASH_ERR" && contentHash.isNotBlank()) {
                // Check if terms changed
                _toastMessage.value = "Signature Rejected: Agreement terms were updated. Please review the latest version."
                return@launch
            }

            val ackId = "${agreementId}_${userId}"
            val ack = AgreementAcknowledgment(
                id = ackId,
                agreementId = agreementId,
                userId = userId,
                acknowledgmentHash = contentHash,
                acknowledgedAt = System.currentTimeMillis()
            )
            repository.insertAcknowledgment(ack)
            
            // Write Audit Log
            repository.insertAuditLog(com.example.data.model.AuditLog(
                id = UUID.randomUUID().toString(),
                adminId = userId,
                adminName = "Creator",
                actionTaken = "AGREEMENT_SIGNED",
                targetType = "AGREEMENT",
                targetId = agreementId,
                reason = "User '$userId' digitally signed agreement hash '$contentHash'.",
                createdAt = System.currentTimeMillis()
            ))

            // Notify Workspace Lead & Workspace Members
            agreement.let { ag ->
                val workspace = repository.getWorkspaceById(ag.workspaceId).firstOrNull()
                workspace?.let { ws ->
                    repository.insertNotification(com.example.data.model.Notification(
                        id = UUID.randomUUID().toString(),
                        userId = ws.createdBy,
                        title = "Agreement Signed by ${userId.take(8)}",
                        body = "Team agreement '${ag.title}' has been digitally signed.",
                        type = "AGREEMENT_SIGNED",
                        createdAt = System.currentTimeMillis()
                    ))

                    // Auto-lock agreement and post message in workspace chat
                    val updatedAgreement = ag.copy(isLocked = true)
                    repository.createAgreement(updatedAgreement)

                    repository.insertMessage(com.example.data.model.Message(
                        id = UUID.randomUUID().toString(),
                        workspaceId = ws.id,
                        senderId = "system",
                        senderName = "Vault Bot",
                        messageBody = "Agreement '${ag.title}' was signed & executed by ${userId.take(8)}. Terms are now active.",
                        timestamp = System.currentTimeMillis()
                    ))
                }
            }

            AnalyticsManager.trackAgreementSigned(agreementId)
            _toastMessage.value = "Agreement signed & logged in system audit vault."
        }
    }

    fun getAcknowledgmentsForUser(userId: String): Flow<List<AgreementAcknowledgment>> {
        return repository.getAcknowledgmentsForUser(userId)
    }

    fun getAllAgreementsFlow(): Flow<List<TeamAgreement>> {
        return repository.getAllAgreementsFlow()
    }

    fun getLatestAgreementFlow(workspaceId: String): Flow<TeamAgreement?> {
        return repository.getLatestAgreement(workspaceId)
    }

    fun notifyLeadCreator(workspaceId: String, userId: String) {
        viewModelScope.launch {
            val workspace = repository.getWorkspaceById(workspaceId).firstOrNull()
            if (workspace != null) {
                repository.insertNotification(com.example.data.model.Notification(
                    id = UUID.randomUUID().toString(),
                    userId = workspace.createdBy,
                    title = "Agreement Required",
                    body = "Contributor '$userId' requested that the Team Agreement draft be initialized.",
                    type = "AGREEMENT_REQUIRED",
                    createdAt = System.currentTimeMillis()
                ))
                _toastMessage.value = "Lead creator notified successfully!"
            } else {
                _toastMessage.value = "Workspace not found."
            }
        }
    }

    fun exportAgreementAsPDF(context: android.content.Context, agreementText: String, title: String = "Co-Op Agreement") {
        viewModelScope.launch {
            try {
                val sendIntent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    putExtra(android.content.Intent.EXTRA_TITLE, title)
                    putExtra(android.content.Intent.EXTRA_TEXT, "--- $title ---\n\n$agreementText")
                    type = "text/plain"
                }
                val chooser = android.content.Intent.createChooser(sendIntent, "Export $title")
                context.startActivity(chooser)
                _toastMessage.value = "Agreement exported successfully!"
            } catch (e: Exception) {
                _toastMessage.value = "Failed to export: ${e.message}"
            }
        }
    }
}
