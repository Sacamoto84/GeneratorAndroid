package com.example.generator2.mp3

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel

val channelDataStreamOutAudioProcessor =
    Channel<ShortArray>(capacity = 1024, BufferOverflow.DROP_OLDEST)

val channelDataStreamOutCompressor = Channel<ShortArray>(capacity = 16, BufferOverflow.DROP_OLDEST)

//Канал для Roll данных из компрессора
val channelDataOutRoll = Channel<ShortArray>(capacity = 16, BufferOverflow.DROP_OLDEST)

val channelDataOutPoints =
    Channel < Pair< List<Offset>, List<Offset> > > (capacity = 16, BufferOverflow.DROP_OLDEST)

enum class OSCILLSYNC{
    NONE,
    R,
    L
}

val oscillSync = mutableStateOf(OSCILLSYNC.NONE)