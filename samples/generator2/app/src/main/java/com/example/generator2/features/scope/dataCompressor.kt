package com.example.generator2.features.scope

import androidx.compose.runtime.mutableFloatStateOf
import com.example.generator2.features.mp3.channelAudioOut
import com.example.generator2.features.mp3.channelDataStreamOutCompressor
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.LinkedList
import kotlin.system.measureNanoTime


/*

   1   |   26 ms  | 38.28 Hz |   1152 |   2304
   2   |   52 ms  | 19.14 Hz |   2304 |   4608
   4   |  104 ms  |  9.57 Hz |   4608 |   9216
   8   |  208 ms  |  4.78 Hz |   9216 |  18432
   16  |  418 ms  |  2.4  Hz |  18432 |  36864
   32  |  836 ms  |  1.2  Hz |  36864 |  73728
   64  |  1.64 s  |  0.6  Hz |  73728 | 147456
   128 |  3.34 s  |  0.3  Hz | 147456 | 294912
   256 |  6.68 s  |  0.15 Hz | 294912 | 589824

 */
//Количество пакетов в которое будет упакован выходной канал
val compressorCount = mutableFloatStateOf(1f)

val roll64 = LinkedList<FloatArray>()

//            compressorCount
//                  |
// channelAudioOut  *     -> channelDataStreamOutCompressor
//                 >=64   -> channelDataOutRoll

@OptIn(DelicateCoroutinesApi::class)
fun dataCompressor() {


    GlobalScope.launch(Dispatchers.IO) {

        var lastCompressorCount = 0f

        while (true) {
            val out = mutableListOf<Float>()

            if (compressorCount.floatValue >= 1.0F) {


                //for (i in 0 until compressorCount.floatValue.toInt()) {

                //val buf = channelAudioOut.receive()

                if (compressorCount.floatValue >= 32) {


                    val buf = channelAudioOut.receive()

                    if (lastCompressorCount != compressorCount.floatValue) {
                        roll64.clear()
                    }

                    while (roll64.size < compressorCount.floatValue)
                        roll64.add(FloatArray(buf.size))


                    while (roll64.size > compressorCount.floatValue)
                        roll64.removeAt(0)

                    roll64.add(buf)

                    val totalSize = roll64.sumOf { it.size }
                    val resultArray = FloatArray(totalSize)

                    val nanos = measureNanoTime {

                        var currentIndex = 0

                        for (floatArray in roll64) {
                            floatArray.copyInto(resultArray, currentIndex)
                            currentIndex += floatArray.size
                        }

                    }
                    println("Roll64: ${nanos / 1000} us totalSize $totalSize байт")
                    //println("Отсылка Roll64")


                    val s = channelDataStreamOutCompressor.trySend(resultArray).isSuccess
                    if (!s)
                        Timber.e("Нет места в channelDataOutRoll")


                } else {

                    //1..16
                    val t = measureNanoTime {
                        for (i in 0 until compressorCount.floatValue.toInt()) {
                            val buf1 = channelAudioOut.receive()
                            out.addAll(buf1.toList())
                        }
                    }
                    //println("... 1..16:${compressorCount.floatValue.toInt()} | ${t / 1000} us | outsize: ${out.size}")
                    channelDataStreamOutCompressor.send(out.toFloatArray())
                }


            } else {
                //compressorCount.floatValue < 1.0F
                val buf = channelAudioOut.receive()
                val size = buf.size * compressorCount.floatValue
                val buf2 = buf.copyOf(size.toInt())
                channelDataStreamOutCompressor.send(buf2)
            }

            lastCompressorCount = compressorCount.floatValue
        }
    }

}
