package com.example.generator2

import android.graphics.Bitmap

object Spectrogram {

    init {
        System.loadLibrary("plasma")
    }

    /**
     *  Длина массива данных (число точек, по которым будет выполняться преобразование Фурье). 4096
     */
    external fun getFftLength(): Int


    /**
     * Задать высоту в пикселях бара падающей волны, по умолчанию 500
     */
    external fun setBarsHeight(barsHeight: Int)




    external fun SetOverlap(timeOverlap: Float)

    external fun SetVolume(volume: Float)
    external fun GetVolume(): Float

    external fun SetAverageCount(progress: Int)
    external fun GetAverageCount(): Int
    external fun SetDecay(decay: Float)
    external fun GetOverlap(): Float
    external fun GetDecay(): Float

    external fun XToFreq(x: Double): Float
    external fun FreqToX(freq: Double): Float

    external fun SetScaler(width: Int, min: Double, max: Double, bLogX: Boolean, bLogY: Boolean)

    external fun setProcessorFFT(length: Int)
    external fun setSampleRate(samplerate: Int)




    external fun HoldData()
    external fun ClearHeldData()
    external fun ResetScanline()
    external fun Init(bitmap: Bitmap?)
    external fun ConnectWithAudioMT()
    external fun Disconnect()
    external fun Lock(bitmap: Bitmap?): Int
    external fun Unlock(bitmap: Bitmap?)
    external fun GetDroppedFrames(): Int
    external fun GetDebugInfo(): String?
//////////////////////////////////////////////
    /**
     * Запуск потока для работы с FFT, запускается один раз
     */
    external fun startFFTLoop()

    /**
     * Отправить порцию данных в буфер FloatRingBufferFFT
     */
    external fun sentToFloatRingBufferFFT(buf : FloatArray, len : Int)
}