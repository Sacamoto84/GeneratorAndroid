package com.example.generator2.features.scope

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.example.generator2.features.audio.BufSplitFloat
import com.example.generator2.features.mp3.OSCILLSYNC
import com.example.generator2.features.mp3.oscillSync
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.system.measureNanoTime


var hiRes: Boolean = false //Режим высокого разрешения


// Определение класса для хранения имени корутины в контексте
class CoroutineName(val name: String) : AbstractCoroutineContextElement(CoroutineName) {
    companion object Key : CoroutineContext.Key<CoroutineName>
}


@OptIn(DelicateCoroutinesApi::class)
fun renderDataToPoints(scope: Scope) {

    var canvas: Canvas

    val paintL = Paint()
    paintL.color = Color.YELLOW
    paintL.alpha = 0x10
    paintL.strokeWidth = 2f

    val paintR = Paint()
    paintR.color = Color.MAGENTA
    paintR.alpha = 0x10
    paintL.strokeWidth = 2f

    val paintLissagu = Paint()
    paintLissagu.color = Color.WHITE
    paintLissagu.alpha = 0xFF
    paintLissagu.strokeWidth = 2f


    paintL.style = Paint.Style.STROKE
    paintR.style = Paint.Style.STROKE
    paintLissagu.style = Paint.Style.STROKE


    var bigPointnL = FloatArray(1) { -1.0f }
    var bigPointnR = FloatArray(1) { -1.0f }


    var frame: Frame

    var buf: FloatArray


    val bufSplit0 = BufSplitFloat()
    val bufSplit1 = BufSplitFloat()

    var bufL: FloatArray
    var bufR: FloatArray

    var pairFlatArray: Pair<FloatArray, FloatArray>

    var sum = 0L
    var avg = 0L
    var sumNanoBitmap = 0L


    val nativeCanvas = NativeCanvas()

    /**
     * Содержит экземплят класса Scope
     */
    val nativeScopeLong = nativeCanvas.createNativeScope()



    GlobalScope.launch(Dispatchers.Default) {


        while (true) {

            delay(1)

            //yield()

            if (scope.isPause.value) {
                delay(100)
                continue
            }

            //Режим высокого разрешения
            //
            // hiRes = if (compressorCount.floatValue >= 32) true else true

            //hiRes = AudioSampleRate.value != 192000

            hiRes = true

            var w: Float = 100.0f
            var h: Float = 100.0f

//            if (hiRes) {
//                w = scope.scopeW
//                h = scope.scopeH
//            } else {
//                w = scope.scopeW / 2
//                h = scope.scopeH / 2
//            }


            val bufRN: FloatArray
            val bufLN: FloatArray

        ////    if ((w == 0f) or (h == 0f)) continue

            w  = 100.0f
            h  = 100.0f
            //

            frame = scope.bitmapPool.getBitmap(w.toInt(), h.toInt(), Bitmap.Config.ARGB_8888)

//            bitmap = Bitmap.createBitmap(
//                w.toInt(),
//                h.toInt(),
//                Bitmap.Config.ARGB_8888
//            )

            //canvas = Canvas()
            //canvas.setBitmap(frame.bitmap)


            var indexStartSignal = 0

            val frames = scope.channelDataStreamOutCompressorIndex.receive()

            val index = scope.floatArrayPool.findFrameIndex(frames)

            if (index == -1)
                continue

            buf = scope.floatArrayPool.pool[index].array



            //buf = scope.channelDataStreamOutCompressor.receive()

            if (buf.isEmpty()) {
                continue
            }

            if (scope.compressorCount.floatValue <= 8f) {

                if (buf.isEmpty()) continue
                val nano0 = measureNanoTime {
                    pairFlatArray = bufSplit0.split(buf)
                }

                val nano1 = measureNanoTime {
                    nativeCanvas.split(nativeScopeLong, buf, buf.size)
                }

                println("!!! kotlin ${nano0/1000} us | JNI ${nano1/1000} us ${nano0.toFloat()/nano1.toFloat()}X")


                bufR = pairFlatArray.first
                bufL = pairFlatArray.second

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
                //16..256
                // Для compressorCount > 8 нет синхронизации
                //buf = scope.channelDataStreamOutCompressor.receive()
                if (buf.isEmpty()) continue

//                val nano0 = measureNanoTime {
//                    pairFlatArray = bufSplit1.split(buf)//BufSplitFloat().split(buf)
//                }

                val nano1 = measureNanoTime {

                    repeat(1000) {
                        nativeCanvas.split(nativeScopeLong, buf, buf.size)
                    }
                }
                println("!!! JNI ${nano1/1000/1000} us")

                //println("!!! kotlin ${nano0/1000} us | JNI ${nano1/1000} us ${nano0.toFloat()/nano1.toFloat()}X")


                //bufR = pairFlatArray.first
                //bufL = pairFlatArray.second

                //bufLN = bufL
                //bufRN = bufR

            }

////////////////////////////////////////////////////////////////

            val pathL = Path()
            val pathR = Path()


            val drawLine: Boolean

            if (scope.compressorCount.floatValue >= 8f) {
                paintL.alpha = 0x60
                paintR.alpha = 0x60
                drawLine = false
            } else {
                paintL.alpha = 0xFF
                paintR.alpha = 0xFF
                drawLine = true
            }


            var mapX: Int = 1
            var offset: Int = 0


//            //32  45.8   48k
//            //16  22.81
//            //8   5.7
//            //4   2.86
//            //2   1.42
//            //1   0.71
//            //0.5 0.35
//            val maxPixelBuffer = (bufRN.size / w).coerceIn(1f, 96f)
//                .toInt() //Размер буфера для одного пикселя
//
//            if (!drawLine) {
//                val len = maxPixelBuffer * w.toInt() * 2
//                if (bigPointnL.size != len)
//                    bigPointnL = FloatArray(len)// { -1.0f }
//                else
//                    nativeCanvas.fillArrayWithZero(bigPointnL, bigPointnL.size)
//
//                if (bigPointnR.size != len)
//                    bigPointnR = FloatArray(len)// { -1.0f }
//                else
//                    nativeCanvas.fillArrayWithZero(bigPointnR, bigPointnR.size)
//            }


            var nanosBitmap = 0L

            val nanos = measureNanoTime {

                //Создаем координаты пикселей bufRN > bigPointR
//                nativeCanvas.jniCanvas(
//                    scope = nativeScopeLong,
//                    bigPointnL = bigPointnL,
//                    bigPointnR = bigPointnR,
//                    w = w.toInt(),
//                    h = h.toInt(),
//                    //maxPixelBuffer,
//                    isOneTwo = scope.isOneTwo.value,
//                    0, (w).toInt()
//                )


                //Рисуем битмап
//                nanosBitmap = measureNanoTime {
//                    nativeCanvas.jniCanvasBitmap(
//                        bigPointnL = bigPointnL,
//                        bigPointnR = bigPointnR,
//                        frame.bitmap,
//                        enableL = scope.isVisibleL.value,
//                        enableR = scope.isVisibleR.value,
//                        start = 0,
//                        length = bigPointnL.size
//                    )
//                }


//                for (x in 0 until w.toInt()) {
//
////                    mapX = maping(
////                        x.toFloat(),
////                        0f,
////                        temp2,
////                        0f,
////                        temp1.toFloat()
////                    ).toInt()//.coerceIn(0, bufRN.size - 1)
//
//
////                    for (i in 0 until maxPixelBuffer) {
////                        offset = (mapX + i)//.coerceAtMost(bufRN.size - 1)
////                        if (offset > temp1)
////                            offset = temp1
////                        pixelBufL[i] = bufLN[offset]
////                        pixelBufR[i] = bufRN[offset]
////                    }
//
//
//                    //}
//                    //println("t1: ${t11/1000} ns")
//
//
//                    mapX = (x * temp1.toFloat() / temp2).toInt()
//                    if (mapX < 0) mapX = 0
//                    if (mapX > temp1) mapX = temp1
//
//                    if (!drawLine) {
//                        }
//
//                    } else {
//
////                        //Рисуем линии
////                        if (x == 0) {
////                            pathL.moveTo(pixelBufL[0], maping(0f, -1f, 1f, minL, maxL))
                       //     pathR.moveTo(pixelBufR[0], maping(0f, -1f, 1f, minR, maxR))
////                        } else {
////
//////                            pathL.lineTo(
//////                                x.toFloat(),
//////                                maping(pixelBufL[0], -1f, 1f, minL, maxL)
//////                            )
//////
//////                            pathR.lineTo(
//////                                x.toFloat(),
//////                                maping(pixelBufR[0], -1f, 1f, minR, maxR)
//////                            )
////
////                        }
//                    }
//                }
//

//                if (drawLine) {
//                    val tt1 = measureNanoTime {
//                        if (scope.isVisibleR.value)
//                            canvas.drawPath(pathR, paintR)
//                    }
//                    val tt2 = measureNanoTime {
//                        if (scope.isVisibleL.value)
//                            canvas.drawPath(pathL, paintL)
//                    }
//                    println("tt1 ${tt1 / 1000} us")
//                    println("tt2 ${tt2 / 1000} us")
//
//                } else {
//                    //16..256 Roll
//
//
//                  if (scope.isVisibleR.value)
//                       canvas.drawPoints(bigPointnR, paintR)
////
//                    if (scope.isVisibleL.value)
//                        canvas.drawPoints(bigPointnL, paintL)
//
//                }


            }

            scope.bitmapOscillIndex.value = frame.frame

            sum += nanos / 1000
            sumNanoBitmap += nanosBitmap / 1000
            avg++

            println("!!! Рендер кадра: " + nanos / 1000 + " us" + " avg: ${sum / avg} us count: $avg,bitmap :${sumNanoBitmap / avg} us")

        }
    }

}


