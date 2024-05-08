package com.example.generator2.features.initialization.utils

import timber.log.Timber
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException

/**
 * Чтение файла и возврат массива байтов
 *
 * @return Массив byte[]
 */
fun readFileMod2048byte(path: String?): ByteArray {
    val file = File(path)
    val len = file.length()
    val fileData = ByteArray(len.toInt())

    //Timber.tag("readFileMod2048byte:").i(path + " len:" + Long.toString(len));
    try {
        val dis = DataInputStream(FileInputStream(file))

        dis.readFully(fileData)
        dis.close()
    } catch (e: IOException) {
        Timber.tag("readFileMod2048byte:").e("!IOException! : Error%s", e.localizedMessage)
    }

    return fileData
}



