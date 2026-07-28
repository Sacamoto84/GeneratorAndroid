package com.example.generator2.features.storage

import cafe.adriel.satchel.Satchel
import cafe.adriel.satchel.encrypter.bypass.BypassSatchelEncrypter
import cafe.adriel.satchel.ktx.getOrDefault
import cafe.adriel.satchel.serializer.raw.RawSatchelSerializer
import cafe.adriel.satchel.storer.file.FileSatchelStorer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class KvFileTest {

    @get:Rule
    val folder = TemporaryFolder()

    private fun file(name: String = "preset.txt") = File(folder.root, name)

    @Test
    fun `записанное читается обратно`() {
        val values = mapOf("name" to "test", "fr" to 400.5f, "en" to true, "star" to 3)

        KvFile.write(file(), values)

        assertEquals(values, KvFile.read(file()))
    }

    @Test
    fun `чтение отсутствующего файла даёт пустую мапу`() {
        assertTrue(KvFile.read(file()).isEmpty())
    }

    @Test
    fun `временный файл не остаётся после записи`() {
        KvFile.write(file(), mapOf("a" to 1))

        assertFalse(File(folder.root, "preset.txt.tmp").exists())
    }

    @Test
    fun `перезапись оставляет прошлую версию в bak`() {
        KvFile.write(file(), mapOf("v" to 1))
        KvFile.write(file(), mapOf("v" to 2))

        assertEquals(2, KvFile.read(file())["v"])
        assertEquals(1, KvFile.read(File(folder.root, "preset.txt.bak"))["v"])
    }

    @Test
    fun `битый основной файл подменяется резервным`() {
        KvFile.write(file(), mapOf("v" to 1))
        KvFile.write(file(), mapOf("v" to 2))

        file().writeText("мусор вместо java-сериализации")

        assertEquals(1, KvFile.read(file())["v"])
    }

    @Test
    fun `пропавший основной файл восстанавливается из bak`() {
        KvFile.write(file(), mapOf("v" to 1))
        KvFile.write(file(), mapOf("v" to 2))

        file().delete()

        assertEquals(1, KvFile.read(file())["v"])
    }

    @Test
    fun `формат совместим с Satchel`() {
        KvFile.write(file(), mapOf("presetsName" to "default", "ch1_Carrier_Fr" to 400.5f))

        val satchel = Satchel.with(
            storer = FileSatchelStorer(file()),
            encrypter = BypassSatchelEncrypter,
            serializer = RawSatchelSerializer
        )

        try {
            assertEquals("default", satchel.getOrDefault("presetsName", ""))
            assertEquals(400.5f, satchel.getOrDefault("ch1_Carrier_Fr", 0f), 0f)
        } finally {
            satchel.close()
        }
    }

    @Test
    fun `valueOr отдаёт значение по умолчанию при чужом типе`() {
        val values = mapOf<String, Any>("fr" to "не число")

        assertEquals(400f, values.valueOr("fr", 400f), 0f)
        assertEquals(400f, values.valueOr("нет ключа", 400f), 0f)
    }
}
