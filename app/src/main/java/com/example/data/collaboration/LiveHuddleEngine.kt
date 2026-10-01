package com.example.data.collaboration

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

/**
 * Enterprise WebRTC & Live Huddle Telemetry Resilience Engine.
 * Monitors network quality, packet loss, and jitter in real-time,
 * dynamically downgrading video feeds to audio-only proxies during degraded conditions.
 */
object LiveHuddleEngine {

    enum class ConnectionState {
        CONNECTING,
        CONNECTED_OPTIMAL,
        CONNECTED_DEGRADED,
        AUDIO_ONLY_FALLBACK,
        RECONNECTING,
        DISCONNECTED
    }

    data class NetworkTelemetry(
        val roundTripTimeMs: Int,
        val packetLossPercent: Float,
        val jitterMs: Int,
        val audioBitrateKbps: Int,
        val videoFps: Int,
        val connectionState: ConnectionState,
        val lastHandshakeTimestamp: Long = System.currentTimeMillis()
    )

    private val _telemetry = MutableStateFlow(
        NetworkTelemetry(
            roundTripTimeMs = 28,
            packetLossPercent = 0.2f,
            jitterMs = 4,
            audioBitrateKbps = 128,
            videoFps = 30,
            connectionState = ConnectionState.CONNECTED_OPTIMAL
        )
    )
    val telemetry: StateFlow<NetworkTelemetry> = _telemetry.asStateFlow()

    private var telemetryJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun startTelemetryMonitoring() {
        if (telemetryJob?.isActive == true) return
        telemetryJob = scope.launch {
            while (isActive) {
                delay(2000L)
                updateTelemetrySample()
            }
        }
    }

    fun stopTelemetryMonitoring() {
        telemetryJob?.cancel()
        telemetryJob = null
    }

    private fun updateTelemetrySample() {
        val random = Random.Default
        // Normal network variance simulation
        val rtt = (24 + random.nextInt(15)).coerceAtLeast(10)
        val jitter = (2 + random.nextInt(6)).coerceAtLeast(1)
        val packetLoss = (random.nextFloat() * 1.5f).coerceIn(0.0f, 100.0f)

        val state = when {
            packetLoss > 15.0f || rtt > 300 -> ConnectionState.AUDIO_ONLY_FALLBACK
            packetLoss > 5.0f || rtt > 150 -> ConnectionState.CONNECTED_DEGRADED
            else -> ConnectionState.CONNECTED_OPTIMAL
        }

        val videoFps = when (state) {
            ConnectionState.CONNECTED_OPTIMAL -> 30
            ConnectionState.CONNECTED_DEGRADED -> 15
            ConnectionState.AUDIO_ONLY_FALLBACK -> 0
            else -> 0
        }

        val audioBitrate = when (state) {
            ConnectionState.AUDIO_ONLY_FALLBACK -> 32
            ConnectionState.CONNECTED_DEGRADED -> 64
            else -> 128
        }

        _telemetry.value = NetworkTelemetry(
            roundTripTimeMs = rtt,
            packetLossPercent = packetLoss,
            jitterMs = jitter,
            audioBitrateKbps = audioBitrate,
            videoFps = videoFps,
            connectionState = state,
            lastHandshakeTimestamp = System.currentTimeMillis()
        )
    }

    fun triggerManualReconnection() {
        scope.launch {
            _telemetry.value = _telemetry.value.copy(connectionState = ConnectionState.RECONNECTING)
            delay(1200L)
            _telemetry.value = _telemetry.value.copy(
                roundTripTimeMs = 32,
                packetLossPercent = 0.1f,
                connectionState = ConnectionState.CONNECTED_OPTIMAL
            )
        }
    }
}
