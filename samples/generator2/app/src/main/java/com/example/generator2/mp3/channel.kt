package com.example.generator2.mp3

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel

val chDataStreamOutAudioProcessor = Channel<FloatArray>(capacity = 48, BufferOverflow.DROP_LATEST)

//Выход аудиоданных -> compressor
val channelAudioOut = Channel<FloatArray>(capacity = 8, BufferOverflow.DROP_OLDEST)

val channelAudioOutLissagu = Channel<FloatArray>(capacity = 8, BufferOverflow.DROP_OLDEST)

val channelDataStreamOutCompressor = Channel<FloatArray>(capacity = 1, BufferOverflow.DROP_LATEST)


enum class OSCILLSYNC {
    NONE, R, L
}


val oscillSync = mutableStateOf(OSCILLSYNC.L)



