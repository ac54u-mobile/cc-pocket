package dev.ccpocket.app.ui

import kotlin.test.Test
import kotlin.test.assertFalse
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
}
