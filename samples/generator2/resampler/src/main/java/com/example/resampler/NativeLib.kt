package com.example.resampler

class NativeLib {

    /**
     * A native method that is implemented by the 'resampler' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'resampler' library on application startup.
        init {
            System.loadLibrary("resampler")
        }
    }
}