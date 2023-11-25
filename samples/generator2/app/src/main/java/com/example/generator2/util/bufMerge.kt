package com.example.generator2.util


fun bufMerge(
    array1: FloatArray,
    array2: FloatArray,
): FloatArray {

    val len = array1.size + array2.size

    val combinedArrayFloat = FloatArray(len)

    var index1 = 0
    var index2 = 0
    for (i in combinedArrayFloat.indices) {
        if (i % 2 == 0) {
            combinedArrayFloat[i] = array1[index1]
            index1++
        } else {
            combinedArrayFloat[i] = array2[index2]
            index2++
        }
    }
    return combinedArrayFloat
}


var combinedArrayShort = ShortArray(0)

fun bufMerge(
    arrayL: ShortArray,
    arrayR: ShortArray,
    enL: Boolean = true,
    enR: Boolean = true,
    invert: Boolean = false
): ShortArray {

    val len = arrayL.size + arrayR.size

    if (combinedArrayShort.size != len)
        combinedArrayShort = ShortArray(arrayL.size + arrayR.size)

    var index1 = 0
    var index2 = 0
    for (i in combinedArrayShort.indices) {
        if (i % 2 == 0) {
            if (enL)
                combinedArrayShort[i] = arrayL[index1]
            else
                combinedArrayShort[i] = 0
            index1++
        } else {
            if (enR)
                combinedArrayShort[i] = (arrayR[index2] * if (invert) (-1) else 1).toShort()
            else
                combinedArrayShort[i] = 0
            index2++
        }
    }
    return combinedArrayShort
}