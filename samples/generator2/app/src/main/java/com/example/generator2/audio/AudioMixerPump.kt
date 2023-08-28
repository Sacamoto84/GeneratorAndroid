package com.example.generator2.audio

import android.media.AudioFormat
import android.media.AudioTrack.WRITE_BLOCKING
import com.example.generator2.generator.gen
import com.example.generator2.mp3.chDataStreamOutAudioProcessor
import com.example.generator2.mp3.exoplayer
import com.example.generator2.mp3.processor.audioProcessorInputFormat
import com.example.generator2.util.bufMerge
import com.example.generator2.util.bufSpit
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Duration
import java.time.LocalDateTime
import java.util.LinkedList
import kotlin.system.measureNanoTime


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

        var start = false
        var volume = 0f

        var delay = 50
        //

        var bufferSize = 8192

        val calculator = Calculator()
        val calculator2 = Calculator()

//        val currentEventTime = LocalDateTime.now()
//        var lastEventTime = LocalDateTime.now()

        GlobalScope.launch(Dispatchers.IO) {

            var init = false

            while (!init) {
                try {
                    exoplayer.player
                    init = true
                } catch (e: UninitializedPropertyAccessException) {
                    init = false
                }
            }

            GlobalScope.launch(Dispatchers.IO) {

                var isPlayingLast = false
                while (true) {
                    isPlaying = exoplayer.isPlayingD
                    if (isPlaying and !isPlayingLast) {
                        start = true; delay = 20
                    }
                    isPlayingLast = isPlaying
                    delay(1)
                }
            }

            while (true) {


                if (exoplayer.isPlayingD) {

//                    val duration = Duration.between(lastEventTime, LocalDateTime.now()).toMillis()
//                    lastEventTime = LocalDateTime.now()
//                    println("Частота вызова mp3: "+duration+" ms")

                    val bigBufMp3 = chDataStreamOutAudioProcessor.receive()

                    bufferSize = bigBufMp3.size

                    //println("bigBufMp3.size ${bigBufMp3.size}")
                    if (start) {
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

                    if ((audioProcessorInputFormat.value.sampleRate != audioOut.sampleRate) or (audioOut.out.format.encoding != AudioFormat.ENCODING_PCM_FLOAT)) {

                        audioOut.destroy(); audioOut =
                            AudioOut(audioProcessorInputFormat.value.sampleRate, 800)

                    }




//                    val bufGen: ShortArray
                    if ((routeL.value == ROUTESTREAM.GEN) or (routeR.value == ROUTESTREAM.GEN ))
                    {
                        gen.sampleRate = audioProcessorInputFormat.value.sampleRate
                        bufGen = gen.renderAudio(bufferSize)
                    }
                    else
                        bufGen = FloatArray(bufferSize)
//
//
//                    for (i in bigBufMp3.indices) {
//                        bigBufMp3[i] = (bigBufMp3[i] * volume).toInt().toShort()
//                    }
//
                    val (bufGenL, bufGenR) = bufSpit(bufGen)
                    val (bufMp3L, bufMp3R) = bufSpit(bigBufMp3)
//
                    val outR = when(routeR.value) {
                        ROUTESTREAM.MP3 -> bufMp3R
                        ROUTESTREAM.GEN -> bufGenR
                        ROUTESTREAM.OFF -> FloatArray(bufferSize/2){0}
                    }

                    val outL = when(routeL.value) {
                        ROUTESTREAM.MP3 -> bufMp3L
                        ROUTESTREAM.GEN -> bufGenL
                        ROUTESTREAM.OFF -> FloatArray(bufferSize/2){0}
                    }
//
//                    val enL = gen.liveData.enL.value
//                    val enR = gen.liveData.enR.value
//
//                    val v = bufMerge(outL, outR, enL ,enR)


                    //LRLRLR
//                    audioOut.out.write(v, 0, v.size, WRITE_BLOCKING)

                } else {

//                    val duration = Duration.between(lastEventTime, LocalDateTime.now()).toMillis()
//                    lastEventTime = LocalDateTime.now()
//                    calculator2.update(duration.toDouble())
//                    println("Частота вызова: " + duration + " ms AVG: ${calculator2.getAvg()} ms")



                    while (chDataStreamOutAudioProcessor.tryReceive().isSuccess) {
                        println("Очистка канала")
                    }

                    if ((routeL.value != ROUTESTREAM.MP3) and (routeR.value != ROUTESTREAM.MP3)) {
                        if ((audioOut.out.sampleRate != 192000) or (audioOut.out.format.encoding != AudioFormat.ENCODING_PCM_FLOAT)) {
                            audioOut.destroy(); audioOut =
                                AudioOut(192000, 800, encoding = AudioFormat.ENCODING_PCM_FLOAT)
                        }
                    }

                    gen.sampleRate = audioOut.sampleRate

                    //val bufGen: ShortArray

                    //val bufGenL : FloatArray
                    //val bufGenR : FloatArray

                    val buf: Pair<FloatArray, FloatArray>

                    //mi8  2220us release 192k 8192
                    //9060 3940us release 192k 8192
                    val nanos = measureNanoTime {
                        buf = gen.renderAudio(bufferSize)
                    }

                    calculator.update(nanos / 1000.0)
                    println("measure :${nanos / 1000.0} us bufferSize: $bufferSize среднее ${calculator.getAvg()}")


                    //val (bufGenL, bufGenR) = bufSpit(bufGen)

                    val outR = when (routeR.value) {
                        ROUTESTREAM.MP3 -> FloatArray(buf.second.size)
                        ROUTESTREAM.GEN -> buf.second
                        ROUTESTREAM.OFF -> FloatArray(bufferSize / 2)
                    }

                    val outL = when (routeL.value) {
                        ROUTESTREAM.MP3 -> FloatArray(buf.first.size)
                        ROUTESTREAM.GEN -> buf.first
                        ROUTESTREAM.OFF -> FloatArray(bufferSize / 2)
                    }
                    val enL = gen.liveData.enL.value
                    val enR = gen.liveData.enR.value

                    val v = bufMerge(outL, outR, enL, enR)  //? нужно проверить

                    audioOut.out.write(v, 0, v.size, WRITE_BLOCKING)

                }


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


class Calculator {
    private val data = mutableListOf<Double>()

    fun update(value: Double) {
        synchronized(data) {
            data.add(value)

            while (data.size > 1000)
                data.removeAt(0)

        }
    }

    fun getMin(): Double {
        synchronized(data) {
            return data.minOrNull() ?: 0.0
        }
    }

    fun getMax(): Double {
        synchronized(data) {
            return data.maxOrNull() ?: 0.0
        }
    }

    fun getAvg(): Double {
        synchronized(data) {
            if (data.isEmpty()) return 0.0
            val sum = data.sum()
            return sum / data.size
        }
    }
}