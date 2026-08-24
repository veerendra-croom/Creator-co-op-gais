package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent

object CustomTabsHelper {
    /**
     * Opens an external portfolio link (YouTube, Behance, GitHub, custom domain) in a Chrome Custom Tab,
     * maintaining the user's in-app session and adopting the cosmic slate dark theme.
     */
    fun openUrl(context: Context, rawUrl: String) {
        if (rawUrl.isBlank()) return
        
        val trimmed = rawUrl.trim()
        val formattedUrl = when {
            trimmed.startsWith("http://", ignoreCase = true) || 
            trimmed.startsWith("https://", ignoreCase = true) || 
            trimmed.startsWith("mailto:", ignoreCase = true) -> trimmed
            else -> "https://$trimmed"
        }

        if (formattedUrl.startsWith("mailto:", ignoreCase = true)) {
            try {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse(formattedUrl)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "No email client found", Toast.LENGTH_SHORT).show()
            }
            return
        }

        try {
            val colorSchemeParams = CustomTabColorSchemeParams.Builder()
                .setToolbarColor(0xFF11151D.toInt()) // SurfaceColor Obsidian
                .setNavigationBarColor(0xFF090B0F.toInt()) // Background slate
                .build()

            val customTabsIntent = CustomTabsIntent.Builder()
                .setDefaultColorSchemeParams(colorSchemeParams)
                .setShowTitle(true)
                .setUrlBarHidingEnabled(true)
                .build()

            customTabsIntent.launchUrl(context, Uri.parse(formattedUrl))
        } catch (e: Exception) {
            // Fallback to standard ACTION_VIEW if custom tabs are unavailable
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
            } catch (e2: Exception) {
                Toast.makeText(context, "Unable to open portfolio link", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
