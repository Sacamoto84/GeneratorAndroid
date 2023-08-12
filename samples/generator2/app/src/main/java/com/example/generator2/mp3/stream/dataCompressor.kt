package com.example.generator2.mp3.stream

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

val channelDataStreamOutAudioProcessor =
    Channel<ShortArray>(capacity = 1024, BufferOverflow.DROP_OLDEST)
val channelDataStreamOutCompressor =
    Channel<ShortArray>(capacity = 1024, BufferOverflow.DROP_OLDEST)

//Количество пакетов в которое будет упакован выходной канал
var compressorCount = 1
    set(value) {
        field = value.coerceIn(1..10)
    }

@OptIn(DelicateCoroutinesApi::class)
fun dataCompressor() {
    GlobalScope.launch(Dispatchers.IO) {
        while (true) {
            val out = mutableListOf<Short>()
            for (i in 0 until compressorCount) {
                val buf = channelDataStreamOutAudioProcessor.receive()
                out.addAll(buf.toList())
            }
            channelDataStreamOutCompressor.send(out.toShortArray())
        }
    }
}