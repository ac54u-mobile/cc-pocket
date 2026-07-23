package dev.ccpocket.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ccpocket.app.feedback.rememberAppHaptics
import dev.ccpocket.app.resources.Res
import dev.ccpocket.app.resources.settings_back
import dev.ccpocket.app.ui.AppicaTok as Tok
import org.jetbrains.compose.resources.stringResource

/** Density + motion tokens for Settings surfaces (phone Hub + detail panes). */
object SettingsMetrics {
    val rowMin = 64.dp
    val rowPadH = 16.dp
    val rowPadV = 12.dp
    val rowPadVCompact = 10.dp
    val sectionTop = 20.dp
    val sectionBottom = 8.dp
    val cardRadius = AppicaMetrics.radius
    val indicatorW = 3.dp
    val indicatorH = 22.dp
    val animMs = AppicaMetrics.motionFastMs
}

/** Uppercase section heading used across Settings hub + detail panes. */
@Composable
fun SettingsSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        color = Tok.foregroundMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.35.sp,
        modifier = modifier.padding(
            start = 2.dp,
            top = SettingsMetrics.sectionTop,
            bottom = SettingsMetrics.sectionBottom,
        ),
    )
}

/** Grouped card: surface + hair border + shared radius. */
@Composable
fun SettingsCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    AppicaSurface(modifier.fillMaxWidth(), content = content)
}

@Composable
fun SettingsDivider() {
    AppicaDivider()
}

/** Leading accent bar — fades/scales in when [selected]. Replaces full-row accent fill. */
@Composable
fun SettingsSelectionIndicator(selected: Boolean, modifier: Modifier = Modifier) {
    val alpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(SettingsMetrics.animMs),
        label = "settings-indicator",
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.55f,
        animationSpec = tween(SettingsMetrics.animMs),
        label = "settings-indicator-scale",
    )
    Box(
        modifier
            .width(SettingsMetrics.indicatorW)
            .height(SettingsMetrics.indicatorH)
            .graphicsLayer {
                this.alpha = alpha
                scaleY = scale
            }
            .clip(RoundedCornerShape(2.dp))
            .background(Tok.primary),
    )
}

/** Top bar: back chevron + title. Phone hides the chevron (edge swipe via [BackNavHost]). */
@Composable
fun SettingsTopBar(title: String, onBack: () -> Unit, trailing: (@Composable () -> Unit)? = null) {
    val backLabel = stringResource(Res.string.settings_back)
    val haptics = rememberAppHaptics()
    val showBtn = showBackButton()
    Row(
        Modifier
            .fillMaxWidth()
            .background(Tok.surface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showBtn) {
            Box(
                Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(AppicaMetrics.radiusXs))
                    .border(1.dp, Tok.border, RoundedCornerShape(AppicaMetrics.radiusXs))
                    .semantics { contentDescription = backLabel }
                    .clickable {
                        haptics.tick()
                        onBack()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = null,
                    tint = Tok.tx2,
                    modifier = Modifier.size(20.dp),
                )
            }
        } else {
            Spacer(Modifier.width(12.dp))
        }
        Text(
            title,
            color = Tok.tx,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp,
            modifier = Modifier.weight(1f).padding(start = 5.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        trailing?.invoke()
        if (trailing == null) Spacer(Modifier.width(4.dp))
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(Tok.hair))
}

/**
 * Navigation row: optional leading icon, title, optional subtitle / trailing value, chevron.
 * Used on the Settings hub and inside cards that push a detail pane.
 */
@Composable
fun SettingsNavRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = Tok.accent,
    danger: Boolean = false,
) {
    val haptics = rememberAppHaptics()
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = SettingsMetrics.rowMin)
            .semantics {
                role = Role.Button
                contentDescription = listOfNotNull(title, subtitle, trailing).joinToString(", ")
            }
            .clickable {
                haptics.tick()
                onClick()
            }
            .padding(
                horizontal = SettingsMetrics.rowPadH,
                vertical = if (subtitle == null) SettingsMetrics.rowPadV else SettingsMetrics.rowPadVCompact,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Box(
                Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(AppicaMetrics.radiusXs))
                    .background(iconTint.copy(alpha = 0.12f))
                    .border(1.dp, iconTint.copy(alpha = 0.10f), RoundedCornerShape(AppicaMetrics.radiusXs)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.width(13.dp))
        }
        Column(Modifier.weight(1f).padding(end = 10.dp)) {
            Text(
                title,
                color = if (danger) Tok.danger else Tok.tx,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    color = Tok.muted,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(top = 3.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (trailing != null) {
            Text(trailing, color = Tok.tx2, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.width(6.dp))
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = Tok.foregroundSubtle, modifier = Modifier.size(18.dp))
    }
}

/** Title + subtitle on the left, Material Switch on the right. */
@Composable
fun SettingsToggleRow(
    label: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    sub: String? = null,
    enabled: Boolean = true,
) {
    val haptics = rememberAppHaptics()
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = SettingsMetrics.rowMin)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = {
                    haptics.tick()
                    onChange(it)
                },
            )
            .padding(start = SettingsMetrics.rowPadH, end = 8.dp, top = 9.dp, bottom = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(label, color = Tok.tx, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            if (sub != null) Text(sub, color = Tok.muted, fontSize = 11.5.sp, lineHeight = 15.sp, modifier = Modifier.padding(top = 2.dp))
        }
        AppicaSwitch(checked = checked, onCheckedChange = onChange, enabled = enabled)
    }
}

