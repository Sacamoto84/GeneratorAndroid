package com.example.generator2.features.threephase

import com.example.generator2.features.threephase.patterns.FingerPattern
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FingerPatternTest {

    @Test
    fun `пока палец на круге точка сразу под ним`() {
        //Сглаживание общее для всех паттернов и живёт в движке
        val p = FingerPattern()
        p.touchDown(0.5f, 0f, 0.0)
        assertTrue(p.isTouching)

        p.update(0.02)
        assertEquals(0.5f, p.alpha, 0f)
        assertEquals(0f, p.beta, 0f)

        p.touchMove(-0.3f, 0.4f, 0.1)
        p.update(0.02)
        assertEquals(-0.3f, p.alpha, 0f)
        assertEquals(0.4f, p.beta, 0f)
    }

    @Test
    fun `отпущенная траектория повторяется по кругу`() {
        val p = FingerPattern()
        p.touchDown(0f, 0f, 0.0)
        p.touchMove(1f, 0f, 1.0)
        p.touchMove(0f, 1f, 2.0)
        p.touchUp()

        p.update(0.5)
        assertEquals(0.5f, p.alpha, 1e-4f)
        assertEquals(0f, p.beta, 1e-4f)

        p.update(1.0)
        assertEquals(0.5f, p.alpha, 1e-4f)
        assertEquals(0.5f, p.beta, 1e-4f)

        //Длительность 2 с: прогресс 2.5 → 0.5
        p.update(1.0)
        assertEquals(0.5f, p.alpha, 1e-4f)
        assertEquals(0f, p.beta, 1e-4f)
    }

    @Test
    fun `меньше двух точек — точка стоит`() {
        val p = FingerPattern()
        p.touchDown(0.3f, 0.2f, 0.0)
        p.touchUp()

        p.update(1.0)
        assertEquals(0.3f, p.alpha, 1e-4f)
        assertEquals(0.2f, p.beta, 1e-4f)

        p.update(1.0)
        assertEquals(0.3f, p.alpha, 1e-4f)
        assertEquals(0.2f, p.beta, 1e-4f)
    }

    @Test
    fun `нулевая длительность не делит на ноль`() {
        val p = FingerPattern()
        p.touchDown(0.3f, 0f, 0.0)
        p.touchMove(0.6f, 0f, 0.0)
        p.touchUp()

        p.update(1.0)
        assertTrue(p.alpha.isFinite() && p.beta.isFinite())
        assertEquals(0.6f, p.alpha, 1e-4f)
        assertEquals(0f, p.beta, 1e-4f)
    }

    @Test
    fun `clear стирает траекторию`() {
        val p = FingerPattern()
        p.touchDown(0f, 0f, 0.0)
        p.touchMove(1f, 0f, 1.0)
        p.touchUp()
        p.update(0.5)

        p.clear()
        assertNull(p.trajectory())

        val alpha = p.alpha
        val beta = p.beta
        p.update(0.3)
        assertEquals(alpha, p.alpha, 1e-6f)
        assertEquals(beta, p.beta, 1e-6f)
    }
}
