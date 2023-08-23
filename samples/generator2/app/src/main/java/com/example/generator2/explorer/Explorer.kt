package com.example.generator2.explorer

data class ExplorerItem(
    val isDirectory : Boolean, //Признак того что файл является директорией
    val name : String = "",         //имя файла
    val fullPatch : String = "",     //Полный путь к файлу

    val samplarate : Int = 0,
    val channel : Int = 0,
    val title : String = ""
)