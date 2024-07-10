package com.example.generator2.features.explorer.domen

import android.annotation.SuppressLint
import android.content.Context
import com.example.generator2.features.explorer.data.treeAllAudio
import com.example.generator2.features.explorer.data.treeAllAudioS3
import com.example.generator2.features.explorer.model.ExploreNodeItem
import com.example.generator2.model.TreeNode
import com.example.generator2.model.traverseTree
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jaudiotagger.audio.AudioFileIO
import timber.log.Timber
import java.io.File


/**
 * Запрос на S3 для получения списка всех файлов
 */
fun readS3List(urlS3 : String= "https://ru-spb-s3.hexcore.cloud/rabbit/list.txt"):List<String>{

    Timber.d("!!! readS3List()")

    val client = OkHttpClient()
    val request = Request.Builder()
        .url(urlS3)
        .build()

    try {
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            return emptyList()
        }
        val responseBody = response.body.string()
        Timber.i(responseBody)
        return responseBody.lines()
    } catch (e: Exception) {
        Timber.e(e.message)
        return emptyList()
    }

}

fun explorerInitialization(context: Context) {

    //Список всех mp3 на телефоне
    val listAllFiles = explorerGetAllMusicFiles(context).toMutableList()

    //Список всех mp3 в S3
    val listAllS3url = readS3List()

    val aa = explorerFilterMediaType(listAllFiles)

    //Убрали https://ru-spb-s3.hexcore.cloud
    val bb = explorerFilterMediaType(listAllS3url).map { it.substringAfter("https://ru-spb-s3.hexcore.cloud") }

    //Получить дерево всех аудиофайлов на телефоне
    treeAllAudio = explorerTreeBuild(listAllFiles)

    //Получить дерево всех аудиофайлов на S3
    treeAllAudioS3 = explorerTreeBuild(bb)


    //В каждый элемент дерева добавить поле path
    traverseTree(treeAllAudio) { node ->
        val p = node.pathToRoot()
        var s = ""
        p.forEach { pp ->
            s += if (pp.name != "/") "/${pp.name}" else ""
        }
        node.value.path = s
        println(node.value.toString())
    }

    //В каждый элемент дерева добавить поле path
    traverseTree(treeAllAudioS3) { node ->
        val p = node.pathToRoot()
        var s = ""
        p.forEach { pp ->
            s += if (pp.name != "/") "/${pp.name}" else ""
        }
        node.value.path = s
        println("!!! S3> "+node.value.toString())
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

        val path = node.value.path
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

        tagInItemMp3(node)

        node.value.isInit = true

        println(node.value.toString())
    }

    treeAllAudio.add(treeAllAudioS3)

}


fun tagInItemMp3(node: TreeNode<ExploreNodeItem>) {
    if ((node.value.isFormat == "") || (node.value.isDirectory)) return
    val audioFile = AudioFileIO.read(File(node.value.path))
    val header = audioFile.audioHeader
    node.value.lengthInSeconds = header.trackLength.toLong().formatSecondsToTime()
    node.value.sampleRate = header.sampleRate
    node.value.bitRate = header.bitRate + "kbps "// + if (header.isVariableBitRate) "VBR" else ""
    node.value.channelMode = header.channels.toString()
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