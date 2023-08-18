package com.example.generator2.audio

import android.media.AudioFormat
import android.media.AudioTrack
import android.media.AudioTrack.MODE_STREAM
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import timber.log.Timber

val audioOut = AudioOut()

val chAudioOut = Channel<ShortArray>(capacity = 1, BufferOverflow.SUSPEND)

@OptIn(DelicateCoroutinesApi::class)
class AudioOut(minBufferMs: Int = 500) {

    val out: AudioTrack

    init {

        val minBufferInBytes=4*(48000/1000)*(minBufferMs/1000.0).toInt()

        val audioFormat= AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(48000)
            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
            .build()
        val minBuffer = AudioTrack.getMinBufferSize(48000, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT)

        out= AudioTrack.Builder()
            .setAudioFormat(audioFormat)
            .setBufferSizeInBytes(minBuffer.coerceAtLeast(minBufferInBytes))
            .setTransferMode(MODE_STREAM)
            .build()

        out.play()

        Timber.i("Запуск AudioOut")

        GlobalScope.launch(Dispatchers.IO) {
            while(true) {
                val buf = chAudioOut.receive()
                //Timber.i(buf.joinToString(","))
                Timber.i("${buf.size}")
                out.write(buf, 0, buf.size)
            }
        }
    }




}