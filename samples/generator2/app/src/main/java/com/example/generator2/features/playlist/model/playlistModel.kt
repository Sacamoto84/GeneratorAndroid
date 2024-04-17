package com.example.generator2.features.playlist.model


data class PlaylistList(val playlistName: String = ".", val data: MutableList<PlaylistItem>)

//Структура для одного Json плейлиста
data class PlaylistJson(val playlistName: String = ".", val data: MutableList<PlaylistItemJson>)

//Структура которая используется для отображения в UI
data class PlaylistItem(
    val name: String = ".", // Отображаемое имя файла
    //val icon: Bitmap? = null,
    val path: String = "", //путь до файла
    val isExist: Boolean = false, //Признак того что файл существует

    val balance: Int = 0,     //Баланс
    val volume: Float = 1.0f, //Громкость

    val lengthInSeconds: String = "",
    val sampleRate: String = "",
    val bitRate: String = "",
    val channelMode: String = ""

)

//Структура которая хранится в JSON по файлу
data class PlaylistItemJson(
    val name: String = "?",
    val path: String = "", //путь до файла
    val balance: Int = 0,     //Баланс
    val volume: Float = 1.0f, //Громкость
    val additionalData: Map<String, Any> = mapOf()
)