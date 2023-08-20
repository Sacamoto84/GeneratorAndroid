package com.example.generator2.audio

import android.media.AudioTrack.WRITE_BLOCKING
import android.media.AudioTrack.WRITE_NON_BLOCKING
import com.example.generator2.generator.gen
import com.example.generator2.mp3.chDataStreamOutAudioProcessor
import com.example.generator2.mp3.exoplayer
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

    val master = MutableStateFlow(MASTERSTREAM.GEN) //Выбор источника синхранизации

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
                        delay = 100
                        volume = 0.1f
                    } else
                        if (!isPlaying and isPlayingLast) {
                            stop = true
                        }
                    isPlayingLast = isPlaying

                    delay(1)
                }
            }

            while (true) {

                if (start) {
                    stop = false

                    Timber.e("1 start $volume $delay")

                    if (delay > 0) {
                        delay--
                        volume = 0.01f
                    } else {
                        volume += 0.05f.coerceAtMost(1f)
                    }



                    if (volume >= 1f) {
                        volume = 1f
                        start = false
                    }
                }

                if (stop) {
                    Timber.e("1 stop $volume")
                    volume = 0f
                    if (volume <= 0f)
                        stop = false
                }

                val bigBufMp3 = chDataStreamOutAudioProcessor.receive()

                if (!isPlaying) {
                    //audioOutMp3.destroy()

//                    while (chDataStreamOutAudioProcessor.tryReceive().isSuccess) {
//                        println("Очистка канала")
//                    }

                } else {

                    if (audioOutMp3.out == null)
                        audioOutMp3.create()

                    val v = ShortArray(bigBufMp3.size)

                    for (i in bigBufMp3.indices) {
                        v[i] = (bigBufMp3[i] * volume).toInt().toShort()
                    }


                    audioOutMp3.out?.write(v, 0, v.size, WRITE_BLOCKING)
                }

                //audioOutMp3.out.write(bigBufMp3, 0, bigBufMp3.size, WRITE_NON_BLOCKING)

                //Блок управления громкостью


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