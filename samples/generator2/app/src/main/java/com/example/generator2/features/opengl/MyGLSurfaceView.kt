package com.example.generator2.features.opengl

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import timber.log.Timber

@SuppressLint("ViewConstructor")
class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {

    private var renderer : Renderer? = null //= MyGLRenderer()

    private var hasSetShader = false


    init {
        id = generateViewId()
    }

    init {
        setEGLContextClientVersion(3)
        preserveEGLContextOnPause = true
    }

    fun setShaderRenderer(
        renderer: Renderer
    ) {
        if (hasSetShader.not()) {
            setRenderer(
                renderer
            )
            this.renderer = renderer

            renderMode = RENDERMODE_WHEN_DIRTY
        }
        hasSetShader = true
    }


    fun onDestroy() {
        super.onDetachedFromWindow()
    }

    override fun onResume() {
        super.onResume()
        Timber.d("!!! MyGLSurfaceView onResume")
    }

    override fun onPause() {
        super.onPause()
        Timber.d("!!! MyGLSurfaceView onPause")
    }

}