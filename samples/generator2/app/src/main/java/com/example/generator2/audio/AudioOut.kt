package com.example.generator2.audio

import android.media.AudioFormat
import android.media.AudioTrack
import android.media.AudioTrack.MODE_STREAM
import timber.log.Timber

var audioOut : AudioOut? = AudioOut(48000,0)
val audioOutMp3 = AudioOut(200)


class AudioOut(val sampleRate: Int = 48000, minBufferMs: Int = 1000) {

    lateinit var out: AudioTrack

    init {

        try {


            val minBufferInBytes = 4 * (sampleRate / 1000) * (minBufferMs / 1000.0).toInt()

            val audioFormat = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                .build()
            val minBuffer = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            out = AudioTrack.Builder()
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(minBuffer.coerceAtLeast(minBufferInBytes))
                .setTransferMode(MODE_STREAM)
                .build()

            out.play()

            Timber.i("Запуск AudioOut")

        }
        catch (e:Exception)
        {
            Timber.e(e.localizedMessage)
        }


    }

    fun destroy() {
        out.stop()
        out.flush()
        out.release()
    }

}