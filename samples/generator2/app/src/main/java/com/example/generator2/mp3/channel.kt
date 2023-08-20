package com.example.generator2.mp3

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel

val chDataStreamOutAudioProcessor =
    Channel<ShortArray>(capacity = 8, BufferOverflow.SUSPEND)

val channelDataStreamOutGenerator =
    Channel<ShortArray>(capacity = 16, BufferOverflow.DROP_OLDEST)



val channelDataStreamOutCompressor = Channel<ShortArray>(capacity = 16, BufferOverflow.DROP_OLDEST)

//Канал для Roll данных из компрессора
val channelDataOutRoll = Channel<ShortArray>(1, BufferOverflow.DROP_OLDEST)



enum class OSCILLSYNC{
    NONE,
    R,
    L
}

val oscillSync = mutableStateOf(OSCILLSYNC.NONE)


// Класс для создания точки
internal class Pt(var x: Float, var y: Float)


