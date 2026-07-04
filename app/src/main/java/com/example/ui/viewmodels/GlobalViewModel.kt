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

    fun navigateToTab(tab: String) {
        if (currentTab.value != tab) {
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

    val showSplash = MutableStateFlow(true)
    val showOnboarding = MutableStateFlow(false)
    val showCelebration = MutableStateFlow(false)
    val syncState = MutableStateFlow(SyncState.Synced)
    val toastMessage = MutableStateFlow<String?>(null)
    
    val themeMode = MutableStateFlow("SYSTEM") // SYSTEM, LIGHT, DARK

    val allAdPlacements = repository.allAdPlacements.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val globalSettings = repository.globalAdSettings.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val featureFlags: StateFlow<List<com.example.data.model.FeatureFlag>> = repository.getAllFeatureFlagsFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val changelogEntries = repository.changelogDao.getAllChangelogEntries()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val showWhatsNew = MutableStateFlow(false)
    val exportedData = MutableStateFlow<String?>(null)

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

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeUser(currentUserId: Flow<String?>): StateFlow<UserProfile?> {
        return currentUserId.flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.userDao.getUserById(id)
        }.onEach { user ->
            user?.id?.let { uid ->
                SupabaseRealtimeManager.subscribeToUserData(repository, viewModelScope, uid)
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
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

    fun generateOrGetReferralCode(user: UserProfile) {
        if (user.referralCode.isEmpty()) {
            val prefix = if (user.displayName.length >= 4) user.displayName.take(4).uppercase() else "CREA"
            val randomPart = (1000..9999).random()
            val code = "$prefix-$randomPart"
            viewModelScope.launch {
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
}
