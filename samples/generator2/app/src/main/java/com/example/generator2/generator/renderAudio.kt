package com.example.generator2.generator

import com.example.generator2.model.LiveData

fun renderAudio(numFrames : Int = 1024): FloatArray {

    val enL = LiveData.enL.value
    val enR = LiveData.enR.value

    //val numFrames = 1024

    var buf: FloatArray = FloatArray(numFrames)

    if (!LiveData.mono.value) {

        //stereo
        val l = renderChanel(ch1, numFrames/2)
        val r = renderChanel(ch2, numFrames/2)

        //Нормальный режим
        if (!LiveData.shuffle.value)

            buf = mergeArrays(l, r)

//            for (i in 0 until numFrames) {
//
//                if (enL)
//                    buf[i * 2] = l[i]
//                else
//                    buf[i * 2] = 0F
//                if (enR)
//                    buf[i * 2 + 1] = r[i]
//                else buf[i * 2 + 1] = 0F
//
//            }



        else
            for (i in 0 until numFrames) {
                if (enL)
                    buf[i * 2] = r[i]
                else
                    buf[i * 2] = 0F
                if (enR)
                    buf[i * 2 + 1] = l[i]
                else buf[i * 2 + 1] = 0F
            }
    } else {
        //Mono
        val m = renderChanel(ch1, 1024)

        if (!LiveData.invert.value) {

            for (i in 0 until numFrames) {

                if (enL)
                    buf[i * 2] = m[i]
                else
                    buf[i * 2] = 0F
                if (enR)
                    buf[i * 2 + 1] = m[i]
                else buf[i * 2 + 1] = 0F

            }

        } else {
            //Invert
            for (i in 0 until numFrames) {

                if (enL)
                    buf[i * 2] = m[i]
                else
                    buf[i * 2] = 0F
                if (enR)
                    buf[i * 2 + 1] = m[i] * (-1.0f)
                else buf[i * 2 + 1] = 0F

            }
        }
    }

    return buf

}

fun mergeArrays(array1: FloatArray, array2: FloatArray): FloatArray
{
    val combinedArray = FloatArray(array1.size + array2.size) { 0F }

    var index1 = 0
    var index2 = 0

    for (i in combinedArray.indices) {
        if (i % 2 == 0) {
            combinedArray[i] = array1[index1]
            index1++
        } else {
            combinedArray[i] = array2[index2]
            index2++
        }
    }

    return combinedArray

}