package dev.ccpocket.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class AppicaStatus { NEUTRAL, INFO, SUCCESS, WARNING, ERROR }

private fun appicaStatusColor(status: AppicaStatus): Color = when (status) {
    AppicaStatus.NEUTRAL -> AppicaTok.foreground
    AppicaStatus.INFO -> AppicaTok.info
    AppicaStatus.SUCCESS -> AppicaTok.success
    AppicaStatus.WARNING -> AppicaTok.warning
    AppicaStatus.ERROR -> AppicaTok.error
}

@Composable
fun AppicaBadge(label: String, modifier: Modifier = Modifier, status: AppicaStatus = AppicaStatus.NEUTRAL) {
    val color = appicaStatusColor(status)
    Text(
        label,
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        modifier = modifier.clip(RoundedCornerShape(99.dp)).background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.22f), RoundedCornerShape(99.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@Composable
fun AppicaChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg by animateColorAsState(if (selected) AppicaTok.primary else AppicaTok.background, tween(150), label = "chip-bg")
    val fg by animateColorAsState(if (selected) AppicaTok.primaryForeground else AppicaTok.foreground, tween(150), label = "chip-fg")
    Row(
        modifier.clip(RoundedCornerShape(99.dp)).background(bg)
            .border(1.dp, if (selected) Color.Transparent else AppicaTok.borderStrong, RoundedCornerShape(99.dp))
            .clickable(role = Role.Button, onClick = onClick).padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (selected) {
            Icon(Icons.Rounded.Check, null, tint = fg, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(5.dp))
        }
        Text(label, color = fg, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun AppicaCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val bg by animateColorAsState(if (checked) AppicaTok.primary else Color.Transparent, tween(150), label = "check-bg")
    Box(
        modifier.size(20.dp).clip(RoundedCornerShape(6.dp)).background(bg)
            .border(1.dp, if (checked) AppicaTok.primary else AppicaTok.borderStrong, RoundedCornerShape(6.dp))
            .clickable(enabled = enabled, role = Role.Checkbox) { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center,
    ) {
        if (checked) Icon(Icons.Rounded.Check, null, tint = AppicaTok.primaryForeground, modifier = Modifier.size(14.dp))
    }
}

@Composable
fun AppicaRadio(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier.size(20.dp).clip(CircleShape).border(1.dp, if (selected) AppicaTok.primary else AppicaTok.borderStrong, CircleShape)
            .clickable(role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) Box(Modifier.size(10.dp).clip(CircleShape).background(AppicaTok.primary))
    }
}

@Composable
fun AppicaProgress(progress: Float, modifier: Modifier = Modifier, status: AppicaStatus = AppicaStatus.NEUTRAL) {
    val value by animateFloatAsState(progress.coerceIn(0f, 1f), tween(200), label = "appica-progress")
    val color = if (status == AppicaStatus.NEUTRAL) AppicaTok.primary else appicaStatusColor(status)
    Box(modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(99.dp)).background(AppicaTok.backgroundStrong)) {
        Box(Modifier.fillMaxWidth(value).height(6.dp).clip(RoundedCornerShape(99.dp)).background(color))
    }
}

@Composable
fun AppicaAlert(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    status: AppicaStatus = AppicaStatus.INFO,
) {
    val color = appicaStatusColor(status)
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(AppicaMetrics.radiusSm)).background(color.copy(alpha = 0.10f))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(AppicaMetrics.radiusSm))
            .padding(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(Modifier.padding(top = 5.dp).size(7.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(title, color = AppicaTok.foregroundIntense, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(body, color = AppicaTok.foreground, fontSize = 12.sp, lineHeight = 17.sp, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
fun <T> AppicaTabs(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(AppicaMetrics.radiusSm)).background(AppicaTok.backgroundMuted).padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEach { option ->
            val active = option == selected
            val bg by animateColorAsState(if (active) AppicaTok.background else Color.Transparent, tween(150), label = "tab-bg")
            Box(
                Modifier.weight(1f).clip(RoundedCornerShape(AppicaMetrics.radiusXs)).background(bg)
                    .then(if (active) Modifier.border(1.dp, AppicaTok.border, RoundedCornerShape(AppicaMetrics.radiusXs)) else Modifier)
                    .clickable { onSelect(option) }.padding(vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label(option), color = if (active) AppicaTok.foregroundIntense else AppicaTok.foregroundMuted,
                    fontSize = 12.sp, fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
