package dev.ccpocket.app.ui

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IslandPullRefreshTest {
    @Test
    fun dropGrowsAndTravelsAsThePullAdvances() {
        val start = islandLiquidGeometry(0f, 178f, 92f)
        val armed = islandLiquidGeometry(1f, 178f, 92f)

        assertTrue(armed.dropRadius > start.dropRadius)
        assertTrue(armed.dropY > start.dropY)
    }

    @Test
    fun liquidNeckConnectsThenDetachesPastThreshold() {
        assertTrue(islandLiquidGeometry(0.45f, 178f, 92f).neckVisible)
        assertFalse(islandLiquidGeometry(1f, 178f, 92f).neckVisible)
    }

    @Test
    fun geometryStaysSymmetricAroundTheIsland() {
        val g = islandLiquidGeometry(0.6f, 178f, 92f)
        assertTrue(kotlin.math.abs((g.centerX - g.dropLeft) - (g.dropRight - g.centerX)) < 0.001f)
        assertTrue(kotlin.math.abs((g.centerX - g.sourceLeft) - (g.sourceRight - g.centerX)) < 0.001f)
    }

    @Test
    fun geometryNeverReliesOnClipping() {
        for (fraction in listOf(0f, 0.2f, 0.6f, 1f, 1.35f)) {
            val width = 360f
            val height = 192f
            val g = islandLiquidGeometry(fraction, width, height)
            assertTrue(g.islandLeft >= 0f)
            assertTrue(g.islandTop >= 0f)
            assertTrue(g.islandLeft + g.islandWidth <= width)
            assertTrue(g.dropY + g.dropRadius <= height)
        }
    }

    @Test
    fun indicatorIsInvisibleAtRestAndVisibleWhileRefreshing() {
        assertEquals(0f, islandIndicatorAlpha(0f, refreshing = false))
        assertEquals(1f, islandIndicatorAlpha(0f, refreshing = true))
        assertEquals(1f, islandIndicatorAlpha(1f, refreshing = false))
    }
}
