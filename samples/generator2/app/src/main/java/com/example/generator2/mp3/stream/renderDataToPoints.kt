package com.example.generator2.mp3.stream

import android.R.attr.path
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.ui.geometry.Offset
import com.example.generator2.mp3.OSCILLSYNC
import com.example.generator2.mp3.channelDataOutBitmap
import com.example.generator2.mp3.channelDataOutRoll
import com.example.generator2.mp3.channelDataStreamOutCompressor
import com.example.generator2.mp3.oscillSync
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import libs.maping


var oscilloscopeW: Float = 1f
var oscilloscopeH: Float = 1f


private fun bufSpit(buf: ShortArray): Pair<ShortArray, ShortArray> {
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

var bitmap_example: Bitmap? = null

@OptIn(DelicateCoroutinesApi::class)
fun renderDataToPoints() {
    GlobalScope.launch(Dispatchers.Default) {
        while (true) {
            //val out = mutableListOf<Short>()

            val outPointR = mutableListOf<Offset>()
            val outPointL = mutableListOf<Offset>()

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

            //canvas.drawARGB(255,16,16,128)

            val paintR = Paint()
            paintR.color = 0xFFFF0000.toInt()
            paintR.isAntiAlias = false
            paintR.style = Paint.Style.FILL
            paintR.strokeWidth = 2f
            val paintL = Paint()
            paintL.color = 0xFF00FFFF.toInt()
            paintL.isAntiAlias = false
            paintL.style = Paint.Style.FILL
            paintL.strokeWidth = 2f





            var indexStartSignal: Int = 0

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


            val pathR = FloatArray(w.toInt()*4+16)
            //val pathL = FloatArray(w.toInt())

            val pathR1 = Array<Pt>(w.toInt()+16){Pt(0f,0f)}
            val pathR2 =  Path()

//            val pathL = Path()
//            pathR.moveTo(0f, h/2)
//            pathL.moveTo(0f, h/2)

            var i = 0

            if (compressorCount.floatValue < 64) {
                for (x in 0 until w.toInt()) {
                    val mapX: Int = maping(x.toFloat(), 0f, w - 1f, 0f, (bufRN.size - 1f)).toInt()
                        .coerceIn(0, bufRN.size - 1)

                    val yR = maping(bufRN[mapX].toFloat(), Short.MIN_VALUE.toFloat(), Short.MAX_VALUE.toFloat(), 0f, h - 1f)
                    val yL = maping(bufLN[mapX].toFloat(), Short.MIN_VALUE.toFloat(), Short.MAX_VALUE.toFloat(), 0f, h - 1f)

//                    pathR[i++] = x.toFloat()
//                    pathR[i++] = yR

                    pathR1[x] = Pt(x.toFloat(), yR)

                    //outPointR.add(Offset(x.toFloat(), yR))
                    //outPointL.add(Offset(x.toFloat(), yL))

                    //pathR[x] = yR
                    //pathL[x] = yL
                    //pathR.lineTo(x.toFloat(), yR)


                    //canvas.drawPoint(x.toFloat(), yR, paintR)
                    //canvas.drawPoint(x.toFloat(), yL, paintL)

                }
                //pathR.lineTo(w, h/2)
                pathR2.moveTo(pathR1[0].x, pathR1[0].y)


                // рисуем отрезки по заданным точкам
                for (i1 in 1 until pathR1.size) {
                    pathR2.lineTo(pathR1[i].x, pathR1[i].y)
                }

                // выводим результат
                //canvas.drawPath(pathR2, paintR)

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

                for (i in 0 until (listIndex0.size - 1)) {


                    val max = bufRN.slice(listIndex0[i] until listIndex0[i + 1]).max().toFloat()
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

                    outPointR.add(Offset(i.toFloat(), y))
                    outPointL.add(Offset(i.toFloat(), y1))

                }


            }


            //val out: Pair<List<Offset>, List<Offset>> = Pair(outPointR.toList(), outPointL.toList())

            //channelDataOutPoints.send(out)
            //bitmap_example!!.prepareToDraw()

            //canvas.drawLines(pathR, paintR)

            channelDataOutBitmap.send(bitmap_example)

            //bitmap_example!!.recycle()
        }
    }
}

// Класс для создания точки
internal class Pt(var x: Float, var y: Float)