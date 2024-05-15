package com.example.generator2.features.audio



private var bufRFloatLissagu = FloatArray(0)
private var bufLFloatLissagu = FloatArray(0)

fun split(buf: FloatArray): Pair<FloatArray, FloatArray> {


    val bufRf = FloatArray(buf.size / 2)
    val bufLf = FloatArray(buf.size / 2)

    var index1 = 0
    var index2 = 0

    if (buf.isNotEmpty())
        for (i in buf.indices) {
            if (i % 2 == 0) {
                bufLf[index1] = buf[i]
                index1++
            } else {
                bufRf[index2] = buf[i]
                index2++
            }
        }

    return Pair(bufLf, bufRf)
}








class BufSplitFloat {

    private var bufRf = FloatArray(0)
    private var bufLf = FloatArray(0)

    fun split(buf: FloatArray): Pair<FloatArray, FloatArray> {

        val bufRf = FloatArray(buf.size / 2)
        val bufLf = FloatArray(buf.size / 2)

        var index1 = 0
        var index2 = 0

        if (buf.isNotEmpty())
            for (i in buf.indices) {
                if (i % 2 == 0) {
                    bufLf[index1] = buf[i]
                    index1++
                } else {
                    bufRf[index2] = buf[i]
                    index2++
                }
            }

        return Pair(bufLf, bufRf)
    }


}



