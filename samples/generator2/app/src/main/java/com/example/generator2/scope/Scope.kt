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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.generator2.mp3.stream.renderCompleteBitmap
import com.example.generator2.mp3.wiget.OscilloscopeControl
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

    val chDataOutBitmap = Channel<Pair<Path, Path>>(1, BufferOverflow.DROP_OLDEST)
    val chLissaguBitmap = Channel<Bitmap>(1, BufferOverflow.DROP_OLDEST)


    @Composable
    fun Oscilloscope() {

        var update by remember { mutableIntStateOf(0) }
        var updateLissagu by remember { mutableIntStateOf(0) }

        var pairPoints: Pair<Path, Path> = Pair(Path(), Path())

        LaunchedEffect(key1 = true)
        {
            while (true) {
                pairPoints = chDataOutBitmap.receive()
                update++
            }
        }

        LaunchedEffect(key1 = true)
        {
            while (true) {
                bitmapLissagu = chLissaguBitmap.receive()
                updateLissagu++
            }
        }

        SideEffect {
            //println("update $update")
            renderCompleteBitmap.value = true
        }

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



                    drawPath(
                        color = Color.Green,
                        path = pairPoints.first,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            //pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f))
                        )
                    )


                    drawPath(
                        color = Color.Red,
                        path = pairPoints.second,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            //pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f))
                        )
                    )

                    //bitmap?.let { drawImage(it.asImageBitmap()) }
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