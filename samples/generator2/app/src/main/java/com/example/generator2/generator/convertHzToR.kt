package com.example.generator2.generator

fun convertHzToR(hz: Float): Float {
    return (hz * 16384.0f / 3.798f * 2.0f * 1000.0 / 48.8 / 2.0 * 1000.0 / 988.0).toFloat()
}

fun map(x: Float, in_min: Float, in_max: Float, out_min: Float, out_max: Float): Float {
    return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min
}