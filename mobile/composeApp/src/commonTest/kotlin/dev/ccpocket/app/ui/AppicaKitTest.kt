package dev.ccpocket.app.ui

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AppicaKitTest {
    @Test
    fun lightPaletteMatchesAppicaUi100CssTokens() {
        assertEquals(Color.White, AppicaLightPalette.background)
        assertEquals(Color(0xFFF9FAFB), AppicaLightPalette.backgroundSubtle)
        assertEquals(Color(0xFFE5E7EB), AppicaLightPalette.border)
        assertEquals(Color(0xFF101828), AppicaLightPalette.primary)
        assertEquals(Color(0xFF4A5565), AppicaLightPalette.foreground)
    }

    @Test
    fun darkPaletteMatchesAppicaUi100CssTokens() {
        assertEquals(Color(0xFF030712), AppicaDarkPalette.background)
        assertEquals(Color(0xFF1E2939), AppicaDarkPalette.border)
        assertEquals(Color.White, AppicaDarkPalette.primary)
        assertEquals(Color(0xFFD1D5DC), AppicaDarkPalette.foreground)
    }

    @Test
    fun themesMaintainContrastPolarity() {
        assertNotEquals(AppicaLightPalette.background, AppicaLightPalette.foregroundIntense)
        assertNotEquals(AppicaDarkPalette.background, AppicaDarkPalette.foregroundIntense)
        assertEquals(AppicaLightPalette.primaryForeground, AppicaDarkPalette.primary)
        assertEquals(AppicaDarkPalette.primaryForeground, AppicaLightPalette.primary)
    }

    @Test
    fun appicaBaseRadiusIsFourteenDp() {
        assertEquals(14f, AppicaMetrics.radius.value)
        assertEquals(150, AppicaMetrics.motionFastMs)
    }

    @Test
    fun avatarInitialsHandleNamesAndEmptyValues() {
        assertEquals("CC", initials("Claude Code"))
        assertEquals("小明", initials("小 明"))
        assertEquals("?", initials("  "))
    }

    @Test
    fun sparklineNormalizationHandlesRangeAndFlatSeries() {
        assertEquals(listOf(0f, .5f, 1f), sparklineNormalized(listOf(2f, 4f, 6f)))
        assertTrue(sparklineNormalized(listOf(3f, 3f)).all { it == .5f })
        assertTrue(sparklineNormalized(emptyList()).isEmpty())
    }

    @Test
    fun calendarCalculationsHandleLeapYearsAndMonthOffsets() {
        assertTrue(isLeapYear(2024))
        assertTrue(!isLeapYear(2100))
        assertEquals(29, daysInMonth(2024, 2))
        assertEquals(28, daysInMonth(2025, 2))
        assertEquals(3, firstWeekday(2026, 1)) // Thursday, Monday-based.
    }

    @Test
    fun appicaDateUsesStableIsoFormatting() {
        assertEquals("2026-07-03", AppicaDate(2026, 7, 3).toString())
    }

    @Test
    fun scrollChromeAccumulatorResetsAsSoonAsDirectionReverses() {
        assertEquals(13f, accumulateScrollDelta(8f, 5f))
        assertEquals(-3f, accumulateScrollDelta(13f, -3f))
        assertEquals(-10f, accumulateScrollDelta(-3f, -7f))
        assertEquals(4f, accumulateScrollDelta(-10f, 4f))
    }
}
