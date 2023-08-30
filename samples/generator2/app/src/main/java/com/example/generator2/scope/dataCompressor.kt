package com.example.generator2.scope

import androidx.compose.runtime.mutableFloatStateOf
import com.example.generator2.mp3.channelAudioOut
import com.example.generator2.mp3.channelDataOutRoll
import com.example.generator2.mp3.channelDataStreamOutCompressor
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import libs.structure.FIFO
import timber.log.Timber
import java.util.LinkedList

//Количество пакетов в которое будет упакован выходной канал
val compressorCount = mutableFloatStateOf(1f)
//    set(value) {
//        field = value.coerceIn(1..32)
//    }

val roll64 = LinkedList<FloatArray>()



//            compressorCount
//                  |
// channelAudioOut  *     -> channelDataStreamOutCompressor
//                 >=64   -> channelDataOutRoll

@OptIn(DelicateCoroutinesApi::class)
fun dataCompressor() {

    var rollBuffer = FloatArray(0)

    GlobalScope.launch(Dispatchers.IO) {

//        val a = arrayOf(0f, 0f).toFloatArray()
//        repeat(64)
//        {
//            roll64.add(a)
//        }

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

                        var currentIndex = 0
                        for (floatArray in roll64) {
                            floatArray.copyInto(resultArray, currentIndex)
                            currentIndex += floatArray.size
                        }

                        //println("Отсылка Roll64")

                        val s = channelDataStreamOutCompressor.trySend(resultArray).isSuccess
                        if (!s)
                            Timber.e("Нет места в channelDataOutRoll")

                    }
                    else {

                        for (i in 0 until compressorCount.floatValue.toInt()) {
                            val buf1 = channelAudioOut.receive()
                            out.addAll(buf1.toList())
                        }

                        channelDataStreamOutCompressor.send(out.toFloatArray())

                    }

                //}




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