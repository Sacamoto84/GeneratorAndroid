package com.example.generator2.mp3.stream

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.ui.geometry.Offset
import com.example.generator2.mp3.OSCILLSYNC
import com.example.generator2.mp3.Pt
import com.example.generator2.mp3.channelDataOutBitmap
import com.example.generator2.mp3.channelDataOutRoll
import com.example.generator2.mp3.channelDataStreamOutCompressor
import com.example.generator2.mp3.oscillSync
import com.example.generator2.mp3.oscilloscopeH
import com.example.generator2.mp3.oscilloscopeW
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import libs.maping




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

val paintR = Paint().apply {
    color = 0xFFFF0000.toInt()
    isAntiAlias = false
    style = Paint.Style.STROKE
    strokeWidth = 2f
}

val paintL = Paint().apply {
    color = 0xFF00FFFF.toInt()
    isAntiAlias = false
    style = Paint.Style.STROKE
    strokeWidth = 2f
}



@OptIn(DelicateCoroutinesApi::class)
fun renderDataToPoints() {
    GlobalScope.launch(Dispatchers.Default) {
        while (true) {
            //val out = mutableListOf<Short>()

            //val outPointR = mutableListOf<Offset>()
            //val outPointL = mutableListOf<Offset>()

            val w = oscilloscopeW
            val h = oscilloscopeH

            val bufRN: ShortArray
            val bufLN: ShortArray

            if ((w == 1f) or (h == 1f))
                continue

            //if (bitmap_example == null)

            //bitmap_example?.recycle() // Освободите память, затем пересоздайте Bitmap
            //if (bitmap_example == null)
            val  bitmap_example = Bitmap.createBitmap(w.toInt(), h.toInt(), Bitmap.Config.ARGB_8888)
            //bitmap_example!!.eraseColor(Color.GRAY)

            val canvas = Canvas(bitmap_example)
            canvas.drawARGB(255,16,16,128)

            var indexStartSignal = 0

            if (compressorCount.floatValue <= 8f) {

                val buf = channelDataStreamOutCompressor.receive()

                if (buf.isEmpty()) continue
                val (bufR, bufL) = bufSpit(buf)

                if (oscillSync.value == OSCILLSYNC.R) {
                    var last: Short = 0
                    for (i in 0 until bufR.size / 2) {
                        val now = bufR[i]
                        if ((last < 0) and (now >= 0)) {
                            indexStartSignal = i
                            break
                        }
                        last = now
                    }
                }

                if (oscillSync.value == OSCILLSYNC.L) {
                    var last: Short = 0
                    for (i in 0 until bufL.size / 2) {
                        val now = bufL[i]
                        if ((last < 0) and (now >= 0)) {
                            indexStartSignal = i
                            break
                        }
                        last = now
                    }
                }

                bufLN = bufL.copyOfRange(indexStartSignal, (bufL.size - 1) / 2 + indexStartSignal)
                bufRN = bufR.copyOfRange(indexStartSignal, (bufR.size - 1) / 2 + indexStartSignal)

            } else {

                // Для compressorCount > 8 нет синхронизации
                if (compressorCount.floatValue < 64) {
                    val buf = channelDataStreamOutCompressor.receive()
                    if (buf.isEmpty()) continue
                    val (bufR, bufL) = bufSpit(buf)
                    bufLN = bufL
                    bufRN = bufR
                } else {
                    //Режим Roll >= 64
                    val buf = channelDataOutRoll.receive()
                    if (buf.isEmpty()) continue
                    val (bufR, bufL) = bufSpit(buf)
                    bufLN = bufL
                    bufRN = bufR
                }

            }

////////////////////////////////////////////////////////////////


            //val pathR = FloatArray(w.toInt()*4)
            //val pathL = FloatArray(w.toInt())

            val ArrayPtR = Array(w.toInt()){Pt(0f,0f)}
            val pathR =  Path()

            val ArrayPtL = Array(w.toInt()){Pt(0f,0f)}
            val pathL =  Path()


            var i = 0

            if (compressorCount.floatValue < 64) {

                for (x in 0 until w.toInt()) {
                    val mapX: Int = maping(x.toFloat(), 0f, w - 1f, 0f, (bufRN.size - 1f)).toInt()
                        .coerceIn(0, bufRN.size - 1)

                    val yR = maping(bufRN[mapX].toFloat(), Short.MIN_VALUE.toFloat(), Short.MAX_VALUE.toFloat(), 0f, h - 1f)
                    val yL = maping(bufLN[mapX].toFloat(), Short.MIN_VALUE.toFloat(), Short.MAX_VALUE.toFloat(), 0f, h - 1f)

                    ArrayPtR[x] = Pt(x.toFloat(), yR)
                    ArrayPtL[x] = Pt(x.toFloat(), yL)

                }

                pathR.moveTo(ArrayPtR[0].x, ArrayPtR[0].y)
                pathL.moveTo(ArrayPtL[0].x, ArrayPtL[0].y)
                for (i1 in 1 until ArrayPtR.size) {
                    pathR.lineTo(ArrayPtR[i1].x, ArrayPtR[i1].y)
                    pathL.lineTo(ArrayPtL[i1].x, ArrayPtL[i1].y)
                }

                // выводим результат
                canvas.drawPath(pathR, paintR)
                canvas.drawPath(pathL, paintL)

            } else {
                //Roll логика
                //Дано  bufRN bufLN
                val listIndex0 =
                    mutableListOf<Int>() //Первый прогон, получение списка начальных индексов из основного буффера
                for (x in 0 until w.toInt()) {
                    listIndex0.add(
                        maping(x.toFloat(), 0f, w - 1f, 0f, (bufRN.size - 1f)).toInt()
                            .coerceIn(0, bufRN.size - 1)
                    )
                }

                for (i3 in 0 until (listIndex0.size - 1)) {


                    val max = bufRN.slice(listIndex0[i3] until listIndex0[i3 + 1]).max().toFloat()
                    val y = maping(
                        max,
                        Short.MIN_VALUE.toFloat(),
                        Short.MAX_VALUE.toFloat(),
                        0f,
                        h - 1f
                    )
                    val y1 = maping(
                        -max,
                        Short.MIN_VALUE.toFloat(),
                        Short.MAX_VALUE.toFloat(),
                        0f,
                        h - 1f
                    )

                    //outPointR.add(Offset(i.toFloat(), y))
                    //outPointL.add(Offset(i.toFloat(), y1))


                }


            }

            channelDataOutBitmap.send(bitmap_example)

        }
    }
}

