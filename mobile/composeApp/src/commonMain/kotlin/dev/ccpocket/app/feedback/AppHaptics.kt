package dev.ccpocket.app.feedback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Light selection tick for Settings (and similar lists).
 * Backed by Compose [LocalHapticFeedback]: real on Android / iOS where supported, no-op elsewhere.
 */
fun interface AppHaptics {
    fun tick()
}

@Composable
fun rememberAppHaptics(): AppHaptics {
    val haptic = LocalHapticFeedback.current
    return remember(haptic) {
        AppHaptics {
            // TextHandleMove is the lightest standard tick across Compose targets
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
}
