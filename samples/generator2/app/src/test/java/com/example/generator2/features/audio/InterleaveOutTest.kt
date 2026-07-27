package com.example.generator2.features.audio

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class InterleaveOutTest {

    @Test
    fun `нормальный режим кладёт левый канал в чётные индексы`() {
        val v = interleaveOut(floatArrayOf(1f, 2f), floatArrayOf(-1f, -2f), shuffle = false)

        assertArrayEquals(floatArrayOf(1f, -1f, 2f, -2f), v, 0f)
    }

    @Test
    fun `shuffle меняет уши местами`() {
        val v = interleaveOut(floatArrayOf(1f, 2f), floatArrayOf(-1f, -2f), shuffle = true)

        assertArrayEquals(floatArrayOf(-1f, 1f, -2f, 2f), v, 0f)
    }
}
