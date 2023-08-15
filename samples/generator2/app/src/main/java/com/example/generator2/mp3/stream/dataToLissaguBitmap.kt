package com.example.generator2.mp3.stream

import android.graphics.Bitmap
import android.graphics.Canvas
import com.example.generator2.mp3.Pt
import libs.maping

fun dataToLissaguBitmap(buf: ShortArray, w: Int, h: Int): Bitmap {

    val arrayPtLissagu = Array(w) { Pt(0f, 0f) }

    val (bufR, bufL) = bufSpit(buf)

    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

    val canvas = Canvas(bitmap)
    canvas.drawARGB(255, 16, 16, 16)

    for (x in 0 until w) {
        val mapX: Int = maping(x.toFloat(), 0f, w - 1f, 0f, (bufR.size - 1f)).toInt().coerceIn(0, bufR.size - 1)

        val yR = maping(
            bufR[mapX].toFloat(),
            Short.MIN_VALUE.toFloat(),
            Short.MAX_VALUE.toFloat(),
            0f,
            h - 1f
        )
        val yL = maping(
            bufL[mapX].toFloat(),
            Short.MIN_VALUE.toFloat(),
            Short.MAX_VALUE.toFloat(),
            0f,
            h - 1f
        )
        arrayPtLissagu[x] = Pt(yR, yL)
        canvas.drawPoint(yR, yL, paintR)
    }

    return bitmap

}