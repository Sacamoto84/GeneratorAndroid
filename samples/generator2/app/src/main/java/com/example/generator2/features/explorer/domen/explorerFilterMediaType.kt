package com.example.generator2.features.explorer.domen

import java.util.Locale


//    Аудио:
//
//    MP3 (.mp3)
//    WAV (.wav)
//    FLAC (.flac)
//    AAC (.aac)
//    OGG (.ogg)
private val grandedList = listOf("mp3", "wav", "wav", "acc", "ogg")

/**
 * Филтр по расширениям
 * @param list - список файлов
 */
fun explorerFilterMediaType(list : List<String>):List<String> {
    val result = mutableListOf<String>()
    list.forEach { item ->
        val format = item.lowercase(Locale.ROOT).substringAfterLast('.') //->lowcase
        if (grandedList.contains(format)) {
          result.add(item)
        }
    }
    return result.toList()
}