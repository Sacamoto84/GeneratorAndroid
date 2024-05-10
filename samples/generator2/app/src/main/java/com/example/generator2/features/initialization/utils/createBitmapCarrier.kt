package com.example.generator2.features.initialization.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.Log
import timber.log.Timber


fun createBitmapCarrier(array8: ByteArray): Bitmap {

    val bitmap = Bitmap.createBitmap(1024, 512, Bitmap.Config.RGB_565)

//    if (array8 != null) {
//        Timber.i("createBitmapCarrier:A8 len: " +array8.size.toString() )
//    }
//    else
//        return bitmap
//
//    if (array8.size != 2048) {
//        bitmap.eraseColor(Color.RED) // Закрашиваем синим цветом
//        return bitmap
//    }

    //bitmap.eraseColor(Color.TRANSPARENT) // Закрашиваем синим цветом

    var i = 0

    val arrayU8 = IntArray(2048)
    i = 0
    while (i < 2048) {
        arrayU8[i] = java.lang.Byte.toUnsignedInt(array8[i])
        i++
    }

    val arrayInt = IntArray(1024)

    i = 0
    while (i < 1024) {
        arrayInt[i] = ((arrayU8[i * 2 + 1]) * 256) + (arrayU8[i * 2])
        i++
    }

    val c = Canvas()
    c.setBitmap(bitmap)

    val mPaint = Paint()

    mPaint.strokeWidth = 4f
    mPaint.color = Color.DKGRAY
    c.drawLine(512f, 0f, 512f, 512f, mPaint)


    mPaint.color = Color.TRANSPARENT
    mPaint.strokeWidth = 10f
   //c.drawLine(0f, 256f, 1023f, 256f, mPaint)

    val mPath = Path()

    // очистка path
    mPath.reset()

    mPath.moveTo(0f, (32 + (4096 - arrayInt[0]) / 9).toFloat())
    i = 1
    while (i < 512) {
        mPath.lineTo(i.toFloat(), (32 + (4096 - arrayInt[i * 2]) / 9).toFloat())
        i++
    }

    //mPath.moveTo(512,  32 + (4096 - arrayInt[0])/9);
    i = 0
    while (i < 512) {
        mPath.lineTo((i + 512).toFloat(), (32 + (4096 - arrayInt[i * 2]) / 9).toFloat())
        i++
    }


    mPaint.color = Color.GREEN
    mPaint.strokeWidth = 10f
    mPaint.style = Paint.Style.STROKE
    c.drawPath(mPath, mPaint)


    // Выводим уменьшенную в два раза картинку
    val bmHalf = Bitmap.createScaledBitmap(
        bitmap, 512,
        256, false
    )

    bitmap.recycle()

    return bmHalf
}