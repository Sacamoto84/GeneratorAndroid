package com.example.generator2.features.initialization.utils

import android.content.Context
import java.io.IOException

/**
 * Получение списка файлов в папке Assets
 */
fun listFilesInAssetsFolder(context: Context, folderName: String = "Carrier"): List<String> {
    val assetManager = context.assets
    try {
        return assetManager.list(folderName)?.toList() ?: emptyList()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return emptyList()
}