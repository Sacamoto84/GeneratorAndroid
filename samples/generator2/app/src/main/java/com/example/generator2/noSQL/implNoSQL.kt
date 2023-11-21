package com.example.generator2.noSQL

import com.example.generator2.AppPath

enum class KEY_NOSQL_CONFIG2(val value: String) {
    LANGUAGE("language"),
}


//language | "ru" "en" - выбор языка
val noSQLConfig2 = NoSQL(path = AppPath().config, nameDB = "config2")





//Файл list.db содержит список названий списков плейлистов
//key [version]
//key list String
//
// key - [имя плейлиста]
// data serial список из
// @name имя файла
// @path путь к файлу
//
//
//
val noSQLplaylist = NoSQL(path = AppPath().config, nameDB = "playlist")
