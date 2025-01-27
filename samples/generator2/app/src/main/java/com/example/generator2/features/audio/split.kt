package com.example.generator2.features.audio

/**
 Пример с четным количеством элементов

 val input = floatArrayOf(1f, 2f, 3f, 4f)

 val (left, right) = split(input)

 Left: [1.0, 3.0]

 Right: [2.0, 4.0]
*/
fun split(buf: FloatArray): Pair<FloatArray, FloatArray> {

    // Размер каналов
    val channelSize = buf.size / 2

    val rightChannel = FloatArray(channelSize)
    val leftChannel = FloatArray(channelSize)

    for (i in buf.indices) {
        if (i % 2 == 0) {
            leftChannel[i / 2] = buf[i]
        } else {
            rightChannel[i / 2] = buf[i]
        }
    }

    return Pair(leftChannel, rightChannel)
}




