package com.example.generator2.mp3.stream

import android.graphics.Paint
import androidx.compose.ui.graphics.Path
import com.example.generator2.mp3.OSCILLSYNC
import com.example.generator2.mp3.Pt
import com.example.generator2.mp3.channelDataOutRoll
import com.example.generator2.mp3.channelDataStreamOutCompressor
import com.example.generator2.mp3.oscillSync
import com.example.generator2.scope.scope
import com.example.generator2.util.BufSplitFloat
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import libs.maping

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


val renderCompleteBitmap = MutableStateFlow(true)

@OptIn(DelicateCoroutinesApi::class)
fun renderDataToPoints() {

//    var bitmap1: Bitmap =
//        Bitmap.createBitmap(scope.scopeW.toInt(), scope.scopeH.toInt(), Bitmap.Config.RGB_565)
//    var bitmap2: Bitmap =
//        Bitmap.createBitmap(scope.scopeW.toInt(), scope.scopeH.toInt(), Bitmap.Config.RGB_565)
//    var bitmapBuffer = false

    var ArrayPtR = Array(0) { Pt(0f, 0f) }
    var ArrayPtL = Array(0) { Pt(0f, 0f) }


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
                        var last: Float = 0f
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
                        var last: Float = 0f
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
                        val (bufR, bufL) =  BufSplitFloat().split(buf)
                        bufLN = bufL
                        bufRN = bufR
                    } else {
                        //Режим Roll >= 64
                        val buf = channelDataOutRoll.receive()
                        if (buf.isEmpty()) continue
                        val (bufR, bufL) =  BufSplitFloat().split(buf)
                        bufLN = bufL
                        bufRN = bufR
                    }

                }

////////////////////////////////////////////////////////////////


                //val pathR = FloatArray(w.toInt()*4)
                //val pathL = FloatArray(w.toInt())

                if (ArrayPtR.size != w.toInt())
                    ArrayPtR = Array(w.toInt()) { Pt(0f, 0f) }

                val pathR = Path()

                if (ArrayPtL.size != w.toInt())
                    ArrayPtL = Array(w.toInt()) { Pt(0f, 0f) }

                val pathL = Path()

                var ptTemp = Pt(0f, 0f)


                var i = 0

                if (compressorCount.floatValue < 64) {

                    for (x in 0 until w.toInt()) {
                        val mapX: Int =
                            maping(x.toFloat(), 0f, w - 1f, 0f, (bufRN.size - 1f)).toInt()
                                .coerceIn(0, bufRN.size - 1)

                        val yR = maping(bufRN[mapX], -1f, 1f, 0f, h - 1f)
                        val yL = maping(bufLN[mapX], -1f, 1f, 0f, h - 1f)


                        ptTemp = Pt(x.toFloat(), yR)
                        ArrayPtR[x] = ptTemp
                        ptTemp = Pt(x.toFloat(), yL)
                        ArrayPtL[x] = ptTemp

                    }

                    pathR.moveTo(ArrayPtR[0].x, ArrayPtR[0].y)
                    pathL.moveTo(ArrayPtL[0].x, ArrayPtL[0].y)
                    for (i1 in 1 until ArrayPtR.size) {
                        pathR.lineTo(ArrayPtR[i1].x, ArrayPtR[i1].y)
                        pathL.lineTo(ArrayPtL[i1].x, ArrayPtL[i1].y)
                    }

                    // выводим результат




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



                val v = Pair(pathR, pathL)
                scope.chDataOutBitmap.send( v )

                    //scope.chDataOutBitmap.send(bitmap2)




            }
        }


    }.start()
}

