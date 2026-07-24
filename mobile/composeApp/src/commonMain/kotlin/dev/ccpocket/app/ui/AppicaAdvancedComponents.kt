@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package dev.ccpocket.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay

@Composable
fun <T> AppicaAutocomplete(
    query: String,
    onQueryChange: (String) -> Unit,
    options: List<T>,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    emptyLabel: String = "No results",
    clearable: Boolean = true,
    expanded: Boolean = query.isNotBlank(),
) {
    val matches = remember(query, options) {
        if (query.isBlank()) options else options.filter { label(it).contains(query, ignoreCase = true) }
    }
    Column(modifier) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = AppicaMetrics.controlLg)
                .clip(RoundedCornerShape(AppicaMetrics.radiusSm)).background(AppicaTok.background)
                .border(1.dp, AppicaTok.borderStrong, RoundedCornerShape(AppicaMetrics.radiusSm))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Search, null, tint = AppicaTok.foregroundMuted, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(9.dp))
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                BasicTextField(
                    value = query, onValueChange = onQueryChange,
                    textStyle = TextStyle(color = AppicaTok.foregroundIntense, fontSize = 14.sp),
                    cursorBrush = SolidColor(AppicaTok.primary), singleLine = true, modifier = Modifier.fillMaxWidth(),
                )
                if (query.isEmpty()) Text(placeholder, color = AppicaTok.foregroundMuted, fontSize = 14.sp)
            }
            if (clearable && query.isNotEmpty()) {
                Box(Modifier.size(30.dp).clip(CircleShape).clickable { onQueryChange("") }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Close, null, tint = AppicaTok.foregroundMuted, modifier = Modifier.size(16.dp))
                }
            }
        }
        AnimatedVisibility(expanded) {
            Column(
                Modifier.padding(top = 6.dp).fillMaxWidth().heightIn(max = 260.dp)
                    .clip(RoundedCornerShape(AppicaMetrics.radiusSm)).background(AppicaTok.background)
                    .border(1.dp, AppicaTok.border, RoundedCornerShape(AppicaMetrics.radiusSm)).padding(4.dp),
            ) {
                if (matches.isEmpty()) Text(emptyLabel, color = AppicaTok.foregroundMuted, fontSize = 13.sp, modifier = Modifier.padding(12.dp))
                else matches.take(12).forEach { option ->
                    Text(
                        label(option), color = AppicaTok.foreground, fontSize = 13.sp, maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(AppicaMetrics.radiusXs))
                            .clickable { onSelect(option) }.padding(horizontal = 11.dp, vertical = 10.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun <T> AppicaCombobox(
    selected: T?, options: List<T>, label: (T) -> String, onSelect: (T) -> Unit,
    placeholder: String, modifier: Modifier = Modifier,
) {
    var open by remember { mutableStateOf(false) }
    var query by remember(selected) { mutableStateOf(selected?.let(label).orEmpty()) }
    AppicaAutocomplete(
        query, { query = it; open = true }, options, label,
        onSelect = { query = label(it); open = false; onSelect(it) }, placeholder = placeholder,
        modifier = modifier, expanded = open,
    )
}

enum class AppicaPresence { NONE, ONLINE, BUSY, AWAY }

@Composable
fun AppicaAvatar(
    name: String, modifier: Modifier = Modifier, size: Dp = 36.dp,
    presence: AppicaPresence = AppicaPresence.NONE,
) {
    val status = when (presence) {
        AppicaPresence.ONLINE -> AppicaTok.success
        AppicaPresence.BUSY -> AppicaTok.error
        AppicaPresence.AWAY -> AppicaTok.warning
        AppicaPresence.NONE -> Color.Transparent
    }
    Box(modifier.size(size)) {
        Box(
            Modifier.size(size).clip(CircleShape).background(AppicaTok.backgroundMuted)
                .border(1.dp, AppicaTok.border, CircleShape), contentAlignment = Alignment.Center,
        ) {
            Text(initials(name), color = AppicaTok.foregroundIntense, fontSize = (size.value * .34f).sp, fontWeight = FontWeight.SemiBold)
        }
        if (presence != AppicaPresence.NONE) Box(
            Modifier.align(Alignment.BottomEnd).size(size * .28f).clip(CircleShape).background(status)
                .border(2.dp, AppicaTok.background, CircleShape),
        )
    }
}

internal fun initials(name: String): String = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    .take(2).joinToString("") { it.take(1).uppercase() }.ifEmpty { "?" }

@Composable
fun AppicaCollapsible(
    expanded: Boolean, onExpandedChange: (Boolean) -> Unit, title: String,
    modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxWidth().animateContentSize()) {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(AppicaMetrics.radiusXs))
                .clickable(role = Role.Button) { onExpandedChange(!expanded) }.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, color = AppicaTok.foregroundIntense, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Icon(Icons.Rounded.ExpandMore, null, tint = AppicaTok.foregroundMuted, modifier = Modifier.size(18.dp).rotate(if (expanded) 180f else 0f))
        }
        AnimatedVisibility(expanded) { Column(Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp), content = content) }
    }
}

