package com.example.generator2.features.scope


import android.graphics.Typeface
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.generator2.R
import com.example.generator2.features.opengl.MyGLSurfaceView
import com.example.generator2.features.scope.opengl.render.GLShaderLissagu
import com.example.generator2.features.scope.opengl.render.GLShaderOscill
import com.example.generator2.features.scope.opengl.render.MyGLRendererLissagu
import com.example.generator2.features.scope.opengl.render.MyGLRendererOscill
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class OSCILLSYNC {
    NONE, R, L
}

private val colorEnabled = Color.Black
private val colorTextDisabled = Color.DarkGray
private val m = Modifier
    .height(32.dp)
    .width(32.dp)
    .border(1.dp, Color.Gray)
    .background(Color.Black)

class Scope {

    var audioSampleRate = 44100

    /**
     * Используем компонент или нет
     */
    val isUse = MutableStateFlow(true)

    //Режимы отображения каналов на осцилографе
    val isVisibleL = MutableStateFlow(true) //Отобразить Левый канал
    val isVisibleR = MutableStateFlow(true) //Отобразить Правый канал
    val isOneTwo = MutableStateFlow(false)   //Комбинация двух каналов или раздельно

    val isPause = MutableStateFlow(false)


    //val bitmapPool = BitmapPool(4)
    //val floatArrayPool = FloatArrayPool(4)

    //============== Lissagu ===================
    val isUseLissagu = MutableStateFlow(true)


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
    val compressorCount = mutableFloatStateOf(256f)

    private fun compressonCountAdd() {
        compressorCount.floatValue = (compressorCount.floatValue * 2).coerceAtMost(256f)
    }

    private fun compressonCountDiv() {
        compressorCount.floatValue = (compressorCount.floatValue / 2.0f).coerceAtLeast(1f)
    }

    /** ## Выход аудиоданных -> compressor */
    val channelAudioOut = Channel<FloatArray>(capacity = 16, BufferOverflow.DROP_OLDEST)


    /** Сжатые данные после компрессора */
    val channelDataStreamOutCompressor = Channel<FloatArray>(capacity = Channel.RENDEZVOUS)


    /** Разрешение на обновление нового кадра осцилографа, признак того что нужно перерисовать */
    val enableOscill = MutableStateFlow(true)

    val enableLissagu = MutableStateFlow(true)

    val deferredOscill =
        Channel<Int>(capacity = 1, BufferOverflow.DROP_OLDEST) //CompletableDeferred<Long>()
    val deferredLissagu = Channel<Int>(capacity = 1, BufferOverflow.DROP_OLDEST)

    val oscillSync = mutableStateOf(OSCILLSYNC.L)


    init {
        println("!!! init Scope")
        dataCompressor()
    }


    private fun dataCompressor() {

        CoroutineScope(Dispatchers.IO).launch {

            while (true) {
                val buf = channelAudioOut.receive()

                //Передаем FFT порцию данных
                //Spectrogram.sentToFloatRingBufferFFT(buf, buf.size, scope.audioSampleRate)

                NativeFloatDirectBuffer.add(buf, buf.size, compressorCount.floatValue.toInt())

                if (enableOscill.value && isPause.value)
                    deferredOscill.send(0)

                if (enableLissagu.value && isPause.value)
                    deferredLissagu.send(0)
            }
        }
    }


