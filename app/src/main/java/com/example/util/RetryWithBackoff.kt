package com.example.util

import android.util.Log
import kotlinx.coroutines.delay

object RetryWithBackoff {
    private const val TAG = "RetryWithBackoff"

    /**
     * Executes a network or database block with exponential backoff.
     */
    suspend fun <T> execute(
        maxAttempts: Int = 3,
        initialDelayMs: Long = 1000L,
        factor: Double = 2.0,
        block: suspend () -> T
    ): T? {
        var currentDelay = initialDelayMs
        for (attempt in 1..maxAttempts) {
            try {
                return block()
            } catch (e: Exception) {
                Log.w(TAG, "Attempt $attempt of $maxAttempts failed: ${e.message}. Retrying in ${currentDelay}ms...")
                PlatformExceptionGuard.logException(e, "NETWORK_API_RETRY_ATTEMPT_${attempt}_FAILED")
                if (attempt == maxAttempts) {
                    Log.e(TAG, "All $maxAttempts retry attempts exhausted.")
                    throw e
                }
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong()
            }
        }
        return null
    }
}
