package com.example.generator2.features.initialization.utils

import android.content.Context
import java.io.IOException


/**
 * Прочесть numBytes байтов из файла из `assets`
 */
fun readBytesFromAssets(context: Context, directory: String, fileName: String, numBytes: Int): ByteArray? {
    val assetManager = context.assets

    return try {
        val inputStream = assetManager.open("$directory/$fileName")
        val buffer = ByteArray(numBytes)
        inputStream.read(buffer)
        inputStream.close()
        buffer
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }

}