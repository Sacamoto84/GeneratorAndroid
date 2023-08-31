package com.example.generator2.scope

import android.graphics.Bitmap
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.generator2.audio.Calculator
import com.example.generator2.util.format
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow

val scope = Scope()


class Scope {


    private val fps = MutableStateFlow(0.0)

    var scopeW: Float = 0f
    var scopeH: Float = 0f

    var scopeLissaguW: Float = 1f
    var scopeLissaguH: Float = 1f

    private var bitmap: Path? = null
    private var bitmapLissagu: Bitmap? = null

    val chPixel = Channel<Bitmap>(1, BufferOverflow.DROP_OLDEST)

    val chDataOutBitmap = Channel<Pair<Path, Path>>(1, BufferOverflow.DROP_OLDEST)
    val chLissaguBitmap = Channel<Bitmap>(1, BufferOverflow.DROP_OLDEST)

    var pairPoints: Bitmap = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)

    var startTime: Long = 0L
    var deltaTime: Long = 0L

    val calculator = Calculator(20)


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
                deltaTime = System.currentTimeMillis() - startTime
                //fps.value = 1f/(deltaTime.toFloat()/1000.0f)
                val v = 1.0 / (deltaTime.toDouble() / 1000.0)
                calculator.update(v)
                fps.value = calculator.getAvg()
                startTime = System.currentTimeMillis()
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

            Text(text = fps.collectAsState().value.toFloat().format(1), color = Color.White)

            Row {

                val textPaint = Paint().asFrameworkPaint().apply {
                    isAntiAlias = true
                    textSize = 20f
                    color = Color.Gray.toArgb()
                    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                }



                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .height(100.dp)
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val x = offset.x

                                if (x < scopeW/3)
                                {
                                    compressorCount.floatValue *= 2
                                }else
                                if (x > scopeW/3)
                                {
                                    compressorCount.floatValue /= 2
                                }
                                else
                                {
                                    println("Пауза")
                                }


                            }
                        }
                )
                {
                    update
                    scopeW = size.width
                    scopeH = size.height

                    if (!hiRes) {

                        val scaledWidth = pairPoints.width * 2
                        val scaledHeight = pairPoints.height * 2
                        val scaledBitmap: Bitmap =
                            Bitmap.createScaledBitmap(
                                pairPoints,
                                scaledWidth,
                                scaledHeight,
                                false
                            )
                        drawImage(image = scaledBitmap.asImageBitmap())
                    } else
                        drawImage(
                            image = pairPoints.asImageBitmap()
                        )

                    //Индекс компресии
                    drawIntoCanvas {
                        it.nativeCanvas.drawText(
                            compressorCount.floatValue.toString(),
                            4f,
                            24f,
                            textPaint
                        )
                    }

                }


//                Canvas(
//                    modifier = Modifier
//                        .width(100.dp)
//                        .height(100.dp)
//                )
//                {
//                    updateLissagu
//                    scopeLissaguW = size.width
//                    scopeLissaguH = size.height
//                    //bitmapLissagu?.let { drawImage(it.asImageBitmap()) }
//                }


            }
            OscilloscopeControl()

        }

    }


}