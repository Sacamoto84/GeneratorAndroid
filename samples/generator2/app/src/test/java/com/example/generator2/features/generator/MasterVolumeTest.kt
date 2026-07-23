package com.example.generator2.features.generator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MasterVolumeTest {

    @Test
    fun `период в r положительный на всём диапазоне`() {
        assertTrue(masterPeriodToR(0.1f, 48000) > 0)
        assertTrue(masterPeriodToR(2f, 48000) > 0)
        assertTrue(masterPeriodToR(100f, 48000) > 0)
    }

    @Test
    fun `меньший период даёт больший r`() {
        assertTrue(masterPeriodToR(0.1f, 48000) > masterPeriodToR(100f, 48000))
    }

    @Test
    fun `период за границами зажимается`() {
        assertEquals(masterPeriodToR(0.1f, 48000), masterPeriodToR(0.01f, 48000))
        assertEquals(masterPeriodToR(100f, 48000), masterPeriodToR(500f, 48000))
    }

    @Test
    fun `секунды в сэмплы`() {
        assertEquals(48000, secToSamples(1f, 48000))
        assertEquals(24000, secToSamples(0.5f, 48000))
        assertEquals(4800, secToSamples(0.1f, 48000))
    }

    @Test
    fun `кнопка активна если канал включён и режим кнопка`() {
        assertTrue(masterButtonActive(true, MASTER_MODE_BUTTON, false, MASTER_MODE_SLOW))
        assertTrue(masterButtonActive(false, MASTER_MODE_SLOW, true, MASTER_MODE_BUTTON))
    }

    @Test
    fun `кнопка не активна без включённого режима кнопка`() {
        assertFalse(masterButtonActive(true, MASTER_MODE_SLOW, true, MASTER_MODE_ONOFF))
        assertFalse(masterButtonActive(false, MASTER_MODE_BUTTON, false, MASTER_MODE_BUTTON))
    }
}
