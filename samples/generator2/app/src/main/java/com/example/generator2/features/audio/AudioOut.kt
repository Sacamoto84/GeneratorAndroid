package com.example.generator2.features.audio

import android.media.AudioFormat
import android.media.AudioTrack
import android.media.AudioTrack.MODE_STREAM
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

class AudioOut(
    val sampleRate: Int = 48000,
    val minBufferMs: Int = 1000,
    val encoding: Int = AudioFormat.ENCODING_PCM_FLOAT
) {

    companion object {

        /**
         * Частота аудио выхода
         */
        val AudioSampleRate = MutableStateFlow(0) //Частота которая используется на аудиовыводе, для UI

    }

    /**
     * Признак того что устройство поддерживает 192k
     */
    var isDeviceSupport192k = false

    var out: AudioTrack? = null

    init {

        try {
            val minBufferInBytes = 4 * (sampleRate / 1000) * minBufferMs / 1000
            val audioFormat = AudioFormat.Builder().setEncoding(encoding).setSampleRate(sampleRate).setChannelMask(AudioFormat.CHANNEL_OUT_STEREO).build()
            val minBuffer = AudioTrack.getMinBufferSize( sampleRate, AudioFormat.CHANNEL_OUT_STEREO, encoding )

            out = AudioTrack.Builder()
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(minBuffer.coerceAtLeast(minBufferInBytes))
                .setTransferMode(MODE_STREAM)
                .build()

            out?.play()
            out?.let { AudioSampleRate.value = it.sampleRate }
            Timber.w("Запуск AudioOut ${out?.sampleRate}")
        } catch (e: Exception) {
            Timber.e( e,"Error initializing AudioOut with sampleRate=$sampleRate, encoding=$encoding" )
        }

    }

    fun destroy() {
        Timber.w("AudioOut destroy")
        out?.let {
            it.stop()
            it.flush()
            it.release()
        }
        out = null
    }

    fun checkSupport192k() {
        try {
            val minBuffer = AudioTrack.getMinBufferSize(
                192000,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_FLOAT
            )

            isDeviceSupport192k = minBuffer > 0

        } catch (e: Exception) {
            Timber.e(e, "Error checking support for 192kHz")
        }
    }

}