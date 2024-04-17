package com.example.generator2.features.update.explorer.model

data class ExplorerItem(
    val isDirectory: Boolean, //Признак того что файл является директорией
    val name: String = "",         //имя файла
    val fullPatch: String = "",     //Полный путь к файлу

    var isMedia: Boolean = false,
    var isMp3: Boolean = false,
    var isFormat: String = "",    //Формат файла mp2 wav flac acc
    var sampleRate: String = "",
    var bitRate: String = "",

    val channel: Int = 0,
    val title: String = "",
    var lengthInSeconds: String = "",
    var channelMode: String = ""
)