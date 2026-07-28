package com.example.generator2.features.playlist

import com.example.generator2.features.playlist.model.PlaylistJson
import com.example.generator2.features.storage.KvFile
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import java.io.File

/**
 * Плейлисты в файле `playlist.db`: ключ — имя списка, значение — его JSON.
 *
 * Формат прежний, но запись идёт одним атомарным сбросом через [KvFile]: раньше
 * каждый плейлист перезаписывал файл целиком, то есть сохранение N списков
 * означало N полных перезаписей.
 *
 * Путь приходит снаружи (см. DI-модуль), про [com.example.generator2.AppPath] и
 * вообще про Android класс не знает — поэтому проверяется обычным JVM-тестом.
 */
class PlaylistStore(private val file: File) {

    /**
     * Чтение всех плейлистов из базы
     */
    fun readAll(): List<PlaylistJson> {
        val values = KvFile.read(file)
        Timber.i("playlist.db: найдено ${values.size} ключей")

        val playlistType = object : TypeToken<PlaylistJson>() {}.type

        return values.mapNotNull { (key, value) ->
            try {
                Gson().fromJson<PlaylistJson>(value as String, playlistType)
            } catch (e: Exception) {
                //Один битый список не должен утащить за собой остальные
                Timber.e(e, "Не удалось разобрать плейлист $key")
                null
            }
        }
    }

    fun write(list: List<PlaylistJson>) {
        val gson = GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .create()

        KvFile.write(file, list.associate { it.playlistName to gson.toJson(it) })
    }

    /**
     * ## Очистить весь плейлист
     */
    fun clear() = KvFile.write(file, emptyMap())
}
