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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.scope.compose.OscilloscopeControl
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow

private val colorEnabled = Color.Black
private val colorTextDisabled = Color.DarkGray
private val m = Modifier
    .height(32.dp)
    .width(32.dp)
    .border(1.dp, Color.Gray)
    .background(Color.Black)

data class ChPixelData(val bitmap: Bitmap, val hiRes: Boolean)

class Scope {

    /**
     * Используем компонент или нет
     */
    val isUse = MutableStateFlow(true)

    //Режимы отображения каналов на осцилографе
    val isVisibleL = MutableStateFlow(true) //Отобразить Левый канал
    val isVisibleR = MutableStateFlow(true) //Отобразить Правый канал
    val isOneTwo = MutableStateFlow(true)   //Комбинация двух каналов или раздельно

    var isPause = MutableStateFlow(false)

    var scopeW: Float = 0f
    var scopeH: Float = 0f


    var isLissagu = MutableStateFlow(true)
    var scopeWLissagu: Float = 0f
    var scopeHLissagu: Float = 0f


    val chPixel = Channel<ChPixelData>(1, BufferOverflow.DROP_OLDEST)

    val chPixelLissagu = Channel<ChPixelData>(1, BufferOverflow.DROP_OLDEST)


    private var pairPoints: ChPixelData =
        ChPixelData(Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888), true)

    private var pairPointsLissagu: ChPixelData =
        ChPixelData(Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888), true)

    var update by mutableIntStateOf(0)
    var updateLissagu by mutableIntStateOf(0)


    private val textPaint = Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        textSize = 20f
        color = Color.Gray.toArgb()
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
    }

    private val textPaintPause = Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        textSize = 40f
        color = Color.White.toArgb()
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
    }


    @Composable
    fun Oscilloscope() {

        LaunchedEffect(key1 = true)
        {
            while (true) {
                if (isPause.value) {
                    delay(1);continue
                }
                pairPoints = chPixel.receive()
                if (isPause.value) {
                    delay(1);continue
                }
                update++
            }
        }

        LaunchedEffect(key1 = true)
        {
            while (true) {
                if (isPause.value) {
                    delay(1);continue
                }
                pairPointsLissagu = chPixelLissagu.receive()
                if (isPause.value) {
                    delay(1);continue
                }
                updateLissagu++
            }
        }


        //if (isUse.collectAsState().value) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF343633))
                .border(1.dp, Color.White)
        )
        {
            Row {
                CanvasOscill(Modifier.weight(1f))
                PanelButton()
                CanvasLissagu()
            }
            OscilloscopeControl()
        }
        //}
    }

    @Composable
    fun CanvasLissagu() {
        if (isLissagu.collectAsState().value)
            Canvas(
                modifier = Modifier.size(100.dp)
            )
            {
                updateLissagu
                scopeWLissagu = size.width
                scopeHLissagu = size.height
                drawImage(
                    image = pairPointsLissagu.bitmap.asImageBitmap()
                )
            }
    }

    @Composable
    fun CanvasOscill(modifier: Modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .then(modifier)
                //.weight(1f)
                .height(100.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val x = offset.x
                        isPause.value = (x in scopeW / 3..scopeW * 2 / 3) xor isPause.value
                        compressorCount.floatValue = when {
                            x < scopeW / 3 -> {
                                isPause.value =
                                    false; (compressorCount.floatValue * 2).coerceAtMost(
                                    256f
                                )
                            }

                            x > scopeW * 2 / 3 -> {
                                isPause.value =
                                    false; (compressorCount.floatValue / 2).coerceAtLeast(
                                    0.125f
                                )
                            }

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
    }

    @Composable
    fun PanelButton() {

        val fontSize = 24.sp

        val stateIsVisibleL = isVisibleL.collectAsState().value
        val stateIsVisibleR = isVisibleR.collectAsState().value
        val stateIsOneTwo = isOneTwo.collectAsState().value

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

                Box(
                    modifier = m
                        .clickable(onClick = { isVisibleL.value = isVisibleL.value.not() })
                        .background(if (stateIsVisibleL) colorEnabled else Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "L",
                        color = if (stateIsVisibleL) Color.Yellow else colorTextDisabled,
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = m
                        .clickable(onClick = { isVisibleR.value = isVisibleR.value.not() })
                        .background(if (stateIsVisibleR) colorEnabled else Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "R",
                        color = if (stateIsVisibleR) Color.Magenta else colorTextDisabled,
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold
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
                        text = if (stateIsOneTwo) "•" else "••",
                        color = Color.White,
                        fontSize = fontSize
                    )
                }
            }
        }


    }

}