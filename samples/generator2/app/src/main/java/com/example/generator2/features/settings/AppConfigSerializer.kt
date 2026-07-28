package com.example.generator2.features.settings

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/**
 * Читает/пишет [AppConfig] как JSON.
 *
 * Сама запись атомарна — этим занимается DataStore (пишет во временный файл и
 * делает rename), поэтому убийство процесса во время сохранения не рвёт конфиг.
 */
object AppConfigSerializer : Serializer<AppConfig> {

    override val defaultValue: AppConfig = AppConfig()

    private val json = Json {
        ignoreUnknownKeys = true //Удалённое поле не ломает чтение старого файла
        encodeDefaults = true    //Пишем всё, файл читается глазами через adb
        prettyPrint = true
    }

    override suspend fun readFrom(input: InputStream): AppConfig =
        try {
            json.decodeFromString(AppConfig.serializer(), input.readBytes().decodeToString())
        } catch (e: SerializationException) {
            throw CorruptionException("Не удалось прочитать $FILE_NAME", e)
        }

    override suspend fun writeTo(t: AppConfig, output: OutputStream) {
        output.write(json.encodeToString(AppConfig.serializer(), t).encodeToByteArray())
    }
}

/** Имя файла настроек во внутреннем хранилище приложения */
internal const val FILE_NAME = "config.json"
