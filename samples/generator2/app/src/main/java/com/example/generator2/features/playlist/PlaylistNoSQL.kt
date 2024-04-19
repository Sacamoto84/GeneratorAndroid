package com.example.generator2.features.playlist

import com.example.generator2.AppPath
import com.example.generator2.features.playlist.model.PlaylistJson
import com.example.generator2.features.noSQL.NoSQL
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import timber.log.Timber


class PlaylistSQL(appPath: AppPath) {

    //Файл playlist.db содержит список названий списков плейлистов
    val noSQLplaylistJson = NoSQL(path = appPath.config, nameDB = "playlist")

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
                //Timber.i("value: $json")
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