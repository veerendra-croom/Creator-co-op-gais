package com.example.data.supabase

import android.util.Log

/**
 * Catches PostgrestException errors and checks for security codes like 'P0001' (spam).
 */
object ClientSecurityInterceptor {
    private const val TAG = "SecurityInterceptor"

    fun checkAndWrap(block: () -> Unit) {
        try {
            block()
        } catch (e: Throwable) {
            handleSecurityError(e)
            throw e
        }
    }

    suspend fun <T> wrap(block: suspend () -> T): T {
        return try {
            block()
        } catch (e: Throwable) {
            handleSecurityError(e)
            throw e
        }
    }

    private fun handleSecurityError(e: Throwable) {
        if (e.message?.contains("P0001") == true) {
            Log.e(TAG, "PRODUCTION ALERT: Security threshold P0001 triggered (Spam Protection Active)")
        }
        
        // SSL Handshake or certificate pinning validation failure checks
        if (e is javax.net.ssl.SSLPeerUnverifiedException || 
            e.cause is javax.net.ssl.SSLPeerUnverifiedException || 
            e.message?.contains("Certificate pinning") == true ||
            e.cause?.message?.contains("Certificate pinning") == true) {
            Log.e(TAG, "CRITICAL: SSL Handshake interception detected!")
            RuntimeSecurityGuard.triggerBreach("SSL/TLS interception detected: Certificate pinning mismatch (MitM mitigation).")
        }
    }
}
