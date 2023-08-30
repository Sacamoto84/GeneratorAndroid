package com.example.generator2.scope

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.example.generator2.audio.Calculator
import com.example.generator2.mp3.OSCILLSYNC
import com.example.generator2.mp3.channelDataOutRoll
import com.example.generator2.mp3.channelDataStreamOutCompressor
import com.example.generator2.mp3.oscillSync
import com.example.generator2.util.BufSplitFloat
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import libs.maping
import timber.log.Timber
import kotlin.math.absoluteValue
import kotlin.system.measureNanoTime

var hiRes: Boolean = false //Режим высокого разрешения

@OptIn(DelicateCoroutinesApi::class)
fun renderDataToPoints() {

    val paintRStroke = Paint().apply {
        color = Color.GREEN
        isAntiAlias = false
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    val paintLStroke = Paint().apply {
        color = Color.RED
        isAntiAlias = false
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }


//    var bitmap2: Bitmap =
//        Bitmap.createBitmap(scope.scopeW.toInt(), scope.scopeH.toInt(), Bitmap.Config.RGB_565)
//    var bitmapBuffer = false

    var canvas: Canvas

    val paintL = Paint()
    paintL.color = Color.YELLOW
    paintL.alpha = 0xFF
    paintL.strokeWidth = 2f

    val paintR = Paint()
    paintR.color = Color.MAGENTA
    paintR.alpha = 0xFF
    paintL.strokeWidth = 2f

    Thread {

        GlobalScope.launch(Dispatchers.IO) {

            val calculator = Calculator()

            while (true) {

                hiRes = if (compressorCount.floatValue >= 32)
                    false //Режим высокого разрешения
                else
                    true
                //hiRes = true
                var w: Float
                var h: Float
                if (hiRes) {
                    w = scope.scopeW
                    h = scope.scopeH
                } else {
                    w = scope.scopeW / 2
                    h = scope.scopeH / 2
                }

                val bufRN: FloatArray
                val bufLN: FloatArray

                if ((w == 0f) or (h == 0f)) continue


                val bitmap: Bitmap = Bitmap.createBitmap(
                    w.toInt(),
                    h.toInt(),
                    Bitmap.Config.RGB_565
                )

                canvas = Canvas()


                canvas.setBitmap(bitmap)

                //canvas.drawARGB(128, 44, 22, 128)

                //delay(16) /////////////////////////////////????????????????????????????

                var indexStartSignal = 0

                if (compressorCount.floatValue <= 8f) {

                    val buf = channelDataStreamOutCompressor.receive()

                    if (buf.isEmpty()) continue

                    val (bufR, bufL) = BufSplitFloat().split(buf)

                    if (oscillSync.value == OSCILLSYNC.R) {
                        var last = 0f
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
                        var last = 0f
                        for (i in 0 until bufL.size / 2) {
                            val now = bufL[i]
                            if ((last < 0) and (now >= 0)) {
                                indexStartSignal = i
                                break
                            }
                            last = now
                        }
                    }

                    bufLN =
                        bufL.copyOfRange(indexStartSignal, (bufL.size - 1) / 2 + indexStartSignal)
                    bufRN =
                        bufR.copyOfRange(indexStartSignal, (bufR.size - 1) / 2 + indexStartSignal)

                } else {

                    // Для compressorCount > 8 нет синхронизации
                    val buf = channelDataStreamOutCompressor.receive()
                    if (buf.isEmpty()) continue
                    val (bufR, bufL) = BufSplitFloat().split(buf)
                    bufLN = bufL
                    bufRN = bufR

                }

////////////////////////////////////////////////////////////////

                val pathL = Path()


                var pixelBufSize: Float
                val pixelBufL = FloatArray(4096)
                val pixelBufR = FloatArray(4096)

                paintL.style = Paint.Style.STROKE

                if (hiRes) {
                    paintL.strokeWidth = 2f
                    paintR.strokeWidth = 2f
                } else {
                    paintL.strokeWidth = 1f
                    paintR.strokeWidth = 1f
                }

                val drawLine: Boolean
                if (compressorCount.floatValue >= 8f) {
                    paintL.alpha = 0x60
                    paintR.alpha = 0x60
                    drawLine = false
                } else {
                    paintL.alpha = 0xFF
                    paintR.alpha = 0xFF
                    drawLine = true
                }


                var mapX: Int
                var offset: Int

                val nanos = measureNanoTime {

                    for (x in 0 until w.toInt()) {

                        //32  45.8   48k
                        //16  22.81
                        //8   5.7
                        //4   2.86
                        //2   1.42
                        //1   0.71
                        //0.5 0.35
                        pixelBufSize =
                            (bufRN.size / w).coerceIn(1f, 96f) //Размер буфера для одного пикселя

                        //Timber.w("pixelBufSize: ${bufRN.size / w}")


                        val maxPixelBuffer = pixelBufSize.toInt()
                        for (pixelI in 0 until maxPixelBuffer) {

                            mapX = maping(x.toFloat(), 0f, w - 1f, 0f, (bufRN.size - 1f)).toInt()
                                .coerceIn(0, bufRN.size - 1)

                            offset = (mapX + pixelI).coerceAtMost(bufRN.size - 1)
                            pixelBufL[pixelI] = bufLN[offset]
                            pixelBufR[pixelI] = bufRN[offset]

                            //var v = Offset(x.toFloat(), maping(pixelBufL[pixelI], -1f, 1f, 0f, h - 1f) )
                            //pointsListL.add(v)
                            //v = Offset(x.toFloat(),  maping(pixelBufR[pixelI], -1f, 1f, 0f, h - 1f))


                            //pointsListL.


                            //pathL.lineTo(x.toFloat(), maping(pixelBufL[pixelI], -1f, 1f, 0f, h - 1f))

                        }


                        if (!drawLine) {
                            for (pixelI in 0 until maxPixelBuffer) {
                                canvas.drawPoint(
                                    x.toFloat(),
                                    maping(pixelBufL[pixelI], -1f, 1f, 0f, h - 1f),
                                    paintL
                                )
                                canvas.drawPoint(
                                    x.toFloat(),
                                    maping(pixelBufR[pixelI], -1f, 1f, 0f, h - 1f),
                                    paintR
                                )
                            }
                        } else {
                            //Рисуем линии
                            if (x == 0) pathL.moveTo(0f, h / 2)

                            //val average = calculateAverage(pixelBufL, maxPixelBuffer)
                            val average = pixelBufL[0]
                            pathL.lineTo(x.toFloat(), maping(average, -1f, 1f, 0f, h - 1f))


                        }


                    }

                    if (drawLine)
                        canvas.drawPath(pathL, paintL)

                }
                calculator.update(nanos / 1000000.0)
                //println("Calculate Pointer: " + nanos/1000 + "us")

                //println("Calculate Pointer :${nanos / 1000000.0} ms ${calculator.getAvg()}")


                scope.chPixel.send(bitmap)


            }
        }


    }.start()
}

fun calculateAverage(array: FloatArray, endIndex: Int): Float {
    require(endIndex >= 0 && endIndex < array.size) { "Invalid endIndex" }

    var max: Float = 0f
    var min: Float = 0f

    var sum = 0.0f
    for (i in 0..endIndex) {
        sum += array[i]
        if (array[i] > max) max = array[i]
        if (array[i] < min) min = array[i]
    }

    val avg = sum / (endIndex + 1)

    val out = if (max > min.absoluteValue) max else min

    return out
}