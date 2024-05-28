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
        h: Int,
        maxPixelBuffer: Int,
        isOneTwo: Boolean,
        start: Int,
        end: Int
    )


    external fun jniCanvasBitmap(
        bigPointnL: FloatArray,
        bigPointnR: FloatArray,
        bitmap: Bitmap,
        enableL: Boolean,
        enableR: Boolean,
        start : Int,
        length : Int
    )

    external fun fill( array : FloatArray, len : Int )


    external fun fillArrayWithZero(array : FloatArray, length : Int)



/////////////////////////////////////////////////////
    /**
     * Создать экземпляр нативного скоупа
     * @return адресс класса
     */
    external fun createNativeScope() : Long
    external fun dectroyNativeScope(scope : Long)

    /**
     * Разделить буффер на части и записать в Scope
     */
    external fun split(scope : Long, buf : FloatArray, length : Int)


}