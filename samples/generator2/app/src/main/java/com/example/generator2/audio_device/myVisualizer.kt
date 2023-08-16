package com.example.generator2.audio_device

import android.media.AudioTrack
import android.media.audiofx.Visualizer
import com.example.generator2.mp3.channelDataStreamOutGenerator
import timber.log.Timber

class MyVisualizer {

    private var audioOutput: Visualizer? = null
    private var visualizedTrack: AudioTrack? = null

    fun createVisualizer() {

        audioOutput = Visualizer(0) // get output audio stream

        audioOutput?.setDataCaptureListener(object : Visualizer.OnDataCaptureListener {

            override fun onWaveFormDataCapture(visualizer: Visualizer, waveform: ByteArray, samplingRate: Int) {
                //visualizedTrack?.write(waveform, 0, waveform.size)
                val length = waveform.size
                val rate = audioOutput?.samplingRate ?: 0
                val size = audioOutput?.captureSize ?: 0
                val a = ShortArray(waveform.size * 2)

                for(i in a.indices step 2)
                {
                    val v = waveform[i/2].toShort() + 128
                    a[i] = (v * 255).toShort()
                }

                channelDataStreamOutGenerator.trySend(a)

                Timber.tag("onWaveFormDataCapture")
                    .i("Есть захват %s байт, rate %s %s", length, samplingRate, size)


            }

            override fun onFftDataCapture(visualizer: Visualizer, fft: ByteArray, samplingRate: Int) {
                // Do nothing for now
            }
        },
            Visualizer.getMaxCaptureRate(), true, false) // waveform not freq data
    }

    fun startVisualizer() {
        audioOutput?.enabled = true
    }

    fun stopVisualizer() {
        audioOutput?.enabled = false
    }

    fun destroyVisualizer() {
        visualizedTrack?.stop()
        visualizedTrack?.release()
        audioOutput?.release()
    }
}