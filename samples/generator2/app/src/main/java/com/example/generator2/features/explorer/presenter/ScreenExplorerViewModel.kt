package com.example.generator2.features.explorer.presenter

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.transformer.EditedMediaItem
import com.example.generator2.AppPath
import com.example.generator2.Global
import com.example.generator2.di.MainAudioMixerPump
import com.example.generator2.features.audio.AudioMixerPump
import com.example.generator2.features.explorer.data.treeAllAudio
import com.example.generator2.features.explorer.domen.explorerGetAllChildNode
import com.example.generator2.features.explorer.domen.explorerMediaFormat
import com.example.generator2.features.explorer.domen.tagInItemMp3
import com.example.generator2.features.explorer.model.ExplorerItem
import com.example.generator2.model.traverseTree
import com.kdownloader.httpclient.HttpClient
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Строка кнопки назад ...
 */
val NODE_UP = """<...>"""


/**
 * То что должно сохраниться при уничтожении вьюмодели
 */
@Singleton
class ScreenExplorerViewModelDataRepository @Inject constructor() {
    var currentNode by mutableStateOf(treeAllAudio) //MutableStateFlow(treeAllAudio)
}

@androidx.media3.common.util.UnstableApi
@SuppressLint("StaticFieldLeak")
@HiltViewModel
class ScreenExplorerViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val appPath: AppPath,

    //val global: Global,

    @MainAudioMixerPump
    val audioMixerPump: AudioMixerPump,

    val dataRepository: ScreenExplorerViewModelDataRepository

) : ViewModel() {

    init {
        Timber.i("!!! init() ScreenExplorerViewModel()")
    }

    override fun onCleared() {
        super.onCleared()
        Timber.i("!!! onCleared() ScreenExplorerViewModel()")
    }



    fun upNode() {
        val parent = dataRepository.currentNode.parent
        if (parent != null) {
            dataRepository.currentNode = parent
        }
    }


    fun scanNode() {
        val childs = explorerGetAllChildNode(dataRepository.currentNode)

        listItems.clear()

        if (dataRepository.currentNode.parent != null) {
            listItems.add(
                ExplorerItem(
                    node = dataRepository.currentNode.parent!!,
                    name = NODE_UP,
                    spec = true
                )
            )
        }

        childs.forEach {
            val value = it.value

            var count = 0

            traverseTree(it) { nod ->

                if (!nod.value.isS3) {
                    val result: Boolean = File(nod.value.path).isFile
                    if (result) {
                        count++
                    }
                } else {
                    if (!nod.value.isDirectory)
                        count++
                }

            }

            listItems.add(
                ExplorerItem(
                    node = it,
                    name = value.name,
                    counterItems = count
                )
            )
        }

        listItems.forEach {

            if (!it.node.value.isS3) {
                if (!it.node.value.isInit) {
                    if (!it.node.value.isDirectory) {
                        val format = explorerMediaFormat(it.node.value.path)
                        it.node.value.isFormat = format
                        tagInItemMp3(it.node)
                    }
                    it.node.value.isInit = true
                }
            }

            // mediaFind(it)
            // tagInItemMp3(it)
        }

        val _spec = listItems.filter { it.spec && it.name == NODE_UP }
        val _folder =
            listItems.filter { it.node.value.isDirectory && !it.spec && it.name != NODE_UP }
                .sortedBy { it.name }
        val _files =
            listItems.filter { !it.node.value.isDirectory && !it.spec && it.name != NODE_UP }
                .sortedBy { it.name }
        listItems.clear()
        listItems.addAll(_spec)
        listItems.addAll(_folder)
        listItems.addAll(_files)

        //.filter { it.node.value.isDirectory }
        //.sortedBy { it.name }
        //.sortedByDescending { it.node.value.isDirectory }


        //listItems.clear()
        //listItems.addAll(l)

//        //Сортировка
//        val l = listItems.filter { it.isDirectory or it.isMedia }.sortedBy { it.name }
//            .sortedByDescending { it.isDirectory }
//        listItems = l.toMutableList()

        //update++
    }


    fun onClick_DrawItem(item: ExplorerItem) {

        //Если есть символ назад
        if (item.name == NODE_UP) {
            upNode()
            return
        }

        if (item.node.value.isDirectory) {
            //Поиск ноды которая отвечает за данный путь item

            val node = treeAllAudio.search(item.node.value)
            if (node != null) {
                dataRepository.currentNode = node
            }

        } else {
            play(if (!item.node.value.isS3) item.node.value.path else item.node.value.uri)
        }

    }


    /**
     * ## ▶ Текущая рабочая папка ◀
     */
    val currentDir = MutableStateFlow(appPath.music)


    //var update by mutableIntStateOf(0)

    val listItems = SnapshotStateList<ExplorerItem>()


    @androidx.media3.common.util.UnstableApi
    fun play(path: String) {
        audioMixerPump.exoplayer.player.stop()
        val uri = Uri.parse(path)
        val a = EditedMediaItem.Builder(MediaItem.fromUri(uri)).build()
        audioMixerPump.exoplayer.player.setMediaItem(a.mediaItem)
        audioMixerPump.exoplayer.player.prepare()
        audioMixerPump.exoplayer.player.playWhenReady = true
    }


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

    //    Аудио:
//
//    MP3 (.mp3)
//    WAV (.wav)
//    FLAC (.flac)
//    AAC (.aac)
//    OGG (.ogg)
//    private val grandedList = listOf("mp3", "wav", "acc", "ogg")
//
//    private fun mediaFind(item: ExplorerItem) {
//        try {
//            //substringAfterLast('.')
//            val format = item.name.lowercase(Locale.ROOT).substringAfterLast('.') //->lowcase
//            if (format.isNotEmpty()) {
//                grandedList.forEach {
//                    if (format.contains(it)) {
//                        when (it) {
//                            "mp3" -> item.isFormat = "mp3"
//                            "wav" -> item.isFormat = "wav"
//                            "acc" -> item.isFormat = "acc"
//                            "ogg" -> item.isFormat = "ogg"
//                        }
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            Timber.e(e.localizedMessage)
//        }
//    }


}