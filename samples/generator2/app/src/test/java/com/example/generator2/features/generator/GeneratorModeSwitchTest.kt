package com.example.generator2.features.generator

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Переключение «3Φ» при включённой инверсии канала: инверсия принадлежит
 * каналам генератора и уходит в кроссфейд вместе с ними, иначе на стыке
 * режимов щелчок. Нативные рендеры подменены — JVM их не загрузит.
 */
class GeneratorModeSwitchTest {

    private fun generator() = Generator().apply {
        channelRenderer = { _, l, r -> l.fill(1f); r.fill(1f) }
        threePhaseRenderer = { _, l, r -> l.fill(0f); r.fill(0f) }
    }

    @Test
    fun `инверсия каналов уходит в кроссфейд при включении 3-фазы`() {
        val gen = generator()
        gen.invertL = true

        val (l, r) = gen.renderAudio(200) //100 кадров, обычные каналы
        assertEquals(-1f, l[0], 0f)
        assertEquals(1f, r[0], 0f)
        assertEquals(false, gen.renderedThreePhase)

        gen.liveData.tp_EN.value = true
        val (l2, _) = gen.renderAudio(200)

        //Кроссфейд на весь короткий блок: первый сэмпл — почти старый, инвертированный
        assertEquals(-0.99f, l2[0], 1e-6f)
        assertEquals(0f, l2[99], 1e-6f)
        assertEquals(true, gen.renderedThreePhase)
    }

    @Test
    fun `3-фаза не инвертируется`() {
        val gen = generator()
        gen.threePhaseRenderer = { _, l, r -> l.fill(0.5f); r.fill(0.5f) }
        gen.invertL = true
        gen.invertR = true
        gen.liveData.tp_EN.value = true

        gen.renderAudio(200)
        val (l, r) = gen.renderAudio(200)
        assertEquals(0.5f, l[50], 0f)
        assertEquals(0.5f, r[50], 0f)
    }
}
