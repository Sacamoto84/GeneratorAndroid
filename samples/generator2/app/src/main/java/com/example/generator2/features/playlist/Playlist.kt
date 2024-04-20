package com.example.generator2.features.playlist

import com.example.generator2.AppPath
import com.example.generator2.features.playlist.model.PlaylistItem
import com.example.generator2.features.playlist.model.PlaylistJson
import com.example.generator2.features.playlist.model.PlaylistList
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Playlist @Inject constructor(
    val appPath: AppPath
) {
    //Список из списков для UI
    var list = mutableListOf<PlaylistList>()

    init {
        try {
            list = readAllFromSQL().toMutableList()
        } catch (e: Exception) {
            Timber.e(e.localizedMessage)
        }
    }

    /**
     * Прочесть все записи из базы
     */
    private fun readAllFromSQL(): List<PlaylistList> {

        val result = mutableListOf<PlaylistList>()

        val playlistJson = mutableListOf<PlaylistJson>()
        playlistJson.addAll(PlaylistSQL(appPath).readAll()) //Читаем списки того что есть в SQL

        //Заполняем list по данным из playlistJson
        playlistJson.forEach {

            val playlistName = it.playlistName //Имя плейлиста
            val data = mutableListOf<PlaylistItem>()
            it.data.forEach { it1 ->

                val name = it1.name
                val path = it1.path
                val balance = it1.balance
                val volume = it1.volume
                val isExist = File(path).exists()

                val item = PlaylistItem(
                    name = name,
                    path = path,
                    isExist = isExist,
                    balance = balance,
                    volume = volume
                )

                data.add(item)
            }

            val i = PlaylistList(playlistName, data)
            result.add(i)

        }

        //Получили минимальный список с проверкой существования файла
        return result

    }






}