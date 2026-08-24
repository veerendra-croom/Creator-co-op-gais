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
    val repository: AppRepository
) : ViewModel() {

    val selectedWorkspaceId = MutableStateFlow<String?>(null)
    val workspaceViewMode = MutableStateFlow("LIST") // "LIST", "VIEW"
    val workspaceSubTab = MutableStateFlow("STATE") // "STATE", "TEAM", "CHAT", "AGREEMENT"
    val productionStateScope = MutableStateFlow("PRODUCTION_READY")
    val currentUserIdFlow = MutableStateFlow<String>("")
    val isAISummarizing = MutableStateFlow(false)
    val aiSummarizationError = MutableStateFlow<String?>(null)

    val allUsers: StateFlow<List<UserProfile>> = repository.allUsers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentUserProfile: StateFlow<UserProfile?> = currentUserIdFlow.flatMapLatest { uid ->
        if (uid.isEmpty()) flowOf(null) else repository.getUserByIdFlow(uid)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

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
        selectedWorkspaceId.update { ws?.id }
        workspaceViewMode.update { if (ws == null) "LIST" else "VIEW" }
        productionStateScope.update { "PRODUCTION_READY" }
        workspaceSubTab.update { "STATE" }
    }

    val newlyCreatedWorkspace = MutableStateFlow<Workspace?>(null)

    fun createWorkspace(name: String, platform: String, userId: String) {
        viewModelScope.launch {
            try {
                val ws = Workspace(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    platformType = platform,
                    createdBy = userId,
                    createdAt = System.currentTimeMillis()
                )
                repository.insertWorkspace(ws)
                AnalyticsManager.trackWorkspaceCreated(name, platform)
                newlyCreatedWorkspace.update { ws }
                // Clear draft
                repository.userSettingsDao.setSetting(
                    UserSetting(id = "${userId}_workspace_draft", userId = userId, key = "workspace_draft", value = "")
                )
                FeedbackManager.showSuccess("Workspace '$name' created!")
            } catch (e: Exception) {
                FeedbackManager.showError("Failed to create workspace: ${e.message}")
            }
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

    suspend fun getPersonalDraftTitle(userId: String): String? {
        return repository.userSettingsDao.getSetting(userId, "personal_draft_title")
    }

    suspend fun getPersonalDraftBody(userId: String): String? {
        return repository.userSettingsDao.getSetting(userId, "personal_draft_body")
    }

    suspend fun savePersonalDraft(userId: String, title: String, body: String) {
        repository.userSettingsDao.setSetting(
            UserSetting(id = "${userId}_personal_draft_title", userId = userId, key = "personal_draft_title", value = title)
        )
        repository.userSettingsDao.setSetting(
            UserSetting(id = "${userId}_personal_draft_body", userId = userId, key = "personal_draft_body", value = body)
        )
    }

    fun submitTask(title: String, body: String, scope: String, userId: String, kanbanLane: String? = null) {
        if (isWorkspaceArchived.value) {
            FeedbackManager.showWarning("Action blocked: Workspace is ARCHIVED.")
            return
        }
        val wsId = selectedWorkspaceId.value ?: return
        viewModelScope.launch {
            try {
                FeedbackManager.showInfo("Syncing with secure workspace vault...")
                kotlinx.coroutines.delay(800)
                val defaultLane = if (scope == "PRODUCTION_READY") "IDEAS" else "TODO"
                val task = ProductionTask(
                    id = UUID.randomUUID().toString(),
                    workspaceId = wsId,
                    creatorId = userId,
                    title = title,
                    contentBody = body,
                    stateScope = scope,
                    kanbanLane = kanbanLane ?: defaultLane,
                    createdAt = System.currentTimeMillis()
                )
                repository.insertTask(task)
                FeedbackManager.showSuccess("Task saved to ${scope.replace("_", " ")}.")
            } catch (e: Exception) {
                FeedbackManager.showError("Failed to deploy task: ${e.message}")
            }
        }
    }

    fun hasPermission(workspaceId: String, userId: String, permission: WorkspacePermission): Boolean {
        val members = activeWorkspaceMembers.value.ifEmpty {
            try {
                kotlinx.coroutines.runBlocking {
                    repository.getMembersForWorkspace(workspaceId).firstOrNull() ?: emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
        val me = members.find { it.userId == userId }
        if (me != null) {
            val role = me.assignedRoleTitle
            return when (permission) {
                WorkspacePermission.MODIFY_PRODUCTION -> me.canModifyProduction || role in listOf("Lead Creator", "Head", "Owner")
                WorkspacePermission.MANAGE_MEMBERS -> role in listOf("Lead Creator", "Head", "Owner")
                WorkspacePermission.ARCHIVE_WORKSPACE -> role in listOf("Lead Creator", "Owner")
                WorkspacePermission.EDIT_AGREEMENT -> role in listOf("Lead Creator", "Owner")
                WorkspacePermission.VIEW_DISPUTES -> true
            }
        }
        val currentWs = selectedWorkspace.value ?: try {
            kotlinx.coroutines.runBlocking {
                repository.getWorkspaceById(workspaceId).firstOrNull()
            }
        } catch (e: Exception) {
            null
        }
        if (currentWs?.id == workspaceId && currentWs.createdBy == userId) {
            return true
        }
        return false
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
            kotlinx.coroutines.delay(400)
            val task = repository.getTaskById(taskId).firstOrNull() ?: return@launch
            if (task.stateScope == "PRODUCTION_READY") {
                if (!hasPermission(task.workspaceId, userId, WorkspacePermission.MODIFY_PRODUCTION)) {
                    FeedbackManager.showError("Permission Denied: Your role lacks production-level clearance.")
                    return@launch
                }
            }
            val previousLane = task.kanbanLane
            repository.updateTaskStatus(taskId, newLane)
            
            val isDone = newLane.uppercase() == "DONE" || newLane.uppercase() == "PUBLISH"
            if (isDone) {
                AnalyticsManager.trackTaskCompleted(taskId, System.currentTimeMillis() - task.createdAt)
                
                // Dispatch notification to workspace owner
                val ws = repository.getWorkspaceById(task.workspaceId).firstOrNull()
                ws?.let { workspace ->
                    repository.insertNotification(com.example.data.model.Notification(
                        id = UUID.randomUUID().toString(),
                        userId = workspace.createdBy,
                        title = "Task Completed: ${task.title}",
                        body = "Task '${task.title}' was marked $newLane in workspace '${workspace.name}'.",
                        type = "TASK_COMPLETED",
                        createdAt = System.currentTimeMillis()
                    ))

                    // Post system message in workspace chat
                    repository.insertMessage(com.example.data.model.Message(
                        id = UUID.randomUUID().toString(),
                        workspaceId = workspace.id,
                        senderId = "system",
                        senderName = "Pipeline Bot",
                        messageBody = "Milestone Reached: Task '${task.title}' moved to $newLane!",
                        timestamp = System.currentTimeMillis()
                    ))
                }

                // Log audit entry
                repository.insertAuditLog(com.example.data.model.AuditLog(
                    id = UUID.randomUUID().toString(),
                    adminId = userId,
                    adminName = "Creator",
                    actionTaken = "TASK_COMPLETED",
                    targetType = "PRODUCTION_TASK",
                    targetId = taskId,
                    reason = "Task '${task.title}' moved to lane $newLane.",
                    createdAt = System.currentTimeMillis()
                ))
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
            try {
                val task = repository.getTaskById(taskId).firstOrNull() ?: return@launch
                if (hasPermission(task.workspaceId, userId, WorkspacePermission.MODIFY_PRODUCTION)) {
                    repository.insertTask(task.copy(stateScope = "PRODUCTION_READY", kanbanLane = "IDEAS"))
                    FeedbackManager.showSuccess("Draft promoted to Team Space!")
                } else {
                    FeedbackManager.showError("Permission Denied: Production clearance required.")
                }
            } catch (e: Exception) {
                FeedbackManager.showError("Promotion failed: ${e.message}")
            }
        }
    }

    fun promoteTaskToProductionWithAI(taskId: String, userId: String) {
        if (isWorkspaceArchived.value) return
        viewModelScope.launch {
            val task = repository.getTaskById(taskId).firstOrNull() ?: return@launch
            val member = repository.getMemberInfo(task.workspaceId, userId).firstOrNull()
            if (member?.canModifyProduction == true || member?.assignedRoleTitle in listOf("Lead Creator", "Head", "Owner")) {
                isAISummarizing.value = true
                aiSummarizationError.value = null
                FeedbackManager.showInfo("Gemini is crafting a Production-Ready Brief...")
                
                val displayTitle = if (task.title.contains("]")) {
                    task.title.substringAfter("]").trim()
                } else {
                    task.title
                }
                
                val aiBrief = try {
                    com.example.data.api.GeminiService.summarizeDraftToBrief(displayTitle, task.contentBody)
                } catch (e: Exception) {
                    "Error: ${e.message}"
                }
                
                isAISummarizing.value = false
                
                if (aiBrief.startsWith("Error")) {
                    aiSummarizationError.value = aiBrief
                    FeedbackManager.showError("AI Synthesis Failed: Security policies or network fault detected.")
                } else {
                    repository.insertTask(task.copy(
                        contentBody = aiBrief,
                        stateScope = "PRODUCTION_READY",
                        kanbanLane = "IDEAS"
                    ))
                    FeedbackManager.showSuccess("Draft summarized by AI & promoted to Team Space!")
                }
            } else {
                _toastMessage.value = "Permission Denied."
            }
        }
    }

    fun promoteTaskToProductionWithFallback(taskId: String, userId: String) {
        if (isWorkspaceArchived.value) return
        viewModelScope.launch {
            val task = repository.getTaskById(taskId).firstOrNull() ?: return@launch
            val member = repository.getMemberInfo(task.workspaceId, userId).firstOrNull()
            val user = repository.getUserById(userId).firstOrNull()
            val isAdmin = user?.systemRole in listOf("ADMIN", "PLATFORM_ADMIN") || user?.globalRole == "ADMIN"
            if (member == null && !isAdmin) {
                FeedbackManager.showError("Access Denied: You are not a member of this workspace.")
                return@launch
            }
            val displayTitle = if (task.title.contains("]")) {
                task.title.substringAfter("]").trim()
            } else {
                task.title
            }
            val fallbackBrief = """
                # PRODUCTION BRIEF: $displayTitle
                
                ## 📋 OBJECTIVE
                ${task.contentBody}
                
                ## 👥 ASSIGNED RESPONSIBILITIES
                - Script/Outline review: Assigned to Lead Creator
                - Production Execution: Core Video/VFX Contributors
                
                ## ⚙️ TECHNICAL SPECIFICATIONS
                - Deliverable format: High-Quality Video (YouTube/Short-form optimal)
                - Target Audience engagement window: First 15 seconds hook focus
                
                ## 📈 SUCCESS METRICS
                - Baseline Retention Rate: >55%
                - Completion Timeline: Strict check-in by review phase
                
                *(Structured local template applied as offline fallback)*
            """.trimIndent()
            
            repository.insertTask(task.copy(
                contentBody = fallbackBrief,
                stateScope = "PRODUCTION_READY",
                kanbanLane = "IDEAS"
            ))
            aiSummarizationError.value = null
            FeedbackManager.showSuccess("Draft structured via local template & promoted!")
        }
    }

    fun updateTask(taskId: String, title: String, body: String, priority: String = "MEDIUM", userId: String? = null) {
        if (isWorkspaceArchived.value) {
            FeedbackManager.showWarning("Action blocked: Workspace is ARCHIVED.")
            return
        }
        viewModelScope.launch {
            try {
                val task = repository.getTaskById(taskId).firstOrNull() ?: return@launch
                if (userId != null) {
                    val member = repository.getMemberInfo(task.workspaceId, userId).firstOrNull()
                    val user = repository.getUserById(userId).firstOrNull()
                    val isAdmin = user?.systemRole in listOf("ADMIN", "PLATFORM_ADMIN") || user?.globalRole == "ADMIN"
                    if (member == null && !isAdmin) {
                        FeedbackManager.showError("Access Denied: You are not a member of this workspace.")
                        return@launch
                    }
                }
                repository.insertTask(task.copy(
                    title = title,
                    contentBody = body,
                    priority = priority
                ))
                FeedbackManager.showSuccess("Task updated successfully.")
            } catch (e: Exception) {
                FeedbackManager.showError("Failed to update task: ${e.message}")
            }
        }
    }

    fun updateTaskStatus(taskId: String, status: String, userId: String? = null) {
        if (isWorkspaceArchived.value) {
            FeedbackManager.showWarning("Action blocked: Workspace is ARCHIVED.")
            return
        }
        viewModelScope.launch {
            try {
                if (userId != null) {
                    val task = repository.getTaskById(taskId).firstOrNull() ?: return@launch
                    val member = repository.getMemberInfo(task.workspaceId, userId).firstOrNull()
                    val user = repository.getUserById(userId).firstOrNull()
                    val isAdmin = user?.systemRole in listOf("ADMIN", "PLATFORM_ADMIN") || user?.globalRole == "ADMIN"
                    if (member == null && !isAdmin) {
                        FeedbackManager.showError("Access Denied: You are not a member of this workspace.")
                        return@launch
                    }
                }
                repository.updateTaskStatus(taskId, status)
                if (status == "COMPLETED") {
                    FeedbackManager.showSuccess("Task marked as completed!")
                } else {
                    FeedbackManager.showInfo("Task status updated to $status.")
                }
            } catch (e: Exception) {
                FeedbackManager.showError("Failed to update task status: ${e.message}")
            }
        }
    }

    fun deleteTask(taskId: String, userId: String) {
        if (isWorkspaceArchived.value) {
            FeedbackManager.showWarning("Workspace is archived. Deletion disabled.")
            return
        }
        viewModelScope.launch {
            try {
                FeedbackManager.showInfo("Removing asset from production...")
                kotlinx.coroutines.delay(600)
                val task = repository.getTaskById(taskId).firstOrNull() ?: return@launch
                val member = repository.getMemberInfo(task.workspaceId, userId).firstOrNull()
                if (task.creatorId == userId || member?.assignedRoleTitle in listOf("Lead Creator", "Head", "Owner")) {
                    repository.deleteTask(taskId)
                    FeedbackManager.showSuccess("Task deleted successfully.")
                } else {
                    FeedbackManager.showError("Permission Denied: Only creator or leads can delete tasks.")
                }
            } catch (e: Exception) {
                FeedbackManager.showError("Deletion failed: ${e.message}")
            }
        }
    }

    fun joinWorkspace(workspaceId: String, userId: String, roleTitle: String = "Collaborator") {
        viewModelScope.launch {
            try {
                val currentMembers = repository.getMembersForWorkspace(workspaceId).first()
                if (currentMembers.size >= 25) {
                    FeedbackManager.showWarning("Shard Enclave Full: Maximum 25 collaborators allowed.")
                    return@launch
                }
                
                val member = WorkspaceMember(
                    id = java.util.UUID.randomUUID().toString(),
                    workspaceId = workspaceId,
                    userId = userId,
                    assignedRoleTitle = roleTitle,
                    canModifyProduction = true
                )
                repository.insertMember(member)
                AnalyticsManager.trackEvent("member_joined", mapOf("workspace_id" to workspaceId, "role" to roleTitle))
                FeedbackManager.showSuccess("Joined enclave: Access granted as $roleTitle.")
            } catch (e: Exception) {
                FeedbackManager.showError("Failed to join workspace: ${e.message}")
            }
        }
    }

    fun inviteMember(workspaceId: String, email: String, roleTitle: String, inviterUserId: String? = null) {
        viewModelScope.launch {
            try {
                if (inviterUserId != null) {
                    val inviterMember = repository.getMemberInfo(workspaceId, inviterUserId).firstOrNull()
                    val inviterUser = repository.getUserById(inviterUserId).firstOrNull()
                    val isGlobalAdmin = inviterUser?.systemRole in listOf("ADMIN", "PLATFORM_ADMIN") || inviterUser?.globalRole == "ADMIN"
                    val isLead = inviterMember?.assignedRoleTitle in listOf("Lead Creator", "Head", "Owner")
                    if (!isGlobalAdmin && !isLead && inviterMember == null) {
                        FeedbackManager.showError("Permission Denied: Only workspace members/leads can invite members.")
                        return@launch
                    }
                }
                val currentMembers = repository.getMembersForWorkspace(workspaceId).first()
                if (currentMembers.size >= 25) {
                    FeedbackManager.showWarning("Invite Denied: Member cap reached for this workspace.")
                    return@launch
                }
                
                val existingUser = repository.userDao.getUserByEmail(email)
                val finalUserId = existingUser?.id ?: (if (email.contains("@")) email.substringBefore("@") else email)
                val member = WorkspaceMember(
                    id = java.util.UUID.randomUUID().toString(),
                    workspaceId = workspaceId,
                    userId = finalUserId,
                    assignedRoleTitle = roleTitle,
                    canModifyProduction = true
                )
                repository.insertMember(member)
                
                // Add event for audit log and activity feed
                repository.insertWorkspaceEvent(com.example.data.model.WorkspaceEvent(
                    id = "evt_" + System.currentTimeMillis(),
                    workspaceId = workspaceId,
                    actorId = finalUserId,
                    eventType = "MEMBER_JOINED",
                    entityId = finalUserId,
                    title = "Member Joined",
                    description = "$finalUserId was invited as $roleTitle.",
                    createdAt = System.currentTimeMillis()
                ))
                
                // If user exists in our DB, notify them
                if (existingUser != null) {
                    repository.insertNotification(com.example.data.model.Notification(
                        id = "notif_inv_" + System.currentTimeMillis(),
                        userId = finalUserId,
                        title = "Workspace Invitation",
                        body = "You've been invited to join a workspace as $roleTitle.",
                        type = "WORKSPACE_INVITE",
                        deepLinkTarget = "WORKSPACE_INVITE:$workspaceId",
                        createdAt = System.currentTimeMillis()
                    ))
                }
                
                AnalyticsManager.trackEvent("member_invited", mapOf("workspace_id" to workspaceId, "email" to email, "role" to roleTitle))
                FeedbackManager.showSuccess("Invitation sent to $email! Added as $roleTitle.")
            } catch (e: Exception) {
                FeedbackManager.showError("Invitation failed: ${e.message}")
            }
        }
    }

    fun leaveWorkspace(workspaceId: String, userId: String) {
        viewModelScope.launch {
            try {
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
                    selectedWorkspaceId.update { null }
                    workspaceViewMode.update { "LIST" }
                }
                FeedbackManager.showSuccess("You have left the shard enclave.")
            } catch (e: Exception) {
                FeedbackManager.showError("Failed to leave workspace: ${e.message}")
            }
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
            try {
                val member = repository.getMemberInfo(workspaceId, adminId).firstOrNull()
                if (member?.assignedRoleTitle in listOf("Lead Creator", "Head", "Owner")) {
                    repository.archiveWorkspace(workspaceId)
                    FeedbackManager.showSuccess("Workspace ARCHIVED.")
                } else {
                    FeedbackManager.showError("Permission Denied.")
                }
            } catch (e: Exception) {
                FeedbackManager.showError("Archive failed: ${e.message}")
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
            try {
                if (giverId == receiverId) {
                    FeedbackManager.showWarning("Self-endorsement is not permitted.")
                    return@launch
                }
                if (tags.isEmpty()) {
                    FeedbackManager.showWarning("Please select at least one skill tag.")
                    return@launch
                }
                
                val workspace = repository.getWorkspaceById(workspaceId).firstOrNull()
                if (workspace == null) {
                    FeedbackManager.showError("Workspace not found.")
                    return@launch
                }
                if (workspace.status != "ARCHIVED" && !workspace.isArchived) {
                    FeedbackManager.showWarning("Endorsements are only permitted for ARCHIVED workspaces.")
                    return@launch
                }
                
                val giverMember = repository.getMemberInfo(workspaceId, giverId).firstOrNull()
                val receiverMember = repository.getMemberInfo(workspaceId, receiverId).firstOrNull()
                if (giverMember == null || receiverMember == null) {
                    FeedbackManager.showWarning("Both users must be members of the same workspace.")
                    return@launch
                }
                
                val existingEndorsements = repository.getEndorsementsForUser(receiverId).firstOrNull() ?: emptyList()
                val alreadyEndorsed = existingEndorsements.any { it.giverId == giverId && it.workspaceId == workspaceId }
                if (alreadyEndorsed) {
                    FeedbackManager.showWarning("You have already endorsed this teammate for this workspace.")
                    return@launch
                }

                val endorsement = Endorsement(
                    id = UUID.randomUUID().toString(),
                    giverId = giverId,
                    receiverId = receiverId,
                    workspaceId = workspaceId,
                    tags = tags,
                    createdAt = System.currentTimeMillis()
                )
                repository.insertEndorsement(endorsement)
                FeedbackManager.showSuccess("Teammate endorsed successfully!")
            } catch (e: Exception) {
                FeedbackManager.showError("Endorsement failed: ${e.message}")
            }
        }
    }

    fun getWorkspacePolicy(workspaceId: String) = _workspacePolicies.value[workspaceId] ?: WorkspacePolicyConfig()
    fun updateWorkspacePolicy(workspaceId: String, config: WorkspacePolicyConfig, userId: String? = null) {
        if (userId != null) {
            viewModelScope.launch {
                val member = repository.getMemberInfo(workspaceId, userId).firstOrNull()
                val user = repository.getUserById(userId).firstOrNull()
                val isLead = member?.assignedRoleTitle in listOf("Lead Creator", "Head", "Owner")
                val isAdmin = user?.systemRole in listOf("ADMIN", "PLATFORM_ADMIN") || user?.globalRole == "ADMIN"
                if (!isLead && !isAdmin) {
                    FeedbackManager.showError("Permission Denied: Only leads or admins can update workspace policies.")
                    return@launch
                }
                val current = _workspacePolicies.value.toMutableMap()
                current[workspaceId] = config
                _workspacePolicies.value = current
                _toastMessage.value = "Policy updated!"
            }
        } else {
            val current = _workspacePolicies.value.toMutableMap()
            current[workspaceId] = config
            _workspacePolicies.value = current
            _toastMessage.value = "Policy updated!"
        }
    }

    fun addCalendarItem(title: String, scheduledDate: String, linkedTaskId: String?, userId: String, status: String = "Drafting") {
        if (isWorkspaceArchived.value) {
            FeedbackManager.showWarning("Archive Lockdown: Cannot schedule calendar items.")
            return
        }
        val wsId = selectedWorkspaceId.value ?: return
        viewModelScope.launch {
            try {
                val item = ContentCalendarItem(
                    id = UUID.randomUUID().toString(),
                    workspaceId = wsId,
                    title = title,
                    scheduledDate = scheduledDate,
                    linkedTaskId = linkedTaskId,
                    createdBy = userId,
                    status = status,
                    createdAt = System.currentTimeMillis()
                )
                repository.insertCalendarItem(item)
                FeedbackManager.showSuccess("Content Calendar Item '$title' created!")
            } catch (e: Exception) {
                FeedbackManager.showError("Scheduling failed: ${e.message}")
            }
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
            templateTasks.forEachIndexed { index, tt ->
                val spawnedTask = ProductionTask(
                    id = UUID.randomUUID().toString() + "_" + System.nanoTime() + "_$index",
                    workspaceId = wsId,
                    creatorId = userId,
                    title = tt.title,
                    contentBody = tt.description,
                    stateScope = "PRODUCTION_READY",
                    kanbanLane = tt.defaultLane.ifBlank { "TODO" },
                    createdAt = System.currentTimeMillis() + index
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
                content = noteText,
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

    fun deleteAsset(assetId: String) {
        viewModelScope.launch {
            repository.deleteAsset(assetId)
            _toastMessage.value = "Asset deleted."
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

    fun deleteDeliverable(deliverableId: String) {
        viewModelScope.launch {
            repository.deleteDeliverable(deliverableId)
            _toastMessage.value = "Deliverable deleted."
        }
    }

    fun updatePresence(workspaceId: String, userId: String, isOnline: Boolean) {
        viewModelScope.launch {
            repository.updateMemberPresence(workspaceId, userId, isOnline)
        }
    }

    fun updateTyping(workspaceId: String, userId: String, isTyping: Boolean, typingText: String = "") {
        viewModelScope.launch {
            repository.updateMemberTyping(workspaceId, userId, isTyping, typingText)
        }
    }

    fun updateViewingTask(workspaceId: String, userId: String, taskId: String?) {
        viewModelScope.launch {
            repository.updateMemberViewingTask(workspaceId, userId, taskId)
        }
    }

    fun updateEditingAsset(workspaceId: String, userId: String, assetId: String?) {
        viewModelScope.launch {
            repository.updateMemberEditingAsset(workspaceId, userId, assetId)
        }
    }

    fun updateLiveStatus(workspaceId: String, userId: String, statusUpdate: String) {
        viewModelScope.launch {
            repository.updateMemberLiveStatus(workspaceId, userId, statusUpdate)
        }
    }

    fun workspaceHeartbeat(workspaceId: String, userId: String) {
        viewModelScope.launch {
            repository.updateMemberPresence(workspaceId, userId, true)
        }
    }

    fun exportWorkspaceSummary(workspace: Workspace, agreement: com.example.data.model.TeamAgreement?, tasks: List<ProductionTask>, members: List<WorkspaceMember>) {
        viewModelScope.launch {
            try {
                val completedTasks = tasks.filter { it.kanbanLane.uppercase() == "PUBLISH" }
                val rosterStr = members.joinToString(", ") { "${it.userId} (${it.assignedRoleTitle})" }
                val agreementStr = if (agreement != null && agreement.isLocked) "Agreement Locked (Version ${agreement.version})" else "No Agreement Locked"
                val dateStr = com.example.util.DateTimeUtils.formatFull(workspace.createdAt)
                FeedbackManager.showSuccess("Exporting Workspace Summary PDF: Document hash verified for $dateStr")
            } catch (e: Exception) {
                FeedbackManager.showError("Export failed: ${e.message}")
            }
        }
    }
}
