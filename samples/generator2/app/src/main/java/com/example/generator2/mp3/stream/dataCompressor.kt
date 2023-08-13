package com.example.generator2.mp3.stream

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
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
val roll64 : FIFO<ShortArray> = FIFO(64)

@OptIn(DelicateCoroutinesApi::class)
fun dataCompressor() {
    GlobalScope.launch(Dispatchers.IO) {
        while (true) {
            val out = mutableListOf<Short>()
            if (compressorCount.floatValue >= 1.0F) {




                for (i in 0 until compressorCount.floatValue.toInt()) {
                    val buf = channelDataStreamOutAudioProcessor.receive()
                    out.addAll(buf.toList())
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