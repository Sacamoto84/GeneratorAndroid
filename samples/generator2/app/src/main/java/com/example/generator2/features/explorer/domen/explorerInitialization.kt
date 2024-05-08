package com.example.generator2.features.explorer.domen

import android.annotation.SuppressLint
import android.content.Context
import com.example.generator2.features.explorer.data.treeAllAudio
import com.example.generator2.model.traverseTree
import org.jaudiotagger.audio.AudioFileIO
import java.io.File

fun explorerInitialization(context: Context) {

    val listAllFiles = explorerGetAllMusicFiles(context)
    val aa = explorerFilterMediaType(listAllFiles)
    //Получить дерево всех аудиофайлов на телефоне
    treeAllAudio = explorerTreeBuild(listAllFiles)

    //В каждый элемент дерева добавить поле path
    traverseTree(treeAllAudio) { node ->
        val p = node.pathToRoot()
        var s = ""
        p.forEach{ pp ->
            s += if (pp.name != "/") "/${pp.name}" else ""
        }
        node.value.path = s
        println(node.value.toString())
    }

    //Итого, дерево из имени и пути к файлу
    traverseTree(treeAllAudio) { node ->

        //////////////////
        /* Подсчет файлов в директориях */
        var count = 0
        traverseTree(node) { nod ->
            val result: Boolean = File(nod.value.path).isFile
            if (result) {
                count++
            }
        }
        node.value.counterItems = count
        ///////////////////////

        val path =  node.value.path
        val isDirectory = File(path).isDirectory
        node.value.isDirectory = isDirectory
        if (isDirectory) {
            node.value.isInit = true
            return@traverseTree
        }

        //Если не директория, то проверить расширение

        val format = explorerMediaFormat(path)
        node.value.isFormat = format

        if (format == "") return@traverseTree

        val audioFile = AudioFileIO.read(File(path))
        val tag = audioFile.tag
        val header = audioFile.audioHeader
        header.toString()

        val lengthInSeconds = header.trackLength.toLong().formatSecondsToTime()
        val sampleRate = header.sampleRate
        val bitRate = header.bitRate + "kbps "// + if (header.isVariableBitRate) "VBR" else ""
        val channelMode = header.channels.toString()

        node.value.lengthInSeconds = lengthInSeconds
        node.value.sampleRate = sampleRate
        node.value.bitRate = bitRate
        node.value.channelMode = channelMode

        node.value.isInit = true

        println(node.value.toString())
    }



}

@SuppressLint("DefaultLocale")
private fun Long.formatSecondsToTime(): String {
    return if (this == 0L) {
        "00:00:00"
    } else {
        val hours = this / 3600
        val minutes = (this % 3600) / 60
        val remainingSeconds = this % 60
        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
    }
}