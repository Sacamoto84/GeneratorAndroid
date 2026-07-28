package com.example.generator2.features.presets

import com.example.generator2.features.storage.KvFile
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class PresetsReadFileTest {

    @get:Rule
    val folder = TemporaryFolder()

    private fun writePreset(name: String, values: Map<String, Any>) =
        KvFile.write(File(folder.root, "$name.txt"), values)

    @Test
    fun `значения пресета попадают в поля`() {
        writePreset(
            "my",
            mapOf(
                "ch1_Carrier_Fr" to 777.0f,
                "ch2_Carrier_Fr" to 888.0f,
                "mono" to true,
                "star" to 4
            )
        )

        val data = presetsReadFile("my", folder.root.absolutePath)

        assertEquals(777.0f, data.chL_Carrier_Fr.value, 0f)
        assertEquals(888.0f, data.chR_Carrier_Fr.value, 0f)
        assertEquals(true, data.mono.value)
        assertEquals(4, data.star.value)
        assertEquals("my", data.presetsName.value)
    }

    @Test
    fun `границы FM второго канала восстанавливаются из ch2FmMin и ch2FmMax`() {
        writePreset("my", mapOf("ch2FmMin" to 1234.0f, "ch2FmMax" to 5678.0f))

        val data = presetsReadFile("my", folder.root.absolutePath)

        assertEquals(1234.0f, data.chRFmMin.value, 0f)
        assertEquals(5678.0f, data.chRFmMax.value, 0f)
    }

    @Test
    fun `старые файлы с parameterFloat2 и parameterFloat3 читаются как раньше`() {
        writePreset("my", mapOf("parameterFloat2" to 1500.0f, "parameterFloat3" to 2500.0f))

        val data = presetsReadFile("my", folder.root.absolutePath)

        assertEquals(1500.0f, data.chRFmMin.value, 0f)
        assertEquals(2500.0f, data.chRFmMax.value, 0f)
    }

    @Test
    fun `отсутствующий пресет даёт значения по умолчанию`() {
        val data = presetsReadFile("нет такого", folder.root.absolutePath)

        assertEquals(400.0f, data.chL_Carrier_Fr.value, 0f)
        assertEquals(0.9f, data.maxVolume0.value, 0f)
    }

    @Test
    fun `битый пресет не роняет чтение`() {
        File(folder.root, "my.txt").writeText("мусор")

        val data = presetsReadFile("my", folder.root.absolutePath)

        assertEquals(400.0f, data.chL_Carrier_Fr.value, 0f)
    }

    @Test
    fun `служебные файлы атомарной записи не попадают в список пресетов`() {
        writePreset("first", mapOf("star" to 1))
        writePreset("first", mapOf("star" to 2)) //появляется first.txt.bak
        writePreset("second", mapOf("star" to 1))

        val names = presetsGetListFile(folder.root.absolutePath).map { it.name }.sorted()

        assertEquals(listOf("first.txt", "second.txt"), names)
    }
}
