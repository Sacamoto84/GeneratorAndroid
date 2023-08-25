package com.example.generator2.explorer

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.generator2.AppPath
import com.example.generator2.di.Hub
import com.example.generator2.mp3.formatMinSec
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.mp3.MP3AudioHeader
import org.jaudiotagger.tag.FieldKey
import timber.log.Timber
import java.io.File
import java.text.ParseException
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class ScreenExplorerViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val hub: Hub
) : ViewModel() {

    val explorerCurrentDir = MutableStateFlow(AppPath().music) //Текущая рабочая папка

    var update by mutableIntStateOf(0)

    var listItems = mutableListOf<ExplorerItem>()

    fun up()
    {
        val s = explorerCurrentDir.value.substringBeforeLast('/')
        explorerCurrentDir.value = s
    }






    fun scan() {

        listItems.clear()

        try {

            val directory = File(explorerCurrentDir.value)

            if (directory.exists() && directory.isDirectory) {
                val files = directory.listFiles()
                if (files != null) {
                    for (file in files) {

                        if (file.isDirectory) {
                            if(file.name[0]!='.') {
                                listItems.add(
                                    ExplorerItem(
                                        isDirectory = true,
                                        name = file.name,
                                        fullPatch = file.path
                                    )
                                )
                            }
                            println("${file.name} - это папка")

                        } else {
                            listItems.add(
                                ExplorerItem(
                                    isDirectory = false,
                                    name = file.name,
                                    fullPatch = file.path
                                )
                            )
                            println("${file.name} - это файл")
                        }
                    }
                } else {
                    println("Ошибка при получении списка файлов")
                }
            } else {
                println("Папка не существует или это не папка")
            }

        } catch (e: Exception) {
            Timber.e(e.localizedMessage)
        }

        listItems.forEach {
            mediaFind(it)
            tagInItemMp3(it)
        }

        //Сортировка
        val l = listItems.filter { it.isDirectory or it.isMedia }.sortedBy { it.name }
            .sortedByDescending { it.isDirectory }
        listItems = l.toMutableList()

        update++

    }

    private fun tagInItemMp3(item: ExplorerItem) {
        try {
            if ((item.isDirectory) or (item.isFormat == "")) return

            println("----------------------------------------------")
            println(item.fullPatch)

            val audioFile = AudioFileIO.read(File(item.fullPatch))
            val tag = audioFile.tag


            val header = audioFile.audioHeader
            header.toString()

            item.lengthInSeconds = header.trackLength.toLong().formatSecondsToTime()
            item.sampleRate = header.sampleRate
            item.bitRate = header.bitRate + "kbps "// + if (header.isVariableBitRate) "VBR" else ""
            item.channelMode = header.channels.toString()

            val title = tag.getFirst(FieldKey.TITLE)
            val artist = tag.getFirst(FieldKey.ARTIST)
            val album = tag.getFirst(FieldKey.ALBUM)

            println("Title: $title")
            println("Artist: $artist")
            println("Album: $album")


        } catch (e: Exception) {
            Timber.e(e.localizedMessage)
        }

    }


    private fun Long.formatSecondsToTime(): String {
        return if (this == 0L) {
            "..."
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
    private val grandedList = listOf("mp3", "wav", "wav", "acc", "ogg")

    private fun mediaFind(item: ExplorerItem) {
        try {
            //substringAfterLast('.')
            val format = item.name.lowercase(Locale.ROOT).substringAfterLast('.') //->lowcase
            if (format.isNotEmpty()) {
                grandedList.forEach {
                    if (format.contains(it)) {
                        item.isMedia = true

                        when (it) {
                            "mp3" -> item.isFormat = "mp3"
                            "wav" -> item.isFormat = "wav"
                            "acc" -> item.isFormat = "acc"
                            "ogg" -> item.isFormat = "ogg"
                        }

                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e.localizedMessage)
        }

    }


}