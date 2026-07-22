package dev.ccpocket.app.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import dev.ccpocket.app.feedback.rememberAppHaptics
import kotlin.math.min

/**
 * Dynamic-Island-inspired pull-to-refresh for the shared iOS/Android UI.
 *
 * Material3 still owns nested scrolling, thresholds and refresh dispatch. This layer only replaces
 * its indicator, so horizontal back gestures and LazyColumn's top detection retain platform-tested
 * behavior. The liquid neck is a vector metaball approximation (two cubic curves) rather than a
 * platform blur filter, which keeps the animation deterministic across Skia targets.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun IslandPullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val state = rememberPullToRefreshState()
    val haptics = rememberAppHaptics()
    val fraction = state.distanceFraction.coerceIn(0f, 1.35f)
    var thresholdTicked by remember { mutableStateOf(false) }
    var wasRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(fraction >= 1f, isRefreshing) {
        if (fraction >= 1f && !thresholdTicked) {
            thresholdTicked = true
            haptics.tick()
        } else if (fraction < 0.65f && !isRefreshing) {
            thresholdTicked = false
        }
        if (wasRefreshing && !isRefreshing) haptics.tick()
        wasRefreshing = isRefreshing
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        state = state,
        indicator = {
            IslandLiquidIndicator(
                fraction = fraction,
                refreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        },
    ) {
        // The source interaction pulls the content too. A restrained 18dp travel preserves that
        // physical connection without making long project lists visibly jump.
        Box(
            Modifier.fillMaxSize().graphicsLayer {
                translationY = min(fraction, 1f) * 18.dp.toPx()
            },
            content = content,
        )
    }
}

@Composable
private fun IslandLiquidIndicator(
    fraction: Float,
    refreshing: Boolean,
    modifier: Modifier = Modifier,
) {
    val rotation by rememberInfiniteTransition(label = "island-refresh").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(850, easing = LinearEasing), RepeatMode.Restart),
        label = "island-spinner",
    )
    val shown = if (refreshing) 1f else fraction.coerceIn(0f, 1.35f)

    Canvas(modifier.width(178.dp).height(92.dp).clipToBounds()) {
        val g = islandLiquidGeometry(shown, size.width, size.height)
        val ink = Color.Black

        if (g.neckVisible) {
            val neck = Path().apply {
                moveTo(g.sourceLeft, g.sourceY)
                cubicTo(g.sourceLeft, g.controlY, g.dropLeft, g.controlY, g.dropLeft, g.dropTop)
                lineTo(g.dropRight, g.dropTop)
                cubicTo(g.dropRight, g.controlY, g.sourceRight, g.controlY, g.sourceRight, g.sourceY)
                close()
            }
            drawPath(neck, ink)
        }

        drawRoundRect(
            color = ink,
            topLeft = androidx.compose.ui.geometry.Offset(g.islandLeft, g.islandTop),
            size = androidx.compose.ui.geometry.Size(g.islandWidth, g.islandHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(g.islandHeight / 2f),
        )
        drawCircle(ink, radius = g.dropRadius, center = androidx.compose.ui.geometry.Offset(g.centerX, g.dropY))

        if (g.dropRadius > 9f) {
            val ringRadius = g.dropRadius * 0.46f
            val ringTopLeft = androidx.compose.ui.geometry.Offset(g.centerX - ringRadius, g.dropY - ringRadius)
            val ringSize = androidx.compose.ui.geometry.Size(ringRadius * 2, ringRadius * 2)
            if (refreshing) {
                drawArc(
                    color = Color.White,
                    startAngle = rotation - 90f,
                    sweepAngle = 255f,
                    useCenter = false,
                    topLeft = ringTopLeft,
                    size = ringSize,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
                )
            } else {
                drawArc(
                    color = Color.White,
                    startAngle = -90f,
                    sweepAngle = 359.5f * fraction.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = ringTopLeft,
                    size = ringSize,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
                )
            }
        }
    }
}

internal data class IslandLiquidGeometry(
    val centerX: Float,
    val islandLeft: Float,
    val islandTop: Float,
    val islandWidth: Float,
    val islandHeight: Float,
    val sourceLeft: Float,
    val sourceRight: Float,
    val sourceY: Float,
    val controlY: Float,
    val dropY: Float,
    val dropRadius: Float,
    val dropLeft: Float,
    val dropRight: Float,
    val dropTop: Float,
    val neckVisible: Boolean,
)

/** Pure geometry kept separate so the detach curve can be verified without a Skia test runtime. */
internal fun islandLiquidGeometry(fraction: Float, width: Float, height: Float): IslandLiquidGeometry {
    val p = fraction.coerceIn(0f, 1.35f)
    val eased = 1f - (1f - min(p, 1f)) * (1f - min(p, 1f))
    val cx = width / 2f
    val islandWidth = min(width * 0.56f, 100f)
    val islandHeight = min(height * 0.27f, 24f)
    val islandTop = -islandHeight * 0.47f
    val sourceY = islandTop + islandHeight
    val dropRadius = 3.5f + 15.5f * eased
    val dropY = sourceY + eased * (height * 0.58f)
    val neckT = (p / 0.88f).coerceIn(0f, 1f)
    val sourceHalf = 17f * (1f - neckT * 0.52f)
    val dropHalf = dropRadius * (0.22f + neckT * 0.42f)
    val controlY = sourceY + (dropY - dropRadius - sourceY) * 0.54f
    return IslandLiquidGeometry(
        centerX = cx,
        islandLeft = cx - islandWidth / 2f,
        islandTop = islandTop,
        islandWidth = islandWidth,
        islandHeight = islandHeight,
        sourceLeft = cx - sourceHalf,
        sourceRight = cx + sourceHalf,
        sourceY = sourceY,
        controlY = controlY,
        dropY = dropY,
        dropRadius = dropRadius,
        dropLeft = cx - dropHalf,
        dropRight = cx + dropHalf,
        dropTop = dropY - dropRadius * 0.82f,
        neckVisible = p in 0.035f..0.88f,
    )
}
