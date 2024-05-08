package com.example.generator2.features.explorer.model

import com.example.generator2.model.TreeNode

data class ExplorerItem(

    val node: TreeNode<ExploreNodeItem>,

    //val isDirectory: Boolean, //Признак того что файл является директорией
    val name: String = "",         //имя файла
    //val fullPatch: String = "",     //Полный путь к файлу

    //var isMedia: Boolean = false,
    //var isMp3: Boolean = false,
    //var isFormat: String = "",    //Формат файла mp2 wav flac acc
    //var sampleRate: String = "",
    //var bitRate: String = "",

    //val channel: Int = 0,
    //val title: String = "",
    //var lengthInSeconds: String = "",
    //var channelMode: String = "",

    var counterItems: Int = 0 //Только для директорий, показ количества и темов которые есть в директории


)