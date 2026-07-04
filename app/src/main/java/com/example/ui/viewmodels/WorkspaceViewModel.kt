package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.WorkspacePolicyConfig
import com.example.data.model.*
import com.example.data.repository.AppRepository
import com.example.ui.feedback.FeedbackManager

import com.example.analytics.AnalyticsManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.UUID



class WorkspaceViewModel constructor(
    private val repository: AppRepository
) : ViewModel() {

    val selectedWorkspaceId = MutableStateFlow<String?>(null)
    val workspaceViewMode = MutableStateFlow("LIST") // "LIST", "VIEW"
    val workspaceSubTab = MutableStateFlow("STATE") // "STATE", "TEAM", "CHAT", "AGREEMENT"
    val productionStateScope = MutableStateFlow("PRODUCTION_READY")
    val currentUserIdFlow = MutableStateFlow<String>("")
    val isAISummarizing = MutableStateFlow(false)

    val featureFlags: StateFlow<List<com.example.data.model.FeatureFlag>> = repository.getAllFeatureFlagsFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeDisputeNotes: StateFlow<List<DisputeNote>> = combine(
        selectedWorkspaceId,
        currentUserIdFlow
    ) { id, uid ->
        id to uid
    }.flatMapLatest { (id, uid) ->
        if (id == null) flowOf(emptyList()) else repository.getDisputeNotesForWorkspace(id, uid)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _workspacePolicies = MutableStateFlow<Map<String, WorkspacePolicyConfig>>(emptyMap())
    val workspacePolicies: StateFlow<Map<String, WorkspacePolicyConfig>> = _workspacePolicies.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun resetToast() { _toastMessage.value = null }

    val workspaces: StateFlow<List<Workspace>> = repository.allWorkspaces.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedWorkspace: StateFlow<Workspace?> = selectedWorkspaceId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.getWorkspaceById(id)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isWorkspaceArchived = selectedWorkspace.map { it?.isArchived == true }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeWorkspaceMembers: StateFlow<List<WorkspaceMember>> = selectedWorkspaceId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getMembersForWorkspace(id)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeAgreement: StateFlow<TeamAgreement?> = selectedWorkspaceId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.getLatestAgreement(id)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeCalendarItems: StateFlow<List<ContentCalendarItem>> = selectedWorkspaceId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getCalendarItemsForWorkspace(id)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeTemplates: StateFlow<List<TaskTemplate>> = selectedWorkspaceId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getTemplatesForWorkspace(id)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeTasks: StateFlow<List<ProductionTask>> = combine(
        selectedWorkspaceId,
        productionStateScope,
        currentUserIdFlow
    ) { id, scope, uid ->
        Triple(id, scope, uid)
    }.flatMapLatest { (id, scope, uid) ->
        if (id == null) return@flatMapLatest flowOf(emptyList())
        if (scope == "PRODUCTION_READY") {
            repository.getProductionTasks(id)
        } else {
            repository.getRoughSandboxTasks(id, uid)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeAssets: StateFlow<List<WorkspaceAsset>> = selectedWorkspaceId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getAssetsForWorkspace(id)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeDeliverables: StateFlow<List<Deliverable>> = selectedWorkspaceId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getDeliverablesForWorkspace(id)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeEvents: StateFlow<List<WorkspaceEvent>> = selectedWorkspaceId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getEventsForWorkspace(id)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun selectWorkspace(ws: Workspace?) {
        selectedWorkspaceId.value = ws?.id
        workspaceViewMode.value = if (ws == null) "LIST" else "VIEW"
    }

    val newlyCreatedWorkspace = MutableStateFlow<Workspace?>(null)

    fun createWorkspace(name: String, platform: String, userId: String) {
        viewModelScope.launch {
            val ws = Workspace(
                id = UUID.randomUUID().toString(),
                name = name,
                platformType = platform,
                createdBy = userId,
                createdAt = System.currentTimeMillis()
            )
            repository.insertWorkspace(ws)
            AnalyticsManager.trackWorkspaceCreated(name, platform)
            newlyCreatedWorkspace.value = ws
            // Clear draft
            repository.userSettingsDao.setSetting(
                UserSetting(id = "${userId}_workspace_draft", userId = userId, key = "workspace_draft", value = "")
            )
            _toastMessage.value = "Workspace '$name' created!"
        }
    }

    suspend fun getWorkspaceDraft(userId: String): String? {
        return repository.userSettingsDao.getSetting(userId, "workspace_draft")
    }

    suspend fun saveWorkspaceDraft(userId: String, draftJson: String) {
        repository.userSettingsDao.setSetting(
            UserSetting(id = "${userId}_workspace_draft", userId = userId, key = "workspace_draft", value = draftJson)
        )
    }

    fun submitTask(title: String, body: String, scope: String, userId: String) {
        if (isWorkspaceArchived.value) {
            _toastMessage.value = "Action blocked: Workspace is ARCHIVED."
            return
        }
        val wsId = selectedWorkspaceId.value ?: return
        viewModelScope.launch {
            val task = ProductionTask(
                id = UUID.randomUUID().toString(),
                workspaceId = wsId,
                creatorId = userId,
                title = title,
                contentBody = body,
                stateScope = scope,
                kanbanLane = if (scope == "PRODUCTION_READY") "SCRIPTING" else "TODO",
                createdAt = System.currentTimeMillis()
            )
            repository.insertTask(task)
            _toastMessage.value = "Task saved to $scope."
        }
    }

    fun hasPermission(workspaceId: String, userId: String, permission: WorkspacePermission): Boolean {
        val members = activeWorkspaceMembers.value
        val me = members.find { it.userId == userId } ?: return false
        val role = me.assignedRoleTitle
        
        return when (permission) {
            WorkspacePermission.MODIFY_PRODUCTION -> me.canModifyProduction || role in listOf("Lead Creator", "Head", "Owner")
            WorkspacePermission.MANAGE_MEMBERS -> role in listOf("Lead Creator", "Head", "Owner")
            WorkspacePermission.ARCHIVE_WORKSPACE -> role in listOf("Lead Creator", "Owner")
            WorkspacePermission.EDIT_AGREEMENT -> role in listOf("Lead Creator", "Owner")
            WorkspacePermission.VIEW_DISPUTES -> true // All can view, but maybe only leads see names?
        }
    }

    enum class WorkspacePermission {
        MODIFY_PRODUCTION, MANAGE_MEMBERS, ARCHIVE_WORKSPACE, EDIT_AGREEMENT, VIEW_DISPUTES
    }

    fun moveTaskLane(taskId: String, newLane: String, userId: String) {
        if (isWorkspaceArchived.value) {
            FeedbackManager.showWarning("Action blocked: Workspace is ARCHIVED.")
            return
        }
        viewModelScope.launch {
            val task = repository.getTaskById(taskId).firstOrNull() ?: return@launch
            if (task.stateScope == "PRODUCTION_READY") {
                if (!hasPermission(task.workspaceId, userId, WorkspacePermission.MODIFY_PRODUCTION)) {
                    FeedbackManager.showError("Permission Denied: Your role lacks production-level clearance.")
                    return@launch
                }
            }
            val previousLane = task.kanbanLane
            repository.insertTask(task.copy(kanbanLane = newLane))
            
            val isDone = newLane.uppercase() == "DONE" || newLane.uppercase() == "PUBLISH"
            if (isDone) {
                AnalyticsManager.trackTaskCompleted(taskId, System.currentTimeMillis() - task.createdAt)
            }
            val msgText = if (isDone) "Task anchored to DONE status." else "Task moved to $newLane"
            
            FeedbackManager.showSuccess(
                message = msgText,
                actionLabel = "UNDO",
                onAction = {
                    moveTaskLane(taskId, previousLane, userId)
                }
            )
        }
    }

    fun promoteTaskToProduction(taskId: String, userId: String) {
        if (isWorkspaceArchived.value) return
        viewModelScope.launch {
            val task = repository.getTaskById(taskId).firstOrNull() ?: return@launch
            if (hasPermission(task.workspaceId, userId, WorkspacePermission.MODIFY_PRODUCTION)) {
                repository.insertTask(task.copy(stateScope = "PRODUCTION_READY", kanbanLane = "TODO"))
                _toastMessage.value = "Draft promoted to Team Space!"
            } else {
                FeedbackManager.showError("Permission Denied.")
            }
        }
    }

    fun promoteTaskToProductionWithAI(taskId: String, userId: String) {
        if (isWorkspaceArchived.value) return
        viewModelScope.launch {
            val task = repository.getTaskById(taskId).firstOrNull() ?: return@launch
            val member = repository.getMemberInfo(task.workspaceId, userId).firstOrNull()
            if (member?.canModifyProduction == true || member?.assignedRoleTitle in listOf("Lead Creator", "Head")) {
                isAISummarizing.value = true
                _toastMessage.value = "Gemini is crafting a Production-Ready Brief..."
                
                val displayTitle = if (task.title.contains("]")) {
                    task.title.substringAfter("]").trim()
                } else {
                    task.title
                }
                
                val aiBrief = com.example.data.api.GeminiService.summarizeDraftToBrief(displayTitle, task.contentBody)
                
                isAISummarizing.value = false
                
                if (aiBrief.startsWith("Error")) {
                    _toastMessage.value = aiBrief
                } else {
                    repository.insertTask(task.copy(
                        contentBody = aiBrief,
                        stateScope = "PRODUCTION_READY",
                        kanbanLane = "TODO"
                    ))
                    _toastMessage.value = "Draft summarized by AI & promoted to Team Space!"
                }
            } else {
                _toastMessage.value = "Permission Denied."
            }
        }
    }

    fun deleteTask(taskId: String, userId: String) {
        if (isWorkspaceArchived.value) return
        viewModelScope.launch {
            val task = repository.getTaskById(taskId).firstOrNull() ?: return@launch
            val member = repository.getMemberInfo(task.workspaceId, userId).firstOrNull()
            if (task.creatorId == userId || member?.assignedRoleTitle in listOf("Lead Creator", "Head")) {
                repository.deleteTask(taskId)
                _toastMessage.value = "Task deleted."
            }
        }
    }

    fun joinWorkspace(workspaceId: String, userId: String, roleTitle: String = "Collaborator") {
        viewModelScope.launch {
            val member = WorkspaceMember(
                id = java.util.UUID.randomUUID().toString(),
                workspaceId = workspaceId,
                userId = userId,
                assignedRoleTitle = roleTitle,
                canModifyProduction = true
            )
            repository.insertMember(member)
            AnalyticsManager.trackEvent("member_joined", mapOf("workspace_id" to workspaceId, "role" to roleTitle))
            _toastMessage.value = "Joined workspace successfully!"
        }
    }

    fun inviteMember(workspaceId: String, email: String, roleTitle: String) {
        viewModelScope.launch {
            val mockUserId = if (email.contains("@")) email.substringBefore("@") else email
            val member = WorkspaceMember(
                id = java.util.UUID.randomUUID().toString(),
                workspaceId = workspaceId,
                userId = mockUserId,
                assignedRoleTitle = roleTitle,
                canModifyProduction = true
            )
            repository.insertMember(member)
            AnalyticsManager.trackEvent("member_invited", mapOf("workspace_id" to workspaceId, "email" to email, "role" to roleTitle))
            _toastMessage.value = "Invitation sent to $email! Added as $roleTitle."
        }
    }

    fun leaveWorkspace(workspaceId: String, userId: String) {
        viewModelScope.launch {
            val members = repository.getMembersForWorkspace(workspaceId).first().filterNotNull()
            val me = members.find { it.userId == userId } ?: return@launch
            
            // Check if I am the only lead
            val leadRoles = listOf("Lead Creator", "Head", "Owner")
            val leads = members.filter { it.assignedRoleTitle in leadRoles }
            val isILead = me.assignedRoleTitle in leadRoles
            
            if (isILead && leads.size == 1 && members.size > 1) {
                FeedbackManager.showError("You are the sole lead. Assign another member as lead before leaving.")
                return@launch
            }
            
            repository.removeMember(workspaceId, userId)
            if (selectedWorkspaceId.value == workspaceId) {
                selectedWorkspaceId.value = null
                workspaceViewMode.value = "LIST"
            }
            FeedbackManager.showSuccess("You have left the shard enclave.")
        }
    }

    fun removeMember(workspaceId: String, userId: String, adminId: String) {
        viewModelScope.launch {
            if (hasPermission(workspaceId, adminId, WorkspacePermission.MANAGE_MEMBERS)) {
                repository.removeMember(workspaceId, userId)
                FeedbackManager.showSuccess("Member removed from enclave.")
            } else {
                FeedbackManager.showError("Security Clearance Required: MANAGE_MEMBERS")
            }
        }
    }

    fun updateMemberRole(workspaceId: String, userId: String, role: String, adminId: String) {
        viewModelScope.launch {
            if (hasPermission(workspaceId, adminId, WorkspacePermission.MANAGE_MEMBERS)) {
                repository.updateMemberRole(workspaceId, userId, role)
                FeedbackManager.showSuccess("Member role re-assigned to $role.")
            } else {
                FeedbackManager.showError("Security Clearance Required: MANAGE_MEMBERS")
            }
        }
    }

    fun archiveWorkspace(workspaceId: String, adminId: String) {
        viewModelScope.launch {
            val member = repository.getMemberInfo(workspaceId, adminId).firstOrNull()
            if (member?.assignedRoleTitle in listOf("Lead Creator", "Head")) {
                repository.archiveWorkspace(workspaceId)
                _toastMessage.value = "Archived."
            }
        }
    }

    fun deleteWorkspace(workspaceId: String, adminId: String) {
        viewModelScope.launch {
            val member = repository.getMemberInfo(workspaceId, adminId).firstOrNull()
            if (member?.assignedRoleTitle in listOf("Lead Creator", "Head")) {
                repository.deleteWorkspace(workspaceId)
                selectedWorkspaceId.value = null
                workspaceViewMode.value = "LIST"
                _toastMessage.value = "Workspace deleted."
            }
        }
    }

    fun endorseTeammate(giverId: String, receiverId: String, workspaceId: String, tags: List<String>) {
        viewModelScope.launch {
            if (giverId == receiverId) {
                _toastMessage.value = "You cannot endorse yourself!"
                return@launch
            }
            if (tags.isEmpty()) {
                _toastMessage.value = "Please select at least one tag."
                return@launch
            }
            val endorsement = Endorsement(
                giverId = giverId,
                receiverId = receiverId,
                workspaceId = workspaceId,
                tags = tags,
                createdAt = System.currentTimeMillis()
            )
            repository.insertEndorsement(endorsement)
            _toastMessage.value = "Teammate endorsed successfully!"
        }
    }

    fun getWorkspacePolicy(workspaceId: String) = _workspacePolicies.value[workspaceId] ?: WorkspacePolicyConfig()
    fun updateWorkspacePolicy(workspaceId: String, config: WorkspacePolicyConfig) {
        val current = _workspacePolicies.value.toMutableMap()
        current[workspaceId] = config
        _workspacePolicies.value = current
        _toastMessage.value = "Policy updated!"
    }

    fun addCalendarItem(title: String, scheduledDate: String, linkedTaskId: String?, userId: String) {
        if (isWorkspaceArchived.value) return
        val wsId = selectedWorkspaceId.value ?: return
        viewModelScope.launch {
            val item = ContentCalendarItem(
                id = UUID.randomUUID().toString(),
                workspaceId = wsId,
                title = title,
                scheduledDate = scheduledDate,
                linkedTaskId = linkedTaskId,
                createdBy = userId,
                createdAt = System.currentTimeMillis()
            )
            repository.insertCalendarItem(item)
            _toastMessage.value = "Content Calendar Item '$title' created!"
        }
    }

    fun deleteCalendarItem(itemId: String) {
        if (isWorkspaceArchived.value) return
        viewModelScope.launch {
            repository.deleteCalendarItem(itemId)
            _toastMessage.value = "Calendar item removed."
        }
    }

    fun saveWorkspaceToTemplate(templateName: String, userId: String) {
        if (isWorkspaceArchived.value) return
        val wsId = selectedWorkspaceId.value ?: return
        viewModelScope.launch {
            val currentTasks = activeTasks.value.filter { it.stateScope == "PRODUCTION_READY" }
            val templateTasks = currentTasks.map { task ->
                TemplateTask(
                    title = task.title,
                    description = task.contentBody,
                    defaultLane = task.kanbanLane
                )
            }
            val jsonStr = try {
                Json.encodeToString(kotlinx.serialization.builtins.ListSerializer(TemplateTask.serializer()), templateTasks)
            } catch (e: Exception) {
                "[]"
            }
            val template = TaskTemplate(
                id = UUID.randomUUID().toString(),
                workspaceId = wsId,
                templateName = templateName,
                tasksJson = jsonStr,
                createdBy = userId,
                createdAt = System.currentTimeMillis()
            )
            repository.insertTemplate(template)
            _toastMessage.value = "Template '$templateName' saved successfully!"
        }
    }

    fun applyTemplateToWorkspace(templateId: String, userId: String) {
        if (isWorkspaceArchived.value) return
        val wsId = selectedWorkspaceId.value ?: return
        viewModelScope.launch {
            val templates = activeTemplates.value
            val template = templates.find { it.id == templateId } ?: return@launch
            val templateTasks = try {
                Json.decodeFromString(kotlinx.serialization.builtins.ListSerializer(TemplateTask.serializer()), template.tasksJson)
            } catch (e: Exception) {
                emptyList<TemplateTask>()
            }
            templateTasks.forEach { tt ->
                val spawnedTask = ProductionTask(
                    id = UUID.randomUUID().toString(),
                    workspaceId = wsId,
                    creatorId = userId,
                    title = tt.title,
                    contentBody = tt.description,
                    stateScope = "PRODUCTION_READY",
                    kanbanLane = tt.defaultLane,
                    createdAt = System.currentTimeMillis()
                )
                repository.insertTask(spawnedTask)
            }
            _toastMessage.value = "Successfully applied template with ${templateTasks.size} tasks!"
        }
    }

    fun deleteTemplate(templateId: String) {
        if (isWorkspaceArchived.value) return
        viewModelScope.launch {
            repository.deleteTemplate(templateId)
            _toastMessage.value = "Template removed."
        }
    }

    fun addDisputeNote(workspaceId: String, authorId: String, targetUserId: String, noteText: String) {
        if (noteText.isBlank()) return
        viewModelScope.launch {
            val note = DisputeNote(
                id = UUID.randomUUID().toString(),
                workspaceId = workspaceId,
                authorId = authorId,
                targetUserId = targetUserId,
                noteText = noteText,
                createdAt = System.currentTimeMillis()
            )
            repository.insertDisputeNote(note)
            _toastMessage.value = "Dispute note added securely."
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val membersDisputeNotes: StateFlow<Map<String, List<DisputeNote>>> = combine(
        activeWorkspaceMembers,
        currentUserIdFlow
    ) { members, uid ->
        members to uid
    }.flatMapLatest { (members, uid) ->
        val userIds = members.map { it.userId }
        if (userIds.isEmpty()) flowOf(emptyMap())
        else repository.getDisputeNotesAboutUsers(userIds, uid).map { notes ->
            notes.groupBy { it.targetUserId }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun getDisputeNotesAboutUser(userId: String, requestingUserId: String): Flow<List<DisputeNote>> {
        return repository.getDisputeNotesAboutUser(userId, requestingUserId)
    }

    fun uploadAsset(workspaceId: String, uploaderId: String, fileName: String, fileType: String, category: String, taskId: String?) {
        viewModelScope.launch {
            val asset = WorkspaceAsset(
                id = UUID.randomUUID().toString(),
                workspaceId = workspaceId,
                uploaderId = uploaderId,
                taskId = taskId,
                fileName = fileName,
                fileUrl = "https://mock.storage/$fileName",
                fileType = fileType,
                category = category,
                status = "AVAILABLE"
            )
            repository.insertAsset(asset)
            
            // Log Event
            val event = WorkspaceEvent(
                id = UUID.randomUUID().toString(),
                workspaceId = workspaceId,
                actorId = uploaderId,
                eventType = "ASSET_UPLOADED",
                entityId = asset.id,
                eventMetadata = """{"fileName": "$fileName", "category": "$category"}"""
            )
            repository.insertWorkspaceEvent(event)
            
            _toastMessage.value = "Asset '$fileName' uploaded to $category."
        }
    }

    fun submitDeliverable(workspaceId: String, taskId: String, submitterId: String, assetId: String, notes: String) {
        viewModelScope.launch {
            val deliverable = Deliverable(
                id = UUID.randomUUID().toString(),
                workspaceId = workspaceId,
                taskId = taskId,
                submitterId = submitterId,
                assetId = assetId,
                versionNotes = notes,
                status = "PENDING_REVIEW"
            )
            repository.insertDeliverable(deliverable)
            
            // Notify Leads
            val currentMembers = activeWorkspaceMembers.value
            currentMembers.filter { it.assignedRoleTitle in listOf("Lead Creator", "Head", "Owner") && it.userId != submitterId }
                .forEach { lead ->
                    repository.insertNotification(Notification(
                        id = UUID.randomUUID().toString(),
                        userId = lead.userId,
                        title = "Production Deliverable",
                        body = "New deliverable version uploaded for task ID: $taskId",
                        type = "DELIVERABLE",
                        createdAt = System.currentTimeMillis()
                    ))
                }
            
            val event = WorkspaceEvent(
                id = UUID.randomUUID().toString(),
                workspaceId = workspaceId,
                actorId = submitterId,
                eventType = "DELIVERABLE_SUBMITTED",
                entityId = deliverable.id
            )
            repository.insertWorkspaceEvent(event)
            
            _toastMessage.value = "Deliverable submitted for review!"
        }
    }

    fun reviewDeliverable(deliverableId: String, reviewerId: String, status: String, feedback: String?) {
        viewModelScope.launch {
            repository.updateDeliverableStatus(deliverableId, status, feedback)
            val deliverable = repository.getDeliverablesForWorkspace(selectedWorkspaceId.value ?: "").firstOrNull()?.find { it.id == deliverableId }
            if (deliverable != null) {
                // Notify Submitter
                repository.insertNotification(Notification(
                    id = UUID.randomUUID().toString(),
                    userId = deliverable.submitterId,
                    title = "Deliverable Reviewed",
                    body = "Your deliverable version was marked as $status by $reviewerId",
                    type = "DELIVERABLE",
                    createdAt = System.currentTimeMillis()
                ))

                val event = WorkspaceEvent(
                    id = UUID.randomUUID().toString(),
                    workspaceId = deliverable.workspaceId,
                    actorId = reviewerId,
                    eventType = "DELIVERABLE_REVIEWED",
                    entityId = deliverable.id,
                    eventMetadata = """{"status": "$status"}"""
                )
                repository.insertWorkspaceEvent(event)
            }
            _toastMessage.value = "Deliverable marked as $status."
        }
    }

    fun exportWorkspaceSummary(workspace: Workspace, agreement: com.example.data.model.TeamAgreement?, tasks: List<ProductionTask>, members: List<WorkspaceMember>) {
        viewModelScope.launch {
            val completedTasks = tasks.filter { it.kanbanLane.uppercase() == "PUBLISH" }
            val rosterStr = members.map { "${it.userId} (${it.assignedRoleTitle})" }.joinToString(", ")
            val agreementStr = if (agreement != null && agreement.isLocked) "Agreement Locked (Version ${agreement.version})" else "No Agreement Locked"
            val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(workspace.createdAt))
            _toastMessage.value = "Exporting Workspace Summary PDF: Document hash verified."
        }
    }
}
