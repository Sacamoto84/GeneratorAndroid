package com.example.generator2.scope

import android.graphics.Bitmap
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.audio.Calculator
import com.example.generator2.mp3.OSCILLSYNC
import com.example.generator2.mp3.oscillSync
import com.example.generator2.util.format
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow

val scope = Scope()

private val colorEnabled = Color.Black
private val colorTextEnabled = Color.Green
private val colorTextDisabled = Color.Gray
private val m = Modifier
    .height(32.dp)
    .width(32.dp)
    .border(1.dp, Color.Gray)
    .background(Color.Black)

data class ChPixelData(val bitmap: Bitmap, val hiRes: Boolean)

class Scope {

    //Режимы отображения каналов на осцилографе
    val isVisibleL = MutableStateFlow(true) //Отобразить Левый канал
    val isVisibleR = MutableStateFlow(true) //Отобразить Правый канал
    val isOneTwo = MutableStateFlow(true)   //Комбинация двух каналов или раздельно

    var isPause = MutableStateFlow(false)

    private val fps = MutableStateFlow(0.0)

    var scopeW: Float = 0f
    var scopeH: Float = 0f

    val chPixel = Channel<ChPixelData>(1, BufferOverflow.DROP_OLDEST)


    var pairPoints: ChPixelData =
        ChPixelData(Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888), true)

    private var startTime: Long = 0L


    @Composable
    fun Oscilloscope() {

        var update by remember { mutableIntStateOf(0) }

        LaunchedEffect(key1 = true)
        {
            while (true) {
                //pairPoints = chDataOutBitmap.receive()
                pairPoints = chPixel.receive()
                update++
                val deltaTime = System.currentTimeMillis() - startTime
                val v = 1.0 / (deltaTime.toDouble() / 1000.0)
                //calculator.update(v)

                fps.value = v

                startTime = System.currentTimeMillis()
            }
        }



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

                val textPaintPause = Paint().asFrameworkPaint().apply {
                    isAntiAlias = true
                    textSize = 40f
                    color = Color.White.toArgb()
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
                                isPause.value = (x in scopeW / 3..scopeW * 2 / 3) xor isPause.value
                                compressorCount.floatValue = when {
                                    x < scopeW / 3     -> {isPause.value = false; (compressorCount.floatValue * 2).coerceAtMost(256f)}
                                    x > scopeW * 2 / 3 -> {isPause.value = false; (compressorCount.floatValue / 2).coerceAtLeast(0.125f)}
                                    else -> compressorCount.floatValue
                                }
                            }
                        }
                )
                {
                    update
                    scopeW = size.width
                    scopeH = size.height

                    if (!pairPoints.hiRes) {

                        val scaledWidth = pairPoints.bitmap.width * 2
                        val scaledHeight = pairPoints.bitmap.height * 2
                        val scaledBitmap: Bitmap =
                            Bitmap.createScaledBitmap(
                                pairPoints.bitmap,
                                scaledWidth,
                                scaledHeight,
                                false
                            )
                        drawImage(image = scaledBitmap.asImageBitmap())
                    } else
                        drawImage(
                            image = pairPoints.bitmap.asImageBitmap()
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

                    //fps
//                    drawIntoCanvas {
//                        it.nativeCanvas.drawText(
//                            "fps:"+fps.value.toFloat().format(0),
//                            size.width - 80f,
//                            24f,
//                            textPaint
//                        )
//                    }

                    if (isPause.value)
                        drawIntoCanvas {
                            it.nativeCanvas.drawText(
                                "Pause",
                                size.width / 2 - 40f,
                                40f,
                                textPaintPause
                            )
                        }

                }

                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(100.dp)
                )
                {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {

                        val stateIsVisibleL = isVisibleL.collectAsState().value
                        val stateIsVisibleR = isVisibleR.collectAsState().value
                        val stateIsOneTwo = isOneTwo.collectAsState().value


                        Box(
                            modifier = m
                                .clickable(onClick = { isVisibleL.value = isVisibleL.value.not() })
                                .background(if (stateIsVisibleL) colorEnabled else Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "1",
                                color = if (stateIsVisibleL) colorTextEnabled else colorTextDisabled
                            )
                        }
                        Box(
                            modifier = m
                                .clickable(onClick = { isVisibleR.value = isVisibleR.value.not() })
                                .background(if (stateIsVisibleR) colorEnabled else Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "2",
                                color = if (stateIsVisibleR) colorTextEnabled else colorTextDisabled
                            )
                        }
                        Box(
                            modifier = m
                                .clickable(onClick = { isOneTwo.value = isOneTwo.value.not() })
                                .background(if (stateIsOneTwo) colorEnabled else Color.Black)
                                .rotate(90f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (stateIsOneTwo) """••""".trimMargin() else "•",
                                color = Color.White,
                                fontSize = 24.sp
                            )
                        }
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