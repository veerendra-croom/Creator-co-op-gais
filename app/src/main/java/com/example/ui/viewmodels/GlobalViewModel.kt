package com.example.ui.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.SyncState
import com.example.data.model.UserProfile
import com.example.data.repository.AppRepository
import com.example.data.supabase.SupabaseConfig
import com.example.data.supabase.SupabaseRealtimeManager
import com.example.data.supabase.SupabaseSynchronizer
import com.example.ui.feedback.*

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch



class GlobalViewModel constructor(
    private val repository: AppRepository,
    private val application: Application
) : ViewModel() {

    val currentTab = MutableStateFlow("HOME")
    private val tabBackStack = mutableListOf<String>()

    val selectedTaskId = MutableStateFlow<String?>(null)
    val selectedUserId = MutableStateFlow<String?>(null)
    val selectedWorkspaceId = MutableStateFlow<String?>(null)
    val selectedProjectId = MutableStateFlow<String?>(null)

    private val _blockedUsers = MutableStateFlow<Set<String>>(emptySet())
    val blockedUsers: StateFlow<Set<String>> = _blockedUsers.asStateFlow()

    fun blockUser(userId: String) {
        _blockedUsers.update { it + userId }
    }

    fun unblockUser(userId: String) {
        _blockedUsers.update { it - userId }
    }

    fun navigateToTaskDetails(taskId: String) {
        selectedTaskId.value = taskId
        navigateToTab("TASK_DETAILS")
    }

    fun navigateToPublicProfile(userId: String) {
        selectedUserId.value = userId
        navigateToTab("PUBLIC_PROFILE")
    }

    fun navigateToWorkspaceSettings(workspaceId: String) {
        selectedWorkspaceId.value = workspaceId
        navigateToTab("WORKSPACE_SETTINGS")
    }

    fun navigateToPortfolioDetail(projectId: String) {
        selectedProjectId.value = projectId
        navigateToTab("PORTFOLIO_DETAIL")
    }

    fun navigateToTab(tab: String) {
        if (currentTab.value != tab) {
            tabBackStack.remove(tab)
            tabBackStack.add(currentTab.value)
            currentTab.value = tab
        }
    }

    fun navigateBack(): Boolean {
        if (tabBackStack.isNotEmpty()) {
            currentTab.value = tabBackStack.removeAt(tabBackStack.size - 1)
            return true
        }
        return false
    }

    fun handleDeepLink(uri: android.net.Uri?) {
        if (uri == null) return
        val path = uri.path ?: ""
        val host = uri.host ?: ""
        val scheme = uri.scheme ?: ""

        when {
            // Profile link: https://creator-studio.app/u/{userId} or creatorstudio://user/{userId}
            path.startsWith("/u/") -> {
                val uid = path.removePrefix("/u/").trim().removeSuffix("/")
                if (uid.isNotBlank()) {
                    showSplash.value = false
                    navigateToPublicProfile(uid)
                    toastMessage.value = "Viewing Creator Profile ($uid)"
                }
            }
            scheme == "creatorstudio" && (host == "user" || host == "u") -> {
                val uid = path.trim('/').trim()
                if (uid.isNotBlank()) {
                    showSplash.value = false
                    navigateToPublicProfile(uid)
                    toastMessage.value = "Viewing Creator Profile ($uid)"
                }
            }
            // Pitch link: https://creator-studio.app/pitch/{pitchId} or creatorstudio://pitch/{pitchId}
            path.startsWith("/pitch/") -> {
                showSplash.value = false
                navigateToTab("SYNDICATE")
                toastMessage.value = "Navigated to Syndicate Pitch Hub"
            }
            scheme == "creatorstudio" && host == "pitch" -> {
                showSplash.value = false
                navigateToTab("SYNDICATE")
                toastMessage.value = "Navigated to Syndicate Pitch Hub"
            }
            // Role link: https://creator-studio.app/role/{roleId} or creatorstudio://role/{roleId}
            path.startsWith("/role/") -> {
                showSplash.value = false
                navigateToTab("SYNDICATE")
                toastMessage.value = "Viewing Open Co-Op Role"
            }
            scheme == "creatorstudio" && host == "role" -> {
                showSplash.value = false
                navigateToTab("SYNDICATE")
                toastMessage.value = "Viewing Open Co-Op Role"
            }
            // Workspace link: https://creator-studio.app/workspace/{workspaceId}
            path.startsWith("/workspace/") -> {
                val wsId = path.removePrefix("/workspace/").trim().removeSuffix("/")
                if (wsId.isNotBlank()) {
                    showSplash.value = false
                    navigateToWorkspaceSettings(wsId)
                    toastMessage.value = "Connecting to Workspace Hub"
                }
            }
            // Project link: https://creator-studio.app/project/{projectId}
            path.startsWith("/project/") -> {
                val projId = path.removePrefix("/project/").trim().removeSuffix("/")
                if (projId.isNotBlank()) {
                    showSplash.value = false
                    navigateToPortfolioDetail(projId)
                    toastMessage.value = "Opening Portfolio Project"
                }
            }
            // Invite link: https://creator-studio.app/invite/{code}
            path.startsWith("/invite/") -> {
                showSplash.value = false
                navigateToTab("WORKSPACES")
                toastMessage.value = "Co-Op Invitation Link Activated"
            }
        }
    }

    val showSplash = MutableStateFlow(true)
    val showOnboarding = MutableStateFlow(false)
    val showCelebration = MutableStateFlow(false)
    val syncState = MutableStateFlow<SyncState>(SyncState.Synced)
    val toastMessage = MutableStateFlow<String?>(null)

    val tourManager = com.example.ui.tour.GuidedTourManager(repository, viewModelScope)

    fun checkAndStartTour(userId: String, config: com.example.ui.tour.GuidedTourConfig) {
        tourManager.checkAndStartTour(userId, config)
    }

    fun startTour(userId: String, config: com.example.ui.tour.GuidedTourConfig, forceReplay: Boolean = true) {
        if (config.targetTab.isNotEmpty()) {
            navigateToTab(config.targetTab)
        }
        tourManager.startTour(userId, config, forceReplay)
    }

    fun replayTour(userId: String, tourId: String) {
        val config = com.example.ui.tour.GuidedTourRepository.getTourById(tourId)
        if (config != null) {
            startTour(userId, config, forceReplay = true)
        }
    }

    fun nextTourStep(userId: String) {
        tourManager.nextStep(userId)
    }

    fun previousTourStep() {
        tourManager.previousStep()
    }

    fun skipTour(userId: String) {
        tourManager.skipTour(userId)
    }

    fun completeTour(userId: String) {
        tourManager.completeTour(userId)
    }

    fun resetAllTours(userId: String) {
        tourManager.resetAllTours(userId)
        toastMessage.value = "All guided tutorials reset! They will auto-trigger on screen visits."
    }
    
    val themeMode = MutableStateFlow("SYSTEM") // SYSTEM, LIGHT, DARK
    val demoSandboxMode = MutableStateFlow(true)

    fun toggleDemoSandboxMode(enabled: Boolean) {
        viewModelScope.launch {
            demoSandboxMode.value = enabled
            repository.setDemoSandboxPreference(enabled)
            if (enabled) {
                repository.prepopulateIfEmpty(forceSeedDemo = true)
                toastMessage.value = "Demo sandbox data loaded."
            } else {
                repository.clearSandboxData()
                toastMessage.value = "Switched to clean production database."
            }
        }
    }

    val allAdPlacements = repository.allAdPlacements.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val globalSettings = repository.globalAdSettings.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val featureFlags: StateFlow<List<com.example.data.model.FeatureFlag>> = repository.getAllFeatureFlagsFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val platformSettings: StateFlow<com.example.data.model.PlatformSettings> = repository.getPlatformSettingsFlow()
        .map { it ?: com.example.data.model.PlatformSettings() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, com.example.data.model.PlatformSettings())

    val onboardingSlides: StateFlow<List<com.example.data.model.OnboardingSlide>> = repository.getAllOnboardingSlidesFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val welcomeMessages: StateFlow<List<com.example.data.model.WelcomeMessage>> = repository.getAllWelcomeMessagesFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val changelogEntries = repository.changelogDao.getAllChangelogEntries()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val showWhatsNew = MutableStateFlow(false)
    val exportedData = MutableStateFlow<String?>(null)

    fun submitReport(reporterId: String, targetType: String, targetId: String, reason: String, details: String) {
        viewModelScope.launch {
            val report = com.example.data.model.Report(
                id = "rep_" + java.util.UUID.randomUUID().toString().take(8),
                reporterId = reporterId,
                targetType = targetType,
                targetId = targetId,
                reason = "$reason - $details".take(200),
                status = "PENDING",
                createdAt = System.currentTimeMillis()
            )
            repository.submitReport(report)
            repository.insertAuditLog(com.example.data.model.AuditLog(
                id = java.util.UUID.randomUUID().toString(),
                adminId = reporterId,
                adminName = "User",
                actionTaken = "REPORT_SUBMITTED",
                targetType = targetType,
                targetId = targetId,
                reason = "Report logged: $reason ($details)".take(200),
                createdAt = System.currentTimeMillis()
            ))
            toastMessage.value = "Report submitted to Moderation Queue. Thank you for keeping the Co-Op safe!"
        }
    }

    fun isFeatureEnabled(key: String): Boolean {
        return featureFlags.value.find { it.flagKey == key }?.isEnabled ?: true
    }

    fun loadTheme(userId: String) {
        viewModelScope.launch {
            themeMode.value = repository.getThemePreference(userId) ?: "SYSTEM"
        }
    }

    fun setTheme(userId: String, mode: String) {
        viewModelScope.launch {
            themeMode.value = mode
            repository.setThemePreference(userId, mode)
        }
    }

    init {
        viewModelScope.launch {
            demoSandboxMode.value = repository.getDemoSandboxPreference()
            syncState.value = SyncState.Syncing
            repository.prepopulateIfEmpty()
            try {
                if (SupabaseConfig.isNetworkAvailable(application) &&
                    !SupabaseConfig.supabaseUrl.contains("your-project")) {
                    SupabaseSynchronizer.syncDownEverything(application, repository)
                    syncState.value = SyncState.Synced
                } else {
                    syncState.value = SyncState.OfflineSandbox
                }
            } catch (e: Throwable) {
                syncState.value = SyncState.OfflineSandbox
            }

            // Real-time subscriptions
            SupabaseRealtimeManager.subscribeToWorkspaces(repository, viewModelScope)
            SupabaseRealtimeManager.subscribeToWorkspaceMembers(repository, viewModelScope)
            SupabaseRealtimeManager.subscribeToProductionTasks(repository, viewModelScope)
            SupabaseRealtimeManager.subscribeToTeamAgreements(repository, viewModelScope)
            SupabaseRealtimeManager.subscribeToAgreementAcknowledgments(repository, viewModelScope)
            SupabaseRealtimeManager.subscribeToMessages(repository, viewModelScope)
            SupabaseRealtimeManager.subscribeToPosts(repository, viewModelScope)
            SupabaseRealtimeManager.subscribeToComments(repository, viewModelScope)
        }
    }

    private var cachedUserFlow: Pair<Flow<String?>, StateFlow<UserProfile?>>? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeUser(currentUserId: Flow<String?>): StateFlow<UserProfile?> {
        val cached = cachedUserFlow
        if (cached != null && cached.first == currentUserId) {
            return cached.second
        }
        val flow = currentUserId.flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.userDao.getUserById(id)
        }.onEach { user ->
            user?.id?.let { uid ->
                SupabaseRealtimeManager.subscribeToUserData(repository, viewModelScope, uid)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        cachedUserFlow = Pair(currentUserId, flow)
        return flow
    }

    val allWorkspaces = repository.allWorkspaces.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun insertTask(task: com.example.data.model.ProductionTask) {
        viewModelScope.launch {
            repository.insertTask(task)
        }
    }

    fun completeSplash() { showSplash.value = false }
    fun resetToast() { toastMessage.value = null }

    fun checkWhatsNew(userId: String) {
        viewModelScope.launch {
            val lastSeenVersion = repository.getLastSeenVersion(userId)
            val latestEntry = repository.changelogDao.getLatestChangelogEntry()
            if (latestEntry != null && latestEntry.versionName != lastSeenVersion) {
                showWhatsNew.value = true
            }
        }
    }

    fun dismissWhatsNew(userId: String, versionName: String) {
        viewModelScope.launch {
            repository.setLastSeenVersion(userId, versionName)
            showWhatsNew.value = false
        }
    }

    fun purchaseProMonthly(userId: String) {
        viewModelScope.launch {
            repository.setVerifiedPro(userId, true)
            toastMessage.value = "Monthly Pro subscription activated!"
        }
    }

    fun purchaseProAnnual(userId: String) {
        viewModelScope.launch {
            repository.setVerifiedPro(userId, true)
            toastMessage.value = "Annual Pro subscription activated! Best choice."
        }
    }

    fun exportUserData(userId: String) {
        viewModelScope.launch {
            exportedData.value = repository.exportUserData(userId)
            toastMessage.value = "Data export generated!"
        }
    }

    fun getEndorsementsForUser(userId: String): Flow<List<com.example.data.model.Endorsement>> {
        return repository.getEndorsementsForUser(userId)
    }

    fun getCompletedWorkspacesCountForUser(userId: String): Flow<Int> {
        return repository.getCompletedWorkspacesCountForUser(userId)
    }

    fun getUserById(userId: String): Flow<UserProfile?> {
        return repository.userDao.getUserById(userId)
    }
    
    val allUsers: Flow<List<UserProfile>> = repository.userDao.getAllUsers()
    val allWorkspaceMembers = repository.getAllWorkspaceMembersFlow().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val allProjectProposals: Flow<List<com.example.data.model.ProjectProposalWithData>> = repository.allProjectProposals
    val lookingForWorkListings: Flow<List<com.example.data.model.LookingForWork>> = repository.allActiveWorkListings
    
    fun getListingForUser(userId: String): Flow<com.example.data.model.LookingForWork?> {
        return repository.getListingForUser(userId)
    }
    
    fun insertWorkListing(listing: com.example.data.model.LookingForWork) {
        viewModelScope.launch {
            repository.insertWorkListing(listing)
        }
    }
    
    fun deleteWorkListingForUser(userId: String) {
        viewModelScope.launch {
            repository.deleteWorkListingForUser(userId)
        }
    }
    
    fun getSavedSearchesForUser(userId: String): Flow<List<com.example.data.model.SavedSearch>> {
        return repository.getSavedSearchesForUser(userId)
    }

    fun getAllTasksForUser(userId: String): Flow<List<com.example.data.model.ProductionTask>> {
        return repository.getAllTasksForUser(userId)
    }
    
    fun insertSavedSearch(search: com.example.data.model.SavedSearch) {
        viewModelScope.launch {
            repository.insertSavedSearch(search)
        }
    }
    
    fun deleteSavedSearch(id: String) {
        viewModelScope.launch {
            repository.deleteSavedSearchById(id)
        }
    }
    
    fun updateUserProfile(user: UserProfile) {
        viewModelScope.launch {
            try {
                repository.updateUserProfile(user)
                toastMessage.value = "Profile updated successfully"
                val awarded = repository.checkAndGrantReferralRewards(user.id)
                if (awarded) {
                    toastMessage.value = "Congratulations! Referral rewards (7 days Premium trial extension) granted!"
                }
            } catch (e: Exception) {
                toastMessage.value = "Update failed: ${e.message}"
            }
        }
    }

    // --- RETENTION & GROWTH FEATURE OPERATIONS ---
    val weeklyDigestEnabled = MutableStateFlow(true)
    val onboardingChecklistDismissed = MutableStateFlow(false)
    val uiTextSize = MutableStateFlow("NORMAL") // SMALL, NORMAL, LARGE
    val syncFrequency = MutableStateFlow("HOURLY") // REALTIME, HOURLY, DAILY
    val hapticFeedbackEnabled = MutableStateFlow(true)

    fun loadWeeklyDigestSetting(userId: String) {
        viewModelScope.launch {
            weeklyDigestEnabled.value = repository.getWeeklyDigestPreference(userId)
        }
    }

    fun setWeeklyDigestSetting(userId: String, isEnabled: Boolean) {
        viewModelScope.launch {
            weeklyDigestEnabled.value = isEnabled
            repository.setWeeklyDigestPreference(userId, isEnabled)
        }
    }

    fun loadUiTextSizeSetting(userId: String) {
        viewModelScope.launch {
            uiTextSize.value = repository.getUiTextSizeSetting(userId) ?: "NORMAL"
        }
    }

    fun setUiTextSizeSetting(userId: String, size: String) {
        viewModelScope.launch {
            uiTextSize.value = size
            repository.setUiTextSizeSetting(userId, size)
        }
    }

    fun loadSyncFrequencySetting(userId: String) {
        viewModelScope.launch {
            syncFrequency.value = repository.getSyncFrequencySetting(userId) ?: "HOURLY"
        }
    }

    fun setSyncFrequencySetting(userId: String, frequency: String) {
        viewModelScope.launch {
            syncFrequency.value = frequency
            repository.setSyncFrequencySetting(userId, frequency)
        }
    }

    fun loadOnboardingChecklistDismissed(userId: String) {
        viewModelScope.launch {
            onboardingChecklistDismissed.value = repository.getOnboardingChecklistDismissed(userId)
        }
    }

    fun setOnboardingChecklistDismissed(userId: String, dismissed: Boolean) {
        viewModelScope.launch {
            onboardingChecklistDismissed.value = dismissed
            repository.setOnboardingChecklistDismissed(userId, dismissed)
        }
    }

    fun loadHapticFeedbackSetting(userId: String) {
        viewModelScope.launch {
            val enabled = repository.getHapticFeedbackSetting(userId)
            hapticFeedbackEnabled.value = enabled
            FeedbackManager.isHapticEnabled = enabled
        }
    }

    fun setHapticFeedbackSetting(userId: String, enabled: Boolean) {
        viewModelScope.launch {
            hapticFeedbackEnabled.value = enabled
            FeedbackManager.isHapticEnabled = enabled
            repository.setHapticFeedbackSetting(userId, enabled)
        }
    }

    val allConnectionRequests: StateFlow<List<com.example.data.model.ConnectionRequest>> = repository.allConnectionRequestsFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun generateOrGetReferralCode(user: UserProfile) {
        if (user.referralCode.isNullOrEmpty()) {
            viewModelScope.launch {
                val basePrefix = if (user.displayName.length >= 4) {
                    user.displayName.filter { it.isLetterOrDigit() }.take(4).uppercase()
                } else "CREA"
                var code = "$basePrefix-${(1000..9999).random()}"
                var isUnique = false
                var attempts = 0
                while (!isUnique && attempts < 15) {
                    val existing = repository.getUserByReferralCode(code)
                    if (existing == null) {
                        isUnique = true
                    } else {
                        code = "$basePrefix-${(1000..9999).random()}"
                        attempts++
                    }
                }
                repository.updateUserProfile(user.copy(referralCode = code))
            }
        }
    }

    fun applyReferralCode(referredUserId: String, enteredCode: String) {
        viewModelScope.launch {
            try {
                val (success, message) = repository.applyReferralCode(referredUserId, enteredCode)
                toastMessage.value = message
                if (success) {
                    repository.checkAndGrantReferralRewards(referredUserId)
                }
            } catch (e: Exception) {
                toastMessage.value = "Referral failed: ${e.message}"
            }
        }
    }

    fun getSuccessfulReferralCount(referrerId: String): Flow<Int> {
        return repository.getSuccessfulReferralCount(referrerId)
    }

    // --- NOTIFICATIONS ---
    val pendingInvitationWorkspaceId = MutableStateFlow<String?>(null)

    fun getNotificationsForUser(userId: String) = repository.getNotificationsForUser(userId)
    fun markNotificationAsRead(id: String) = viewModelScope.launch { repository.markNotificationAsRead(id) }
    fun updateNotificationReadState(id: String, isRead: Boolean) = viewModelScope.launch { repository.updateNotificationReadState(id, isRead) }
    fun markAllNotificationsAsRead(userId: String) = viewModelScope.launch { repository.markAllNotificationsAsRead(userId) }
    fun deleteNotification(notification: com.example.data.model.Notification) {
        viewModelScope.launch {
            repository.deleteNotification(notification.id)
            FeedbackManager.showSuccess(
                message = "Notification dismissed",
                actionLabel = "UNDO",
                onAction = {
                    viewModelScope.launch {
                        repository.insertNotification(notification)
                    }
                }
            )
        }
    }

    fun pinNotification(id: String, isPinned: Boolean) {
        viewModelScope.launch {
            repository.pinNotification(id, isPinned)
        }
    }

    fun archiveNotification(id: String, isArchived: Boolean) {
        viewModelScope.launch {
            repository.archiveNotification(id, isArchived)
        }
    }

    fun sendNotification(userId: String, title: String, body: String, type: String, deepLinkTarget: String? = null) {
        viewModelScope.launch {
            val notification = com.example.data.model.Notification(
                id = java.util.UUID.randomUUID().toString(),
                userId = userId,
                title = title,
                body = body,
                type = type,
                isRead = false,
                createdAt = System.currentTimeMillis(),
                deepLinkTarget = deepLinkTarget
            )
            repository.insertNotification(notification)
        }
    }

    fun handleNotificationClick(notification: com.example.data.model.Notification, workspaceViewModel: WorkspaceViewModel) {
        markNotificationAsRead(notification.id)
        val target = notification.deepLinkTarget ?: return
        
        viewModelScope.launch {
            if (target.startsWith("WORKSPACE_INVITE:")) {
                val workspaceId = target.substringAfter("WORKSPACE_INVITE:")
                pendingInvitationWorkspaceId.value = workspaceId
                currentTab.value = "WORKSPACE_INVITATION"
            } else if (target.startsWith("AGREEMENT_VAULT:")) {
                val workspaceId = target.substringAfter("AGREEMENT_VAULT:")
                val ws = repository.getWorkspaceById(workspaceId).firstOrNull()
                if (ws != null) {
                    workspaceViewModel.selectWorkspace(ws)
                    workspaceViewModel.workspaceSubTab.value = "AGREEMENT"
                    currentTab.value = "WORKSPACES"
                }
            } else if (target.startsWith("TASK_DETAILS:")) {
                // Format: TASK_DETAILS:<taskId>:<workspaceId>
                val parts = target.substringAfter("TASK_DETAILS:").split(":")
                if (parts.size >= 2) {
                    val workspaceId = parts[1]
                    val ws = repository.getWorkspaceById(workspaceId).firstOrNull()
                    if (ws != null) {
                        workspaceViewModel.selectWorkspace(ws)
                        workspaceViewModel.workspaceSubTab.value = "STATE"
                        currentTab.value = "WORKSPACES"
                    }
                }
            } else if (target.startsWith("CHAT_THREAD:")) {
                val workspaceId = target.substringAfter("CHAT_THREAD:")
                val ws = repository.getWorkspaceById(workspaceId).firstOrNull()
                if (ws != null) {
                    workspaceViewModel.selectWorkspace(ws)
                    workspaceViewModel.workspaceSubTab.value = "CHAT"
                    currentTab.value = "WORKSPACES"
                }
            }
        }
    }

    // --- CONNECTION REQUESTS ---
    fun getPendingConnectionRequests(userId: String) = repository.getPendingConnectionRequests(userId)
    fun getResolvedConnectionRequests(userId: String) = repository.getResolvedConnectionRequests(userId)
    fun acceptConnectionRequest(requestId: String) = viewModelScope.launch { repository.resolveConnectionRequest(requestId, true) }
    fun declineConnectionRequest(requestId: String) = viewModelScope.launch { repository.resolveConnectionRequest(requestId, false) }

    suspend fun getOnboardingChecklistDismissed(userId: String): Boolean {
        return repository.getOnboardingChecklistDismissed(userId)
    }

    fun dismissOnboardingChecklist(userId: String) {
        viewModelScope.launch {
            repository.setOnboardingChecklistDismissed(userId, true)
        }
    }

    suspend fun hasAppliedReferralCode(userId: String): Boolean {
        return repository.getReferralForUser(userId) != null
    }

    // --- ADVANCED OFFLINE SYNCHRONIZATION ---
    val allSyncEvents = repository.allSyncEvents.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val lastSyncTime = MutableStateFlow<Long>(System.currentTimeMillis())
    val isSyncActive = MutableStateFlow<Boolean>(false)
    val conflictPolicy = MutableStateFlow("CLIENT_WINS")

    fun triggerManualSync() {
        if (isSyncActive.value) return
        viewModelScope.launch {
            isSyncActive.value = true
            syncState.value = SyncState.Syncing
            try {
                // 1. Process pending local uploads / writes first
                repository.syncUpPendingEvents()
                
                // 2. Fetch latest changes from remote server
                if (SupabaseConfig.isNetworkAvailable(application) &&
                    !SupabaseConfig.supabaseUrl.contains("your-project")) {
                    SupabaseSynchronizer.syncDownEverything(application, repository)
                }
                
                lastSyncTime.value = System.currentTimeMillis()
                
                // Check if there are still any pending offline edits
                val remainingCount = repository.syncDao.getPendingSyncEventsSuspend().size
                if (remainingCount > 0) {
                    syncState.value = SyncState.PendingLocalChanges
                } else {
                    syncState.value = SyncState.Synced
                }
                toastMessage.value = "Synchronization complete!"
            } catch (e: Exception) {
                android.util.Log.e("GlobalViewModel", "Manual synchronization failed", e)
                syncState.value = SyncState.OfflineSandbox
                toastMessage.value = "Sync failed: ${e.localizedMessage ?: "Offline Mode"}"
            } finally {
                isSyncActive.value = false
            }
        }
    }

    fun retrySyncEvent(eventId: String) {
        viewModelScope.launch {
            val event = allSyncEvents.value.find { it.id == eventId } ?: return@launch
            repository.updateSyncEvent(event.copy(syncStatus = "PENDING", retryCount = 0, lastAttemptedAt = System.currentTimeMillis()))
            triggerManualSync()
        }
    }

    fun deleteSyncEvent(eventId: String) {
        viewModelScope.launch {
            repository.deleteSyncEvent(eventId)
        }
    }

    fun clearSyncedHistory() {
        viewModelScope.launch {
            repository.clearSyncedHistory()
            toastMessage.value = "Synced logs cleared"
        }
    }

    fun setConflictPolicy(policy: String) {
        conflictPolicy.value = policy
    }
}
