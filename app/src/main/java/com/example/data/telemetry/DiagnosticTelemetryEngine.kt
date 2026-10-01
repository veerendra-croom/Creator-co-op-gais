package com.example.data.telemetry

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentLinkedDeque
import kotlinx.serialization.Serializable

/**
 * Diagnostic Telemetry & Crash Resilience Engine.
 * Collects lightweight breadcrumbs, UI frame jank alerts, and network error heatmaps
 * entirely locally without transmitting PII.
 */
object DiagnosticTelemetryEngine {

    @Serializable
    data class Breadcrumb(
        val category: String, // UI, NETWORK, DATABASE, AUTH
        val message: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    @Serializable
    data class PerformanceSnapshot(
        val totalRecordedBreadcrumbs: Int,
        val jankFrameCount: Int,
        val slowNetworkCallsCount: Int,
        val appUptimeSeconds: Long,
        val recentBreadcrumbs: List<Breadcrumb>
    )

    private val breadcrumbs = ConcurrentLinkedDeque<Breadcrumb>()
    private val maxBreadcrumbs = 100

    private val startTime = System.currentTimeMillis()
    private var jankFrameCounter = 0
    private var slowNetworkCounter = 0

    private val _snapshotFlow = MutableStateFlow(buildSnapshot())
    val snapshotFlow: StateFlow<PerformanceSnapshot> = _snapshotFlow.asStateFlow()

    fun recordBreadcrumb(category: String, message: String) {
        val bc = Breadcrumb(category, message)
        breadcrumbs.addLast(bc)
        while (breadcrumbs.size > maxBreadcrumbs) {
            breadcrumbs.pollFirst()
        }
        _snapshotFlow.value = buildSnapshot()
    }

    fun recordJankFrame(renderDurationMs: Long) {
        if (renderDurationMs > 16) {
            jankFrameCounter++
            if (renderDurationMs > 32) {
                recordBreadcrumb("PERF", "Severe UI jank detected: ${renderDurationMs}ms frame")
            }
        }
    }

    fun recordNetworkMetric(endpoint: String, durationMs: Long, isSuccess: Boolean) {
        if (durationMs > 1000L || !isSuccess) {
            slowNetworkCounter++
            recordBreadcrumb("NETWORK", "Network issue on $endpoint: ${durationMs}ms (success=$isSuccess)")
        }
    }

    private fun buildSnapshot(): PerformanceSnapshot {
        val uptime = (System.currentTimeMillis() - startTime) / 1000
        return PerformanceSnapshot(
            totalRecordedBreadcrumbs = breadcrumbs.size,
            jankFrameCount = jankFrameCounter,
            slowNetworkCallsCount = slowNetworkCounter,
            appUptimeSeconds = uptime,
            recentBreadcrumbs = breadcrumbs.toList().takeLast(20)
        )
    }
}
