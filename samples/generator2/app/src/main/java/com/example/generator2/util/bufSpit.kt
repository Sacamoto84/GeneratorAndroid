package com.example.generator2.util

class BufSplitShort
{
    private var bufR = ShortArray(0)
    private var bufL = ShortArray(0)

    /**
     * LR
     */
    fun split(buf: ShortArray): Pair<ShortArray, ShortArray> {

        if (bufR.size != buf.size / 2) {
            bufR = ShortArray(buf.size / 2)
            bufL = ShortArray(buf.size / 2)
        }

        var index1 = 0
        var index2 = 0

        for (i in buf.indices) {
            if (i % 2 == 0) {
                bufL[index1] = buf[i]
                index1++
            } else {
                bufR[index2] = buf[i]
                index2++
            }
        }

        return Pair(bufL, bufR)
    }

}



class BufSplitFloat
{
    private var bufRf = FloatArray(0)
    private var bufLf = FloatArray(0)

    fun split(buf: FloatArray): Pair<FloatArray, FloatArray> {

        if (bufRf.size != buf.size / 2) {
            bufRf = FloatArray(buf.size / 2)
            bufLf = FloatArray(buf.size / 2)
        }

        var index1 = 0
        var index2 = 0

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



