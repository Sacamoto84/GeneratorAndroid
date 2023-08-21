package com.example.generator2.audio

import android.media.AudioTrack.WRITE_BLOCKING
import com.example.generator2.generator.gen
import com.example.generator2.mp3.chDataStreamOutAudioProcessor
import com.example.generator2.mp3.exoplayer
import com.example.generator2.util.bufMerge
import com.example.generator2.util.bufSpit
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.LinkedList

enum class ROUTESTREAM {
    GEN,
    MP3,
    OFF
}

val audioMixerPump = AudioMixerPump()


class AudioMixerPump {



    val routeR = MutableStateFlow(ROUTESTREAM.MP3) //Выбор источника для вывода сигнала
    val routeL = MutableStateFlow(ROUTESTREAM.MP3)

    val bufferSizeGenDefault = 8192 //размер буфера по умолчанию для генератора
    var bufferSize: Int =
        bufferSizeGenDefault //Текущий размер буфера который берется от размера буфера от плеера

    var bufferSizeMp3 = 0

    init {
        Timber.i("Запуск AudioMixerPump ")
        run()
    }


    @OptIn(DelicateCoroutinesApi::class)
    @androidx.media3.common.util.UnstableApi
    fun run() {

        //Имеется два источника синхронизации, это наличие пакетов в chDataStreamOutAudioProcessor и он является главным и запись иет в неблокирующем режиме,
        //И когда только генератор, и запись в блокирующем режиме

        var isPlaying = false
        var isPlayingLast = false
        var stop = false
        var start = false
        var volume = 0f

        var delay = 50
        //

        var bufferSize = 8192

        GlobalScope.launch(Dispatchers.IO) {

            var init = false

            while (!init) {
                try {
                    // Попытка обращения к переменной exoplayer
                    exoplayer.player // Примерное действие, замените на соответствующий вызов
                    init = true
                } catch (e: UninitializedPropertyAccessException) {
                    // Переменная exoplayer еще не инициализирована
                    init = false
                }
            }

            GlobalScope.launch(Dispatchers.IO) {
                while (true) {
                    isPlaying = exoplayer.isPlayingD
                    if (isPlaying and !isPlayingLast) {
                        start = true
                        delay = 20
                        //volume = 0.1f
                    } else
                        if (!isPlaying and isPlayingLast) {
                            stop = true
                        }
                    isPlayingLast = isPlaying
                    delay(1)
                }
            }



            while (true) {


                if (isPlaying) {

                    val bigBufMp3 = chDataStreamOutAudioProcessor.receive()

                    bufferSize = bigBufMp3.size

                    //println("bigBufMp3.size ${bigBufMp3.size}")
                    if (start) {
                        stop = false
                        Timber.e("1 start $volume $delay")
                        if (delay > 0) {
                            delay--; volume = 0.01f
                        } else {
                            volume += 0.05f
                        }
                        if (volume >= 1f) {
                            volume = 1f; start = false
                        }
                    }

                    if (stop) {
                        Timber.e("1 stop $volume"); stop = false
                    }


                    //audioOutMp3.destroy()

//                    if (audioOutMp3.out == null)
//                        audioOutMp3.create()


                    val bufGen = gen.renderAudio(bufferSize)

                    //val v = ShortArray(bigBufMp3.size)

                    for (i in bigBufMp3.indices) {
                        bigBufMp3[i] = (bigBufMp3[i] * volume).toInt().toShort()
                    }

                    val (bufGenL, bufGenR) = bufSpit(bufGen)

                    val (bufMp3L, bufMp3R) = bufSpit(bigBufMp3)

                    val outR = when(routeR.value) {
                        ROUTESTREAM.MP3 -> bufMp3R
                        ROUTESTREAM.GEN -> bufGenR
                        ROUTESTREAM.OFF -> ShortArray(bufferSize/2){0}
                    }

                    val outL = when(routeL.value) {
                        ROUTESTREAM.MP3 -> bufMp3L
                        ROUTESTREAM.GEN -> bufGenL
                        ROUTESTREAM.OFF -> ShortArray(bufferSize/2){0}
                    }

                    val enL = gen.liveData.enL.value
                    val enR = gen.liveData.enR.value

                    val v = bufMerge(outL, outR, enL ,enR)

                    //LRLRLR
                    audioOut.out?.write(v, 0, v.size, WRITE_BLOCKING)

                } else {

                    while (chDataStreamOutAudioProcessor.tryReceive().isSuccess) {
                        println("Очистка канала")
                    }

                    val bufGen = gen.renderAudio(bufferSize)
                    val (bufGenR, bufGenL) = bufSpit(bufGen)

                    val outR = when(routeR.value) {
                        ROUTESTREAM.MP3 -> ShortArray(bufGenR.size){0}
                        ROUTESTREAM.GEN -> bufGenR
                        ROUTESTREAM.OFF -> ShortArray(bufferSize/2){0}
                    }

                    val outL = when(routeL.value) {
                        ROUTESTREAM.MP3 -> ShortArray(bufGenL.size){0}
                        ROUTESTREAM.GEN -> bufGenL
                        ROUTESTREAM.OFF -> ShortArray(bufferSize/2){0}
                    }

                    val v = bufMerge(outL, outR)  //? нужно проверить

                    audioOut.out?.write(v, 0, v.size, WRITE_BLOCKING)
                }


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

//                val bufGen = gen.renderAudio(bufferSize)
//
//
//                if (bufGen.isNotEmpty()) {
//                    //Timber.w("bufferSize = ${bigBufMp3.size}")
//                    audioOut.out?.write(bufGen, 0, bufGen.size, WRITE_BLOCKING)
//                }

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