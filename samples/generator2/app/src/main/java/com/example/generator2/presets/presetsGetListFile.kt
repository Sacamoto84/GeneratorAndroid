package com.example.generator2.presets

import android.content.Context
import com.example.generator2.AppPath
import java.io.File

/**
 * Получить список файлов preset
 */
fun presetsGetListFile(context: Context): List<File> {

    val pathDocuments = context.getExternalFilesDir(AppPath().presets)

    val r: MutableList<File> = mutableListOf()

    if (pathDocuments != null) {
        pathDocuments.list()?.let { it ->
            r.addAll( it.map { File(it) } ) }
    }
    return r.toList()

}