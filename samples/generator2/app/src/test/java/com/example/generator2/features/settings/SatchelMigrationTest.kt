package com.example.generator2.features.settings

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.ObjectOutputStream

class SatchelMigrationTest {

    @get:Rule
    val folder = TemporaryFolder()

    /** Пишет файл в формате RawSatchelSerializer: сериализованная java-мапа целиком */
    private fun writeLegacy(name: String, values: Map<String, Any>) {
        val file = File(folder.root, name)
        file.outputStream().use { out ->
            ObjectOutputStream(out).use { it.writeObject(values) }
        }
    }

    private fun migration() = SatchelMigration(folder.root.absolutePath)

    @Test
    fun `без старых файлов мигрировать нечего`() = runBlocking {
        assertFalse(migration().shouldMigrate(AppConfig()))
    }

    @Test
    fun `настройки переносятся из трёх старых файлов`() = runBlocking {
        writeLegacy("config2.db", mapOf("language" to "en", "updateauto" to true))
        writeLegacy("volume.txt", mapOf("maxVolume0" to 0.5f, "maxVolume1" to 0.7f))
        writeLegacy(
            "constrain.txt",
            mapOf(
                "sensetingSliderCr" to 0.3f,
                "sensetingSliderFmDev" to 0.4f,
                "sensetingSliderAmFm" to 0.05f
            )
        )

        val migration = migration()
        assertTrue(migration.shouldMigrate(AppConfig()))

        val config = migration.migrate(AppConfig())

        assertEquals("en", config.language)
        assertTrue(config.autoUpdate)
        assertEquals(0.5f, config.maxVolume0, 0f)
        assertEquals(0.7f, config.maxVolume1, 0f)
        assertEquals(0.3f, config.sensitivitySliderCr, 0f)
        assertEquals(0.4f, config.sensitivitySliderFmDev, 0f)
        assertEquals(0.05f, config.sensitivitySliderAmFm, 0f)
    }

    @Test
    fun `отсутствующий ключ оставляет значение по умолчанию`() = runBlocking {
        writeLegacy("config2.db", mapOf("language" to "en"))

        val config = migration().migrate(AppConfig())

        assertEquals("en", config.language)
        assertEquals(AppConfig().autoUpdate, config.autoUpdate)
    }

    @Test
    fun `битый файл не роняет миграцию`() = runBlocking {
        File(folder.root, "config2.db").writeText("это не java-сериализация")
        writeLegacy("volume.txt", mapOf("maxVolume0" to 0.5f))

        val config = migration().migrate(AppConfig())

        assertEquals(AppConfig().language, config.language)
        assertEquals(0.5f, config.maxVolume0, 0f)
    }

    @Test
    fun `после переноса старые файлы становятся bak и повторная миграция не нужна`() = runBlocking {
        writeLegacy("config2.db", mapOf("language" to "en"))

        val migration = migration()
        migration.migrate(AppConfig())
        migration.cleanUp()

        assertFalse(File(folder.root, "config2.db").exists())
        assertTrue(File(folder.root, "config2.db.bak").exists())
        assertFalse(migration.shouldMigrate(AppConfig()))
    }
}
