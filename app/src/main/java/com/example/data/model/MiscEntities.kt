package com.example.data.model

import kotlinx.serialization.Serializable

@Serializable
sealed class SecurityState {
    @Serializable
    object Idle : SecurityState()
    @Serializable
    object SybilThrottled : SecurityState()
}

@Serializable
data class SavedSearchFilter(
    val type: String? = null,
    val niche: String? = null,
    val searchQuery: String = ""
)

@Serializable
data class LookingForWorkDetails(
    val skills: String = "",
    val availability: String = "",
    val rateExpectations: String = ""
)

@Serializable
data class RecruitmentAuditingConfig(
    val interviewResponsePersona: String = "FRIENDLY",
    val customInterviewDirective: String = ""
)

@Serializable
data class WorkspacePolicyConfig(
    val governanceRule: String = "MERITOCRACY",
    val reviewStandard: String = "STRICT",
    val discordWebhookUrl: String? = null,
    val youtubeApiKey: String? = null,
    val instagramAuthToken: String? = null,
    val autoApprovePitch: Boolean = false,
    val minKarmaRequired: Int = 0,
    val minAvailabilityHours: Int = 0,
    val tags: List<String> = emptyList()
)

@Serializable
data class TemplateTask(
    val title: String,
    val description: String,
    val defaultLane: String = "TODO"
)
