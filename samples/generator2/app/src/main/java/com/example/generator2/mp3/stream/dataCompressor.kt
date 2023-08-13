package com.example.generator2.mp3.stream

import androidx.compose.runtime.mutableFloatStateOf
import com.example.generator2.mp3.channelDataOutRoll
import com.example.generator2.mp3.channelDataStreamOutAudioProcessor
import com.example.generator2.mp3.channelDataStreamOutCompressor
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import libs.structure.FIFO

//Количество пакетов в которое будет упакован выходной канал
val compressorCount = mutableFloatStateOf(1f)
//    set(value) {
//        field = value.coerceIn(1..32)
//    }

val roll512 : FIFO<ShortArray> = FIFO(512)
val roll256 : FIFO<ShortArray> = FIFO(256)
val roll128 : FIFO<ShortArray> = FIFO(128)

val roll64  : MutableList<ShortArray> = mutableListOf()

@OptIn(DelicateCoroutinesApi::class)
fun dataCompressor() {
    GlobalScope.launch(Dispatchers.IO) {

        val a = arrayOf<Short>(0,0,0,0,0,0).toShortArray()
        repeat(64)
        {
            roll64.add(a)
        }


        while (true) {
            val out = mutableListOf<Short>()
            if (compressorCount.floatValue >= 1.0F) {




                for (i in 0 until compressorCount.floatValue.toInt()) {
                    val buf = channelDataStreamOutAudioProcessor.receive()
                    out.addAll(buf.toList())



                    if (compressorCount.floatValue >= 64) {
                        while (roll64.size > 64) roll64.removeAt(0)
                        roll64.add(buf)
                        val rollBuf = roll64.flatMap { it.asIterable() }.toShortArray()
                        channelDataOutRoll.send(rollBuf)
                    }



                }
                channelDataStreamOutCompressor.send(out.toShortArray())




            } else {
                val buf = channelDataStreamOutAudioProcessor.receive()
                val size = buf.size * compressorCount.floatValue
                val buf2 = buf.copyOf(size.toInt())
                out.addAll(buf2.toList())
                channelDataStreamOutCompressor.send(out.toShortArray())
            }
        }
    }
}