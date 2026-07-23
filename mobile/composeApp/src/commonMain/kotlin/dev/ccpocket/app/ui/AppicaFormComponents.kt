package dev.ccpocket.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppicaButtonGroup(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier.clip(RoundedCornerShape(AppicaMetrics.radiusSm)).border(1.dp, AppicaTok.borderStrong, RoundedCornerShape(AppicaMetrics.radiusSm)),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
fun AppicaField(
    label: String,
    modifier: Modifier = Modifier,
    hint: String? = null,
    error: String? = null,
    required: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = AppicaTok.foregroundIntense, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            if (required) Text(" *", color = AppicaTok.error, fontSize = 12.sp)
        }
        content()
        val help = error ?: hint
        if (help != null) Text(help, color = if (error != null) AppicaTok.error else AppicaTok.foregroundMuted, fontSize = 11.sp)
    }
}

@Composable
fun AppicaForm(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
}

@Composable
fun AppicaInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    minHeight: Dp = AppicaMetrics.controlLg,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        textStyle = TextStyle(color = AppicaTok.foregroundIntense, fontSize = 14.sp, lineHeight = 20.sp),
        cursorBrush = SolidColor(AppicaTok.primary),
        modifier = modifier.fillMaxWidth(),
        decorationBox = { inner ->
            Box(
                Modifier.fillMaxWidth().heightIn(min = minHeight).clip(RoundedCornerShape(AppicaMetrics.radiusSm))
                    .background(if (enabled) AppicaTok.background else AppicaTok.backgroundMuted)
                    .border(1.dp, AppicaTok.borderStrong, RoundedCornerShape(AppicaMetrics.radiusSm)).padding(horizontal = 12.dp, vertical = 11.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isEmpty()) Text(placeholder, color = AppicaTok.foregroundMuted, fontSize = 14.sp)
                inner()
            }
        },
    )
}

@Composable
fun AppicaTextarea(value: String, onValueChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    AppicaInput(value, onValueChange, placeholder, modifier, singleLine = false, minHeight = 108.dp)
}

@Composable
fun AppicaNumberField(value: String, onValueChange: (String) -> Unit, placeholder: String = "0", modifier: Modifier = Modifier) {
    AppicaInput(value, { next -> if (next.isEmpty() || next.toDoubleOrNull() != null) onValueChange(next) }, placeholder, modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
}

@Composable
fun <T> AppicaSelect(
    selected: T?, options: List<T>, label: (T) -> String, onSelect: (T) -> Unit,
    placeholder: String, modifier: Modifier = Modifier,
) {
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        Row(
            Modifier.fillMaxWidth().height(AppicaMetrics.controlLg).clip(RoundedCornerShape(AppicaMetrics.radiusSm))
                .background(AppicaTok.background).border(1.dp, AppicaTok.borderStrong, RoundedCornerShape(AppicaMetrics.radiusSm))
                .clickable(role = Role.Button) { open = true }.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(selected?.let(label) ?: placeholder, color = if (selected == null) AppicaTok.foregroundMuted else AppicaTok.foregroundIntense,
                fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Icon(Icons.Rounded.ArrowDropDown, null, tint = AppicaTok.foregroundMuted, modifier = Modifier.size(20.dp))
        }
        AppicaDropdownMenu(open, { open = false }, options.map { option -> AppicaMenuItem(label(option)) { onSelect(option) } })
    }
}

@Composable
fun AppicaSlider(value: Float, onValueChange: (Float) -> Unit, modifier: Modifier = Modifier, range: ClosedFloatingPointRange<Float> = 0f..1f) {
    Slider(
        value = value.coerceIn(range), onValueChange = onValueChange, valueRange = range, modifier = modifier,
        colors = SliderDefaults.colors(thumbColor = AppicaTok.primary, activeTrackColor = AppicaTok.primary,
            inactiveTrackColor = AppicaTok.backgroundStrong),
    )
}

@Composable
fun AppicaToggle(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val background = if (selected) AppicaTok.backgroundStrong else Color.Transparent
    Text(
        label, color = if (selected) AppicaTok.foregroundIntense else AppicaTok.foreground,
        fontSize = 12.sp, fontWeight = FontWeight.Medium,
        modifier = modifier.height(AppicaMetrics.controlMd).clip(RoundedCornerShape(AppicaMetrics.radiusXs)).background(background)
            .clickable(role = Role.Button, onClick = onClick).padding(horizontal = 11.dp, vertical = 8.dp),
    )
}

@Composable
fun <T> AppicaToggleGroup(options: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.clip(RoundedCornerShape(AppicaMetrics.radiusSm)).background(AppicaTok.backgroundMuted).padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        options.forEach { AppicaToggle(label(it), it == selected, { onSelect(it) }, Modifier.weight(1f)) }
    }
}

@Composable
fun AppicaSeparator(modifier: Modifier = Modifier, vertical: Boolean = false) {
    Box(if (vertical) modifier.width(1.dp).height(24.dp).background(AppicaTok.border) else modifier.fillMaxWidth().height(1.dp).background(AppicaTok.border))
}

@Composable
fun AppicaSpinner(modifier: Modifier = Modifier, size: Dp = 20.dp) {
    CircularProgressIndicator(modifier.size(size), color = AppicaTok.primary, strokeWidth = 2.dp)
}

@Composable
fun AppicaLoader(label: String? = null, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        AppicaSpinner()
        if (label != null) Text(label, color = AppicaTok.foreground, fontSize = 13.sp)
    }
}

@Composable
fun AppicaMeter(value: Float, label: String, modifier: Modifier = Modifier, status: AppicaStatus = AppicaStatus.NEUTRAL) {
    val animated by animateFloatAsState(value.coerceIn(0f, 1f), tween(220), label = "appica-meter")
    Column(modifier, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Text(label, color = AppicaTok.foreground, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Text("${(animated * 100).toInt()}%", color = AppicaTok.foregroundIntense, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
        AppicaProgress(animated, status = status)
    }
}
