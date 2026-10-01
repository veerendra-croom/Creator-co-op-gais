package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.analytics.AnalyticsEvent
import com.example.analytics.AnalyticsManager
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class AnalyticsViewModel(private val repository: AppRepository) : ViewModel() {

    private val events = AnalyticsManager.events

    // 1. Beta User Health
    val userActivity = events.map { allEvents ->
        allEvents.groupBy { it.properties["user_id"] as? String ?: "unknown" }
            .mapValues { it.value.size }
            .filter { it.key != "unknown" }
            .toList()
            .sortedByDescending { it.second }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // 2. Funnel Monitoring
    val funnelData = events.map { allEvents ->
        val registered = allEvents.count { it.name == "user_registered" || it.name == "user_activated" }
        val workspace = allEvents.count { it.name == "workspace_created" }
        val agreement = allEvents.count { it.name == "agreement_signed" }
        val task = allEvents.count { it.name == "task_completed" }
        listOf(
            FunnelStep("Registration", registered),
            FunnelStep("Workspace", workspace),
            FunnelStep("Agreement", agreement),
            FunnelStep("Task Done", task)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // 3. Churn Detection (Inactive for 3+ days)
    val churnAlerts = events.map { allEvents ->
        val now = System.currentTimeMillis()
        val threeDaysAgo = now - TimeUnit.DAYS.toMillis(3)
        
        val lastSeen = allEvents.groupBy { it.properties["user_id"] as? String ?: "unknown" }
            .mapValues { it.value.maxOfOrNull { e -> e.timestamp } ?: 0L }
            .filter { it.key != "unknown" }

        lastSeen.filter { it.value < threeDaysAgo }
            .map { "User ${it.key} inactive for ${TimeUnit.MILLISECONDS.toDays(now - it.value)} days" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // 4. Referral Monitoring
    val referralStats = events.map { allEvents ->
        val shared = allEvents.count { it.name == "portfolio_shared" }
        val invited = allEvents.count { it.name == "member_invited" }
        val joined = allEvents.count { it.name == "member_joined" }
        
        mapOf(
            "Portfolio Shares" to shared,
            "Invites Sent" to invited,
            "Invites Accepted" to joined,
            "Viral Coeff" to if (shared > 0) joined.toFloat() / shared else 0f
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyMap())

    // 5. PMF Metrics
    val pmfMetrics = events.map { allEvents ->
        val activeWorkspaces = allEvents.filter { it.name == "task_completed" || it.name == "member_joined" }
            .mapNotNull { it.properties["workspace_id"] as? String }
            .distinct().size
            
        val agreementRate = if (allEvents.count { it.name == "agreement_drafted" } > 0) {
            allEvents.count { it.name == "agreement_signed" }.toFloat() / allEvents.count { it.name == "agreement_drafted" }
        } else 0f

        mapOf(
            "Active Collaborations" to activeWorkspaces,
            "Agreement Completion" to "${(agreementRate * 100).toInt()}%",
            "Avg Tasks/User" to if (userActivity.value.isNotEmpty()) allEvents.count { it.name == "task_completed" }.toFloat() / userActivity.value.size else 0f
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyMap())

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    fun resetToast() { _toastMessage.value = null }

    private fun escapeCsv(value: String): String {
        val needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")
        val escaped = value.replace("\"", "\"\"")
        return if (needsQuotes) "\"$escaped\"" else escaped
    }

    private fun escapeJson(value: String): String {
        return value.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    fun getExportContent(format: String): String {
        val allEvents = AnalyticsManager.events.value
        return if (format == "JSON") {
            "[" + allEvents.joinToString(",") { event ->
                val propsJson = event.properties.entries.joinToString(",") { (k, v) ->
                    "\"${escapeJson(k)}\":\"${escapeJson(v.toString())}\""
                }
                "{\"event\":\"${escapeJson(event.name)}\",\"timestamp\":${event.timestamp},\"properties\":{$propsJson}}"
            } + "]"
        } else {
            "Event,Timestamp,Properties\n" + allEvents.joinToString("\n") { event ->
                val propsString = event.properties.entries.joinToString(";") { (k, v) -> "$k=$v" }
                val escapedEvent = escapeCsv(event.name)
                val escapedProps = escapeCsv(propsString)
                "$escapedEvent,${event.timestamp},$escapedProps"
            }
        }
    }

    fun exportData(format: String) {
        val content = getExportContent(format)
        android.util.Log.d("AnalyticsExport", content)
        _toastMessage.value = "$format telemetry compiled (${AnalyticsManager.events.value.size} events)"
    }
}

data class FunnelStep(val name: String, val count: Int)
