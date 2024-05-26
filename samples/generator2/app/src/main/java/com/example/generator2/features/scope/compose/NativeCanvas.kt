package com.example.generator2.features.scope.compose

class NativeCanvas {

    companion object {
        init {
            System.loadLibrary("generator2")
        }
    }



    external fun copyFloatArrayJNI(source: FloatArray, destination: FloatArray)

}