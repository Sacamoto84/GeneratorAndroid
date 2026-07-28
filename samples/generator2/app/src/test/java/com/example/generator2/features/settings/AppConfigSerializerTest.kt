package com.example.generator2.features.settings

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class AppConfigSerializerTest {

    private fun read(json: String): AppConfig = runBlocking {
        AppConfigSerializer.readFrom(ByteArrayInputStream(json.encodeToByteArray()))
    }

    private fun write(config: AppConfig): String = runBlocking {
        ByteArrayOutputStream().also { AppConfigSerializer.writeTo(config, it) }.toString("UTF-8")
    }

    @Test
    fun `запись и чтение возвращают ту же структуру`() {
        val config = AppConfig(language = "en", autoUpdate = true, maxVolume0 = 0.42f)
        assertEquals(config, read(write(config)))
    }

    @Test
    fun `новое поле в структуре читается из старого файла со значением по умолчанию`() {
        val config = read("""{"language":"en"}""")

        assertEquals("en", config.language)
        assertEquals(AppConfig().autoUpdate, config.autoUpdate)
        assertEquals(AppConfig().maxVolume0, config.maxVolume0, 0f)
    }

    @Test
    fun `удалённое поле в файле не ломает чтение`() {
        val config = read("""{"language":"en","режимКоторогоБольшеНет":123}""")

        assertEquals("en", config.language)
    }

    @Test
    fun `битый json отдаёт CorruptionException`() {
        assertThrows(CorruptionException::class.java) { read("{не json") }
    }
}
