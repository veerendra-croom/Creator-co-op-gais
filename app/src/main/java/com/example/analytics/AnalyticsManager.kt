package com.example.analytics

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * CREATOR CO-OP ANALYTICS ENGINE
 * Captures high-intent events for PMF and Retention analysis.
 */
object AnalyticsManager {
    private const val TAG = "CoOpAnalytics"
    
    private val _events = MutableStateFlow<List<AnalyticsEvent>>(emptyList())
    val events = _events.asStateFlow()

    fun trackEvent(name: String, properties: Map<String, Any> = emptyMap()) {
        val event = AnalyticsEvent(
            name = name,
            timestamp = System.currentTimeMillis(),
            properties = properties
        )
        
        // Log for debugging during beta
        Log.d(TAG, "TRACKING: ${event.name} | PROPS: ${event.properties}")
        
        // Update local state (could be synced to Supabase/PostHog in production)
        val currentList = _events.value.toMutableList()
        currentList.add(event)
        _events.value = currentList
    }

    // Standard Funnel Events
    fun trackUserActivation(userId: String) = trackEvent("user_activated", mapOf("user_id" to userId))
    fun trackWorkspaceCreated(name: String, type: String) = trackEvent("workspace_created", mapOf("name" to name, "type" to type))
    fun trackAgreementSigned(agreementId: String) = trackEvent("agreement_signed", mapOf("agreement_id" to agreementId))
    fun trackTaskCompleted(taskId: String, duration: Long) = trackEvent("task_completed", mapOf("task_id" to taskId, "duration_ms" to duration))
    fun trackReputationGain(newScore: Int) = trackEvent("reputation_gain", mapOf("new_score" to newScore))
    fun trackPortfolioShared() = trackEvent("portfolio_shared")
}

data class AnalyticsEvent(
    val name: String,
    val timestamp: Long,
    val properties: Map<String, Any>
)
