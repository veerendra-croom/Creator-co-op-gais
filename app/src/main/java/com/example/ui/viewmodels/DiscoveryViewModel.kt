package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.example.data.model.RecruitmentAuditingConfig
import com.example.data.model.*
import com.example.data.repository.AppRepository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID


class DiscoveryViewModel constructor(
    private val repository: AppRepository
) : ViewModel() {

    val selectedNicheFilter = MutableStateFlow("All")
    val pitchAttemptCount = MutableStateFlow(0)
    val securityState = MutableStateFlow<SecurityState>(SecurityState.Idle)

    private val _recruitmentAuditing = MutableStateFlow<Map<String, RecruitmentAuditingConfig>>(emptyMap())
    val recruitmentAuditing: StateFlow<Map<String, RecruitmentAuditingConfig>> = _recruitmentAuditing.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun resetToast() { _toastMessage.value = null }

    private val isTestEnv = try {
        Class.forName("org.robolectric.Robolectric") != null
    } catch (e: Throwable) {
        false
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val projectProposals: Flow<PagingData<ProjectProposalWithData>> = selectedNicheFilter.flatMapLatest { niche ->
        Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = {
                repository.getProjectProposals()
            }
        ).flow.map { pagingData: PagingData<ProjectProposalWithData> ->
            if (niche == "All") pagingData else pagingData.filter { it.proposal.niche.equals(niche, ignoreCase = true) }
        }
    }.cachedIn(viewModelScope)

    fun boostRole(proposalId: String) {
        viewModelScope.launch {
            repository.boostProposal(proposalId)
            _toastMessage.value = "Listing boosted for 48 hours!"
        }
    }

    fun submitProjectProposal(title: String, niche: String, brief: String, user: UserProfile?, userId: String, isBoosted: Boolean = false) {
        if (user == null || userId != user.id) {
            _toastMessage.value = "Identity verification failed. Please re-login."
            return
        }
        if (user.systemRole != "CREATOR" && user.globalRole != "ADMIN" && !user.isVerifiedPro) {
            _toastMessage.value = "Only verified Creators can post production collaboration listings."
            return
        }
        viewModelScope.launch {
            val proposal = ProjectProposal(
                id = "proj_" + UUID.randomUUID().toString().take(8),
                title = title,
                niche = niche,
                brief = brief,
                authorId = userId,
                authorName = user.displayName,
                boostedUntil = if (isBoosted) System.currentTimeMillis() + (48 * 60 * 60 * 1000) else 0,
                createdAt = System.currentTimeMillis()
            )
            repository.insertProjectProposal(proposal)
            _toastMessage.value = if (isBoosted) "Project proposal posted and boosted successfully!" else "Project proposal posted successfully!"
        }
    }

    fun submitTalentPitch(projectId: String, coverMessage: String, portfolioUrl: String, user: UserProfile?, userId: String) {
        if (user == null || userId != user.id) {
            _toastMessage.value = "Identity verification failed."
            return
        }
        // Marketplace creator-role authorization check for pitching as well
        if (user.systemRole != "CREATOR" && user.globalRole != "ADMIN" && !user.isVerifiedPro) {
            _toastMessage.value = "Only verified Creators can pitch to collaboration listings."
            return
        }

        val attempts = pitchAttemptCount.value
        if (attempts >= 3) {
            securityState.value = SecurityState.SybilThrottled
            _toastMessage.value = "Platform rate limit exceeded: Too many proposal attempts."
            return
        }
        viewModelScope.launch {
            pitchAttemptCount.value = attempts + 1
            val pitch = TalentPitch(
                id = UUID.randomUUID().toString(),
                projectId = projectId,
                senderId = userId,
                senderName = user.displayName,
                senderSpecialty = user.primarySpecialty,
                coverMessage = coverMessage,
                portfolioUrl = portfolioUrl,
                createdAt = System.currentTimeMillis()
            )
            repository.insertTalentPitch(pitch)

            // Notify Project Author
            repository.getProjectProposalById(projectId).firstOrNull()?.let { project ->
                repository.insertNotification(Notification(
                    id = UUID.randomUUID().toString(),
                    userId = project.authorId,
                    title = "New Syndicate Pitch",
                    body = "${user.displayName} has pitched for '${project.title}' as a ${user.primarySpecialty}.",
                    type = "PITCH_RECEIVED",
                    createdAt = System.currentTimeMillis()
                ))
            }

            _toastMessage.value = "Pitch submitted successfully! Attempts: ${attempts + 1}/3"
        }
    }

    fun acceptTalentPitch(pitch: TalentPitch, project: ProjectProposal, userId: String) {
        viewModelScope.launch {
            repository.updatePitchStatus(pitch.id, "ACCEPTED")
            val wsId = "ws_" + UUID.randomUUID().toString().take(8)
            val ws = Workspace(
                id = wsId,
                name = project.title,
                platformType = "YOUTUBE",
                createdBy = userId,
                createdAt = System.currentTimeMillis()
            )
            repository.insertWorkspace(ws)
            repository.insertMember(WorkspaceMember(
                id = UUID.randomUUID().toString(),
                workspaceId = wsId,
                userId = project.authorId,
                assignedRoleTitle = "Lead Creator",
                canModifyProduction = true
            ))
            repository.insertMember(WorkspaceMember(
                id = UUID.randomUUID().toString(),
                workspaceId = wsId,
                userId = pitch.senderId,
                assignedRoleTitle = pitch.senderSpecialty,
                canModifyProduction = true
            ))
            repository.insertMessage(Message(
                id = UUID.randomUUID().toString(),
                workspaceId = wsId,
                senderId = "system",
                senderName = "Welcome Bot",
                messageBody = "Congratulations! Match confirmed. New workspace is active. Let's create!",
                timestamp = System.currentTimeMillis()
            ))

            // Notify Applicant
            repository.insertNotification(Notification(
                id = UUID.randomUUID().toString(),
                userId = pitch.senderId,
                title = "Pitch Accepted!",
                body = "Your pitch for '${project.title}' was accepted! A new workspace has been created.",
                type = "PITCH_ACCEPTED",
                createdAt = System.currentTimeMillis()
            ))

            repository.deleteProjectProposalById(project.id)
            _toastMessage.value = "Match confirmed! Workspace '${project.title}' is ready!"
        }
    }

    fun declineTalentPitch(pitchId: String) {
        viewModelScope.launch {
            repository.updatePitchStatus(pitchId, "DECLINED")
            _toastMessage.value = "Pitch declined."
        }
    }

    fun deleteProposal(id: String) {
        viewModelScope.launch {
            repository.deleteProjectProposalById(id)
            _toastMessage.value = "Collaboration listing removed."
        }
    }

    fun getPitchesForProjectFlow(projectId: String) = repository.getPitchesForProject(projectId)
    fun getPitchesForUserFlow(userId: String) = repository.getPitchesBySender(userId)
    fun getInterviewMessages(pitchId: String) = repository.getMessagesForWorkspace("interview_$pitchId")

    fun getRecruitmentAuditing(pitchId: String) = _recruitmentAuditing.value[pitchId] ?: RecruitmentAuditingConfig()

    fun updateRecruitmentAuditing(pitchId: String, config: RecruitmentAuditingConfig) {
        val current = _recruitmentAuditing.value.toMutableMap()
        current[pitchId] = config
        _recruitmentAuditing.value = current
        _toastMessage.value = "Auditor criteria updated for applicant."
    }

    fun dismissSybilThrottling() { securityState.value = SecurityState.Idle }

    fun sendPreMatchMessage(pitchId: String, senderName: String, body: String, applicantName: String, applicantSpecialty: String, userId: String) {
        viewModelScope.launch {
            val hostMsg = Message(
                id = "msg_" + UUID.randomUUID().toString().take(8),
                workspaceId = "interview_$pitchId",
                senderId = userId,
                senderName = senderName,
                senderRole = "Workspace Lead",
                messageBody = body,
                timestamp = System.currentTimeMillis()
            )
            repository.insertMessage(hostMsg)
            delay(1000)
            val config = getRecruitmentAuditing(pitchId)
            val finalReply = generateCandidateReply(body, applicantSpecialty, config, applicantName)
            val candidateMsg = Message(
                id = "msg_" + UUID.randomUUID().toString().take(8),
                workspaceId = "interview_$pitchId",
                senderId = "candidate_$pitchId",
                senderName = applicantName,
                senderRole = applicantSpecialty,
                messageBody = finalReply,
                timestamp = System.currentTimeMillis()
            )
            repository.insertMessage(candidateMsg)
        }
    }

    private fun generateCandidateReply(body: String, applicantSpecialty: String, config: RecruitmentAuditingConfig, applicantName: String): String {
        val lowercaseBody = body.lowercase()
        val baseReply = when {
            lowercaseBody.contains("software") || lowercaseBody.contains("tool") || lowercaseBody.contains("edit") || lowercaseBody.contains("vfx") || lowercaseBody.contains("app") -> {
                when (applicantSpecialty.lowercase()) {
                    "video editor", "editor" -> "For primary edits, I use Premiere Pro and DaVinci Resolve. I also do advanced tracking and color-grading workflows to keep videos super cinematic!"
                    "writer", "scriptwriter" -> "I work using Notion and Google Docs for joint storyboarding. I also use retention trackers to map script high-points!"
                    else -> "I utilize custom After Effects shaders, Blender for 3D elements, and Premiere for finalizing layouts. Very comfortable matching your standard pipeline!"
                }
            }
            lowercaseBody.contains("rate") || lowercaseBody.contains("agreement") || lowercaseBody.contains("rules") || lowercaseBody.contains("terms") -> {
                "Our Team Agreement covers all collaboration norms and expectations. I'm fully prepared to acknowledge it once the workspace is initialized."
            }
            lowercaseBody.contains("time") || lowercaseBody.contains("hour") || lowercaseBody.contains("days") || lowercaseBody.contains("duration") -> {
                "I generally spend 15 to 25 hours a week on collaborative sprints. I'm active on Slack/Discord and have daily standups to sync up."
            }
            lowercaseBody.contains("portfolio") || lowercaseBody.contains("work") || lowercaseBody.contains("show") || lowercaseBody.contains("previous") -> {
                "My core portfolio highlights are in my dossier! I've worked on similar tech niches, reaching over 500k views on previous collaborations."
            }
            else -> "That sounds great! I'm completely aligned with your design goals. Let me know if you would like me to prepare any draft edits or mock concepts before onboarding!"
        }

        val adaptedReply = when (config.interviewResponsePersona) {
            "Highly Eager" -> "Oh neat, absolutely! 🚀 I would be ridiculously excited about this! $baseReply Let's do this!"
            "Conservative/Professional" -> "Respectfully acknowledged. $baseReply Rest assured that I maintain rigorous high-quality outputs and comply with SLAs."
            "Financial Negotiator" -> "Regarding coordination: $baseReply I want to make sure we coordinate our workloads to factor in extra revisions and high complexity."
            else -> baseReply
        }

        return if (config.customInterviewDirective.isNotBlank()) {
            "[Custom Directive Response Focus: \"${config.customInterviewDirective}\"]\n\n$adaptedReply\n\nI will ensure my deliverables adhere fully to this focus."
        } else adaptedReply
    }
}
