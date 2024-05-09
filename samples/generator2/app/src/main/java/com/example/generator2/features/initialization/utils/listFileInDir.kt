package com.example.generator2.features.initialization.utils

import com.example.generator2.application
import com.snatik.storage.Storage
import timber.log.Timber

/**
 * Получить список файлов по пути
 *
 * @return Список String[] файлов в папке
 */
fun listFileInDir(path: String): List<String> {
    Timber.i( "Получить список файлов по пути: $path")
    val storage = Storage(application)
    val files = storage.getFiles(path).toList().sorted().mapNotNull { it?.name }
    return files
}