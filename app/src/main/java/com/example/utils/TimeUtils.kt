package com.example.utils

import android.text.format.DateUtils

object TimeUtils {
    fun formatRelative(timestamp: Long): String {
        return DateUtils.getRelativeTimeSpanString(
            timestamp,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        ).toString()
    }
}
