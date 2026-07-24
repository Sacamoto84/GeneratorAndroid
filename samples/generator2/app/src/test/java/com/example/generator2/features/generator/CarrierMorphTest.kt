package com.example.generator2.features.generator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CarrierMorphTest {

    @Test
    fun `время шага в сэмплы`() {
        assertEquals(48000, morphSteps(1f, 48000))
        assertEquals(24000, morphSteps(0.5f, 48000))
        assertEquals(4800, morphSteps(0.1f, 48000))
        assertEquals(4800000, morphSteps(100f, 48000))
    }

    @Test
    fun `время за границами зажимается`() {
        assertEquals(morphSteps(0.1f, 48000), morphSteps(0.001f, 48000))
        assertEquals(morphSteps(100f, 48000), morphSteps(500f, 48000))
    }

    @Test
    fun `шаг никогда не меньше одного сэмпла`() {
        assertTrue(morphSteps(0.1f, 1) >= 1)
    }

    @Test
    fun `маска слотов собирается по битам`() {
        assertEquals(0, morphMask(false, false, false))
        assertEquals(1, morphMask(true, false, false))
        assertEquals(2, morphMask(false, true, false))
        assertEquals(4, morphMask(false, false, true))
        assertEquals(5, morphMask(true, false, true))
        assertEquals(7, morphMask(true, true, true))
    }

    @Test
    fun `метаморфоза не работает без активных слотов`() {
        assertFalse(morphEffective(true, 0))
    }

    @Test
    fun `метаморфоза не работает выключенной`() {
        assertFalse(morphEffective(false, 7))
    }

    @Test
    fun `метаморфоза работает при включении и хотя бы одном слоте`() {
        assertTrue(morphEffective(true, 1))
        assertTrue(morphEffective(true, 4))
    }
}
