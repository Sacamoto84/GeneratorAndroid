package com.example.generator2.generator

import com.example.generator2.model.LiveData

fun renderAudio(numFrames : Int = 1024): FloatArray {

    val enL = LiveData.enL.value
    val enR = LiveData.enR.value

    //val numFrames = 1024

    val buf: FloatArray = FloatArray(numFrames)

    if (!LiveData.mono.value) {

        //stereo
        val l = renderChanel(ch1, numFrames/2)
        val r = renderChanel(ch2, numFrames/2)

        //Нормальный режим
        if (!LiveData.shuffle.value)
            for (i in 0 until numFrames) {

                if (enL)
                    buf[i * 2] = l[i]
                else
                    buf[i * 2] = 0F
                if (enR)
                    buf[i * 2 + 1] = r[i]
                else buf[i * 2 + 1] = 0F

            }
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