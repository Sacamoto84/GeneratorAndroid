package com.example.generator2.mp3.wiget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.example.generator2.mp3.bufferQueueAudioProcessor
import com.example.generator2.player



//position->modelPosition
fun map(
    x: Float, in_min: Float, in_max: Float, out_min: Float, out_max: Float
): Float {

    return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min

}


/**
 * Создать точки из signal для отображения
 */
fun createPoint(size: Size, buf : ShortArray, rl : String = "R"): MutableList<Offset> {

    val w = size.width
    val h = size.height

    val points = mutableListOf<Offset>()



    //signal[signal.lastIndex] = signal[signal.lastIndex - 1]
if (buf.isNotEmpty()) {
    val R = ShortArray(buf.size / 2)
    val L = ShortArray(buf.size / 2)

    var index1 = 0
    var index2 = 0

    for (i in buf.indices) {
        if (i % 2 == 0) {
            R[index1] = buf[i]
            index1++
        } else {
            L[index2] = buf[i]
            index2++
        }
    }

    var RL: ShortArray = R

    if (rl == "L")
        RL = L

    for (x in 0 until w.toInt()) {
        val mapX: Int =
            map(x.toFloat(), 0f, w - 1f, 0f, (RL.size - 1f)).toInt().coerceIn(0, RL.size - 1)
        val v = RL[mapX].toFloat()
        val y = map(v, Short.MIN_VALUE.toFloat(), Short.MAX_VALUE.toFloat(), 0f, h - 1f)
        points.add(Offset(x.toFloat(), y))
    }

}

    return points
}


//bufferQueueAudioProcessor

@Composable
fun Mp3Ooscilloscope() {

    val update = bufferQueueAudioProcessor.updateInput.collectAsState().value

    SideEffect {
        //println("update $update")
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(Color.Black)
    )
    {

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        )
        {

            update

            //println("update $update")

            val w = size.width
            val h = size.height

            val buf = bufferQueueAudioProcessor.dequeue()
            val sizeBuf = buf?.size
            println(sizeBuf)
            //2304

            if (buf != null) {
                val pointsR = createPoint(size, buf, "R")

                drawPoints( //                brush = Brush.linearGradient(
                    //                    colors = listOf(Color.Red, Color.Yellow)
                    //                ),
                    color = Color.Yellow,
                    points = pointsR,
                    cap = StrokeCap.Round,
                    pointMode = PointMode.Polygon,
                    strokeWidth = 3f
                )

                val pointsL = createPoint(size, buf, "L")

                drawPoints( //                brush = Brush.linearGradient(
                    //                    colors = listOf(Color.Red, Color.Yellow)
                    //                ),
                    color = Color.Magenta,
                    points = pointsL,
                    cap = StrokeCap.Round,
                    pointMode = PointMode.Polygon,
                    strokeWidth = 3f
                )

            }



            //val sizeBufRL = player.bufR.capacity() //Размер всего буфера
//
//            val pixelStep : Int = (sizeBufRL/w).toInt() //сколько буфера берется на пиксель
//
//            pixelStep
//
//
//            val pixelBuf = ShortArray(pixelStep)
//
//            if (sizeBufRL > 0)
//            for (i in 0 until pixelStep)
//            {
//                pixelBuf[i] = player.bufR.get(i)
//            }


        }

    }


}
