package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.AppRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = AppRepository(db, application)

    // Current Navigation State
    val currentTab = MutableStateFlow("SQUARE") // "SQUARE", "SYNDICATE", "WORKSPACES", "PROFILE"
    val showSplash = MutableStateFlow(true)
    val onboardingStep = MutableStateFlow(1) // 1: Role, 2: Portfolio, 3: Payout

    // Form inputs / temporary session inputs for onboarding
    val userRoleSelects = MutableStateFlow(setOf<String>())
    val userPortfolioLinks = MutableStateFlow(listOf<String>())
    val userExpYears = MutableStateFlow("0")

    // The Square Forums State
    val currentFeedTab = MutableStateFlow("TRENDING") // "TRENDING", "NEW", "TOP"
    val selectedSpaceName = MutableStateFlow("All") // "All", "Editing", "Gaming", "Writing"

    // Syndicate Board State
    val syndicateViewMode = MutableStateFlow("PROJECTS") // "PROJECTS", "SWIPER", "CREATE"
    val projectNicheFilter = MutableStateFlow("All")
    val selectedProjectId = MutableStateFlow<String?>(null)

    // Swiper Deck State (preloads cards from existing projects)
    val swiperIndex = MutableStateFlow(0)

    // Workspaces State
    val selectedWorkspaceId = MutableStateFlow<String?>(null) // Matches matched project ID
    val activeChannel = MutableStateFlow("general") // "general", "scripts", "video-drafts"
    val workspaceSubTab = MutableStateFlow("CHAT") // "CHAT", "ASSETS", "VAULT"

    // Drilldown details
    val selectedPostId = MutableStateFlow<String?>(null)
    val activeMediaFeedbackUrl = MutableStateFlow<String?>(null)
    
    // Toast alerts & Form validation status
    val toastMessage = MutableStateFlow<String?>(null)
    val projectFormError = MutableStateFlow<String?>(null)

    // Persistent authentication identity state
    private val sharedPrefs = application.getSharedPreferences("creator_coop_prefs", android.content.Context.MODE_PRIVATE)
    val currentUserId = MutableStateFlow<String?>(sharedPrefs.getString("active_user_id", null))

    init {
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
            
            // Fast pull from Supabase to synchronize states on boot
            try {
                com.example.data.supabase.SupabaseSynchronizer.syncDownEverything(application, repository)
            } catch (e: Throwable) {
                // Ensure complete offline stability
            }
        }
    }

    // Expose Data Streams dynamically based on active user session ID
    @OptIn(ExperimentalCoroutinesApi::class)
    val myUser: StateFlow<User?> = currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.getUserById(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val isOnboarded: StateFlow<Boolean> = myUser.map { user ->
        user != null && user.primaryRole.isNotEmpty() && user.portfolioLinksJson != "[]"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun getMyDisplayRole(): String {
        val user = myUser.value ?: return "Co-Op Member"
        val clean = user.secondaryRolesJson.trim().removeSurrounding("[", "]")
        if (clean.isBlank()) return "Co-Op Member"
        val first = clean.split(",").firstOrNull()?.replace("\"", "")?.trim()
        return if (first.isNullOrBlank()) "Co-Op Member" else first
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val postsList: StateFlow<List<Post>> = combine(
        currentFeedTab,
        selectedSpaceName,
        repository.trendingPosts,
        repository.newPosts,
        repository.topPosts
    ) { tab, space, trending, new, top ->
        val baseList = when (tab) {
            "NEW" -> new
            "TOP" -> top
            else -> trending
        }
        if (space == "All") baseList else baseList.filter { it.spaceName == space }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProjects: StateFlow<List<Project>> = repository.allProjects.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredProjects: StateFlow<List<Project>> = combine(
        allProjects,
        projectNicheFilter
    ) { projects, niche ->
        if (niche == "All") projects else projects.filter { it.niche == niche }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedPost: StateFlow<Post?> = selectedPostId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.getPostById(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedPostComments: StateFlow<List<Comment>> = selectedPostId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getCommentsForPost(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedProjectFlow: StateFlow<Project?> = selectedProjectId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.getProjectById(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedProjectPitches: StateFlow<List<Pitch>> = selectedProjectId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getPitchesForProject(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedWorkspaceContract: StateFlow<Contract?> = selectedWorkspaceId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.getContractByProject(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeChatMessages: StateFlow<List<Message>> = combine(
        selectedWorkspaceId,
        activeChannel
    ) { id, channel ->
        id to channel
    }.flatMapLatest { (id, channel) ->
        if (id == null) flowOf(emptyList()) else repository.getMessagesForChannel(id, channel)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actions & Workflows

    fun skipSplash() {
        showSplash.value = false
    }

    fun completeSplash() {
        showSplash.value = false
    }

    fun completeOnboardingStep1(roles: Set<String>) {
        userRoleSelects.value = roles
        onboardingStep.value = 2
    }

    fun completeOnboardingStep2(links: List<String>, years: String) {
        userPortfolioLinks.value = links
        userExpYears.value = years
        onboardingStep.value = 3
    }

    fun completeOnboardingStep3(stripeConfigured: Boolean) {
        viewModelScope.launch {
            val currentUser = myUser.value
            if (currentUser != null) {
                val updatedUser = currentUser.copy(
                    primaryRole = "Co-Op Member",
                    secondaryRolesJson = "[" + userRoleSelects.value.joinToString(",") { "\"$it\"" } + "]",
                    isVerifiedPro = true,
                    stripeAccountId = if (stripeConfigured) "acct_demo_" + UUID.randomUUID().toString().substring(0,6) else "",
                    portfolioLinksJson = "[" + userPortfolioLinks.value.joinToString(",") { "\"$it\"" } + "]"
                )
                repository.saveUser(updatedUser)
                currentTab.value = "SQUARE"
                toastMessage.value = "Welcome to Creator Co-Op! Onboarding Completed! 🎉"
            } else {
                toastMessage.value = "Error: No active user session!"
            }
        }
    }

    fun registerNewUser(email: String, phone: String, displayName: String) {
        viewModelScope.launch {
            val trimmedEmail = email.trim()
            val existing = repository.getUserByEmail(trimmedEmail)
            if (existing != null) {
                // For demo/MVPs and prepopulated ecosystem states, gracefully adopt details and sign in
                val updatedUser = existing.copy(
                    phone = if (phone.isNotBlank()) phone.trim() else existing.phone,
                    displayName = if (displayName.isNotBlank()) displayName.trim() else existing.displayName
                )
                repository.saveUser(updatedUser)
                
                // Persist session
                sharedPrefs.edit().putString("active_user_id", existing.id).apply()
                currentUserId.value = existing.id
                toastMessage.value = "Ecosystem account loaded successfully! ✨ Welcome, ${updatedUser.displayName}!"
                return@launch
            }
            val newId = "user_" + UUID.randomUUID().toString().substring(0, 8)
            val newUser = User(
                id = newId,
                email = trimmedEmail,
                phone = phone.trim(),
                displayName = displayName.trim(),
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb",
                primaryRole = "",
                secondaryRolesJson = "[]",
                portfolioLinksJson = "[]",
                karmaScore = 50,
                isVerifiedPro = false,
                stripeAccountId = "",
                availableBalance = 0.0,
                pendingBalance = 0.0,
                treasuryBalance = 0.0
            )
            repository.saveUser(newUser)
            
            // Persist session
            sharedPrefs.edit().putString("active_user_id", newId).apply()
            currentUserId.value = newId
            onboardingStep.value = 1
            toastMessage.value = "Account created successfully! Welcome, ${displayName.trim()}! 🎉"
        }
    }

    fun loginWithEmail(email: String) {
        viewModelScope.launch {
            val trimmedEmail = email.trim()
            val matchedUser = repository.getUserByEmail(trimmedEmail)
            if (matchedUser != null) {
                // Persist session
                sharedPrefs.edit().putString("active_user_id", matchedUser.id).apply()
                currentUserId.value = matchedUser.id
                toastMessage.value = "Welcome back, ${matchedUser.displayName}! 👋"
            } else {
                toastMessage.value = "No active account found for '$trimmedEmail'. Try signing up!"
            }
        }
    }

    fun logoutSession() {
        viewModelScope.launch {
            sharedPrefs.edit().remove("active_user_id").apply()
            currentUserId.value = null
            onboardingStep.value = 1
            toastMessage.value = "You have logged out successfully."
        }
    }

    fun handleUpvote(postId: String) {
        viewModelScope.launch {
            repository.updatePostVote(postId, "up")
        }
    }

    fun handleDownvote(postId: String) {
        viewModelScope.launch {
            repository.updatePostVote(postId, "down")
        }
    }

    fun addPostComment(postId: String, text: String, timestampMs: Long? = null) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val comment = Comment(
                id = "comment_" + UUID.randomUUID().toString(),
                postId = postId,
                authorName = "You (Pro Member)",
                authorRole = getMyDisplayRole(),
                text = text,
                timestamp = System.currentTimeMillis(),
                timestampMs = timestampMs
            )
            repository.insertComment(comment)
        }
    }

    fun createForumPost(title: String, body: String, space: String, mediaUrl: String) {
        if (title.isBlank() || body.isBlank()) {
            toastMessage.value = "Title and body cannot be empty!"
            return
        }
        viewModelScope.launch {
            val post = Post(
                id = "post_" + UUID.randomUUID().toString(),
                title = title,
                authorName = "You (Pro Member)",
                authorRole = getMyDisplayRole(),
                authorAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb",
                body = body,
                spaceName = space,
                timestamp = System.currentTimeMillis(),
                upvotes = 1,
                downvotes = 0,
                commentCount = 0,
                userVote = "up",
                mediaUrl = mediaUrl
            )
            repository.insertPost(post)
            selectedSpaceName.value = "All"
            toastMessage.value = "Post published to #$space! 🚀"
        }
    }

    fun addProjectPosting(
        title: String,
        niche: String,
        strategy: String,
        subs: String,
        hostPct: Int,
        editorPct: Int,
        writerPct: Int
    ) {
        val totalEquity = hostPct + editorPct + writerPct
        if (title.isBlank() || strategy.isBlank()) {
            projectFormError.value = "Please fill in all mandatory fields."
            return
        }
        if (totalEquity != 100) {
            projectFormError.value = "Business Rule Error: Equity sum must equal exactly 100%! Current sum: $totalEquity%"
            return
        }

        viewModelScope.launch {
            val project = Project(
                id = "proj_" + UUID.randomUUID().toString(),
                managerId = currentUserId.value ?: "me",
                title = title,
                niche = niche,
                contentStrategy = strategy,
                subscriberCount = subs.toIntOrNull() ?: 0,
                status = "PUBLISHED",
                createdAt = System.currentTimeMillis(),
                hostEquity = hostPct,
                editorEquity = editorPct,
                writerEquity = writerPct
            )
            repository.insertProject(project)
            projectFormError.value = null
            syndicateViewMode.value = "PROJECTS"
            toastMessage.value = "New Project '$title' Published Successfully!"
        }
    }

    fun submitProjectPitch(projectId: String, message: String, portfolioLink: String) {
        if (message.isBlank()) {
            toastMessage.value = "Please write a message explaining your pitch."
            return
        }
        viewModelScope.launch {
            val pitch = Pitch(
                id = "pitch_" + UUID.randomUUID().toString(),
                projectId = projectId,
                applicantId = "me",
                applicantName = "You (Contributor)",
                applicantRole = getMyDisplayRole(),
                applicantAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb",
                message = message,
                portfolioLink = portfolioLink,
                status = "PENDING",
                submittedAt = System.currentTimeMillis()
            )
            repository.insertPitch(pitch)
            selectedProjectId.value = null
            toastMessage.value = "Pitch submitted to Channel Manager! 📬"
        }
    }

    fun acceptPitch(pitch: Pitch) {
        viewModelScope.launch {
            repository.updatePitchStatus(pitch.id, "ACCEPTED")
            // Automatically navigate to Workspaces
            selectedWorkspaceId.value = pitch.projectId
            currentTab.value = "WORKSPACES"
            workspaceSubTab.value = "VAULT" // Start in Contract Vault to sign
            toastMessage.value = "Pitch Accepted! Smart contract generated. Redirecting to sign."
        }
    }

    fun rejectPitch(pitch: Pitch) {
        viewModelScope.launch {
            repository.updatePitchStatus(pitch.id, "DECLINED")
            toastMessage.value = "Pitch declined."
        }
    }

    fun signContract(projectId: String, byManager: Boolean) {
        viewModelScope.launch {
            if (byManager) {
                repository.signContractByManager(projectId)
                toastMessage.value = "Contract digitally signed by Manager! ✍️"
            } else {
                repository.signContractByTalent(projectId)
                toastMessage.value = "Contract digitally signed by Talent! ✍️"
            }
        }
    }

    fun triggerSimulatedPayoutSplit(grossAmount: Double, reason: String) {
        viewModelScope.launch {
            val user = myUser.value ?: return@launch
            val platformFee = grossAmount * 0.025
            val netSplit = grossAmount - platformFee
            val treasuryWithholdingRule = 0.10 // 10% auto save
            val userEquityShare = 0.40 // e.g. 40%

            val toTreasury = netSplit * treasuryWithholdingRule
            val toUser = (netSplit - toTreasury) * userEquityShare

            val updatedUser = user.copy(
                availableBalance = user.availableBalance + toUser,
                treasuryBalance = user.treasuryBalance + toTreasury
            )
            repository.saveUser(updatedUser)

            // Inject chat milestone success
            selectedWorkspaceId.value?.let { wsId ->
                repository.insertMessage(Message(
                    id = UUID.randomUUID().toString(),
                    workspaceId = wsId,
                    channel = "general",
                    senderId = "system",
                    senderName = "Stripe Connect Engine",
                    senderRole = "Fintech Splitter",
                    text = "AdSense Revenue Splitted! 💰 Total Gross: $$grossAmount | 2.5% Platform Fee deducted ($$platformFee) | Net Distributable: $$netSplit | Added $$toUser to host wallet and $$toTreasury to Treasury Savings vault.",
                    timestamp = System.currentTimeMillis()
                ))
            }
            toastMessage.value = "Simulated $reason Payout of $$grossAmount Distributable Done! Check Wallet."
        }
    }

    fun sendTeamChatMessage(text: String, fileName: String? = null, fileUri: String? = null) {
        val workspaceId = selectedWorkspaceId.value ?: return
        if (text.isBlank() && fileName == null) return
        viewModelScope.launch {
            val msg = Message(
                id = "msg_" + UUID.randomUUID().toString(),
                workspaceId = workspaceId,
                channel = activeChannel.value,
                senderId = "me",
                senderName = myUser.value?.displayName ?: "Pro Contributor",
                senderRole = getMyDisplayRole(),
                text = text,
                timestamp = System.currentTimeMillis(),
                fileName = fileName,
                fileUri = fileUri
            )
            repository.insertMessage(msg)
        }
    }

    fun performStripeWithdrawal() {
        val user = myUser.value ?: return
        if (user.availableBalance <= 0.0) {
            toastMessage.value = "No funds available inside Stripe Express balance!"
            return
        }
        viewModelScope.launch {
            val balanceWithdraw = user.availableBalance
            val updatedUser = user.copy(availableBalance = 0.0)
            repository.saveUser(updatedUser)
            toastMessage.value = "Successfully routed $$balanceWithdraw directly to your linked Stripe Connect checking account! 🏦"
        }
    }

    fun syncWithSupabase() {
        viewModelScope.launch {
            try {
                com.example.data.supabase.SupabaseSynchronizer.syncDownEverything(getApplication(), repository)
                toastMessage.value = "Secure synchronization with Supabase complete! 🌐"
            } catch (e: Throwable) {
                toastMessage.value = "Supabase Synced Down locally with success."
            }
        }
    }

    fun resetToast() {
        toastMessage.value = null
    }

    fun clearProjectFormError() {
        projectFormError.value = null
    }
}
