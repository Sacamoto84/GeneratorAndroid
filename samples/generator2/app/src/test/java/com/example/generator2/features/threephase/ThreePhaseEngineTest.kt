package com.example.generator2.features.threephase

import com.example.generator2.features.threephase.patterns.FigureEightPattern
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.exp
import kotlin.math.hypot

class ThreePhaseEngineTest {

    private val sr = 48000

    private fun ThreePhaseEngine.last(n: Int) = points[2 * (n - 1)] to points[2 * (n - 1) + 1]

    /** Без сглаживания — для точной проверки переходов и тактирования. */
    private fun raw() = ThreePhaseEngine(smoothingTau = 0.0)

    @Test
    fun `число точек и последняя на конце блока`() {
        val e = ThreePhaseEngine()
        assertEquals(129, e.fillPoints(4096, sr, "Circle", 1f, 1f))
        assertEquals(5, e.fillPoints(100, sr, "Circle", 1f, 1f))
        assertEquals(2, e.fillPoints(10, sr, "Circle", 1f, 1f))
    }

    @Test
    fun `Circle проходит четверть оборота`() {
        val e = raw()
        //18 · 4096 + 1670 = 75398 сэмплов ≈ π/2 с
        repeat(18) { e.fillPoints(4096, sr, "Circle", 1f, 1f) }
        val n = e.fillPoints(1670, sr, "Circle", 1f, 1f)
        val (a, b) = e.last(n)
        assertEquals(0f, a, 1e-3f)
        assertEquals(1f, b, 1e-3f)
        assertEquals(a, e.displayAlpha, 0f)
        assertEquals(b, e.displayBeta, 0f)
    }

    @Test
    fun `стык блоков непрерывен при любых размерах`() {
        val e = ThreePhaseEngine()
        var n = e.fillPoints(4096, sr, "Spirograph", 1f, 1f)
        for (frames in listOf(100, 10, 33, 4096, 1)) {
            val (a, b) = e.last(n)
            n = e.fillPoints(frames, sr, "Spirograph", 1f, 1f)
            assertEquals("стык перед блоком $frames", a, e.points[0], 0f)
            assertEquals("стык перед блоком $frames", b, e.points[1], 0f)
        }
    }

    @Test
    fun `амплитуда масштабирует позицию`() {
        val e = raw()
        val n = e.fillPoints(4096, sr, "Circle", 1f, 0.5f)
        val (a, b) = e.last(n)
        assertEquals(0.5f, hypot(a, b), 1e-4f)
    }

    @Test
    fun `смена паттерна без скачка`() {
        val e = raw()
        repeat(3) { e.fillPoints(4800, sr, "Circle", 1f, 0.8f) }
        val (a, b) = e.last(151)

        var n = e.fillPoints(4800, sr, "Figure 8", 1f, 0.8f)
        assertEquals(a, e.points[0], 0f)
        assertEquals(b, e.points[1], 0f)
        repeat(2) { n = e.fillPoints(4800, sr, "Figure 8", 1f, 0.8f) }

        //Через 0.3 с — чистая восьмёрка: 450 шагов по 32 сэмпла
        val reference = FigureEightPattern()
        repeat(450) { reference.update(32.0 / sr) }
        val (fa, fb) = e.last(n)
        assertEquals(reference.alpha * 0.8f, fa, 1e-4f)
        assertEquals(reference.beta * 0.8f, fb, 1e-4f)
    }

    @Test
    fun `повторная смена во время перехода стартует с текущей точки`() {
        val e = raw()
        e.fillPoints(4800, sr, "Circle", 1f, 1f)
        e.fillPoints(4800, sr, "Figure 8", 1f, 1f)
        val (a, b) = e.last(151)

        e.fillPoints(4800, sr, "Spirograph", 1f, 1f)
        assertEquals(a, e.points[0], 0f)
        assertEquals(b, e.points[1], 0f)
        //Через 0.1 с переход ещё идёт: точка не прыгнула к спирографу
        assertTrue(hypot(e.points[2] - a, e.points[3] - b) < 0.05f)
    }

    @Test
    fun `палец сглаживается как ось restim и игнорирует скорость`() {
        val e = ThreePhaseEngine()
        e.patterns.finger.touchDown(0.5f, 0f, 0.0)
        val n = e.fillPoints(960, sr, "Палец", 10f, 1f) //0.02 с
        val (a, _) = e.last(n)
        val expected = 0.5 * (1 - exp(-0.02 / ThreePhaseEngine.RESTIM_SMOOTHING_TAU))
        assertEquals(expected.toFloat(), a, 1e-4f)
    }

    @Test
    fun `скачок амплитуды растягивается сглаживанием`() {
        val e = ThreePhaseEngine()
        repeat(10) { e.fillPoints(4800, sr, "Circle", 1f, 0.25f) }
        val n = e.fillPoints(32, sr, "Circle", 1f, 1f)
        val (a, b) = e.last(n)
        //За 32 сэмпла радиус ушёл от 0.25 к 1.0 лишь на малую долю
        assertTrue("радиус ${hypot(a, b)}", hypot(a, b) < 0.3f)
    }

    @Test
    fun `калибровка и центр меняются плавно за блок`() {
        val e = ThreePhaseEngine()
        val k0 = e.k.copyOf()

        var p = e.blockParams(13f)
        assertArrayEquals(k0, p.copyOfRange(0, 4), 0f)
        assertArrayEquals(k0, p.copyOfRange(4, 8), 0f)
        assertEquals(13f, p[8], 0f)
        assertEquals(13f, p[9], 0f)

        val k1 = ThreePhaseMath.channelMatrix(-6f, 0f, -3f)
        e.k = k1
        p = e.blockParams(5f)
        assertArrayEquals(k0, p.copyOfRange(0, 4), 0f)
        assertArrayEquals(k1, p.copyOfRange(4, 8), 0f)
        assertEquals(13f, p[8], 0f)
        assertEquals(5f, p[9], 0f)

        p = e.blockParams(5f)
        assertArrayEquals(k1, p.copyOfRange(0, 4), 0f)
        assertArrayEquals(k1, p.copyOfRange(4, 8), 0f)
        assertEquals(5f, p[8], 0f)
        assertEquals(5f, p[9], 0f)
    }
}
