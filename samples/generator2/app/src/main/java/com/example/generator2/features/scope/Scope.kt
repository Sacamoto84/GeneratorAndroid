package com.example.generator2.features.scope

import android.annotation.SuppressLint
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
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
import com.example.generator2.features.audio.AudioOut
import com.example.generator2.features.scope.compose.OscilloscopeControl
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.LinkedList

private val colorEnabled = Color.Black
private val colorTextDisabled = Color.DarkGray
private val m = Modifier
    .height(32.dp)
    .width(32.dp)
    .border(1.dp, Color.Gray)
    .background(Color.Black)

data class ChPixelData(val bitmap: Bitmap, val hiRes: Boolean, val fps: Float = 0f)

class Scope {

    var audioSampleRate = 44100


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


    val bitmapPool = BitmapPool(4)

    val floatArrayPool = FloatArrayPool(4)






    var isLissagu = MutableStateFlow(true)
    var scopeWLissagu: Float = 0f
    var scopeHLissagu: Float = 0f


    /** Приемный канал для кадра осцилографа */
    val inboxCanvasPixelData = Channel<ChPixelData>(1, BufferOverflow.DROP_OLDEST)

    /** Приемный канал для кадра лиссажу */
    val inboxLisagguPixelData = Channel<ChPixelData>(1, BufferOverflow.DROP_OLDEST)


    /** Приемный канал для кадра осцилографа */
    val inboxCanvasPixelDataFrames = Channel<Long>(1, BufferOverflow.DROP_OLDEST)


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


    /*
       1   |   26 ms  | 38.28 Hz |   1152 |   2304
       2   |   52 ms  | 19.14 Hz |   2304 |   4608
       4   |  104 ms  |  9.57 Hz |   4608 |   9216
       8   |  208 ms  |  4.78 Hz |   9216 |  18432
       16  |  418 ms  |  2.4  Hz |  18432 |  36864
       32  |  836 ms  |  1.2  Hz |  36864 |  73728
       64  |  1.64 s  |  0.6  Hz |  73728 | 147456
       128 |  3.34 s  |  0.3  Hz | 147456 | 294912
       256 |  6.68 s  |  0.15 Hz | 294912 | 589824

     */
    /** Количество пакетов в которое будет упакован выходной канал */
    val compressorCount = mutableFloatStateOf(1f)

    /** Выход аудиоданных -> compressor */
    val channelAudioOut = Channel<FloatArray>(capacity = 16, BufferOverflow.DROP_OLDEST)


    /** Выход аудиоданных -> compressor */
    val channelAudioOutLissagu = Channel<FloatArray>(capacity = 8, BufferOverflow.DROP_OLDEST)



    /** Сжатые данные после компрессора */
    val channelDataStreamOutCompressor = Channel<FloatArray>(capacity = Channel.RENDEZVOUS)

    /** Сжатые данные после компрессора */
    val channelDataStreamOutCompressorIndex = Channel<Long>(capacity = 3)


    init {

       dataCompressor(this)

     //  renderDataToPoints(this)

        //lissaguToBitmap(this)

    }






    @Composable
    fun Oscilloscope() {



//        LaunchedEffect(key1 = true)
//        {
//            while (true) {
//                if (isPause.value) {
//                    delay(1);continue
//                }
//                pairPointsLissagu = inboxLisagguPixelData.receive()
//                if (isPause.value) {
//                    delay(1);continue
//                }
//                updateLissagu++
//            }
//        }


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
//        if (isLissagu.collectAsState().value)
//            Canvas(
//                modifier = Modifier.size(100.dp)
//            )
//            {
//                updateLissagu
//                scopeWLissagu = size.width
//                scopeHLissagu = size.height
//                drawImage(
//                    image = pairPointsLissagu.bitmap.asImageBitmap()
//                )
//            }
    }




    val bitmapOscillIndex = MutableStateFlow(0L)


    @SuppressLint("SuspiciousIndentation")
    @Composable
    fun CanvasOscill(modifier: Modifier) {

//        var index by remember {
//            mutableIntStateOf(0)
//        }

//        LaunchedEffect(key1 = true)
//        {
//            while (true) {
//                if (isPause.value) {
//                    delay(1);continue
//                }
//
//                //pairPoints = inboxCanvasPixelData.receive()
//
//
//
//               val frames = inboxCanvasPixelDataFrames.receive() //Текущий кадр
//               index = bitmapPool.findFrameIndex(frames)
//
//                if (index == -1)
//                    continue
//
//                if (isPause.value) {
//                    delay(1);continue
//                }
//                update++
//            }
//        }


        val frames = bitmapPool.findFrameIndex(bitmapOscillIndex.collectAsState().value)

 if (frames == -1)
     return


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

//            if (!pairPoints.hiRes) {
//
//                val scaledWidth = pairPoints.bitmap.width * 2
//                val scaledHeight = pairPoints.bitmap.height * 2
//                val scaledBitmap: Bitmap =
//                    Bitmap.createScaledBitmap(
//                        bitmapPool.pool[index].bitmap,
//                        scaledWidth,
//                        scaledHeight,
//                        false
//                    )
//                drawImage(image = scaledBitmap.asImageBitmap())
//            } else

                drawImage(
                    image = bitmapPool.pool[frames].bitmap.asImageBitmap()//pairPoints.bitmap.asImageBitmap()
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



            drawIntoCanvas {
                it.nativeCanvas.drawText(
                    pairPoints.fps.toString(),
                    size.width / 2 - 40f,
                    40f,
                    textPaintPause
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