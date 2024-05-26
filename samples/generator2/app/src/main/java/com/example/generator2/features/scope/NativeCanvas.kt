package com.example.generator2.features.scope

import android.graphics.Bitmap

class NativeCanvas {

    companion object {
        init {
            System.loadLibrary("generator2")
        }
    }


    external fun jniCanvas(
        bigPointnL: FloatArray,
        bigPointnR: FloatArray,
        bufRN: FloatArray,
        bufLN: FloatArray,
        w: Int,
        h : Int,
        maxPixelBuffer: Int,
        bitmap: Bitmap,
        isOneTwo : Boolean,

    )

}