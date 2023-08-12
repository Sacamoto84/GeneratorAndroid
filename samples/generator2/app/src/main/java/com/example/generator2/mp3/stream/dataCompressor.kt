package com.example.generator2.mp3.stream

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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