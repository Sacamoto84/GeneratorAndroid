package com.example.generator2.features.explorer.model

data class ExploreNodeItem (
    val name : String,
    var path : String,

    //Часть для сканера
    var lengthInSeconds: String = "",
    var sampleRate: String = "",  //44100
    var bitRate: String = "",     //224kbps
    var channelMode: String = "", //Mono, Stereo

    var isDirectory: Boolean = true, //Признак того что файл является директорией
    var isFormat: String = "",    //Формат файла mp3 wav flac acc



    //var channel: Int = 0,
    var title: String = "",



    /* */
    var isInit: Boolean = false, //Флаг инициализации, если true то можно использовать эти данные

    var counterItems: Int = 0 //Только для директорий, показ количества и темов которые есть в директории

)

