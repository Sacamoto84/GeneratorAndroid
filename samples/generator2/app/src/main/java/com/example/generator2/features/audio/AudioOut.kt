package com.example.generator2.features.audio

import android.media.AudioFormat
import android.media.AudioTrack
import android.media.AudioTrack.MODE_STREAM
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(DelicateCoroutinesApi::class)
class AudioOut(
    val sampleRate: Int = 48000,
    val minBufferMs: Int = 1000,
    val encoding: Int = AudioFormat.ENCODING_PCM_FLOAT
) {

    /**
     * Признак того что устройство поддерживает 192k
     */
    var isDeviceSupport192k = false

    var out: AudioTrack? = null

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

            out =
                AudioTrack.Builder()
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(minBuffer.coerceAtLeast(minBufferInBytes))
                    .setTransferMode(MODE_STREAM)
                    .build()

            out!!.play()

            GlobalScope.launch { AudioSampleRate.value = out!!.sampleRate }

            Timber.w("Запуск AudioOut ${out!!.sampleRate}")

        } catch (e: Exception) {
            Timber.e(e.localizedMessage)
        }


    }

    fun destroy() {
        Timber.w("AudioOut destroy")
        out?.stop()
        out?.flush()
        out?.release()
    }

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


}