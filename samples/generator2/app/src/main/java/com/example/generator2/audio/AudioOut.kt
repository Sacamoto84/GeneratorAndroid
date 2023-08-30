package com.example.generator2.audio

import android.media.AudioFormat
import android.media.AudioTrack
import android.media.AudioTrack.MODE_STREAM
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

var audioOut : AudioOut = AudioOut(48000,200, AudioFormat.ENCODING_PCM_FLOAT)

val AudioSampleRate = MutableStateFlow(0) //Частота которая используется на аудиовыводе, для UI

@OptIn(DelicateCoroutinesApi::class)
class AudioOut(val sampleRate: Int = 48000, minBufferMs: Int = 1000, encoding: Int = AudioFormat.ENCODING_PCM_16BIT) {

    lateinit var out: AudioTrack

    init {

        try {

            val minBufferInBytes = 4 * (sampleRate / 1000) * (minBufferMs / 1000.0).toInt()

            val audioFormat = AudioFormat.Builder()
                .setEncoding(encoding)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                .build()

            val minBuffer = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_STEREO,
                encoding
            )

            out = AudioTrack.Builder()
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(minBuffer.coerceAtLeast(minBufferInBytes))
                .setTransferMode(MODE_STREAM)
                .build()

            out.play()

            val s = out.sampleRate

            GlobalScope.launch { AudioSampleRate.value = out.sampleRate }

            Timber.w("Запуск AudioOut ${out.sampleRate}")

        }
        catch (e:Exception)
        {
            Timber.e(e.localizedMessage)
        }


    }

    fun destroy() {
        Timber.w("AudioOut destroy")
        out.stop()
        out.flush()
        out.release()
    }

}