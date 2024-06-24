package com.example.generator2.features.scope

import com.paramsen.noise.Noise
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.system.measureNanoTime


class NativeLib {
    companion object {
        init {
            System.loadLibrary("generator2")
        }
    }

    external fun copyFloatArrayJNI(source: FloatArray, destination: FloatArray)


    external fun createBuffer(entrySize: Int, bufferSize: Int): Long
    external fun addEntry(bufferPtr: Long, entry: FloatArray)
    external fun toExternalFloatArray(bufferPtr: Long, result: FloatArray)
    external fun destroyBuffer(bufferPtr: Long)

}


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

    var roll256 = FloatRingBuffer(2048)

    val nativeLib = NativeLib()


    var bufferSizeJNI = 4
    var entitySizeJNI = 1024
    var roll256JNI = nativeLib.createBuffer(entitySizeJNI, bufferSizeJNI)


    var sum0: Long = 0L
    var sum1: Long = 0L

    var cnt0: Long = 0L
    var cnt1: Long = 0L

    val noise = Noise.real(4096)
        

    GlobalScope.launch(Dispatchers.IO) {

        var lastCompressorCount = 0f

        while (true) {

            //delay(10)

            if (scope.compressorCount.floatValue >= 1.0F) {

                val buf = scope.channelAudioOut.receive()


                val src = FloatArray(4096)
                val dst = FloatArray(4096 + 2) //real output length equals src+2
                val fft = noise.fft(src, dst)



                val nanos = measureNanoTime {

                    if ((buf.size != entitySizeJNI) || (bufferSizeJNI != scope.compressorCount.floatValue.toInt())) {
                        nativeLib.destroyBuffer(roll256JNI)
                        bufferSizeJNI = scope.compressorCount.floatValue.toInt()
                        entitySizeJNI = buf.size
                        roll256JNI = nativeLib.createBuffer(entitySizeJNI, bufferSizeJNI)

                        sum0 = 0L
                        sum1 = 0L
                        cnt0 = 0L
                        cnt1 = 0L
                    }

                    nativeLib.addEntry(roll256JNI, buf)

                    val samplerate = scope.audioSampleRate
                    val timeBuf = buf.size / 2.0f / samplerate //44100 1152 26ms
                    val herz = 1.0f / timeBuf                   //44100 1152 38.28Hz

//                    println(samplerate)
//                    println(timeBuf)
//                    println(herz)
                    //Количество кадров, которое нужно пропустить

                    val framesSkip = findBestDivisor(
                        herz.toInt(),
                        if (scope.compressorCount.floatValue >= 32) 14.0 else 14.0
                    )

                    //  println(framesSkip)

                    if (
                        frame % framesSkip == 0L
                    //((scope.compressorCount.floatValue >= 32) && (frame % 6 == 0L))
                    //||
                    //(scope.compressorCount.floatValue < 32)
                    ) {

                        totalSize = entitySizeJNI * bufferSizeJNI

                        resultArray =
                            scope.floatArrayPool.getFloatArrayFrame(totalSize)  //FloatArray(totalSize)

                        val timeJNI5 = measureNanoTime {
                            nativeLib.toExternalFloatArray(roll256JNI, resultArray.array)
                        }
                        //    println("!!! > JNI toExternalFloatArray time: ${timeJNI5/1000} us")

                        sum1 += timeJNI5 / 1000
                        cnt0++
                        cnt1++

                        val s =
                            scope.channelDataStreamOutCompressorIndex.trySend(resultArray.frame).isSuccess

                        //if (!s)
                        //    Timber.e("Нет места в channelDataOutRoll")

                    }

                }

                //println("Roll64: ${nanos / 1000} us totalSize $totalSize")

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

fun findBestDivisor(herz: Int, target: Double = 7.0): Int {
    var bestDivisor = 1
    var bestDifference = Double.MAX_VALUE

    for (divisor in 1..herz) {
        val result = herz.toDouble() / divisor
        if (result <= target) {
            val difference = target - result
            if (difference < bestDifference) {
                bestDifference = difference
                bestDivisor = divisor
            }
        }
    }

    return bestDivisor
}

class FloatRingBuffer(val entrySize: Int, val bufferSize: Int = 256) {
    val buffer = FloatArray(entrySize * bufferSize)
    private var start = 0
    private var end = 0
    private var isFull = false

    fun add(entry: FloatArray) {
        require(entry.size == entrySize) { "Entry size must be $entrySize" }

        // Записываем новую запись в буфер
        //entry.copyInto(buffer, end * entrySize)

        System.arraycopy(entry, 0, buffer, end * entrySize, entrySize)

        end = (end + 1) % bufferSize

        // Проверяем, если буфер заполнен, двигаем старт
        if (isFull) {
            start = (start + 1) % bufferSize
        } else if (end == start) {
            isFull = true
        }
    }

    fun toExternalFloatArray(result: FloatArray) {
        if (isFull) {
            // Копируем данные из двух частей буфера
            val part1Size = (bufferSize - start) * entrySize
            System.arraycopy(buffer, start * entrySize, result, 0, part1Size)
            System.arraycopy(buffer, 0, result, part1Size, end * entrySize)
        } else {
            // Копируем данные из одной части буфера
            System.arraycopy(buffer, 0, result, 0, end * entrySize)
        }
    }
}