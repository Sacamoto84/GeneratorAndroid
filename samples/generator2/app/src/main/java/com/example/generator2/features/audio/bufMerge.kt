package com.example.generator2.features.audio

fun bufMerge(
    array1: FloatArray,
    array2: FloatArray,
): FloatArray {

    require(array1.size == array2.size) { "Arrays must have the same size to merge them alternately." }

    val combinedArrayFloat = FloatArray(array1.size + array2.size)

    for (i in array1.indices) {
        combinedArrayFloat[2 * i] = array1[i]
        combinedArrayFloat[2 * i + 1] = array2[i]
    }

    return combinedArrayFloat
}
