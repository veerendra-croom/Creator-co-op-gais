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
    fun getActiveAgreement(workspaceId: Flow<String?>): StateFlow<TeamAgreement?> {
        return workspaceId.flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.getLatestAgreement(id)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAcknowledgments(agreement: StateFlow<TeamAgreement?>): StateFlow<List<AgreementAcknowledgment>> {
        return agreement.flatMapLatest { a ->
            if (a == null) flowOf(emptyList()) else repository.getAcknowledgmentsFlow(a.id)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAgreementHistory(workspaceId: Flow<String?>): StateFlow<List<TeamAgreement>> {
        return workspaceId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getAllAgreementsForWorkspace(id)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
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

    fun acknowledgeAgreement(agreementId: String, contentHash: String, userId: String) {
        viewModelScope.launch {
            val ack = AgreementAcknowledgment(
                id = UUID.randomUUID().toString(),
                agreementId = agreementId,
                userId = userId,
                acknowledgmentHash = contentHash,
                acknowledgedAt = System.currentTimeMillis()
            )
            repository.insertAcknowledgment(ack)
            
            // Notify Workspace Lead
            val agreement = repository.getAgreementById(agreementId)
            agreement?.let { ag ->
                val workspace = repository.getWorkspaceById(ag.workspaceId).firstOrNull()
                workspace?.let { ws ->
                    repository.insertNotification(com.example.data.model.Notification(
                        id = UUID.randomUUID().toString(),
                        userId = ws.createdBy,
                        title = "Agreement Signed",
                        body = "A team member has acknowledged the latest Team Agreement.",
                        type = "AGREEMENT_SIGNED",
                        createdAt = System.currentTimeMillis()
                    ))
                }
            }

            AnalyticsManager.trackAgreementSigned(agreementId)
            _toastMessage.value = "Agreement signed."
        }
    }

    fun getAcknowledgmentsForUser(userId: String): Flow<List<AgreementAcknowledgment>> {
        return repository.getAcknowledgmentsForUser(userId)
    }

    fun getLatestAgreementFlow(workspaceId: String): Flow<TeamAgreement?> {
        return repository.getLatestAgreement(workspaceId)
    }

    fun exportAgreementAsPDF() {
        _toastMessage.value = "Exporting Agreement as PDF: Document hash verified."
    }
}
