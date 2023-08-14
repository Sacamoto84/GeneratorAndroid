package com.example.generator2.mp3.wiget

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.example.generator2.mp3.channelDataOutBitmap
import com.example.generator2.mp3.channelDataOutPoints
import com.example.generator2.mp3.stream.oscilloscopeH
import com.example.generator2.mp3.stream.oscilloscopeW

var bitmap : Bitmap? = null

@Composable
fun Mp3Oscilloscope() {

    //val update = bufferQueueAudioProcessor.updateInput.collectAsState().value

    var update by remember { mutableIntStateOf(0) }

    var pairPoints : Pair< List<Offset>, List<Offset> > = Pair(emptyList(), emptyList())

    LaunchedEffect(key1 = true)
    {
        while (true) {
            //pairPoints = channelDataOutPoints.receive()
            //update++
            //println(">>>Update -> $update")

            bitmap = channelDataOutBitmap.receive()
            update++
        }
    }

    SideEffect {
        //println("update $update")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            //.height(100.dp)
            .background(Color(0xFF343633))
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

            bitmap?.let { drawImage(it.asImageBitmap()) }


//            drawPoints( //                brush = Brush.linearGradient(
//                //                    colors = listOf(Color.Red, Color.Yellow)
//                //                ),
//                color = Color.Green,
//                points = pairPoints.first,
//                cap = StrokeCap.Round,
//                pointMode = PointMode.Points,
//                strokeWidth = 3f
//            )
//
//            drawPoints( //                brush = Brush.linearGradient(
//                //                    colors = listOf(Color.Red, Color.Yellow)
//                //                ),
//                color = Color.Magenta,
//                points = pairPoints.second,
//                cap = StrokeCap.Round,
//                pointMode = PointMode.Points,
//                strokeWidth = 3f
//            )

        }

        OscilloscopeControl()

    }


}
