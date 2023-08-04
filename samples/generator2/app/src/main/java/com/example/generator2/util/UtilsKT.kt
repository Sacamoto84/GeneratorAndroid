package com.example.generator2.util

import android.content.Context
import c.ponom.audiuostreams.audiostreams.ArrayUtils.byteToShortArrayLittleEndian
import com.example.generator2.PlaybackEngine
import com.example.generator2.generator.ch1
import com.example.generator2.generator.ch2
import java.io.File
import java.io.IOException

fun Float.format(digits: Int) = "%.${digits}f".format(this)

class UtilsKT(private var context: Context, private var playbackEngine: PlaybackEngine) {

    /**
     * Получить список файлов по пути
     *
     *  @param dir Путь к сканируемой папке Mod
     *  @return Текстовый список имен файлов
     */
    fun filesInDirToList( //context: Context,
        dir: String = ""
    ): List<String> { ///storage/emulated/0/Android/data/com.example.generator2/files
        val pathDocuments = context.getExternalFilesDir(dir)

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
        //val pathDocuments =
        //    Global.contextActivity!!.getExternalFilesDir("/Script")!!.absolutePath.toString() + "/${name}.sk"

        val pathDocuments =
            context.getExternalFilesDir("/Script")!!.absolutePath.toString() + "/${name}.sk"

        var str = "" //val m = list.toMutableList()
        val m = list.toMutableList()
        m[0] = name
        for (i in m.indices) {
            str += "$i:${m[i]}\n"
        }
        File(pathDocuments).writeText(str)
    }


    /**
     *  Прочитать файл скрипта и записать его в список
     */
    fun readScriptFileToList(name: String): List<String> { ///storage/emulated/0/Android/data/com.example.generator2/files
        val pathDocuments =
            context.getExternalFilesDir("/Script")!!.absolutePath.toString() + "/${name}.sk"

        val list = File(pathDocuments).readText().split("\n").toMutableList()

        if (list.last() == "") list.removeLast()

        for (i in list.indices) {
            val l = list[i].split(":")
            list[i] = l[1]
        }

        return list.toList()
    }

    fun deleteScriptFile(name: String) {
        val pathDocuments =
            context.getExternalFilesDir("/Script")!!.absolutePath.toString() + "/${name}.sk"
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


    //Для спиннера, отсылка массива
    @OptIn(ExperimentalUnsignedTypes::class)
    fun Spinner_Send_Buffer(
        CH: String,
        Mod: String,
        name: String
    ) { //String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
        var path = ""
        path += if (Mod == "CR") Utils.patchCarrier + name + ".dat" else Utils.patchMod + name + ".dat"
        val buf = Utils.readFileMod2048byte(path) //Здесь должны прочитать файл и записать в массив;
        var ch = 0
        var mod = 0
        if (Mod == "AM") mod = 1
        if (Mod == "FM") mod = 2
        if (CH == "CH1") ch = 1

        if (CH == "CH0") {
            when (Mod) {
                "AM" -> ch1.buffer_am = byteToShortArrayLittleEndian(buf)
                "FM" -> ch1.buffer_fm = byteToShortArrayLittleEndian(buf)
                else -> ch1.buffer_carrier = byteToShortArrayLittleEndian(buf)
            }
        }else
        {
            when (Mod) {
                "AM" -> ch2.buffer_am = byteToShortArrayLittleEndian(buf)
                "FM" -> ch2.buffer_fm = byteToShortArrayLittleEndian(buf)
                else -> ch2.buffer_carrier = byteToShortArrayLittleEndian(buf)
            }
        }

        //playbackEngine.CH_Send_Buffer(ch, mod, buf) //Послали буффер

    }


}
