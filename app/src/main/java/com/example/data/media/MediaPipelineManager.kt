package com.example.data.media

import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * High-Performance Media Pipeline & Video Asset Stream Buffering Engine.
 * Manages video thumbnail caching, waveform extraction for audio stems,
 * dynamic bitrate adaptation, and scrub buffer telemetry.
 */
object MediaPipelineManager {

    enum class StreamQuality(val label: String, val targetBitrateKbps: Int, val resolution: String) {
        UHD_4K("4K Master", 25_000, "3840x2160"),
        FHD_1080P("1080p High", 8_000, "1920x1080"),
        HD_720P("720p Proxy", 3_500, "1280x720"),
        SD_480P("480p Preview", 1_200, "854x480"),
        AUDIO_STEM("Audio Only", 320, "N/A")
    }

    data class BufferTelemetry(
        val assetId: String,
        val bufferedPercentage: Float, // 0.0 to 1.0
        val currentQuality: StreamQuality,
        val isBuffering: Boolean,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val audioWaveformPeaks: List<Float>
    )

    // Memory LRU Cache for extracted waveform vectors
    private val waveformCache = LruCache<String, List<Float>>(50)
    
    // Memory LRU Cache for video metadata proxies
    private val proxyCache = LruCache<String, String>(100)

    private val _activeBufferState = MutableStateFlow<Map<String, BufferTelemetry>>(emptyMap())
    val activeBufferState: StateFlow<Map<String, BufferTelemetry>> = _activeBufferState.asStateFlow()

    /**
     * Generates or retrieves cached audio waveform peak data for rough cut review.
     */
    suspend fun getAudioWaveformPeaks(assetId: String, sampleCount: Int = 40): List<Float> = withContext(Dispatchers.Default) {
        val cached = waveformCache.get(assetId)
        if (cached != null) return@withContext cached

        // Deterministically compute synthetic sound wave peaks based on assetId hash
        val seed = assetId.hashCode().toLong()
        val random = Random(seed)
        val peaks = (0 until sampleCount).map {
            (0.15f + random.nextFloat() * 0.85f).coerceIn(0.1f, 1.0f)
        }
        waveformCache.put(assetId, peaks)
        peaks
    }

    /**
     * Adapts stream quality automatically according to observed network latency and packet loss.
     */
    fun selectOptimalStreamQuality(bandwidthKbps: Int, isMeteredConnection: Boolean): StreamQuality {
        return when {
            isMeteredConnection && bandwidthKbps < 5_000 -> StreamQuality.HD_720P
            bandwidthKbps >= 30_000 -> StreamQuality.UHD_4K
            bandwidthKbps >= 10_000 -> StreamQuality.FHD_1080P
            bandwidthKbps >= 4_000 -> StreamQuality.HD_720P
            bandwidthKbps >= 1_500 -> StreamQuality.SD_480P
            else -> StreamQuality.AUDIO_STEM
        }
    }

    /**
     * Updates active buffer scrubbing telemetry for UI feedback components.
     */
    fun updateBufferTelemetry(
        assetId: String,
        bufferedPercent: Float,
        quality: StreamQuality,
        isBuffering: Boolean,
        totalBytes: Long = 104_857_600L // 100 MB default proxy
    ) {
        val current = _activeBufferState.value.toMutableMap()
        val cachedPeaks = waveformCache.get(assetId) ?: emptyList()
        current[assetId] = BufferTelemetry(
            assetId = assetId,
            bufferedPercentage = bufferedPercent.coerceIn(0.0f, 1.0f),
            currentQuality = quality,
            isBuffering = isBuffering,
            downloadedBytes = (totalBytes * bufferedPercent).toLong(),
            totalBytes = totalBytes,
            audioWaveformPeaks = cachedPeaks
        )
        _activeBufferState.value = current
    }

    /**
     * Clears cached memory resources during low-memory system warnings.
     */
    fun onLowMemory() {
        waveformCache.evictAll()
        proxyCache.evictAll()
    }
}