    @Suppress("NonSkippableComposable")
    @Composable
    fun OscilloscopeCompose() {

        LazyColumn(
            userScrollEnabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(242.dp)
                .border(1.dp, Color.Gray)
        ) {

            item {
                Row {
                    Oscilloscope(modifier = Modifier.weight(1f))

                    if (isUseLissagu.collectAsState().value) {
                        Lissagu()
                    }
                }
            }

            item {
                Divider()
            }
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                    //.horizontalScroll(rememberScrollState())
                ) {
                    PanelButton()
                    //Spacer(modifier = Modifier.width(8.dp))
                    //OscilloscopeControl()

                }
            }
            item {
                Divider()
            }

        }


    }


    @Suppress("NonSkippableComposable")
    @Composable
    fun Oscilloscope(modifier: Modifier = Modifier) {

        var scopeW by remember { mutableFloatStateOf(0f) }
        var view: MyGLSurfaceView? = remember { null }
        val shaderRenderer = remember { MyGLRendererOscill() }

        LaunchedEffect(key1 = true) {
            withContext(Dispatchers.IO) {
                while (true) {
                    //delay(1)
                    deferredOscill.receive()//.await()//channelDataStreamOutCompressorIndex.receive()
                    shaderRenderer.updateVerticesDirect()
                    shaderRenderer.compressorCount = compressorCount.floatValue
                    shaderRenderer.bools[0] = if (isOneTwo.value) 1 else 0
                    shaderRenderer.bools[1] = if (isVisibleL.value) 1 else 0
                    shaderRenderer.bools[2] = if (isVisibleR.value) 1 else 0
                    view?.requestRender()
                }
            }
        }

        val lifecycle = LocalLifecycleOwner.current.lifecycle

        DisposableEffect(Unit) {
            view?.onResume()
            enableOscill.value = true

            val lifecycleObserver = ScreenLifecycleObserver(
                onPauseAction = {
                    println("!!! lifecycleObserver onPauseAction Oscilloscope()")
                    enableOscill.value = false
                },
                onResumeAction = {
                    println("!!! lifecycleObserver onResumeAction Oscilloscope()")
                    enableOscill.value = true
                }
            )

            lifecycle.addObserver(lifecycleObserver)

            onDispose {
                println("!!! onDispose Oscilloscope()")
                lifecycle.removeObserver(lifecycleObserver)
                enableOscill.value = false
                view?.onPause()
                shaderRenderer.deleteProgram()
                view?.onDestroy()
                view = null
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .then(modifier)
                .onGloballyPositioned { coordinates ->
                    scopeW = coordinates.size.width.toFloat()
                }
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
                },
            //contentAlignment = Alignment.TopStart,
        ) {
            GLShaderOscill(renderer = shaderRenderer, update = { view = it })

            Text(
                text = compressorCount.floatValue.toString(),
                color = Color.LightGray,
                fontSize = 12.sp
            )


        }

    }

    @Suppress("NonSkippableComposable")
    @Composable
    fun Lissagu() {

        var view: MyGLSurfaceView? = remember { null }
        val shaderRenderer = remember { MyGLRendererLissagu() }

        LaunchedEffect(key1 = true) {
            withContext(Dispatchers.IO) {
                while (true) {
                    deferredLissagu.receive()
                    shaderRenderer.updateVerticesDirect()
                    view?.requestRender()
                }
            }
        }

        val lifecycle = LocalLifecycleOwner.current.lifecycle

        DisposableEffect(Unit) {
            view?.onResume()
            enableLissagu.value = true

            val lifecycleObserver = ScreenLifecycleObserver(
                onPauseAction = {
                    println("!!! lifecycleObserver onPauseAction Oscilloscope()")
                    enableLissagu.value = false
                },
                onResumeAction = {
                    println("!!! lifecycleObserver onResumeAction Oscilloscope()")
                    enableLissagu.value = true
                }
            )

            lifecycle.addObserver(lifecycleObserver)

            onDispose {
                println("!!! onDispose Lissagu()")
                lifecycle.removeObserver(lifecycleObserver)
                enableLissagu.value = false
                view?.onPause()
                shaderRenderer.deleteProgram()
                view?.onDestroy()
                view = null
            }
        }

        GLShaderLissagu(
            renderer = shaderRenderer,
            update = { view = it },
            modifier = Modifier
                .height(100.dp)
                .width(100.dp)
        )

    }


    @Suppress("NonSkippableComposable")
    @Composable
    fun PanelButton() {

        val fontSize = 24.sp

        val stateIsVisibleL = isVisibleL.collectAsState().value
        val stateIsVisibleR = isVisibleR.collectAsState().value
        val stateIsOneTwo = isOneTwo.collectAsState().value

        Row(
            modifier = Modifier
                .height(40.dp)
                .fillMaxWidth()
                .background(Color.Cyan),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {


            Row {
                Box(
                    modifier = m
                        .clickable(onClick = { isVisibleL.value = isVisibleL.value.not() })
                        .border(1.dp, Color.Gray)
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
                        .border(1.dp, Color.Gray)
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
                        .border(1.dp, Color.Gray)
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


            if (isPause.collectAsState().value) {
                Text(
                    text = "Pause",
                    color = Color.Red,
                    fontSize = 24.sp
                )
            }


            Box(
                modifier = Modifier
                    .height(40.dp)
                    .width(64.dp)
                    .border(1.dp, Color.Gray)
                    .background(Color.Black)
                    .clickable {
                        isPause.value =  isPause.value.not()
                    }

                , contentAlignment = Alignment.Center
            ) {
                Text("Pause", color = Color.White)
            }


            //Spacer(modifier = Modifier.width(8.dp))


            Row {


                //Знак Плюс
                Box(
                    modifier = m
                        .clickable(onClick = { compressonCountAdd() })
                        .border(1.dp, Color.Gray)
                        .background(Color.Black)
                        .drawBehind {
                            drawLine(
                                Color.White,
                                start = Offset(size.width * 1 / 3f, size.height / 2f),
                                end = Offset(size.width * 2 / 3f, size.height / 2f),
                                strokeWidth = 3.dp.toPx()
                            )

                            drawLine(
                                Color.White,
                                start = Offset(size.width * 1 / 2f, size.height / 3f),
                                end = Offset(size.width * 1 / 2f, size.height * 2f / 3f),
                                strokeWidth = 3.dp.toPx()
                            )

                        })


                Text(
                    text = compressorCount.floatValue.toInt().toString(),
                    modifier = Modifier
                        .width(64.dp)
                        .height(40.dp)
                        //.border(1.dp, Color.Gray)
                        .wrapContentHeight(Alignment.CenterVertically)
                        .background(Color.Black),
                    color = Color.White,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center, fontFamily = FontFamily(Font(R.font.nunito))
                )

                //Знак минус
                Box(
                    modifier = m
                        .clickable(onClick = { compressonCountDiv() })
                        .border(1.dp, Color.Gray)
                        .background(Color.Black)
                        .drawBehind {
                            drawLine(
                                Color.White,
                                start = Offset(size.width * 1 / 3f, size.height / 2f),
                                end = Offset(size.width * 2 / 3f, size.height / 2f),
                                strokeWidth = 3.dp.toPx()
                            )
                        })
            }

/////////////////////////////////////// Кнопка лиссажу ///////////////////////////////////////
            Box(
                modifier = m
                    .clickable(onClick = { isUseLissagu.value = isUseLissagu.value.not() })
                    .border(1.dp, Color.Green)
                    .background(Color.Black)
                    .drawBehind {
                        // Размеры овала
                        val ovalWidth = size.height * 0.75f
                        val ovalHeight = ovalWidth * 0.45f

                        // Центр канвы
                        val canvasCenter = Offset(x = size.width / 2, y = size.height / 2)

                        // Верхний левый угол для центрирования овала
                        val topLeft = Offset(
                            x = canvasCenter.x - ovalWidth / 2,
                            y = canvasCenter.y - ovalHeight / 2
                        )

                        // Поворачиваем канву
                        rotate(degrees = -45f, pivot = canvasCenter) {
                            drawOval(
                                color = Color.White,
                                topLeft = topLeft,
                                size = Size(width = ovalWidth, height = ovalHeight),
                                style = Stroke(width = 1.dp.toPx())
                            )
                        }

                        drawLine(
                            Color.White,
                            start = Offset(size.width * 0.1f, size.height / 2f),
                            end = Offset(size.width * 0.9f, size.height / 2f),
                            strokeWidth = 1.dp.toPx()
                        )

                        drawLine(
                            Color.White,
                            start = Offset(size.width * 1 / 2f, size.height * 0.2f),
                            end = Offset(size.width * 1 / 2f, size.height * 0.8f),
                            strokeWidth = 1.dp.toPx()
                        )


                    }
            )
//////////////////////////////////////////////////////////////////////////////////////////////

        }


    }

    private val m = Modifier
        .height(40.dp)
        .width(40.dp)
//.border(1.dp, Color.Gray)
//.background(Color.Black)

    private val colorEnabled = Color.Black
    private val colorTextEnabled = Color.Green
    private val colorTextDisabled = Color.Gray

    @Suppress("NonSkippableComposable")
    @Composable
    fun OscilloscopeControl() {

        val a = 8.dp

        Row {

            Box(
                modifier = m
                    .clip(RoundedCornerShape(topStart = a, bottomStart = a))
                    .border(1.dp, Color.Gray, RoundedCornerShape(topStart = a, bottomStart = a))
                    .clickable(onClick = { oscillSync.value = OSCILLSYNC.NONE })
                    .background(if (oscillSync.value == OSCILLSYNC.NONE) colorEnabled else Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "N",
                    color = if (oscillSync.value == OSCILLSYNC.NONE) colorTextEnabled else colorTextDisabled
                )
            }

            Box(
                modifier = m
                    .border(1.dp, Color.Gray)
                    .clickable(onClick = { oscillSync.value = OSCILLSYNC.L })
                    .background(if (oscillSync.value == OSCILLSYNC.L) colorEnabled else Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "L",
                    color = if (oscillSync.value == OSCILLSYNC.L) colorTextEnabled else colorTextDisabled
                )
            }

            Box(
                modifier = m
                    .clip(RoundedCornerShape(topEnd = a, bottomEnd = a))
                    .border(1.dp, Color.Gray, RoundedCornerShape(topEnd = a, bottomEnd = a))
                    .clickable(onClick = { oscillSync.value = OSCILLSYNC.R })
                    .background(if (oscillSync.value == OSCILLSYNC.R) colorEnabled else Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "R",
                    color = if (oscillSync.value == OSCILLSYNC.R) colorTextEnabled else colorTextDisabled
                )
            }

        }
    }


}


class ScreenLifecycleObserver(
    private val onPauseAction: () -> Unit,
    private val onResumeAction: () -> Unit
) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        onPauseAction()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        onResumeAction()
    }
}


