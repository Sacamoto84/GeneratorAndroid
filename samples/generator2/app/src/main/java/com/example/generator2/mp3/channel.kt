package com.example.generator2.mp3

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED

val channelDataStreamOutAudioProcessor =
    Channel<ShortArray>(capacity = 16, BufferOverflow.DROP_OLDEST)

val channelDataStreamOutGenerator =
    Channel<ShortArray>(capacity = 16, BufferOverflow.DROP_OLDEST)


val channelDataStreamOutMixer = Channel<ShortArray>(capacity = 16, BufferOverflow.DROP_OLDEST)

val channelDataStreamOutCompressor = Channel<ShortArray>(capacity = 16, BufferOverflow.DROP_OLDEST)

//Канал для Roll данных из компрессора
val channelDataOutRoll = Channel<ShortArray>(1, BufferOverflow.DROP_OLDEST)

val channelDataOutPoints = Channel < Pair< List<Offset>, List<Offset> > > (1, BufferOverflow.DROP_OLDEST)

val channelDataOutBitmap = Channel <Bitmap> (1, BufferOverflow.DROP_OLDEST)
val channelDataOutLissaguBitmap = Channel <Bitmap> (1, BufferOverflow.DROP_OLDEST)

enum class OSCILLSYNC{
    NONE,
    R,
    L
}

val oscillSync = mutableStateOf(OSCILLSYNC.NONE)


// Класс для создания точки
internal class Pt(var x: Float, var y: Float)


var oscilloscopeW: Float = 1f
var oscilloscopeH: Float = 1f

var oscilloscopeLissaguW: Float = 1f
var oscilloscopeLissaguH: Float = 1f