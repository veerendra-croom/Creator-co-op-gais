package com.example.ui.feedback

import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

enum class FeedbackType {
    SUCCESS,
    WARNING,
    ERROR,
    INFO
}

data class FeedbackMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val message: String,
    val type: FeedbackType = FeedbackType.INFO,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
    val duration: SnackbarDuration = SnackbarDuration.Short
)

object FeedbackManager {
    private val _messages = MutableSharedFlow<FeedbackMessage>(extraBufferCapacity = 16)
    val messages = _messages.asSharedFlow()

    var currentMessage = androidx.compose.runtime.mutableStateOf<FeedbackMessage?>(null)
        private set

    fun showMessage(
        message: String,
        type: FeedbackType = FeedbackType.INFO,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        val msg = FeedbackMessage(
            message = message,
            type = type,
            actionLabel = actionLabel,
            onAction = onAction,
            duration = duration
        )
        currentMessage.value = msg
        _messages.tryEmit(msg)
    }

    fun showSuccess(message: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
        showMessage(message, FeedbackType.SUCCESS, actionLabel, onAction)
    }

    fun showWarning(message: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
        showMessage(message, FeedbackType.WARNING, actionLabel, onAction)
    }

    fun showError(message: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
        showMessage(message, FeedbackType.ERROR, actionLabel, onAction)
    }

    fun showInfo(message: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
        showMessage(message, FeedbackType.INFO, actionLabel, onAction)
    }

    fun clearMessage() {
        currentMessage.value = null
    }
}
