package com.example.generator2.features.scope

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
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

    val roll64 = ArrayList<FloatArray>()
    var totalSize = 0
    var resultArray: FloatArrayFrame

    var frame = 0L

    GlobalScope.launch(Dispatchers.IO) {

        var lastCompressorCount = 0f

        while (true) {

            delay(10)

            if (scope.compressorCount.floatValue >= 1.0F) {

                val buf = scope.channelAudioOut.receive()

                val nanos = measureNanoTime {

                    if (lastCompressorCount != scope.compressorCount.floatValue) {
                        roll64.clear()
                    }

                    while (roll64.size < scope.compressorCount.floatValue) {
                        roll64.add(FloatArray(buf.size))
                    }

                    while (roll64.size >= scope.compressorCount.floatValue) {
                        roll64.removeAt(0)
                    }

                    roll64.add(buf)

                    if (
                        frame % 6 == 0L
                        //((scope.compressorCount.floatValue >= 32) && (frame % 6 == 0L))
                        //||
                        //(scope.compressorCount.floatValue < 32)
                    ) {

                        totalSize = roll64.sumOf { it.size }

                        resultArray =
                            scope.floatArrayPool.getFloatArrayFrame(totalSize)  //FloatArray(totalSize)

                        var currentIndex = 0
                        for (floatArray in roll64) {
                            floatArray.copyInto(resultArray.array, currentIndex)
                            currentIndex += floatArray.size
                        }

                        val s =
                            scope.channelDataStreamOutCompressorIndex.trySend(resultArray.frame).isSuccess

                        if (!s)
                            Timber.e("Нет места в channelDataOutRoll")

                    }

                }

                println("Roll64: ${nanos / 1000} us totalSize $totalSize")

            } else {
                //compressorCount.floatValue < 1.0F
                val buf = scope.channelAudioOut.receive()
                val size = buf.size * scope.compressorCount.floatValue
                val buf2 = buf.copyOf(size.toInt())
                //scope.channelDataStreamOutCompressor.send(buf2)
            }

            frame++

            lastCompressorCount = scope.compressorCount.floatValue
        }
    }
}
