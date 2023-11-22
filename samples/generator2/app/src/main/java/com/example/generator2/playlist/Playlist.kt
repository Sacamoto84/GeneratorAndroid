package com.example.generator2.playlist

import java.io.File

object Playlist {

    init {
        readFromSQL()
    }

    //Список из списков для UI
    val list = mutableListOf<PlaylistList>()


    fun readFromSQL() {
        val playlistJson = mutableListOf<PlaylistJson>()
        playlistJson.addAll(PlaylistSQL.read()) //Читаем списки того что есть в SQL
        playlistJson

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
                val item = PlaylistItem(name = name, path = path, isExist = isExist, balance = balance, volume = volume)
                data.add(item)
            }

            val i = PlaylistList(playlistName, data)
            list.add(i)

        }

        //Получили минимальный список с проверкой существования файла


    }


}