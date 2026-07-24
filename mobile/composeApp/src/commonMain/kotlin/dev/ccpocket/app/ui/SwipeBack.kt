package dev.ccpocket.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.ccpocket.app.SystemBackHandler
import dev.ccpocket.app.isPhonePlatform
import dev.ccpocket.app.theme.Tok
import kotlin.math.roundToInt

/** Every platform shows an explicit affordance; phones additionally keep edge swipe/system back. */
fun showBackButton(): Boolean = true

private val BackChevron: ImageVector by lazy {
    ImageVector.Builder(
        name = "BackChevron",
        defaultWidth = 22.dp,
        defaultHeight = 22.dp,
        viewportWidth = 22f,
        viewportHeight = 22f,
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2.05f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(13.5f, 4.5f)
            lineTo(7f, 11f)
            lineTo(13.5f, 17.5f)
        }
    }.build()
}

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

/** Compact iOS/X-style back control with a full 44dp accessible touch target. */
@Composable
fun BackTextButton(onBack: () -> Unit) {
    if (!showBackButton()) return
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(
        Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (pressed) Tok.tx.copy(alpha = 0.07f) else Color.Transparent)
            .clickable(interactionSource = interaction, indication = null, onClick = onBack),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        Icon(BackChevron, contentDescription = "Back", tint = Tok.tx, modifier = Modifier.size(22.dp))
    }
}
