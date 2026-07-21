package dev.ccpocket.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
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
import dev.ccpocket.app.resources.Res
import dev.ccpocket.app.resources.settings_back
import dev.ccpocket.app.theme.Tok
import org.jetbrains.compose.resources.stringResource

/** Uppercase section heading used across Settings hub + detail panes. */
@Composable
fun SettingsSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        color = Tok.muted,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.6.sp,
        modifier = modifier.padding(top = 16.dp, bottom = 8.dp),
    )
}

/** Grouped card: surface + hair border + 12dp radius. */
@Composable
fun SettingsCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Tok.surface)
            .border(1.dp, Tok.hair, RoundedCornerShape(12.dp)),
        content = content,
    )
}

@Composable
fun SettingsDivider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(Tok.hair))
}

/** Top bar: back chevron + title. */
@Composable
fun SettingsTopBar(title: String, onBack: () -> Unit, trailing: (@Composable () -> Unit)? = null) {
    val backLabel = stringResource(Res.string.settings_back)
    Row(
        Modifier.fillMaxWidth().background(Tok.surface).padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .semantics { contentDescription = backLabel }
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = null,
                tint = Tok.tx2,
                modifier = Modifier.size(20.dp),
            )
        }
        Text(
            title,
            color = Tok.tx,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f).padding(start = 2.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        trailing?.invoke()
        // silence unused when trailing null — keep layout stable
        if (trailing == null) Spacer(Modifier.width(4.dp))
    }
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
    iconTint: androidx.compose.ui.graphics.Color = Tok.accent,
    danger: Boolean = false,
) {
    Row(
        modifier
            .fillMaxWidth()
            .semantics {
                role = Role.Button
                contentDescription = listOfNotNull(title, subtitle, trailing).joinToString(", ")
            }
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = if (subtitle == null) 14.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Box(
                Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconTint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(17.dp))
            }
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f).padding(end = 10.dp)) {
            Text(
                title,
                color = if (danger) Tok.danger else Tok.tx,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    color = Tok.muted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (trailing != null) {
            Text(trailing, color = Tok.tx2, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.width(6.dp))
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = Tok.muted, modifier = Modifier.size(18.dp))
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
    Row(
        Modifier.fillMaxWidth().padding(start = 14.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(label, color = Tok.tx, fontSize = 14.sp)
            if (sub != null) Text(sub, color = Tok.muted, fontSize = 11.5.sp, lineHeight = 15.sp)
        }
        Switch(checked = checked, onCheckedChange = onChange, enabled = enabled)
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
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Tok.surface)
            .border(1.dp, Tok.hair, RoundedCornerShape(10.dp))
            .padding(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEach { opt ->
            val sel = selected == opt
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(7.dp))
                    .then(if (sel) Modifier.background(Tok.accent) else Modifier)
                    .clickable { onPick(opt) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label(opt),
                    color = if (sel) Tok.base else Tok.tx2,
                    fontFamily = if (mono) FontFamily.Monospace else FontFamily.Default,
                    fontSize = 11.sp,
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
            .clip(RoundedCornerShape(12.dp))
            .background(Tok.surface)
            .border(1.dp, Tok.hair, RoundedCornerShape(12.dp))
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
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Tok.surface)
            .border(1.dp, Tok.hair, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
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
                .clickable(onClick = onAction)
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
 * [trailing] is optional monospace hint (alias / token count).
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
    SettingsCard(modifier) {
        options.forEachIndexed { i, opt ->
            if (i > 0) SettingsDivider()
            val sel = selected == opt
            val title = label(opt)
            val trail = trailing(opt)
            Row(
                Modifier
                    .fillMaxWidth()
                    .semantics {
                        role = Role.RadioButton
                        this.selected = sel
                        contentDescription = title
                    }
                    .clickable { onPick(opt) }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
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
                if (sel) {
                    Icon(Icons.Rounded.Check, null, tint = Tok.accent, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
