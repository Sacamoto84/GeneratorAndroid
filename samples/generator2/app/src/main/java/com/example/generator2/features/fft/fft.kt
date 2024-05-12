package com.example.generator2.features.fft

import com.paramsen.noise.Noise

class FFT{

    companion object {
        const val SAMPLE_SIZE = 4096
    }

    private var noise: Noise? = null

    private val src = FloatArray(SAMPLE_SIZE)
    private val dst = FloatArray(SAMPLE_SIZE + 2)

    interface FFTListener {
        fun onFFTReady(sampleRateHz: Int, channelCount: Int, fft: FloatArray)
    }

    var listener: FFTListener? = null

}