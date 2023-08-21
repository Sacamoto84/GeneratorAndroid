package com.example.generator2.util



fun bufMerge(
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





fun bufMerge(
    arrayR: ShortArray,
    arrayL: ShortArray,
    enR: Boolean =  true,
    enL: Boolean = true,
    invert: Boolean = false
): ShortArray {
    val combinedArray = ShortArray(arrayL.size + arrayR.size) { 0 }
    var index1 = 0
    var index2 = 0
    for (i in combinedArray.indices) {
        if (i % 2 == 0) {
            if (enR)
                combinedArray[i] = arrayR[index1]
            else
                combinedArray[i] = 0
            index1++
        } else {
            if (enL)
                combinedArray[i] = (arrayL[index2] * if (invert) (-1) else 1).toShort()
            else
                combinedArray[i] = 0
            index2++
        }
    }
    return combinedArray
}