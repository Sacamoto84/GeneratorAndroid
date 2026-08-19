package com.example.generator2.features.scope

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.generator2.features.opengl.MyGLSurfaceView
import com.example.generator2.features.scope.opengl.render.GLShaderLissagu
import com.example.generator2.features.scope.opengl.render.GLShaderOscill
import com.example.generator2.features.scope.opengl.render.MyGLRendererLissagu
import com.example.generator2.features.scope.opengl.render.MyGLRendererOscill
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import timber.log.Timber

/** Осциллограф, лиссажу и панель управления одним блоком. */
@Suppress("NonSkippableComposable")
@Composable
fun OscilloscopeCompose(scope: Scope) {

    LazyColumn(
        userScrollEnabled = false,
        modifier = Modifier
            .fillMaxWidth()
            .height(242.dp)
            .border(1.dp, Color.Gray)
    ) {

        item {
            Row {
                Oscilloscope(scope, modifier = Modifier.weight(1f))

                if (scope.isUseLissagu.collectAsStateWithLifecycle().value) {
                    Lissagu(scope)
                }
            }
        }

        item {
            Divider()
        }
        item {
            // Кнопок больше, чем влезает в ширину экрана, поэтому строка
            // прокручивается: иначе крайние просто обрезаются и до них
            // не добраться.
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                PanelButton(scope)
            }
        }
        item {
            Divider()
        }

    }

}

@Suppress("NonSkippableComposable")
@Composable
fun Oscilloscope(scope: Scope, modifier: Modifier = Modifier) {

    var scopeW by remember { mutableFloatStateOf(0f) }

    // Ссылка на GL-вью обязана пережить рекомпозицию. Обычная локальная var
    // пересоздаётся на каждой, а эффекты продолжают держать переменную первой
    // композиции: после пересоздания surface рендер дёргался бы у мёртвого вью.
    val view = remember { mutableStateOf<MyGLSurfaceView?>(null) }

    val shaderRenderer = remember { MyGLRendererOscill() }

    LaunchedEffect(key1 = true) {
        withContext(Dispatchers.IO) {
            while (isActive) {
                scope.deferredOscill.receive()
                shaderRenderer.compressorCount = scope.compressorCount.floatValue
                shaderRenderer.triggerSync = scope.isMonoOut()
                shaderRenderer.bools[0] = if (scope.isOneTwo.value) 1 else 0
                shaderRenderer.bools[1] = if (scope.isVisibleL.value) 1 else 0
                shaderRenderer.bools[2] = if (scope.isVisibleR.value) 1 else 0
                view.value?.requestRender()
            }
        }
    }

    // Рендер идёт непрерывно по vsync, поэтому паузу нельзя больше
    // держать на том, что requestRender() не зовут.
    shaderRenderer.isPaused = scope.isPause.collectAsStateWithLifecycle().value

    val lifecycle = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(Unit) {
        view.value?.onResume()
        scope.enableOscill.value = true

        val lifecycleObserver = ScreenLifecycleObserver(
            onPauseAction = {
                Timber.i("!!! lifecycleObserver onPauseAction Oscilloscope()")
                scope.enableOscill.value = false
            },
            onResumeAction = {
                Timber.i("!!! lifecycleObserver onResumeAction Oscilloscope()")
                scope.enableOscill.value = true
            }
        )

        lifecycle.addObserver(lifecycleObserver)

        onDispose {
            Timber.i("!!! onDispose Oscilloscope()")
            lifecycle.removeObserver(lifecycleObserver)
            scope.enableOscill.value = false
            view.value?.onPause()
            shaderRenderer.deleteProgram()
            view.value?.onDestroy()
            view.value = null
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
                // Экран поделён на три полосы: края меняют развёртку,
                // середина ставит на паузу.
                detectTapGestures { offset ->

                    val x = offset.x

                    scope.isPause.value =
                        (x in scopeW / 3..scopeW * 2 / 3) xor scope.isPause.value

                    when {
                        x < scopeW / 3 -> scope.compressorCountUp()
                        x > scopeW * 2 / 3 -> scope.compressorCountDown()
                    }
                }
            },
    ) {
        GLShaderOscill(renderer = shaderRenderer, update = { view.value = it })

        Text(
            text = scope.sweepLabel(scope.compressorCount.floatValue),
            color = Color.LightGray,
            fontSize = 12.sp
        )

    }

}

@Suppress("NonSkippableComposable")
@Composable
fun Lissagu(scope: Scope) {

    // См. комментарий в Oscilloscope(): ссылка должна пережить рекомпозицию
    val view = remember { mutableStateOf<MyGLSurfaceView?>(null) }

    val shaderRenderer = remember { MyGLRendererLissagu() }

    LaunchedEffect(key1 = true) {
        withContext(Dispatchers.IO) {
            while (isActive) {
                scope.deferredLissagu.receive()
                shaderRenderer.updateVerticesDirect()
                view.value?.requestRender()
            }
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(Unit) {
        view.value?.onResume()
        scope.enableLissagu.value = true

        val lifecycleObserver = ScreenLifecycleObserver(
            onPauseAction = {
                Timber.i("!!! lifecycleObserver onPauseAction Oscilloscope()")
                scope.enableLissagu.value = false
            },
            onResumeAction = {
                Timber.i("!!! lifecycleObserver onResumeAction Oscilloscope()")
                scope.enableLissagu.value = true
            }
        )

        lifecycle.addObserver(lifecycleObserver)

        onDispose {
            Timber.i("!!! onDispose Lissagu()")
            lifecycle.removeObserver(lifecycleObserver)
            scope.enableLissagu.value = false
            view.value?.onPause()
            shaderRenderer.deleteProgram()
            view.value?.onDestroy()
            view.value = null
        }
    }

    GLShaderLissagu(
        renderer = shaderRenderer,
        update = { view.value = it },
        modifier = Modifier
            .height(100.dp)
            .width(100.dp)
    )

}
