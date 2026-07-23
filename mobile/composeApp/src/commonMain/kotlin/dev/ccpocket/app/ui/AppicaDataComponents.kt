package dev.ccpocket.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class AppicaDate(val year: Int, val month: Int, val day: Int) {
    init { require(month in 1..12); require(day in 1..daysInMonth(year, month)) }
    override fun toString(): String = "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
}

internal fun isLeapYear(year: Int) = year % 400 == 0 || (year % 4 == 0 && year % 100 != 0)

internal fun daysInMonth(year: Int, month: Int): Int = when (month) {
    2 -> if (isLeapYear(year)) 29 else 28
    4, 6, 9, 11 -> 30
    else -> 31
}

/** Monday=0 … Sunday=6, calculated without platform date APIs. */
internal fun firstWeekday(year: Int, month: Int): Int {
    var y = year
    var m = month
    if (m < 3) { m += 12; y-- }
    val zeller = (1 + 13 * (m + 1) / 5 + y + y / 4 - y / 100 + y / 400) % 7
    return (zeller + 5) % 7
}

@Composable
fun AppicaCalendar(
    displayedYear: Int,
    displayedMonth: Int,
    selected: AppicaDate?,
    onSelect: (AppicaDate) -> Unit,
    onMonthChange: (year: Int, month: Int) -> Unit,
    modifier: Modifier = Modifier,
    weekdayLabels: List<String> = listOf("M", "T", "W", "T", "F", "S", "S"),
) {
    val days = daysInMonth(displayedYear, displayedMonth)
    val offset = firstWeekday(displayedYear, displayedMonth)
    Column(
        modifier.fillMaxWidth().clip(RoundedCornerShape(AppicaMetrics.radiusLg)).background(AppicaTok.background)
            .border(1.dp, AppicaTok.border, RoundedCornerShape(AppicaMetrics.radiusLg)).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            CalendarNav(false) {
                if (displayedMonth == 1) onMonthChange(displayedYear - 1, 12) else onMonthChange(displayedYear, displayedMonth - 1)
            }
            Text("$displayedYear / ${displayedMonth.toString().padStart(2, '0')}", color = AppicaTok.foregroundIntense,
                fontSize = 14.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            CalendarNav(true) {
                if (displayedMonth == 12) onMonthChange(displayedYear + 1, 1) else onMonthChange(displayedYear, displayedMonth + 1)
            }
        }
        Row(Modifier.fillMaxWidth()) {
            weekdayLabels.take(7).forEach { Text(it, color = AppicaTok.foregroundMuted, fontSize = 11.sp,
                textAlign = TextAlign.Center, modifier = Modifier.weight(1f).padding(vertical = 4.dp)) }
        }
        repeat((offset + days + 6) / 7) { week ->
            Row(Modifier.fillMaxWidth()) {
                repeat(7) { weekday ->
                    val day = week * 7 + weekday - offset + 1
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        if (day in 1..days) {
                            val active = selected == AppicaDate(displayedYear, displayedMonth, day)
                            Box(
                                Modifier.size(36.dp).clip(CircleShape)
                                    .background(if (active) AppicaTok.primary else androidx.compose.ui.graphics.Color.Transparent)
                                    .clickable(role = Role.Button) { onSelect(AppicaDate(displayedYear, displayedMonth, day)) },
                                contentAlignment = Alignment.Center,
                            ) { Text(day.toString(), color = if (active) AppicaTok.primaryForeground else AppicaTok.foreground, fontSize = 12.sp) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarNav(next: Boolean, onClick: () -> Unit) {
    Box(Modifier.size(34.dp).clip(RoundedCornerShape(AppicaMetrics.radiusXs)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(if (next) Icons.AutoMirrored.Rounded.KeyboardArrowRight else Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
            null, tint = AppicaTok.foreground, modifier = Modifier.size(19.dp))
    }
}

@Composable
fun AppicaDateField(value: AppicaDate?, placeholder: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().heightIn(min = AppicaMetrics.controlLg).clip(RoundedCornerShape(AppicaMetrics.radiusSm))
            .background(AppicaTok.background).border(1.dp, AppicaTok.borderStrong, RoundedCornerShape(AppicaMetrics.radiusSm))
            .clickable(role = Role.Button, onClick = onClick).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(value?.toString() ?: placeholder, color = if (value == null) AppicaTok.foregroundMuted else AppicaTok.foregroundIntense,
            fontSize = 14.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Rounded.CalendarToday, null, tint = AppicaTok.foregroundMuted, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun AppicaDatePicker(value: AppicaDate?, onValueChange: (AppicaDate) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    var year by remember(value) { mutableStateOf(value?.year ?: 2026) }
    var month by remember(value) { mutableStateOf(value?.month ?: 1) }
    AppicaDateField(value, placeholder, { open = true }, modifier)
    if (open) AppicaDialog("Select date", { open = false }) {
        AppicaCalendar(year, month, value, { onValueChange(it); open = false }, { y, m -> year = y; month = m })
    }
}

@Composable
fun AppicaTimeField(hour: Int, minute: Int, onChange: (hour: Int, minute: Int) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        AppicaSelect(hour, (0..23).toList(), { it.toString().padStart(2, '0') }, { onChange(it, minute) }, "HH", Modifier.weight(1f))
        Text(":", color = AppicaTok.foregroundMuted, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        AppicaSelect(minute, (0..59).toList(), { it.toString().padStart(2, '0') }, { onChange(hour, it) }, "MM", Modifier.weight(1f))
    }
}

data class AppicaTableColumn<T>(val title: String, val weight: Float = 1f, val value: (T) -> String)

@Composable
fun <T> AppicaTable(rows: List<T>, columns: List<AppicaTableColumn<T>>, modifier: Modifier = Modifier, emptyLabel: String = "No data") {
    Column(modifier.fillMaxWidth().clip(RoundedCornerShape(AppicaMetrics.radiusSm)).border(1.dp, AppicaTok.border, RoundedCornerShape(AppicaMetrics.radiusSm))) {
        Row(Modifier.fillMaxWidth().background(AppicaTok.backgroundMuted).padding(horizontal = 12.dp, vertical = 10.dp)) {
            columns.forEach { TableCell(it.title, it.weight, true) }
        }
        if (rows.isEmpty()) Text(emptyLabel, color = AppicaTok.foregroundMuted, fontSize = 13.sp, modifier = Modifier.padding(16.dp))
        rows.forEach { row ->
            AppicaSeparator()
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 11.dp)) {
                columns.forEach { TableCell(it.value(row), it.weight, false) }
            }
        }
    }
}

@Composable
private fun RowScope.TableCell(value: String, weight: Float, header: Boolean) {
    Text(value, color = if (header) AppicaTok.foregroundMuted else AppicaTok.foreground, fontSize = if (header) 11.sp else 12.5.sp,
        fontWeight = if (header) FontWeight.Medium else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(weight).padding(end = 8.dp))
}

@Composable
fun AppicaScrollArea(modifier: Modifier = Modifier, maxHeight: Dp = 320.dp, horizontal: Boolean = false, content: @Composable () -> Unit) {
    val state = rememberScrollState()
    Box(modifier.fillMaxWidth().heightIn(max = maxHeight).clip(RoundedCornerShape(AppicaMetrics.radiusSm))) {
        Box(if (horizontal) Modifier.horizontalScroll(state) else Modifier.verticalScroll(state)) { content() }
    }
}
