package com.example.generator2.scope

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
    paintL.color = Color.GREEN
    paintL.alpha = 0x60
    paintL.strokeWidth = 2f

    val paintR = Paint()
    paintR.color = Color.RED
    paintR.alpha = 0x60
    paintL.strokeWidth = 2f

    Thread {

        GlobalScope.launch(Dispatchers.IO) {

            while (true) {

                val w = scope.scopeW
                val h = scope.scopeH

                val bufRN: FloatArray
                val bufLN: FloatArray

                if ((w == 1f) or (h == 1f)) continue

                val bitmap: Bitmap = Bitmap.createBitmap(
                    scope.scopeW.toInt(),
                    scope.scopeH.toInt(),
                    Bitmap.Config.RGB_565
                )

                canvas = Canvas()
                canvas.setBitmap(bitmap)

                delay(16)

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


                if (compressorCount.floatValue < 640) {

                    //val pointsListL = mutableListOf<Offset>()
                    //val pointsListR = mutableListOf<Offset>()

                    for (x in 0 until w.toInt()) {

                        //32  45.8   48k
                        //16  22.81
                        //8   5.7
                        //4   2.86
                        //2   1.42
                        //1   0.71
                        //0.5 0.35
                        var pixelBufSize = bufRN.size / w //Размер буфера для одного пикселя

                        Timber.w("pixelBufSize: $pixelBufSize")

                        if (pixelBufSize < 1f)
                            pixelBufSize = 1f

                        val pixelBufL = FloatArray(pixelBufSize.toInt())
                        val pixelBufR = FloatArray(pixelBufSize.toInt())

                        for (pixelI in 0 until pixelBufSize.toInt()) {
                            val mapX: Int =
                                maping(x.toFloat(), 0f, w - 1f, 0f, (bufRN.size - 1f)).toInt()
                                    .coerceIn(0, bufRN.size - 1)

                            val offset = (mapX + pixelI).coerceAtMost(bufRN.size - 1)
                            pixelBufL[pixelI] = bufLN[offset]
                            pixelBufR[pixelI] = bufRN[offset]

                            //var v = Offset(x.toFloat(), maping(pixelBufL[pixelI], -1f, 1f, 0f, h - 1f) )
                            //pointsListL.add(v)
                            //v = Offset(x.toFloat(),  maping(pixelBufR[pixelI], -1f, 1f, 0f, h - 1f))


                            //pointsListL.

                            //canvas.drawPoint( x.toFloat(), maping(pixelBufL[pixelI], -1f, 1f, 0f, h - 1f), paintL)



                        }



                        for (pixelI in 0 until pixelBufSize.toInt()) {
                            canvas.drawLine(
                                x.toFloat(),
                                maping(pixelBufL[pixelI], -1f, 1f, 0f, h - 1f),
                                (x + 1).toFloat(),
                                maping(pixelBufL[(pixelI + 1).coerceAtMost(pixelBufSize.toInt() - 1)], -1f, 1f, 0f, h - 1f),
                                paintL
                            )
                        }




                    }



                    scope.chPixel.send(bitmap)

                }


            }
        }


    }.start()
}

