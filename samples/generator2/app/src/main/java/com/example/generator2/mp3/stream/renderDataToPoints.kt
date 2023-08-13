package com.example.generator2.mp3.stream

import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import libs.maping


var oscilloscopeW : Float = 1f
var oscilloscopeH : Float = 1f


@OptIn(DelicateCoroutinesApi::class)
fun renderDataToPoints()
{
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

            val buf = channelDataStreamOutCompressor.receive()

            if (buf.isEmpty()) continue

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

            var indexStartSignal : Int = 0

            if (compressorCount.floatValue <= 8f ) {

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

                 bufLN = bufL.copyOfRange(indexStartSignal, (bufL.size-1)/2 + indexStartSignal)
                 bufRN = bufR.copyOfRange(indexStartSignal, (bufR.size-1)/2 + indexStartSignal)
            }
            else {
                // Для compressorCount > 8 нет синхронизации
                bufLN = bufL
                bufRN = bufR


                //Режим Roll >= 64


            }

            for (x in 0 until w.toInt()) {
                val mapX: Int = maping(x.toFloat(), 0f, w - 1f, 0f, (bufRN.size - 1f)).toInt().coerceIn(0, bufRN.size - 1)

                val vR = bufRN[mapX].toFloat()
                val vL = bufLN[mapX].toFloat()

                val yR = maping(vR, Short.MIN_VALUE.toFloat(), Short.MAX_VALUE.toFloat(), 0f, h - 1f)
                val yL = maping(vL, Short.MIN_VALUE.toFloat(), Short.MAX_VALUE.toFloat(), 0f, h - 1f)

                outPointR.add(Offset(x.toFloat(), yR))
                outPointL.add(Offset(x.toFloat(), yL))
            }


            val out : Pair< List<Offset>, List<Offset> >  = Pair(outPointR.toList() , outPointL.toList())

            channelDataOutPoints.send(out)

        }
    }
}