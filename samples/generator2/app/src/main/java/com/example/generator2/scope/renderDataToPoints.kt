package com.example.generator2.scope

import android.graphics.Paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import com.example.generator2.mp3.OSCILLSYNC
import com.example.generator2.mp3.Pt
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

//    var bitmap1: Bitmap =
//        Bitmap.createBitmap(scope.scopeW.toInt(), scope.scopeH.toInt(), Bitmap.Config.RGB_565)
//    var bitmap2: Bitmap =
//        Bitmap.createBitmap(scope.scopeW.toInt(), scope.scopeH.toInt(), Bitmap.Config.RGB_565)
//    var bitmapBuffer = false


    Thread {

        GlobalScope.launch(Dispatchers.IO) {

            while (true) {

                val w = scope.scopeW
                val h = scope.scopeH

                val bufRN: FloatArray
                val bufLN: FloatArray

                if ((w == 1f) or (h == 1f)) continue

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
                    if (compressorCount.floatValue < 64) {
                        val buf = channelDataStreamOutCompressor.receive()
                        if (buf.isEmpty()) continue
                        val (bufR, bufL) = BufSplitFloat().split(buf)
                        bufLN = bufL
                        bufRN = bufR
                    } else {
                        //Режим Roll >= 64
                        val buf = channelDataOutRoll.receive()
                        if (buf.isEmpty()) continue
                        val (bufR, bufL) = BufSplitFloat().split(buf)
                        bufLN = bufL
                        bufRN = bufR
                    }

                }

////////////////////////////////////////////////////////////////

                var i = 0

                if (compressorCount.floatValue < 64) {

                    val arrayPtR = Array(w.toInt()) { Pt(0f, 0f) }
                    val arrayPtL = Array(w.toInt()) { Pt(0f, 0f) }


                    val points = Offset(0f,0f)

                    val pointsListL = mutableListOf<Offset>()
                    val pointsListR = mutableListOf<Offset>()

                    val pathL = Path(); val pathR = Path()

                    for (x in 0 until w.toInt()) {

                        //32  45.8   48k
                        //16  22.81
                        //8   5.7
                        //4   2.86
                        //2   1.42
                        //1   0.71
                        //0.5 0.35
                        val pixelBufSize = bufRN.size/w //Размер буфера для одного пикселя

                        Timber.w("pixelBufSize: $pixelBufSize")


                        val pixelBufL = FloatArray(pixelBufSize.toInt())
                        val pixelBufR = FloatArray(pixelBufSize.toInt())

                        for (pixelI in 0 until pixelBufSize.toInt())
                        {
                            val mapX: Int =
                                maping(x.toFloat(), 0f, w - 1f, 0f, (bufRN.size - 1f)).toInt()
                                    .coerceIn(0, bufRN.size - 1)

                            val offset = (mapX + pixelI).coerceAtMost(bufRN.size - 1)
                            pixelBufL[pixelI] = bufLN[offset]
                            pixelBufR[pixelI] = bufRN[offset]

                            var v = Offset(x.toFloat(), maping(pixelBufL[pixelI], -1f, 1f, 0f, h - 1f) )
                            pointsListL.add(v)
                            v = Offset(x.toFloat(),  maping(pixelBufR[pixelI], -1f, 1f, 0f, h - 1f))
                            pointsListR.add(v)
                        }





                        val mapX: Int =
                            maping(x.toFloat(), 0f, w - 1f, 0f, (bufRN.size - 1f)).toInt()
                                .coerceIn(0, bufRN.size - 1)

                        val yR = maping(bufRN[mapX], -1f, 1f, 0f, h - 1f)
                        val yL = maping(bufLN[mapX], -1f, 1f, 0f, h - 1f)


                        arrayPtR[x] = Pt(x.toFloat(), yR)
                        arrayPtL[x] = Pt(x.toFloat(), yL)

                    }


                    pathL.moveTo(arrayPtL[0].x, arrayPtL[0].y); pathR.moveTo(arrayPtR[0].x, arrayPtR[0].y)
                    for (i1 in 1 until arrayPtR.size) {
                        pathR.lineTo(arrayPtR[i1].x, arrayPtR[i1].y)
                        pathL.lineTo(arrayPtL[i1].x, arrayPtL[i1].y)
                    }

                    // выводим результат
                    scope.chDataOutBitmap.send(Pair(pathR, pathL))

                    scope.chPixel.send(Pair(pointsListL, pointsListR))

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


                        val max =
                            bufRN.slice(listIndex0[i3] until listIndex0[i3 + 1]).max().toFloat()
                        val y = maping(max, -1f, 1f, 0f, h - 1f)
                        val y1 = maping(-max, -1f, 1f, 0f, h - 1f)

                        //outPointR.add(Offset(i.toFloat(), y))
                        //outPointL.add(Offset(i.toFloat(), y1))

                    }


                }


                //scope.chDataOutBitmap.send(bitmap2)


            }
        }


    }.start()
}

