package com.example.generator2.features.explorer.domen

import com.example.generator2.features.explorer.model.ExplorerItem
import timber.log.Timber
import java.util.Locale

//    Аудио:
//
//    MP3 (.mp3)
//    WAV (.wav)
//    FLAC (.flac)
//    AAC (.aac)
//    OGG (.ogg)
private val grandedList = listOf("mp3", "wav", "acc", "ogg")

fun explorerMediaFormat(path: String): String {
    var result = ""

    try {
        val format = path.lowercase(Locale.ROOT).substringAfterLast('.') //->lowcase
        if (format.isNotEmpty()) {
            grandedList.forEach {
                if (format.contains(it)) {
                    when (it) {
                        "mp3" -> result = "mp3"
                        "wav" -> result = "wav"
                        "acc" -> result = "acc"
                        "ogg" -> result = "ogg"
                    }
                }
            }
        }
    } catch (e: Exception) {
        Timber.e(e.localizedMessage)
    }
    return result
}

