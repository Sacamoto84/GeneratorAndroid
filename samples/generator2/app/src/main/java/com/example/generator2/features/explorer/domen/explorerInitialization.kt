package com.example.generator2.features.explorer.domen

import android.annotation.SuppressLint
import android.content.Context
import com.example.generator2.features.explorer.data.treeAllAudio
import com.example.generator2.features.explorer.data.treeAllAudioS3
import com.example.generator2.features.explorer.model.ExploreNodeItem
import com.example.generator2.model.TreeNode
import com.example.generator2.model.traverseTree
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jaudiotagger.audio.AudioFileIO
import timber.log.Timber
import java.io.File

private const val server = "https://ru-spb-s3.hexcore.cloud"



data class FileInfoS3(
    val path: String,
    val url: String,
    val format: String,
    val isDirectory: Boolean,
    val size: Long,
    val isStereo: Boolean = false,
    val lengthInSeconds: Long = 0,
    var sampleRate: Int,  //44100
    var bitRate: Int,      //224kbps
    var count : Int = 0
)




/**
 * Запрос на S3 для получения списка всех файлов
 */
fun readS3List(urlS3 : String= "$server/rabbit/list.txt"):List<FileInfoS3>{

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

        val gson = Gson()
        val listPersonType = object : TypeToken<List<FileInfoS3>>() {}.type
        val persons: List<FileInfoS3> = gson.fromJson(responseBody, listPersonType)

        return persons

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
    //val bb = listAllS3url.map { it.url.substringAfter(server) }

    //Получить дерево всех аудиофайлов на телефоне
    treeAllAudio = explorerTreeBuild(listAllFiles, isS3 = false, listAllFiles)

    //Получить дерево всех аудиофайлов на S3
    treeAllAudioS3 = explorerTreeBuildS3(listAllS3url.filter { !it.isDirectory  })


    //В каждый элемент дерева добавить поле path
    traverseTree(treeAllAudio) { node ->
        val p = node.pathToRoot()
        var s = ""
        p.forEach { pp ->
            s += if (pp.name != "/") "/${pp.name}" else ""
        }
        node.value.path = s
        //node.value.isS3 = false
        println("!!! >"+node.value.toString())
    }

    //В каждый элемент дерева добавить поле path
    traverseTree(treeAllAudioS3) { node ->
        val p = node.pathToRoot()
        var s = ""
        p.forEach { pp ->
            s += if (pp.name != "/") "/${pp.name}" else ""
        }
        node.value.uri = server + s

        val item = listAllS3url.find { it.url == node.value.uri  }

        if (item != null)
        {
            node.value.lengthInSeconds = item.lengthInSeconds.formatSecondsToTime()
            node.value.sampleRate = item.sampleRate.toString()
            node.value.bitRate = item.bitRate.toString()
            node.value.channelMode = if (item.isStereo) "Stereo" else "Mono"
            node.value.fileSize = item.size
            node.value.isDirectory = item.isDirectory
            node.value.isFormat = item.format
            node.value.counterItems = item.count
            node.value.isS3 = true
            node.value.isInit = true
        }
        else{
            Timber.e("error")
        }


//        if (node.children.isEmpty())
//            node.value.isDirectory = false
//        else
//            node.value.isDirectory = true

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