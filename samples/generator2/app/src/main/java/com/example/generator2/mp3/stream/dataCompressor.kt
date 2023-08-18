package com.example.generator2.mp3.stream

import androidx.compose.runtime.mutableFloatStateOf
import com.example.generator2.mp3.chDataStreamOutAudioProcessor

import com.example.generator2.mp3.channelDataOutRoll
import com.example.generator2.mp3.channelDataStreamOutCompressor
import com.example.generator2.mp3.channelDataStreamOutGenerator
import com.example.generator2.scope.scope
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

val roll512: FIFO<ShortArray> = FIFO(512)
val roll256: FIFO<ShortArray> = FIFO(256)
val roll128: FIFO<ShortArray> = FIFO(128)

val roll64 = LinkedList<ShortArray>()

private var rollBuffer = ShortArray(1)

@OptIn(DelicateCoroutinesApi::class)
fun dataCompressor() {

    return

    GlobalScope.launch(Dispatchers.IO) {

        val a = arrayOf<Short>(0, 0, 0, 0, 0, 0).toShortArray()
        repeat(64)
        {
            roll64.add(a)
        }


        while (true) {
            val out = mutableListOf<Short>()

            if (compressorCount.floatValue >= 1.0F) {


                for (i in 0 until compressorCount.floatValue.toInt()) {

                    //val buf = channelDataStreamOutAudioProcessor.receive()
                    val buf = channelDataStreamOutGenerator.receive()

                    out.addAll(buf.toList())

                    GlobalScope.launch(Dispatchers.IO)
                    {
                        val b = dataToLissaguBitmap( buf, scope.scopeLissaguW.toInt(), scope.scopeLissaguH.toInt() )
                        scope.chLissaguBitmap.send(b)
                    }

                    if (compressorCount.floatValue >= 64) {

                        while (roll64.size > 64) roll64.removeAt(0)
                        roll64.add(buf)

                        var fullSize = 0
                        roll64.forEach {
                            fullSize += it.size
                        }
                        //println("Общий размер $fullSize")
                        if (fullSize > rollBuffer.size) {
                            rollBuffer = ShortArray(fullSize) { 0 }
                        }

                        var index = 0
                        roll64.forEach {
                            for (ii in it.indices) {
                                rollBuffer[index] = it[ii]
                                index++
                            }
                        }

                        //val rollBuf = roll64.flatMap { it.asIterable() }.toShortArray()

                        val s = channelDataOutRoll.trySend(rollBuffer).isSuccess
                        if (!s)
                            Timber.e("Нет места в channelDataOutRoll")

                    }


                }
                channelDataStreamOutCompressor.send(out.toShortArray())


            } else {
                //compressorCount.floatValue < 1.0F
                val buf = chDataStreamOutAudioProcessor.receive()

                GlobalScope.launch(Dispatchers.IO)
                {
                    val b = dataToLissaguBitmap( buf, scope.scopeLissaguW.toInt(), scope.scopeLissaguH.toInt() )
                    scope.chLissaguBitmap.send(b)
                }

                val size = buf.size * compressorCount.floatValue
                val buf2 = buf.copyOf(size.toInt())
                out.addAll(buf2.toList())
                channelDataStreamOutCompressor.send(out.toShortArray())
            }
        }
    }
}