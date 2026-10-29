package com.example.generator2.features.threephase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * Математика калибровки и сэмпла сверяется с restim: эталоны в
 * `threephase/calib.csv` и `threephase/math.csv`.
 */
class ThreePhaseMathTest {

    @Test
    fun `calibrationToUdLr совпадает с restim`() {
        for (row in ReferenceCsv.read("calib.csv")) {
            val (n, r) = ThreePhaseMath.calibrationToUdLr(row.d("aDb"), row.d("bDb"), row.d("cDb"))
            assertEquals("neutral $row", row.d("neutralDb"), n, 1e-4)
            assertEquals("right $row", row.d("rightDb"), r, 1e-4)
        }
    }

    @Test
    fun `channelMatrix совпадает с restim`() {
        for (row in ReferenceCsv.read("calib.csv")) {
            val k = ThreePhaseMath.channelMatrix(row.f("aDb"), row.f("bDb"), row.f("cDb"))
            assertEquals("k00 $row", row.f("k00"), k[0], 1e-4f)
            assertEquals("k01 $row", row.f("k01"), k[1], 1e-4f)
            assertEquals("k10 $row", row.f("k10"), k[2], 1e-4f)
            assertEquals("k11 $row", row.f("k11"), k[3], 1e-4f)
        }
    }

    @Test
    fun `sample совпадает с restim`() {
        val out = FloatArray(2)
        for (row in ReferenceCsv.read("math.csv")) {
            val k = ThreePhaseMath.channelMatrix(row.f("aDb"), row.f("bDb"), row.f("cDb"))
            ThreePhaseMath.sample(row.f("alpha"), row.f("beta"), row.d("theta"), k, out)
            assertEquals("L $row", row.f("L"), out[0], 1e-4f)
            assertEquals("R $row", row.f("R"), out[1], 1e-4f)
        }
    }

    @Test
    fun `центр приглушается на заданный процент`() {
        assertEquals(0.87f, ThreePhaseMath.centerGain(0f, 13f), 1e-6f)
        assertEquals(0.935f, ThreePhaseMath.centerGain(0.5f, 13f), 1e-6f)
        assertEquals(1f, ThreePhaseMath.centerGain(1f, 13f), 1e-6f)
        assertEquals(1f, ThreePhaseMath.centerGain(0f, 0f), 1e-6f)
    }

    @Test
    fun `выход не превышает 1 при любой калибровке`() {
        val out = FloatArray(2)
        for (row in ReferenceCsv.read("calib.csv")) {
            val k = ThreePhaseMath.channelMatrix(row.f("aDb"), row.f("bDb"), row.f("cDb"))
            assertTrue("K конечна $row", k.all { it.isFinite() })
            for (ia in -15..15) for (ib in -15..15) {
                var theta = 0.0
                while (theta < 2 * Math.PI) {
                    ThreePhaseMath.sample(ia / 10f, ib / 10f, theta, k, out)
                    assertTrue("L ${out[0]} $row", out[0].isFinite() && abs(out[0]) <= 1.0001f)
                    assertTrue("R ${out[1]} $row", out[1].isFinite() && abs(out[1]) <= 1.0001f)
                    theta += 0.05
                }
            }
        }
    }
}
