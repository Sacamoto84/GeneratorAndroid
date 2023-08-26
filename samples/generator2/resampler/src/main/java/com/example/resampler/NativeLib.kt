package com.example.resampler

//https://audio-smarc.sourceforge.net/integratesmarc.html#resample-multiple-signals-concurrently

class NativeLib {

    /**
     * A native method that is implemented by the 'resampler' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    external fun resampleOneImpl( frIn : Int, frOut : Int, buf : DoubleArray, len : Int) : DoubleArray

    companion object {
        // Used to load the 'resampler' library on application startup.
        init {
            System.loadLibrary("resampler")
        }
    }
}