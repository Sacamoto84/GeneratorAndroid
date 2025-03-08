package com.example.generator2.util

import android.content.Context
import com.example.generator2.AppPath
import com.example.generator2.features.generator.Generator
import com.example.generator2.features.initialization.utils.readFileMod2048byte
import com.example.generator2.util.ArrayUtils.byteToShortArrayLittleEndian
import java.io.File
import java.io.IOException

fun Float.format(digits: Int) = "%.${digits}f".format(this)

class UtilsKT(private var context: Context, val appPath: AppPath) {

    /**
     * Получить список файлов по пути
     *
     *  @param dir Путь к сканируемой папке Mod
     *  @return Текстовый список имен файлов
     */
    fun filesInDirToList( //context: Context,
        dir: String = ""
    ): List<String> { ///storage/emulated/0/Android/data/com.example.generator2/files
        val pathDocuments = File(dir)//context.getExternalFilesDir(dir)
        val r: MutableList<String> = mutableListOf()
        if (pathDocuments != null) {
            pathDocuments.list()?.let { r.addAll(it) }
        }
        return r
    }

}
