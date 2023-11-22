package com.example.generator2.playlist

import com.example.generator2.AppPath
import com.example.generator2.noSQL.NoSQL
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import timber.log.Timber



object PlaylistSQL {

    //Файл playlist.db содержит список названий списков плейлистов
    val noSQLplaylistJson = NoSQL(path = AppPath().config, nameDB = "playlist")

    /**
     * Чтение Json плейлиста из базы playlist.db
     */
    fun read(): List<PlaylistJson> {
        Timber.i("Получение списка ключей в playlist.db")
        val list = mutableListOf<PlaylistJson>()

        try {
            Timber.i("Найденно ${noSQLplaylistJson.keys()} ключей")

            noSQLplaylistJson.keys().forEach {
                Timber.i("Ключ: $it")
                val json = noSQLplaylistJson.read(it, "")
                Timber.i("value: $json")
                val playlistType = object : TypeToken<PlaylistJson>() {}.type
                val res: PlaylistJson = Gson().fromJson(json, playlistType)
                list.add(res)
            }

        } catch (e: Exception) {
            Timber.e(e.localizedMessage)
        }
        return list
    }

    fun write(list: List<PlaylistJson>) {
        val gson = GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .create()

        list.forEach {
            val json = gson.toJson(it)
            noSQLplaylistJson.write(it.playlistName, json)
        }
    }

    fun clear() = noSQLplaylistJson.clear()

}