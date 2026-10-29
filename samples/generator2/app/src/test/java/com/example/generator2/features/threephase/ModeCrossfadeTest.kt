package com.example.generator2.features.threephase

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class ModeCrossfadeTest {

    @Test
    fun `кроссфейд линейный и заканчивается новым сигналом`() {
        val oldL = FloatArray(6) { 1f }
        val oldR = FloatArray(6) { -1f }
        val dstL = floatArrayOf(0f, 0f, 0f, 0f, 0.9f, 0.9f)
        val dstR = floatArrayOf(0f, 0f, 0f, 0f, 0.9f, 0.9f)

        crossfadeFrom(oldL, oldR, dstL, dstR, 4)

        assertArrayEquals(floatArrayOf(0.75f, 0.5f, 0.25f, 0f, 0.9f, 0.9f), dstL, 1e-6f)
        assertArrayEquals(floatArrayOf(-0.75f, -0.5f, -0.25f, 0f, 0.9f, 0.9f), dstR, 1e-6f)
    }
}
