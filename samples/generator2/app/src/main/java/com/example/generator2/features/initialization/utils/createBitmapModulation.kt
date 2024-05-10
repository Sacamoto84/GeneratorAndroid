package com.example.generator2.features.initialization.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import timber.log.Timber

fun createBitmapModulation(array8: ByteArray): Bitmap {

    val bitmap = Bitmap.createBitmap(1024, 512, Bitmap.Config.RGB_565)

   // val array8 = readFileMod2048byte(path) //Получим массив 8 бит

   // val array8 = readBytesFromAssets(context, "Mod", path, 2048) //Получим массив 8 бит


//        if (array8?.size != 2048) {
//            Timber.tag("!ERROR!")
//                .i("CreateBitmapModulation:readFileMod2048byte len:" + array8?.size.toString())
//            bitmap.eraseColor(Color.RED) // Закрашиваем синим цветом
//            return bitmap
//        }



    //bitmap.eraseColor(Color.BLACK) // Закрашиваем синим цветом

    val arrayU8 = IntArray(2048)
    val arrayInt = IntArray(1024)

    for (i in 0 until 2048) {
        arrayU8[i] = java.lang.Byte.toUnsignedInt(array8[i])
    }

    for (i in 0 until 1024) {
        arrayInt[i] = ((arrayU8[i * 2 + 1]) * 256) + (arrayU8[i * 2])
    }

    val mPaint = Paint()

    val c = Canvas()
    c.setBitmap(bitmap)

    mPaint.style = Paint.Style.STROKE
    mPaint.strokeWidth = 4f
    mPaint.color = Color.DKGRAY

    c.drawLine(512f, 0f, 512f, 512f, mPaint)

    mPaint.color = Color.GREEN
    mPaint.strokeWidth = 10f


    val lines = FloatArray(1024 * 4)

    for (i in 0 until 512) {
        val startIndex = i * 4

        lines[startIndex] = i.toFloat()                                      // x1
        lines[startIndex + 1] = 32 + (4095 - arrayInt[i * 2]).toFloat() / 18 // y1
        lines[startIndex + 2] = i.toFloat()                                  // x2
        lines[startIndex + 3] = arrayInt[i * 2].toFloat() / 18 + 256         // y2

        val startIndex2 = (i + 512) * 4
        lines[startIndex2] = (i + 512).toFloat()
        lines[startIndex2 + 1] = 32 + (4095 - arrayInt[i * 2]).toFloat() / 18
        lines[startIndex2 + 2] = (i + 512).toFloat()
        lines[startIndex2 + 3] = arrayInt[i * 2].toFloat() / 18 + 256
    }

    c.drawLines(lines, mPaint)

    mPaint.strokeWidth = 2f
    //mPaint.setColor(Color.BLUE);
    //mPaint.setPathEffect(new DashPathEffect(new float[] { 20F, 30F, 40F, 50F}, 0));
    val pathLine = Path()
    pathLine.moveTo(0f, 256f)
    pathLine.lineTo(0f, 256f)
    pathLine.lineTo(1023f, 255f)
    c.drawPath(pathLine, mPaint)

    c.drawLine(0f, 256f, 1023f, 256f, mPaint)

    // Выводим уменьшенную в два раза картинку
    val bmHalf = Bitmap.createScaledBitmap(
        bitmap, 512,
        256, false
    )

    bitmap.recycle()

    return bmHalf
}