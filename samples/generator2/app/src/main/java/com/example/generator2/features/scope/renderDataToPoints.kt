package com.example.generator2.features.scope

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.example.generator2.audio.Calculator
import com.example.generator2.features.mp3.OSCILLSYNC
import com.example.generator2.features.mp3.channelAudioOutLissagu
import com.example.generator2.features.mp3.channelDataStreamOutCompressor
import com.example.generator2.features.mp3.oscillSync
import com.example.generator2.util.BufSplitFloat
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import utils.maping
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

var hiRes: Boolean = false //Режим высокого разрешения

@OptIn(DelicateCoroutinesApi::class)
fun renderDataToPoints(scope: Scope) {

    var canvas: Canvas

    val paintL = Paint()
    paintL.color = Color.YELLOW
    paintL.alpha = 0xFF
    paintL.strokeWidth = 2f

    val paintR = Paint()
    paintR.color = Color.MAGENTA
    paintR.alpha = 0xFF
    paintL.strokeWidth = 2f

    val paintLissagu = Paint()
    paintLissagu.color = Color.WHITE
    paintLissagu.alpha = 0xFF
    paintLissagu.strokeWidth = 2f


    paintL.style = Paint.Style.STROKE
    paintR.style = Paint.Style.STROKE
    paintLissagu.style = Paint.Style.STROKE



    Thread {

        GlobalScope.launch(Dispatchers.IO) {

            val calculator = Calculator()

            while (true) {



                    if (scope.isPause.value) continue

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
                            bufL.copyOfRange(
                                indexStartSignal,
                                (bufL.size - 1) / 2 + indexStartSignal
                            )
                        bufRN =
                            bufR.copyOfRange(
                                indexStartSignal,
                                (bufR.size - 1) / 2 + indexStartSignal
                            )

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
                    val pathR = Path()

                    var pixelBufSize: Float
                    val pixelBufL = FloatArray(4096)
                    val pixelBufR = FloatArray(4096)



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
                                (bufRN.size / w).coerceIn(
                                    1f,
                                    96f
                                ) //Размер буфера для одного пикселя

                            val maxPixelBuffer = pixelBufSize.toInt()
                            for (pixelI in 0 until maxPixelBuffer) {

                                mapX =
                                    maping(x.toFloat(), 0f, w - 1f, 0f, (bufRN.size - 1f)).toInt()
                                        .coerceIn(0, bufRN.size - 1)

                                offset = (mapX + pixelI).coerceAtMost(bufRN.size - 1)
                                pixelBufL[pixelI] = bufLN[offset]
                                pixelBufR[pixelI] = bufRN[offset]
                            }


                            val maxL: Float
                            val minL: Float
                            val maxR: Float
                            val minR: Float

                            //Пиксели
                            if (scope.isOneTwo.value) {
                                maxL = h - 1f
                                minL = 0f
                                maxR = h - 1f
                                minR = 0f
                            } else {
                                maxR = h - 1f
                                minR = h / 2
                                maxL = h / 2
                                minL = 0f
                            }


                            if (!drawLine) {
                                for (pixelI in 0 until maxPixelBuffer) {

                                    if (scope.isVisibleR.value) {
                                        canvas.drawPoint(
                                            x.toFloat(),
                                            maping(pixelBufR[pixelI], -1f, 1f, minR, maxR),
                                            paintR
                                        )
                                    }

                                    if (scope.isVisibleL.value) {
                                        canvas.drawPoint(
                                            x.toFloat(),
                                            maping(pixelBufL[pixelI], -1f, 1f, minL, maxL),
                                            paintL
                                        )
                                    }
                                }
                            } else {
                                //Рисуем линии
                                if (x == 0) {
                                    pathL.moveTo(pixelBufL[0], maping(0f, -1f, 1f, minL, maxL))
                                    pathR.moveTo(pixelBufR[0], maping(0f, -1f, 1f, minR, maxR))
                                } else {

                                    if (scope.isVisibleL.value) {
                                        pathL.lineTo(
                                            x.toFloat(),
                                            maping(pixelBufL[0], -1f, 1f, minL, maxL)
                                        )
                                    }

                                    if (scope.isVisibleR.value) {
                                        pathR.lineTo(
                                            x.toFloat(),
                                            maping(pixelBufR[0], -1f, 1f, minR, maxR)
                                        )
                                    }
                                }
                            }
                        }

                        if (drawLine) {
                            canvas.drawPath(pathR, paintR)
                            canvas.drawPath(pathL, paintL)
                        }
                    }
                    calculator.update(nanos / 1000000.0)

                    //println("Calculate Pointer: " + nanos/1000 + "us")
                    //println("Calculate Pointer :${nanos / 1000000.0} ms ${calculator.getAvg()}")

                    scope.chPixel.send(ChPixelData(bitmap, hiRes))








            }
        }


    }.start()
}


@OptIn(DelicateCoroutinesApi::class)
fun lissaguToBitmap(scope: Scope) {
    val paintLissagu = Paint()
    paintLissagu.color = Color.GREEN
    paintLissagu.alpha = 0xFF
    paintLissagu.strokeWidth = 1f
    paintLissagu.style = Paint.Style.STROKE

    GlobalScope.launch(Dispatchers.IO) {
        while (true) {
            val w: Float = scope.scopeWLissagu
            val h: Float = scope.scopeHLissagu
            if ((w == 0f) or (h == 0f)) continue
            //val buf = channelDataStreamOutCompressorLissagu.receive()
            //channelAudioOutLissagu

            val buf = channelAudioOutLissagu.receive()

            if (buf.isEmpty()) continue
            val (bufR, bufL) = BufSplitFloat().split(buf)
            val bitmap: Bitmap = Bitmap.createBitmap(w.toInt(), h.toInt(), Bitmap.Config.RGB_565)
            val canvas = Canvas(); canvas.setBitmap(bitmap)
            //val bufLN = if (bufL.size >= 200) bufL.copyOf(200) else bufL
            //val bufRN = if (bufL.size >= 200) bufR.copyOf(200) else bufR

            val bufLN = bufL.copyOf(bufL.size / 4)
            val bufRN = bufR.copyOf(bufL.size / 4)

            val len = bufLN.size
            val max = h - 1f;
            val min = 0f

//            for (i in 0 until len) {
//                canvas.drawPoint(
//                    maping(bufLN[i], -1f, 1f, min, max),
//                    maping(bufRN[i], -1f, 1f, min, max),
//                    paintLissagu
//                )
//            }

            val path = Path()
            path.moveTo(maping(bufLN[0], -1f, 1f, min, max), maping(bufRN[0], -1f, 1f, min, max))
            for (i in 1 until len) {
                path.lineTo(
                    maping(bufLN[i], -1f, 1f, min, max),
                    maping(bufRN[i], -1f, 1f, min, max)
                )
            }
            canvas.drawPath(path, paintLissagu)

            scope.chPixelLissagu.send(ChPixelData(bitmap, true))
        }
    }
}