@Composable
fun AppicaAccordion(
    items: List<Pair<String, String>>, modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf<Int?>(null) }
    Column(modifier.fillMaxWidth().clip(RoundedCornerShape(AppicaMetrics.radiusSm)).border(1.dp, AppicaTok.border, RoundedCornerShape(AppicaMetrics.radiusSm))) {
        items.forEachIndexed { index, item ->
            if (index > 0) AppicaDivider()
            AppicaCollapsible(expanded == index, { expanded = if (it) index else null }, item.first) {
                Text(item.second, color = AppicaTok.foreground, fontSize = 12.5.sp, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
fun AppicaToolbar(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier.shadow(1.dp, RoundedCornerShape(AppicaMetrics.radiusSm), clip = false)
            .clip(RoundedCornerShape(AppicaMetrics.radiusSm)).background(AppicaTok.backgroundMuted)
            .padding(horizontal = 2.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        content = content,
    )
}

data class AppicaMenuItem(val label: String, val destructive: Boolean = false, val enabled: Boolean = true, val action: () -> Unit)

@Composable
fun AppicaDropdownMenu(expanded: Boolean, onDismiss: () -> Unit, items: List<AppicaMenuItem>) {
    DropdownMenu(
        expanded = expanded, onDismissRequest = onDismiss,
        modifier = Modifier.background(AppicaTok.background).border(1.dp, AppicaTok.border, RoundedCornerShape(AppicaMetrics.radiusSm)),
    ) {
        items.forEach { item ->
            DropdownMenuItem(
                text = { Text(item.label, color = if (item.destructive) AppicaTok.error else AppicaTok.foreground, fontSize = 13.sp) },
                enabled = item.enabled, onClick = { onDismiss(); item.action() },
            )
        }
    }
}

@Composable
fun AppicaContextMenu(items: List<AppicaMenuItem>, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box(modifier.combinedClickable(onClick = {}, onLongClick = { open = true })) {
        content()
        AppicaDropdownMenu(open, { open = false }, items)
    }
}

@Composable
fun AppicaDialog(
    title: String, onDismiss: () -> Unit, modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}, content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier.fillMaxWidth().clip(RoundedCornerShape(AppicaMetrics.radius2Xl)).background(AppicaTok.background)
                .border(1.dp, AppicaTok.border, RoundedCornerShape(AppicaMetrics.radius2Xl)).padding(20.dp),
        ) {
            Text(title, color = AppicaTok.foregroundIntense, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Column(Modifier.padding(top = 12.dp), content = content)
            Row(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalArrangement = Arrangement.End, content = actions)
        }
    }
}

@Composable
fun AppicaAlertDialog(
    title: String, body: String, confirmLabel: String, dismissLabel: String,
    onConfirm: () -> Unit, onDismiss: () -> Unit, destructive: Boolean = false,
) {
    AppicaDialog(title, onDismiss, actions = {
        AppicaButton(dismissLabel, onDismiss, variant = AppicaButtonVariant.GHOST)
        Spacer(Modifier.width(8.dp))
        AppicaButton(confirmLabel, onConfirm, variant = if (destructive) AppicaButtonVariant.DANGER else AppicaButtonVariant.PRIMARY)
    }) { Text(body, color = AppicaTok.foreground, fontSize = 13.sp, lineHeight = 19.sp) }
}

enum class AppicaToastTone { DEFAULT, SUCCESS, ERROR }
data class AppicaToastMessage(val text: String, val tone: AppicaToastTone = AppicaToastTone.DEFAULT)

@Stable
class AppicaToastState {
    var current by mutableStateOf<AppicaToastMessage?>(null)
        private set
    fun show(text: String, tone: AppicaToastTone = AppicaToastTone.DEFAULT) { current = AppicaToastMessage(text, tone) }
    fun dismiss() { current = null }
}

@Composable fun rememberAppicaToastState() = remember { AppicaToastState() }

@Composable
fun AppicaToastHost(state: AppicaToastState, modifier: Modifier = Modifier) {
    val message = state.current
    LaunchedEffect(message) { if (message != null) { delay(2400); state.dismiss() } }
    AnimatedVisibility(message != null, modifier = modifier) {
        message?.let {
            val color = when (it.tone) {
                AppicaToastTone.SUCCESS -> AppicaTok.success
                AppicaToastTone.ERROR -> AppicaTok.error
                AppicaToastTone.DEFAULT -> AppicaTok.foregroundIntense
            }
            Row(
                Modifier.clip(RoundedCornerShape(AppicaMetrics.radiusSm)).background(AppicaTok.backgroundInverse)
                    .border(1.dp, AppicaTok.borderStrong, RoundedCornerShape(AppicaMetrics.radiusSm)).padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(color))
                Spacer(Modifier.width(9.dp))
                Text(it.text, color = AppicaTok.foregroundInverse, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun AppicaSkeleton(modifier: Modifier = Modifier, radius: Dp = AppicaMetrics.radiusXs) {
    val alpha by rememberInfiniteTransition(label = "skeleton").animateFloat(
        .45f, .85f, infiniteRepeatable(tween(850), RepeatMode.Reverse), label = "skeleton-alpha",
    )
    Box(modifier.clip(RoundedCornerShape(radius)).background(AppicaTok.backgroundStrong.copy(alpha = alpha)))
}

@Composable
fun AppicaCountdown(seconds: Int, modifier: Modifier = Modifier) {
    val safe = seconds.coerceAtLeast(0)
    val hours = safe / 3600
    val minutes = (safe % 3600) / 60
    val secs = safe % 60
    val mm = minutes.toString().padStart(2, '0')
    val ss = secs.toString().padStart(2, '0')
    val text = if (hours > 0) "${hours.toString().padStart(2, '0')}:$mm:$ss" else "$mm:$ss"
    Text(
        text, color = AppicaTok.foregroundIntense, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
        modifier = modifier.clip(RoundedCornerShape(AppicaMetrics.radiusXs)).background(AppicaTok.backgroundMuted)
            .border(1.dp, AppicaTok.border, RoundedCornerShape(AppicaMetrics.radiusXs)).padding(horizontal = 9.dp, vertical = 5.dp),
    )
}

internal fun sparklineNormalized(values: List<Float>): List<Float> {
    if (values.isEmpty()) return emptyList()
    val min = values.minOrNull() ?: 0f
    val max = values.maxOrNull() ?: min
    if (max == min) return values.map { .5f }
    return values.map { (it - min) / (max - min) }
}

@Composable
fun AppicaSparkline(
    values: List<Float>, modifier: Modifier = Modifier, color: Color = AppicaTok.primary,
) {
    val normalized = remember(values) { sparklineNormalized(values) }
    Canvas(modifier.fillMaxWidth().height(44.dp)) {
        if (normalized.size < 2) return@Canvas
        val path = Path()
        normalized.forEachIndexed { index, value ->
            val x = size.width * index / (normalized.lastIndex.toFloat())
            val y = size.height * (1f - value)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()))
    }
}
