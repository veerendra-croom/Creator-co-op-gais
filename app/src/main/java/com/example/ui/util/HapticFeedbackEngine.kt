package com.example.ui.util

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Unified Haptic Feedback & Accessibility Engine.
 * Enforces Android Material 3 tactile sensations and accessibility touch targets.
 */
object HapticFeedbackEngine {

    enum class HapticType {
        LIGHT_CLICK,
        DRAG_START,
        DROP_SNAP,
        CONFIRM_SUCCESS,
        REJECT_WARNING,
        LONG_PRESS
    }

    /**
     * Triggers distinct tactile haptic feedback on a target View.
     */
    fun performHaptic(view: View?, type: HapticType) {
        if (view == null) return
        when (type) {
            HapticType.LIGHT_CLICK -> {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }
            HapticType.DRAG_START -> {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
            HapticType.DROP_SNAP -> {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            HapticType.CONFIRM_SUCCESS -> {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            }
            HapticType.REJECT_WARNING -> {
                view.performHapticFeedback(HapticFeedbackConstants.REJECT)
            }
            HapticType.LONG_PRESS -> {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        }
    }
}

/**
 * Modifier extension to guarantee minimum 48x48dp interactive touch target bounds
 * in strict compliance with Android Material 3 accessibility standards.
 */
fun Modifier.accessibleTouchTarget(): Modifier = this.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
