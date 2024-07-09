package com.example.generator2.features.scope.opengl.render

import android.opengl.GLSurfaceView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.generator2.features.opengl.MyGLSurfaceView
import timber.log.Timber

@Suppress("NonSkippableComposable")
@Composable
fun GLShaderLissagu(
    renderer: MyGLRendererLissagu,
    modifier: Modifier = Modifier,
    update: (MyGLSurfaceView?) -> Unit
) {

    var view: MyGLSurfaceView? = remember {  null }

    val lifeCycleState = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(key1 = lifeCycleState) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    view?.onResume()
                    renderer.onResume()
                }

                Lifecycle.Event.ON_PAUSE -> {
                    view?.onPause()
                    renderer.onPause()
                }

                else -> {
                }
            }
        }
        lifeCycleState.addObserver(observer)

        onDispose {
            Timber.d("View Disposed ${view.hashCode()}")
            lifeCycleState.removeObserver(observer)
            view?.onPause()
            view = null
        }
    }

    AndroidView(modifier = modifier,
        factory = { MyGLSurfaceView(it) }) { glSurfaceView ->
        view = glSurfaceView
        glSurfaceView.debugFlags = GLSurfaceView.DEBUG_CHECK_GL_ERROR or GLSurfaceView.DEBUG_LOG_GL_CALLS
        glSurfaceView.setShaderRenderer( renderer )
        update.invoke(view)
    }

}