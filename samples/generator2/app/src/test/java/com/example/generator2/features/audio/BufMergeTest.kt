package com.example.generator2.features.audio

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class BufMergeTest {

    @Test
    fun `первый аргумент bufMerge попадает в чётные индексы - левое ухо LRLR`() {
        val left = floatArrayOf(1f, 2f, 3f)
        val right = floatArrayOf(-1f, -2f, -3f)

        val v = bufMerge(left, right)

        assertArrayEquals(floatArrayOf(1f, -1f, 2f, -2f, 3f, -3f), v, 0f)
    }

    @Test
    fun `split возвращает чётные сэмплы первым элементом пары - левый канал`() {
        val interleaved = floatArrayOf(1f, -1f, 2f, -2f)

        val (l, r) = split(interleaved)

        assertArrayEquals(floatArrayOf(1f, 2f), l, 0f)
        assertArrayEquals(floatArrayOf(-1f, -2f), r, 0f)
    }
}
