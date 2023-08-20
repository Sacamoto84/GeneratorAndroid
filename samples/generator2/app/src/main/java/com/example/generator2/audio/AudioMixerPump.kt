package com.example.generator2.audio

import android.media.AudioTrack.WRITE_BLOCKING
import android.media.AudioTrack.WRITE_NON_BLOCKING
import com.example.generator2.generator.gen
import com.example.generator2.mp3.chDataStreamOutAudioProcessor
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.LinkedList


val audioMixerPump = AudioMixerPump()

class AudioMixerPump {

    enum class MASTERSTREAM {
        GEN,
        MP3
    }

    val master =  MutableStateFlow(MASTERSTREAM.GEN) //Выбор источника синхранизации

    val bufferSizeGenDefault = 8192 //размер буфера по умолчанию для генератора
    var bufferSize: Int =
        bufferSizeGenDefault //Текущий размер буфера который берется от размера буфера от плеера

    var bufferSizeMp3 = 0

    init {
        Timber.i("Запуск AudioMixerPump ")
        run()
    }


    @OptIn(DelicateCoroutinesApi::class)
    fun run() {

        //Имеется два источника синхронизации, это наличие пакетов в chDataStreamOutAudioProcessor и он является главным и запись иет в неблокирующем режиме,
        //И когда только генератор, и запись в блокирующем режиме

        GlobalScope.launch(Dispatchers.IO) {
            while (true) {
                val bigBufMp3 = chDataStreamOutAudioProcessor.receive()
                bufferSizeMp3 = bigBufMp3.size

                //Блок управления громкостью

                audioOutMp3.out.write(bigBufMp3, 0, bigBufMp3.size, WRITE_NON_BLOCKING)
            }
        }

        GlobalScope.launch(Dispatchers.IO) {

            while (true) {

                //val bigBufMp3 = chDataStreamOutAudioProcessor.receive()

//                bufferSize = if (bigBufMp3.isEmpty()) {
//                    Timber.e("bufMp3 size == 0")
//                    bufferSizeGenDefault
//                } else {
//                    bigBufMp3.size
//                }

                val bufGen = gen.renderAudio(bufferSize)


                if (bufGen.isNotEmpty()) {
                    //Timber.w("bufferSize = ${bigBufMp3.size}")
                    audioOut.out.write(bufGen, 0, bufGen.size, WRITE_BLOCKING)
                }

                //audioOut.chOut.send(bigBufMp3)


            }
        }
    }

}


fun ListToShortArray(bigList: LinkedList<ShortArray>): ShortArray {
    // Вычисляем общий размер результирующего массива
    var totalSize = 0
    for (shortArray in bigList) {
        totalSize += shortArray.size
    }

    // Создаем результирующий массив
    val resultArray = ShortArray(totalSize)

    // Копируем элементы из каждого ShortArray в результирующий массив
    var currentIndex = 0
    for (shortArray in bigList) {
        shortArray.copyInto(resultArray, currentIndex)
        currentIndex += shortArray.size
    }

    // Теперь resultArray содержит объединенные элементы из всех ShortArray
    return resultArray
}