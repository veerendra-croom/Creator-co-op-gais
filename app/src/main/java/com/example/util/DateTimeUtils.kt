package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateTimeUtils {
    private val fullDateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun formatFull(timestamp: Long): String = fullDateFormat.format(Date(timestamp))
    
    fun formatShort(timestamp: Long): String = shortDateFormat.format(Date(timestamp))
    
    fun formatTime(timestamp: Long): String = timeFormat.format(Date(timestamp))

    fun getRelativeTimeSpanString(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> "${diff / TimeUnit.MINUTES.toMillis(1)}m ago"
            diff < TimeUnit.DAYS.toMillis(1) -> "${diff / TimeUnit.HOURS.toMillis(1)}h ago"
            diff < TimeUnit.DAYS.toMillis(7) -> "${diff / TimeUnit.DAYS.toMillis(1)}d ago"
            else -> formatShort(timestamp)
        }
    }
}
