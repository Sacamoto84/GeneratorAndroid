package com.example.generator2.features.settings

import androidx.datastore.core.DataMigration
import cafe.adriel.satchel.Satchel
import cafe.adriel.satchel.SatchelStorage
import cafe.adriel.satchel.encrypter.bypass.BypassSatchelEncrypter
import cafe.adriel.satchel.ktx.getOrDefault
import cafe.adriel.satchel.serializer.raw.RawSatchelSerializer
import cafe.adriel.satchel.storer.file.FileSatchelStorer
import timber.log.Timber
import java.io.File

/**
 * ### Разовый перенос настроек из старых Satchel-файлов в [AppConfig]
 *
 * Старое хранилище: три файла в `<sdcard>/Gen3/Config`, каждый — Java-сериализованная
 * мапа целиком (`config2.db`, `volume.txt`, `constrain.txt`).
 *
 * После успешного переноса файлы переименовываются в `*.bak`: это же и признак того,
 * что мигрировать больше нечего. Старые данные не удаляются — на случай отката.
 *
 * @param configDir каталог старых файлов, `AppPath.config`
 */
class SatchelMigration(private val configDir: String) : DataMigration<AppConfig> {

    private companion object {
        const val FILE_CONFIG2 = "config2.db"
        const val FILE_VOLUME = "volume.txt"
        const val FILE_CONSTRAIN = "constrain.txt"

        val LEGACY_FILES = listOf(FILE_CONFIG2, FILE_VOLUME, FILE_CONSTRAIN)
    }

    override suspend fun shouldMigrate(currentData: AppConfig): Boolean =
        LEGACY_FILES.any { File(configDir, it).exists() }

    override suspend fun migrate(currentData: AppConfig): AppConfig {
        var config = currentData

        readLegacy(FILE_CONFIG2) { satchel ->
            config = config.copy(
                language = satchel.getOrDefault("language", config.language),
                autoUpdate = satchel.getOrDefault("updateauto", config.autoUpdate)
            )
        }

        readLegacy(FILE_VOLUME) { satchel ->
            config = config.copy(
                maxVolume0 = satchel.getOrDefault("maxVolume0", config.maxVolume0),
                maxVolume1 = satchel.getOrDefault("maxVolume1", config.maxVolume1)
            )
        }

        readLegacy(FILE_CONSTRAIN) { satchel ->
            config = config.copy(
                sensitivitySliderCr =
                    satchel.getOrDefault("sensetingSliderCr", config.sensitivitySliderCr),
                sensitivitySliderFmDev =
                    satchel.getOrDefault("sensetingSliderFmDev", config.sensitivitySliderFmDev),
                sensitivitySliderAmFm =
                    satchel.getOrDefault("sensetingSliderAmFm", config.sensitivitySliderAmFm)
            )
        }

        Timber.i("Настройки перенесены из Satchel: $config")
        return config
    }

    override suspend fun cleanUp() {
        LEGACY_FILES.forEach { name ->
            val file = File(configDir, name)
            if (file.exists()) {
                val renamed = file.renameTo(File(configDir, "$name.bak"))
                if (!renamed) {
                    //Файл остался на месте - миграция повторится на следующем запуске,
                    //результат тот же, данные не портятся
                    Timber.w("Не удалось переименовать $name в $name.bak")
                }
            }
        }
    }

    /**
     * Открывает старый файл, отдаёт хранилище в [block] и обязательно закрывает его:
     * незакрытый Satchel держит живую корутину до конца процесса.
     * Битый файл не должен ломать старт - настройка просто остаётся с дефолтом.
     */
    private fun readLegacy(name: String, block: (SatchelStorage) -> Unit) {
        val file = File(configDir, name)
        if (!file.exists()) return

        var satchel: SatchelStorage? = null
        try {
            satchel = Satchel.with(
                storer = FileSatchelStorer(file),
                encrypter = BypassSatchelEncrypter,
                serializer = RawSatchelSerializer
            )
            block(satchel)
        } catch (e: Exception) {
            Timber.e(e, "Не удалось прочитать старый файл настроек $name")
        } finally {
            satchel?.close()
        }
    }
}
