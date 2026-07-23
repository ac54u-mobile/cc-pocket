package dev.ccpocket.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ccpocket.app.theme.Tok

/**
 * Compose-native port of Appica UI's public design tokens.
 *
 * Values are taken from Appica UI 1.0.0's shipped CSS variables. Components remain native Compose;
 * no WebView or React runtime is involved. CC Pocket's agent identity colors stay outside this
 * neutral system and can be supplied explicitly where product meaning requires them.
 */
@Immutable
data class AppicaPalette(
    val foreground: Color,
    val foregroundSubtle: Color,
    val foregroundMuted: Color,
    val foregroundStrong: Color,
    val foregroundEmphasis: Color,
    val foregroundIntense: Color,
    val foregroundInverse: Color,
    val background: Color,
    val backgroundSubtle: Color,
    val backgroundMuted: Color,
    val backgroundStrong: Color,
    val backgroundInverse: Color,
    val border: Color,
    val borderMuted: Color,
    val borderStrong: Color,
    val borderEmphasis: Color,
    val borderIntense: Color,
    val primary: Color,
    val primaryForeground: Color,
    val secondary: Color,
    val error: Color,
    val success: Color,
    val warning: Color,
    val info: Color,
    val shadow: Color,
)

internal val AppicaLightPalette = AppicaPalette(
    foreground = Color(0xFF4A5565), foregroundSubtle = Color(0xFF99A1AF),
    foregroundMuted = Color(0xFF6A7282), foregroundStrong = Color(0xFF364153),
    foregroundEmphasis = Color(0xFF1E2939), foregroundIntense = Color(0xFF101828),
    foregroundInverse = Color.White, background = Color.White,
    backgroundSubtle = Color(0xFFF9FAFB), backgroundMuted = Color(0xFFF3F4F6),
    backgroundStrong = Color(0xFFE5E7EB), backgroundInverse = Color(0xFF030712),
    border = Color(0xFFE5E7EB), borderMuted = Color(0xFFF3F4F6),
    borderStrong = Color(0xFFD1D5DC), borderEmphasis = Color(0xFF99A1AF),
    borderIntense = Color(0xFF6A7282), primary = Color(0xFF101828),
    primaryForeground = Color.White, secondary = Color(0xFF90C5FF),
    error = Color(0xFFFFA3A3), success = Color(0xFF5EE9B5),
    warning = Color(0xFFFFB96D), info = Color(0xFF77D4FF),
    shadow = Color(0x2E6A7282),
)

internal val AppicaDarkPalette = AppicaPalette(
    foreground = Color(0xFFD1D5DC), foregroundSubtle = Color(0xFF6A7282),
    foregroundMuted = Color(0xFF99A1AF), foregroundStrong = Color(0xFFE5E7EB),
    foregroundEmphasis = Color(0xFFF3F4F6), foregroundIntense = Color.White,
    foregroundInverse = Color(0xFF101828), background = Color(0xFF030712),
    backgroundSubtle = Color(0x146A7282), backgroundMuted = Color(0xFF101828),
    backgroundStrong = Color(0xFF1E2939), backgroundInverse = Color.White,
    border = Color(0xFF1E2939), borderMuted = Color(0xFF101828),
    borderStrong = Color(0xFF364153), borderEmphasis = Color(0xFF4A5565),
    borderIntense = Color(0xFF6A7282), primary = Color.White,
    primaryForeground = Color(0xFF101828), secondary = Color(0xFF54A2FF),
    error = Color(0xFFFF6568), success = Color(0xFF00D294),
    warning = Color(0xFFFF8B1A), info = Color(0xFF00BCFE),
    shadow = Color(0x4D000000),
)

object AppicaTok {
    val palette: AppicaPalette get() = if (Tok.current.dark) AppicaDarkPalette else AppicaLightPalette
    val foreground get() = palette.foreground
    val foregroundSubtle get() = palette.foregroundSubtle
    val foregroundMuted get() = palette.foregroundMuted
    val foregroundStrong get() = palette.foregroundStrong
    val foregroundEmphasis get() = palette.foregroundEmphasis
    val foregroundIntense get() = palette.foregroundIntense
    val foregroundInverse get() = palette.foregroundInverse
    val background get() = palette.background
    val backgroundSubtle get() = palette.backgroundSubtle
    val backgroundMuted get() = palette.backgroundMuted
    val backgroundStrong get() = palette.backgroundStrong
    val border get() = palette.border
    val borderMuted get() = palette.borderMuted
    val borderStrong get() = palette.borderStrong
    val borderEmphasis get() = palette.borderEmphasis
    val primary get() = palette.primary
    val primaryForeground get() = palette.primaryForeground
    val secondary get() = palette.secondary
    val error get() = palette.error
    val success get() = palette.success
    val warning get() = palette.warning
    val info get() = palette.info

