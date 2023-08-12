package com.example.generator2.mp3.stream

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel

val channelDataStreamOutAudioProcessor = Channel<ShortArray>(capacity = 1024, BufferOverflow.DROP_OLDEST)
val channelDataStreamOutCompressor = Channel<ShortArray>(capacity = 1024, BufferOverflow.DROP_OLDEST)

