package com.example.generator2.features.presets

import java.io.File

/**
 * Получить список файлов preset.
 *
 * Только `*.txt`: рядом лежат служебные `*.txt.bak` и `*.txt.tmp` атомарной
 * записи, в списке пресетов им делать нечего.
 */
fun presetsGetListFile(path : String): List<File> {
    return File(path).listFiles { file -> file.isFile && file.name.endsWith(".txt") }?.toList()
        ?: emptyList()
}