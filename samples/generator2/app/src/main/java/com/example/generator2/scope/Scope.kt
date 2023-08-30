package com.example.generator2.scope

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel

val scope = Scope()



class Scope {

    var scopeW: Float = 1f
    var scopeH: Float = 1f

    var scopeLissaguW: Float = 1f
    var scopeLissaguH: Float = 1f

    private var bitmap: Path? = null
    private var bitmapLissagu: Bitmap? = null

    val chPixel = Channel<Bitmap>(1, BufferOverflow.DROP_OLDEST)

    val chDataOutBitmap = Channel<Pair<Path, Path>>(1, BufferOverflow.DROP_OLDEST)
    val chLissaguBitmap = Channel<Bitmap>(1, BufferOverflow.DROP_OLDEST)

    var pairPoints : Bitmap = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)


    @Composable
    fun Oscilloscope() {



        var update by remember { mutableIntStateOf(0) }
        var updateLissagu by remember { mutableIntStateOf(0) }

        //var pairPoints: Pair<Path, Path> = Pair(Path(), Path())


        LaunchedEffect(key1 = true)
        {
            while (true) {
                //pairPoints = chDataOutBitmap.receive()
                pairPoints = chPixel.receive()
                update++

            }
        }

//        LaunchedEffect(key1 = true)
//        {
//            while (true) {
//                bitmapLissagu = chLissaguBitmap.receive()
//                updateLissagu++
//            }
//        }


        Column(
            modifier = Modifier
                .fillMaxWidth()
                //.height(100.dp)
                .background(Color(0xFF343633))
        )
        {

            Row {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .height(100.dp)
                )
                {
                    update
                    scopeW = size.width
                    scopeH = size.height

//                    val colorList: List<Color> = listOf(Color.Red, Color.Blue,
//                        Color.Magenta, Color.Yellow, Color.Green, Color.Cyan)
//
//                    val brush = Brush.horizontalGradient(
//                        colors = colorList,
//                        startX = 0f,
//                        endX = 300.dp.toPx(),
//                        tileMode = TileMode.Repeated
//                    )



//                    drawPoints(
//                        points = pairPoints.first,
//                        strokeWidth = 3f,
//                        pointMode = PointMode.Points,
//                        color = Color(0x40FFFFFF)
//                    )

//                    drawPath(
//                        color = Color.Green,
//                        path = pairPoints.first,
//                        style = Stroke(
//                            width = 2.dp.toPx(),
//                            //pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f))
//                        )
//                    )


//                    drawPath(
//                        color = Color.Red,
//                        path = pairPoints.second,
//                        style = Stroke(
//                            width = 2.dp.toPx(),
//                            //pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f))
//                        )
//                    )

                     drawImage(pairPoints.asImageBitmap())
                }

                Canvas(
                    modifier = Modifier
                        .width(100.dp)
                        .height(100.dp)
                )
                {
                    updateLissagu
                    scopeLissaguW = size.width
                    scopeLissaguH = size.height
                    //bitmapLissagu?.let { drawImage(it.asImageBitmap()) }
                }
            }
            OscilloscopeControl()

        }

    }


}