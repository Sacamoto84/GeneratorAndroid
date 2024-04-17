package com.example.generator2.features.presets

import java.io.File

/**
 * Получить список файлов preset
 */
fun presetsGetListFile(path : String): List<File> {
    return File(path).listFiles()?.toList() ?: emptyList()
}