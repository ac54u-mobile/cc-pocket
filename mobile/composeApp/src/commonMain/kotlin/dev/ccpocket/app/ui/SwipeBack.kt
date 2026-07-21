package dev.ccpocket.app.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ccpocket.app.SystemBackHandler
import dev.ccpocket.app.isPhonePlatform
import dev.ccpocket.app.theme.Tok
import kotlin.math.roundToInt

/** Phone: hide ← / ‹ affordances and use edge swipe (+ Android system back). Desktop: keep buttons. */
fun showBackButton(): Boolean = !isPhonePlatform()

/**
 * Left-edge swipe (finger moves right) triggers [onBack]. No-op when [enabled] is false.
 * Slight content follow during the drag; snaps back if the gesture is cancelled.
 */
fun Modifier.swipeBack(enabled: Boolean, onBack: () -> Unit): Modifier = composed {
    if (!enabled) return@composed this
    val density = LocalDensity.current
    val edgePx = with(density) { 28.dp.toPx() }
    val thresholdPx = with(density) { 72.dp.toPx() }
    var dragX by remember { mutableFloatStateOf(0f) }
    this
        .offset { IntOffset(dragX.roundToInt().coerceAtLeast(0), 0) }
        .pointerInput(onBack, edgePx, thresholdPx) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                if (down.position.x > edgePx) return@awaitEachGesture
                var total = 0f
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull() ?: break
                    if (!change.pressed) {
                        if (total >= thresholdPx) onBack()
                        dragX = 0f
                        break
                    }
                    val dx = change.positionChange().x
                    if (dx != 0f) {
                        change.consume()
                        total = (total + dx).coerceAtLeast(0f)
                        dragX = total.coerceAtMost(thresholdPx * 1.4f)
                    }
                }
            }
        }
}

/**
 * System back (Android) + phone edge-swipe around [content]. Prefer this over a bare
 * [SystemBackHandler] on full-screen panes that used to show a ← button.
 */
@Composable
fun BackNavHost(
    onBack: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    SystemBackHandler(enabled = enabled, onBack = onBack)
    Box(
        modifier
            .fillMaxSize()
            .swipeBack(enabled = enabled && isPhonePlatform(), onBack = onBack),
        content = content,
    )
}

/** Desktop-only ← TextButton; phone relies on [BackNavHost] edge swipe. */
@Composable
fun BackTextButton(onBack: () -> Unit, glyph: String = "←") {
    if (!showBackButton()) return
    TextButton(onClick = onBack) { Text(glyph, color = Tok.tx2, fontSize = 18.sp) }
}