/** Equal-width segmented control on a surface track; selected segment fills accent. */
@Composable
fun <T> SettingsSegmented(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onPick: (T) -> Unit,
    modifier: Modifier = Modifier,
    mono: Boolean = true,
) {
    val haptics = rememberAppHaptics()
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(Tok.raised)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEach { opt ->
            val sel = selected == opt
            val bg by animateColorAsState(
                targetValue = if (sel) Tok.accent else Color.Transparent,
                animationSpec = tween(SettingsMetrics.animMs),
                label = "seg-bg",
            )
            val fg by animateColorAsState(
                targetValue = if (sel) Tok.base else Tok.tx2,
                animationSpec = tween(SettingsMetrics.animMs),
                label = "seg-fg",
            )
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bg)
                    .clickable {
                        haptics.tick()
                        onPick(opt)
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label(opt),
                    color = fg,
                    fontFamily = if (mono) FontFamily.Monospace else FontFamily.Default,
                    fontSize = 11.5.sp,
                    fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                )
            }
        }
    }
}

/** Search field for the Settings hub. */
@Composable
fun SettingsSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SettingsMetrics.cardRadius))
            .background(Tok.surface)
            .border(1.dp, Tok.hair, RoundedCornerShape(SettingsMetrics.cardRadius))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Search, null, tint = Tok.muted, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(placeholder, color = Tok.muted, fontSize = 14.sp)
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(color = Tok.tx, fontSize = 14.sp),
                cursorBrush = SolidColor(Tok.accent),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (query.isNotEmpty()) {
            Box(
                Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onQueryChange("") },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Close, null, tint = Tok.muted, modifier = Modifier.size(16.dp))
            }
        }
    }
}

/** Compact status strip: connected computer name + optional action. */
@Composable
fun SettingsStatusStrip(
    title: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    val haptics = rememberAppHaptics()
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SettingsMetrics.cardRadius))
            .background(Tok.surface)
            .border(1.dp, Tok.hair, RoundedCornerShape(SettingsMetrics.cardRadius))
            .padding(horizontal = SettingsMetrics.rowPadH, vertical = SettingsMetrics.rowPadVCompact),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 10.dp)) {
            Text(title, color = Tok.tx, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, color = Tok.muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(
            actionLabel,
            color = Tok.accent,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    haptics.tick()
                    onAction()
                }
                .padding(horizontal = 8.dp, vertical = 6.dp),
        )
    }
}

/** Hint / footer under a control. */
@Composable
fun SettingsHint(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        color = Tok.muted,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        modifier = modifier.padding(top = 10.dp, start = 2.dp),
    )
}

/**
 * Consequence / scope callout — tells the user what a setting actually affects
 * (e.g. "new sessions only"). Info = neutral; warn = narrowed reach.
 */
@Composable
fun SettingsConsequenceHint(
    text: String,
    modifier: Modifier = Modifier,
    warn: Boolean = false,
) {
    val tint = if (warn) Tok.warn else Tok.info
    Text(
        text,
        color = tint,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        modifier = modifier
            .padding(top = 10.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(tint.copy(alpha = 0.09f))
            .border(1.dp, tint.copy(alpha = 0.28f), RoundedCornerShape(10.dp))
            .padding(horizontal = 11.dp, vertical = 9.dp),
    )
}

/**
 * Single-select list (preferred over long Segmented controls on small screens).
 * Leading accent indicator + soft wash when selected; light haptic on pick.
 */
@Composable
fun <T> SettingsChoiceList(
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onPick: (T) -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable (T) -> String? = { null },
    leading: (@Composable (T) -> Unit)? = null,
) {
    val haptics = rememberAppHaptics()
    SettingsCard(modifier) {
        options.forEachIndexed { i, opt ->
            if (i > 0) SettingsDivider()
            val sel = selected == opt
            val title = label(opt)
            val trail = trailing(opt)
            val wash by animateFloatAsState(
                targetValue = if (sel) 0.10f else 0f,
                animationSpec = tween(SettingsMetrics.animMs),
                label = "choice-wash",
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = SettingsMetrics.rowMin)
                    .background(Tok.accent.copy(alpha = wash))
                    .semantics {
                        role = Role.RadioButton
                        this.selected = sel
                        contentDescription = title
                    }
                    .clickable {
                        haptics.tick()
                        onPick(opt)
                    }
                    .padding(
                        start = 12.dp,
                        end = SettingsMetrics.rowPadH,
                        top = SettingsMetrics.rowPadVCompact,
                        bottom = SettingsMetrics.rowPadVCompact,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SettingsSelectionIndicator(sel)
                Spacer(Modifier.width(10.dp))
                if (leading != null) leading(opt)
                Text(
                    title,
                    color = if (sel) Tok.accent else Tok.tx,
                    fontSize = 14.sp,
                    fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (trail != null) {
                    Text(
                        trail,
                        color = Tok.muted,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.5.sp,
                        modifier = Modifier.padding(end = 8.dp),
                        maxLines = 1,
                    )
                }
                val checkAlpha by animateFloatAsState(
                    targetValue = if (sel) 1f else 0f,
                    animationSpec = tween(SettingsMetrics.animMs),
                    label = "choice-check",
                )
                Box(
                    Modifier
                        .size(24.dp)
                        .graphicsLayer { alpha = checkAlpha }
                        .clip(RoundedCornerShape(12.dp))
                        .background(Tok.accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Check, null, tint = Tok.base, modifier = Modifier.size(15.dp))
                }
            }
        }
    }
}
