package com.example.generator2.mp3.wiget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.example.generator2.mp3.bufferQueueAudioProcessor
import com.example.generator2.mp3.stream.channelDataOutPoints
import com.example.generator2.mp3.stream.oscilloscopeH
import com.example.generator2.mp3.stream.oscilloscopeW
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel






@Composable
fun Mp3Oscilloscope(inData: Channel<ShortArray>) {

    //val update = bufferQueueAudioProcessor.updateInput.collectAsState().value

    var update by remember { mutableIntStateOf(0) }

    var pairPoints : Pair< List<Offset>, List<Offset> > = Pair(emptyList(), emptyList())

    LaunchedEffect(key1 = true)
    {
        while (true) {
            pairPoints = channelDataOutPoints.receive()
            update++
            println(">>>Update -> $update")
        }
    }

    SideEffect {
        //println("update $update")
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(Color(0xFF0E0E0E))
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

            oscilloscopeW = size.width
            oscilloscopeH = size.height

            //val buf = bufferQueueAudioProcessor.dequeue()


            //2304



            drawPoints( //                brush = Brush.linearGradient(
                //                    colors = listOf(Color.Red, Color.Yellow)
                //                ),
                color = Color.Green,
                points = pairPoints.first,
                cap = StrokeCap.Round,
                pointMode = PointMode.Polygon,
                strokeWidth = 3f
            )



            drawPoints( //                brush = Brush.linearGradient(
                //                    colors = listOf(Color.Red, Color.Yellow)
                //                ),
                color = Color.Magenta,
                points = pairPoints.second,
                cap = StrokeCap.Round,
                pointMode = PointMode.Polygon,
                strokeWidth = 3f
            )


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
