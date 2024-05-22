package com.example.generator2.util


class BufMenge(){

    private var combinedArrayFloat = FloatArray(0)

    fun merge(
        array1: FloatArray,
        array2: FloatArray,
    ): FloatArray {

        combinedArrayFloat.fill(0f)

        val len = array1.size + array2.size

        if (combinedArrayFloat.size != len)
            combinedArrayFloat = FloatArray(len)

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

}



var combinedArrayFloat0 = FloatArray(0)

fun bufMerge0(
    array1: FloatArray,
    array2: FloatArray,
): FloatArray {

    val len = array1.size + array2.size

    if (combinedArrayFloat0.size != len)
      combinedArrayFloat0 = FloatArray(len)

    var index1 = 0
    var index2 = 0
    for (i in combinedArrayFloat0.indices) {
        if (i % 2 == 0) {
            combinedArrayFloat0[i] = array1[index1]
            index1++
        } else {
            combinedArrayFloat0[i] = array2[index2]
            index2++
        }
    }
    return combinedArrayFloat0
}

var combinedArrayFloat1 = FloatArray(0)
fun bufMerge1(
    array1: FloatArray,
    array2: FloatArray,
): FloatArray {

    val len = array1.size + array2.size

    if (combinedArrayFloat1.size != len)
        combinedArrayFloat1 = FloatArray(len)

    var index1 = 0
    var index2 = 0
    for (i in combinedArrayFloat1.indices) {
        if (i % 2 == 0) {
            combinedArrayFloat1[i] = array1[index1]
            index1++
        } else {
            combinedArrayFloat1[i] = array2[index2]
            index2++
        }
    }
    return combinedArrayFloat1
}

var combinedArrayFloat2 = FloatArray(0)
fun bufMerge2(
    array1: FloatArray,
    array2: FloatArray,
): FloatArray {

    val len = array1.size + array2.size

    if (combinedArrayFloat2.size != len)
        combinedArrayFloat2 = FloatArray(len)

    var index1 = 0
    var index2 = 0
    for (i in combinedArrayFloat2.indices) {
        if (i % 2 == 0) {
            combinedArrayFloat2[i] = array1[index1]
            index1++
        } else {
            combinedArrayFloat2[i] = array2[index2]
            index2++
        }
    }
    return combinedArrayFloat2
}

var combinedArrayFloat3 = FloatArray(0)
fun bufMerge3(
    array1: FloatArray,
    array2: FloatArray,
): FloatArray {

    val len = array1.size + array2.size

    if (combinedArrayFloat3.size != len)
        combinedArrayFloat3 = FloatArray(len)

    var index1 = 0
    var index2 = 0
    for (i in combinedArrayFloat3.indices) {
        if (i % 2 == 0) {
            combinedArrayFloat3[i] = array1[index1]
            index1++
        } else {
            combinedArrayFloat3[i] = array2[index2]
            index2++
        }
    }
    return combinedArrayFloat3
}


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