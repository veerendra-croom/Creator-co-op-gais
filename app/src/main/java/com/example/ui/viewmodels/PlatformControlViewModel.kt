package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class PlatformControlViewModel(private val repository: AppRepository) : ViewModel() {

    // Platform Settings State
    val platformSettings: StateFlow<PlatformSettings> = repository.getPlatformSettingsFlow()
        .map { it ?: PlatformSettings() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, PlatformSettings())

    // Feature Flags State
    val featureFlags: StateFlow<List<FeatureFlag>> = repository.getAllFeatureFlagsFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Dynamic Content States
    val onboardingSlides: StateFlow<List<OnboardingSlide>> = repository.getAllOnboardingSlidesFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val welcomeMessages: StateFlow<List<WelcomeMessage>> = repository.getAllWelcomeMessagesFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val emptyStates: StateFlow<List<EmptyStateConfig>> = repository.getAllEmptyStatesFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val helpTexts: StateFlow<List<HelpText>> = repository.getAllHelpTextsFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Notification toast feedback
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun clearToast() {
        _toastMessage.value = null
    }

    // Settings mutations
    fun updateMaintenanceMode(enabled: Boolean) {
        viewModelScope.launch {
            val updated = platformSettings.value.copy(maintenanceMode = enabled)
            repository.insertPlatformSettings(updated)
            _toastMessage.value = "Maintenance Mode set to ${if (enabled) "ON" else "OFF"}."
        }
    }

    fun updateBetaMode(enabled: Boolean) {
        viewModelScope.launch {
            val updated = platformSettings.value.copy(betaMode = enabled)
            repository.insertPlatformSettings(updated)
            _toastMessage.value = "Beta Mode set to ${if (enabled) "ON" else "OFF"}."
        }
    }

    fun updateRegistrationToggle(enabled: Boolean) {
        viewModelScope.launch {
            val updated = platformSettings.value.copy(registrationEnabled = enabled)
            repository.insertPlatformSettings(updated)
            _toastMessage.value = "Public Registration ${if (enabled) "ENABLED" else "DISABLED"}."
        }
    }

    fun updateInviteOnlyToggle(enabled: Boolean) {
        viewModelScope.launch {
            val updated = platformSettings.value.copy(inviteOnlyEnabled = enabled)
            repository.insertPlatformSettings(updated)
            _toastMessage.value = "Invite Only Mode ${if (enabled) "ACTIVE" else "INACTIVE"}."
        }
    }

    // Feature Flag mutations
    fun toggleFeatureFlag(key: String, enabled: Boolean) {
        viewModelScope.launch {
            val existing = repository.getFeatureFlag(key)
            val updated = existing?.copy(
                isEnabled = enabled,
                globalOverrideEnabled = enabled,
                organizerEnabled = if (!enabled) false else existing.organizerEnabled,
                participantEnabled = if (!enabled) false else existing.participantEnabled,
                lastModifiedAt = System.currentTimeMillis()
            ) ?: FeatureFlag(
                flagKey = key,
                isEnabled = enabled,
                globalOverrideEnabled = enabled,
                organizerEnabled = enabled,
                participantEnabled = enabled,
                lastModifiedAt = System.currentTimeMillis()
            )
            repository.insertFeatureFlag(updated)
            _toastMessage.value = "Feature Flag '$key' set to ${if (enabled) "ENABLED" else "DISABLED"}."
        }
    }

    fun updateFeatureFlagRbac(
        key: String,
        organizerEnabled: Boolean,
        participantEnabled: Boolean,
        globalOverrideEnabled: Boolean,
        adminId: String = "admin_master",
        reason: String = "Admin console RBAC update"
    ) {
        viewModelScope.launch {
            val existing = repository.getFeatureFlag(key)
            val effectiveOrg = if (globalOverrideEnabled) organizerEnabled else false
            val effectivePart = if (globalOverrideEnabled) participantEnabled else false
            val isOverallActive = globalOverrideEnabled && (effectiveOrg || effectivePart)
            val updated = existing?.copy(
                organizerEnabled = effectiveOrg,
                participantEnabled = effectivePart,
                globalOverrideEnabled = globalOverrideEnabled,
                isEnabled = isOverallActive,
                lastModifiedByAdminId = adminId,
                lastModifiedAt = System.currentTimeMillis()
            ) ?: FeatureFlag(
                flagKey = key,
                organizerEnabled = effectiveOrg,
                participantEnabled = effectivePart,
                globalOverrideEnabled = globalOverrideEnabled,
                isEnabled = isOverallActive,
                lastModifiedByAdminId = adminId,
                lastModifiedAt = System.currentTimeMillis()
            )
            repository.updateFeatureFlag(updated, adminId, reason)
            _toastMessage.value = "RBAC policy for '$key' updated successfully."
        }
    }

    fun applyFeatureFlagPresetProfile(
        presetKey: String,
        adminId: String = "admin_master",
        reason: String = "Admin preset applied"
    ) {
        viewModelScope.launch {
            val currentFlags = featureFlags.value.ifEmpty { repository.getAllFeatureFlagsFlow().first() }
            currentFlags.forEach { flag ->
                val (newGlobal, newOrg, newPart) = when (presetKey) {
                    "OPEN_BETA" -> Triple(true, true, true)
                    "ORGANIZER_FIRST" -> {
                        val isRestrictedForPart = flag.category == "GOVERNANCE" || 
                            flag.flagKey in listOf("analytics_enabled", "founder_crm_enabled", "moderation_center_enabled", "AGREEMENT_DRAFTING")
                        Triple(true, true, !isRestrictedForPart)
                    }
                    "RESTRICTED_MAINTENANCE" -> {
                        val isHeavyCreation = flag.flagKey in listOf(
                            "SYNDICATE_PITCH_CREATION", "AGREEMENT_DRAFTING", "COMMUNITY_FORUM_POSTING",
                            "VOICE_HUDDLE_BETA", "FILE_UPLOADS", "huddles_enabled", "TASK_CREATION"
                        )
                        Triple(!isHeavyCreation, !isHeavyCreation, !isHeavyCreation)
                    }
                    "MEDIA_BANDWIDTH_FREEZE" -> {
                        val isMediaHeavy = flag.flagKey in listOf("VOICE_HUDDLE_BETA", "FILE_UPLOADS", "huddles_enabled")
                        if (isMediaHeavy) Triple(false, false, false) else Triple(flag.globalOverrideEnabled, flag.organizerEnabled, flag.participantEnabled)
                    }
                    else -> Triple(flag.globalOverrideEnabled, flag.organizerEnabled, flag.participantEnabled)
                }
                val isOverall = newGlobal && (newOrg || newPart)
                val updated = flag.copy(
                    globalOverrideEnabled = newGlobal,
                    organizerEnabled = newOrg,
                    participantEnabled = newPart,
                    isEnabled = isOverall,
                    lastModifiedByAdminId = adminId,
                    lastModifiedAt = System.currentTimeMillis()
                )
                repository.updateFeatureFlag(updated, adminId, "Preset applied: $presetKey. Reason: $reason")
            }
            _toastMessage.value = "Applied preset profile: $presetKey"
        }
    }

    // Onboarding Slides CRUD
    fun upsertOnboardingSlide(slide: OnboardingSlide) {
        if (slide.title.isBlank() || slide.description.isBlank()) {
            _toastMessage.value = "Slide fields cannot be empty."
            return
        }
        viewModelScope.launch {
            val item = if (slide.id.isBlank()) slide.copy(id = "slide_" + UUID.randomUUID().toString().take(6)) else slide
            repository.insertOnboardingSlide(item)
            _toastMessage.value = "Onboarding Slide saved successfully."
        }
    }

    fun deleteOnboardingSlide(id: String) {
        viewModelScope.launch {
            repository.deleteOnboardingSlide(id)
            _toastMessage.value = "Onboarding Slide removed."
        }
    }

    // Welcome Messages CRUD
    fun upsertWelcomeMessage(msg: WelcomeMessage) {
        if (msg.title.isBlank() || msg.greeting.isBlank()) {
            _toastMessage.value = "Welcome title & greeting are required."
            return
        }
        viewModelScope.launch {
            // Deactivate all others if this one is active to ensure single banner
            if (msg.isActive) {
                welcomeMessages.value.forEach { existing ->
                    if (existing.id != msg.id && existing.isActive) {
                        repository.insertWelcomeMessage(existing.copy(isActive = false))
                    }
                }
            }

            val item = if (msg.id.isBlank()) msg.copy(id = "welcome_" + UUID.randomUUID().toString().take(6)) else msg
            repository.insertWelcomeMessage(item)
            _toastMessage.value = "Welcome banner message updated."
        }
    }

    fun deleteWelcomeMessage(id: String) {
        viewModelScope.launch {
            repository.deleteWelcomeMessage(id)
            _toastMessage.value = "Welcome message banner removed."
        }
    }

    // Empty States CRUD
    fun upsertEmptyState(config: EmptyStateConfig) {
        if (config.screenName.isBlank() || config.title.isBlank() || config.suggestion.isBlank()) {
            _toastMessage.value = "Screen name, title, and suggestion are mandatory."
            return
        }
        viewModelScope.launch {
            val item = if (config.id.isBlank()) config.copy(id = "empty_" + UUID.randomUUID().toString().take(6)) else config
            repository.insertEmptyState(item)
            _toastMessage.value = "Empty state configuration saved."
        }
    }

    fun deleteEmptyState(id: String) {
        viewModelScope.launch {
            repository.deleteEmptyState(id)
            _toastMessage.value = "Empty state configuration deleted."
        }
    }

    // Help Texts CRUD
    fun upsertHelpText(helpText: HelpText) {
        if (helpText.topicKey.isBlank() || helpText.textContent.isBlank() || helpText.category.isBlank()) {
            _toastMessage.value = "All help text fields are mandatory."
            return
        }
        viewModelScope.launch {
            val item = if (helpText.id.isBlank()) helpText.copy(id = "help_" + UUID.randomUUID().toString().take(6)) else helpText
            repository.insertHelpText(item)
            _toastMessage.value = "Help topic topic saved."
        }
    }

    fun deleteHelpText(id: String) {
        viewModelScope.launch {
            repository.deleteHelpText(id)
            _toastMessage.value = "Help topic removed."
        }
    }
}
