package com.example.generator2.features.scope

import java.nio.ByteBuffer

object NativeFloatDirectBuffer {

    init {
        System.loadLibrary("plasma")
    }

    external fun add(data: FloatArray, len : Int, itemCount : Int)
    external fun getByteBuffer(len : Int):ByteBuffer
    /** Возвращает null, пока в истории накоплено меньше [len] отсчётов. */
    external fun getByteBufferSmallLissagu(len : Int = 4096):ByteBuffer?

}