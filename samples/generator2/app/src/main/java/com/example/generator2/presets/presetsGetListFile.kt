package com.example.generator2.presets

import com.example.generator2.AppPath
import java.io.File

/**
 * Получить список файлов preset
 */
fun presetsGetListFile(): List<File> {
    return File(AppPath().presets).listFiles()?.toList() ?: emptyList()
}