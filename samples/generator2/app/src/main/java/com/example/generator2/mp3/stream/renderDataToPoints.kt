package com.example.generator2.mp3.stream

import androidx.compose.ui.geometry.Offset
import com.example.generator2.mp3.OSCILLSYNC
import com.example.generator2.mp3.channelDataOutPoints
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


@OptIn(DelicateCoroutinesApi::class)
fun renderDataToPoints() {
    GlobalScope.launch(Dispatchers.IO) {
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


            if (compressorCount.floatValue < 64) {
                for (x in 0 until w.toInt()) {
                    val mapX: Int = maping(x.toFloat(), 0f, w - 1f, 0f, (bufRN.size - 1f)).toInt()
                        .coerceIn(0, bufRN.size - 1)

                    val vR = bufRN[mapX].toFloat()
                    val vL = bufLN[mapX].toFloat()

                    val yR =
                        maping(vR, Short.MIN_VALUE.toFloat(), Short.MAX_VALUE.toFloat(), 0f, h - 1f)
                    val yL =
                        maping(vL, Short.MIN_VALUE.toFloat(), Short.MAX_VALUE.toFloat(), 0f, h - 1f)

                    outPointR.add(Offset(x.toFloat(), yR))
                    outPointL.add(Offset(x.toFloat(), yL))
                }
            }
            else
            {
                //Roll логика
                //Дано  bufRN bufLN
                val listIndex0 = mutableListOf<Int>() //Первый прогон, получение списка начальных индексов из основного буффера
                for (x in 0 until w.toInt()) {
                    listIndex0.add(maping(x.toFloat(), 0f, w - 1f, 0f, (bufRN.size - 1f)).toInt()
                        .coerceIn(0, bufRN.size - 1))
                }

                listIndex0



//                var a = 0
//                var b = 100
//                repeat(1000000)
//                {
//                    a++
//                    b++
//                }
//
//                val array = IntArray(2)
//                repeat(1000000)
//                {
//                    array[0] = array[0] + 1
//                    array[1] = array[1] + 1
//                }






            }


            val out: Pair<List<Offset>, List<Offset>> = Pair(outPointR.toList(), outPointL.toList())

            channelDataOutPoints.send(out)

        }
    }
}