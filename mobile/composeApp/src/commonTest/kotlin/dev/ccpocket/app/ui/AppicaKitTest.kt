package dev.ccpocket.app.ui

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

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
}
