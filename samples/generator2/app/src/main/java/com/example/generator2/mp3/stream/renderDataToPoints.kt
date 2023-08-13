package com.example.generator2.mp3.stream

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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

            for (x in 0 until w.toInt()) {
                val mapX: Int = maping(x.toFloat(), 0f, w - 1f, 0f, (bufR.size - 1f)).toInt().coerceIn(0, bufR.size - 1)

                val vR = bufR[mapX].toFloat()
                val vL = bufL[mapX].toFloat()

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