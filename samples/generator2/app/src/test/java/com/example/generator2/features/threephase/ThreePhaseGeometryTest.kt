package com.example.generator2.features.threephase

import org.junit.Assert.assertEquals
import org.junit.Test

class ThreePhaseGeometryTest {

    @Test
    fun `Neutral сверху, L+ слева снизу, R+ справа снизу`() {
        val (nx, ny) = ThreePhaseGeometry.toScreen(1f, 0f, 100f, 100f, 100f)
        assertEquals(100f, nx, 1e-3f)
        assertEquals(0f, ny, 1e-3f)

        val (lx, ly) = ThreePhaseGeometry.toScreen(-0.5f, 0.8660254f, 100f, 100f, 100f)
        assertEquals(13.39746f, lx, 1e-3f)
        assertEquals(150f, ly, 1e-3f)

        val (rx, ry) = ThreePhaseGeometry.toScreen(-0.5f, -0.8660254f, 100f, 100f, 100f)
        assertEquals(186.60254f, rx, 1e-3f)
        assertEquals(150f, ry, 1e-3f)
    }

    @Test
    fun `fromScreen обратен и прижимает к кругу`() {
        val (x, y) = ThreePhaseGeometry.toScreen(0.3f, -0.4f, 100f, 100f, 100f)
        val (a, b) = ThreePhaseGeometry.fromScreen(x, y, 100f, 100f, 100f)
        assertEquals(0.3f, a, 1e-5f)
        assertEquals(-0.4f, b, 1e-5f)

        val (ea, eb) = ThreePhaseGeometry.fromScreen(400f, 100f, 100f, 100f, 100f)
        assertEquals(0f, ea, 1e-5f)
        assertEquals(-1f, eb, 1e-5f)
    }
}
