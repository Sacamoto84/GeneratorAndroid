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




    /**
     * Получить список файлов по пути
     *
     *  @return Текстовый список имен файлов
     */
    fun filesInScriptToList(): List<String> {
        val pathDocuments = File(appPath.script)
        val r: MutableList<String> = mutableListOf()
        if (pathDocuments != null) {
            pathDocuments.list()?.let { r.addAll(it) }
        }
        return r
    }

    /**
     * Сохранить list String в файл /Script/name.sk
     */
    fun saveListToScriptFile(
        list: List<String>, name: String
    ) { ///storage/emulated/0/Android/data/com.example.generator2/files
        val pathDocuments  = appPath.script + "/${name}.sk"
        var str = ""
        for (i in list.indices) {
            str += "$i:${list[i]}\n"
        }
        File(pathDocuments).writeText(str)
    }


    /**
     *  Прочитать файл скрипта и записать его в список
     */
    fun readScriptFileToList(name: String): List<String> { ///storage/emulated/0/Android/data/com.example.generator2/files
       val pathDocuments = appPath.script + "/${name}.sk"

        val list = File(pathDocuments).readText().split("\n").toMutableList()

        if (list.last() == "") list.removeAt(list.lastIndex)

        for (i in list.indices) {
            val l = list[i].split(":")
            list[i] = l[1]
        }

        return list.toList()
    }

    fun deleteScriptFile(name: String) {
        val pathDocuments = appPath.script + "/${name}.sk"
        File(pathDocuments).delete()
    }

    fun renameScriptFile(nameSource: String, nameDescination: String) {
        try {
            val l = readScriptFileToList(nameSource)
            deleteScriptFile(nameSource)
            saveListToScriptFile(l, nameDescination)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }





}
