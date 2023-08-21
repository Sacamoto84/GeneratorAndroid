package com.example.generator2.util

fun bufSpit(buf: ShortArray): Pair<ShortArray, ShortArray> {
    val bufR = ShortArray(buf.size / 2)
    val bufL = ShortArray(buf.size / 2)

    var index1 = 0
    var index2 = 0

    for (i in buf.indices) {
        if (i % 2 == 0) {
            bufR[index1] = buf[i]
            index1++
        } else {
            bufL[index2] = buf[i]
            index2++
        }
    }

    return Pair(bufR, bufL)
}