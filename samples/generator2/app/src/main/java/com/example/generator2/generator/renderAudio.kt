package com.example.generator2.generator

import com.example.generator2.model.LiveData

fun renderAudio(numFrames: Int = 1024): FloatArray {

    val enL = LiveData.enL.value
    val enR = LiveData.enR.value

    val buf: FloatArray

    if (!LiveData.mono.value) {

        //stereo
        val l = renderChanel(ch1, numFrames / 2)
        val r = renderChanel(ch2, numFrames / 2)

        //Нормальный режим
        buf = if (!LiveData.shuffle.value)
            mergeArrays(l, r, enL, enR)
        else
            mergeArrays(r, l, enL, enR)

    } else {
        //Mono
        val m = renderChanel(ch1, numFrames / 2)

        buf = if (!LiveData.invert.value)
            mergeArrays(m, m, enL, enR)
        else
            mergeArrays(m, m, enL, enR, true)

    }

    return buf

}

private fun mergeArrays(
    array1: FloatArray,
    array2: FloatArray,
    enL: Boolean,
    enR: Boolean,
    invert: Boolean = false
): FloatArray {
    val combinedArray = FloatArray(array1.size + array2.size) { 0F }

    var index1 = 0
    var index2 = 0

    for (i in combinedArray.indices) {
        if (i % 2 == 0) {

            if (enL)
                combinedArray[i] = array1[index1]
            else
                combinedArray[i] = 0F

            index1++

        } else {

            if (enR)
                combinedArray[i] = array2[index2] * if (invert) (-1.0f) else 1.0f
            else
                combinedArray[i] = 0F

            index2++
        }
    }

    return combinedArray

}