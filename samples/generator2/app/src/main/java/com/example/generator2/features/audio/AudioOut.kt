package com.example.generator2.features.audio

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.AudioTrack.MODE_STREAM
import android.os.Build
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber




fun checkSupport192k() {
    try {
        val minBuffer = AudioTrack.getMinBufferSize(
            192000,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_FLOAT
        )

        if (minBuffer >= 0)
            isDeviceSupport192k = true

    } catch (e: Exception) {
        Timber.e(e.localizedMessage)
    }
}


@OptIn(DelicateCoroutinesApi::class)
class AudioOut(
    val sampleRate: Int = 48000,
    minBufferMs: Int = 1000,
    encoding: Int = AudioFormat.ENCODING_PCM_FLOAT
) {

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

            out = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AudioTrack.Builder()
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(minBuffer.coerceAtLeast(minBufferInBytes))
                    .setTransferMode(MODE_STREAM)
                    .build()
            } else {
                AudioTrack(
                    AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_STEREO,
                    encoding, minBuffer.coerceAtLeast(minBufferInBytes), MODE_STREAM
                )
            }

            out.play()

            val s = out.sampleRate

            GlobalScope.launch { AudioSampleRate.value = out.sampleRate }

            Timber.w("Запуск AudioOut ${out.sampleRate}")

        } catch (e: Exception) {
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