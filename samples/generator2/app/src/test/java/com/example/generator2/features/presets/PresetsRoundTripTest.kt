package com.example.generator2.features.presets

import com.example.generator2.features.generator.Generator
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Сохранение и чтение пресета обязаны сходиться по ключам: рассинхрон уже
 * приводил к тому, что границы FM второго канала писались в ch2FmMin/ch2FmMax,
 * а читались из parameterFloat2/parameterFloat3 и терялись.
 */
class PresetsRoundTripTest {

    @get:Rule
    val folder = TemporaryFolder()

    private fun roundTrip(gen: Generator): com.example.generator2.features.generator.DataLiveData {
        presetsWrite("my", folder.root.absolutePath, presetsSnapshot("my", gen))
        return presetsReadFile("my", folder.root.absolutePath)
    }

    @Test
    fun `состояние генератора переживает запись и чтение`() {
        val gen = Generator()
        val d = gen.liveData

        d.star.value = 5
        d.chL_EN.value = true
        d.chL_Carrier_Fr.value = 123.5f
        d.chL_AM_Fr.value = 3.25f
        d.chL_FM_Dev.value = 700f
        d.chR_EN.value = true
        d.chR_Carrier_Fr.value = 4321.5f
        d.chR_FM_Fr.value = 9.5f
        d.mono.value = true
        d.invert.value = true
        d.shuffle.value = true
        d.enL.value = false
        d.maxVolume0.value = 0.4f
        d.maxVolume1.value = 0.6f
        d.currentVolume0.value = 0.3f
        d.volume1.value = 0.2f
        d.chLAmDepth.value = 0.75f
        d.chL_Master_EN.value = true
        d.chL_Master_Period.value = 3.5f
        d.chR_Master_TOff.value = 2.5f
        d.chL_Morph_EN.value = true
        d.chL_Morph_Time.value = 4.5f
        d.chR_Morph_Slot2_Filename.value = "Triangle"
        d.chLFmMin.value = 1111f
        d.chLFmMax.value = 2222f
        d.chRFmMin.value = 3333f
        d.chRFmMax.value = 4444f
        d.parameterInt0.value = 1
        d.parameterInt1.value = 1

        val restored = roundTrip(gen)

        assertEquals(5, restored.star.value)
        assertEquals(true, restored.chL_EN.value)
        assertEquals(123.5f, restored.chL_Carrier_Fr.value, 0f)
        assertEquals(3.25f, restored.chL_AM_Fr.value, 0f)
        assertEquals(700f, restored.chL_FM_Dev.value, 0f)
        assertEquals(true, restored.chR_EN.value)
        assertEquals(4321.5f, restored.chR_Carrier_Fr.value, 0f)
        assertEquals(9.5f, restored.chR_FM_Fr.value, 0f)
        assertEquals(true, restored.mono.value)
        assertEquals(true, restored.invert.value)
        assertEquals(true, restored.shuffle.value)
        assertEquals(false, restored.enL.value)
        assertEquals(0.4f, restored.maxVolume0.value, 0f)
        assertEquals(0.6f, restored.maxVolume1.value, 0f)
        assertEquals(0.3f, restored.currentVolume0.value, 0f)
        assertEquals(0.2f, restored.volume1.value, 0f)
        assertEquals(0.75f, restored.chLAmDepth.value, 0f)
        assertEquals(true, restored.chL_Master_EN.value)
        assertEquals(3.5f, restored.chL_Master_Period.value, 0f)
        assertEquals(2.5f, restored.chR_Master_TOff.value, 0f)
        assertEquals(true, restored.chL_Morph_EN.value)
        assertEquals(4.5f, restored.chL_Morph_Time.value, 0f)
        assertEquals("Triangle", restored.chR_Morph_Slot2_Filename.value)
        assertEquals(1111f, restored.chLFmMin.value, 0f)
        assertEquals(2222f, restored.chLFmMax.value, 0f)
        assertEquals(3333f, restored.chRFmMin.value, 0f)
        assertEquals(4444f, restored.chRFmMax.value, 0f)
        assertEquals(1, restored.parameterInt0.value)
        assertEquals(1, restored.parameterInt1.value)
    }

    @Test
    fun `имя пресета попадает в файл`() {
        val restored = roundTrip(Generator())

        assertEquals("my", restored.presetsName.value)
    }
}
