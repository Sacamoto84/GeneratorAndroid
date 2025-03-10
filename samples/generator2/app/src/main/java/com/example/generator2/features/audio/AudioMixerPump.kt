package com.example.generator2.features.audio

import android.content.Context
import android.media.AudioFormat
import android.media.AudioTrack.WRITE_BLOCKING
import com.example.generator2.App
import com.example.generator2.features.generator.Generator
import com.example.generator2.features.initialization.utils.listFilesInAssetsFolder
import com.example.generator2.features.mp3.PlayerMP3
import com.example.generator2.features.mp3.processor.audioProcessorInputFormat
import com.example.generator2.features.scope.Scope
import com.example.generator2.model.itemList
import com.example.generator2.util.MeasureMicroAvg
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import java.util.LinkedList
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

enum class ROUTESTREAM {
    GEN,
    MP3,
    OFF,
}

val a48 = 44100

@androidx.media3.common.util.UnstableApi
class AudioMixerPump
    (
    context: Context,
    val gen: Generator
) {

    //PUBLIC
    val routeR = MutableStateFlow(ROUTESTREAM.GEN) //Выбор источника для вывода сигнала
    val routeL = MutableStateFlow(ROUTESTREAM.OFF)

    val invertL = MutableStateFlow(false)
    val invertR = MutableStateFlow(false)

    val shuffle = MutableStateFlow(false)

    //DI

    //Звуковая аудиовыхода
    var audioOut: AudioOut = AudioOut(a48, 200, AudioFormat.ENCODING_PCM_FLOAT)
    private val isDeviceSupport192k = audioOut.isDeviceSupport192k.or(true)

    val scope = Scope()

    val exoplayer = PlayerMP3(context)

    //val bufferSizeGenDefault = 8192 //размер буфера по умолчанию для генератора
    //var bufferSize: Int = bufferSizeGenDefault //Текущий размер буфера который берется от размера буфера от плеера
    //var bufferSizeMp3 = 0

    init {
        Timber.i("Запуск AudioMixerPump ")
        //run()
    }


    @OptIn(DelicateCoroutinesApi::class)
    @androidx.media3.common.util.UnstableApi
    suspend fun run() {

        //Имеется два источника синхронизации, это наличие пакетов в chDataStreamOutAudioProcessor и он является главным и запись иет в неблокирующем режиме,
        //И когда только генератор, и запись в блокирующем режиме

        var isPlaying: Boolean

        var start = false
        var volume = 0f

        var delay = 50
        //

        var bufferSize = 1152 * 2 //R+L

        val measureMicroAvg = MeasureMicroAvg()

        //GlobalScope.launch(Dispatchers.Default) {

        var init = false

        while (!init) {
            init = try {
                exoplayer.player
                true
            } catch (e: UninitializedPropertyAccessException) {
                false
            }
        }

        var outL: FloatArray
        var outR: FloatArray

//            GlobalScope.launch(Dispatchers.IO) {
//
//                var isPlayingLast = false
//                while (true) {
//                    isPlaying = exoplayer.isPlayingD
//                    if (isPlaying and !isPlayingLast) {
//                        start = true; delay = 20
//                    }
//                    isPlayingLast = isPlaying
//                    delay(1)
//                }
//            }

        while (true) {


            if (exoplayer.isPlayingD.value) {

//                    val duration = Duration.between(lastEventTime, LocalDateTime.now()).toMillis()
//                    lastEventTime = LocalDateTime.now()
//                    println("Частота вызова mp3: "+duration+" ms")

                val bigBufMp3 = exoplayer.streamOut.receive()

                bufferSize = bigBufMp3.size

                if (bufferSize == 0)
                    continue

                //println("bigBufMp3.size ${bigBufMp3.size}")

//                    if (start) {
//                        Timber.e("1 start $volume $delay")
//                        if (delay > 0) {
//                            delay--; volume = 0.01f
//                        } else {
//                            volume += 0.05f
//                        }
//                        if (volume >= 1f) {
//                            volume = 1f; start = false
//                        }
//                    }


                if ((audioProcessorInputFormat.value.sampleRate != audioOut.sampleRate)) {
                    audioOut.destroy(); audioOut = AudioOut(
                        audioProcessorInputFormat.value.sampleRate,
                        200,
                        AudioFormat.ENCODING_PCM_FLOAT
                    )
                }


                val bufGenL: FloatArray
                val bufGenR: FloatArray

                if ((routeL.value == ROUTESTREAM.GEN) or (routeR.value == ROUTESTREAM.GEN)) {
                    gen.sampleRate = audioProcessorInputFormat.value.sampleRate

                    val p = gen.renderAudio(bufferSize)

                    bufGenL = p.first
                    bufGenR = p.second
                } else {
                    bufGenL = FloatArray(bufferSize)
                    bufGenR = FloatArray(bufferSize)
                }
//
//                    for (i in bigBufMp3.indices) {
//                        bigBufMp3[i] = bigBufMp3[i] * volume
//                    }

                val (bufMp3L, bufMp3R) = split(bigBufMp3)
//
                outR = when (routeR.value) {
                    ROUTESTREAM.MP3 -> bufMp3R
                    ROUTESTREAM.GEN -> bufGenR
                    ROUTESTREAM.OFF -> FloatArray(bufferSize / 2)
                }

                outL = when (routeL.value) {
                    ROUTESTREAM.MP3 -> bufMp3L
                    ROUTESTREAM.GEN -> bufGenL
                    ROUTESTREAM.OFF -> FloatArray(bufferSize / 2)
                }



            } else {

//                    val duration = Duration.between(lastEventTime, LocalDateTime.now()).toMillis()
//                    lastEventTime = LocalDateTime.now()
//                    calculator2.update(duration.toDouble())
//                    println("Частота вызова: " + duration + " ms AVG: ${calculator2.getAvg()} ms")

                while (exoplayer.streamOut.tryReceive().isSuccess) {
                    println("Очистка канала")
                }

                //8192 LR-4096    192k -> 21.3ms 48k->85.4ms

                //Перевод на 192k только если есть поддержка устройтвом
                if ((routeL.value == ROUTESTREAM.GEN) and (routeR.value == ROUTESTREAM.GEN)) {
                    if ((audioOut.out!!.sampleRate != 192000) and isDeviceSupport192k) {
                        Timber.w("Меняем частоту на 192k")
                        audioOut.destroy()
                        audioOut = AudioOut(192000, 200, encoding = AudioFormat.ENCODING_PCM_FLOAT)
                    }
                } else {
                    if (audioOut.out!!.sampleRate == 192000) {
                        Timber.w("Меняем частоту на 48k")
                        audioOut.destroy()
                        audioOut = AudioOut(a48, 200, AudioFormat.ENCODING_PCM_FLOAT)
                    }
                }

                gen.sampleRate = audioOut.sampleRate
                scope.audioSampleRate = audioOut.sampleRate

                val buf: Pair<FloatArray, FloatArray>

                //8192 LR-4096    192k -> 21.3ms 48k->85.4ms
                //8192

                // S7         1200us   5.6% | 192K   21.3ms
                //                     1.4% |  48k   85.4ms
                // X6 Pro      350us   1.6% | 192k
                //                     0.4% |  48k
                // 23 Ultra     70us
                // 22 Ultra    750us
                // Pixel8 Pro  990us

                //mi8  2220us release 192k 8192
                //9060 3940us release 192k 8192
                val nanos = measureNanoTime {
                    val t = measureMicroAvg.measureNanoTime {//measureNanoTime {
                        buf = gen.renderAudio(bufferSize)
                    }
                    //println("!!! renderAudio ${measureMicroAvg.avgUs.toInt()} us ${measureMicroAvg.count} t : ${t} us")
                }

                //calculator.update(nanos / 1000.0)

                //println("measure :${nanos / 1000.0} us bufferSize: $bufferSize среднее ${calculator.getAvg()}")

                outR = when (routeR.value) {
                    ROUTESTREAM.MP3 -> FloatArray(buf.second.size)
                    ROUTESTREAM.GEN -> buf.second
                    ROUTESTREAM.OFF -> FloatArray(bufferSize / 2)
                }

                outL = when (routeL.value) {
                    ROUTESTREAM.MP3 -> FloatArray(buf.first.size)
                    ROUTESTREAM.GEN -> buf.first
                    ROUTESTREAM.OFF -> FloatArray(bufferSize / 2)
                }

            }

            //───────────────────────────────────────────────┐
            // Инверсия                                      │
            //───────────────────────────────────────────────┤
            //                                               │
            if (invertL.value) for (i in outL.indices) {  // │
                outL[i] = -outL[i]                        // │
            }                                             // │
            //                                               │
            if (invertR.value) for (i in outR.indices) {  // │
                outR[i] = -outR[i]                        // │
            }                                             // │
            //───────────────────────────────────────────────┘
            //───────────────────────────────────────────────┐
            // Переворот канала                              │
            //───────────────────────────────────────────────┤
            val v = if (shuffle.value) {                  // │
                bufMerge(outL, outR)                      // │
            } else {                                      // │
                //Нормальный режим                        // │
                bufMerge(outR, outL)                      // │
            }                                             // │
            //───────────────────────────────────────────────┘
            //Отравили в scope
            if (scope.isUse.value) {
                scope.channelAudioOut.send(v)
                //scope.channelAudioOutLissagu.send(v)
            }

            //───────────────────────────────────────────────┐
            // Вывод в аудио устройство                      │
            //───────────────────────────────────────────────┤
            // LRLRLR
            audioOut.out?.write(v, 0, v.size, WRITE_BLOCKING)
            //───────────────────────────────────────────────┘

        }



    }


    @OptIn(DelicateCoroutinesApi::class)
    suspend fun initializationGen() {

        val s1 = GlobalScope.async(Dispatchers.Main) {
            val t = measureTimeMillis {
                Timber.tag("Время работы").i("firstDeferred start")
                val arrFilesCarrier = listFilesInAssetsFolder(App.application, "Carrier")
                for (i in arrFilesCarrier.indices) {
                    gen.itemlistCarrier.add(itemList("Carrier", arrFilesCarrier[i], 0))
                }
            }
            Timber.tag("Время работы").i("firstDeferred stop : $t ms")
        }

        val s2 = GlobalScope.async(Dispatchers.IO) {
            val t111 = measureTimeMillis {
                Timber.tag("Время работы").i("secondDeferred start")
                val arrFilesMod = listFilesInAssetsFolder(App.application, "Mod")
                //listFileInDir(appPath.mod) //Получение списка файлов в папке Mod //6ms
                for (i in arrFilesMod.indices) {
                    gen.itemlistAM.add(itemList("Mod", arrFilesMod[i], 1)) //648ms -> 369 -> 207
                    gen.itemlistFM.add(itemList("Mod", arrFilesMod[i], 0)) // all 65ms
                }
            }
            Timber.tag("Время работы").i("secondDeferred stop : $t111 ms")
        }

        s1.await()
        s2.await()

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

class Calculator(val count: Int = 1000) {
    private val data = mutableListOf<Double>()

    fun update(value: Double) {
        synchronized(data) {
            data.add(value)

            while (data.size > count)
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