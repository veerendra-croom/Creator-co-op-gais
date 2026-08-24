package com.example.ui.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity

object NotificationHelper {
    private const val TAG = "NotificationHelper"

    const val CHANNEL_AGREEMENTS = "channel_agreements"
    const val CHANNEL_SYNDICATE = "channel_syndicate"
    const val CHANNEL_MESSAGES = "channel_messages"
    const val CHANNEL_TASKS = "channel_tasks"
    const val CHANNEL_SAVED_SEARCHES = "saved_searches_notifications"
    const val CHANNEL_GENERAL = "channel_general"

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
                
                val channels = listOf(
                    NotificationChannel(CHANNEL_AGREEMENTS, "Agreements & Signatures", NotificationManager.IMPORTANCE_HIGH).apply {
                        description = "Alerts for new digital agreements, signing updates, and hash verifications."
                        enableVibration(true)
                    },
                    NotificationChannel(CHANNEL_SYNDICATE, "Syndicate & Co-Op Pitches", NotificationManager.IMPORTANCE_HIGH).apply {
                        description = "Alerts for pitch proposals, role matchmaking, and syndication milestones."
                        enableVibration(true)
                    },
                    NotificationChannel(CHANNEL_MESSAGES, "Direct & Team Messages", NotificationManager.IMPORTANCE_HIGH).apply {
                        description = "Direct messages and workspace chat notifications."
                        enableVibration(true)
                    },
                    NotificationChannel(CHANNEL_TASKS, "Tasks & Milestones", NotificationManager.IMPORTANCE_DEFAULT).apply {
                        description = "Updates on task assignments, kanban status, and sprint sprints."
                    },
                    NotificationChannel(CHANNEL_SAVED_SEARCHES, "Saved Searches Updates", NotificationManager.IMPORTANCE_DEFAULT).apply {
                        description = "Notifies users about new matches on their saved talent/role searches."
                    },
                    NotificationChannel(CHANNEL_GENERAL, "General Platform Updates", NotificationManager.IMPORTANCE_DEFAULT).apply {
                        description = "General system alerts and platform notifications."
                    }
                )
                channels.forEach { notificationManager.createNotificationChannel(it) }
            } catch (e: Throwable) {
                Log.w(TAG, "Failed to initialize notification channels", e)
            }
        }
    }

    fun showNotification(
        context: Context,
        title: String,
        message: String,
        channelId: String = CHANNEL_SAVED_SEARCHES,
        deepLinkUri: String? = null
    ) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

            initNotificationChannels(context)

            val intent = if (!deepLinkUri.isNullOrBlank()) {
                Intent(Intent.ACTION_VIEW, Uri.parse(deepLinkUri), context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            } else {
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                (System.currentTimeMillis() % 100000).toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(
                    if (channelId == CHANNEL_AGREEMENTS || channelId == CHANNEL_SYNDICATE || channelId == CHANNEL_MESSAGES)
                        NotificationCompat.PRIORITY_HIGH
                    else
                        NotificationCompat.PRIORITY_DEFAULT
                )
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "Notification skipped: POST_NOTIFICATIONS permission not granted.")
                    return
                }
            }

            notificationManager.notify((System.currentTimeMillis() % 100000).toInt(), notification)
        } catch (e: Throwable) {
            Log.w(TAG, "Failed to dispatch notification: ${e.message}", e)
        }
    }

    fun handleIncomingPayload(
        context: Context,
        data: Map<String, String>,
        fallbackTitle: String? = null,
        fallbackBody: String? = null
    ) {
        val type = data["type"] ?: "GENERAL"
        val title = data["title"] ?: fallbackTitle ?: "Creator Co-Op"
        val message = data["message"] ?: data["body"] ?: fallbackBody ?: "You have a new update."
        val targetId = data["target_id"] ?: data["id"] ?: ""

        val (channelId, deepLinkUri) = when (type.uppercase()) {
            "AGREEMENT", "SIGNATURE" -> Pair(
                CHANNEL_AGREEMENTS,
                if (targetId.isNotBlank()) "https://creator-studio.app/workspace/$targetId" else null
            )
            "PITCH", "SYNDICATE" -> Pair(
                CHANNEL_SYNDICATE,
                if (targetId.isNotBlank()) "https://creator-studio.app/pitch/$targetId" else null
            )
            "ROLE", "JOB" -> Pair(
                CHANNEL_SYNDICATE,
                if (targetId.isNotBlank()) "https://creator-studio.app/role/$targetId" else null
            )
            "MESSAGE", "CHAT" -> Pair(
                CHANNEL_MESSAGES,
                if (targetId.isNotBlank()) "https://creator-studio.app/workspace/$targetId" else null
            )
            "USER", "PROFILE" -> Pair(
                CHANNEL_GENERAL,
                if (targetId.isNotBlank()) "https://creator-studio.app/u/$targetId" else null
            )
            "TASK" -> Pair(
                CHANNEL_TASKS,
                if (targetId.isNotBlank()) "https://creator-studio.app/workspace/$targetId" else null
            )
            else -> Pair(CHANNEL_GENERAL, null)
        }

        showNotification(context, title, message, channelId, deepLinkUri)
    }
}
