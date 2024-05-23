package com.example.generator2.features.scope

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.LinkedList
import kotlin.system.measureNanoTime

//            compressorCount
//                  |
// channelAudioOut  *     -> channelDataStreamOutCompressor
//                 >=64   -> channelDataOutRoll
@OptIn(DelicateCoroutinesApi::class)
fun dataCompressor(scope: Scope) {

    val roll64 = LinkedList<FloatArray>()






    GlobalScope.launch(Dispatchers.IO) {

        var lastCompressorCount = 0f

        while (true) {





            if (scope.compressorCount.floatValue >= 1.0F) {

                if (scope.compressorCount.floatValue >= 32) {

                    val buf = scope.channelAudioOut.receive()

                    if (lastCompressorCount != scope.compressorCount.floatValue) {
                        roll64.clear()
                    }

                    while (roll64.size < scope.compressorCount.floatValue)
                        roll64.add(FloatArray(buf.size))

                    while (roll64.size > scope.compressorCount.floatValue)
                        roll64.removeAt(0)

                    roll64.add(buf)

                    val totalSize = roll64.sumOf { it.size }
                    val resultArray = scope.floatArrayPool.getFloatArrayFrame(totalSize)  //FloatArray(totalSize)

//                    if (lastCompressorCount != scope.compressorCount.floatValue) {
//                       for( i in resultArray.array.indices)
//                         {
//                             resultArray.array[i] = 0.0f
//                        }
//                    }

                    val nanos = measureNanoTime {
                        var currentIndex = 0
                        for (floatArray in roll64) {
                            floatArray.copyInto(resultArray.array, currentIndex)
                            currentIndex += floatArray.size
                        }
                    }
                    println("Roll64: ${nanos / 1000} us totalSize $totalSize байт")

                    val s = scope.channelDataStreamOutCompressorIndex.trySend(resultArray.frame).isSuccess

                    //val s = scope.channelDataStreamOutCompressor.trySend(resultArray.array).isSuccess
                    if (!s)
                        Timber.e("Нет места в channelDataOutRoll")

                } else {

                    //1..16
                    val out = mutableListOf<FloatArray>()

                    val t = measureNanoTime {
                        for (i in 0 until scope.compressorCount.floatValue.toInt()) {
                            val buf1 = scope.channelAudioOut.receive()
                            out.add(buf1)
                        }
                    }
                    val totalSize = out.sumOf { it.size }
                    val resultArray = scope.floatArrayPool.getFloatArrayFrame(totalSize)

                    var currentIndex = 0
                    for (floatArray in out) {
                        floatArray.copyInto(resultArray.array, currentIndex)
                        currentIndex += floatArray.size
                    }

                    val s = scope.channelDataStreamOutCompressorIndex.trySend(resultArray.frame).isSuccess

                    if (!s)
                        Timber.e("1..16 Нет места в channelDataStreamOutCompressorIndex")

                    //println("... 1..16:${compressorCount.floatValue.toInt()} | ${t / 1000} us | outsize: ${out.size}")
                    //scope.channelDataStreamOutCompressor.send(out.toFloatArray())
                }


            } else {
                //compressorCount.floatValue < 1.0F
                val buf = scope.channelAudioOut.receive()
                val size = buf.size * scope.compressorCount.floatValue
                val buf2 = buf.copyOf(size.toInt())
                //scope.channelDataStreamOutCompressor.send(buf2)
            }

            lastCompressorCount = scope.compressorCount.floatValue
        }
    }
}