    // Compact semantic aliases used while legacy screens migrate onto the Appica vocabulary.
    val base get() = backgroundSubtle
    val surface get() = background
    val raised get() = backgroundMuted
    val hair get() = border
    val tx get() = foregroundIntense
    val tx2 get() = foreground
    val muted get() = foregroundMuted
    val accent get() = primary
    val danger get() = error
    val warn get() = warning
}

object AppicaMetrics {
    val borderWidth = 1.dp
    val radius = 14.dp
    val radius2Xs = 8.dp
    val radiusXs = 10.dp
    val radiusSm = 12.dp
    val radiusMd = 14.dp
    val radiusLg = 16.dp
    val radiusXl = 18.dp
    val radius2Xl = 24.dp
    val controlSm = 32.dp
    val controlMd = 40.dp
    val controlLg = 48.dp
    const val motionFastMs = 150
    const val motionNormalMs = 200
}

enum class AppicaButtonVariant { PRIMARY, OUTLINE, SOFT, GHOST, DANGER }

@Composable
fun AppicaSurface(
    modifier: Modifier = Modifier,
    radius: Dp = AppicaMetrics.radius,
    border: Color = AppicaTok.border,
    background: Color = AppicaTok.background,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier.clip(RoundedCornerShape(radius)).background(background)
            .border(AppicaMetrics.borderWidth, border, RoundedCornerShape(radius)),
        content = content,
    )
}

@Composable
fun AppicaDivider(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(AppicaMetrics.borderWidth).background(AppicaTok.border))
}

@Composable
fun AppicaButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AppicaButtonVariant = AppicaButtonVariant.PRIMARY,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
) {
    val bg = when (variant) {
        AppicaButtonVariant.PRIMARY -> AppicaTok.primary
        AppicaButtonVariant.SOFT -> AppicaTok.backgroundMuted
        AppicaButtonVariant.DANGER -> AppicaTok.error
        else -> Color.Transparent
    }
    val fg = when (variant) {
        AppicaButtonVariant.PRIMARY -> AppicaTok.primaryForeground
        AppicaButtonVariant.DANGER -> Color(0xFF030712)
        else -> AppicaTok.foregroundIntense
    }
    val outline = if (variant == AppicaButtonVariant.OUTLINE) AppicaTok.borderStrong else Color.Transparent
    Box(
        modifier.clip(RoundedCornerShape(AppicaMetrics.radiusSm)).background(bg)
            .border(1.dp, outline, RoundedCornerShape(AppicaMetrics.radiusSm))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = fg.copy(alpha = if (enabled) 1f else 0.65f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun AppicaSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val track by animateColorAsState(
        if (checked) AppicaTok.primary else AppicaTok.backgroundStrong,
        tween(AppicaMetrics.motionFastMs), label = "appica-switch-track",
    )
    val thumb by animateColorAsState(
        if (checked) AppicaTok.primaryForeground else AppicaTok.background,
        tween(AppicaMetrics.motionFastMs), label = "appica-switch-thumb",
    )
    val offset by animateDpAsState(if (checked) 18.dp else 2.dp, tween(AppicaMetrics.motionFastMs), label = "appica-switch-offset")
    Box(
        modifier.size(width = 38.dp, height = 22.dp).clip(RoundedCornerShape(99.dp)).background(track)
            .border(1.dp, if (checked) Color.Transparent else AppicaTok.borderStrong, RoundedCornerShape(99.dp))
            .clickable(
                enabled = enabled,
                role = Role.Switch,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onCheckedChange(!checked) },
    ) {
        Box(
            Modifier.padding(start = offset, top = 2.dp).size(18.dp).clip(RoundedCornerShape(99.dp)).background(thumb),
        )
    }
}

@Composable
fun AppicaInlineControl(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(AppicaMetrics.radiusSm))
            .background(AppicaTok.background).border(1.dp, AppicaTok.borderStrong, RoundedCornerShape(AppicaMetrics.radiusSm))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
fun AppicaOverlayScrim(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit = {}) {
    Box(modifier.background(Color.Black.copy(alpha = 0.48f)), content = content)
}