//@OptIn(DelicateCoroutinesApi::class)
//fun lissaguToBitmap(scope: Scope) {
//    val paintLissagu = Paint()
//    paintLissagu.color = Color.GREEN
//    paintLissagu.alpha = 0xFF
//    paintLissagu.strokeWidth = 1f
//    paintLissagu.style = Paint.Style.STROKE
//
//    GlobalScope.launch(Dispatchers.IO) {
//        while (true) {
//            val w: Float = scope.scopeWLissagu
//            val h: Float = scope.scopeHLissagu
//            if ((w == 0f) or (h == 0f)) continue
//            //val buf = channelDataStreamOutCompressorLissagu.receive()
//            //channelAudioOutLissagu
//
//            val buf = scope.channelAudioOutLissagu.receive()
//
//            if (buf.isEmpty()) continue
//            val (bufR, bufL) = BufSplitFloat().split(buf)
//            val bitmap: Bitmap = Bitmap.createBitmap(w.toInt(), h.toInt(), Bitmap.Config.RGB_565)
//            val canvas = Canvas(); canvas.setBitmap(bitmap)
//            //val bufLN = if (bufL.size >= 200) bufL.copyOf(200) else bufL
//            //val bufRN = if (bufL.size >= 200) bufR.copyOf(200) else bufR
//
//            val bufLN = bufL.copyOf(bufL.size / 4)
//            val bufRN = bufR.copyOf(bufL.size / 4)
//
//            val len = bufLN.size
//            val max = h - 1f;
//            val min = 0f
//
////            for (i in 0 until len) {
////                canvas.drawPoint(
////                    maping(bufLN[i], -1f, 1f, min, max),
////                    maping(bufRN[i], -1f, 1f, min, max),
////                    paintLissagu
////                )
////            }
//
//            val path = Path()
//            path.moveTo(maping(bufLN[0], -1f, 1f, min, max), maping(bufRN[0], -1f, 1f, min, max))
//            for (i in 1 until len) {
//                path.lineTo(
//                    maping(bufLN[i], -1f, 1f, min, max),
//                    maping(bufRN[i], -1f, 1f, min, max)
//                )
//            }
//            canvas.drawPath(path, paintLissagu)
//
//            scope.inboxLisagguPixelData.send(ChPixelData(bitmap, true))
//        }
//    }
//}
