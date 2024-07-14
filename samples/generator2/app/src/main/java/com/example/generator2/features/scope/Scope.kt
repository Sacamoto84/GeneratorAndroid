package com.example.generator2.features.scope

import android.graphics.Typeface
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.generator2.features.opengl.MyGLSurfaceView
import com.example.generator2.features.scope.compose.OscilloscopeControl
import com.example.generator2.features.scope.opengl.render.GLShaderLissagu
import com.example.generator2.features.scope.opengl.render.GLShaderOscill
import com.example.generator2.features.scope.opengl.render.MyGLRendererLissagu
import com.example.generator2.features.scope.opengl.render.MyGLRendererOscill
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

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

    var isPause = MutableStateFlow(false)


    //val bitmapPool = BitmapPool(4)
    //val floatArrayPool = FloatArrayPool(4)

    //============== Lissagu ===================
    var isUseLissagu = MutableStateFlow(true)


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

    /** ## Выход аудиоданных -> compressor */
    val channelAudioOut = Channel<FloatArray>(capacity = 16, BufferOverflow.DROP_OLDEST)


    /** Сжатые данные после компрессора */
    val channelDataStreamOutCompressor = Channel<FloatArray>(capacity = Channel.RENDEZVOUS)

    /** Сжатые данные после компрессора */



    val deferredOscill = Channel<Int>(capacity = 1, BufferOverflow.DROP_OLDEST) //CompletableDeferred<Long>()
    val deferredLissagu = Channel<Int>(capacity = 1, BufferOverflow.DROP_OLDEST)

    init {

        println("!!! init Scope")

        dataCompressor(this)

        //renderDataToPoints(this)

        //lissaguToBitmap(this)

        //myGLSurfaceST =  getGLSurface(application)

    }

    var signalLevels =
        floatArrayOf(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f, 0.15f, 0.18f)

    var update: Int = 0

    @Suppress("NonSkippableComposable")
    @Composable
    fun Oscilloscope() {

        var scopeW by remember { mutableFloatStateOf(0f) }

        var view: MyGLSurfaceView? = remember { null }

        val shaderRenderer = remember {

            MyGLRendererOscill().apply {
//                setShaders(
//                    shader.fragmentShader,
//                    shader.vertexShader,
//                    "MainListing ${shader.title}"
//                )
            }

        }

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

        DisposableEffect(Unit) {
            view?.onResume()
            onDispose {
                println("!!! onDispose")
                view?.onPause()
                shaderRenderer?.deleteProgram()
                view?.onDestroy()
                view = null
            }
        }

        Column(modifier = Modifier.border(1.dp, color = Color.Gray)) {

            Row(modifier = Modifier.fillMaxWidth()) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .weight(1f)
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
                    contentAlignment = Alignment.TopStart,
                ) {
                    GLShaderOscill(renderer = shaderRenderer, update = { view = it })

                    Text(
                        text = compressorCount.floatValue.toString(),
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }

                PanelButton()

            }
            OscilloscopeControl()
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

        DisposableEffect(Unit) {
            view?.onResume()
            onDispose {
                println("!!! onDispose")
                view?.onPause()
                shaderRenderer.deleteProgram()
                view?.onDestroy()
                view = null
            }
        }

        GLShaderLissagu(renderer = shaderRenderer, update = { view = it }, modifier = Modifier.height(200.dp).width(200.dp))

    }


    @Suppress("NonSkippableComposable")
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
        ) {
            Column(
                modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween
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


